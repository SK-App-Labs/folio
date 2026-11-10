package com.folioledger.app.ui.ledger.subtabs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.folioledger.app.domain.model.CurrencySpec
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.NumberGroupingSystem
import com.folioledger.app.domain.model.SupportedCurrencies
import com.folioledger.app.domain.model.TransactionItem
import com.folioledger.app.domain.util.MoneyFormatterAndCalculator
import com.folioledger.app.ui.components.CategoryIconBadge
import com.folioledger.app.ui.components.EmptyStateCard
import com.folioledger.app.ui.components.MoneyColorMode
import com.folioledger.app.ui.components.MoneyText
import com.folioledger.app.ui.components.ThreeColumnSummaryBanner
import com.folioledger.app.ui.ledger.*
import com.folioledger.app.ui.theme.LocalFolioSemanticColors
import com.folioledger.app.ui.theme.tabularMoneyStyle
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
internal fun SummaryLedgerSubTabContent(
    uiState: LedgerHomeUiState,
    onExportCsvClick: () -> Unit
) {
    val semanticColors = LocalFolioSemanticColors.current
    val summary = uiState.periodSummary
    val closingBalanceWithCarryOver = summary.netCashflow + summary.carryOverFromPreviousPeriod

    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 12.dp,
            bottom = 32.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item(key = "summary_banner") {
            ThreeColumnSummaryBanner(
                periodSummary = summary,
                currencySpec = uiState.currencySpec,
                grouping = uiState.numberGrouping,
                showCarryOver = uiState.userPreferences.carryOverEnabled && uiState.scopedAccountId == null
            )
        }

        item(key = "cashflow_digest") {
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
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "${uiState.activePeriodRange.title} Cashflow Digest",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        val hasIncome = summary.totalIncome.isPositive
                        val hasExpense = summary.totalExpense.isPositive
                        val (rateBadgeText, rateContainerColor, rateContentColor) = when {
                            hasIncome && uiState.savingsRatePercent >= 0f -> Triple(
                                String.format(Locale.US, "Savings Rate: %.1f%%", uiState.savingsRatePercent),
                                semanticColors.incomeEmeraldContainer,
                                semanticColors.onIncomeEmeraldContainer
                            )
                            hasIncome && uiState.savingsRatePercent < 0f -> Triple(
                                String.format(Locale.US, "Savings Rate: %.1f%%", uiState.savingsRatePercent),
                                semanticColors.expenseCoralContainer,
                                semanticColors.onExpenseCoralContainer
                            )
                            hasExpense -> Triple(
                                "Deficit (No Income)",
                                semanticColors.expenseCoralContainer,
                                semanticColors.onExpenseCoralContainer
                            )
                            else -> Triple(
                                "No Activity",
                                MaterialTheme.colorScheme.surfaceContainerHighest,
                                MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = rateContainerColor
                        ) {
                            Text(
                                text = rateBadgeText,
                                style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                                fontWeight = FontWeight.Bold,
                                color = rateContentColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = semanticColors.cardStrokeSubtle)

                    SummaryMetricRow(
                        label = "Total Period Income",
                        amount = summary.totalIncome,
                        colorMode = MoneyColorMode.INCOME,
                        currencySpec = uiState.currencySpec,
                        grouping = uiState.numberGrouping
                    )
                    SummaryMetricRow(
                        label = "Total Period Expenses",
                        amount = summary.totalExpense,
                        colorMode = MoneyColorMode.EXPENSE,
                        currencySpec = uiState.currencySpec,
                        grouping = uiState.numberGrouping
                    )
                    SummaryMetricRow(
                        label = "Net Period Cashflow",
                        amount = summary.netCashflow,
                        colorMode = MoneyColorMode.AUTO_SIGN,
                        currencySpec = uiState.currencySpec,
                        grouping = uiState.numberGrouping
                    )

                    if (uiState.userPreferences.carryOverEnabled && uiState.scopedAccountId == null) {
                        HorizontalDivider(color = semanticColors.cardStrokeSubtle)
                        SummaryMetricRow(
                            label = "Carry-Over from Previous Period",
                            amount = summary.carryOverFromPreviousPeriod,
                            colorMode = MoneyColorMode.AUTO_SIGN,
                            currencySpec = uiState.currencySpec,
                            grouping = uiState.numberGrouping
                        )
                        SummaryMetricRow(
                            label = "Closing Cumulative Balance",
                            amount = closingBalanceWithCarryOver,
                            colorMode = MoneyColorMode.AUTO_SIGN,
                            currencySpec = uiState.currencySpec,
                            grouping = uiState.numberGrouping
                        )
                    }
                }
            }
        }

        if (uiState.accountPeriodFlows.isNotEmpty()) {
            item(key = "account_breakdown") {
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
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (uiState.scopedAccount != null) {
                                    "${uiState.scopedAccount.name} Flow Summary"
                                } else {
                                    "Account Breakdown"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${uiState.accountPeriodFlows.size} active",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        HorizontalDivider(color = semanticColors.cardStrokeSubtle)

                        uiState.accountPeriodFlows.forEachIndexed { index, flow ->
                            if (index > 0) {
                                HorizontalDivider(
                                    color = semanticColors.cardStrokeSubtle.copy(alpha = 0.5f),
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                            val flowCurrencySpec = SupportedCurrencies.fromCode(flow.currencyCode)
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = flow.accountName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.surfaceContainerHighest
                                    ) {
                                        Text(
                                            text = "${flow.transactionCount} trans",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row {
                                            Text(
                                                text = "In: ",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            MoneyText(
                                                money = flow.incomeTotal,
                                                currencySpec = flowCurrencySpec,
                                                grouping = uiState.numberGrouping,
                                                colorMode = MoneyColorMode.INCOME,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                        Row {
                                            Text(
                                                text = "Out: ",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            MoneyText(
                                                money = flow.expenseTotal,
                                                currencySpec = flowCurrencySpec,
                                                grouping = uiState.numberGrouping,
                                                colorMode = MoneyColorMode.EXPENSE,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Net: ",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        MoneyText(
                                            money = flow.netChange,
                                            currencySpec = flowCurrencySpec,
                                            grouping = uiState.numberGrouping,
                                            colorMode = MoneyColorMode.AUTO_SIGN,
                                            style = if (flowCurrencySpec.symbol.length > 2) MaterialTheme.typography.labelMedium else MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            showPlusSignForPositive = true
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (uiState.budgetProgressList.isNotEmpty() && uiState.scopedAccountId == null) {
            item(key = "budget_status") {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Budget Status",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        val budgetsToDisplay = remember(uiState.budgetProgressList) {
                            val overallBudget = uiState.budgetProgressList.firstOrNull { it.categoryId == null }
                            val categoryBudgets = uiState.budgetProgressList.filter { it.categoryId != null }
                            if (overallBudget != null) {
                                listOf(overallBudget) + categoryBudgets.take(2)
                            } else {
                                categoryBudgets.take(3)
                            }
                        }

                        budgetsToDisplay.forEachIndexed { idx, budget ->
                            if (idx > 0) {
                                HorizontalDivider(
                                    color = semanticColors.cardStrokeSubtle.copy(alpha = 0.5f)
                                )
                            }
                            val fraction = (budget.usagePercentage / 100f).coerceIn(0f, 1f)
                            val barColor = when {
                                budget.usagePercentage >= 100f -> semanticColors.expenseCoral
                                budget.usagePercentage >= budget.alertThresholdPercent -> semanticColors.warningAmber
                                else -> semanticColors.incomeEmerald
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = budget.categoryName,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = String.format(Locale.US, "%.1f%% used", budget.usagePercentage),
                                        style = tabularMoneyStyle(MaterialTheme.typography.labelMedium),
                                        fontWeight = FontWeight.Bold,
                                        color = barColor
                                    )
                                }

                                LinearProgressIndicator(
                                    progress = { fraction },
                                    color = barColor,
                                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                    strokeCap = StrokeCap.Round,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                )

                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row {
                                        Text(
                                            text = "Spent: ",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        MoneyText(
                                            money = budget.spentAmount,
                                            currencySpec = uiState.currencySpec,
                                            grouping = uiState.numberGrouping,
                                            colorMode = MoneyColorMode.EXPENSE,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Row {
                                        Text(
                                            text = "Remaining: ",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        MoneyText(
                                            money = budget.remainingAmount,
                                            currencySpec = uiState.currencySpec,
                                            grouping = uiState.numberGrouping,
                                            colorMode = MoneyColorMode.AUTO_SIGN,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (uiState.topExpenseCategories.isNotEmpty()) {
            item(key = "top_expenses") {
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
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = if (uiState.scopedAccount != null) {
                                "Top Expenses (${uiState.scopedAccount.name})"
                            } else {
                                "Top Expense Categories"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        uiState.topExpenseCategories.forEach { stat ->
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
                                        text = stat.categoryName,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = String.format(
                                            Locale.US,
                                            "%.1f%% · %d trans",
                                            stat.percentageOfTotal,
                                            stat.transactionCount
                                        ),
                                        style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                MoneyText(
                                    money = stat.netAmount,
                                    currencySpec = uiState.currencySpec,
                                    grouping = uiState.numberGrouping,
                                    colorMode = MoneyColorMode.EXPENSE,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        item(key = "csv_export_button") {
            OutlinedButton(
                onClick = onExportCsvClick,
                enabled = !uiState.isExportingCsv,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.Download,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (uiState.isExportingCsv) {
                        "Exporting CSV..."
                    } else if (uiState.scopedAccount != null) {
                        "Export ${uiState.scopedAccount.name} (${uiState.activePeriodRange.title}) to CSV"
                    } else {
                        "Export ${uiState.activePeriodRange.title} Transactions to CSV"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
internal fun SummaryMetricRow(
    label: String,
    amount: MinorMoney,
    colorMode: MoneyColorMode,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        MoneyText(
            money = amount,
            currencySpec = currencySpec,
            grouping = grouping,
            colorMode = colorMode,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            showPlusSignForPositive = colorMode == MoneyColorMode.AUTO_SIGN
        )
    }
}
