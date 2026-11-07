// File: app/src/main/java/com/folioledger/app/ui/ledger/LedgerHomeViewModel.kt
package com.folioledger.app.ui.ledger

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.folioledger.app.R
import com.folioledger.app.domain.model.AccountSummary
import com.folioledger.app.domain.model.BudgetProgress
import com.folioledger.app.domain.model.CategoryNode
import com.folioledger.app.domain.model.CategorySpendStat
import com.folioledger.app.domain.model.CategoryType
import com.folioledger.app.domain.model.CurrencySpec
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.NumberGroupingSystem
import com.folioledger.app.domain.model.PeriodLedgerSummary
import com.folioledger.app.domain.model.SecondaryCurrency
import com.folioledger.app.domain.model.SupportedCurrencies
import com.folioledger.app.domain.model.TransactionBookmark
import com.folioledger.app.domain.model.TransactionItem
import com.folioledger.app.domain.model.TransactionSearchFilter
import com.folioledger.app.domain.model.TransactionType
import com.folioledger.app.domain.model.UserPreferences
import com.folioledger.app.domain.repository.AccountRepository
import com.folioledger.app.domain.repository.AnalyticsAndBudgetRepository
import com.folioledger.app.domain.repository.CategoryAndTagRepository
import com.folioledger.app.domain.repository.SettingsAndBackupRepository
import com.folioledger.app.domain.repository.TransactionRepository
import com.folioledger.app.domain.usecase.AccountingRulesEngine
import com.folioledger.app.domain.util.LedgerBucketingCalculator
import com.folioledger.app.domain.util.MoneyFormatterAndCalculator
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as JavaTextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val SHORT_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM", Locale.US)
private val MONTH_YEAR_FORMATTER = DateTimeFormatter.ofPattern("MMM yyyy", Locale.US)

// Backwards-compatible typealiases for domain models
typealias FinancialPeriodRange = com.folioledger.app.domain.model.FinancialPeriodRange
typealias DailyLedgerGroup = com.folioledger.app.domain.model.DailyLedgerGroup
typealias CalendarDayCell = com.folioledger.app.domain.model.CalendarDayCell
typealias WeeklyLedgerBucket = com.folioledger.app.domain.model.WeeklyLedgerBucket
typealias YearlyMonthOverviewItem = com.folioledger.app.domain.model.YearlyMonthOverviewItem
typealias AccountPeriodFlowSummary = com.folioledger.app.domain.model.AccountPeriodFlowSummary

object LedgerPeriodCalculator {
    fun resolveClampedDate(yearMonth: YearMonth, desiredDayOfMonth: Int): LocalDate =
        LedgerBucketingCalculator.resolveClampedDate(yearMonth, desiredDayOfMonth)

    fun computeFinancialMonthRange(
        referenceDateEpochDay: Long,
        monthStartDay: Int
    ): FinancialPeriodRange =
        LedgerBucketingCalculator.computeFinancialMonthRange(referenceDateEpochDay, monthStartDay)

    fun stepFinancialMonth(
        currentReferenceEpochDay: Long,
        monthStartDay: Int,
        deltaMonths: Long
    ): Long =
        LedgerBucketingCalculator.stepFinancialMonth(currentReferenceEpochDay, monthStartDay, deltaMonths)

    fun computeFinancialYearMonthRanges(
        anchorYear: Int,
        monthStartDay: Int
    ): List<FinancialPeriodRange> =
        LedgerBucketingCalculator.computeFinancialYearMonthRanges(anchorYear, monthStartDay)
}

enum class LedgerSubTab(
    val title: String,
    @param:StringRes val titleRes: Int,
) {
    DAILY("Daily", R.string.subtab_daily),
    CALENDAR("Calendar", R.string.subtab_calendar),
    WEEKLY("Weekly", R.string.subtab_weekly),
    MONTHLY("Monthly", R.string.subtab_monthly),
    SUMMARY("Summary", R.string.subtab_summary)
}

