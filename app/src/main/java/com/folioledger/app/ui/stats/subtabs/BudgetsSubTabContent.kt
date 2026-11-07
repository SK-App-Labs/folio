package com.folioledger.app.ui.stats.subtabs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.folioledger.app.R
import com.folioledger.app.domain.model.AccountSavingsGoal
import com.folioledger.app.domain.model.BudgetProgress
import com.folioledger.app.ui.components.EmptyStateCard
import com.folioledger.app.ui.stats.StatsAndBudgetsUiState
import com.folioledger.app.ui.stats.components.AccountSavingsGoalCard
import com.folioledger.app.ui.stats.components.BudgetProgressCard
import com.folioledger.app.ui.stats.components.MonthlySavingsTargetHeroCard
import com.folioledger.app.ui.stats.components.SafeToSpendCard
import com.folioledger.app.ui.stats.components.ShortfallAlertCard
import com.folioledger.app.ui.theme.LocalFolioSemanticColors
import com.folioledger.app.ui.theme.tabularMoneyStyle
import java.util.Locale

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
                                text = "${stringResource(R.string.budget_category_budgets_title)} (${categoryBudgets.size})",
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
                                text = stringResource(R.string.budget_add_budget),
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
                                text = "${stringResource(R.string.budget_savings_goals_title)} (${savingsGoals.size})",
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
                                text = stringResource(R.string.budget_add_goal),
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
