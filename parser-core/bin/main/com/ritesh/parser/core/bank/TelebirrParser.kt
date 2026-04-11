package com.ritesh.parser.core.bank

import com.ritesh.parser.core.TransactionType
import java.math.BigDecimal

/**
 * Parser for Telebirr - handles ETB currency transactions
 */
class TelebirrParser: BankParser() {

    override fun getBankName() = "Telebirr"

    override fun getCurrency() = "ETB"

    override fun canHandle(sender: String): Boolean {
        val upperSender = sender.uppercase().trim()
        return upperSender == "127" ||
                upperSender.contains("127") ||
                upperSender.matches(Regex("""^[A-Z]{2}-127-[A-Z]$""")) ||
                upperSender.matches(Regex("""^127-[A-Z0-9]+$""")) ||
                upperSender.matches(Regex("""^[A-Z0-9]+-127$"""))
    }

    override fun extractAmount(message: String): BigDecimal? {
        val patterns = listOf(
            Regex("""ETB\s+([0-9,]+(?:\.[0-9]{2})?)\s""", RegexOption.IGNORE_CASE),
            Regex("""ETB\s*([0-9,]+(?:\.[0-9]{2})?)(?:\s|$|\.)""", RegexOption.IGNORE_CASE),
            Regex(
                """(?:Credited|debited|transfered)\s+(?:with\s+)?ETB\s+([0-9,]+(?:\.[0-9]{2})?)""",
                RegexOption.IGNORE_CASE
            )
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
            lowerMessage.contains("deposited etb") &&
                    lowerMessage.contains("to your saving account") -> TransactionType.EXPENSE
            (lowerMessage.contains(" withdraw etb") || lowerMessage.contains("withdraw etb")) &&
                    lowerMessage.contains("from your saving account") -> TransactionType.INCOME
            lowerMessage.contains("you have received") -> TransactionType.INCOME
            lowerMessage.contains("you have paid") -> TransactionType.EXPENSE
            lowerMessage.contains("you have transferred") -> TransactionType.EXPENSE
            else -> null
        }
    }

    override fun extractMerchant(message: String, sender: String): String? {
        val savingsDepositPattern = Regex(
            """deposited\s+ETB\s+[0-9,]+(?:\.[0-9]{2})?\s+to\s+your\s+(.+?)\s+on\s+\d{2}/\d{2}/\d{4}""",
            RegexOption.IGNORE_CASE
        )
        savingsDepositPattern.find(message)?.let { match ->
            val merchant = match.groupValues[1].trim()
            if (merchant.isNotEmpty()) return merchant
        }

        val savingsWithdrawPattern = Regex(
            """withdraw(?:n)?\s+ETB\s+[0-9,]+(?:\.[0-9]{2})?\s+from\s+your\s+(.+?)\s+on\s+\d{2}/\d{2}/\d{4}""",
            RegexOption.IGNORE_CASE
        )
        savingsWithdrawPattern.find(message)?.let { match ->
            val merchant = match.groupValues[1].trim()
            if (merchant.isNotEmpty()) return merchant
        }

        val bankFromPattern = Regex("""from\s+([A-Za-z\s]+Bank)\s+to\s+your""", RegexOption.IGNORE_CASE)
        bankFromPattern.find(message)?.let { match ->
            val merchant = match.groupValues[1].trim()
            if (isValidMerchantName(merchant)) {
                return merchant
            }
        }

        val paidToPattern = Regex("""paid\s+ETB\s+[0-9,]+(?:\.[0-9]{2})?\s+to\s+([^,\n]+?)(?=\s+on\s+\d{2}/\d{2}/\d{4}|\.\s+Your\s+transaction|$)""", RegexOption.IGNORE_CASE)
        paidToPattern.find(message)?.let { match ->
            var merchant = match.groupValues[1]
            if (merchant.isNotEmpty()) {
                return merchant.trim()
            }
        }

        val purchasedFromPattern = Regex("""for\s+goods\s+purchased\s+from\s+([^,\n]+?)(?:\s+on\s+\d{2}/\d{2}/\d{4}|\.\s+Your\s+transaction|$)""", RegexOption.IGNORE_CASE)
        purchasedFromPattern.find(message)?.let { match ->
            val merchant = match.groupValues[1].trim()
            if (merchant.isNotEmpty()) {
                return merchant
            }
        }

        val packagePattern = Regex("""for\s+package\s+([^,\n]+?)(?:\s+purchase\s+made|\s+on\s+\d{2}/\d{2}/\d{4}|\.\s+Your\s+transaction|$)""", RegexOption.IGNORE_CASE)
        packagePattern.find(message)?.let { match ->
            var merchant = match.groupValues[1].trim()
            val purchaseMadePattern = Regex("""purchase\s+made\s+for\s+(\d+)""", RegexOption.IGNORE_CASE)
            val purchaseMatch = purchaseMadePattern.find(message)
            if (purchaseMatch != null) {
                merchant += " purchase made for ${purchaseMatch.groupValues[1]}"
            }
            if (merchant.isNotEmpty()) {
                return merchant
            }
        }

        val transferredToPattern = Regex("""transferred\s+[^,\n]+?\s+to\s+([^,\n]+?)(?:\s+on\s+\d{2}/\d{2}/\d{4}|\.|$)""", RegexOption.IGNORE_CASE)
        transferredToPattern.find(message)?.let { match ->
            val merchant = match.groupValues[1].trim()
            if (merchant.contains("(") && merchant.contains(")")) {
                return merchant
            }
            val cleaned = cleanMerchantName(merchant)
            if (isValidMerchantName(cleaned)) {
                return cleaned
            }
        }

        val fromPattern = Regex("""from\s+(?!your\s+account)([^,\n]+?)(?:\s+on\s+\d{2}/\d{2}/\d{4}|\s+to\s+your|\.|$)""", RegexOption.IGNORE_CASE)
        fromPattern.find(message)?.let { match ->
            var merchant = match.groupValues[1].trim()
            merchant = merchant.replace(Regex("""([A-Za-z\s]+)\((\d+\*+\d+)\)"""), "$1 ($2)")
            if (merchant.contains("(") && merchant.contains(")")) {
                return merchant
            }
            val cleaned = cleanMerchantName(merchant)
            if (isValidMerchantName(cleaned)) {
                return cleaned
            }
        }

        return super.extractMerchant(message, sender)
    }

