package com.folioledger.app.ui.accounts.sheets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.folioledger.app.ui.accounts.ReconcileBalanceDialogState
import com.folioledger.app.ui.accounts.formatMinorPlainForInput
import com.folioledger.app.ui.accounts.parseInputToMinorOrZero
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.folioledger.app.domain.model.AccountGroup
import com.folioledger.app.domain.model.AccountSummary
import com.folioledger.app.domain.model.CurrencySpec
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.NumberGroupingSystem
import com.folioledger.app.domain.model.SupportedCurrencies
import com.folioledger.app.domain.model.TransactionItem
import com.folioledger.app.domain.model.TransactionType
import com.folioledger.app.ui.accounts.AccountPassbookDayGroup
import com.folioledger.app.ui.accounts.AccountPassbookEntryUiModel
import com.folioledger.app.ui.accounts.AccountPassbookUiModel
import com.folioledger.app.ui.accounts.CreditCardCycleCalculator
import com.folioledger.app.ui.accounts.components.CreditCardPassbookHeroCard
import com.folioledger.app.ui.accounts.components.InvestmentPassbookHeroCard
import com.folioledger.app.ui.accounts.components.PassbookDayGroupHeaderRow
import com.folioledger.app.ui.accounts.components.PassbookTransactionEntryRowCard
import com.folioledger.app.ui.accounts.components.PassbookTwoTierSummaryCard
import com.folioledger.app.ui.accounts.components.formatAccountGroupTitle
import com.folioledger.app.ui.components.CategoryIconBadge
import com.folioledger.app.ui.components.EmptyStateCard
import com.folioledger.app.ui.components.MoneyColorMode
import com.folioledger.app.ui.components.MoneyText
import com.folioledger.app.ui.components.PeriodSelectorHeader
import com.folioledger.app.ui.theme.LocalFolioSemanticColors
import com.folioledger.app.ui.theme.tabularMoneyStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AccountPassbookBottomSheet(
    passbook: AccountPassbookUiModel,
    referenceDateEpochDay: Long,
    defaultCurrencySpec: CurrencySpec,
    grouping: NumberGroupingSystem,
    onPreviousPeriod: () -> Unit,
    onNextPeriod: () -> Unit,
    onResetToCurrentMonth: () -> Unit,
    onEditAccountClick: () -> Unit,
    onAddTransactionClick: () -> Unit,
    onSettleBillClick: () -> Unit,
    onReconcileClick: () -> Unit = {},
    onTogglePin: () -> Unit = {},
    onViewInLedgerClick: () -> Unit,
    onTransactionRowClick: (TransactionItem) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val semanticColors = LocalFolioSemanticColors.current
    val account = passbook.account
    val accountCurrency = remember(account.currencyCode, defaultCurrencySpec) {
        SupportedCurrencies.getByCode(account.currencyCode) ?: defaultCurrencySpec
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
        ) {
            // Top Passbook Account Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    CategoryIconBadge(
                        iconName = account.iconName,
                        colorHex = account.colorHex,
                        size = 46.dp,
                        iconSize = 24.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = account.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = formatAccountGroupTitle(account.groupType),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            if (passbook.parentBankAccount != null) {
                                Text(
                                    text = "Linked to ${passbook.parentBankAccount.name}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            } else if (passbook.linkedDebitCards.isNotEmpty()) {
                                Text(
                                    text = "+${passbook.linkedDebitCards.size} Debit Card(s)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (account.groupType == AccountGroup.CREDIT_CARD) {
                                "Total Debt"
                            } else if (account.groupType == AccountGroup.DEBIT_CARD) {
                                "Bank Live Balance"
                            } else {
                                "Live Balance"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        MoneyText(
                            money = passbook.currentLiveBalance,
                            currencySpec = accountCurrency,
                            grouping = grouping,
                            colorMode = MoneyColorMode.AUTO_SIGN,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = onTogglePin,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PushPin,
                            contentDescription = if (account.isPinned) "Unpin Account" else "Pin Account to Top",
                            tint = if (account.isPinned) semanticColors.warningAmber else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    if (account.groupType == AccountGroup.CREDIT_CARD) {
                        IconButton(
                            onClick = onReconcileClick,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Tune,
                                contentDescription = "Reconcile Balance",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(
                            onClick = onEditAccountClick,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Edit Card",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close Passbook",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Monthly Statement Period Switcher (< Prev | Month Year | Next >)
            PeriodSelectorHeader(
                periodTitle = passbook.periodTitle,
                periodSubtitle = passbook.periodSubtitle,
                onPreviousPeriod = onPreviousPeriod,
                onNextPeriod = onNextPeriod,
                onPeriodTitleClick = onResetToCurrentMonth,
                trailingContent = {
                    AssistChip(
                        onClick = onViewInLedgerClick,
                        label = {
                            Text(
                                text = "View in Ledger",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            )

            HorizontalDivider(color = semanticColors.cardStrokeSubtle)

            LazyColumn(
                contentPadding = PaddingValues(
                    start = 18.dp,
                    end = 18.dp,
                    top = 12.dp,
                    bottom = 36.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (account.groupType == AccountGroup.CREDIT_CARD) {
                    // Unified Credit Card Statement & Debt Hub Card
                    item(contentType = "passbook_hero") {
                        CreditCardPassbookHeroCard(
                            account = account,
                            passbook = passbook,
                            referenceDateEpochDay = referenceDateEpochDay,
                            currencySpec = accountCurrency,
                            grouping = grouping
                        )
                    }

                    // Dual Primary Actions Hub (Settle Bill + Add Expense)
                    item(contentType = "passbook_actions") {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val statementPayable = account.creditCardStatementPayable ?: MinorMoney.ZERO
                            if (statementPayable.isPositive) {
                                Button(
                                    onClick = onSettleBillClick,
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Payments,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Settle Bill",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                FilledTonalButton(
                                    onClick = onSettleBillClick,
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Payments,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Settle Bill",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            FilledTonalButton(
                                onClick = onAddTransactionClick,
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Add Expense",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else if (account.groupType == AccountGroup.INVESTMENT) {
                    // Investment Portfolio Valuation Hero Card
                    item(contentType = "investment_hero") {
                        InvestmentPassbookHeroCard(
                            account = account,
                            passbook = passbook,
                            currencySpec = accountCurrency,
                            grouping = grouping
                        )
                    }

                    // Quick Actions Row for Investment Accounts (Update Value)
                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = onAddTransactionClick,
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                                modifier = Modifier.weight(1.2f)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Add Transaction",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            FilledTonalButton(
                                onClick = onReconcileClick,
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Update Value",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            OutlinedButton(
                                onClick = onEditAccountClick,
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Edit Account",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // 2-Tier Passbook Statement Summary Card
                    item(contentType = "passbook_summary") {
                        PassbookTwoTierSummaryCard(
                            passbook = passbook,
                            currencySpec = accountCurrency,
                            grouping = grouping
                        )
                    }
                } else {
                    // Quick Actions Row for Standard Accounts
                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = onAddTransactionClick,
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                                modifier = Modifier.weight(1.2f)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Add Transaction",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (account.groupType != AccountGroup.DEBIT_CARD) {
                                FilledTonalButton(
                                    onClick = onReconcileClick,
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Tune,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Reconcile",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            OutlinedButton(
                                onClick = onEditAccountClick,
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Edit Account",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // 2-Tier Passbook Statement Summary Card (Opening -> Inflows -> Outflows -> Closing)
                    item(contentType = "passbook_summary") {
                        PassbookTwoTierSummaryCard(
                            passbook = passbook,
                            currencySpec = accountCurrency,
                            grouping = grouping
                        )
                    }
                }

                // Passbook Statement Entries Header
                item(contentType = "passbook_header") {
                    Text(
                        text = "${passbook.periodTitle} Statement · ${passbook.totalPeriodTransactionCount} entries",
                        style = tabularMoneyStyle(MaterialTheme.typography.titleSmall),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (passbook.dayGroups.isEmpty()) {
                    item {
                        EmptyStateCard(
                            title = "No Activity in ${passbook.periodTitle}",
                            subtitle = "Opening Balance (${passbook.periodSubtitle}) carries forward unchanged to Closing Balance.",
                            icon = Icons.AutoMirrored.Filled.ReceiptLong,
                            actionLabel = "+ Add Transaction",
                            onActionClick = onAddTransactionClick
                        )
                    }
                } else {
                    passbook.dayGroups.forEach { dayGroup ->
                        item(key = "pb_day_${dayGroup.epochDay}", contentType = "passbook_day_header") {
                            PassbookDayGroupHeaderRow(
                                dayGroup = dayGroup,
                                currencySpec = accountCurrency,
                                grouping = grouping
                            )
                        }
                        items(
                            items = dayGroup.entries,
                            key = { "pb_entry_${it.transaction.id}_${it.runningBalanceAfter.amountMinor}" },
                            contentType = { "passbook_entry" }
                        ) { entry ->
                            PassbookTransactionEntryRowCard(
                                entry = entry,
                                inspectedAccountId = account.id,
                                currencySpec = accountCurrency,
                                grouping = grouping,
                                onClick = { onTransactionRowClick(entry.transaction) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Deprecated(
    message = "Moved to com.folioledger.app.ui.accounts.dialogs.ReconcileBalanceDialog",
    replaceWith = ReplaceWith(
        "ReconcileBalanceDialog(state, onUpdateTargetBalance, onUpdateNote, onConfirm, onDismiss)",
        "com.folioledger.app.ui.accounts.dialogs.ReconcileBalanceDialog"
    )
)
@Composable
fun ReconcileBalanceDialog(
    state: ReconcileBalanceDialogState,
    onUpdateTargetBalance: (String) -> Unit,
    onUpdateNote: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) = com.folioledger.app.ui.accounts.dialogs.ReconcileBalanceDialog(
    state = state,
    onUpdateTargetBalance = onUpdateTargetBalance,
    onUpdateNote = onUpdateNote,
    onConfirm = onConfirm,
    onDismiss = onDismiss
)
