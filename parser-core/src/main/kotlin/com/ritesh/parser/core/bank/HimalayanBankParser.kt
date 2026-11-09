package com.ritesh.parser.core.bank

import com.ritesh.parser.core.TransactionType
import java.math.BigDecimal

/**
 * Parser for Himalayan Bank Limited (Nepal)
 * Handles NPR currency transactions
 * 
 * Common sender IDs: HBL_ALERT, HBL
 */
class HimalayanBankParser : BankParser() {

    override fun getBankName() = "Himalayan Bank"

    override fun getCurrency() = "NPR"

    override fun canHandle(sender: String): Boolean {
        val upperSender = sender.uppercase()
        return upperSender == "HBL_ALERT" ||
                upperSender == "HBL" ||
                upperSender.contains("HIMALAYANBANK") ||
                upperSender.matches(Regex("""^[A-Z]{2}-HBL-[A-Z]$"""))
    }

    override fun extractAmount(message: String): BigDecimal? {
        val patterns = listOf(
            Regex("""NPR\s+([0-9,]+(?:\.[0-9]{2})?)\s""", RegexOption.IGNORE_CASE),
            Regex("""NPR\s+([0-9,]+(?:\.[0-9]{2})?)(?:\s|$)""", RegexOption.IGNORE_CASE),
            Regex("""(?:debited|credited)\s+by\s+NPR\s+([0-9,]+(?:\.[0-9]{2})?)""", RegexOption.IGNORE_CASE)
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
            lowerMessage.contains("debited") -> TransactionType.EXPENSE
            lowerMessage.contains("credited") -> TransactionType.INCOME
            else -> null
        }
    }

    override fun extractMerchant(message: String, sender: String): String? {
        return when {
            message.contains("ESEWA", ignoreCase = true) -> "eSewa"
            message.contains("KHALTI", ignoreCase = true) -> "Khalti"
            message.contains("ATM", ignoreCase = true) -> "ATM Withdrawal"
            message.contains("POS", ignoreCase = true) -> "POS Transaction"
            else -> super.extractMerchant(message, sender)
        }
    }

    override fun isTransactionMessage(message: String): Boolean {
        val lowerMessage = message.lowercase()
        
        if (lowerMessage.contains("otp") || lowerMessage.contains("verification code")) {
            return false
        }

        return lowerMessage.contains("npr") && 
               (lowerMessage.contains("debited") || lowerMessage.contains("credited"))
    }
}
