// File: app/src/main/java/com/folioledger/app/ui/stats/CreditCardsCockpitSubTabContent.kt
package com.folioledger.app.ui.stats

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.folioledger.app.domain.model.AccountSummary
import com.folioledger.app.domain.model.CategoryType
import com.folioledger.app.domain.model.CurrencySpec
import com.folioledger.app.domain.model.NumberGroupingSystem
import com.folioledger.app.domain.model.SupportedCurrencies
import com.folioledger.app.domain.util.MoneyFormatterAndCalculator
import com.folioledger.app.ui.components.CategoryIconBadge
import com.folioledger.app.ui.components.EmptyStateCard
import com.folioledger.app.ui.components.MoneyColorMode
import com.folioledger.app.ui.components.MoneyText
import com.folioledger.app.ui.components.SixMonthSmoothTrendAreaChart
import com.folioledger.app.ui.components.parseHexColorSafely
import com.folioledger.app.ui.theme.LocalFolioSemanticColors
import com.folioledger.app.ui.theme.tabularMoneyStyle
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

private val shortDateFormatter = DateTimeFormatter.ofPattern("dd MMM", Locale.US)

@Composable
fun CreditCardsCockpitSubTabContent(
    uiState: StatsAndBudgetsUiState,
    onOpenSettlement: (AccountSummary) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val cockpit = uiState.creditCockpitState
    val semanticColors = LocalFolioSemanticColors.current

    if (cockpit.activeCards.isEmpty()) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(24.dp)
        ) {
            EmptyStateCard(
                icon = Icons.Filled.CreditCard,
                title = "No Active Credit Cards",
                subtitle = "Add a credit card account from the Accounts tab to track credit health, billing cycle timelines, free-credit float days, and annual fee waiver milestones."
            )
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 32.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxSize()
    ) {
        // SECTION 1: HERO UTILIZATION GAUGE & CASH COVERAGE
        item(key = "hero_gauge_card") {
            CreditHeroArcGaugeCard(
                cockpit = cockpit,
                currencySpec = uiState.currencySpec,
                grouping = uiState.numberGrouping
            )
        }

        // PRO FEATURE 1: SMART SWIPE ADVISOR BANNER
        cockpit.smartSwipeRecommendation?.let { recommendation ->
            item(key = "smart_swipe_advisor") {
                SmartSwipeAdvisorBanner(
                    recommendation = recommendation
                )
            }
        }

        // SECTION 2: PAYMENT & CYCLE TIMELINE
        if (cockpit.timelineItems.isNotEmpty()) {
            item(key = "payment_timeline_section") {
                CreditPaymentTimelineSection(
                    timelineItems = cockpit.timelineItems,
                    currencySpec = uiState.currencySpec,
                    grouping = uiState.numberGrouping,
                    onOpenSettlement = onOpenSettlement
                )
            }
        }

        // SECTION 3: REFINED CARD HEALTH BREAKDOWN
        if (cockpit.cardAnalyticsItems.isNotEmpty()) {
            item(key = "card_health_breakdown_section") {
                CardHealthBreakdownSection(
                    items = cockpit.cardAnalyticsItems,
                    currencySpec = uiState.currencySpec,
                    grouping = uiState.numberGrouping
                )
            }
        }

        // SECTION 4: SPEND MODE RATIO (CREDIT VS LIQUID)
        item(key = "spend_mode_ratio_card") {
            SpendModeRatioCard(
                ratio = cockpit.spendRatio,
                currencySpec = uiState.currencySpec,
                grouping = uiState.numberGrouping,
                periodTitle = uiState.activePeriodRange.title
            )
        }

        // SECTION 5: 6-MONTH HISTORICAL CREDIT SPEND TREND
        if (cockpit.sixMonthCreditTrendBars.isNotEmpty()) {
            item(key = "six_month_credit_trend_card") {
                CreditSpendTrendCard(
                    bars = cockpit.sixMonthCreditTrendBars,
                    currencySpec = uiState.currencySpec,
                    grouping = uiState.numberGrouping
                )
            }
        }
    }
}

