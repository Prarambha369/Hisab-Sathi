package com.ritesh.cashiro.utils

import android.util.Log
import java.time.LocalDate

object SubscriptionUtils {
    /**
     * Calculates the next payment date based on the current date and billing cycle.
     * Supports standard cycles (Weekly, Monthly, Quarterly, Semi-Annual, Annual)
     * and custom cycles (e.g., custom_1_day_2026-04-30).
     */
    fun calculateNextPaymentDate(
        fromDate: LocalDate,
        billingCycle: String?
    ): LocalDate {
        val today = LocalDate.now()
        val cycle = billingCycle?.lowercase() ?: "monthly"
        
        if (cycle.startsWith("custom_")) {
            val parts = cycle.split("_")
            val count = parts.getOrNull(1)?.toLongOrNull() ?: 1L
            val unit = parts.getOrNull(2) ?: "month"
            val endDateStr = parts.getOrNull(3)
            
            var nextDate = when (unit) {
                "day" -> fromDate.plusDays(count)
                "week" -> fromDate.plusWeeks(count)
                "year" -> fromDate.plusYears(count)
                else -> fromDate.plusMonths(count) // month
            }
            
            // Catch up to today if needed
            while (nextDate.isBefore(today)) {
                nextDate = when (unit) {
                    "day" -> nextDate.plusDays(count)
                    "week" -> nextDate.plusWeeks(count)
                    "year" -> nextDate.plusYears(count)
                    else -> nextDate.plusMonths(count)
                }
            }
            
            // Check if we passed the end date
            if (endDateStr != null && endDateStr != "forever") {
                try {
                    val endDate = LocalDate.parse(endDateStr)
                    if (nextDate.isAfter(endDate)) {
                        // For now we still return it, letting the system handle expiry later
                        // or we could return null/special value if it shouldn't repeat anymore.
                        Log.d("SubscriptionUtils", "Next date $nextDate is after end date $endDate")
                    }
                } catch (e: Exception) {
                    Log.e("SubscriptionUtils", "Error parsing end date: $endDateStr", e)
                }
            }
            
            return nextDate
        }

        // Standard cycles
        var nextDate = when (cycle) {
            "weekly" -> fromDate.plusWeeks(1)
            "quarterly" -> fromDate.plusMonths(3)
            "semi-annual" -> fromDate.plusMonths(6)
            "annual" -> fromDate.plusYears(1)
            else -> fromDate.plusMonths(1) // covers "monthly" and defaults
        }

        while (nextDate.isBefore(today)) {
            nextDate = when (cycle) {
                "weekly" -> nextDate.plusWeeks(1)
                "quarterly" -> nextDate.plusMonths(3)
                "semi-annual" -> nextDate.plusMonths(6)
                "annual" -> nextDate.plusYears(1)
                else -> nextDate.plusMonths(1)
            }
        }
        return nextDate
    }
}
