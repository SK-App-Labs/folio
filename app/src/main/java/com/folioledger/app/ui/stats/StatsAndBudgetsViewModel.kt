// File: app/src/main/java/com/folioledger/app/ui/stats/StatsAndBudgetsViewModel.kt
package com.folioledger.app.ui.stats

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.folioledger.app.domain.model.AccountGroup
import com.folioledger.app.domain.model.AccountSummary
import com.folioledger.app.domain.model.BudgetProgress
import com.folioledger.app.domain.model.CategoryNode
import com.folioledger.app.domain.model.CategorySpendStat
import com.folioledger.app.domain.model.CategoryType
import com.folioledger.app.domain.model.CurrencySpec
import com.folioledger.app.domain.model.MinorMoney
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
import com.folioledger.app.domain.util.MoneyFormatterAndCalculator
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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class StatsPeriodGranularity(val title: String) {
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    ANNUALLY("Annually"),
    CUSTOM("Custom")
}

enum class StatsSubTab(val title: String) {
    STATS("Overview"),
    CREDIT_CARDS("Cards"),
    BUDGETS("Budgets"),
    TAGS_AND_NOTES("Tags")
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

@Immutable
data class NetWorthGrowthTrendItem(
    val epochDay: Long,
    val monthYearText: String,
    val assets: MinorMoney,
    val liabilities: MinorMoney,
    val netWorth: MinorMoney
)

enum class RunwayHealthTier {
    RESILIENT,
    ADEQUATE,
    VULNERABLE
}

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

enum class PayeeSortMode(val title: String) {
    BY_SPEND("By Spend"),
    BY_FREQUENCY("By Visits")
}

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
    val isCurrentPeriod: Boolean = true
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
    val floatDaysRemaining: Long
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

enum class RankingMode(val title: String) {
    TAGS("#Tag Rankings"),
    PAYEES_AND_NOTES("Payee / Note Rankings")
}

@Immutable
data class StatsPeriodRange(
    val startEpochDay: Long,
    val endEpochDay: Long,
    val title: String,
    val subtitle: String,
    val anchorYear: Int
)

object StatsPeriodCalculator {

    private val shortMonthYearFormatter = DateTimeFormatter.ofPattern("MMM yyyy", Locale.US)
    private val shortDayMonthFormatter = DateTimeFormatter.ofPattern("dd MMM", Locale.US)
    private val fullDayMonthYearFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.US)

    private fun resolveClampedDate(yearMonth: YearMonth, desiredDay: Int): LocalDate {
        val safeDay = desiredDay.coerceIn(1, 31)
        val clampedDay = minOf(safeDay, yearMonth.lengthOfMonth())
        return yearMonth.atDay(clampedDay)
    }

    fun computePeriodRange(
        granularity: StatsPeriodGranularity,
        referenceDateEpochDay: Long,
        monthStartDay: Int,
        weekStartDayIso: Int,
        customStartEpochDay: Long,
        customEndEpochDay: Long
    ): StatsPeriodRange = when (granularity) {
        StatsPeriodGranularity.WEEKLY -> computeWeeklyRange(
            referenceDateEpochDay = referenceDateEpochDay,
            weekStartDayIso = weekStartDayIso
        )
        StatsPeriodGranularity.MONTHLY -> computeFinancialMonthRange(
            referenceDateEpochDay = referenceDateEpochDay,
            monthStartDay = monthStartDay
        )
        StatsPeriodGranularity.ANNUALLY -> computeAnnualRange(
            referenceDateEpochDay = referenceDateEpochDay,
            monthStartDay = monthStartDay
        )
        StatsPeriodGranularity.CUSTOM -> computeCustomRange(
            customStartEpochDay = customStartEpochDay,
            customEndEpochDay = customEndEpochDay
        )
    }

    fun computeWeeklyRange(
        referenceDateEpochDay: Long,
        weekStartDayIso: Int
    ): StatsPeriodRange {
        val refDate = LocalDate.ofEpochDay(referenceDateEpochDay)
        val startDow = DayOfWeek.of(weekStartDayIso.coerceIn(1, 7))
        val startDate = refDate.with(TemporalAdjusters.previousOrSame(startDow))
        val endDate = startDate.plusDays(6)

        val title = "${startDate.format(shortDayMonthFormatter)} – ${endDate.format(shortDayMonthFormatter)}"
        val subtitle = "Weekly View • ${endDate.year}"

        return StatsPeriodRange(
            startEpochDay = startDate.toEpochDay(),
            endEpochDay = endDate.toEpochDay(),
            title = title,
            subtitle = subtitle,
            anchorYear = endDate.year
        )
    }

    fun computeFinancialMonthRange(
        referenceDateEpochDay: Long,
        monthStartDay: Int
    ): StatsPeriodRange {
        val safeStartDay = monthStartDay.coerceIn(1, 31)
        val (startEpoch, endEpoch) = AccountingRulesEngine.computeFinancialMonthRange(
            referenceDateEpochDay = referenceDateEpochDay,
            monthStartDay = safeStartDay
        )
        val startDate = LocalDate.ofEpochDay(startEpoch)
        val endDate = LocalDate.ofEpochDay(endEpoch)

        val title = startDate.format(shortMonthYearFormatter)
        val subtitle = "${startDate.format(shortDayMonthFormatter)} – ${endDate.format(fullDayMonthYearFormatter)}"

        return StatsPeriodRange(
            startEpochDay = startEpoch,
            endEpochDay = endEpoch,
            title = title,
            subtitle = subtitle,
            anchorYear = startDate.year
        )
    }

    fun computeAnnualRange(
        referenceDateEpochDay: Long,
        monthStartDay: Int
    ): StatsPeriodRange {
        val currentMonthRange = computeFinancialMonthRange(referenceDateEpochDay, monthStartDay)
        val anchorYear = currentMonthRange.anchorYear
        val safeStartDay = monthStartDay.coerceIn(1, 31)

        val startDate = resolveClampedDate(YearMonth.of(anchorYear, 1), safeStartDay)
        val nextYearStart = resolveClampedDate(YearMonth.of(anchorYear + 1, 1), safeStartDay)
        val endDate = nextYearStart.minusDays(1)

        val title = "Year $anchorYear"
        val subtitle = "${startDate.format(fullDayMonthYearFormatter)} – ${endDate.format(fullDayMonthYearFormatter)}"

        return StatsPeriodRange(
            startEpochDay = startDate.toEpochDay(),
            endEpochDay = endDate.toEpochDay(),
            title = title,
            subtitle = subtitle,
            anchorYear = anchorYear
        )
    }

