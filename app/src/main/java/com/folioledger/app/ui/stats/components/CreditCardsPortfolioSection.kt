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


private val shortDateFormatter = DateTimeFormatter.ofPattern("dd MMM", Locale.US)
private val fullDateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.US)

// -------------------------------------------------------------------------
// SECTION 3: UNIFIED CARDS PORTFOLIO SECTION
// -------------------------------------------------------------------------

@Composable
internal fun UnifiedCardsPortfolioSection(
    cards: List<CreditCardAnalyticsItem>,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem,
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
        }

        sortedCards.forEach { cardItem ->
            CardPortfolioItemCard(
                card = cardItem,
                currencySpec = currencySpec,
                grouping = grouping,
                onOpenMilestoneEditor = { onOpenMilestoneEditor(cardItem) }
            )
        }
    }
}

@Composable
internal fun CardPortfolioItemCard(
    card: CreditCardAnalyticsItem,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem,
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
        modifier = modifier.fillMaxWidth()
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

                // Action Area: Billed Status Pill, Float Pill, or Clear Pill
                if (card.isBilled && card.billedAmount.isPositive) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (card.isUrgent || card.isOverdue) {
                            semanticColors.expenseCoral.copy(alpha = 0.14f)
                        } else {
                            semanticColors.warningAmber.copy(alpha = 0.14f)
                        }
                    ) {
                        Text(
                            text = if (card.isOverdue) "Overdue" else "Billed",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (card.isUrgent || card.isOverdue) {
                                semanticColors.expenseCoral
                            } else {
                                semanticColors.warningAmber
                            },
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
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
