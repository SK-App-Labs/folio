// File: app/src/main/java/com/folioledger/app/ui/stats/StatsAndBudgetsViewModel.kt
package com.folioledger.app.ui.stats

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.folioledger.app.R
import com.folioledger.app.domain.model.AccountGroup
import com.folioledger.app.domain.model.AccountSavingsGoal
import com.folioledger.app.domain.model.AccountSavingsGoalConfig
import com.folioledger.app.domain.model.AccountSummary
import com.folioledger.app.domain.model.BudgetProgress
import com.folioledger.app.domain.model.CardMilestoneConfig
import com.folioledger.app.domain.model.CardMilestoneInfo
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
import com.folioledger.app.domain.model.RepeatFrequency
import com.folioledger.app.domain.model.SecondaryCurrency
import com.folioledger.app.domain.model.SpendRankingItem
import com.folioledger.app.domain.model.SupportedCurrencies
import com.folioledger.app.domain.model.TransactionItem
import com.folioledger.app.domain.model.TransactionType
import com.folioledger.app.domain.model.UserPreferences
import com.folioledger.app.domain.repository.AccountRepository
import com.folioledger.app.domain.repository.AnalyticsAndBudgetRepository
import com.folioledger.app.domain.repository.CategoryAndTagRepository
import com.folioledger.app.domain.repository.SettingsAndBackupRepository
import com.folioledger.app.domain.repository.TransactionRepository
import com.folioledger.app.domain.usecase.AccountingRulesEngine
import com.folioledger.app.domain.util.CardMilestoneCalculator
import com.folioledger.app.domain.util.CreditCardCockpitCalculator
import com.folioledger.app.domain.util.MerchantAndTagAnalyticsCalculator
import com.folioledger.app.domain.util.MoneyFormatterAndCalculator
import com.folioledger.app.domain.util.NetWorthPredictiveCalculator
import com.folioledger.app.domain.util.RecurringScheduleCalculator
import com.folioledger.app.domain.util.SafeToSpendCalculator
import com.folioledger.app.domain.util.SavingsGoalCalculator
import com.folioledger.app.domain.util.StatsPeriodCalculator
import com.folioledger.app.ui.accounts.CreditCardCycleCalculator
import com.folioledger.app.ui.accounts.CreditCardCycleWindow
import com.folioledger.app.ui.accounts.CreditCardSettlementSheetState
import com.folioledger.app.ui.accounts.SettlementAmountPreset
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
typealias StatsPeriodCalculator = com.folioledger.app.domain.util.StatsPeriodCalculator

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

