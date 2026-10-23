package com.folioledger.app.ui.accounts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.folioledger.app.domain.model.AccountGroup
import com.folioledger.app.domain.model.AccountSummary
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.TransactionItem
import com.folioledger.app.ui.accounts.components.AccountGroupSectionCard
import com.folioledger.app.ui.accounts.sheets.AccountEditorBottomSheet
import com.folioledger.app.ui.accounts.sheets.AccountPassbookBottomSheet
import com.folioledger.app.ui.accounts.sheets.CreditCardSettlementBottomSheet
import com.folioledger.app.ui.components.EmptyStateCard
import com.folioledger.app.ui.components.ThreeColumnSummaryBanner

@Composable
fun AccountsScreen(
    viewModel: AccountsViewModel,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onAccountClick: ((AccountSummary) -> Unit)? = null,
    onViewAccountInLedger: ((AccountSummary) -> Unit)? = null,
    onAddTransactionForAccount: (AccountSummary) -> Unit = {},
    onEditTransaction: (TransactionItem) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        val message = uiState.snackbarMessage
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearSnackbarMessage()
        }
    }

    AccountsScreenContent(
        uiState = uiState,
        contentPadding = contentPadding,
        snackbarHostState = snackbarHostState,
        onToggleGroupCollapsed = viewModel::toggleGroupCollapsed,
        onToggleShowArchived = viewModel::toggleShowArchivedAccounts,
        onOpenCreateAccount = viewModel::openCreateAccountDialog,
        onOpenEditAccount = viewModel::openEditAccountDialog,
        onOpenSettlementSheet = viewModel::openSettlementSheet,
        onAccountClick = { account ->
            viewModel.selectAccountForInspection(account.id)
            onAccountClick?.invoke(account)
        }
    )

    val passbook = uiState.accountPassbook
    if (passbook != null) {
        AccountPassbookBottomSheet(
            passbook = passbook,
            referenceDateEpochDay = uiState.referenceDateEpochDay,
            defaultCurrencySpec = uiState.currencySpec,
            grouping = uiState.numberGrouping,
            onPreviousPeriod = { viewModel.stepPassbookMonth(-1) },
            onNextPeriod = { viewModel.stepPassbookMonth(1) },
            onResetToCurrentMonth = viewModel::resetPassbookToCurrentMonth,
            onEditAccountClick = {
                viewModel.dismissInspectedAccountSheet()
                viewModel.openEditAccountDialog(passbook.account)
            },
            onAddTransactionClick = {
                viewModel.dismissInspectedAccountSheet()
                onAddTransactionForAccount(passbook.account)
            },
            onSettleBillClick = {
                viewModel.dismissInspectedAccountSheet()
                viewModel.openSettlementSheet(passbook.account)
            },
            onViewInLedgerClick = {
                viewModel.dismissInspectedAccountSheet()
                onViewAccountInLedger?.invoke(passbook.account)
            },
            onTransactionRowClick = { tx ->
                viewModel.dismissInspectedAccountSheet()
                onEditTransaction(tx)
            },
            onDismiss = viewModel::dismissInspectedAccountSheet
        )
    }

    val editorState = uiState.editorDialogState
    if (editorState != null) {
        AccountEditorBottomSheet(
            state = editorState,
            referenceDateEpochDay = uiState.referenceDateEpochDay,
            parentBankCandidates = uiState.parentBankCandidates,
            isSaving = uiState.isSaving,
            onUpdateState = viewModel::updateEditorDialogState,
            onSave = viewModel::saveAccountFromDialog,
            onToggleArchive = {
                if (editorState.isEditing && editorState.accountId > 0L) {
                    viewModel.setAccountArchived(editorState.accountId, !editorState.isArchived)
                }
            },
            onDismiss = viewModel::dismissAccountEditorDialog
        )
    }

    val settlementState = uiState.settlementSheetState
    if (settlementState != null) {
        CreditCardSettlementBottomSheet(
            state = settlementState,
            assetAccounts = uiState.assetAccountsForSettlement,
            grouping = uiState.numberGrouping,
            isSaving = uiState.isSaving,
            onSelectPreset = viewModel::selectSettlementPreset,
            onUpdateState = viewModel::updateSettlementSheetState,
            onConfirmSettlement = viewModel::submitCreditCardSettlement,
            onDismiss = viewModel::dismissSettlementSheet
        )
    }
}

