package com.ritesh.parser.core.bank

import com.ritesh.parser.core.TransactionType
import com.ritesh.parser.core.test.ExpectedTransaction
import com.ritesh.parser.core.test.ParserTestCase
import com.ritesh.parser.core.test.ParserTestUtils
import com.ritesh.parser.core.test.SimpleTestCase
import org.junit.jupiter.api.*
import java.math.BigDecimal

class EsewaParserTest {

    private val parser = EsewaParser()

    @TestFactory
    fun `esewa parser handles key paths`(): List<DynamicTest> {
        ParserTestUtils.printTestHeader(
            parserName = "eSewa Digital Wallet (Nepal)",
            bankName = parser.getBankName(),
            currency = parser.getCurrency()
        )

        val cases = listOf(
            ParserTestCase(
                name = "Payment successful - electricity",
                message = "Payment Successful! You have paid NPR 1,822.00 to Nepal Electricity Authority. Transaction ID: ESEWA20251210001.",
                sender = "ESEWA_ALERT",
                expected = ExpectedTransaction(
                    amount = BigDecimal("1822.00"),
                    currency = "NPR",
                    type = TransactionType.EXPENSE,
                    merchant = "Nepal Electricity Authority"
                )
            ),
            ParserTestCase(
                name = "Cashback received",
                message = "Congratulations! NPR 100.00 cashback has been credited to your eSewa wallet. Order ID: CB20251210002.",
                sender = "ESEWA_ALERT",
                expected = ExpectedTransaction(
                    amount = BigDecimal("100.00"),
                    currency = "NPR",
                    type = TransactionType.INCOME
                )
            ),
            ParserTestCase(
                name = "Wallet load",
                message = "Your eSewa wallet load of NPR 2,000.00 was successful. Transaction ID: WL20251210003.",
                sender = "ESEWA",
                expected = ExpectedTransaction(
                    amount = BigDecimal("2000.00"),
                    currency = "NPR",
                    type = TransactionType.EXPENSE
                )
            ),
            ParserTestCase(
                name = "Failed payment should not parse",
                message = "Payment Failed! Your payment of NPR 500.00 could not be processed. Please try again.",
                sender = "ESEWA_ALERT",
                shouldParse = false
            ),
            ParserTestCase(
                name = "Unsuccessful transaction should not parse",
                message = "Transaction unsuccessful. NPR 250.00 payment to merchant was declined.",
                sender = "ESEWA_ALERT",
                shouldParse = false
            ),
            ParserTestCase(
                name = "OTP message should not parse",
                message = "Your eSewa OTP is 456789. Valid for 5 minutes. Do not share with anyone.",
                sender = "ESEWA_ALERT",
                shouldParse = false
            )
        )

        return ParserTestUtils.runTestSuite(
            parser = parser,
            testCases = cases,
            handleCases = listOf(
                "ESEWA_ALERT" to true,
                "ESEWA" to true,
                "ESEWALIMITED" to true,
                "KHALTI" to false,
                "NABIL" to false
            )
        )
    }

    @TestFactory
    fun `factory resolves esewa`(): List<DynamicTest> {
        val cases = listOf(
            SimpleTestCase(
                bankName = "eSewa",
                sender = "ESEWA_ALERT",
                currency = "NPR",
                message = "Payment Successful! You have paid NPR 300.00 to Mobile Topup. Transaction ID: ESEWA20251210020.",
                expected = ExpectedTransaction(
                    amount = BigDecimal("300.00"),
                    currency = "NPR",
                    type = TransactionType.EXPENSE
                ),
                shouldHandle = true
            )
        )

        return ParserTestUtils.runFactoryTestSuite(cases, "Factory smoke tests")
    }
}