    fun computeCustomRange(
        customStartEpochDay: Long,
        customEndEpochDay: Long
    ): StatsPeriodRange {
        val safeStart = minOf(customStartEpochDay, customEndEpochDay)
        val safeEnd = maxOf(customStartEpochDay, customEndEpochDay)
        val startDate = LocalDate.ofEpochDay(safeStart)
        val endDate = LocalDate.ofEpochDay(safeEnd)
        val daysCount = (safeEnd - safeStart) + 1L

        val title = if (startDate.year == endDate.year) {
            "${startDate.format(shortDayMonthFormatter)} – ${endDate.format(fullDayMonthYearFormatter)}"
        } else {
            "${startDate.format(fullDayMonthYearFormatter)} – ${endDate.format(fullDayMonthYearFormatter)}"
        }
        val subtitle = "Custom Period ($daysCount ${if (daysCount == 1L) "day" else "days"})"

        return StatsPeriodRange(
            startEpochDay = safeStart,
            endEpochDay = safeEnd,
            title = title,
            subtitle = subtitle,
            anchorYear = endDate.year
        )
    }

    fun stepFinancialMonth(
        currentReferenceEpochDay: Long,
        monthStartDay: Int,
        deltaMonths: Long
    ): Long {
        val currentRange = computeFinancialMonthRange(currentReferenceEpochDay, monthStartDay)
        val startLocalDate = LocalDate.ofEpochDay(currentRange.startEpochDay)
        val targetYearMonth = YearMonth.from(startLocalDate).plusMonths(deltaMonths)
        return resolveClampedDate(targetYearMonth, monthStartDay).toEpochDay()
    }

    fun stepFinancialYear(
        currentReferenceEpochDay: Long,
        monthStartDay: Int,
        deltaYears: Long
    ): Long {
        val currentRange = computeFinancialMonthRange(currentReferenceEpochDay, monthStartDay)
        val startLocalDate = LocalDate.ofEpochDay(currentRange.startEpochDay)
        val targetYearMonth = YearMonth.from(startLocalDate).plusYears(deltaYears)
        return resolveClampedDate(targetYearMonth, monthStartDay).toEpochDay()
    }
}

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

