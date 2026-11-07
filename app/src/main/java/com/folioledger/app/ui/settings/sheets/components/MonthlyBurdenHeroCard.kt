package com.folioledger.app.ui.settings.sheets.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.folioledger.app.domain.model.TransactionType
import com.folioledger.app.ui.components.MoneyColorMode
import com.folioledger.app.ui.components.MoneyText
import com.folioledger.app.ui.settings.SettingsUiState
import com.folioledger.app.ui.theme.LocalFolioSemanticColors
import com.folioledger.app.ui.theme.tabularMoneyStyle

@Composable
fun MonthlyBurdenHeroCard(
    uiState: SettingsUiState,
    modifier: Modifier = Modifier
) {
    val semanticColors = LocalFolioSemanticColors.current

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "TOTAL MONTHLY FIXED COMMITMENT",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                MoneyText(
                    money = uiState.totalMonthlyFixedBurden,
                    currencySpec = uiState.currencySpec,
                    grouping = uiState.userPreferences.numberGrouping,
                    style = tabularMoneyStyle(MaterialTheme.typography.headlineMedium),
                    colorMode = MoneyColorMode.NEUTRAL
                )
                Text(
                    text = "/ month",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            HorizontalDivider(
                color = semanticColors.cardStrokeSubtle,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Subscriptions Breakdown
                val activeSubscriptionsCount = uiState.recurringRules.count { it.isActive && it.type == TransactionType.EXPENSE }
                Column {
                    Text(
                        text = "Subscriptions ($activeSubscriptionsCount active)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    MoneyText(
                        money = uiState.activeMonthlyRecurringBurden,
                        currencySpec = uiState.currencySpec,
                        grouping = uiState.userPreferences.numberGrouping,
                        style = tabularMoneyStyle(MaterialTheme.typography.bodyMedium),
                        colorMode = MoneyColorMode.NEUTRAL
                    )
                }

                // Auto-Savings / Transfers Breakdown (if any active transfers exist)
                val activeTransfersCount = uiState.recurringRules.count { it.isActive && it.type == TransactionType.TRANSFER }
                if (activeTransfersCount > 0) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Auto-Save ($activeTransfersCount active)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        MoneyText(
                            money = uiState.activeMonthlyTransfersBurden,
                            currencySpec = uiState.currencySpec,
                            grouping = uiState.userPreferences.numberGrouping,
                            style = tabularMoneyStyle(MaterialTheme.typography.bodyMedium),
                            colorMode = MoneyColorMode.NEUTRAL
                        )
                    }
                }

                // EMIs Breakdown
                val activePlansCount = uiState.installmentPlans.count { it.remainingInstallmentsCount > 0 }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Credit Card EMIs ($activePlansCount active)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    MoneyText(
                        money = uiState.activeMonthlyInstallmentsBurden,
                        currencySpec = uiState.currencySpec,
                        grouping = uiState.userPreferences.numberGrouping,
                        style = tabularMoneyStyle(MaterialTheme.typography.bodyMedium),
                        colorMode = MoneyColorMode.NEUTRAL
                    )
                }
            }
        }
    }
}
