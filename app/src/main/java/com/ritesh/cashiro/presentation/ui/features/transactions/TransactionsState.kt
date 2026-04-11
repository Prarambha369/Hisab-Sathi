package com.ritesh.cashiro.presentation.ui.features.transactions

import com.ritesh.cashiro.data.database.entity.TransactionEntity
import com.ritesh.cashiro.presentation.common.TimePeriod
import com.ritesh.cashiro.presentation.common.TransactionTypeFilter
import java.math.BigDecimal

data class TransactionsUiState(
    val transactions: List<TransactionEntity> = emptyList(),
    val groupedTransactions: Map<DateGroup, List<TransactionEntity>> = emptyMap(),
    val convertedAmounts: Map<Long, BigDecimal> = emptyMap(),
    val isLoading: Boolean = true
)

data class FilterParams(
    val query: String,
    val period: TimePeriod,
    val category: String?,
    val typeFilter: TransactionTypeFilter
)

enum class DateGroup(val label: String) {
    TODAY("Today"),
    YESTERDAY("Yesterday"),
    THIS_WEEK("This Week"),
    EARLIER("Earlier")
}

enum class SortOption(val label: String) {
    DATE_NEWEST("Newest First"),
    DATE_OLDEST("Oldest First"),
    AMOUNT_HIGHEST("Highest Amount"),
    AMOUNT_LOWEST("Lowest Amount"),
    MERCHANT_AZ("Merchant (A-Z)"),
    MERCHANT_ZA("Merchant (Z-A)")
}

data class FilteredTotals(
    val income: BigDecimal = BigDecimal.ZERO,
    val expenses: BigDecimal = BigDecimal.ZERO,
    val credit: BigDecimal = BigDecimal.ZERO,
    val transfer: BigDecimal = BigDecimal.ZERO,
    val investment: BigDecimal = BigDecimal.ZERO,
    val netBalance: BigDecimal = BigDecimal.ZERO,
    val transactionCount: Int = 0
)
