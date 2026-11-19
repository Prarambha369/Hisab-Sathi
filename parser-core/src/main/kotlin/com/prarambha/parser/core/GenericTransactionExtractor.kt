package com.prarambha.parser.core

import java.math.BigDecimal

/**
 * A cautious first reading of an SMS that no bank parser claims.
 *
 * This is deliberately not a bank parser and is never registered in
 * [com.prarambha.parser.core.bank.BankParserFactory]: its output is offered to the user as a
 * suggestion, and only a confirmed entry is stored.
 *
 * A reading needs a currency-tagged sum and a word saying which way the money went. Extra
 * corroboration raises [Estimate.score] by 20 for an account/card number, 20 for a merchant
 * and 10 for a running balance or available limit, so [WORTH_SUGGESTING] means "the base
 * plus at least one other fact" — a bare "debited 500" is reported at 50 but not offered.
 *
 * Money is read only in the simple "1,234.56" form. Nepali rupee wording (`Rs.`, `Rs`, `रू`)
 * reads as NPR. Messages that are OTPs, promos, requests, due notices, declined attempts or
 * person-to-person numbers never produce a reading at all.
 */
object GenericTransactionExtractor {

    /** The score at which a reading has enough corroboration to be shown as a suggestion. */
    const val WORTH_SUGGESTING = 60

    private const val BASE_SCORE = 50
    private const val ACCOUNT_BONUS = 20
    private const val MERCHANT_BONUS = 20
    private const val BALANCE_BONUS = 10

    data class Estimate(val transaction: ParsedTransaction, val score: Int)

    fun estimate(sender: String, message: String, timestamp: Long = 0L): Estimate? {
        if (neverTransaction(sender, message)) return null

        val mentions = amountsIn(message)
        val moved = mentions.firstOrNull { it.role == Role.MOVED } ?: return null
        val direction = directionOf(message) ?: return null

        // A balance is only trusted when it is written in the same currency as the movement.
        val balance = mentions.firstOrNull { it.role == Role.BALANCE && it.currency == moved.currency }?.value
        val limit = mentions.firstOrNull { it.role == Role.LIMIT && it.currency == moved.currency }?.value
        val account = accountLast4(message)
        val merchant = merchantOf(message)

        val score = BASE_SCORE +
            (if (account != null) ACCOUNT_BONUS else 0) +
            (if (merchant != null) MERCHANT_BONUS else 0) +
            (if (balance != null || limit != null) BALANCE_BONUS else 0)

        return Estimate(
            ParsedTransaction(
                amount = moved.value,
                type = direction,
                merchant = merchant,
                reference = null,
                accountLast4 = account,
                balance = balance,
                creditLimit = limit,
                smsBody = message,
                sender = sender,
                timestamp = timestamp,
                bankName = sender,
                currency = moved.currency
            ),
            score
        )
    }

    /**
     * Money out or in, by the first direction word in [message]. A reversal or refund that has
     * already happened is money back in, whatever the original purchase wording says; one that
     * may happen later ("will be reversed if…") is still the original movement.
     */
    fun directionOf(message: String): TransactionType? {
        if (REFUND_COMPLETED.containsMatchIn(message)) return TransactionType.INCOME

        val out = OUTBOUND.find(message)?.range?.first
        val into = INBOUND.find(message)?.range?.first
        return when {
            out == null && into == null -> null
            into == null -> TransactionType.EXPENSE
            out == null -> TransactionType.INCOME
            out < into -> TransactionType.EXPENSE
            else -> TransactionType.INCOME
        }
    }

    /** True when the message can never describe money moving, whoever sent it. */
    fun neverTransaction(sender: String, message: String): Boolean =
        isPersonalNumber(sender) || SmsFilter.isNonTransactionMessage(message) || FAILED.containsMatchIn(message)

    /** True when [text] says a payment did not go through or was undone. */
    internal fun saysFailure(text: String): Boolean =
        FAILED.containsMatchIn(text) || UNDONE.containsMatchIn(text)

    // ─── Money ────────────────────────────────────────────────────────────────

    private enum class Role { MOVED, BALANCE, LIMIT }

    private data class Mention(val value: BigDecimal, val currency: String, val role: Role)

    /** Every currency-tagged sum in [message], in the order written, each with what it is. */
    private fun amountsIn(message: String): List<Mention> {
        val mentions = ArrayList<Mention>()
        for (match in MONEY.findAll(message)) {
            val tag = match.groups[1]?.value ?: match.groups[2]?.value ?: match.groups[5]?.value ?: continue
            val written = match.groups[3]?.value ?: match.groups[4]?.value ?: continue
            val value = written.replace(",", "").toBigDecimalOrNull() ?: continue
            if (value.signum() <= 0) continue
            val currency = CurrencyText.tagOf(tag) ?: continue
            mentions += Mention(value, currency, roleAt(message, match.range.first))
        }
        return mentions
    }

    /**
     * What the sum starting at [at] stands for, decided by the words just before it: a
     * balance or limit word with no other number in between, otherwise money that moved.
     */
    private fun roleAt(message: String, at: Int): Role {
        val before = message.substring((at - 30).coerceAtLeast(0), at)
        val words = before.takeLastWhile { !it.isDigit() }.lowercase()
        return when {
            LIMIT_WORDS.any { it in words } -> Role.LIMIT
            BALANCE_WORDS.any { it in words } -> Role.BALANCE
            else -> Role.MOVED
        }
    }

