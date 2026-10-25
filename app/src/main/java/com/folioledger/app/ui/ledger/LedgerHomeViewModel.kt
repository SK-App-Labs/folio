// File: app/src/main/java/com/folioledger/app/ui/ledger/LedgerHomeViewModel.kt
package com.folioledger.app.ui.ledger

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.folioledger.app.domain.model.AccountSummary
import com.folioledger.app.domain.model.BudgetProgress
import com.folioledger.app.domain.model.CategoryNode
import com.folioledger.app.domain.model.CategorySpendStat
import com.folioledger.app.domain.model.CategoryType
import com.folioledger.app.domain.model.CurrencySpec
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.NumberGroupingSystem
import com.folioledger.app.domain.model.PeriodLedgerSummary
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
import com.folioledger.app.domain.util.MoneyFormatterAndCalculator
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as JavaTextStyle
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

@Immutable
data class FinancialPeriodRange(
    val startEpochDay: Long,
    val endEpochDay: Long,
    val title: String,
    val subtitle: String,
    val anchorYear: Int,
    val anchorMonthValue: Int
)

object AccountingRulesEngine {

    private val shortMonthYearFormatter = DateTimeFormatter.ofPattern("MMM yyyy", Locale.US)
    private val shortDayMonthFormatter = DateTimeFormatter.ofPattern("dd MMM", Locale.US)
    private val fullDayMonthYearFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.US)

    private fun resolveClampedDate(yearMonth: YearMonth, desiredDayOfMonth: Int): LocalDate {
        val safeDay = desiredDayOfMonth.coerceIn(1, 31)
        val clampedDay = minOf(safeDay, yearMonth.lengthOfMonth())
        return yearMonth.atDay(clampedDay)
    }

    fun computeFinancialMonthRange(
        referenceDateEpochDay: Long,
        monthStartDay: Int
    ): FinancialPeriodRange {
        val safeStartDay = monthStartDay.coerceIn(1, 31)
        val refDate = LocalDate.ofEpochDay(referenceDateEpochDay)
        val refYearMonth = YearMonth.from(refDate)

        val startDate: LocalDate
        val endDate: LocalDate

        if (safeStartDay == 1) {
            startDate = refYearMonth.atDay(1)
            endDate = refYearMonth.atEndOfMonth()
        } else {
            val startInRefMonth = resolveClampedDate(refYearMonth, safeStartDay)
            if (!refDate.isBefore(startInRefMonth)) {
                startDate = startInRefMonth
                val nextMonthStart = resolveClampedDate(refYearMonth.plusMonths(1), safeStartDay)
                endDate = nextMonthStart.minusDays(1)
            } else {
                startDate = resolveClampedDate(refYearMonth.minusMonths(1), safeStartDay)
                endDate = startInRefMonth.minusDays(1)
            }
        }

        val title = startDate.format(shortMonthYearFormatter)
        val subtitle = "${startDate.format(shortDayMonthFormatter)} – ${endDate.format(fullDayMonthYearFormatter)}"

        return FinancialPeriodRange(
            startEpochDay = startDate.toEpochDay(),
            endEpochDay = endDate.toEpochDay(),
            title = title,
            subtitle = subtitle,
            anchorYear = startDate.year,
            anchorMonthValue = startDate.monthValue
        )
    }

    fun stepFinancialMonth(
        currentReferenceEpochDay: Long,
        monthStartDay: Int,
        deltaMonths: Long
    ): Long {
        val safeStartDay = monthStartDay.coerceIn(1, 31)
        val currentRange = computeFinancialMonthRange(currentReferenceEpochDay, safeStartDay)
        val anchorYearMonth = YearMonth.of(currentRange.anchorYear, currentRange.anchorMonthValue)
        val targetYearMonth = anchorYearMonth.plusMonths(deltaMonths)
        return resolveClampedDate(targetYearMonth, safeStartDay).toEpochDay()
    }

    fun computeFinancialYearMonthRanges(
        anchorYear: Int,
        monthStartDay: Int
    ): List<FinancialPeriodRange> {
        val safeStartDay = monthStartDay.coerceIn(1, 31)
        return (1..12).map { monthIndex ->
            val ym = YearMonth.of(anchorYear, monthIndex)
            val monthAnchorDate = resolveClampedDate(ym, safeStartDay)
            computeFinancialMonthRange(monthAnchorDate.toEpochDay(), safeStartDay)
        }
    }
}

