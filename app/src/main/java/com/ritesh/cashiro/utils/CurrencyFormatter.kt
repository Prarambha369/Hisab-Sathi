package com.ritesh.cashiro.utils

import com.ritesh.cashiro.data.currency.model.CurrencySymbols
import com.ritesh.parser.core.bank.BankParserFactory
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/**
 * Utility class for formatting currency values
 */
object CurrencyFormatter {

    private val INDIAN_LOCALE = Locale("en", "IN")

    /**
     * Locale mapping for different currencies
     */
    private val CURRENCY_LOCALES = mapOf(
        "INR" to INDIAN_LOCALE,
        "USD" to Locale.US,
        "EUR" to Locale.GERMANY,
        "GBP" to Locale.UK,
        "AED" to Locale.Builder().setLanguage("en").setRegion("AE").build(),
        "SGD" to Locale.Builder().setLanguage("en").setRegion("SG").build(),
        "CAD" to Locale.CANADA,
        "AUD" to Locale.Builder().setLanguage("en").setRegion("AU").build(),
        "JPY" to Locale.JAPAN,
        "CNY" to Locale.CHINA,
        "NPR" to Locale.Builder().setLanguage("ne").setRegion("NP").build(),
        "ETB" to Locale.Builder().setLanguage("am").setRegion("ET").build(),
        "THB" to Locale.Builder().setLanguage("th").setRegion("TH").build(),
        "MYR" to Locale.Builder().setLanguage("ms").setRegion("MY").build(),
        "KWD" to Locale.Builder().setLanguage("en").setRegion("KW").build(),
        "KRW" to Locale.KOREA,
        "SEK" to Locale.Builder().setLanguage("sv").setRegion("SE").build(),
        "CHF" to Locale.Builder().setLanguage("de").setRegion("CH").build(),
        "NZD" to Locale.Builder().setLanguage("en").setRegion("NZ").build(),
        "MXN" to Locale.Builder().setLanguage("es").setRegion("MX").build()
    )

    /**
     * Formats a BigDecimal amount as currency with the specified currency code
     */
    fun formatCurrency(amount: BigDecimal, currencyCode: String = "INR"): String {
        return try {
            val locale = CURRENCY_LOCALES[currencyCode] ?: INDIAN_LOCALE
            val formatter = NumberFormat.getCurrencyInstance(locale)
            
            // Get our custom symbol
            val customSymbol = CurrencySymbols.getSymbol(currencyCode)

            // Configure formatting rules
            formatter.minimumFractionDigits = 0
            formatter.maximumFractionDigits = 2
 
            // Set the currency if supported
            try {
                formatter.currency = Currency.getInstance(currencyCode)
            } catch (e: Exception) {
                // If currency not supported, use symbol mapping
                return "$customSymbol${formatAmount(amount)}"
            }
 
            val formatted = formatter.format(amount)
            
            // If the formatted string doesn't contain our custom symbol, or contains the ISO code,
            // we override it to ensure the custom symbol is used.
            if (formatted.contains(currencyCode) || !formatted.contains(customSymbol)) {
                val cleanAmount = formatAmount(amount)
                return if (locale == Locale.US || locale == INDIAN_LOCALE || locale == Locale.UK) {
                    "$customSymbol$cleanAmount"
                } else {
                    "$cleanAmount $customSymbol"
                }
            }
            
            formatted
        } catch (e: Exception) {
            // Fallback to symbol + amount
            val symbol = CurrencySymbols.getSymbol(currencyCode)
            "$symbol${formatAmount(amount)}"
        }
    }

    /**
     * Formats a Double amount as currency with the specified currency code
     */
    fun formatCurrency(amount: Double, currencyCode: String = "INR"): String {
        return formatCurrency(amount.toBigDecimal(), currencyCode)
    }

    /**
     * Legacy method for backward compatibility - defaults to INR
     */
    fun formatAmount(amount: BigDecimal): String {
        val formatter = DecimalFormat("#,##,##0.00")
        return formatter.format(amount)
    }

    /**
     * Legacy method for backward compatibility - defaults to INR
     */
    fun formatAmount(amount: Double): String {
        return formatAmount(amount.toBigDecimal())
    }

    /**
     * Get symbol for a currency code
     */
    fun getCurrencySymbol(currencyCode: String): String {
        return CurrencySymbols.getSymbol(currencyCode)
    }

    /**
     * Gets the base currency for a bank using the BankParserFactory
     * Returns INR as default for unknown banks
     */
    fun getBankBaseCurrency(bankName: String?): String {
        if (bankName == null) return "INR"

        // Try to find a parser that can handle this bank name
        return try {
            val parser = BankParserFactory.getParser(bankName)
            parser?.getCurrency() ?: "INR"
        } catch (e: Exception) {
            "INR"
        }
    }
}