private data class StatsInternalControlState(
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

private data class PeriodQueryParameters(
    val granularity: StatsPeriodGranularity,
    val referenceDateEpochDay: Long,
    val customStartEpochDay: Long,
    val customEndEpochDay: Long,
    val isRankingsAllTime: Boolean,
    val prefs: UserPreferences
)

private data class TrendsAndBudgetsBundle(
    val trendBars: List<MonthlyTrendBar>,
    val budgets: List<BudgetProgress>,
    val periodTransactions: List<TransactionItem>,
    val recurringCommitments: List<RecurringCommitmentItem>
)

private data class EnrichedPeriodStats(
    val summary: PeriodLedgerSummary,
    val priorSummary: PeriodLedgerSummary,
    val expenseStats: List<CategorySpendStat>,
    val incomeStats: List<CategorySpendStat>
)

private data class PeriodAnalyticsBundle(
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

@OptIn(ExperimentalCoroutinesApi::class)
class StatsAndBudgetsViewModel(
    private val analyticsAndBudgetRepository: AnalyticsAndBudgetRepository,
    categoryAndTagRepository: CategoryAndTagRepository,
    transactionRepository: TransactionRepository,
    private val settingsAndBackupRepository: SettingsAndBackupRepository,
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val controlState = MutableStateFlow(StatsInternalControlState())

    private val userPreferencesFlow = settingsAndBackupRepository.observeUserPreferences()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = UserPreferences()
        )

    private val allAccountsFlow = accountRepository.observeAllAccounts(LocalDate.now().toEpochDay())

    private val netWorthQueryParamsFlow = controlState
        .map { ctrl -> ctrl.periodGranularity to ctrl.referenceDateEpochDay }
        .distinctUntilChanged()

    private val netWorthWindowTxsFlow = netWorthQueryParamsFlow.flatMapLatest { (granularity, refEpochDay) ->
        val refDate = LocalDate.ofEpochDay(refEpochDay)
        val todayEpochDay = LocalDate.now().toEpochDay()
        val windowStartEpochDay = if (granularity == StatsPeriodGranularity.ANNUALLY) {
            LocalDate.of(refDate.year, 1, 1).toEpochDay()
        } else {
            refDate.minusMonths(7).withDayOfMonth(1).toEpochDay()
        }
        val windowEndEpochDay = maxOf(todayEpochDay, refDate.plusMonths(13).toEpochDay())
        transactionRepository.observeTransactionsInRange(windowStartEpochDay, windowEndEpochDay)
    }

    private val categoryTreesFlow: Flow<Pair<List<CategoryNode>, List<CategoryNode>>> = combine(
        categoryAndTagRepository.observeCategoryTree(CategoryType.EXPENSE),
        categoryAndTagRepository.observeCategoryTree(CategoryType.INCOME)
    ) { expenseTree, incomeTree ->
        expenseTree to incomeTree
    }

    private data class PeriodQueryParametersKey(
        val granularity: StatsPeriodGranularity,
        val referenceDateEpochDay: Long,
        val customStartEpochDay: Long,
        val customEndEpochDay: Long,
        val isRankingsAllTime: Boolean
    )

    private val periodQueryParametersFlow = combine(
        controlState.map { ctrl ->
            PeriodQueryParametersKey(
                granularity = ctrl.periodGranularity,
                referenceDateEpochDay = ctrl.referenceDateEpochDay,
                customStartEpochDay = ctrl.customStartEpochDay,
                customEndEpochDay = ctrl.customEndEpochDay,
                isRankingsAllTime = ctrl.isRankingsAllTime
            )
        }.distinctUntilChanged(),
        settingsAndBackupRepository.observeUserPreferences().distinctUntilChanged()
    ) { key, prefs ->
        PeriodQueryParameters(
            granularity = key.granularity,
            referenceDateEpochDay = key.referenceDateEpochDay,
            customStartEpochDay = key.customStartEpochDay,
            customEndEpochDay = key.customEndEpochDay,
            isRankingsAllTime = key.isRankingsAllTime,
            prefs = prefs
        )
    }.distinctUntilChanged()

    private val periodAnalyticsFlow: Flow<PeriodAnalyticsBundle> = periodQueryParametersFlow.flatMapLatest { params ->
        val prefs = params.prefs
        val periodRange = StatsPeriodCalculator.computePeriodRange(
            granularity = params.granularity,
            referenceDateEpochDay = params.referenceDateEpochDay,
            monthStartDay = prefs.monthStartDay,
            weekStartDayIso = prefs.weekStartDayIso,
            customStartEpochDay = params.customStartEpochDay,
            customEndEpochDay = params.customEndEpochDay
        )

        val priorPeriodRange = when (params.granularity) {
            StatsPeriodGranularity.WEEKLY -> StatsPeriodCalculator.computeWeeklyRange(
                referenceDateEpochDay = LocalDate.ofEpochDay(periodRange.startEpochDay).minusDays(1).toEpochDay(),
                weekStartDayIso = prefs.weekStartDayIso
            )
            StatsPeriodGranularity.MONTHLY -> StatsPeriodCalculator.computeFinancialMonthRange(
                referenceDateEpochDay = LocalDate.ofEpochDay(periodRange.startEpochDay).minusDays(1).toEpochDay(),
                monthStartDay = prefs.monthStartDay
            )
            StatsPeriodGranularity.ANNUALLY -> StatsPeriodCalculator.computeAnnualRange(
                referenceDateEpochDay = LocalDate.ofEpochDay(periodRange.startEpochDay).minusDays(1).toEpochDay(),
                monthStartDay = prefs.monthStartDay
            )
            StatsPeriodGranularity.CUSTOM -> {
                val span = periodRange.endEpochDay - periodRange.startEpochDay + 1
                StatsPeriodCalculator.computeCustomRange(
                    customStartEpochDay = periodRange.startEpochDay - span,
                    customEndEpochDay = periodRange.startEpochDay - 1
                )
            }
        }

        val currentStatsFlow = combine(
            analyticsAndBudgetRepository.observePeriodLedgerSummary(
                startEpochDay = periodRange.startEpochDay,
                endEpochDay = periodRange.endEpochDay,
                includeCarryOver = prefs.carryOverEnabled &&
                        params.granularity == StatsPeriodGranularity.MONTHLY
            ),
            analyticsAndBudgetRepository.observeCategorySpendStats(
                startEpochDay = periodRange.startEpochDay,
                endEpochDay = periodRange.endEpochDay,
                type = CategoryType.EXPENSE
            ),
            analyticsAndBudgetRepository.observeCategorySpendStats(
                startEpochDay = periodRange.startEpochDay,
                endEpochDay = periodRange.endEpochDay,
                type = CategoryType.INCOME
            )
        ) { summary, expenseStats, incomeStats ->
            Triple(summary, expenseStats, incomeStats)
        }

        val priorStatsFlow = combine(
            analyticsAndBudgetRepository.observePeriodLedgerSummary(
                startEpochDay = priorPeriodRange.startEpochDay,
                endEpochDay = priorPeriodRange.endEpochDay,
                includeCarryOver = false
            ),
            analyticsAndBudgetRepository.observeCategorySpendStats(
                startEpochDay = priorPeriodRange.startEpochDay,
                endEpochDay = priorPeriodRange.endEpochDay,
                type = CategoryType.EXPENSE
            ),
            analyticsAndBudgetRepository.observeCategorySpendStats(
                startEpochDay = priorPeriodRange.startEpochDay,
                endEpochDay = priorPeriodRange.endEpochDay,
                type = CategoryType.INCOME
            )
        ) { priorSummary, priorExpense, priorIncome ->
            Triple(priorSummary, priorExpense, priorIncome)
        }

        val summaryAndCategoryStatsFlow = combine(
            currentStatsFlow,
            priorStatsFlow
        ) { (summary, expenseStats, incomeStats), (priorSummary, priorExpense, priorIncome) ->
            fun attachComparativeDeltas(
                currentList: List<CategorySpendStat>,
                priorList: List<CategorySpendStat>
            ): List<CategorySpendStat> {
                val priorMap = priorList.associateBy { it.categoryId }
                return currentList.map { current ->
                    val prior = priorMap[current.categoryId]
                    val priorAmount = prior?.netAmount
                    val changePct = if (priorAmount != null && priorAmount.amountMinor > 0L) {
                        ((current.netAmount.amountMinor - priorAmount.amountMinor).toFloat() / priorAmount.amountMinor.toFloat()) * 100f
                    } else if (priorAmount != null && priorAmount.amountMinor == 0L && current.netAmount.amountMinor > 0L) {
                        100f
                    } else {
                        null
                    }
                    current.copy(
                        priorPeriodAmount = priorAmount,
                        changePercentage = changePct
                    )
                }
            }

            val enrichedExpenses = attachComparativeDeltas(expenseStats, priorExpense)
            val enrichedIncomes = attachComparativeDeltas(incomeStats, priorIncome)
            EnrichedPeriodStats(
                summary = summary,
                priorSummary = priorSummary,
                expenseStats = enrichedExpenses,
                incomeStats = enrichedIncomes
            )
        }

        val trendsAndBudgetsFlow = combine(
            analyticsAndBudgetRepository.observeSixMonthTrendBars(
                referenceDateEpochDay = periodRange.endEpochDay,
                monthStartDay = prefs.monthStartDay.coerceIn(1, 31)
            ),
            analyticsAndBudgetRepository.observeBudgetProgressList(
                startEpochDay = periodRange.startEpochDay,
                endEpochDay = periodRange.endEpochDay
            ),
            transactionRepository.observeTransactionsInRange(
                startEpochDay = periodRange.startEpochDay,
                endEpochDay = periodRange.endEpochDay
            ),
            analyticsAndBudgetRepository.observeActiveRecurringCommitments()
        ) { trendBars, budgets, periodTxns, commitments ->
            TrendsAndBudgetsBundle(trendBars, budgets, periodTxns, commitments)
        }

        val rankingsStartDay = if (params.isRankingsAllTime) 0L else periodRange.startEpochDay
        val rankingsEndDay = if (params.isRankingsAllTime) Long.MAX_VALUE else periodRange.endEpochDay
        val rankingsFlow = combine(
            analyticsAndBudgetRepository.observeTagSpendRankings(
                startEpochDay = rankingsStartDay,
                endEpochDay = rankingsEndDay
            ),
            analyticsAndBudgetRepository.observePayeeOrNoteRankings(
                startEpochDay = rankingsStartDay,
                endEpochDay = rankingsEndDay
            )
        ) { tags, payees ->
            tags to payees
        }

        combine(
            summaryAndCategoryStatsFlow,
            trendsAndBudgetsFlow,
            rankingsFlow
        ) { enrichedStats, trendsBundle, (tags, payees) ->
            PeriodAnalyticsBundle(
                prefs = prefs,
                periodRange = periodRange,
                periodSummary = enrichedStats.summary,
                priorPeriodSummary = enrichedStats.priorSummary,
                expenseStats = enrichedStats.expenseStats,
                incomeStats = enrichedStats.incomeStats,
                trendBars = trendsBundle.trendBars,
                budgets = trendsBundle.budgets,
                tagRankings = tags,
                payeeRankings = payees,
                periodTransactions = trendsBundle.periodTransactions,
                recurringCommitments = trendsBundle.recurringCommitments
            )
        }
    }.flowOn(Dispatchers.Default)

    val uiState: StateFlow<StatsAndBudgetsUiState> = combine(
        controlState,
        periodAnalyticsFlow,
        categoryTreesFlow,
        allAccountsFlow,
        netWorthWindowTxsFlow
    ) { ctrl, bundle, (expenseTree, incomeTree), allAccounts, allTxs ->
        val prefs = bundle.prefs
        val currencySpec = SupportedCurrencies.fromCode(prefs.primaryCurrencyCode)

        val netWorthTrend = computeNetWorthTrend(
            allAccounts = allAccounts,
            allTransactions = allTxs,
            referenceDateEpochDay = ctrl.referenceDateEpochDay,
            periodGranularity = ctrl.periodGranularity
        )

        val creditHealthList = computeCreditCardHealthList(allAccounts)
        val creditCards = allAccounts.filter { !it.isArchived && it.groupType == AccountGroup.CREDIT_CARD }
        var totalLimitMinor = 0L
        var totalUsedMinor = 0L
        for (card in creditCards) {
            totalLimitMinor += card.creditLimit?.amountMinor ?: 0L
            totalUsedMinor += card.currentBalance.abs().amountMinor
        }
        val totalLimit = MinorMoney(totalLimitMinor)
        val totalUsed = MinorMoney(totalUsedMinor)
        val totalAvailable = if (totalLimit > totalUsed) totalLimit - totalUsed else MinorMoney.ZERO
        val overallUtilization = if (totalLimit.isPositive) {
            (totalUsed.amountMinor.toFloat() / totalLimit.amountMinor.toFloat() * 100f).coerceIn(0f, 100f)
        } else {
            0f
        }

        val overallBudget = bundle.budgets.firstOrNull { it.categoryId == null }
        val categoryBudgets = bundle.budgets
            .filter { it.categoryId != null }
            .sortedByDescending { it.usagePercentage }

        val activeStats = if (ctrl.selectedCategoryType == CategoryType.EXPENSE) {
            bundle.expenseStats
        } else {
            bundle.incomeStats
        }
        val activeTree = if (ctrl.selectedCategoryType == CategoryType.EXPENSE) {
            expenseTree
        } else {
            incomeTree
        }

        val drillDownState = ctrl.drillDownCategoryId?.let { targetCatId ->
            val stat = activeStats.firstOrNull { it.categoryId == targetCatId }
                ?: bundle.expenseStats.firstOrNull { it.categoryId == targetCatId }
                ?: bundle.incomeStats.firstOrNull { it.categoryId == targetCatId }

            if (stat != null) {
                val matchingCategoryIds = collectCategoryAndSubcategoryIds(
                    tree = activeTree,
                    targetCategoryId = targetCatId
                )
                val matchingTxns = bundle.periodTransactions
                    .filter { tx ->
                        val typeMatches = when (ctrl.selectedCategoryType) {
                            CategoryType.EXPENSE -> tx.type == TransactionType.EXPENSE || tx.type == TransactionType.REFUND
                            CategoryType.INCOME -> tx.type == TransactionType.INCOME
                        }
                        if (!typeMatches) return@filter false

                        val directMatch = tx.categoryId != null && tx.categoryId in matchingCategoryIds
                        val splitMatch = tx.isSplit && tx.splits.any { it.categoryId in matchingCategoryIds }
                        val nameMatch = tx.categoryName != null &&
                                tx.categoryName.equals(stat.categoryName, ignoreCase = true)
                        val parentNameMatch = tx.parentCategoryName != null &&
                                tx.parentCategoryName.equals(stat.categoryName, ignoreCase = true)

                        directMatch || splitMatch || nameMatch || parentNameMatch
                    }
                    .sortedWith(
                        compareByDescending<TransactionItem> { it.ledgerDateEpochDay }
                            .thenByDescending { it.timestampEpochMillis }
                    )

                val matchingBudget = categoryBudgets.firstOrNull { it.categoryId == targetCatId }

                // Compute category-specific 6-month historical trend
                val categoryTrendBars = bundle.trendBars.map { bar ->
                    var cycleExpenseMinor = 0L
                    var cycleIncomeMinor = 0L

                    for (tx in allTxs) {
                        if (tx.ledgerDateEpochDay !in bar.startEpochDay..bar.endEpochDay) continue

                        val directMatch = tx.categoryId != null && tx.categoryId in matchingCategoryIds
                        val splitMatch = tx.isSplit && tx.splits.any { it.categoryId in matchingCategoryIds }
                        val nameMatch = tx.categoryName != null &&
                                tx.categoryName.equals(stat.categoryName, ignoreCase = true)
                        val parentNameMatch = tx.parentCategoryName != null &&
                                tx.parentCategoryName.equals(stat.categoryName, ignoreCase = true)

                        if (!directMatch && !splitMatch && !nameMatch && !parentNameMatch) continue

                        val rawAmount = Math.abs(tx.amount.amountMinor)
                        if (ctrl.selectedCategoryType == CategoryType.EXPENSE) {
                            when (tx.type) {
                                TransactionType.EXPENSE -> cycleExpenseMinor += rawAmount
                                TransactionType.REFUND -> cycleExpenseMinor -= rawAmount
                                else -> {}
                            }
                        } else {
                            if (tx.type == TransactionType.INCOME) {
                                cycleIncomeMinor += rawAmount
                            }
                        }
                    }

                    MonthlyTrendBar(
                        periodLabel = bar.periodLabel,
                        startEpochDay = bar.startEpochDay,
                        endEpochDay = bar.endEpochDay,
                        totalIncome = MinorMoney(cycleIncomeMinor.coerceAtLeast(0L)),
                        totalExpense = MinorMoney(cycleExpenseMinor.coerceAtLeast(0L)),
                        netSavings = MinorMoney(cycleIncomeMinor - cycleExpenseMinor)
                    )
                }

                CategoryDrillDownState(
                    categoryStat = stat,
                    categoryType = ctrl.selectedCategoryType,
                    transactions = matchingTxns,
                    matchingBudget = matchingBudget,
                    categorySixMonthTrendBars = categoryTrendBars
                )
            } else {
                null
            }
        }

        val sortedPayees = if (ctrl.selectedPayeeRankingSortMode == PayeeSortMode.BY_FREQUENCY) {
            bundle.payeeRankings.sortedWith(
                compareByDescending<SpendRankingItem> { it.transactionCount }
                    .thenByDescending { it.totalAmount }
            )
        } else {
            bundle.payeeRankings
        }

        val payeeDrillDownState = ctrl.drillDownPayeeName?.let { targetPayee ->
            StatsPredictiveCalculations.computePayeeDrillDownState(
                payeeName = targetPayee,
                allTxs = allTxs,
                trendBars = bundle.trendBars,
                startEpochDay = bundle.periodRange.startEpochDay,
                endEpochDay = bundle.periodRange.endEpochDay,
                isAllTime = ctrl.isRankingsAllTime,
                primaryCurrencyCode = prefs.primaryCurrencyCode
            )
        }

        val tagDrillDownState = ctrl.drillDownTagName?.let { targetTag ->
            StatsPredictiveCalculations.computeTagProjectDrillDownState(
                tagName = targetTag,
                allTxs = allTxs,
                trendBars = bundle.trendBars,
                startEpochDay = bundle.periodRange.startEpochDay,
                endEpochDay = bundle.periodRange.endEpochDay,
                isAllTime = ctrl.isRankingsAllTime,
                primaryCurrencyCode = prefs.primaryCurrencyCode,
                secondaryCurrencies = prefs.secondaryCurrencies
            )
        }

        val assetAccountsForSettlement = allAccounts.filter {
            !it.isArchived && (
                it.groupType == AccountGroup.BANK_ACCOUNT ||
                it.groupType == AccountGroup.CASH ||
                it.groupType == AccountGroup.SAVINGS ||
                it.groupType == AccountGroup.PREPAID_WALLET
            )
        }

        val creditCockpitState = computeCreditCockpitState(
            allAccounts = allAccounts,
            allTxs = allTxs,
            periodTransactions = bundle.periodTransactions,
            trendBars = bundle.trendBars,
            referenceDateEpochDay = ctrl.referenceDateEpochDay,
            milestonesJson = prefs.cardMilestonesJson
        )

        val todayEpochDay = LocalDate.now().toEpochDay()
        val safeToSpendState = StatsPredictiveCalculations.computeSafeToSpend(
            allAccounts = allAccounts,
            recurringRules = bundle.recurringCommitments,
            futureTransactions = allTxs,
            todayEpochDay = todayEpochDay,
            periodStartEpochDay = bundle.periodRange.startEpochDay,
            periodEndEpochDay = bundle.periodRange.endEpochDay,
            primaryCurrencyCode = prefs.primaryCurrencyCode,
            secondaryCurrencies = prefs.secondaryCurrencies
        )

        val shortfallAlerts = StatsPredictiveCalculations.computeShortfallAlerts(
            allAccounts = allAccounts,
            recurringRules = bundle.recurringCommitments,
            futureTransactions = allTxs,
            todayEpochDay = todayEpochDay,
            primaryCurrencyCode = prefs.primaryCurrencyCode,
            secondaryCurrencies = prefs.secondaryCurrencies
        )

        val monthlySavingsProgress = SavingsGoalCalculator.computeMonthlySavingsProgress(
            allAccounts = allAccounts,
            periodTransactions = bundle.periodTransactions,
            periodSummary = bundle.periodSummary,
            monthlySavingsTargetMinor = prefs.monthlySavingsTargetMinor
        )

        val accountSavingsGoals = SavingsGoalCalculator.computeAccountSavingsGoals(
            allAccounts = allAccounts,
            goalsJson = prefs.savingsGoalsJson
        )

        StatsAndBudgetsUiState(
            selectedSubTab = ctrl.selectedSubTab,
            selectedOverviewMode = ctrl.selectedOverviewMode,
            periodGranularity = ctrl.periodGranularity,
            selectedCategoryType = ctrl.selectedCategoryType,
            selectedRankingMode = ctrl.selectedRankingMode,
            isRankingsAllTime = ctrl.isRankingsAllTime,
            selectedTrendChartMode = ctrl.selectedTrendChartMode,
            selectedNetWorthMetricFilter = ctrl.selectedNetWorthMetricFilter,
            referenceDateEpochDay = ctrl.referenceDateEpochDay,
            customStartEpochDay = ctrl.customStartEpochDay,
            customEndEpochDay = ctrl.customEndEpochDay,
            isCustomDatePickerOpen = ctrl.isCustomDatePickerOpen,
            activePeriodRange = bundle.periodRange,
            periodSummary = bundle.periodSummary,
            priorPeriodSummary = bundle.priorPeriodSummary,
            userPreferences = prefs,
            currencySpec = currencySpec,
            numberGrouping = prefs.numberGrouping,
            expenseCategoryStats = bundle.expenseStats,
            incomeCategoryStats = bundle.incomeStats,
            highlightedDonutCategoryId = ctrl.highlightedDonutCategoryId,
            sixMonthTrendBars = bundle.trendBars,
            sixMonthNetWorthTrend = netWorthTrend,
            netWorthVelocity = StatsPredictiveCalculations.computeNetWorthVelocity(netWorthTrend),
            emergencyRunway = StatsPredictiveCalculations.computeEmergencyRunway(
                allAccounts = allAccounts,
                trendBars = bundle.trendBars,
                primaryCurrencyCode = prefs.primaryCurrencyCode,
                secondaryCurrencies = prefs.secondaryCurrencies,
                includesInvestments = ctrl.isRunwayIncludingInvestments
            ),
            totalAvailableCredit = creditCockpitState.totalAvailableCredit,
            totalCreditLimit = creditCockpitState.totalCreditLimit,
            totalUsedCredit = creditCockpitState.totalCcDebt,
            overallCreditUtilizationPercent = creditCockpitState.aggregateUtilizationPercent,
            creditCardHealthList = creditHealthList,
            creditCockpitState = creditCockpitState,
            assetAccountsForSettlement = assetAccountsForSettlement,
            settlementSheetState = ctrl.settlementSheetState,
            isSettlementSaving = ctrl.isSettlementSaving,
            budgetProgressList = bundle.budgets,
            overallBudgetProgress = overallBudget,
            categoryBudgetProgressList = categoryBudgets,
            expenseCategoryTree = expenseTree,
            incomeCategoryTree = incomeTree,
            tagRankings = bundle.tagRankings,
            payeeOrNoteRankings = sortedPayees,
            selectedPayeeRankingSortMode = ctrl.selectedPayeeRankingSortMode,
            drillDownState = drillDownState,
            payeeDrillDownState = payeeDrillDownState,
            tagDrillDownState = tagDrillDownState,
            safeToSpend = safeToSpendState,
            shortfallAlerts = shortfallAlerts,
            budgetEditorDialogState = ctrl.budgetEditorDialogState,
            isSavingBudget = ctrl.isSavingBudget,
            monthlySavingsProgress = monthlySavingsProgress,
            accountSavingsGoals = accountSavingsGoals,
            isMonthlySavingsTargetDialogOpen = ctrl.isMonthlySavingsTargetDialogOpen,
            monthlySavingsTargetInput = ctrl.monthlySavingsTargetInput,
            isSavingsGoalSheetOpen = ctrl.isSavingsGoalSheetOpen,
            editingSavingsGoalAccountId = ctrl.editingSavingsGoalAccountId,
            editingSavingsGoalTitle = ctrl.editingSavingsGoalTitle,
            editingSavingsGoalTargetAmountInput = ctrl.editingSavingsGoalTargetAmountInput,
            editingSavingsGoalTargetDateEpochDay = ctrl.editingSavingsGoalTargetDateEpochDay,
            snackbarMessage = ctrl.snackbarMessage
        )
    }.flowOn(Dispatchers.Default)
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = StatsAndBudgetsUiState()
    )

    fun selectSubTab(subTab: StatsSubTab) {
        controlState.update { current ->
            if (subTab == StatsSubTab.BUDGETS && current.periodGranularity != StatsPeriodGranularity.MONTHLY) {
                current.copy(
                    selectedSubTab = subTab,
                    periodGranularity = StatsPeriodGranularity.MONTHLY
                )
            } else {
                current.copy(selectedSubTab = subTab)
            }
        }
    }

    fun toggleRankingsAllTime() {
        controlState.update { it.copy(isRankingsAllTime = !it.isRankingsAllTime) }
    }

    fun selectPeriodGranularity(granularity: StatsPeriodGranularity) {
        val prefs = userPreferencesFlow.value
        controlState.update { current ->
            val shouldInitCustomFromCurrent = granularity == StatsPeriodGranularity.CUSTOM &&
                    current.periodGranularity != StatsPeriodGranularity.CUSTOM
            val currentRange = if (shouldInitCustomFromCurrent) {
                StatsPeriodCalculator.computePeriodRange(
                    granularity = current.periodGranularity,
                    referenceDateEpochDay = current.referenceDateEpochDay,
                    monthStartDay = prefs.monthStartDay,
                    weekStartDayIso = prefs.weekStartDayIso,
                    customStartEpochDay = current.customStartEpochDay,
                    customEndEpochDay = current.customEndEpochDay
                )
            } else null

            current.copy(
                periodGranularity = granularity,
                customStartEpochDay = currentRange?.startEpochDay ?: current.customStartEpochDay,
                customEndEpochDay = currentRange?.endEpochDay ?: current.customEndEpochDay,
                isCustomDatePickerOpen = granularity == StatsPeriodGranularity.CUSTOM &&
                        current.periodGranularity != StatsPeriodGranularity.CUSTOM,
                highlightedDonutCategoryId = null,
                drillDownCategoryId = null
            )
        }
    }

    fun setCustomDatePickerOpen(open: Boolean) {
        controlState.update { it.copy(isCustomDatePickerOpen = open) }
    }

    fun setCustomDateRange(startEpochDay: Long, endEpochDay: Long) {
        val safeStart = minOf(startEpochDay, endEpochDay)
        val safeEnd = maxOf(startEpochDay, endEpochDay)
        controlState.update {
            it.copy(
                periodGranularity = StatsPeriodGranularity.CUSTOM,
                customStartEpochDay = safeStart,
                customEndEpochDay = safeEnd,
                referenceDateEpochDay = safeEnd,
                isCustomDatePickerOpen = false,
                highlightedDonutCategoryId = null,
                drillDownCategoryId = null
            )
        }
    }

    fun selectCategoryType(type: CategoryType) {
        controlState.update {
            it.copy(
                selectedCategoryType = type,
                highlightedDonutCategoryId = null,
                drillDownCategoryId = null
            )
        }
    }

    fun selectRankingMode(mode: RankingMode) {
        controlState.update { it.copy(selectedRankingMode = mode) }
    }

    fun selectPayeeSortMode(mode: PayeeSortMode) {
        controlState.update { it.copy(selectedPayeeRankingSortMode = mode) }
    }

    fun openPayeeDrillDown(payeeName: String) {
        controlState.update { it.copy(drillDownPayeeName = payeeName) }
    }

    fun dismissPayeeDrillDown() {
        controlState.update { it.copy(drillDownPayeeName = null) }
    }

    fun openTagDrillDown(tagName: String) {
        controlState.update { it.copy(drillDownTagName = tagName) }
    }

    fun dismissTagDrillDown() {
        controlState.update { it.copy(drillDownTagName = null) }
    }

    fun navigateToPreviousPeriod() {
        val prefs = userPreferencesFlow.value
        controlState.update { current ->
            when (current.periodGranularity) {
                StatsPeriodGranularity.WEEKLY -> {
                    current.copy(
                        referenceDateEpochDay = current.referenceDateEpochDay - 7L,
                        highlightedDonutCategoryId = null,
                        drillDownCategoryId = null
                    )
                }
                StatsPeriodGranularity.MONTHLY -> {
                    val prevEpochDay = StatsPeriodCalculator.stepFinancialMonth(
                        currentReferenceEpochDay = current.referenceDateEpochDay,
                        monthStartDay = prefs.monthStartDay,
                        deltaMonths = -1L
                    )
                    current.copy(
                        referenceDateEpochDay = prevEpochDay,
                        highlightedDonutCategoryId = null,
                        drillDownCategoryId = null
                    )
                }
                StatsPeriodGranularity.ANNUALLY -> {
                    val prevYearEpochDay = StatsPeriodCalculator.stepFinancialYear(
                        currentReferenceEpochDay = current.referenceDateEpochDay,
                        monthStartDay = prefs.monthStartDay,
                        deltaYears = -1L
                    )
                    current.copy(
                        referenceDateEpochDay = prevYearEpochDay,
                        highlightedDonutCategoryId = null,
                        drillDownCategoryId = null
                    )
                }
                StatsPeriodGranularity.CUSTOM -> {
                    val spanDays = (current.customEndEpochDay - current.customStartEpochDay + 1L)
                        .coerceAtLeast(1L)
                    val newStart = current.customStartEpochDay - spanDays
                    val newEnd = current.customEndEpochDay - spanDays
                    current.copy(
                        customStartEpochDay = newStart,
                        customEndEpochDay = newEnd,
                        referenceDateEpochDay = newEnd,
                        highlightedDonutCategoryId = null,
                        drillDownCategoryId = null
                    )
                }
            }
        }
    }

    fun navigateToNextPeriod() {
        val prefs = userPreferencesFlow.value
        controlState.update { current ->
            when (current.periodGranularity) {
                StatsPeriodGranularity.WEEKLY -> {
                    current.copy(
                        referenceDateEpochDay = current.referenceDateEpochDay + 7L,
                        highlightedDonutCategoryId = null,
                        drillDownCategoryId = null
                    )
                }
                StatsPeriodGranularity.MONTHLY -> {
                    val nextEpochDay = StatsPeriodCalculator.stepFinancialMonth(
                        currentReferenceEpochDay = current.referenceDateEpochDay,
                        monthStartDay = prefs.monthStartDay,
                        deltaMonths = 1L
                    )
                    current.copy(
                        referenceDateEpochDay = nextEpochDay,
                        highlightedDonutCategoryId = null,
                        drillDownCategoryId = null
                    )
                }
                StatsPeriodGranularity.ANNUALLY -> {
                    val nextYearEpochDay = StatsPeriodCalculator.stepFinancialYear(
                        currentReferenceEpochDay = current.referenceDateEpochDay,
                        monthStartDay = prefs.monthStartDay,
                        deltaYears = 1L
                    )
                    current.copy(
                        referenceDateEpochDay = nextYearEpochDay,
                        highlightedDonutCategoryId = null,
                        drillDownCategoryId = null
                    )
                }
                StatsPeriodGranularity.CUSTOM -> {
                    val spanDays = (current.customEndEpochDay - current.customStartEpochDay + 1L)
                        .coerceAtLeast(1L)
                    val newStart = current.customStartEpochDay + spanDays
                    val newEnd = current.customEndEpochDay + spanDays
                    current.copy(
                        customStartEpochDay = newStart,
                        customEndEpochDay = newEnd,
                        referenceDateEpochDay = newEnd,
                        highlightedDonutCategoryId = null,
                        drillDownCategoryId = null
                    )
                }
            }
        }
    }

    fun jumpToToday() {
        val today = LocalDate.now().toEpochDay()
        controlState.update { current ->
            if (current.periodGranularity == StatsPeriodGranularity.CUSTOM) {
                current.copy(isCustomDatePickerOpen = true)
            } else {
                current.copy(
                    referenceDateEpochDay = today,
                    highlightedDonutCategoryId = null,
                    drillDownCategoryId = null
                )
            }
        }
    }

    fun onDonutSliceSelected(stat: CategorySpendStat?) {
        controlState.update {
            it.copy(highlightedDonutCategoryId = stat?.categoryId)
        }
    }

    fun openCategoryDrillDown(stat: CategorySpendStat) {
        controlState.update {
            it.copy(
                highlightedDonutCategoryId = stat.categoryId,
                drillDownCategoryId = stat.categoryId
            )
        }
    }

    fun dismissCategoryDrillDown() {
        controlState.update { it.copy(drillDownCategoryId = null) }
    }

    fun selectOverviewMode(mode: OverviewMode) {
        controlState.update { it.copy(selectedOverviewMode = mode) }
    }

    fun selectTrendChartMode(mode: TrendChartMode) {
        controlState.update { it.copy(selectedTrendChartMode = mode) }
    }

    fun selectNetWorthChartMetricFilter(filter: NetWorthChartMetricFilter) {
        controlState.update { it.copy(selectedNetWorthMetricFilter = filter) }
    }

    fun toggleRunwayIncludesInvestments() {
        controlState.update { it.copy(isRunwayIncludingInvestments = !it.isRunwayIncludingInvestments) }
    }

    // Budget CRUD Actions

    fun openCreateBudgetDialog(preselectedCategoryId: Long? = null) {
        val state = uiState.value
        val existingOverall = state.overallBudgetProgress
        if (preselectedCategoryId == null && existingOverall != null) {
            openEditBudgetDialog(existingOverall)
            return
        }

        val matchingCategoryBudget = if (preselectedCategoryId != null) {
            state.categoryBudgetProgressList.firstOrNull { it.categoryId == preselectedCategoryId }
        } else {
            null
        }
        if (matchingCategoryBudget != null) {
            openEditBudgetDialog(matchingCategoryBudget)
            return
        }

        val isOverall = preselectedCategoryId == null
        val selectedNode = if (!isOverall) {
            findCategoryNode(state.expenseCategoryTree, preselectedCategoryId)
                ?: state.expenseCategoryTree.firstOrNull()
        } else {
            null
        }

        controlState.update {
            it.copy(
                budgetEditorDialogState = BudgetEditorDialogState(
                    isEditing = false,
                    budgetId = 0L,
                    isOverallMonthlyBudget = isOverall,
                    selectedCategoryId = selectedNode?.id,
                    selectedCategoryName = selectedNode?.name ?: "Overall Monthly Budget",
                    selectedCategoryColorHex = selectedNode?.colorHex ?: "#059669",
                    limitAmountInput = "",
                    alertThresholdPercent = 80,
                    validationError = null
                )
            )
        }
    }

    fun openCreateCategoryBudgetDialog() {
        val state = uiState.value
        val budgetedCatIds = state.categoryBudgetProgressList.mapNotNull { it.categoryId }.toSet()
        val firstUnbudgeted = state.expenseCategoryTree.firstOrNull { it.id !in budgetedCatIds }
            ?: state.expenseCategoryTree.firstOrNull()

        controlState.update {
            it.copy(
                budgetEditorDialogState = BudgetEditorDialogState(
                    isEditing = false,
                    budgetId = 0L,
                    isOverallMonthlyBudget = false,
                    selectedCategoryId = firstUnbudgeted?.id,
                    selectedCategoryName = firstUnbudgeted?.name ?: "Select Category",
                    selectedCategoryColorHex = firstUnbudgeted?.colorHex ?: "#059669",
                    limitAmountInput = "",
                    alertThresholdPercent = 80,
                    validationError = null
                )
            )
        }
    }

    fun openEditBudgetDialog(budget: BudgetProgress) {
        val currencySpec = SupportedCurrencies.fromCode(userPreferencesFlow.value.primaryCurrencyCode)
        val formattedLimit = formatMinorPlainForInput(budget.limitAmount, currencySpec.fractionDigits)

        controlState.update {
            it.copy(
                budgetEditorDialogState = BudgetEditorDialogState(
                    isEditing = true,
                    budgetId = budget.budgetId,
                    isOverallMonthlyBudget = budget.categoryId == null,
                    selectedCategoryId = budget.categoryId,
                    selectedCategoryName = budget.categoryName,
                    selectedCategoryColorHex = budget.colorHex,
                    limitAmountInput = formattedLimit,
                    alertThresholdPercent = budget.alertThresholdPercent.coerceIn(50, 100),
                    validationError = null
                )
            )
        }
    }

    fun updateBudgetEditorState(
        transform: (BudgetEditorDialogState) -> BudgetEditorDialogState
    ) {
        controlState.update { current ->
            val existing = current.budgetEditorDialogState ?: return@update current
            current.copy(budgetEditorDialogState = transform(existing).copy(validationError = null))
        }
    }

    fun dismissBudgetEditorDialog() {
        controlState.update { it.copy(budgetEditorDialogState = null) }
    }

    fun saveBudgetFromDialog() {
        val editor = controlState.value.budgetEditorDialogState ?: return
        val currencySpec = SupportedCurrencies.fromCode(userPreferencesFlow.value.primaryCurrencyCode)

        if (!editor.isOverallMonthlyBudget && editor.selectedCategoryId == null) {
            updateBudgetEditorState {
                it.copy(validationError = "Please select an expense category for this budget.")
            }
            return
        }

        val evaluatedLimit = MoneyFormatterAndCalculator.evaluateKeypadExpressionToMinor(
            expression = editor.limitAmountInput,
            fractionDigits = currencySpec.fractionDigits
        ).getOrNull()

        if (evaluatedLimit == null || !evaluatedLimit.isPositive) {
            updateBudgetEditorState {
                it.copy(validationError = "Enter a valid positive budget limit amount.")
            }
            return
        }

        val targetCategoryId = if (editor.isOverallMonthlyBudget) null else editor.selectedCategoryId

        viewModelScope.launch {
            controlState.update { it.copy(isSavingBudget = true) }
            val result = analyticsAndBudgetRepository.upsertBudget(
                budgetId = editor.budgetId,
                categoryId = targetCategoryId,
                limitAmount = evaluatedLimit,
                alertThresholdPercent = editor.alertThresholdPercent.coerceIn(50, 100)
            )
            result.fold(
                onSuccess = {
                    val label = if (editor.isOverallMonthlyBudget) {
                        "Overall Monthly Budget"
                    } else {
                        editor.selectedCategoryName
                    }
                    controlState.update {
                        it.copy(
                            isSavingBudget = false,
                            budgetEditorDialogState = null,
                            snackbarMessage = "Saved budget for $label"
                        )
                    }
                },
                onFailure = { err ->
                    controlState.update { state ->
                        state.copy(
                            isSavingBudget = false,
                            budgetEditorDialogState = state.budgetEditorDialogState?.copy(
                                validationError = err.message ?: "Unable to save budget."
                            )
                        )
                    }
                }
            )
        }
    }

    fun deleteBudget(budgetId: Long) {
        viewModelScope.launch {
            val result = analyticsAndBudgetRepository.deleteBudget(budgetId)
            result.fold(
                onSuccess = {
                    controlState.update {
                        it.copy(
                            budgetEditorDialogState = null,
                            snackbarMessage = "Budget removed"
                        )
                    }
                },
                onFailure = { err ->
                    controlState.update {
                        it.copy(snackbarMessage = err.message ?: "Unable to delete budget")
                    }
                }
            )
        }
    }

    fun clearSnackbarMessage() {
        controlState.update { it.copy(snackbarMessage = null) }
    }

    private fun computeNetWorthTrend(
        allAccounts: List<AccountSummary>,
        allTransactions: List<TransactionItem>,
        referenceDateEpochDay: Long,
        periodGranularity: StatsPeriodGranularity
    ): List<NetWorthGrowthTrendItem> = NetWorthPredictiveCalculator.computeNetWorthTrend(
        allAccounts = allAccounts,
        allTransactions = allTransactions,
        referenceDateEpochDay = referenceDateEpochDay,
        periodGranularity = periodGranularity
    )

    private fun computeCreditCardHealthList(
        allAccounts: List<AccountSummary>
    ): List<CreditCardHealthUiModel> = CreditCardCockpitCalculator.computeCreditCardHealthList(allAccounts)

    private fun collectCategoryAndSubcategoryIds(
        tree: List<CategoryNode>,
        targetCategoryId: Long
    ): Set<Long> {
        for ((id, _, _, _, _, _, subcategories) in tree) {
            if (id == targetCategoryId) {
                return buildSet {
                    add(id)
                    subcategories.forEach { add(it.id) }
                }
            }
            if (subcategories.any { it.id == targetCategoryId }) {
                return setOf(targetCategoryId)
            }
        }
        return setOf(targetCategoryId)
    }

    private fun findCategoryNode(tree: List<CategoryNode>, categoryId: Long?): CategoryNode? {
        if (categoryId == null) return null
        for (parent in tree) {
            if (parent.id == categoryId) return parent
            val matchingSub = parent.subcategories.firstOrNull { it.id == categoryId }
            if (matchingSub != null) return matchingSub
        }
        return null
    }

    private fun formatMinorPlainForInput(money: MinorMoney, fractionDigits: Int): String {
        if (!money.isPositive) return ""
        val formatted = MoneyFormatterAndCalculator.formatMinorMoney(
            money = money.abs(),
            currencySpec = CurrencySpec(
                code = "TMP",
                symbol = "",
                displayName = "",
                fractionDigits = fractionDigits,
                defaultGrouping = NumberGroupingSystem.INTERNATIONAL
            ),
            includeSymbol = false
        ).replace(",", "")
        return if (formatted.contains('.')) {
            formatted.trimEnd('0').trimEnd('.')
        } else {
            formatted
        }
    }

    private fun computeCreditCockpitState(
        allAccounts: List<AccountSummary>,
        allTxs: List<TransactionItem>,
        periodTransactions: List<TransactionItem>,
        trendBars: List<MonthlyTrendBar>,
        referenceDateEpochDay: Long,
        milestonesJson: String? = null
    ): CreditCardCockpitUiState = CreditCardCockpitCalculator.computeCreditCockpitState(
        allAccounts = allAccounts,
        allTxs = allTxs,
        periodTransactions = periodTransactions,
        trendBars = trendBars,
        referenceDateEpochDay = referenceDateEpochDay,
        milestonesJson = milestonesJson
    )

    fun openCreditCardSettlementSheet(creditCard: AccountSummary) {
        val currentState = controlState.value
        val spec = SupportedCurrencies.fromCode(creditCard.currencyCode)
        val statementDue = creditCard.creditCardStatementPayable ?: MinorMoney.ZERO
        val totalDebt = creditCard.currentBalance.abs()
        val initialPreset = if (statementDue.isPositive) {
            SettlementAmountPreset.STATEMENT_PAYABLE
        } else {
            SettlementAmountPreset.FULL_OUTSTANDING
        }
        val initialMinor = if (statementDue.isPositive) statementDue else totalDebt

        val defaultSourceId = uiState.value.assetAccountsForSettlement.firstOrNull {
            it.currencyCode == creditCard.currencyCode && it.currentBalance.isPositive
        }?.id ?: uiState.value.assetAccountsForSettlement.firstOrNull()?.id

        controlState.update {
            it.copy(
                settlementSheetState = CreditCardSettlementSheetState(
                    creditCardAccount = creditCard,
                    selectedSourceAccountId = defaultSourceId,
                    preset = initialPreset,
                    amountInput = formatMinorPlainForInput(initialMinor, spec.fractionDigits),
                    ledgerDateEpochDay = currentState.referenceDateEpochDay,
                    note = "CC Bill Settlement - ${creditCard.name}",
                    validationError = null
                )
            )
        }
    }

    fun updateSettlementSheetState(
        transform: (CreditCardSettlementSheetState) -> CreditCardSettlementSheetState
    ) {
        controlState.update { current ->
            val existing = current.settlementSheetState ?: return@update current
            current.copy(settlementSheetState = transform(existing).copy(validationError = null))
        }
    }

    fun selectSettlementPreset(preset: SettlementAmountPreset) {
        val sheet = controlState.value.settlementSheetState ?: return
        val card = sheet.creditCardAccount
        val spec = SupportedCurrencies.fromCode(card.currencyCode)
        val newAmountInput = when (preset) {
            SettlementAmountPreset.STATEMENT_PAYABLE -> {
                val payable = card.creditCardStatementPayable ?: MinorMoney.ZERO
                formatMinorPlainForInput(payable.abs(), spec.fractionDigits)
            }
            SettlementAmountPreset.FULL_OUTSTANDING -> {
                formatMinorPlainForInput(card.currentBalance.abs(), spec.fractionDigits)
            }
            SettlementAmountPreset.CUSTOM -> sheet.amountInput
        }
        updateSettlementSheetState {
            it.copy(preset = preset, amountInput = newAmountInput)
        }
    }

    fun dismissSettlementSheet() {
        controlState.update { it.copy(settlementSheetState = null) }
    }

    fun submitCreditCardSettlement() {
        val sheet = controlState.value.settlementSheetState ?: return
        val sourceId = sheet.selectedSourceAccountId
        if (sourceId == null) {
            updateSettlementSheetState {
                it.copy(validationError = "Select a source Bank or Cash account to pay from.")
            }
            return
        }

        val sourceAccount = uiState.value.assetAccountsForSettlement.firstOrNull { it.id == sourceId }
        if (sourceAccount != null && sourceAccount.currencyCode != sheet.creditCardAccount.currencyCode) {
            updateSettlementSheetState {
                it.copy(validationError = "Source account currency (${sourceAccount.currencyCode}) must match credit card currency (${sheet.creditCardAccount.currencyCode}).")
            }
            return
        }

        val spec = SupportedCurrencies.fromCode(sheet.creditCardAccount.currencyCode)
        val evaluatedResult = MoneyFormatterAndCalculator.evaluateKeypadExpressionToMinor(
            expression = sheet.amountInput,
            fractionDigits = spec.fractionDigits
        )

        val settlementMoney = evaluatedResult.getOrNull()
        if (settlementMoney == null || !settlementMoney.isPositive) {
            updateSettlementSheetState {
                it.copy(validationError = "Enter a valid positive settlement amount.")
            }
            return
        }

        viewModelScope.launch {
            controlState.update { it.copy(isSettlementSaving = true) }
            val result = accountRepository.settleCreditCardStatement(
                sourceAssetAccountId = sourceId,
                creditCardAccountId = sheet.creditCardAccount.id,
                settlementAmount = settlementMoney,
                timestampEpochMillis = System.currentTimeMillis(),
                ledgerDateEpochDay = sheet.ledgerDateEpochDay,
                note = sheet.note.trim()
            )
            result.fold(
                onSuccess = {
                    val formatted = MoneyFormatterAndCalculator.formatMinorMoney(
                        money = settlementMoney,
                        currencySpec = spec
                    )
                    controlState.update {
                        it.copy(
                            isSettlementSaving = false,
                            settlementSheetState = null,
                            snackbarMessage = "Settled $formatted towards ${sheet.creditCardAccount.name}"
                        )
                    }
                },
                onFailure = { error ->
                    controlState.update { state ->
                        state.copy(
                            isSettlementSaving = false,
                            settlementSheetState = state.settlementSheetState?.copy(
                                validationError = error.message ?: "Failed to record settlement."
                            )
                        )
                    }
                }
            )
        }
    }

    fun saveCardMilestoneConfig(
        accountId: Long,
        milestoneTitle: String,
        cycleStartDateEpochDay: Long,
        targetAmountMinor: Long,
        annualFeeSavedMinor: Long?,
        isAutoRenewing: Boolean
    ) {
        viewModelScope.launch {
            settingsAndBackupRepository.updateUserPreferences { current ->
                val updatedJson = CardMilestoneCalculator.upsertMilestone(
                    currentJson = current.cardMilestonesJson,
                    config = CardMilestoneConfig(
                        accountId = accountId,
                        milestoneTitle = milestoneTitle.ifBlank { "Annual Fee Waiver" },
                        cycleStartDateEpochDay = cycleStartDateEpochDay,
                        targetAmountMinor = targetAmountMinor,
                        annualFeeSavedMinor = annualFeeSavedMinor,
                        isAutoRenewing = isAutoRenewing
                    )
                )
                current.copy(cardMilestonesJson = updatedJson)
            }
        }
    }

    fun openMonthlySavingsTargetDialog() {
        val currentTarget = uiState.value.monthlySavingsProgress.targetAmount
        val currentInput = if (currentTarget.isPositive) {
            formatMinorPlainForInput(
                currentTarget,
                uiState.value.currencySpec.fractionDigits
            )
        } else ""
        controlState.update {
            it.copy(
                isMonthlySavingsTargetDialogOpen = true,
                monthlySavingsTargetInput = currentInput
            )
        }
    }

    fun closeMonthlySavingsTargetDialog() {
        controlState.update { it.copy(isMonthlySavingsTargetDialogOpen = false) }
    }

    fun updateMonthlySavingsTargetInput(input: String) {
        controlState.update { it.copy(monthlySavingsTargetInput = input) }
    }

    fun saveMonthlySavingsTarget() {
        val input = controlState.value.monthlySavingsTargetInput.trim()
        val parsedMinor = MoneyFormatterAndCalculator.evaluateKeypadExpressionToMinor(
            expression = input,
            fractionDigits = uiState.value.currencySpec.fractionDigits
        ).getOrNull()?.amountMinor ?: 0L
        viewModelScope.launch {
            settingsAndBackupRepository.updateUserPreferences { current ->
                current.copy(monthlySavingsTargetMinor = parsedMinor)
            }
            controlState.update { it.copy(isMonthlySavingsTargetDialogOpen = false) }
        }
    }

    fun removeMonthlySavingsTarget() {
        viewModelScope.launch {
            settingsAndBackupRepository.updateUserPreferences { current ->
                current.copy(monthlySavingsTargetMinor = 0L)
            }
            controlState.update { it.copy(isMonthlySavingsTargetDialogOpen = false) }
        }
    }

    fun openAddSavingsGoalSheet(defaultAccountId: Long? = null) {
        val availableAccount = uiState.value.assetAccountsForSettlement.firstOrNull {
            it.groupType in setOf(AccountGroup.SAVINGS, AccountGroup.INVESTMENT)
        } ?: uiState.value.assetAccountsForSettlement.firstOrNull()

        controlState.update {
            it.copy(
                isSavingsGoalSheetOpen = true,
                editingSavingsGoalAccountId = defaultAccountId ?: availableAccount?.id,
                editingSavingsGoalTitle = "Savings Goal",
                editingSavingsGoalTargetAmountInput = "",
                editingSavingsGoalTargetDateEpochDay = null
            )
        }
    }

    fun openEditSavingsGoalSheet(goal: AccountSavingsGoal) {
        val targetInput = formatMinorPlainForInput(
            goal.targetAmount,
            uiState.value.currencySpec.fractionDigits
        )
        controlState.update {
            it.copy(
                isSavingsGoalSheetOpen = true,
                editingSavingsGoalAccountId = goal.accountId,
                editingSavingsGoalTitle = goal.goalTitle,
                editingSavingsGoalTargetAmountInput = targetInput,
                editingSavingsGoalTargetDateEpochDay = goal.targetDateEpochDay
            )
        }
    }

    fun closeSavingsGoalSheet() {
        controlState.update { it.copy(isSavingsGoalSheetOpen = false) }
    }

    fun updateEditingSavingsGoal(
        accountId: Long?,
        title: String,
        targetAmountInput: String,
        targetDateEpochDay: Long?
    ) {
        controlState.update {
            it.copy(
                editingSavingsGoalAccountId = accountId,
                editingSavingsGoalTitle = title,
                editingSavingsGoalTargetAmountInput = targetAmountInput,
                editingSavingsGoalTargetDateEpochDay = targetDateEpochDay
            )
        }
    }

    fun saveSavingsGoal() {
        val ctrl = controlState.value
        val accountId = ctrl.editingSavingsGoalAccountId ?: return
        val targetMinor = MoneyFormatterAndCalculator.evaluateKeypadExpressionToMinor(
            expression = ctrl.editingSavingsGoalTargetAmountInput,
            fractionDigits = uiState.value.currencySpec.fractionDigits
        ).getOrNull()?.amountMinor ?: 0L
        if (targetMinor <= 0L) return

        val config = AccountSavingsGoalConfig(
            accountId = accountId,
            goalTitle = ctrl.editingSavingsGoalTitle.ifBlank { "Savings Goal" },
            targetAmountMinor = targetMinor,
            targetDateEpochDay = ctrl.editingSavingsGoalTargetDateEpochDay
        )

        viewModelScope.launch {
            settingsAndBackupRepository.updateUserPreferences { current ->
                val updatedJson = SavingsGoalCalculator.upsertGoal(current.savingsGoalsJson, config)
                current.copy(savingsGoalsJson = updatedJson)
            }
            controlState.update { it.copy(isSavingsGoalSheetOpen = false) }
        }
    }

    fun removeSavingsGoal(accountId: Long) {
        viewModelScope.launch {
            settingsAndBackupRepository.updateUserPreferences { current ->
                val updatedJson = SavingsGoalCalculator.removeGoal(current.savingsGoalsJson, accountId)
                current.copy(savingsGoalsJson = updatedJson)
            }
            controlState.update { it.copy(isSavingsGoalSheetOpen = false) }
        }
    }
}

