package com.folioledger.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin
import com.folioledger.app.domain.model.CurrencySpec
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.NumberGroupingSystem
import com.folioledger.app.ui.stats.CreditCardHealthUiModel
import com.folioledger.app.ui.stats.CreditUtilizationStatus
import com.folioledger.app.ui.stats.NetWorthChartMetricFilter
import com.folioledger.app.ui.stats.NetWorthGrowthTrendItem
import com.folioledger.app.ui.theme.LocalFolioSemanticColors

@Composable
fun NetWorthGrowthCanvasChart(
    trendItems: List<NetWorthGrowthTrendItem>,
    selectedFilter: NetWorthChartMetricFilter,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem,
    onSelectFilter: (NetWorthChartMetricFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    val semanticColors = LocalFolioSemanticColors.current
    var selectedIndex by remember(trendItems) {
        mutableIntStateOf(if (trendItems.isNotEmpty()) trendItems.lastIndex else -1)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            // Header Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ShowChart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Net Worth & Wealth Trajectory",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Filter Chips Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                NetWorthChartMetricFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { onSelectFilter(filter) },
                        label = {
                            Text(
                                text = filter.title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selectedFilter == filter) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                }
            }

            if (trendItems.isEmpty()) {
                Text(
                    text = "Insufficient historical ledger data to plot trend.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                val activeItem = trendItems.getOrNull(selectedIndex) ?: trendItems.last()

                // Live Active Month Inspection Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "${activeItem.monthYearText} Snapshot",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = "🟢 Assets",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                MoneyText(
                                    money = activeItem.assets,
                                    currencySpec = currencySpec,
                                    grouping = grouping,
                                    colorMode = MoneyColorMode.INCOME,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Column {
                                Text(
                                    text = "🔴 Liabilities",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                MoneyText(
                                    money = activeItem.liabilities,
                                    currencySpec = currencySpec,
                                    grouping = grouping,
                                    colorMode = MoneyColorMode.EXPENSE,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "✨ Net Worth",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                MoneyText(
                                    money = activeItem.netWorth,
                                    currencySpec = currencySpec,
                                    grouping = grouping,
                                    colorMode = MoneyColorMode.AUTO_SIGN,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Dynamic Scaled Canvas Area Chart with Negative Baseline Support
                val assetColor = semanticColors.incomeEmerald
                val liabilityColor = semanticColors.expenseCoral
                val primaryColor = MaterialTheme.colorScheme.primary
                val surfaceColor = MaterialTheme.colorScheme.surface
                val gridColor = semanticColors.cardStrokeSubtle

                val maxAsset = remember(trendItems) { trendItems.maxOfOrNull { it.assets.amountMinor } ?: 0L }
                val maxLiability = remember(trendItems) { trendItems.maxOfOrNull { it.liabilities.amountMinor } ?: 0L }
                val maxNetWorth = remember(trendItems) { trendItems.maxOfOrNull { it.netWorth.amountMinor } ?: 0L }
                val minNetWorth = remember(trendItems) { trendItems.minOfOrNull { it.netWorth.amountMinor } ?: 0L }

                val ceiling = remember(maxAsset, maxNetWorth) {
                    maxOf(maxAsset, maxNetWorth, 1000L).toFloat()
                }
                val floor = remember(maxLiability, minNetWorth) {
                    minOf(-maxLiability, minNetWorth, 0L).toFloat()
                }
                val totalSpan = remember(ceiling, floor) {
                    (ceiling - floor).coerceAtLeast(1f)
                }

                val zeroDashEffect = remember { PathEffect.dashPathEffect(floatArrayOf(6f, 6f)) }
                val activeDashEffect = remember { PathEffect.dashPathEffect(floatArrayOf(4f, 4f)) }
                val nwPath = remember { Path() }
                val fillPath = remember { Path() }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .pointerInput(trendItems) {
                            detectTapGestures { offset ->
                                val stepX = size.width / (trendItems.size - 1).coerceAtLeast(1)
                                val tappedIdx = (offset.x / stepX).toInt().coerceIn(0, trendItems.lastIndex)
                                selectedIndex = tappedIdx
                            }
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height
                        val padTop = 16.dp.toPx()
                        val padBottom = 16.dp.toPx()
                        val usableHeight = height - padTop - padBottom
                        val stepX = width / (trendItems.size - 1).coerceAtLeast(1)
                        val zeroY = padTop + usableHeight * (ceiling / totalSpan)
                        val barWidth = (stepX * 0.32f).coerceIn(4.dp.toPx(), 20.dp.toPx())

                        fun getY(amountMinor: Long): Float {
                            return padTop + usableHeight * ((ceiling - amountMinor.toFloat()) / totalSpan)
                        }

                        // Dashed Zero Baseline
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, zeroY),
                            end = Offset(width, zeroY),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = zeroDashEffect
                        )

                        // Waterfall Bars (Green Assets reaching UP, Red Liabilities reaching DOWN)
                        trendItems.forEachIndexed { i, item ->
                            val centerX = i * stepX

                            if (selectedFilter == NetWorthChartMetricFilter.ALL_IN_ONE || selectedFilter == NetWorthChartMetricFilter.ASSETS) {
                                val assetY = getY(item.assets.amountMinor)
                                val assetH = (zeroY - assetY).coerceAtLeast(0f)
                                if (assetH > 0f) {
                                    drawRect(
                                        color = assetColor.copy(alpha = 0.80f),
                                        topLeft = Offset(centerX - barWidth, assetY),
                                        size = Size(barWidth, assetH)
                                    )
                                }
                            }

                            if (selectedFilter == NetWorthChartMetricFilter.ALL_IN_ONE || selectedFilter == NetWorthChartMetricFilter.LIABILITIES) {
                                val liabilityY = getY(-item.liabilities.amountMinor)
                                val liabilityH = (liabilityY - zeroY).coerceAtLeast(0f)
                                if (liabilityH > 0f) {
                                    drawRect(
                                        color = liabilityColor.copy(alpha = 0.80f),
                                        topLeft = Offset(centerX, zeroY),
                                        size = Size(barWidth, liabilityH)
                                    )
                                }
                            }
                        }

                        // Smooth Bezier Curve & Gradient Area Fill for Net Worth
                        if (selectedFilter == NetWorthChartMetricFilter.ALL_IN_ONE || selectedFilter == NetWorthChartMetricFilter.NET_WORTH) {
                            nwPath.reset()
                            fillPath.reset()

                            trendItems.forEachIndexed { i, item ->
                                val x = i * stepX
                                val y = getY(item.netWorth.amountMinor)
                                if (i == 0) {
                                    nwPath.moveTo(x, y)
                                    fillPath.moveTo(x, zeroY)
                                    fillPath.lineTo(x, y)
                                } else {
                                    val prevX = (i - 1) * stepX
                                    val prevY = getY(trendItems[i - 1].netWorth.amountMinor)
                                    val cX = (prevX + x) / 2f
                                    nwPath.cubicTo(cX, prevY, cX, y, x, y)
                                    fillPath.cubicTo(cX, prevY, cX, y, x, y)
                                }
                            }
                            val lastX = (trendItems.size - 1) * stepX
                            fillPath.lineTo(lastX, zeroY)
                            fillPath.close()

                            // Area Gradient Fill
                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(primaryColor.copy(alpha = 0.28f), primaryColor.copy(alpha = 0.02f)),
                                    startY = padTop,
                                    endY = zeroY.coerceAtLeast(padTop + 1f)
                                )
                            )

                            // Stroke Curve
                            drawPath(
                                path = nwPath,
                                color = primaryColor,
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                            )

                            // Marker Nodes
                            trendItems.forEachIndexed { i, item ->
                                val x = i * stepX
                                val y = getY(item.netWorth.amountMinor)
                                drawCircle(
                                    color = surfaceColor,
                                    radius = 4.5.dp.toPx(),
                                    center = Offset(x, y)
                                )
                                drawCircle(
                                    color = primaryColor,
                                    radius = 3.dp.toPx(),
                                    center = Offset(x, y)
                                )
                            }
                        }

                        // Active Selected Month Indicator Line & Dot
                        if (selectedIndex in trendItems.indices) {
                            val activeX = selectedIndex * stepX
                            drawLine(
                                color = primaryColor.copy(alpha = 0.6f),
                                start = Offset(activeX, 0f),
                                end = Offset(activeX, height),
                                strokeWidth = 1.5.dp.toPx(),
                                pathEffect = activeDashEffect
                            )
                            val activeItem = trendItems[selectedIndex]
                            val activeY = getY(activeItem.netWorth.amountMinor)
                            drawCircle(
                                color = primaryColor,
                                radius = 6.dp.toPx(),
                                center = Offset(activeX, activeY)
                            )
                        }
                    }
                }

                // Month X-Axis Labels (adaptive for 6 vs 12 months)
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp)
                ) {
                    trendItems.forEachIndexed { idx, item ->
                        val label = if (trendItems.size > 6) {
                            if (idx == selectedIndex) item.monthYearText.take(3) else item.monthYearText.take(1)
                        } else {
                            item.monthYearText.take(3)
                        }
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (idx == selectedIndex) FontWeight.Bold else FontWeight.Medium,
                            color = if (idx == selectedIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private data class CreditTierInfo(
    val tierText: String,
    val tierBg: Color,
    val tierFg: Color,
    val activeColor: Color
)

@Composable
fun CreditUtilizationGauge(
    utilizationPercent: Float,
    modifier: Modifier = Modifier
) {
    val semanticColors = LocalFolioSemanticColors.current
    val trackColor = MaterialTheme.colorScheme.surfaceContainerHighest

    val tierInfo = remember(utilizationPercent) {
        when {
            utilizationPercent > 50f -> CreditTierInfo(
                tierText = "Risky (>50%)",
                tierBg = semanticColors.expenseCoralContainer,
                tierFg = semanticColors.onExpenseCoralContainer,
                activeColor = semanticColors.expenseCoral
            )
            utilizationPercent > 30f -> CreditTierInfo(
                tierText = "High (30-50%)",
                tierBg = semanticColors.warningAmberContainer,
                tierFg = semanticColors.onWarningAmberContainer,
                activeColor = semanticColors.warningAmber
            )
            utilizationPercent > 10f -> CreditTierInfo(
                tierText = "Good (10-30%)",
                tierBg = semanticColors.incomeEmeraldContainer,
                tierFg = semanticColors.onIncomeEmeraldContainer,
                activeColor = Color(0xFF00BFA5)
            )
            else -> CreditTierInfo(
                tierText = "Optimal (<10%)",
                tierBg = semanticColors.incomeEmeraldContainer,
                tierFg = semanticColors.onIncomeEmeraldContainer,
                activeColor = semanticColors.incomeEmerald
            )
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth(0.72f)
                .height(120.dp)
        ) {
            val strokeWidth = 14.dp.toPx()
            val arcPadding = strokeWidth / 2f
            val arcWidth = size.width - strokeWidth
            val arcHeight = (size.height - arcPadding) * 2f

            val arcTopLeft = Offset(arcPadding, arcPadding)
            val arcSize = Size(arcWidth, arcHeight)

            // Background Track Arc (180 to 360 degrees)
            drawArc(
                color = trackColor,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Active Progress Arc
            val sweep = (utilizationPercent / 100f).coerceIn(0f, 1f) * 180f
            if (sweep > 0f) {
                drawArc(
                    color = tierInfo.activeColor,
                    startAngle = 180f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // Threshold tick marks at 10%, 30%, 50%
            val cx = size.width / 2f
            val cy = arcHeight / 2f + arcPadding
            val r = arcWidth / 2f
            val thresholds = floatArrayOf(0.10f, 0.30f, 0.50f)
            for (t in thresholds) {
                val angleDeg = 180f + t * 180f
                val rad = Math.toRadians(angleDeg.toDouble())
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
                    color = trackColor.copy(alpha = 0.9f),
                    start = p1,
                    end = p2,
                    strokeWidth = 2.dp.toPx()
                )
            }
        }

        // Center Content Overlay (Percentage & Tier Badge)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.padding(top = 26.dp)
        ) {
            Text(
                text = "${String.format(Locale.US, "%.1f", utilizationPercent)}%",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Credit Utilized",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = tierInfo.tierBg
            ) {
                Text(
                    text = tierInfo.tierText,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = tierInfo.tierFg,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun CreditHealthDashboardCard(
    totalAvailableCredit: MinorMoney,
    totalCreditLimit: MinorMoney,
    totalUsedCredit: MinorMoney,
    overallUtilizationPercent: Float,
    creditCardHealthList: List<CreditCardHealthUiModel>,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem,
    modifier: Modifier = Modifier
) {
    val semanticColors = LocalFolioSemanticColors.current

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with Live Snapshot Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.CreditCard,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Credit & Debt Health",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest
                ) {
                    Text(
                        text = "⏱ Live Snapshot",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Semi-Circular Utilization Gauge
            CreditUtilizationGauge(
                utilizationPercent = overallUtilizationPercent
            )

            // Summary Numbers
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Used Debt: ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    MoneyText(
                        money = totalUsedCredit,
                        currencySpec = currencySpec,
                        grouping = grouping,
                        colorMode = MoneyColorMode.EXPENSE,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Remaining Available: ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        MoneyText(
                            money = totalAvailableCredit,
                            currencySpec = currencySpec,
                            grouping = grouping,
                            colorMode = MoneyColorMode.INCOME,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = " / ",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        MoneyText(
                            money = totalCreditLimit,
                            currencySpec = currencySpec,
                            grouping = grouping,
                            colorMode = MoneyColorMode.NEUTRAL,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            HorizontalDivider(color = semanticColors.cardStrokeSubtle)

            if (creditCardHealthList.isEmpty()) {
                Text(
                    text = "No active credit cards found.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Individual Credit Card Breakdown",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )

                    creditCardHealthList.forEach { card ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CategoryIconBadge(
                                            iconName = card.account.iconName,
                                            colorHex = card.account.colorHex,
                                            size = 32.dp,
                                            iconSize = 16.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = card.account.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = when (card.status) {
                                            CreditUtilizationStatus.WARNING -> semanticColors.expenseCoralContainer
                                            CreditUtilizationStatus.MODERATE -> semanticColors.warningAmberContainer
                                            CreditUtilizationStatus.SAFE -> semanticColors.incomeEmeraldContainer
                                        }
                                    ) {
                                        Text(
                                            text = "${String.format(Locale.US, "%.1f", card.utilizationPercent)}%",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = when (card.status) {
                                                CreditUtilizationStatus.WARNING -> semanticColors.onExpenseCoralContainer
                                                CreditUtilizationStatus.MODERATE -> semanticColors.onWarningAmberContainer
                                                CreditUtilizationStatus.SAFE -> semanticColors.onIncomeEmeraldContainer
                                            },
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Used: ",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    MoneyText(
                                        money = card.totalDebt,
                                        currencySpec = currencySpec,
                                        grouping = grouping,
                                        colorMode = MoneyColorMode.EXPENSE,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                LinearProgressIndicator(
                                    progress = { (card.utilizationPercent / 100f).coerceIn(0f, 1f) },
                                    color = when (card.status) {
                                        CreditUtilizationStatus.WARNING -> semanticColors.expenseCoral
                                        CreditUtilizationStatus.MODERATE -> semanticColors.warningAmber
                                        CreditUtilizationStatus.SAFE -> semanticColors.incomeEmerald
                                    },
                                    trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    strokeCap = StrokeCap.Round,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(CircleShape)
                                )

                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Available: ",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        MoneyText(
                                            money = card.availableCredit,
                                            currencySpec = currencySpec,
                                            grouping = grouping,
                                            colorMode = MoneyColorMode.INCOME,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = " / ",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        MoneyText(
                                            money = card.creditLimit,
                                            currencySpec = currencySpec,
                                            grouping = grouping,
                                            colorMode = MoneyColorMode.NEUTRAL,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }

                                if (card.status == CreditUtilizationStatus.WARNING) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Warning,
                                            contentDescription = null,
                                            tint = semanticColors.expenseCoral,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "High utilization (>80%): Paying down this balance will help protect CIBIL/Credit score.",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = semanticColors.expenseCoral
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
}
