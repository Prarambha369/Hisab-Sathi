package com.prarambha.parser.core

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class ParsedTransactionTest {

    private fun sampleTransaction(
        amount: BigDecimal = BigDecimal("500.00"),
        type: TransactionType = TransactionType.EXPENSE,
        merchant: String? = "Bhatbhateni",
        reference: String? = "REF123456",
        accountLast4: String? = "1234",
        balance: BigDecimal? = BigDecimal("25000.00"),
        smsBody: String = "Rs. 500 debited from a/c 1234 for Bhatbhateni. Ref: REF123456. Bal: Rs. 25000.00",
        sender: String = "NABIL_ALERT",
        timestamp: Long = 1770000000000L
    ) = ParsedTransaction(
        amount = amount,
        type = type,
        merchant = merchant,
        reference = reference,
        accountLast4 = accountLast4,
        balance = balance,
        smsBody = smsBody,
        sender = sender,
        timestamp = timestamp,
        bankName = "Nabil Bank"
    )

    @Test
    fun `identical transactions generate identical transaction id`() {
        val t1 = sampleTransaction()
        val t2 = sampleTransaction()

        assertEquals(t1.generateTransactionId(), t2.generateTransactionId())
    }

    @Test
    fun `transactions with different references on same day do not collide`() {
        val t1 = sampleTransaction(reference = "REF001", smsBody = "Rs. 500 debited. Ref: REF001")
        val t2 = sampleTransaction(reference = "REF002", smsBody = "Rs. 500 debited. Ref: REF002")

        assertNotEquals(t1.generateTransactionId(), t2.generateTransactionId())
    }

    @Test
    fun `transactions with different balance on same day do not collide`() {
        val t1 = sampleTransaction(balance = BigDecimal("10000.00"))
        val t2 = sampleTransaction(balance = BigDecimal("9500.00"))

        assertNotEquals(t1.generateTransactionId(), t2.generateTransactionId())
    }

    @Test
    fun `transactions with different type do not collide`() {
        val t1 = sampleTransaction(type = TransactionType.EXPENSE)
        val t2 = sampleTransaction(type = TransactionType.INCOME)

        assertNotEquals(t1.generateTransactionId(), t2.generateTransactionId())
    }

    @Test
    fun `transactions with different accounts do not collide`() {
        val t1 = sampleTransaction(accountLast4 = "1111")
        val t2 = sampleTransaction(accountLast4 = "2222")

        assertNotEquals(t1.generateTransactionId(), t2.generateTransactionId())
    }

    @Test
    fun `legacy transaction id produces stable 32-char hex string`() {
        val t = sampleTransaction()
        val legacyId = t.generateLegacyTransactionId()

        assertEquals(32, legacyId.length)
        assertTrue(legacyId.matches(Regex("^[0-9a-f]{32}$")))
    }
}
