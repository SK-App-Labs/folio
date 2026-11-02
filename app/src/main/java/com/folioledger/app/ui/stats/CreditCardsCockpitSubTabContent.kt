// File: app/src/main/java/com/folioledger/app/ui/stats/CreditCardsCockpitSubTabContent.kt
package com.folioledger.app.ui.stats

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
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
import com.folioledger.app.domain.model.CardMilestoneConfig
import com.folioledger.app.domain.model.CategoryType
import com.folioledger.app.domain.model.CurrencySpec
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.NumberGroupingSystem
import com.folioledger.app.domain.model.SupportedCurrencies
import com.folioledger.app.domain.util.CardMilestoneCalculator
import com.folioledger.app.domain.util.MoneyFormatterAndCalculator
import com.folioledger.app.ui.components.CategoryIconBadge
import com.folioledger.app.ui.components.EmptyStateCard
import com.folioledger.app.ui.components.MoneyColorMode
import com.folioledger.app.ui.components.MoneyText
import com.folioledger.app.ui.components.SixMonthSmoothTrendAreaChart
import com.folioledger.app.ui.components.parseHexColorSafely
import com.folioledger.app.ui.theme.LocalFolioSemanticColors
import com.folioledger.app.ui.theme.tabularMoneyStyle
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

private val shortDateFormatter = DateTimeFormatter.ofPattern("dd MMM", Locale.US)
private val fullDateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.US)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditCardsCockpitSubTabContent(
    uiState: StatsAndBudgetsUiState,
    onOpenSettlement: (AccountSummary) -> Unit,
    onAccountClick: ((AccountSummary) -> Unit)? = null,
    onAddTransactionForAccount: ((AccountSummary) -> Unit)? = null,
    onSaveCardMilestone: ((accountId: Long, title: String, startDateEpochDay: Long, targetMinor: Long, feeMinor: Long?, autoRenew: Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val cockpit = uiState.creditCockpitState
    val semanticColors = LocalFolioSemanticColors.current

    var selectedMilestoneCard by remember { mutableStateOf<CreditCardAnalyticsItem?>(null) }
    var isPaydownSimulatorOpen by remember { mutableStateOf(false) }
    var isFloatRankingOpen by remember { mutableStateOf(false) }

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
        // SECTION 1: HERO UTILIZATION GAUGE & CASH SAFETY HUB
        item(key = "hero_gauge_card") {
            CreditHeroArcGaugeCard(
                cockpit = cockpit,
                currencySpec = uiState.currencySpec,
                grouping = uiState.numberGrouping,
                onOpenPaydownSimulator = { isPaydownSimulatorOpen = true }
            )
        }

        // SECTION 2: SMART SWIPE ADVISOR BANNER (ACTIONABLE)
        cockpit.smartSwipeRecommendation?.let { recommendation ->
            item(key = "smart_swipe_advisor") {
                SmartSwipeAdvisorBanner(
                    recommendation = recommendation,
                    onUseCard = { onAddTransactionForAccount?.invoke(recommendation.account) },
                    onOpenFloatRanking = { isFloatRankingOpen = true }
                )
            }
        }

        // SECTION 3: UNIFIED CARDS PORTFOLIO (ELIMINATES DUPLICATION & NESTED BOXES)
        if (cockpit.cardAnalyticsItems.isNotEmpty()) {
            item(key = "unified_cards_portfolio") {
                UnifiedCardsPortfolioSection(
                    cards = cockpit.cardAnalyticsItems,
                    currencySpec = uiState.currencySpec,
                    grouping = uiState.numberGrouping,
                    onOpenSettlement = onOpenSettlement,
                    onCardClick = onAccountClick,
                    onOpenMilestoneEditor = { cardItem -> selectedMilestoneCard = cardItem }
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

    // MODAL BOTTOM SHEETS & DIALOGS
    selectedMilestoneCard?.let { card ->
        CardMilestoneEditorBottomSheet(
            card = card,
            currencySpec = uiState.currencySpec,
            grouping = uiState.numberGrouping,
            onDismiss = { selectedMilestoneCard = null },
            onSave = { title, startDateEpoch, targetMinor, feeMinor, autoRenew ->
                onSaveCardMilestone?.invoke(
                    card.account.id,
                    title,
                    startDateEpoch,
                    targetMinor,
                    feeMinor,
                    autoRenew
                )
                selectedMilestoneCard = null
            }
        )
    }

    if (isPaydownSimulatorOpen) {
        CreditPaydownSimulatorBottomSheet(
            cockpit = cockpit,
            currencySpec = uiState.currencySpec,
            grouping = uiState.numberGrouping,
            onDismiss = { isPaydownSimulatorOpen = false },
            onSettleTargetDebt = {
                isPaydownSimulatorOpen = false
                val targetCard = cockpit.cardAnalyticsItems.maxByOrNull { it.utilizationPercent }?.account
                    ?: cockpit.activeCards.firstOrNull()
                if (targetCard != null) {
                    onOpenSettlement(targetCard)
                }
            }
        )
    }

    if (isFloatRankingOpen) {
        AllCardsFloatRankingDialog(
            cards = cockpit.cardAnalyticsItems,
            onDismiss = { isFloatRankingOpen = false },
            onSelectCard = { account ->
                isFloatRankingOpen = false
                onAddTransactionForAccount?.invoke(account)
            }
        )
    }
}

// -------------------------------------------------------------------------
// SECTION 1: HERO GAUGE & CASH SAFETY HUB
// -------------------------------------------------------------------------

@Composable
private fun CreditHeroArcGaugeCard(
    cockpit: CreditCardCockpitUiState,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem,
    onOpenPaydownSimulator: () -> Unit,
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
                val animProgressState = animateFloatAsState(
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

                            val tickStrokePx = 2.dp.toPx()
                            val tickColor = trackColor.copy(alpha = 0.8f)
                            val oneDpPx = 1.dp.toPx()

                            val cx = size.width / 2f
                            val cy = arcHeight / 2f + arcPadding
                            val r = arcWidth / 2f
                            val tickRatios = floatArrayOf(0.10f, 0.30f, 0.50f)
                            val cachedTickLines = tickRatios.map { t ->
                                val rad = Math.toRadians((180f + t * 180f).toDouble())
                                val innerR = r - strokeWidth / 2f - oneDpPx
                                val outerR = r + strokeWidth / 2f + oneDpPx
                                val p1 = Offset(
                                    (cx + innerR * cos(rad)).toFloat(),
                                    (cy + innerR * sin(rad)).toFloat()
                                )
                                val p2 = Offset(
                                    (cx + outerR * cos(rad)).toFloat(),
                                    (cy + outerR * sin(rad)).toFloat()
                                )
                                p1 to p2
                            }

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
                                val sweep = animProgressState.value * 180f
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

                                // Cached tick marks
                                for ((p1, p2) in cachedTickLines) {
                                    drawLine(
                                        color = tickColor,
                                        start = p1,
                                        end = p2,
                                        strokeWidth = tickStrokePx
                                    )
                                }
                            }
                        }
                )

                // Center Content (Interactive to open Paydown Simulator)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(top = 28.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onOpenPaydownSimulator)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
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
                        border = BorderStroke(1.dp, progressColor.copy(alpha = 0.3f)),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
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
                                }
                            )
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = "Safe Tier Simulator",
                                tint = progressColor,
                                modifier = Modifier.size(11.dp)
                            )
                        }
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
                        style = if (currencySpec.symbol.length > 2) MaterialTheme.typography.labelMedium else MaterialTheme.typography.titleSmall,
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
                        style = if (currencySpec.symbol.length > 2) MaterialTheme.typography.labelMedium else MaterialTheme.typography.titleSmall,
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
                        style = if (currencySpec.symbol.length > 2) MaterialTheme.typography.labelMedium else MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // LIQUID CASH COVERAGE BANNER
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
// SECTION 2: SMART SWIPE ADVISOR BANNER (ACTIONABLE)
// -------------------------------------------------------------------------

