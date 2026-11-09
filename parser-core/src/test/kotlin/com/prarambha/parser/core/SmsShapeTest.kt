package com.prarambha.parser.core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertTimeoutPreemptively
import java.math.BigDecimal
import java.time.Duration

/** Teaching one message by tapping it, then reading the sender's next ones. */
class SmsShapeTest {

    private val sender = "NABIL_ALERT"

    private fun rangeOf(body: String, text: String): IntRange {
        val at = body.indexOf(text)
        assertTrue(at >= 0, "'$text' is not in the message")
        return at until at + text.length
    }

    private fun teach(
        example: String,
        amount: String,
        merchant: String? = null,
        account: String? = null,
        direction: TransactionType = TransactionType.EXPENSE,
        currency: String = "NPR",
        style: DecimalStyle? = null,
        defaultMerchant: String? = null
    ): TaughtShape? = SmsShapes.learn(
        id = "shape",
        senderKey = "NABIL",
        example = example,
        taps = TapChoice(
            amount = rangeOf(example, amount),
            merchant = merchant?.let { rangeOf(example, it) },
            account = account?.let { rangeOf(example, it) }
        ),
        direction = direction,
        bankLabel = "Nabil Bank",
        currency = currency,
        decimalStyle = style,
        defaultMerchant = defaultMerchant
    )

    private fun read(shape: TaughtShape, body: String) = SmsShapes.read(shape, body, sender, 1L)

    private val example = "Rs. 500.00 debited from A/c 1234 to Bhatbhateni. Ref 88231. Bal Rs. 25,000.00"
    private val taught get() = teach(example, "500.00", "Bhatbhateni", "1234")!!

    // ─── Reading later messages ───────────────────────────────────────────────

    @Test
    fun `reads a later message of the same shape`() {
        val later = "Rs. 1,250.75 debited from A/c 9876 to City Pharmacy. Ref 77120. Bal Rs. 12,000.00"
        val parsed = read(taught, later)!!

        assertEquals(0, BigDecimal("1250.75").compareTo(parsed.amount))
        assertEquals("City Pharmacy", parsed.merchant)
        assertEquals("9876", parsed.accountLast4)
        assertEquals(TransactionType.EXPENSE, parsed.type)
        assertEquals("NPR", parsed.currency)
        assertEquals("Nabil Bank", parsed.bankName)
        assertEquals(sender, parsed.sender)
        // The message is the dedupe identity, so it must be stored exactly as received.
        assertEquals(later, parsed.smsBody)
    }

    @Test
    fun `reads the example it was taught from`() {
        val parsed = read(taught, example)!!
        assertEquals(0, BigDecimal("500.00").compareTo(parsed.amount))
        assertEquals("Bhatbhateni", parsed.merchant)
        assertEquals("1234", parsed.accountLast4)
    }

    @Test
    fun `refuses a message of another shape`() {
        assertNull(read(taught, "USD 1,250.75 debited from A/c 9876 to City Pharmacy. Ref 1. Bal USD 12.00"))
        assertNull(read(taught, "Rs. 1,250.75 debited from A/c 9876 to City Pharmacy. Ref 1. Bal Rs. 12.00 Riyadh."))
        assertNull(read(taught, "Rs. 1,250.75 debited from A/c 9876 to City Pharmacy. Ref 1."))
        assertNull(read(taught, "You spent Rs. 1,250.75 at City Pharmacy"))
        assertNull(read(taught, "Rs. 0.00 debited from A/c 9876 to City Pharmacy. Ref 1. Bal Rs. 12.00"))
        assertNull(read(taught, "rs. 1,250.75 debited from A/c 9876 to City Pharmacy. Ref 1. Bal Rs. 12.00"))
    }

    @Test
    fun `a balance can never slide into the amount`() {
        // The figures swapped places: the shape no longer fits.
        assertNull(read(taught, "Bal Rs. 12,000.00. Rs. 500.00 debited from A/c 1234 to Bhatbhateni. Ref 1."))
        assertEquals(0, BigDecimal("1.00").compareTo(read(taught, "Rs. 1.00 debited from A/c 1234 to X. Ref 1. Bal Rs. 9.00")!!.amount))
    }

