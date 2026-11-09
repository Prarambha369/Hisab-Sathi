package com.ritesh.parser.core.bank

/**
 * Factory for creating bank-specific parsers based on SMS sender.
 * Hisab Sathi - नेपाली एसएमएस बैंकिङ एआई
 * Supports only Nepali banks and digital wallets
 */
object BankParserFactory {

    private val parsers = listOf(
        // Digital Wallets
        EsewaParser(),      // eSewa - Nepal's most popular digital wallet
        KhaltiParser(),     // Khalti Digital Wallet
        
        // Commercial Banks
        NabilBankParser(),          // Nabil Bank - First private bank of Nepal
        NMBBankParser(),            // NMB Bank Limited
        EverestBankParser(),        // Everest Bank Limited
        LaxmiBankParser(),          // Laxmi Sunrise Bank
        SiddharthaBankParser(),     // Siddhartha Bank Limited
        HimalayanBankParser()       // Himalayan Bank Limited
    )

    /**
     * Returns the appropriate bank parser for the given sender.
     * Returns null if no specific parser is found.
     */
    fun getParser(sender: String): BankParser? {
        return parsers.firstOrNull { it.canHandle(sender) }
    }

    /**
     * Returns the bank parser for the given bank name.
     * Returns null if no specific parser is found.
     */
    fun getParserByName(bankName: String): BankParser? {
        return parsers.firstOrNull { it.getBankName() == bankName }
    }

    /**
     * Returns all available bank parsers.
     */
    fun getAllParsers(): List<BankParser> = parsers

    /**
     * Checks if the sender belongs to any known bank.
     */
    fun isKnownBankSender(sender: String): Boolean {
        return parsers.any { it.canHandle(sender) }
    }
    
    /**
     * Returns the list of supported Nepali banks and wallets.
     */
    fun getSupportedBanks(): List<String> {
        return parsers.map { it.getBankName() }
    }
}
