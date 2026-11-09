# Test Implementation Standards for Parser Modules

This repository now provides a shared Kotlin test harness for all SMS parser
implementations (`ParserTestUtils`). Any new or existing parser tests must follow
the guidelines below.

## 1. Organise tests with JUnit 5

* Each parser must expose a dedicated JUnit 5 test class (e.g.
  `class NabilBankParserTest`).
* Avoid `fun main` entry points; every scenario should be expressed through
  standard JUnit test methods annotated with `@Test`.

## 2. Leverage `ParserTestUtils`

* Import utilities from `com.prarambha.parser.core.test`:
  * `ExpectedTransaction`
  * `ParserTestCase`
  * `SimpleTestCase`
  * `ParserTestUtils`
* Use `ParserTestUtils.runTestSuite` for parser-specific assertions and
  `ParserTestUtils.runFactoryTestSuite` for sender lookups that delegate to
  `BankParserFactory`.

### Parser-specific tests

```kotlin
@Test
fun `nabil bank parser handles key paths`() {
    val parser = NabilBankParser()

    ParserTestUtils.printTestHeader(
        parserName = "Nabil Bank",
        bankName = parser.getBankName(),
        currency = parser.getCurrency()
    )

    val cases = listOf(
        ParserTestCase(
            name = "Debit transaction",
            message = "Your A/C ...0123 has been debited by NPR 1,500.00 on 12-JAN-2026. Avail Bal NPR 25,000.00.",
            sender = "NABIL_ALERT",
            expected = ExpectedTransaction(
                amount = BigDecimal("1500.00"),
                currency = "NPR",
                type = TransactionType.EXPENSE,
                accountLast4 = "0123",
                balance = BigDecimal("25000.00")
            )
        )
    )

   ParserTestUtils.runTestSuite(parser, cases)
}
```

### Factory coverage

```kotlin
@Test
fun `factory resolves nabil bank`() {
    val cases = listOf(
        SimpleTestCase(
            bankName = "Nabil Bank",
            sender = "NABIL_ALERT",
            currency = "NPR",
            message = "...",
            expected = ExpectedTransaction(
                amount = BigDecimal("1500.00"),
                currency = "NPR",
                type = TransactionType.EXPENSE
            ),
            shouldHandle = true
        )
    )

   ParserTestUtils.runFactoryTestSuite(cases, "Factory smoke tests")
}
```

## 3. Populate `ExpectedTransaction` thoughtfully

* Provide only the fields asserted by the parser implementation. Superfluous
  expectations (e.g. `creditLimit`) will fail when the parser leaves values
  `null`.
* Comparison uses exact equality; normalise formatted output in the test case if
  the parser already strips punctuation or casing.

## 4. Maintain deterministic test output

* The utilities print per-case status plus a summary table. This output is
  relied on in existing workflows—do not remove it.
* The utilities also register per-case JUnit assertions via `assertAll`, so
  every failure is visible in IDEs and CI.

## 5. Migration checklist for existing tests

1. Replace any ad-hoc `main` runners with JUnit test classes.
2. Move hard-coded assertions into `ExpectedTransaction` entries.
3. Swap manual loops for `ParserTestUtils.runTestSuite`.
4. Keep the final summary print to retain parity with legacy scripts.

Following these standards keeps parser coverage consistent and guarantees that
human-readable logs and JUnit tooling stay in sync.
