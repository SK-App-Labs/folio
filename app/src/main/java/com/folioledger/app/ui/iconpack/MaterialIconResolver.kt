// File: app/src/main/java/com/folioledger/app/ui/iconpack/MaterialIconResolver.kt
package com.folioledger.app.ui.iconpack

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
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
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.SportsSoccer
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
import androidx.compose.ui.graphics.vector.ImageVector

internal object MaterialIconResolver {

    fun resolve(key: String): ImageVector {
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
}
