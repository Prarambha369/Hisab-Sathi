package com.ritesh.parser.core.bank

import com.ritesh.parser.core.TransactionType
import java.math.BigDecimal

/**
 * Parser for Khalti (Nepal) - Digital Wallet
 * Handles NPR currency transactions
 * 
 * Common sender IDs: KHALTI_ALERT, KHALTI
 */
class KhaltiParser : BankParser() {

    override fun getBankName() = "Khalti"

    override fun getCurrency() = "NPR"

    override fun canHandle(sender: String): Boolean {
        val upperSender = sender.uppercase()
        return upperSender == "KHALTI_ALERT" ||
                upperSender == "KHALTI" ||
                upperSender.contains("KHALTIDIGITAL") ||
                upperSender.matches(Regex("""^[A-Z]{2}-KHALTI-[A-Z]$"""))
    }

    override fun extractAmount(message: String): BigDecimal? {
        val patterns = listOf(
            Regex("""NPR\s+([0-9,]+(?:\.[0-9]{2})?)\s""", RegexOption.IGNORE_CASE),
            Regex("""NPR\s+([0-9,]+(?:\.[0-9]{2})?)(?:\s|$)""", RegexOption.IGNORE_CASE),
            Regex("""Rs\.\s*([0-9,]+(?:\.[0-9]{2})?)""", RegexOption.IGNORE_CASE),
            Regex("""amount\s+(?:of)?\s*([0-9,]+(?:\.[0-9]{2})?)""", RegexOption.IGNORE_CASE)
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
            lowerMessage.contains("paid") || 
            lowerMessage.contains("debited") ||
            lowerMessage.contains("sent") ||
            lowerMessage.contains("purchase") -> 
                TransactionType.EXPENSE
            
            lowerMessage.contains("received") || 
            lowerMessage.contains("credited") ||
            lowerMessage.contains("cashback") ||
            lowerMessage.contains("refunded") ||
            lowerMessage.contains("bonus") -> 
                TransactionType.INCOME

            else -> null
        }
    }

    override fun extractMerchant(message: String, sender: String): String? {
        val lowerMessage = message.lowercase()

        return when {
            lowerMessage.contains("electricity") || lowerMessage.contains("nea") -> 
                "Nepal Electricity Authority"
            
            lowerMessage.contains("water") -> 
                "Water Supply"
            
            lowerMessage.contains("internet") -> 
                "Internet Service"
            
            lowerMessage.contains("mobile") || lowerMessage.contains("topup") -> 
                "Mobile Topup"
            
            lowerMessage.contains("movie") || lowerMessage.contains("ticket") -> 
                "Movie Ticket"
            
            lowerMessage.contains("game") || lowerMessage.contains("gaming") -> 
                "Gaming"
            
            lowerMessage.contains("wallet load") -> 
                "Wallet Load"
            
            lowerMessage.contains("qr payment") -> "QR Payment"
            lowerMessage.contains("scan pay") -> "Scan & Pay"
            
            else -> super.extractMerchant(message, sender)
        }
    }

    override fun extractAccountLast4(message: String): String? {
        val patterns = listOf(
            Regex("""from\s+(\d{10})""", RegexOption.IGNORE_CASE),
            Regex("""to\s+(\d{10})""", RegexOption.IGNORE_CASE),
            Regex("""mobile[:\s]+(\d{10})""", RegexOption.IGNORE_CASE),
            Regex("""(\d{4})\s+(?:has been|was)""", RegexOption.IGNORE_CASE)
        )

        for (pattern in patterns) {
            pattern.find(message)?.let { match ->
                val number = match.groupValues[1]
                return if (number.length >= 4) number.takeLast(4) else number
            }
        }

        return super.extractAccountLast4(message)
    }

    override fun extractReference(message: String): String? {
        val patterns = listOf(
            Regex("""Ref(?:erence)?[:\s]*([A-Z0-9]+)""", RegexOption.IGNORE_CASE),
            Regex("""Transaction ID[:\s]*([A-Z0-9]+)""", RegexOption.IGNORE_CASE),
            Regex("""Order ID[:\s]*([A-Z0-9]+)""", RegexOption.IGNORE_CASE),
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

        if (lowerMessage.contains("otp") ||
            lowerMessage.contains("verification code") ||
            lowerMessage.contains("password") && !lowerMessage.contains("transaction")) {
            return false
        }

        // Explicitly filter out failed/declined/unsuccessful transactions
        if (lowerMessage.contains("payment failed") ||
            lowerMessage.contains("transaction failed") ||
            lowerMessage.contains("declined") ||
            lowerMessage.contains("unsuccessful")) {
            return false
        }

        val khaltiKeywords = listOf(
            "payment successful",
            "transaction successful",
            "has been debited",
            "has been credited",
            "received npr",
            "paid npr",
            "cashback",
            "wallet load",
            "purchase successful"
        )

        return khaltiKeywords.any { lowerMessage.contains(it) }
    }
}
