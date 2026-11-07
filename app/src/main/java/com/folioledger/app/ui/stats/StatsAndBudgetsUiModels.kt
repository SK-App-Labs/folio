package com.folioledger.app.ui.stats

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.folioledger.app.R
import com.folioledger.app.domain.model.AccountSavingsGoal
import com.folioledger.app.domain.model.AccountSummary
import com.folioledger.app.domain.model.BudgetProgress
import com.folioledger.app.domain.model.CategoryNode
import com.folioledger.app.domain.model.CategorySpendStat
import com.folioledger.app.domain.model.CategoryType
import com.folioledger.app.domain.model.CurrencySpec
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.MonthlySavingsProgress
import com.folioledger.app.domain.model.MonthlyTrendBar
import com.folioledger.app.domain.model.NumberGroupingSystem
import com.folioledger.app.domain.model.PeriodLedgerSummary
import com.folioledger.app.domain.model.RecurringCommitmentItem
import com.folioledger.app.domain.model.SpendRankingItem
import com.folioledger.app.domain.model.SupportedCurrencies
import com.folioledger.app.domain.model.TransactionItem
import com.folioledger.app.domain.model.UserPreferences
import com.folioledger.app.domain.util.StatsPeriodCalculator
import com.folioledger.app.ui.accounts.CreditCardSettlementSheetState
import java.time.LocalDate

typealias StatsPeriodGranularity = com.folioledger.app.domain.model.StatsPeriodGranularity
typealias OverviewMode = com.folioledger.app.domain.model.OverviewMode

enum class StatsSubTab(
    val title: String,
    @param:StringRes val titleRes: Int,
) {
    STATS("Overview", R.string.stats_subtab_overview),
    CREDIT_CARDS("Cards", R.string.stats_subtab_cards),
    BUDGETS("Budgets", R.string.stats_subtab_budgets),
    TAGS_AND_NOTES("Merchants & Tags", R.string.stats_subtab_tags)
}

typealias TrendChartMode = com.folioledger.app.domain.model.TrendChartMode
typealias NetWorthChartMetricFilter = com.folioledger.app.domain.model.NetWorthChartMetricFilter
typealias CreditUtilizationStatus = com.folioledger.app.domain.model.CreditUtilizationStatus
typealias RunwayHealthTier = com.folioledger.app.domain.model.RunwayHealthTier
typealias SafeToSpendStatus = com.folioledger.app.domain.model.SafeToSpendStatus
typealias PayeeSortMode = com.folioledger.app.domain.model.PayeeSortMode
typealias RankingMode = com.folioledger.app.domain.model.RankingMode
typealias NetWorthGrowthTrendItem = com.folioledger.app.domain.model.NetWorthGrowthTrendItem
typealias NetWorthVelocityState = com.folioledger.app.domain.model.NetWorthVelocityState
typealias EmergencyRunwayState = com.folioledger.app.domain.model.EmergencyRunwayState
typealias PayeeDrillDownState = com.folioledger.app.domain.model.PayeeDrillDownState
typealias TagProjectDrillDownState = com.folioledger.app.domain.model.TagProjectDrillDownState
typealias SafeToSpendState = com.folioledger.app.domain.model.SafeToSpendState
typealias AccountShortfallAlert = com.folioledger.app.domain.model.AccountShortfallAlert
typealias CreditCardHealthUiModel = com.folioledger.app.domain.model.CreditCardHealthUiModel
typealias SmartSwipeRecommendation = com.folioledger.app.domain.model.SmartSwipeRecommendation
typealias LiquidCashCoverageState = com.folioledger.app.domain.model.LiquidCashCoverageState
typealias CreditCardTimelineItem = com.folioledger.app.domain.model.CreditCardTimelineItem
typealias CreditCardAnalyticsItem = com.folioledger.app.domain.model.CreditCardAnalyticsItem
typealias CreditSpendRatio = com.folioledger.app.domain.model.CreditSpendRatio
typealias CreditCardCockpitUiState = com.folioledger.app.domain.model.CreditCardCockpitUiState

typealias StatsPeriodRange = com.folioledger.app.domain.model.StatsPeriodRange

@Immutable
data class BudgetEditorDialogState(
    val isEditing: Boolean = false,
    val budgetId: Long = 0L,
    val isOverallMonthlyBudget: Boolean = true,
    val selectedCategoryId: Long? = null,
    val selectedCategoryName: String = "Overall Monthly Budget",
    val selectedCategoryColorHex: String = "#059669",
    val limitAmountInput: String = "",
    val alertThresholdPercent: Int = 80,
    val validationError: String? = null
)

@Immutable
data class CategoryDrillDownState(
    val categoryStat: CategorySpendStat,
    val categoryType: CategoryType,
    val transactions: List<TransactionItem>,
    val matchingBudget: BudgetProgress?,
    val categorySixMonthTrendBars: List<MonthlyTrendBar> = emptyList()
)

