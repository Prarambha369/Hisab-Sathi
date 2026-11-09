package com.ritesh.parser.core.bank

import com.ritesh.parser.core.TransactionType
import com.ritesh.parser.core.test.ExpectedTransaction
import com.ritesh.parser.core.test.ParserTestCase
import com.ritesh.parser.core.test.ParserTestUtils
import com.ritesh.parser.core.test.SimpleTestCase
import org.junit.jupiter.api.*
import java.math.BigDecimal

class KhaltiParserTest {

    private val parser = KhaltiParser()

    @TestFactory
    fun `khalti parser handles key paths`(): List<DynamicTest> {
        ParserTestUtils.printTestHeader(
            parserName = "Khalti Digital Wallet (Nepal)",
            bankName = parser.getBankName(),
            currency = parser.getCurrency()
        )

        val cases = listOf(
            ParserTestCase(
                name = "Payment successful",
                message = "Payment Successful! You have paid NPR 500.00 to Nepal Electricity Authority. Transaction ID: TXN20251210001.",
                sender = "KHALTI_ALERT",
                expected = ExpectedTransaction(
                    amount = BigDecimal("500.00"),
                    currency = "NPR",
                    type = TransactionType.EXPENSE,
                    merchant = "Nepal Electricity Authority"
                )
            ),
            ParserTestCase(
                name = "Cashback credit",
                message = "Congratulations! NPR 50.00 cashback has been credited to your Khalti wallet. Transaction ID: CB20251210002.",
                sender = "KHALTI_ALERT",
                expected = ExpectedTransaction(
                    amount = BigDecimal("50.00"),
                    currency = "NPR",
                    type = TransactionType.INCOME
                )
            ),
            ParserTestCase(
                name = "Wallet load",
                message = "NPR 1,000.00 paid for wallet load. Transaction successful.",
                sender = "KHALTI",
                expected = ExpectedTransaction(
                    amount = BigDecimal("1000.00"),
                    currency = "NPR",
                    type = TransactionType.EXPENSE,
                    merchant = "Wallet Load"
                )
            ),
            ParserTestCase(
                name = "Failed payment should not parse",
                message = "Payment Failed! Your payment of NPR 200.00 to Merchant could not be processed.",
                sender = "KHALTI_ALERT",
                shouldParse = false
            ),
            ParserTestCase(
                name = "Declined transaction should not parse",
                message = "Transaction Declined. NPR 300.00 payment was not completed.",
                sender = "KHALTI_ALERT",
                shouldParse = false
            ),
            ParserTestCase(
                name = "OTP message should not parse",
                message = "Your Khalti OTP is 789012. Valid for 5 minutes. Do not share.",
                sender = "KHALTI_ALERT",
                shouldParse = false
            )
        )

        return ParserTestUtils.runTestSuite(
            parser = parser,
            testCases = cases,
            handleCases = listOf(
                "KHALTI_ALERT" to true,
                "KHALTI" to true,
                "KHALTIDIGITAL" to true,
                "NABIL" to false,
                "ESEWA" to false
            )
        )
    }

    @TestFactory
    fun `factory resolves khalti`(): List<DynamicTest> {
        val cases = listOf(
            SimpleTestCase(
                bankName = "Khalti",
                sender = "KHALTI_ALERT",
                currency = "NPR",
                message = "Payment Successful! You have paid NPR 750.00 to Internet Service. Transaction ID: TXN20251210010.",
                expected = ExpectedTransaction(
                    amount = BigDecimal("750.00"),
                    currency = "NPR",
                    type = TransactionType.EXPENSE
                ),
                shouldHandle = true
            )
        )

        return ParserTestUtils.runFactoryTestSuite(cases, "Factory smoke tests")
    }
}
