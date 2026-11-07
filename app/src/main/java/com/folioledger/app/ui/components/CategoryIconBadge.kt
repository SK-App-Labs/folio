// File: app/src/main/java/com/folioledger/app/ui/components/CategoryIconBadge.kt
package com.folioledger.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.folioledger.app.domain.model.CategoryType
import com.folioledger.app.ui.iconpack.EmojiIconResolver
import com.folioledger.app.ui.iconpack.FolioIconPackTheme
import com.folioledger.app.ui.iconpack.FolioIconResolver
import com.folioledger.app.ui.iconpack.LocalFolioIconPackTheme
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

val ALL_CATEGORY_ICON_KEYS: List<String> = listOf(
    // Food & Dining (8)
    "restaurant",
    "fastfood",
    "local_cafe",
    "local_bar",
    "bakery_dining",
    "lunch_dining",
    "icecream",
    "kitchen",
    // Groceries & Home (13)
    "shopping_cart",
    "shopping_bag",
    "storefront",
    "local_mall",
    "cleaning_services",
    "chair",
    "weekend",
    "yard",
    "plumbing",
    "handyman",
    "build",
    "home",
    "apartment",
    // Utilities & Bills (9)
    "bolt",
    "water_drop",
    "local_fire_department",
    "wifi",
    "phone_android",
    "router",
    "tv",
    "ac_unit",
    "security",
    // Transport & Travel (13)
    "directions_car",
    "two_wheeler",
    "local_gas_station",
    "ev_station",
    "directions_bus",
    "train",
    "subway",
    "local_taxi",
    "flight",
    "hotel",
    "luggage",
    "beach_access",
    "commute",
    // Health & Care (7)
    "local_hospital",
    "medical_services",
    "medication",
    "fitness_center",
    "self_improvement",
    "spa",
    "content_cut",
    // Lifestyle & Family (16)
    "movie",
    "theaters",
    "music_note",
    "headphones",
    "sports_esports",
    "sports_soccer",
    "celebration",
    "cake",
    "school",
    "menu_book",
    "child_care",
    "toys",
    "pets",
    "checkroom",
    "watch",
    "diamond",
    // Finance & Work (20)
    "payments",
    "account_balance",
    "account_balance_wallet",
    "credit_card",
    "savings",
    "trending_up",
    "show_chart",
    "work",
    "business_center",
    "computer",
    "receipt_long",
    "request_quote",
    "gavel",
    "volunteer_activism",
    "card_giftcard",
    "redeem",
    "currency_rupee",
    "attach_money",
    "percent",
    "category",
)

