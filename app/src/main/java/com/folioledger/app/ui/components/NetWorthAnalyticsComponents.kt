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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.Locale
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

                // Interactive Canvas Area Chart
                val assetColor = semanticColors.incomeEmerald
                val liabilityColor = semanticColors.expenseCoral
                val primaryColor = MaterialTheme.colorScheme.primary
                val gridColor = semanticColors.cardStrokeSubtle

                val maxVal = remember(trendItems) {
                    trendItems.maxOf { item ->
                        maxOf(
                            item.assets.amountMinor.toFloat(),
                            item.liabilities.amountMinor.toFloat(),
                            item.netWorth.amountMinor.toFloat().coerceAtLeast(1f)
                        )
                    }.coerceAtLeast(1f)
                }

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
                        val stepX = width / (trendItems.size - 1).coerceAtLeast(1)
                        val zeroY = height * 0.5f
                        val barWidth = (stepX * 0.35f).coerceAtMost(24.dp.toPx())

                        // Center Zero Baseline
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, zeroY),
                            end = Offset(width, zeroY),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                        )

                        fun getScaleY(amountMinor: Long): Float {
                            val ratio = (amountMinor.toFloat() / maxVal).coerceIn(0f, 1f)
                            return ratio * (height * 0.4f)
                        }

                        // Waterfall Bars
                        trendItems.forEachIndexed { i, item ->
                            val centerX = i * stepX

                            // Green Assets Bar (Reaching UP from zero baseline)
                            if (selectedFilter == NetWorthChartMetricFilter.ALL_IN_ONE || selectedFilter == NetWorthChartMetricFilter.ASSETS) {
                                val assetH = getScaleY(item.assets.amountMinor)
                                drawRect(
                                    color = assetColor.copy(alpha = 0.85f),
                                    topLeft = Offset(centerX - barWidth, zeroY - assetH),
                                    size = androidx.compose.ui.geometry.Size(barWidth, assetH)
                                )
                            }

                            // Red Liabilities Bar (Reaching DOWN from zero baseline)
                            if (selectedFilter == NetWorthChartMetricFilter.ALL_IN_ONE || selectedFilter == NetWorthChartMetricFilter.LIABILITIES) {
                                val liabilityH = getScaleY(item.liabilities.amountMinor)
                                drawRect(
                                    color = liabilityColor.copy(alpha = 0.85f),
                                    topLeft = Offset(centerX, zeroY),
                                    size = androidx.compose.ui.geometry.Size(barWidth, liabilityH)
                                )
                            }
                        }

                        // Connecting Net Worth Line & Nodes
                        if (selectedFilter == NetWorthChartMetricFilter.ALL_IN_ONE || selectedFilter == NetWorthChartMetricFilter.NET_WORTH) {
                            val netWorthPath = Path()
                            trendItems.forEachIndexed { i, item ->
                                val x = i * stepX
                                val netH = getScaleY(item.netWorth.amountMinor.coerceAtLeast(0L))
                                val y = zeroY - netH
                                if (i == 0) netWorthPath.moveTo(x, y) else netWorthPath.lineTo(x, y)
                            }
                            drawPath(
                                path = netWorthPath,
                                color = primaryColor,
                                style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                            )

                            // Nodes
                            trendItems.forEachIndexed { i, item ->
                                val x = i * stepX
                                val netH = getScaleY(item.netWorth.amountMinor.coerceAtLeast(0L))
                                val y = zeroY - netH
                                drawCircle(
                                    color = primaryColor,
                                    radius = 4.dp.toPx(),
                                    center = Offset(x, y)
                                )
                            }
                        }

                        // Active Selected Month Indicator
                        if (selectedIndex in trendItems.indices) {
                            val activeX = selectedIndex * stepX
                            drawLine(
                                color = primaryColor,
                                start = Offset(activeX, 0f),
                                end = Offset(activeX, height),
                                strokeWidth = 2.dp.toPx()
                            )
                        }
                    }
                }

                // Month X-Axis Labels
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    trendItems.forEachIndexed { idx, item ->
                        Text(
                            text = item.monthYearText.take(3),
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
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
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
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        overallUtilizationPercent > 80f -> semanticColors.expenseCoralContainer
                        overallUtilizationPercent > 30f -> semanticColors.warningAmberContainer
                        else -> semanticColors.incomeEmeraldContainer
                    }
                ) {
                    Text(
                        text = "${String.format(Locale.US, "%.1f", overallUtilizationPercent)}% Utilized",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            overallUtilizationPercent > 80f -> semanticColors.onExpenseCoralContainer
                            overallUtilizationPercent > 30f -> semanticColors.onWarningAmberContainer
                            else -> semanticColors.onIncomeEmeraldContainer
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Overall Utilization Progress Bar
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

                LinearProgressIndicator(
                    progress = { (overallUtilizationPercent / 100f).coerceIn(0f, 1f) },
                    color = when {
                        overallUtilizationPercent > 80f -> semanticColors.expenseCoral
                        overallUtilizationPercent > 30f -> semanticColors.warningAmber
                        else -> semanticColors.incomeEmerald
                    },
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    strokeCap = StrokeCap.Round,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape)
                )

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
