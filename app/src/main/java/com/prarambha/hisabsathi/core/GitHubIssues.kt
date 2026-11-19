package com.prarambha.hisabsathi.core

import android.os.Build
import com.prarambha.hisabsathi.BuildConfig
import java.net.URLEncoder

/**
 * Builds links that open a pre-filled GitHub issue form (see .github/ISSUE_TEMPLATE/).
 *
 * Field keys must match the `id`s in the form YAML; GitHub fills each field from the query
 * parameter of the same name. Everything goes into a public issue, so SMS text is passed
 * through [maskSensitive] first and the user still reviews it in the browser before posting.
 */
object GitHubIssues {
    const val BUG_REPORT = "1-bug_report.yml"
    const val SMS_PARSING = "2-sms_parsing.yml"
    const val BANK_SUPPORT = "3-bank_support_request.yml"
    const val FEATURE_REQUEST = "4-feature_request.yml"
    const val FEEDBACK = "5-feedback.yml"

    /** Institutions listed in the SMS parsing form's dropdown (must match the YAML exactly). */
    private val FORM_INSTITUTIONS = listOf(
        "eSewa", "Khalti", "Nabil Bank", "NMB Bank", "Himalayan Bank",
        "Laxmi Sunrise Bank", "Siddhartha Bank", "Everest Bank"
    )
    private const val OTHER_INSTITUTION = "Another bank or wallet (write it below)"

    // GitHub rejects very long URLs; keep well under its limit.
    private const val MAX_FIELD_LENGTH = 1500

    // 8+ digits in a row (optionally grouped by single spaces, as card numbers are) are account,
    // card or phone numbers. Amounts are shorter or use commas (1,00,000) and dates use dashes,
    // so neither is touched.
    private val LONG_NUMBER = Regex("""\d(?: ?\d){7,}""")

    val appVersion: String
        get() = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE}, ${BuildConfig.FLAVOR})"

    val device: String
        get() = "${Build.MANUFACTURER} ${Build.MODEL}".trim()

    val androidVersion: String
        get() = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"

    fun url(template: String, title: String? = null, fields: Map<String, String?> = emptyMap()): String {
        val params = buildList {
            add("template" to template)
            title?.let { add("title" to it) }
            fields.forEach { (key, value) ->
                if (!value.isNullOrBlank()) add(key to value.take(MAX_FIELD_LENGTH))
            }
        }
        return Constants.Links.GITHUB_URL + "/issues/new?" +
                params.joinToString("&") { (k, v) -> "${encode(k)}=${encode(v)}" }
    }

    fun bugReportUrl(): String = url(
        BUG_REPORT,
        fields = mapOf("app-version" to appVersion, "device" to device, "android" to androidVersion)
    )

    fun feedbackUrl(): String = url(FEEDBACK, fields = mapOf("app-version" to appVersion))

    fun smsParsingUrl(
        sender: String,
        smsBody: String,
        institution: String? = null,
        parsed: String? = null,
    ): String {
        val maskedSender = maskSensitive(sender)
        return url(
            SMS_PARSING,
            title = "[SMS]: $maskedSender",
            fields = mapOf(
                "institution" to institutionOption(institution),
                "sender" to maskedSender,
                "sms" to maskSensitive(smsBody),
                "parsed" to parsed,
                "app-version" to appVersion,
            )
        )
    }

    /** Replaces all but the last 3 digits of long digit runs with X. */
    fun maskSensitive(text: String): String = LONG_NUMBER.replace(text) { match ->
        val digitsSeen = match.value.count { it.isDigit() }
        var index = 0
        match.value.map { c ->
            if (c.isDigit()) {
                index++
                if (index <= digitsSeen - 3) 'X' else c
            } else c
        }.joinToString("")
    }

    private fun institutionOption(name: String?): String {
        if (name.isNullOrBlank()) return OTHER_INSTITUTION
        val n = name.lowercase()
        // Match on the first word: "Nabil", "NMB", "Laxmi", "eSewa"…
        return FORM_INSTITUTIONS.firstOrNull { n.contains(it.lowercase().substringBefore(' ')) }
            ?: OTHER_INSTITUTION
    }

    // GitHub decodes '+' as a literal plus in some fields; use %20 for spaces.
    private fun encode(value: String) = URLEncoder.encode(value, "UTF-8").replace("+", "%20")
}