private data class PeriodAnalyticsBundle(
    val prefs: UserPreferences,
    val periodRange: StatsPeriodRange,
    val periodSummary: PeriodLedgerSummary,
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
    settingsAndBackupRepository: SettingsAndBackupRepository,
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
    private val netWorthWindowTxsFlow = controlState.flatMapLatest { ctrl ->
        val refDate = LocalDate.ofEpochDay(ctrl.referenceDateEpochDay)
        val todayEpochDay = LocalDate.now().toEpochDay()
        val windowStartEpochDay = if (ctrl.periodGranularity == StatsPeriodGranularity.ANNUALLY) {
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

    private val periodAnalyticsFlow: Flow<PeriodAnalyticsBundle> = combine(
        controlState,
        settingsAndBackupRepository.observeUserPreferences()
    ) { ctrl, prefs ->
        PeriodQueryParameters(
            granularity = ctrl.periodGranularity,
            referenceDateEpochDay = ctrl.referenceDateEpochDay,
            customStartEpochDay = ctrl.customStartEpochDay,
            customEndEpochDay = ctrl.customEndEpochDay,
            isRankingsAllTime = ctrl.isRankingsAllTime,
            prefs = prefs
        )
    }.flatMapLatest { params ->
        val prefs = params.prefs
        val periodRange = StatsPeriodCalculator.computePeriodRange(
            granularity = params.granularity,
            referenceDateEpochDay = params.referenceDateEpochDay,
            monthStartDay = prefs.monthStartDay,
            weekStartDayIso = prefs.weekStartDayIso,
            customStartEpochDay = params.customStartEpochDay,
            customEndEpochDay = params.customEndEpochDay
        )

        val summaryAndCategoryStatsFlow = combine(
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
        ) { (summary, expenseStats, incomeStats), trendsBundle, (tags, payees) ->
            PeriodAnalyticsBundle(
                prefs = prefs,
                periodRange = periodRange,
                periodSummary = summary,
                expenseStats = expenseStats,
                incomeStats = incomeStats,
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
            referenceDateEpochDay = ctrl.referenceDateEpochDay
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

        StatsAndBudgetsUiState(
            selectedSubTab = ctrl.selectedSubTab,
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
    ): List<NetWorthGrowthTrendItem> {
        val refLocalDate = LocalDate.ofEpochDay(referenceDateEpochDay)
        val shortMonthFormatter = DateTimeFormatter.ofPattern("MMM yyyy", Locale.US)
        
        val targetYearMonths: List<YearMonth> = if (periodGranularity == StatsPeriodGranularity.ANNUALLY) {
            (1..12).map { month -> YearMonth.of(refLocalDate.year, month) }
        } else {
            (5 downTo 0).map { offset -> YearMonth.from(refLocalDate.minusMonths(offset.toLong())) }
        }
        val trendItems = ArrayList<NetWorthGrowthTrendItem>(targetYearMonths.size)

        val activeAccounts = allAccounts.filter { !it.isArchived && it.includeInNetWorth && it.groupType != AccountGroup.DEBIT_CARD }

        for (targetYearMonth in targetYearMonths) {
            val endOfMonthEpochDay = targetYearMonth.atEndOfMonth().toEpochDay()

            var monthAssetsMinor = 0L
            var monthLiabilitiesMinor = 0L

            for (acc in activeAccounts) {
                var currentMinor = acc.currentBalance.amountMinor

                val futureTxs = allTransactions.filter { tx ->
                    tx.ledgerDateEpochDay > endOfMonthEpochDay &&
                            (tx.accountId == acc.id || tx.transferToAccountId == acc.id)
                }

                for (tx in futureTxs) {
                    val amount = Math.abs(tx.amount.amountMinor)
                    val fee = Math.abs(tx.transferFee.amountMinor)

                    if (tx.accountId == acc.id) {
                        when (tx.type) {
                            TransactionType.INCOME, TransactionType.REFUND -> currentMinor -= amount
                            TransactionType.EXPENSE -> currentMinor += (amount + fee)
                            TransactionType.TRANSFER, TransactionType.CC_SETTLEMENT -> currentMinor += (amount + fee)
                            TransactionType.OPENING_BALANCE,
                            TransactionType.RECONCILIATION -> {
                                val isOutflow = tx.transferToAccountId != null
                                if (isOutflow) {
                                    currentMinor += amount
                                } else {
                                    currentMinor -= amount
                                }
                            }
                        }
                    } else if (tx.transferToAccountId == acc.id) {
                        currentMinor -= amount
                    }
                }

                if (currentMinor >= 0L) {
                    monthAssetsMinor += currentMinor
                } else {
                    monthLiabilitiesMinor += Math.abs(currentMinor)
                }
            }

            val assets = MinorMoney(monthAssetsMinor)
            val liabilities = MinorMoney(monthLiabilitiesMinor)
            val netWorth = MinorMoney(monthAssetsMinor - monthLiabilitiesMinor)

            trendItems.add(
                NetWorthGrowthTrendItem(
                    epochDay = endOfMonthEpochDay,
                    monthYearText = targetYearMonth.format(shortMonthFormatter),
                    assets = assets,
                    liabilities = liabilities,
                    netWorth = netWorth
                )
            )
        }

        return trendItems
    }

    private fun computeCreditCardHealthList(
        allAccounts: List<AccountSummary>
    ): List<CreditCardHealthUiModel> {
        val creditCards = allAccounts.filter { !it.isArchived && it.groupType == AccountGroup.CREDIT_CARD }
        return creditCards.map { card ->
            val limit = card.creditLimit ?: MinorMoney.ZERO
            val debt = card.currentBalance.abs()
            val payable = card.creditCardStatementPayable ?: MinorMoney.ZERO
            val unbilled = card.creditCardUnbilledAmount ?: MinorMoney.ZERO

            val available = if (limit > debt) limit - debt else MinorMoney.ZERO
            val utilization = if (limit.isPositive) {
                (debt.amountMinor.toFloat() / limit.amountMinor.toFloat() * 100f).coerceIn(0f, 100f)
            } else {
                0f
            }

            val status = when {
                utilization > 80f -> CreditUtilizationStatus.WARNING
                utilization > 30f -> CreditUtilizationStatus.MODERATE
                else -> CreditUtilizationStatus.SAFE
            }

            CreditCardHealthUiModel(
                account = card,
                statementPayable = payable,
                unbilledSpend = unbilled,
                totalDebt = debt,
                creditLimit = limit,
                availableCredit = available,
                utilizationPercent = utilization,
                status = status
            )
        }
    }

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
        referenceDateEpochDay: Long
    ): CreditCardCockpitUiState {
        val today = LocalDate.ofEpochDay(referenceDateEpochDay)
        val creditCards = allAccounts.filter { !it.isArchived && it.groupType == AccountGroup.CREDIT_CARD }
        val creditCardIds = creditCards.map { it.id }.toSet()

        if (creditCards.isEmpty()) {
            return CreditCardCockpitUiState(activeCards = emptyList())
        }

        // 1. Total Limits & Balances
        var totalLimitMinor = 0L
        var totalDebtMinor = 0L
        for (card in creditCards) {
            totalLimitMinor += card.creditLimit?.amountMinor ?: 0L
            totalDebtMinor += card.currentBalance.abs().amountMinor
        }
        val totalLimit = MinorMoney(totalLimitMinor)
        val totalCcDebt = MinorMoney(totalDebtMinor)
        val totalAvailable = if (totalLimit > totalCcDebt) totalLimit - totalCcDebt else MinorMoney.ZERO
        val aggregateUtilization = if (totalLimit.isPositive) {
            (totalCcDebt.amountMinor.toFloat() / totalLimit.amountMinor.toFloat() * 100f).coerceIn(0f, 100f)
        } else {
            0f
        }
        val aggregateStatus = when {
            aggregateUtilization > 50f -> CreditUtilizationStatus.WARNING
            aggregateUtilization > 30f -> CreditUtilizationStatus.MODERATE
            else -> CreditUtilizationStatus.SAFE
        }

        // 2. Liquid Cash Coverage
        val liquidAccounts = allAccounts.filter {
            !it.isArchived && (
                it.groupType == AccountGroup.BANK_ACCOUNT ||
                it.groupType == AccountGroup.CASH ||
                it.groupType == AccountGroup.SAVINGS ||
                it.groupType == AccountGroup.PREPAID_WALLET
            )
        }
        var totalLiquidMinor = 0L
        for (acc in liquidAccounts) {
            if (acc.currentBalance.isPositive) {
                totalLiquidMinor += acc.currentBalance.amountMinor
            }
        }
        val totalLiquidCash = MinorMoney(totalLiquidMinor)
        val netLiquidMinor = totalLiquidMinor - totalDebtMinor
        val netLiquidCash = MinorMoney(netLiquidMinor)
        val coverageRatio = if (totalDebtMinor > 0L) {
            totalLiquidMinor.toFloat() / totalDebtMinor.toFloat()
        } else {
            999f
        }
        val liquidCoverage = LiquidCashCoverageState(
            totalLiquidCash = totalLiquidCash,
            totalCcDebt = totalCcDebt,
            netLiquidCash = netLiquidCash,
            coverageRatio = coverageRatio,
            isCashBacked = totalLiquidMinor >= totalDebtMinor
        )

        // 3. Cycle Window, Timeline & Smart Swipe
        val currentYearStartEpoch = LocalDate.of(today.year, 1, 1).toEpochDay()
        val timelineItems = mutableListOf<CreditCardTimelineItem>()
        val cardAnalyticsItems = mutableListOf<CreditCardAnalyticsItem>()
        var bestSmartSwipe: SmartSwipeRecommendation? = null
        var maxFloatDays = -1L
        var singleCardExceeding30: CreditCardAnalyticsItem? = null

        for (card in creditCards) {
            val stmtDay = (card.settlementDayOfMonth ?: 1).coerceIn(1, 31)
            val dueDay = (card.paymentDueDayOfMonth ?: 20).coerceIn(1, 31)
            val cycle = CreditCardCycleCalculator.computeCycleWindow(
                referenceDateEpochDay = referenceDateEpochDay,
                settlementDayOfMonth = stmtDay,
                paymentDueDayOfMonth = dueDay
            )

            val cardDebt = card.currentBalance.abs()
            val cardLimit = card.creditLimit ?: MinorMoney.ZERO
            val cardAvailable = if (cardLimit > cardDebt) cardLimit - cardDebt else MinorMoney.ZERO
            val cardPayable = card.creditCardStatementPayable ?: MinorMoney.ZERO
            val cardUnbilled = card.creditCardUnbilledAmount ?: MinorMoney.ZERO

            val cardUtil = if (cardLimit.isPositive) {
                (cardDebt.amountMinor.toFloat() / cardLimit.amountMinor.toFloat() * 100f).coerceIn(0f, 100f)
            } else {
                0f
            }
            val cardStatus = when {
                cardUtil > 50f -> CreditUtilizationStatus.WARNING
                cardUtil > 30f -> CreditUtilizationStatus.MODERATE
                else -> CreditUtilizationStatus.SAFE
            }

            // Float days if swiped today (prioritize cards with available credit)
            val floatDays = ChronoUnit.DAYS.between(today, cycle.activeCycleNextDueDate).coerceAtLeast(0L)
            val hasAvailableHeadroom = cardAvailable.isPositive
            if (floatDays > maxFloatDays && hasAvailableHeadroom) {
                maxFloatDays = floatDays
                bestSmartSwipe = SmartSwipeRecommendation(
                    account = card,
                    floatDaysRemaining = floatDays,
                    statementCloseDate = cycle.activeCycleEnd,
                    nextDueDate = cycle.activeCycleNextDueDate
                )
            }

            // Timeline item
            if (cardPayable.isPositive) {
                val daysRem = ChronoUnit.DAYS.between(today, cycle.lastStatementDueDate)
                timelineItems.add(
                    CreditCardTimelineItem(
                        account = card,
                        isBilled = true,
                        amountDue = cardPayable,
                        daysRemaining = daysRem,
                        targetDate = cycle.lastStatementDueDate,
                        isUrgent = daysRem in 0..3,
                        isOverdue = daysRem < 0,
                        floatDays = floatDays
                    )
                )
            } else {
                val daysRem = ChronoUnit.DAYS.between(today, cycle.activeCycleEnd)
                timelineItems.add(
                    CreditCardTimelineItem(
                        account = card,
                        isBilled = false,
                        amountDue = cardUnbilled,
                        daysRemaining = daysRem,
                        targetDate = cycle.activeCycleEnd,
                        isUrgent = false,
                        isOverdue = false,
                        floatDays = floatDays
                    )
                )
            }

            // Card YTD Spend & Top Category
            var ytdMinor = 0L
            val catSpendMap = mutableMapOf<String, Long>()
            for (tx in allTxs) {
                if (tx.accountId == card.id && tx.type == TransactionType.EXPENSE && tx.ledgerDateEpochDay >= currentYearStartEpoch) {
                    val amt = Math.abs(tx.amount.amountMinor)
                    ytdMinor += amt
                    val catName = tx.categoryName ?: "General"
                    catSpendMap[catName] = (catSpendMap[catName] ?: 0L) + amt
                }
            }
            val ytdSpend = MinorMoney(ytdMinor)
            val topCatEntry = catSpendMap.maxByOrNull { it.value }
            val topCategoryName = topCatEntry?.key
            val topCategoryPercent = if (ytdMinor > 0L && topCatEntry != null) {
                (topCatEntry.value.toFloat() / ytdMinor.toFloat() * 100f).coerceIn(0f, 100f)
            } else {
                0f
            }

            val analyticsItem = CreditCardAnalyticsItem(
                account = card,
                currentDebt = cardDebt,
                creditLimit = cardLimit,
                availableCredit = cardAvailable,
                utilizationPercent = cardUtil,
                status = cardStatus,
                billedAmount = cardPayable,
                unbilledAmount = cardUnbilled,
                ytdSpend = ytdSpend,
                topCategoryName = topCategoryName,
                topCategoryPercent = topCategoryPercent,
                floatDaysRemaining = floatDays
            )
            cardAnalyticsItems.add(analyticsItem)

            if (aggregateUtilization <= 30f && cardUtil > 30f && (singleCardExceeding30 == null || cardUtil > singleCardExceeding30.utilizationPercent)) {
                singleCardExceeding30 = analyticsItem
            }
        }

        // Fallback for smart swipe if all cards were maxed out
        if (bestSmartSwipe == null && cardAnalyticsItems.isNotEmpty()) {
            val bestItem = cardAnalyticsItems.maxByOrNull { it.floatDaysRemaining }
            if (bestItem != null) {
                val stmtDay = (bestItem.account.settlementDayOfMonth ?: 1).coerceIn(1, 31)
                val dueDay = (bestItem.account.paymentDueDayOfMonth ?: 20).coerceIn(1, 31)
                val cycle = CreditCardCycleCalculator.computeCycleWindow(referenceDateEpochDay, stmtDay, dueDay)
                bestSmartSwipe = SmartSwipeRecommendation(
                    account = bestItem.account,
                    floatDaysRemaining = bestItem.floatDaysRemaining,
                    statementCloseDate = cycle.activeCycleEnd,
                    nextDueDate = cycle.activeCycleNextDueDate
                )
            }
        }

        // Sort timeline: billed first (sorted by daysRemaining ascending), then unbilled (sorted by daysRemaining ascending)
        val sortedTimeline = timelineItems.sortedWith(
            compareBy<CreditCardTimelineItem> { !it.isBilled }
                .thenBy { it.daysRemaining }
        )

        // 4. Spend Mode Ratio (Pure EXPENSE only, excluding transfers and cc settlements)
        var creditSpendMinor = 0L
        var liquidSpendMinor = 0L
        for (tx in periodTransactions) {
            if (tx.type == TransactionType.EXPENSE) {
                val amt = Math.abs(tx.amount.amountMinor)
                if (tx.accountId in creditCardIds) {
                    creditSpendMinor += amt
                } else {
                    liquidSpendMinor += amt
                }
            }
        }
        val totalExpenseMinor = creditSpendMinor + liquidSpendMinor
        val creditPct = if (totalExpenseMinor > 0L) (creditSpendMinor.toFloat() / totalExpenseMinor.toFloat() * 100f) else 0f
        val liquidPct = if (totalExpenseMinor > 0L) (liquidSpendMinor.toFloat() / totalExpenseMinor.toFloat() * 100f) else 0f
        val spendRatio = CreditSpendRatio(
            creditSpend = MinorMoney(creditSpendMinor),
            liquidSpend = MinorMoney(liquidSpendMinor),
            creditPercentage = creditPct,
            liquidPercentage = liquidPct
        )

        // 5. 6-Month Historical Credit Spend Trend
        val creditTrendBars = trendBars.map { bar ->
            var barSpendMinor = 0L
            for (tx in allTxs) {
                if (tx.ledgerDateEpochDay in bar.startEpochDay..bar.endEpochDay &&
                    tx.type == TransactionType.EXPENSE &&
                    tx.accountId in creditCardIds
                ) {
                    barSpendMinor += Math.abs(tx.amount.amountMinor)
                }
            }
            MonthlyTrendBar(
                periodLabel = bar.periodLabel,
                startEpochDay = bar.startEpochDay,
                endEpochDay = bar.endEpochDay,
                totalIncome = MinorMoney.ZERO,
                totalExpense = MinorMoney(barSpendMinor),
                netSavings = MinorMoney(-barSpendMinor)
            )
        }

        return CreditCardCockpitUiState(
            totalCreditLimit = totalLimit,
            totalCcDebt = totalCcDebt,
            totalAvailableCredit = totalAvailable,
            aggregateUtilizationPercent = aggregateUtilization,
            aggregateStatus = aggregateStatus,
            singleCardExceeding30 = singleCardExceeding30,
            liquidCoverage = liquidCoverage,
            smartSwipeRecommendation = bestSmartSwipe,
            timelineItems = sortedTimeline,
            cardAnalyticsItems = cardAnalyticsItems,
            spendRatio = spendRatio,
            sixMonthCreditTrendBars = creditTrendBars,
            activeCards = creditCards
        )
    }

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
}

object StatsPredictiveCalculations {

    fun computeNetWorthVelocity(
        netWorthTrend: List<NetWorthGrowthTrendItem>
    ): NetWorthVelocityState {
        if (netWorthTrend.isEmpty()) {
            return NetWorthVelocityState()
        }
        val currentItem = netWorthTrend.last()
        val currentNW = currentItem.netWorth
        val previousItem = if (netWorthTrend.size >= 2) netWorthTrend[netWorthTrend.size - 2] else currentItem
        val previousNW = previousItem.netWorth
        val absChange = currentNW - previousNW
        val isPositive = absChange.amountMinor >= 0L
        val pct = if (previousNW.amountMinor != 0L) {
            (absChange.amountMinor.toFloat() / Math.abs(previousNW.amountMinor).toFloat()) * 100f
        } else {
            if (absChange.amountMinor > 0L) 100f else if (absChange.amountMinor < 0L) -100f else 0f
        }
        return NetWorthVelocityState(
            currentNetWorth = currentNW,
            previousMonthNetWorth = previousNW,
            absoluteChange = absChange,
            percentageChange = pct,
            isPositive = isPositive
        )
    }

    fun computeEmergencyRunway(
        allAccounts: List<AccountSummary>,
        trendBars: List<MonthlyTrendBar>,
        primaryCurrencyCode: String,
        secondaryCurrencies: List<SecondaryCurrency>,
        includesInvestments: Boolean
    ): EmergencyRunwayState {
        val primarySpec = SupportedCurrencies.fromCode(primaryCurrencyCode)
        val secondaryMap = secondaryCurrencies.associateBy { it.code.trim().uppercase() }

        var totalLiquidMinor = 0L
        var totalInvestmentsMinor = 0L

        for (acc in allAccounts) {
            if (acc.isArchived || acc.currentBalance.amountMinor <= 0L) continue

            val isLiquid = acc.groupType == AccountGroup.BANK_ACCOUNT ||
                    acc.groupType == AccountGroup.CASH ||
                    acc.groupType == AccountGroup.SAVINGS ||
                    acc.groupType == AccountGroup.PREPAID_WALLET
            val isInvestment = acc.groupType == AccountGroup.INVESTMENT

            if (!isLiquid && !isInvestment) continue

            val rawBalanceMinor = acc.currentBalance.amountMinor
            val accountCurrency = acc.currencyCode.trim().uppercase()
            val convertedBalanceMinor = if (accountCurrency == primarySpec.code.uppercase()) {
                rawBalanceMinor
            } else {
                val secondary = secondaryMap[accountCurrency]
                if (secondary != null && secondary.exchangeRateToPrimary > 0.0) {
                    val accSpec = SupportedCurrencies.fromCode(accountCurrency)
                    MoneyFormatterAndCalculator.convertMinorMoney(
                        sourceMinor = rawBalanceMinor,
                        sourceFractionDigits = accSpec.fractionDigits,
                        targetFractionDigits = primarySpec.fractionDigits,
                        rateToTarget = secondary.exchangeRateToPrimary
                    )
                } else {
                    rawBalanceMinor
                }
            }

            if (isLiquid) {
                totalLiquidMinor = Math.addExact(totalLiquidMinor, convertedBalanceMinor)
            } else if (isInvestment) {
                totalInvestmentsMinor = Math.addExact(totalInvestmentsMinor, convertedBalanceMinor)
            }
        }

        val positiveExpenseMonths = trendBars.map { it.totalExpense.amountMinor }.filter { it > 0L }
        val averageBurnMinor = if (positiveExpenseMonths.isNotEmpty()) {
            positiveExpenseMonths.average().toLong()
        } else if (trendBars.isNotEmpty()) {
            trendBars.map { it.totalExpense.amountMinor }.average().toLong()
        } else {
            0L
        }

        val applicableFundsMinor = if (includesInvestments) {
            Math.addExact(totalLiquidMinor, totalInvestmentsMinor)
        } else {
            totalLiquidMinor
        }

        val runwayMonths = if (averageBurnMinor > 0L) {
            (applicableFundsMinor.toFloat() / averageBurnMinor.toFloat()).coerceIn(0f, 99.9f)
        } else {
            if (applicableFundsMinor > 0L) 99.9f else 0f
        }

        val healthTier = when {
            runwayMonths >= 6.0f -> RunwayHealthTier.RESILIENT
            runwayMonths >= 3.0f -> RunwayHealthTier.ADEQUATE
            else -> RunwayHealthTier.VULNERABLE
        }

        return EmergencyRunwayState(
            totalLiquidFunds = MinorMoney(totalLiquidMinor),
            totalInvestmentFunds = MinorMoney(totalInvestmentsMinor),
            averageMonthlyBurnMinor = averageBurnMinor,
            runwayMonths = runwayMonths,
            healthTier = healthTier,
            targetMonths = 6.0f,
            includesInvestments = includesInvestments
        )
    }

    fun computePayeeDrillDownState(
        payeeName: String,
        allTxs: List<TransactionItem>,
        trendBars: List<MonthlyTrendBar>,
        startEpochDay: Long,
        endEpochDay: Long,
        isAllTime: Boolean,
        primaryCurrencyCode: String
    ): PayeeDrillDownState {
        val trimmedTarget = payeeName.trim().lowercase()

        val allMatchingTxs = allTxs.filter { tx ->
            val raw = tx.payeeOrDescription.trim().ifEmpty { tx.note.trim() }.lowercase()
            raw == trimmedTarget && (tx.type == TransactionType.EXPENSE || tx.type == TransactionType.REFUND)
        }

        val periodMatchingTxs = if (isAllTime) {
            allMatchingTxs
        } else {
            allMatchingTxs.filter { it.ledgerDateEpochDay in startEpochDay..endEpochDay }
        }

        var totalPeriodMinor = 0L
        var periodVisitCount = 0
        for (tx in periodMatchingTxs) {
            if (tx.type == TransactionType.EXPENSE) {
                totalPeriodMinor += Math.abs(tx.amount.amountMinor) + Math.abs(tx.transferFee.amountMinor)
                periodVisitCount++
            } else if (tx.type == TransactionType.REFUND) {
                totalPeriodMinor -= Math.abs(tx.amount.amountMinor)
            }
        }
        totalPeriodMinor = Math.max(0L, totalPeriodMinor)

        var totalAllTimeMinor = 0L
        var allTimeVisitCount = 0
        for (tx in allMatchingTxs) {
            if (tx.type == TransactionType.EXPENSE) {
                totalAllTimeMinor += Math.abs(tx.amount.amountMinor) + Math.abs(tx.transferFee.amountMinor)
                allTimeVisitCount++
            } else if (tx.type == TransactionType.REFUND) {
                totalAllTimeMinor -= Math.abs(tx.amount.amountMinor)
            }
        }
        totalAllTimeMinor = Math.max(0L, totalAllTimeMinor)

        val averageTicketMinor = if (periodVisitCount > 0) {
            totalPeriodMinor / periodVisitCount
        } else if (allTimeVisitCount > 0) {
            totalAllTimeMinor / allTimeVisitCount
        } else {
            0L
        }

        // Category distribution for this merchant
        val catAmountMap = mutableMapOf<String, Long>()
        val catCountMap = mutableMapOf<String, Int>()
        val catColorMap = mutableMapOf<String, String>()
        val catIconMap = mutableMapOf<String, String>()

        val activeForCategories = if (periodMatchingTxs.isNotEmpty()) periodMatchingTxs else allMatchingTxs
        for (tx in activeForCategories) {
            val catName = tx.categoryName ?: "General"
            val amt = Math.abs(tx.amount.amountMinor)
            catAmountMap[catName] = (catAmountMap[catName] ?: 0L) + amt
            catCountMap[catName] = (catCountMap[catName] ?: 0) + 1
            if (tx.categoryColorHex != null) catColorMap[catName] = tx.categoryColorHex
            if (tx.categoryIconName != null) catIconMap[catName] = tx.categoryIconName
        }

        val grandCategoryMinor = catAmountMap.values.sum().coerceAtLeast(1L)
        val catDistribution = catAmountMap.map { (catName, minorSum) ->
            CategorySpendStat(
                categoryId = 0L,
                categoryName = catName,
                colorHex = catColorMap[catName] ?: "#8B5CF6",
                iconName = catIconMap[catName] ?: "category",
                netAmount = MinorMoney(minorSum),
                percentageOfTotal = (minorSum.toFloat() / grandCategoryMinor.toFloat() * 100f).coerceIn(0f, 100f),
                transactionCount = catCountMap[catName] ?: 1
            )
        }.sortedByDescending { it.netAmount }

        // 6-month monthly spend bars for this merchant
        val payeeMonthlyBars = trendBars.map { bar ->
            var barExpenseMinor = 0L
            for (tx in allMatchingTxs) {
                if (tx.ledgerDateEpochDay in bar.startEpochDay..bar.endEpochDay) {
                    if (tx.type == TransactionType.EXPENSE) {
                        barExpenseMinor += Math.abs(tx.amount.amountMinor) + Math.abs(tx.transferFee.amountMinor)
                    } else if (tx.type == TransactionType.REFUND) {
                        barExpenseMinor -= Math.abs(tx.amount.amountMinor)
                    }
                }
            }
            val safeMinor = Math.max(0L, barExpenseMinor)
            MonthlyTrendBar(
                periodLabel = bar.periodLabel,
                startEpochDay = bar.startEpochDay,
                endEpochDay = bar.endEpochDay,
                totalIncome = MinorMoney.ZERO,
                totalExpense = MinorMoney(safeMinor),
                netSavings = MinorMoney(-safeMinor)
            )
        }

        val sortedTransactions = activeForCategories.sortedWith(
            compareByDescending<TransactionItem> { it.ledgerDateEpochDay }
                .thenByDescending { it.timestampEpochMillis }
        )

        return PayeeDrillDownState(
            payeeName = payeeName,
            totalPeriodSpend = MinorMoney(totalPeriodMinor),
            totalAllTimeSpend = MinorMoney(totalAllTimeMinor),
            periodVisitCount = periodVisitCount,
            allTimeVisitCount = allTimeVisitCount,
            averageTicketSize = MinorMoney(averageTicketMinor),
            categoryDistribution = catDistribution,
            monthlyTrendBars = payeeMonthlyBars,
            transactions = sortedTransactions
        )
    }

    fun computeTagProjectDrillDownState(
        tagName: String,
        allTxs: List<TransactionItem>,
        trendBars: List<MonthlyTrendBar>,
        startEpochDay: Long,
        endEpochDay: Long,
        isAllTime: Boolean,
        primaryCurrencyCode: String,
        secondaryCurrencies: List<SecondaryCurrency> = emptyList()
    ): TagProjectDrillDownState {
        val cleanTargetTag = tagName.removePrefix("#").trim().lowercase()

        val allMatchingTxs = allTxs.filter { tx ->
            tx.tags.any { it.removePrefix("#").trim().lowercase() == cleanTargetTag }
        }

        val periodMatchingTxs = if (isAllTime) {
            allMatchingTxs
        } else {
            val inPeriod = allMatchingTxs.filter { it.ledgerDateEpochDay in startEpochDay..endEpochDay }
            if (inPeriod.isNotEmpty()) inPeriod else allMatchingTxs
        }

        val primarySpec = SupportedCurrencies.fromCode(primaryCurrencyCode)
        val secondaryMap = secondaryCurrencies.associateBy { it.code.trim().uppercase() }

        fun convertToPrimary(rawAmountMinor: Long, currencyCode: String): Long {
            val accCurr = currencyCode.trim().uppercase()
            if (accCurr == primarySpec.code.uppercase()) return rawAmountMinor
            val secondary = secondaryMap[accCurr]
            return if (secondary != null && secondary.exchangeRateToPrimary > 0.0) {
                val accSpec = SupportedCurrencies.fromCode(accCurr)
                MoneyFormatterAndCalculator.convertMinorMoney(
                    sourceMinor = rawAmountMinor,
                    sourceFractionDigits = accSpec.fractionDigits,
                    targetFractionDigits = primarySpec.fractionDigits,
                    rateToTarget = secondary.exchangeRateToPrimary
                )
            } else {
                rawAmountMinor
            }
        }

        val isReimbursableKeyword = cleanTargetTag.contains("reimburs") ||
                cleanTargetTag.contains("claim") ||
                cleanTargetTag.contains("split") ||
                cleanTargetTag.contains("office") ||
                cleanTargetTag.contains("work") ||
                cleanTargetTag.contains("bill") ||
                cleanTargetTag.contains("owed") ||
                cleanTargetTag.contains("due")

        val hasInflowTransactions = allMatchingTxs.any {
            it.type == TransactionType.REFUND || it.type == TransactionType.INCOME
        }

        val isReimbursableProject = isReimbursableKeyword || hasInflowTransactions

        var totalSpentMinor = 0L
        var totalReimbursedMinor = 0L
        var minEpochDay: Long? = null
        var maxEpochDay: Long? = null

        val activeTxs = if (periodMatchingTxs.isNotEmpty()) periodMatchingTxs else allMatchingTxs

        for (tx in activeTxs) {
            val convertedAmount = convertToPrimary(Math.abs(tx.amount.amountMinor), tx.currencyCode)
            val convertedFee = convertToPrimary(Math.abs(tx.transferFee.amountMinor), tx.currencyCode)

            when (tx.type) {
                TransactionType.EXPENSE -> {
                    totalSpentMinor += convertedAmount + convertedFee
                    val d = tx.ledgerDateEpochDay
                    minEpochDay = if (minEpochDay == null) d else minOf(minEpochDay, d)
                    maxEpochDay = if (maxEpochDay == null) d else maxOf(maxEpochDay, d)
                }
                TransactionType.REFUND, TransactionType.INCOME -> {
                    totalReimbursedMinor += convertedAmount
                    val d = tx.ledgerDateEpochDay
                    minEpochDay = if (minEpochDay == null) d else minOf(minEpochDay, d)
                    maxEpochDay = if (maxEpochDay == null) d else maxOf(maxEpochDay, d)
                }
                TransactionType.TRANSFER, TransactionType.CC_SETTLEMENT -> {
                    if (convertedFee > 0L) {
                        totalSpentMinor += convertedFee
                    }
                }
                TransactionType.OPENING_BALANCE, TransactionType.RECONCILIATION -> {}
            }
        }

        val netPendingOwedMinor = Math.max(0L, totalSpentMinor - totalReimbursedMinor)
        val isFullySettled = totalSpentMinor > 0L && netPendingOwedMinor == 0L

        val dateSpanDays = if (minEpochDay != null && maxEpochDay != null) {
            (maxEpochDay - minEpochDay + 1).toInt().coerceAtLeast(1)
        } else {
            1
        }

        val dailyAverageSpendMinor = if (dateSpanDays > 0) {
            totalSpentMinor / dateSpanDays
        } else {
            totalSpentMinor
        }

        // Category distribution for this project
        val catAmountMap = mutableMapOf<String, Long>()
        val catCountMap = mutableMapOf<String, Int>()
        val catColorMap = mutableMapOf<String, String>()
        val catIconMap = mutableMapOf<String, String>()

        for (tx in activeTxs) {
            if (tx.type == TransactionType.EXPENSE) {
                val catName = tx.categoryName ?: "General"
                val amt = convertToPrimary(Math.abs(tx.amount.amountMinor), tx.currencyCode)
                catAmountMap[catName] = (catAmountMap[catName] ?: 0L) + amt
                catCountMap[catName] = (catCountMap[catName] ?: 0) + 1
                if (tx.categoryColorHex != null) catColorMap[catName] = tx.categoryColorHex
                if (tx.categoryIconName != null) catIconMap[catName] = tx.categoryIconName
            }
        }

        val grandCategoryMinor = catAmountMap.values.sum().coerceAtLeast(1L)
        val catDistribution = catAmountMap.map { (catName, minorSum) ->
            CategorySpendStat(
                categoryId = 0L,
                categoryName = catName,
                colorHex = catColorMap[catName] ?: "#8B5CF6",
                iconName = catIconMap[catName] ?: "category",
                netAmount = MinorMoney(minorSum),
                percentageOfTotal = (minorSum.toFloat() / grandCategoryMinor.toFloat() * 100f).coerceIn(0f, 100f),
                transactionCount = catCountMap[catName] ?: 1
            )
        }.sortedByDescending { it.netAmount }

        // 6-month monthly spend bars for this tag
        val tagMonthlyBars = trendBars.map { bar ->
            var barExpenseMinor = 0L
            for (tx in allMatchingTxs) {
                if (tx.ledgerDateEpochDay in bar.startEpochDay..bar.endEpochDay) {
                    val amt = convertToPrimary(Math.abs(tx.amount.amountMinor), tx.currencyCode)
                    if (tx.type == TransactionType.EXPENSE) {
                        val fee = convertToPrimary(Math.abs(tx.transferFee.amountMinor), tx.currencyCode)
                        barExpenseMinor += amt + fee
                    } else if (tx.type == TransactionType.REFUND) {
                        barExpenseMinor -= amt
                    }
                }
            }
            val safeMinor = Math.max(0L, barExpenseMinor)
            MonthlyTrendBar(
                periodLabel = bar.periodLabel,
                startEpochDay = bar.startEpochDay,
                endEpochDay = bar.endEpochDay,
                totalIncome = MinorMoney.ZERO,
                totalExpense = MinorMoney(safeMinor),
                netSavings = MinorMoney(-safeMinor)
            )
        }

        val sortedTransactions = activeTxs.sortedWith(
            compareByDescending<TransactionItem> { it.ledgerDateEpochDay }
                .thenByDescending { it.timestampEpochMillis }
        )

        return TagProjectDrillDownState(
            tagName = tagName,
            isReimbursableProject = isReimbursableProject,
            totalSpent = MinorMoney(totalSpentMinor),
            totalReimbursed = MinorMoney(totalReimbursedMinor),
            netPendingOwed = MinorMoney(netPendingOwedMinor),
            isFullySettled = isFullySettled,
            dateSpanDays = dateSpanDays,
            minEpochDay = minEpochDay,
            maxEpochDay = maxEpochDay,
            dailyAverageSpend = MinorMoney(dailyAverageSpendMinor),
            categoryDistribution = catDistribution,
            monthlyTrendBars = tagMonthlyBars,
            transactions = sortedTransactions
        )
    }

    fun projectRecurringOccurrencesInHorizon(
        startDay: Long,
        frequency: RepeatFrequency,
        todayEpochDay: Long,
        horizonEpochDay: Long
    ): List<Long> {
        val occurrences = mutableListOf<Long>()
        var cursor = LocalDate.ofEpochDay(startDay)

        // Advance cursor until it reaches or exceeds today
        while (cursor.toEpochDay() < todayEpochDay) {
            cursor = when (frequency) {
                RepeatFrequency.DAILY -> cursor.plusDays(1)
                RepeatFrequency.WEEKLY -> cursor.plusWeeks(1)
                RepeatFrequency.BIWEEKLY -> cursor.plusWeeks(2)
                RepeatFrequency.MONTHLY -> cursor.plusMonths(1)
                RepeatFrequency.YEARLY -> cursor.plusYears(1)
            }
        }

        while (cursor.toEpochDay() <= horizonEpochDay) {
            occurrences.add(cursor.toEpochDay())
            cursor = when (frequency) {
                RepeatFrequency.DAILY -> cursor.plusDays(1)
                RepeatFrequency.WEEKLY -> cursor.plusWeeks(1)
                RepeatFrequency.BIWEEKLY -> cursor.plusWeeks(2)
                RepeatFrequency.MONTHLY -> cursor.plusMonths(1)
                RepeatFrequency.YEARLY -> cursor.plusYears(1)
            }
        }

        return occurrences
    }

    fun computeSafeToSpend(
        allAccounts: List<AccountSummary>,
        recurringRules: List<RecurringCommitmentItem>,
        futureTransactions: List<TransactionItem>,
        todayEpochDay: Long,
        periodStartEpochDay: Long,
        periodEndEpochDay: Long,
        primaryCurrencyCode: String,
        secondaryCurrencies: List<SecondaryCurrency> = emptyList()
    ): SafeToSpendState {
        val primarySpec = SupportedCurrencies.fromCode(primaryCurrencyCode)
        val secondaryMap = secondaryCurrencies.associateBy { it.code.trim().uppercase() }

        fun convertToPrimary(rawAmountMinor: Long, currencyCode: String): Long {
            val accCurr = currencyCode.trim().uppercase()
            if (accCurr == primarySpec.code.uppercase()) return rawAmountMinor
            val secondary = secondaryMap[accCurr]
            return if (secondary != null && secondary.exchangeRateToPrimary > 0.0) {
                val accSpec = SupportedCurrencies.fromCode(accCurr)
                MoneyFormatterAndCalculator.convertMinorMoney(
                    sourceMinor = rawAmountMinor,
                    sourceFractionDigits = accSpec.fractionDigits,
                    targetFractionDigits = primarySpec.fractionDigits,
                    rateToTarget = secondary.exchangeRateToPrimary
                )
            } else {
                rawAmountMinor
            }
        }

        // 1. Total Liquid Cash & Bank Balances
        var totalLiquidMinor = 0L
        for (acc in allAccounts) {
            if (acc.isArchived || acc.currentBalance.amountMinor <= 0L) continue
            val isLiquid = acc.groupType == AccountGroup.BANK_ACCOUNT ||
                    acc.groupType == AccountGroup.CASH ||
                    acc.groupType == AccountGroup.SAVINGS ||
                    acc.groupType == AccountGroup.PREPAID_WALLET
            if (isLiquid) {
                totalLiquidMinor += convertToPrimary(acc.currentBalance.amountMinor, acc.currencyCode)
            }
        }

        // 2. Total Credit Card Debt (Billed + Unbilled)
        var totalCcDebtMinor = 0L
        for (acc in allAccounts) {
            if (acc.isArchived || acc.groupType != AccountGroup.CREDIT_CARD) continue
            val debtMinor = acc.currentBalance.abs().amountMinor
            totalCcDebtMinor += convertToPrimary(debtMinor, acc.currencyCode)
        }

        // 3. Upcoming 14-Day Commitments
        val horizonEpochDay = todayEpochDay + 14L
        var upcomingCommitmentsMinor = 0L

        // a. Recurring Rules
        for (rule in recurringRules) {
            if (!rule.isActive) continue
            if (rule.type != TransactionType.EXPENSE && rule.type != TransactionType.TRANSFER) continue

            val occurrences = projectRecurringOccurrencesInHorizon(
                startDay = rule.nextOccurrenceEpochDay,
                frequency = rule.frequency,
                todayEpochDay = todayEpochDay,
                horizonEpochDay = horizonEpochDay
            )

            val singleAmt = convertToPrimary(rule.amount.amountMinor, rule.currencyCode)
            upcomingCommitmentsMinor += (singleAmt * occurrences.size)
        }

        // b. Future Scheduled Transactions
        for (tx in futureTransactions) {
            if (tx.ledgerDateEpochDay in (todayEpochDay + 1L)..horizonEpochDay) {
                if (tx.type == TransactionType.EXPENSE) {
                    val amt = convertToPrimary(Math.abs(tx.amount.amountMinor) + Math.abs(tx.transferFee.amountMinor), tx.currencyCode)
                    upcomingCommitmentsMinor += amt
                }
            }
        }

        val trueSafeToSpendMinor = totalLiquidMinor - totalCcDebtMinor - upcomingCommitmentsMinor
        val isPositive = trueSafeToSpendMinor >= 0L
        val isCurrentPeriod = todayEpochDay in periodStartEpochDay..periodEndEpochDay

        return SafeToSpendState(
            totalLiquidBankAndCash = MinorMoney(totalLiquidMinor),
            totalCreditCardDebt = MinorMoney(totalCcDebtMinor),
            upcoming14DayCommitments = MinorMoney(upcomingCommitmentsMinor),
            trueSafeToSpend = MinorMoney(trueSafeToSpendMinor),
            isPositive = isPositive,
            isCurrentPeriod = isCurrentPeriod
        )
    }

    fun computeShortfallAlerts(
        allAccounts: List<AccountSummary>,
        recurringRules: List<RecurringCommitmentItem>,
        futureTransactions: List<TransactionItem>,
        todayEpochDay: Long,
        primaryCurrencyCode: String,
        secondaryCurrencies: List<SecondaryCurrency> = emptyList()
    ): List<AccountShortfallAlert> {
        val primarySpec = SupportedCurrencies.fromCode(primaryCurrencyCode)
        val secondaryMap = secondaryCurrencies.associateBy { it.code.trim().uppercase() }

        fun convertToPrimary(rawAmountMinor: Long, currencyCode: String): Long {
            val accCurr = currencyCode.trim().uppercase()
            if (accCurr == primarySpec.code.uppercase()) return rawAmountMinor
            val secondary = secondaryMap[accCurr]
            return if (secondary != null && secondary.exchangeRateToPrimary > 0.0) {
                val accSpec = SupportedCurrencies.fromCode(accCurr)
                MoneyFormatterAndCalculator.convertMinorMoney(
                    sourceMinor = rawAmountMinor,
                    sourceFractionDigits = accSpec.fractionDigits,
                    targetFractionDigits = primarySpec.fractionDigits,
                    rateToTarget = secondary.exchangeRateToPrimary
                )
            } else {
                rawAmountMinor
            }
        }

        val horizonEpochDay = todayEpochDay + 14L
        val debitsByAccount = mutableMapOf<Long, Long>()
        val earliestDebitByAccount = mutableMapOf<Long, Long>()

        // 1. Recurring Rules
        for (rule in recurringRules) {
            if (!rule.isActive) continue
            if (rule.type != TransactionType.EXPENSE && rule.type != TransactionType.TRANSFER) continue

            val occurrences = projectRecurringOccurrencesInHorizon(
                startDay = rule.nextOccurrenceEpochDay,
                frequency = rule.frequency,
                todayEpochDay = todayEpochDay,
                horizonEpochDay = horizonEpochDay
            )

            val singleAmt = convertToPrimary(rule.amount.amountMinor, rule.currencyCode)
            for (occDay in occurrences) {
                debitsByAccount[rule.accountId] = (debitsByAccount[rule.accountId] ?: 0L) + singleAmt
                val curr = earliestDebitByAccount[rule.accountId]
                earliestDebitByAccount[rule.accountId] = if (curr == null) occDay else minOf(curr, occDay)
            }
        }

        // 2. Future Scheduled Transactions
        for (tx in futureTransactions) {
            if (tx.ledgerDateEpochDay in (todayEpochDay + 1L)..horizonEpochDay) {
                if (tx.type == TransactionType.EXPENSE) {
                    val amt = convertToPrimary(Math.abs(tx.amount.amountMinor) + Math.abs(tx.transferFee.amountMinor), tx.currencyCode)
                    debitsByAccount[tx.accountId] = (debitsByAccount[tx.accountId] ?: 0L) + amt
                    val curr = earliestDebitByAccount[tx.accountId]
                    earliestDebitByAccount[tx.accountId] = if (curr == null) tx.ledgerDateEpochDay else minOf(curr, tx.ledgerDateEpochDay)
                }
            }
        }

        // 3. Match against live liquid account balances
        val alerts = mutableListOf<AccountShortfallAlert>()
        for (acc in allAccounts) {
            if (acc.isArchived) continue
            val isLiquidBank = acc.groupType == AccountGroup.BANK_ACCOUNT ||
                    acc.groupType == AccountGroup.SAVINGS ||
                    acc.groupType == AccountGroup.PREPAID_WALLET
            if (!isLiquidBank) continue

            val scheduledDebitsMinor = debitsByAccount[acc.id] ?: 0L
            if (scheduledDebitsMinor <= 0L) continue

            val liveBalanceMinor = convertToPrimary(acc.currentBalance.amountMinor, acc.currencyCode)
            if (liveBalanceMinor < scheduledDebitsMinor) {
                val shortfallMinor = scheduledDebitsMinor - liveBalanceMinor
                val earliestDay = earliestDebitByAccount[acc.id] ?: todayEpochDay
                val daysUntil = maxOf(0L, earliestDay - todayEpochDay)

                alerts.add(
                    AccountShortfallAlert(
                        accountId = acc.id,
                        accountName = acc.name,
                        currentBalance = acc.currentBalance,
                        upcomingDebits = MinorMoney(scheduledDebitsMinor),
                        shortfallAmount = MinorMoney(shortfallMinor),
                        daysUntilShortfall = daysUntil
                    )
                )
            }
        }

        return alerts.sortedBy { it.daysUntilShortfall }
    }
}