@Composable
private fun SmartSwipeAdvisorBanner(
    recommendation: SmartSwipeRecommendation,
    onUseCard: () -> Unit,
    onOpenFloatRanking: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onOpenFloatRanking)
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
                verticalArrangement = Arrangement.spacedBy(3.dp),
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

            // 1-Tap Action: Use This Card to record expense
            Button(
                onClick = onUseCard,
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Text(
                    text = "+ Spend",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// -------------------------------------------------------------------------
// SECTION 3: UNIFIED CARDS PORTFOLIO SECTION
// -------------------------------------------------------------------------

@Composable
private fun UnifiedCardsPortfolioSection(
    cards: List<CreditCardAnalyticsItem>,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem,
    onOpenSettlement: (AccountSummary) -> Unit,
    onCardClick: ((AccountSummary) -> Unit)?,
    onOpenMilestoneEditor: (CreditCardAnalyticsItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val sortedCards = remember(cards) {
        cards.sortedWith(
            compareBy<CreditCardAnalyticsItem> { !it.isBilled }
                .thenBy { it.daysRemaining }
                .thenByDescending { it.utilizationPercent }
        )
    }

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
                text = "My Cards Portfolio (${cards.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Tap card for passbook",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }

        sortedCards.forEach { cardItem ->
            CardPortfolioItemCard(
                card = cardItem,
                currencySpec = currencySpec,
                grouping = grouping,
                onOpenSettlement = { onOpenSettlement(cardItem.account) },
                onCardClick = { onCardClick?.invoke(cardItem.account) },
                onOpenMilestoneEditor = { onOpenMilestoneEditor(cardItem) }
            )
        }
    }
}

@Composable
private fun CardPortfolioItemCard(
    card: CreditCardAnalyticsItem,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem,
    onOpenSettlement: () -> Unit,
    onCardClick: () -> Unit,
    onOpenMilestoneEditor: () -> Unit,
    modifier: Modifier = Modifier
) {
    val semanticColors = LocalFolioSemanticColors.current
    val cardSpec = remember(card.account.currencyCode) {
        SupportedCurrencies.getByCode(card.account.currencyCode)
            ?: SupportedCurrencies.fromCode(card.account.currencyCode)
    }
    val progressColor = when (card.status) {
        CreditUtilizationStatus.WARNING -> semanticColors.expenseCoral
        CreditUtilizationStatus.MODERATE -> semanticColors.warningAmber
        CreditUtilizationStatus.SAFE -> semanticColors.incomeEmerald
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onCardClick)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(14.dp)
        ) {
            // Top Row: Logo + Name + Status + Pay Button or Float Tag
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    CategoryIconBadge(
                        iconName = card.account.iconName,
                        colorHex = card.account.colorHex,
                        size = 36.dp,
                        iconSize = 18.dp
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text(
                            text = card.account.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = when {
                                card.isBilled && card.isOverdue -> "🚨 Overdue (${card.targetDate.format(shortDateFormatter)})"
                                card.isBilled && card.isUrgent -> "🚨 Due in ${card.daysRemaining}d (${card.targetDate.format(shortDateFormatter)})"
                                card.isBilled -> "Due in ${card.daysRemaining}d (${card.targetDate.format(shortDateFormatter)})"
                                !card.currentDebt.isPositive -> "● Zero Balance Due"
                                else -> "Bill closes in ${card.daysRemaining}d (${card.targetDate.format(shortDateFormatter)})"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (card.isUrgent || card.isOverdue) FontWeight.Bold else FontWeight.Medium,
                            color = when {
                                card.isBilled && (card.isOverdue || card.isUrgent) -> semanticColors.expenseCoral
                                card.isBilled -> semanticColors.warningAmber
                                !card.currentDebt.isPositive -> semanticColors.incomeEmerald
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }

                // Action Area: Pay Button if Billed, otherwise Float or Clear Pill
                if (card.isBilled && card.billedAmount.isPositive) {
                    Button(
                        onClick = onOpenSettlement,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (card.isUrgent || card.isOverdue) {
                                semanticColors.expenseCoral
                            } else {
                                semanticColors.warningAmber
                            }
                        ),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(
                            text = "Pay Now",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                } else if (card.currentDebt.isPositive) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Text(
                            text = "✨ ${card.floatDaysRemaining}d Float",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Text(
                            text = "Clear",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = semanticColors.incomeEmerald,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Utilization Progress Bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LinearProgressIndicator(
                    progress = { (card.utilizationPercent / 100f).coerceIn(0f, 1f) },
                    color = progressColor,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    strokeCap = StrokeCap.Round,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                )

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Used: ",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        MoneyText(
                            money = card.currentDebt,
                            currencySpec = cardSpec,
                            grouping = grouping,
                            colorMode = MoneyColorMode.EXPENSE,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = " / ",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        MoneyText(
                            money = card.creditLimit,
                            currencySpec = cardSpec,
                            grouping = grouping,
                            colorMode = MoneyColorMode.NEUTRAL,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Text(
                        text = "${String.format(Locale.US, "%.1f", card.utilizationPercent)}%",
                        style = tabularMoneyStyle(MaterialTheme.typography.titleSmall),
                        fontWeight = FontWeight.Bold,
                        color = progressColor
                    )
                }
            }

            // Interactive Ribbon Footer: Fee Waiver Chip + Top Category
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceContainer,
                        RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                // Interactive Fee Waiver Chip
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (card.milestoneInfo?.isAchieved == true) {
                        semanticColors.incomeEmeraldContainer.copy(alpha = 0.6f)
                    } else {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onOpenMilestoneEditor)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = when {
                                card.milestoneInfo == null -> "🎯 Set Fee Waiver Target ✏️"
                                card.milestoneInfo.isAchieved -> "🎉 Waiver Done! (100%)"
                                else -> {
                                    val remFormatted = MoneyFormatterAndCalculator.formatCompactMinorMoney(
                                        card.milestoneInfo.remainingSpend,
                                        cardSpec,
                                        grouping
                                    )
                                    "🎯 Waiver: ${String.format(Locale.US, "%.0f", card.milestoneInfo.progressPercentage)}% ($remFormatted left) ✏️"
                                }
                            },
                            style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                            fontWeight = FontWeight.Bold,
                            color = if (card.milestoneInfo?.isAchieved == true) {
                                semanticColors.onIncomeEmeraldContainer
                            } else {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            }
                        )
                    }
                }

                // Top Category
                Text(
                    text = if (card.topCategoryName != null) {
                        "Top: ${card.topCategoryName} (${String.format(Locale.US, "%.0f", card.topCategoryPercent)}%)"
                    } else {
                        "Top: None Yet"
                    },
                    style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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

// -------------------------------------------------------------------------
// MODAL BOTTOM SHEET: CARD MILESTONE & ANNUAL FEE WAIVER EDITOR
// -------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CardMilestoneEditorBottomSheet(
    card: CreditCardAnalyticsItem,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem,
    onDismiss: () -> Unit,
    onSave: (title: String, startDateEpoch: Long, targetMinor: Long, feeMinor: Long?, autoRenew: Boolean) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val semanticColors = LocalFolioSemanticColors.current
    val cardSpec = remember(card.account.currencyCode) {
        SupportedCurrencies.getByCode(card.account.currencyCode)
            ?: SupportedCurrencies.fromCode(card.account.currencyCode)
    }

    var milestoneTitle by remember {
        mutableStateOf(card.milestoneInfo?.milestoneTitle ?: "Annual Fee Waiver")
    }
    var startDateEpochDay by remember {
        mutableLongStateOf(card.milestoneInfo?.cycleStartDate?.toEpochDay() ?: LocalDate.now().toEpochDay())
    }
    var targetAmountInput by remember {
        val currentTarget = card.milestoneInfo?.targetAmount?.amountMinor ?: 100_000_00L
        mutableStateOf((currentTarget / 100L).toString())
    }
    var feeSavedInput by remember {
        val currentFee = card.milestoneInfo?.annualFeeSaved?.amountMinor ?: 1_000_00L
        mutableStateOf((currentFee / 100L).toString())
    }
    var isAutoRenewing by remember {
        mutableStateOf(card.milestoneInfo?.isAutoRenewing ?: true)
    }
    var showDatePickerDialog by remember { mutableStateOf(false) }

    val parsedTargetMinor = (targetAmountInput.toDoubleOrNull() ?: 0.0) * 100.0
    val parsedFeeMinor = feeSavedInput.toDoubleOrNull()?.let { (it * 100.0).toLong() }

    // Live preview evaluation
    val liveConfig = remember(milestoneTitle, startDateEpochDay, parsedTargetMinor, parsedFeeMinor, isAutoRenewing) {
        CardMilestoneConfig(
            accountId = card.account.id,
            milestoneTitle = milestoneTitle.ifBlank { "Annual Fee Waiver" },
            cycleStartDateEpochDay = startDateEpochDay,
            targetAmountMinor = parsedTargetMinor.toLong(),
            annualFeeSavedMinor = parsedFeeMinor,
            isAutoRenewing = isAutoRenewing
        )
    }
    val livePreviewInfo = remember(liveConfig, card.ytdSpend) {
        CardMilestoneCalculator.evaluateMilestone(
            config = liveConfig,
            qualifyingSpendMinor = card.ytdSpend.amountMinor,
            referenceDate = LocalDate.now()
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
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
                        iconName = card.account.iconName,
                        colorHex = card.account.colorHex,
                        size = 36.dp,
                        iconSize = 18.dp
                    )
                    Column {
                        Text(
                            text = "${card.account.name} Milestone",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Track fee waivers and spend bonuses",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Filled.Close, contentDescription = "Close")
                }
            }

            // Milestone Title
            OutlinedTextField(
                value = milestoneTitle,
                onValueChange = { milestoneTitle = it },
                label = { Text("Milestone Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // START DATE / ANNIVERSARY DATE PICKER
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { showDatePickerDialog = true }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "CYCLE / ANNIVERSARY START DATE",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = LocalDate.ofEpochDay(startDateEpochDay).format(fullDateFormatter),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.CalendarToday,
                        contentDescription = "Pick Date",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Target Spend & Annual Fee Inputs
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = targetAmountInput,
                    onValueChange = { targetAmountInput = it.filter { char -> char.isDigit() } },
                    label = { Text("Target Spend (${cardSpec.symbol})") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = feeSavedInput,
                    onValueChange = { feeSavedInput = it.filter { char -> char.isDigit() } },
                    label = { Text("Fee Saved (${cardSpec.symbol})") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            // Auto-Renewal Switch (Card Anniversary)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auto-renew each anniversary year",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Automatically advances by 1 year each card anniversary.",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isAutoRenewing,
                        onCheckedChange = { isAutoRenewing = it }
                    )
                }
            }

            // Live Calculated Insights Box
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Active Window:", style = MaterialTheme.typography.bodySmall)
                        Text(
                            "${livePreviewInfo.cycleStartDate.format(shortDateFormatter)} – ${livePreviewInfo.cycleEndDate.format(fullDateFormatter)}",
                            style = tabularMoneyStyle(MaterialTheme.typography.bodySmall),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Current Spend:", style = MaterialTheme.typography.bodySmall)
                        MoneyText(
                            money = livePreviewInfo.qualifyingSpend,
                            currencySpec = cardSpec,
                            grouping = grouping,
                            colorMode = MoneyColorMode.INCOME,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Remaining to Waive:", style = MaterialTheme.typography.bodySmall)
                        MoneyText(
                            money = livePreviewInfo.remainingSpend,
                            currencySpec = cardSpec,
                            grouping = grouping,
                            colorMode = MoneyColorMode.EXPENSE,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Days Remaining:", style = MaterialTheme.typography.bodySmall)
                        Text(
                            "${livePreviewInfo.daysRemaining} Days",
                            style = tabularMoneyStyle(MaterialTheme.typography.bodySmall),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Required Daily Pace:", style = MaterialTheme.typography.bodySmall)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            MoneyText(
                                money = livePreviewInfo.requiredDailySpend,
                                currencySpec = cardSpec,
                                grouping = grouping,
                                colorMode = MoneyColorMode.NEUTRAL,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = " / day",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            // Save Button
            Button(
                onClick = {
                    onSave(
                        milestoneTitle.ifBlank { "Annual Fee Waiver" },
                        startDateEpochDay,
                        parsedTargetMinor.toLong(),
                        parsedFeeMinor,
                        isAutoRenewing
                    )
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "Save Milestone Target",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    if (showDatePickerDialog) {
        val initialMillis = remember(startDateEpochDay) {
            LocalDate.ofEpochDay(startDateEpochDay)
                .atStartOfDay(ZoneId.of("UTC"))
                .toInstant()
                .toEpochMilli()
        }
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val picked = datePickerState.selectedDateMillis
                        if (picked != null) {
                            val epochDay = Instant.ofEpochMilli(picked)
                                .atZone(ZoneId.of("UTC"))
                                .toLocalDate()
                                .toEpochDay()
                            startDateEpochDay = epochDay
                        }
                        showDatePickerDialog = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

// -------------------------------------------------------------------------
// MODAL BOTTOM SHEET: CREDIT BUREAU SAFE TIER PAYDOWN SIMULATOR
// -------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreditPaydownSimulatorBottomSheet(
    cockpit: CreditCardCockpitUiState,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem,
    onDismiss: () -> Unit,
    onSettleTargetDebt: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val semanticColors = LocalFolioSemanticColors.current

    val totalLimitMinor = cockpit.totalCreditLimit.amountMinor
    val totalDebtMinor = cockpit.totalCcDebt.amountMinor
    val safe30Minor = (totalLimitMinor * 0.30).toLong()
    val neededPaydownMinor = (totalDebtMinor - safe30Minor).coerceAtLeast(0L)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
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
                    Icon(
                        imageVector = Icons.Filled.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Bureau Safe Tier Simulator",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Filled.Close, contentDescription = "Close")
                }
            }

            Text(
                text = "Credit bureaus (CIBIL / Experian) reward keeping overall credit card utilization below 30%. Single card spikes (>30%) can also temporarily lower scores.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(14.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Current Aggregate Debt:", style = MaterialTheme.typography.bodyMedium)
                        MoneyText(
                            money = cockpit.totalCcDebt,
                            currencySpec = currencySpec,
                            grouping = grouping,
                            colorMode = MoneyColorMode.EXPENSE,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Safe Tier Ceiling (30%):", style = MaterialTheme.typography.bodyMedium)
                        MoneyText(
                            money = MinorMoney(safe30Minor),
                            currencySpec = currencySpec,
                            grouping = grouping,
                            colorMode = MoneyColorMode.NEUTRAL,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    HorizontalDivider(color = semanticColors.cardStrokeSubtle)

                    if (neededPaydownMinor > 0L) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Paydown Needed for <30%:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = semanticColors.warningAmber
                            )
                            MoneyText(
                                money = MinorMoney(neededPaydownMinor),
                                currencySpec = currencySpec,
                                grouping = grouping,
                                colorMode = MoneyColorMode.EXPENSE,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Black
                            )
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Shield,
                                contentDescription = null,
                                tint = semanticColors.incomeEmerald,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Your overall debt is already in the Safe Tier (<30%)!",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = semanticColors.incomeEmerald
                            )
                        }
                    }
                }
            }

            if (neededPaydownMinor > 0L) {
                Button(
                    onClick = onSettleTargetDebt,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "Pay Down Debt Now",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                FilledTonalButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Done")
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// DIALOG: ALL CARDS FLOAT RANKING DRAWER
// -------------------------------------------------------------------------

@Composable
private fun AllCardsFloatRankingDialog(
    cards: List<CreditCardAnalyticsItem>,
    onDismiss: () -> Unit,
    onSelectCard: (AccountSummary) -> Unit
) {
    val sortedByFloat = remember(cards) {
        cards.sortedByDescending { it.floatDaysRemaining }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Smart Float Rankings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Cards ranked by interest-free grace float if swiped today:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                sortedByFloat.forEachIndexed { index, item ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (index == 0) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        } else {
                            MaterialTheme.colorScheme.surfaceContainer
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelectCard(item.account) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CategoryIconBadge(
                                    iconName = item.account.iconName,
                                    colorHex = item.account.colorHex,
                                    size = 28.dp,
                                    iconSize = 14.dp
                                )
                                Column {
                                    Text(
                                        text = item.account.name,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (index == 0) "Top recommendation" else "Due ${item.targetDate.format(shortDateFormatter)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (index == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh
                            ) {
                                Text(
                                    text = "✨ ${item.floatDaysRemaining}d",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (index == 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
