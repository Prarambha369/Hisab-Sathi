package com.prarambha.hisabsathi.presentation.common

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class NepalFiscalYearTest {

    @Test
    fun `dates on or after mid-July start the fiscal year that same July`() {
        assertEquals(LocalDate.of(2026, 7, 16), nepalFiscalYearStart(LocalDate.of(2026, 7, 16)))
        assertEquals(LocalDate.of(2026, 7, 16), nepalFiscalYearStart(LocalDate.of(2026, 11, 19)))
        assertEquals(LocalDate.of(2026, 7, 16), nepalFiscalYearStart(LocalDate.of(2026, 12, 31)))
    }

    @Test
    fun `dates before mid-July belong to the fiscal year that started the previous July`() {
        assertEquals(LocalDate.of(2025, 7, 16), nepalFiscalYearStart(LocalDate.of(2026, 1, 1)))
        assertEquals(LocalDate.of(2025, 7, 16), nepalFiscalYearStart(LocalDate.of(2026, 4, 1)))
        assertEquals(LocalDate.of(2025, 7, 16), nepalFiscalYearStart(LocalDate.of(2026, 7, 15)))
    }
}
