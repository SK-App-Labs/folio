package com.folioledger.app.ui.stats.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.folioledger.app.R
import com.folioledger.app.domain.model.AccountShortfallAlert
import com.folioledger.app.domain.model.CurrencySpec
import com.folioledger.app.domain.model.NumberGroupingSystem
import com.folioledger.app.domain.model.SafeToSpendState
import com.folioledger.app.domain.model.SafeToSpendStatus
import com.folioledger.app.domain.util.MoneyFormatterAndCalculator
import com.folioledger.app.ui.components.MoneyColorMode
import com.folioledger.app.ui.components.MoneyText
import com.folioledger.app.ui.theme.LocalFolioSemanticColors

@Composable
fun SafeToSpendCard(
    safeToSpend: SafeToSpendState,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem,
    modifier: Modifier = Modifier
) {
    val semanticColors = LocalFolioSemanticColors.current

    val (statusText, statusDescription) = when (safeToSpend.status) {
        SafeToSpendStatus.SAFE -> Pair(
            "🛡️ Safe Discretionary Cash",
            if (safeToSpend.totalCreditCardDebt.isPositive && safeToSpend.upcoming14DayCommitments.isPositive) {
                "Unencumbered cash you can safely spend after accounting for card debt and bills due in the next 14 days."
            } else if (safeToSpend.totalCreditCardDebt.isPositive) {
                "Unencumbered cash you can safely spend after covering all outstanding credit card balances."
            } else if (safeToSpend.upcoming14DayCommitments.isPositive) {
                "Unencumbered cash you can safely spend after covering upcoming bills due in the next 14 days."
            } else {
                "Unencumbered cash you can safely spend with zero outstanding card debt or upcoming commitments."
            }
        )
        SafeToSpendStatus.CARD_DEBT_EXCEEDS_CASH -> Pair(
            "⚠️ Card Debt Exceeds Cash",
            "Your liquid bank balances are insufficient to cover your total credit card debt. You are carrying an unbacked credit float."
        )
        SafeToSpendStatus.BILLS_EXCEED_CASH -> Pair(
            "⚠️ Bills Exceed Cash",
            "Your liquid bank balances are insufficient to cover upcoming bills due in the next 14 days."
        )
        SafeToSpendStatus.BILLS_EXCEED_REMAINING_CASH -> Pair(
            "⚠️ Bills Exceed Remaining Cash",
            "Your liquid cash covers credit card debt, but is insufficient once bills due in the next 14 days are included."
        )
        SafeToSpendStatus.COMBINED_OBLIGATIONS_EXCEED_CASH -> Pair(
            "⚠️ Debt & Bills Exceed Cash",
            "Your liquid bank balances are insufficient to cover combined card debt and bills due in the next 14 days."
        )
        SafeToSpendStatus.ZERO_RESERVES -> Pair(
            "● Zero Liquid Cash",
            "No liquid cash reserves available to spend."
        )
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            // Header Row: Shield Icon, Title, Status Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (safeToSpend.isPositive) {
                            semanticColors.incomeEmeraldContainer
                        } else {
                            semanticColors.warningAmberContainer
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (safeToSpend.isPositive) Icons.Default.Shield else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (safeToSpend.isPositive) {
                                    semanticColors.onIncomeEmeraldContainer
                                } else {
                                    semanticColors.onWarningAmberContainer
                                },
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Text(
                        text = stringResource(R.string.safe_to_spend_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (safeToSpend.isPositive) {
                        semanticColors.incomeEmeraldContainer
                    } else {
                        semanticColors.warningAmberContainer
                    }
                ) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (safeToSpend.isPositive) {
                            semanticColors.onIncomeEmeraldContainer
                        } else {
                            semanticColors.onWarningAmberContainer
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Big Headline Money
            MoneyText(
                money = safeToSpend.trueSafeToSpend,
                currencySpec = currencySpec,
                grouping = grouping,
                colorMode = if (safeToSpend.isPositive) MoneyColorMode.INCOME else MoneyColorMode.EXPENSE,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black
            )

            Text(
                text = statusDescription,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider(color = semanticColors.cardStrokeSubtle)

            // Equation Breakdown Row: Liquid Cash - CC Debt - 14d Bills
            val equationMetricStyle = if (currencySpec.symbol.length > 2) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = stringResource(R.string.safe_to_spend_formula_liquid),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.outline
                    )
                    MoneyText(
                        money = safeToSpend.totalLiquidBankAndCash,
                        currencySpec = currencySpec,
                        grouping = grouping,
                        colorMode = MoneyColorMode.NEUTRAL,
                        style = equationMetricStyle,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "−",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.outline
                )

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = stringResource(R.string.safe_to_spend_formula_cards),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.outline
                    )
                    MoneyText(
                        money = safeToSpend.totalCreditCardDebt,
                        currencySpec = currencySpec,
                        grouping = grouping,
                        colorMode = MoneyColorMode.EXPENSE,
                        style = equationMetricStyle,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "−",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.outline
                )

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = stringResource(R.string.safe_to_spend_formula_commitments),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.outline
                    )
                    MoneyText(
                        money = safeToSpend.upcoming14DayCommitments,
                        currencySpec = currencySpec,
                        grouping = grouping,
                        colorMode = MoneyColorMode.EXPENSE,
                        style = equationMetricStyle,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ShortfallAlertCard(
    alerts: List<AccountShortfallAlert>,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem,
    modifier: Modifier = Modifier
) {
    val semanticColors = LocalFolioSemanticColors.current

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = semanticColors.warningAmberContainer),
        border = BorderStroke(1.dp, semanticColors.warningAmber.copy(alpha = 0.5f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = semanticColors.onWarningAmberContainer,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = stringResource(R.string.shortfall_alert_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = semanticColors.onWarningAmberContainer
                )
            }

            alerts.forEach { alert ->
                val balFormatted = remember(alert.currentBalance, currencySpec, grouping) {
                    MoneyFormatterAndCalculator.formatMinorMoney(alert.currentBalance, currencySpec)
                }
                val debFormatted = remember(alert.upcomingDebits, currencySpec, grouping) {
                    MoneyFormatterAndCalculator.formatMinorMoney(alert.upcomingDebits, currencySpec)
                }
                val shortFormatted = remember(alert.shortfallAmount, currencySpec, grouping) {
                    MoneyFormatterAndCalculator.formatMinorMoney(alert.shortfallAmount, currencySpec)
                }

                Text(
                    text = "⚠️ ${alert.accountName}: Current balance ($balFormatted) may not cover upcoming commitments ($debFormatted) due in ${alert.daysUntilShortfall} ${if (alert.daysUntilShortfall == 1L) "day" else "days"} (Shortfall: $shortFormatted).",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = semanticColors.onWarningAmberContainer
                )
            }
        }
    }
}
