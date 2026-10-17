// File: app/src/main/java/com/folioledger/app/domain/model/DomainEnumsAndMoney.kt
package com.folioledger.app.domain.model

@JvmInline
value class MinorMoney(val amountMinor: Long) : Comparable<MinorMoney> {
    operator fun plus(other: MinorMoney): MinorMoney =
        MinorMoney(Math.addExact(amountMinor, other.amountMinor))

    operator fun minus(other: MinorMoney): MinorMoney =
        MinorMoney(Math.subtractExact(amountMinor, other.amountMinor))

    operator fun unaryMinus(): MinorMoney =
        MinorMoney(Math.negateExact(amountMinor))

    operator fun times(multiplier: Int): MinorMoney =
        MinorMoney(Math.multiplyExact(amountMinor, multiplier.toLong()))

    operator fun times(multiplier: Long): MinorMoney =
        MinorMoney(Math.multiplyExact(amountMinor, multiplier))

    fun abs(): MinorMoney =
        if (amountMinor < 0L) MinorMoney(Math.negateExact(amountMinor)) else this

    val isPositive: Boolean get() = amountMinor > 0L
    val isNegative: Boolean get() = amountMinor < 0L
    val isZero: Boolean get() = amountMinor == 0L

    override fun compareTo(other: MinorMoney): Int =
        amountMinor.compareTo(other.amountMinor)

    companion object {
        val ZERO = MinorMoney(0L)
    }
}

enum class NumberGroupingSystem {
    INTERNATIONAL,
    INDIAN
}

data class CurrencySpec(
    val code: String,          // e.g. "INR", "USD", "EUR", "JPY", "KWD"
    val symbol: String,        // e.g. "₹", "$", "€", "¥", "KD"
    val displayName: String,
    val fractionDigits: Int,   // 2 for INR/USD/EUR, 0 for JPY, 3 for KWD
    val defaultGrouping: NumberGroupingSystem
)

object SupportedCurrencies {
    val INR = CurrencySpec(
        code = "INR",
        symbol = "₹",
        displayName = "Indian Rupee",
        fractionDigits = 2,
        defaultGrouping = NumberGroupingSystem.INDIAN
    )
    val USD = CurrencySpec(
        code = "USD",
        symbol = "$",
        displayName = "US Dollar",
        fractionDigits = 2,
        defaultGrouping = NumberGroupingSystem.INTERNATIONAL
    )
    val EUR = CurrencySpec(
        code = "EUR",
        symbol = "€",
        displayName = "Euro",
        fractionDigits = 2,
        defaultGrouping = NumberGroupingSystem.INTERNATIONAL
    )
    val GBP = CurrencySpec(
        code = "GBP",
        symbol = "£",
        displayName = "British Pound",
        fractionDigits = 2,
        defaultGrouping = NumberGroupingSystem.INTERNATIONAL
    )
    val JPY = CurrencySpec(
        code = "JPY",
        symbol = "¥",
        displayName = "Japanese Yen",
        fractionDigits = 0,
        defaultGrouping = NumberGroupingSystem.INTERNATIONAL
    )
    val KWD = CurrencySpec(
        code = "KWD",
        symbol = "KD",
        displayName = "Kuwaiti Dinar",
        fractionDigits = 3,
        defaultGrouping = NumberGroupingSystem.INTERNATIONAL
    )
    val AED = CurrencySpec(
        code = "AED",
        symbol = "AED",
        displayName = "UAE Dirham",
        fractionDigits = 2,
        defaultGrouping = NumberGroupingSystem.INTERNATIONAL
    )
    val SGD = CurrencySpec(
        code = "SGD",
        symbol = "S$",
        displayName = "Singapore Dollar",
        fractionDigits = 2,
        defaultGrouping = NumberGroupingSystem.INTERNATIONAL
    )
    val AUD = CurrencySpec(
        code = "AUD",
        symbol = "A$",
        displayName = "Australian Dollar",
        fractionDigits = 2,
        defaultGrouping = NumberGroupingSystem.INTERNATIONAL
    )
    val CAD = CurrencySpec(
        code = "CAD",
        symbol = "C$",
        displayName = "Canadian Dollar",
        fractionDigits = 2,
        defaultGrouping = NumberGroupingSystem.INTERNATIONAL
    )
    val CHF = CurrencySpec(
        code = "CHF",
        symbol = "CHF",
        displayName = "Swiss Franc",
        fractionDigits = 2,
        defaultGrouping = NumberGroupingSystem.INTERNATIONAL
    )
    val CNY = CurrencySpec(
        code = "CNY",
        symbol = "¥",
        displayName = "Chinese Yuan",
        fractionDigits = 2,
        defaultGrouping = NumberGroupingSystem.INTERNATIONAL
    )

    val ALL: List<CurrencySpec> = listOf(
        INR,
        USD,
        EUR,
        GBP,
        JPY,
        KWD,
        AED,
        SGD,
        AUD,
        CAD,
        CHF,
        CNY
    )

    val BY_CODE: Map<String, CurrencySpec> = ALL.associateBy { it.code }

    fun fromCode(code: String): CurrencySpec =
        BY_CODE[code.trim().uppercase()] ?: INR

    fun getByCode(code: String): CurrencySpec? =
        BY_CODE[code.trim().uppercase()]
}

enum class AccountGroup(val isLiabilityByDefault: Boolean) {
    CASH(false),
    BANK_ACCOUNT(false),
    DEBIT_CARD(false),
    CREDIT_CARD(true),
    SAVINGS(false),
    PREPAID_WALLET(false),
    INVESTMENT(false),
    OVERDRAFT(true),
    LOAN(true),
    OTHER(false)
}

enum class TransactionType {
    INCOME,
    EXPENSE,
    TRANSFER,
    CC_SETTLEMENT,
    REFUND
}

enum class CategoryType {
    INCOME,
    EXPENSE
}

enum class RepeatFrequency {
    DAILY,
    WEEKLY,
    BIWEEKLY,
    MONTHLY,
    YEARLY
}