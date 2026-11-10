package com.folioledger.app.ui.stats.components

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
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shield
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.folioledger.app.R
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
import com.folioledger.app.ui.stats.*
import com.folioledger.app.ui.stats.components.*
import com.folioledger.app.ui.stats.dialogs.*
import com.folioledger.app.ui.stats.sheets.*
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


// -------------------------------------------------------------------------
// SECTION 1: HERO GAUGE & CASH SAFETY HUB
// -------------------------------------------------------------------------

@Composable
internal fun CreditHeroArcGaugeCard(
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
                        text = stringResource(R.string.safe_to_spend_formula_cards),
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

