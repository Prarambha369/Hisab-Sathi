package com.ritesh.parser.core.bank

import com.ritesh.parser.core.TransactionType
import java.math.BigDecimal

/**
 * Parser for Nabil Bank (Nepal) - First private bank of Nepal
 * Handles NPR currency transactions
 * 
 * Common sender IDs: NABIL_ALERT, NABIL
 */
class NabilBankParser : BankParser() {

    override fun getBankName() = "Nabil Bank"

    override fun getCurrency() = "NPR"  // Nepalese Rupee

    override fun canHandle(sender: String): Boolean {
        val upperSender = sender.uppercase()
        return upperSender == "NABIL_ALERT" ||
                upperSender == "NABIL" ||
                upperSender.contains("NABILBANK") ||
                upperSender.matches(Regex("""^[A-Z]{2}-NABIL-[A-Z]$"""))
    }

    override fun extractAmount(message: String): BigDecimal? {
        val patterns = listOf(
            Regex("""NPR\s+([0-9,]+(?:\.[0-9]{2})?)\s""", RegexOption.IGNORE_CASE),
            Regex("""NPR\s+([0-9,]+(?:\.[0-9]{2})?)(?:\s|$)""", RegexOption.IGNORE_CASE),
            Regex("""(?:debited|credited)\s+by\s+NPR\s+([0-9,]+(?:\.[0-9]{2})?)""", RegexOption.IGNORE_CASE),
            Regex("""amount[:\s]+npr\s*([0-9,]+(?:\.[0-9]{2})?)""", RegexOption.IGNORE_CASE)
        )

        for (pattern in patterns) {
            pattern.find(message)?.let { match ->
                val amountStr = match.groupValues[1].replace(",", "")
                return try {
                    BigDecimal(amountStr)
                } catch (e: NumberFormatException) {
                    null
                }
            }
        }

        return super.extractAmount(message)
    }

    override fun extractTransactionType(message: String): TransactionType? {
        val lowerMessage = message.lowercase()

        return when {
            lowerMessage.contains("has been debited") || 
            lowerMessage.contains("debited by") ||
            lowerMessage.contains("debit") && lowerMessage.contains("transaction") -> 
                TransactionType.EXPENSE
            
            lowerMessage.contains("has been credited") || 
            lowerMessage.contains("credited by") ||
            lowerMessage.contains("credit") && lowerMessage.contains("transaction") -> 
                TransactionType.INCOME

            else -> null
        }
    }

    override fun extractMerchant(message: String, sender: String): String? {
        // Pattern: Extract from transaction details
        val patterns = listOf(
            Regex("""at\s+([^.\n]+?)(?:\.|\s+on|\s+with)""", RegexOption.IGNORE_CASE),
            Regex("""payment to\s+([^.\n]+)""", RegexOption.IGNORE_CASE),
            Regex("""transfer to\s+([^.\n]+)""", RegexOption.IGNORE_CASE)
        )

        for (pattern in patterns) {
            pattern.find(message)?.let { match ->
                val merchant = cleanMerchantName(match.groupValues[1].trim())
                if (isValidMerchantName(merchant)) {
                    return merchant
                }
            }
        }

        // Check for common Nepali merchants
        return when {
            message.contains("ESEWA", ignoreCase = true) -> "eSewa"
            message.contains("KHALTI", ignoreCase = true) -> "Khalti"
            message.contains("FONEPAY", ignoreCase = true) -> "Fonepay"
            message.contains("ATM", ignoreCase = true) -> "ATM Withdrawal"
            message.contains("POS", ignoreCase = true) -> "POS Transaction"
            else -> super.extractMerchant(message, sender)
        }
    }

    override fun extractAccountLast4(message: String): String? {
        // Pattern: "A/C XXXX1234" or "Account ending 1234"
        val patterns = listOf(
            Regex("""A/C\s+(?:X*#?(\d{4})|X*(\d{5,}))""", RegexOption.IGNORE_CASE),
            Regex("""account\s+(?:ending|number)?\s*(?:in)?\s*(\d{4})""", RegexOption.IGNORE_CASE),
            Regex("""(\d{4})\s+(?:has been|was)""", RegexOption.IGNORE_CASE)
        )

        for (pattern in patterns) {
            pattern.find(message)?.let { match ->
                val last4 = match.groups[1]?.value ?: match.groups[2]?.value
                if (!last4.isNullOrBlank()) {
                    return if (last4.length > 4) last4.takeLast(4) else last4
                }
            }
        }

        return super.extractAccountLast4(message)
    }

    override fun extractReference(message: String): String? {
        // Look for transaction reference numbers
        val patterns = listOf(
            Regex("""Ref(?:erence)?[:\s]*([A-Z0-9]+)""", RegexOption.IGNORE_CASE),
            Regex("""Transaction ID[:\s]*([A-Z0-9]+)""", RegexOption.IGNORE_CASE),
            Regex("""\(([A-Z0-9]{6,})\)""")
        )

        for (pattern in patterns) {
            pattern.find(message)?.let { match ->
                return match.groupValues[1]
            }
        }

        return super.extractReference(message)
    }

    override fun isTransactionMessage(message: String): Boolean {
        val lowerMessage = message.lowercase()

        // Skip OTP and promotional messages
        if (lowerMessage.contains("otp") ||
            lowerMessage.contains("verification code") ||
            lowerMessage.contains("password") && !lowerMessage.contains("transaction")) {
            return false
        }

        val hasCurrency = Regex("""\b(?:npr|rs\.?)\b""", RegexOption.IGNORE_CASE).containsMatchIn(message)
        val hasAmount = Regex(
            """(?:\b(?:npr|rs\.?)\s*)\d{1,3}(?:,\d{3})*(?:\.\d{1,2})?""",
            RegexOption.IGNORE_CASE
        ).containsMatchIn(message)
        val hasTransactionAction = listOf(
            "has been debited",
            "has been credited",
            "debited",
            "credited",
            "withdrawn",
            "deposited",
            "purchase",
            "spent",
            "transfer",
            "transferred"
        ).any { lowerMessage.contains(it) }
        val hasTransactionContext = listOf(
            "your account",
            "a/c",
            "account",
            "transaction",
            "available balance",
            "bal"
        ).any { lowerMessage.contains(it) }

        return (hasAmount && hasCurrency && hasTransactionAction) ||
            (hasAmount && hasTransactionAction && hasTransactionContext)
    }
}
