// File: app/src/main/java/com/folioledger/app/ui/shell/FolioMainShell.kt
package com.folioledger.app.ui.shell

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.folioledger.app.domain.model.AccountGroup
import com.folioledger.app.domain.model.AccountSummary
import com.folioledger.app.domain.model.CategorySpendStat
import com.folioledger.app.domain.model.CurrencySpec
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.MonthlyTrendBar
import com.folioledger.app.domain.model.NetWorthOverview
import com.folioledger.app.domain.model.NumberGroupingSystem
import com.folioledger.app.domain.model.PeriodLedgerSummary
import com.folioledger.app.domain.model.SupportedCurrencies
import com.folioledger.app.domain.model.TransactionItem
import com.folioledger.app.domain.model.TransactionType
import com.folioledger.app.ui.components.CategoryIconBadge
import com.folioledger.app.ui.components.EmptyStateCard
import com.folioledger.app.ui.components.InteractiveCategoryDonutChart
import com.folioledger.app.ui.components.MoneyColorMode
import com.folioledger.app.ui.components.MoneyText
import com.folioledger.app.ui.components.PeriodSelectorHeader
import com.folioledger.app.ui.components.SixMonthTrendBarChart
import com.folioledger.app.ui.components.ThreeColumnSummaryBanner
import com.folioledger.app.ui.components.fromTransactionType
import com.folioledger.app.ui.components.parseHexColorSafely
import com.folioledger.app.ui.theme.FolioAccentPalette
import com.folioledger.app.ui.theme.FolioTheme
import com.folioledger.app.ui.theme.FolioThemeMode
import com.folioledger.app.ui.theme.LocalFolioSemanticColors
import com.folioledger.app.ui.theme.tabularMoneyStyle
import kotlinx.coroutines.launch

enum class FolioMainTab(
    val label: String,
    val icon: ImageVector
) {
    TRANSACTIONS("Trans.", Icons.AutoMirrored.Filled.ReceiptLong),
    STATS("Stats", Icons.Filled.PieChart),
    ACCOUNTS("Accounts", Icons.Filled.AccountBalanceWallet),
    SETTINGS("Settings", Icons.Filled.Settings)
}