@Immutable
data class StatsAndBudgetsUiState(
    val selectedSubTab: StatsSubTab = StatsSubTab.STATS,
    val selectedOverviewMode: OverviewMode = OverviewMode.CATEGORIES,
    val periodGranularity: StatsPeriodGranularity = StatsPeriodGranularity.MONTHLY,
    val selectedCategoryType: CategoryType = CategoryType.EXPENSE,
    val selectedRankingMode: RankingMode = RankingMode.TAGS,
    val isRankingsAllTime: Boolean = false,
    val selectedTrendChartMode: TrendChartMode = TrendChartMode.INCOME_VS_EXPENSE,
    val selectedNetWorthMetricFilter: NetWorthChartMetricFilter = NetWorthChartMetricFilter.ALL_IN_ONE,
    val referenceDateEpochDay: Long = LocalDate.now().toEpochDay(),
    val customStartEpochDay: Long = LocalDate.now().withDayOfMonth(1).toEpochDay(),
    val customEndEpochDay: Long = LocalDate.now().toEpochDay(),
    val isCustomDatePickerOpen: Boolean = false,
    val activePeriodRange: StatsPeriodRange = StatsPeriodCalculator.computeFinancialMonthRange(
        referenceDateEpochDay = LocalDate.now().toEpochDay(),
        monthStartDay = 1
    ),
    val periodSummary: PeriodLedgerSummary = PeriodLedgerSummary(
        startEpochDay = 0L,
        endEpochDay = 0L,
        totalIncome = MinorMoney.ZERO,
        totalExpense = MinorMoney.ZERO,
        netCashflow = MinorMoney.ZERO,
        carryOverFromPreviousPeriod = MinorMoney.ZERO
    ),
    val priorPeriodSummary: PeriodLedgerSummary? = null,
    val userPreferences: UserPreferences = UserPreferences(),
    val currencySpec: CurrencySpec = SupportedCurrencies.INR,
    val numberGrouping: NumberGroupingSystem = NumberGroupingSystem.INDIAN,
    val expenseCategoryStats: List<CategorySpendStat> = emptyList(),
    val incomeCategoryStats: List<CategorySpendStat> = emptyList(),
    val highlightedDonutCategoryId: Long? = null,
    val sixMonthTrendBars: List<MonthlyTrendBar> = emptyList(),
    val sixMonthNetWorthTrend: List<NetWorthGrowthTrendItem> = emptyList(),
    val netWorthVelocity: NetWorthVelocityState = NetWorthVelocityState(),
    val emergencyRunway: EmergencyRunwayState = EmergencyRunwayState(),
    val totalAvailableCredit: MinorMoney = MinorMoney.ZERO,
    val totalCreditLimit: MinorMoney = MinorMoney.ZERO,
    val totalUsedCredit: MinorMoney = MinorMoney.ZERO,
    val overallCreditUtilizationPercent: Float = 0f,
    val creditCardHealthList: List<CreditCardHealthUiModel> = emptyList(),
    val creditCockpitState: CreditCardCockpitUiState = CreditCardCockpitUiState(),
    val assetAccountsForSettlement: List<AccountSummary> = emptyList(),
    val settlementSheetState: CreditCardSettlementSheetState? = null,
    val isSettlementSaving: Boolean = false,
    val budgetProgressList: List<BudgetProgress> = emptyList(),
    val overallBudgetProgress: BudgetProgress? = null,
    val categoryBudgetProgressList: List<BudgetProgress> = emptyList(),
    val expenseCategoryTree: List<CategoryNode> = emptyList(),
    val incomeCategoryTree: List<CategoryNode> = emptyList(),
    val tagRankings: List<SpendRankingItem> = emptyList(),
    val payeeOrNoteRankings: List<SpendRankingItem> = emptyList(),
    val selectedPayeeRankingSortMode: PayeeSortMode = PayeeSortMode.BY_SPEND,
    val drillDownState: CategoryDrillDownState? = null,
    val payeeDrillDownState: PayeeDrillDownState? = null,
    val tagDrillDownState: TagProjectDrillDownState? = null,
    val safeToSpend: SafeToSpendState = SafeToSpendState(),
    val shortfallAlerts: List<AccountShortfallAlert> = emptyList(),
    val budgetEditorDialogState: BudgetEditorDialogState? = null,
    val isSavingBudget: Boolean = false,
    val monthlySavingsProgress: MonthlySavingsProgress = MonthlySavingsProgress(
        targetAmount = MinorMoney.ZERO,
        savedAmount = MinorMoney.ZERO,
        remainingAmount = MinorMoney.ZERO,
        progressPercentage = 0f,
        unallocatedSurplus = MinorMoney.ZERO,
        isConfigured = false
    ),
    val accountSavingsGoals: List<AccountSavingsGoal> = emptyList(),
    val isMonthlySavingsTargetDialogOpen: Boolean = false,
    val monthlySavingsTargetInput: String = "",
    val isSavingsGoalSheetOpen: Boolean = false,
    val editingSavingsGoalAccountId: Long? = null,
    val editingSavingsGoalTitle: String = "Savings Goal",
    val editingSavingsGoalTargetAmountInput: String = "",
    val editingSavingsGoalTargetDateEpochDay: Long? = null,
    val snackbarMessage: String? = null
) {
    val activeCategoryStats: List<CategorySpendStat>
        get() = when (selectedCategoryType) {
            CategoryType.EXPENSE -> expenseCategoryStats
            CategoryType.INCOME -> incomeCategoryStats
        }

    val activeRankingItems: List<SpendRankingItem>
        get() = when (selectedRankingMode) {
            RankingMode.TAGS -> tagRankings
            RankingMode.PAYEES_AND_NOTES -> payeeOrNoteRankings
        }
}