enum class LedgerSubTab(val title: String) {
    DAILY("Daily"),
    CALENDAR("Calendar"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    SUMMARY("Summary")
}

@Immutable
data class DailyLedgerGroup(
    val epochDay: Long,
    val dayOfMonthText: String,
    val dayOfWeekText: String,
    val monthYearText: String,
    val isToday: Boolean,
    val dailyIncome: MinorMoney,
    val dailyExpense: MinorMoney,
    val dailyNet: MinorMoney,
    val transactions: List<TransactionItem>
)

@Immutable
data class CalendarDayCell(
    val epochDay: Long?,
    val dayNumber: Int?,
    val dateLabel: String = "",
    val isInActivePeriod: Boolean = false,
    val isToday: Boolean = false,
    val incomeTotal: MinorMoney = MinorMoney.ZERO,
    val expenseTotal: MinorMoney = MinorMoney.ZERO,
    val netTotal: MinorMoney = MinorMoney.ZERO,
    val transactionCount: Int = 0
)

@Immutable
data class WeeklyLedgerBucket(
    val weekIndex: Int,
    val label: String,
    val dateRangeSubtitle: String,
    val startEpochDay: Long,
    val endEpochDay: Long,
    val incomeTotal: MinorMoney,
    val expenseTotal: MinorMoney,
    val netTotal: MinorMoney,
    val transactions: List<TransactionItem>
)

@Immutable
data class YearlyMonthOverviewItem(
    val monthIndex: Int,
    val periodRange: FinancialPeriodRange,
    val isCurrentActiveMonth: Boolean,
    val incomeTotal: MinorMoney,
    val expenseTotal: MinorMoney,
    val netTotal: MinorMoney,
    val transactionCount: Int,
    val weeklyBuckets: List<WeeklyLedgerBucket>
)

@Immutable
data class AccountPeriodFlowSummary(
    val accountId: Long,
    val accountName: String,
    val incomeTotal: MinorMoney,
    val expenseTotal: MinorMoney,
    val netChange: MinorMoney,
    val transactionCount: Int
)

@Immutable
data class LedgerHomeUiState(
    val selectedSubTab: LedgerSubTab = LedgerSubTab.DAILY,
    val referenceDateEpochDay: Long = LocalDate.now().toEpochDay(),
    val activePeriodRange: FinancialPeriodRange = AccountingRulesEngine.computeFinancialMonthRange(
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
    val weeklyBuckets: List<WeeklyLedgerBucket> = emptyList(),
    val expandedWeekIndices: Set<Int> = setOf(1),
    val yearlyMonthItems: List<YearlyMonthOverviewItem> = emptyList(),
    val expandedYearlyMonths: Set<Int> = emptySet(),
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
    val availableAccounts: List<AccountSummary> = emptyList(),
    val availableCategories: List<CategoryNode> = emptyList(),
    val availableTags: List<String> = emptyList(),
    val bookmarks: List<TransactionBookmark> = emptyList(),
    val inspectedTransaction: TransactionItem? = null,
    val isExportingCsv: Boolean = false,
    val snackbarMessage: String? = null,
    val isInitialDataLoaded: Boolean = false
)

private data class LedgerControlState(
    val selectedSubTab: LedgerSubTab = LedgerSubTab.DAILY,
    val referenceDateEpochDay: Long = LocalDate.now().toEpochDay(),
    val selectedCalendarDayEpochDay: Long = LocalDate.now().toEpochDay(),
    val scopedAccountId: Long? = null,
    val expandedWeekIndices: Set<Int> = setOf(1),
    val expandedYearlyMonths: Set<Int> = emptySet(),
    val searchFilter: TransactionSearchFilter = TransactionSearchFilter(),
    val filterAllPeriods: Boolean = false,
    val minAmountFilterInput: String = "",
    val maxAmountFilterInput: String = "",
    val isSearchBarVisible: Boolean = false,
    val isFilterSheetOpen: Boolean = false,
    val inspectedTransaction: TransactionItem? = null,
    val isExportingCsv: Boolean = false,
    val snackbarMessage: String? = null
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

@OptIn(ExperimentalCoroutinesApi::class)
class LedgerHomeViewModel(
    private val transactionRepository: TransactionRepository,
    private val analyticsAndBudgetRepository: AnalyticsAndBudgetRepository,
    private val accountRepository: AccountRepository,
    private val categoryAndTagRepository: CategoryAndTagRepository,
    private val settingsAndBackupRepository: SettingsAndBackupRepository
) : ViewModel() {

    private val controlState = MutableStateFlow(LedgerControlState())

    private val periodDrivenFlow: Flow<PeriodDrivenBundle> = combine(
        controlState,
        settingsAndBackupRepository.observeUserPreferences()
    ) { ctrl, prefs ->
        ctrl.referenceDateEpochDay to prefs
    }.flatMapLatest { (refEpochDay, prefs) ->
        val periodRange = AccountingRulesEngine.computeFinancialMonthRange(
            referenceDateEpochDay = refEpochDay,
            monthStartDay = prefs.monthStartDay
        )
        val yearRanges = AccountingRulesEngine.computeFinancialYearMonthRanges(
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
    }

    private val referenceAndFilterFlow: Flow<ReferenceFilterBundle> = combine(
        controlState,
        settingsAndBackupRepository.observeUserPreferences()
    ) { ctrl, prefs ->
        ctrl to prefs
    }.flatMapLatest { (ctrl, prefs) ->
        val periodRange = AccountingRulesEngine.computeFinancialMonthRange(
            referenceDateEpochDay = ctrl.referenceDateEpochDay,
            monthStartDay = prefs.monthStartDay
        )
        val effectiveFilter = if (!ctrl.filterAllPeriods) {
            ctrl.searchFilter.copy(
                startEpochDay = periodRange.startEpochDay,
                endEpochDay = periodRange.endEpochDay
            )
        } else {
            ctrl.searchFilter.copy(
                startEpochDay = null,
                endEpochDay = null
            )
        }

        val filteredTxFlow = if (isUserFilterNonEmpty(ctrl.searchFilter)) {
            transactionRepository.searchAndFilterTransactions(effectiveFilter)
        } else {
            MutableStateFlow(emptyList())
        }

        combine(
            accountRepository.observeActiveAccounts(ctrl.referenceDateEpochDay),
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
    }

    val uiState: StateFlow<LedgerHomeUiState> = combine(
        controlState,
        periodDrivenFlow,
        referenceAndFilterFlow
    ) { ctrl, periodBundle, refBundle ->
        val prefs = periodBundle.prefs
        val periodRange = periodBundle.periodRange
        val todayEpochDay = LocalDate.now().toEpochDay()

        val scopedAccount = ctrl.scopedAccountId?.let { targetId ->
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
            val (scopedInc, scopedExp) = computeIncomeAndExpenseTotals(
                transactions = activePeriodTransactions,
                scopedAccountId = activeScopedAccountId
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
            periodBundle.periodSummary
        }

        val dailyGroups = buildDailyGroups(
            transactions = activePeriodTransactions,
            todayEpochDay = todayEpochDay,
            scopedAccountId = activeScopedAccountId
        )

        val weekdayHeaders = buildWeekdayHeaders(prefs.weekStartDayIso)
        val calendarCells = buildCalendarGridCells(
            periodRange = periodRange,
            weekStartDayIso = prefs.weekStartDayIso,
            transactions = activePeriodTransactions,
            todayEpochDay = todayEpochDay,
            scopedAccountId = activeScopedAccountId
        )

        val effectiveSelectedCalendarDay = if (
            ctrl.selectedCalendarDayEpochDay in periodRange.startEpochDay..periodRange.endEpochDay
        ) {
            ctrl.selectedCalendarDayEpochDay
        } else {
            todayEpochDay.coerceIn(periodRange.startEpochDay, periodRange.endEpochDay)
        }

        val selectedDayTxns = activePeriodTransactions
            .filter { it.ledgerDateEpochDay == effectiveSelectedCalendarDay }
            .sortedByDescending { it.timestampEpochMillis }

        val (selectedDayIncome, selectedDayExpense) = computeIncomeAndExpenseTotals(
            transactions = selectedDayTxns,
            scopedAccountId = activeScopedAccountId
        )

        val weeklyBuckets = buildWeeklyBucketsForPeriod(
            periodRange = periodRange,
            weekStartDayIso = prefs.weekStartDayIso,
            transactions = activePeriodTransactions,
            scopedAccountId = activeScopedAccountId
        )

        val yearlyMonthItems = periodBundle.yearRanges.mapIndexed { idx, monthRange ->
            val monthTxns = activeYearTransactions.filter {
                it.ledgerDateEpochDay in monthRange.startEpochDay..monthRange.endEpochDay
            }
            val (mIncome, mExpense) = computeIncomeAndExpenseTotals(
                transactions = monthTxns,
                scopedAccountId = activeScopedAccountId
            )
            val mWeeks = buildWeeklyBucketsForPeriod(
                periodRange = monthRange,
                weekStartDayIso = prefs.weekStartDayIso,
                transactions = monthTxns,
                scopedAccountId = activeScopedAccountId
            )
            YearlyMonthOverviewItem(
                monthIndex = idx + 1,
                periodRange = monthRange,
                isCurrentActiveMonth = monthRange.startEpochDay == periodRange.startEpochDay,
                incomeTotal = mIncome,
                expenseTotal = mExpense,
                netTotal = mIncome - mExpense,
                transactionCount = monthTxns.size,
                weeklyBuckets = mWeeks
            )
        }

        val accountFlows = buildAccountPeriodFlows(activePeriodTransactions)
        val savingsRate = if (resolvedPeriodSummary.totalIncome.isPositive) {
            val netMinor = resolvedPeriodSummary.netCashflow.amountMinor.toFloat()
            val incMinor = resolvedPeriodSummary.totalIncome.amountMinor.toFloat()
            ((netMinor / incMinor) * 100f).coerceIn(-100f, 100f)
        } else {
            0f
        }

        val filterCount = countActiveUserFilters(ctrl.searchFilter, ctrl.filterAllPeriods)
        val filterActive = isUserFilterNonEmpty(ctrl.searchFilter)

        val scopedFilteredTransactions = if (activeScopedAccountId != null) {
            refBundle.filteredTransactions.filter { tx ->
                tx.accountId == activeScopedAccountId || tx.transferToAccountId == activeScopedAccountId
            }
        } else {
            refBundle.filteredTransactions
        }

        val (filteredInc, filteredExp) = computeIncomeAndExpenseTotals(
            transactions = scopedFilteredTransactions,
            scopedAccountId = activeScopedAccountId
        )
        val filteredDailyGroups = if (filterActive) {
            buildDailyGroups(
                transactions = scopedFilteredTransactions,
                todayEpochDay = todayEpochDay,
                scopedAccountId = activeScopedAccountId
            )
        } else {
            emptyList()
        }

        val updatedInspected = ctrl.inspectedTransaction?.let { current ->
            activePeriodTransactions.firstOrNull { it.id == current.id }
                ?: scopedFilteredTransactions.firstOrNull { it.id == current.id }
        }

        LedgerHomeUiState(
            isInitialDataLoaded = true,
            selectedSubTab = ctrl.selectedSubTab,
            referenceDateEpochDay = ctrl.referenceDateEpochDay,
            activePeriodRange = periodRange,
            periodSummary = resolvedPeriodSummary,
            scopedAccountId = activeScopedAccountId,
            scopedAccount = scopedAccount,
            userPreferences = prefs,
            currencySpec = currencySpec,
            numberGrouping = prefs.numberGrouping,
            dailyGroups = dailyGroups,
            calendarWeekdayHeaders = weekdayHeaders,
            calendarCells = calendarCells,
            selectedCalendarDayEpochDay = effectiveSelectedCalendarDay,
            selectedCalendarDayTransactions = selectedDayTxns,
            selectedCalendarDayIncome = selectedDayIncome,
            selectedCalendarDayExpense = selectedDayExpense,
            weeklyBuckets = weeklyBuckets,
            expandedWeekIndices = ctrl.expandedWeekIndices,
            yearlyMonthItems = yearlyMonthItems,
            expandedYearlyMonths = ctrl.expandedYearlyMonths.ifEmpty {
                setOf(periodRange.anchorMonthValue)
            },
            topExpenseCategories = periodBundle.categoryStats.take(6),
            budgetProgressList = periodBundle.budgets,
            accountPeriodFlows = accountFlows,
            savingsRatePercent = savingsRate,
            searchFilter = ctrl.searchFilter,
            filterAllPeriods = ctrl.filterAllPeriods,
            minAmountFilterInput = ctrl.minAmountFilterInput,
            maxAmountFilterInput = ctrl.maxAmountFilterInput,
            isSearchBarVisible = ctrl.isSearchBarVisible,
            isFilterSheetOpen = ctrl.isFilterSheetOpen,
            activeFilterCount = filterCount,
            isFilterActive = filterActive,
            filteredTransactions = scopedFilteredTransactions,
            filteredDailyGroups = filteredDailyGroups,
            filteredIncomeTotal = filteredInc,
            filteredExpenseTotal = filteredExp,
            filteredNetTotal = filteredInc - filteredExp,
            availableAccounts = refBundle.accounts,
            availableCategories = refBundle.categories,
            availableTags = refBundle.tags,
            bookmarks = refBundle.bookmarks,
            inspectedTransaction = updatedInspected,
            isExportingCsv = ctrl.isExportingCsv,
            snackbarMessage = ctrl.snackbarMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = LedgerHomeUiState()
    )

    fun setScopedAccount(accountId: Long?) {
        controlState.update { it.copy(scopedAccountId = accountId) }
    }

    fun selectSubTab(subTab: LedgerSubTab) {
        controlState.update { it.copy(selectedSubTab = subTab) }
    }

    fun navigateToPreviousPeriod() {
        val monthStartDay = uiState.value.userPreferences.monthStartDay
        controlState.update { current ->
            val deltaMonths = if (current.selectedSubTab == LedgerSubTab.MONTHLY) -12L else -1L
            val prevEpochDay = AccountingRulesEngine.stepFinancialMonth(
                currentReferenceEpochDay = current.referenceDateEpochDay,
                monthStartDay = monthStartDay,
                deltaMonths = deltaMonths
            )
            current.copy(
                referenceDateEpochDay = prevEpochDay,
                selectedCalendarDayEpochDay = prevEpochDay
            )
        }
    }

    fun navigateToNextPeriod() {
        val monthStartDay = uiState.value.userPreferences.monthStartDay
        controlState.update { current ->
            val deltaMonths = if (current.selectedSubTab == LedgerSubTab.MONTHLY) 12L else 1L
            val nextEpochDay = AccountingRulesEngine.stepFinancialMonth(
                currentReferenceEpochDay = current.referenceDateEpochDay,
                monthStartDay = monthStartDay,
                deltaMonths = deltaMonths
            )
            current.copy(
                referenceDateEpochDay = nextEpochDay,
                selectedCalendarDayEpochDay = nextEpochDay
            )
        }
    }

    fun jumpToToday() {
        val today = LocalDate.now().toEpochDay()
        controlState.update {
            it.copy(
                referenceDateEpochDay = today,
                selectedCalendarDayEpochDay = today
            )
        }
    }

    fun selectCalendarDay(epochDay: Long) {
        controlState.update { it.copy(selectedCalendarDayEpochDay = epochDay) }
    }

    fun toggleWeekExpanded(weekIndex: Int) {
        controlState.update { current ->
            val next = if (weekIndex in current.expandedWeekIndices) {
                current.expandedWeekIndices - weekIndex
            } else {
                current.expandedWeekIndices + weekIndex
            }
            current.copy(expandedWeekIndices = next)
        }
    }

    fun toggleYearlyMonthExpanded(monthIndex: Int) {
        controlState.update { current ->
            val baseSet = current.expandedYearlyMonths.ifEmpty {
                setOf(uiState.value.activePeriodRange.anchorMonthValue)
            }
            val next = if (monthIndex in baseSet) {
                baseSet - monthIndex
            } else {
                baseSet + monthIndex
            }
            current.copy(expandedYearlyMonths = next)
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
        viewModelScope.launch {
            controlState.update { it.copy(isExportingCsv = true) }
            val result = settingsAndBackupRepository.exportTransactionsToCsv(
                destinationUriString = destinationUriString,
                startEpochDay = period.startEpochDay,
                endEpochDay = period.endEpochDay
            )
            result.fold(
                onSuccess = { rowCount ->
                    controlState.update {
                        it.copy(
                            isExportingCsv = false,
                            snackbarMessage = "Exported $rowCount transactions for ${period.title} to CSV"
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

    private fun computeIncomeAndExpenseTotals(
        transactions: List<TransactionItem>,
        scopedAccountId: Long? = null
    ): Pair<MinorMoney, MinorMoney> {
        var incomeMinor = 0L
        var expenseMinor = 0L
        transactions.forEach { tx ->
            when (tx.type) {
                TransactionType.INCOME -> {
                    incomeMinor = Math.addExact(incomeMinor, tx.amount.amountMinor)
                }
                TransactionType.EXPENSE -> {
                    expenseMinor = Math.addExact(expenseMinor, tx.amount.amountMinor)
                }
                TransactionType.REFUND -> {
                    if (scopedAccountId != null) {
                        incomeMinor = Math.addExact(incomeMinor, tx.amount.amountMinor)
                    } else {
                        expenseMinor = (expenseMinor - tx.amount.amountMinor).coerceAtLeast(0L)
                    }
                }
                TransactionType.TRANSFER, TransactionType.CC_SETTLEMENT -> {
                    if (scopedAccountId != null) {
                        if (tx.transferToAccountId == scopedAccountId) {
                            incomeMinor = Math.addExact(incomeMinor, tx.amount.amountMinor)
                        } else if (tx.accountId == scopedAccountId) {
                            expenseMinor = Math.addExact(
                                expenseMinor,
                                Math.addExact(tx.amount.amountMinor, tx.transferFee.amountMinor)
                            )
                        }
                    } else if (tx.transferFee.isPositive) {
                        expenseMinor = Math.addExact(expenseMinor, tx.transferFee.amountMinor)
                    }
                }
            }
        }
        return MinorMoney(incomeMinor) to MinorMoney(expenseMinor)
    }

    private fun buildDailyGroups(
        transactions: List<TransactionItem>,
        todayEpochDay: Long,
        scopedAccountId: Long? = null
    ): List<DailyLedgerGroup> {
        if (transactions.isEmpty()) return emptyList()
        val monthYearFormatter = DateTimeFormatter.ofPattern("MMM yyyy", Locale.US)

        return transactions
            .groupBy { it.ledgerDateEpochDay }
            .entries
            .sortedByDescending { it.key }
            .map { (epochDay, dayTxns) ->
                val date = LocalDate.ofEpochDay(epochDay)
                val sortedTxns = dayTxns.sortedByDescending { it.timestampEpochMillis }
                val (dayInc, dayExp) = computeIncomeAndExpenseTotals(sortedTxns, scopedAccountId)
                DailyLedgerGroup(
                    epochDay = epochDay,
                    dayOfMonthText = String.format(Locale.US, "%02d", date.dayOfMonth),
                    dayOfWeekText = date.dayOfWeek
                        .getDisplayName(JavaTextStyle.SHORT, Locale.US)
                        .uppercase(Locale.US),
                    monthYearText = date.format(monthYearFormatter),
                    isToday = epochDay == todayEpochDay,
                    dailyIncome = dayInc,
                    dailyExpense = dayExp,
                    dailyNet = dayInc - dayExp,
                    transactions = sortedTxns
                )
            }
    }

    private fun buildWeekdayHeaders(weekStartDayIso: Int): List<String> {
        val startIso = weekStartDayIso.coerceIn(1, 7)
        return (0 until 7).map { offset ->
            val dowNumber = ((startIso - 1 + offset) % 7) + 1
            DayOfWeek.of(dowNumber).getDisplayName(JavaTextStyle.SHORT, Locale.US)
        }
    }

    private fun buildCalendarGridCells(
        periodRange: FinancialPeriodRange,
        weekStartDayIso: Int,
        transactions: List<TransactionItem>,
        todayEpochDay: Long,
        scopedAccountId: Long? = null
    ): List<CalendarDayCell> {
        val startDate = LocalDate.ofEpochDay(periodRange.startEpochDay)
        val endDate = LocalDate.ofEpochDay(periodRange.endEpochDay)
        val startDow = DayOfWeek.of(weekStartDayIso.coerceIn(1, 7))

        val firstGridDate = startDate.with(TemporalAdjusters.previousOrSame(startDow))
        val endDow = startDow.minus(1)
        val lastGridDate = endDate.with(TemporalAdjusters.nextOrSame(endDow))

        val txByDay = transactions.groupBy { it.ledgerDateEpochDay }
        val shortDateFormatter = DateTimeFormatter.ofPattern("dd MMM", Locale.US)

        val cells = mutableListOf<CalendarDayCell>()
        var cursor = firstGridDate
        while (!cursor.isAfter(lastGridDate)) {
            val epoch = cursor.toEpochDay()
            val inPeriod = epoch in periodRange.startEpochDay..periodRange.endEpochDay
            if (!inPeriod) {
                cells.add(
                    CalendarDayCell(
                        epochDay = null,
                        dayNumber = null,
                        isInActivePeriod = false
                    )
                )
            } else {
                val dayTxns = txByDay[epoch].orEmpty()
                val (inc, exp) = computeIncomeAndExpenseTotals(dayTxns, scopedAccountId)
                cells.add(
                    CalendarDayCell(
                        epochDay = epoch,
                        dayNumber = cursor.dayOfMonth,
                        dateLabel = cursor.format(shortDateFormatter),
                        isInActivePeriod = true,
                        isToday = epoch == todayEpochDay,
                        incomeTotal = inc,
                        expenseTotal = exp,
                        netTotal = inc - exp,
                        transactionCount = dayTxns.size
                    )
                )
            }
            cursor = cursor.plusDays(1)
        }
        return cells
    }

    private fun buildWeeklyBucketsForPeriod(
        periodRange: FinancialPeriodRange,
        weekStartDayIso: Int,
        transactions: List<TransactionItem>,
        scopedAccountId: Long? = null
    ): List<WeeklyLedgerBucket> {
        val periodStart = LocalDate.ofEpochDay(periodRange.startEpochDay)
        val periodEnd = LocalDate.ofEpochDay(periodRange.endEpochDay)
        val startDow = DayOfWeek.of(weekStartDayIso.coerceIn(1, 7))
        val endDow = startDow.minus(1)
        val dayMonthFormatter = DateTimeFormatter.ofPattern("dd MMM", Locale.US)

        val buckets = mutableListOf<WeeklyLedgerBucket>()
        var currentStart = periodStart
        var weekNum = 1

        while (!currentStart.isAfter(periodEnd)) {
            val candidateEnd = currentStart.with(TemporalAdjusters.nextOrSame(endDow))
            val currentEnd = if (candidateEnd.isAfter(periodEnd)) periodEnd else candidateEnd

            val startEpoch = currentStart.toEpochDay()
            val endEpoch = currentEnd.toEpochDay()
            val weekTxns = transactions
                .filter { it.ledgerDateEpochDay in startEpoch..endEpoch }
                .sortedWith(
                    compareByDescending<TransactionItem> { it.ledgerDateEpochDay }
                        .thenByDescending { it.timestampEpochMillis }
                )
            val (inc, exp) = computeIncomeAndExpenseTotals(weekTxns, scopedAccountId)

            buckets.add(
                WeeklyLedgerBucket(
                    weekIndex = weekNum,
                    label = "Week $weekNum",
                    dateRangeSubtitle = "${currentStart.format(dayMonthFormatter)} – ${currentEnd.format(dayMonthFormatter)}",
                    startEpochDay = startEpoch,
                    endEpochDay = endEpoch,
                    incomeTotal = inc,
                    expenseTotal = exp,
                    netTotal = inc - exp,
                    transactions = weekTxns
                )
            )

            currentStart = currentEnd.plusDays(1)
            weekNum++
        }
        return buckets
    }

    private fun buildAccountPeriodFlows(
        transactions: List<TransactionItem>
    ): List<AccountPeriodFlowSummary> {
        if (transactions.isEmpty()) return emptyList()
        return transactions
            .groupBy { it.accountId to it.accountName }
            .map { (key, txns) ->
                val (inc, exp) = computeIncomeAndExpenseTotals(txns, key.first)
                AccountPeriodFlowSummary(
                    accountId = key.first,
                    accountName = key.second,
                    incomeTotal = inc,
                    expenseTotal = exp,
                    netChange = inc - exp,
                    transactionCount = txns.size
                )
            }
            .sortedByDescending { it.transactionCount }
    }
}