package com.prarambha.hisabsathi.presentation.common.icons

import com.prarambha.hisabsathi.BuildConfig

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import com.prarambha.hisabsathi.R
import com.prarambha.hisabsathi.utils.IconResolutionUtils
import com.prarambha.hisabsathi.data.database.entity.CategoryEntity
import com.prarambha.hisabsathi.data.database.entity.SubcategoryEntity
import java.util.Locale

/**
 * Centralized category mapping system for consistent categorization
 * across transactions and subscriptions
 */
object CategoryMapping {

    data class CategoryInfo(
        val displayName: String,
        val iconResId: Int,
        val color: Color,
        val fallbackIcon: ImageVector = Icons.Default.Category
    )

    // --- helpers ---
    private val regexCache = mutableMapOf<String, Regex>()

    private fun matches(
        merchantRaw: String,
        anyOf: Set<String>,
        noneOf: Set<String> = emptySet()
    ): Boolean {
        val m = merchantRaw.lowercase(Locale.ROOT)
        if (noneOf.any { m.contains(it) }) return false

        // Check for exact matches or word boundaries
        return anyOf.any { keyword ->
            // For single word keywords, check word boundaries
            if (!keyword.contains(" ")) {
                val regex = regexCache.getOrPut(keyword) {
                    "\\b${keyword.replace(".", "\\.")}\\b".toRegex()
                }
                regex.containsMatchIn(m)
            } else {
                // For multi-word keywords, use contains as before
                m.contains(keyword)
            }
        }
    }

    // --- keyword sets (single source of truth) ---
    // Nepal-only: Nepali merchants and services first, plus generic terms and the few
    // global services commonly paid for from Nepal. Single-word keywords match on word
    // boundaries; multi-word keywords match as substrings.
    private val FOOD = setOf(
        // generic
        "restaurant", "resturant", "cafe", "café", "coffee", "bakery", "food", "momo",
        "khaja", "bhojanalaya", "sekuwa", "chiya", "tea house", "canteen", "pizza", "burger",
        "dining", "kitchen", "eatery", "sweets", "mithai",
        // delivery
        "foodmandu", "pathao food", "bhoj", "foodpanda",
        // chains in Nepal
        "kfc", "pizza hut", "dominos", "domino's", "burger king", "baskin robbins",
        "himalayan java", "coffee bean", "roadhouse", "bajeko sekuwa", "bota momo",
        "nanglo", "chikusa", "the burger house", "cinnabon", "bakery cafe", "bakery café",
        "tukche thakali", "thakali", "newari", "big belly", "subway", "starbucks"
    )

    private val GROCERY = setOf(
        "grocery", "kirana", "supermarket", "super market", "departmental store",
        "bhatbhateni", "big mart", "bigmart", "salesberry", "saleways", "kk mart",
        "himalayan supermarket", "civil mall mart", "q mart", "family mart", "daily needs",
        "fresh mart", "tarkari", "vegetable", "fruits", "dairy", "meat shop", "khasi",
        "kalimati", "cg mart", "ooji", "sajha pasal"
    )

    private val TRANSPORT = setOf(
        "pathao", "indrive", "in drive", "tootle", "uber", "taxi", "tempo", "micro bus",
        "microbus", "sajha yatayat", "sajha bus", "bus park", "bus ticket", "bus sewa",
        "yatayat", "petrol", "diesel", "fuel", "petrol pump", "filling station",
        "nepal oil", "noc", "parking", "toll", "bluebook", "yatayat byawastha",
        "transport management", "driving license"
    )

    private val SHOPPING = setOf(
        "daraz", "sastodeal", "hamrobazar", "hamro bazar", "thulo", "okdam", "gyapu",
        "muncha", "smartdoko", "store", "mart", "pasal", "shopping", "mall",
        "civil mall", "labim mall", "city centre", "kathmandu mall", "chhaya center",
        "rising mall", "sherpa mall", "ctc mall", "bluebird mall"
    )

    private val SHOPPING_EXCLUDE = setOf("big mart", "bigmart", "kk mart", "q mart", "family mart", "fresh mart", "cg mart")

