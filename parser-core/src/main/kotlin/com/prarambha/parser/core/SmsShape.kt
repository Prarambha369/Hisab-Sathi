package com.prarambha.parser.core

import java.math.BigDecimal

/**
 * Teaching an unparsed SMS by tapping it.
 *
 * A user picks the amount (and optionally the merchant and account) out of a real message.
 * What is left between the picks becomes a *shape*: the fixed wording has to appear again
 * word for word, and the parts that change (a date, a reference, a balance) are read past.
 * Later messages are read only when the whole shape fits, so an unfamiliar message reads as
 * nothing rather than as a wrong amount.
 *
 * Nothing a user taps is ever compiled into a pattern: segments are compared literally, and
 * free text (the merchant) is bounded before it is captured. A taught shape therefore cannot
 * hang on a hostile message or turn "Rs." into a wildcard.
 *
 * A shape understands the wording, not the meaning: two messages that differ only in a
 * status number fit the same shape. That is why a taught message only ever produces a
 * suggestion for the user to confirm, never an automatic entry.
 */
enum class ShapeSlot {
    /** Wording that has to appear exactly, including currency codes and papers like "approved". */
    FIXED,

    /** The money that moved. Exactly one per shape. */
    AMOUNT,

    /** Free text between two fixed pieces: who was paid. */
    MERCHANT,

    /** A run of digits: the account or card the movement happened on. */
    ACCOUNT,

    /** A number nobody tapped (a date, a time, a balance). Read past, never kept. */
    FILLER,

    /** One token mixing digits and letters (a reference, a written date). Read past, never kept. */
    CODE,

    /** A month, weekday or am/pm beside a number, so the shape can survive a changing date. */
    DATE
}

data class ShapeSegment(val slot: ShapeSlot = ShapeSlot.FIXED, val literal: String = "")

/** How a sender writes the decimals of an amount. */
enum class DecimalStyle { POINT, COMMA }

data class TaughtShape(
    val id: String,
    val senderKey: String,
    val segments: List<ShapeSegment>,
    val direction: TransactionType,
    val currency: String,
    val bankLabel: String,
    val decimalStyle: DecimalStyle? = null,
    val defaultMerchant: String? = null
)

/** The ranges a user tapped, measured on the message exactly as received. */
data class TapChoice(
    val amount: IntRange,
    val merchant: IntRange? = null,
    val account: IntRange? = null
)

/** One selectable word or number, as a range of the original message. */
data class TapWord(val range: IntRange, val numeric: Boolean)

/** A first, tentative reading of a message, offered so the user only has to correct it. */
data class ShapeSuggestion(
    val amount: IntRange? = null,
    val merchant: IntRange? = null,
    val account: IntRange? = null,
    val direction: TransactionType? = null,
    val currency: String? = null
)

object SmsShapes {

    private const val MAX_BODY = 2000
    private const val MAX_MERCHANT = 60
    private const val MAX_DIGITS = 15
    private const val MAX_CODE = 64
    private const val MIN_FIXED_LETTERS = 3

    // Currencies with three decimals, where one separator before three digits may be either.
    private val THREE_DECIMAL_CURRENCIES = setOf("BHD", "IQD", "JOD", "KWD", "LYD", "OMR", "TND")

    // ─── Teaching ─────────────────────────────────────────────────────────────

