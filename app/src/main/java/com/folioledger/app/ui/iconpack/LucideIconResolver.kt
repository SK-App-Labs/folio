// File: app/src/main/java/com/folioledger/app/ui/iconpack/LucideIconResolver.kt
package com.folioledger.app.ui.iconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal object LucideIconResolver {

    private fun lucide(name: String, block: PathBuilder.() -> Unit): ImageVector {
        return ImageVector.Builder(
            name = "Lucide.$name",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            pathBuilder = block
        ).build()
    }

    private val utensils by lazy {
        lucide("Utensils") {
            moveTo(3f, 2f)
            verticalLineTo(9f)
            curveTo(3f, 10.1f, 3.9f, 11f, 5f, 11f)
            horizontalLineTo(9f)
            curveTo(10.1f, 11f, 11f, 10.1f, 11f, 9f)
            verticalLineTo(2f)
            moveTo(7f, 2f)
            verticalLineTo(22f)
            moveTo(21f, 15f)
            verticalLineTo(2f)
            curveTo(18.24f, 2f, 16f, 4.24f, 16f, 7f)
            verticalLineTo(13f)
            curveTo(16f, 14.1f, 16.9f, 15f, 18f, 15f)
            horizontalLineTo(21f)
            close()
            moveTo(21f, 15f)
            verticalLineTo(22f)
        }
    }

    private val coffee by lazy {
        lucide("Coffee") {
            moveTo(17f, 8f)
            horizontalLineTo(3f)
            verticalLineTo(17f)
            curveTo(3f, 19.2f, 4.8f, 21f, 7f, 21f)
            horizontalLineTo(13f)
            curveTo(15.2f, 21f, 17f, 19.2f, 17f, 17f)
            verticalLineTo(8f)
            close()
            moveTo(17f, 8f)
            horizontalLineTo(18f)
            curveTo(20.2f, 8f, 22f, 9.8f, 22f, 12f)
            curveTo(22f, 14.2f, 20.2f, 16f, 18f, 16f)
            horizontalLineTo(17f)
            moveTo(6f, 2f)
            verticalLineTo(4f)
            moveTo(10f, 2f)
            verticalLineTo(4f)
            moveTo(14f, 2f)
            verticalLineTo(4f)
        }
    }

    private val wine by lazy {
        lucide("Wine") {
            moveTo(8f, 22f)
            horizontalLineTo(16f)
            moveTo(12f, 11f)
            verticalLineTo(22f)
            moveTo(7f, 2f)
            horizontalLineTo(17f)
            verticalLineTo(7f)
            curveTo(17f, 9.76f, 14.76f, 12f, 12f, 12f)
            curveTo(9.24f, 12f, 7f, 9.76f, 7f, 7f)
            verticalLineTo(2f)
            close()
        }
    }

    private val shoppingCart by lazy {
        lucide("ShoppingCart") {
            moveTo(1f, 1f)
            horizontalLineTo(5f)
            lineTo(7.68f, 14.39f)
            curveTo(7.77f, 14.86f, 8.18f, 15.2f, 8.66f, 15.2f)
            horizontalLineTo(19.4f)
            curveTo(19.86f, 15.2f, 20.25f, 14.89f, 20.36f, 14.45f)
            lineTo(22f, 6f)
            horizontalLineTo(6f)
            moveTo(9f, 21f)
            horizontalLineTo(9.01f)
            moveTo(20f, 21f)
            horizontalLineTo(20.01f)
        }
    }

    private val shoppingBag by lazy {
        lucide("ShoppingBag") {
            moveTo(6f, 2f)
            lineTo(3f, 6f)
            verticalLineTo(20f)
            curveTo(3f, 21.1f, 3.9f, 22f, 5f, 22f)
            horizontalLineTo(19f)
            curveTo(20.1f, 22f, 21f, 21.1f, 21f, 20f)
            verticalLineTo(6f)
            lineTo(18f, 2f)
            horizontalLineTo(6f)
            close()
            moveTo(3f, 6f)
            horizontalLineTo(21f)
            moveTo(16f, 10f)
            curveTo(16f, 12.2f, 14.2f, 14f, 12f, 14f)
            curveTo(9.8f, 14f, 8f, 12.2f, 8f, 10f)
        }
    }

    private val home by lazy {
        lucide("Home") {
            moveTo(3f, 9f)
            lineTo(12f, 2f)
            lineTo(21f, 9f)
            verticalLineTo(20f)
            curveTo(21f, 21.1f, 20.1f, 22f, 19f, 22f)
            horizontalLineTo(5f)
            curveTo(3.9f, 22f, 3f, 21.1f, 3f, 20f)
            verticalLineTo(9f)
            close()
            moveTo(9f, 22f)
            verticalLineTo(12f)
            horizontalLineTo(15f)
            verticalLineTo(22f)
        }
    }

    private val bolt by lazy {
        lucide("Zap") {
            moveTo(13f, 2f)
            lineTo(3f, 14f)
            horizontalLineTo(12f)
            lineTo(11f, 22f)
            lineTo(21f, 10f)
            horizontalLineTo(12f)
            lineTo(13f, 2f)
            close()
        }
    }

    private val waterDrop by lazy {
        lucide("Droplet") {
            moveTo(12f, 2.69f)
            lineTo(17.66f, 8.35f)
            curveTo(20.78f, 11.47f, 20.78f, 16.53f, 17.66f, 19.66f)
            curveTo(14.54f, 22.78f, 9.48f, 22.78f, 6.34f, 19.66f)
            curveTo(3.22f, 16.54f, 3.22f, 11.48f, 6.34f, 8.36f)
            lineTo(12f, 2.69f)
            close()
        }
    }

    private val wifi by lazy {
        lucide("Wifi") {
            moveTo(12f, 20f)
            horizontalLineTo(12.01f)
            moveTo(8.5f, 16.5f)
            curveTo(10.43f, 14.57f, 13.57f, 14.57f, 15.5f, 16.5f)
            moveTo(5f, 13f)
            curveTo(8.87f, 9.13f, 15.13f, 9.13f, 19f, 13f)
            moveTo(1.5f, 9.5f)
            curveTo(7.3f, 3.7f, 16.7f, 3.7f, 22.5f, 9.5f)
        }
    }

    private val smartphone by lazy {
        lucide("Smartphone") {
            moveTo(7f, 2f)
            horizontalLineTo(17f)
            curveTo(18.1f, 2f, 19f, 2.9f, 19f, 4f)
            verticalLineTo(20f)
            curveTo(19f, 21.1f, 18.1f, 22f, 17f, 22f)
            horizontalLineTo(7f)
            curveTo(5.9f, 22f, 5f, 21.1f, 5f, 20f)
            verticalLineTo(4f)
            curveTo(5f, 2.9f, 5.9f, 2f, 7f, 2f)
            close()
            moveTo(12f, 18f)
            horizontalLineTo(12.01f)
        }
    }

    private val car by lazy {
        lucide("Car") {
            moveTo(19f, 17f)
            horizontalLineTo(5f)
            curveTo(3.9f, 17f, 3f, 16.1f, 3f, 15f)
            lineTo(3f, 10f)
            lineTo(5.5f, 5.5f)
            curveTo(5.8f, 5f, 6.4f, 4.7f, 7f, 4.7f)
            horizontalLineTo(17f)
            curveTo(17.6f, 4.7f, 18.2f, 5f, 18.5f, 5.5f)
            lineTo(21f, 10f)
            verticalLineTo(15f)
            curveTo(21f, 16.1f, 20.1f, 17f, 19f, 17f)
            close()
            moveTo(7f, 17f)
            verticalLineTo(19f)
            moveTo(17f, 17f)
            verticalLineTo(19f)
            moveTo(7f, 13f)
            horizontalLineTo(7.01f)
            moveTo(17f, 13f)
            horizontalLineTo(17.01f)
        }
    }

    private val plane by lazy {
        lucide("Plane") {
            moveTo(17.8f, 19.2f)
            lineTo(16f, 11f)
            lineTo(21f, 8f)
            curveTo(21.5f, 7.7f, 22f, 7f, 22f, 6f)
            curveTo(22f, 4.9f, 21.1f, 4f, 20f, 4f)
            curveTo(19f, 4f, 18.3f, 4.5f, 18f, 5f)
            lineTo(15f, 10f)
            lineTo(7f, 8f)
            lineTo(4f, 3f)
            horizontalLineTo(2f)
            lineTo(4f, 10f)
            lineTo(2f, 14f)
            horizontalLineTo(4f)
            lineTo(8f, 13f)
            lineTo(11f, 20f)
            horizontalLineTo(13f)
            lineTo(12f, 14f)
            lineTo(17.8f, 19.2f)
            close()
        }
    }

    private val bank by lazy {
        lucide("Landmark") {
            moveTo(3f, 22f)
            horizontalLineTo(21f)
            moveTo(6f, 18f)
            verticalLineTo(11f)
            moveTo(10f, 18f)
            verticalLineTo(11f)
            moveTo(14f, 18f)
            verticalLineTo(11f)
            moveTo(18f, 18f)
            verticalLineTo(11f)
            moveTo(12f, 2f)
            lineTo(20f, 7f)
            horizontalLineTo(4f)
            lineTo(12f, 2f)
            close()
        }
    }

    private val creditCard by lazy {
        lucide("CreditCard") {
            moveTo(3f, 5f)
            horizontalLineTo(21f)
            curveTo(22.1f, 5f, 23f, 5.9f, 23f, 7f)
            verticalLineTo(17f)
            curveTo(23f, 18.1f, 22.1f, 19f, 21f, 19f)
            horizontalLineTo(3f)
            curveTo(1.9f, 19f, 1f, 18.1f, 1f, 17f)
            verticalLineTo(7f)
            curveTo(1f, 5.9f, 1.9f, 5f, 3f, 5f)
            close()
            moveTo(1f, 10f)
            horizontalLineTo(23f)
        }
    }

    private val wallet by lazy {
        lucide("Wallet") {
            moveTo(19f, 7f)
            verticalLineTo(4f)
            curveTo(19f, 3.45f, 18.55f, 3f, 18f, 3f)
            horizontalLineTo(5f)
            curveTo(3.9f, 3f, 3f, 3.9f, 3f, 5f)
            verticalLineTo(19f)
            curveTo(3f, 20.1f, 3.9f, 21f, 5f, 21f)
            horizontalLineTo(19f)
            curveTo(20.1f, 21f, 21f, 20.1f, 21f, 19f)
            verticalLineTo(15f)
            horizontalLineTo(17f)
            curveTo(15.9f, 15f, 15f, 14.1f, 15f, 13f)
            curveTo(15f, 11.9f, 15.9f, 11f, 17f, 11f)
            horizontalLineTo(21f)
            verticalLineTo(8f)
            curveTo(21f, 7.45f, 20.55f, 7f, 20f, 7f)
            horizontalLineTo(19f)
            close()
        }
    }

    private val trendingUp by lazy {
        lucide("TrendingUp") {
            moveTo(22f, 7f)
            lineTo(13.5f, 15.5f)
            lineTo(8.5f, 10.5f)
            lineTo(2f, 17f)
            moveTo(16f, 7f)
            horizontalLineTo(22f)
            verticalLineTo(13f)
        }
    }

    private val trendingDown by lazy {
        lucide("TrendingDown") {
            moveTo(22f, 17f)
            lineTo(13.5f, 8.5f)
            lineTo(8.5f, 13.5f)
            lineTo(2f, 7f)
            moveTo(16f, 17f)
            horizontalLineTo(22f)
            verticalLineTo(11f)
        }
    }

    private val receipt by lazy {
        lucide("Receipt") {
            moveTo(4f, 2f)
            verticalLineTo(22f)
            lineTo(7f, 20f)
            lineTo(10f, 22f)
            lineTo(13f, 20f)
            lineTo(16f, 22f)
            lineTo(19f, 20f)
            lineTo(20f, 22f)
            verticalLineTo(2f)
            lineTo(17f, 4f)
            lineTo(14f, 2f)
            lineTo(11f, 4f)
            lineTo(8f, 2f)
            lineTo(4f, 2f)
            close()
            moveTo(8f, 8f)
            horizontalLineTo(16f)
            moveTo(8f, 12f)
            horizontalLineTo(16f)
            moveTo(8f, 16f)
            horizontalLineTo(12f)
        }
    }

    private val swapHoriz by lazy {
        lucide("ArrowRightLeft") {
            moveTo(8f, 3f)
            lineTo(4f, 7f)
            lineTo(8f, 11f)
            moveTo(4f, 7f)
            horizontalLineTo(20f)
            moveTo(16f, 21f)
            lineTo(20f, 17f)
            lineTo(16f, 13f)
            moveTo(20f, 17f)
            horizontalLineTo(4f)
        }
    }

    private val repeat by lazy {
        lucide("Repeat") {
            moveTo(17f, 1f)
            lineTo(21f, 5f)
            lineTo(17f, 9f)
            moveTo(3f, 11f)
            verticalLineTo(9f)
            curveTo(3f, 6.79f, 4.79f, 5f, 7f, 5f)
            horizontalLineTo(21f)
            moveTo(7f, 23f)
            lineTo(3f, 19f)
            lineTo(7f, 15f)
            moveTo(21f, 13f)
            verticalLineTo(15f)
            curveTo(21f, 17.21f, 19.21f, 19f, 17f, 19f)
            horizontalLineTo(3f)
        }
    }

    private val gift by lazy {
        lucide("Gift") {
            moveTo(20f, 12f)
            verticalLineTo(22f)
            horizontalLineTo(4f)
            verticalLineTo(12f)
            moveTo(2f, 7f)
            horizontalLineTo(22f)
            verticalLineTo(12f)
            horizontalLineTo(2f)
            verticalLineTo(7f)
            close()
            moveTo(12f, 22f)
            verticalLineTo(7f)
            moveTo(12f, 7f)
            horizontalLineTo(7.5f)
            curveTo(6.12f, 7f, 5f, 5.88f, 5f, 4.5f)
            curveTo(5f, 3.12f, 6.12f, 2f, 7.5f, 2f)
            curveTo(10f, 2f, 12f, 7f, 12f, 7f)
            close()
            moveTo(12f, 7f)
            horizontalLineTo(16.5f)
            curveTo(17.88f, 7f, 19f, 5.88f, 19f, 4.5f)
            curveTo(19f, 3.12f, 17.88f, 2f, 16.5f, 2f)
            curveTo(14f, 2f, 12f, 7f, 12f, 7f)
            close()
        }
    }

    private val film by lazy {
        lucide("Film") {
            moveTo(3f, 3f)
            horizontalLineTo(21f)
            curveTo(22.1f, 3f, 23f, 3.9f, 23f, 5f)
            verticalLineTo(19f)
            curveTo(23f, 20.1f, 22.1f, 21f, 21f, 21f)
            horizontalLineTo(3f)
            curveTo(1.9f, 21f, 1f, 20.1f, 1f, 19f)
            verticalLineTo(5f)
            curveTo(1f, 3.9f, 1.9f, 3f, 3f, 3f)
            close()
            moveTo(7f, 3f)
            verticalLineTo(21f)
            moveTo(17f, 3f)
            verticalLineTo(21f)
            moveTo(1f, 12f)
            horizontalLineTo(23f)
        }
    }

    private val music by lazy {
        lucide("Music") {
            moveTo(9f, 18f)
            curveTo(9f, 19.66f, 7.66f, 21f, 6f, 21f)
            curveTo(4.34f, 21f, 3f, 19.66f, 3f, 18f)
            curveTo(3f, 16.34f, 4.34f, 15f, 6f, 15f)
            curveTo(7.66f, 15f, 9f, 16.34f, 9f, 18f)
            close()
            moveTo(9f, 18f)
            verticalLineTo(5f)
            lineTo(21f, 3f)
            verticalLineTo(16f)
            moveTo(21f, 16f)
            curveTo(21f, 17.66f, 19.66f, 19f, 18f, 19f)
            curveTo(16.34f, 19f, 15f, 17.66f, 15f, 16f)
            curveTo(15f, 14.34f, 16.34f, 13f, 18f, 13f)
            curveTo(19.66f, 13f, 21f, 14.34f, 21f, 16f)
            close()
        }
    }

    private val gamepad by lazy {
        lucide("Gamepad2") {
            moveTo(6f, 12f)
            horizontalLineTo(10f)
            moveTo(8f, 10f)
            verticalLineTo(14f)
            moveTo(15f, 13f)
            horizontalLineTo(15.01f)
            moveTo(18f, 11f)
            horizontalLineTo(18.01f)
            moveTo(6f, 6f)
            horizontalLineTo(18f)
            curveTo(20.2f, 6f, 22f, 7.8f, 22f, 10f)
            verticalLineTo(14f)
            curveTo(22f, 16.2f, 20.2f, 18f, 18f, 18f)
            horizontalLineTo(6f)
            curveTo(3.8f, 18f, 2f, 16.2f, 2f, 14f)
            verticalLineTo(10f)
            curveTo(2f, 7.8f, 3.8f, 6f, 6f, 6f)
            close()
        }
    }

    private val bookOpen by lazy {
        lucide("BookOpen") {
            moveTo(2f, 3f)
            horizontalLineTo(8f)
            curveTo(10.2f, 3f, 12f, 4.8f, 12f, 7f)
            verticalLineTo(21f)
            curveTo(12f, 19.9f, 10.2f, 19f, 8f, 19f)
            horizontalLineTo(2f)
            verticalLineTo(3f)
            close()
            moveTo(22f, 3f)
            horizontalLineTo(16f)
            curveTo(13.8f, 3f, 12f, 4.8f, 12f, 7f)
            verticalLineTo(21f)
            curveTo(12f, 19.9f, 13.8f, 19f, 16f, 19f)
            horizontalLineTo(22f)
            verticalLineTo(3f)
            close()
        }
    }

    private val heartPulse by lazy {
        lucide("HeartPulse") {
            moveTo(19f, 14f)
            curveTo(20.5f, 12.5f, 21f, 10.5f, 21f, 8.5f)
            curveTo(21f, 5.42f, 18.58f, 3f, 15.5f, 3f)
            curveTo(13.68f, 3f, 12.13f, 3.88f, 11.21f, 5.25f)
            curveTo(10.29f, 3.88f, 8.74f, 3f, 6.92f, 3f)
            curveTo(3.84f, 3f, 1.42f, 5.42f, 1.42f, 8.5f)
            curveTo(1.42f, 11f, 2.5f, 13f, 4.5f, 15f)
            lineTo(12f, 22f)
            lineTo(15.5f, 18.5f)
            moveTo(3.22f, 12f)
            horizontalLineTo(7.5f)
            lineTo(9f, 9f)
            lineTo(11f, 15f)
            lineTo(12.5f, 12f)
            horizontalLineTo(16.5f)
        }
    }

    private val dumbbell by lazy {
        lucide("Dumbbell") {
            moveTo(14.4f, 14.4f)
            lineTo(9.6f, 9.6f)
            moveTo(18.66f, 8.66f)
            lineTo(15.34f, 5.34f)
            moveTo(21.49f, 5.83f)
            lineTo(18.17f, 2.51f)
            moveTo(22f, 9f)
            lineTo(15f, 2f)
            moveTo(8.66f, 18.66f)
            lineTo(5.34f, 15.34f)
            moveTo(5.83f, 21.49f)
            lineTo(2.51f, 18.17f)
            moveTo(9f, 22f)
            lineTo(2f, 15f)
        }
    }

    private val briefcase by lazy {
        lucide("Briefcase") {
            moveTo(16f, 20f)
            verticalLineTo(4f)
            curveTo(16f, 2.9f, 15.1f, 2f, 14f, 2f)
            horizontalLineTo(10f)
            curveTo(8.9f, 2f, 8f, 2.9f, 8f, 4f)
            verticalLineTo(20f)
            moveTo(2f, 7f)
            horizontalLineTo(22f)
            verticalLineTo(19f)
            curveTo(22f, 20.1f, 21.1f, 21f, 20f, 21f)
            horizontalLineTo(4f)
            curveTo(2.9f, 21f, 2f, 20.1f, 2f, 19f)
            verticalLineTo(7f)
            close()
        }
    }

    private val tv by lazy {
        lucide("Tv") {
            moveTo(20f, 7f)
            horizontalLineTo(4f)
            curveTo(2.9f, 7f, 2f, 7.9f, 2f, 9f)
            verticalLineTo(18f)
            curveTo(2f, 19.1f, 2.9f, 20f, 4f, 20f)
            horizontalLineTo(20f)
            curveTo(21.1f, 20f, 22f, 19.1f, 22f, 18f)
            verticalLineTo(9f)
            curveTo(22f, 7.9f, 21.1f, 7f, 20f, 7f)
            close()
            moveTo(17f, 2f)
            lineTo(12f, 7f)
            lineTo(7f, 2f)
        }
    }

    private val tag by lazy {
        lucide("Tag") {
            moveTo(20.59f, 13.41f)
            lineTo(12f, 22f)
            lineTo(2f, 12f)
            verticalLineTo(2f)
            horizontalLineTo(12f)
            lineTo(20.59f, 10.59f)
            curveTo(21.37f, 11.37f, 21.37f, 12.63f, 20.59f, 13.41f)
            close()
            moveTo(7f, 7f)
            horizontalLineTo(7.01f)
        }
    }

    fun resolve(key: String): ImageVector? {
        return when (key) {
            "restaurant", "food", "dining", "utensils" -> utensils
            "local_cafe", "cafe", "coffee", "tea" -> coffee
            "local_bar", "bar", "drinks", "pub", "wine" -> wine
            "shopping_cart", "groceries", "shopping", "cart", "supermarket" -> shoppingCart
            "shopping_bag", "bag", "retail", "order" -> shoppingBag
            "home", "rent", "housing", "mortgage", "house" -> home
            "bolt", "electricity", "power", "utilities" -> bolt
            "water_drop", "water" -> waterDrop
            "wifi", "internet", "broadband" -> wifi
            "phone_android", "mobile", "recharge", "telecom" -> smartphone
            "directions_car", "car", "vehicle", "transport" -> car
            "flight", "travel", "airfare", "airplane" -> plane
            "payments", "salary", "paycheck", "cashflow" -> bank
            "account_balance", "bank", "bank_account" -> bank
            "account_balance_wallet", "wallet", "cash", "prepaid_wallet" -> wallet
            "credit_card", "card", "debit_card", "emi" -> creditCard
            "trending_up", "investment", "stocks", "mutual_fund", "sip" -> trendingUp
            "trending_down", "loss", "loan" -> trendingDown
            "receipt_long", "bills", "invoice", "subscription" -> receipt
            "swap_horiz", "transfer", "cc_settlement" -> swapHoriz
            "autorenew", "recurring", "refund" -> repeat
            "card_giftcard", "gift", "bonus", "redeem" -> gift
            "movie", "theaters", "cinema" -> film
            "music_note", "music", "spotify" -> music
            "sports_esports", "gaming", "game", "console" -> gamepad
            "school", "education", "courses", "tuition", "menu_book", "books" -> bookOpen
            "local_hospital", "medical_services", "health" -> heartPulse
            "fitness_center", "gym" -> dumbbell
            "work", "business_center" -> briefcase
            "tv" -> tv
            "category" -> tag
            else -> null // Graceful fallback to Material
        }
    }
}
