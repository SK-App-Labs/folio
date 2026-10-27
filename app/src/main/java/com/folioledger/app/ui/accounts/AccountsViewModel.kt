// File: app/src/main/java/com/folioledger/app/ui/accounts/AccountsViewModel.kt
package com.folioledger.app.ui.accounts

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.folioledger.app.domain.model.AccountGroup
import com.folioledger.app.domain.model.AccountSummary
import com.folioledger.app.domain.model.CurrencySpec
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.NetWorthOverview
import com.folioledger.app.domain.model.NumberGroupingSystem
import com.folioledger.app.domain.model.SupportedCurrencies
import com.folioledger.app.domain.model.TransactionItem
import com.folioledger.app.domain.model.TransactionSearchFilter
import com.folioledger.app.domain.model.TransactionType
import com.folioledger.app.domain.model.UserPreferences
import com.folioledger.app.domain.repository.AccountRepository
import com.folioledger.app.domain.repository.SettingsAndBackupRepository
import com.folioledger.app.domain.repository.TransactionRepository
import com.folioledger.app.domain.util.MoneyFormatterAndCalculator
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class CreditCardCycleWindow(
    val statementDayOfMonth: Int,
    val paymentDueDayOfMonth: Int,
    val lastStatementStart: LocalDate,
    val lastStatementEnd: LocalDate,
    val lastStatementDueDate: LocalDate,
    val activeCycleStart: LocalDate,
    val activeCycleEnd: LocalDate,
    val activeCycleNextDueDate: LocalDate
) {
    private val shortFormatter: DateTimeFormatter
        get() = DateTimeFormatter.ofPattern("dd MMM", Locale.US)

    private val fullFormatter: DateTimeFormatter
        get() = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.US)

    val lastStatementRangeShortText: String
        get() = "${lastStatementStart.format(shortFormatter)} – ${lastStatementEnd.format(shortFormatter)}"

    val lastStatementRangeFullText: String
        get() = "${lastStatementStart.format(fullFormatter)} – ${lastStatementEnd.format(fullFormatter)}"

    val lastStatementDueShortText: String
        get() = lastStatementDueDate.format(shortFormatter)

    val lastStatementDueFullText: String
        get() = lastStatementDueDate.format(fullFormatter)

    val activeCycleRangeShortText: String
        get() = "${activeCycleStart.format(shortFormatter)} – ${activeCycleEnd.format(shortFormatter)}"

    val activeCycleRangeFullText: String
        get() = "${activeCycleStart.format(fullFormatter)} – ${activeCycleEnd.format(fullFormatter)}"

    @Suppress("unused")
    val activeCycleNextDueFullText: String
        get() = activeCycleNextDueDate.format(fullFormatter)
}

object CreditCardCycleCalculator {

    fun resolveClampedDate(yearMonth: YearMonth, desiredDayOfMonth: Int): LocalDate {
        val safeDay = desiredDayOfMonth.coerceIn(1, 31)
        val clampedDay = minOf(safeDay, yearMonth.lengthOfMonth())
        return yearMonth.atDay(clampedDay)
    }

    fun computeCycleWindow(
        referenceDateEpochDay: Long,
        settlementDayOfMonth: Int,
        paymentDueDayOfMonth: Int
    ): CreditCardCycleWindow {
        val safeStatementDay = settlementDayOfMonth.coerceIn(1, 31)
        val safeDueDay = paymentDueDayOfMonth.coerceIn(1, 31)
        val refDate = LocalDate.ofEpochDay(referenceDateEpochDay)
        val refYearMonth = YearMonth.from(refDate)
        val statementDateInRefMonth = resolveClampedDate(refYearMonth, safeStatementDay)

        val lastStatementEnd: LocalDate = if (refDate.isAfter(statementDateInRefMonth)) {
            statementDateInRefMonth
        } else {
            resolveClampedDate(refYearMonth.minusMonths(1), safeStatementDay)
        }

        val prevStatementEnd = resolveClampedDate(
            YearMonth.from(lastStatementEnd).minusMonths(1),
            safeStatementDay
        )
        val lastStatementStart = prevStatementEnd.plusDays(1)

        val activeCycleStart = lastStatementEnd.plusDays(1)
        val activeCycleEnd = resolveClampedDate(
            YearMonth.from(lastStatementEnd).plusMonths(1),
            safeStatementDay
        )

        val lastStatementDueDate = computeDueDateAfterStatementClose(
            statementCloseDate = lastStatementEnd,
            dueDayOfMonth = safeDueDay
        )
        val activeCycleNextDueDate = computeDueDateAfterStatementClose(
            statementCloseDate = activeCycleEnd,
            dueDayOfMonth = safeDueDay
        )

        return CreditCardCycleWindow(
            statementDayOfMonth = safeStatementDay,
            paymentDueDayOfMonth = safeDueDay,
            lastStatementStart = lastStatementStart,
            lastStatementEnd = lastStatementEnd,
            lastStatementDueDate = lastStatementDueDate,
            activeCycleStart = activeCycleStart,
            activeCycleEnd = activeCycleEnd,
            activeCycleNextDueDate = activeCycleNextDueDate
        )
    }

