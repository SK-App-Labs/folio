package com.folioledger.app.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class FinancialPeriodRange(
    val startEpochDay: Long,
    val endEpochDay: Long,
    val title: String,
    val subtitle: String,
    val anchorYear: Int,
    val anchorMonthValue: Int
)

@Immutable
data class DailyLedgerGroup(
    val epochDay: Long,
    val dayOfMonthText: String,
    val dayOfWeekText: String,
    val monthYearText: String,
    val isToday: Boolean,
    val dailyIncome: MinorMoney = MinorMoney.ZERO,
    val dailyExpense: MinorMoney = MinorMoney.ZERO,
    val dailyNet: MinorMoney = MinorMoney.ZERO,
    val dailyTransferTotal: MinorMoney = MinorMoney.ZERO,
    val transferCount: Int = 0,
    val transactions: List<TransactionItem> = emptyList()
)

@Immutable
data class CalendarDayCell(
    val epochDay: Long?,
    val dayNumber: Int?,
    val dateLabel: String = "",
    val isInActivePeriod: Boolean = false,
    val isToday: Boolean = false,
    val incomeTotal: MinorMoney = MinorMoney.ZERO,
    val expenseTotal: MinorMoney = MinorMoney.ZERO,
    val netTotal: MinorMoney = MinorMoney.ZERO,
    val transferTotal: MinorMoney = MinorMoney.ZERO,
    val transferCount: Int = 0,
    val transactionCount: Int = 0,
    val formattedCompactIncome: String = "",
    val formattedCompactExpense: String = "",
    val formattedCompactTransfer: String = ""
)

@Immutable
data class WeeklyLedgerBucket(
    val weekIndex: Int,
    val label: String,
    val dateRangeSubtitle: String,
    val startEpochDay: Long,
    val endEpochDay: Long,
    val incomeTotal: MinorMoney,
    val expenseTotal: MinorMoney,
    val netTotal: MinorMoney,
    val transactions: List<TransactionItem>
)

@Immutable
data class YearlyMonthOverviewItem(
    val monthIndex: Int,
    val periodRange: FinancialPeriodRange,
    val isCurrentActiveMonth: Boolean,
    val incomeTotal: MinorMoney,
    val expenseTotal: MinorMoney,
    val netTotal: MinorMoney,
    val transactionCount: Int,
    val weeklyBuckets: List<WeeklyLedgerBucket>
)

@Immutable
data class AccountPeriodFlowSummary(
    val accountId: Long,
    val accountName: String,
    val currencyCode: String,
    val incomeTotal: MinorMoney,
    val expenseTotal: MinorMoney,
    val netChange: MinorMoney,
    val transactionCount: Int
)
