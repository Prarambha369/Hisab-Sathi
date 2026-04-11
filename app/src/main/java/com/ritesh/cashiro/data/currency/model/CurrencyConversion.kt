package com.ritesh.cashiro.data.currency.model

data class CurrencyConversion(
    val currencyCode: String,
    val rate: Double,
    val lastUpdated: Long = 0L
) {
    val symbol: String get() = CurrencySymbols.getSymbol(currencyCode)
    val displayRate: String get() = String.format("%.6f", rate)
}