@Composable
fun AccountsScreenContent(
    uiState: AccountsUiState,
    contentPadding: PaddingValues,
    snackbarHostState: SnackbarHostState,
    onToggleGroupCollapsed: (AccountGroup) -> Unit,
    onToggleShowArchived: () -> Unit,
    onOpenCreateAccount: (AccountGroup) -> Unit,
    onOpenEditAccount: (AccountSummary) -> Unit,
    onOpenSettlementSheet: (AccountSummary) -> Unit,
    onAccountClick: (AccountSummary) -> Unit,
    modifier: Modifier = Modifier
) {
    val allById = remember(uiState.allAccounts) {
        uiState.allAccounts.associateBy { it.id }
    }
    val orderedGroups = remember(uiState.netWorthOverview.accountsByGroup) {
        AccountGroup.entries.filter { group ->
            !uiState.netWorthOverview.accountsByGroup[group].isNullOrEmpty()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { scaffoldPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = contentPadding.calculateTopPadding() + scaffoldPadding.calculateTopPadding() + 12.dp,
                bottom = contentPadding.calculateBottomPadding() + scaffoldPadding.calculateBottomPadding() + 96.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Accounts & Net Worth",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Tap to open passbook • Long press to edit",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    FilledTonalButton(
                        onClick = { onOpenCreateAccount(AccountGroup.BANK_ACCOUNT) },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add Account",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Add Account",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            item {
                ThreeColumnSummaryBanner(
                    netWorthOverview = uiState.netWorthOverview,
                    currencySpec = uiState.currencySpec,
                    grouping = uiState.numberGrouping
                )
            }

            if (orderedGroups.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = "No Active Accounts Yet",
                        subtitle = "Add your Bank Accounts, Cash Wallet, Credit Cards, and linked Debit Cards to track your live Net Worth.",
                        icon = Icons.Filled.AccountBalanceWallet,
                        actionLabel = "Create First Account",
                        onActionClick = { onOpenCreateAccount(AccountGroup.BANK_ACCOUNT) }
                    )
                }
            } else {
                orderedGroups.forEach { group ->
                    val groupAccounts = uiState.netWorthOverview.accountsByGroup[group].orEmpty()
                    val isCollapsed = group in uiState.collapsedGroups
                    val groupTotalMinor = groupAccounts.fold(0L) { sum, acc ->
                        if (acc.groupType == AccountGroup.DEBIT_CARD) {
                            sum + (acc.debitCardPeriodSpend?.amountMinor ?: 0L)
                        } else {
                            sum + acc.currentBalance.amountMinor
                        }
                    }

                    item(key = "group_header_${group.name}") {
                        AccountGroupSectionCard(
                            group = group,
                            accounts = groupAccounts,
                            allAccountsById = allById,
                            referenceDateEpochDay = uiState.referenceDateEpochDay,
                            groupSubtotal = MinorMoney(groupTotalMinor),
                            currencySpec = uiState.currencySpec,
                            grouping = uiState.numberGrouping,
                            isCollapsed = isCollapsed,
                            onToggleCollapse = { onToggleGroupCollapsed(group) },
                            onAddAccountInGroup = { onOpenCreateAccount(group) },
                            onEditAccount = onOpenEditAccount,
                            onSettleCreditCard = onOpenSettlementSheet,
                            onAccountClick = onAccountClick
                        )
                    }
                }
            }

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    Text(
                        text = "Include Archived Accounts",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Switch(
                        checked = uiState.showArchivedAccounts,
                        onCheckedChange = { onToggleShowArchived() }
                    )
                }
            }
        }
    }
}