fun suggestIconAndColorForCategoryName(
    name: String,
    type: CategoryType = CategoryType.EXPENSE,
): Pair<String, String>? {
    val normalized = name.trim().lowercase(Locale.US)
    if (normalized.isEmpty()) return null

    fun matchesAny(vararg keywords: String): Boolean =
        keywords.any { kw -> normalized.contains(kw) }

    return when {
        // Food & Beverages
        matchesAny("coffee", "tea", "chai", "cafe", "starbucks", "filter coffee", "beverage") ->
            "local_cafe" to "#D97706"
        matchesAny("bakery", "cake", "pastry", "sweet", "mithai") ->
            "bakery_dining" to "#EC4899"
        matchesAny("icecream", "ice cream", "gelato", "dessert") ->
            "icecream" to "#EC4899"
        matchesAny("bar", "pub", "alcohol", "beer", "wine", "liquor", "drinks") ->
            "local_bar" to "#7C3AED"
        matchesAny("fastfood", "fast food", "burger", "pizza", "domino", "kfc", "mcdonald", "snack", "roll", "wrap", "faasos") ->
            "fastfood" to "#F97316"
        matchesAny("swiggy", "zomato", "food", "dining", "restaurant", "lunch", "dinner", "breakfast", "tiffin", "idli", "dosa", "bhavan", "biryani", "meal") ->
            "restaurant" to "#F97316"

        // Groceries & Shopping
        matchesAny("grocery", "groceries", "kirana", "mart", "instamart", "blinkit", "zepto", "bigbasket", "supermarket", "provision", "vegetable", "fruit", "milk", "dairy") ->
            "shopping_cart" to "#10B981"
        matchesAny("mall", "department store", "hypermarket") ->
            "local_mall" to "#EC4899"
        matchesAny("shopping", "amazon", "flipkart", "meesho", "online order", "store") ->
            "shopping_bag" to "#EC4899"

        // Transport & Travel
        matchesAny("fuel", "petrol", "diesel", "shell", "hpcl", "bpcl", "iocl", "cng", "gas station") ->
            "local_gas_station" to "#E11D48"
        matchesAny("ev ", "evcharging", "charging", "ather", "electric vehicle") ->
            "ev_station" to "#0D9488"
        matchesAny("uber", "ola", "rapido", "cab", "taxi", "auto", "rickshaw") ->
            "local_taxi" to "#D97706"
        matchesAny("bike", "scooter", "two wheeler", "motorcycle", "raider", "activa", "helmet") ->
            "two_wheeler" to "#0284C7"
        matchesAny("car", "vehicle", "parking", "fastag", "toll", "car wash") ->
            "directions_car" to "#2563EB"
        matchesAny("metro", "subway", "cmrl", "dmrc", "bmrc") ->
            "subway" to "#0D9488"
        matchesAny("train", "irctc", "railway", "rail", "loco") ->
            "train" to "#2563EB"
        matchesAny("bus", "redbus", "mtc", "ksrtc", "transit", "commute", "pass") ->
            "directions_bus" to "#0284C7"
        matchesAny("flight", "airline", "indigo", "air india", "vistara", "spicejet", "airfare", "airport") ->
            "flight" to "#4F46E5"
        matchesAny("hotel", "resort", "airbnb", "oyo", "stay", "lodging") ->
            "hotel" to "#0D9488"
        matchesAny("beach", "holiday", "vacation", "tour", "sightseeing") ->
            "beach_access" to "#0284C7"
        matchesAny("travel", "trip", "luggage", "baggage", "visa", "passport") ->
            "luggage" to "#4F46E5"

        // Housing, Home & Maintenance
        matchesAny("rent", "house", "home", "housing", "society", "maintenance", "mortgage") ->
            "home" to "#2563EB"
        matchesAny("apartment", "flat", "pg", "hostel") ->
            "apartment" to "#2563EB"
        matchesAny("maid", "cleaning", "laundry", "dry clean", "housekeeping", "cook") ->
            "cleaning_services" to "#0D9488"
        matchesAny("plumb", "pipe", "leak", "RO ", "water purifier") ->
            "plumbing" to "#0284C7"
        matchesAny("repair", "handyman", "carpenter", "urban company", "service", "hardware", "drill") ->
            "handyman" to "#EA580C"
        matchesAny("furniture", "sofa", "chair", "table", "mattress", "ikea", "decor") ->
            "chair" to "#D97706"
        matchesAny("garden", "plant", "lawn", "nursery", "yard") ->
            "yard" to "#10B981"

        // Utilities & Bills
        matchesAny("electricity", "eb ", "eb bill", "power", "tangedco", "bescom", "current", "inverter") ->
            "bolt" to "#D97706"
        matchesAny("water", "can", "metro water", "sewage") ->
            "water_drop" to "#0284C7"
        matchesAny("gas", "lpg", "cylinder", "indane", "bharatgas", "hp gas", "piped gas") ->
            "local_fire_department" to "#EA580C"
        matchesAny("wifi", "broadband", "internet", "fiber", "act fibernet", "jiofiber", "airtel xstream") ->
            "wifi" to "#2563EB"
        matchesAny("router", "modem", "data plan") ->
            "router" to "#0284C7"
        matchesAny("mobile", "phone", "recharge", "postpaid", "prepaid", "jio", "airtel", "vi ", "bsnl", "sim") ->
            "phone_android" to "#4F46E5"
        matchesAny("tv", "dth", "tata play", "cable", "dish") ->
            "tv" to "#7C3AED"
        matchesAny("ac ", "air condition", "cooling", "hvac") ->
            "ac_unit" to "#0284C7"

        // Health, Wellness & Personal Care
        matchesAny("hospital", "doctor", "clinic", "consultation", "surgery", "dental", "dentist", "eye", "lab test", "scan") ->
            "local_hospital" to "#E11D48"
        matchesAny("medicine", "pharmacy", "medical", "apollo", "medplus", "netmeds", "1mg", "tablet", "first aid") ->
            "medication" to "#059669"
        matchesAny("gym", "fitness", "workout", "cult", "protein", "supplement") ->
            "fitness_center" to "#0D9488"
        matchesAny("yoga", "meditation", "mindfulness", "therapy", "wellness") ->
            "self_improvement" to "#10B981"
        matchesAny("salon", "haircut", "barber", "grooming", "parlour", "shave") ->
            "content_cut" to "#EC4899"
        matchesAny("spa", "massage", "skincare", "cosmetic", "beauty", "nykaa") ->
            "spa" to "#EC4899"

        // Lifestyle, Entertainment, Family & Education
        matchesAny("movie", "cinema", "pvr", "inox", "imax", "bookmyshow", "netflix", "prime video", "hotstar", "youtube", "ott", "streaming") ->
            "movie" to "#7C3AED"
        matchesAny("music", "spotify", "apple music", "concert", "audio", "podcast", "earbuds", "headphone") ->
            "headphones" to "#4F46E5"
        matchesAny("game", "gaming", "steam", "playstation", "xbox", "nintendo", "esports") ->
            "sports_esports" to "#4F46E5"
        matchesAny("sport", "cricket", "football", "badminton", "turf", "tennis", "swimming") ->
            "sports_soccer" to "#059669"
        matchesAny("party", "celebration", "festival", "diwali", "pongal", "christmas", "wedding", "event") ->
            "celebration" to "#D97706"
        matchesAny("birthday", "anniversary") ->
            "cake" to "#EC4899"
        matchesAny("school", "college", "tuition", "university", "coaching", "course", "udemy", "coursera", "exam", "fee") ->
            "school" to "#2563EB"
        matchesAny("book", "kindle", "audible", "stationery", "notebook", "library", "newspaper", "magazine") ->
            "menu_book" to "#0D9488"
        matchesAny("baby", "kid", "child", "diaper", "pediatric", "daycare") ->
            "child_care" to "#EC4899"
        matchesAny("toy", "lego", "board game") ->
            "toys" to "#F97316"
        matchesAny("pet", "dog", "cat", "vet", "pet food") ->
            "pets" to "#D97706"
        matchesAny("clothes", "clothing", "apparel", "fashion", "myntra", "ajio", "zudio", "westside", "shirt", "jeans", "saree", "dress", "shoe", "footwear") ->
            "checkroom" to "#7C3AED"
        matchesAny("watch", "accessory", "sunglass", "bag", "backpack", "zouk", "luggage") ->
            "watch" to "#334155"
        matchesAny("gold", "silver", "jewel", "grt", "tanishq", "kalyan", "malabar", "diamond", "ornament") ->
            "diamond" to "#D97706"

        // Finance, Investments, Taxes & Work
        matchesAny("sip", "mutual fund", "dsp", "elss", "invest", "stock", "equity", "zerodha", "groww", "upstox", "nifty", "etf", "gold bond") ->
            "trending_up" to "#059669"
        matchesAny("trading", "intraday", "options", "futures", "crypto", "market") ->
            "show_chart" to "#2563EB"
        matchesAny("fd", "fixed deposit", "rd", "ppf", "epf", "nps", "saving", "emergency fund") ->
            "savings" to "#10B981"
        matchesAny("insurance", "lic", "term plan", "health policy", "vehicle insurance", "premium") ->
            "security" to "#0D9488"
        matchesAny("tax", "income tax", "gst", "tds", "property tax", "corporation tax", "legal", "ca ", "challan", "fine") ->
            "gavel" to "#334155"
        matchesAny("loan", "emi", "interest paid", "debt", "credit card bill", "settlement") ->
            "receipt_long" to "#E11D48"
        matchesAny("salary", "paycheck", "wage", "stipend", "payroll") ->
            "payments" to "#059669"
        matchesAny("freelance", "consulting", "client", "contract", "side hustle") ->
            "business_center" to "#2563EB"
        matchesAny("royalty", "kdp", "publishing", "software", "tech", "laptop", "monitor", "computer", "cloud", "google play") ->
            "computer" to "#4F46E5"
        matchesAny("business", "office", "work", "coworking", "tally") ->
            "work" to "#2563EB"
        matchesAny("dividend", "interest", "coupon", "yield") ->
            "percent" to "#10B981"
        matchesAny("gift", "bonus", "shagun", "present") ->
            "card_giftcard" to "#D97706"
        matchesAny("cashback", "reward", "voucher", "coupon", "refund", "reimbursement") ->
            "redeem" to "#10B981"
        matchesAny("charity", "donation", "temple", "church", "mosque", "gurudwara", "ngo", "help", "seva") ->
            "volunteer_activism" to "#10B981"
        else -> {
            if (type == CategoryType.INCOME) {
                "payments" to "#059669"
            } else {
                null
            }
        }
    }
}

