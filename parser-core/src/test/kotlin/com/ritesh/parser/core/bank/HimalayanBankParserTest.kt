package com.ritesh.parser.core.bank

import com.ritesh.parser.core.TransactionType
import com.ritesh.parser.core.test.ExpectedTransaction
import com.ritesh.parser.core.test.ParserTestCase
import com.ritesh.parser.core.test.ParserTestUtils
import com.ritesh.parser.core.test.SimpleTestCase
import org.junit.jupiter.api.*
import java.math.BigDecimal

class HimalayanBankParserTest {

    private val parser = HimalayanBankParser()

    @TestFactory
    fun `himalayan bank parser handles key paths`(): List<DynamicTest> {
        ParserTestUtils.printTestHeader(
            parserName = "Himalayan Bank Limited (Nepal)",
            bankName = parser.getBankName(),
            currency = parser.getCurrency()
        )

        val cases = listOf(
            ParserTestCase(
                name = "Debit transaction",
                message = "Dear Customer, your A/C XXXX2345 has been debited by NPR 3,500.00 on 10/12/2025. Available Bal: NPR 20,000.00.",
                sender = "HBL_ALERT",
                expected = ExpectedTransaction(
                    amount = BigDecimal("3500.00"),
                    currency = "NPR",
                    type = TransactionType.EXPENSE,
                    accountLast4 = "2345"
                )
            ),
            ParserTestCase(
                name = "Credit transaction",
                message = "Dear Customer, your A/C XXXX2345 has been credited by NPR 7,000.00 on 10/12/2025. Available Bal: NPR 27,000.00.",
                sender = "HBL_ALERT",
                expected = ExpectedTransaction(
                    amount = BigDecimal("7000.00"),
                    currency = "NPR",
                    type = TransactionType.INCOME,
                    accountLast4 = "2345"
                )
            ),
            ParserTestCase(
                name = "ATM withdrawal",
                message = "Dear Customer, your A/C XXXX2345 has been debited by NPR 5,000.00 at ATM on 10/12/2025.",
                sender = "HBL",
                expected = ExpectedTransaction(
                    amount = BigDecimal("5000.00"),
                    currency = "NPR",
                    type = TransactionType.EXPENSE,
                    merchant = "ATM Withdrawal"
                )
            ),
            ParserTestCase(
                name = "OTP message should not parse",
                message = "Your HBL OTP is 321654. Do not share with anyone.",
                sender = "HBL_ALERT",
                shouldParse = false
            )
        )

        return ParserTestUtils.runTestSuite(
            parser = parser,
            testCases = cases,
            handleCases = listOf(
                "HBL_ALERT" to true,
                "HBL" to true,
                "HIMALAYANBANK" to true,
                "NABIL" to false,
                "ESEWA" to false
            )
        )
    }

    @TestFactory
    fun `factory resolves himalayan bank`(): List<DynamicTest> {
        val cases = listOf(
            SimpleTestCase(
                bankName = "Himalayan Bank",
                sender = "HBL_ALERT",
                currency = "NPR",
                message = "Dear Customer, your A/C XXXX4321 has been debited by NPR 1,200.00 on 10/12/2025.",
                expected = ExpectedTransaction(
                    amount = BigDecimal("1200.00"),
                    currency = "NPR",
                    type = TransactionType.EXPENSE
                ),
                shouldHandle = true
            )
        )

        return ParserTestUtils.runFactoryTestSuite(cases, "Factory smoke tests")
    }
}