    private val SHOPPING_EXTENDED = setOf(
        "goldstar", "caliber shoes", "bata", "baby bazaar", "cg electronics", "cg digital",
        "hukut", "oliz store", "neostore", "itti", "dealayo", "electronics", "mobile shop",
        "samsung plaza", "apple store", "nike", "adidas", "puma", "zara", "h&m", "uniqlo",
        "amazon", "aliexpress", "temu", "shein", "paypal", "adobe", "openai", "chatgpt",
        "fiverr", "canva"
    )

    private val UTILITIES = setOf(
        "electricity", "nea", "nepal electricity", "bijuli", "khanepani", "water supply",
        "kukl", "water", "gas", "lpg", "cylinder", "internet", "broadband", "wifi",
        "worldlink", "vianet", "classic tech", "classictech", "subisu", "dishhome",
        "dish home", "nettv", "net tv", "simtv", "sim tv", "clear tv", "techminds",
        "websurfer", "bill", "garbage", "fohor", "municipality", "nagarpalika"
    )

    private val ENTERTAINMENT = setOf(
        "netflix", "spotify", "youtube", "prime video", "disney", "hbo", "apple music",
        "cinema", "movie", "qfx", "big movies", "fcube", "f-cube", "one cinemas",
        "movie ticket", "concert", "event", "khalti ticket", "esewa ticket", "game", "steam",
        "playstation", "xbox", "pubg", "free fire"
    )

    private val HEALTHCARE = setOf(
        "pharmacy", "medical", "medicals", "hospital", "clinic", "doctor", "dental",
        "diagnostic", "lab", "polyclinic", "aushadhi", "pharma", "health",
        "norvic", "grande", "mediciti", "nepal mediciti", "hams", "bir hospital",
        "teaching hospital", "tuth", "om hospital", "b&b hospital", "kist", "civil hospital",
        "patan hospital", "kathmandu model", "star hospital", "alka hospital", "sumeru",
        "nidan", "tilganga"
    )

    private val INVESTMENT = setOf(
        "nepse", "meroshare", "mero share", "cdsc", "demat", "ipo", "fpo", "share",
        "shares", "mutual fund", "sip", "broker", "capital", "securities", "nabil invest",
        "nibl ace", "siddhartha capital", "laxmi sunrise capital", "nmb capital",
        "global ime capital", "ns capital", "gold", "silver"
    )

    private val BANKING = setOf(
        "fund transfer", "connectips", "connect ips", "rtgs", "fonepay", "nchl",
        "atm", "cash deposit", "branch", "remittance", "ime remit", "prabhu money",
        "city express", "western union", "moneygram", "himal remit", "bank transfer"
    )

    private val PERSONAL_CARE = setOf(
        "salon", "parlour", "parlor", "beauty", "spa", "barber", "cosmetics", "nykaa",
        "foreveryng", "sastobazaar beauty", "grooming", "laundry", "dry cleaning",
        "tailor", "darji"
    )

    private val EDUCATION = setOf(
        "school", "college", "campus", "university", "tuition", "coaching", "institute",
        "academy", "exam fee", "admission", "tribhuvan university", "kathmandu university", "pokhara university",
        "purbanchal", "neb", "bridge course", "ielts", "pte", "toefl", "consultancy",
        "books", "stationery", "stationary"
    )

    private val MOBILE = setOf(
        "ntc", "nepal telecom", "namaste", "ncell", "smart cell", "smartcell", "recharge",
        "topup", "top up", "top-up", "data pack", "sim card", "esewa load", "khalti load",
        "wallet load", "load fund"
    )

    private val FITNESS = setOf(
        "gym", "fitness", "yoga", "zumba", "crossfit", "futsal", "swimming", "pool",
        "badminton", "sports club", "martial arts", "taekwondo", "karate"
    )

    private val INSURANCE = setOf(
        "insurance", "bima", "life insurance", "nepal life", "national life", "lic nepal",
        "metlife", "sanima life", "himalayan life", "shikhar insurance", "sagarmatha lumbini",
        "neco insurance", "insurance premium"
    )

