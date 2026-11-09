package com.folioledger.app.ui.stats

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.folioledger.app.domain.model.AccountGroup
import com.folioledger.app.domain.model.AccountSavingsGoal
import com.folioledger.app.domain.model.AccountSummary
import com.folioledger.app.domain.model.BudgetProgress
import com.folioledger.app.domain.model.CategoryNode
import com.folioledger.app.domain.model.CategorySpendStat
import com.folioledger.app.domain.model.CategoryType
import com.folioledger.app.domain.model.CurrencySpec
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.MonthlySavingsProgress
import com.folioledger.app.domain.model.NumberGroupingSystem
import com.folioledger.app.domain.model.PeriodLedgerSummary
import com.folioledger.app.domain.model.SpendRankingItem
import com.folioledger.app.domain.model.TransactionItem
import com.folioledger.app.domain.model.TransactionType
import com.folioledger.app.domain.util.MoneyFormatterAndCalculator
import com.folioledger.app.ui.components.CategoryIconBadge
import com.folioledger.app.ui.components.CreditHealthDashboardCard
import com.folioledger.app.ui.components.EmptyStateCard
import com.folioledger.app.ui.components.InteractiveCategoryDonutChart
import com.folioledger.app.ui.components.MoneyColorMode
import com.folioledger.app.ui.components.MoneyText
import com.folioledger.app.ui.components.NetWorthGrowthCanvasChart
import com.folioledger.app.ui.components.PeriodSelectorHeader
import com.folioledger.app.ui.components.SixMonthSmoothTrendAreaChart
import com.folioledger.app.ui.components.SixMonthTrendBarChart
import com.folioledger.app.ui.components.ThreeColumnSummaryBanner
import com.folioledger.app.ui.components.fromTransactionType
import com.folioledger.app.ui.components.parseHexColorSafely
import com.folioledger.app.ui.stats.*
import com.folioledger.app.ui.stats.components.*
import com.folioledger.app.ui.stats.dialogs.*
import com.folioledger.app.ui.stats.sheets.*
import com.folioledger.app.ui.stats.subtabs.*
import com.folioledger.app.ui.theme.LocalFolioSemanticColors
import com.folioledger.app.ui.theme.tabularMoneyStyle
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
fun StatsAndBudgetsScreen(
    viewModel: StatsAndBudgetsViewModel,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onInspectTransaction: (TransactionItem) -> Unit = {},
    onAddTransactionForAccount: ((AccountSummary) -> Unit)? = null,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = uiState.selectedSubTab.ordinal) { StatsSubTab.entries.size }

    // Sync ViewModel's selectedSubTab ONLY when user swipe gesture settles
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { settledPage ->
            val targetTab = StatsSubTab.entries[settledPage]
            if (uiState.selectedSubTab != targetTab) {
                viewModel.selectSubTab(targetTab)
            }
        }
    }

    // Sync pagerState when ViewModel's selectedSubTab changes externally (e.g. Tab click)
    LaunchedEffect(uiState.selectedSubTab) {
        if (pagerState.currentPage != uiState.selectedSubTab.ordinal && !pagerState.isScrollInProgress) {
            pagerState.animateScrollToPage(uiState.selectedSubTab.ordinal)
        }
    }

    LaunchedEffect(uiState.snackbarMessage) {
        val message = uiState.snackbarMessage
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearSnackbarMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = contentPadding.calculateTopPadding() + scaffoldPadding.calculateTopPadding(),
                    bottom = contentPadding.calculateBottomPadding()
                )
        ) {
            PeriodSelectorHeader(
                periodTitle = uiState.activePeriodRange.title,
                periodSubtitle = null,
                onPreviousPeriod = viewModel::navigateToPreviousPeriod,
                onNextPeriod = viewModel::navigateToNextPeriod,
                onPeriodTitleClick = {
                    if (uiState.periodGranularity == StatsPeriodGranularity.CUSTOM) {
                        viewModel.setCustomDatePickerOpen(open = true)
                    } else {
                        viewModel.jumpToToday()
                    }
                },
                trailingContent = {
                    if (uiState.selectedSubTab != StatsSubTab.BUDGETS) {
                        StatsPeriodGranularityDropdownChip(
                            selectedGranularity = uiState.periodGranularity,
                            onSelectGranularity = viewModel::selectPeriodGranularity,
                            onOpenCustomDatePicker = { viewModel.setCustomDatePickerOpen(true) }
                        )
                    }
                }
            )

            PrimaryTabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = MaterialTheme.colorScheme.surface,
                divider = {}
            ) {
                StatsSubTab.entries.forEach { tab ->
                    val isSelected = pagerState.currentPage == tab.ordinal
                    Tab(
                        selected = isSelected,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(tab.ordinal)
                            }
                        },
                        text = {
                            Text(
                                text = stringResource(tab.titleRes),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                }
            }

            HorizontalDivider(color = LocalFolioSemanticColors.current.cardStrokeSubtle)

            HorizontalPager(
                state = pagerState,
                beyondViewportPageCount = 0,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (StatsSubTab.entries[page]) {
                    StatsSubTab.STATS -> {
                        StatsBreakdownSubTabContent(
                            uiState = uiState,
                            onSelectOverviewMode = viewModel::selectOverviewMode,
                            onSelectCategoryType = viewModel::selectCategoryType,
                            onDonutSliceSelected = viewModel::onDonutSliceSelected,
                            onOpenCategoryDrillDown = viewModel::openCategoryDrillDown,
                            onSelectTrendChartMode = viewModel::selectTrendChartMode,
                            onSelectNetWorthChartMetricFilter = viewModel::selectNetWorthChartMetricFilter,
                            onToggleRunwayIncludesInvestments = viewModel::toggleRunwayIncludesInvestments
                        )
                    }

                    StatsSubTab.CREDIT_CARDS -> {
                        CreditCardsCockpitSubTabContent(
                            uiState = uiState,
                            onAddTransactionForAccount = onAddTransactionForAccount,
                            onSaveCardMilestone = viewModel::saveCardMilestoneConfig,
                            contentPadding = contentPadding
                        )
                    }

                    StatsSubTab.BUDGETS -> {
                        BudgetsManagerSubTabContent(
                            uiState = uiState,
                            onOpenOverallBudgetEditor = {
                                val existing = uiState.overallBudgetProgress
                                if (existing != null) {
                                    viewModel.openEditBudgetDialog(existing)
                                } else {
                                    viewModel.openCreateBudgetDialog(null)
                                }
                            },
                            onOpenAddCategoryBudget = viewModel::openCreateCategoryBudgetDialog,
                            onOpenEditBudget = viewModel::openEditBudgetDialog,
                            onOpenMonthlySavingsTargetDialog = viewModel::openMonthlySavingsTargetDialog,
                            onOpenAddSavingsGoal = { viewModel.openAddSavingsGoalSheet(null) },
                            onOpenEditSavingsGoal = viewModel::openEditSavingsGoalSheet
                        )
                    }

                    StatsSubTab.TAGS_AND_NOTES -> {
                        TagsAndNotesRankingsSubTabContent(
                            uiState = uiState,
                            onSelectRankingMode = viewModel::selectRankingMode,
                            onSelectPayeeSortMode = viewModel::selectPayeeSortMode,
                            onToggleRankingsAllTime = viewModel::toggleRankingsAllTime,
                            onPayeeClick = viewModel::openPayeeDrillDown,
                            onTagClick = viewModel::openTagDrillDown
                        )
                    }
                }
            }
        }
    }

    if (uiState.isCustomDatePickerOpen) {
        CustomDateRangePickerDialog(
            initialStartEpochDay = uiState.customStartEpochDay,
            initialEndEpochDay = uiState.customEndEpochDay,
            onConfirmRange = viewModel::setCustomDateRange,
            onDismiss = { viewModel.setCustomDatePickerOpen(false) }
        )
    }

    uiState.drillDownState?.let { drillDown ->
        CategoryDrillDownBottomSheet(
            drillDownState = drillDown,
            periodTitle = uiState.activePeriodRange.title,
            currencySpec = uiState.currencySpec,
            grouping = uiState.numberGrouping,
            onConfigureBudgetForCategory = {
                val categoryId = drillDown.categoryStat.categoryId
                viewModel.openCreateBudgetDialog(categoryId)
            },
            onTransactionClick = { tx ->
                onInspectTransaction(tx)
            },
            onDismiss = viewModel::dismissCategoryDrillDown
        )
    }

    uiState.payeeDrillDownState?.let { payeeDrillDown ->
        PayeeDrillDownBottomSheet(
            drillDownState = payeeDrillDown,
            periodTitle = uiState.activePeriodRange.title,
            currencySpec = uiState.currencySpec,
            grouping = uiState.numberGrouping,
            onTransactionClick = { tx ->
                onInspectTransaction(tx)
            },
            onDismiss = viewModel::dismissPayeeDrillDown
        )
    }

    uiState.tagDrillDownState?.let { tagDrillDown ->
        TagProjectDrillDownBottomSheet(
            drillDownState = tagDrillDown,
            periodTitle = uiState.activePeriodRange.title,
            isAllTime = uiState.isRankingsAllTime,
            currencySpec = uiState.currencySpec,
            grouping = uiState.numberGrouping,
            onTransactionClick = { tx ->
                onInspectTransaction(tx)
            },
            onDismiss = viewModel::dismissTagDrillDown
        )
    }

    val budgetEditor = uiState.budgetEditorDialogState
    if (budgetEditor != null) {
        BudgetEditorBottomSheet(
            state = budgetEditor,
            expenseCategories = uiState.expenseCategoryTree,
            currencySpec = uiState.currencySpec,
            isSaving = uiState.isSavingBudget,
            onUpdateState = viewModel::updateBudgetEditorState,
            onSave = viewModel::saveBudgetFromDialog,
            onDelete = {
                if ((budgetEditor.isEditing) && (budgetEditor.budgetId > 0L)) {
                    viewModel.deleteBudget(budgetEditor.budgetId)
                }
            },
            onDismiss = viewModel::dismissBudgetEditorDialog
        )
    }

    if (uiState.isMonthlySavingsTargetDialogOpen) {
        MonthlySavingsTargetDialog(
            currentInput = uiState.monthlySavingsTargetInput,
            currencySpec = uiState.currencySpec,
            isConfigured = uiState.monthlySavingsProgress.isConfigured,
            onInputChange = viewModel::updateMonthlySavingsTargetInput,
            onSave = viewModel::saveMonthlySavingsTarget,
            onRemove = viewModel::removeMonthlySavingsTarget,
            onDismiss = viewModel::closeMonthlySavingsTargetDialog
        )
    }

    if (uiState.isSavingsGoalSheetOpen) {
        SavingsGoalEditorBottomSheet(
            selectedAccountId = uiState.editingSavingsGoalAccountId,
            title = uiState.editingSavingsGoalTitle,
            targetAmountInput = uiState.editingSavingsGoalTargetAmountInput,
            targetDateEpochDay = uiState.editingSavingsGoalTargetDateEpochDay,
            availableAccounts = uiState.assetAccountsForSettlement,
            currencySpec = uiState.currencySpec,
            onUpdate = viewModel::updateEditingSavingsGoal,
            onSave = viewModel::saveSavingsGoal,
            onDelete = {
                uiState.editingSavingsGoalAccountId?.let { viewModel.removeSavingsGoal(it) }
            },
            onDismiss = viewModel::closeSavingsGoalSheet
        )
    }
}

@Composable
private fun StatsPeriodGranularityDropdownChip(
    selectedGranularity: StatsPeriodGranularity,
    onSelectGranularity: (StatsPeriodGranularity) -> Unit,
    onOpenCustomDatePicker: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.padding(end = 8.dp)) {
        FilterChip(
            selected = true,
            onClick = { menuExpanded = true },
            label = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (selectedGranularity == StatsPeriodGranularity.CUSTOM) {
                        Icon(
                            imageVector = Icons.Filled.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = selectedGranularity.title,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = "Select Granularity",
                        modifier = Modifier.size(18.dp)
                    )
                }
            },
            shape = RoundedCornerShape(10.dp)
        )

        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false }
        ) {
            StatsPeriodGranularity.entries.forEach { granularity ->
                val isSelected = granularity == selectedGranularity
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (granularity == StatsPeriodGranularity.CUSTOM) {
                                Icon(
                                    imageVector = Icons.Filled.DateRange,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                            Text(
                                text = granularity.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    },
                    onClick = {
                        menuExpanded = false
                        onSelectGranularity(granularity)
                        if (granularity == StatsPeriodGranularity.CUSTOM) {
                            onOpenCustomDatePicker()
                        }
                    }
                )
            }
        }
    }
}