// -------------------------------------------------------------------------
// SECTION 1: HERO GAUGE & CASH COVERAGE
// -------------------------------------------------------------------------

@Composable
private fun CreditHeroArcGaugeCard(
    cockpit: CreditCardCockpitUiState,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem,
    modifier: Modifier = Modifier
) {
    val semanticColors = LocalFolioSemanticColors.current

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Credit Health & Exposure",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "${cockpit.activeCards.size} Cards Active",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            // Semi-Circular Arc Gauge with Big Metric
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                var animTarget by remember { mutableFloatStateOf(0f) }
                LaunchedEffect(cockpit.aggregateUtilizationPercent) {
                    animTarget = (cockpit.aggregateUtilizationPercent / 100f).coerceIn(0f, 1f)
                }
                val animProgress by animateFloatAsState(
                    targetValue = animTarget,
                    animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
                    label = "HeroGaugeAnim"
                )

                val trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                val progressColor = when (cockpit.aggregateStatus) {
                    CreditUtilizationStatus.WARNING -> semanticColors.expenseCoral
                    CreditUtilizationStatus.MODERATE -> semanticColors.warningAmber
                    CreditUtilizationStatus.SAFE -> semanticColors.incomeEmerald
                }

                Spacer(
                    modifier = Modifier
                        .fillMaxWidth(0.74f)
                        .height(130.dp)
                        .drawWithCache {
                            val strokeWidth = 14.dp.toPx()
                            val arcPadding = strokeWidth / 2f
                            val arcWidth = size.width - strokeWidth
                            val arcHeight = (size.height - arcPadding) * 2f
                            val arcTopLeft = Offset(arcPadding, arcPadding)
                            val arcSize = Size(arcWidth, arcHeight)

                            onDrawBehind {
                                // Background Track
                                drawArc(
                                    color = trackColor,
                                    startAngle = 180f,
                                    sweepAngle = 180f,
                                    useCenter = false,
                                    topLeft = arcTopLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )

                                // Active Arc
                                val sweep = animProgress * 180f
                                if (sweep > 0f) {
                                    drawArc(
                                        color = progressColor,
                                        startAngle = 180f,
                                        sweepAngle = sweep,
                                        useCenter = false,
                                        topLeft = arcTopLeft,
                                        size = arcSize,
                                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                    )
                                }

                                // Tick marks at 10%, 30%, 50%
                                val cx = size.width / 2f
                                val cy = arcHeight / 2f + arcPadding
                                val r = arcWidth / 2f
                                val ticks = floatArrayOf(0.10f, 0.30f, 0.50f)
                                for (t in ticks) {
                                    val rad = Math.toRadians((180f + t * 180f).toDouble())
                                    val innerR = r - strokeWidth / 2f - 1.dp.toPx()
                                    val outerR = r + strokeWidth / 2f + 1.dp.toPx()
                                    val p1 = Offset(
                                        (cx + innerR * cos(rad)).toFloat(),
                                        (cy + innerR * sin(rad)).toFloat()
                                    )
                                    val p2 = Offset(
                                        (cx + outerR * cos(rad)).toFloat(),
                                        (cy + outerR * sin(rad)).toFloat()
                                    )
                                    drawLine(
                                        color = trackColor.copy(alpha = 0.8f),
                                        start = p1,
                                        end = p2,
                                        strokeWidth = 2.dp.toPx()
                                    )
                                }
                            }
                        }
                )

                // Center Content
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 28.dp)
                ) {
                    Text(
                        text = "${String.format(Locale.US, "%.1f", cockpit.aggregateUtilizationPercent)}%",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        color = progressColor
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = when (cockpit.aggregateStatus) {
                            CreditUtilizationStatus.WARNING -> semanticColors.expenseCoralContainer
                            CreditUtilizationStatus.MODERATE -> semanticColors.warningAmberContainer
                            CreditUtilizationStatus.SAFE -> semanticColors.incomeEmeraldContainer
                        },
                        border = BorderStroke(
                            1.dp,
                            progressColor.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = when (cockpit.aggregateStatus) {
                                CreditUtilizationStatus.WARNING -> "● High Risk (>50%)"
                                CreditUtilizationStatus.MODERATE -> "● Warning (30-50%)"
                                CreditUtilizationStatus.SAFE -> "● Safe Tier (<30%)"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = when (cockpit.aggregateStatus) {
                                CreditUtilizationStatus.WARNING -> semanticColors.onExpenseCoralContainer
                                CreditUtilizationStatus.MODERATE -> semanticColors.onWarningAmberContainer
                                CreditUtilizationStatus.SAFE -> semanticColors.onIncomeEmeraldContainer
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // 3-Col KPI Grid
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceContainer,
                        RoundedCornerShape(14.dp)
                    )
                    .padding(vertical = 12.dp, horizontal = 8.dp)
            ) {
                // Metric 1: Total Debt
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "TOTAL CC DEBT",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    MoneyText(
                        money = cockpit.totalCcDebt,
                        currencySpec = currencySpec,
                        grouping = grouping,
                        colorMode = MoneyColorMode.EXPENSE,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Metric 2: Available Headroom
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "AVAILABLE",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    MoneyText(
                        money = cockpit.totalAvailableCredit,
                        currencySpec = currencySpec,
                        grouping = grouping,
                        colorMode = MoneyColorMode.INCOME,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Metric 3: Total Limit
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "TOTAL LIMIT",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    MoneyText(
                        money = cockpit.totalCreditLimit,
                        currencySpec = currencySpec,
                        grouping = grouping,
                        colorMode = MoneyColorMode.NEUTRAL,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // PRO FEATURE 2: LIQUID CASH COVERAGE BANNER
            val coverage = cockpit.liquidCoverage
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (coverage.isCashBacked) {
                    semanticColors.incomeEmeraldContainer.copy(alpha = 0.5f)
                } else {
                    semanticColors.warningAmberContainer.copy(alpha = 0.5f)
                },
                border = BorderStroke(
                    1.dp,
                    if (coverage.isCashBacked) semanticColors.incomeEmerald.copy(alpha = 0.3f)
                    else semanticColors.warningAmber.copy(alpha = 0.3f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (coverage.isCashBacked) Icons.Filled.Shield else Icons.Filled.Warning,
                            contentDescription = null,
                            tint = if (coverage.isCashBacked) semanticColors.incomeEmerald else semanticColors.warningAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                            Text(
                                text = if (coverage.isCashBacked) "100% Cash-Backed" else "Riding the Credit Float",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (coverage.isCashBacked) semanticColors.incomeEmerald else semanticColors.warningAmber
                            )
                            Text(
                                text = if (coverage.isCashBacked) {
                                    val netText = MoneyFormatterAndCalculator.formatMinorMoney(
                                        money = cockpit.liquidCoverage.netLiquidCash,
                                        currencySpec = currencySpec,
                                        grouping = grouping
                                    )
                                    "Liquid bank cash covers all debt ($netText Net Cash)."
                                } else {
                                    "Total card debt exceeds liquid cash reserves."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.padding(start = 6.dp)
                    ) {
                        Text(
                            text = if (coverage.isCashBacked) {
                                "${String.format(Locale.US, "%.1f", coverage.coverageRatio)}x Covered"
                            } else {
                                "Float Risk"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (coverage.isCashBacked) semanticColors.incomeEmerald else semanticColors.warningAmber,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // SINGLE CARD SPIKE ALERT (if any individual card exceeds 30%)
            cockpit.singleCardExceeding30?.let { spikeCard ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = semanticColors.warningAmberContainer.copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, semanticColors.warningAmber.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = null,
                            tint = semanticColors.warningAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Single Card Alert: ${spikeCard.account.name} is at ${String.format(Locale.US, "%.1f", spikeCard.utilizationPercent)}% utilization. Credit bureaus penalize single-card spikes (>30%) even when total debt is safe.",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// PRO FEATURE 1: SMART SWIPE ADVISOR BANNER
// -------------------------------------------------------------------------

@Composable
private fun SmartSwipeAdvisorBanner(
    recommendation: SmartSwipeRecommendation,
    modifier: Modifier = Modifier
) {
    val cardColor = parseHexColorSafely(recommendation.account.colorHex)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    )
                )
                .padding(14.dp)
        ) {
            // Icon
            CategoryIconBadge(
                iconName = recommendation.account.iconName,
                colorHex = recommendation.account.colorHex,
                size = 38.dp,
                iconSize = 20.dp
            )

            // Details
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Smart Swipe Advisor",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "✨ ${recommendation.floatDaysRemaining}D FLOAT",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = "Swipe ${recommendation.account.name} today for maximum interest-free days (Statement closed ${recommendation.statementCloseDate.format(shortDateFormatter)}; due ${recommendation.nextDueDate.format(shortDateFormatter)}).",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// -------------------------------------------------------------------------
// SECTION 2: PAYMENT & CYCLE TIMELINE
// -------------------------------------------------------------------------

@Composable
private fun CreditPaymentTimelineSection(
    timelineItems: List<CreditCardTimelineItem>,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem,
    onOpenSettlement: (AccountSummary) -> Unit,
    modifier: Modifier = Modifier
) {
    val semanticColors = LocalFolioSemanticColors.current

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Payment & Cycle Timeline",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${timelineItems.size} Cards",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(12.dp)
            ) {
                timelineItems.forEach { item ->
                    TimelineItemRow(
                        item = item,
                        currencySpec = currencySpec,
                        grouping = grouping,
                        onOpenSettlement = onOpenSettlement
                    )
                }
            }
        }
    }
}

@Composable
private fun TimelineItemRow(
    item: CreditCardTimelineItem,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem,
    onOpenSettlement: (AccountSummary) -> Unit
) {
    val semanticColors = LocalFolioSemanticColors.current
    val cardColor = remember(item.account.colorHex) { parseHexColorSafely(item.account.colorHex) }
    val cardSpec = remember(item.account.currencyCode) {
        SupportedCurrencies.getByCode(item.account.currencyCode)
            ?: SupportedCurrencies.fromCode(item.account.currencyCode)
    }

    val borderLeftColor = when {
        item.isOverdue || item.isUrgent -> semanticColors.expenseCoral
        item.isBilled -> semanticColors.warningAmber
        else -> MaterialTheme.colorScheme.primary
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(
                    if (item.isUrgent || item.isOverdue) {
                        Brush.horizontalGradient(
                            listOf(semanticColors.expenseCoral.copy(alpha = 0.08f), Color.Transparent)
                        )
                    } else {
                        Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                    }
                )
                .padding(12.dp)
        ) {
            // Left color indicator bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(38.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(borderLeftColor)
            )
            Spacer(modifier = Modifier.width(10.dp))

            // Card details
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(cardColor)
                    )
                    Text(
                        text = item.account.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = when {
                        item.isOverdue -> "🚨 Overdue by ${Math.abs(item.daysRemaining)} days (${item.targetDate.format(shortDateFormatter)})"
                        item.isUrgent -> "🚨 Due in ${item.daysRemaining} days (${item.targetDate.format(shortDateFormatter)})"
                        item.isBilled -> "Due in ${item.daysRemaining} days (${item.targetDate.format(shortDateFormatter)})"
                        else -> "Bill generates in ${item.daysRemaining} days (${item.targetDate.format(shortDateFormatter)})"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (item.isUrgent || item.isOverdue) FontWeight.Bold else FontWeight.Medium,
                    color = when {
                        item.isOverdue || item.isUrgent -> semanticColors.expenseCoral
                        item.isBilled -> semanticColors.warningAmber
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            // Right: Amount and Pay button (or Float badge)
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                MoneyText(
                    money = item.amountDue,
                    currencySpec = cardSpec,
                    grouping = grouping,
                    colorMode = if (item.isBilled) MoneyColorMode.EXPENSE else MoneyColorMode.NEUTRAL,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                if (item.isBilled) {
                    Button(
                        onClick = { onOpenSettlement(item.account) },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (item.isUrgent || item.isOverdue) {
                                semanticColors.expenseCoral
                            } else {
                                semanticColors.warningAmber
                            }
                        ),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Text(
                            text = "Pay Now",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Text(
                            text = "✨ ${item.floatDays}d Float",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// SECTION 3: REFINED CARD HEALTH BREAKDOWN
// -------------------------------------------------------------------------

@Composable
private fun CardHealthBreakdownSection(
    items: List<CreditCardAnalyticsItem>,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem,
    modifier: Modifier = Modifier
) {
    val semanticColors = LocalFolioSemanticColors.current

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = "Card Health & Utilization (${items.size})",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(14.dp)
            ) {
                items.forEach { cardItem ->
                    CardAnalyticsItemRow(
                        item = cardItem,
                        currencySpec = currencySpec,
                        grouping = grouping
                    )
                }
            }
        }
    }
}

@Composable
private fun CardAnalyticsItemRow(
    item: CreditCardAnalyticsItem,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem
) {
    val semanticColors = LocalFolioSemanticColors.current
    val cardColor = remember(item.account.colorHex) { parseHexColorSafely(item.account.colorHex) }
    val cardSpec = remember(item.account.currencyCode) {
        SupportedCurrencies.getByCode(item.account.currencyCode)
            ?: SupportedCurrencies.fromCode(item.account.currencyCode)
    }
    val progressColor = when (item.status) {
        CreditUtilizationStatus.WARNING -> semanticColors.expenseCoral
        CreditUtilizationStatus.MODERATE -> semanticColors.warningAmber
        CreditUtilizationStatus.SAFE -> semanticColors.incomeEmerald
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(12.dp)
        ) {
            // Header Row
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
                        iconName = item.account.iconName,
                        colorHex = item.account.colorHex,
                        size = 34.dp,
                        iconSize = 18.dp
                    )
                    Column {
                        Text(
                            text = item.account.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Limit: ",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            MoneyText(
                                money = item.creditLimit,
                                currencySpec = cardSpec,
                                grouping = grouping,
                                colorMode = MoneyColorMode.NEUTRAL,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${String.format(Locale.US, "%.1f", item.utilizationPercent)}%",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = progressColor
                    )
                    Text(
                        text = when (item.status) {
                            CreditUtilizationStatus.WARNING -> "Warning (>50%)"
                            CreditUtilizationStatus.MODERATE -> "Moderate (30-50%)"
                            CreditUtilizationStatus.SAFE -> "Safe (<30%)"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = progressColor
                    )
                }
            }

            // Utilization Progress Bar
            LinearProgressIndicator(
                progress = { (item.utilizationPercent / 100f).coerceIn(0f, 1f) },
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                strokeCap = StrokeCap.Round,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
            )

            // PRO FEATURE 3: Fee Waiver Tracker & Top Spend Category Footer
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                // Annual / YTD Spend
                Column {
                    Text(
                        text = "ANNUAL / YTD SPEND",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.outline
                    )
                    MoneyText(
                        money = item.ytdSpend,
                        currencySpec = cardSpec,
                        grouping = grouping,
                        colorMode = MoneyColorMode.NEUTRAL,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Top Category
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "TOP CATEGORY",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    if (item.topCategoryName != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                            border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(cardColor)
                                )
                                Text(
                                    text = "${item.topCategoryName} · ${String.format(Locale.US, "%.0f", item.topCategoryPercent)}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "None Yet",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// SECTION 4: SPEND MODE RATIO (CREDIT VS LIQUID)
// -------------------------------------------------------------------------

@Composable
private fun SpendModeRatioCard(
    ratio: CreditSpendRatio,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem,
    periodTitle: String,
    modifier: Modifier = Modifier
) {
    val semanticColors = LocalFolioSemanticColors.current

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
        modifier = modifier.fillMaxWidth()
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
                    text = "Spend Mode Ratio ($periodTitle)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Text(
                        text = "Pure Expenses",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            val hasExpenses = !ratio.creditSpend.isZero || !ratio.liquidSpend.isZero
            if (!hasExpenses) {
                Text(
                    text = "No expenses recorded in $periodTitle.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            } else {
                // Dual Progress Split Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                ) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        if (ratio.creditPercentage > 0f) {
                            Box(
                                modifier = Modifier
                                    .weight(ratio.creditPercentage.coerceAtLeast(1f))
                                    .fillMaxSize()
                                    .background(semanticColors.expenseCoral)
                            )
                        }
                        if (ratio.liquidPercentage > 0f) {
                            Box(
                                modifier = Modifier
                                    .weight(ratio.liquidPercentage.coerceAtLeast(1f))
                                    .fillMaxSize()
                                    .background(semanticColors.incomeEmerald)
                            )
                        }
                    }
                }

                // Legend
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(semanticColors.expenseCoral)
                        )
                        Text(
                            text = "💳 Credit: ${String.format(Locale.US, "%.0f", ratio.creditPercentage)}% (",
                            style = MaterialTheme.typography.labelSmall,
                            color = semanticColors.expenseCoral,
                            fontWeight = FontWeight.Bold
                        )
                        MoneyText(
                            money = ratio.creditSpend,
                            currencySpec = currencySpec,
                            grouping = grouping,
                            colorMode = MoneyColorMode.EXPENSE,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = ")",
                            style = MaterialTheme.typography.labelSmall,
                            color = semanticColors.expenseCoral
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(semanticColors.incomeEmerald)
                        )
                        Text(
                            text = "🏦 Bank/Cash: ${String.format(Locale.US, "%.0f", ratio.liquidPercentage)}% (",
                            style = MaterialTheme.typography.labelSmall,
                            color = semanticColors.incomeEmerald,
                            fontWeight = FontWeight.Bold
                        )
                        MoneyText(
                            money = ratio.liquidSpend,
                            currencySpec = currencySpec,
                            grouping = grouping,
                            colorMode = MoneyColorMode.INCOME,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = ")",
                            style = MaterialTheme.typography.labelSmall,
                            color = semanticColors.incomeEmerald
                        )
                    }
                }

                Text(
                    text = "💡 Higher credit share maximizes reward points, provided statement balances are cleared in full before due dates.",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// -------------------------------------------------------------------------
// SECTION 5: 6-MONTH HISTORICAL CREDIT SPEND TREND
// -------------------------------------------------------------------------

@Composable
private fun CreditSpendTrendCard(
    bars: List<com.folioledger.app.domain.model.MonthlyTrendBar>,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem,
    modifier: Modifier = Modifier
) {
    val semanticColors = LocalFolioSemanticColors.current

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
        modifier = modifier.fillMaxWidth()
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
                    text = "6-Month Credit Spend Trend",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Historical Spends",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            SixMonthSmoothTrendAreaChart(
                bars = bars,
                currencySpec = currencySpec,
                grouping = grouping,
                categoryType = CategoryType.EXPENSE,
                customColor = semanticColors.expenseCoral,
                chartHeight = 150.dp
            )
        }
    }
}
