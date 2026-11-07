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
import androidx.compose.ui.res.stringResource
import com.folioledger.app.R
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
internal fun MonthlyLedgerSubTabContent(
    uiState: LedgerHomeUiState,
    onToggleMonth: (Int) -> Unit,
    onDrillDownToMonth: (Long) -> Unit = {},
    onDrillDownToWeek: (Long, Int) -> Unit = { _, _ -> }
) {
    val semanticColors = LocalFolioSemanticColors.current
    val reversedMonthItems = remember(uiState.yearlyMonthItems) {
        uiState.yearlyMonthItems.reversed()
    }

    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 12.dp,
            bottom = 32.dp
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item(key = "monthly_summary_banner") {
            ThreeColumnSummaryBanner(
                firstLabel = "Annual Income",
                firstAmount = uiState.yearlyIncomeTotal,
                secondLabel = "Annual Expenses",
                secondAmount = uiState.yearlyExpenseTotal,
                thirdLabel = "Annual Net",
                thirdAmount = uiState.yearlyNetTotal,
                currencySpec = uiState.currencySpec,
                grouping = uiState.numberGrouping
            )
        }

        item(key = "monthly_year_header") {
            Text(
                text = "${uiState.activePeriodRange.anchorYear} Financial Year (12 Months)",
                style = tabularMoneyStyle(MaterialTheme.typography.titleSmall),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        items(
            items = reversedMonthItems,
            key = { "${it.periodRange.startEpochDay}_${it.monthIndex}" },
            contentType = { "yearly_month_card" }
        ) { monthItem ->
            val isExpanded = monthItem.monthIndex in uiState.expandedYearlyMonths

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (monthItem.isCurrentActiveMonth) {
                        MaterialTheme.colorScheme.surfaceContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerLow
                    }
                ),
                border = BorderStroke(
                    width = if (monthItem.isCurrentActiveMonth) 1.5.dp else 1.dp,
                    color = if (monthItem.isCurrentActiveMonth) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        semanticColors.cardStrokeSubtle
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleMonth(monthItem.monthIndex) }
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isExpanded) {
                                    Icons.Filled.ExpandLess
                                } else {
                                    Icons.Filled.ExpandMore
                                },
                                contentDescription = if (isExpanded) {
                                    "Collapse ${monthItem.periodRange.title}"
                                } else {
                                    "Expand ${monthItem.periodRange.title}"
                                },
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = monthItem.periodRange.title,
                                        style = tabularMoneyStyle(MaterialTheme.typography.titleMedium),
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (monthItem.isCurrentActiveMonth) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer
                                        ) {
                                            Text(
                                                text = stringResource(R.string.ledger_current_month_badge),
                                                style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "${monthItem.periodRange.subtitle} · ${monthItem.transactionCount} trans",
                                    style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Row(horizontalArrangement = Arrangement.spacedBy(if (uiState.currencySpec.symbol.length > 2) 6.dp else 10.dp)) {
                                MoneyText(
                                    money = monthItem.incomeTotal,
                                    currencySpec = uiState.currencySpec,
                                    grouping = uiState.numberGrouping,
                                    colorMode = MoneyColorMode.INCOME,
                                    style = if (uiState.currencySpec.symbol.length > 2) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                MoneyText(
                                    money = monthItem.expenseTotal,
                                    currencySpec = uiState.currencySpec,
                                    grouping = uiState.numberGrouping,
                                    colorMode = MoneyColorMode.EXPENSE,
                                    style = if (uiState.currencySpec.symbol.length > 2) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            MoneyText(
                                money = monthItem.netTotal,
                                currencySpec = uiState.currencySpec,
                                grouping = uiState.numberGrouping,
                                colorMode = MoneyColorMode.AUTO_SIGN,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                showPlusSignForPositive = true
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 14.dp, end = 14.dp, bottom = 12.dp)
                        ) {
                            HorizontalDivider(color = semanticColors.cardStrokeSubtle)

                            if (monthItem.transactionCount == 0) {
                                Text(
                                    text = stringResource(R.string.ledger_no_tx_for_month),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp)
                                )
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Weekly Breakdown",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                        Text(
                                            text = "In",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = semanticColors.incomeEmerald
                                        )
                                        Text(
                                            text = "Out",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = semanticColors.expenseCoral
                                        )
                                        Text(
                                            text = "Net",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                monthItem.weeklyBuckets.forEach { week ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable { onDrillDownToWeek(monthItem.periodRange.startEpochDay, week.weekIndex) }
                                            .padding(vertical = 4.dp, horizontal = 2.dp)
                                    ) {
                                        Text(
                                            text = "${week.label} (${week.dateRangeSubtitle})",
                                            style = tabularMoneyStyle(MaterialTheme.typography.labelMedium),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Row(horizontalArrangement = Arrangement.spacedBy(if (uiState.currencySpec.symbol.length > 2) 6.dp else 10.dp)) {
                                            MoneyText(
                                                money = week.incomeTotal,
                                                currencySpec = uiState.currencySpec,
                                                grouping = uiState.numberGrouping,
                                                colorMode = MoneyColorMode.INCOME,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                            MoneyText(
                                                money = week.expenseTotal,
                                                currencySpec = uiState.currencySpec,
                                                grouping = uiState.numberGrouping,
                                                colorMode = MoneyColorMode.EXPENSE,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                            MoneyText(
                                                money = week.netTotal,
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

                            HorizontalDivider(color = semanticColors.cardStrokeSubtle.copy(alpha = 0.5f))

                            Row(
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                TextButton(
                                    onClick = { onDrillDownToMonth(monthItem.periodRange.startEpochDay) },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "View in Daily →",
                                        style = MaterialTheme.typography.labelMedium,
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
}