    /**
     * Turns [taps] on [example] into a reusable shape, or returns null when the example
     * cannot be read back to exactly what was tapped. A shape that cannot reproduce its
     * own example is not offered: no shape is better than a wrong one.
     */
    fun learn(
        id: String,
        senderKey: String,
        example: String,
        taps: TapChoice,
        direction: TransactionType,
        bankLabel: String,
        currency: String = "NPR",
        decimalStyle: DecimalStyle? = null,
        defaultMerchant: String? = null
    ): TaughtShape? {
        if (example.length > MAX_BODY || !isCurrencyCode(currency)) return null

        val folded = Folded.of(example)
        val text = folded.text
        val amount = folded.fold(taps.amount) ?: return null
        if (isGroupTail(text, amount) || isBareFraction(text, amount)) return null
        val merchant = taps.merchant?.let { folded.fold(it) ?: return null }
        val account = taps.account?.let { folded.fold(it) ?: return null }

        val picks = listOfNotNull(
            amount to ShapeSlot.AMOUNT,
            merchant?.let { it to ShapeSlot.MERCHANT },
            account?.let { it to ShapeSlot.ACCOUNT }
        ).sortedBy { it.first.first }
        if (picks.zipWithNext().any { (l, r) -> l.first.last >= r.first.first }) return null

        val segments = ArrayList<ShapeSegment>()
        var cursor = 0
        for ((range, slot) in picks) {
            maskGap(text.substring(cursor, range.first), segments)
            segments += ShapeSegment(slot)
            cursor = range.last + 1
        }
        maskGap(text.substring(cursor), segments)
        val shapeSegments = mergeFixed(segments)

        if (shapeSegments.count { it.slot == ShapeSlot.MERCHANT } > 1) return null
        val merchantAt = shapeSegments.indexOfFirst { it.slot == ShapeSlot.MERCHANT }
        if (merchantAt >= 0) {
            // A merchant ends where the next fixed text starts; with none there, only the message's end can stop it.
            val next = shapeSegments.getOrNull(merchantAt + 1)
            if (next != null && next.slot != ShapeSlot.FIXED) return null
        }
        if (fixedLetters(shapeSegments) < MIN_FIXED_LETTERS) return null

        val shape = TaughtShape(
            id = id,
            senderKey = senderKey,
            segments = shapeSegments,
            direction = direction,
            currency = currency,
            bankLabel = bankLabel,
            decimalStyle = decimalStyle,
            defaultMerchant = defaultMerchant
        )
        val expected = Reading(
            amount = text.substring(amount.first, amount.last + 1),
            merchant = merchant?.let { text.substring(it.first, it.last + 1).trim() },
            account = account?.let { text.substring(it.first, it.last + 1) }
        )
        if (walk(shape.segments, text) != expected) return null
        if (parseDecimal(expected.amount.orEmpty(), decimalStyle, currency) == null) return null
        return shape
    }

    // ─── Reading later messages ───────────────────────────────────────────────

    /**
     * [message] as [shape] reads it, or null unless every segment fits in exactly one way.
     * The message is kept verbatim in the result: it is what identifies the transaction.
     */
    fun read(shape: TaughtShape, message: String, sender: String, timestamp: Long): ParsedTransaction? {
        if (message.length > MAX_BODY) return null
        if (shape.segments.count { it.slot == ShapeSlot.MERCHANT } > 1) return null
        val reading = walk(shape.segments, Folded.of(message).text) ?: return null
        val amount = parseDecimal(reading.amount ?: return null, shape.decimalStyle, shape.currency) ?: return null

        return ParsedTransaction(
            amount = amount,
            type = shape.direction,
            merchant = reading.merchant ?: shape.defaultMerchant,
            reference = null,
            accountLast4 = reading.account?.takeLast(4),
            balance = null,
            smsBody = message,
            sender = sender,
            timestamp = timestamp,
            bankName = shape.bankLabel,
            currency = shape.currency
        )
    }

    /**
     * The one reading all of [shapes] agree on. When two shapes fit and disagree, only the
     * user can say which applies, so nothing is returned.
     */
    fun readConsistent(shapes: List<TaughtShape>, message: String, sender: String, timestamp: Long): ParsedTransaction? {
        val readings = shapes.mapNotNull { read(it, message, sender, timestamp) }
        val first = readings.firstOrNull() ?: return null
        val agreed = readings.all {
            it.amount.compareTo(first.amount) == 0 &&
                it.type == first.type &&
                it.currency == first.currency &&
                it.merchant == first.merchant &&
                it.accountLast4 == first.accountLast4
        }
        return first.takeIf { agreed }
    }

    // ─── Amounts ──────────────────────────────────────────────────────────────

    /**
     * [written] as a value, or null when its separators do not fit [style], or when [style]
     * is unknown and they could mean two different things.
     */
    fun parseDecimal(written: String, style: DecimalStyle?, currency: String): BigDecimal? {
        val text = Folded.of(written).text
        if (text.isEmpty()) return null

        val hasSeparator = text.any { it == '.' || it == ',' }
        val effective = style ?: if (hasSeparator) detectDecimalStyle(text, currency) ?: return null else DecimalStyle.POINT
        val decimalMark = if (effective == DecimalStyle.POINT) '.' else ','
        val groupMark = if (effective == DecimalStyle.POINT) ',' else '.'

        val parts = text.split(decimalMark)
        if (parts.size > 2) return null
        val whole = parts[0]
        val fraction = parts.getOrNull(1).orEmpty()
        if (parts.size == 2) {
            if (fraction.isEmpty() || fraction.length > 3 || fraction.any { !isDigit(it) }) return null
        }
        if (!wholeIsGrouped(whole, groupMark)) return null

        val digits = whole.replace(groupMark.toString(), "")
        if (digits.isEmpty() || digits.length > MAX_DIGITS) return null
        val value = try {
            BigDecimal(if (fraction.isEmpty()) digits else "$digits.$fraction")
        } catch (e: NumberFormatException) {
            return null
        }
        return value.takeIf { it.signum() > 0 }
    }