    private fun computeDueDateAfterStatementClose(
        statementCloseDate: LocalDate,
        dueDayOfMonth: Int
    ): LocalDate {
        val closeYearMonth = YearMonth.from(statementCloseDate)
        val sameMonthCandidate = resolveClampedDate(closeYearMonth, dueDayOfMonth)
        return if (sameMonthCandidate.isAfter(statementCloseDate)) {
            sameMonthCandidate
        } else {
            resolveClampedDate(closeYearMonth.plusMonths(1), dueDayOfMonth)
        }
    }
}

@Immutable
data class PassbookPeriodRange(
    val startEpochDay: Long,
    val endEpochDay: Long,
    val periodTitle: String,
    val periodSubtitle: String
)

private object PassbookPeriodCalculator {

    private val shortMonthYearFormatter = DateTimeFormatter.ofPattern("MMM yyyy", Locale.US)
    private val shortDayMonthFormatter = DateTimeFormatter.ofPattern("dd MMM", Locale.US)
    private val fullDayMonthYearFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.US)

    private fun resolveClampedDate(yearMonth: YearMonth, desiredDayOfMonth: Int): LocalDate {
        val safeDay = desiredDayOfMonth.coerceIn(1, 31)
        val clampedDay = minOf(safeDay, yearMonth.lengthOfMonth())
        return yearMonth.atDay(clampedDay)
    }

    fun computeFinancialMonthRange(
        referenceDateEpochDay: Long,
        monthStartDay: Int
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

        val periodTitle = startDate.format(shortMonthYearFormatter)
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
        deltaMonths: Int
    ): Long {
        if (deltaMonths == 0) return currentReferenceEpochDay
        val safeStartDay = monthStartDay.coerceIn(1, 31)
        val currentRange = computeFinancialMonthRange(currentReferenceEpochDay, safeStartDay)
        val startLocalDate = LocalDate.ofEpochDay(currentRange.startEpochDay)
        val targetYearMonth = YearMonth.from(startLocalDate).plusMonths(deltaMonths.toLong())
        return resolveClampedDate(targetYearMonth, safeStartDay).toEpochDay()
    }
}

@Immutable
data class AccountPassbookEntryUiModel(
    val transaction: TransactionItem,
    val signedDelta: MinorMoney,
    val isInflow: Boolean,
    val runningBalanceAfter: MinorMoney,
    val viaLinkedDebitCardName: String? = null
)

@Immutable
data class AccountPassbookDayGroup(
    val epochDay: Long,
    val dateHeaderTitle: String,
    val dayOfMonthText: String,
    val dayOfWeekText: String,
    val monthYearText: String,
    val isToday: Boolean,
    val dayInflowTotal: MinorMoney,
    val dayOutflowTotal: MinorMoney,
    val dayEndRunningBalance: MinorMoney,
    val entries: List<AccountPassbookEntryUiModel>
)

@Immutable
data class AccountPassbookUiModel(
    val account: AccountSummary,
    val parentBankAccount: AccountSummary? = null,
    val linkedDebitCards: List<AccountSummary> = emptyList(),
    val monthOffset: Int = 0,
    val startEpochDay: Long = 0L,
    val endEpochDay: Long = 0L,
    val periodTitle: String = "",
    val periodSubtitle: String = "",
    val currentLiveBalance: MinorMoney = MinorMoney.ZERO,
    val periodOpeningBalance: MinorMoney = MinorMoney.ZERO,
    val periodInflows: MinorMoney = MinorMoney.ZERO,
    val periodOutflows: MinorMoney = MinorMoney.ZERO,
    val periodNetFlow: MinorMoney = MinorMoney.ZERO,
    val periodClosingBalance: MinorMoney = MinorMoney.ZERO,
    val totalPeriodTransactionCount: Int = 0,
    val dayGroups: List<AccountPassbookDayGroup> = emptyList(),
    val entries: List<AccountPassbookEntryUiModel> = emptyList()
)

@Immutable
data class AccountEditorDialogState(
    val isEditing: Boolean = false,
    val accountId: Long = 0L,
    val name: String = "",
    val groupType: AccountGroup = AccountGroup.BANK_ACCOUNT,
    val currencyCode: String = "INR",
    val openingBalanceInput: String = "0",
    val openingDateEpochDay: Long = LocalDate.now().toEpochDay(),
    val creditLimitInput: String = "",
    val statementPayableInput: String = "",
    val unbilledAmountInput: String = "",
    val settlementDayOfMonth: Int = 18,
    val paymentDueDayOfMonth: Int = 5,
    val parentAccountId: Long? = null,
    val includeInNetWorth: Boolean = true,
    val isArchived: Boolean = false,
    val colorHex: String = "#059669",
    val iconName: String = "account_balance",
    val validationError: String? = null
)

enum class SettlementAmountPreset {
    STATEMENT_PAYABLE,
    FULL_OUTSTANDING,
    CUSTOM
}

@Immutable
data class CreditCardSettlementSheetState(
    val creditCardAccount: AccountSummary,
    val selectedSourceAccountId: Long?,
    val preset: SettlementAmountPreset = SettlementAmountPreset.STATEMENT_PAYABLE,
    val amountInput: String = "",
    val ledgerDateEpochDay: Long = LocalDate.now().toEpochDay(),
    val note: String = "Credit Card Statement Settlement",
    val validationError: String? = null
)

@Immutable
data class ReconcileBalanceDialogState(
    val accountId: Long,
    val accountName: String,
    val currencyCode: String,
    val recordedBalance: MinorMoney,
    val targetBalanceInput: String = "",
    val reconcileDateEpochDay: Long = LocalDate.now().toEpochDay(),
    val note: String = "",
    val validationError: String? = null,
    val isLiability: Boolean = false
)

