// File: app/src/main/java/com/folioledger/app/domain/model/StatsAndAnalyticsModels.kt
package com.folioledger.app.domain.model

import androidx.compose.runtime.Immutable
import java.time.LocalDate

enum class OverviewMode(val title: String) {
    CATEGORIES("Categories"),
    CASHFLOW("Cashflow"),
    NET_WORTH("Net Worth")
}

enum class TrendChartMode(val title: String) {
    INCOME_VS_EXPENSE("Income vs. Expense"),
    NET_WORTH("Net Worth"),
    CREDIT_ANALYSIS("Credit Analysis")
}

enum class NetWorthChartMetricFilter(val title: String) {
    ALL_IN_ONE("All-in-One"),
    NET_WORTH("Net Worth"),
    ASSETS("Assets"),
    LIABILITIES("Liabilities")
}

enum class CreditUtilizationStatus {
    SAFE,
    MODERATE,
    WARNING
}

enum class RunwayHealthTier {
    RESILIENT,
    ADEQUATE,
    VULNERABLE
}

enum class SafeToSpendStatus {
    SAFE,
    CARD_DEBT_EXCEEDS_CASH,
    BILLS_EXCEED_CASH,
    BILLS_EXCEED_REMAINING_CASH,
    COMBINED_OBLIGATIONS_EXCEED_CASH,
    ZERO_RESERVES
}

enum class PayeeSortMode(val title: String) {
    BY_SPEND("By Spend"),
    BY_FREQUENCY("By Visits")
}

enum class RankingMode(val title: String) {
    TAGS("#Tags & Projects"),
    PAYEES_AND_NOTES("Merchants & Payees")
}

enum class StatsPeriodGranularity(val title: String) {
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    ANNUALLY("Annually"),
    CUSTOM("Custom")
}

@Immutable
data class StatsPeriodRange(
    val startEpochDay: Long,
    val endEpochDay: Long,
    val title: String,
    val subtitle: String,
    val anchorYear: Int
)

@Immutable
data class NetWorthGrowthTrendItem(
    val epochDay: Long,
    val monthYearText: String,
    val assets: MinorMoney,
    val liabilities: MinorMoney,
    val netWorth: MinorMoney
)

@Immutable
data class NetWorthVelocityState(
    val currentNetWorth: MinorMoney = MinorMoney.ZERO,
    val previousMonthNetWorth: MinorMoney = MinorMoney.ZERO,
    val absoluteChange: MinorMoney = MinorMoney.ZERO,
    val percentageChange: Float = 0f,
    val isPositive: Boolean = true
)

@Immutable
data class EmergencyRunwayState(
    val totalLiquidFunds: MinorMoney = MinorMoney.ZERO,
    val totalInvestmentFunds: MinorMoney = MinorMoney.ZERO,
    val averageMonthlyBurnMinor: Long = 0L,
    val runwayMonths: Float = 0f,
    val healthTier: RunwayHealthTier = RunwayHealthTier.ADEQUATE,
    val targetMonths: Float = 6.0f,
    val includesInvestments: Boolean = false
)

@Immutable
data class PayeeDrillDownState(
    val payeeName: String,
    val totalPeriodSpend: MinorMoney,
    val totalAllTimeSpend: MinorMoney,
    val periodVisitCount: Int,
    val allTimeVisitCount: Int,
    val averageTicketSize: MinorMoney,
    val categoryDistribution: List<CategorySpendStat>,
    val monthlyTrendBars: List<MonthlyTrendBar>,
    val transactions: List<TransactionItem>
)

@Immutable
data class TagProjectDrillDownState(
    val tagName: String,
    val isReimbursableProject: Boolean,
    val totalSpent: MinorMoney,
    val totalReimbursed: MinorMoney,
    val netPendingOwed: MinorMoney,
    val isFullySettled: Boolean,
    val dateSpanDays: Int,
    val minEpochDay: Long? = null,
    val maxEpochDay: Long? = null,
    val dailyAverageSpend: MinorMoney,
    val categoryDistribution: List<CategorySpendStat>,
    val monthlyTrendBars: List<MonthlyTrendBar>,
    val transactions: List<TransactionItem>
)

