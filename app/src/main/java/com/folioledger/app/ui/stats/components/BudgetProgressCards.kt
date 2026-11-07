package com.folioledger.app.ui.stats.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.folioledger.app.domain.model.BudgetProgress
import com.folioledger.app.domain.model.CurrencySpec
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.NumberGroupingSystem
import com.folioledger.app.ui.components.CategoryIconBadge
import com.folioledger.app.ui.components.MoneyColorMode
import com.folioledger.app.ui.components.MoneyText
import com.folioledger.app.ui.stats.StatsPeriodRange
import com.folioledger.app.ui.theme.LocalFolioSemanticColors
import com.folioledger.app.ui.theme.tabularMoneyStyle
import java.time.LocalDate
import java.util.Locale

@Composable
internal fun BudgetTrackWithPacingTick(
    progressFraction: Float,
    elapsedFraction: Float?,
    alertThresholdPercent: Int,
    statusColor: Color,
    trackColor: Color,
    isHeroOverall: Boolean,
    modifier: Modifier = Modifier
) {
    val barHeight = if (isHeroOverall) 12.dp else 10.dp
    val tickOverflow = 4.dp
    val totalCanvasHeight = barHeight + tickOverflow * 2
    val onSurface = MaterialTheme.colorScheme.onSurface
    val primaryColor = MaterialTheme.colorScheme.primary

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(totalCanvasHeight)
    ) {
        val width = size.width
        val height = barHeight.toPx()
        val topOffset = tickOverflow.toPx()
        val cornerRadius = CornerRadius(height / 2f, height / 2f)

        // 1. Draw track background
        drawRoundRect(
            color = trackColor,
            topLeft = Offset(0f, topOffset),
            size = Size(width, height),
            cornerRadius = cornerRadius
        )

        // 2. Draw progress fill
        val fillWidth = (width * progressFraction).coerceIn(0f, width)
        if (fillWidth > 0f) {
            drawRoundRect(
                color = statusColor,
                topLeft = Offset(0f, topOffset),
                size = Size(fillWidth, height),
                cornerRadius = cornerRadius
            )
        }

        // 3. Draw alert threshold marker (e.g. 80%)
        val thresholdFraction = (alertThresholdPercent / 100f).coerceIn(0f, 1f)
        val thresholdX = width * thresholdFraction
        drawLine(
            color = onSurface.copy(alpha = 0.28f),
            start = Offset(thresholdX, topOffset),
            end = Offset(thresholdX, topOffset + height),
            strokeWidth = 2.dp.toPx()
        )

        // 4. Draw month progress pacing tick (if current period)
        if (elapsedFraction != null) {
            val tickX = width * elapsedFraction
            val tickWidth = 3.dp.toPx()
            val tickHeight = height + (tickOverflow.toPx() * 1.5f)
            val tickTop = topOffset - (tickOverflow.toPx() * 0.75f)

            // High contrast needle shadow/border
            drawRoundRect(
                color = onSurface.copy(alpha = 0.35f),
                topLeft = Offset(tickX - (tickWidth / 2f) - 1.dp.toPx(), tickTop - 1.dp.toPx()),
                size = Size(tickWidth + 2.dp.toPx(), tickHeight + 2.dp.toPx()),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )

            // Primary needle
            drawRoundRect(
                color = primaryColor,
                topLeft = Offset(tickX - (tickWidth / 2f), tickTop),
                size = Size(tickWidth, tickHeight),
                cornerRadius = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx())
            )
        }
    }
}