@Immutable
data class LedgerHomeUiState(
    val selectedSubTab: LedgerSubTab = LedgerSubTab.DAILY,
    val referenceDateEpochDay: Long = LocalDate.now().toEpochDay(),
    val activePeriodRange: FinancialPeriodRange = LedgerPeriodCalculator.computeFinancialMonthRange(
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
    val scopedAccountId: Long? = null,
    val scopedAccount: AccountSummary? = null,
    val userPreferences: UserPreferences = UserPreferences(),
    val currencySpec: CurrencySpec = SupportedCurrencies.INR,
    val numberGrouping: NumberGroupingSystem = NumberGroupingSystem.INDIAN,
    val dailyGroups: List<DailyLedgerGroup> = emptyList(),
    val calendarWeekdayHeaders: List<String> = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"),
    val calendarCells: List<CalendarDayCell> = emptyList(),
    val selectedCalendarDayEpochDay: Long = LocalDate.now().toEpochDay(),
    val selectedCalendarDayTransactions: List<TransactionItem> = emptyList(),
    val selectedCalendarDayIncome: MinorMoney = MinorMoney.ZERO,
    val selectedCalendarDayExpense: MinorMoney = MinorMoney.ZERO,
    val selectedCalendarDayTransfer: MinorMoney = MinorMoney.ZERO,
    val weeklyBuckets: List<WeeklyLedgerBucket> = emptyList(),
    val expandedWeekIndices: Set<Int> = setOf(1),
    val yearlyMonthItems: List<YearlyMonthOverviewItem> = emptyList(),
    val expandedYearlyMonths: Set<Int> = emptySet(),
    val yearlyIncomeTotal: MinorMoney = MinorMoney.ZERO,
    val yearlyExpenseTotal: MinorMoney = MinorMoney.ZERO,
    val yearlyNetTotal: MinorMoney = MinorMoney.ZERO,
    val topExpenseCategories: List<CategorySpendStat> = emptyList(),
    val budgetProgressList: List<BudgetProgress> = emptyList(),
    val accountPeriodFlows: List<AccountPeriodFlowSummary> = emptyList(),
    val savingsRatePercent: Float = 0f,
    val searchFilter: TransactionSearchFilter = TransactionSearchFilter(),
    val filterAllPeriods: Boolean = false,
    val minAmountFilterInput: String = "",
    val maxAmountFilterInput: String = "",
    val isSearchBarVisible: Boolean = false,
    val isFilterSheetOpen: Boolean = false,
    val activeFilterCount: Int = 0,
    val isFilterActive: Boolean = false,
    val filteredTransactions: List<TransactionItem> = emptyList(),
    val filteredDailyGroups: List<DailyLedgerGroup> = emptyList(),
    val filteredIncomeTotal: MinorMoney = MinorMoney.ZERO,
    val filteredExpenseTotal: MinorMoney = MinorMoney.ZERO,
    val filteredNetTotal: MinorMoney = MinorMoney.ZERO,
    val filteredTransferTotal: MinorMoney = MinorMoney.ZERO,
    val availableAccounts: List<AccountSummary> = emptyList(),
    val availableCategories: List<CategoryNode> = emptyList(),
    val expenseCategories: List<CategoryNode> = emptyList(),
    val incomeCategories: List<CategoryNode> = emptyList(),
    val expenseFilterCount: Int = 0,
    val incomeFilterCount: Int = 0,
    val accountFilterCount: Int = 0,
    val availableTags: List<String> = emptyList(),
    val bookmarks: List<TransactionBookmark> = emptyList(),
    val inspectedTransaction: TransactionItem? = null,
    val isExportingCsv: Boolean = false,
    val snackbarMessage: String? = null,
    val isInitialDataLoaded: Boolean = false,
    val scrollToTopSignal: Int = 0
)

private data class LedgerControlState(
    val selectedSubTab: LedgerSubTab = LedgerSubTab.DAILY,
    val referenceDateEpochDay: Long = LocalDate.now().toEpochDay(),
    val selectedCalendarDayEpochDay: Long = LocalDate.now().toEpochDay(),
    val scopedAccountId: Long? = null,
    val expandedWeekIndices: Set<Int>? = null,
    val expandedYearlyMonths: Set<Int>? = null,
    val searchFilter: TransactionSearchFilter = TransactionSearchFilter(),
    val filterAllPeriods: Boolean = false,
    val minAmountFilterInput: String = "",
    val maxAmountFilterInput: String = "",
    val isSearchBarVisible: Boolean = false,
    val isFilterSheetOpen: Boolean = false,
    val inspectedTransaction: TransactionItem? = null,
    val isExportingCsv: Boolean = false,
    val snackbarMessage: String? = null,
    val scrollToTopSignal: Int = 0
)

private data class PeriodDrivenBundle(
    val prefs: UserPreferences,
    val periodRange: FinancialPeriodRange,
    val periodSummary: PeriodLedgerSummary,
    val periodTransactions: List<TransactionItem>,
    val yearRanges: List<FinancialPeriodRange>,
    val yearTransactions: List<TransactionItem>,
    val categoryStats: List<CategorySpendStat>,
    val budgets: List<BudgetProgress>
)

private data class ReferenceFilterBundle(
    val accounts: List<AccountSummary>,
    val categories: List<CategoryNode>,
    val tags: List<String>,
    val bookmarks: List<TransactionBookmark>,
    val filteredTransactions: List<TransactionItem>
)

private data class MonthOverviewBase(
    val monthIndex: Int,
    val periodRange: FinancialPeriodRange,
    val isCurrentActiveMonth: Boolean,
    val incomeTotal: MinorMoney,
    val expenseTotal: MinorMoney,
    val netTotal: MinorMoney,
    val transactionCount: Int,
    val precomputedWeeklyBuckets: List<WeeklyLedgerBucket>
)

private data class BaseComputedLedgerData(
    val prefs: UserPreferences,
    val periodRange: FinancialPeriodRange,
    val yearRanges: List<FinancialPeriodRange>,
    val categoryStats: List<CategorySpendStat>,
    val budgets: List<BudgetProgress>,
    val scopedAccount: AccountSummary?,
    val activeScopedAccountId: Long?,
    val currencySpec: CurrencySpec,
    val activePeriodTransactions: List<TransactionItem>,
    val activeYearTransactions: List<TransactionItem>,
    val resolvedPeriodSummary: PeriodLedgerSummary,
    val dailyGroups: List<DailyLedgerGroup>,
    val weekdayHeaders: List<String>,
    val calendarCells: List<CalendarDayCell>,
    val weeklyBuckets: List<WeeklyLedgerBucket>,
    val monthOverviewBases: List<MonthOverviewBase>,
    val yearlyIncome: MinorMoney,
    val yearlyExpense: MinorMoney,
    val yearlyNet: MinorMoney,
    val accountFlows: List<AccountPeriodFlowSummary>,
    val savingsRate: Float,
    val expenseCats: List<CategoryNode>,
    val incomeCats: List<CategoryNode>,
    val expenseCategoryIds: Set<Long>,
    val incomeCategoryIds: Set<Long>,
    val scopedFilteredTransactions: List<TransactionItem>,
    val filteredInc: MinorMoney,
    val filteredExp: MinorMoney,
    val filteredTransferTotal: MinorMoney
)

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class LedgerHomeViewModel(
    private val transactionRepository: TransactionRepository,
    private val analyticsAndBudgetRepository: AnalyticsAndBudgetRepository,
    private val accountRepository: AccountRepository,
    private val categoryAndTagRepository: CategoryAndTagRepository,
    private val settingsAndBackupRepository: SettingsAndBackupRepository
) : ViewModel() {

    private val controlState = MutableStateFlow(LedgerControlState())

    private val userPreferencesFlow = settingsAndBackupRepository.observeUserPreferences()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = UserPreferences()
        )

    private val periodDrivenFlow: Flow<PeriodDrivenBundle> = combine(
        controlState.map { it.referenceDateEpochDay }.distinctUntilChanged(),
        settingsAndBackupRepository.observeUserPreferences()
    ) { refEpochDay, prefs ->
        refEpochDay to prefs
    }.flatMapLatest { (refEpochDay, prefs) ->
        val periodRange = LedgerPeriodCalculator.computeFinancialMonthRange(
            referenceDateEpochDay = refEpochDay,
            monthStartDay = prefs.monthStartDay
        )
        val yearRanges = LedgerPeriodCalculator.computeFinancialYearMonthRanges(
            anchorYear = periodRange.anchorYear,
            monthStartDay = prefs.monthStartDay
        )
        val yearStart = yearRanges.first().startEpochDay
        val yearEnd = yearRanges.last().endEpochDay

        val summaryAndTxFlow = combine(
            analyticsAndBudgetRepository.observePeriodLedgerSummary(
                startEpochDay = periodRange.startEpochDay,
                endEpochDay = periodRange.endEpochDay,
                includeCarryOver = prefs.carryOverEnabled
            ),
            transactionRepository.observeTransactionsInRange(
                startEpochDay = periodRange.startEpochDay,
                endEpochDay = periodRange.endEpochDay
            ),
            transactionRepository.observeTransactionsInRange(
                startEpochDay = yearStart,
                endEpochDay = yearEnd
            )
        ) { summary, periodTxns, yearTxns ->
            Triple(summary, periodTxns, yearTxns)
        }

        val statsAndBudgetsFlow = combine(
            analyticsAndBudgetRepository.observeCategorySpendStats(
                startEpochDay = periodRange.startEpochDay,
                endEpochDay = periodRange.endEpochDay,
                type = CategoryType.EXPENSE
            ),
            analyticsAndBudgetRepository.observeBudgetProgressList(
                startEpochDay = periodRange.startEpochDay,
                endEpochDay = periodRange.endEpochDay
            )
        ) { stats, budgets ->
            stats to budgets
        }

        combine(summaryAndTxFlow, statsAndBudgetsFlow) { (summary, periodTxns, yearTxns), (stats, budgets) ->
            PeriodDrivenBundle(
                prefs = prefs,
                periodRange = periodRange,
                periodSummary = summary,
                periodTransactions = periodTxns,
                yearRanges = yearRanges,
                yearTransactions = yearTxns,
                categoryStats = stats,
                budgets = budgets
            )
        }
    }.flowOn(Dispatchers.Default)

    private val debouncedSearchFilterFlow: Flow<Pair<TransactionSearchFilter, Boolean>> = controlState
        .map { it.searchFilter to it.filterAllPeriods }
        .distinctUntilChanged()
        .debounce { (filter, _) ->
            if (filter.queryText.isEmpty()) 0L else 150L
        }

    private val referenceAndFilterFlow: Flow<ReferenceFilterBundle> = combine(
        controlState.map { it.referenceDateEpochDay to it.scopedAccountId }.distinctUntilChanged(),
        settingsAndBackupRepository.observeUserPreferences(),
        debouncedSearchFilterFlow
    ) { (refEpochDay, _), prefs, (searchFilter, filterAllPeriods) ->
        val periodRange = LedgerPeriodCalculator.computeFinancialMonthRange(
            referenceDateEpochDay = refEpochDay,
            monthStartDay = prefs.monthStartDay
        )
        val effectiveFilter = if (!filterAllPeriods) {
            searchFilter.copy(
                startEpochDay = periodRange.startEpochDay,
                endEpochDay = periodRange.endEpochDay
            )
        } else {
            searchFilter.copy(
                startEpochDay = null,
                endEpochDay = null
            )
        }

        val filteredTxFlow = if (isUserFilterNonEmpty(searchFilter)) {
            transactionRepository.searchAndFilterTransactions(effectiveFilter)
        } else {
            MutableStateFlow(emptyList())
        }

        combine(
            accountRepository.observeActiveAccounts(refEpochDay),
            categoryAndTagRepository.observeAllCategoryTrees(),
            categoryAndTagRepository.observeAllTags(),
            categoryAndTagRepository.observeBookmarks(),
            filteredTxFlow
        ) { accounts, categories, tags, bookmarks, filteredTxns ->
            ReferenceFilterBundle(
                accounts = accounts,
                categories = categories,
                tags = tags,
                bookmarks = bookmarks,
                filteredTransactions = filteredTxns
            )
        }
    }.flatMapLatest { it }
    .flowOn(Dispatchers.Default)

    private val scopedAccountIdFlow = controlState.map { it.scopedAccountId }.distinctUntilChanged()

    private val baseComputedDataFlow: Flow<BaseComputedLedgerData> = combine(
        periodDrivenFlow,
        referenceAndFilterFlow,
        scopedAccountIdFlow
    ) { periodBundle, refBundle, scopedAccountId ->
        val prefs = periodBundle.prefs
        val periodRange = periodBundle.periodRange
        val todayEpochDay = LocalDate.now().toEpochDay()

        val scopedAccount = scopedAccountId?.let { targetId ->
            refBundle.accounts.firstOrNull { it.id == targetId }
        }
        val activeScopedAccountId = scopedAccount?.id

        val currencySpec = SupportedCurrencies.fromCode(
            scopedAccount?.currencyCode ?: prefs.primaryCurrencyCode
        )

        val activePeriodTransactions = if (activeScopedAccountId != null) {
            periodBundle.periodTransactions.filter { tx ->
                tx.accountId == activeScopedAccountId || tx.transferToAccountId == activeScopedAccountId
            }
        } else {
            periodBundle.periodTransactions
        }

        val activeYearTransactions = if (activeScopedAccountId != null) {
            periodBundle.yearTransactions.filter { tx ->
                tx.accountId == activeScopedAccountId || tx.transferToAccountId == activeScopedAccountId
            }
        } else {
            periodBundle.yearTransactions
        }

        val resolvedPeriodSummary = if (activeScopedAccountId != null) {
            val (scopedInc, scopedExp) = LedgerBucketingCalculator.computeIncomeAndExpenseTotals(
                transactions = activePeriodTransactions,
                scopedAccountId = activeScopedAccountId,
                primaryCurrencySpec = currencySpec,
                secondaryCurrencies = prefs.secondaryCurrencies
            )
            PeriodLedgerSummary(
                startEpochDay = periodRange.startEpochDay,
                endEpochDay = periodRange.endEpochDay,
                totalIncome = scopedInc,
                totalExpense = scopedExp,
                netCashflow = scopedInc - scopedExp,
                carryOverFromPreviousPeriod = MinorMoney.ZERO
            )
        } else {
            val (allInc, allExp) = LedgerBucketingCalculator.computeIncomeAndExpenseTotals(
                transactions = activePeriodTransactions,
                scopedAccountId = null,
                primaryCurrencySpec = currencySpec,
                secondaryCurrencies = prefs.secondaryCurrencies
            )
            PeriodLedgerSummary(
                startEpochDay = periodRange.startEpochDay,
                endEpochDay = periodRange.endEpochDay,
                totalIncome = allInc,
                totalExpense = allExp,
                netCashflow = allInc - allExp,
                carryOverFromPreviousPeriod = periodBundle.periodSummary.carryOverFromPreviousPeriod
            )
        }

        val dailyGroups = LedgerBucketingCalculator.buildDailyGroups(
            transactions = activePeriodTransactions,
            todayEpochDay = todayEpochDay,
            scopedAccountId = activeScopedAccountId,
            primaryCurrencySpec = currencySpec,
            secondaryCurrencies = prefs.secondaryCurrencies
        )

        val weekdayHeaders = LedgerBucketingCalculator.buildWeekdayHeaders(prefs.weekStartDayIso)
        val calendarCells = LedgerBucketingCalculator.buildCalendarGridCells(
            periodRange = periodRange,
            weekStartDayIso = prefs.weekStartDayIso,
            transactions = activePeriodTransactions,
            todayEpochDay = todayEpochDay,
            scopedAccountId = activeScopedAccountId,
            primaryCurrencySpec = currencySpec,
            secondaryCurrencies = prefs.secondaryCurrencies,
            numberGrouping = prefs.numberGrouping
        )

        val weeklyBuckets = LedgerBucketingCalculator.buildWeeklyBucketsForPeriod(
            periodRange = periodRange,
            weekStartDayIso = prefs.weekStartDayIso,
            transactions = activePeriodTransactions,
            scopedAccountId = activeScopedAccountId,
            primaryCurrencySpec = currencySpec,
            secondaryCurrencies = prefs.secondaryCurrencies
        )

        val monthOverviewBases = periodBundle.yearRanges.mapIndexed { idx, monthRange ->
            val monthTxns = activeYearTransactions.filter {
                it.ledgerDateEpochDay in monthRange.startEpochDay..monthRange.endEpochDay
            }
            val (mIncome, mExpense) = LedgerBucketingCalculator.computeIncomeAndExpenseTotals(
                transactions = monthTxns,
                scopedAccountId = activeScopedAccountId,
                primaryCurrencySpec = currencySpec,
                secondaryCurrencies = prefs.secondaryCurrencies
            )
            val precomputedWeeks = LedgerBucketingCalculator.buildWeeklyBucketsForPeriod(
                periodRange = monthRange,
                weekStartDayIso = prefs.weekStartDayIso,
                transactions = monthTxns,
                scopedAccountId = activeScopedAccountId,
                primaryCurrencySpec = currencySpec,
                secondaryCurrencies = prefs.secondaryCurrencies
            )
            MonthOverviewBase(
                monthIndex = idx + 1,
                periodRange = monthRange,
                isCurrentActiveMonth = todayEpochDay in monthRange.startEpochDay..monthRange.endEpochDay,
                incomeTotal = mIncome,
                expenseTotal = mExpense,
                netTotal = mIncome - mExpense,
                transactionCount = monthTxns.size,
                precomputedWeeklyBuckets = precomputedWeeks
            )
        }

        val yearlyIncome = MinorMoney(monthOverviewBases.sumOf { it.incomeTotal.amountMinor })
        val yearlyExpense = MinorMoney(monthOverviewBases.sumOf { it.expenseTotal.amountMinor })
        val yearlyNet = yearlyIncome - yearlyExpense

        val accountFlows = LedgerBucketingCalculator.buildAccountPeriodFlows(activePeriodTransactions, refBundle.accounts)
        val savingsRate = if (resolvedPeriodSummary.totalIncome.isPositive) {
            val netMinor = resolvedPeriodSummary.netCashflow.amountMinor.toFloat()
            val incMinor = resolvedPeriodSummary.totalIncome.amountMinor.toFloat()
            ((netMinor / incMinor) * 100f).coerceIn(-100f, 100f)
        } else {
            0f
        }

        val expenseCats = refBundle.categories.filter { it.type == CategoryType.EXPENSE }
        val incomeCats = refBundle.categories.filter { it.type == CategoryType.INCOME }

        val expenseCategoryIds = buildSet {
            expenseCats.forEach { parent ->
                add(parent.id)
                parent.subcategories.forEach { add(it.id) }
            }
        }
        val incomeCategoryIds = buildSet {
            incomeCats.forEach { parent ->
                add(parent.id)
                parent.subcategories.forEach { add(it.id) }
            }
        }

        val scopedFilteredTransactions = if (activeScopedAccountId != null) {
            refBundle.filteredTransactions.filter { tx ->
                tx.accountId == activeScopedAccountId || tx.transferToAccountId == activeScopedAccountId
            }
        } else {
            refBundle.filteredTransactions
        }

        val (filteredInc, filteredExp) = LedgerBucketingCalculator.computeIncomeAndExpenseTotals(
            transactions = scopedFilteredTransactions,
            scopedAccountId = activeScopedAccountId,
            primaryCurrencySpec = currencySpec,
            secondaryCurrencies = prefs.secondaryCurrencies
        )

        val filteredTransferTotal = LedgerBucketingCalculator.computeTransferTotal(
            transactions = scopedFilteredTransactions,
            primaryCurrencySpec = currencySpec,
            secondaryCurrencies = prefs.secondaryCurrencies
        )

        BaseComputedLedgerData(
            prefs = prefs,
            periodRange = periodRange,
            yearRanges = periodBundle.yearRanges,
            categoryStats = periodBundle.categoryStats,
            budgets = periodBundle.budgets,
            scopedAccount = scopedAccount,
            activeScopedAccountId = activeScopedAccountId,
            currencySpec = currencySpec,
            activePeriodTransactions = activePeriodTransactions,
            activeYearTransactions = activeYearTransactions,
            resolvedPeriodSummary = resolvedPeriodSummary,
            dailyGroups = dailyGroups,
            weekdayHeaders = weekdayHeaders,
            calendarCells = calendarCells,
            weeklyBuckets = weeklyBuckets,
            monthOverviewBases = monthOverviewBases,
            yearlyIncome = yearlyIncome,
            yearlyExpense = yearlyExpense,
            yearlyNet = yearlyNet,
            accountFlows = accountFlows,
            savingsRate = savingsRate,
            expenseCats = expenseCats,
            incomeCats = incomeCats,
            expenseCategoryIds = expenseCategoryIds,
            incomeCategoryIds = incomeCategoryIds,
            scopedFilteredTransactions = scopedFilteredTransactions,
            filteredInc = filteredInc,
            filteredExp = filteredExp,
            filteredTransferTotal = filteredTransferTotal
        )
    }.flowOn(Dispatchers.Default)

    val uiState: StateFlow<LedgerHomeUiState> = combine(
        controlState,
        baseComputedDataFlow,
        referenceAndFilterFlow
    ) { ctrl, base, refBundle ->
        val prefs = base.prefs
        val periodRange = base.periodRange
        val todayEpochDay = LocalDate.now().toEpochDay()
        val activeScopedAccountId = base.activeScopedAccountId

        val effectiveSelectedCalendarDay = if (
            ctrl.selectedCalendarDayEpochDay in periodRange.startEpochDay..periodRange.endEpochDay
        ) {
            ctrl.selectedCalendarDayEpochDay
        } else {
            todayEpochDay.coerceIn(periodRange.startEpochDay, periodRange.endEpochDay)
        }

        val selectedDayTxns = base.activePeriodTransactions
            .filter { it.ledgerDateEpochDay == effectiveSelectedCalendarDay }
            .sortedByDescending { it.timestampEpochMillis }

        val (selectedDayIncome, selectedDayExpense) = LedgerBucketingCalculator.computeIncomeAndExpenseTotals(
            transactions = selectedDayTxns,
            scopedAccountId = activeScopedAccountId,
            primaryCurrencySpec = base.currencySpec,
            secondaryCurrencies = prefs.secondaryCurrencies
        )

        val selectedDayTransfer = LedgerBucketingCalculator.computeTransferTotal(
            transactions = selectedDayTxns,
            primaryCurrencySpec = base.currencySpec,
            secondaryCurrencies = prefs.secondaryCurrencies
        )

        val isCurrentPeriod = todayEpochDay in periodRange.startEpochDay..periodRange.endEpochDay
        val defaultExpandedWeeks = if (isCurrentPeriod) {
            val activeWeek = base.weeklyBuckets.firstOrNull { bucket ->
                todayEpochDay in bucket.startEpochDay..bucket.endEpochDay
            }?.weekIndex ?: base.weeklyBuckets.lastOrNull()?.weekIndex ?: 1
            setOf(activeWeek)
        } else {
            emptySet()
        }
        val effectiveExpandedWeeks = ctrl.expandedWeekIndices ?: defaultExpandedWeeks

        val yearStartDay = base.yearRanges.firstOrNull()?.startEpochDay ?: 0L
        val yearEndDay = base.yearRanges.lastOrNull()?.endEpochDay ?: Long.MAX_VALUE
        val isCurrentYear = todayEpochDay in yearStartDay..yearEndDay
        val defaultExpandedMonths = if (isCurrentYear) {
            val activeMonthIndex = base.yearRanges.indexOfFirst {
                todayEpochDay in it.startEpochDay..it.endEpochDay
            }.let { if (it >= 0) it + 1 else periodRange.anchorMonthValue }
            setOf(activeMonthIndex)
        } else {
            emptySet()
        }
        val effectiveExpandedMonths = ctrl.expandedYearlyMonths ?: defaultExpandedMonths

        val yearlyMonthItems = base.monthOverviewBases.map { mBase ->
            YearlyMonthOverviewItem(
                monthIndex = mBase.monthIndex,
                periodRange = mBase.periodRange,
                isCurrentActiveMonth = mBase.isCurrentActiveMonth,
                incomeTotal = mBase.incomeTotal,
                expenseTotal = mBase.expenseTotal,
                netTotal = mBase.netTotal,
                transactionCount = mBase.transactionCount,
                weeklyBuckets = if (mBase.monthIndex in effectiveExpandedMonths) mBase.precomputedWeeklyBuckets else emptyList()
            )
        }

        val filterCount = countActiveUserFilters(ctrl.searchFilter, ctrl.filterAllPeriods)
        val filterActive = isUserFilterNonEmpty(ctrl.searchFilter)

        val activeSearchFilter = ctrl.searchFilter
        var expFilterCount = 0
        if (TransactionType.EXPENSE in activeSearchFilter.types) expFilterCount++
        if (TransactionType.REFUND in activeSearchFilter.types) expFilterCount++
        expFilterCount += activeSearchFilter.categoryIds.count { it in base.expenseCategoryIds }
        expFilterCount += activeSearchFilter.tags.size

        var incFilterCount = 0
        if (TransactionType.INCOME in activeSearchFilter.types) incFilterCount++
        incFilterCount += activeSearchFilter.categoryIds.count { it in base.incomeCategoryIds }

        var accFilterCount = 0
        accFilterCount += activeSearchFilter.accountIds.size
        if (TransactionType.TRANSFER in activeSearchFilter.types || TransactionType.CC_SETTLEMENT in activeSearchFilter.types) accFilterCount++
        if (activeSearchFilter.minAmount != null || activeSearchFilter.maxAmount != null) accFilterCount++

        val filteredDailyGroups = if (filterActive) {
            LedgerBucketingCalculator.buildDailyGroups(
                transactions = base.scopedFilteredTransactions,
                todayEpochDay = todayEpochDay,
                scopedAccountId = activeScopedAccountId,
                primaryCurrencySpec = base.currencySpec,
                secondaryCurrencies = prefs.secondaryCurrencies
            )
        } else {
            emptyList()
        }

        val updatedInspected = ctrl.inspectedTransaction?.let { current ->
            base.activePeriodTransactions.firstOrNull { it.id == current.id }
                ?: base.scopedFilteredTransactions.firstOrNull { it.id == current.id }
        }

        LedgerHomeUiState(
            isInitialDataLoaded = true,
            selectedSubTab = ctrl.selectedSubTab,
            referenceDateEpochDay = ctrl.referenceDateEpochDay,
            activePeriodRange = periodRange,
            periodSummary = base.resolvedPeriodSummary,
            scopedAccountId = activeScopedAccountId,
            scopedAccount = base.scopedAccount,
            userPreferences = prefs,
            currencySpec = base.currencySpec,
            numberGrouping = prefs.numberGrouping,
            dailyGroups = base.dailyGroups,
            calendarWeekdayHeaders = base.weekdayHeaders,
            calendarCells = base.calendarCells,
            selectedCalendarDayEpochDay = effectiveSelectedCalendarDay,
            selectedCalendarDayTransactions = selectedDayTxns,
            selectedCalendarDayIncome = selectedDayIncome,
            selectedCalendarDayExpense = selectedDayExpense,
            selectedCalendarDayTransfer = selectedDayTransfer,
            weeklyBuckets = base.weeklyBuckets,
            expandedWeekIndices = effectiveExpandedWeeks,
            yearlyMonthItems = yearlyMonthItems,
            expandedYearlyMonths = effectiveExpandedMonths,
            yearlyIncomeTotal = base.yearlyIncome,
            yearlyExpenseTotal = base.yearlyExpense,
            yearlyNetTotal = base.yearlyNet,
            topExpenseCategories = if (activeScopedAccountId != null) {
                LedgerBucketingCalculator.buildScopedCategorySpendStats(base.activePeriodTransactions, refBundle.categories)
            } else {
                base.categoryStats.take(6)
            },
            budgetProgressList = base.budgets,
            accountPeriodFlows = base.accountFlows,
            savingsRatePercent = base.savingsRate,
            searchFilter = ctrl.searchFilter,
            filterAllPeriods = ctrl.filterAllPeriods,
            minAmountFilterInput = ctrl.minAmountFilterInput,
            maxAmountFilterInput = ctrl.maxAmountFilterInput,
            isSearchBarVisible = ctrl.isSearchBarVisible,
            isFilterSheetOpen = ctrl.isFilterSheetOpen,
            activeFilterCount = filterCount,
            isFilterActive = filterActive,
            filteredTransactions = base.scopedFilteredTransactions,
            filteredDailyGroups = filteredDailyGroups,
            filteredIncomeTotal = base.filteredInc,
            filteredExpenseTotal = base.filteredExp,
            filteredNetTotal = base.filteredInc - base.filteredExp,
            filteredTransferTotal = base.filteredTransferTotal,
            availableAccounts = refBundle.accounts,
            availableCategories = refBundle.categories,
            expenseCategories = base.expenseCats,
            incomeCategories = base.incomeCats,
            expenseFilterCount = expFilterCount,
            incomeFilterCount = incFilterCount,
            accountFilterCount = accFilterCount,
            availableTags = refBundle.tags,
            bookmarks = refBundle.bookmarks,
            inspectedTransaction = updatedInspected,
            isExportingCsv = ctrl.isExportingCsv,
            snackbarMessage = ctrl.snackbarMessage,
            scrollToTopSignal = ctrl.scrollToTopSignal
        )
    }.flowOn(Dispatchers.Default)
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = LedgerHomeUiState()
    )

    fun setScopedAccount(accountId: Long?) {
        controlState.update { it.copy(scopedAccountId = accountId) }
    }

    fun selectSubTab(subTab: LedgerSubTab) {
        controlState.update { it.copy(selectedSubTab = subTab) }
    }

    fun navigateToPreviousPeriod() {
        val monthStartDay = userPreferencesFlow.value.monthStartDay
        val today = LocalDate.now().toEpochDay()
        controlState.update { current ->
            val deltaMonths = if (current.selectedSubTab == LedgerSubTab.MONTHLY) -12L else -1L
            val prevEpochDay = LedgerPeriodCalculator.stepFinancialMonth(
                currentReferenceEpochDay = current.referenceDateEpochDay,
                monthStartDay = monthStartDay,
                deltaMonths = deltaMonths
            )
            val targetPeriod = LedgerPeriodCalculator.computeFinancialMonthRange(prevEpochDay, monthStartDay)
            val targetSelectedDay = if (today in targetPeriod.startEpochDay..targetPeriod.endEpochDay) {
                today
            } else {
                prevEpochDay
            }
            current.copy(
                referenceDateEpochDay = prevEpochDay,
                selectedCalendarDayEpochDay = targetSelectedDay,
                expandedWeekIndices = null,
                expandedYearlyMonths = if (current.selectedSubTab == LedgerSubTab.MONTHLY) null else current.expandedYearlyMonths
            )
        }
    }

    fun navigateToNextPeriod() {
        val monthStartDay = userPreferencesFlow.value.monthStartDay
        val today = LocalDate.now().toEpochDay()
        controlState.update { current ->
            val deltaMonths = if (current.selectedSubTab == LedgerSubTab.MONTHLY) 12L else 1L
            val nextEpochDay = LedgerPeriodCalculator.stepFinancialMonth(
                currentReferenceEpochDay = current.referenceDateEpochDay,
                monthStartDay = monthStartDay,
                deltaMonths = deltaMonths
            )
            val targetPeriod = LedgerPeriodCalculator.computeFinancialMonthRange(nextEpochDay, monthStartDay)
            val targetSelectedDay = if (today in targetPeriod.startEpochDay..targetPeriod.endEpochDay) {
                today
            } else {
                nextEpochDay
            }
            current.copy(
                referenceDateEpochDay = nextEpochDay,
                selectedCalendarDayEpochDay = targetSelectedDay,
                expandedWeekIndices = null,
                expandedYearlyMonths = if (current.selectedSubTab == LedgerSubTab.MONTHLY) null else current.expandedYearlyMonths
            )
        }
    }

    fun jumpToToday() {
        val today = LocalDate.now().toEpochDay()
        controlState.update {
            it.copy(
                referenceDateEpochDay = today,
                selectedCalendarDayEpochDay = today,
                expandedWeekIndices = null,
                expandedYearlyMonths = null,
                scrollToTopSignal = it.scrollToTopSignal + 1
            )
        }
    }

    fun resetToDailySubTabAndJumpToToday() {
        val today = LocalDate.now().toEpochDay()
        controlState.update {
            it.copy(
                selectedSubTab = LedgerSubTab.DAILY,
                referenceDateEpochDay = today,
                selectedCalendarDayEpochDay = today,
                expandedWeekIndices = null,
                expandedYearlyMonths = null,
                scrollToTopSignal = it.scrollToTopSignal + 1
            )
        }
    }

    fun selectCalendarDay(epochDay: Long) {
        controlState.update { it.copy(selectedCalendarDayEpochDay = epochDay) }
    }

    fun toggleWeekExpanded(weekIndex: Int) {
        controlState.update { current ->
            val base = current.expandedWeekIndices ?: uiState.value.expandedWeekIndices
            val next = if (weekIndex in base) {
                base - weekIndex
            } else {
                base + weekIndex
            }
            current.copy(expandedWeekIndices = next)
        }
    }

    fun toggleYearlyMonthExpanded(monthIndex: Int) {
        val monthStartDay = userPreferencesFlow.value.monthStartDay
        val today = LocalDate.now().toEpochDay()
        controlState.update { current ->
            val currentRange = LedgerPeriodCalculator.computeFinancialMonthRange(
                referenceDateEpochDay = current.referenceDateEpochDay,
                monthStartDay = monthStartDay
            )
            val yearRanges = LedgerPeriodCalculator.computeFinancialYearMonthRanges(
                anchorYear = currentRange.anchorYear,
                monthStartDay = monthStartDay
            )
            val isCurrentYear = today in (yearRanges.first().startEpochDay..yearRanges.last().endEpochDay)
            val defaultExpanded = if (isCurrentYear) {
                val activeMonthIdx = yearRanges.indexOfFirst {
                    today in it.startEpochDay..it.endEpochDay
                }.let { if (it >= 0) it + 1 else currentRange.anchorMonthValue }
                setOf(activeMonthIdx)
            } else {
                emptySet()
            }
            val baseSet = current.expandedYearlyMonths ?: defaultExpanded
            val next = if (monthIndex in baseSet) {
                baseSet - monthIndex
            } else {
                baseSet + monthIndex
            }
            current.copy(expandedYearlyMonths = next)
        }
    }

    fun drillDownToMonth(startEpochDay: Long) {
        controlState.update { current ->
            current.copy(
                referenceDateEpochDay = startEpochDay,
                selectedCalendarDayEpochDay = startEpochDay,
                selectedSubTab = LedgerSubTab.DAILY,
                expandedWeekIndices = null
            )
        }
    }

    fun drillDownToWeek(monthStartEpochDay: Long, weekIndex: Int) {
        controlState.update { current ->
            current.copy(
                referenceDateEpochDay = monthStartEpochDay,
                selectedCalendarDayEpochDay = monthStartEpochDay,
                selectedSubTab = LedgerSubTab.WEEKLY,
                expandedWeekIndices = setOf(weekIndex)
            )
        }
    }

    fun inspectTransaction(transaction: TransactionItem?) {
        controlState.update { it.copy(inspectedTransaction = transaction) }
    }

    fun deleteTransaction(transactionId: Long) {
        viewModelScope.launch {
            val result = transactionRepository.deleteTransaction(transactionId)
            result.fold(
                onSuccess = {
                    controlState.update {
                        it.copy(
                            inspectedTransaction = null,
                            snackbarMessage = "Transaction deleted from ledger"
                        )
                    }
                },
                onFailure = { err ->
                    controlState.update {
                        it.copy(snackbarMessage = err.message ?: "Unable to delete transaction")
                    }
                }
            )
        }
    }

    // Search & Reinforced Multi-Filter Actions

    fun toggleSearchBar() {
        controlState.update { current ->
            val nextVisible = !current.isSearchBarVisible
            current.copy(
                isSearchBarVisible = nextVisible,
                searchFilter = if (!nextVisible) {
                    current.searchFilter.copy(queryText = "")
                } else {
                    current.searchFilter
                }
            )
        }
    }

    fun updateSearchQuery(query: String) {
        controlState.update { current ->
            current.copy(searchFilter = current.searchFilter.copy(queryText = query))
        }
    }

    fun setFilterSheetOpen(open: Boolean) {
        controlState.update { it.copy(isFilterSheetOpen = open) }
    }

    fun toggleFilterTransactionType(type: TransactionType) {
        controlState.update { current ->
            val existing = current.searchFilter.types
            val next = if (type in existing) existing - type else existing + type
            current.copy(searchFilter = current.searchFilter.copy(types = next))
        }
    }

    fun toggleFilterAccountId(accountId: Long) {
        controlState.update { current ->
            val existing = current.searchFilter.accountIds
            val next = if (accountId in existing) existing - accountId else existing + accountId
            current.copy(searchFilter = current.searchFilter.copy(accountIds = next))
        }
    }

    fun toggleFilterCategoryId(categoryId: Long) {
        controlState.update { current ->
            val existing = current.searchFilter.categoryIds
            val next = if (categoryId in existing) existing - categoryId else existing + categoryId
            current.copy(searchFilter = current.searchFilter.copy(categoryIds = next))
        }
    }

    fun toggleFilterTag(tag: String) {
        controlState.update { current ->
            val existing = current.searchFilter.tags
            val next = if (tag in existing) existing - tag else existing + tag
            current.copy(searchFilter = current.searchFilter.copy(tags = next))
        }
    }

    fun toggleFilterTransfers() {
        controlState.update { current ->
            val existing = current.searchFilter.types
            val hasTransfers = TransactionType.TRANSFER in existing || TransactionType.CC_SETTLEMENT in existing
            val next = if (hasTransfers) {
                existing - TransactionType.TRANSFER - TransactionType.CC_SETTLEMENT
            } else {
                existing + TransactionType.TRANSFER + TransactionType.CC_SETTLEMENT
            }
            current.copy(searchFilter = current.searchFilter.copy(types = next))
        }
    }

    fun updateFilterAmountBounds(minInput: String, maxInput: String) {
        val fractionDigits = uiState.value.currencySpec.fractionDigits
        val minMinor = parseOptionalMoneyFilter(minInput, fractionDigits)
        val maxMinor = parseOptionalMoneyFilter(maxInput, fractionDigits)
        controlState.update { current ->
            current.copy(
                minAmountFilterInput = minInput,
                maxAmountFilterInput = maxInput,
                searchFilter = current.searchFilter.copy(
                    minAmount = minMinor,
                    maxAmount = maxMinor
                )
            )
        }
    }

    fun setFilterAllPeriods(allPeriods: Boolean) {
        controlState.update { current ->
            current.copy(filterAllPeriods = allPeriods)
        }
    }

    @Suppress("unused")
    fun restrictFilterToActivePeriod(restrict: Boolean) {
        setFilterAllPeriods(!restrict)
    }

    fun clearAllFilters() {
        controlState.update { current ->
            current.copy(
                searchFilter = TransactionSearchFilter(),
                filterAllPeriods = false,
                minAmountFilterInput = "",
                maxAmountFilterInput = "",
                isSearchBarVisible = false,
                isFilterSheetOpen = false
            )
        }
    }

    fun exportActivePeriodToCsv(destinationUriString: String) {
        val period = uiState.value.activePeriodRange
        val scopedAccId = uiState.value.scopedAccountId
        val scopedName = uiState.value.scopedAccount?.name
        viewModelScope.launch {
            controlState.update { it.copy(isExportingCsv = true) }
            val result = settingsAndBackupRepository.exportTransactionsToCsv(
                destinationUriString = destinationUriString,
                startEpochDay = period.startEpochDay,
                endEpochDay = period.endEpochDay,
                accountId = scopedAccId
            )
            result.fold(
                onSuccess = { rowCount ->
                    val label = if (scopedName != null) "$scopedName (${period.title})" else period.title
                    controlState.update {
                        it.copy(
                            isExportingCsv = false,
                            snackbarMessage = "Exported $rowCount transactions for $label to CSV"
                        )
                    }
                },
                onFailure = { err ->
                    controlState.update {
                        it.copy(
                            isExportingCsv = false,
                            snackbarMessage = err.message ?: "CSV export failed"
                        )
                    }
                }
            )
        }
    }

    fun clearSnackbarMessage() {
        controlState.update { it.copy(snackbarMessage = null) }
    }

    private fun parseOptionalMoneyFilter(input: String, fractionDigits: Int): MinorMoney? {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return null
        return MoneyFormatterAndCalculator.evaluateKeypadExpressionToMinor(
            expression = trimmed,
            fractionDigits = fractionDigits
        ).getOrNull()
    }

    private fun isUserFilterNonEmpty(filter: TransactionSearchFilter): Boolean {
        return filter.queryText.isNotBlank() ||
                filter.accountIds.isNotEmpty() ||
                filter.categoryIds.isNotEmpty() ||
                filter.types.isNotEmpty() ||
                filter.tags.isNotEmpty() ||
                filter.minAmount != null ||
                filter.maxAmount != null
    }

    private fun countActiveUserFilters(
        filter: TransactionSearchFilter,
        filterAllPeriods: Boolean
    ): Int {
        var count = 0
        if (filter.queryText.isNotBlank()) count++
        if (filter.types.isNotEmpty()) count += filter.types.size
        if (filter.accountIds.isNotEmpty()) count += filter.accountIds.size
        if (filter.categoryIds.isNotEmpty()) count += filter.categoryIds.size
        if (filter.tags.isNotEmpty()) count += filter.tags.size
        if (filter.minAmount != null || filter.maxAmount != null) count++
        if (filterAllPeriods && count > 0) count++
        return count
    }

}
