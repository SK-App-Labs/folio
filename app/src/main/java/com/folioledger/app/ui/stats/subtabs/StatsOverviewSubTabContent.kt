package com.folioledger.app.ui.stats.subtabs

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
import com.folioledger.app.ui.accounts.sheets.CreditCardSettlementBottomSheet
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
internal fun StatsBreakdownSubTabContent(
    uiState: StatsAndBudgetsUiState,
    onSelectOverviewMode: (OverviewMode) -> Unit,
    onSelectCategoryType: (CategoryType) -> Unit,
    onDonutSliceSelected: (CategorySpendStat?) -> Unit,
    onOpenCategoryDrillDown: (CategorySpendStat) -> Unit,
    onSelectTrendChartMode: (TrendChartMode) -> Unit,
    onSelectNetWorthChartMetricFilter: (NetWorthChartMetricFilter) -> Unit,
    onToggleRunwayIncludesInvestments: () -> Unit
) {
    val semanticColors = LocalFolioSemanticColors.current
    val activeStats = uiState.activeCategoryStats

    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 12.dp,
            bottom = 32.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // 1. Period Headline Summary Banner
        item(contentType = "stats_summary_banner") {
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

        // 2. 3-Mode Segmented Hero Switcher: [ Categories ] | [ Cashflow ] | [ Net Worth ]
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OverviewMode.entries.forEach { mode ->
                    val isSelected = uiState.selectedOverviewMode == mode
                    val icon = when (mode) {
                        OverviewMode.CATEGORIES -> Icons.Filled.PieChart
                        OverviewMode.CASHFLOW -> Icons.AutoMirrored.Filled.ShowChart
                        OverviewMode.NET_WORTH -> Icons.AutoMirrored.Filled.TrendingUp
                    }
                    val containerColor = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerLow
                    }
                    val contentColor = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    val borderColor = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        semanticColors.cardStrokeSubtle
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = containerColor,
                        border = BorderStroke(1.dp, borderColor),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelectOverviewMode(mode) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = contentColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = mode.title,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = contentColor,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // 3. Dynamic Mode-Matched Content
        when (uiState.selectedOverviewMode) {
            OverviewMode.CATEGORIES -> {
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
                                width = 1.dp,
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
                                width = 1.dp,
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
                item(contentType = "donut_chart") {
                    InteractiveCategoryDonutChart(
                        stats = activeStats,
                        currencySpec = uiState.currencySpec,
                        grouping = uiState.numberGrouping,
                        categoryType = uiState.selectedCategoryType,
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
                item(contentType = "category_list_header") {
                    Text(
                        text = if (uiState.selectedCategoryType == CategoryType.EXPENSE) {
                            "Expense Categories (${activeStats.size})"
                        } else {
                            "Income Categories (${activeStats.size})"
                        },
                        style = tabularMoneyStyle(MaterialTheme.typography.titleMedium),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )
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
                    item(key = "ranked_categories_grouped_card_${uiState.selectedCategoryType}", contentType = "ranked_categories_card") {
                        RankedCategoryBreakdownGroupedCard(
                            stats = activeStats,
                            categoryType = uiState.selectedCategoryType,
                            highlightedCategoryId = uiState.highlightedDonutCategoryId,
                            currencySpec = uiState.currencySpec,
                            grouping = uiState.numberGrouping,
                            onCategoryClick = onOpenCategoryDrillDown
                        )
                    }
                }
            }

            OverviewMode.CASHFLOW -> {
                // Hero: 6-Month Income vs. Expense Trend Bar Chart
                item(contentType = "cashflow_trend_chart") {
                    SixMonthTrendBarChart(
                        bars = uiState.sixMonthTrendBars,
                        currencySpec = uiState.currencySpec,
                        grouping = uiState.numberGrouping
                    )
                }

                // Period Cashflow Performance Summary
                item(contentType = "cashflow_performance_card") {
                    CashflowPerformanceInsightCard(
                        periodSummary = uiState.periodSummary,
                        priorPeriodSummary = uiState.priorPeriodSummary,
                        currencySpec = uiState.currencySpec,
                        grouping = uiState.numberGrouping
                    )
                }

                // Top Spending Drivers
                if (uiState.expenseCategoryStats.isNotEmpty()) {
                    item(contentType = "top_expense_drivers") {
                        TopDriversSummaryCard(
                            title = "Top Expense Drivers",
                            stats = uiState.expenseCategoryStats.take(3),
                            totalMinor = uiState.periodSummary.totalExpense,
                            currencySpec = uiState.currencySpec,
                            grouping = uiState.numberGrouping,
                            colorMode = MoneyColorMode.EXPENSE,
                            onCategoryClick = onOpenCategoryDrillDown
                        )
                    }
                }

                // Top Income Sources
                if (uiState.incomeCategoryStats.isNotEmpty()) {
                    item(contentType = "top_income_sources") {
                        TopDriversSummaryCard(
                            title = "Top Income Sources",
                            stats = uiState.incomeCategoryStats.take(3),
                            totalMinor = uiState.periodSummary.totalIncome,
                            currencySpec = uiState.currencySpec,
                            grouping = uiState.numberGrouping,
                            colorMode = MoneyColorMode.INCOME,
                            onCategoryClick = onOpenCategoryDrillDown
                        )
                    }
                }
            }

            OverviewMode.NET_WORTH -> {
                // Hero: Cubic Bezier Net Worth Growth Trend Canvas Chart
                item(contentType = "net_worth_trend_chart") {
                    NetWorthGrowthCanvasChart(
                        trendItems = uiState.sixMonthNetWorthTrend,
                        selectedFilter = uiState.selectedNetWorthMetricFilter,
                        currencySpec = uiState.currencySpec,
                        grouping = uiState.numberGrouping,
                        onSelectFilter = onSelectNetWorthChartMetricFilter
                    )
                }

                // Net Worth Velocity Card
                item(contentType = "net_worth_velocity") {
                    NetWorthVelocityCard(
                        velocity = uiState.netWorthVelocity,
                        currencySpec = uiState.currencySpec,
                        grouping = uiState.numberGrouping
                    )
                }

                // Emergency Runway Card
                item(contentType = "emergency_runway") {
                    EmergencyRunwayCard(
                        runway = uiState.emergencyRunway,
                        currencySpec = uiState.currencySpec,
                        grouping = uiState.numberGrouping,
                        onToggleIncludeInvestments = onToggleRunwayIncludesInvestments
                    )
                }
            }
        }
    }
}

@Composable
internal fun CashflowPerformanceInsightCard(
    periodSummary: PeriodLedgerSummary,
    priorPeriodSummary: PeriodLedgerSummary? = null,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem,
    modifier: Modifier = Modifier
) {
    val semanticColors = LocalFolioSemanticColors.current
    val totalIncomeMinor = periodSummary.totalIncome.amountMinor
    val totalExpenseMinor = periodSummary.totalExpense.amountMinor
    val netCashflowMinor = periodSummary.netCashflow.amountMinor
    val isPositive = netCashflowMinor >= 0L

    val savingsRatePct = if (totalIncomeMinor > 0L) {
        val rate = (netCashflowMinor.toDouble() / totalIncomeMinor.toDouble()) * 100.0
        rate.coerceIn(-100.0, 100.0)
    } else {
        null
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "CASHFLOW EFFICIENCY",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (savingsRatePct != null) {
                    val rateContainer = if (savingsRatePct >= 0) semanticColors.incomeEmeraldContainer else semanticColors.expenseCoralContainer
                    val rateContent = if (savingsRatePct >= 0) semanticColors.onIncomeEmeraldContainer else semanticColors.onExpenseCoralContainer
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = rateContainer
                    ) {
                        Text(
                            text = String.format(Locale.US, "Savings Rate: %+.1f%%", savingsRatePct),
                            style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                            fontWeight = FontWeight.Bold,
                            color = rateContent,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Net Cashflow",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    MoneyText(
                        money = periodSummary.netCashflow,
                        currencySpec = currencySpec,
                        grouping = grouping,
                        colorMode = if (isPositive) MoneyColorMode.INCOME else MoneyColorMode.EXPENSE,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Income vs. Expenses",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        MoneyText(
                            money = periodSummary.totalIncome,
                            currencySpec = currencySpec,
                            grouping = grouping,
                            colorMode = MoneyColorMode.INCOME,
                            style = tabularMoneyStyle(MaterialTheme.typography.bodyMedium),
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = " / ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        MoneyText(
                            money = periodSummary.totalExpense,
                            currencySpec = currencySpec,
                            grouping = grouping,
                            colorMode = MoneyColorMode.EXPENSE,
                            style = tabularMoneyStyle(MaterialTheme.typography.bodyMedium),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            if (priorPeriodSummary != null && priorPeriodSummary.totalExpense.amountMinor > 0L) {
                val priorExpenseMinor = priorPeriodSummary.totalExpense.amountMinor
                val expenseDelta = totalExpenseMinor - priorExpenseMinor
                val expenseDeltaPct = (expenseDelta.toDouble() / priorExpenseMinor.toDouble()) * 100.0
                val isExpenseIncrease = expenseDeltaPct > 0
                val isZero = kotlin.math.abs(expenseDeltaPct) < 0.1
                val arrow = if (isZero) "•" else if (isExpenseIncrease) "▲" else "▼"
                val sign = if (isExpenseIncrease) "+" else ""
                val deltaColor = if (isZero) MaterialTheme.colorScheme.onSurfaceVariant else if (isExpenseIncrease) semanticColors.expenseCoral else semanticColors.incomeEmerald

                HorizontalDivider(color = semanticColors.cardStrokeSubtle)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "vs. Prior Period Expense",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$arrow $sign${String.format(Locale.US, "%.1f%%", expenseDeltaPct)} (",
                            style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                            fontWeight = FontWeight.Bold,
                            color = deltaColor
                        )
                        MoneyText(
                            money = MinorMoney(kotlin.math.abs(expenseDelta)),
                            currencySpec = currencySpec,
                            grouping = grouping,
                            colorMode = MoneyColorMode.NEUTRAL,
                            style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = ")",
                            style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                            fontWeight = FontWeight.Bold,
                            color = deltaColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun TopDriversSummaryCard(
    title: String,
    stats: List<CategorySpendStat>,
    totalMinor: MinorMoney,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem,
    colorMode: MoneyColorMode = MoneyColorMode.EXPENSE,
    onCategoryClick: (CategorySpendStat) -> Unit,
    modifier: Modifier = Modifier
) {
    val semanticColors = LocalFolioSemanticColors.current
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            stats.forEach { stat ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onCategoryClick(stat) }
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        CategoryIconBadge(
                            iconName = stat.iconName,
                            colorHex = stat.colorHex,
                            size = 32.dp,
                            iconSize = 16.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = stat.categoryName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = String.format(Locale.US, "%.1f%% of total", stat.percentageOfTotal),
                                style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    MoneyText(
                        money = stat.netAmount,
                        currencySpec = currencySpec,
                        grouping = grouping,
                        colorMode = colorMode,
                        style = tabularMoneyStyle(MaterialTheme.typography.bodyLarge),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
@Composable
internal fun NetWorthVelocityCard(
    velocity: NetWorthVelocityState,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem,
    modifier: Modifier = Modifier
) {
    val semanticColors = LocalFolioSemanticColors.current
    val isPositive = velocity.isPositive
    val accentColor = if (isPositive) semanticColors.incomeEmerald else semanticColors.expenseCoral
    val badgeBg = if (isPositive) semanticColors.incomeEmeraldContainer.copy(alpha = 0.5f) else semanticColors.expenseCoralContainer.copy(alpha = 0.5f)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
        modifier = modifier.fillMaxWidth()
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(badgeBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPositive) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text(
                            text = "Net Worth Velocity",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "30-Day Growth Rate",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeBg
                ) {
                    Text(
                        text = "${if (isPositive) "+" else ""}${String.format(Locale.US, "%.1f", velocity.percentageChange)}% MoM",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "MONTH-OVER-MONTH CHANGE",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (isPositive) "+" else "-",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                        MoneyText(
                            money = velocity.absoluteChange.abs(),
                            currencySpec = currencySpec,
                            grouping = grouping,
                            colorMode = if (isPositive) MoneyColorMode.INCOME else MoneyColorMode.EXPENSE,
                            style = if (currencySpec.symbol.length > 2) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "CURRENT NET WORTH",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.outline
                    )
                    MoneyText(
                        money = velocity.currentNetWorth,
                        currencySpec = currencySpec,
                        grouping = grouping,
                        colorMode = MoneyColorMode.NEUTRAL,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
internal fun EmergencyRunwayCard(
    runway: EmergencyRunwayState,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem,
    onToggleIncludeInvestments: () -> Unit,
    modifier: Modifier = Modifier
) {
    val semanticColors = LocalFolioSemanticColors.current
    val tierColor = when (runway.healthTier) {
        RunwayHealthTier.RESILIENT -> semanticColors.incomeEmerald
        RunwayHealthTier.ADEQUATE -> semanticColors.warningAmber
        RunwayHealthTier.VULNERABLE -> semanticColors.expenseCoral
    }
    val tierBg = when (runway.healthTier) {
        RunwayHealthTier.RESILIENT -> semanticColors.incomeEmeraldContainer.copy(alpha = 0.5f)
        RunwayHealthTier.ADEQUATE -> semanticColors.warningAmberContainer.copy(alpha = 0.5f)
        RunwayHealthTier.VULNERABLE -> semanticColors.expenseCoralContainer.copy(alpha = 0.5f)
    }
    val tierLabel = when (runway.healthTier) {
        RunwayHealthTier.RESILIENT -> "Resilient (>6 mos)"
        RunwayHealthTier.ADEQUATE -> "Adequate (3–6 mos)"
        RunwayHealthTier.VULNERABLE -> "Vulnerable (<3 mos)"
    }

    val animatedFraction by animateFloatAsState(
        targetValue = (runway.runwayMonths / runway.targetMonths).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "RunwayProgress"
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(14.dp)
        ) {
            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(tierBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Shield,
                            contentDescription = null,
                            tint = tierColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text(
                            text = "Emergency Fund Runway",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = tierLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = tierColor
                        )
                    }
                }

                FilterChip(
                    selected = runway.includesInvestments,
                    onClick = onToggleIncludeInvestments,
                    label = {
                        Text(
                            text = if (runway.includesInvestments) "+ Investments" else "Liquid Only",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    modifier = Modifier.height(28.dp)
                )
            }

            // Big Metric + Progress Bar
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = String.format(Locale.US, "%.1f", runway.runwayMonths),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = tierColor
                        )
                        Text(
                            text = "Months Covered",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    Text(
                        text = "Target: ${String.format(Locale.US, "%.0f", runway.targetMonths)} mos",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                LinearProgressIndicator(
                    progress = { animatedFraction },
                    color = tierColor,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    strokeCap = StrokeCap.Round,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                )
            }

            HorizontalDivider(
                color = semanticColors.cardStrokeSubtle,
                thickness = 1.dp
            )

            // Sub-metrics: 2 columns
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = if (runway.includesInvestments) "TOTAL AVAILABLE FUNDS" else "LIQUID EMERGENCY FUNDS",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.outline
                    )
                    val funds = if (runway.includesInvestments) {
                        runway.totalLiquidFunds + runway.totalInvestmentFunds
                    } else {
                        runway.totalLiquidFunds
                    }
                    MoneyText(
                        money = funds,
                        currencySpec = currencySpec,
                        grouping = grouping,
                        colorMode = MoneyColorMode.NEUTRAL,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (runway.includesInvestments) "Bank + Cash + Savings + Invest" else "Bank + Cash + Savings",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "AVG MONTHLY LIVING EXPENSE",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.outline
                    )
                    MoneyText(
                        money = MinorMoney(runway.averageMonthlyBurnMinor),
                        currencySpec = currencySpec,
                        grouping = grouping,
                        colorMode = MoneyColorMode.EXPENSE,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Past 6-Month Average",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
internal fun ShortfallAlertCard(
    alerts: List<AccountShortfallAlert>,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem
) {
    val semanticColors = LocalFolioSemanticColors.current

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = semanticColors.warningAmberContainer),
        border = BorderStroke(1.dp, semanticColors.warningAmber.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = semanticColors.onWarningAmberContainer,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "14-Day Overdraft / Shortfall Warning",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = semanticColors.onWarningAmberContainer
                )
            }

            alerts.forEach { alert ->
                val balFormatted = remember(alert.currentBalance, currencySpec, grouping) {
                    MoneyFormatterAndCalculator.formatMinorMoney(alert.currentBalance, currencySpec)
                }
                val debFormatted = remember(alert.upcomingDebits, currencySpec, grouping) {
                    MoneyFormatterAndCalculator.formatMinorMoney(alert.upcomingDebits, currencySpec)
                }
                val shortFormatted = remember(alert.shortfallAmount, currencySpec, grouping) {
                    MoneyFormatterAndCalculator.formatMinorMoney(alert.shortfallAmount, currencySpec)
                }

                Text(
                    text = "⚠️ ${alert.accountName}: Current balance ($balFormatted) may not cover upcoming commitments ($debFormatted) due in ${alert.daysUntilShortfall} ${if (alert.daysUntilShortfall == 1L) "day" else "days"} (Shortfall: $shortFormatted).",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = semanticColors.onWarningAmberContainer
                )
            }
        }
    }
}

@Composable
internal fun SafeToSpendCard(
    safeToSpend: SafeToSpendState,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem
) {
    val semanticColors = LocalFolioSemanticColors.current

    val (statusText, statusDescription) = when (safeToSpend.status) {
        SafeToSpendStatus.SAFE -> Pair(
            "🛡️ Safe Discretionary Cash",
            if (safeToSpend.totalCreditCardDebt.isPositive && safeToSpend.upcoming14DayCommitments.isPositive) {
                "Unencumbered cash you can safely spend after accounting for card debt and bills due in the next 14 days."
            } else if (safeToSpend.totalCreditCardDebt.isPositive) {
                "Unencumbered cash you can safely spend after covering all outstanding credit card balances."
            } else if (safeToSpend.upcoming14DayCommitments.isPositive) {
                "Unencumbered cash you can safely spend after covering upcoming bills due in the next 14 days."
            } else {
                "Unencumbered cash you can safely spend with zero outstanding card debt or upcoming commitments."
            }
        )
        SafeToSpendStatus.CARD_DEBT_EXCEEDS_CASH -> Pair(
            "⚠️ Card Debt Exceeds Cash",
            "Your liquid bank balances are insufficient to cover your total credit card debt. You are carrying an unbacked credit float."
        )
        SafeToSpendStatus.BILLS_EXCEED_CASH -> Pair(
            "⚠️ Bills Exceed Cash",
            "Your liquid bank balances are insufficient to cover upcoming bills due in the next 14 days."
        )
        SafeToSpendStatus.BILLS_EXCEED_REMAINING_CASH -> Pair(
            "⚠️ Bills Exceed Remaining Cash",
            "Your liquid cash covers credit card debt, but is insufficient once bills due in the next 14 days are included."
        )
        SafeToSpendStatus.COMBINED_OBLIGATIONS_EXCEED_CASH -> Pair(
            "⚠️ Debt & Bills Exceed Cash",
            "Your liquid bank balances are insufficient to cover combined card debt and bills due in the next 14 days."
        )
        SafeToSpendStatus.ZERO_RESERVES -> Pair(
            "● Zero Liquid Cash",
            "No liquid cash reserves available to spend."
        )
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            // Header Row: Shield Icon, Title, Status Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (safeToSpend.isPositive) {
                            semanticColors.incomeEmeraldContainer
                        } else {
                            semanticColors.warningAmberContainer
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (safeToSpend.isPositive) Icons.Default.Shield else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (safeToSpend.isPositive) {
                                    semanticColors.onIncomeEmeraldContainer
                                } else {
                                    semanticColors.onWarningAmberContainer
                                },
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Text(
                        text = "Safe-to-Spend Today",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (safeToSpend.isPositive) {
                        semanticColors.incomeEmeraldContainer
                    } else {
                        semanticColors.warningAmberContainer
                    }
                ) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (safeToSpend.isPositive) {
                            semanticColors.onIncomeEmeraldContainer
                        } else {
                            semanticColors.onWarningAmberContainer
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Big Headline Money
            MoneyText(
                money = safeToSpend.trueSafeToSpend,
                currencySpec = currencySpec,
                grouping = grouping,
                colorMode = if (safeToSpend.isPositive) MoneyColorMode.INCOME else MoneyColorMode.EXPENSE,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black
            )

            Text(
                text = statusDescription,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider(color = semanticColors.cardStrokeSubtle)

            // Equation Breakdown Row: Liquid Cash - CC Debt - 14d Bills
            val equationMetricStyle = if (currencySpec.symbol.length > 2) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "LIQUID CASH",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.outline
                    )
                    MoneyText(
                        money = safeToSpend.totalLiquidBankAndCash,
                        currencySpec = currencySpec,
                        grouping = grouping,
                        colorMode = MoneyColorMode.NEUTRAL,
                        style = equationMetricStyle,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "−",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.outline
                )

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "CARD DEBT",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.outline
                    )
                    MoneyText(
                        money = safeToSpend.totalCreditCardDebt,
                        currencySpec = currencySpec,
                        grouping = grouping,
                        colorMode = MoneyColorMode.EXPENSE,
                        style = equationMetricStyle,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "−",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.outline
                )

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "14D BILLS",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.outline
                    )
                    MoneyText(
                        money = safeToSpend.upcoming14DayCommitments,
                        currencySpec = currencySpec,
                        grouping = grouping,
                        colorMode = MoneyColorMode.EXPENSE,
                        style = equationMetricStyle,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}


@Composable
internal fun RankedCategoryBreakdownGroupedCard(
    stats: List<CategorySpendStat>,
    categoryType: CategoryType,
    highlightedCategoryId: Long?,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem,
    onCategoryClick: (CategorySpendStat) -> Unit,
    modifier: Modifier = Modifier
) {
    val semanticColors = LocalFolioSemanticColors.current
    val haptic = LocalHapticFeedback.current
    var isExpanded by rememberSaveable(categoryType) { mutableStateOf(false) }

    val hasMore = stats.size > 5
    val visibleStats = if (isExpanded || !hasMore) stats else stats.take(5)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            visibleStats.forEachIndexed { index, stat ->
                if (index > 0) {
                    HorizontalDivider(
                        color = semanticColors.cardStrokeSubtle,
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )
                }
                RankedCategoryStatRowItem(
                    stat = stat,
                    categoryType = categoryType,
                    isHighlighted = stat.categoryId == highlightedCategoryId,
                    currencySpec = currencySpec,
                    grouping = grouping,
                    onClick = { onCategoryClick(stat) }
                )
            }

            if (hasMore) {
                HorizontalDivider(color = semanticColors.cardStrokeSubtle)
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.45f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            isExpanded = !isExpanded
                        }
                        .padding(vertical = 10.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isExpanded) {
                                "Show Top 5 Only"
                            } else {
                                "Show All ${stats.size} Categories (${stats.size - 5} more)"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun RankedCategoryStatRowItem(
    stat: CategorySpendStat,
    categoryType: CategoryType,
    isHighlighted: Boolean,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem,
    onClick: () -> Unit
) {
    val semanticColors = LocalFolioSemanticColors.current
    val haptic = LocalHapticFeedback.current
    val catColor = remember(stat.colorHex) { parseHexColorSafely(stat.colorHex) }
    var isSubcategoriesExpanded by rememberSaveable(stat.categoryId) { mutableStateOf(false) }

    val targetFraction = (stat.percentageOfTotal / 100f).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = targetFraction,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "CategoryBarProgress"
    )

    val pctFormatted = remember(stat.percentageOfTotal) {
        String.format(Locale.US, "%.1f%%", stat.percentageOfTotal)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (stat.subcategories.isNotEmpty()) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    isSubcategoriesExpanded = !isSubcategoriesExpanded
                } else {
                    onClick()
                }
            }
            .background(if (isHighlighted) MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f) else Color.Transparent)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            CategoryIconBadge(
                iconName = stat.iconName,
                colorHex = stat.colorHex,
                size = 32.dp,
                iconSize = 16.dp
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${stat.categoryName} (${stat.transactionCount})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (stat.changePercentage != null) {
                        val deltaPct = stat.changePercentage
                        val isIncrease = deltaPct > 0f
                        val isZero = kotlin.math.abs(deltaPct) < 0.1f
                        val deltaColor = if (isZero) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else if (categoryType == CategoryType.EXPENSE) {
                            if (isIncrease) semanticColors.expenseCoral else semanticColors.incomeEmerald
                        } else {
                            if (isIncrease) semanticColors.incomeEmerald else semanticColors.expenseCoral
                        }
                        val arrow = if (isZero) "•" else if (isIncrease) "▲" else "▼"
                        val sign = if (isIncrease) "+" else ""
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = deltaColor.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "$arrow $sign${String.format(Locale.US, "%.1f%%", deltaPct)}",
                                style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                                color = deltaColor,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    if (stat.subcategories.isNotEmpty()) {
                        Text(
                            text = if (stat.changePercentage != null) "• ${stat.subcategories.size} sub" else "${stat.subcategories.size} subcategories",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

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

            val trailingIcon = if (stat.subcategories.isNotEmpty()) {
                if (isSubcategoriesExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown
            } else {
                Icons.AutoMirrored.Filled.KeyboardArrowRight
            }
            Icon(
                imageVector = trailingIcon,
                contentDescription = if (stat.subcategories.isNotEmpty()) "Toggle subcategories" else "Inspect Category",
                tint = if (stat.subcategories.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        LinearProgressIndicator(
            progress = { animatedProgress },
            color = catColor,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            strokeCap = StrokeCap.Round,
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(1.5.dp))
        )

        if (stat.subcategories.isNotEmpty()) {
            AnimatedVisibility(
                visible = isSubcategoriesExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, start = 8.dp, end = 4.dp)
                ) {
                    stat.subcategories.forEach { sub ->
                        val subColor = remember(sub.colorHex) { parseHexColorSafely(sub.colorHex) }
                        val subProgress = (sub.percentageOfTotal / 100f).coerceIn(0f, 1f)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    CategoryIconBadge(
                                        iconName = sub.iconName,
                                        colorHex = sub.colorHex,
                                        size = 22.dp,
                                        iconSize = 12.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${sub.categoryName} (${sub.transactionCount})",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${String.format(Locale.US, "%.1f%%", sub.percentageOfTotal)} of parent",
                                        style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    MoneyText(
                                        money = sub.netAmount,
                                        currencySpec = currencySpec,
                                        grouping = grouping,
                                        colorMode = if (categoryType == CategoryType.EXPENSE) MoneyColorMode.EXPENSE else MoneyColorMode.INCOME,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { subProgress },
                                    color = subColor,
                                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                    strokeCap = StrokeCap.Round,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(2.5.dp)
                                        .clip(RoundedCornerShape(1.25.dp))
                                )
                            }
                        }
                    }

                    // 1-Tap Drilldown Link for the entire category
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onClick)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 12.dp)
                        ) {
                            Text(
                                text = "View All ${stat.categoryName} Transactions (${stat.transactionCount})",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

