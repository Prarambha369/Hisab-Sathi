package com.ritesh.parser.core.bank

/**
 * Registry for bank-specific parsers.
 *
 * This class wraps an ordered list of [BankParser] instances and provides
 * parser-discovery / selection by SMS sender ID.  Callers can either inject
 * their own list of parsers (useful for testing) or use the pre-built
 * [BankParserRegistry.default] instance which contains all parsers registered
 * in [BankParserFactory].
 *
 * ## Usage
 *
 * ```kotlin
 * // Use the default registry
 * val registry = BankParserRegistry.default()
 * val parser = registry.getParser(senderAddress)
 * val result = parser?.parse(smsBody, senderAddress, timestamp)
 *
 * // Inject a custom parser list (e.g. in tests)
 * val testRegistry = BankParserRegistry(listOf(MyParser()))
 * ```
 *
 * The static [BankParserFactory] object exposes the same functionality for
 * code that prefers a singleton access pattern.
 */
class BankParserRegistry(private val parsers: List<BankParser>) {

    /**
     * Returns the first parser that can handle [sender], or `null` if none
     * matches.
     */
    fun getParser(sender: String): BankParser? =
        parsers.firstOrNull { it.canHandle(sender) }

    /**
     * Returns all registered parsers in priority order.
     */
    fun all(): List<BankParser> = parsers

    /**
     * Returns `true` when at least one registered parser can handle [sender].
     */
    fun isKnownSender(sender: String): Boolean =
        parsers.any { it.canHandle(sender) }

    companion object {
        /**
         * Returns a [BankParserRegistry] pre-loaded with all parsers registered
         * in [BankParserFactory], in priority order.
         */
        fun default(): BankParserRegistry =
            BankParserRegistry(BankParserFactory.getAllParsers())
    }
}
