package com.prarambha.parser.core

/**
 * Utility to filter and identify transaction messages.
 */
object SmsFilter {

    /**
     * Checks if the message is a transaction message (not OTP, promotional, etc.)
     */
    fun isTransactionMessage(message: String): Boolean {
        if (isNonTransactionMessage(message)) return false

        // Must contain transaction keywords
        val lowerMessage = message.lowercase()
        val transactionKeywords = listOf(
            "debited", "credited", "withdrawn", "withdrawal", "withdrawing", "deposited",
            "spent", "received", "transferred", "paid", "credit", "debit"
        )

        return transactionKeywords.any { lowerMessage.contains(it) }
    }

    /**
     * Messages that carry an amount but are never a transaction: OTPs, promos,
     * payment requests and due/reminder notices. Kept apart from the keyword list
     * above so callers that read direction from the message themselves (the generic
     * extractor) can rule these out without also demanding a debit/credit word.
     */
    fun isNonTransactionMessage(message: String): Boolean {
        val lowerMessage = message.lowercase()

        // Skip OTP messages
        if (lowerMessage.contains("otp") ||
            lowerMessage.contains("one time password") ||
            lowerMessage.contains("verification code")
        ) {
            return true
        }

        // Skip promotional messages
        if (lowerMessage.contains("offer") ||
            lowerMessage.contains("discount") ||
            lowerMessage.contains("cashback offer") ||
            lowerMessage.contains("win ")
        ) {
            return true
        }

        // Skip payment request messages (common across banks)
        if (lowerMessage.contains("has requested") ||
            lowerMessage.contains("payment request") ||
            lowerMessage.contains("collect request") ||
            lowerMessage.contains("requesting payment") ||
            lowerMessage.contains("requests rs") ||
            lowerMessage.contains("ignore if already paid")
        ) {
            return true
        }

        // Skip merchant payment acknowledgments
        if (lowerMessage.contains("have received payment")) {
            return true
        }

        // Skip payment reminder/due messages
        if (lowerMessage.contains("is due") ||
            lowerMessage.contains("min amount due") ||
            lowerMessage.contains("minimum amount due") ||
            lowerMessage.contains("in arrears") ||
            lowerMessage.contains("is overdue") ||
            lowerMessage.contains("ignore if paid") ||
            (lowerMessage.contains("pls pay") && lowerMessage.contains("min of"))
        ) {
            return true
        }

        return false
    }
}