@Immutable
data class AccountsUiState(
    val netWorthOverview: NetWorthOverview = NetWorthOverview(
        totalAssets = MinorMoney.ZERO,
        totalLiabilities = MinorMoney.ZERO,
        netWorth = MinorMoney.ZERO,
        accountsByGroup = emptyMap()
    ),
    val allAccounts: List<AccountSummary> = emptyList(),
    val assetAccountsForSettlement: List<AccountSummary> = emptyList(),
    val parentBankCandidates: List<AccountSummary> = emptyList(),
    val recentTransactionsForSelectedAccount: List<TransactionItem> = emptyList(),
    val inspectedAccountId: Long? = null,
    val inspectedAccount: AccountSummary? = null,
    val passbookMonthOffset: Int = 0,
    val accountPassbook: AccountPassbookUiModel? = null,
    val currencySpec: CurrencySpec = SupportedCurrencies.INR,
    val numberGrouping: NumberGroupingSystem = NumberGroupingSystem.INDIAN,
    val referenceDateEpochDay: Long = LocalDate.now().toEpochDay(),
    val showArchivedAccounts: Boolean = false,
    val collapsedGroups: Set<AccountGroup> = emptySet(),
    val editorDialogState: AccountEditorDialogState? = null,
    val settlementSheetState: CreditCardSettlementSheetState? = null,
    val reconcileDialogState: ReconcileBalanceDialogState? = null,
    val pendingDeletionAccountId: Long? = null,
    val pendingDeletionAccountName: String? = null,
    val pendingDeletionTxCount: Int = 0,
    val pendingDeletionLinkedCardsCount: Int = 0,
    val isSaving: Boolean = false,
    val snackbarMessage: String? = null
)

private data class AccountsInternalControlState(
    val referenceDateEpochDay: Long = LocalDate.now().toEpochDay(),
    val showArchivedAccounts: Boolean = false,
    val collapsedGroups: Set<AccountGroup> = emptySet(),
    val inspectedAccountId: Long? = null,
    val passbookMonthOffset: Int = 0,
    val editorDialogState: AccountEditorDialogState? = null,
    val settlementSheetState: CreditCardSettlementSheetState? = null,
    val reconcileDialogState: ReconcileBalanceDialogState? = null,
    val pendingDeletionAccountId: Long? = null,
    val pendingDeletionAccountName: String? = null,
    val pendingDeletionTxCount: Int = 0,
    val pendingDeletionLinkedCardsCount: Int = 0,
    val isSaving: Boolean = false,
    val snackbarMessage: String? = null
)

private data class ChronologicalPassbookItem(
    val transaction: TransactionItem,
    val deltaMinor: Long,
    val runningBalanceAfterMinor: Long,
    val viaLinkedDebitCardName: String?
)

