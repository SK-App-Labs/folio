// File: app/src/main/java/com/folioledger/app/ui/stats/StatsAndBudgetsScreen.kt
package com.folioledger.app.ui.stats

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.folioledger.app.domain.model.BudgetProgress
import com.folioledger.app.domain.model.CategoryNode
import com.folioledger.app.domain.model.CategorySpendStat
import com.folioledger.app.domain.model.CategoryType
import com.folioledger.app.domain.model.CurrencySpec
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.NumberGroupingSystem
import com.folioledger.app.domain.model.SpendRankingItem
import com.folioledger.app.domain.model.TransactionItem
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
import com.folioledger.app.ui.theme.LocalFolioSemanticColors
import com.folioledger.app.ui.theme.tabularMoneyStyle
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun StatsAndBudgetsScreen(
    viewModel: StatsAndBudgetsViewModel,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onInspectTransaction: (TransactionItem) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

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
                    bottom = contentPadding.calculateBottomPadding() + scaffoldPadding.calculateBottomPadding()
                )
        ) {
            PeriodSelectorHeader(
                periodTitle = uiState.activePeriodRange.title,
                periodSubtitle = uiState.activePeriodRange.subtitle,
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
                    StatsPeriodGranularityDropdownChip(
                        selectedGranularity = uiState.periodGranularity,
                        onSelectGranularity = viewModel::selectPeriodGranularity,
                        onOpenCustomDatePicker = { viewModel.setCustomDatePickerOpen(true) }
                    )
                }
            )

            PrimaryTabRow(
                selectedTabIndex = uiState.selectedSubTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surface,
                divider = {}
            ) {
                StatsSubTab.entries.forEach { tab ->
                    val isSelected = uiState.selectedSubTab == tab
                    Tab(
                        selected = isSelected,
                        onClick = { viewModel.selectSubTab(tab) },
                        text = {
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                }
            }

            HorizontalDivider(color = LocalFolioSemanticColors.current.cardStrokeSubtle)

            when (uiState.selectedSubTab) {
                StatsSubTab.STATS -> {
                    StatsBreakdownSubTabContent(
                        uiState = uiState,
                        onSelectCategoryType = viewModel::selectCategoryType,
                        onDonutSliceSelected = viewModel::onDonutSliceSelected,
                        onOpenCategoryDrillDown = viewModel::openCategoryDrillDown,
                        onSelectTrendChartMode = viewModel::selectTrendChartMode,
                        onSelectNetWorthChartMetricFilter = viewModel::selectNetWorthChartMetricFilter
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
                        onOpenEditBudget = viewModel::openEditBudgetDialog
                    )
                }

                StatsSubTab.TAGS_AND_NOTES -> {
                    TagsAndNotesRankingsSubTabContent(
                        uiState = uiState,
                        onSelectRankingMode = viewModel::selectRankingMode
                    )
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
            sixMonthTrendBars = uiState.sixMonthTrendBars,
            currencySpec = uiState.currencySpec,
            grouping = uiState.numberGrouping,
            onConfigureBudgetForCategory = {
                val categoryId = drillDown.categoryStat.categoryId
                viewModel.dismissCategoryDrillDown()
                viewModel.openCreateBudgetDialog(categoryId)
            },
            onTransactionClick = { tx ->
                viewModel.dismissCategoryDrillDown()
                onInspectTransaction(tx)
            },
            onDismiss = viewModel::dismissCategoryDrillDown
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

@Composable
private fun CustomDateRangePickerDialog(
    initialStartEpochDay: Long,
    initialEndEpochDay: Long,
    onConfirmRange: (startEpochDay: Long, endEpochDay: Long) -> Unit,
    onDismiss: () -> Unit
) {
    val startMillis = remember(initialStartEpochDay) {
        LocalDate.ofEpochDay(initialStartEpochDay)
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()
    }
    val endMillis = remember(initialEndEpochDay) {
        LocalDate.ofEpochDay(initialEndEpochDay)
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()
    }

    val pickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = startMillis,
        initialSelectedEndDateMillis = endMillis
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val selectedStart = pickerState.selectedStartDateMillis
                    val selectedEnd = pickerState.selectedEndDateMillis ?: selectedStart
                    if (selectedStart != null && selectedEnd != null) {
                        val sEpoch = Instant.ofEpochMilli(selectedStart)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate()
                            .toEpochDay()
                        val eEpoch = Instant.ofEpochMilli(selectedEnd)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate()
                            .toEpochDay()
                        onConfirmRange(sEpoch, eEpoch)
                    } else {
                        onDismiss()
                    }
                }
            ) {
                Text("Apply Range")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DateRangePicker(
            state = pickerState,
            title = {
                Text(
                    text = "Select Custom Analytics Date Range",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(460.dp)
        )
    }
}

@Composable
private fun StatsBreakdownSubTabContent(
    uiState: StatsAndBudgetsUiState,
    onSelectCategoryType: (CategoryType) -> Unit,
    onDonutSliceSelected: (CategorySpendStat?) -> Unit,
    onOpenCategoryDrillDown: (CategorySpendStat) -> Unit,
    onSelectTrendChartMode: (TrendChartMode) -> Unit,
    onSelectNetWorthChartMetricFilter: (NetWorthChartMetricFilter) -> Unit
) {
    val semanticColors = LocalFolioSemanticColors.current
    val activeStats = uiState.activeCategoryStats

    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 12.dp,
            bottom = 96.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            ThreeColumnSummaryBanner(
                firstLabel = "Income",
                firstAmount = uiState.periodSummary.totalIncome,
                secondLabel = "Expenses",
                secondAmount = uiState.periodSummary.totalExpense,
                thirdLabel = "Net Cashflow",
                thirdAmount = uiState.periodSummary.netCashflow,
                currencySpec = uiState.currencySpec,
                grouping = uiState.numberGrouping,
                carryOverAmount = if (
                    uiState.userPreferences.carryOverEnabled &&
                    uiState.periodGranularity == StatsPeriodGranularity.MONTHLY
                ) {
                    uiState.periodSummary.carryOverFromPreviousPeriod
                } else {
                    null
                }
            )
        }

        // Segmented Expenses vs Income Toggle
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val isExpense = uiState.selectedCategoryType == CategoryType.EXPENSE
                val isIncome = uiState.selectedCategoryType == CategoryType.INCOME

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isExpense) {
                        semanticColors.expenseCoralContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerLow
                    },
                    border = BorderStroke(
                        width = if (isExpense) 1.5.dp else 1.dp,
                        color = if (isExpense) {
                            semanticColors.expenseCoral
                        } else {
                            semanticColors.cardStrokeSubtle
                        }
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelectCategoryType(CategoryType.EXPENSE) }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                            contentDescription = null,
                            tint = if (isExpense) {
                                semanticColors.onExpenseCoralContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Expenses",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (isExpense) FontWeight.Bold else FontWeight.Medium,
                            color = if (isExpense) {
                                semanticColors.onExpenseCoralContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isIncome) {
                        semanticColors.incomeEmeraldContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerLow
                    },
                    border = BorderStroke(
                        width = if (isIncome) 1.5.dp else 1.dp,
                        color = if (isIncome) {
                            semanticColors.incomeEmerald
                        } else {
                            semanticColors.cardStrokeSubtle
                        }
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelectCategoryType(CategoryType.INCOME) }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            tint = if (isIncome) {
                                semanticColors.onIncomeEmeraldContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Income",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (isIncome) FontWeight.Bold else FontWeight.Medium,
                            color = if (isIncome) {
                                semanticColors.onIncomeEmeraldContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }
        }

        // Interactive Category Donut Chart
        item {
            InteractiveCategoryDonutChart(
                stats = activeStats,
                currencySpec = uiState.currencySpec,
                grouping = uiState.numberGrouping,
                selectedCategoryId = uiState.highlightedDonutCategoryId,
                onCategorySelected = { tappedStat ->
                    onDonutSliceSelected(tappedStat)
                },
                centerLabelWhenUnselected = if (uiState.selectedCategoryType == CategoryType.EXPENSE) {
                    "Total Expenses"
                } else {
                    "Total Income"
                }
            )
        }

        // Ranked Category Breakdown List Header
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (uiState.selectedCategoryType == CategoryType.EXPENSE) {
                        "Expense Categories (${activeStats.size})"
                    } else {
                        "Income Categories (${activeStats.size})"
                    },
                    style = tabularMoneyStyle(MaterialTheme.typography.titleMedium),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Tap row to drill down",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (activeStats.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "No ${uiState.selectedCategoryType.name.lowercase().replaceFirstChar { it.uppercase() }} Data",
                    subtitle = "No transactions recorded for ${uiState.activePeriodRange.title} in this category type.",
                    icon = Icons.Filled.PieChart
                )
            }
        } else {
            items(activeStats, key = { "${uiState.selectedCategoryType}_${it.categoryId}" }) { stat ->
                RankedCategoryStatRowCard(
                    stat = stat,
                    categoryType = uiState.selectedCategoryType,
                    isHighlighted = stat.categoryId == uiState.highlightedDonutCategoryId,
                    currencySpec = uiState.currencySpec,
                    grouping = uiState.numberGrouping,
                    onClick = { onOpenCategoryDrillDown(stat) }
                )
            }
        }

        // Trend Analytics Section with Option A Toggle Chip
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Trend Analytics",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Segmented Toggle Chips: [ 📊 Cashflow ] | [ 📈 Net Worth ] | [ 💳 Credit Health ]
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilterChip(
                        selected = uiState.selectedTrendChartMode == TrendChartMode.INCOME_VS_EXPENSE,
                        onClick = { onSelectTrendChartMode(TrendChartMode.INCOME_VS_EXPENSE) },
                        label = { Text("📊 Cashflow", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = uiState.selectedTrendChartMode == TrendChartMode.NET_WORTH,
                        onClick = { onSelectTrendChartMode(TrendChartMode.NET_WORTH) },
                        label = { Text("📈 Net Worth", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = uiState.selectedTrendChartMode == TrendChartMode.CREDIT_ANALYSIS,
                        onClick = { onSelectTrendChartMode(TrendChartMode.CREDIT_ANALYSIS) },
                        label = { Text("💳 Credit Health", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.weight(1f)
                    )
                }

                when (uiState.selectedTrendChartMode) {
                    TrendChartMode.INCOME_VS_EXPENSE -> {
                        SixMonthTrendBarChart(
                            bars = uiState.sixMonthTrendBars,
                            currencySpec = uiState.currencySpec,
                            grouping = uiState.numberGrouping
                        )
                    }
                    TrendChartMode.NET_WORTH -> {
                        NetWorthGrowthCanvasChart(
                            trendItems = uiState.sixMonthNetWorthTrend,
                            selectedFilter = uiState.selectedNetWorthMetricFilter,
                            currencySpec = uiState.currencySpec,
                            grouping = uiState.numberGrouping,
                            onSelectFilter = onSelectNetWorthChartMetricFilter
                        )
                    }
                    TrendChartMode.CREDIT_ANALYSIS -> {
                        CreditHealthDashboardCard(
                            totalAvailableCredit = uiState.totalAvailableCredit,
                            totalCreditLimit = uiState.totalCreditLimit,
                            totalUsedCredit = uiState.totalUsedCredit,
                            overallUtilizationPercent = uiState.overallCreditUtilizationPercent,
                            creditCardHealthList = uiState.creditCardHealthList,
                            currencySpec = uiState.currencySpec,
                            grouping = uiState.numberGrouping
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RankedCategoryStatRowCard(
    stat: CategorySpendStat,
    categoryType: CategoryType,
    isHighlighted: Boolean,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem,
    onClick: () -> Unit
) {
    val semanticColors = LocalFolioSemanticColors.current
    val catColor = remember(stat.colorHex) { parseHexColorSafely(stat.colorHex) }

    val targetFraction = (stat.percentageOfTotal / 100f).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = targetFraction,
        animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing),
        label = "CategoryBarProgress"
    )

    val pctFormatted = remember(stat.percentageOfTotal) {
        String.format(Locale.US, "%.1f%%", stat.percentageOfTotal)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted) {
                MaterialTheme.colorScheme.surfaceContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            }
        ),
        border = BorderStroke(
            width = if (isHighlighted) 1.5.dp else 1.dp,
            color = if (isHighlighted) catColor else semanticColors.cardStrokeSubtle
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                CategoryIconBadge(
                    iconName = stat.iconName,
                    colorHex = stat.colorHex,
                    size = 40.dp,
                    iconSize = 20.dp
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stat.categoryName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = catColor.copy(alpha = 0.16f)
                        ) {
                            Text(
                                text = pctFormatted,
                                style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                                fontWeight = FontWeight.Bold,
                                color = catColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = "${stat.transactionCount} transactions in period",
                        style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                MoneyText(
                    money = stat.netAmount,
                    currencySpec = currencySpec,
                    grouping = grouping,
                    colorMode = if (categoryType == CategoryType.EXPENSE) {
                        MoneyColorMode.EXPENSE
                    } else {
                        MoneyColorMode.INCOME
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(4.dp))

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Inspect Category",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { animatedProgress },
                color = catColor,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                strokeCap = StrokeCap.Round,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            )
        }
    }
}

@Composable
private fun BudgetsManagerSubTabContent(
    uiState: StatsAndBudgetsUiState,
    onOpenOverallBudgetEditor: () -> Unit,
    onOpenAddCategoryBudget: () -> Unit,
    onOpenEditBudget: (BudgetProgress) -> Unit
) {
    val overallBudget = uiState.overallBudgetProgress
    val categoryBudgets = uiState.categoryBudgetProgressList

    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 12.dp,
            bottom = 96.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            ThreeColumnSummaryBanner(
                firstLabel = "Income",
                firstAmount = uiState.periodSummary.totalIncome,
                secondLabel = "Expenses",
                secondAmount = uiState.periodSummary.totalExpense,
                thirdLabel = "Net Cashflow",
                thirdAmount = uiState.periodSummary.netCashflow,
                currencySpec = uiState.currencySpec,
                grouping = uiState.numberGrouping
            )
        }

        // Hero Overall Monthly Budget Card or Set Monthly Budget CTA
        item {
            if (overallBudget != null) {
                BudgetProgressCard(
                    budget = overallBudget,
                    isHeroOverall = true,
                    currencySpec = uiState.currencySpec,
                    grouping = uiState.numberGrouping,
                    onClick = { onOpenEditBudget(overallBudget) }
                )
            } else {
                EmptyStateCard(
                    title = "Set Overall Monthly Budget",
                    subtitle = "Configure a master spending target with customizable 80% warning alerts for ${uiState.activePeriodRange.title}.",
                    icon = Icons.Filled.Savings,
                    actionLabel = "Set Monthly Budget",
                    onActionClick = onOpenOverallBudgetEditor
                )
            }
        }

        // Category Budgets Section Header + Add Button
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Category Budgets (${categoryBudgets.size})",
                        style = tabularMoneyStyle(MaterialTheme.typography.titleMedium),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Track limits per expense category (${uiState.activePeriodRange.title})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                FilledTonalButton(
                    onClick = onOpenAddCategoryBudget,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add Category Budget",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Add Budget",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        if (categoryBudgets.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    border = BorderStroke(1.dp, LocalFolioSemanticColors.current.cardStrokeSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No category-specific budgets configured yet. Tap \"+ Add Budget\" above to set limits for Food, Groceries, Fuel, or Shopping.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(20.dp)
                    )
                }
            }
        } else {
            items(categoryBudgets, key = { it.budgetId }) { budget ->
                BudgetProgressCard(
                    budget = budget,
                    isHeroOverall = false,
                    currencySpec = uiState.currencySpec,
                    grouping = uiState.numberGrouping,
                    onClick = { onOpenEditBudget(budget) }
                )
            }
        }
    }
}

@Composable
private fun BudgetProgressCard(
    budget: BudgetProgress,
    isHeroOverall: Boolean,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem,
    onClick: () -> Unit
) {
    val semanticColors = LocalFolioSemanticColors.current
    val isExceeded = budget.usagePercentage >= 100f || budget.remainingAmount.isNegative
    val isNearAlert = !isExceeded && budget.usagePercentage >= budget.alertThresholdPercent.toFloat()

    val statusColor = when {
        isExceeded -> semanticColors.expenseCoral
        isNearAlert -> semanticColors.warningAmber
        else -> semanticColors.incomeEmerald
    }

    val statusContainerColor = when {
        isExceeded -> semanticColors.expenseCoralContainer
        isNearAlert -> semanticColors.warningAmberContainer
        else -> semanticColors.incomeEmeraldContainer
    }

    val onStatusContainerColor = when {
        isExceeded -> semanticColors.onExpenseCoralContainer
        isNearAlert -> semanticColors.onWarningAmberContainer
        else -> semanticColors.onIncomeEmeraldContainer
    }

    val animatedFraction by animateFloatAsState(
        targetValue = (budget.usagePercentage / 100f).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "BudgetUsageFraction"
    )

    val pctBadgeText = remember(budget.usagePercentage) {
        String.format(Locale.US, "%.1f%%", budget.usagePercentage)
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHeroOverall) {
                MaterialTheme.colorScheme.surfaceContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            }
        ),
        border = BorderStroke(
            width = if (isHeroOverall || isExceeded) 1.5.dp else 1.dp,
            color = when {
                isExceeded -> semanticColors.expenseCoral
                isHeroOverall -> MaterialTheme.colorScheme.primary
                else -> semanticColors.cardStrokeSubtle
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    CategoryIconBadge(
                        iconName = if (isHeroOverall) "savings" else "category",
                        colorHex = budget.colorHex,
                        size = if (isHeroOverall) 42.dp else 36.dp,
                        iconSize = if (isHeroOverall) 22.dp else 18.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isHeroOverall) "Overall Monthly Budget" else budget.categoryName,
                            style = if (isHeroOverall) {
                                MaterialTheme.typography.titleMedium
                            } else {
                                MaterialTheme.typography.titleSmall
                            },
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Alert threshold at ${budget.alertThresholdPercent}%",
                            style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = statusContainerColor
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            if (isExceeded || isNearAlert) {
                                Icon(
                                    imageVector = Icons.Filled.Warning,
                                    contentDescription = null,
                                    tint = onStatusContainerColor,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = pctBadgeText,
                                style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                                fontWeight = FontWeight.Bold,
                                color = onStatusContainerColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit Budget",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            LinearProgressIndicator(
                progress = { animatedFraction },
                color = statusColor,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                strokeCap = StrokeCap.Round,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isHeroOverall) 10.dp else 8.dp)
                    .clip(RoundedCornerShape(5.dp))
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Spent",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    MoneyText(
                        money = budget.spentAmount,
                        currencySpec = currencySpec,
                        grouping = grouping,
                        colorMode = MoneyColorMode.EXPENSE,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Budget Limit",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    MoneyText(
                        money = budget.limitAmount,
                        currencySpec = currencySpec,
                        grouping = grouping,
                        colorMode = MoneyColorMode.NEUTRAL,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (budget.remainingAmount.isNegative) "Over Budget" else "Remaining",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    MoneyText(
                        money = budget.remainingAmount,
                        currencySpec = currencySpec,
                        grouping = grouping,
                        colorMode = MoneyColorMode.AUTO_SIGN,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun TagsAndNotesRankingsSubTabContent(
    uiState: StatsAndBudgetsUiState,
    onSelectRankingMode: (RankingMode) -> Unit
) {
    val items = uiState.activeRankingItems

    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 12.dp,
            bottom = 96.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            ThreeColumnSummaryBanner(
                firstLabel = "Income",
                firstAmount = uiState.periodSummary.totalIncome,
                secondLabel = "Expenses",
                secondAmount = uiState.periodSummary.totalExpense,
                thirdLabel = "Net Cashflow",
                thirdAmount = uiState.periodSummary.netCashflow,
                currencySpec = uiState.currencySpec,
                grouping = uiState.numberGrouping
            )
        }

        // Toggle between #Tag Rankings and Payee / Note Rankings
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                RankingMode.entries.forEach { mode ->
                    val isSelected = uiState.selectedRankingMode == mode
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectRankingMode(mode) },
                        leadingIcon = {
                            Icon(
                                imageVector = if (mode == RankingMode.TAGS) {
                                    Icons.AutoMirrored.Filled.Label
                                } else {
                                    Icons.AutoMirrored.Filled.ReceiptLong
                                },
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = {
                            Text(
                                text = mode.title,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        if (items.isEmpty()) {
            item {
                EmptyStateCard(
                    title = if (uiState.selectedRankingMode == RankingMode.TAGS) {
                        "No Tagged Spend in ${uiState.activePeriodRange.title}"
                    } else {
                        "No Payee or Note Rankings Yet"
                    },
                    subtitle = if (uiState.selectedRankingMode == RankingMode.TAGS) {
                        "Attach #tags like #weekend, #family, or #travel when logging expenses to rank them here."
                    } else {
                        "Add Payee names or Notes to your transactions to see your top merchants and descriptions."
                    },
                    icon = if (uiState.selectedRankingMode == RankingMode.TAGS) {
                        Icons.AutoMirrored.Filled.Label
                    } else {
                        Icons.AutoMirrored.Filled.ReceiptLong
                    }
                )
            }
        } else {
            itemsIndexed(
                items = items,
                key = { idx, item -> "${uiState.selectedRankingMode}_${idx}_${item.label}" }
            ) { index, item ->
                SpendRankingItemCard(
                    rankNumber = index + 1,
                    item = item,
                    isTagMode = uiState.selectedRankingMode == RankingMode.TAGS,
                    currencySpec = uiState.currencySpec,
                    grouping = uiState.numberGrouping
                )
            }
        }
    }
}

@Composable
private fun SpendRankingItemCard(
    rankNumber: Int,
    item: SpendRankingItem,
    isTagMode: Boolean,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem
) {
    val semanticColors = LocalFolioSemanticColors.current
    val animatedFraction by animateFloatAsState(
        targetValue = (item.percentageOfTotal / 100f).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "RankingShareFraction"
    )
    val pctFormatted = remember(item.percentageOfTotal) {
        String.format(Locale.US, "%.1f%% of spend", item.percentageOfTotal)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "#$rankNumber",
                            style = tabularMoneyStyle(MaterialTheme.typography.labelMedium),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isTagMode && !item.label.startsWith("#")) {
                            "#${item.label}"
                        } else {
                            item.label
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${item.transactionCount} transactions · $pctFormatted",
                        style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                MoneyText(
                    money = item.totalAmount,
                    currencySpec = currencySpec,
                    grouping = grouping,
                    colorMode = MoneyColorMode.EXPENSE,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            LinearProgressIndicator(
                progress = { animatedFraction },
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                strokeCap = StrokeCap.Round,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            )
        }
    }
}

private data class SubcategoryBreakdownItem(
    val name: String,
    val totalAmount: MinorMoney,
    val transactionCount: Int,
    val percentageOfParent: Float
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDrillDownBottomSheet(
    drillDownState: CategoryDrillDownState,
    periodTitle: String,
    sixMonthTrendBars: List<com.folioledger.app.domain.model.MonthlyTrendBar>,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem,
    onConfigureBudgetForCategory: () -> Unit,
    onTransactionClick: (TransactionItem) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val semanticColors = LocalFolioSemanticColors.current
    val stat = drillDownState.categoryStat
    val catColor = remember(stat.colorHex) { parseHexColorSafely(stat.colorHex) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.US) }

    val subcategoryBreakdown = remember(drillDownState.transactions, stat.netAmount) {
        val amountBySubcategory = mutableMapOf<String, Long>()
        val countBySubcategory = mutableMapOf<String, Int>()

        for (tx in drillDownState.transactions) {
            if (tx.isSplit && tx.splits.isNotEmpty()) {
                for (split in tx.splits) {
                    val subName = split.categoryName.ifBlank { "Uncategorized Split" }
                    val subAmount = Math.abs(split.amount.amountMinor)
                    amountBySubcategory[subName] = (amountBySubcategory[subName] ?: 0L) + subAmount
                    countBySubcategory[subName] = (countBySubcategory[subName] ?: 0) + 1
                }
            } else {
                val subName = tx.categoryName?.ifBlank { null } ?: stat.categoryName
                val subAmount = Math.abs(tx.amount.amountMinor)
                amountBySubcategory[subName] = (amountBySubcategory[subName] ?: 0L) + subAmount
                countBySubcategory[subName] = (countBySubcategory[subName] ?: 0) + 1
            }
        }

        val parentTotalMinor = stat.netAmount.amountMinor.coerceAtLeast(1L)
        amountBySubcategory.map { (subName, minorSum) ->
            val pct = (minorSum.toFloat() / parentTotalMinor.toFloat() * 100f).coerceIn(0f, 100f)
            SubcategoryBreakdownItem(
                name = subName,
                totalAmount = MinorMoney(minorSum),
                transactionCount = countBySubcategory[subName] ?: 1,
                percentageOfParent = pct
            )
        }.sortedByDescending { it.totalAmount.amountMinor }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        LazyColumn(
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        CategoryIconBadge(
                            iconName = stat.iconName,
                            colorHex = stat.colorHex,
                            size = 44.dp,
                            iconSize = 22.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = stat.categoryName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$periodTitle · ${stat.transactionCount} transactions (${
                                    String.format(Locale.US, "%.1f%%", stat.percentageOfTotal)
                                })",
                                style = tabularMoneyStyle(MaterialTheme.typography.bodySmall),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    MoneyText(
                        money = stat.netAmount,
                        currencySpec = currencySpec,
                        grouping = grouping,
                        colorMode = if (drillDownState.categoryType == CategoryType.EXPENSE) {
                            MoneyColorMode.EXPENSE
                        } else {
                            MoneyColorMode.INCOME
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Close")
                    }
                }
            }

            if (drillDownState.categoryType == CategoryType.EXPENSE) {
                item {
                    val budget = drillDownState.matchingBudget
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, catColor.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            if (budget != null) {
                                Column {
                                    Text(
                                        text = "Category Budget (${String.format(Locale.US, "%.1f%%", budget.usagePercentage)} used)",
                                        style = tabularMoneyStyle(MaterialTheme.typography.labelMedium),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Row {
                                        Text(
                                            text = "Limit: ",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        MoneyText(
                                            money = budget.limitAmount,
                                            currencySpec = currencySpec,
                                            grouping = grouping,
                                            colorMode = MoneyColorMode.NEUTRAL,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                OutlinedButton(
                                    onClick = onConfigureBudgetForCategory,
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Edit Budget", style = MaterialTheme.typography.labelMedium)
                                }
                            } else {
                                Text(
                                    text = "No monthly budget set for ${stat.categoryName}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                FilledTonalButton(
                                    onClick = onConfigureBudgetForCategory,
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Set Budget", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }
            }

            if (subcategoryBreakdown.size > 1 || (subcategoryBreakdown.size == 1 && subcategoryBreakdown.first().name != stat.categoryName)) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Subcategory Breakdown (${subcategoryBreakdown.size})",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Share of ${stat.categoryName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            HorizontalDivider(color = semanticColors.cardStrokeSubtle)

                            subcategoryBreakdown.forEach { subItem ->
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = subItem.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = String.format(
                                                    Locale.US,
                                                    "%.1f%% · %d trans",
                                                    subItem.percentageOfParent,
                                                    subItem.transactionCount
                                                ),
                                                style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        MoneyText(
                                            money = subItem.totalAmount,
                                            currencySpec = currencySpec,
                                            grouping = grouping,
                                            colorMode = if (drillDownState.categoryType == CategoryType.EXPENSE) {
                                                MoneyColorMode.EXPENSE
                                            } else {
                                                MoneyColorMode.INCOME
                                            },
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    val fraction = (subItem.percentageOfParent / 100f).coerceIn(0f, 1f)
                                    LinearProgressIndicator(
                                        progress = { fraction },
                                        color = catColor,
                                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                        strokeCap = StrokeCap.Round,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(5.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (sixMonthTrendBars.isNotEmpty()) {
                item {
                    SixMonthSmoothTrendAreaChart(
                        bars = sixMonthTrendBars,
                        currencySpec = currencySpec,
                        grouping = grouping,
                        categoryType = drillDownState.categoryType,
                        chartHeight = 160.dp
                    )
                }
            }

            item {
                Text(
                    text = "Transactions in ${stat.categoryName} (${drillDownState.transactions.size})",
                    style = tabularMoneyStyle(MaterialTheme.typography.titleSmall),
                    fontWeight = FontWeight.Bold
                )
            }

            if (drillDownState.transactions.isEmpty()) {
                item {
                    Text(
                        text = "No matching transactions found for this category.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(drillDownState.transactions, key = { "drill_${it.id}" }) { tx ->
                    val dateLabel = remember(tx.ledgerDateEpochDay) {
                        LocalDate.ofEpochDay(tx.ledgerDateEpochDay).format(dateFormatter)
                    }
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onTransactionClick(tx) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tx.payeeOrDescription
                                        .ifBlank { tx.note }
                                        .ifBlank { tx.categoryName ?: stat.categoryName },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "$dateLabel · ${tx.accountName}",
                                    style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            MoneyText(
                                money = tx.amount,
                                currencySpec = currencySpec,
                                grouping = grouping,
                                colorMode = MoneyColorMode.fromTransactionType(tx.type),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BudgetEditorBottomSheet(
    state: BudgetEditorDialogState,
    expenseCategories: List<CategoryNode>,
    currencySpec: CurrencySpec,
    isSaving: Boolean,
    onUpdateState: ((BudgetEditorDialogState) -> BudgetEditorDialogState) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val semanticColors = LocalFolioSemanticColors.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, bottom = 32.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CategoryIconBadge(
                        iconName = if (state.isOverallMonthlyBudget) "savings" else "category",
                        colorHex = state.selectedCategoryColorHex,
                        size = 40.dp,
                        iconSize = 20.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (state.isEditing) "Edit Monthly Budget" else "New Monthly Budget",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Filled.Close, contentDescription = "Close")
                }
            }

            if (!state.validationError.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = semanticColors.expenseCoralContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = state.validationError,
                        style = MaterialTheme.typography.labelMedium,
                        color = semanticColors.onExpenseCoralContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

            // Scope Switcher: Overall Monthly Budget vs Specific Expense Category
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilterChip(
                    selected = state.isOverallMonthlyBudget,
                    onClick = {
                        onUpdateState {
                            it.copy(
                                isOverallMonthlyBudget = true,
                                selectedCategoryId = null,
                                selectedCategoryName = "Overall Monthly Budget",
                                selectedCategoryColorHex = "#059669"
                            )
                        }
                    },
                    label = { Text("Overall Monthly Budget") }
                )

                FilterChip(
                    selected = !state.isOverallMonthlyBudget,
                    onClick = {
                        val firstCat = expenseCategories.firstOrNull()
                        onUpdateState {
                            it.copy(
                                isOverallMonthlyBudget = false,
                                selectedCategoryId = it.selectedCategoryId ?: firstCat?.id,
                                selectedCategoryName = if (it.selectedCategoryId != null) {
                                    it.selectedCategoryName
                                } else {
                                    firstCat?.name ?: "Select Category"
                                },
                                selectedCategoryColorHex = firstCat?.colorHex ?: "#059669"
                            )
                        }
                    },
                    label = { Text("Specific Category") }
                )
            }

            if (!state.isOverallMonthlyBudget) {
                Text(
                    text = "Select Expense Category",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    expenseCategories.forEach { cat ->
                        FilterChip(
                            selected = state.selectedCategoryId == cat.id,
                            onClick = {
                                onUpdateState {
                                    it.copy(
                                        selectedCategoryId = cat.id,
                                        selectedCategoryName = cat.name,
                                        selectedCategoryColorHex = cat.colorHex
                                    )
                                }
                            },
                            label = { Text(cat.name) }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = state.limitAmountInput,
                onValueChange = { v -> onUpdateState { it.copy(limitAmountInput = v) } },
                label = { Text("Monthly Limit Amount (${currencySpec.symbol})") },
                placeholder = { Text("e.g. 50000 or 15000+5000") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Warning Alert Threshold",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${state.alertThresholdPercent}%",
                        style = tabularMoneyStyle(MaterialTheme.typography.labelLarge),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Slider(
                    value = state.alertThresholdPercent.toFloat(),
                    onValueChange = { value ->
                        onUpdateState { it.copy(alertThresholdPercent = value.toInt().coerceIn(50, 100)) }
                    },
                    valueRange = 50f..100f,
                    steps = 9
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isEditing && state.budgetId > 0L) {
                    OutlinedButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = semanticColors.expenseCoral
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Delete")
                    }
                }

                Button(
                    onClick = onSave,
                    enabled = !isSaving,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (state.isEditing) "Save Budget" else "Create Budget")
                }
            }
        }
    }
}