    /**
     * What the separators of [written] say, or null when there are none or when a single
     * separator before three digits could be a decimal point or a thousands mark.
     */
    fun detectDecimalStyle(written: String, currency: String): DecimalStyle? {
        val text = Folded.of(written).text
        val dots = text.count { it == '.' }
        val commas = text.count { it == ',' }
        return when {
            dots > 0 && commas > 0 ->
                if (text.lastIndexOf('.') > text.lastIndexOf(',')) DecimalStyle.POINT else DecimalStyle.COMMA
            dots > 1 -> DecimalStyle.COMMA // "1.234.567": the dots group thousands
            commas > 1 -> DecimalStyle.POINT // "1,234,567"
            dots + commas == 0 -> null
            else -> {
                val mark = if (dots == 1) '.' else ','
                val trailing = text.length - text.indexOf(mark) - 1
                when {
                    trailing != 3 -> if (dots == 1) DecimalStyle.POINT else DecimalStyle.COMMA
                    currency in THREE_DECIMAL_CURRENCIES -> null
                    else -> if (dots == 1) DecimalStyle.COMMA else DecimalStyle.POINT
                }
            }
        }
    }

    /** True when only the user can say what the separator in [written] means for [currency]. */
    fun isDecimalStyleAmbiguous(written: String, currency: String): Boolean {
        val text = Folded.of(written).text
        val hasSeparator = text.any { it == '.' || it == ',' }
        return hasSeparator && detectDecimalStyle(text, currency) == null
    }

    // ─── Helping the user tap ─────────────────────────────────────────────────

    /** Every word and number of [message] a tap can land on, in reading order. */
    fun tapWords(message: String): List<TapWord> {
        val folded = Folded.of(message)
        val text = folded.text
        val words = ArrayList<TapWord>()
        var i = 0
        while (i < text.length) {
            val c = text[i]
            when {
                isDigit(c) -> {
                    val end = numberEnd(text, i, AMOUNT_SEPARATORS)
                    words += TapWord(folded.original(i until end), numeric = true)
                    i = end
                }
                isWordStart(c) -> {
                    var end = i
                    while (end < text.length && !text[end].isWhitespace() && !isDigit(text[end])) end++
                    while (end > i && !isWordStart(text[end - 1])) end--
                    if (end == i) {
                        i++
                    } else {
                        words += TapWord(folded.original(i until end), numeric = false)
                        i = end
                    }
                }
                else -> i++
            }
        }
        return words
    }

    /**
     * A first reading of [message]: the single priced number, plus a merchant and account
     * when exactly one of each is plausible. Anything uncertain stays null for the user to tap.
     */
    fun suggest(sender: String, message: String): ShapeSuggestion {
        if (message.length > MAX_BODY) return ShapeSuggestion()
        val folded = Folded.of(message)
        val text = folded.text

        val estimate = GenericTransactionExtractor.estimate(sender, text)?.transaction
        val numbers = numberRanges(text)
        val priced = numbers.filter {
            currencyNear(text, it) != null && !isFigure(text, it) && !isGroupTail(text, it) && !isBareFraction(text, it)
        }
        // Two different prices: only the user can say which one moved.
        val amount = priced.singleOrNull()

        val merchant = (estimate?.merchant ?: merchantHint(text))?.let { name ->
            val at = text.indexOf(name)
            if (at >= 0 && text.indexOf(name, at + 1) < 0) at until at + name.length else null
        }
        val account = estimate?.accountLast4?.let { last4 ->
            numbers.filter { text.substring(it.first, it.last + 1).endsWith(last4) }.singleOrNull()
        }

        return ShapeSuggestion(
            amount = amount?.let(folded::original),
            merchant = merchant?.let(folded::original),
            account = account?.takeIf { it != amount }?.let(folded::original),
            direction = estimate?.type ?: GenericTransactionExtractor.directionOf(text),
            currency = amount?.let { currencyNear(text, it) }
        )
    }

