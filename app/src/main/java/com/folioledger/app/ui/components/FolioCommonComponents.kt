// File: app/src/main/java/com/folioledger/app/ui/components/FolioCommonComponents.kt
package com.folioledger.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.BakeryDining
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Commute
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EvStation
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Icecream
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.LocalTaxi
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Plumbing
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Subway
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Theaters
import androidx.compose.material.icons.filled.Toys
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.filled.Yard
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.graphics.toColorInt
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.folioledger.app.domain.model.CategoryType
import com.folioledger.app.domain.model.CurrencySpec
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.NetWorthOverview
import com.folioledger.app.domain.model.NumberGroupingSystem
import com.folioledger.app.domain.model.PeriodLedgerSummary
import com.folioledger.app.domain.model.SupportedCurrencies
import com.folioledger.app.domain.model.TransactionType
import com.folioledger.app.domain.util.MoneyFormatterAndCalculator
import com.folioledger.app.ui.theme.LocalFolioSemanticColors
import com.folioledger.app.ui.theme.tabularMoneyStyle
import java.util.Locale

enum class MoneyColorMode {
    AUTO_SIGN,
    INCOME,
    EXPENSE,
    TRANSFER,
    NEUTRAL;

    companion object
}

fun MoneyColorMode.Companion.fromTransactionType(type: TransactionType): MoneyColorMode =
    when (type) {
        TransactionType.INCOME, TransactionType.REFUND -> MoneyColorMode.INCOME
        TransactionType.EXPENSE -> MoneyColorMode.EXPENSE
        TransactionType.TRANSFER, TransactionType.CC_SETTLEMENT -> MoneyColorMode.TRANSFER
    }

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

@Composable
fun MoneyText(
    money: MinorMoney,
    modifier: Modifier = Modifier,
    currencySpec: CurrencySpec = SupportedCurrencies.INR,
    grouping: NumberGroupingSystem = currencySpec.defaultGrouping,
    colorMode: MoneyColorMode = MoneyColorMode.AUTO_SIGN,
    colorOverride: Color? = null,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    includeSymbol: Boolean = true,
    showPlusSignForPositive: Boolean = false,
    maxLines: Int = 1,
) {
    val semanticColors = LocalFolioSemanticColors.current
    val formattedText = remember(
        money,
        currencySpec,
        grouping,
        includeSymbol,
        showPlusSignForPositive,
    ) {
        MoneyFormatterAndCalculator.formatMinorMoney(
            money = money,
            currencySpec = currencySpec,
            grouping = grouping,
            includeSymbol = includeSymbol,
            showPlusSignForPositive = showPlusSignForPositive,
        )
    }

    val resolvedColor = colorOverride ?: when (colorMode) {
        MoneyColorMode.AUTO_SIGN -> when {
            money.isPositive -> semanticColors.incomeEmerald
            money.isNegative -> semanticColors.expenseCoral
            else -> LocalContentColor.current
        }
        MoneyColorMode.INCOME -> semanticColors.incomeEmerald
        MoneyColorMode.EXPENSE -> semanticColors.expenseCoral
        MoneyColorMode.TRANSFER -> semanticColors.transferBlue
        MoneyColorMode.NEUTRAL -> LocalContentColor.current
    }

    val tabularStyle = tabularMoneyStyle(style).let { base ->
        if (fontWeight != null) base.copy(fontWeight = fontWeight) else base
    }

    Text(
        text = formattedText,
        color = resolvedColor,
        style = tabularStyle,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )
}

