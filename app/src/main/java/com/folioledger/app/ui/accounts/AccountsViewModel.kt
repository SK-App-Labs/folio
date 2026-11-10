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
import com.folioledger.app.domain.model.SecondaryCurrency
import com.folioledger.app.domain.model.SupportedCurrencies
import com.folioledger.app.domain.model.TransactionItem
import com.folioledger.app.domain.model.TransactionSearchFilter
import com.folioledger.app.domain.model.TransactionType
import com.folioledger.app.domain.model.UserPreferences
import com.folioledger.app.domain.repository.AccountRepository
import com.folioledger.app.domain.repository.SettingsAndBackupRepository
import com.folioledger.app.domain.repository.TransactionRepository
import com.folioledger.app.domain.util.MoneyFormatterAndCalculator
import com.folioledger.app.domain.util.AccountPassbookCalculator
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

typealias CreditCardCycleWindow = com.folioledger.app.domain.util.CreditCardCycleWindow
typealias CreditCardCycleCalculator = com.folioledger.app.domain.util.CreditCardCycleCalculator

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

    private val passbookTransactionsFlow = controlState
        .map { it.inspectedAccountId }
        .distinctUntilChanged()
        .flatMapLatest { inspectedId ->
            if (inspectedId == null) {
                flowOf(emptyList())
            } else {
                transactionRepository.searchAndFilterTransactions(TransactionSearchFilter())
            }
        }

    val uiState: StateFlow<AccountsUiState> = combine(
        netWorthFlow,
        allAccountsFlow,
        settingsAndBackupRepository.observeUserPreferences(),
        passbookTransactionsFlow,
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
            AccountPassbookCalculator.buildAccountPassbookUiModel(
                inspectedAccount = inspectedAccount,
                allAccounts = allAccounts,
                allTransactions = allTransactions,
                monthStartDay = prefs.monthStartDay,
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
            primaryCurrencyCode = prefs.primaryCurrencyCode,
            secondaryCurrencies = prefs.secondaryCurrencies,
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
    }.flowOn(Dispatchers.Default)
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = AccountsUiState()
    )

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

    fun toggleAccountPin(accountId: Long) {
        viewModelScope.launch {
            val result = accountRepository.toggleAccountPinned(accountId)
            if (result.isFailure) {
                controlState.update {
                    it.copy(snackbarMessage = "Failed to update pin: ${result.exceptionOrNull()?.message}")
                }
            }
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
        val isInvestment = account.groupType == AccountGroup.INVESTMENT
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
                    isLiability = isLiability,
                    isInvestment = isInvestment
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
                    isPinned = false,
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
                    isPinned = account.isPinned,
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
            isPinned = editor.isPinned,
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

    fun openSettlementSheetForAccountId(accountId: Long) {
        viewModelScope.launch {
            val account = uiState.value.allAccounts.firstOrNull { it.id == accountId }
                ?: allAccountsFlow.filter { it.isNotEmpty() }.first().firstOrNull { it.id == accountId }
                ?: return@launch
            openSettlementSheet(account)
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

    fun submitCreditCardSettlement(onSuccess: (() -> Unit)? = null) {
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
                    onSuccess?.invoke()
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