    private val TAX = setOf(
        "tax", "income tax", "ird", "inland revenue", "vat", "tds", "customs", "malpot",
        "land revenue", "house tax", "property tax", "vehicle tax", "road tax",
        "sampati kar"
    )

    private val BANK_CHARGE = setOf(
        "service charge", "bank charge", "sms charge", "sms alert", "annual fee",
        "debit card fee", "card fee", "maintenance fee", "penalty", "ats charge",
        "commission", "processing fee", "stamp duty"
    )

    private val CC_PAYMENT = setOf(
        "credit card payment", "card payment received", "credit card bill", "cc payment",
        "credit card repayment"
    )

    private val TRAVEL = setOf(
        // airlines operating in Nepal
        "buddha air", "yeti airlines", "shree airlines", "nepal airlines", "tara air",
        "summit air", "sita air", "saurya", "himalaya airlines", "qatar airways",
        "emirates", "flydubai", "air arabia", "air india", "indigo", "malaysia airlines",
        "thai airways", "turkish airlines", "korean air", "air china", "cathay",
        "singapore airlines", "etihad",
        // booking / travel agents
        "air ticket", "flight", "airlines", "airways", "travels", "tours", "treks",
        "trekking", "expedition", "travel agency", "booking.com", "agoda", "airbnb",
        "makemytrip", "trip.com", "nepal tourism", "ntb", "tims", "trekking permit",
        "national park", "conservation area", "entry fee",
        // stays
        "hotel", "resort", "lodge", "guest house", "guesthouse", "homestay", "hostel",
        "soaltee", "hyatt", "marriott", "radisson", "yak & yeti", "yak and yeti",
        "annapurna hotel", "shangri-la", "dwarika", "aloft", "fairfield",
        "fulbari", "temple tree", "barahi", "chandragiri", "tiger tops",
        // places
        "pokhara", "chitwan", "lumbini", "nagarkot", "bandipur", "mustang", "everest"
    )

    // --- single ordered rule list (priority preserved) ---
    private data class Rule(
        val categoryName: String,
        val includes: Set<String>,
        val excludes: Set<String> = emptySet()
    )

    private val RULES: List<Rule> = listOf(
        Rule("Tax", TAX),
        Rule("Hidden Charges", BANK_CHARGE),
        Rule("Credit Bill", CC_PAYMENT),
        Rule("Food & Drinks", FOOD),
        Rule("Groceries", GROCERY),
        Rule("Transport", TRANSPORT),
        Rule("Shopping", SHOPPING + SHOPPING_EXTENDED, SHOPPING_EXCLUDE),
        Rule("Bill", UTILITIES),
        Rule("Entertainment", ENTERTAINMENT),
        Rule("Medical", HEALTHCARE),
        Rule("Investment", INVESTMENT),
        Rule("Business", BANKING),
        Rule("Personal", PERSONAL_CARE),
        Rule("Children", EDUCATION),
        Rule("Top-up", MOBILE),
        Rule("Fitness", FITNESS),
        Rule("Insurance", INSURANCE),
        Rule("Travel", TRAVEL),
    )

    //find duplicates in each rule and print it from RULES
    public fun findDuplicateKeywords(): Set<String> {
        val duplicates = RULES.flatMap { it.includes + it.excludes }.groupingBy { it }.eachCount()
            .filter { it.value > 1 }.keys
        if (duplicates.isNotEmpty()) {
            if (BuildConfig.DEBUG) println("Duplicate keywords found across categories: $duplicates")
        }
        return duplicates
    }

