package com.prarambha.hisabsathi.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.net.URI
import java.net.URLDecoder

class GitHubIssuesTest {

    private fun params(url: String): Map<String, String> =
        URI(url).rawQuery.split("&").associate {
            val (k, v) = it.split("=", limit = 2)
            URLDecoder.decode(k, "UTF-8") to URLDecoder.decode(v, "UTF-8")
        }

    private val templateDir = listOf(File("../.github/ISSUE_TEMPLATE"), File(".github/ISSUE_TEMPLATE"))
        .first { it.isDirectory }

    @Test
    fun `account card and phone numbers are masked but NPR amounts are kept`() {
        val sms = "Dear Customer, A/C 01234567890123 debited by NPR 1,00,000.00 on 2026-11-19. " +
                "Card 4111 1111 1111 1234. Call 9801234567. Bal NPR 45000.00. Rs 1000000 loaded"
        val masked = GitHubIssues.maskSensitive(sms)

        assertTrue(masked.contains("A/C XXXXXXXXXXX123 "))
        assertTrue(masked.contains("Card XXXX XXXX XXXX X234."))
        assertTrue(masked.contains("Call XXXXXXX567."))
        assertTrue(masked.contains("NPR 1,00,000.00"))
        assertTrue(masked.contains("NPR 45000.00"))
        assertTrue(masked.contains("Rs 1000000 loaded"))
        assertTrue(masked.contains("2026-11-19"))
        assertFalse(masked.contains("01234567890123"))
    }

    @Test
    fun `SMS report opens the SMS parsing form on the Hisab-Sathi repo with fields filled`() {
        val url = GitHubIssues.smsParsingUrl(
            sender = "NABIL_ALERT",
            smsBody = "A/C 0123456789 debited by NPR 1,500 at Daraz",
            institution = "Nabil Bank",
            parsed = "Not detected"
        )
        assertTrue(url.startsWith("https://github.com/Prarambha369/Hisab-Sathi/issues/new?"))
        assertFalse("spaces must be %20, not +", url.contains("+"))

        val p = params(url)
        assertEquals(GitHubIssues.SMS_PARSING, p["template"])
        assertEquals("[SMS]: NABIL_ALERT", p["title"])
        assertEquals("Nabil Bank", p["institution"])
        assertEquals("NABIL_ALERT", p["sender"])
        assertEquals("A/C XXXXXXX789 debited by NPR 1,500 at Daraz", p["sms"])
        assertEquals("Not detected", p["parsed"])
    }

    @Test
    fun `unknown institutions fall back to the Other option`() {
        val p = params(GitHubIssues.smsParsingUrl("GIBL", "x", institution = "Global IME Bank"))
        assertEquals("Another bank or wallet (write it below)", p["institution"])
        val laxmi = params(GitHubIssues.smsParsingUrl("LSBL", "x", institution = "Laxmi Sunrise"))
        assertEquals("Laxmi Sunrise Bank", laxmi["institution"])
    }

    @Test
    fun `every template and field the app links to exists in the issue forms`() {
        val fieldsByTemplate = mapOf(
            GitHubIssues.BUG_REPORT to listOf("app-version", "device", "android"),
            GitHubIssues.SMS_PARSING to listOf("institution", "sender", "sms", "parsed", "app-version"),
            GitHubIssues.BANK_SUPPORT to emptyList(),
            GitHubIssues.FEATURE_REQUEST to emptyList(),
            GitHubIssues.FEEDBACK to listOf("app-version"),
        )
        fieldsByTemplate.forEach { (template, fields) ->
            val yaml = File(templateDir, template)
            assertTrue("missing form $template", yaml.isFile)
            val text = yaml.readText()
            fields.forEach { id -> assertTrue("$template has no field '$id'", text.contains("id: $id\n")) }
        }

        val smsForm = File(templateDir, GitHubIssues.SMS_PARSING).readText()
        listOf(
            "eSewa", "Khalti", "Nabil Bank", "NMB Bank", "Himalayan Bank", "Laxmi Sunrise Bank",
            "Siddhartha Bank", "Everest Bank", "Another bank or wallet (write it below)"
        ).forEach { option ->
            assertTrue("SMS form dropdown lacks '$option'", smsForm.contains("        - $option\n"))
        }
    }
}
