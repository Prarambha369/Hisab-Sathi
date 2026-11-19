package com.prarambha.hisabsathi.presentation.common.icons

import com.prarambha.hisabsathi.R

object BrandIcons {
    private val NTC_WORD = Regex("""\bntc\b""", RegexOption.IGNORE_CASE)

    // Nepal-only: Nepali banks plus global services commonly paid for from Nepal.
    // Matching is by substring of the lowercased merchant, so keys must be specific.
    private val brandMappings = mapOf(
        // Banks - Nepal
        "everest bank" to R.drawable.ic_brand_everest_bank,
        "laxmi sunrise" to R.drawable.ic_brand_laxmi_sunrise_bank,
        "nmb bank" to R.drawable.ic_brand_nmb_bank,
        "siddhartha bank" to R.drawable.ic_brand_siddhartha_bank,
        "prime commercial bank" to R.drawable.ic_brand_prime_commercial_bank,
        "standard chartered" to R.drawable.ic_brand_standard_chartered,
        "american express" to R.drawable.ic_brand_amex,
        "amex" to R.drawable.ic_brand_amex,
        // Airlines serving Nepal
        "air india" to R.drawable.ic_brand_air_india,
        "indigo" to R.drawable.ic_brand_indigo,
        // Food chains in Nepal
        "kfc" to R.drawable.ic_brand_kfc,
        "pizza hut" to R.drawable.ic_brand_pizza_hut,
        "burger king" to R.drawable.ic_brand_burger_king,
        "dominos" to R.drawable.ic_brand_dominos,
        // Global subscriptions / online services
        "netflix" to R.drawable.ic_brand_netflix,
        "spotify" to R.drawable.ic_brand_spotify,
        "youtube" to R.drawable.ic_brand_youtube,
        "apple music" to R.drawable.ic_brand_apple_music,
        "amazon prime" to R.drawable.ic_brand_amazon_prime,
        "amazon" to R.drawable.ic_brand_amazon,
        "microsoft teams" to R.drawable.ic_brand_microsoft_teams,
        "slack" to R.drawable.ic_brand_slack,
        "zoom" to R.drawable.ic_brand_zoom,
        "coursera" to R.drawable.ic_brand_coursera,
        "udemy" to R.drawable.ic_brand_udemy,
        "udacity" to R.drawable.ic_brand_udacity,
        // Fitness
        "anytime fitness" to R.drawable.ic_brand_anytime_fitness,
        "golds gym" to R.drawable.ic_brand_golds_gym,
    )

    fun getIconResource(merchantName: String): Int? {
        val normalized = merchantName.lowercase()
        return brandMappings.entries.firstOrNull { (key, _) -> normalized.contains(key) }?.value
    }

    fun getBrandColor(merchantName: String): String? = when {
        // Wallets & banks - Nepal
        merchantName.contains("esewa", ignoreCase = true) -> "#60BB46"
        merchantName.contains("khalti", ignoreCase = true) -> "#5C2D91"
        merchantName.contains("ime pay", ignoreCase = true) -> "#E31E24"
        merchantName.contains("fonepay", ignoreCase = true) -> "#ED1C24"
        merchantName.contains("nabil", ignoreCase = true) -> "#0C4DA2"
        merchantName.contains("nmb bank", ignoreCase = true) -> "#E2231A"
        merchantName.contains("himalayan bank", ignoreCase = true) -> "#003F87"
        merchantName.contains("laxmi sunrise", ignoreCase = true) -> "#8B1D41"
        merchantName.contains("siddhartha bank", ignoreCase = true) -> "#1B3E93"
        merchantName.contains("everest bank", ignoreCase = true) -> "#ED1C24"
        merchantName.contains("prime commercial bank", ignoreCase = true) -> "#0054A6"
        merchantName.contains("standard chartered", ignoreCase = true) -> "#0072AA"

        // Food & delivery
        merchantName.contains("foodmandu", ignoreCase = true) -> "#E53935"
        merchantName.contains("pathao", ignoreCase = true) -> "#ED1C24"
        merchantName.contains("kfc", ignoreCase = true) -> "#F40027"
        merchantName.contains("pizza hut", ignoreCase = true) -> "#EE3A43"
        merchantName.contains("dominos", ignoreCase = true) -> "#0066B3"
        merchantName.contains("himalayan java", ignoreCase = true) -> "#4E2A1E"

        // Shopping & groceries
        merchantName.contains("daraz", ignoreCase = true) -> "#F85606"
        merchantName.contains("bhatbhateni", ignoreCase = true) -> "#D71920"

        // Telecom & internet
        merchantName.contains("ncell", ignoreCase = true) -> "#6A1B9A"
        merchantName.contains("nepal telecom", ignoreCase = true) ||
        NTC_WORD.containsMatchIn(merchantName) -> "#0055A5"
        merchantName.contains("worldlink", ignoreCase = true) -> "#1A4D8F"
        merchantName.contains("vianet", ignoreCase = true) -> "#E30613"

        // Entertainment
        merchantName.contains("netflix", ignoreCase = true) -> "#E50914"
        merchantName.contains("spotify", ignoreCase = true) -> "#1DB954"
        merchantName.contains("youtube", ignoreCase = true) -> "#FF0000"
        merchantName.contains("qfx", ignoreCase = true) -> "#E4002B"

        // Travel
        merchantName.contains("buddha air", ignoreCase = true) -> "#00529B"
        merchantName.contains("yeti airlines", ignoreCase = true) -> "#0A3D7A"
        merchantName.contains("nepal airlines", ignoreCase = true) -> "#C8102E"

        else -> null
    }
}
