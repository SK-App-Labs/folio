package com.folioledger.app.ui.ledger

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import com.folioledger.app.domain.model.CurrencySpec
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.NumberGroupingSystem
import com.folioledger.app.domain.model.TransactionItem
import com.folioledger.app.ui.components.CategoryIconBadge
import com.folioledger.app.ui.components.EmptyStateCard
import com.folioledger.app.ui.components.MoneyColorMode
import com.folioledger.app.ui.components.MoneyText
import com.folioledger.app.ui.components.ThreeColumnSummaryBanner
import com.folioledger.app.ui.theme.LocalFolioSemanticColors
import com.folioledger.app.ui.theme.tabularMoneyStyle
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
internal fun FilteredSearchResultsContent(
    uiState: LedgerHomeUiState,
    onTransactionClick: (TransactionItem) -> Unit,
    onToggleFilterAllPeriods: (Boolean) -> Unit,
    onClearFilters: () -> Unit
) {
    val lazyListState = rememberLazyListState()

    androidx.compose.runtime.LaunchedEffect(uiState.scrollToTopSignal) {
        if (uiState.scrollToTopSignal > 0) {
            lazyListState.animateScrollToItem(0)
        }
    }

    LazyColumn(
        state = lazyListState,
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 12.dp,
            bottom = 96.dp
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            ThreeColumnSummaryBanner(
                firstLabel = "Filtered Income",
                firstAmount = uiState.filteredIncomeTotal,
                secondLabel = "Filtered Expenses",
                secondAmount = uiState.filteredExpenseTotal,
                thirdLabel = "Filtered Net",
                thirdAmount = uiState.filteredNetTotal,
                currencySpec = uiState.currencySpec,
                grouping = uiState.numberGrouping
            )
        }

        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${uiState.filteredTransactions.size} matches",
                        style = tabularMoneyStyle(MaterialTheme.typography.labelLarge),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    FilterChip(
                        selected = !uiState.filterAllPeriods,
                        onClick = { onToggleFilterAllPeriods(!uiState.filterAllPeriods) },
                        label = {
                            Text(
                                text = if (uiState.filterAllPeriods) {
                                    "All Dates"
                                } else {
                                    "In ${uiState.activePeriodRange.title}"
                                },
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    )
                }

                TextButton(onClick = onClearFilters) {
                    Text("Exit Filter")
                }
            }
        }

        if (uiState.filteredDailyGroups.isEmpty()) {
            item {
                EmptyStateCard(
                    title = if (uiState.filterAllPeriods) {
                        "No Matching Transactions"
                    } else {
                        "No Matches in ${uiState.activePeriodRange.title}"
                    },
                    subtitle = if (uiState.filterAllPeriods) {
                        "No ledger entries matched your search query and multi-filter criteria."
                    } else {
                        "Use < Prev / Next > above to check other months, or tap 'In ${uiState.activePeriodRange.title}' to search across all dates."
                    },
                    icon = Icons.Filled.Search,
                    actionLabel = "Clear All Filters",
                    onActionClick = onClearFilters
                )
            }
        } else {
            uiState.filteredDailyGroups.forEach { group ->
                val headerKey = "filtered_day_${group.epochDay}"
                stickyHeader(key = headerKey, contentType = "day_group_header") {
                    DailyDateGroupHeaderRow(
                        group = group,
                        currencySpec = uiState.currencySpec,
                        grouping = uiState.numberGrouping
                    )
                }
                items(
                    items = group.transactions,
                    key = { "filtered_tx_${it.id}" },
                    contentType = { "transaction_row" }
                ) { tx ->
                    LedgerTransactionItemCard(
                        transaction = tx,
                        scopedAccountId = uiState.scopedAccountId,
                        currencySpec = uiState.currencySpec,
                        grouping = uiState.numberGrouping,
                        onClick = { onTransactionClick(tx) }
                    )
                }
            }
        }
    }
}

