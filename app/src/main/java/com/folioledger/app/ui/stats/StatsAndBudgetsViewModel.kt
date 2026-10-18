// File: app/src/main/java/com/folioledger/app/ui/stats/StatsAndBudgetsViewModel.kt
package com.folioledger.app.ui.stats

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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
import com.folioledger.app.domain.repository.AnalyticsAndBudgetRepository
import com.folioledger.app.domain.repository.CategoryAndTagRepository
import com.folioledger.app.domain.repository.SettingsAndBackupRepository
import com.folioledger.app.domain.repository.TransactionRepository
import com.folioledger.app.domain.util.MoneyFormatterAndCalculator
import com.folioledger.app.ui.ledger.AccountingRulesEngine
import com.folioledger.app.ui.ledger.FinancialPeriodRange
import java.time.LocalDate
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

enum class StatsSubTab(val title: String) {
    STATS("Stats"),
    BUDGETS("Budgets"),
    TAGS_AND_NOTES("Tags & Notes")
}

enum class RankingMode(val title: String) {
    TAGS("#Tag Rankings"),
    PAYEES_AND_NOTES("Payee / Note Rankings")
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
    val selectedCategoryType: CategoryType = CategoryType.EXPENSE,
    val selectedRankingMode: RankingMode = RankingMode.TAGS,
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
    val userPreferences: UserPreferences = UserPreferences(),
    val currencySpec: CurrencySpec = SupportedCurrencies.INR,
    val numberGrouping: NumberGroupingSystem = NumberGroupingSystem.INDIAN,
    val expenseCategoryStats: List<CategorySpendStat> = emptyList(),
    val incomeCategoryStats: List<CategorySpendStat> = emptyList(),
    val highlightedDonutCategoryId: Long? = null,
    val sixMonthTrendBars: List<MonthlyTrendBar> = emptyList(),
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
    val selectedCategoryType: CategoryType = CategoryType.EXPENSE,
    val selectedRankingMode: RankingMode = RankingMode.TAGS,
    val referenceDateEpochDay: Long = LocalDate.now().toEpochDay(),
    val highlightedDonutCategoryId: Long? = null,
    val drillDownCategoryId: Long? = null,
    val budgetEditorDialogState: BudgetEditorDialogState? = null,
    val isSavingBudget: Boolean = false,
    val snackbarMessage: String? = null
)

private data class PeriodAnalyticsBundle(
    val prefs: UserPreferences,
    val periodRange: FinancialPeriodRange,
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
    private val categoryAndTagRepository: CategoryAndTagRepository,
    private val transactionRepository: TransactionRepository,
    private val settingsAndBackupRepository: SettingsAndBackupRepository
) : ViewModel() {

    private val controlState = MutableStateFlow(StatsInternalControlState())

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
        ctrl.referenceDateEpochDay to prefs
    }.flatMapLatest { (refEpochDay, prefs) ->
        val periodRange = AccountingRulesEngine.computeFinancialMonthRange(
            referenceDateEpochDay = refEpochDay,
            monthStartDay = prefs.monthStartDay
        )

        val summaryAndCategoryStatsFlow = combine(
            analyticsAndBudgetRepository.observePeriodLedgerSummary(
                startEpochDay = periodRange.startEpochDay,
                endEpochDay = periodRange.endEpochDay,
                includeCarryOver = prefs.carryOverEnabled
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
                referenceDateEpochDay = refEpochDay,
                monthStartDay = prefs.monthStartDay
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
        categoryTreesFlow
    ) { ctrl, bundle, (expenseTree, incomeTree) ->
        val prefs = bundle.prefs
        val currencySpec = SupportedCurrencies.fromCode(prefs.primaryCurrencyCode)

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
            selectedCategoryType = ctrl.selectedCategoryType,
            selectedRankingMode = ctrl.selectedRankingMode,
            referenceDateEpochDay = ctrl.referenceDateEpochDay,
            activePeriodRange = bundle.periodRange,
            periodSummary = bundle.periodSummary,
            userPreferences = prefs,
            currencySpec = currencySpec,
            numberGrouping = prefs.numberGrouping,
            expenseCategoryStats = bundle.expenseStats,
            incomeCategoryStats = bundle.incomeStats,
            highlightedDonutCategoryId = ctrl.highlightedDonutCategoryId,
            sixMonthTrendBars = bundle.trendBars,
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
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = StatsAndBudgetsUiState()
    )

    fun selectSubTab(subTab: StatsSubTab) {
        controlState.update { it.copy(selectedSubTab = subTab) }
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
        val monthStartDay = uiState.value.userPreferences.monthStartDay
        controlState.update { current ->
            val prevEpochDay = AccountingRulesEngine.stepFinancialMonth(
                currentReferenceEpochDay = current.referenceDateEpochDay,
                monthStartDay = monthStartDay,
                deltaMonths = -1L
            )
            current.copy(
                referenceDateEpochDay = prevEpochDay,
                highlightedDonutCategoryId = null,
                drillDownCategoryId = null
            )
        }
    }

    fun navigateToNextPeriod() {
        val monthStartDay = uiState.value.userPreferences.monthStartDay
        controlState.update { current ->
            val nextEpochDay = AccountingRulesEngine.stepFinancialMonth(
                currentReferenceEpochDay = current.referenceDateEpochDay,
                monthStartDay = monthStartDay,
                deltaMonths = 1L
            )
            current.copy(
                referenceDateEpochDay = nextEpochDay,
                highlightedDonutCategoryId = null,
                drillDownCategoryId = null
            )
        }
    }

    fun jumpToToday() {
        val today = LocalDate.now().toEpochDay()
        controlState.update {
            it.copy(
                referenceDateEpochDay = today,
                highlightedDonutCategoryId = null,
                drillDownCategoryId = null
            )
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
        val fractionDigits = uiState.value.currencySpec.fractionDigits
        val formattedLimit = formatMinorPlainForInput(budget.limitAmount, fractionDigits)

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
        val currencySpec = uiState.value.currencySpec

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

    private fun collectCategoryAndSubcategoryIds(
        tree: List<CategoryNode>,
        targetCategoryId: Long
    ): Set<Long> {
        for (parent in tree) {
            if (parent.id == targetCategoryId) {
                return buildSet {
                    add(parent.id)
                    parent.subcategories.forEach { add(it.id) }
                }
            }
            for (sub in parent.subcategories) {
                if (sub.id == targetCategoryId) {
                    return setOf(sub.id)
                }
            }
        }
        return setOf(targetCategoryId)
    }

    private fun findCategoryNode(tree: List<CategoryNode>, categoryId: Long?): CategoryNode? {
        if (categoryId == null) return null
        for (parent in tree) {
            if (parent.id == categoryId) return parent
            for (sub in parent.subcategories) {
                if (sub.id == categoryId) return sub
            }
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

    companion object {
        fun provideFactory(
            analyticsAndBudgetRepository: AnalyticsAndBudgetRepository,
            categoryAndTagRepository: CategoryAndTagRepository,
            transactionRepository: TransactionRepository,
            settingsAndBackupRepository: SettingsAndBackupRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return StatsAndBudgetsViewModel(
                    analyticsAndBudgetRepository = analyticsAndBudgetRepository,
                    categoryAndTagRepository = categoryAndTagRepository,
                    transactionRepository = transactionRepository,
                    settingsAndBackupRepository = settingsAndBackupRepository
                ) as T
            }
        }
    }
}