private val hexColorCache = ConcurrentHashMap<String, Color>()
private val folioIconCache = ConcurrentHashMap<String, ImageVector>()

fun parseHexColorSafely(colorHex: String?, fallback: Color = Color(0xFF059669)): Color {
    if (colorHex.isNullOrBlank()) return fallback
    return hexColorCache.getOrPut(colorHex) {
        val trimmed = colorHex.trim()
        val normalized = if (trimmed.startsWith("#")) trimmed else "#$trimmed"
        try {
            Color(normalized.toColorInt())
        } catch (_: IllegalArgumentException) {
            fallback
        }
    }
}

fun resolveFolioIcon(
    iconName: String?,
    packTheme: FolioIconPackTheme = FolioIconPackTheme.MATERIAL_FILLED
): ImageVector {
    val normalizedKey = iconName?.trim()?.lowercase(Locale.US).takeUnless { it.isNullOrBlank() } ?: "category"
    val cacheKey = "${packTheme.key}_$normalizedKey"
    return folioIconCache.getOrPut(cacheKey) {
        FolioIconResolver.resolve(normalizedKey, packTheme)
    }
}

@Composable
fun CategoryIconBadge(
    iconName: String?,
    colorHex: String?,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    iconSize: Dp = 20.dp
) {
    val primaryFallback = MaterialTheme.colorScheme.primary
    val baseColor = remember(colorHex, primaryFallback) {
        parseHexColorSafely(colorHex, fallback = primaryFallback)
    }
    val iconPack = LocalFolioIconPackTheme.current
    val isDirectEmoji = remember(iconName) {
        EmojiIconResolver.isDirectEmoji(iconName)
    }
    val renderAsEmoji = iconPack.isEmoji || isDirectEmoji

    val shape = remember(iconPack.isSquircle || isDirectEmoji, size) {
        if (iconPack.isSquircle || isDirectEmoji) {
            RoundedCornerShape(size * 0.28f)
        } else {
            CircleShape
        }
    }

    if (renderAsEmoji) {
        val emoji = remember(iconName) {
            EmojiIconResolver.resolve(iconName)
        }
        val emojiFontSize = (iconSize.value * 1.05f).sp

        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .size(size)
                .clip(shape)
                .background(baseColor.copy(alpha = 0.18f))
                .border(BorderStroke(1.dp, baseColor.copy(alpha = 0.25f)), shape)
        ) {
            Text(
                text = emoji,
                fontSize = emojiFontSize,
                textAlign = TextAlign.Center
            )
        }
    } else {
        val iconVector = remember(iconName, iconPack) {
            resolveFolioIcon(iconName, iconPack)
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .size(size)
                .clip(shape)
                .background(baseColor.copy(alpha = if (iconPack.isOutlined) 0.12f else 0.16f))
                .then(
                    if (iconPack.isOutlined) {
                        Modifier.border(BorderStroke(1.dp, baseColor.copy(alpha = 0.35f)), shape)
                    } else {
                        Modifier
                    }
                )
        ) {
            Icon(
                imageVector = iconVector,
                contentDescription = iconName ?: "Category",
                tint = baseColor,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}