@Composable
fun PeriodSelectorHeader(
    periodTitle: String,
    onPreviousPeriod: () -> Unit,
    onNextPeriod: () -> Unit,
    modifier: Modifier = Modifier,
    periodSubtitle: String? = null,
    onPeriodTitleClick: (() -> Unit)? = null,
    onSearchClick: (() -> Unit)? = null,
    onFilterClick: (() -> Unit)? = null,
    activeFilterCount: Int = 0,
    onBookmarksClick: (() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                IconButton(
                    onClick = onPreviousPeriod,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous Period",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .then(
                            if (onPeriodTitleClick != null) {
                                Modifier.clickable(onClick = onPeriodTitleClick)
                            } else {
                                Modifier
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = periodTitle,
                            style = tabularMoneyStyle(MaterialTheme.typography.titleMedium),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!periodSubtitle.isNullOrBlank()) {
                            Text(
                                text = periodSubtitle,
                                style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onNextPeriod,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next Period",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                trailingContent?.invoke()

                onBookmarksClick?.let { onClick ->
                    IconButton(
                        onClick = onClick,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Favorite Bookmarks",
                            tint = LocalFolioSemanticColors.current.warningAmber
                        )
                    }
                }

                onFilterClick?.let { onClick ->
                    IconButton(
                        onClick = onClick,
                        modifier = Modifier.size(38.dp)
                    ) {
                        BadgedBox(
                            badge = {
                                if (activeFilterCount > 0) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ) {
                                        Text(
                                            text = activeFilterCount.toString(),
                                            style = tabularMoneyStyle(MaterialTheme.typography.labelSmall)
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.FilterList,
                                contentDescription = "Filter Transactions",
                                tint = if (activeFilterCount > 0) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }

                onSearchClick?.let { onClick ->
                    IconButton(
                        onClick = onClick,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search Ledger",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ThreeColumnSummaryBanner(
    firstLabel: String,
    firstAmount: MinorMoney,
    secondLabel: String,
    secondAmount: MinorMoney,
    thirdLabel: String,
    thirdAmount: MinorMoney,
    modifier: Modifier = Modifier,
    currencySpec: CurrencySpec = SupportedCurrencies.INR,
    grouping: NumberGroupingSystem = currencySpec.defaultGrouping,
    firstColorMode: MoneyColorMode = MoneyColorMode.INCOME,
    secondColorMode: MoneyColorMode = MoneyColorMode.EXPENSE,
    thirdColorMode: MoneyColorMode = MoneyColorMode.AUTO_SIGN,
    carryOverAmount: MinorMoney? = null,
    onColumnClick: ((columnIndex: Int) -> Unit)? = null,
) {
    val semanticColors = LocalFolioSemanticColors.current

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .padding(vertical = 12.dp)
            ) {
                SummaryColumnCell(
                    label = firstLabel,
                    amount = firstAmount,
                    currencySpec = currencySpec,
                    grouping = grouping,
                    colorMode = firstColorMode,
                    onClick = if (onColumnClick != null) ({ onColumnClick(0) }) else null,
                    modifier = Modifier.weight(1f)
                )

                VerticalDivider(
                    color = semanticColors.cardStrokeSubtle,
                    thickness = 1.dp,
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(vertical = 4.dp)
                )

                SummaryColumnCell(
                    label = secondLabel,
                    amount = secondAmount,
                    currencySpec = currencySpec,
                    grouping = grouping,
                    colorMode = secondColorMode,
                    onClick = if (onColumnClick != null) ({ onColumnClick(1) }) else null,
                    modifier = Modifier.weight(1f)
                )

                VerticalDivider(
                    color = semanticColors.cardStrokeSubtle,
                    thickness = 1.dp,
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(vertical = 4.dp)
                )

                SummaryColumnCell(
                    label = thirdLabel,
                    amount = thirdAmount,
                    currencySpec = currencySpec,
                    grouping = grouping,
                    colorMode = thirdColorMode,
                    showPlusSignForPositive = false,
                    onClick = if (onColumnClick != null) ({ onColumnClick(2) }) else null,
                    modifier = Modifier.weight(1f)
                )
            }

            if ((carryOverAmount != null) && (!carryOverAmount.isZero)) {
                HorizontalDivider(
                    color = semanticColors.cardStrokeSubtle,
                    thickness = 1.dp
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Carry-over from previous period",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    MoneyText(
                        money = carryOverAmount,
                        currencySpec = currencySpec,
                        grouping = grouping,
                        colorMode = MoneyColorMode.AUTO_SIGN,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        showPlusSignForPositive = true
                    )
                }
            }
        }
    }
}

@Composable
fun ThreeColumnSummaryBanner(
    periodSummary: PeriodLedgerSummary,
    modifier: Modifier = Modifier,
    currencySpec: CurrencySpec = SupportedCurrencies.INR,
    grouping: NumberGroupingSystem = currencySpec.defaultGrouping,
    showCarryOver: Boolean = false,
    onColumnClick: ((columnIndex: Int) -> Unit)? = null,
) {
    ThreeColumnSummaryBanner(
        firstLabel = "Income",
        firstAmount = periodSummary.totalIncome,
        secondLabel = "Expenses",
        secondAmount = periodSummary.totalExpense,
        thirdLabel = "Total",
        thirdAmount = periodSummary.netCashflow,
        currencySpec = currencySpec,
        grouping = grouping,
        firstColorMode = MoneyColorMode.INCOME,
        secondColorMode = MoneyColorMode.EXPENSE,
        thirdColorMode = MoneyColorMode.AUTO_SIGN,
        carryOverAmount = if (showCarryOver) periodSummary.carryOverFromPreviousPeriod else null,
        onColumnClick = onColumnClick,
        modifier = modifier
    )
}

@Composable
fun ThreeColumnSummaryBanner(
    netWorthOverview: NetWorthOverview,
    modifier: Modifier = Modifier,
    currencySpec: CurrencySpec = SupportedCurrencies.INR,
    grouping: NumberGroupingSystem = currencySpec.defaultGrouping,
    onColumnClick: ((columnIndex: Int) -> Unit)? = null,
) {
    ThreeColumnSummaryBanner(
        firstLabel = "Assets",
        firstAmount = netWorthOverview.totalAssets,
        secondLabel = "Liabilities",
        secondAmount = netWorthOverview.totalLiabilities,
        thirdLabel = "Net Worth",
        thirdAmount = netWorthOverview.netWorth,
        currencySpec = currencySpec,
        grouping = grouping,
        firstColorMode = MoneyColorMode.INCOME,
        secondColorMode = MoneyColorMode.EXPENSE,
        thirdColorMode = MoneyColorMode.AUTO_SIGN,
        carryOverAmount = null,
        onColumnClick = onColumnClick,
        modifier = modifier
    )
}

@Composable
private fun SummaryColumnCell(
    label: String,
    amount: MinorMoney,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem,
    colorMode: MoneyColorMode,
    modifier: Modifier = Modifier,
    showPlusSignForPositive: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        MoneyText(
            money = amount,
            currencySpec = currencySpec,
            grouping = grouping,
            colorMode = colorMode,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            showPlusSignForPositive = showPlusSignForPositive
        )
    }
}

fun parseHexColorSafely(colorHex: String?, fallback: Color = Color(0xFF059669)): Color {
    if (colorHex.isNullOrBlank()) return fallback
    val normalized = if (colorHex.trim().startsWith("#")) {
        colorHex.trim()
    } else {
        "#${colorHex.trim()}"
    }
    return try {
        Color(normalized.toColorInt())
    } catch (_: IllegalArgumentException) {
        fallback
    }
}

fun resolveFolioIcon(iconName: String?): ImageVector {
    val key = iconName?.trim()?.lowercase(Locale.US) ?: return Icons.Filled.Category
    return when (key) {
        // Food & Dining
        "restaurant", "food", "dining", "utensils" -> Icons.Filled.Restaurant
        "fastfood", "snacks", "street_food", "burger" -> Icons.Filled.Fastfood
        "local_cafe", "cafe", "coffee", "tea" -> Icons.Filled.LocalCafe
        "local_bar", "bar", "drinks", "pub", "wine" -> Icons.Filled.LocalBar
        "bakery_dining", "bakery", "bread", "pastry" -> Icons.Filled.BakeryDining
        "lunch_dining", "lunch", "meal", "tiffin" -> Icons.Filled.LunchDining
        "icecream", "ice_cream", "dessert", "sweet" -> Icons.Filled.Icecream
        "kitchen", "cooking", "appliances" -> Icons.Filled.Kitchen

        // Groceries & Home
        "shopping_cart", "groceries", "shopping", "cart", "supermarket" -> Icons.Filled.ShoppingCart
        "shopping_bag", "bag", "retail", "order" -> Icons.Filled.ShoppingBag
        "storefront", "store", "kirana", "shop" -> Icons.Filled.Storefront
        "local_mall", "mall", "plaza" -> Icons.Filled.LocalMall
        "cleaning_services", "cleaning", "maid", "laundry", "housekeeping" -> Icons.Filled.CleaningServices
        "chair", "furniture", "decor" -> Icons.Filled.Chair
        "weekend", "sofa", "lounge", "living_room" -> Icons.Filled.Weekend
        "yard", "garden", "plants", "lawn" -> Icons.Filled.Yard
        "plumbing", "plumber", "pipes", "water_purifier" -> Icons.Filled.Plumbing
        "handyman", "carpenter", "tools", "hardware" -> Icons.Filled.Handyman
        "build", "maintenance", "repair", "service" -> Icons.Filled.Build
        "home", "rent", "housing", "mortgage", "house" -> Icons.Filled.Home
        "apartment", "flat", "society", "condo" -> Icons.Filled.Apartment

        // Utilities & Bills
        "bolt", "electricity", "power", "utilities" -> Icons.Filled.Bolt
        "water_drop", "water" -> Icons.Filled.WaterDrop
        "local_fire_department", "gas", "lpg", "cylinder", "heating" -> Icons.Filled.LocalFireDepartment
        "wifi", "internet", "broadband" -> Icons.Filled.Wifi
        "phone_android", "mobile", "recharge", "telecom" -> Icons.Filled.PhoneAndroid
        "router", "modem", "fiber" -> Icons.Filled.Router
        "tv", "television", "dth", "cable" -> Icons.Filled.Tv
        "ac_unit", "ac", "cooling", "air_conditioner" -> Icons.Filled.AcUnit
        "security", "insurance", "protection" -> Icons.Filled.Security

        // Transport & Travel
        "directions_car", "car", "vehicle", "transport" -> Icons.Filled.DirectionsCar
        "two_wheeler", "bike", "scooter", "motorcycle" -> Icons.Filled.TwoWheeler
        "local_gas_station", "fuel", "petrol", "diesel" -> Icons.Filled.LocalGasStation
        "ev_station", "ev", "charging" -> Icons.Filled.EvStation
        "directions_bus", "bus", "public_transit" -> Icons.Filled.DirectionsBus
        "train", "railway", "irctc", "rail" -> Icons.Filled.Train
        "subway", "metro", "local_train" -> Icons.Filled.Subway
        "local_taxi", "cab", "taxi", "uber", "ola", "auto" -> Icons.Filled.LocalTaxi
        "flight", "travel", "airfare", "airplane" -> Icons.Filled.Flight
        "hotel", "lodging", "resort", "stay" -> Icons.Filled.Hotel
        "luggage", "baggage", "trip", "suitcase" -> Icons.Filled.Luggage
        "beach_access", "vacation", "holiday", "beach" -> Icons.Filled.BeachAccess
        "commute", "daily_travel", "pass" -> Icons.Filled.Commute

        // Health & Care
        "local_hospital", "health", "hospital", "doctor" -> Icons.Filled.LocalHospital
        "medical_services", "clinic", "checkup", "dental", "first_aid" -> Icons.Filled.MedicalServices
        "medication", "medicine", "pharmacy", "pills" -> Icons.Filled.Medication
        "fitness_center", "gym", "fitness" -> Icons.Filled.FitnessCenter
        "self_improvement", "yoga", "meditation", "wellness" -> Icons.Filled.SelfImprovement
        "spa", "personal_care", "massage", "skincare", "beauty" -> Icons.Filled.Spa
        "content_cut", "salon", "haircut", "barber", "grooming" -> Icons.Filled.ContentCut

        // Lifestyle & Family
        "movie", "entertainment", "streaming", "cinema" -> Icons.Filled.Movie
        "theaters", "show", "drama", "imax" -> Icons.Filled.Theaters
        "music_note", "music", "spotify", "concert" -> Icons.Filled.MusicNote
        "headphones", "audio", "podcast", "earbuds" -> Icons.Filled.Headphones
        "sports_esports", "gaming", "game", "console" -> Icons.Filled.SportsEsports
        "sports_soccer", "sports", "cricket", "football", "turf" -> Icons.Filled.SportsSoccer
        "celebration", "party", "festival", "event" -> Icons.Filled.Celebration
        "cake", "birthday", "anniversary" -> Icons.Filled.Cake
        "school", "education", "courses", "tuition", "college" -> Icons.Filled.School
        "menu_book", "books", "reading", "kindle", "stationery" -> Icons.AutoMirrored.Filled.MenuBook
        "child_care", "baby", "kids", "family" -> Icons.Filled.ChildCare
        "toys", "play", "games" -> Icons.Filled.Toys
        "pets", "pet", "vet", "dog", "cat" -> Icons.Filled.Pets
        "checkroom", "clothing", "apparel", "fashion" -> Icons.Filled.Checkroom
        "watch", "accessories", "wrist_watch" -> Icons.Filled.Watch
        "diamond", "jewellery", "gold", "luxury", "gems" -> Icons.Filled.Diamond

        // Finance & Work
        "payments", "salary", "paycheck", "cashflow" -> Icons.Filled.Payments
        "account_balance", "bank", "bank_account" -> Icons.Filled.AccountBalance
        "account_balance_wallet", "wallet", "cash", "prepaid_wallet" -> Icons.Filled.AccountBalanceWallet
        "credit_card", "card", "debit_card", "emi" -> Icons.Filled.CreditCard
        "savings", "piggy_bank", "deposit", "emergency_fund", "fd" -> Icons.Filled.Savings
        "trending_up", "investment", "stocks", "mutual_fund", "sip" -> Icons.AutoMirrored.Filled.TrendingUp
        "show_chart", "trading", "market", "analytics" -> Icons.AutoMirrored.Filled.ShowChart
        "trending_down", "loss", "loan" -> Icons.AutoMirrored.Filled.TrendingDown
        "work", "business", "office" -> Icons.Filled.Work
        "business_center", "freelance", "consulting", "briefcase" -> Icons.Filled.BusinessCenter
        "computer", "tech", "software", "laptop", "royalty" -> Icons.Filled.Computer
        "receipt_long", "bills", "invoice", "subscription" -> Icons.AutoMirrored.Filled.ReceiptLong
        "request_quote", "quote", "estimate", "reimbursement" -> Icons.Filled.RequestQuote
        "gavel", "tax", "legal", "fine", "audit" -> Icons.Filled.Gavel
        "volunteer_activism", "charity", "donation", "ngo" -> Icons.Filled.VolunteerActivism
        "card_giftcard", "gift", "bonus" -> Icons.Filled.CardGiftcard
        "redeem", "cashback", "rewards", "voucher" -> Icons.Filled.Redeem
        "currency_rupee", "inr", "rupee" -> Icons.Filled.CurrencyRupee
        "attach_money", "usd", "dollar", "money" -> Icons.Filled.AttachMoney
        "percent", "interest", "dividend", "discount" -> Icons.Filled.Percent
        "workspace_premium", "patron", "premium" -> Icons.Filled.WorkspacePremium
        "swap_horiz", "transfer", "cc_settlement" -> Icons.Filled.SwapHoriz
        "compare_arrows", "exchange" -> Icons.AutoMirrored.Filled.CompareArrows
        "autorenew", "recurring", "refund" -> Icons.Filled.Autorenew
        "more_horiz", "other", "miscellaneous" -> Icons.Filled.MoreHoriz
        else -> Icons.Filled.Category
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
    val iconVector = remember(iconName) {
        resolveFolioIcon(iconName)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(baseColor.copy(alpha = 0.16f))
    ) {
        Icon(
            imageVector = iconVector,
            contentDescription = iconName ?: "Category",
            tint = baseColor,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
fun EmptyStateCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.AutoMirrored.Filled.ReceiptLong,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    val semanticColors = LocalFolioSemanticColors.current

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f))
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            if ((!actionLabel.isNullOrBlank()) && (onActionClick != null)) {
                Spacer(modifier = Modifier.height(18.dp))
                Button(
                    onClick = onActionClick,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}