package com.prarambha.parser.core

object CompiledPatterns {
    object Amount {
        val RS_PATTERN = Regex("""Rs\.?\s*([0-9,]+(?:\.\d{2})?)""", RegexOption.IGNORE_CASE)
        val NPR_PATTERN = Regex("""NPR\.?\s*([0-9,]+(?:\.\d{1,2})?)""", RegexOption.IGNORE_CASE)
        val NEPALI_RUPEE_PATTERN = Regex("""(?:रु|रू)\.?\s*([0-9,]+(?:\.\d{1,2})?)""")
        val ALL_PATTERNS = listOf(NPR_PATTERN, RS_PATTERN, NEPALI_RUPEE_PATTERN)
    }

    object Reference {
        val GENERIC_REF = Regex(
            """(?:Ref|Reference|Txn|Transaction)(?:\s+No)?[:\s]+([A-Z0-9]+)""",
            RegexOption.IGNORE_CASE
        )
        val REF_NUMBER = Regex("""Reference\s+Number[:\s]+([A-Z0-9]+)""", RegexOption.IGNORE_CASE)
        val ALL_PATTERNS = listOf(GENERIC_REF, REF_NUMBER)
    }

    object Account {
        val AC_WITH_MASK = Regex(
            """(?:A/c|Account|Acct)(?:\s+No)?\.?\s+(?:[Xx\*]*\**)?(\d+)""",
            RegexOption.IGNORE_CASE
        )
        val CARD_WITH_MASK = Regex("""Card\s+(?:[Xx\*]*\**)?(\d+)""", RegexOption.IGNORE_CASE)
        val GENERIC_ACCOUNT =
            Regex("""(?:A/c|Account).*?(\d+)(?:\s|$)""", RegexOption.IGNORE_CASE)
        val ALL_PATTERNS = listOf(AC_WITH_MASK, CARD_WITH_MASK, GENERIC_ACCOUNT)
    }

    object Balance {
        val AVL_BAL_RS = Regex("""(?:Bal|Balance|Avl Bal|Available Balance)[:\s]+Rs\.?\s*([0-9,]+(?:\.\d{2})?)""", RegexOption.IGNORE_CASE)
        val AVL_BAL_NPR = Regex("""(?:Bal|Balance|Avl Bal|Available Balance)[:\s]+NPR\.?\s*([0-9,]+(?:\.\d{1,2})?)""", RegexOption.IGNORE_CASE)
        val AVL_BAL_NEPALI_RUPEE = Regex("""(?:Bal|Balance|Avl Bal|Available Balance)[:\s]+(?:रु|रू)\.?\s*([0-9,]+(?:\.\d{1,2})?)""", RegexOption.IGNORE_CASE)
        val AVL_BAL_NO_CURRENCY = Regex("""(?:Bal|Balance|Avl Bal|Available Balance)[:\s]+([0-9,]+(?:\.\d{2})?)""", RegexOption.IGNORE_CASE)
        val UPDATED_BAL_RS = Regex("""(?:Updated Balance|Remaining Balance)[:\s]+Rs\.?\s*([0-9,]+(?:\.\d{2})?)""", RegexOption.IGNORE_CASE)
        val UPDATED_BAL_NPR = Regex("""(?:Updated Balance|Remaining Balance)[:\s]+NPR\.?\s*([0-9,]+(?:\.\d{1,2})?)""", RegexOption.IGNORE_CASE)
        val ALL_PATTERNS = listOf(AVL_BAL_NPR, AVL_BAL_RS, AVL_BAL_NEPALI_RUPEE, AVL_BAL_NO_CURRENCY, UPDATED_BAL_NPR, UPDATED_BAL_RS)
    }

    object Merchant {
        val TO_PATTERN =
            Regex("""to\s+([^\.\n]+?)(?:\s+on|\s+at|\s+Ref)""", RegexOption.IGNORE_CASE)
        val FROM_PATTERN =
            Regex("""from\s+([^\.\n]+?)(?:\s+on|\s+at|\s+Ref)""", RegexOption.IGNORE_CASE)
        val AT_PATTERN = Regex("""at\s+([^\.\n]+?)(?:\s+on|\s+Ref)""", RegexOption.IGNORE_CASE)
        val FOR_PATTERN =
            Regex("""for\s+([^\.\n]+?)(?:\s+on|\s+at|\s+Ref)""", RegexOption.IGNORE_CASE)
        val ALL_PATTERNS = listOf(TO_PATTERN, FROM_PATTERN, AT_PATTERN, FOR_PATTERN)
    }

    /**
     * Nepali bank-specific patterns
     */
    object Nabil {
        val DLT_PATTERNS = listOf(
            Regex("^[A-Z]{2}-NABIL.*$"),
            Regex("^NABIL-[A-Z]+$"),
            Regex("^NABILBANK$")
        )
    }
    
    object NMB {
        val DLT_PATTERNS = listOf(
            Regex("^[A-Z]{2}-NMB.*$"),
            Regex("^NMB-[A-Z]+$"),
            Regex("^NMBBANK$")
        )
    }
    
    object Everest {
        val DLT_PATTERNS = listOf(
            Regex("^[A-Z]{2}-EVEREST.*$"),
            Regex("^EVEREST-[A-Z]+$"),
            Regex("^EVERESTBANK$")
        )
    }
    
    object Laxmi {
        val DLT_PATTERNS = listOf(
            Regex("^[A-Z]{2}-LAXMI.*$"),
            Regex("^LAXMI-[A-Z]+$"),
            Regex("^LAXMIBANK$")
        )
    }
    
    object Siddhartha {
        val DLT_PATTERNS = listOf(
            Regex("^[A-Z]{2}-SIDDHARTHA.*$"),
            Regex("^SIDDHARTHA-[A-Z]+$"),
            Regex("^SBL$")
        )
    }
    
    object Himalayan {
        val DLT_PATTERNS = listOf(
            Regex("^[A-Z]{2}-HIMALAYAN.*$"),
            Regex("^HIMALAYAN-[A-Z]+$"),
            Regex("^HIMALAYANBANK$")
        )
    }
    
    object Esewa {
        val DLT_PATTERNS = listOf(
            Regex("^ESEWA$"),
            Regex("^ESEWAPAYMENT$")
        )
    }
    
    object Khalti {
        val DLT_PATTERNS = listOf(
            Regex("^KHALTI$"),
            Regex("^KHALTIDIGITAL$")
        )
    }

    object Cleaning {
        val TRAILING_PARENTHESES = Regex("""\s*\(.*?\)\s*$""")
        val REF_NUMBER_SUFFIX = Regex("""\s+Ref\s+No.*""", RegexOption.IGNORE_CASE)
        val DATE_SUFFIX = Regex("""\s+on\s+\d{2}.*""")
        val TIME_SUFFIX = Regex("""\s+at\s+\d{2}:\d{2}.*""")
        val TRAILING_DASH = Regex("""\s*-\s*$""")
        val PVT_LTD =
            Regex("""(\s+PVT\.?\s*LTD\.?|\s+PRIVATE\s+LIMITED)$""", RegexOption.IGNORE_CASE)
        val LTD = Regex("""(\s+LTD\.?|\s+LIMITED)$""", RegexOption.IGNORE_CASE)
    }
}
