package com.folioledger.app.ui.stats.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.folioledger.app.R
import com.folioledger.app.domain.model.AccountSavingsGoal
import com.folioledger.app.domain.model.CurrencySpec
import com.folioledger.app.domain.model.MonthlySavingsProgress
import com.folioledger.app.domain.model.NumberGroupingSystem
import com.folioledger.app.domain.util.MoneyFormatterAndCalculator
import com.folioledger.app.ui.components.CategoryIconBadge
import com.folioledger.app.ui.components.MoneyColorMode
import com.folioledger.app.ui.components.MoneyText
import com.folioledger.app.ui.theme.LocalFolioSemanticColors
import com.folioledger.app.ui.theme.tabularMoneyStyle
import java.util.Locale

@Composable
internal fun MonthlySavingsTargetHeroCard(
    savingsProgress: MonthlySavingsProgress,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem,
    activePeriodTitle: String,
    onConfigureTarget: () -> Unit,
    modifier: Modifier = Modifier
) {
    val semanticColors = LocalFolioSemanticColors.current
    val isConfigured = savingsProgress.isConfigured
    val isAchieved = savingsProgress.isAchieved

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onConfigureTarget)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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
                        iconName = "savings",
                        colorHex = if (isAchieved) "#10B981" else "#3B82F6",
                        size = 38.dp,
                        iconSize = 20.dp
                    )
                    Column {
                        Text(
                            text = "Monthly Wealth Savings",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Double-Entry Savings · $activePeriodTitle",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (isConfigured) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isAchieved) semanticColors.incomeEmeraldContainer else MaterialTheme.colorScheme.primaryContainer,
                        border = BorderStroke(
                            0.5.dp,
                            if (isAchieved) semanticColors.incomeEmerald else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = String.format(Locale.US, "%.1f%%", savingsProgress.progressPercentage),
                            style = tabularMoneyStyle(MaterialTheme.typography.labelMedium),
                            fontWeight = FontWeight.Bold,
                            color = if (isAchieved) semanticColors.onIncomeEmeraldContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = onConfigureTarget,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.budget_btn_set_target),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            if (!isConfigured) {
                Text(
                    text = "Track net deposits to Savings & Investments (\"Pay Yourself First\") independently from expense budgeting.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Net Transferred",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        MoneyText(
                            money = savingsProgress.savedAmount,
                            currencySpec = currencySpec,
                            grouping = grouping,
                            colorMode = MoneyColorMode.INCOME,
                            style = tabularMoneyStyle(MaterialTheme.typography.titleSmall),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Target Goal",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        MoneyText(
                            money = savingsProgress.targetAmount,
                            currencySpec = currencySpec,
                            grouping = grouping,
                            style = tabularMoneyStyle(MaterialTheme.typography.titleSmall),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Unallocated Surplus",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        MoneyText(
                            money = savingsProgress.unallocatedSurplus,
                            currencySpec = currencySpec,
                            grouping = grouping,
                            colorMode = if (savingsProgress.unallocatedSurplus.amountMinor >= 0L) MoneyColorMode.INCOME else MoneyColorMode.EXPENSE,
                            style = tabularMoneyStyle(MaterialTheme.typography.titleSmall),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                LinearProgressIndicator(
                    progress = { (savingsProgress.progressPercentage / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (isAchieved) semanticColors.incomeEmerald else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )

                Text(
                    text = if (isAchieved) {
                        "🎉 Monthly savings target achieved! Unallocated surplus is safe for extra investments or rollover."
                    } else {
                        val remainingFormatted = MoneyFormatterAndCalculator.formatMinorMoney(savingsProgress.remainingAmount, currencySpec)
                        "$remainingFormatted remaining to reach monthly target."
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isAchieved) semanticColors.incomeEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
internal fun AccountSavingsGoalCard(
    goal: AccountSavingsGoal,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val semanticColors = LocalFolioSemanticColors.current
    val isAchieved = goal.isAchieved

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
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
                        iconName = goal.iconName,
                        colorHex = goal.colorHex,
                        size = 36.dp,
                        iconSize = 18.dp
                    )
                    Column {
                        Text(
                            text = goal.goalTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = goal.accountName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isAchieved) semanticColors.incomeEmeraldContainer else MaterialTheme.colorScheme.primaryContainer,
                    border = BorderStroke(
                        0.5.dp,
                        if (isAchieved) semanticColors.incomeEmerald else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = if (isAchieved) "ACHIEVED" else String.format(Locale.US, "%.0f%%", goal.progressPercentage),
                        style = tabularMoneyStyle(MaterialTheme.typography.labelMedium),
                        fontWeight = FontWeight.Bold,
                        color = if (isAchieved) semanticColors.onIncomeEmeraldContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    MoneyText(
                        money = goal.currentBalance,
                        currencySpec = currencySpec,
                        grouping = grouping,
                        style = tabularMoneyStyle(MaterialTheme.typography.titleMedium),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "of",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    MoneyText(
                        money = goal.targetAmount,
                        currencySpec = currencySpec,
                        grouping = grouping,
                        style = tabularMoneyStyle(MaterialTheme.typography.bodyMedium),
                        colorMode = MoneyColorMode.NEUTRAL
                    )
                }

                if (!isAchieved) {
                    val remainingFormatted = MoneyFormatterAndCalculator.formatMinorMoney(goal.remainingAmount, currencySpec)
                    Text(
                        text = "$remainingFormatted left",
                        style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            LinearProgressIndicator(
                progress = { (goal.progressPercentage / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (isAchieved) semanticColors.incomeEmerald else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
            )

            Text(
                text = if (isAchieved) {
                    "🎉 Live account balance meets or exceeds goal target!"
                } else {
                    "Live ledger verified · Zero virtual envelope drift"
                },
                style = MaterialTheme.typography.labelSmall,
                color = if (isAchieved) semanticColors.incomeEmerald else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