    /** The currency written beside [range] of [message], when exactly one tag names it. */
    fun currencyBeside(message: String, range: IntRange): String? {
        val folded = Folded.of(message)
        val local = folded.fold(range) ?: return null
        return currencyNear(folded.text, local)
    }

    /** A message worth offering to teach: it shows a priced number and is not an OTP, promo or failure. */
    fun worthOffering(sender: String, message: String): Boolean {
        if (GenericTransactionExtractor.neverTransaction(sender, message)) return false
        val text = Folded.of(message).text
        return numberRanges(text).any { currencyNear(text, it) != null }
    }

    /**
     * The words around [range] of [message], for showing where an amount came from.
     * The result never starts or ends inside a word.
     */
    fun contextAround(message: String, range: IntRange, wordsEachSide: Int = 2): IntRange {
        val words = tapWords(message)
        val span = wordsEachSide.coerceIn(0, words.size)
        val firstInside = words.indexOfFirst { it.range.first <= range.first && range.first <= it.range.last }
        if (firstInside < 0) return range
        val lastInside = words.indexOfFirst { it.range.first <= range.last && range.last <= it.range.last }
            .takeIf { it >= 0 } ?: firstInside
        val from = words[(firstInside - span).coerceAtLeast(0)].range.first
        val to = words[(lastInside + span).coerceAtMost(words.lastIndex)].range.last
        return from..to
    }

    // ─── Reading internals ────────────────────────────────────────────────────

    private data class Reading(val amount: String? = null, val merchant: String? = null, val account: String? = null)

    private fun walk(segments: List<ShapeSegment>, text: String): Reading? =
        walkFrom(segments, 0, 0, text, Reading())

    private fun walkFrom(segments: List<ShapeSegment>, from: Int, start: Int, text: String, found: Reading): Reading? {
        var position = start
        var reading = found
        var index = from
        while (index < segments.size) {
            val segment = segments[index]
            when (segment.slot) {
                ShapeSlot.FIXED -> {
                    if (!text.startsWith(segment.literal, position)) return null
                    position += segment.literal.length
                }
                ShapeSlot.AMOUNT -> {
                    val end = numberEnd(text, position, AMOUNT_SEPARATORS)
                    // "1" of "1 234.50" is a number too, and would leave the rest misaligned.
                    if (end <= position || isGroupTail(text, position until end)) return null
                    reading = reading.copy(amount = text.substring(position, end))
                    position = end
                }
                ShapeSlot.ACCOUNT -> {
                    var end = position
                    while (end < text.length && isDigit(text[end])) end++
                    val length = end - position
                    if (length < 2 || length > MAX_DIGITS + 5) return null
                    reading = reading.copy(account = text.substring(position, end))
                    position = end
                }
                ShapeSlot.FILLER -> {
                    val end = numberEnd(text, position, ANY_NUMBER_SEPARATORS)
                    if (end <= position) return null
                    position = end
                }
                ShapeSlot.CODE -> {
                    val end = codeEnd(text, position)
                    if (end <= position) return null
                    position = end
                }
                ShapeSlot.DATE -> {
                    var end = position
                    while (end < text.length && text[end].isLetter()) end++
                    if (end == position || text.substring(position, end).lowercase() !in DATE_WORDS) return null
                    position = end
                }
                ShapeSlot.MERCHANT -> return walkMerchant(segments, index, position, text, reading)
            }
            index++
        }
        return reading.takeIf { position == text.length }
    }

    /**
     * A merchant is free text, so its end is decided by the next fixed segment. When that
     * text occurs more than once in the remainder ("Shop on Main on 12/10"), each placement
     * is tried; if two of them complete the shape, the message is ambiguous and reads as nothing.
     */
    private fun walkMerchant(
        segments: List<ShapeSegment>,
        index: Int,
        position: Int,
        text: String,
        found: Reading
    ): Reading? {
        val next = segments.getOrNull(index + 1)
        val amountBefore = touchesAmount(segments, index, -1)
        if (next == null) {
            val name = merchantName(text.substring(position), amountBefore, false) ?: return null
            return found.copy(merchant = name)
        }
        if (next.slot != ShapeSlot.FIXED || next.literal.isEmpty()) return null
        val amountAfter = touchesAmount(segments, index, 1)

        var agreed: Reading? = null
        var at = text.indexOf(next.literal, position)
        while (at >= 0 && at - position <= MAX_MERCHANT) {
            val name = merchantName(text.substring(position, at), amountBefore, amountAfter)
            if (name != null) {
                val rest = walkFrom(segments, index + 1, at, text, found.copy(merchant = name))
                if (rest != null) {
                    if (agreed != null) return null
                    agreed = rest
                }
            }
            at = text.indexOf(next.literal, at + 1)
        }
        return agreed
    }

