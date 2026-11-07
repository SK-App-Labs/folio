package com.folioledger.app.ui.stats.sheets.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.folioledger.app.domain.model.CurrencySpec
import com.folioledger.app.domain.model.NumberGroupingSystem
import com.folioledger.app.domain.util.TagDateSpanFormatter
import com.folioledger.app.ui.components.MoneyColorMode
import com.folioledger.app.ui.components.MoneyText
import com.folioledger.app.ui.stats.TagProjectDrillDownState
import com.folioledger.app.ui.theme.LocalFolioSemanticColors

/**
 * Hero summary card for the Tag & Project drill-down sheet.
 * Adaptively renders either a Reimbursements settlement overview (with progress bar)
 * or an Event/Trip passbook overview (with duration, daily burn rate, and total spend).
 */
@Composable
fun TagProjectHeroSummaryCard(
    drillDownState: TagProjectDrillDownState,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem,
    modifier: Modifier = Modifier
) {
    val semanticColors = LocalFolioSemanticColors.current

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(14.dp)
        ) {
            if (drillDownState.isReimbursableProject) {
                // Reimbursement status badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (drillDownState.isFullySettled) {
                            semanticColors.incomeEmeraldContainer
                        } else {
                            semanticColors.warningAmberContainer
                        }
                    ) {
                        Text(
                            text = if (drillDownState.isFullySettled) {
                                "✓ Fully Settled & Reimbursed"
                            } else if (drillDownState.netPendingOwed.isPositive) {
                                "⏳ Pending Claim / Owed to You"
                            } else {
                                "✓ No Pending Dues"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (drillDownState.isFullySettled) {
                                semanticColors.onIncomeEmeraldContainer
                            } else {
                                semanticColors.onWarningAmberContainer
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = "${drillDownState.transactions.size} transactions",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                // 3-Column Metrics: Outlay | Reimbursed | Still Owed
                val reimbursableMetricStyle = if (currencySpec.symbol.length > 2) {
                    MaterialTheme.typography.labelMedium
                } else {
                    MaterialTheme.typography.titleSmall
                }

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "TOTAL SPENT",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.outline
                        )
                        MoneyText(
                            money = drillDownState.totalSpent,
                            currencySpec = currencySpec,
                            grouping = grouping,
                            colorMode = MoneyColorMode.EXPENSE,
                            style = reimbursableMetricStyle,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "REIMBURSED",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.outline
                        )
                        MoneyText(
                            money = drillDownState.totalReimbursed,
                            currencySpec = currencySpec,
                            grouping = grouping,
                            colorMode = MoneyColorMode.INCOME,
                            style = reimbursableMetricStyle,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "STILL OWED",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.outline
                        )
                        MoneyText(
                            money = drillDownState.netPendingOwed,
                            currencySpec = currencySpec,
                            grouping = grouping,
                            colorMode = if (drillDownState.netPendingOwed.isPositive) {
                                MoneyColorMode.NEUTRAL
                            } else {
                                MoneyColorMode.INCOME
                            },
                            style = reimbursableMetricStyle,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Settlement progress bar
                val progressFraction = if (drillDownState.totalSpent.amountMinor > 0L) {
                    (drillDownState.totalReimbursed.amountMinor.toFloat() / drillDownState.totalSpent.amountMinor.toFloat()).coerceIn(0f, 1f)
                } else {
                    1f
                }
                LinearProgressIndicator(
                    progress = { progressFraction },
                    color = semanticColors.incomeEmerald,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    strokeCap = StrokeCap.Round,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                )
            } else {
                // Event / Trip Hero: Duration | Daily Burn | Total Spend
                val dateSubtext = remember(drillDownState.minEpochDay, drillDownState.maxEpochDay) {
                    TagDateSpanFormatter.formatSpan(drillDownState.minEpochDay, drillDownState.maxEpochDay)
                }

                val catDrillMetricStyle = if (currencySpec.symbol.length > 2) {
                    MaterialTheme.typography.labelMedium
                } else {
                    MaterialTheme.typography.titleSmall
                }

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "DURATION",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "${drillDownState.dateSpanDays} ${if (drillDownState.dateSpanDays == 1) "Day" else "Days"}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = dateSubtext,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "DAILY BURN",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.outline
                        )
                        MoneyText(
                            money = drillDownState.dailyAverageSpend,
                            currencySpec = currencySpec,
                            grouping = grouping,
                            colorMode = MoneyColorMode.EXPENSE,
                            style = catDrillMetricStyle,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "per day avg",
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
                            text = "TOTAL SPENT",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.outline
                        )
                        MoneyText(
                            money = drillDownState.totalSpent,
                            currencySpec = currencySpec,
                            grouping = grouping,
                            colorMode = MoneyColorMode.EXPENSE,
                            style = catDrillMetricStyle,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${drillDownState.transactions.size} records",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}
