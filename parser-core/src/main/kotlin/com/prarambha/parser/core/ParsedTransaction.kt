package com.prarambha.parser.core

import java.math.BigDecimal
import java.security.MessageDigest

data class ParsedTransaction(
    val amount: BigDecimal,
    val type: TransactionType,
    val merchant: String?,
    val reference: String?,
    val accountLast4: String?,
    val balance: BigDecimal?,
    val creditLimit: BigDecimal? = null,
    val smsBody: String,
    val sender: String,
    val timestamp: Long,
    val bankName: String,
    val transactionHash: String? = null,
    val isFromCard: Boolean = false,
    val currency: String = "NPR",
    val fromAccount: String? = null,
    val toAccount: String? = null,
    val cardType: String? = null,
    val dueDate: Long? = null,
    val minDue: BigDecimal? = null
) {
    /**
     * Generates a stable unique transaction identifier for deduplication.
     * Incorporates reference, account, balance, merchant, type, sender, amount, day, and body hash
     * to eliminate collision issues for multiple transactions on the same day.
     */
    fun generateTransactionId(): String {
        val normalizedAmount = amount.setScale(2, java.math.RoundingMode.HALF_UP)
        val smsBodyHash = MessageDigest.getInstance("MD5")
            .digest(smsBody.toByteArray())
            .joinToString("") { "%02x".format(it) }
        val refPart = reference?.trim()?.takeIf { it.isNotEmpty() } ?: ""
        val accPart = accountLast4?.trim()?.takeIf { it.isNotEmpty() } ?: ""
        val balPart = balance?.setScale(2, java.math.RoundingMode.HALF_UP)?.toString() ?: ""
        val merchantPart = merchant?.trim()?.lowercase() ?: ""
        val dayBucket = timestamp / (24 * 60 * 60 * 1000)

        val data = "$sender|$normalizedAmount|$dayBucket|$type|$refPart|$accPart|$balPart|$merchantPart|$smsBodyHash"
        return MessageDigest.getInstance("MD5")
            .digest(data.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    /**
     * Legacy hash calculation for backward compatibility with transactions
     * stored prior to the collision fix.
     */
    fun generateLegacyTransactionId(): String {
        val normalizedAmount = amount.setScale(2, java.math.RoundingMode.HALF_UP)
        val smsBodyHash = MessageDigest.getInstance("MD5")
            .digest(smsBody.toByteArray())
            .joinToString("") { "%02x".format(it) }
            .take(16)
        val data = "$sender|$normalizedAmount|${timestamp / (24 * 60 * 60 * 1000)}|$smsBodyHash"
        return MessageDigest.getInstance("MD5")
            .digest(data.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }
}
