package com.prarambha.parser.core.test

import com.prarambha.parser.core.bank.BankParserFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AgentsSmokeTest {

    @Test
    fun `factory resolves a known nepali bank sender`() {
        val parser = BankParserFactory.getParser("NABIL_ALERT")

        assertTrue(BankParserFactory.isKnownBankSender("NABIL_ALERT"))
        assertEquals("Nabil Bank", parser?.getBankName())
        assertTrue(BankParserFactory.getSupportedBanks().contains("Nabil Bank"))
    }
}