    /** True when the segment at [index]+[step] is the amount, separated only by blank fixed text. */
    private fun touchesAmount(segments: List<ShapeSegment>, index: Int, step: Int): Boolean {
        val between = segments.getOrNull(index + step) ?: return false
        return between.slot == ShapeSlot.FIXED &&
            between.literal.isBlank() &&
            segments.getOrNull(index + 2 * step)?.slot == ShapeSlot.AMOUNT
    }

    /**
     * A merchant cannot hide what changes the money: it may not carry a price, say the payment
     * failed, or end in a credit/debit marker. Against the amount, its first or last word has
     * to be a name rather than a currency code or a fragment of a grouped number.
     */
    private fun merchantName(raw: String, amountBefore: Boolean, amountAfter: Boolean): String? {
        val name = raw.trim()
        if (name.length > MAX_MERCHANT || name.none { it.isLetter() }) return null
        if (numberRanges(name).any { currencyNear(name, it) != null }) return null
        if (GenericTransactionExtractor.saysFailure(name)) return null

        val words = name.split(' ')
        if (words.drop(1).any { it.trim { c -> !c.isLetterOrDigit() }.uppercase() in LEDGER_MARKS }) return null
        if (amountBefore && !isNameWord(words.first())) return null
        if (amountAfter && !isNameWord(words.last())) return null
        return name
    }

    private fun isNameWord(word: String): Boolean =
        word.none(::isDigit) && word.any { it.isLetter() } && currencyTag(word.trim('.', ',', ':', ';')) == null

    /** Digits joined by single separators; always ends on a digit, so trailing punctuation stays out. */
    private fun numberEnd(text: String, from: Int, separators: String): Int {
        var i = from
        while (i < text.length && isDigit(text[i])) i++
        if (i == from) return from
        while (i + 1 < text.length && text[i] in separators && isDigit(text[i + 1])) {
            i++
            while (i < text.length && isDigit(text[i])) i++
        }
        return i
    }

    /** One token of letters, digits and joiners, with at least one digit in it. */
    private fun codeEnd(text: String, from: Int): Int {
        var i = from
        var digit = false
        while (i < text.length) {
            val c = text[i]
            if (c.isLetterOrDigit()) {
                digit = digit || isDigit(c)
                i++
            } else if (c in CODE_JOINERS && i + 1 < text.length && text[i + 1].isLetterOrDigit()) {
                i++
            } else {
                break
            }
        }
        if (!digit || i - from > MAX_CODE) return from
        if (GenericTransactionExtractor.saysFailure(text.substring(from, i))) return from
        return i
    }

    /** "1,234,567", "1.234,56" and Nepal's own "1,00,000": groups are only groups when they add up. */
    private fun wholeIsGrouped(whole: String, groupMark: Char): Boolean {
        if (whole.isEmpty()) return false
        val groups = whole.split(groupMark)
        if (groups.any { group -> group.isEmpty() || group.any { !isDigit(it) } }) return false
        if (groups.size == 1) return true
        // "0,500" is half of something written with the wrong mark, not five hundred.
        if (groups.first().startsWith('0')) return false
        if (groups.first().length > 3 || groups.last().length != 3) return false
        return groups.subList(1, groups.size - 1).all { it.length in 2..3 }
    }

    // ─── Building internals ───────────────────────────────────────────────────

    /** Turns the text between picks into fixed wording, and what changes between messages into read-past slots. */
    private fun maskGap(gap: String, out: MutableList<ShapeSegment>) {
        val words = ArrayList<IntRange>()
        var i = 0
        while (i < gap.length) {
            if (gap[i].isWhitespace()) {
                i++
                continue
            }
            val start = i
            while (i < gap.length && !gap[i].isWhitespace()) i++
            words += start until i
        }

        var cursor = 0
        words.forEachIndexed { index, word ->
            if (word.first > cursor) out += ShapeSegment(ShapeSlot.FIXED, gap.substring(cursor, word.first))
            val token = gap.substring(word.first, word.last + 1)
            val before = words.getOrNull(index - 1)?.let { gap.substring(it.first, it.last + 1) }
            val after = words.getOrNull(index + 1)?.let { gap.substring(it.first, it.last + 1) }
            maskToken(token, before, after, out)
            cursor = word.last + 1
        }
        if (cursor < gap.length) out += ShapeSegment(ShapeSlot.FIXED, gap.substring(cursor))
    }