enum class TransactionsSubTab(val title: String) {
    DAILY("Daily"),
    CALENDAR("Calendar"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    SUMMARY("Summary")
}

@Composable
fun FolioMainShell(
    initialThemeMode: String = FolioThemeMode.SYSTEM.key,
    initialAccentPalette: String = FolioAccentPalette.EMERALD.key,
    initialDynamicColor: Boolean = false,
    onNewTransactionClick: (() -> Unit)? = null,
    transactionsTabContent: (@Composable (PaddingValues) -> Unit)? = null,
    statsTabContent: (@Composable (PaddingValues) -> Unit)? = null,
    accountsTabContent: (@Composable (PaddingValues) -> Unit)? = null,
    settingsTabContent: (@Composable (PaddingValues) -> Unit)? = null
) {
    var activeThemeMode by rememberSaveable { mutableStateOf(initialThemeMode) }
    var activeAccentPalette by rememberSaveable { mutableStateOf(initialAccentPalette) }
    var activeDynamicColor by rememberSaveable { mutableStateOf(initialDynamicColor) }
    var activeCurrencyCode by rememberSaveable { mutableStateOf("INR") }
    var activeGroupingName by rememberSaveable { mutableStateOf(NumberGroupingSystem.INDIAN.name) }
    var carryOverEnabled by rememberSaveable { mutableStateOf(true) }

    var selectedMainTab by rememberSaveable { mutableStateOf(FolioMainTab.TRANSACTIONS) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val currencySpec = remember(activeCurrencyCode) {
        SupportedCurrencies.fromCode(activeCurrencyCode)
    }
    val groupingSystem = remember(activeGroupingName) {
        runCatching { NumberGroupingSystem.valueOf(activeGroupingName) }
            .getOrDefault(currencySpec.defaultGrouping)
    }

    FolioTheme(
        themeMode = activeThemeMode,
        accentPalette = activeAccentPalette,
        dynamicColorEnabled = activeDynamicColor
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    tonalElevation = 3.dp
                ) {
                    FolioMainTab.entries.forEach { tab ->
                        val isSelected = selectedMainTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedMainTab = tab },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.label
                                )
                            },
                            label = {
                                Text(
                                    text = tab.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        )
                    }
                }
            },
            floatingActionButton = {
                if (selectedMainTab == FolioMainTab.TRANSACTIONS || selectedMainTab == FolioMainTab.ACCOUNTS) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            if (onNewTransactionClick != null) {
                                onNewTransactionClick()
                            } else {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "New Transaction composer sheet connects in Milestone 3"
                                    )
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "New Transaction"
                            )
                        },
                        text = {
                            Text(
                                text = "New Transaction",
                                style = MaterialTheme.typography.labelLarge
                            )
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        ) { innerPadding ->
            AnimatedContent(
                targetState = selectedMainTab,
                label = "FolioMainTabTransition"
            ) { currentTab ->
                when (currentTab) {
                    FolioMainTab.TRANSACTIONS -> {
                        if (transactionsTabContent != null) {
                            transactionsTabContent(innerPadding)
                        } else {
                            TransactionsPreviewTab(
                                contentPadding = innerPadding,
                                currencySpec = currencySpec,
                                grouping = groupingSystem,
                                carryOverEnabled = carryOverEnabled,
                                onQuickFeedback = { msg ->
                                    coroutineScope.launch { snackbarHostState.showSnackbar(msg) }
                                }
                            )
                        }
                    }

                    FolioMainTab.STATS -> {
                        if (statsTabContent != null) {
                            statsTabContent(innerPadding)
                        } else {
                            StatsPreviewTab(
                                contentPadding = innerPadding,
                                currencySpec = currencySpec,
                                grouping = groupingSystem
                            )
                        }
                    }

                    FolioMainTab.ACCOUNTS -> {
                        if (accountsTabContent != null) {
                            accountsTabContent(innerPadding)
                        } else {
                            AccountsPreviewTab(
                                contentPadding = innerPadding,
                                currencySpec = currencySpec,
                                grouping = groupingSystem
                            )
                        }
                    }

                    FolioMainTab.SETTINGS -> {
                        if (settingsTabContent != null) {
                            settingsTabContent(innerPadding)
                        } else {
                            SettingsInteractivePreviewTab(
                                contentPadding = innerPadding,
                                activeThemeMode = activeThemeMode,
                                onThemeModeChange = { activeThemeMode = it },
                                activeAccentPalette = activeAccentPalette,
                                onAccentPaletteChange = { activeAccentPalette = it },
                                dynamicColorEnabled = activeDynamicColor,
                                onDynamicColorChange = { activeDynamicColor = it },
                                activeCurrencyCode = activeCurrencyCode,
                                onCurrencyCodeChange = { code ->
                                    activeCurrencyCode = code
                                    activeGroupingName = SupportedCurrencies.fromCode(code).defaultGrouping.name
                                },
                                activeGrouping = groupingSystem,
                                onGroupingChange = { activeGroupingName = it.name },
                                carryOverEnabled = carryOverEnabled,
                                onCarryOverChange = { carryOverEnabled = it }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionsPreviewTab(
    contentPadding: PaddingValues,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem,
    carryOverEnabled: Boolean,
    onQuickFeedback: (String) -> Unit
) {
    val months = remember {
        listOf("Aug 2026", "Sep 2026", "Oct 2026", "Nov 2026")
    }
    var monthIndex by rememberSaveable { mutableIntStateOf(2) }
    var selectedSubTab by rememberSaveable { mutableStateOf(TransactionsSubTab.DAILY) }
    var activeFilterCount by rememberSaveable { mutableIntStateOf(0) }

    val periodSummary = remember(monthIndex) {
        val factor = (monthIndex + 1).toLong()
        val income = MinorMoney(145000_00L + (factor * 5000_00L))
        val expense = MinorMoney(58420_00L + (factor * 2100_00L))
        PeriodLedgerSummary(
            startEpochDay = 20727L,
            endEpochDay = 20757L,
            totalIncome = income,
            totalExpense = expense,
            netCashflow = income - expense,
            carryOverFromPreviousPeriod = MinorMoney(42150_00L)
        )
    }

    val sampleTransactions = remember {
        listOf(
            TransactionItem(
                id = 1L,
                type = TransactionType.EXPENSE,
                timestampEpochMillis = 1792243200000L,
                ledgerDateEpochDay = 20743L,
                amount = MinorMoney(1850_00L),
                currencyCode = "INR",
                accountId = 2L,
                accountName = "HDFC Regalia CC",
                categoryId = 101L,
                categoryName = "South Indian Dining",
                parentCategoryName = "Food & Dining",
                categoryColorHex = "#F97316",
                categoryIconName = "restaurant",
                payeeOrDescription = "Family Dinner & Filter Coffee",
                tags = listOf("weekend", "family")
            ),
            TransactionItem(
                id = 2L,
                type = TransactionType.EXPENSE,
                timestampEpochMillis = 1792221600000L,
                ledgerDateEpochDay = 20743L,
                amount = MinorMoney(4320_00L),
                currencyCode = "INR",
                accountId = 1L,
                accountName = "ICICI Salary A/c",
                categoryId = 102L,
                categoryName = "Groceries & Provisions",
                parentCategoryName = "Household",
                categoryColorHex = "#10B981",
                categoryIconName = "shopping_cart",
                payeeOrDescription = "Seeraga Samba Rice, Cold-Pressed Oil & Spices",
                isSplit = true
            ),
            TransactionItem(
                id = 3L,
                type = TransactionType.TRANSFER,
                timestampEpochMillis = 1792156800000L,
                ledgerDateEpochDay = 20742L,
                amount = MinorMoney(25000_00L),
                currencyCode = "INR",
                accountId = 1L,
                accountName = "ICICI Salary A/c",
                transferToAccountId = 4L,
                transferToAccountName = "Liquid Mutual Fund",
                categoryColorHex = "#2563EB",
                categoryIconName = "swap_horiz",
                payeeOrDescription = "Monthly Emergency Reserve SIP"
            ),
            TransactionItem(
                id = 4L,
                type = TransactionType.INCOME,
                timestampEpochMillis = 1790860800000L,
                ledgerDateEpochDay = 20728L,
                amount = MinorMoney(160000_00L),
                currencyCode = "INR",
                accountId = 1L,
                accountName = "ICICI Salary A/c",
                categoryId = 201L,
                categoryName = "Primary Salary",
                parentCategoryName = "Income",
                categoryColorHex = "#059669",
                categoryIconName = "payments",
                payeeOrDescription = "Monthly Salary Credit"
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
    ) {
        PeriodSelectorHeader(
            periodTitle = months[monthIndex],
            periodSubtitle = "01 ${months[monthIndex].take(3)} – 31 ${months[monthIndex].take(3)}",
            onPreviousPeriod = {
                monthIndex = (monthIndex - 1 + months.size) % months.size
            },
            onNextPeriod = {
                monthIndex = (monthIndex + 1) % months.size
            },
            onBookmarksClick = {
                onQuickFeedback("Bookmarks quick-templates ready for Milestone 3")
            },
            onFilterClick = {
                activeFilterCount = (activeFilterCount + 1) % 3
            },
            activeFilterCount = activeFilterCount,
            onSearchClick = {
                onQuickFeedback("Multi-criteria search drawer connects in Milestone 3")
            }
        )

        PrimaryScrollableTabRow(
            selectedTabIndex = selectedSubTab.ordinal,
            edgePadding = 12.dp,
            containerColor = MaterialTheme.colorScheme.surface,
            divider = {}
        ) {
            TransactionsSubTab.entries.forEach { subTab ->
                val selected = selectedSubTab == subTab
                Tab(
                    selected = selected,
                    onClick = { selectedSubTab = subTab },
                    text = {
                        Text(
                            text = subTab.title,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
            }
        }

        HorizontalDivider(color = LocalFolioSemanticColors.current.cardStrokeSubtle)

        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 12.dp,
                bottom = 96.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                ThreeColumnSummaryBanner(
                    periodSummary = periodSummary,
                    currencySpec = currencySpec,
                    grouping = grouping,
                    showCarryOver = carryOverEnabled
                )
            }

            when (selectedSubTab) {
                TransactionsSubTab.DAILY -> {
                    item {
                        DailyDateGroupHeader(
                            dayNumber = "17",
                            dayOfWeek = "SAT",
                            monthYear = months[monthIndex],
                            dayIncome = MinorMoney.ZERO,
                            dayExpense = MinorMoney(6170_00L),
                            currencySpec = currencySpec,
                            grouping = grouping
                        )
                    }
                    items(sampleTransactions.take(2), key = { it.id }) { tx ->
                        TransactionRowCard(
                            transaction = tx,
                            currencySpec = currencySpec,
                            grouping = grouping
                        )
                    }
                    item {
                        DailyDateGroupHeader(
                            dayNumber = "16",
                            dayOfWeek = "FRI",
                            monthYear = months[monthIndex],
                            dayIncome = MinorMoney(160000_00L),
                            dayExpense = MinorMoney.ZERO,
                            currencySpec = currencySpec,
                            grouping = grouping
                        )
                    }
                    items(sampleTransactions.drop(2), key = { it.id }) { tx ->
                        TransactionRowCard(
                            transaction = tx,
                            currencySpec = currencySpec,
                            grouping = grouping
                        )
                    }
                }

                TransactionsSubTab.CALENDAR -> {
                    item {
                        CalendarSubTabPreviewCard(
                            monthLabel = months[monthIndex],
                            currencySpec = currencySpec,
                            grouping = grouping
                        )
                    }
                }

                TransactionsSubTab.WEEKLY -> {
                    item {
                        WeeklyBreakdownPreviewCard(
                            currencySpec = currencySpec,
                            grouping = grouping
                        )
                    }
                }

                TransactionsSubTab.MONTHLY -> {
                    item {
                        MonthlyOverviewPreviewCard(
                            months = months,
                            currencySpec = currencySpec,
                            grouping = grouping
                        )
                    }
                }

                TransactionsSubTab.SUMMARY -> {
                    item {
                        SummarySubTabPreviewCard(
                            summary = periodSummary,
                            currencySpec = currencySpec,
                            grouping = grouping
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyDateGroupHeader(
    dayNumber: String,
    dayOfWeek: String,
    monthYear: String,
    dayIncome: MinorMoney,
    dayExpense: MinorMoney,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 2.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = dayNumber,
                style = tabularMoneyStyle(MaterialTheme.typography.titleLarge),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = dayOfWeek,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = monthYear,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (dayIncome.isPositive) {
                MoneyText(
                    money = dayIncome,
                    currencySpec = currencySpec,
                    grouping = grouping,
                    colorMode = MoneyColorMode.INCOME,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    showPlusSignForPositive = true
                )
            }
            if (dayExpense.isPositive) {
                MoneyText(
                    money = dayExpense,
                    currencySpec = currencySpec,
                    grouping = grouping,
                    colorMode = MoneyColorMode.EXPENSE,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun TransactionRowCard(
    transaction: TransactionItem,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem
) {
    val semanticColors = LocalFolioSemanticColors.current

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            CategoryIconBadge(
                iconName = transaction.categoryIconName,
                colorHex = transaction.categoryColorHex
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                val primaryTitle = when (transaction.type) {
                    TransactionType.TRANSFER -> "${transaction.accountName} → ${transaction.transferToAccountName ?: "Account"}"
                    else -> transaction.categoryName ?: "Uncategorized"
                }
                Text(
                    text = primaryTitle,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (transaction.payeeOrDescription.isNotBlank()) {
                    Text(
                        text = transaction.payeeOrDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = if (transaction.isSplit) {
                        "${transaction.accountName} · Split Item"
                    } else {
                        transaction.accountName
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            MoneyText(
                money = transaction.amount,
                currencySpec = currencySpec,
                grouping = grouping,
                colorMode = when (transaction.type) {
                    com.folioledger.app.domain.model.TransactionType.INCOME,
                    com.folioledger.app.domain.model.TransactionType.REFUND -> MoneyColorMode.INCOME
                    com.folioledger.app.domain.model.TransactionType.EXPENSE -> MoneyColorMode.EXPENSE
                    com.folioledger.app.domain.model.TransactionType.TRANSFER,
                    com.folioledger.app.domain.model.TransactionType.CC_SETTLEMENT -> MoneyColorMode.TRANSFER
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CalendarSubTabPreviewCard(
    monthLabel: String,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem
) {
    val semanticColors = LocalFolioSemanticColors.current
    val dayHeaders = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "$monthLabel Cashflow Calendar",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                dayHeaders.forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            for (week in 0 until 5) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (dow in 1..7) {
                        val dayNum = week * 7 + dow - 2
                        val isValidDay = dayNum in 1..31
                        val isSelected = dayNum == 17
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when {
                                !isValidDay -> Color.Transparent
                                isSelected -> MaterialTheme.colorScheme.primaryContainer
                                else -> MaterialTheme.colorScheme.surfaceContainer
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp)
                        ) {
                            if (isValidDay) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.padding(4.dp)
                                ) {
                                    Text(
                                        text = dayNum.toString(),
                                        style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                    if (dayNum == 1) {
                                        MoneyText(
                                            money = MinorMoney(160000_00L),
                                            currencySpec = currencySpec,
                                            grouping = grouping,
                                            includeSymbol = false,
                                            colorMode = MoneyColorMode.INCOME,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    } else if (dayNum == 17 || dayNum % 5 == 0) {
                                        MoneyText(
                                            money = MinorMoney((dayNum * 350_00L)),
                                            currencySpec = currencySpec,
                                            grouping = grouping,
                                            includeSymbol = false,
                                            colorMode = MoneyColorMode.EXPENSE,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeeklyBreakdownPreviewCard(
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem
) {
    val weeks = listOf(
        Triple("Week 1 (01 Oct – 04 Oct)", MinorMoney(160000_00L), MinorMoney(14200_00L)),
        Triple("Week 2 (05 Oct – 11 Oct)", MinorMoney.ZERO, MinorMoney(19850_00L)),
        Triple("Week 3 (12 Oct – 18 Oct)", MinorMoney(5000_00L), MinorMoney(16420_00L)),
        Triple("Week 4 (19 Oct – 25 Oct)", MinorMoney.ZERO, MinorMoney(14250_00L))
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        weeks.forEach { (label, inc, exp) ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                border = BorderStroke(1.dp, LocalFolioSemanticColors.current.cardStrokeSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        MoneyText(
                            money = inc,
                            currencySpec = currencySpec,
                            grouping = grouping,
                            colorMode = MoneyColorMode.INCOME,
                            style = MaterialTheme.typography.labelMedium
                        )
                        MoneyText(
                            money = exp,
                            currencySpec = currencySpec,
                            grouping = grouping,
                            colorMode = MoneyColorMode.EXPENSE,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthlyOverviewPreviewCard(
    months: List<String>,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        months.reversed().forEachIndexed { idx, m ->
            val inc = MinorMoney(160000_00L)
            val exp = MinorMoney(61500_00L - (idx * 3400_00L))
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                border = BorderStroke(1.dp, LocalFolioSemanticColors.current.cardStrokeSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Text(
                        text = m,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        MoneyText(
                            money = inc,
                            currencySpec = currencySpec,
                            grouping = grouping,
                            colorMode = MoneyColorMode.INCOME,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        MoneyText(
                            money = exp,
                            currencySpec = currencySpec,
                            grouping = grouping,
                            colorMode = MoneyColorMode.EXPENSE,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummarySubTabPreviewCard(
    summary: PeriodLedgerSummary,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, LocalFolioSemanticColors.current.cardStrokeSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Period Financial Digest",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            HorizontalDivider(color = LocalFolioSemanticColors.current.cardStrokeSubtle)
            SummaryDigestRow("Gross Period Income", summary.totalIncome, MoneyColorMode.INCOME, currencySpec, grouping)
            SummaryDigestRow("Total Period Expenses", summary.totalExpense, MoneyColorMode.EXPENSE, currencySpec, grouping)
            SummaryDigestRow("Net Period Savings", summary.netCashflow, MoneyColorMode.AUTO_SIGN, currencySpec, grouping)
            SummaryDigestRow(
                "Cumulative Balance (with Carry-over)",
                summary.netCashflow + summary.carryOverFromPreviousPeriod,
                MoneyColorMode.AUTO_SIGN,
                currencySpec,
                grouping
            )
        }
    }
}

@Composable
private fun SummaryDigestRow(
    label: String,
    amount: MinorMoney,
    colorMode: MoneyColorMode,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        MoneyText(
            money = amount,
            currencySpec = currencySpec,
            grouping = grouping,
            colorMode = colorMode,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun StatsPreviewTab(
    contentPadding: PaddingValues,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem
) {
    val categoryStats = remember {
        listOf(
            CategorySpendStat(
                categoryId = 1L,
                categoryName = "Housing & Utilities",
                colorHex = "#2563EB",
                iconName = "home",
                netAmount = MinorMoney(24000_00L),
                percentageOfTotal = 37.1f,
                transactionCount = 4
            ),
            CategorySpendStat(
                categoryId = 2L,
                categoryName = "Food & Groceries",
                colorHex = "#059669",
                iconName = "shopping_cart",
                netAmount = MinorMoney(16800_00L),
                percentageOfTotal = 26.0f,
                transactionCount = 19
            ),
            CategorySpendStat(
                categoryId = 3L,
                categoryName = "Family & Baby Care",
                colorHex = "#D97706",
                iconName = "child_care",
                netAmount = MinorMoney(11400_00L),
                percentageOfTotal = 17.6f,
                transactionCount = 9
            ),
            CategorySpendStat(
                categoryId = 4L,
                categoryName = "Fuel & Commute",
                colorHex = "#E11D48",
                iconName = "local_gas_station",
                netAmount = MinorMoney(7200_00L),
                percentageOfTotal = 11.1f,
                transactionCount = 6
            ),
            CategorySpendStat(
                categoryId = 5L,
                categoryName = "Health & Fitness",
                colorHex = "#7C3AED",
                iconName = "fitness_center",
                netAmount = MinorMoney(5320_00L),
                percentageOfTotal = 8.2f,
                transactionCount = 5
            )
        )
    }

    val trendBars = remember {
        listOf(
            MonthlyTrendBar("May", 1L, 2L, MinorMoney(150000_00L), MinorMoney(62000_00L), MinorMoney(88000_00L)),
            MonthlyTrendBar("Jun", 3L, 4L, MinorMoney(150000_00L), MinorMoney(71400_00L), MinorMoney(78600_00L)),
            MonthlyTrendBar("Jul", 5L, 6L, MinorMoney(165000_00L), MinorMoney(84200_00L), MinorMoney(80800_00L)),
            MonthlyTrendBar("Aug", 7L, 8L, MinorMoney(155000_00L), MinorMoney(59100_00L), MinorMoney(95900_00L)),
            MonthlyTrendBar("Sep", 9L, 10L, MinorMoney(155000_00L), MinorMoney(61800_00L), MinorMoney(93200_00L)),
            MonthlyTrendBar("Oct", 11L, 12L, MinorMoney(160000_00L), MinorMoney(64720_00L), MinorMoney(95280_00L))
        )
    }

    var selectedCategoryId by rememberSaveable { mutableStateOf<Long?>(null) }

    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 12.dp,
            bottom = contentPadding.calculateBottomPadding() + 28.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "Analytics & Category Breakdown",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            InteractiveCategoryDonutChart(
                stats = categoryStats,
                currencySpec = currencySpec,
                grouping = grouping,
                selectedCategoryId = selectedCategoryId,
                onCategorySelected = { selectedCategoryId = it?.categoryId }
            )
        }

        item {
            SixMonthTrendBarChart(
                bars = trendBars,
                currencySpec = currencySpec,
                grouping = grouping
            )
        }
    }
}

@Composable
private fun AccountsPreviewTab(
    contentPadding: PaddingValues,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem
) {
    val accounts = remember {
        listOf(
            AccountSummary(
                id = 1L,
                name = "ICICI Salary Account",
                groupType = AccountGroup.BANK_ACCOUNT,
                currencyCode = "INR",
                currentBalance = MinorMoney(284500_00L),
                colorHex = "#059669",
                iconName = "account_balance"
            ),
            AccountSummary(
                id = 2L,
                name = "Liquid Mutual Fund Reserve",
                groupType = AccountGroup.INVESTMENT,
                currencyCode = "INR",
                currentBalance = MinorMoney(650000_00L),
                colorHex = "#2563EB",
                iconName = "trending_up"
            ),
            AccountSummary(
                id = 3L,
                name = "HDFC Regalia Credit Card",
                groupType = AccountGroup.CREDIT_CARD,
                currencyCode = "INR",
                currentBalance = MinorMoney(-34280_00L),
                creditLimit = MinorMoney(400000_00L),
                creditCardStatementPayable = MinorMoney(21400_00L),
                creditCardUnbilledAmount = MinorMoney(12880_00L),
                settlementDayOfMonth = 18,
                paymentDueDayOfMonth = 5,
                colorHex = "#E11D48",
                iconName = "credit_card"
            ),
            AccountSummary(
                id = 4L,
                name = "Cash Wallet",
                groupType = AccountGroup.CASH,
                currencyCode = "INR",
                currentBalance = MinorMoney(4850_00L),
                colorHex = "#D97706",
                iconName = "account_balance_wallet"
            )
        )
    }

    val netWorthOverview = remember(accounts) {
        val totalAssets = MinorMoney(939350_00L)
        val totalLiabilities = MinorMoney(34280_00L)
        NetWorthOverview(
            totalAssets = totalAssets,
            totalLiabilities = totalLiabilities,
            netWorth = totalAssets - totalLiabilities,
            accountsByGroup = accounts.groupBy { it.groupType }
        )
    }

    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 12.dp,
            bottom = contentPadding.calculateBottomPadding() + 96.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "Accounts & Net Worth",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            ThreeColumnSummaryBanner(
                netWorthOverview = netWorthOverview,
                currencySpec = currencySpec,
                grouping = grouping
            )
        }

        items(accounts, key = { it.id }) { account ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                border = BorderStroke(1.dp, LocalFolioSemanticColors.current.cardStrokeSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CategoryIconBadge(
                            iconName = account.iconName,
                            colorHex = account.colorHex
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = account.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = account.groupType.name.replace('_', ' '),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        MoneyText(
                            money = account.currentBalance,
                            currencySpec = currencySpec,
                            grouping = grouping,
                            colorMode = MoneyColorMode.AUTO_SIGN,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (account.groupType == AccountGroup.CREDIT_CARD &&
                        account.creditCardStatementPayable != null &&
                        account.creditCardUnbilledAmount != null
                    ) {
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = LocalFolioSemanticColors.current.cardStrokeSubtle)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = "Statement Payable (Due Day ${account.paymentDueDayOfMonth ?: 5})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                MoneyText(
                                    money = account.creditCardStatementPayable,
                                    currencySpec = currencySpec,
                                    grouping = grouping,
                                    colorMode = MoneyColorMode.EXPENSE,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Unbilled Active Cycle",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                MoneyText(
                                    money = account.creditCardUnbilledAmount,
                                    currencySpec = currencySpec,
                                    grouping = grouping,
                                    colorMode = MoneyColorMode.NEUTRAL,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SettingsInteractivePreviewTab(
    contentPadding: PaddingValues,
    activeThemeMode: String,
    onThemeModeChange: (String) -> Unit,
    activeAccentPalette: String,
    onAccentPaletteChange: (String) -> Unit,
    dynamicColorEnabled: Boolean,
    onDynamicColorChange: (Boolean) -> Unit,
    activeCurrencyCode: String,
    onCurrencyCodeChange: (String) -> Unit,
    activeGrouping: NumberGroupingSystem,
    onGroupingChange: (NumberGroupingSystem) -> Unit,
    carryOverEnabled: Boolean,
    onCarryOverChange: (Boolean) -> Unit
) {
    val semanticColors = LocalFolioSemanticColors.current

    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 12.dp,
            bottom = contentPadding.calculateBottomPadding() + 32.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "Appearance & Ledger Preferences",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Test live Material 3 themes, AMOLED pitch black, accent palettes & Indian/International number grouping",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.DarkMode,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Theme Mode",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FolioThemeMode.entries.forEach { mode ->
                            FilterChip(
                                selected = activeThemeMode.equals(mode.key, ignoreCase = true),
                                onClick = { onThemeModeChange(mode.key) },
                                label = { Text(mode.displayName) }
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Accent Palette (6 Presets)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FolioAccentPalette.entries.forEach { palette ->
                            val isSelected = activeAccentPalette.equals(palette.key, ignoreCase = true)
                            val swatchColor = parseHexColorSafely(palette.seedHex, palette.previewColor)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) {
                                    swatchColor.copy(alpha = 0.16f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainer
                                },
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) swatchColor else semanticColors.cardStrokeSubtle
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onAccentPaletteChange(palette.key) }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(swatchColor)
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Filled.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = palette.displayName,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = semanticColors.cardStrokeSubtle)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Material You Dynamic Color (Android 12+)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Overrides preset accent palette with wallpaper colors",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = dynamicColorEnabled,
                            onCheckedChange = onDynamicColorChange
                        )
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Currency & Number Grouping",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("INR", "USD", "EUR", "GBP", "JPY", "KWD").forEach { code ->
                            FilterChip(
                                selected = activeCurrencyCode == code,
                                onClick = { onCurrencyCodeChange(code) },
                                label = { Text(code) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = activeGrouping == NumberGroupingSystem.INDIAN,
                            onClick = { onGroupingChange(NumberGroupingSystem.INDIAN) },
                            label = { Text("Indian (12,34,567.89)") }
                        )
                        FilterChip(
                            selected = activeGrouping == NumberGroupingSystem.INTERNATIONAL,
                            onClick = { onGroupingChange(NumberGroupingSystem.INTERNATIONAL) },
                            label = { Text("International (1,234,567.89)") }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Show Period Carry-Over in Summary Banner",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Switch(
                            checked = carryOverEnabled,
                            onCheckedChange = onCarryOverChange
                        )
                    }
                }
            }
        }

        item {
            EmptyStateCard(
                title = "Empty State Component Preview",
                subtitle = "Used when a filtered ledger view, search query, or budget list has no matching records."
            )
        }
    }
}