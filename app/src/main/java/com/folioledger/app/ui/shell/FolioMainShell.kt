// File: app/src/main/java/com/folioledger/app/ui/shell/FolioMainShell.kt
package com.folioledger.app.ui.shell

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.folioledger.app.domain.util.MoneyFormatterAndCalculator
import com.folioledger.app.ui.accounts.AccountsScreen
import com.folioledger.app.ui.accounts.AccountsViewModel
import com.folioledger.app.ui.entry.TransactionEntrySheet
import com.folioledger.app.ui.entry.TransactionEntryViewModel
import com.folioledger.app.ui.ledger.LedgerHomeScreen
import com.folioledger.app.ui.ledger.LedgerHomeUiState
import com.folioledger.app.ui.ledger.LedgerHomeViewModel
import com.folioledger.app.ui.theme.FolioTheme

enum class FolioMainTab(
    val label: String,
    val icon: ImageVector
) {
    TRANSACTIONS("Trans.", Icons.Default.ReceiptLong),
    STATS("Stats", Icons.Default.PieChart),
    ACCOUNTS("Accounts", Icons.Default.AccountBalanceWallet),
    SETTINGS("Settings", Icons.Default.Settings)
}

@Composable
fun FolioMainShell(
    viewModelFactory: ViewModelProvider.Factory
) {
    val ledgerViewModel: LedgerHomeViewModel = viewModel(factory = viewModelFactory)
    val accountsViewModel: AccountsViewModel = viewModel(factory = viewModelFactory)
    val entryViewModel: TransactionEntryViewModel = viewModel(factory = viewModelFactory)

    val ledgerUiState by ledgerViewModel.uiState.collectAsStateWithLifecycle()

    var selectedTab by rememberSaveable { mutableStateOf(FolioMainTab.TRANSACTIONS) }
    var isEntrySheetOpen by rememberSaveable { mutableStateOf(false) }

    FolioTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                NavigationBar {
                    FolioMainTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.label
                                )
                            },
                            label = { Text(text = tab.label) }
                        )
                    }
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        entryViewModel.resetForNewTransaction(
                            initialEpochDay = ledgerUiState.selectedCalendarDayEpochDay
                        )
                        isEntrySheetOpen = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Transaction"
                    )
                }
            }
        ) { innerPadding ->
            when (selectedTab) {
                FolioMainTab.TRANSACTIONS -> {
                    LedgerHomeScreen(
                        viewModel = ledgerViewModel,
                        contentPadding = innerPadding,
                        onOpenNewTransactionForDate = { epochDay ->
                            entryViewModel.resetForNewTransaction(initialEpochDay = epochDay)
                            isEntrySheetOpen = true
                        },
                        onEditTransaction = { transaction ->
                            entryViewModel.loadTransactionForEdit(transaction.id)
                            isEntrySheetOpen = true
                        },
                        onSelectBookmarkTemplate = { bookmark ->
                            entryViewModel.resetForNewTransaction(
                                initialEpochDay = ledgerUiState.selectedCalendarDayEpochDay
                            )
                            entryViewModel.applyBookmark(bookmark)
                            isEntrySheetOpen = true
                        }
                    )
                }

                FolioMainTab.ACCOUNTS -> {
                    AccountsScreen(
                        viewModel = accountsViewModel,
                        contentPadding = innerPadding,
                        onAccountClick = { account ->
                            accountsViewModel.selectAccountForInspection(account.id)
                        }
                    )
                }

                FolioMainTab.STATS -> {
                    LiveStatsSummaryPlaceholder(
                        uiState = ledgerUiState,
                        contentPadding = innerPadding
                    )
                }

                FolioMainTab.SETTINGS -> {
                    LiveSettingsSummaryPlaceholder(
                        uiState = ledgerUiState,
                        contentPadding = innerPadding
                    )
                }
            }

            if (isEntrySheetOpen) {
                TransactionEntrySheet(
                    viewModel = entryViewModel,
                    onDismiss = { isEntrySheetOpen = false },
                    onTransactionSaved = { isEntrySheetOpen = false }
                )
            }
        }
    }
}

@Composable
private fun LiveStatsSummaryPlaceholder(
    uiState: LedgerHomeUiState,
    contentPadding: PaddingValues
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Text(
                text = "Statistics & Budgets (${uiState.activePeriodRange.title})",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Live category breakdown from your SQLite ledger. Full interactive Stats & Budget manager arrives in Milestone 4.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (uiState.topExpenseCategories.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "No Expenses Recorded Yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Create an account in the Accounts tab and tap + to add your first expense.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(uiState.topExpenseCategories, key = { it.categoryId }) { stat ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stat.categoryName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = MoneyFormatterAndCalculator.formatMinorMoney(
                                    money = stat.netAmount,
                                    currencySpec = uiState.currencySpec,
                                    grouping = uiState.numberGrouping
                                ),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { (stat.percentageOfTotal / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${String.format("%.1f", stat.percentageOfTotal)}% of period spend • ${stat.transactionCount} txns",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveSettingsSummaryPlaceholder(
    uiState: LedgerHomeUiState,
    contentPadding: PaddingValues
) {
    val prefs = uiState.userPreferences
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Text(
                text = "Folio Configuration",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Active database preferences. Full Settings, Category Editor, Backup & CSV Export screen arrives in Milestone 4 & 5.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Primary Currency: ${uiState.currencySpec.code} (${uiState.currencySpec.symbol})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Number Grouping: ${prefs.numberGrouping.name}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Financial Month Start Day: Day ${prefs.monthStartDay}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "2-Level Subcategories: ${if (prefs.subcategoriesEnabled) "Enabled" else "Disabled"}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Carry-Over Balance: ${if (prefs.carryOverEnabled) "Enabled" else "Disabled"}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}