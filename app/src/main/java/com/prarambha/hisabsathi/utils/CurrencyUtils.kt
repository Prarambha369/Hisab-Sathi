package com.prarambha.hisabsathi.utils

import com.prarambha.hisabsathi.core.Constants
import java.math.BigDecimal

/**
 * Utility functions for formatting amounts in the app's base currency (NPR).
 */
object CurrencyUtils {

    /**
     * Formats a BigDecimal amount as Nepalese Rupees.
     * @return Formatted string like "रु 1,234" or "रु 1,23,456.5"
     */
    fun formatCurrency(amount: BigDecimal): String =
        if (amount.stripTrailingZeros().scale() > 0) {
            CurrencyFormatter.formatNpr(amount, minFraction = 1, maxFraction = 2)
        } else {
            CurrencyFormatter.formatNpr(amount, minFraction = 0, maxFraction = 0)
        }

    /** Formats a Double amount as Nepalese Rupees. */
    fun formatCurrency(amount: Double): String = formatCurrency(BigDecimal.valueOf(amount))

    /** Formats an Int amount as Nepalese Rupees. */
    fun formatCurrency(amount: Int): String = formatCurrency(BigDecimal(amount))

    /** Formats an NPR amount with a fixed number of decimal places. */
    fun formatCurrency(amount: BigDecimal, decimalPlaces: Int): String =
        CurrencyFormatter.formatNpr(amount, minFraction = decimalPlaces, maxFraction = decimalPlaces)

    /**
     * Sorts currency codes with the base currency (NPR) first, then alphabetically.
     * This is the standard sorting for currency lists throughout the app.
     *
     * Example: `sortCurrencies(listOf("USD", "INR", "NPR"))` returns `["NPR", "INR", "USD"]`.
     */
    fun sortCurrencies(currencies: List<String>): List<String> =
        currencies.sortedWith { a, b ->
            when {
                a == b -> 0
                a == Constants.DEFAULT_CURRENCY -> -1
                b == Constants.DEFAULT_CURRENCY -> 1
                else -> a.compareTo(b)
            }
        }
}