    @Test
    fun `a merchant that merely looks unusual still reads`() {
        val shape = teach("Paid Rs. 5.00 at Bar on 12/10", "5.00", "Bar")!!
        assertEquals("Store 24", read(shape, "Paid Rs. 9.00 at Store 24 on 12/10")!!.merchant)
        assertEquals("Dr Ahmed Clinic", read(shape, "Paid Rs. 9.00 at Dr Ahmed Clinic on 12/10")!!.merchant)
    }

    @Test
    fun `a merchant cannot carry a price or a failure`() {
        val shape = teach("Paid Rs. 5.00 at Bar", "5.00", "Bar")!!
        assertNull(read(shape, "Paid Rs. 9.00 at Bar: Rs. 900.00 remaining"))
        assertNull(read(shape, "Paid Rs. 9.00 at Bar declined"))
        assertNull(read(shape, "Paid Rs. 9.00 at Bar was reversed"))
        assertNull(read(shape, "Paid Rs. 9.00 at Bar CR"))
    }

    @Test
    fun `a merchant ending the message keeps its whole name`() {
        val shape = teach("Paid Rs. 5.00 at Juice Bar", "5.00", "Juice Bar")!!
        assertEquals("City Pharmacy Main", read(shape, "Paid Rs. 9.00 at City Pharmacy Main")!!.merchant)
    }

    @Test
    fun `a default merchant stands in when the message names none`() {
        val shape = teach("Rent paid Rs. 5.00", "5.00", defaultMerchant = "Landlord")!!
        assertEquals("Landlord", read(shape, "Rent paid Rs. 9.00")!!.merchant)
    }

    @Test
    fun `direction comes from the taught shape, not the wording`() {
        val shape = teach("Salary Rs. 5.00 received", "5.00", direction = TransactionType.INCOME)!!
        assertEquals(TransactionType.INCOME, read(shape, "Salary Rs. 9.00 received")!!.type)
    }

    // ─── What may not be taught ───────────────────────────────────────────────

    @Test
    fun `refuses an example it cannot read back`() {
        assertNull(teach("Paid Rs. 5.00 for Rent", "Paid"), "an amount has to be a number")
        assertNull(teach("Paid Rs. 5.00 for Rent", "5.00", merchant = "5.00"), "picks cannot overlap")
        assertNull(teach("5.00 1.00", "5.00"), "a shape of only numbers would match anything")
        assertNull(teach("Paid Rs. 5.00 at Bar77", "5.00", merchant = "Bar"), "a merchant needs an end")
    }

    @Test
    fun `refuses a currency that is not a code`() {
        assertNull(teach("Paid 2.500 now", "2.500", currency = "npr"))
        assertNull(teach("Paid 2.500 now", "2.500", currency = "   "))
    }

    @Test
    fun `refuses a bare fraction as the amount`() {
        assertNull(teach("Paid NPR .50 at Cafe", ".50", "Cafe"))
        assertNull(SmsShapes.suggest(sender, "Paid NPR .50 at Cafe").amount)
        // A separator straight after a word is the currency's own, so this is fifty.
        val shape = teach("Paid Rs.50 at Cafe", "50", "Cafe")!!
        assertEquals(0, BigDecimal("75").compareTo(read(shape, "Paid Rs.75 at Cafe")!!.amount))
    }

    @Test
    fun `refuses a piece of a spaced thousands number`() {
        assertNull(teach("Paid Rs. 1 234.50 at Bar now", "1", currency = "NPR", merchant = "Bar"))
        assertNull(SmsShapes.suggest(sender, "Paid Rs. 1 234.50 at Bar now").amount)
    }

    @Test
    fun `a restored shape with more than one merchant reads nothing`() {
        val paid = ShapeSegment(ShapeSlot.FIXED, "Paid ")
        val merchant = ShapeSegment(ShapeSlot.MERCHANT)
        val amount = ShapeSegment(ShapeSlot.AMOUNT)
        val broken = TaughtShape("t", "K", listOf(paid, merchant, ShapeSegment(), merchant, amount), TransactionType.EXPENSE, "NPR", "B")
        assertNull(read(broken, "Paid a b 5.00"))
    }

    // ─── Amounts ──────────────────────────────────────────────────────────────