object StatsPredictiveCalculations {

    fun computeNetWorthVelocity(
        netWorthTrend: List<NetWorthGrowthTrendItem>
    ): NetWorthVelocityState = NetWorthPredictiveCalculator.computeNetWorthVelocity(netWorthTrend)

    fun computeEmergencyRunway(
        allAccounts: List<AccountSummary>,
        trendBars: List<MonthlyTrendBar>,
        primaryCurrencyCode: String,
        secondaryCurrencies: List<SecondaryCurrency>,
        includesInvestments: Boolean
    ): EmergencyRunwayState = NetWorthPredictiveCalculator.computeEmergencyRunway(
        allAccounts = allAccounts,
        trendBars = trendBars,
        primaryCurrencyCode = primaryCurrencyCode,
        secondaryCurrencies = secondaryCurrencies,
        includesInvestments = includesInvestments
    )

    fun computePayeeDrillDownState(
        payeeName: String,
        allTxs: List<TransactionItem>,
        trendBars: List<MonthlyTrendBar>,
        startEpochDay: Long,
        endEpochDay: Long,
        isAllTime: Boolean,
        primaryCurrencyCode: String
    ): PayeeDrillDownState = MerchantAndTagAnalyticsCalculator.computePayeeDrillDownState(
        payeeName = payeeName,
        allTxs = allTxs,
        trendBars = trendBars,
        startEpochDay = startEpochDay,
        endEpochDay = endEpochDay,
        isAllTime = isAllTime,
        primaryCurrencyCode = primaryCurrencyCode
    )