    private fun maskToken(token: String, before: String?, after: String?, out: MutableList<ShapeSegment>) {
        val core = token.trim { !it.isLetterOrDigit() }
        val coreAt = token.indexOf(core).coerceAtLeast(0)
        val lead = token.substring(0, coreAt)
        val trail = token.substring(lead.length + core.length)

        if (token.none(::isDigit)) {
            val besideNumber = before?.any(::isDigit) == true || after?.any(::isDigit) == true
            if (besideNumber && core.lowercase() in DATE_WORDS) {
                if (lead.isNotEmpty()) out += ShapeSegment(ShapeSlot.FIXED, lead)
                out += ShapeSegment(ShapeSlot.DATE)
                if (trail.isNotEmpty()) out += ShapeSegment(ShapeSlot.FIXED, trail)
            } else {
                out += ShapeSegment(ShapeSlot.FIXED, token)
            }
            return
        }

        val onlyNumber = numberRanges(token).singleOrNull()
        if (onlyNumber != null) {
            val prefix = token.substring(0, onlyNumber.first)
            val suffix = token.substring(onlyNumber.last + 1)
            // A currency or mask glued to the number stays literal: "Rs500.00", "XX1234", "500रू".
            val gluedIntoWords = prefix.any { it.isLetter() } && suffix.any { it.isLetter() }
            if (!gluedIntoWords) {
                if (prefix.isNotEmpty()) out += ShapeSegment(ShapeSlot.FIXED, prefix)
                out += ShapeSegment(ShapeSlot.FILLER)
                if (suffix.isNotEmpty()) out += ShapeSegment(ShapeSlot.FIXED, suffix)
                return
            }
        }
        // A reference or a written date: "AB12CD34", "12-Oct-26".
        if (lead.isNotEmpty()) out += ShapeSegment(ShapeSlot.FIXED, lead)
        out += ShapeSegment(ShapeSlot.CODE)
        if (trail.isNotEmpty()) out += ShapeSegment(ShapeSlot.FIXED, trail)
    }

    private fun mergeFixed(segments: List<ShapeSegment>): List<ShapeSegment> {
        val merged = ArrayList<ShapeSegment>(segments.size)
        for (segment in segments) {
            val last = merged.lastOrNull()
            if (segment.slot == ShapeSlot.FIXED && last?.slot == ShapeSlot.FIXED) {
                merged[merged.lastIndex] = ShapeSegment(ShapeSlot.FIXED, last.literal + segment.literal)
            } else if (segment.slot != ShapeSlot.FIXED || segment.literal.isNotEmpty()) {
                merged += segment
            }
        }
        return merged
    }

    private fun fixedLetters(segments: List<ShapeSegment>): Int =
        segments.filter { it.slot == ShapeSlot.FIXED }.sumOf { segment -> segment.literal.count { it.isLetter() } }

    // ─── Guessing internals ───────────────────────────────────────────────────

    private fun numberRanges(text: String, separators: String = AMOUNT_SEPARATORS): List<IntRange> {
        val ranges = ArrayList<IntRange>()
        var i = 0
        while (i < text.length) {
            if (isDigit(text[i])) {
                val end = numberEnd(text, i, separators)
                ranges += i until end
                i = end
            } else {
                i++
            }
        }
        return ranges
    }

    /** The currency tag printed next to [range], when the two sides agree on one. */
    private fun currencyNear(text: String, range: IntRange): String? {
        val left = text.substring((range.first - 12).coerceAtLeast(0), range.first)
            .trimEnd()
            .takeLastWhile { !it.isWhitespace() }
            .trimEnd('.', ':')
        val right = text.substring((range.last + 1).coerceAtMost(text.length))
            .trimStart()
            .takeWhile { !it.isWhitespace() }

        val before = currencyTag(left.takeLastWhile { !isDigit(it) })
        val after = currencyTag(right.trimEnd('.', ',', ':', ';', ')'))
        return when {
            before != null && after != null && before != after -> null
            else -> before ?: after
        }
    }

