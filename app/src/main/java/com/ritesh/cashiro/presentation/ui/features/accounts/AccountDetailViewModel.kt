package com.ritesh.cashiro.presentation.ui.features.accounts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ritesh.cashiro.data.currency.CurrencyConversionService
import com.ritesh.cashiro.data.database.entity.TransactionType
import com.ritesh.cashiro.data.repository.AccountBalanceRepository
import com.ritesh.cashiro.data.repository.CategoryRepository
import com.ritesh.cashiro.data.repository.SubcategoryRepository
import com.ritesh.cashiro.data.repository.TransactionRepository
import com.ritesh.cashiro.data.repository.CurrencyRepository
import com.ritesh.cashiro.presentation.ui.components.BalancePoint
import com.ritesh.cashiro.utils.CurrencyFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDateTime
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AccountDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val transactionRepository: TransactionRepository,
    private val accountBalanceRepository: AccountBalanceRepository,
    private val categoryRepository: CategoryRepository,
    private val subcategoryRepository: SubcategoryRepository,
    private val currencyRepository: CurrencyRepository,
    private val currencyConversionService: CurrencyConversionService
) : ViewModel() {
    
    private val bankName: String = savedStateHandle.get<String>("bankName") ?: ""
    private val accountLast4: String = savedStateHandle.get<String>("accountLast4") ?: ""
    
    private val _uiState = MutableStateFlow(AccountDetailUiState())
    val uiState: StateFlow<AccountDetailUiState> = _uiState.asStateFlow()

    val categoriesMap = categoryRepository.getAllCategories()
        .map { cats -> cats.associateBy { it.name } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val subcategoriesMap = subcategoryRepository.getAllSubcategories()
        .map { subcats -> subcats.associateBy { it.name } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())
    
    private val _selectedDateRange = MutableStateFlow(DateRange.LAST_30_DAYS)
    val selectedDateRange: StateFlow<DateRange> = _selectedDateRange.asStateFlow()
    
    init {
        loadAccountData()
        observeTransactions()
        observeBalanceHistory()
        observeBalanceChartData()
    }
    
    private fun loadAccountData() {
        _uiState.update { it.copy(
            bankName = bankName,
            accountLast4 = accountLast4,
            isLoading = true
        ) }
    }
    
    private fun observeTransactions() {
        viewModelScope.launch {
            combine(
                selectedDateRange,
                transactionRepository.getTransactionsByAccount(bankName, accountLast4),
                currencyRepository.baseCurrencyCode
            ) { dateRange, allTransactions, mainCurrency ->
                val (startDate, endDate) = getDateRangeValues(dateRange)

                val filteredTransactions = if (dateRange == DateRange.ALL_TIME) {
                    allTransactions
                } else {
                    allTransactions.filter { transaction ->
                        transaction.dateTime.isAfter(startDate) &&
                        transaction.dateTime.isBefore(endDate)
                    }
                }

                val accountPrimaryCurrency = getPrimaryCurrencyForAccount(bankName)
                val hasMultipleCurrencies = filteredTransactions
                    .map { it.currency }
                    .distinct()
                    .size > 1

                // Refresh exchange rates if we have multiple currencies
                if (hasMultipleCurrencies) {
                    val accountCurrencies = filteredTransactions.map { it.currency }.distinct()
                    currencyConversionService.refreshExchangeRatesForAccount(accountCurrencies)
                }

                // Calculate total income and expenses with account primary currency conversion
                var totalIncome = BigDecimal.ZERO
                var totalExpenses = BigDecimal.ZERO

                filteredTransactions.forEach { transaction ->
                    val convertedAmount = if (transaction.currency != accountPrimaryCurrency) {
                        currencyConversionService.convertAmount(
                            amount = transaction.amount,
                            fromCurrency = transaction.currency,
                            toCurrency = accountPrimaryCurrency
                        ) ?: transaction.amount
                    } else {
                        transaction.amount
                    }

                    if (transaction.transactionType == TransactionType.INCOME) {
                        totalIncome += convertedAmount
                    } else {
                        totalExpenses += convertedAmount
                    }
                }

                // Calculate converted amounts for the UI (TransactionItem) based on Main App Currency
                val converted = filteredTransactions
                    .filter { it.currency != mainCurrency }
                    .associate { tx ->
                        tx.id to (currencyConversionService.convertAmount(tx.amount, tx.currency, mainCurrency) ?: tx.amount)
                    }

                _uiState.update { state ->
                    state.copy(
                        transactions = filteredTransactions,
                        totalIncome = totalIncome,
                        totalExpenses = totalExpenses,
                        netBalance = totalIncome - totalExpenses,
                        primaryCurrency = accountPrimaryCurrency,
                        baseCurrency = mainCurrency,
                        hasMultipleCurrencies = hasMultipleCurrencies,
                        convertedAmounts = converted,
                        isLoading = false
                    )
                }
            }.collectLatest { }
        }
    }
    
    private fun observeBalanceHistory() {
        viewModelScope.launch {
            accountBalanceRepository.getLatestBalanceFlow(bankName, accountLast4)
                .collect { latestBalance ->
                    _uiState.update { state ->
                        state.copy(currentBalance = latestBalance)
                    }
                }
        }
        
        viewModelScope.launch {
            selectedDateRange.flatMapLatest { dateRange ->
                val (startDate, endDate) = getDateRangeValues(dateRange)
                accountBalanceRepository.getBalanceHistory(
                    bankName, 
                    accountLast4,
                    startDate,
                    endDate
                )
            }.collect { balanceHistory ->
                _uiState.update { state ->
                    state.copy(balanceHistory = balanceHistory)
                }
            }
        }
    }
    
    private fun observeBalanceChartData() {
        viewModelScope.launch {
            selectedDateRange.flatMapLatest { dateRange ->
                val (startDate, endDate) = getDateRangeValues(dateRange)


                val chartStartDate = when (dateRange) {
                    DateRange.LAST_7_DAYS -> endDate.minusDays(14)
                    DateRange.LAST_30_DAYS -> endDate.minusMonths(2)
                    DateRange.LAST_3_MONTHS -> endDate.minusMonths(4)
                    DateRange.LAST_6_MONTHS -> endDate.minusMonths(8)
                    DateRange.LAST_YEAR -> endDate.minusMonths(15)
                    DateRange.ALL_TIME -> LocalDateTime.of(2000, 1, 1, 0, 0)
                }

                accountBalanceRepository.getBalanceHistory(
                    bankName,
                    accountLast4,
                    chartStartDate,
                    endDate
                )
            }.collect { balanceHistory ->

                val chartData = balanceHistory.map { entity ->
                    BalancePoint(
                        timestamp = entity.timestamp,
                        balance = entity.balance,
                        currency = entity.currency
                    )
                }

                _uiState.update { state ->
                    state.copy(balanceChartData = chartData)
                }
            }
        }
    }
    
    fun selectDateRange(dateRange: DateRange) {
        _selectedDateRange.value = dateRange
    }
    
    private fun getDateRangeValues(dateRange: DateRange): Pair<LocalDateTime, LocalDateTime> {
        val endDate = LocalDateTime.now()
        val startDate = when (dateRange) {
            DateRange.LAST_7_DAYS -> endDate.minusDays(7)
            DateRange.LAST_30_DAYS -> endDate.minusDays(30)
            DateRange.LAST_3_MONTHS -> endDate.minusMonths(3)
            DateRange.LAST_6_MONTHS -> endDate.minusMonths(6)
            DateRange.LAST_YEAR -> endDate.minusYears(1)
            DateRange.ALL_TIME -> LocalDateTime.of(2000, 1, 1, 0, 0)
        }
        return startDate to endDate
    }

    private fun getPrimaryCurrencyForAccount(bankName: String): String {
        return CurrencyFormatter.getBankBaseCurrency(bankName)
    }
}