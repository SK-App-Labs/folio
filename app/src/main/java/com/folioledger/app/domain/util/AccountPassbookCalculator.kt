package com.folioledger.app.domain.util

import com.folioledger.app.domain.model.AccountGroup
import com.folioledger.app.domain.model.AccountSummary
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.TransactionItem
import com.folioledger.app.domain.model.TransactionType
import com.folioledger.app.domain.usecase.AccountingRulesEngine
import com.folioledger.app.ui.accounts.AccountPassbookDayGroup
import com.folioledger.app.ui.accounts.AccountPassbookEntryUiModel
import com.folioledger.app.ui.accounts.AccountPassbookUiModel
import com.folioledger.app.ui.accounts.PassbookPeriodRange
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale

object PassbookPeriodCalculator {

    private val shortMonthYearFormatter = DateTimeFormatter.ofPattern("MMM yyyy", Locale.US)
    private val shortDayMonthFormatter = DateTimeFormatter.ofPattern("dd MMM", Locale.US)
    private val fullDayMonthYearFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.US)
    val fullDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy (EEE)", Locale.US)
    val monthYearFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM yyyy", Locale.US)

    fun resolveClampedDate(yearMonth: YearMonth, desiredDayOfMonth: Int): LocalDate {
        val safeDay = desiredDayOfMonth.coerceIn(1, 31)
        val clampedDay = minOf(safeDay, yearMonth.lengthOfMonth())
        return yearMonth.atDay(clampedDay)
    }

    fun computeFinancialMonthRange(
        referenceDateEpochDay: Long,
        monthStartDay: Int,
        isCreditCard: Boolean = false
    ): PassbookPeriodRange {
        val safeStartDay = monthStartDay.coerceIn(1, 31)
        val refDate = LocalDate.ofEpochDay(referenceDateEpochDay)
        val refYearMonth = YearMonth.from(refDate)

        val startDate: LocalDate
        val endDate: LocalDate

        if (safeStartDay == 1) {
            startDate = refYearMonth.atDay(1)
            endDate = refYearMonth.atEndOfMonth()
        } else {
            val startInRefMonth = resolveClampedDate(refYearMonth, safeStartDay)
            if (!refDate.isBefore(startInRefMonth)) {
                startDate = startInRefMonth
                val nextMonthStart = resolveClampedDate(refYearMonth.plusMonths(1), safeStartDay)
                endDate = nextMonthStart.minusDays(1)
            } else {
                startDate = resolveClampedDate(refYearMonth.minusMonths(1), safeStartDay)
                endDate = startInRefMonth.minusDays(1)
            }
        }

        val periodTitle = if (isCreditCard) {
            val today = LocalDate.now()
            if (!endDate.isBefore(today)) {
                "${endDate.format(shortMonthYearFormatter)} Cycle (Active)"
            } else {
                "${endDate.format(shortMonthYearFormatter)} Statement"
            }
        } else {
            startDate.format(shortMonthYearFormatter)
        }
        val periodSubtitle = "${startDate.format(shortDayMonthFormatter)} – ${endDate.format(fullDayMonthYearFormatter)}"

        return PassbookPeriodRange(
            startEpochDay = startDate.toEpochDay(),
            endEpochDay = endDate.toEpochDay(),
            periodTitle = periodTitle,
            periodSubtitle = periodSubtitle
        )
    }

    fun stepFinancialMonth(
        currentReferenceEpochDay: Long,
        monthStartDay: Int,
        deltaMonths: Int,
        isCreditCard: Boolean = false
    ): Long {
        if (deltaMonths == 0) return currentReferenceEpochDay
        val safeStartDay = monthStartDay.coerceIn(1, 31)
        val currentRange = computeFinancialMonthRange(currentReferenceEpochDay, safeStartDay, isCreditCard)
        val startLocalDate = LocalDate.ofEpochDay(currentRange.startEpochDay)
        val targetYearMonth = YearMonth.from(startLocalDate).plusMonths(deltaMonths.toLong())
        return resolveClampedDate(targetYearMonth, safeStartDay).toEpochDay()
    }
}

private data class ChronologicalPassbookItem(
    val transaction: TransactionItem,
    val deltaMinor: Long,
    val runningBalanceAfterMinor: Long,
    val viaLinkedDebitCardName: String?
)

object AccountPassbookCalculator {

    fun computeSignedDeltaMinorForAccount(
        tx: TransactionItem,
        effectiveAccountIds: Set<Long>
    ): Long = AccountingRulesEngine.computeSignedDeltaMinorForAccount(tx, effectiveAccountIds)