@Composable
internal fun BudgetProgressCard(
    budget: BudgetProgress,
    isHeroOverall: Boolean,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem,
    activePeriodRange: StatsPeriodRange,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val semanticColors = LocalFolioSemanticColors.current
    val isExceeded = budget.usagePercentage >= 100f || budget.remainingAmount.isNegative
    val isNearAlert = !isExceeded && budget.usagePercentage >= budget.alertThresholdPercent.toFloat()

    val statusColor = when {
        isExceeded -> semanticColors.expenseCoral
        isNearAlert -> semanticColors.warningAmber
        else -> semanticColors.incomeEmerald
    }

    val statusContainerColor = when {
        isExceeded -> semanticColors.expenseCoralContainer
        isNearAlert -> semanticColors.warningAmberContainer
        else -> semanticColors.incomeEmeraldContainer
    }

    val onStatusContainerColor = when {
        isExceeded -> semanticColors.onExpenseCoralContainer
        isNearAlert -> semanticColors.onWarningAmberContainer
        else -> semanticColors.onIncomeEmeraldContainer
    }

    val animatedFraction by animateFloatAsState(
        targetValue = (budget.usagePercentage / 100f).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "BudgetUsageFraction"
    )

    val pctBadgeText = remember(budget.usagePercentage) {
        String.format(Locale.US, "%.1f%%", budget.usagePercentage)
    }

    val todayEpochDay = remember { LocalDate.now().toEpochDay() }
    val isCurrentPeriod = todayEpochDay in activePeriodRange.startEpochDay..activePeriodRange.endEpochDay
    val totalDays = remember(activePeriodRange.startEpochDay, activePeriodRange.endEpochDay) {
        (activePeriodRange.endEpochDay - activePeriodRange.startEpochDay + 1).coerceAtLeast(1L)
    }
    val elapsedDays = remember(todayEpochDay, activePeriodRange.startEpochDay, totalDays) {
        (todayEpochDay - activePeriodRange.startEpochDay + 1).coerceIn(1L, totalDays)
    }
    val elapsedMonthFraction = if (isCurrentPeriod) {
        (elapsedDays.toFloat() / totalDays.toFloat()).coerceIn(0f, 1f)
    } else {
        null
    }
    val elapsedPercent: Int? = elapsedMonthFraction?.let { (it * 100f).toInt() }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHeroOverall) {
                MaterialTheme.colorScheme.surfaceContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            }
        ),
        border = BorderStroke(
            width = if (isHeroOverall || isExceeded) 1.5.dp else 1.dp,
            color = when {
                isExceeded -> semanticColors.expenseCoral
                isHeroOverall -> MaterialTheme.colorScheme.primary
                else -> semanticColors.cardStrokeSubtle
            }
        ),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    CategoryIconBadge(
                        iconName = if (isHeroOverall) "savings" else "category",
                        colorHex = budget.colorHex,
                        size = if (isHeroOverall) 42.dp else 36.dp,
                        iconSize = if (isHeroOverall) 22.dp else 18.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isHeroOverall) "Overall Monthly Budget" else budget.categoryName,
                            style = if (isHeroOverall) {
                                MaterialTheme.typography.titleMedium
                            } else {
                                MaterialTheme.typography.titleSmall
                            },
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Alert threshold at ${budget.alertThresholdPercent}%",
                            style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = statusContainerColor
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            if (isExceeded || isNearAlert) {
                                Icon(
                                    imageVector = Icons.Filled.Warning,
                                    contentDescription = null,
                                    tint = onStatusContainerColor,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = when {
                                    isExceeded -> "OVERSPENT ($pctBadgeText)"
                                    isNearAlert -> "NEAR LIMIT ($pctBadgeText)"
                                    else -> "ON TRACK ($pctBadgeText)"
                                },
                                style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                                fontWeight = FontWeight.Bold,
                                color = onStatusContainerColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit Budget",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            BudgetTrackWithPacingTick(
                progressFraction = animatedFraction,
                elapsedFraction = elapsedMonthFraction,
                alertThresholdPercent = budget.alertThresholdPercent,
                statusColor = statusColor,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                isHeroOverall = isHeroOverall
            )

            if (isCurrentPeriod && elapsedPercent != null) {
                val usagePct = budget.usagePercentage
                val paceDiff = usagePct - elapsedPercent.toFloat()
                val isPacingFast = paceDiff > 10f && !isExceeded
                val isPacingWell = paceDiff < -10f && !isExceeded
                val usageInt = usagePct.toInt()

                val paceTextColor = when {
                    isExceeded -> semanticColors.expenseCoral
                    isPacingFast -> semanticColors.warningAmber
                    isPacingWell -> semanticColors.incomeEmerald
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = paceTextColor,
                            modifier = Modifier.size(6.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when {
                                isExceeded -> "Exceeded limit on Day $elapsedDays of $totalDays"
                                isPacingFast -> "Pacing Fast: $usageInt% spent vs $elapsedPercent% month elapsed"
                                isPacingWell -> "Pacing Well: $usageInt% spent vs $elapsedPercent% month elapsed"
                                else -> "On Track: $usageInt% spent vs $elapsedPercent% month elapsed"
                            },
                            style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                            fontWeight = FontWeight.SemiBold,
                            color = paceTextColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Day $elapsedDays of $totalDays",
                        style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Spent",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    MoneyText(
                        money = budget.spentAmount,
                        currencySpec = currencySpec,
                        grouping = grouping,
                        colorMode = MoneyColorMode.EXPENSE,
                        style = if (currencySpec.symbol.length > 2) MaterialTheme.typography.labelMedium else MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Budget Limit",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    MoneyText(
                        money = budget.limitAmount,
                        currencySpec = currencySpec,
                        grouping = grouping,
                        colorMode = MoneyColorMode.NEUTRAL,
                        style = if (currencySpec.symbol.length > 2) MaterialTheme.typography.labelMedium else MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (budget.remainingAmount.isNegative) "Over Budget" else "Remaining",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    MoneyText(
                        money = budget.remainingAmount,
                        currencySpec = currencySpec,
                        grouping = grouping,
                        colorMode = MoneyColorMode.AUTO_SIGN,
                        style = if (currencySpec.symbol.length > 2) MaterialTheme.typography.labelMedium else MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            HorizontalDivider(color = semanticColors.cardStrokeSubtle)

            // Spend Velocity / Burn Rate & Daily Safe Spend
            val today = remember { LocalDate.now() }
            val daysInMonth = remember(today) { today.lengthOfMonth() }
            val currentDay = remember(today) { today.dayOfMonth }
            val remainingDays = remember(daysInMonth, currentDay) { (daysInMonth - currentDay + 1).coerceAtLeast(1) }
            val monthElapsedRatio = remember(daysInMonth, currentDay) { (currentDay.toFloat() / daysInMonth.toFloat()).coerceAtLeast(0.01f) }
            val spendRatio = (budget.usagePercentage / 100f)
            val velocityRatio = spendRatio / monthElapsedRatio

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val (velocityText, velocityColor) = when {
                        isExceeded -> "⚠️ Spending limit exceeded" to semanticColors.expenseCoral
                        velocityRatio > 1.2f -> "⚡ Spending pace is ${String.format(Locale.US, "%.1f", velocityRatio)}× faster than normal" to semanticColors.warningAmber
                        velocityRatio < 0.85f -> "🌿 Spending pace is ${String.format(Locale.US, "%.0f", (1f - velocityRatio) * 100f)}% under expected pace" to semanticColors.incomeEmerald
                        else -> "✓ Spending pace on track" to MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    Text(
                        text = velocityText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = velocityColor
                    )
                }

                if (!isExceeded && budget.remainingAmount.isPositive) {
                    val dailySafeMinor = budget.remainingAmount.amountMinor / remainingDays
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Daily safe spend ($remainingDays days remaining):",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        MoneyText(
                            money = MinorMoney(dailySafeMinor),
                            currencySpec = currencySpec,
                            grouping = grouping,
                            colorMode = MoneyColorMode.INCOME,
                            style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