internal data class StatsInternalControlState(
    val selectedSubTab: StatsSubTab = StatsSubTab.STATS,
    val selectedOverviewMode: OverviewMode = OverviewMode.CATEGORIES,
    val periodGranularity: StatsPeriodGranularity = StatsPeriodGranularity.MONTHLY,
    val selectedCategoryType: CategoryType = CategoryType.EXPENSE,
    val selectedRankingMode: RankingMode = RankingMode.TAGS,
    val isRankingsAllTime: Boolean = false,
    val selectedTrendChartMode: TrendChartMode = TrendChartMode.INCOME_VS_EXPENSE,
    val selectedNetWorthMetricFilter: NetWorthChartMetricFilter = NetWorthChartMetricFilter.ALL_IN_ONE,
    val referenceDateEpochDay: Long = LocalDate.now().toEpochDay(),
    val customStartEpochDay: Long = LocalDate.now().withDayOfMonth(1).toEpochDay(),
    val customEndEpochDay: Long = LocalDate.now().toEpochDay(),
    val isCustomDatePickerOpen: Boolean = false,
    val highlightedDonutCategoryId: Long? = null,
    val drillDownCategoryId: Long? = null,
    val selectedPayeeRankingSortMode: PayeeSortMode = PayeeSortMode.BY_SPEND,
    val drillDownPayeeName: String? = null,
    val drillDownTagName: String? = null,
    val budgetEditorDialogState: BudgetEditorDialogState? = null,
    val isSavingBudget: Boolean = false,
    val isRunwayIncludingInvestments: Boolean = false,
    val settlementSheetState: CreditCardSettlementSheetState? = null,
    val isSettlementSaving: Boolean = false,
    val isMonthlySavingsTargetDialogOpen: Boolean = false,
    val monthlySavingsTargetInput: String = "",
    val isSavingsGoalSheetOpen: Boolean = false,
    val editingSavingsGoalAccountId: Long? = null,
    val editingSavingsGoalTitle: String = "Savings Goal",
    val editingSavingsGoalTargetAmountInput: String = "",
    val editingSavingsGoalTargetDateEpochDay: Long? = null,
    val snackbarMessage: String? = null
)

internal data class PeriodQueryParameters(
    val granularity: StatsPeriodGranularity,
    val referenceDateEpochDay: Long,
    val customStartEpochDay: Long,
    val customEndEpochDay: Long,
    val isRankingsAllTime: Boolean,
    val prefs: UserPreferences
)

internal data class TrendsAndBudgetsBundle(
    val trendBars: List<MonthlyTrendBar>,
    val budgets: List<BudgetProgress>,
    val periodTransactions: List<TransactionItem>,
    val recurringCommitments: List<RecurringCommitmentItem>
)

internal data class EnrichedPeriodStats(
    val summary: PeriodLedgerSummary,
    val priorSummary: PeriodLedgerSummary,
    val expenseStats: List<CategorySpendStat>,
    val incomeStats: List<CategorySpendStat>
)

internal data class PeriodAnalyticsBundle(
    val prefs: UserPreferences,
    val periodRange: StatsPeriodRange,
    val periodSummary: PeriodLedgerSummary,
    val priorPeriodSummary: PeriodLedgerSummary? = null,
    val expenseStats: List<CategorySpendStat>,
    val incomeStats: List<CategorySpendStat>,
    val trendBars: List<MonthlyTrendBar>,
    val budgets: List<BudgetProgress>,
    val tagRankings: List<SpendRankingItem>,
    val payeeRankings: List<SpendRankingItem>,
    val periodTransactions: List<TransactionItem>,
    val recurringCommitments: List<RecurringCommitmentItem> = emptyList()
)
