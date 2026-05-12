package com.prarambha.parser.core.bank

import com.prarambha.parser.core.CompiledPatterns
import com.prarambha.parser.core.Constants
import com.prarambha.parser.core.ParsedTransaction
import com.prarambha.parser.core.TransactionType
import java.math.BigDecimal
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

abstract class BankParser {
    abstract fun getBankName(): String
    open fun getCurrency(): String = "NPR"
    abstract fun canHandle(sender: String): Boolean

    fun parse(message: String, sender: String, timestamp: Long): ParsedTransaction? {
        if (!isTransactionMessage(message)) return null

        val amount = extractAmount(message) ?: return null
        val type = extractTransactionType(message) ?: return null

        return ParsedTransaction(
            amount = amount,
            type = type,
            merchant = extractMerchant(message, sender),
            reference = extractReference(message),
            accountLast4 = extractAccountLast4(message),
            balance = extractBalance(message),
            creditLimit = extractAvailableLimit(message),
            smsBody = message,
            sender = sender,
            timestamp = timestamp,
            bankName = getBankName(),
            isFromCard = detectIsCard(message),
            currency = getCurrency(),
            cardType = extractCardType(message),
            dueDate = extractDueDate(message),
            minDue = extractMinDue(message)
        )
    }

    protected open fun isTransactionMessage(message: String): Boolean {
        val lowerMessage = message.lowercase()
        if (lowerMessage.contains("otp") || lowerMessage.contains("verification code")) return false
        val transactionKeywords = listOf("debited", "credited", "withdrawn", "deposited", "spent", "received", "transferred", "paid", "purchase")
        return transactionKeywords.any { lowerMessage.contains(it) }
    }

    protected open fun extractAmount(message: String): BigDecimal? {
        for (pattern in CompiledPatterns.Amount.ALL_PATTERNS) {
            pattern.find(message)?.let { match ->
                val amountStr = match.groupValues[1].replace(",", "")
                return try { BigDecimal(amountStr) } catch (e: Exception) { null }
            }
        }
        return null
    }

    protected open fun extractTransactionType(message: String): TransactionType? {
        val lowerMessage = message.lowercase()
        return when {
            lowerMessage.contains("debited") || lowerMessage.contains("withdrawn") || lowerMessage.contains("spent") || lowerMessage.contains("paid") || lowerMessage.contains("purchase") -> TransactionType.EXPENSE
            lowerMessage.contains("credited") || lowerMessage.contains("deposited") || lowerMessage.contains("received") -> TransactionType.INCOME
            else -> null
        }
    }

    protected open fun extractMerchant(message: String, sender: String): String? {
        for (pattern in CompiledPatterns.Merchant.ALL_PATTERNS) {
            pattern.find(message)?.let { match ->
                val merchant = cleanMerchantName(match.groupValues[1].trim())
                if (isValidMerchantName(merchant)) return merchant
            }
        }
        return null
    }

    protected open fun extractReference(message: String): String? {
        for (pattern in CompiledPatterns.Reference.ALL_PATTERNS) {
            pattern.find(message)?.let { match -> return match.groupValues[1].trim() }
        }
        return null
    }

    protected open fun extractAccountLast4(message: String): String? {
        for (pattern in CompiledPatterns.Account.ALL_PATTERNS) {
            pattern.find(message)?.let { match -> return extractLast4Digits(match.groupValues[1]) }
        }
        return null
    }

    protected open fun extractBalance(message: String): BigDecimal? {
        for (pattern in CompiledPatterns.Balance.ALL_PATTERNS) {
            pattern.find(message)?.let { match ->
                val balanceStr = match.groupValues[1].replace(",", "")
                return try { BigDecimal(balanceStr) } catch (e: Exception) { null }
            }
        }
        return null
    }

