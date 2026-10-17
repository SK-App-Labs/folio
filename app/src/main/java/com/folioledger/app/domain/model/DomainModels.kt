// File: app/src/main/java/com/folioledger/app/domain/model/DomainModels.kt
package com.folioledger.app.domain.model

data class AccountSummary(
    val id: Long,
    val name: String,
    val groupType: AccountGroup,
    val currencyCode: String,
    val currentBalance: MinorMoney,          // For Credit Card: negative when owing debt
    val creditLimit: MinorMoney? = null,
    val creditCardStatementPayable: MinorMoney? = null, // Amount due for closed statement cycle
    val creditCardUnbilledAmount: MinorMoney? = null,   // Unbilled spend in active cycle
    val settlementDayOfMonth: Int? = null,
    val paymentDueDayOfMonth: Int? = null,
    val parentAccountId: Long? = null,              // Used by DEBIT_CARD
    val debitCardPeriodSpend: MinorMoney? = null,   // Current financial month spend via Debit Card
    val includeInNetWorth: Boolean = true,
    val isArchived: Boolean = false,
    val colorHex: String = "#10B981",
    val iconName: String = "account_balance_wallet"
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
    val transferToAccountId: Long? = null,
    val transferToAccountName: String? = null,
    val transferFee: MinorMoney = MinorMoney.ZERO,
    val categoryId: Long? = null,
    val categoryName: String? = null,
    val parentCategoryName: String? = null,
    val categoryColorHex: String? = null,
    val categoryIconName: String? = null,
    val isSplit: Boolean = false,
    val splits: List<SplitLineItem> = emptyList(),
    val tags: List<String> = emptyList(),
    val payeeOrDescription: String = "",
    val note: String = "",
    val installmentIndex: Int? = null,
    val installmentTotalCount: Int? = null
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

data class MonthlyTrendBar(
    val periodLabel: String,
    val startEpochDay: Long,
    val endEpochDay: Long,
    val totalIncome: MinorMoney,
    val totalExpense: MinorMoney,
    val netSavings: MinorMoney
)

data class SpendRankingItem(
    val label: String,
    val totalAmount: MinorMoney,
    val transactionCount: Int,
    val percentageOfTotal: Float
)

data class TransactionBookmark(
    val id: Long = 0L,
    val title: String,
    val type: TransactionType,
    val amount: MinorMoney,
    val accountId: Long,
    val transferToAccountId: Long? = null,
    val categoryId: Long? = null,
    val payeeOrDescription: String = "",
    val note: String = "",
    val tags: List<String> = emptyList()
)

data class TransactionSearchFilter(
    val queryText: String = "",
    val startEpochDay: Long? = null,
    val endEpochDay: Long? = null,
    val accountIds: Set<Long> = emptySet(),
    val categoryIds: Set<Long> = emptySet(),
    val types: Set<TransactionType> = emptySet(),
    val tags: Set<String> = emptySet(),
    val minAmount: MinorMoney? = null,
    val maxAmount: MinorMoney? = null
)