    // Define all categories with their visual properties
    val categories = mapOf(
        "Food & Drinks" to CategoryInfo(
            displayName = "Food & Drinks",
            iconResId = R.drawable.type_food_stuffed_flatbread,
            color = Color(0xFFFC8019),
        ),
        "Transport" to CategoryInfo(
            displayName = "Transport",
            iconResId = R.drawable.type_travel_transport_airplane,
            color = Color(0xFF0066CC),
        ),
        "Shopping" to CategoryInfo(
            displayName = "Shopping",
            iconResId = R.drawable.type_shopping_shopping_bags,
            color = Color(0xFF893BBE),
        ),
        "Groceries" to CategoryInfo(
            displayName = "Groceries",
            iconResId = R.drawable.type_groceries_bread,
            color = Color(0xFF9E7155),
        ),
        "Home" to CategoryInfo(
            displayName = "Home",
            iconResId = R.drawable.type_event_and_place_house,
            color = Color(0xFFFFC107),
        ),
        "Entertainment" to CategoryInfo(
            displayName = "Entertainment",
            iconResId = R.drawable.type_snack_popcorn,
            color = Color(0xFFCC1A56),
        ),
        "Events" to CategoryInfo(
            displayName = "Events",
            iconResId = R.drawable.type_event_and_place_party_popper,
            color = Color(0xFF9C27B0),
        ),
        "Travel" to CategoryInfo(
            displayName = "Travel",
            iconResId = R.drawable.type_travel_transport_luggage,
            color = Color(0xFF0066CC),
        ),
        "Medical" to CategoryInfo(
            displayName = "Medical",
            iconResId = R.drawable.type_health_pill,
            color = Color(0xFFFF0041),
        ),
        "Personal" to CategoryInfo(
            displayName = "Personal",
            iconResId = R.drawable.type_tool_electronic_scissors,
            color = Color(0xFF9C27B0),
        ),
        "Fitness" to CategoryInfo(
            displayName = "Fitness",
            iconResId = R.drawable.type_sports_baseball,
            color = Color(0xFF91CC4D),
        ),
        "Services" to CategoryInfo(
            displayName = "Services",
            iconResId = R.drawable.type_tool_electronic_high_voltage,
            color = Color(0xFFFF9800),
        ),
        "Bill" to CategoryInfo(
            displayName = "Bill",
            iconResId = R.drawable.type_travel_transport_admission_tickets,
            color = Color(0xFFFF0041),
        ),
        "Subscription" to CategoryInfo(
            displayName = "Subscription",
            iconResId = R.drawable.type_tool_electronic_clapper_board,
            color = Color(0xFF3F51B5),
        ),
        "EMI" to CategoryInfo(
            displayName = "EMI",
            iconResId = R.drawable.type_travel_transport_automobile,
            color = Color(0xFFFF0041),
        ),
        "Credit Bill" to CategoryInfo(
            displayName = "Credit Bill",
            iconResId = R.drawable.type_stationary_card_file_box,
            color = Color(0xFFFF9800),
        ),
        "Investment" to CategoryInfo(
            displayName = "Investment",
            iconResId = R.drawable.type_flower_and_tree_herb,
            color = Color(0xFF91CC4D),
        ),
        "Support" to CategoryInfo(
            displayName = "Support",
            iconResId = R.drawable.type_health_stethoscope,
            color = Color(0xFF673AB7),
        ),
        "Insurance" to CategoryInfo(
            displayName = "Insurance",
            iconResId = R.drawable.type_health_mending_heart,
            color = Color(0xFFFF0041),
        ),
        "Tax" to CategoryInfo(
            displayName = "Tax",
            iconResId = R.drawable.type_finance_chart_decreasing,
            color = Color(0xFFFF5722),
        ),
        "Top-up" to CategoryInfo(
            displayName = "Top-up",
            iconResId = R.drawable.type_finance_money_bag,
            color = Color(0xFFFF9800),
        ),
        "Children" to CategoryInfo(
            displayName = "Children",
            iconResId = R.drawable.type_fruit_kiwi_fruit,
            color = Color(0xFF8BC34A),
        ),
        "Pet Care" to CategoryInfo(
            displayName = "Pet Care",
            iconResId = R.drawable.type_animal_dog_face,
            color = Color(0xFFF44336),
        ),
        "Business" to CategoryInfo(
            displayName = "Business",
            iconResId = R.drawable.type_finance_classical_building,
            color = Color(0xFF795548),
        ),
        "Miscellaneous" to CategoryInfo(
            displayName = "Miscellaneous",
            iconResId = R.drawable.type_stationary_clipboard,
            color = Color(0xFF91CC4D),
        ),
        "Self Transfer" to CategoryInfo(
            displayName = "Self Transfer",
            iconResId = R.drawable.type_finance_bank,
            color = Color(0xFF795548),
        ),
        "Savings" to CategoryInfo(
            displayName = "Savings",
            iconResId = R.drawable.type_sports_bullseye,
            color = Color(0xFFFF0041),
        ),
        "Gift" to CategoryInfo(
            displayName = "Gift",
            iconResId = R.drawable.type_stationary_wrapped_gift,
            color = Color(0xFFFF5722),
        ),
        "Lent" to CategoryInfo(
            displayName = "Lent",
            iconResId = R.drawable.type_finance_money_with_wings,
            color = Color(0xFF4CAF50),
        ),
        "Donation" to CategoryInfo(
            displayName = "Donation",
            iconResId = R.drawable.type_health_drop_of_blood,
            color = Color(0xFFFF4081),
        ),
        "Hidden Charges" to CategoryInfo(
            displayName = "Hidden Charges",
            iconResId = R.drawable.type_animal_goblin,
            color = Color(0xFFF44336),
        ),
        "Cash Withdrawal" to CategoryInfo(
            displayName = "Cash Withdrawal",
            iconResId = R.drawable.type_finance_dollar_banknote,
            color = Color(0xFF8BC34A),
        ),
        "Return" to CategoryInfo(
            displayName = "Return",
            iconResId = R.drawable.type_finance_currency_exchange,
            color = Color(0xFF33B5E5),
        ),
        "Salary" to CategoryInfo(
            displayName = "Salary",
            iconResId = R.drawable.type_finance_dollar_banknote,
            color = Color(0xFF8BC34A),
        )
    )