    private fun currencyTag(word: String): String? = CurrencyText.tagOf(word)

    /** A balance or a limit, recognised by the word just before it with no other number between. */
    private fun isFigure(text: String, range: IntRange): Boolean {
        val before = text.substring((range.first - 30).coerceAtLeast(0), range.first)
        val sinceNumber = before.takeLastWhile { !isDigit(it) }.lowercase()
        if (BALANCE_WORDS.any { it in sinceNumber }) return true
        // "Rs 900 available" says what the figure is after the number.
        val after = text.substring((range.last + 1).coerceAtMost(text.length)).take(24).lowercase()
        return BALANCE_WORDS_AFTER.any { it in after }
    }

    private fun merchantHint(text: String): String? {
        val found = MERCHANT_HINT.find(text) ?: return null
        val name = found.groupValues[1].trim()
        if (name.length > MAX_MERCHANT || name.any(::isDigit)) return null
        if (ACCOUNT_PHRASE.containsMatchIn(name)) return null
        return name
    }

    /**
     * One piece of a number written with spaces or apostrophes for thousands ("1 234.50").
     * Read alone it would be 1 or 234.50, so it is neither suggested nor taught.
     */
    private fun isGroupTail(text: String, range: IntRange): Boolean {
        fun joins(c: Char?) = c == ' ' || c == '\''
        val piece = text.substring(range.first, range.last + 1)
        val whole = piece.takeWhile(::isDigit)
        val before = text.getOrNull(range.first - 1)
        val after = text.getOrNull(range.last + 1)
        val leftDigits = if (range.first >= 2 && joins(before)) {
            text.substring(0, range.first - 1).takeLastWhile(::isDigit)
        } else {
            ""
        }
        val rightDigits = if (joins(after)) {
            text.substring((range.last + 2).coerceAtMost(text.length)).takeWhile(::isDigit)
        } else {
            ""
        }
        if (whole.length == 3 && leftDigits.length in 1..3) return true
        return piece.all(::isDigit) && whole.length in 1..3 && rightDigits.length == 3
    }

    /**
     * The 50 of ".50": alone it is a hundred times the amount. A separator directly after a
     * word is the currency's own ("Rs.50"), so only a separator at a message edge or after
     * punctuation makes a bare fraction.
     */
    private fun isBareFraction(text: String, range: IntRange): Boolean {
        if (text.getOrNull(range.first - 1)?.let { it in AMOUNT_SEPARATORS } != true) return false
        return text.getOrNull(range.first - 2)?.isLetterOrDigit() != true
    }

    // ─── Text ─────────────────────────────────────────────────────────────────

    /**
     * [text] with one spelling for digits and spaces, remembering where every kept character
     * came from so taps on the original message can be translated and back.
     */
    private class Folded(val text: String, private val origin: IntArray) {

        fun original(range: IntRange): IntRange = origin[range.first]..origin[range.last]

        /** [range] of the original message as a range of [text], or null when nothing of it survived. */
        fun fold(range: IntRange): IntRange? {
            if (range.isEmpty()) return null
            val first = origin.indexOfFirst { it >= range.first }
            if (first < 0) return null
            val last = origin.indexOfLast { it <= range.last }
            if (last < first) return null
            return first..last
        }

        companion object {
            fun of(raw: String): Folded {
                val text = StringBuilder(raw.length)
                val origin = IntArray(raw.length)
                var pendingSpace = -1

                for (i in raw.indices) {
                    val c = raw[i]
                    if (c in INVISIBLE_MARKS) continue
                    if (c.isWhitespace()) {
                        // Runs of spaces and line breaks collapse to one, at the first of them.
                        if (text.isNotEmpty() && pendingSpace < 0) pendingSpace = i
                        continue
                    }
                    if (pendingSpace >= 0) {
                        origin[text.length] = pendingSpace
                        text.append(' ')
                        pendingSpace = -1
                    }
                    origin[text.length] = i
                    text.append(if (c.isDigit()) ('0' + c.digitToInt()) else c)
                }
                return Folded(text.toString(), origin.copyOf(text.length))
            }
        }
    }

    private fun isDigit(c: Char) = c in '0'..'9'

    private fun isWordStart(c: Char) = c.isLetter() || c == '&' || c == '\''