@Immutable
data class SafeToSpendState(
    val totalLiquidBankAndCash: MinorMoney = MinorMoney.ZERO,
    val totalCreditCardDebt: MinorMoney = MinorMoney.ZERO,
    val upcoming14DayCommitments: MinorMoney = MinorMoney.ZERO,
    val trueSafeToSpend: MinorMoney = MinorMoney.ZERO,
    val isPositive: Boolean = true,
    val isCurrentPeriod: Boolean = true,
    val status: SafeToSpendStatus = SafeToSpendStatus.SAFE
)

@Immutable
data class AccountShortfallAlert(
    val accountId: Long,
    val accountName: String,
    val currentBalance: MinorMoney,
    val upcomingDebits: MinorMoney,
    val shortfallAmount: MinorMoney,
    val daysUntilShortfall: Long
)

@Immutable
data class CreditCardHealthUiModel(
    val account: AccountSummary,
    val statementPayable: MinorMoney,
    val unbilledSpend: MinorMoney,
    val totalDebt: MinorMoney,
    val creditLimit: MinorMoney,
    val availableCredit: MinorMoney,
    val utilizationPercent: Float,
    val status: CreditUtilizationStatus
)

@Immutable
data class SmartSwipeRecommendation(
    val account: AccountSummary,
    val floatDaysRemaining: Long,
    val statementCloseDate: LocalDate,
    val nextDueDate: LocalDate
)

@Immutable
data class LiquidCashCoverageState(
    val totalLiquidCash: MinorMoney,
    val totalCcDebt: MinorMoney,
    val netLiquidCash: MinorMoney,
    val coverageRatio: Float,
    val isCashBacked: Boolean
)

@Immutable
data class CreditCardTimelineItem(
    val account: AccountSummary,
    val isBilled: Boolean,
    val amountDue: MinorMoney,
    val daysRemaining: Long,
    val targetDate: LocalDate,
    val isUrgent: Boolean,
    val isOverdue: Boolean,
    val floatDays: Long
)

@Immutable
data class CreditCardAnalyticsItem(
    val account: AccountSummary,
    val currentDebt: MinorMoney,
    val creditLimit: MinorMoney,
    val availableCredit: MinorMoney,
    val utilizationPercent: Float,
    val status: CreditUtilizationStatus,
    val billedAmount: MinorMoney,
    val unbilledAmount: MinorMoney,
    val ytdSpend: MinorMoney,
    val topCategoryName: String?,
    val topCategoryPercent: Float,
    val floatDaysRemaining: Long,
    val isBilled: Boolean = false,
    val daysRemaining: Long = 0L,
    val targetDate: LocalDate = LocalDate.now(),
    val isUrgent: Boolean = false,
    val isOverdue: Boolean = false,
    val milestoneInfo: CardMilestoneInfo? = null
)

@Immutable
data class CreditSpendRatio(
    val creditSpend: MinorMoney,
    val liquidSpend: MinorMoney,
    val creditPercentage: Float,
    val liquidPercentage: Float
)

@Immutable
data class CreditCardCockpitUiState(
    val totalCreditLimit: MinorMoney = MinorMoney.ZERO,
    val totalCcDebt: MinorMoney = MinorMoney.ZERO,
    val totalAvailableCredit: MinorMoney = MinorMoney.ZERO,
    val aggregateUtilizationPercent: Float = 0f,
    val aggregateStatus: CreditUtilizationStatus = CreditUtilizationStatus.SAFE,
    val singleCardExceeding30: CreditCardAnalyticsItem? = null,
    val liquidCoverage: LiquidCashCoverageState = LiquidCashCoverageState(
        totalLiquidCash = MinorMoney.ZERO,
        totalCcDebt = MinorMoney.ZERO,
        netLiquidCash = MinorMoney.ZERO,
        coverageRatio = 1f,
        isCashBacked = true
    ),
    val smartSwipeRecommendation: SmartSwipeRecommendation? = null,
    val timelineItems: List<CreditCardTimelineItem> = emptyList(),
    val cardAnalyticsItems: List<CreditCardAnalyticsItem> = emptyList(),
    val spendRatio: CreditSpendRatio = CreditSpendRatio(
        creditSpend = MinorMoney.ZERO,
        liquidSpend = MinorMoney.ZERO,
        creditPercentage = 0f,
        liquidPercentage = 0f
    ),
    val sixMonthCreditTrendBars: List<MonthlyTrendBar> = emptyList(),
    val activeCards: List<AccountSummary> = emptyList()
)