    /**
     * Get category for a merchant name (unified logic)
     */
    fun getCategory(merchantName: String): String {
        val merchantLower = merchantName.lowercase(Locale.ROOT)
        for (rule in RULES) {
            if (matches(merchantLower, rule.includes, rule.excludes)) {
                return rule.categoryName
            }
        }
        return "Miscellaneous"
    }
}

/**
 * Icon provider with fallback mechanism
 */
object IconProvider {

    /**
     * Get icon with full fallback chain for transactions
     * Priority: Brand Icon > Subcategory Icon > Category Icon > CategoryMapping > Miscellaneous
     */
    fun getIconForTransaction(
        context: Context? = null,
        merchantName: String,
        categoryEntity: CategoryEntity? = null,
        subcategoryEntity: SubcategoryEntity? = null,
        category: String? = null,
        subcategory: String? = null,
        accountIconResId: Int = 0,
        accountIconName: String? = null
    ): IconResource {
        // brand icon first
        BrandIcons.getIconResource(merchantName)?.let { iconRes ->
            return IconResource.DrawableResource(iconRes)
        }

        // subcategory icon
        subcategoryEntity?.let { subcat ->
            val resId = if (!subcat.iconName.isNullOrEmpty()) {
                IconResolutionUtils.nameToResId(context, subcat.iconName)
            } else {
                IconResolutionUtils.getSafeResId(context ?: return@let null, subcat.iconResId, 0)
            }

            if (resId != 0) {
                return IconResource.TintedResIcon(
                    resId = resId,
                    tint = Color(android.graphics.Color.parseColor(subcat.color))
                )
            }
        }

        // subcategory string fallback
        subcategory?.let { subcatName ->
            CategoryMapping.categories[subcatName]?.let { categoryInfo ->
                return IconResource.TintedResIcon(
                    resId = categoryInfo.iconResId,
                    tint = categoryInfo.color
                )
            }
        }

        // category icon from entity
        categoryEntity?.let { cat ->
            val resId = if (!cat.iconName.isNullOrEmpty()) {
                IconResolutionUtils.nameToResId(context, cat.iconName)
            } else {
                IconResolutionUtils.getSafeResId(context ?: return@let null, cat.iconResId, 0)
            }

            if (resId != 0) {
                return IconResource.TintedResIcon(
                    resId = resId,
                    tint = Color(android.graphics.Color.parseColor(cat.color))
                )
            }
        }

        // category string fallback
        category?.let { catName ->
            CategoryMapping.categories[catName]?.let { categoryInfo ->
                return IconResource.TintedResIcon(
                    resId = categoryInfo.iconResId,
                    tint = categoryInfo.color
                )
            }
        }

        // CategoryMapping using the transaction's category name (if available)
        // This handles built-in categories that don't have iconResId set in the entity
        categoryEntity?.let { cat ->
            CategoryMapping.categories[cat.name]?.let { categoryInfo ->
                return IconResource.TintedResIcon(
                    resId = categoryInfo.iconResId,
                    tint = categoryInfo.color
                )
            }
        }

        // account icon - Fallback
        if (!accountIconName.isNullOrEmpty()) {
            val resId = IconResolutionUtils.nameToResId(context, accountIconName)
            if (resId != 0) {
                return if (accountIconName.startsWith("ic_brand") || accountIconName.startsWith("ic_launcher") || accountIconName.startsWith("merchant_")) {
                    IconResource.DrawableResource(resId)
                } else {
                    IconResource.TintedResIcon(
                        resId = resId,
                        tint = Color.Unspecified
                    )
                }
            }
        }
        if (accountIconResId != 0) {
            val safeResId = IconResolutionUtils.getSafeResId(context ?: return@getIconForTransaction IconResource.TintedResIcon(
                resId = CategoryMapping.categories["Miscellaneous"]!!.iconResId,
                tint = CategoryMapping.categories["Miscellaneous"]!!.color
            ), accountIconResId, 0)
            
            if (safeResId != 0) {
                return IconResource.TintedResIcon(
                    resId = safeResId,
                    tint = Color.Unspecified // Use original colors of the resource
                )
            }
        }


        // Final fallback: Use Miscellaneous icon
        val categoryInfo = CategoryMapping.categories["Miscellaneous"]!!
        return IconResource.TintedResIcon(
            resId = categoryInfo.iconResId,
            tint = categoryInfo.color
        )
    }