@Composable
internal fun DailyLedgerSubTabContent(
    uiState: LedgerHomeUiState,
    onTransactionClick: (TransactionItem) -> Unit,
    onAddTransactionClick: () -> Unit
) {
    val lazyListState = rememberLazyListState()

    androidx.compose.runtime.LaunchedEffect(uiState.scrollToTopSignal) {
        if (uiState.scrollToTopSignal > 0) {
            lazyListState.animateScrollToItem(0)
        }
    }

    LazyColumn(
        state = lazyListState,
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 12.dp,
            bottom = 96.dp
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            ThreeColumnSummaryBanner(
                periodSummary = uiState.periodSummary,
                currencySpec = uiState.currencySpec,
                grouping = uiState.numberGrouping,
                showCarryOver = uiState.userPreferences.carryOverEnabled && uiState.scopedAccountId == null
            )
        }

        if (uiState.dailyGroups.isEmpty()) {
            item {
                val scopedName = uiState.scopedAccount?.name
                EmptyStateCard(
                    title = if (scopedName != null) {
                        "No Transactions for $scopedName in ${uiState.activePeriodRange.title}"
                    } else {
                        "No Transactions in ${uiState.activePeriodRange.title}"
                    },
                    subtitle = "Record an expense, income, or transfer to populate your daily chronological ledger.",
                    icon = Icons.AutoMirrored.Filled.ReceiptLong,
                    actionLabel = "+ Add Transaction",
                    onActionClick = onAddTransactionClick
                )
            }
        } else {
            uiState.dailyGroups.forEach { group ->
                val headerKey = "day_header_${group.epochDay}"
                stickyHeader(key = headerKey, contentType = "day_group_header") {
                    DailyDateGroupHeaderRow(
                        group = group,
                        currencySpec = uiState.currencySpec,
                        grouping = uiState.numberGrouping
                    )
                }
                item(key = "day_card_${group.epochDay}", contentType = "day_group_card") {
                    DailyGroupedTransactionsCard(
                        transactions = group.transactions,
                        scopedAccountId = uiState.scopedAccountId,
                        currencySpec = uiState.currencySpec,
                        grouping = uiState.numberGrouping,
                        onTransactionClick = onTransactionClick
                    )
                }
            }
        }
    }
}

