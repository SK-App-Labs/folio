// File: app/src/main/java/com/folioledger/app/ui/iconpack/EmojiIconResolver.kt
package com.folioledger.app.ui.iconpack

import java.util.Locale

object EmojiIconResolver {

    /**
     * Checks if the given string represents a direct unicode emoji.
     */
    fun isDirectEmoji(text: String?): Boolean {
        if (text.isNullOrBlank()) return false
        val trimmed = text.trim()
        return trimmed.any { ch ->
            ch.code > 127 && (
                Character.isSurrogate(ch) ||
                ch.code in 0x2000..0x3300 ||
                ch.code in 0xFE00..0xFE0F
            )
        }
    }

    /**
     * Resolves a semantic Folio category key (or pass-through direct emoji) into a vibrant, full-color emoji.
     */
    fun resolve(iconName: String?): String {
        if (iconName.isNullOrBlank()) return "🏷️"
        val trimmed = iconName.trim()
        if (isDirectEmoji(trimmed)) return trimmed

        val key = trimmed.lowercase(Locale.US)
        return when (key) {
            // Food & Dining
            "restaurant", "food", "dining", "utensils" -> "🍔"
            "fastfood", "snacks", "street_food", "burger" -> "🍕"
            "local_cafe", "cafe", "coffee", "tea" -> "☕"
            "local_bar", "bar", "drinks", "pub", "wine" -> "🍷"
            "bakery_dining", "bakery", "bread", "pastry" -> "🥐"
            "lunch_dining", "lunch", "meal", "tiffin" -> "🍱"
            "icecream", "ice_cream", "dessert", "sweet" -> "🍦"
            "kitchen", "cooking", "appliances" -> "🍳"

            // Groceries & Home
            "shopping_cart", "groceries", "shopping", "cart", "supermarket" -> "🛒"
            "shopping_bag", "bag", "retail", "order" -> "🛍️"
            "storefront", "store", "kirana", "shop" -> "🏪"
            "local_mall", "mall", "plaza" -> "🏬"
            "cleaning_services", "cleaning", "maid", "laundry", "housekeeping" -> "🧹"
            "chair", "furniture", "decor" -> "🪑"
            "weekend", "sofa", "lounge", "living_room" -> "🛋️"
            "yard", "garden", "plants", "lawn" -> "🪴"
            "plumbing", "plumber", "pipes", "water_purifier" -> "🔧"
            "handyman", "carpenter", "tools", "hardware" -> "🔨"
            "build", "maintenance", "repair", "service" -> "🛠️"
            "home", "rent", "housing", "mortgage", "house" -> "🏠"
            "apartment", "flat", "society", "condo" -> "🏢"

            // Utilities & Bills
            "bolt", "electricity", "power", "utilities" -> "⚡"
            "water_drop", "water" -> "💧"
            "local_fire_department", "gas", "lpg", "cylinder", "heating" -> "🔥"
            "wifi", "internet", "broadband" -> "📶"
            "phone_android", "mobile", "recharge", "telecom" -> "📱"
            "router", "modem", "fiber" -> "📡"
            "tv", "television", "dth", "cable" -> "📺"
            "ac_unit", "ac", "cooling", "air_conditioner" -> "❄️"
            "security", "insurance", "protection" -> "🛡️"

            // Transport & Travel
            "directions_car", "car", "vehicle", "transport" -> "🚗"
            "two_wheeler", "bike", "scooter", "motorcycle" -> "🛵"
            "local_gas_station", "fuel", "petrol", "diesel" -> "⛽"
            "ev_station", "ev", "charging" -> "🔌"
            "directions_bus", "bus", "public_transit" -> "🚌"
            "train", "railway", "irctc", "rail" -> "🚆"
            "subway", "metro", "local_train" -> "🚇"
            "local_taxi", "cab", "taxi", "uber", "ola", "auto" -> "🚕"
            "flight", "travel", "airfare", "airplane" -> "✈️"
            "hotel", "lodging", "resort", "stay" -> "🏨"
            "luggage", "baggage", "trip", "suitcase" -> "🧳"
            "beach_access", "vacation", "holiday", "beach" -> "🏖️"
            "commute", "daily_travel", "pass" -> "🎫"

            // Health & Care
            "local_hospital", "health", "hospital", "doctor" -> "🏥"
            "medical_services", "clinic", "checkup", "dental", "first_aid" -> "🩺"
            "medication", "medicine", "pharmacy", "pills" -> "💊"
            "fitness_center", "gym", "fitness" -> "🏋️"
            "self_improvement", "yoga", "meditation", "wellness" -> "🧘"
            "spa", "personal_care", "massage", "skincare", "beauty" -> "💆"
            "content_cut", "salon", "haircut", "barber", "grooming" -> "💇"

            // Lifestyle & Family
            "movie", "entertainment", "streaming", "cinema" -> "🎬"
            "theaters", "show", "drama", "imax" -> "🎭"
            "music_note", "music", "spotify", "concert" -> "🎧"
            "headphones", "audio", "podcast", "earbuds" -> "🎙️"
            "sports_esports", "gaming", "game", "console" -> "🎮"
            "sports_soccer", "sports", "cricket", "football", "turf" -> "⚽"
            "celebration", "party", "festival", "event" -> "🎉"
            "cake", "birthday", "anniversary" -> "🎂"
            "school", "education", "courses", "tuition", "college" -> "🎓"
            "menu_book", "books", "reading", "kindle", "stationery" -> "📚"
            "child_care", "baby", "kids", "family" -> "👶"
            "toys", "play", "games" -> "🧸"
            "pets", "pet", "vet", "dog", "cat" -> "🐾"
            "checkroom", "clothing", "apparel", "fashion" -> "👕"
            "watch", "accessories", "wrist_watch" -> "⌚"
            "diamond", "jewellery", "gold", "luxury", "gems" -> "💎"

            // Finance & Work
            "payments", "salary", "paycheck", "cashflow" -> "💰"
            "account_balance", "bank", "bank_account" -> "🏦"
            "account_balance_wallet", "wallet", "cash", "prepaid_wallet" -> "👛"
            "credit_card", "card", "debit_card", "emi" -> "💳"
            "savings", "piggy_bank", "deposit", "emergency_fund", "fd" -> "🐷"
            "trending_up", "investment", "stocks", "mutual_fund", "sip" -> "📈"
            "show_chart", "trading", "market", "analytics" -> "📊"
            "trending_down", "loss", "loan" -> "📉"
            "work", "business", "office" -> "💼"
            "business_center", "freelance", "consulting", "briefcase" -> "📁"
            "computer", "tech", "software", "laptop", "royalty" -> "💻"
            "receipt_long", "bills", "invoice", "subscription" -> "🧾"
            "request_quote", "quote", "estimate", "reimbursement" -> "📜"
            "gavel", "tax", "legal", "fine", "audit" -> "⚖️"
            "volunteer_activism", "charity", "donation", "ngo" -> "🤝"
            "card_giftcard", "gift", "bonus" -> "🎁"
            "redeem", "cashback", "rewards", "voucher" -> "🎟️"
            "currency_rupee", "inr", "rupee" -> "₹"
            "attach_money", "usd", "dollar", "money" -> "💵"
            "percent", "interest", "dividend", "discount" -> "🏷️"
            "workspace_premium", "patron", "premium" -> "👑"
            "swap_horiz", "transfer", "cc_settlement" -> "🔄"
            "compare_arrows", "exchange" -> "🔁"
            "autorenew", "recurring", "refund" -> "🔁"
            "more_horiz", "other", "miscellaneous" -> "📦"
            else -> "🏷️"
        }
    }
}
