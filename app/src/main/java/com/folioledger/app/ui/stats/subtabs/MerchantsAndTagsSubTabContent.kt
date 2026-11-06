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
internal fun TagsAndNotesRankingsSubTabContent(
    uiState: StatsAndBudgetsUiState,
    onSelectRankingMode: (RankingMode) -> Unit,
    onSelectPayeeSortMode: (PayeeSortMode) -> Unit,
    onToggleRankingsAllTime: () -> Unit,
    onPayeeClick: (String) -> Unit,
    onTagClick: (String) -> Unit
) {
    val items = uiState.activeRankingItems

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
        // Toggle between #Tag Rankings and Payee / Note Rankings
        item(key = "ranking_mode_switcher", contentType = "ranking_mode_switcher") {
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
                                }
                            ,
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

        // Sub-header with All-Time Scope Filter Chip and Spend vs Visits Sort Toggle
        item(key = "ranking_scope_filter_header", contentType = "ranking_scope_filter_header") {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (uiState.selectedRankingMode == RankingMode.TAGS) {
                        "Ranked Tags (${items.size})"
                    } else {
                        "Ranked Payees & Merchants (${items.size})"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (uiState.selectedRankingMode == RankingMode.PAYEES_AND_NOTES) {
                        FilterChip(
                            selected = uiState.selectedPayeeRankingSortMode == PayeeSortMode.BY_FREQUENCY,
                            onClick = {
                                val next = if (uiState.selectedPayeeRankingSortMode == PayeeSortMode.BY_SPEND) {
                                    PayeeSortMode.BY_FREQUENCY
                                } else {
                                    PayeeSortMode.BY_SPEND
                                }
                                onSelectPayeeSortMode(next)
                            },
                            label = {
                                Text(
                                    text = if (uiState.selectedPayeeRankingSortMode == PayeeSortMode.BY_FREQUENCY) "🔁 Visits" else "💰 Spend",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        )
                    }

                    FilterChip(
                        selected = uiState.isRankingsAllTime,
                        onClick = onToggleRankingsAllTime,
                        label = {
                            Text(
                                text = if (uiState.isRankingsAllTime) "All-Time" else "In ${uiState.activePeriodRange.title}",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    )
                }
            }
        }

        if (items.isEmpty()) {
            item(key = "empty_rankings_card", contentType = "empty_rankings_card") {
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
                    grouping = uiState.numberGrouping,
                    onClick = if (uiState.selectedRankingMode == RankingMode.PAYEES_AND_NOTES) {
                        { onPayeeClick(item.label) }
                    } else {
                        { onTagClick(item.label) }
                    }
                )
            }
        }
    }
}

@Composable
internal fun SpendRankingItemCard(
    rankNumber: Int,
    item: SpendRankingItem,
    isTagMode: Boolean,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem,
    onClick: (() -> Unit)? = null
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
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
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
                    val avgMinor = if (item.transactionCount > 0) item.totalAmount.amountMinor / item.transactionCount else 0L
                    val avgTicket = remember(avgMinor, currencySpec, grouping) {
                        MoneyFormatterAndCalculator.formatMinorMoney(MinorMoney(avgMinor), currencySpec)
                    }
                    Text(
                        text = if (isTagMode) {
                            "${item.transactionCount} transactions · $pctFormatted"
                        } else {
                            "${item.transactionCount} ${if (item.transactionCount == 1) "order" else "orders"} · $avgTicket avg · $pctFormatted"
                        },
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

                if (onClick != null) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Inspect",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(18.dp)
                    )
                }
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