    /**
     * Get color with full fallback chain for transactions
     * Priority: Brand Color → Subcategory Color → Category Color → Account Color
     * Returns: Pair of (colorHex, alpha) where brand colors have full opacity (1.0f)
     * and other colors have reduced opacity (0.2f)
     */
    fun getColorForTransaction(
        merchantName: String,
        categoryEntity: CategoryEntity? = null,
        subcategoryEntity: SubcategoryEntity? = null,
        category: String? = null,
        subcategory: String? = null,
        accountColorHex: String? = null
    ): Pair<String, Float>? {
        // brand color first - full opacity
        BrandIcons.getBrandColor(merchantName)?.let { return Pair(it, 1.0f) }

        // subcategory color - reduced opacity
        subcategoryEntity?.color?.let { return Pair(it, 0.2f) }

        // subcategory string fallback
        subcategory?.let { subcatName ->
            CategoryMapping.categories[subcatName]?.let { categoryInfo ->
                return Pair(String.format("#%06X", (0xFFFFFF and categoryInfo.color.toArgb())), 0.2f)
            }
        }

        // category color - reduced opacity
        categoryEntity?.color?.let { return Pair(it, 0.2f) }

        // category string fallback
        category?.let { catName ->
            CategoryMapping.categories[catName]?.let { categoryInfo ->
                return Pair(String.format("#%06X", (0xFFFFFF and categoryInfo.color.toArgb())), 0.2f)
            }
        }

        // account color fallback - reduced opacity
        accountColorHex?.let { return Pair(it, 0.2f) }

        // Return null to use default
        return null
    }

    /**
     * Get category info including icon and color
     */
    fun getCategoryInfo(merchantName: String): CategoryMapping.CategoryInfo {
        val category = CategoryMapping.getCategory(merchantName)
        return CategoryMapping.categories[category]
            ?: CategoryMapping.categories["Miscellaneous"]!!
    }
}

/**
 * Sealed class for different icon types
 */
sealed class IconResource {
    data class DrawableResource(val resId: Int) : IconResource()
    data class VectorIcon(val icon: ImageVector, val tint: Color) : IconResource()
    data class TintedResIcon(val resId: Int, val tint: Color) : IconResource()
}