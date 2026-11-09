package com.ritesh.parser.core.bank

import com.ritesh.parser.core.TransactionType
import com.ritesh.parser.core.test.ExpectedTransaction
import com.ritesh.parser.core.test.ParserTestCase
import com.ritesh.parser.core.test.ParserTestUtils
import com.ritesh.parser.core.test.SimpleTestCase
import org.junit.jupiter.api.*
import java.math.BigDecimal

class NabilBankParserTest {

    private val parser = NabilBankParser()

    @TestFactory
    fun `nabil bank parser handles key paths`(): List<DynamicTest> {
        ParserTestUtils.printTestHeader(
            parserName = "Nabil Bank (Nepal)",
            bankName = parser.getBankName(),
            currency = parser.getCurrency()
        )

        val cases = listOf(
            ParserTestCase(
                name = "Debit transaction",
                message = "Dear Customer, your A/C XXXX1234 has been debited by NPR 5,000.00 on 10/12/2025. Available Bal: NPR 45,000.00.",
                sender = "NABIL_ALERT",
                expected = ExpectedTransaction(
                    amount = BigDecimal("5000.00"),
                    currency = "NPR",
                    type = TransactionType.EXPENSE,
                    accountLast4 = "1234"
                )
            ),
            ParserTestCase(
                name = "Credit transaction",
                message = "Dear Customer, your A/C XXXX5678 has been credited by NPR 10,000.00 on 10/12/2025. Available Bal: NPR 55,000.00.",
                sender = "NABIL_ALERT",
                expected = ExpectedTransaction(
                    amount = BigDecimal("10000.00"),
                    currency = "NPR",
                    type = TransactionType.INCOME,
                    accountLast4 = "5678"
                )
            ),
            ParserTestCase(
                name = "OTP message should not parse",
                message = "Your OTP for Nabil Bank login is 123456. Valid for 5 minutes.",
                sender = "NABIL_ALERT",
                shouldParse = false
            ),
            ParserTestCase(
                name = "Promotional message with only 'dear customer' should not parse",
                message = "Dear Customer, please visit our branch for special offers.",
                sender = "NABIL_ALERT",
                shouldParse = false
            ),
            ParserTestCase(
                name = "Password reset message should not parse",
                message = "Your password has been reset. If not done by you, contact us.",
                sender = "NABIL_ALERT",
                shouldParse = false
            )
        )

        return ParserTestUtils.runTestSuite(
            parser = parser,
            testCases = cases,
            handleCases = listOf(
                "NABIL_ALERT" to true,
                "NABIL" to true,
                "NABILBANK" to true,
                "HDFC" to false,
                "UNKNOWN" to false
            )
        )
    }

    @TestFactory
    fun `factory resolves nabil bank`(): List<DynamicTest> {
        val cases = listOf(
            SimpleTestCase(
                bankName = "Nabil Bank",
                sender = "NABIL_ALERT",
                currency = "NPR",
                message = "Dear Customer, your A/C XXXX1234 has been debited by NPR 1,500.00 on 10/12/2025.",
                expected = ExpectedTransaction(
                    amount = BigDecimal("1500.00"),
                    currency = "NPR",
                    type = TransactionType.EXPENSE
                ),
                shouldHandle = true
            ),
            SimpleTestCase(
                bankName = "Nabil Bank",
                sender = "NABIL",
                currency = "NPR",
                message = "Dear Customer, your A/C XXXX9999 has been credited by NPR 2,000.00 on 10/12/2025.",
                expected = ExpectedTransaction(
                    amount = BigDecimal("2000.00"),
                    currency = "NPR",
                    type = TransactionType.INCOME
                ),
                shouldHandle = true
            )
        )

        return ParserTestUtils.runFactoryTestSuite(cases, "Factory smoke tests")
    }
}
