// File: app/src/main/java/com/folioledger/app/ui/shell/FolioMainShell.kt
package com.folioledger.app.ui.shell

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.folioledger.app.ui.accounts.AccountsScreen
import com.folioledger.app.ui.accounts.AccountsViewModel
import com.folioledger.app.ui.entry.TransactionEntrySheet
import com.folioledger.app.ui.entry.TransactionEntryViewModel
import com.folioledger.app.ui.ledger.LedgerHomeScreen
import com.folioledger.app.ui.ledger.LedgerHomeViewModel
import com.folioledger.app.ui.settings.SettingsScreen
import com.folioledger.app.ui.settings.SettingsViewModel
import com.folioledger.app.ui.stats.StatsAndBudgetsScreen
import com.folioledger.app.ui.stats.StatsAndBudgetsViewModel
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
    val statsViewModel: StatsAndBudgetsViewModel = viewModel(factory = viewModelFactory)
    val settingsViewModel: SettingsViewModel = viewModel(factory = viewModelFactory)

    val ledgerUiState by ledgerViewModel.uiState.collectAsStateWithLifecycle()
    val prefs = ledgerUiState.userPreferences

    var selectedTab by rememberSaveable { mutableStateOf(FolioMainTab.TRANSACTIONS) }
    var isEntrySheetOpen by rememberSaveable { mutableStateOf(false) }

    FolioTheme(
        themeMode = prefs.themeMode,
        accentPalette = prefs.accentPalette,
        dynamicColorEnabled = prefs.dynamicColorEnabled
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
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
            // Only forward bottom navigation bar padding so child screens don't double-pad the top status bar
            val screenContentPadding = PaddingValues(
                bottom = innerPadding.calculateBottomPadding()
            )

            when (selectedTab) {
                FolioMainTab.TRANSACTIONS -> {
                    LedgerHomeScreen(
                        viewModel = ledgerViewModel,
                        contentPadding = screenContentPadding,
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

                FolioMainTab.STATS -> {
                    StatsAndBudgetsScreen(
                        viewModel = statsViewModel,
                        contentPadding = screenContentPadding,
                        onInspectTransaction = { transaction ->
                            entryViewModel.loadTransactionForEdit(transaction.id)
                            isEntrySheetOpen = true
                        }
                    )
                }

                FolioMainTab.ACCOUNTS -> {
                    AccountsScreen(
                        viewModel = accountsViewModel,
                        contentPadding = screenContentPadding,
                        onAccountClick = { account ->
                            accountsViewModel.selectAccountForInspection(account.id)
                        }
                    )
                }

                FolioMainTab.SETTINGS -> {
                    SettingsScreen(
                        viewModel = settingsViewModel,
                        contentPadding = screenContentPadding
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