    @Test
    fun `parses written amounts`() {
        fun value(text: String, currency: String = "NPR", style: DecimalStyle? = null) =
            SmsShapes.parseDecimal(text, style, currency)

        assertEquals(0, BigDecimal("1234.56").compareTo(value("1,234.56")))
        assertEquals(0, BigDecimal("1234.56").compareTo(value("1.234,56", "EUR")))
        assertEquals(0, BigDecimal("12.50").compareTo(value("12,50", "EUR")))
        assertEquals(0, BigDecimal("1234").compareTo(value("1,234", "USD")))
        assertEquals(0, BigDecimal("1234").compareTo(value("1.234", "EUR")))
        assertEquals(0, BigDecimal("100000.00").compareTo(value("1,00,000.00")))
        assertEquals(0, BigDecimal("1.234").compareTo(value("1.234", "KWD", DecimalStyle.POINT)))
    }

    @Test
    fun `refuses amounts that could mean two things`() {
        assertNull(SmsShapes.parseDecimal("1.234", null, "KWD"))
        assertNull(SmsShapes.parseDecimal("1,234", null, "OMR"))
        assertNull(SmsShapes.parseDecimal("12,50", DecimalStyle.POINT, "USD"))
        assertNull(SmsShapes.parseDecimal("1.2.3", DecimalStyle.POINT, "USD"))
        assertNull(SmsShapes.parseDecimal("0", null, "USD"))
        assertNull(SmsShapes.parseDecimal("1234567890123456", null, "USD"))
        assertNull(SmsShapes.parseDecimal("12.3456", DecimalStyle.POINT, "USD"))
        assertNull(SmsShapes.parseDecimal("0,500", DecimalStyle.POINT, "NPR"))
        assertNull(SmsShapes.parseDecimal(".50", null, "NPR"))
    }

    @Test
    fun `a three-decimal currency waits for the user to say what the separator means`() {
        assertTrue(SmsShapes.isDecimalStyleAmbiguous("1.234", "KWD"))
        assertFalse(SmsShapes.isDecimalStyleAmbiguous("1.234", "EUR"))
        assertFalse(SmsShapes.isDecimalStyleAmbiguous("1.23", "KWD"))
        assertFalse(SmsShapes.isDecimalStyleAmbiguous("500", "KWD"))

        assertNull(teach("Paid KWD 1.234 for Rent", "1.234", currency = "KWD"))
        assertNotNull(teach("Paid KWD 1.234 for Rent", "1.234", currency = "KWD", style = DecimalStyle.POINT))
    }

    @Test
    fun `a Devanagari message reads with Devanagari digits`() {
        val body = "रू ५०.०० खर्च भयो"
        val shape = teach(body, "५०.००")!!
        val parsed = read(shape, "रू ७५.५० खर्च भयो")!!

        assertEquals(0, BigDecimal("75.50").compareTo(parsed.amount))
        assertEquals("NPR", parsed.currency)
    }

    // ─── Several shapes ───────────────────────────────────────────────────────

    @Test
    fun `shapes that disagree read as nothing`() {
        val body = "Moved Rs. 5.00 for Rent"
        val out = teach(body, "5.00", direction = TransactionType.EXPENSE)!!
        val into = teach(body, "5.00", direction = TransactionType.INCOME)!!

        assertNull(SmsShapes.readConsistent(listOf(out, into), body, sender, 1L))
        assertNotNull(SmsShapes.readConsistent(listOf(out, out.copy(id = "second")), body, sender, 1L))
    }

    // ─── Suggesting and tapping ───────────────────────────────────────────────

    @Test
    fun `suggests the amount, merchant and account of an English message`() {
        val body = "Purchased Rs. 50.00 at Juice Bar card ending 1234 on 12/10/26. Balance Rs. 900.00"
        val suggestion = SmsShapes.suggest(sender, body)

        assertEquals("50.00", suggestion.amount?.let { body.substring(it.first, it.last + 1) })
        assertEquals("Juice Bar", suggestion.merchant?.let { body.substring(it.first, it.last + 1) })
        assertEquals("1234", suggestion.account?.let { body.substring(it.first, it.last + 1) })
        assertEquals(TransactionType.EXPENSE, suggestion.direction)
        assertEquals("NPR", suggestion.currency)
    }

    @Test
    fun `leaves competing prices for the user to pick`() {
        val suggestion = SmsShapes.suggest(sender, "Spent Rs. 50.00 fee Rs. 2.00 at Juice Bar")
        assertNull(suggestion.amount)
        assertNull(suggestion.currency)
        assertEquals(TransactionType.EXPENSE, suggestion.direction)
    }

