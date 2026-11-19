package com.prarambha.parser.core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal

/** Readings offered for SMS that no bank parser claims. */
class GenericTransactionExtractorTest {

    private fun estimate(sender: String, message: String) =
        GenericTransactionExtractor.estimate(sender, message)

    @Test
    fun `a Nepali debit carries amount, merchant, account and balance`() {
        val found = estimate(
            "VM-NABIL",
            "Rs.500.00 debited from your A/c XXX1234 to TEST STORE on 25-10-26. Avl Bal Rs.12,000.50"
        )!!

        assertEquals(0, BigDecimal("500.00").compareTo(found.transaction.amount))
        assertEquals("NPR", found.transaction.currency)
        assertEquals(TransactionType.EXPENSE, found.transaction.type)
        assertEquals("TEST STORE", found.transaction.merchant)
        assertEquals("1234", found.transaction.accountLast4)
        assertEquals(0, BigDecimal("12000.50").compareTo(found.transaction.balance!!))
        assertEquals(100, found.score)
    }

    @Test
    fun `a card purchase keeps the available limit separate from the amount`() {
        val found = estimate(
            "ZQTCARD",
            "Purchase of USD 12.50 at TEST CAFE on card ending 4321. Available limit USD 1,987.50"
        )!!

        assertEquals(0, BigDecimal("12.50").compareTo(found.transaction.amount))
        assertEquals("USD", found.transaction.currency)
        assertEquals("4321", found.transaction.accountLast4)
        assertEquals(0, BigDecimal("1987.50").compareTo(found.transaction.creditLimit!!))
        assertNull(found.transaction.balance)
        assertEquals(100, found.score)
    }

    @Test
    fun `dollar signs and pound signs are read`() {
        val dollars = estimate("ZQTCARD", "You spent \$45.99 at TEST MART with card **9876.")!!
        assertEquals(0, BigDecimal("45.99").compareTo(dollars.transaction.amount))
        assertEquals("USD", dollars.transaction.currency)

        val pounds = estimate("ZQTMONEY", "£20.00 received from TEST FRIEND via transfer.")!!
        assertEquals("GBP", pounds.transaction.currency)
        assertEquals(TransactionType.INCOME, pounds.transaction.type)
        assertEquals("TEST FRIEND", pounds.transaction.merchant)
        assertNull(pounds.transaction.accountLast4)
    }

    @Test
    fun `a credit names the payer rather than the account`() {
        val found = estimate(
            "ZQTMONEY",
            "AED 2,500.00 has been credited to your account XXXX5555 from TEST EMPLOYER on 25/10/2026."
        )!!

        assertEquals(TransactionType.INCOME, found.transaction.type)
        assertEquals("TEST EMPLOYER", found.transaction.merchant)
        assertEquals("5555", found.transaction.accountLast4)
        assertEquals(90, found.score)
    }

    @Test
    fun `money returned is income whatever the original wording says`() {
        val reversed = estimate(
            "ZQTCARD",
            "Purchase of NPR 500 at TEST STORE with card **1234 has been reversed and credited back."
        )!!
        assertEquals(TransactionType.INCOME, reversed.transaction.type)

        val credited = estimate("ZQTMONEY", "NPR 500 credited to your A/c XX1234 (transaction reversed).")!!
        assertEquals(TransactionType.INCOME, credited.transaction.type)
    }

    @Test
    fun `a reversal that may happen later is still the original expense`() {
        val found = estimate(
            "ZQTMONEY",
            "NPR 500 debited from A/c XX1234 for ATM withdrawal. If cash is not dispensed, amount will be reversed within 24 hours."
        )!!
        assertEquals(TransactionType.EXPENSE, found.transaction.type)
        assertEquals("1234", found.transaction.accountLast4)
    }

    @Test
    fun `Nepali rupee wording reads as NPR`() {
        val found = estimate("ESEWA", "Rs 1,200 debited for Topup. Balance Rs 350")!!
        assertEquals("NPR", found.transaction.currency)
        assertEquals(0, BigDecimal("1200").compareTo(found.transaction.amount))
        assertEquals(0, BigDecimal("350").compareTo(found.transaction.balance!!))
    }

    @Test
    fun `messages that never move money produce nothing`() {
        val rejected = listOf(
            "ZQTCARD" to "Purchase declined: USD 12.50 at TEST CAFE with card **9876.",
            "ZQTMONEY" to "Transaction of NPR 750.00 from A/c XX1234 to TEST STORE has failed.",
            "ZQTMONEY" to "Your OTP for the payment of NPR 999 at TEST STORE is 482913. Do not share it.",
            "ZQTMONEY" to "Spend NPR 2,000 at TEST MALL and get 10% back! Offer valid till Sunday.",
            "ZQTCARD" to "Your card bill of USD 450.00 is due on 30-Oct.",
            "ZQTMONEY" to "Your account XX1234 balance is AED 2,500.00 as of 25-Oct.",
            "ZQTMONEY" to "Your account was debited. Contact us for details.",
            "+15550001111" to "Sent USD 10.00 to TEST CAFE."
        )
        for ((sender, message) in rejected) {
            assertNull(estimate(sender, message), "should not read: $message")
        }
    }

    @Test
    fun `a bare debit is read but not offered`() {
        val bare = estimate("ESEWA", "Rs 500 debited")!!
        assertEquals(0, BigDecimal("500").compareTo(bare.transaction.amount))
        assertTrue(bare.score < GenericTransactionExtractor.WORTH_SUGGESTING)

        val corroborated = estimate("ESEWA", "Rs 500 debited from A/c XX1234 to BHATBHATENI on 12-10-26")!!
        assertTrue(corroborated.score >= GenericTransactionExtractor.WORTH_SUGGESTING)
    }

    @Test
    fun `a person-to-person number is never a bank`() {
        assertNull(estimate("+9779800000000", "I paid Rs 5.00"))
        assertNull(estimate("9800000000", "I paid Rs 5.00"))
        assertNotNull(estimate("NABIL_ALERT", "Rs 5.00 debited"))
    }
}