    private fun isCurrencyCode(code: String) = code.length == 3 && code.all { it in 'A'..'Z' }

    private const val AMOUNT_SEPARATORS = ".,"
    private const val ANY_NUMBER_SEPARATORS = ".,:/-"
    private const val CODE_JOINERS = "-_/:.,"

    private val LEDGER_MARKS = setOf("CR", "DR")

    private val INVISIBLE_MARKS = setOf(
        '\u061C', '\u200B', '\u200C', '\u200D', '\u200E', '\u200F',
        '\u202A', '\u202B', '\u202C', '\u202D', '\u202E', '\u2066', '\u2067', '\u2068', '\u2069', '\uFEFF'
    )

    private val DATE_WORDS = setOf(
        "am", "pm",
        "jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "sept", "oct", "nov", "dec",
        "january", "february", "march", "april", "june", "july", "august", "september", "october", "november", "december",
        "mon", "tue", "wed", "thu", "fri", "sat", "sun",
        "monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday",
        // Nepali months and weekdays, in the spelling banks use.
        "बैशाख", "जेठ", "असार", "साउन", "भदौ", "असोज", "कात्तिक", "मंसिर", "पुष", "माघ", "फागुन", "चैत",
        "आइत", "सोम", "मङ्गल", "बुध", "बिहि", "शुक्र", "शनि"
    )

    private val BALANCE_WORDS = listOf(
        "bal", "balance", "limit", "lmt", "available", "avl",
        "बाँकी", "बाकी", "मौज्दात", "सीमा"
    )
    private val BALANCE_WORDS_AFTER = listOf("available", "limit", "balance", "बाँकी", "मौज्दात")

    private val MERCHANT_HINT = Regex(
        """\b(?:at|to|from)\s+(\p{L}[^,;:\n]*?)\s+(?:card|ending|on|ref|using|via|with|a/c|acct|account|dated)\b""",
        RegexOption.IGNORE_CASE
    )
    private val ACCOUNT_PHRASE = Regex("""^(?:your|you|a/c|ac|acct|account|card|the)\b""", RegexOption.IGNORE_CASE)
}

/**
 * The currency tags Nepal's messages use, in one place.
 *
 * "Rs.", "Rs" and "रू" are how Nepali banks and wallets write the rupee, so they read as NPR;
 * "₹" is the Indian sign and stays INR. Codes are matched against a spelled-out ISO 4217 list
 * rather than [java.util.Currency], which on Android accepts ordinary words like "and".
 */
internal object CurrencyText {
    private const val NPR = "NPR"

    private val SYMBOLS = mapOf(
        "Rs" to NPR, "RS" to NPR, "rs" to NPR, "रू" to NPR, "रु" to NPR,
        "₹" to "INR", "$" to "USD", "€" to "EUR", "£" to "GBP"
    )

    val ISO: Set<String> = (
        "AED AFN ALL AMD ANG AOA ARS AUD AWG AZN BAM BBD BDT BGN BHD BIF BMD BND BOB BRL BSD BTN BWP BYN BZD " +
            "CAD CDF CHF CLP CNY COP CRC CUP CVE CZK DJF DKK DOP DZD EGP ERN ETB EUR FJD FKP GBP GEL GHS GIP GMD GNF " +
            "GTQ GYD HKD HNL HTG HUF IDR ILS INR IQD IRR ISK JMD JOD JPY KES KGS KHR KMF KPW KRW KWD KYD KZT LAK LBP " +
            "LKR LRD LSL LYD MAD MDL MGA MKD MMK MNT MOP MRU MUR MVR MWK MXN MYR MZN NAD NGN NIO NOK NPR NZD OMR PAB " +
            "PEN PGK PHP PKR PLN PYG QAR RON RSD RUB RWF SAR SBD SCR SDG SEK SGD SHP SLE SOS SRD SSP STN SVC SYP SZL " +
            "THB TJS TMT TND TOP TRY TTD TWD TZS UAH UGX USD UYU UZS VES VND VUV WST XAF XCD XOF XPF YER ZAR ZMW ZWG"
        ).split(' ').toSet()

    /** The ISO code [word] names, or null when it names none. */
    fun tagOf(word: String): String? {
        if (word.isEmpty()) return null
        SYMBOLS[word]?.let { return it }
        return word.takeIf { it.length == 3 && it.all { c -> c in 'A'..'Z' } && it in ISO }
    }
}