    protected open fun extractAvailableLimit(message: String): BigDecimal? {
        val creditLimitPatterns = listOf(
            Regex("""Available\s+limit\s+Rs\.([0-9,]+(?:\.\d{2})?)""", RegexOption.IGNORE_CASE),
            Regex("""Available\s+limit:?\s*Rs\.?\s*([0-9,]+(?:\.\d{2})?)""", RegexOption.IGNORE_CASE),
            Regex("""Avl\s+Lmt:?\s*Rs\.?\s*([0-9,]+(?:\.\d{2})?)""", RegexOption.IGNORE_CASE)
        )
        for (pattern in creditLimitPatterns) {
            pattern.find(message)?.let { match ->
                val limitStr = match.groupValues[1].replace(",", "")
                return try { BigDecimal(limitStr) } catch (e: Exception) { null }
            }
        }
        return null
    }

    protected open fun detectIsCard(message: String): Boolean {
        val lowerMessage = message.lowercase()
        val cardPatterns = listOf("card ending", "card xx", "debit card", "credit card")
        return cardPatterns.any { lowerMessage.contains(it) }
    }

    protected open fun cleanMerchantName(merchant: String): String {
        return merchant.replace(CompiledPatterns.Cleaning.TRAILING_PARENTHESES, "")
            .replace(CompiledPatterns.Cleaning.REF_NUMBER_SUFFIX, "")
            .replace(CompiledPatterns.Cleaning.DATE_SUFFIX, "")
            .replace(CompiledPatterns.Cleaning.UPI_SUFFIX, "")
            .replace(CompiledPatterns.Cleaning.TIME_SUFFIX, "")
            .replace(CompiledPatterns.Cleaning.TRAILING_DASH, "")
            .replace(CompiledPatterns.Cleaning.PVT_LTD, "")
            .replace(CompiledPatterns.Cleaning.LTD, "")
            .trim()
    }

    protected open fun isValidMerchantName(name: String): Boolean {
        val commonWords = setOf("USING", "VIA", "THROUGH", "BY", "WITH", "FOR", "TO", "FROM", "AT", "THE")
        return name.length >= 3 && name.any { it.isLetter() } && name.uppercase() !in commonWords && !name.all { it.isDigit() } && !name.contains("@")
    }

    protected fun extractLast4Digits(raw: String): String? {
        val digits = raw.filter { it.isDigit() }
        return if (digits.length >= 4) digits.takeLast(4) else digits.ifEmpty { null }
    }

    protected open fun extractCardType(message: String): String? {
        val lower = message.lowercase()
        return when {
            lower.contains("visa") -> "Visa"
            lower.contains("mastercard") || lower.contains("mc") -> "Mastercard"
            lower.contains("sct") -> "SCT"
            else -> null
        }
    }

    protected open fun extractDueDate(message: String): Long? {
        val isoPattern = Regex("""Due Date[:\s]+(\d{4}-\d{2}-\d{2})""", RegexOption.IGNORE_CASE)
        val dmyPattern = Regex("""Due Date[:\s]+(\d{2}/\d{2}/\d{4})""", RegexOption.IGNORE_CASE)
        isoPattern.find(message)?.let { match ->
            return try {
                LocalDate.parse(match.groupValues[1], DateTimeFormatter.ISO_LOCAL_DATE)
                    .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
            } catch (_: Exception) { null }
        }
        dmyPattern.find(message)?.let { match ->
            return try {
                LocalDate.parse(match.groupValues[1], DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
            } catch (_: Exception) { null }
        }
        return null
    }

    protected open fun extractMinDue(message: String): BigDecimal? {
        val patterns = listOf(
            Regex("""Min(?:imum)?\s+Amt\s+Due[:\s]+(?:NPR|Rs\.?)\s*([0-9,]+(?:\.[0-9]{2})?)""", RegexOption.IGNORE_CASE)
        )
        for (pattern in patterns) {
            pattern.find(message)?.let { match ->
                val amountStr = match.groupValues[1].replace(",", "")
                return try { BigDecimal(amountStr) } catch (e: Exception) { null }
            }
        }
        return null
    }
}