    fun computeTagProjectDrillDownState(
        tagName: String,
        allTxs: List<TransactionItem>,
        trendBars: List<MonthlyTrendBar>,
        startEpochDay: Long,
        endEpochDay: Long,
        isAllTime: Boolean,
        primaryCurrencyCode: String,
        secondaryCurrencies: List<SecondaryCurrency> = emptyList()
    ): TagProjectDrillDownState = MerchantAndTagAnalyticsCalculator.computeTagProjectDrillDownState(
        tagName = tagName,
        allTxs = allTxs,
        trendBars = trendBars,
        startEpochDay = startEpochDay,
        endEpochDay = endEpochDay,
        isAllTime = isAllTime,
        primaryCurrencyCode = primaryCurrencyCode,
        secondaryCurrencies = secondaryCurrencies
    )

    fun projectRecurringOccurrencesInHorizon(
        startDay: Long,
        frequency: RepeatFrequency,
        todayEpochDay: Long,
        horizonEpochDay: Long
    ): List<Long> = SafeToSpendCalculator.projectRecurringOccurrencesInHorizon(
        startDay = startDay,
        frequency = frequency,
        todayEpochDay = todayEpochDay,
        horizonEpochDay = horizonEpochDay
    )

    fun computeSafeToSpend(
        allAccounts: List<AccountSummary>,
        recurringRules: List<RecurringCommitmentItem>,
        futureTransactions: List<TransactionItem>,
        todayEpochDay: Long,
        periodStartEpochDay: Long,
        periodEndEpochDay: Long,
        primaryCurrencyCode: String,
        secondaryCurrencies: List<SecondaryCurrency> = emptyList()
    ): SafeToSpendState = SafeToSpendCalculator.computeSafeToSpend(
        allAccounts = allAccounts,
        recurringRules = recurringRules,
        futureTransactions = futureTransactions,
        todayEpochDay = todayEpochDay,
        periodStartEpochDay = periodStartEpochDay,
        periodEndEpochDay = periodEndEpochDay,
        primaryCurrencyCode = primaryCurrencyCode,
        secondaryCurrencies = secondaryCurrencies
    )

    fun computeShortfallAlerts(
        allAccounts: List<AccountSummary>,
        recurringRules: List<RecurringCommitmentItem>,
        futureTransactions: List<TransactionItem>,
        todayEpochDay: Long,
        primaryCurrencyCode: String,
        secondaryCurrencies: List<SecondaryCurrency> = emptyList()
    ): List<AccountShortfallAlert> = SafeToSpendCalculator.computeShortfallAlerts(
        allAccounts = allAccounts,
        recurringRules = recurringRules,
        futureTransactions = futureTransactions,
        todayEpochDay = todayEpochDay,
        primaryCurrencyCode = primaryCurrencyCode,
        secondaryCurrencies = secondaryCurrencies
    )
}