    @Test
    fun `a balance is never suggested as the amount`() {
        // A figure word right beside a number suppresses it, so this message offers no amount at all.
        assertNull(SmsShapes.suggest(sender, "Rs. 900.00 available").amount)

        val body = "Paid Rs. 50.00 at Juice Bar on 12/10. Balance Rs. 900.00"
        val suggestion = SmsShapes.suggest(sender, body)
        assertEquals("50.00", suggestion.amount?.let { body.substring(it.first, it.last + 1) })
    }

    @Test
    fun `a suggestion is enough to teach a shape that reads the next message`() {
        // The screen pre-selects exactly these ranges, so learn() has to accept them as they come.
        val body = "Purchased Rs. 50.00 at Juice Bar card ending 1234 on 12/10/26. Balance Rs. 900.00"
        val suggestion = SmsShapes.suggest(sender, body)

        val shape = SmsShapes.learn(
            id = "from-suggestion",
            senderKey = sender,
            example = body,
            taps = TapChoice(
                amount = suggestion.amount!!,
                merchant = suggestion.merchant,
                account = suggestion.account
            ),
            direction = suggestion.direction ?: TransactionType.EXPENSE,
            bankLabel = sender,
            currency = suggestion.currency ?: "NPR"
        )
        assertNotNull(shape)

        val later = "Purchased Rs. 275.00 at Juice Bar card ending 1234 on 05/11/26. Balance Rs. 625.00"
        val parsed = SmsShapes.readConsistent(listOf(shape!!), later, sender, 2L)!!

        assertEquals(0, BigDecimal("275.00").compareTo(parsed.amount))
        assertEquals("Juice Bar", parsed.merchant)
        assertEquals("1234", parsed.accountLast4)
        assertEquals(TransactionType.EXPENSE, parsed.type)
        assertEquals("NPR", parsed.currency)
        assertEquals(later, parsed.smsBody)
    }

    @Test
    fun `which messages are worth offering to teach`() {
        assertTrue(SmsShapes.worthOffering(sender, "Paid Rs. 5.00 at Bar"))
        assertFalse(SmsShapes.worthOffering(sender, "Your OTP is 1234 for Rs. 5.00"))
        assertFalse(SmsShapes.worthOffering(sender, "Special offer! Rs. 5.00 off"))
        assertFalse(SmsShapes.worthOffering(sender, "Payment of Rs. 5.00 failed"))
        assertFalse(SmsShapes.worthOffering("+9779800000000", "I paid Rs. 5.00"))
        assertFalse(SmsShapes.worthOffering(sender, "Your parcel 1234 arrived"))
    }

    @Test
    fun `tappable words separate glued words and numbers`() {
        val body = "Paid Rs500.00, card XX1234."
        val words = SmsShapes.tapWords(body).map { body.substring(it.range.first, it.range.last + 1) to it.numeric }

        assertEquals(
            listOf("Paid" to false, "Rs" to false, "500.00" to true, "card" to false, "XX" to false, "1234" to true),
            words
        )
    }

    @Test
    fun `the shown context stops at whole words`() {
        val body = "Purchase of Rs 50.00 at Juice Bar now"
        val around = SmsShapes.contextAround(body, rangeOf(body, "50.00"))
        assertEquals("of Rs 50.00 at Juice", body.substring(around.first, around.last + 1))
    }

    @Test
    fun `currency beside a tapped number`() {
        val body = "Paid EUR 5.00 and 3 items"
        assertEquals("EUR", SmsShapes.currencyBeside(body, rangeOf(body, "5.00")))
        assertNull(SmsShapes.currencyBeside(body, rangeOf(body, "3")))
        assertNull(SmsShapes.currencyBeside("USD 5.00 EUR", 4..7), "two tags say nothing")
    }

    @Test
    fun `hostile and huge messages finish quickly`() {
        val shape = teach("Paid Rs. 5.00 at Bar on 12/10/26", "5.00", "Bar")!!
        assertTimeoutPreemptively(Duration.ofSeconds(2)) {
            assertNull(read(shape, "Paid Rs. 5.00 at " + "on ".repeat(600) + "on 12/10/26"))
            assertNull(read(shape, "Paid Rs. 5.00 at Bar on 12/10/26 ".repeat(5000)))
            assertNull(SmsShapes.parseDecimal("1,".repeat(5000) + "1", null, "USD"))
            assertEquals(ShapeSuggestion(), SmsShapes.suggest(sender, "at ".repeat(1000)))
        }
    }
}
