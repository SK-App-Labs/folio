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

private enum class BudgetsSubTabSection(val title: String) {
    EXPENSE_BUDGETS("Expense Budgets"),
    WEALTH_AND_SAVINGS("Wealth & Savings")
}

@Composable
internal fun BudgetsManagerSubTabContent(
    uiState: StatsAndBudgetsUiState,
    onOpenOverallBudgetEditor: () -> Unit,
    onOpenAddCategoryBudget: () -> Unit,
    onOpenEditBudget: (BudgetProgress) -> Unit,
    onOpenMonthlySavingsTargetDialog: () -> Unit,
    onOpenAddSavingsGoal: () -> Unit,
    onOpenEditSavingsGoal: (AccountSavingsGoal) -> Unit
) {
    val semanticColors = LocalFolioSemanticColors.current
    val haptic = LocalHapticFeedback.current
    var selectedSection by rememberSaveable { mutableStateOf(BudgetsSubTabSection.EXPENSE_BUDGETS) }

    val overallBudget = uiState.overallBudgetProgress
    val categoryBudgets = uiState.categoryBudgetProgressList

    val allActiveBudgets = remember(overallBudget, categoryBudgets) {
        listOfNotNull(overallBudget) + categoryBudgets
    }
    val exceededBudgets = remember(allActiveBudgets) {
        allActiveBudgets.filter { it.usagePercentage >= 100f || it.remainingAmount.isNegative }
    }
    val nearWarningBudgets = remember(allActiveBudgets, exceededBudgets) {
        allActiveBudgets.filter { !exceededBudgets.contains(it) && it.usagePercentage >= it.alertThresholdPercent.toFloat() }
    }

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
        // Mode Switcher: Expense Budgets vs Wealth & Savings
        item(key = "budgets_mode_switcher", contentType = "budgets_mode_switcher") {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                BudgetsSubTabSection.entries.forEach { section ->
                    val isSelected = selectedSection == section
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
                    val icon = if (section == BudgetsSubTabSection.EXPENSE_BUDGETS) {
                        Icons.Filled.PieChart
                    } else {
                        Icons.Filled.Savings
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = containerColor,
                        border = BorderStroke(1.dp, borderColor),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                selectedSection = section
                            }
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
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = section.title,
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

        when (selectedSection) {
            BudgetsSubTabSection.EXPENSE_BUDGETS -> {
                if (uiState.safeToSpend.isCurrentPeriod && uiState.shortfallAlerts.isNotEmpty()) {
                    item(key = "budget_shortfall_alert", contentType = "shortfall_alert") {
                        ShortfallAlertCard(
                            alerts = uiState.shortfallAlerts,
                            currencySpec = uiState.currencySpec,
                            grouping = uiState.numberGrouping
                        )
                    }
                }

                if (uiState.safeToSpend.isCurrentPeriod) {
                    item(key = "budget_safe_to_spend", contentType = "safe_to_spend") {
                        SafeToSpendCard(
                            safeToSpend = uiState.safeToSpend,
                            currencySpec = uiState.currencySpec,
                            grouping = uiState.numberGrouping
                        )
                    }
                }

                // Proactive Budget Threshold Alert Banner
                if (exceededBudgets.isNotEmpty() || nearWarningBudgets.isNotEmpty()) {
                    item(key = "budget_threshold_alert", contentType = "budget_threshold_alert") {
                        val isCritical = exceededBudgets.isNotEmpty()
                        val bannerBg = if (isCritical) semanticColors.expenseCoralContainer else semanticColors.warningAmberContainer
                        val bannerFg = if (isCritical) semanticColors.onExpenseCoralContainer else semanticColors.onWarningAmberContainer
                        val bannerBorder = if (isCritical) semanticColors.expenseCoral else semanticColors.warningAmber

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = bannerBg),
                            border = BorderStroke(1.dp, bannerBorder.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(14.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Warning,
                                    contentDescription = null,
                                    tint = bannerFg,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = if (isCritical) {
                                            "${exceededBudgets.size} Budget${if (exceededBudgets.size > 1) "s" else ""} Exceeded!"
                                        } else {
                                            "${nearWarningBudgets.size} Budget${if (nearWarningBudgets.size > 1) "s" else ""} Near 80% Alert Threshold"
                                        },
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = bannerFg
                                    )
                                    val summaryDetail = buildString {
                                        if (exceededBudgets.isNotEmpty()) {
                                            append(exceededBudgets.joinToString(", ") { b ->
                                                val name = if (b.budgetId == -1L || b.categoryId == null) "Overall" else b.categoryName
                                                "$name (${String.format(Locale.US, "%.0f%%", b.usagePercentage)})"
                                            })
                                        }
                                        if (nearWarningBudgets.isNotEmpty()) {
                                            if (exceededBudgets.isNotEmpty()) append(" • ")
                                            append(nearWarningBudgets.joinToString(", ") { b ->
                                                val name = if (b.budgetId == -1L || b.categoryId == null) "Overall" else b.categoryName
                                                "$name (${String.format(Locale.US, "%.0f%%", b.usagePercentage)})"
                                            })
                                        }
                                    }
                                    Text(
                                        text = summaryDetail,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = bannerFg.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Hero Overall Monthly Budget Card or Set Monthly Budget CTA
                item(key = "overall_budget_card", contentType = "overall_budget_card") {
                    if (overallBudget != null) {
                        BudgetProgressCard(
                            budget = overallBudget,
                            isHeroOverall = true,
                            currencySpec = uiState.currencySpec,
                            grouping = uiState.numberGrouping,
                            activePeriodRange = uiState.activePeriodRange,
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
                item(key = "category_budgets_header", contentType = "category_budgets_header") {
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
                    item(key = "category_budgets_empty", contentType = "category_budgets_empty") {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            ),
                            border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
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
                    items(
                        items = categoryBudgets,
                        key = { it.budgetId },
                        contentType = { "budget_progress_card" }
                    ) { budget ->
                        BudgetProgressCard(
                            budget = budget,
                            isHeroOverall = false,
                            currencySpec = uiState.currencySpec,
                            grouping = uiState.numberGrouping,
                            activePeriodRange = uiState.activePeriodRange,
                            onClick = { onOpenEditBudget(budget) }
                        )
                    }
                }
            }

            BudgetsSubTabSection.WEALTH_AND_SAVINGS -> {
                // Double-Entry Monthly Savings Target Hero Card
                item(key = "monthly_savings_hero", contentType = "monthly_savings_hero") {
                    MonthlySavingsTargetHeroCard(
                        savingsProgress = uiState.monthlySavingsProgress,
                        currencySpec = uiState.currencySpec,
                        grouping = uiState.numberGrouping,
                        activePeriodTitle = uiState.activePeriodRange.title,
                        onConfigureTarget = onOpenMonthlySavingsTargetDialog
                    )
                }

                // Account-Backed Savings Goals ("Wealth Vaults") Section
                val savingsGoals = uiState.accountSavingsGoals
                item(key = "savings_goals_header", contentType = "savings_goals_header") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Savings Goals & Wealth Vaults (${savingsGoals.size})",
                                style = tabularMoneyStyle(MaterialTheme.typography.titleMedium),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Account-backed targets verified directly against live balances",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        FilledTonalButton(
                            onClick = onOpenAddSavingsGoal,
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Add Savings Goal",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Add Goal",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }

                if (savingsGoals.isEmpty()) {
                    item(key = "savings_goals_empty", contentType = "savings_goals_empty") {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            ),
                            border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "No account savings goals yet. Link a goal (e.g., Emergency Fund, House Down Payment) directly to any Savings or Investment account to track live ledger progress with zero envelope drift.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(20.dp)
                            )
                        }
                    }
                } else {
                    items(
                        items = savingsGoals,
                        key = { it.accountId },
                        contentType = { "account_savings_goal" }
                    ) { goal ->
                        AccountSavingsGoalCard(
                            goal = goal,
                            currencySpec = uiState.currencySpec,
                            grouping = uiState.numberGrouping,
                            onClick = { onOpenEditSavingsGoal(goal) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun BudgetTrackWithPacingTick(
    progressFraction: Float,
    elapsedFraction: Float?,
    alertThresholdPercent: Int,
    statusColor: Color,
    trackColor: Color,
    isHeroOverall: Boolean,
    modifier: Modifier = Modifier
) {
    val barHeight = if (isHeroOverall) 12.dp else 10.dp
    val tickOverflow = 4.dp
    val totalCanvasHeight = barHeight + tickOverflow * 2
    val onSurface = MaterialTheme.colorScheme.onSurface
    val primaryColor = MaterialTheme.colorScheme.primary

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(totalCanvasHeight)
    ) {
        val width = size.width
        val height = barHeight.toPx()
        val topOffset = tickOverflow.toPx()
        val cornerRadius = CornerRadius(height / 2f, height / 2f)

        // 1. Draw track background
        drawRoundRect(
            color = trackColor,
            topLeft = Offset(0f, topOffset),
            size = Size(width, height),
            cornerRadius = cornerRadius
        )

        // 2. Draw progress fill
        val fillWidth = (width * progressFraction).coerceIn(0f, width)
        if (fillWidth > 0f) {
            drawRoundRect(
                color = statusColor,
                topLeft = Offset(0f, topOffset),
                size = Size(fillWidth, height),
                cornerRadius = cornerRadius
            )
        }

        // 3. Draw alert threshold marker (e.g. 80%)
        val thresholdFraction = (alertThresholdPercent / 100f).coerceIn(0f, 1f)
        val thresholdX = width * thresholdFraction
        drawLine(
            color = onSurface.copy(alpha = 0.28f),
            start = Offset(thresholdX, topOffset),
            end = Offset(thresholdX, topOffset + height),
            strokeWidth = 2.dp.toPx()
        )

        // 4. Draw month progress pacing tick (if current period)
        if (elapsedFraction != null) {
            val tickX = width * elapsedFraction
            val tickWidth = 3.dp.toPx()
            val tickHeight = height + (tickOverflow.toPx() * 1.5f)
            val tickTop = topOffset - (tickOverflow.toPx() * 0.75f)

            // High contrast needle shadow/border
            drawRoundRect(
                color = onSurface.copy(alpha = 0.35f),
                topLeft = Offset(tickX - (tickWidth / 2f) - 1.dp.toPx(), tickTop - 1.dp.toPx()),
                size = Size(tickWidth + 2.dp.toPx(), tickHeight + 2.dp.toPx()),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )

            // Primary needle
            drawRoundRect(
                color = primaryColor,
                topLeft = Offset(tickX - (tickWidth / 2f), tickTop),
                size = Size(tickWidth, tickHeight),
                cornerRadius = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx())
            )
        }
    }
}

@Composable
internal fun BudgetProgressCard(
    budget: BudgetProgress,
    isHeroOverall: Boolean,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem,
    activePeriodRange: StatsPeriodRange,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
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

    val todayEpochDay = remember { LocalDate.now().toEpochDay() }
    val isCurrentPeriod = todayEpochDay in activePeriodRange.startEpochDay..activePeriodRange.endEpochDay
    val totalDays = remember(activePeriodRange.startEpochDay, activePeriodRange.endEpochDay) {
        (activePeriodRange.endEpochDay - activePeriodRange.startEpochDay + 1).coerceAtLeast(1L)
    }
    val elapsedDays = remember(todayEpochDay, activePeriodRange.startEpochDay, totalDays) {
        (todayEpochDay - activePeriodRange.startEpochDay + 1).coerceIn(1L, totalDays)
    }
    val elapsedMonthFraction = if (isCurrentPeriod) {
        (elapsedDays.toFloat() / totalDays.toFloat()).coerceIn(0f, 1f)
    } else {
        null
    }
    val elapsedPercent: Int? = elapsedMonthFraction?.let { (it * 100f).toInt() }

    Card(
        shape = RoundedCornerShape(16.dp),
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
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
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
                                text = when {
                                    isExceeded -> "OVERSPENT ($pctBadgeText)"
                                    isNearAlert -> "NEAR LIMIT ($pctBadgeText)"
                                    else -> "ON TRACK ($pctBadgeText)"
                                },
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

            BudgetTrackWithPacingTick(
                progressFraction = animatedFraction,
                elapsedFraction = elapsedMonthFraction,
                alertThresholdPercent = budget.alertThresholdPercent,
                statusColor = statusColor,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                isHeroOverall = isHeroOverall
            )

            if (isCurrentPeriod && elapsedPercent != null) {
                val usagePct = budget.usagePercentage
                val paceDiff = usagePct - elapsedPercent.toFloat()
                val isPacingFast = paceDiff > 10f && !isExceeded
                val isPacingWell = paceDiff < -10f && !isExceeded
                val usageInt = usagePct.toInt()

                val paceTextColor = when {
                    isExceeded -> semanticColors.expenseCoral
                    isPacingFast -> semanticColors.warningAmber
                    isPacingWell -> semanticColors.incomeEmerald
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = paceTextColor,
                            modifier = Modifier.size(6.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when {
                                isExceeded -> "Exceeded limit on Day $elapsedDays of $totalDays"
                                isPacingFast -> "Pacing Fast: $usageInt% spent vs $elapsedPercent% month elapsed"
                                isPacingWell -> "Pacing Well: $usageInt% spent vs $elapsedPercent% month elapsed"
                                else -> "On Track: $usageInt% spent vs $elapsedPercent% month elapsed"
                            },
                            style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                            fontWeight = FontWeight.SemiBold,
                            color = paceTextColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Day $elapsedDays of $totalDays",
                        style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

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
                        style = if (currencySpec.symbol.length > 2) MaterialTheme.typography.labelMedium else MaterialTheme.typography.titleSmall,
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
                        style = if (currencySpec.symbol.length > 2) MaterialTheme.typography.labelMedium else MaterialTheme.typography.titleSmall,
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
                        style = if (currencySpec.symbol.length > 2) MaterialTheme.typography.labelMedium else MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            HorizontalDivider(color = semanticColors.cardStrokeSubtle)

            // Spend Velocity / Burn Rate & Daily Safe Spend
            val today = remember { LocalDate.now() }
            val daysInMonth = remember(today) { today.lengthOfMonth() }
            val currentDay = remember(today) { today.dayOfMonth }
            val remainingDays = remember(daysInMonth, currentDay) { (daysInMonth - currentDay + 1).coerceAtLeast(1) }
            val monthElapsedRatio = remember(daysInMonth, currentDay) { (currentDay.toFloat() / daysInMonth.toFloat()).coerceAtLeast(0.01f) }
            val spendRatio = (budget.usagePercentage / 100f)
            val velocityRatio = spendRatio / monthElapsedRatio

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val (velocityText, velocityColor) = when {
                        isExceeded -> "⚠️ Spending limit exceeded" to semanticColors.expenseCoral
                        velocityRatio > 1.2f -> "⚡ Spending pace is ${String.format(Locale.US, "%.1f", velocityRatio)}× faster than normal" to semanticColors.warningAmber
                        velocityRatio < 0.85f -> "🌿 Spending pace is ${String.format(Locale.US, "%.0f", (1f - velocityRatio) * 100f)}% under expected pace" to semanticColors.incomeEmerald
                        else -> "✓ Spending pace on track" to MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    Text(
                        text = velocityText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = velocityColor
                    )
                }

                if (!isExceeded && budget.remainingAmount.isPositive) {
                    val dailySafeMinor = budget.remainingAmount.amountMinor / remainingDays
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Daily safe spend ($remainingDays days remaining):",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        MoneyText(
                            money = MinorMoney(dailySafeMinor),
                            currencySpec = currencySpec,
                            grouping = grouping,
                            colorMode = MoneyColorMode.INCOME,
                            style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}


@Composable
internal fun MonthlySavingsTargetHeroCard(
    savingsProgress: MonthlySavingsProgress,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem,
    activePeriodTitle: String,
    onConfigureTarget: () -> Unit,
    modifier: Modifier = Modifier
) {
    val semanticColors = LocalFolioSemanticColors.current
    val isConfigured = savingsProgress.isConfigured
    val isAchieved = savingsProgress.isAchieved

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onConfigureTarget)
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CategoryIconBadge(
                        iconName = "savings",
                        colorHex = if (isAchieved) "#10B981" else "#3B82F6",
                        size = 38.dp,
                        iconSize = 20.dp
                    )
                    Column {
                        Text(
                            text = "Monthly Wealth Savings",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Double-Entry Savings · $activePeriodTitle",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (isConfigured) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isAchieved) semanticColors.incomeEmeraldContainer else MaterialTheme.colorScheme.primaryContainer,
                        border = BorderStroke(
                            0.5.dp,
                            if (isAchieved) semanticColors.incomeEmerald else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = String.format(Locale.US, "%.1f%%", savingsProgress.progressPercentage),
                            style = tabularMoneyStyle(MaterialTheme.typography.labelMedium),
                            fontWeight = FontWeight.Bold,
                            color = if (isAchieved) semanticColors.onIncomeEmeraldContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = onConfigureTarget,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            text = "Set Target",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            if (!isConfigured) {
                Text(
                    text = "Track net deposits to Savings & Investments (\"Pay Yourself First\") independently from expense budgeting.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Net Transferred",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        MoneyText(
                            money = savingsProgress.savedAmount,
                            currencySpec = currencySpec,
                            grouping = grouping,
                            colorMode = MoneyColorMode.INCOME,
                            style = tabularMoneyStyle(MaterialTheme.typography.titleSmall),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Target Goal",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        MoneyText(
                            money = savingsProgress.targetAmount,
                            currencySpec = currencySpec,
                            grouping = grouping,
                            style = tabularMoneyStyle(MaterialTheme.typography.titleSmall),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Unallocated Surplus",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        MoneyText(
                            money = savingsProgress.unallocatedSurplus,
                            currencySpec = currencySpec,
                            grouping = grouping,
                            colorMode = if (savingsProgress.unallocatedSurplus.amountMinor >= 0L) MoneyColorMode.INCOME else MoneyColorMode.EXPENSE,
                            style = tabularMoneyStyle(MaterialTheme.typography.titleSmall),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                LinearProgressIndicator(
                    progress = { (savingsProgress.progressPercentage / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (isAchieved) semanticColors.incomeEmerald else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )

                Text(
                    text = if (isAchieved) {
                        "🎉 Monthly savings target achieved! Unallocated surplus is safe for extra investments or rollover."
                    } else {
                        val remainingFormatted = MoneyFormatterAndCalculator.formatMinorMoney(savingsProgress.remainingAmount, currencySpec)
                        "$remainingFormatted remaining to reach monthly target."
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isAchieved) semanticColors.incomeEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
internal fun AccountSavingsGoalCard(
    goal: AccountSavingsGoal,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val semanticColors = LocalFolioSemanticColors.current
    val isAchieved = goal.isAchieved

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CategoryIconBadge(
                        iconName = goal.iconName,
                        colorHex = goal.colorHex,
                        size = 36.dp,
                        iconSize = 18.dp
                    )
                    Column {
                        Text(
                            text = goal.goalTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = goal.accountName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isAchieved) semanticColors.incomeEmeraldContainer else MaterialTheme.colorScheme.primaryContainer,
                    border = BorderStroke(
                        0.5.dp,
                        if (isAchieved) semanticColors.incomeEmerald else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = if (isAchieved) "ACHIEVED" else String.format(Locale.US, "%.0f%%", goal.progressPercentage),
                        style = tabularMoneyStyle(MaterialTheme.typography.labelMedium),
                        fontWeight = FontWeight.Bold,
                        color = if (isAchieved) semanticColors.onIncomeEmeraldContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    MoneyText(
                        money = goal.currentBalance,
                        currencySpec = currencySpec,
                        grouping = grouping,
                        style = tabularMoneyStyle(MaterialTheme.typography.titleMedium),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "of",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    MoneyText(
                        money = goal.targetAmount,
                        currencySpec = currencySpec,
                        grouping = grouping,
                        style = tabularMoneyStyle(MaterialTheme.typography.bodyMedium),
                        colorMode = MoneyColorMode.NEUTRAL
                    )
                }

                if (!isAchieved) {
                    val remainingFormatted = MoneyFormatterAndCalculator.formatMinorMoney(goal.remainingAmount, currencySpec)
                    Text(
                        text = "$remainingFormatted left",
                        style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            LinearProgressIndicator(
                progress = { (goal.progressPercentage / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (isAchieved) semanticColors.incomeEmerald else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
            )

            Text(
                text = if (isAchieved) {
                    "🎉 Live account balance meets or exceeds goal target!"
                } else {
                    "Live ledger verified · Zero virtual envelope drift"
                },
                style = MaterialTheme.typography.labelSmall,
                color = if (isAchieved) semanticColors.incomeEmerald else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
