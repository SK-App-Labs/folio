// File: app/src/main/java/com/folioledger/app/ui/accounts/AccountsViewModel.kt
package com.folioledger.app.ui.accounts

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.folioledger.app.domain.model.AccountGroup
import com.folioledger.app.domain.model.AccountSummary
import com.folioledger.app.domain.model.CurrencySpec
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.NetWorthOverview
import com.folioledger.app.domain.model.NumberGroupingSystem
import com.folioledger.app.domain.model.SupportedCurrencies
import com.folioledger.app.domain.model.TransactionItem
import com.folioledger.app.domain.repository.AccountRepository
import com.folioledger.app.domain.repository.SettingsAndBackupRepository
import com.folioledger.app.domain.repository.TransactionRepository
import com.folioledger.app.domain.util.MoneyFormatterAndCalculator
import java.time.LocalDate
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
data class AccountEditorDialogState(
    val isEditing: Boolean = false,
    val accountId: Long = 0L,
    val name: String = "",
    val groupType: AccountGroup = AccountGroup.BANK_ACCOUNT,
    val currencyCode: String = "INR",
    val openingBalanceInput: String = "0",
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
    val currencySpec: CurrencySpec = SupportedCurrencies.INR,
    val numberGrouping: NumberGroupingSystem = NumberGroupingSystem.INDIAN,
    val referenceDateEpochDay: Long = LocalDate.now().toEpochDay(),
    val showArchivedAccounts: Boolean = false,
    val collapsedGroups: Set<AccountGroup> = emptySet(),
    val editorDialogState: AccountEditorDialogState? = null,
    val settlementSheetState: CreditCardSettlementSheetState? = null,
    val isSaving: Boolean = false,
    val snackbarMessage: String? = null
)

private data class AccountsInternalControlState(
    val referenceDateEpochDay: Long = LocalDate.now().toEpochDay(),
    val showArchivedAccounts: Boolean = false,
    val collapsedGroups: Set<AccountGroup> = emptySet(),
    val inspectedAccountId: Long? = null,
    val editorDialogState: AccountEditorDialogState? = null,
    val settlementSheetState: CreditCardSettlementSheetState? = null,
    val isSaving: Boolean = false,
    val snackbarMessage: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class AccountsViewModel(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val settingsAndBackupRepository: SettingsAndBackupRepository
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

    private val inspectedAccountTransactionsFlow = controlState
        .flatMapLatest { state ->
            val accountId = state.inspectedAccountId
            if (accountId != null && accountId > 0L) {
                transactionRepository.observeTransactionsForAccount(accountId)
            } else {
                MutableStateFlow(emptyList())
            }
        }

    val uiState: StateFlow<AccountsUiState> = combine(
        netWorthFlow,
        allAccountsFlow,
        settingsAndBackupRepository.observeUserPreferences(),
        inspectedAccountTransactionsFlow,
        controlState
    ) { netWorth, allAccounts, prefs, recentTxns, ctrl ->
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

        AccountsUiState(
            netWorthOverview = resolvedOverview,
            allAccounts = allAccounts,
            assetAccountsForSettlement = assetAccounts,
            parentBankCandidates = bankCandidates,
            recentTransactionsForSelectedAccount = recentTxns.take(10),
            inspectedAccountId = ctrl.inspectedAccountId,
            currencySpec = currencySpec,
            numberGrouping = prefs.numberGrouping,
            referenceDateEpochDay = ctrl.referenceDateEpochDay,
            showArchivedAccounts = ctrl.showArchivedAccounts,
            collapsedGroups = ctrl.collapsedGroups,
            editorDialogState = ctrl.editorDialogState,
            settlementSheetState = ctrl.settlementSheetState,
            isSaving = ctrl.isSaving,
            snackbarMessage = ctrl.snackbarMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
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

    fun selectAccountForInspection(accountId: Long?) {
        controlState.update { it.copy(inspectedAccountId = accountId) }
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

        controlState.update {
            it.copy(
                editorDialogState = AccountEditorDialogState(
                    isEditing = false,
                    accountId = 0L,
                    name = "",
                    groupType = defaultGroup,
                    currencyCode = currentState.currencySpec.code,
                    openingBalanceInput = "0",
                    creditLimitInput = if (defaultGroup == AccountGroup.CREDIT_CARD) "100000" else "",
                    statementPayableInput = "",
                    unbilledAmountInput = "",
                    settlementDayOfMonth = 18,
                    paymentDueDayOfMonth = 5,
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
                    creditLimitInput = limitFormatted,
                    statementPayableInput = payableFormatted,
                    unbilledAmountInput = unbilledFormatted,
                    settlementDayOfMonth = account.settlementDayOfMonth ?: 18,
                    paymentDueDayOfMonth = account.paymentDueDayOfMonth ?: 5,
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

        val signedBalance = when {
            editor.groupType == AccountGroup.DEBIT_CARD -> MinorMoney.ZERO
            editor.groupType.isLiabilityByDefault && parsedOpeningBalance.isPositive -> -parsedOpeningBalance
            else -> parsedOpeningBalance
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

        val statementPayable = if (editor.groupType == AccountGroup.CREDIT_CARD &&
            editor.statementPayableInput.isNotBlank()
        ) {
            parseInputToMinorOrZero(editor.statementPayableInput, fractionDigits)
        } else {
            null
        }

        val unbilledAmount = if (editor.groupType == AccountGroup.CREDIT_CARD &&
            editor.unbilledAmountInput.isNotBlank()
        ) {
            parseInputToMinorOrZero(editor.unbilledAmountInput, fractionDigits)
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
                editor.settlementDayOfMonth.coerceIn(1, 28)
            } else {
                null
            },
            paymentDueDayOfMonth = if (editor.groupType == AccountGroup.CREDIT_CARD) {
                editor.paymentDueDayOfMonth.coerceIn(1, 28)
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
                openingBalance = signedBalance
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
        val defaultSourceId = currentState.assetAccountsForSettlement.firstOrNull()?.id
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

    private fun parseInputToMinorOrZero(rawInput: String, fractionDigits: Int): MinorMoney? {
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

    private fun formatMinorPlainForInput(money: MinorMoney, fractionDigits: Int): String {
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

        fun provideFactory(
            accountRepository: AccountRepository,
            transactionRepository: TransactionRepository,
            settingsAndBackupRepository: SettingsAndBackupRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AccountsViewModel(
                    accountRepository = accountRepository,
                    transactionRepository = transactionRepository,
                    settingsAndBackupRepository = settingsAndBackupRepository
                ) as T
            }
        }
    }
}