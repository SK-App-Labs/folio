package com.folioledger.app.ui.stats.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.folioledger.app.domain.model.CurrencySpec
import com.folioledger.app.domain.model.NumberGroupingSystem
import com.folioledger.app.domain.model.TransactionItem
import com.folioledger.app.ui.components.MoneyColorMode
import com.folioledger.app.ui.components.MoneyText
import com.folioledger.app.ui.components.SixMonthTrendBarChart
import com.folioledger.app.ui.stats.TagProjectDrillDownState
import com.folioledger.app.ui.stats.sheets.components.TagCategoryDistributionSection
import com.folioledger.app.ui.stats.sheets.components.TagProjectHeroSummaryCard
import com.folioledger.app.ui.stats.sheets.components.TagTransactionRowItem
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Bottom sheet displaying comprehensive tag, trip, and project passbook drill-down metrics.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TagProjectDrillDownBottomSheet(
    drillDownState: TagProjectDrillDownState,
    periodTitle: String,
    isAllTime: Boolean,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem,
    onTransactionClick: (TransactionItem) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.US) }
    val cleanName = remember(drillDownState.tagName) {
        drillDownState.tagName.removePrefix("#").trim()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        LazyColumn(
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header: Tag Avatar, Tag Name, Mode Context Subtitle, Headline Amount, Close Button
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
                        Surface(
                            shape = CircleShape,
                            color = if (drillDownState.isReimbursableProject) {
                                MaterialTheme.colorScheme.secondaryContainer
                            } else {
                                MaterialTheme.colorScheme.primaryContainer
                            },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (drillDownState.isReimbursableProject) {
                                        Icons.AutoMirrored.Filled.ReceiptLong
                                    } else {
                                        Icons.AutoMirrored.Filled.Label
                                    },
                                    contentDescription = null,
                                    tint = if (drillDownState.isReimbursableProject) {
                                        MaterialTheme.colorScheme.onSecondaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    },
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "#$cleanName",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (drillDownState.isReimbursableProject) {
                                    "Reimbursements & Claims · ${if (isAllTime) "All-Time" else periodTitle}"
                                } else {
                                    "Event & Trip Passbook · ${if (isAllTime) "All-Time" else periodTitle}"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    MoneyText(
                        money = if (drillDownState.isReimbursableProject) {
                            if (drillDownState.netPendingOwed.isPositive) {
                                drillDownState.netPendingOwed
                            } else {
                                drillDownState.totalSpent
                            }
                        } else {
                            drillDownState.totalSpent
                        },
                        currencySpec = currencySpec,
                        grouping = grouping,
                        colorMode = if (drillDownState.isReimbursableProject && drillDownState.netPendingOwed.isPositive) {
                            MoneyColorMode.NEUTRAL
                        } else {
                            MoneyColorMode.EXPENSE
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

            // Hero Card: Reimbursable vs Event/Trip
            item {
                TagProjectHeroSummaryCard(
                    drillDownState = drillDownState,
                    currencySpec = currencySpec,
                    grouping = grouping
                )
            }

            // 6-Month Monthly Spend Trend Bars
            if (drillDownState.monthlyTrendBars.any { it.totalExpense.isPositive }) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "6-Month Outlay Trend",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        SixMonthTrendBarChart(
                            bars = drillDownState.monthlyTrendBars,
                            currencySpec = currencySpec,
                            grouping = grouping
                        )
                    }
                }
            }

            // Categories Distribution
            if (drillDownState.categoryDistribution.isNotEmpty()) {
                item {
                    TagCategoryDistributionSection(
                        categoryDistribution = drillDownState.categoryDistribution,
                        currencySpec = currencySpec,
                        grouping = grouping
                    )
                }
            }

            // Tagged Transactions List Header
            item {
                Text(
                    text = "Passbook Transactions (${drillDownState.transactions.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Tagged Transaction Items
            items(
                items = drillDownState.transactions,
                key = { it.id }
            ) { tx ->
                TagTransactionRowItem(
                    transaction = tx,
                    currencySpec = currencySpec,
                    grouping = grouping,
                    dateFormatter = dateFormatter,
                    onClick = { onTransactionClick(tx) }
                )
            }
        }
    }
}
