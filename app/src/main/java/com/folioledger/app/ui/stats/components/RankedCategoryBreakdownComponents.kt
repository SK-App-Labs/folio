package com.folioledger.app.ui.stats.components

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
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.folioledger.app.domain.model.CategorySpendStat
import com.folioledger.app.domain.model.CategoryType
import com.folioledger.app.domain.model.CurrencySpec
import com.folioledger.app.domain.model.NumberGroupingSystem
import com.folioledger.app.ui.components.CategoryIconBadge
import com.folioledger.app.ui.components.MoneyColorMode
import com.folioledger.app.ui.components.MoneyText
import com.folioledger.app.ui.components.parseHexColorSafely
import com.folioledger.app.ui.theme.LocalFolioSemanticColors
import com.folioledger.app.ui.theme.tabularMoneyStyle
import java.util.Locale
import kotlin.math.abs

@Composable
fun RankedCategoryBreakdownGroupedCard(
    stats: List<CategorySpendStat>,
    categoryType: CategoryType,
    highlightedCategoryId: Long?,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem,
    onCategoryClick: (CategorySpendStat) -> Unit,
    modifier: Modifier = Modifier
) {
    val semanticColors = LocalFolioSemanticColors.current
    val haptic = LocalHapticFeedback.current
    var isExpanded by rememberSaveable(categoryType) { mutableStateOf(false) }

    val hasMore = stats.size > 5
    val visibleStats = if (isExpanded || !hasMore) stats else stats.take(5)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            visibleStats.forEachIndexed { index, stat ->
                key(stat.categoryId) {
                    if (index > 0) {
                        HorizontalDivider(
                            color = semanticColors.cardStrokeSubtle,
                            modifier = Modifier.padding(horizontal = 14.dp)
                        )
                    }
                    RankedCategoryStatRowItem(
                        stat = stat,
                        categoryType = categoryType,
                        isHighlighted = stat.categoryId == highlightedCategoryId,
                        currencySpec = currencySpec,
                        grouping = grouping,
                        onClick = { onCategoryClick(stat) }
                    )
                }
            }

            if (hasMore) {
                HorizontalDivider(color = semanticColors.cardStrokeSubtle)
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.45f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            isExpanded = !isExpanded
                        }
                        .padding(vertical = 10.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isExpanded) {
                                "Show Top 5 Only"
                            } else {
                                "Show All ${stats.size} Categories (${stats.size - 5} more)"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RankedCategoryStatRowItem(
    stat: CategorySpendStat,
    categoryType: CategoryType,
    isHighlighted: Boolean,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val semanticColors = LocalFolioSemanticColors.current
    val haptic = LocalHapticFeedback.current
    val catColor = remember(stat.colorHex) { parseHexColorSafely(stat.colorHex) }
    var isSubcategoriesExpanded by rememberSaveable(stat.categoryId) { mutableStateOf(false) }

    val targetFraction = (stat.percentageOfTotal / 100f).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = targetFraction,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "CategoryBarProgress"
    )

    val pctFormatted = remember(stat.percentageOfTotal) {
        String.format(Locale.US, "%.1f%%", stat.percentageOfTotal)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                if (stat.subcategories.isNotEmpty()) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    isSubcategoriesExpanded = !isSubcategoriesExpanded
                } else {
                    onClick()
                }
            }
            .background(if (isHighlighted) MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f) else Color.Transparent)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            CategoryIconBadge(
                iconName = stat.iconName,
                colorHex = stat.colorHex,
                size = 32.dp,
                iconSize = 16.dp
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${stat.categoryName} (${stat.transactionCount})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (stat.changePercentage != null) {
                        val deltaPct = stat.changePercentage
                        val isIncrease = deltaPct > 0f
                        val isZero = abs(deltaPct) < 0.1f
                        val deltaColor = if (isZero) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else if (categoryType == CategoryType.EXPENSE) {
                            if (isIncrease) semanticColors.expenseCoral else semanticColors.incomeEmerald
                        } else {
                            if (isIncrease) semanticColors.incomeEmerald else semanticColors.expenseCoral
                        }
                        val arrow = if (isZero) "•" else if (isIncrease) "▲" else "▼"
                        val sign = if (isIncrease) "+" else ""
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = deltaColor.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "$arrow $sign${String.format(Locale.US, "%.1f%%", deltaPct)}",
                                style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                                color = deltaColor,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    if (stat.subcategories.isNotEmpty()) {
                        Text(
                            text = if (stat.changePercentage != null) "• ${stat.subcategories.size} sub" else "${stat.subcategories.size} subcategories",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = catColor.copy(alpha = 0.16f)
            ) {
                Text(
                    text = pctFormatted,
                    style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                    fontWeight = FontWeight.Bold,
                    color = catColor,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            MoneyText(
                money = stat.netAmount,
                currencySpec = currencySpec,
                grouping = grouping,
                colorMode = if (categoryType == CategoryType.EXPENSE) {
                    MoneyColorMode.EXPENSE
                } else {
                    MoneyColorMode.INCOME
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.width(4.dp))

            val trailingIcon = if (stat.subcategories.isNotEmpty()) {
                if (isSubcategoriesExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown
            } else {
                Icons.AutoMirrored.Filled.KeyboardArrowRight
            }
            Icon(
                imageVector = trailingIcon,
                contentDescription = if (stat.subcategories.isNotEmpty()) "Toggle subcategories" else "Inspect Category",
                tint = if (stat.subcategories.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        LinearProgressIndicator(
            progress = { animatedProgress },
            color = catColor,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            strokeCap = StrokeCap.Round,
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(1.5.dp))
        )

        if (stat.subcategories.isNotEmpty()) {
            AnimatedVisibility(
                visible = isSubcategoriesExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, start = 8.dp, end = 4.dp)
                ) {
                    stat.subcategories.forEach { sub ->
                        val subColor = remember(sub.colorHex) { parseHexColorSafely(sub.colorHex) }
                        val subProgress = (sub.percentageOfTotal / 100f).coerceIn(0f, 1f)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    CategoryIconBadge(
                                        iconName = sub.iconName,
                                        colorHex = sub.colorHex,
                                        size = 22.dp,
                                        iconSize = 12.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${sub.categoryName} (${sub.transactionCount})",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${String.format(Locale.US, "%.1f%%", sub.percentageOfTotal)} of parent",
                                        style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    MoneyText(
                                        money = sub.netAmount,
                                        currencySpec = currencySpec,
                                        grouping = grouping,
                                        colorMode = if (categoryType == CategoryType.EXPENSE) MoneyColorMode.EXPENSE else MoneyColorMode.INCOME,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { subProgress },
                                    color = subColor,
                                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                    strokeCap = StrokeCap.Round,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(2.5.dp)
                                        .clip(RoundedCornerShape(1.25.dp))
                                )
                            }
                        }
                    }

                    // 1-Tap Drilldown Link for the entire category
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onClick)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 12.dp)
                        ) {
                            Text(
                                text = "View All ${stat.categoryName} Transactions (${stat.transactionCount})",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
