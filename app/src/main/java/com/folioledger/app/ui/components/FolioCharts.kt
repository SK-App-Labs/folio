// File: app/src/main/java/com/folioledger/app/ui/components/FolioCharts.kt
package com.folioledger.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.folioledger.app.domain.model.CategorySpendStat
import com.folioledger.app.domain.model.CurrencySpec
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.MonthlyTrendBar
import com.folioledger.app.domain.model.NumberGroupingSystem
import com.folioledger.app.domain.model.SupportedCurrencies
import com.folioledger.app.ui.theme.LocalFolioSemanticColors
import com.folioledger.app.ui.theme.tabularMoneyStyle
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.sin

private data class DonutSliceArc(
    val stat: CategorySpendStat,
    val startAngle: Float,
    val sweepAngle: Float,
    val color: Color
)

@Composable
fun InteractiveCategoryDonutChart(
    stats: List<CategorySpendStat>,
    modifier: Modifier = Modifier,
    currencySpec: CurrencySpec = SupportedCurrencies.INR,
    grouping: NumberGroupingSystem = currencySpec.defaultGrouping,
    selectedCategoryId: Long? = null,
    onCategorySelected: ((CategorySpendStat?) -> Unit)? = null,
    centerLabelWhenUnselected: String = "Total Spend",
    chartSize: Dp = 230.dp,
    strokeWidth: Dp = 32.dp,
    showLegendChips: Boolean = true
) {
    val semanticColors = LocalFolioSemanticColors.current
    var internalSelectedId by rememberSaveable { mutableStateOf<Long?>(null) }
    val activeSelectedId = selectedCategoryId ?: internalSelectedId

    var entryTarget by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(stats) {
        entryTarget = 1f
    }

    val entryProgress by animateFloatAsState(
        targetValue = entryTarget,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "DonutEntryAnimation"
    )

    val totalMinorSum = remember(stats) {
        stats.fold(0L) { acc, item -> acc + max(0L, item.netAmount.amountMinor) }
    }
    val totalMoney = remember(totalMinorSum) { MinorMoney(totalMinorSum) }

    val slices = remember(stats, totalMinorSum) {
        if (totalMinorSum <= 0L || stats.isEmpty()) {
            emptyList()
        } else {
            var runningAngle = -90f
            stats.filter { it.netAmount.amountMinor > 0L }.map { stat ->
                val ratio = stat.netAmount.amountMinor.toFloat() / totalMinorSum.toFloat()
                val sweep = (ratio * 360f).coerceAtLeast(1.5f)
                val slice = DonutSliceArc(
                    stat = stat,
                    startAngle = runningAngle,
                    sweepAngle = sweep,
                    color = parseHexColorSafely(stat.colorHex)
                )
                runningAngle += sweep
                slice
            }
        }
    }

    val selectedStat = remember(slices, activeSelectedId) {
        slices.firstOrNull { it.stat.categoryId == activeSelectedId }?.stat
    }

    val handleSelect: (CategorySpendStat?) -> Unit = { tapped ->
        val next = if (tapped?.categoryId == activeSelectedId) null else tapped
        internalSelectedId = next?.categoryId
        onCategorySelected?.invoke(next)
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(chartSize)
            ) {
                val trackColor = MaterialTheme.colorScheme.surfaceContainerHighest

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(slices) {
                            detectTapGestures { tapOffset ->
                                if (slices.isEmpty()) return@detectTapGestures
                                val center = Offset(size.width / 2f, size.height / 2f)
                                val dx = tapOffset.x - center.x
                                val dy = tapOffset.y - center.y
                                val distance = hypot(dx, dy)

                                val strokePx = strokeWidth.toPx()
                                val outerRadius = (kotlin.math.min(size.width, size.height).toFloat() / 2f) - (strokePx * 0.2f)
                                val innerRadius = outerRadius - (strokePx * 1.45f)

                                if (distance < innerRadius) {
                                    handleSelect(null)
                                    return@detectTapGestures
                                }
                                if (distance > outerRadius + strokePx * 0.5f) {
                                    return@detectTapGestures
                                }

                                var touchDeg = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                                if (touchDeg < -90f) {
                                    touchDeg += 360f
                                }

                                val hitSlice = slices.firstOrNull { slice ->
                                    val normalizedTouch = if (touchDeg < slice.startAngle) {
                                        touchDeg + 360f
                                    } else {
                                        touchDeg
                                    }
                                    normalizedTouch >= slice.startAngle &&
                                            normalizedTouch <= (slice.startAngle + slice.sweepAngle)
                                }
                                handleSelect(hitSlice?.stat)
                            }
                        }
                ) {
                    val baseStrokePx = strokeWidth.toPx()
                    val diameter = size.minDimension - baseStrokePx * 1.35f
                    val topLeft = Offset(
                        x = (size.width - diameter) / 2f,
                        y = (size.height - diameter) / 2f
                    )
                    val arcSize = Size(diameter, diameter)

                    if (slices.isEmpty()) {
                        drawArc(
                            color = trackColor,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = baseStrokePx, cap = StrokeCap.Round)
                        )
                    } else {
                        val gapAngle = if (slices.size > 1) 2.2f else 0f
                        slices.forEach { slice ->
                            val isSelected = slice.stat.categoryId == activeSelectedId
                            val animatedSweep = (slice.sweepAngle - gapAngle).coerceAtLeast(1f) * entryProgress
                            val currentStroke = if (isSelected) baseStrokePx * 1.22f else baseStrokePx
                            val alpha = if (activeSelectedId == null || isSelected) 1f else 0.42f

                            val shiftDistance = if (isSelected) baseStrokePx * 0.14f else 0f
                            val midAngleRad = Math.toRadians(
                                (slice.startAngle + slice.sweepAngle / 2f).toDouble()
                            )
                            val shiftedTopLeft = Offset(
                                x = topLeft.x + (cos(midAngleRad) * shiftDistance).toFloat(),
                                y = topLeft.y + (sin(midAngleRad) * shiftDistance).toFloat()
                            )

                            drawArc(
                                color = slice.color.copy(alpha = alpha),
                                startAngle = slice.startAngle + gapAngle / 2f,
                                sweepAngle = animatedSweep,
                                useCenter = false,
                                topLeft = shiftedTopLeft,
                                size = arcSize,
                                style = Stroke(width = currentStroke, cap = StrokeCap.Butt)
                            )
                        }
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .padding(horizontal = strokeWidth + 14.dp)
                        .clickable(enabled = selectedStat != null) {
                            handleSelect(null)
                        }
                ) {
                    if (selectedStat != null) {
                        Text(
                            text = selectedStat.categoryName,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        MoneyText(
                            money = selectedStat.netAmount,
                            currencySpec = currencySpec,
                            grouping = grouping,
                            colorMode = MoneyColorMode.NEUTRAL,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val formattedPct = remember(selectedStat.percentageOfTotal) {
                            String.format(java.util.Locale.US, "%.1f%%", selectedStat.percentageOfTotal)
                        }
                        Surface(
                            color = parseHexColorSafely(selectedStat.colorHex).copy(alpha = 0.16f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "$formattedPct · ${selectedStat.transactionCount} txns",
                                style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                                color = parseHexColorSafely(selectedStat.colorHex),
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    } else {
                        Text(
                            text = centerLabelWhenUnselected,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        MoneyText(
                            money = totalMoney,
                            currencySpec = currencySpec,
                            grouping = grouping,
                            colorMode = MoneyColorMode.NEUTRAL,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        if (slices.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Tap slice to inspect",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (showLegendChips && slices.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    slices.forEach { slice ->
                        val isSelected = slice.stat.categoryId == activeSelectedId
                        val pctText = remember(slice.stat.percentageOfTotal) {
                            String.format(java.util.Locale.US, "%.1f%%", slice.stat.percentageOfTotal)
                        }
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) {
                                slice.color.copy(alpha = 0.2f)
                            } else {
                                MaterialTheme.colorScheme.surfaceContainer
                            },
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isSelected) slice.color else semanticColors.cardStrokeSubtle
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { handleSelect(slice.stat) }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(slice.color)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = slice.stat.categoryName,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = pctText,
                                    style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SixMonthTrendBarChart(
    bars: List<MonthlyTrendBar>,
    modifier: Modifier = Modifier,
    currencySpec: CurrencySpec = SupportedCurrencies.INR,
    grouping: NumberGroupingSystem = currencySpec.defaultGrouping,
    initialNetMode: Boolean = false,
    chartHeight: Dp = 200.dp
) {
    val semanticColors = LocalFolioSemanticColors.current
    var showNetMode by rememberSaveable { mutableStateOf(initialNetMode) }
    var selectedBarIndex by rememberSaveable(bars.size) {
        mutableIntStateOf(if (bars.isNotEmpty()) bars.lastIndex else -1)
    }

    var entryTarget by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(bars, showNetMode) {
        entryTarget = 0f
        entryTarget = 1f
    }

    val entryProgress by animateFloatAsState(
        targetValue = entryTarget,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "BarChartEntryAnimation"
    )

    val maxMagnitudeMinor = remember(bars, showNetMode) {
        if (bars.isEmpty()) {
            1L
        } else if (showNetMode) {
            bars.maxOf { it.netSavings.abs().amountMinor }.coerceAtLeast(1L)
        } else {
            bars.maxOf {
                max(it.totalIncome.amountMinor, it.totalExpense.amountMinor)
            }.coerceAtLeast(1L)
        }
    }

    val selectedBar = bars.getOrNull(selectedBarIndex)

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "6-Month Cashflow Trend",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Tap any month bar to inspect exact ledger totals",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = !showNetMode,
                        onClick = { showNetMode = false },
                        label = {
                            Text(
                                text = "Inc / Exp",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    )
                    FilterChip(
                        selected = showNetMode,
                        onClick = { showNetMode = true },
                        label = {
                            Text(
                                text = "Net",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    )
                }
            }

            if (selectedBar != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = selectedBar.periodLabel,
                            style = tabularMoneyStyle(MaterialTheme.typography.labelLarge),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Income",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                MoneyText(
                                    money = selectedBar.totalIncome,
                                    currencySpec = currencySpec,
                                    grouping = grouping,
                                    colorMode = MoneyColorMode.INCOME,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Expense",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                MoneyText(
                                    money = selectedBar.totalExpense,
                                    currencySpec = currencySpec,
                                    grouping = grouping,
                                    colorMode = MoneyColorMode.EXPENSE,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Net",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                MoneyText(
                                    money = selectedBar.netSavings,
                                    currencySpec = currencySpec,
                                    grouping = grouping,
                                    colorMode = MoneyColorMode.AUTO_SIGN,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    showPlusSignForPositive = true
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            val gridColor = semanticColors.chartGridLine
            val incomeColor = semanticColors.incomeEmerald
            val expenseColor = semanticColors.expenseCoral
            val highlightSurface = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(chartHeight)
                    .pointerInput(bars) {
                        detectTapGestures { offset ->
                            if (bars.isEmpty()) return@detectTapGestures
                            val slotWidth = size.width / bars.size.toFloat()
                            val index = (offset.x / slotWidth).toInt().coerceIn(0, bars.lastIndex)
                            selectedBarIndex = index
                        }
                    }
            ) {
                val horizontalLines = 4
                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                val usableHeight = size.height - 12.dp.toPx()
                val topPadding = 6.dp.toPx()

                for (i in 0..horizontalLines) {
                    val y = topPadding + (usableHeight / horizontalLines) * i
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = if (i == horizontalLines) null else dashEffect
                    )
                }

                if (bars.isEmpty()) return@Canvas

                val slotWidth = size.width / bars.size.toFloat()
                val cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())

                bars.forEachIndexed { index, bar ->
                    val slotLeft = index * slotWidth
                    val isSelected = index == selectedBarIndex

                    if (isSelected) {
                        drawRoundRect(
                            color = highlightSurface,
                            topLeft = Offset(slotLeft + 4.dp.toPx(), 0f),
                            size = Size(slotWidth - 8.dp.toPx(), usableHeight + topPadding),
                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                        )
                    }

                    if (!showNetMode) {
                        val barWidth = (slotWidth * 0.28f).coerceAtMost(18.dp.toPx())
                        val gap = 4.dp.toPx()
                        val groupWidth = barWidth * 2f + gap
                        val startX = slotLeft + (slotWidth - groupWidth) / 2f
                        val baselineY = topPadding + usableHeight

                        val incomeRatio = (bar.totalIncome.amountMinor.toFloat() / maxMagnitudeMinor.toFloat())
                            .coerceIn(0f, 1f) * entryProgress
                        val expenseRatio = (bar.totalExpense.amountMinor.toFloat() / maxMagnitudeMinor.toFloat())
                            .coerceIn(0f, 1f) * entryProgress

                        val incomeHeight = (usableHeight * incomeRatio).coerceAtLeast(3.dp.toPx())
                        val expenseHeight = (usableHeight * expenseRatio).coerceAtLeast(3.dp.toPx())

                        drawRoundRect(
                            color = if (isSelected) incomeColor else incomeColor.copy(alpha = 0.78f),
                            topLeft = Offset(startX, baselineY - incomeHeight),
                            size = Size(barWidth, incomeHeight),
                            cornerRadius = cornerRadius
                        )

                        drawRoundRect(
                            color = if (isSelected) expenseColor else expenseColor.copy(alpha = 0.78f),
                            topLeft = Offset(startX + barWidth + gap, baselineY - expenseHeight),
                            size = Size(barWidth, expenseHeight),
                            cornerRadius = cornerRadius
                        )
                    } else {
                        val zeroY = topPadding + (usableHeight / 2f)
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, zeroY),
                            end = Offset(size.width, zeroY),
                            strokeWidth = 1.5.dp.toPx()
                        )

                        val barWidth = (slotWidth * 0.42f).coerceAtMost(24.dp.toPx())
                        val startX = slotLeft + (slotWidth - barWidth) / 2f
                        val netMinor = bar.netSavings.amountMinor
                        val ratio = (kotlin.math.abs(netMinor).toFloat() / maxMagnitudeMinor.toFloat())
                            .coerceIn(0f, 1f) * entryProgress
                        val barHeight = (usableHeight * 0.46f * ratio).coerceAtLeast(3.dp.toPx())

                        val color = if (netMinor >= 0L) incomeColor else expenseColor
                        val topY = if (netMinor >= 0L) zeroY - barHeight else zeroY

                        drawRoundRect(
                            color = if (isSelected) color else color.copy(alpha = 0.8f),
                            topLeft = Offset(startX, topY),
                            size = Size(barWidth, barHeight),
                            cornerRadius = cornerRadius
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                bars.forEachIndexed { index, bar ->
                    val isSelected = index == selectedBarIndex
                    Text(
                        text = bar.periodLabel,
                        style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { selectedBarIndex = index }
                            .padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}