    override fun extractAccountLast4(message: String): String? {
        val dearPattern = Regex("""Dear\s+\[([^\]]+)\]""", RegexOption.IGNORE_CASE)
        dearPattern.find(message)?.let { match ->
            return "[${match.groupValues[1]}]"
        }
        return null
    }

    override fun extractBalance(message: String): BigDecimal? {
        val eMoneyBalancePattern =
            Regex("""E-Money Account\s+balance is ETB\s+([0-9,]+(?:\.[0-9]{2})?)""", RegexOption.IGNORE_CASE)
        eMoneyBalancePattern.find(message)?.let { match ->
            val balanceStr = match.groupValues[1].replace(",", "")
            return try {
                BigDecimal(balanceStr)
            } catch (e: NumberFormatException) {
                null
            }
        }

        val currentBalancePattern =
            Regex("""current balance is ETB\s+([0-9,]+(?:\.[0-9]{2})?)""", RegexOption.IGNORE_CASE)
        currentBalancePattern.find(message)?.let { match ->
            val balanceStr = match.groupValues[1].replace(",", "")
            return try {
                BigDecimal(balanceStr)
            } catch (e: NumberFormatException) {
                null
            }
        }

        val telebirrBalancePattern =
            Regex("""telebirr account balance is\s+ETB\s+([0-9,]+(?:\.[0-9]{2})?)""", RegexOption.IGNORE_CASE)
        telebirrBalancePattern.find(message)?.let { match ->
            val balanceStr = match.groupValues[1].replace(",", "")
            return try {
                BigDecimal(balanceStr)
            } catch (e: NumberFormatException) {
                null
            }
        }

        return super.extractBalance(message)
    }

    override fun extractReference(message: String): String? {
        val bankTransactionPattern = Regex("""bank transaction number is\s+([A-Z0-9]+)""", RegexOption.IGNORE_CASE)
        bankTransactionPattern.find(message)?.let { match ->
            return match.groupValues[1]
        }

        val byTransactionPattern = Regex("""by transaction number\s+([A-Z0-9]+)""", RegexOption.IGNORE_CASE)
        byTransactionPattern.find(message)?.let { match ->
            return match.groupValues[1]
        }

        val transactionPattern = Regex("""(?:your\s+)?transaction number is\s+([A-Z0-9]+)""", RegexOption.IGNORE_CASE)
        transactionPattern.find(message)?.let { match ->
            return match.groupValues[1]
        }

        return super.extractReference(message)
    }

    override fun isTransactionMessage(message: String): Boolean {
        val lowerMessage = message.lowercase()

        val telebirrTransactionKeywords = listOf(
            "dear", "you have received", "you have paid", "you have transferred",
            "current balance", "e-money account balance", "telebirr account balance",
            "thank you for using telebirr", "etb", "transaction number"
        )

        if (telebirrTransactionKeywords.any { lowerMessage.contains(it) }) {
            return true
        }

        return super.isTransactionMessage(message)
    }
}
