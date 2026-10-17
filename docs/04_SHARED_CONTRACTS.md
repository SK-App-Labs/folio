# Folio — Shared Domain Models & Repository Contracts (SSOT #4)

All files declared in this specification reside in `com.folioledger.app.domain.*` and are implemented by Chat 2 and consumed by Chat 3.

## 1. Core Value Classes & Enums (`com.folioledger.app.domain.model`)

package com.folioledger.app.domain.model

@JvmInline
value class MinorMoney(val amountMinor: Long) : Comparable<MinorMoney> {
operator fun plus(other: MinorMoney): MinorMoney = MinorMoney(Math.addExact(amountMinor, other.amountMinor))
operator fun minus(other: MinorMoney): MinorMoney = MinorMoney(Math.subtractExact(amountMinor, other.amountMinor))
operator fun unaryMinus(): MinorMoney = MinorMoney(Math.negateExact(amountMinor))
val isPositive: Boolean get() = amountMinor > 0L
val isNegative: Boolean get() = amountMinor < 0L
val isZero: Boolean get() = amountMinor == 0L
override fun compareTo(other: MinorMoney): Int = amountMinor.compareTo(other.amountMinor)
companion object {
val ZERO = MinorMoney(0L)
}
}

enum class NumberGroupingSystem { INTERNATIONAL, INDIAN }

data class CurrencySpec(
val code: String,          // e.g. "INR", "USD", "EUR", "JPY", "KWD"
val symbol: String,        // e.g. "₹", "$", "€", "¥", "KD"
val displayName: String,
val fractionDigits: Int,   // 2 for INR/USD/EUR, 0 for JPY, 3 for KWD
val defaultGrouping: NumberGroupingSystem
)

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

enum class CategoryType { INCOME, EXPENSE }

enum class RepeatFrequency { DAILY, WEEKLY, BIWEEKLY, MONTHLY, YEARLY }

## 2. Primary Domain Entities (`com.folioledger.app.domain.model`)

data class AccountSummary(
val id: Long,
val name: String,
val groupType: AccountGroup,
val currencyCode: String,
val currentBalance: MinorMoney,          // For Credit Card: negative when owing debt
val creditLimit: MinorMoney?,
val creditCardStatementPayable: MinorMoney?, // Amount due for closed statement cycle
val creditCardUnbilledAmount: MinorMoney?,   // Unbilled spend in active cycle
val settlementDayOfMonth: Int?,
val paymentDueDayOfMonth: Int?,
val parentAccountId: Long?,              // Used by DEBIT_CARD
val debitCardPeriodSpend: MinorMoney?,   // Current financial month spend via Debit Card
val includeInNetWorth: Boolean,
val isArchived: Boolean,
val colorHex: String,
val iconName: String
)

data class NetWorthOverview(
val totalAssets: MinorMoney,
val totalLiabilities: MinorMoney,
val netWorth: MinorMoney,
val accountsByGroup: Map<AccountGroup, List<AccountSummary>>
)

data class CategoryNode(
val id: Long,
val name: String,
val type: CategoryType,
val parentId: Long?,
val iconName: String,
val colorHex: String,
val subcategories: List<CategoryNode> = emptyList()
)

data class SplitLineItem(
val id: Long = 0L,
val categoryId: Long,
val categoryName: String = "",
val amount: MinorMoney,
val note: String = ""
)

data class TransactionItem(
val id: Long,
val type: TransactionType,
val timestampEpochMillis: Long,
val ledgerDateEpochDay: Long,
val amount: MinorMoney,
val currencyCode: String,
val accountId: Long,
val accountName: String,
val transferToAccountId: Long?,
val transferToAccountName: String?,
val transferFee: MinorMoney,
val categoryId: Long?,
val categoryName: String?,
val parentCategoryName: String?,
val categoryColorHex: String?,
val categoryIconName: String?,
val isSplit: Boolean,
val splits: List<SplitLineItem>,
val tags: List<String>,
val payeeOrDescription: String,
val note: String,
val installmentIndex: Int?,
val installmentTotalCount: Int?
)

data class PeriodLedgerSummary(
val startEpochDay: Long,
val endEpochDay: Long,
val totalIncome: MinorMoney,
val totalExpense: MinorMoney,
val netCashflow: MinorMoney,
val carryOverFromPreviousPeriod: MinorMoney
)

data class CategorySpendStat(
val categoryId: Long,
val categoryName: String,
val colorHex: String,
val iconName: String,
val netAmount: MinorMoney,
val percentageOfTotal: Float, // 0f..100f for UI display only
val transactionCount: Int
)

data class BudgetProgress(
val budgetId: Long,
val categoryId: Long?, // null = Overall Monthly Budget
val categoryName: String,
val colorHex: String,
val limitAmount: MinorMoney,
val spentAmount: MinorMoney,
val remainingAmount: MinorMoney,
val usagePercentage: Float,
val alertThresholdPercent: Int
)

data class UserPreferences(
val primaryCurrencyCode: String = "INR",
val numberGrouping: NumberGroupingSystem = NumberGroupingSystem.INDIAN,
val monthStartDay: Int = 1,         // 1..28
val weekStartDayIso: Int = 1,       // 1 = Monday, 7 = Sunday
val subcategoriesEnabled: Boolean = true,
val carryOverEnabled: Boolean = false,
val themeMode: String = "SYSTEM",   // SYSTEM, LIGHT, DARK, AMOLED_BLACK
val dynamicColorEnabled: Boolean = false,
val accentPalette: String = "EMERALD",
val biometricLockEnabled: Boolean = false
)

## 3. Repository Interfaces (`com.folioledger.app.domain.repository`)

- AccountRepository: Observe NetWorthOverview, create/update/archive accounts, settle credit card statement.
- TransactionRepository: Observe daily/calendar/weekly/monthly transactions, insert/update/delete single, split, refund, transfer, and installment-plan transactions, search & multi-filter.
- CategoryAndTagRepository: Observe category trees, manage categories, subcategories, tags, and bookmarks.
- AnalyticsAndBudgetRepository: Observe PeriodLedgerSummary, category breakdown stats, 6-month trend bars, note/tag rankings, and BudgetProgress.
- SettingsAndBackupRepository: Observe/update UserPreferences, export CSV, create portable .zip backup, restore .zip backup.