    fun buildAccountPassbookUiModel(
        inspectedAccount: AccountSummary,
        allAccounts: List<AccountSummary>,
        allTransactions: List<TransactionItem>,
        monthStartDay: Int,
        referenceDateEpochDay: Long,
        passbookMonthOffset: Int
    ): AccountPassbookUiModel {
        val isCreditCard = inspectedAccount.groupType == AccountGroup.CREDIT_CARD
        val safeMonthStartDay = if (isCreditCard) {
            val statementDay = (inspectedAccount.settlementDayOfMonth ?: 18).coerceIn(1, 31)
            if (statementDay >= 31) 1 else statementDay + 1
        } else {
            monthStartDay.coerceIn(1, 31)
        }
        val targetRefEpochDay = PassbookPeriodCalculator.stepFinancialMonth(
            currentReferenceEpochDay = referenceDateEpochDay,
            monthStartDay = safeMonthStartDay,
            deltaMonths = passbookMonthOffset,
            isCreditCard = isCreditCard
        )
        val periodRange = PassbookPeriodCalculator.computeFinancialMonthRange(
            referenceDateEpochDay = targetRefEpochDay,
            monthStartDay = safeMonthStartDay,
            isCreditCard = isCreditCard
        )

        val parentBankAccount = if (inspectedAccount.groupType == AccountGroup.DEBIT_CARD) {
            inspectedAccount.parentAccountId?.let { parentId ->
                allAccounts.firstOrNull { it.id == parentId }
            }
        } else {
            null
        }

        val childDebitCards = if (inspectedAccount.groupType != AccountGroup.DEBIT_CARD) {
            allAccounts.filter { candidate ->
                candidate.groupType == AccountGroup.DEBIT_CARD &&
                        candidate.parentAccountId == inspectedAccount.id
            }
        } else {
            emptyList()
        }
        val childDebitCardById = childDebitCards.associateBy { it.id }

        val effectiveAccountIds: Set<Long> = buildSet {
            add(inspectedAccount.id)
            if (inspectedAccount.groupType == AccountGroup.DEBIT_CARD) {
                inspectedAccount.parentAccountId?.let { add(it) }
            } else {
                childDebitCards.forEach { add(it.id) }
            }
        }

        val matchingChronological = allTransactions
            .filter { tx ->
                when (tx.type) {
                    TransactionType.EXPENSE,
                    TransactionType.INCOME,
                    TransactionType.REFUND,
                    TransactionType.OPENING_BALANCE,
                    TransactionType.RECONCILIATION -> tx.accountId in effectiveAccountIds
                    TransactionType.TRANSFER,
                    TransactionType.CC_SETTLEMENT -> tx.accountId in effectiveAccountIds ||
                            (tx.transferToAccountId != null && tx.transferToAccountId in effectiveAccountIds)
                }
            }
            .sortedWith(
                compareBy<TransactionItem> { it.ledgerDateEpochDay }
                    .thenBy { it.timestampEpochMillis }
                    .thenBy { it.id }
            )

        val deltasWithTx: List<Pair<TransactionItem, Long>> = matchingChronological.map { tx ->
            val deltaMinor = computeSignedDeltaMinorForAccount(
                tx = tx,
                effectiveAccountIds = effectiveAccountIds
            )
            tx to deltaMinor
        }

        val todayEpochDay = LocalDate.now().toEpochDay()
        val netDeltaUpToToday: Long = deltasWithTx
            .filter { (tx, _) -> tx.ledgerDateEpochDay <= todayEpochDay }
            .fold(0L) { sum, (_, delta) -> sum + delta }

        val anchorLiveBalanceMinor: Long = if (inspectedAccount.groupType == AccountGroup.DEBIT_CARD) {
            parentBankAccount?.currentBalance?.amountMinor ?: 0L
        } else {
            inspectedAccount.currentBalance.amountMinor
        }

        val initialOpeningBalanceMinor: Long = anchorLiveBalanceMinor - netDeltaUpToToday

        var runningBalanceMinor = initialOpeningBalanceMinor
        var periodOpeningBalanceMinor = initialOpeningBalanceMinor
        var periodInflowsMinor = 0L
        var periodOutflowsMinor = 0L

        val periodChronologicalItems = mutableListOf<ChronologicalPassbookItem>()

        for ((tx, deltaMinor) in deltasWithTx) {
            if (tx.ledgerDateEpochDay < periodRange.startEpochDay) {
                runningBalanceMinor += deltaMinor
                periodOpeningBalanceMinor = runningBalanceMinor
            } else if (tx.ledgerDateEpochDay in periodRange.startEpochDay..periodRange.endEpochDay) {
                runningBalanceMinor += deltaMinor
                if (deltaMinor > 0L) {
                    periodInflowsMinor += deltaMinor
                } else if (deltaMinor < 0L) {
                    periodOutflowsMinor += (-deltaMinor)
                }
                if (deltaMinor != 0L) {
                    val viaCardName = if (
                        inspectedAccount.groupType != AccountGroup.DEBIT_CARD &&
                        tx.accountId in childDebitCardById
                    ) {
                        childDebitCardById[tx.accountId]?.name
                    } else if (
                        inspectedAccount.groupType == AccountGroup.DEBIT_CARD &&
                        tx.accountId == inspectedAccount.id
                    ) {
                        inspectedAccount.name
                    } else {
                        null
                    }
                    periodChronologicalItems.add(
                        ChronologicalPassbookItem(
                            transaction = tx,
                            deltaMinor = deltaMinor,
                            runningBalanceAfterMinor = runningBalanceMinor,
                            viaLinkedDebitCardName = viaCardName
                        )
                    )
                }
            } else {
                runningBalanceMinor += deltaMinor
            }
        }

        val periodClosingBalanceMinor = periodOpeningBalanceMinor + periodInflowsMinor - periodOutflowsMinor

        val periodEntriesNewestFirst = periodChronologicalItems
            .asReversed()
            .map { item ->
                AccountPassbookEntryUiModel(
                    transaction = item.transaction,
                    signedDelta = MinorMoney(item.deltaMinor),
                    isInflow = item.deltaMinor >= 0L,
                    runningBalanceAfter = MinorMoney(item.runningBalanceAfterMinor),
                    viaLinkedDebitCardName = item.viaLinkedDebitCardName
                )
            }

        val fullDateFormatter = PassbookPeriodCalculator.fullDateFormatter
        val monthYearFormatter = PassbookPeriodCalculator.monthYearFormatter

        val dayGroups = periodEntriesNewestFirst
            .groupBy { it.transaction.ledgerDateEpochDay }
            .entries
            .sortedByDescending { it.key }
            .map { (epochDay, dayEntriesNewestFirst) ->
                val date = LocalDate.ofEpochDay(epochDay)
                var dayInflow = 0L
                var dayOutflow = 0L
                dayEntriesNewestFirst.forEach { entry ->
                    val d = entry.signedDelta.amountMinor
                    if (d > 0L) {
                        dayInflow += d
                    } else if (d < 0L) {
                        dayOutflow += (-d)
                    }
                }
                val dayEndBalance = dayEntriesNewestFirst.first().runningBalanceAfter
                val headerTitle = when (epochDay) {
                    todayEpochDay -> "Today, ${date.format(fullDateFormatter)}"
                    todayEpochDay - 1L -> "Yesterday, ${date.format(fullDateFormatter)}"
                    else -> date.format(fullDateFormatter)
                }

                AccountPassbookDayGroup(
                    epochDay = epochDay,
                    dateHeaderTitle = headerTitle,
                    dayOfMonthText = String.format(Locale.US, "%02d", date.dayOfMonth),
                    dayOfWeekText = date.dayOfWeek
                        .getDisplayName(JavaTextStyle.SHORT, Locale.US)
                        .uppercase(Locale.US),
                    monthYearText = date.format(monthYearFormatter),
                    isToday = epochDay == todayEpochDay,
                    dayInflowTotal = MinorMoney(dayInflow),
                    dayOutflowTotal = MinorMoney(dayOutflow),
                    dayEndRunningBalance = dayEndBalance,
                    entries = dayEntriesNewestFirst
                )
            }

        return AccountPassbookUiModel(
            account = inspectedAccount,
            parentBankAccount = parentBankAccount,
            linkedDebitCards = childDebitCards,
            monthOffset = passbookMonthOffset,
            startEpochDay = periodRange.startEpochDay,
            endEpochDay = periodRange.endEpochDay,
            periodTitle = periodRange.periodTitle,
            periodSubtitle = periodRange.periodSubtitle,
            currentLiveBalance = MinorMoney(anchorLiveBalanceMinor),
            periodOpeningBalance = MinorMoney(periodOpeningBalanceMinor),
            periodInflows = MinorMoney(periodInflowsMinor),
            periodOutflows = MinorMoney(periodOutflowsMinor),
            periodNetFlow = MinorMoney(periodInflowsMinor - periodOutflowsMinor),
            periodClosingBalance = MinorMoney(periodClosingBalanceMinor),
            totalPeriodTransactionCount = periodEntriesNewestFirst.size,
            dayGroups = dayGroups,
            entries = periodEntriesNewestFirst,
            investedPrincipal = inspectedAccount.investedPrincipal,
            unrealizedPnl = inspectedAccount.unrealizedPnl,
            returnPercentage = inspectedAccount.returnPercentage
        )
    }
}
