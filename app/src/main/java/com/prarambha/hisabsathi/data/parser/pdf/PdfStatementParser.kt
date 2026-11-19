package com.prarambha.hisabsathi.data.parser.pdf

import com.prarambha.hisabsathi.BuildConfig

import android.util.Log
import com.prarambha.parser.core.ParsedTransaction
import com.prarambha.parser.core.TransactionType
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

interface PdfStatementParser {
    fun canHandle(text: String): Boolean
    fun parse(text: String): List<ParsedTransaction>
}

class GPayPdfParser : PdfStatementParser {
    override fun canHandle(text: String): Boolean {
        val canHandle = (text.contains("GPay", ignoreCase = true) || text.contains("Google Pay", ignoreCase = true)) 
                        && text.contains("UPI Transaction ID", ignoreCase = true)
        if (BuildConfig.DEBUG) Log.d("PDF_PARSER_DEBUG", "GPayPdfParser canHandle: $canHandle")
        return canHandle
    }

    override fun parse(text: String): List<ParsedTransaction> {
        if (BuildConfig.DEBUG) Log.d("PDF_PARSER_DEBUG", "GPayPdfParser starting parse. Text size: ${text.length}")
        val transactions = mutableListOf<ParsedTransaction>()

        // Regex to find start of a transaction (Date pattern)
        // More flexible date: "01 Aug 2025" or "01 Aug, 2025"
        // Also handling possible newlines between date and time
        val dateRegex = Regex(
            """(\d{1,2}\s+[A-Za-z]{3},?\s+\d{4})\s*(\d{1,2}:\d{2}\s+[AP]M)""",
            RegexOption.IGNORE_CASE
        )

        val matches = dateRegex.findAll(text).toList()
        if (matches.isEmpty()) return emptyList()

        val allRows = mutableListOf<String>()
        for (i in matches.indices) {
            val start = matches[i].range.first
            val end = if (i + 1 < matches.size) matches[i + 1].range.first else text.length
            allRows.add(text.substring(start, end).replace(Regex("""\s+"""), " "))
        }

        val amountRegex = Regex("""(?:₹|Rs\.?)\s*([0-9,]+(?:\.\d+)?)""", RegexOption.IGNORE_CASE)

        for (row in allRows) {
            val dateMatch = dateRegex.find(row) ?: continue
            val dateStr = dateMatch.groupValues[1]
            val timeStr = dateMatch.groupValues[2]

            val dateTime = try {
                val cleanedDate = dateStr.replace(",", "")
                LocalDateTime.parse(
                    "$cleanedDate $timeStr",
                    DateTimeFormatter.ofPattern("d MMM yyyy h:mm a", Locale.ENGLISH)
                )
            } catch (e: Exception) {
                try {
                    val cleanedDate = dateStr.replace(",", "")
                    LocalDateTime.parse(
                        "$cleanedDate $timeStr",
                        DateTimeFormatter.ofPattern("dd MMM yyyy hh:mm a", Locale.ENGLISH)
                    )
                } catch (e2: Exception) {
                    continue
                }
            }

            val amountMatch = amountRegex.find(row)
            val amountStr = amountMatch?.groupValues?.get(1)?.replace(",", "") ?: continue
            val amount = BigDecimal(amountStr)

            val isIncome = row.contains("Received from", ignoreCase = true)
            val type = if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE

            // Non-greedy merchant extraction
            val merchantMatch = if (isIncome) {
                Regex(
                    """(?:Received from|Paid by)\s+(.+?)(?=\s+UPI Transaction ID|Paid to|Paid by|$)""",
                    RegexOption.IGNORE_CASE
                ).find(row)
            } else {
                Regex(
                    """Paid to\s+(.+?)(?=\s+UPI Transaction ID|Paid to|Paid by|$)""",
                    RegexOption.IGNORE_CASE
                ).find(row)
            }

            val merchant = merchantMatch?.groupValues?.get(1)?.trim() ?: "Unknown"

            // Bank info is usually at the end before the amount
            // For Expense: "Paid by [Bank] [Last4] [Amount]"
            // For Income: "Paid to [Bank] [Last4] [Amount]"
            val bankMatch = if (isIncome) {
                Regex(
                    """Paid to\s+(.+?)\s+(\d{4})(?=\s*[₹Rs])""",
                    RegexOption.IGNORE_CASE
                ).find(row)
            } else {
                Regex(
                    """Paid by\s+(.+?)\s+(\d{4})(?=\s*[₹Rs])""",
                    RegexOption.IGNORE_CASE
                ).find(row)
            }
            val bankName = bankMatch?.groupValues?.get(1)?.trim()
            val accountLast4 = bankMatch?.groupValues?.get(2)

            val upiMatch = Regex("""UPI Transaction ID:\s*(\d+)""").find(row)
            val upiId = upiMatch?.groupValues?.get(1)

            val originalMessage = buildString {
                if (isIncome) {
                    append("Received from $merchant\n")
                } else {
                    append("Paid to $merchant\n")
                }
                if (upiId != null) append("UPI Transaction ID: $upiId\n")
                if (bankName != null && accountLast4 != null) {
                    if (isIncome) {
                        append("Paid to $bankName $accountLast4")
                    } else {
                        append("Paid by $bankName $accountLast4")
                    }
                }
            }.trim()

            transactions.add(
                ParsedTransaction(
                    amount = amount,
                    type = type,
                    merchant = merchant,
                    reference = upiId,
                    accountLast4 = accountLast4,
                    bankName = bankName ?: "GPay",
                    smsBody = originalMessage,
                    timestamp = dateTime.atZone(java.time.ZoneId.systemDefault()).toInstant()
                        .toEpochMilli(),
                    sender = "GPay PDF",
                    balance = null
                )
            )
        }

        return transactions
    }
}
