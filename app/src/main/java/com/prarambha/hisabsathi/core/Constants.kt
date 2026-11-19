package com.prarambha.hisabsathi.core

/**
 * Application-wide constants to avoid hardcoded values
 */
object Constants {

    /**
     * Hisab Sathi is Nepal-only: every currency default and fallback is NPR.
     * Other currencies stay selectable for foreign-currency transactions.
     */
    const val DEFAULT_CURRENCY = "NPR"
    
    /**
     * SMS Processing Configuration
     */
    object SmsProcessing {
        const val DEFAULT_BATCH_SIZE = 100
        const val SMS_PREVIEW_LENGTH = 200
        const val QUERY_LIMIT = 100
        const val INITIAL_SCAN_MONTHS = 3
        const val SCANNING_DELAY_MS = 3000L
    }
    
    /**
     * UI Configuration - Moved to ui/theme/Dimensions.kt for better organization
     * Keeping only non-dimension constants here
     */
    object UI {
        const val BUTTON_WIDTH_RATIO = 0.8f
        const val PROGRESS_STROKE_WIDTH = 2f
    }
    
    /**
     * Database Configuration
     */
    object Database {
        const val DATABASE_NAME = "hisabsathi_database"
        const val CURRENT_VERSION = 2
        const val TRANSACTION_HASH_DEFAULT = ""
    }
    
    /**
     * WorkManager Configuration
     */
    object WorkManager {
        const val SMS_READER_WORK_NAME = "sms_reader_work"
        const val PERIODIC_SCAN_INTERVAL_HOURS = 24L
        const val INITIAL_DELAY_MINUTES = 15L
    }
    
    /**
     * Parsing Configuration
     */
    object Parsing {
        const val MIN_MERCHANT_NAME_LENGTH = 2
        const val MD5_ALGORITHM = "MD5"
        const val AMOUNT_SCALE = 2
        const val CONFIDENCE_PATTERN_BASED = 0.7f
        const val CONFIDENCE_AI_BASED = 0.9f
    }
    
    /**
     * Navigation Routes
     */
    object Routes {
        const val HOME = "home"
        const val TRANSACTIONS = "transactions"
        const val ANALYTICS = "analytics"
        const val CHAT = "chat"
        const val SETTINGS = "settings"
    }
    
    // On-device AI models: see data/model/AiModel.kt (AiModelCatalog).

    /**
     * External Links
     */
    object Links {
        const val GITHUB_URL = "https://github.com/Prarambha369/Hisab-Sathi"
        const val WEBSITE_URL = "$GITHUB_URL/releases"
        const val PRIVACY_POLICY_URL = "$GITHUB_URL/blob/main/PRIVACY.md"
        const val TERMS_OF_SERVICE_URL = "$GITHUB_URL/blob/main/TERMS.md"
        const val FAQ_URL = "$GITHUB_URL/blob/main/docs/FAQ.md"
        const val GUIDE_URL = "$GITHUB_URL/blob/main/docs/USER_GUIDE.md"
        const val REPORT_BUG_URL = "https://github.com/Prarambha369/Hisab-Sathi/issues/new/choose"
    }
}
