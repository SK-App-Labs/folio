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
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
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
    STATS("Stats"),
    BUDGETS("Budgets"),
    TAGS_AND_NOTES("Tags & Notes")
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
    val matchingBudget: BudgetProgress?
)

@Immutable
data class StatsAndBudgetsUiState(
    val selectedSubTab: StatsSubTab = StatsSubTab.STATS,
    val periodGranularity: StatsPeriodGranularity = StatsPeriodGranularity.MONTHLY,
    val selectedCategoryType: CategoryType = CategoryType.EXPENSE,
    val selectedRankingMode: RankingMode = RankingMode.TAGS,
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
    val totalAvailableCredit: MinorMoney = MinorMoney.ZERO,
    val totalCreditLimit: MinorMoney = MinorMoney.ZERO,
    val totalUsedCredit: MinorMoney = MinorMoney.ZERO,
    val overallCreditUtilizationPercent: Float = 0f,
    val creditCardHealthList: List<CreditCardHealthUiModel> = emptyList(),
    val budgetProgressList: List<BudgetProgress> = emptyList(),
    val overallBudgetProgress: BudgetProgress? = null,
    val categoryBudgetProgressList: List<BudgetProgress> = emptyList(),
    val expenseCategoryTree: List<CategoryNode> = emptyList(),
    val incomeCategoryTree: List<CategoryNode> = emptyList(),
    val tagRankings: List<SpendRankingItem> = emptyList(),
    val payeeOrNoteRankings: List<SpendRankingItem> = emptyList(),
    val drillDownState: CategoryDrillDownState? = null,
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
    val selectedTrendChartMode: TrendChartMode = TrendChartMode.INCOME_VS_EXPENSE,
    val selectedNetWorthMetricFilter: NetWorthChartMetricFilter = NetWorthChartMetricFilter.ALL_IN_ONE,
    val referenceDateEpochDay: Long = LocalDate.now().toEpochDay(),
    val customStartEpochDay: Long = LocalDate.now().withDayOfMonth(1).toEpochDay(),
    val customEndEpochDay: Long = LocalDate.now().toEpochDay(),
    val isCustomDatePickerOpen: Boolean = false,
    val highlightedDonutCategoryId: Long? = null,
    val drillDownCategoryId: Long? = null,
    val budgetEditorDialogState: BudgetEditorDialogState? = null,
    val isSavingBudget: Boolean = false,
    val snackbarMessage: String? = null
)

private data class PeriodQueryParameters(
    val granularity: StatsPeriodGranularity,
    val referenceDateEpochDay: Long,
    val customStartEpochDay: Long,
    val customEndEpochDay: Long,
    val prefs: UserPreferences
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
    val periodTransactions: List<TransactionItem>
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
        val windowStartEpochDay = refDate.minusMonths(7).withDayOfMonth(1).toEpochDay()
        val windowEndEpochDay = refDate.plusMonths(13).toEpochDay()
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
            )
        ) { trendBars, budgets, periodTxns ->
            Triple(trendBars, budgets, periodTxns)
        }

        val rankingsFlow = combine(
            analyticsAndBudgetRepository.observeTagSpendRankings(
                startEpochDay = periodRange.startEpochDay,
                endEpochDay = periodRange.endEpochDay
            ),
            analyticsAndBudgetRepository.observePayeeOrNoteRankings(
                startEpochDay = periodRange.startEpochDay,
                endEpochDay = periodRange.endEpochDay
            )
        ) { tags, payees ->
            tags to payees
        }

        combine(
            summaryAndCategoryStatsFlow,
            trendsAndBudgetsFlow,
            rankingsFlow
        ) { (summary, expenseStats, incomeStats), (trendBars, budgets, periodTxns), (tags, payees) ->
            PeriodAnalyticsBundle(
                prefs = prefs,
                periodRange = periodRange,
                periodSummary = summary,
                expenseStats = expenseStats,
                incomeStats = incomeStats,
                trendBars = trendBars,
                budgets = budgets,
                tagRankings = tags,
                payeeRankings = payees,
                periodTransactions = periodTxns
            )
        }
    }

    val uiState: StateFlow<StatsAndBudgetsUiState> = combine(
        controlState,
        periodAnalyticsFlow,
        categoryTreesFlow,
        allAccountsFlow,
        netWorthWindowTxsFlow
    ) { ctrl, bundle, (expenseTree, incomeTree), allAccounts, allTxs ->
        val prefs = bundle.prefs
        val currencySpec = SupportedCurrencies.fromCode(prefs.primaryCurrencyCode)

        val netWorthTrend = computeSixMonthNetWorthTrend(
            allAccounts = allAccounts,
            allTransactions = allTxs,
            referenceDateEpochDay = ctrl.referenceDateEpochDay
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
                CategoryDrillDownState(
                    categoryStat = stat,
                    categoryType = ctrl.selectedCategoryType,
                    transactions = matchingTxns,
                    matchingBudget = matchingBudget
                )
            } else {
                null
            }
        }

        StatsAndBudgetsUiState(
            selectedSubTab = ctrl.selectedSubTab,
            periodGranularity = ctrl.periodGranularity,
            selectedCategoryType = ctrl.selectedCategoryType,
            selectedRankingMode = ctrl.selectedRankingMode,
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
            totalAvailableCredit = totalAvailable,
            totalCreditLimit = totalLimit,
            totalUsedCredit = totalUsed,
            overallCreditUtilizationPercent = overallUtilization,
            creditCardHealthList = creditHealthList,
            budgetProgressList = bundle.budgets,
            overallBudgetProgress = overallBudget,
            categoryBudgetProgressList = categoryBudgets,
            expenseCategoryTree = expenseTree,
            incomeCategoryTree = incomeTree,
            tagRankings = bundle.tagRankings,
            payeeOrNoteRankings = bundle.payeeRankings,
            drillDownState = drillDownState,
            budgetEditorDialogState = ctrl.budgetEditorDialogState,
            isSavingBudget = ctrl.isSavingBudget,
            snackbarMessage = ctrl.snackbarMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = StatsAndBudgetsUiState()
    )

    fun selectSubTab(subTab: StatsSubTab) {
        controlState.update { it.copy(selectedSubTab = subTab) }
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

    private fun computeSixMonthNetWorthTrend(
        allAccounts: List<AccountSummary>,
        allTransactions: List<TransactionItem>,
        referenceDateEpochDay: Long
    ): List<NetWorthGrowthTrendItem> {
        val refLocalDate = LocalDate.ofEpochDay(referenceDateEpochDay)
        val shortMonthFormatter = DateTimeFormatter.ofPattern("MMM yyyy", Locale.US)
        val trendItems = ArrayList<NetWorthGrowthTrendItem>(6)

        val activeAccounts = allAccounts.filter { !it.isArchived && it.includeInNetWorth && it.groupType != AccountGroup.DEBIT_CARD }

        for (monthOffset in 5 downTo 0) {
            val targetDate = refLocalDate.minusMonths(monthOffset.toLong())
            val targetYearMonth = YearMonth.from(targetDate)
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
}