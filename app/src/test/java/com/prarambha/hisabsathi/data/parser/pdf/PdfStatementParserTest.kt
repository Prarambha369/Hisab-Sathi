package com.prarambha.hisabsathi.data.parser.pdf

import com.prarambha.parser.core.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class PdfStatementParserTest {

    @Test
    fun `GPayPdfParser parses income and expense correctly`() {
        val parser = GPayPdfParser()
        val text = """
            01 Aug, 2025 05:19 PM   Received from JOHN DOE
            UPI Transaction ID: 531799428648
            Paid to Punjab National Bank 8690   ₹2,000
            
            08 Aug, 2025 03:55 PM   Paid to Tej Rajput
            UPI Transaction ID: 527032400498
            Paid by Punjab National Bank 8690   ₹150
        """.trimIndent()

        val transactions = parser.parse(text)

        assertEquals(2, transactions.size)

        // Income
        assertTrue(BigDecimal("2000").compareTo(transactions[0].amount) == 0)
        assertEquals(TransactionType.INCOME, transactions[0].type)
        assertEquals("JOHN DOE", transactions[0].merchant)
        assertEquals("8690", transactions[0].accountLast4)
        assertEquals("Punjab National Bank", transactions[0].bankName)

        // Expense
        assertTrue(BigDecimal("150").compareTo(transactions[1].amount) == 0)
        assertEquals(TransactionType.EXPENSE, transactions[1].type)
        assertEquals("Tej Rajput", transactions[1].merchant)
        assertEquals("8690", transactions[1].accountLast4)
        assertEquals("Punjab National Bank", transactions[1].bankName)
    }

    @Test
    fun `GPayPdfParser handles multi-line dates and merged text correctly`() {
        val parser = GPayPdfParser()
        // Simulate a case where dates might be on their own line or text is weirdly merged
        val text = """
            01 Aug, 2025 
            05:19 PM   Received from MERCH A
            UPI Transaction ID: 123
            Paid to BANK A 1111   ₹1,000
            02 Aug, 2025 06:10 AM Paid to MERCH B UPI Transaction ID: 456 Paid by BANK B 2222 ₹500
        """.trimIndent()

        val transactions = parser.parse(text)

        assertEquals(2, transactions.size)

        assertEquals("MERCH A", transactions[0].merchant)
        assertEquals("1111", transactions[0].accountLast4)
        assertEquals("BANK A", transactions[0].bankName)
        assertTrue(BigDecimal("1000").compareTo(transactions[0].amount) == 0)

        assertEquals("MERCH B", transactions[1].merchant)
        assertEquals("2222", transactions[1].accountLast4)
        assertEquals("BANK B", transactions[1].bankName)
        assertTrue(BigDecimal("500").compareTo(transactions[1].amount) == 0)
    }
}