@Composable
internal fun CalendarLedgerSubTabContent(
    uiState: LedgerHomeUiState,
    onSelectDay: (Long) -> Unit,
    onAddOnDate: (Long) -> Unit,
    onTransactionClick: (TransactionItem) -> Unit
) {
    val semanticColors = LocalFolioSemanticColors.current
    val selectedDateFormatted = remember(uiState.selectedCalendarDayEpochDay) {
        LocalDate.ofEpochDay(uiState.selectedCalendarDayEpochDay)
            .format(DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy", Locale.US))
    }

    val weeksOfCells = remember(uiState.calendarCells) {
        uiState.calendarCells.chunked(7)
    }

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
                periodSummary = uiState.periodSummary,
                currencySpec = uiState.currencySpec,
                grouping = uiState.numberGrouping,
                showCarryOver = uiState.userPreferences.carryOverEnabled && uiState.scopedAccountId == null
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        uiState.calendarWeekdayHeaders.forEach { header ->
                            Text(
                                text = header,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    weeksOfCells.forEach { weekRow ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        ) {
                            weekRow.forEach { cell ->
                                val isSelected = cell.epochDay != null &&
                                        cell.epochDay == uiState.selectedCalendarDayEpochDay
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = when {
                                        !cell.isInActivePeriod -> Color.Transparent
                                        isSelected -> MaterialTheme.colorScheme.primaryContainer
                                        cell.isToday -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
                                        else -> MaterialTheme.colorScheme.surfaceContainer
                                    },
                                    border = if (isSelected) {
                                        BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                                    } else {
                                        null
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(62.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .then(
                                            if (cell.epochDay != null) {
                                                Modifier.clickable { onSelectDay(cell.epochDay) }
                                            } else {
                                                Modifier
                                            }
                                        )
                                ) {
                                    if (cell.isInActivePeriod && cell.dayNumber != null) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(vertical = 4.dp, horizontal = 2.dp)
                                        ) {
                                            Text(
                                                text = cell.dayNumber.toString(),
                                                style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                                                fontWeight = if (isSelected || cell.isToday) {
                                                    FontWeight.Bold
                                                } else {
                                                    FontWeight.Medium
                                                },
                                                color = if (isSelected) {
                                                    MaterialTheme.colorScheme.onPrimaryContainer
                                                } else {
                                                    MaterialTheme.colorScheme.onSurface
                                                }
                                            )

                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                if (cell.incomeTotal.isPositive) {
                                                    MoneyText(
                                                        money = cell.incomeTotal,
                                                        currencySpec = uiState.currencySpec,
                                                        grouping = uiState.numberGrouping,
                                                        includeSymbol = false,
                                                        colorMode = MoneyColorMode.INCOME,
                                                        style = MaterialTheme.typography.labelSmall
                                                    )
                                                }
                                                if (cell.expenseTotal.isPositive) {
                                                    MoneyText(
                                                        money = cell.expenseTotal,
                                                        currencySpec = uiState.currencySpec,
                                                        grouping = uiState.numberGrouping,
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
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selectedDateFormatted,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "${uiState.selectedCalendarDayTransactions.size} trans",
                                style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (uiState.selectedCalendarDayIncome.isPositive) {
                                MoneyText(
                                    money = uiState.selectedCalendarDayIncome,
                                    currencySpec = uiState.currencySpec,
                                    grouping = uiState.numberGrouping,
                                    colorMode = MoneyColorMode.INCOME,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    showPlusSignForPositive = true
                                )
                            }
                            if (uiState.selectedCalendarDayExpense.isPositive) {
                                MoneyText(
                                    money = uiState.selectedCalendarDayExpense,
                                    currencySpec = uiState.currencySpec,
                                    grouping = uiState.numberGrouping,
                                    colorMode = MoneyColorMode.EXPENSE,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    FilledTonalButton(
                        onClick = { onAddOnDate(uiState.selectedCalendarDayEpochDay) },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Add on this date",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        if (uiState.selectedCalendarDayTransactions.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No transactions recorded on this date. Tap \"+ Add on this date\" above to log an entry.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(20.dp)
                    )
                }
            }
        } else {
            items(
                items = uiState.selectedCalendarDayTransactions,
                key = { "cal_tx_${it.id}" }
            ) { tx ->
                LedgerTransactionItemCard(
                    transaction = tx,
                    scopedAccountId = uiState.scopedAccountId,
                    currencySpec = uiState.currencySpec,
                    grouping = uiState.numberGrouping,
                    onClick = { onTransactionClick(tx) }
                )
            }
        }
    }
}

@Composable
internal fun WeeklyLedgerSubTabContent(
    uiState: LedgerHomeUiState,
    onToggleWeek: (Int) -> Unit,
    onTransactionClick: (TransactionItem) -> Unit
) {
    val semanticColors = LocalFolioSemanticColors.current

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
                periodSummary = uiState.periodSummary,
                currencySpec = uiState.currencySpec,
                grouping = uiState.numberGrouping,
                showCarryOver = uiState.userPreferences.carryOverEnabled && uiState.scopedAccountId == null
            )
        }

        items(
            items = uiState.weeklyBuckets,
            key = { it.weekIndex },
            contentType = { "weekly_bucket_card" }
        ) { bucket ->
            val isExpanded = bucket.weekIndex in uiState.expandedWeekIndices

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleWeek(bucket.weekIndex) }
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isExpanded) {
                                    Icons.Filled.ExpandLess
                                } else {
                                    Icons.Filled.ExpandMore
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "${bucket.label} · ${bucket.dateRangeSubtitle}",
                                    style = tabularMoneyStyle(MaterialTheme.typography.titleSmall),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${bucket.transactions.size} transactions",
                                    style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                MoneyText(
                                    money = bucket.incomeTotal,
                                    currencySpec = uiState.currencySpec,
                                    grouping = uiState.numberGrouping,
                                    colorMode = MoneyColorMode.INCOME,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    showPlusSignForPositive = true
                                )
                                MoneyText(
                                    money = bucket.expenseTotal,
                                    currencySpec = uiState.currencySpec,
                                    grouping = uiState.numberGrouping,
                                    colorMode = MoneyColorMode.EXPENSE,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            MoneyText(
                                money = bucket.netTotal,
                                currencySpec = uiState.currencySpec,
                                grouping = uiState.numberGrouping,
                                colorMode = MoneyColorMode.AUTO_SIGN,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                showPlusSignForPositive = true
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = isExpanded && bucket.transactions.isNotEmpty(),
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 12.dp, end = 12.dp, bottom = 12.dp)
                        ) {
                            HorizontalDivider(color = semanticColors.cardStrokeSubtle)
                            bucket.transactions.forEach { tx ->
                                LedgerTransactionItemCard(
                                    transaction = tx,
                                    scopedAccountId = uiState.scopedAccountId,
                                    currencySpec = uiState.currencySpec,
                                    grouping = uiState.numberGrouping,
                                    onClick = { onTransactionClick(tx) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun MonthlyLedgerSubTabContent(
    uiState: LedgerHomeUiState,
    onToggleMonth: (Int) -> Unit
) {
    val semanticColors = LocalFolioSemanticColors.current

    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 12.dp,
            bottom = 96.dp
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            ThreeColumnSummaryBanner(
                periodSummary = uiState.periodSummary,
                currencySpec = uiState.currencySpec,
                grouping = uiState.numberGrouping,
                showCarryOver = uiState.userPreferences.carryOverEnabled && uiState.scopedAccountId == null
            )
        }

        item {
            Text(
                text = "${uiState.activePeriodRange.anchorYear} Financial Year (12 Months)",
                style = tabularMoneyStyle(MaterialTheme.typography.titleSmall),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        items(
            items = uiState.yearlyMonthItems.reversed(),
            key = { it.monthIndex },
            contentType = { "yearly_month_card" }
        ) { monthItem ->
            val isExpanded = monthItem.monthIndex in uiState.expandedYearlyMonths

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (monthItem.isCurrentActiveMonth) {
                        MaterialTheme.colorScheme.surfaceContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerLow
                    }
                ),
                border = BorderStroke(
                    width = if (monthItem.isCurrentActiveMonth) 1.5.dp else 1.dp,
                    color = if (monthItem.isCurrentActiveMonth) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        semanticColors.cardStrokeSubtle
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleMonth(monthItem.monthIndex) }
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isExpanded) {
                                    Icons.Filled.ExpandLess
                                } else {
                                    Icons.Filled.ExpandMore
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = monthItem.periodRange.title,
                                    style = tabularMoneyStyle(MaterialTheme.typography.titleMedium),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${monthItem.periodRange.subtitle} · ${monthItem.transactionCount} trans",
                                    style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                MoneyText(
                                    money = monthItem.incomeTotal,
                                    currencySpec = uiState.currencySpec,
                                    grouping = uiState.numberGrouping,
                                    colorMode = MoneyColorMode.INCOME,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                MoneyText(
                                    money = monthItem.expenseTotal,
                                    currencySpec = uiState.currencySpec,
                                    grouping = uiState.numberGrouping,
                                    colorMode = MoneyColorMode.EXPENSE,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            MoneyText(
                                money = monthItem.netTotal,
                                currencySpec = uiState.currencySpec,
                                grouping = uiState.numberGrouping,
                                colorMode = MoneyColorMode.AUTO_SIGN,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                showPlusSignForPositive = true
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 14.dp, end = 14.dp, bottom = 12.dp)
                        ) {
                            HorizontalDivider(color = semanticColors.cardStrokeSubtle)
                            monthItem.weeklyBuckets.forEach { week ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${week.label} (${week.dateRangeSubtitle})",
                                        style = tabularMoneyStyle(MaterialTheme.typography.labelMedium),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        MoneyText(
                                            money = week.incomeTotal,
                                            currencySpec = uiState.currencySpec,
                                            grouping = uiState.numberGrouping,
                                            colorMode = MoneyColorMode.INCOME,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                        MoneyText(
                                            money = week.expenseTotal,
                                            currencySpec = uiState.currencySpec,
                                            grouping = uiState.numberGrouping,
                                            colorMode = MoneyColorMode.EXPENSE,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                        MoneyText(
                                            money = week.netTotal,
                                            currencySpec = uiState.currencySpec,
                                            grouping = uiState.numberGrouping,
                                            colorMode = MoneyColorMode.AUTO_SIGN,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
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
internal fun SummaryLedgerSubTabContent(
    uiState: LedgerHomeUiState,
    onExportCsvClick: () -> Unit
) {
    val semanticColors = LocalFolioSemanticColors.current
    val summary = uiState.periodSummary
    val closingBalanceWithCarryOver = summary.netCashflow + summary.carryOverFromPreviousPeriod
    val overallBudget = uiState.budgetProgressList.firstOrNull { it.categoryId == null }
        ?: uiState.budgetProgressList.firstOrNull()

    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 12.dp,
            bottom = 96.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            ThreeColumnSummaryBanner(
                periodSummary = summary,
                currencySpec = uiState.currencySpec,
                grouping = uiState.numberGrouping,
                showCarryOver = uiState.userPreferences.carryOverEnabled && uiState.scopedAccountId == null
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
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "${uiState.activePeriodRange.title} Cashflow Digest",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        val savingsRateText = remember(uiState.savingsRatePercent) {
                            String.format(Locale.US, "Savings Rate: %.1f%%", uiState.savingsRatePercent)
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (uiState.savingsRatePercent >= 0f) {
                                semanticColors.incomeEmeraldContainer
                            } else {
                                semanticColors.expenseCoralContainer
                            }
                        ) {
                            Text(
                                text = savingsRateText,
                                style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.savingsRatePercent >= 0f) {
                                    semanticColors.onIncomeEmeraldContainer
                                } else {
                                    semanticColors.onExpenseCoralContainer
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = semanticColors.cardStrokeSubtle)

                    SummaryMetricRow(
                        label = "Total Period Income",
                        amount = summary.totalIncome,
                        colorMode = MoneyColorMode.INCOME,
                        currencySpec = uiState.currencySpec,
                        grouping = uiState.numberGrouping
                    )
                    SummaryMetricRow(
                        label = "Total Period Expenses",
                        amount = summary.totalExpense,
                        colorMode = MoneyColorMode.EXPENSE,
                        currencySpec = uiState.currencySpec,
                        grouping = uiState.numberGrouping
                    )
                    SummaryMetricRow(
                        label = "Net Period Cashflow",
                        amount = summary.netCashflow,
                        colorMode = MoneyColorMode.AUTO_SIGN,
                        currencySpec = uiState.currencySpec,
                        grouping = uiState.numberGrouping
                    )

                    if (uiState.userPreferences.carryOverEnabled && uiState.scopedAccountId == null) {
                        HorizontalDivider(color = semanticColors.cardStrokeSubtle)
                        SummaryMetricRow(
                            label = "Carry-Over from Previous Period",
                            amount = summary.carryOverFromPreviousPeriod,
                            colorMode = MoneyColorMode.AUTO_SIGN,
                            currencySpec = uiState.currencySpec,
                            grouping = uiState.numberGrouping
                        )
                        SummaryMetricRow(
                            label = "Closing Cumulative Balance",
                            amount = closingBalanceWithCarryOver,
                            colorMode = MoneyColorMode.AUTO_SIGN,
                            currencySpec = uiState.currencySpec,
                            grouping = uiState.numberGrouping
                        )
                    }
                }
            }
        }

        if (overallBudget != null && uiState.scopedAccountId == null) {
            item {
                val fraction = (overallBudget.usagePercentage / 100f).coerceIn(0f, 1f)
                val barColor = when {
                    overallBudget.usagePercentage >= 100f -> semanticColors.expenseCoral
                    overallBudget.usagePercentage >= overallBudget.alertThresholdPercent -> semanticColors.warningAmber
                    else -> semanticColors.incomeEmerald
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
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Budget Status (${overallBudget.categoryName})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = String.format(Locale.US, "%.1f%% used", overallBudget.usagePercentage),
                                style = tabularMoneyStyle(MaterialTheme.typography.labelMedium),
                                fontWeight = FontWeight.Bold,
                                color = barColor
                            )
                        }

                        LinearProgressIndicator(
                            progress = { fraction },
                            color = barColor,
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            strokeCap = StrokeCap.Round,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row {
                                Text(
                                    text = "Spent: ",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                MoneyText(
                                    money = overallBudget.spentAmount,
                                    currencySpec = uiState.currencySpec,
                                    grouping = uiState.numberGrouping,
                                    colorMode = MoneyColorMode.EXPENSE,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Row {
                                Text(
                                    text = "Remaining: ",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                MoneyText(
                                    money = overallBudget.remainingAmount,
                                    currencySpec = uiState.currencySpec,
                                    grouping = uiState.numberGrouping,
                                    colorMode = MoneyColorMode.AUTO_SIGN,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        if (uiState.topExpenseCategories.isNotEmpty() && uiState.scopedAccountId == null) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Top Expense Categories",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        uiState.topExpenseCategories.forEach { stat ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                CategoryIconBadge(
                                    iconName = stat.iconName,
                                    colorHex = stat.colorHex,
                                    size = 32.dp,
                                    iconSize = 16.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stat.categoryName,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = String.format(
                                            Locale.US,
                                            "%.1f%% · %d trans",
                                            stat.percentageOfTotal,
                                            stat.transactionCount
                                        ),
                                        style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                MoneyText(
                                    money = stat.netAmount,
                                    currencySpec = uiState.currencySpec,
                                    grouping = uiState.numberGrouping,
                                    colorMode = MoneyColorMode.EXPENSE,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            OutlinedButton(
                onClick = onExportCsvClick,
                enabled = !uiState.isExportingCsv,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.Download,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (uiState.isExportingCsv) {
                        "Exporting CSV..."
                    } else {
                        "Export ${uiState.activePeriodRange.title} Transactions to CSV"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
internal fun SummaryMetricRow(
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
            fontWeight = FontWeight.Bold,
            showPlusSignForPositive = colorMode == MoneyColorMode.AUTO_SIGN
        )
    }
}
