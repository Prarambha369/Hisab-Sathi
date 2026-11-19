package com.prarambha.hisabsathi.utils

import com.prarambha.hisabsathi.core.Constants
import com.prarambha.hisabsathi.data.currency.model.CurrencySymbols
import com.prarambha.parser.core.bank.BankParserFactory
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/**
 * Utility class for formatting currency values
 */
object CurrencyFormatter {

    private val INDIAN_LOCALE = Locale("en", "IN")

    /**
     * Latin digits with South-Asian (lakh/crore) grouping, e.g. 1,23,456.5 — how amounts
     * are written in Nepal. The ne-NP locale is deliberately not used for NPR because it
     * renders Devanagari digits and its own currency symbol.
     */
    private fun lakhGrouped(amount: BigDecimal, minFraction: Int = 0, maxFraction: Int = 2): String =
        NumberFormat.getNumberInstance(INDIAN_LOCALE).apply {
            minimumFractionDigits = minFraction
            maximumFractionDigits = maxFraction
        }.format(amount)

    /** Formats an NPR amount as "रु 1,23,456". */
    fun formatNpr(amount: BigDecimal, minFraction: Int = 0, maxFraction: Int = 2): String {
        val sign = if (amount.signum() < 0) "-" else ""
        return "$sign${CurrencySymbols.getSymbol("NPR")} ${lakhGrouped(amount.abs(), minFraction, maxFraction)}"
    }

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
    fun formatCurrency(amount: BigDecimal, currencyCode: String = Constants.DEFAULT_CURRENCY): String {
        if (currencyCode.equals("NPR", ignoreCase = true)) return formatNpr(amount)
        return try {
            val locale = CURRENCY_LOCALES[currencyCode] ?: Locale.US
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
    fun formatCurrency(amount: Double, currencyCode: String = Constants.DEFAULT_CURRENCY): String {
        return formatCurrency(amount.toBigDecimal(), currencyCode)
    }

    /**
     * Formats a bare amount (no symbol) with lakh grouping and two decimals.
     */
    fun formatAmount(amount: BigDecimal): String = lakhGrouped(amount, 2, 2)

    /**
     * Formats a bare amount (no symbol) with lakh grouping and two decimals.
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
     * Returns NPR as default for unknown banks
     */
    fun getBankBaseCurrency(bankName: String?): String {
        if (bankName == null) return "NPR"

        // Try to find a parser that can handle this bank name
        return try {
            val parser = BankParserFactory.getParser(bankName)
            parser?.getCurrency() ?: "NPR"
        } catch (e: Exception) {
            "NPR"
        }
    }
}