@OptIn(ExperimentalCoroutinesApi::class)
class AccountsViewModel(
    private val accountRepository: AccountRepository,
    transactionRepository: TransactionRepository,
    settingsAndBackupRepository: SettingsAndBackupRepository
) : ViewModel() {

    private val controlState = MutableStateFlow(AccountsInternalControlState())

    private val netWorthFlow = controlState
        .flatMapLatest { state ->
            accountRepository.observeNetWorthOverview(state.referenceDateEpochDay)
        }

    private val allAccountsFlow = controlState
        .flatMapLatest { state ->
            accountRepository.observeAllAccounts(state.referenceDateEpochDay)
        }

    private val allTransactionsFlow = transactionRepository
        .searchAndFilterTransactions(TransactionSearchFilter())

    val uiState: StateFlow<AccountsUiState> = combine(
        netWorthFlow,
        allAccountsFlow,
        settingsAndBackupRepository.observeUserPreferences(),
        allTransactionsFlow,
        controlState
    ) { netWorth, allAccounts, prefs, allTransactions, ctrl ->
        val currencySpec = SupportedCurrencies.fromCode(prefs.primaryCurrencyCode)
        val visibleAccounts = if (ctrl.showArchivedAccounts) {
            allAccounts
        } else {
            allAccounts.filter { !it.isArchived }
        }

        val groupedAccounts = visibleAccounts.groupBy { it.groupType }
        val resolvedOverview = netWorth.copy(accountsByGroup = groupedAccounts)

        val assetAccounts = allAccounts.filter { acc ->
            !acc.isArchived && (
                    acc.groupType == AccountGroup.BANK_ACCOUNT ||
                            acc.groupType == AccountGroup.CASH ||
                            acc.groupType == AccountGroup.SAVINGS ||
                            acc.groupType == AccountGroup.PREPAID_WALLET
                    )
        }

        val bankCandidates = allAccounts.filter { acc ->
            !acc.isArchived && (
                    acc.groupType == AccountGroup.BANK_ACCOUNT ||
                            acc.groupType == AccountGroup.SAVINGS ||
                            acc.groupType == AccountGroup.OVERDRAFT
                    )
        }

        val inspectedAccount = ctrl.inspectedAccountId?.let { targetId ->
            allAccounts.firstOrNull { it.id == targetId }
        }

        val passbookModel = if (inspectedAccount != null) {
            buildAccountPassbookUiModel(
                inspectedAccount = inspectedAccount,
                allAccounts = allAccounts,
                allTransactions = allTransactions,
                prefs = prefs,
                referenceDateEpochDay = ctrl.referenceDateEpochDay,
                passbookMonthOffset = ctrl.passbookMonthOffset
            )
        } else {
            null
        }

        val periodTxnsNewestFirst = passbookModel?.entries?.map { it.transaction }.orEmpty()

        AccountsUiState(
            netWorthOverview = resolvedOverview,
            allAccounts = allAccounts,
            assetAccountsForSettlement = assetAccounts,
            parentBankCandidates = bankCandidates,
            recentTransactionsForSelectedAccount = periodTxnsNewestFirst,
            inspectedAccountId = ctrl.inspectedAccountId,
            inspectedAccount = inspectedAccount,
            passbookMonthOffset = ctrl.passbookMonthOffset,
            accountPassbook = passbookModel,
            currencySpec = currencySpec,
            numberGrouping = prefs.numberGrouping,
            referenceDateEpochDay = ctrl.referenceDateEpochDay,
            showArchivedAccounts = ctrl.showArchivedAccounts,
            collapsedGroups = ctrl.collapsedGroups,
            editorDialogState = ctrl.editorDialogState,
            settlementSheetState = ctrl.settlementSheetState,
            reconcileDialogState = ctrl.reconcileDialogState,
            pendingDeletionAccountId = ctrl.pendingDeletionAccountId,
            pendingDeletionAccountName = ctrl.pendingDeletionAccountName,
            pendingDeletionTxCount = ctrl.pendingDeletionTxCount,
            pendingDeletionLinkedCardsCount = ctrl.pendingDeletionLinkedCardsCount,
            isSaving = ctrl.isSaving,
            snackbarMessage = ctrl.snackbarMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = AccountsUiState()
    )

    private fun buildAccountPassbookUiModel(
        inspectedAccount: AccountSummary,
        allAccounts: List<AccountSummary>,
        allTransactions: List<TransactionItem>,
        prefs: UserPreferences,
        referenceDateEpochDay: Long,
        passbookMonthOffset: Int
    ): AccountPassbookUiModel {
        val safeMonthStartDay = prefs.monthStartDay.coerceIn(1, 31)
        val targetRefEpochDay = PassbookPeriodCalculator.stepFinancialMonth(
            currentReferenceEpochDay = referenceDateEpochDay,
            monthStartDay = safeMonthStartDay,
            deltaMonths = passbookMonthOffset
        )
        val periodRange = PassbookPeriodCalculator.computeFinancialMonthRange(
            referenceDateEpochDay = targetRefEpochDay,
            monthStartDay = safeMonthStartDay
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

        val fullDateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy (EEE)", Locale.US)
        val monthYearFormatter = DateTimeFormatter.ofPattern("MMM yyyy", Locale.US)

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
            entries = periodEntriesNewestFirst
        )
    }

    private fun computeSignedDeltaMinorForAccount(
        tx: TransactionItem,
        effectiveAccountIds: Set<Long>
    ): Long {
        val amt = tx.amount.amountMinor.coerceAtLeast(0L)
        val fee = tx.transferFee.amountMinor.coerceAtLeast(0L)
        val isSource = tx.accountId in effectiveAccountIds
        val isTarget = tx.transferToAccountId != null && tx.transferToAccountId in effectiveAccountIds

        return when (tx.type) {
            TransactionType.INCOME,
            TransactionType.REFUND -> if (isSource) amt else 0L

            TransactionType.EXPENSE -> if (isSource) -(amt + fee) else 0L

            TransactionType.TRANSFER,
            TransactionType.CC_SETTLEMENT -> when {
                isSource && isTarget -> -fee
                isSource -> -(amt + fee)
                isTarget -> amt
                else -> 0L
            }

            TransactionType.OPENING_BALANCE,
            TransactionType.RECONCILIATION -> {
                val isOutflow = tx.transferToAccountId != null
                if (isSource) (if (isOutflow) -amt else amt) else 0L
            }
        }
    }

    fun toggleGroupCollapsed(group: AccountGroup) {
        controlState.update { current ->
            val next = if (group in current.collapsedGroups) {
                current.collapsedGroups - group
            } else {
                current.collapsedGroups + group
            }
            current.copy(collapsedGroups = next)
        }
    }

    fun toggleShowArchivedAccounts() {
        controlState.update { current ->
            current.copy(showArchivedAccounts = !current.showArchivedAccounts)
        }
    }

    fun selectAccountForInspection(accountId: Long?) {
        controlState.update {
            it.copy(
                inspectedAccountId = accountId,
                passbookMonthOffset = 0
            )
        }
    }

    fun openReconcileDialog(account: AccountSummary) {
        val spec = SupportedCurrencies.fromCode(account.currencyCode)
        val isLiability = account.groupType.isLiabilityByDefault
        val initialAmount = if (isLiability) account.currentBalance.abs() else account.currentBalance
        val initialInput = formatMinorPlainForInput(initialAmount, spec.fractionDigits)
        controlState.update {
            it.copy(
                reconcileDialogState = ReconcileBalanceDialogState(
                    accountId = account.id,
                    accountName = account.name,
                    currencyCode = account.currencyCode,
                    recordedBalance = account.currentBalance,
                    targetBalanceInput = initialInput,
                    reconcileDateEpochDay = LocalDate.now().toEpochDay(),
                    note = "",
                    isLiability = isLiability
                )
            )
        }
    }

    fun updateReconcileDialogState(transform: (ReconcileBalanceDialogState) -> ReconcileBalanceDialogState) {
        controlState.update { current ->
            val existing = current.reconcileDialogState ?: return@update current
            current.copy(reconcileDialogState = transform(existing).copy(validationError = null))
        }
    }

    fun dismissReconcileDialog() {
        controlState.update { it.copy(reconcileDialogState = null) }
    }

    fun submitReconcileDialog() {
        val dialog = controlState.value.reconcileDialogState ?: return
        val spec = SupportedCurrencies.fromCode(dialog.currencyCode)
        val parsed = parseInputToMinorOrZero(dialog.targetBalanceInput, spec.fractionDigits)
        if (parsed == null) {
            updateReconcileDialogState { it.copy(validationError = "Invalid target balance.") }
            return
        }

        val targetBalance = if (dialog.isLiability) {
            parsed.abs().unaryMinus()
        } else {
            parsed
        }

        viewModelScope.launch {
            controlState.update { it.copy(isSaving = true) }
            val result = accountRepository.reconcileAccountBalance(
                accountId = dialog.accountId,
                reconciledBalance = targetBalance,
                reconciledDateEpochDay = dialog.reconcileDateEpochDay,
                note = dialog.note
            )
            controlState.update {
                it.copy(
                    isSaving = false,
                    reconcileDialogState = null,
                    snackbarMessage = if (result.isSuccess) "Account balance reconciled successfully." else "Failed to reconcile balance: ${result.exceptionOrNull()?.message}"
                )
            }
        }
    }

    fun dismissInspectedAccountSheet() {
        controlState.update {
            it.copy(
                inspectedAccountId = null,
                passbookMonthOffset = 0
            )
        }
    }

    fun stepPassbookMonth(deltaMonths: Int) {
        controlState.update { current ->
            current.copy(passbookMonthOffset = current.passbookMonthOffset + deltaMonths)
        }
    }

    fun resetPassbookToCurrentMonth() {
        controlState.update { current ->
            current.copy(passbookMonthOffset = 0)
        }
    }

    fun clearSnackbarMessage() {
        controlState.update { it.copy(snackbarMessage = null) }
    }

    fun openCreateAccountDialog(defaultGroup: AccountGroup = AccountGroup.BANK_ACCOUNT) {
        val currentState = uiState.value
        val defaultIcon = defaultIconForGroup(defaultGroup)
        val defaultColor = defaultColorForGroup(defaultGroup)
        val defaultParentId = if (defaultGroup == AccountGroup.DEBIT_CARD) {
            currentState.parentBankCandidates.firstOrNull()?.id
        } else {
            null
        }

        val defaultStatementDay = 18
        val defaultDueDay = 5
        val initialAnchorEpochDay = if (defaultGroup == AccountGroup.CREDIT_CARD) {
            CreditCardCycleCalculator.computeCycleWindow(
                referenceDateEpochDay = currentState.referenceDateEpochDay,
                settlementDayOfMonth = defaultStatementDay,
                paymentDueDayOfMonth = defaultDueDay
            ).lastStatementEnd.toEpochDay()
        } else {
            LocalDate.now().toEpochDay()
        }

        controlState.update {
            it.copy(
                editorDialogState = AccountEditorDialogState(
                    isEditing = false,
                    accountId = 0L,
                    name = "",
                    groupType = defaultGroup,
                    currencyCode = currentState.currencySpec.code,
                    openingBalanceInput = "0",
                    openingDateEpochDay = initialAnchorEpochDay,
                    creditLimitInput = if (defaultGroup == AccountGroup.CREDIT_CARD) "100000" else "",
                    statementPayableInput = "",
                    unbilledAmountInput = "",
                    settlementDayOfMonth = defaultStatementDay,
                    paymentDueDayOfMonth = defaultDueDay,
                    parentAccountId = defaultParentId,
                    includeInNetWorth = true,
                    isArchived = false,
                    colorHex = defaultColor,
                    iconName = defaultIcon,
                    validationError = null
                )
            )
        }
    }

    fun openEditAccountDialog(account: AccountSummary) {
        val spec = SupportedCurrencies.fromCode(account.currencyCode)
        val balanceFormatted = formatMinorPlainForInput(account.currentBalance.abs(), spec.fractionDigits)
        val limitFormatted = account.creditLimit?.let {
            formatMinorPlainForInput(it.abs(), spec.fractionDigits)
        } ?: ""
        val payableFormatted = account.creditCardStatementPayable?.let {
            formatMinorPlainForInput(it.abs(), spec.fractionDigits)
        } ?: ""
        val unbilledFormatted = account.creditCardUnbilledAmount?.let {
            formatMinorPlainForInput(it.abs(), spec.fractionDigits)
        } ?: ""

        controlState.update {
            it.copy(
                editorDialogState = AccountEditorDialogState(
                    isEditing = true,
                    accountId = account.id,
                    name = account.name,
                    groupType = account.groupType,
                    currencyCode = account.currencyCode,
                    openingBalanceInput = balanceFormatted,
                    openingDateEpochDay = LocalDate.now().toEpochDay(),
                    creditLimitInput = limitFormatted,
                    statementPayableInput = payableFormatted,
                    unbilledAmountInput = unbilledFormatted,
                    settlementDayOfMonth = (account.settlementDayOfMonth ?: 18).coerceIn(1, 31),
                    paymentDueDayOfMonth = (account.paymentDueDayOfMonth ?: 5).coerceIn(1, 31),
                    parentAccountId = account.parentAccountId,
                    includeInNetWorth = account.includeInNetWorth,
                    isArchived = account.isArchived,
                    colorHex = account.colorHex,
                    iconName = account.iconName,
                    validationError = null
                )
            )
        }
    }

    fun updateEditorDialogState(transform: (AccountEditorDialogState) -> AccountEditorDialogState) {
        controlState.update { current ->
            val existing = current.editorDialogState ?: return@update current
            current.copy(editorDialogState = transform(existing).copy(validationError = null))
        }
    }

    fun dismissAccountEditorDialog() {
        controlState.update { it.copy(editorDialogState = null) }
    }

    fun requestAccountDeletion(accountId: Long) {
        viewModelScope.launch {
            val txCount = accountRepository.getTransactionCountForAccount(accountId)
            val all = uiState.value.allAccounts
            val account = all.firstOrNull { it.id == accountId }
            val linkedCardsCount = all.count { it.parentAccountId == accountId }
            controlState.update {
                it.copy(
                    editorDialogState = null,
                    pendingDeletionAccountId = accountId,
                    pendingDeletionAccountName = account?.name ?: "Account #$accountId",
                    pendingDeletionTxCount = txCount,
                    pendingDeletionLinkedCardsCount = linkedCardsCount
                )
            }
        }
    }

    fun confirmDeleteAccount(accountId: Long, deleteTransactions: Boolean) {
        viewModelScope.launch {
            controlState.update { it.copy(isSaving = true) }
            val result = accountRepository.deleteAccount(accountId, deleteTransactions)
            controlState.update { current ->
                current.copy(
                    isSaving = false,
                    editorDialogState = null,
                    inspectedAccountId = if (current.inspectedAccountId == accountId) null else current.inspectedAccountId,
                    pendingDeletionAccountId = null,
                    pendingDeletionAccountName = null,
                    pendingDeletionTxCount = 0,
                    pendingDeletionLinkedCardsCount = 0,
                    snackbarMessage = if (result.isSuccess) "Account deleted successfully." else "Failed to delete account: ${result.exceptionOrNull()?.message}"
                )
            }
        }
    }

    fun cancelPendingDeletion() {
        controlState.update {
            it.copy(
                pendingDeletionAccountId = null,
                pendingDeletionAccountName = null,
                pendingDeletionTxCount = 0,
                pendingDeletionLinkedCardsCount = 0
            )
        }
    }

    fun saveAccountFromDialog() {
        val editor = controlState.value.editorDialogState ?: return
        val trimmedName = editor.name.trim()
        if (trimmedName.isEmpty()) {
            updateEditorDialogState { it.copy(validationError = "Account name is required.") }
            return
        }

        val spec = SupportedCurrencies.fromCode(editor.currencyCode)
        val fractionDigits = spec.fractionDigits

        if (editor.groupType == AccountGroup.DEBIT_CARD && editor.parentAccountId == null) {
            updateEditorDialogState {
                it.copy(validationError = "Please link this Debit Card to a Parent Bank Account.")
            }
            return
        }

        val statementPayable = if (editor.groupType == AccountGroup.CREDIT_CARD &&
            !editor.isEditing && editor.statementPayableInput.isNotBlank()
        ) {
            val parsed = parseInputToMinorOrZero(editor.statementPayableInput, fractionDigits)
            if (parsed == null) {
                updateEditorDialogState {
                    it.copy(validationError = "Invalid Billed Statement Due amount.")
                }
                return
            }
            parsed.abs()
        } else {
            null
        }

        val unbilledAmount = if (editor.groupType == AccountGroup.CREDIT_CARD &&
            !editor.isEditing && editor.unbilledAmountInput.isNotBlank()
        ) {
            val parsed = parseInputToMinorOrZero(editor.unbilledAmountInput, fractionDigits)
            if (parsed == null) {
                updateEditorDialogState {
                    it.copy(validationError = "Invalid Unbilled Spend amount.")
                }
                return
            }
            parsed.abs()
        } else {
            null
        }

        val parsedOpeningBalance = parseInputToMinorOrZero(
            rawInput = editor.openingBalanceInput,
            fractionDigits = fractionDigits
        )
        if (parsedOpeningBalance == null) {
            updateEditorDialogState {
                it.copy(validationError = "Invalid balance amount.")
            }
            return
        }

        val effectiveOpeningMagnitude = if (editor.groupType == AccountGroup.CREDIT_CARD) {
            if (editor.isEditing) parsedOpeningBalance else (statementPayable ?: MinorMoney.ZERO) + (unbilledAmount ?: MinorMoney.ZERO)
        } else {
            parsedOpeningBalance
        }

        val signedBalance = when {
            editor.groupType == AccountGroup.DEBIT_CARD -> MinorMoney.ZERO
            editor.groupType.isLiabilityByDefault && effectiveOpeningMagnitude.isPositive -> -effectiveOpeningMagnitude
            else -> effectiveOpeningMagnitude
        }

        val creditLimit = if (editor.groupType == AccountGroup.CREDIT_CARD) {
            val parsedLimit = parseInputToMinorOrZero(editor.creditLimitInput, fractionDigits)
            if (parsedLimit == null || !parsedLimit.isPositive) {
                updateEditorDialogState {
                    it.copy(validationError = "Enter a valid positive Credit Limit for this Credit Card.")
                }
                return
            }
            parsedLimit
        } else {
            null
        }

        val accountSummary = AccountSummary(
            id = editor.accountId,
            name = trimmedName,
            groupType = editor.groupType,
            currencyCode = spec.code,
            currentBalance = signedBalance,
            creditLimit = creditLimit,
            creditCardStatementPayable = statementPayable,
            creditCardUnbilledAmount = unbilledAmount,
            settlementDayOfMonth = if (editor.groupType == AccountGroup.CREDIT_CARD) {
                editor.settlementDayOfMonth.coerceIn(1, 31)
            } else {
                null
            },
            paymentDueDayOfMonth = if (editor.groupType == AccountGroup.CREDIT_CARD) {
                editor.paymentDueDayOfMonth.coerceIn(1, 31)
            } else {
                null
            },
            parentAccountId = if (editor.groupType == AccountGroup.DEBIT_CARD) {
                editor.parentAccountId
            } else {
                null
            },
            debitCardPeriodSpend = if (editor.groupType == AccountGroup.DEBIT_CARD) {
                MinorMoney.ZERO
            } else {
                null
            },
            includeInNetWorth = editor.includeInNetWorth,
            isArchived = editor.isArchived,
            colorHex = editor.colorHex,
            iconName = editor.iconName
        )

        viewModelScope.launch {
            controlState.update { it.copy(isSaving = true) }
            val result = accountRepository.upsertAccount(
                account = accountSummary,
                openingBalance = signedBalance,
                openingDateEpochDay = editor.openingDateEpochDay
            )
            result.fold(
                onSuccess = {
                    controlState.update {
                        it.copy(
                            isSaving = false,
                            editorDialogState = null,
                            snackbarMessage = if (editor.isEditing) {
                                "Updated account \"${accountSummary.name}\""
                            } else {
                                "Created account \"${accountSummary.name}\""
                            }
                        )
                    }
                },
                onFailure = { error ->
                    controlState.update { state ->
                        state.copy(
                            isSaving = false,
                            editorDialogState = state.editorDialogState?.copy(
                                validationError = error.message ?: "Failed to save account."
                            )
                        )
                    }
                }
            )
        }
    }

    fun setAccountArchived(accountId: Long, isArchived: Boolean) {
        viewModelScope.launch {
            val result = accountRepository.setAccountArchived(accountId, isArchived)
            result.fold(
                onSuccess = {
                    controlState.update {
                        it.copy(
                            editorDialogState = null,
                            snackbarMessage = if (isArchived) {
                                "Account archived"
                            } else {
                                "Account restored from archive"
                            }
                        )
                    }
                },
                onFailure = { err ->
                    controlState.update {
                        it.copy(snackbarMessage = err.message ?: "Unable to update archive status")
                    }
                }
            )
        }
    }

    fun openSettlementSheet(creditCard: AccountSummary) {
        val currentState = uiState.value
        val eligibleSource = currentState.assetAccountsForSettlement.firstOrNull {
            it.currencyCode == creditCard.currencyCode
        }
        val defaultSourceId = eligibleSource?.id
        val spec = SupportedCurrencies.fromCode(creditCard.currencyCode)

        val statementDue = creditCard.creditCardStatementPayable ?: MinorMoney.ZERO
        val totalDebt = creditCard.currentBalance.abs()
        val initialPreset = if (statementDue.isPositive) {
            SettlementAmountPreset.STATEMENT_PAYABLE
        } else {
            SettlementAmountPreset.FULL_OUTSTANDING
        }
        val initialMinor = if (statementDue.isPositive) statementDue else totalDebt

        controlState.update {
            it.copy(
                settlementSheetState = CreditCardSettlementSheetState(
                    creditCardAccount = creditCard,
                    selectedSourceAccountId = defaultSourceId,
                    preset = initialPreset,
                    amountInput = formatMinorPlainForInput(initialMinor, spec.fractionDigits),
                    ledgerDateEpochDay = currentState.referenceDateEpochDay,
                    note = "CC Bill Settlement - ${creditCard.name}",
                    validationError = null
                )
            )
        }
    }

    fun updateSettlementSheetState(
        transform: (CreditCardSettlementSheetState) -> CreditCardSettlementSheetState
    ) {
        controlState.update { current ->
            val existing = current.settlementSheetState ?: return@update current
            current.copy(settlementSheetState = transform(existing).copy(validationError = null))
        }
    }

    fun selectSettlementPreset(preset: SettlementAmountPreset) {
        val sheet = controlState.value.settlementSheetState ?: return
        val card = sheet.creditCardAccount
        val spec = SupportedCurrencies.fromCode(card.currencyCode)
        val newAmountInput = when (preset) {
            SettlementAmountPreset.STATEMENT_PAYABLE -> {
                val payable = card.creditCardStatementPayable ?: MinorMoney.ZERO
                formatMinorPlainForInput(payable.abs(), spec.fractionDigits)
            }
            SettlementAmountPreset.FULL_OUTSTANDING -> {
                formatMinorPlainForInput(card.currentBalance.abs(), spec.fractionDigits)
            }
            SettlementAmountPreset.CUSTOM -> sheet.amountInput
        }
        updateSettlementSheetState {
            it.copy(preset = preset, amountInput = newAmountInput)
        }
    }

    fun dismissSettlementSheet() {
        controlState.update { it.copy(settlementSheetState = null) }
    }

    fun submitCreditCardSettlement() {
        val sheet = controlState.value.settlementSheetState ?: return
        val sourceId = sheet.selectedSourceAccountId
        if (sourceId == null) {
            updateSettlementSheetState {
                it.copy(validationError = "Select a source Bank or Cash account to pay from.")
            }
            return
        }

        val sourceAccount = uiState.value.allAccounts.firstOrNull { it.id == sourceId }
        if (sourceAccount != null && sourceAccount.currencyCode != sheet.creditCardAccount.currencyCode) {
            updateSettlementSheetState {
                it.copy(validationError = "Source account currency (${sourceAccount.currencyCode}) must match credit card currency (${sheet.creditCardAccount.currencyCode}).")
            }
            return
        }

        val spec = SupportedCurrencies.fromCode(sheet.creditCardAccount.currencyCode)
        val evaluatedResult = MoneyFormatterAndCalculator.evaluateKeypadExpressionToMinor(
            expression = sheet.amountInput,
            fractionDigits = spec.fractionDigits
        )

        val settlementMoney = evaluatedResult.getOrNull()
        if (settlementMoney == null || !settlementMoney.isPositive) {
            updateSettlementSheetState {
                it.copy(validationError = "Enter a valid positive settlement amount.")
            }
            return
        }

        viewModelScope.launch {
            controlState.update { it.copy(isSaving = true) }
            val result = accountRepository.settleCreditCardStatement(
                sourceAssetAccountId = sourceId,
                creditCardAccountId = sheet.creditCardAccount.id,
                settlementAmount = settlementMoney,
                timestampEpochMillis = System.currentTimeMillis(),
                ledgerDateEpochDay = sheet.ledgerDateEpochDay,
                note = sheet.note.trim()
            )
            result.fold(
                onSuccess = {
                    val formatted = MoneyFormatterAndCalculator.formatMinorMoney(
                        money = settlementMoney,
                        currencySpec = spec
                    )
                    controlState.update {
                        it.copy(
                            isSaving = false,
                            settlementSheetState = null,
                            snackbarMessage = "Settled $formatted towards ${sheet.creditCardAccount.name}"
                        )
                    }
                },
                onFailure = { error ->
                    controlState.update { state ->
                        state.copy(
                            isSaving = false,
                            settlementSheetState = state.settlementSheetState?.copy(
                                validationError = error.message ?: "Failed to record settlement."
                            )
                        )
                    }
                }
            )
        }
    }



    companion object {
        fun defaultIconForGroup(group: AccountGroup): String = when (group) {
            AccountGroup.CASH -> "account_balance_wallet"
            AccountGroup.BANK_ACCOUNT -> "account_balance"
            AccountGroup.DEBIT_CARD -> "credit_card"
            AccountGroup.CREDIT_CARD -> "credit_card"
            AccountGroup.SAVINGS -> "savings"
            AccountGroup.PREPAID_WALLET -> "account_balance_wallet"
            AccountGroup.INVESTMENT -> "trending_up"
            AccountGroup.OVERDRAFT -> "account_balance"
            AccountGroup.LOAN -> "trending_down"
            AccountGroup.OTHER -> "payments"
        }

        fun defaultColorForGroup(group: AccountGroup): String = when (group) {
            AccountGroup.CASH -> "#D97706"
            AccountGroup.BANK_ACCOUNT -> "#059669"
            AccountGroup.DEBIT_CARD -> "#0D9488"
            AccountGroup.CREDIT_CARD -> "#E11D48"
            AccountGroup.SAVINGS -> "#10B981"
            AccountGroup.PREPAID_WALLET -> "#4F46E5"
            AccountGroup.INVESTMENT -> "#2563EB"
            AccountGroup.OVERDRAFT -> "#EA580C"
            AccountGroup.LOAN -> "#BE123C"
            AccountGroup.OTHER -> "#475569"
        }
    }
}

internal fun parseInputToMinorOrZero(rawInput: String, fractionDigits: Int): MinorMoney? {
    val cleaned = rawInput.trim()
    if (cleaned.isEmpty() || cleaned == "0" || cleaned == "0.0" || cleaned == "0.00") {
        return MinorMoney.ZERO
    }
    val isNegative = cleaned.startsWith("-")
    val unsignedExpression = cleaned.removePrefix("-").removePrefix("+").trim()
    if (unsignedExpression.isEmpty()) return MinorMoney.ZERO

    return MoneyFormatterAndCalculator.evaluateKeypadExpressionToMinor(
        expression = unsignedExpression,
        fractionDigits = fractionDigits
    ).getOrNull()?.let { minor ->
        if (isNegative) -minor else minor
    }
}

internal fun formatMinorPlainForInput(money: MinorMoney, fractionDigits: Int): String {
    if (money.isZero) return "0"
    val formatted = MoneyFormatterAndCalculator.formatMinorMoney(
        money = money.abs(),
        currencySpec = CurrencySpec(
            code = "TMP",
            symbol = "",
            displayName = "",
            fractionDigits = fractionDigits,
            defaultGrouping = NumberGroupingSystem.INTERNATIONAL
        ),
        includeSymbol = false
    ).replace(",", "")
    return if (formatted.contains('.')) {
        formatted.trimEnd('0').trimEnd('.')
    } else {
        formatted
    }
}
