package com.prarambha.hisabsathi.presentation.common

import java.time.LocalDate
import java.time.YearMonth

enum class TimePeriod(val label: String) {
    THIS_MONTH("This Month"),
    LAST_MONTH("Last Month"),
    CURRENT_FY("Current FY"),
    ALL("All Time"),
    CUSTOM("Custom Range")
}

enum class TransactionTypeFilter(val label: String) {
    ALL("All"),
    INCOME("Income"),
    EXPENSE("Expense"),
    CREDIT("Credit"),
    TRANSFER("Transfer"),
    INVESTMENT("Investment")
}

fun getDateRangeForPeriod(period: TimePeriod): Pair<LocalDate, LocalDate>? {
    val today = LocalDate.now()
    return when (period) {
        TimePeriod.THIS_MONTH -> {
            val start = YearMonth.now().atDay(1)
            start to today
        }
        TimePeriod.LAST_MONTH -> {
            val lastMonth = YearMonth.now().minusMonths(1)
            val start = lastMonth.atDay(1)
            val end = lastMonth.atEndOfMonth()
            start to end
        }
        TimePeriod.CURRENT_FY -> {
            nepalFiscalYearStart(today) to today
        }
        TimePeriod.ALL -> {
            // Use a reasonable date range for "All Time" - 10 years back to today
            val start = today.minusYears(10)
            start to today
        }
        TimePeriod.CUSTOM -> {
            // Custom range is handled separately in ViewModel
            null
        }
    }
}

/**
 * Start of Nepal's fiscal year (Shrawan 1 BS) containing [date].
 *
 * Shrawan 1 falls on 16 or 17 July in the Gregorian calendar depending on the year.
 * The app has no Bikram Sambat calendar, so this uses 16 July as an approximation;
 * the only effect is that "Current FY" may include one extra day at its start.
 */
internal fun nepalFiscalYearStart(date: LocalDate): LocalDate {
    val thisYearStart = LocalDate.of(date.year, 7, 16)
    return if (!date.isBefore(thisYearStart)) thisYearStart else thisYearStart.minusYears(1)
}
