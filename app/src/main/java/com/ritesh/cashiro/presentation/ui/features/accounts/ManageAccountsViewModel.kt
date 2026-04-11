package com.ritesh.cashiro.presentation.ui.features.accounts

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ritesh.cashiro.data.database.entity.AccountBalanceEntity
import com.ritesh.cashiro.data.database.entity.CardEntity
import com.ritesh.cashiro.data.database.entity.CardType
import com.ritesh.cashiro.data.preferences.UserPreferencesRepository
import com.ritesh.cashiro.data.repository.AccountBalanceRepository
import com.ritesh.cashiro.data.repository.CardRepository
import com.ritesh.cashiro.data.repository.TransactionRepository
import com.ritesh.cashiro.utils.CurrencyFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.math.BigDecimal
import java.time.LocalDateTime
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.ritesh.cashiro.R
import com.ritesh.cashiro.utils.IconResolutionUtils
import androidx.core.content.edit

data class ManageAccountsUiState(
    val accounts: List<AccountBalanceEntity> = emptyList(),
    val hiddenAccounts: Set<String> = emptySet(),
    val balanceHistory: List<AccountBalanceEntity> = emptyList(),
    val linkedCards: Map<String, List<CardEntity>> = emptyMap(),
    val orphanedCards: List<CardEntity> = emptyList(),
    val isLoading: Boolean = false,
    val mainAccountKey: String? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

data class AccountFormState(
    val bankName: String = "",
    val accountLast4: String = "",
    val balance: String = "",
    val creditLimit: String = "",
    val accountType: AccountType = AccountType.SAVINGS,
    val iconResId: Int = 0,
    val iconName: String = "",
    val currency: String = "INR",
    val isValid: Boolean = false,
    val errorMessage: String? = null
)

enum class AccountType {
    SAVINGS,
    CURRENT,
    CREDIT,
    WALLET
}

@HiltViewModel
class ManageAccountsViewModel
@Inject
constructor(
    @ApplicationContext private val context: Context,
    private val accountBalanceRepository: AccountBalanceRepository,
    private val cardRepository: CardRepository,
    private val transactionRepository: TransactionRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val sharedPrefs = context.getSharedPreferences("account_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(ManageAccountsUiState())
    val uiState: StateFlow<ManageAccountsUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(AccountFormState())
    val formState: StateFlow<AccountFormState> = _formState.asStateFlow()

    init {
        loadAccounts()
        loadHiddenAccounts()
        loadMainAccount()
        loadCards()
        initializeDefaultWallet()
    }

    private fun initializeDefaultWallet() {
        viewModelScope.launch {
            val wallet = accountBalanceRepository.getLatestBalance("Cash", "wallet")
            if (wallet == null) {
                accountBalanceRepository.insertBalance(
                    AccountBalanceEntity(
                        bankName = "Cash",
                        accountLast4 = "wallet",
                        balance = BigDecimal.ZERO,
                        timestamp = LocalDateTime.now(),
                        sourceType = "MANUAL",
                        isWallet = true,
                        iconResId = R.drawable.type_finance_dollar_banknote,
                        iconName = "type_finance_dollar_banknote",
                        color = "#4CAF50"
                    )
                )
            }
        }
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            accountBalanceRepository.getAllLatestBalances().collect { accounts ->
                _uiState.update { it.copy(accounts = accounts) }
            }
        }
    }

    private fun loadCards() {
        viewModelScope.launch {
            cardRepository.getAllCards().collect { allCards ->
                // Group cards by linked account
                val linkedCardsMap = mutableMapOf<String, MutableList<CardEntity>>()
                val orphaned = mutableListOf<CardEntity>()

                for (card in allCards) {
                    when {
                        card.cardType == CardType.DEBIT && card.accountLast4 != null -> {
                            linkedCardsMap.getOrPut(card.accountLast4) { mutableListOf() }.add(card)
                        }
                        card.cardType == CardType.DEBIT && card.accountLast4 == null -> {
                            orphaned.add(card)
                        }
                    // Credit cards are not orphaned, they're standalone
                    }
                }

                _uiState.update { it.copy(linkedCards = linkedCardsMap, orphanedCards = orphaned) }
            }
        }
    }

    private fun loadHiddenAccounts() {
        val hidden = sharedPrefs.getStringSet("hidden_accounts", emptySet()) ?: emptySet()
        _uiState.update { it.copy(hiddenAccounts = hidden) }
    }

    private fun loadMainAccount() {
        val main = sharedPrefs.getString("main_account", null)
        _uiState.update { it.copy(mainAccountKey = main) }
    }

    fun setAsMainAccount(bankName: String, accountLast4: String) {
        val key = "${bankName}_${accountLast4}"
        sharedPrefs.edit { putString("main_account", key) }
        _uiState.update { it.copy(mainAccountKey = key, successMessage = "Main account set successfully") }
        viewModelScope.launch {
            // Persist this account's currency as the app-wide base currency
            val account = _uiState.value.accounts.find {
                it.bankName == bankName && it.accountLast4 == accountLast4
            }
            if (account != null) {
                userPreferencesRepository.updateBaseCurrency(account.currency)
            }
            delay(3000)
            _uiState.update { it.copy(successMessage = null) }
        }
    }

    fun updateBankName(name: String) {
        _formState.update {
            it.copy(bankName = name, isValid = validateForm(name, it.accountLast4, it.balance))
        }
    }

    fun updateAccountLast4(last4: String) {
        // Only allow 4 characters
        if (last4.length <= 4) {
            _formState.update {
                it.copy(
                    accountLast4 = last4,
                    isValid = validateForm(it.bankName, last4, it.balance)
                )
            }
        }
    }

    fun updateBalance(balance: String) {
        // Only allow valid numeric input
        if (balance.isEmpty() || balance.matches(Regex("^\\d*\\.?\\d*$"))) {
            _formState.update {
                it.copy(
                    balance = balance,
                    isValid = validateForm(it.bankName, it.accountLast4, balance)
                )
            }
        }
    }

    fun updateCreditLimit(limit: String) {
        // Only allow valid numeric input
        if (limit.isEmpty() || limit.matches(Regex("^\\d*\\.?\\d*$"))) {
            _formState.update { it.copy(creditLimit = limit) }
        }
    }

    fun updateAccountType(type: AccountType) {
        _formState.update { it.copy(accountType = type) }
    }

    fun updateIcon(iconName: String) {
        val iconResId = IconResolutionUtils.nameToResId(context, iconName)
        _formState.update { it.copy(iconResId = iconResId, iconName = iconName) }
    }

    fun updateCurrency(currency: String) {
        _formState.update { it.copy(currency = currency) }
    }

    private fun validateForm(bankName: String, last4: String, balance: String): Boolean {
        return bankName.isNotBlank() &&
                last4.length == 4 &&
                balance.isNotBlank() &&
                balance.toDoubleOrNull() != null
    }

    fun addAccount(
        bankName: String,
        balance: BigDecimal,
        accountLast4: String,
        iconResId: Int,
        iconName: String,
        colorHex: String,
        isCreditCard: Boolean = false,
        isWallet: Boolean = false,
        creditLimit: BigDecimal? = null,
        currency: String = "INR"
    ) {
        viewModelScope.launch {
            // Check for duplicates
            val existingAccount = accountBalanceRepository.getLatestBalance(bankName, accountLast4)

            if (existingAccount != null) {
                _uiState.update {
                    it.copy(
                            errorMessage = "Account with this name and last 4 digits already exists"
                    )
                }
                return@launch
            }

            accountBalanceRepository.insertBalance(
                AccountBalanceEntity(
                    bankName = bankName,
                    accountLast4 = accountLast4,
                    balance = balance,
                    creditLimit = creditLimit,
                    timestamp = LocalDateTime.now(),
                    isCreditCard = isCreditCard,
                    isWallet = isWallet,
                    iconResId = iconResId,
                    iconName = iconName,
                    currency = currency,
                    color = colorHex
                )
            )

            _uiState.update { it.copy(successMessage = "Account added successfully") }
            delay(3000)
            _uiState.update { it.copy(successMessage = null) }
        }
    }

    fun addAccount() {
        val state = _formState.value
        if (!state.isValid) return

        val creditLimit =
                if (state.accountType == AccountType.CREDIT && state.creditLimit.isNotBlank()) {
                    BigDecimal(state.creditLimit)
                } else null

        addAccount(
            bankName = state.bankName,
            balance = BigDecimal(state.balance),
            accountLast4 = state.accountLast4,
            iconResId = state.iconResId,
            iconName = state.iconName,
            colorHex = "#33B5E5", // Default or handle color
            isCreditCard = (state.accountType == AccountType.CREDIT),
            isWallet = (state.accountType == AccountType.WALLET),
            creditLimit = creditLimit,
            currency = state.currency
        )

        // Clear form
        _formState.value = AccountFormState()
    }

    fun updateAccountBalance(bankName: String, accountLast4: String, newBalance: BigDecimal) {
        viewModelScope.launch {
            // Get the latest balance to preserve credit limit
            val latestBalance = accountBalanceRepository.getLatestBalance(bankName, accountLast4)

            accountBalanceRepository.insertBalance(AccountBalanceEntity(
                bankName = bankName,
                accountLast4 = accountLast4,
                balance = newBalance,
                creditLimit = latestBalance?.creditLimit,
                timestamp = LocalDateTime.now(),
                iconResId = latestBalance?.iconResId ?: 0,
                iconName = latestBalance?.iconName ?: "",
                isWallet = latestBalance?.isWallet ?: false,
                color = latestBalance?.color ?: "#33B5E5"
            )
            )
        }
    }

    fun updateCreditCard(
            bankName: String,
            accountLast4: String,
            newBalance: BigDecimal,
            newLimit: BigDecimal
    ) {
        viewModelScope.launch {
            accountBalanceRepository.insertBalance(
                AccountBalanceEntity(
                    bankName = bankName,
                    accountLast4 = accountLast4,
                    balance = newBalance,
                    creditLimit = newLimit,
                    timestamp = LocalDateTime.now(),
                    isCreditCard = true,
                    iconName = "type_finance_credit_card",
                    color = "#E91E63"
                )
            )
        }
    }

    fun toggleAccountVisibility(bankName: String, accountLast4: String) {
        val key = "${bankName}_${accountLast4}"
        val hidden = _uiState.value.hiddenAccounts.toMutableSet()

        if (hidden.contains(key)) {
            hidden.remove(key)
        } else {
            hidden.add(key)
        }

        // Save to SharedPreferences
        sharedPrefs.edit().putStringSet("hidden_accounts", hidden).apply()

        // Update UI state
        _uiState.update { it.copy(hiddenAccounts = hidden) }
    }

    fun isAccountHidden(bankName: String, accountLast4: String): Boolean {
        val key = "${bankName}_${accountLast4}"
        return _uiState.value.hiddenAccounts.contains(key)
    }

    fun clearError() {
        _formState.update { it.copy(errorMessage = null) }
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun loadBalanceHistory(bankName: String, accountLast4: String) {
        viewModelScope.launch {
            val history =
                    accountBalanceRepository.getBalanceHistoryForAccount(bankName, accountLast4)
            _uiState.update { it.copy(balanceHistory = history) }
        }
    }

    fun deleteBalanceRecord(id: Long, bankName: String, accountLast4: String) {
        viewModelScope.launch {
            // Check if this is the only record
            val count = accountBalanceRepository.getBalanceCountForAccount(bankName, accountLast4)
            if (count > 1) {
                accountBalanceRepository.deleteBalanceById(id)
                // Reload history and accounts
                loadBalanceHistory(bankName, accountLast4)
                loadAccounts()
            }
        }
    }

    fun updateBalanceRecord(
            id: Long,
            newBalance: BigDecimal,
            bankName: String,
            accountLast4: String
    ) {
        viewModelScope.launch {
            accountBalanceRepository.updateBalanceById(id, newBalance)
            // Reload history and accounts
            loadBalanceHistory(bankName, accountLast4)
            loadAccounts()
        }
    }

    fun clearBalanceHistory() {
        _uiState.update { it.copy(balanceHistory = emptyList()) }
    }

    fun linkCardToAccount(cardId: Long, accountLast4: String) {
        viewModelScope.launch {
            try {
                Log.d(
                        "ManageAccountsViewModel",
                        "Starting to link card $cardId to account $accountLast4"
                )

                // Get card info first to know if there's a balance to copy
                val card = cardRepository.getCardById(cardId)
                val hasBalance = card?.lastBalance != null

                // Link the card to the account
                cardRepository.linkCardToAccount(cardId, accountLast4)

                // If card had a balance, copy it to the account
                if (card != null && hasBalance) {
                    try {
                        val insertedId =
                                accountBalanceRepository.insertBalanceUpdate(
                                        bankName = card.bankName,
                                        accountLast4 = accountLast4,
                                        balance = card.lastBalance!!,
                                        timestamp = card.lastBalanceDate ?: LocalDateTime.now(),
                                        smsSource = card.lastBalanceSource,
                                        sourceType = "CARD_LINK"
                                )
                        Log.d(
                                "ManageAccountsViewModel",
                                "Balance copied to account. Insert ID: $insertedId"
                        )

                        // Show success message with balance
                        val message =
                                "Card linked successfully. Balance updated to ${CurrencyFormatter.formatCurrency(card.lastBalance)}"
                        _uiState.update { it.copy(successMessage = message) }
                    } catch (e: Exception) {
                        Log.e(
                                "ManageAccountsViewModel",
                                "Failed to copy balance: ${e.message}",
                                e
                        )
                        // Still show success for linking, but note the balance issue
                        _uiState.update {
                            it.copy(
                                    successMessage =
                                            "Card linked successfully (balance update failed)"
                            )
                        }
                    }
                } else {
                    // No balance to copy, just show link success
                    _uiState.update { it.copy(successMessage = "Card linked successfully") }
                }

                // Clear message after delay
                delay(3000)
                _uiState.update { it.copy(successMessage = null) }

                loadCards()
                loadAccounts()
            } catch (e: Exception) {
                Log.e("ManageAccountsViewModel", "Failed to link card", e)
                _uiState.update { it.copy(errorMessage = "Failed to link card: ${e.message}") }
            }
        }
    }

    fun unlinkCard(cardId: Long) {
        viewModelScope.launch {
            cardRepository.unlinkCard(cardId)
            loadCards()
        }
    }

    fun deleteCard(cardId: Long) {
        viewModelScope.launch {
            try {
                Log.d("ManageAccountsViewModel", "Deleting card with ID: $cardId")
                cardRepository.deleteCard(cardId)
                _uiState.update { it.copy(successMessage = "Card deleted successfully") }

                // Clear message after delay
                delay(2000)
                _uiState.update { it.copy(successMessage = null) }

                loadCards()
            } catch (e: Exception) {
               Log.e("ManageAccountsViewModel", "Failed to delete card", e)
                _uiState.update { it.copy(errorMessage = "Failed to delete card: ${e.message}") }
            }
        }
    }

    fun setCardActive(cardId: Long, isActive: Boolean) {
        viewModelScope.launch {
            cardRepository.setCardActive(cardId, isActive)
            loadCards()
        }
    }

    fun deleteAccount(bankName: String, accountLast4: String) {
        viewModelScope.launch {
            try {
                // Unlink any cards linked to this account
                val linkedCards = _uiState.value.linkedCards[accountLast4] ?: emptyList()
                linkedCards.forEach { card -> cardRepository.unlinkCard(card.id) }

                // Delete all balance records for this account
                val deletedCount = accountBalanceRepository.deleteAccount(bankName, accountLast4)

                // Remove from hidden accounts if present
                val key = "${bankName}_${accountLast4}"
                val hidden = _uiState.value.hiddenAccounts.toMutableSet()
                hidden.remove(key)
                sharedPrefs.edit().putStringSet("hidden_accounts", hidden).apply()

                // Remove from main account if present
                if (_uiState.value.mainAccountKey == key) {
                    sharedPrefs.edit().remove("main_account").apply()
                }

                _uiState.update {
                    it.copy(
                            hiddenAccounts = hidden,
                            mainAccountKey = if (it.mainAccountKey == key) null else it.mainAccountKey,
                            successMessage =
                                    "Account deleted successfully ($deletedCount balance records removed)"
                    )
                }

                // Clear message after delay
                delay(3000)
                _uiState.update { it.copy(successMessage = null) }

                loadCards() // Reload cards to update UI
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to delete account: ${e.message}") }
            }
        }
    }

    fun editAccount(
            oldBankName: String,
            accountLast4: String,
            newBankName: String,
            newBalance: BigDecimal,
            newCreditLimit: BigDecimal?,
            isCreditCard: Boolean,
            isWallet: Boolean,
            newIconResId: Int,
            newIconName: String,
            newColorHex: String,
            newCurrency: String = "INR"
    ) {
        viewModelScope.launch {
            try {
                // Update bank name if changed
                if (newBankName != oldBankName) {
                    accountBalanceRepository.updateAccountBankName(
                        oldBankName,
                        accountLast4,
                        newBankName
                    )

                    // Update hidden accounts preference if bank name changed
                    val oldKey = "${oldBankName}_${accountLast4}"
                    val newKey = "${newBankName}_${accountLast4}"
                    val hidden = _uiState.value.hiddenAccounts.toMutableSet()
                    if (hidden.contains(oldKey)) {
                        hidden.remove(oldKey)
                        hidden.add(newKey)
                        sharedPrefs.edit { putStringSet("hidden_accounts", hidden) }
                        _uiState.update { it.copy(hiddenAccounts = hidden) }
                    }

                    // Update main account preference if bank name changed
                    if (_uiState.value.mainAccountKey == oldKey) {
                        sharedPrefs.edit { putString("main_account", newKey) }
                        _uiState.update { it.copy(mainAccountKey = newKey) }
                    }
                }

                // Insert new balance record with updated values
                accountBalanceRepository.insertBalance(
                    AccountBalanceEntity(
                        bankName = newBankName,
                        accountLast4 = accountLast4,
                        balance = newBalance,
                        creditLimit = newCreditLimit,
                        timestamp = LocalDateTime.now(),
                        isCreditCard = isCreditCard,
                        isWallet = isWallet,
                        sourceType = "MANUAL",
                        iconResId = newIconResId,
                        iconName = newIconName,
                        currency = newCurrency,
                        color = newColorHex
                    )
                )

                _uiState.update { it.copy(successMessage = "Account updated successfully") }


                delay(3000)
                _uiState.update { it.copy(successMessage = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to update account: ${e.message}") }
            }
        }
    }

    fun mergeAccounts(targetAccount: AccountBalanceEntity, sourceAccounts: List<AccountBalanceEntity>, newBalance: BigDecimal?
    ) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }

                //Reassign transactions
                sourceAccounts.forEach { source ->
                    transactionRepository.updateAccountForTransactions(
                        oldBankName = source.bankName,
                        oldAccountNumber = source.accountLast4,
                        newBankName = targetAccount.bankName,newAccountNumber = targetAccount.accountLast4
                    )
                }

                // Update target balance if requested
                if (newBalance != null) {
                    accountBalanceRepository.insertBalance(AccountBalanceEntity(
                        bankName = targetAccount.bankName,
                        accountLast4 = targetAccount.accountLast4,
                        balance = newBalance,
                        creditLimit = targetAccount.creditLimit,
                        timestamp = LocalDateTime.now(),
                        isCreditCard = targetAccount.isCreditCard,
                        sourceType = "MERGE",
                        iconResId = targetAccount.iconResId,
                        iconName = targetAccount.iconName,
                        color = targetAccount.color
                    )
                    )
                }

                // Delete source accounts
                sourceAccounts.forEach { source ->
                    // Unlink cards
                    val linkedCards = _uiState.value.linkedCards[source.accountLast4] ?: emptyList()
                    linkedCards.forEach { card -> cardRepository.unlinkCard(card.id) }

                    // Delete account balances
                    accountBalanceRepository.deleteAccount(source.bankName, source.accountLast4)
                }

                // Reload data
                loadAccounts()
                loadCards()

                _uiState.update {
                    it.copy(isLoading = false, successMessage = "Accounts merged successfully")
                }
                delay(3000)
                _uiState.update { it.copy(successMessage = null) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to merge accounts: ${e.message}"
                    )
                }
            }
        }
    }
}

