package com.folioledger.app.ui.accounts.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.folioledger.app.domain.model.AccountSummary
import com.folioledger.app.domain.model.CurrencySpec
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.NumberGroupingSystem
import com.folioledger.app.domain.util.CreditCardCycleCalculator
import com.folioledger.app.ui.accounts.AccountPassbookUiModel
import com.folioledger.app.ui.components.MoneyColorMode
import com.folioledger.app.ui.components.MoneyText
import com.folioledger.app.ui.theme.tabularMoneyStyle
import com.folioledger.app.ui.theme.LocalFolioSemanticColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

data class CreditPassbookTierInfo(
    val tierText: String,
    val tierBg: Color,
    val tierFg: Color,
    val tierColor: Color
)

@Composable
fun CreditCardPassbookHeroCard(
    account: AccountSummary,
    passbook: AccountPassbookUiModel,
    referenceDateEpochDay: Long,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem
) {
    val semanticColors = LocalFolioSemanticColors.current
    val statementDay = (account.settlementDayOfMonth ?: 18).coerceIn(1, 31)
    val dueDay = (account.paymentDueDayOfMonth ?: 5).coerceIn(1, 31)

    val cycleWindow = remember(referenceDateEpochDay, statementDay, dueDay) {
        CreditCardCycleCalculator.computeCycleWindow(
            referenceDateEpochDay = referenceDateEpochDay,
            settlementDayOfMonth = statementDay,
            paymentDueDayOfMonth = dueDay
        )
    }

    val totalOutstandingDebt = account.currentBalance.abs()
    val statementPayable = account.creditCardStatementPayable ?: MinorMoney.ZERO
    val unbilledSpend = account.creditCardUnbilledAmount
        ?: if (totalOutstandingDebt > statementPayable) {
            totalOutstandingDebt - statementPayable
        } else {
            MinorMoney.ZERO
        }
    val creditLimit = account.creditLimit ?: MinorMoney.ZERO
    val availableCredit = if (creditLimit > totalOutstandingDebt) {
        creditLimit - totalOutstandingDebt
    } else {
        MinorMoney.ZERO
    }

    val utilizationPercent = if (creditLimit.isPositive) {
        (totalOutstandingDebt.amountMinor.toFloat() / creditLimit.amountMinor.toFloat() * 100f).coerceIn(0f, 100f)
    } else {
        0f
    }

    val tierInfo = remember(utilizationPercent) {
        when {
            utilizationPercent > 50f -> CreditPassbookTierInfo(
                tierText = "Risky",
                tierBg = semanticColors.expenseCoralContainer,
                tierFg = semanticColors.onExpenseCoralContainer,
                tierColor = semanticColors.expenseCoral
            )
            utilizationPercent > 30f -> CreditPassbookTierInfo(
                tierText = "High",
                tierBg = semanticColors.warningAmberContainer,
                tierFg = semanticColors.onWarningAmberContainer,
                tierColor = semanticColors.warningAmber
            )
            utilizationPercent > 10f -> CreditPassbookTierInfo(
                tierText = "Good",
                tierBg = semanticColors.incomeEmeraldContainer,
                tierFg = semanticColors.onIncomeEmeraldContainer,
                tierColor = Color(0xFF00BFA5)
            )
            else -> CreditPassbookTierInfo(
                tierText = "Optimal",
                tierBg = semanticColors.incomeEmeraldContainer,
                tierFg = semanticColors.onIncomeEmeraldContainer,
                tierColor = semanticColors.incomeEmerald
            )
        }
    }

    val today = remember { LocalDate.now() }
    val daysUntilDue = remember(today, cycleWindow.lastStatementDueDate) {
        ChronoUnit.DAYS.between(today, cycleWindow.lastStatementDueDate)
    }
    val dueBadgeText = when {
        !statementPayable.isPositive -> "Paid in Full ✓"
        daysUntilDue < 0L -> "Overdue by ${-daysUntilDue}d (${cycleWindow.lastStatementDueShortText})"
        daysUntilDue == 0L -> "Due Today (${cycleWindow.lastStatementDueShortText})"
        daysUntilDue == 1L -> "Due Tomorrow (${cycleWindow.lastStatementDueShortText})"
        else -> "Due in ${daysUntilDue} days (${cycleWindow.lastStatementDueShortText})"
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Outstanding Debt & Utilization Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Total Outstanding Debt",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    MoneyText(
                        money = account.currentBalance,
                        currencySpec = currencySpec,
                        grouping = grouping,
                        colorMode = MoneyColorMode.AUTO_SIGN,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = tierInfo.tierBg
                ) {
                    Text(
                        text = "${String.format(Locale.US, "%.1f", utilizationPercent)}% ${tierInfo.tierText}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = tierInfo.tierFg,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Credit Limit Progress Bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LinearProgressIndicator(
                    progress = { (utilizationPercent / 100f).coerceIn(0f, 1f) },
                    color = tierInfo.tierColor,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    strokeCap = StrokeCap.Round,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                )

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Available: ",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        MoneyText(
                            money = availableCredit,
                            currencySpec = currencySpec,
                            grouping = grouping,
                            colorMode = MoneyColorMode.INCOME,
                            style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Limit: ",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        MoneyText(
                            money = creditLimit,
                            currencySpec = currencySpec,
                            grouping = grouping,
                            colorMode = MoneyColorMode.NEUTRAL,
                            style = tabularMoneyStyle(MaterialTheme.typography.labelSmall)
                        )
                    }
                }
            }

            HorizontalDivider(color = semanticColors.cardStrokeSubtle)

            // Two-Column Statement Split (Billed vs Unbilled)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                // Column 1: Billed Statement Due Now
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "BILLED STATEMENT",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    MoneyText(
                        money = statementPayable,
                        currencySpec = currencySpec,
                        grouping = grouping,
                        colorMode = if (statementPayable.isPositive) {
                            MoneyColorMode.EXPENSE
                        } else {
                            MoneyColorMode.INCOME
                        },
                        style = if (currencySpec.symbol.length > 2) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when {
                            !statementPayable.isPositive -> semanticColors.incomeEmeraldContainer
                            daysUntilDue <= 3 -> semanticColors.expenseCoralContainer
                            else -> semanticColors.warningAmberContainer
                        }
                    ) {
                        Text(
                            text = dueBadgeText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                !statementPayable.isPositive -> semanticColors.onIncomeEmeraldContainer
                                daysUntilDue <= 3 -> semanticColors.onExpenseCoralContainer
                                else -> semanticColors.onWarningAmberContainer
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                VerticalDivider(
                    color = semanticColors.cardStrokeSubtle,
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )

                // Column 2: Active Unbilled Spend
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "ACTIVE UNBILLED",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    MoneyText(
                        money = unbilledSpend,
                        currencySpec = currencySpec,
                        grouping = grouping,
                        colorMode = MoneyColorMode.NEUTRAL,
                        style = if (currencySpec.symbol.length > 2) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Closes ${cycleWindow.activeCycleEnd.format(DateTimeFormatter.ofPattern("dd MMM", Locale.US))}",
                        style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            HorizontalDivider(color = semanticColors.cardStrokeSubtle)

            // Activity Ribbon (Payments vs Spends for this cycle)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Payments: ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    MoneyText(
                        money = passbook.periodInflows,
                        currencySpec = currencySpec,
                        grouping = grouping,
                        colorMode = MoneyColorMode.INCOME,
                        style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Spends: ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    MoneyText(
                        money = passbook.periodOutflows,
                        currencySpec = currencySpec,
                        grouping = grouping,
                        colorMode = MoneyColorMode.EXPENSE,
                        style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "${passbook.totalPeriodTransactionCount} entries",
                    style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun InvestmentPassbookHeroCard(
    account: AccountSummary,
    passbook: AccountPassbookUiModel,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem
) {
    val semanticColors = LocalFolioSemanticColors.current
    val currentValuation = passbook.currentLiveBalance
    val investedPrincipal = passbook.investedPrincipal ?: currentValuation
    val unrealizedPnl = passbook.unrealizedPnl ?: MinorMoney.ZERO
    val returnPct = passbook.returnPercentage ?: 0f

    val isPositive = unrealizedPnl.amountMinor >= 0L
    val pnlContainerColor = if (isPositive) semanticColors.incomeEmeraldContainer else semanticColors.expenseCoralContainer
    val onPnlContainerColor = if (isPositive) semanticColors.onIncomeEmeraldContainer else semanticColors.onExpenseCoralContainer

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "CURRENT PORTFOLIO VALUE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (investedPrincipal.isPositive) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = pnlContainerColor
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = if (isPositive) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                                contentDescription = null,
                                tint = onPnlContainerColor,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = String.format(Locale.US, "%s%.1f%%", if (isPositive) "+" else "", returnPct),
                                style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                                fontWeight = FontWeight.Bold,
                                color = onPnlContainerColor
                            )
                        }
                    }
                }
            }

            MoneyText(
                money = currentValuation,
                currencySpec = currencySpec,
                grouping = grouping,
                colorMode = MoneyColorMode.NEUTRAL,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold
            )

            HorizontalDivider(color = semanticColors.cardStrokeSubtle)

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "TOTAL INVESTED",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    MoneyText(
                        money = investedPrincipal,
                        currencySpec = currencySpec,
                        grouping = grouping,
                        colorMode = MoneyColorMode.NEUTRAL,
                        style = tabularMoneyStyle(MaterialTheme.typography.titleMedium),
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isPositive) "UNREALIZED GAIN" else "UNREALIZED LOSS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    MoneyText(
                        money = unrealizedPnl,
                        currencySpec = currencySpec,
                        grouping = grouping,
                        colorMode = if (isPositive) MoneyColorMode.INCOME else MoneyColorMode.EXPENSE,
                        style = tabularMoneyStyle(MaterialTheme.typography.titleMedium),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