    // ─── Account ──────────────────────────────────────────────────────────────

    private fun accountLast4(message: String): String? =
        MASKED_DIGITS.find(message)?.groupValues?.get(1)
            ?: ENDING_DIGITS.find(message)?.groupValues?.get(1)
            ?: NAMED_ACCOUNT_DIGITS.find(message)?.groupValues?.get(1)?.takeLast(4)

    // ─── Merchant ─────────────────────────────────────────────────────────────

    private fun merchantOf(message: String): String? {
        for (match in MERCHANT.findAll(message)) {
            val name = tidy(match.groupValues[1])
            if (usable(name)) return name
        }
        return null
    }

    private fun tidy(raw: String): String =
        raw.trim().replace(RUN_OF_SPACES, " ").trim(' ', '.', ',', ';', ':', '-')

    /**
     * A suggestion has to be about to be confirmed by a person, so only a name that reads like
     * a name is offered: no account phrase, article, stop word, price or currency tag.
     */
    private fun usable(name: String): Boolean {
        if (name.length < 3 || name.none { it.isLetter() }) return false
        if (name.contains('@')) return false
        if (ACCOUNT_PHRASE.containsMatchIn(name)) return false
        if (name.substringBefore(' ').uppercase() in STOP_WORDS) return false
        if (DECIMAL_NUMBER.containsMatchIn(name)) return false
        if (name.split(' ').any { CurrencyText.tagOf(it.trim('.', ',', ':', ';')) != null }) return false
        return true
    }

    private fun isPersonalNumber(sender: String): Boolean =
        PERSONAL_NUMBER.matches(sender.replace(" ", ""))

    // ─── Patterns ─────────────────────────────────────────────────────────────

    private const val NUMBER = """(\d[\d,]*(?:\.\d{1,2})?)"""

    // A code or symbol before the sum, or the code after it. Codes stay upper case, so
    // "try 2 times" never reads as TRY 2.
    private val MONEY = Regex(
        """(?:\b([A-Z]{3}|[Rr][Ss])\.?|([₹$€£रूरु]))\s*$NUMBER(?![\d.])""" +
            """|(?<![\w.,])$NUMBER\s*([A-Z]{3})\b"""
    )

    private val OUTBOUND = Regex(
        """\b(?:debited|spent|purchased?|paid|withdrawn|charged|deducted|sent|used|transferred to)\b""",
        RegexOption.IGNORE_CASE
    )
    private val INBOUND = Regex(
        """\b(?:credited|received|deposited|refunded|refund|reversed)\b""",
        RegexOption.IGNORE_CASE
    )
    private val REFUND_COMPLETED = Regex(
        """\b(?:has been|have been|was|were|is|got)\s+(?:reversed|refunded)\b|\bcredited back\b|\brefunded to\b""",
        RegexOption.IGNORE_CASE
    )
    private val FAILED = Regex(
        """\b(?:declined|failed|unsuccessful|rejected|could not be processed|was not processed)\b""",
        RegexOption.IGNORE_CASE
    )
    private val UNDONE = Regex(
        """\b(?:reversed|reversal|refund|refunded|cancelled|canceled|voided)\b""",
        RegexOption.IGNORE_CASE
    )

    private val MASKED_DIGITS = Regex("""(?:[Xx*•]{2,}|\.{2,})(\d{4})\b""")
    private val ENDING_DIGITS = Regex("""\bending\s+(\d{4})\b""", RegexOption.IGNORE_CASE)
    private val NAMED_ACCOUNT_DIGITS = Regex("""\b(?:a/c|acct|account)\b[^\d]{0,8}(\d{6,})\b""", RegexOption.IGNORE_CASE)

    // The merchant starts after at/to/from/towards and stops at the next piece of bank wording.
    private val MERCHANT = Regex(
        """\b(?:at|to|from|towards)\s+([A-Za-z0-9&' .-]{2,60}?)""" +
            """(?=\s+(?:on|at|via|using|with|ref|for|dated|avl|avail|available|bal|balance|txn|from|to|card|ending|a/c|acct|account)\b|[.,;:\n]|$)""",
        RegexOption.IGNORE_CASE
    )

    private val PERSONAL_NUMBER = Regex("""\+?\d{10,}""")
    private val RUN_OF_SPACES = Regex("""\s{2,}""")
    private val DECIMAL_NUMBER = Regex("""\d[\d,]*[.,]\d+|\d,\d""")
    private val ACCOUNT_PHRASE = Regex("""^(?:your|you|a/c|ac|acct|account|card|the)\b""", RegexOption.IGNORE_CASE)

    private val STOP_WORDS = setOf(
        "YOUR", "THE", "A/C", "AC", "ACCT", "ACCOUNT", "CARD",
        "USING", "VIA", "BY", "WITH", "FOR", "TO", "FROM", "AT"
    )

    private val LIMIT_WORDS = listOf("limit", "lmt", "सीमा")
    private val BALANCE_WORDS = listOf("bal", "balance", "available", "avl", "बाँकी", "बाकी", "मौज्दात")
}
