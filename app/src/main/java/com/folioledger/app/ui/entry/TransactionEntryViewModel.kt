// File: app/src/main/java/com/folioledger/app/ui/entry/TransactionEntryViewModel.kt
package com.folioledger.app.ui.entry

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.folioledger.app.domain.model.AccountGroup
import com.folioledger.app.domain.model.AccountSummary
import com.folioledger.app.domain.model.CategoryNode
import com.folioledger.app.domain.model.CategoryType
import com.folioledger.app.domain.model.CurrencySpec
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.NumberGroupingSystem
import com.folioledger.app.domain.model.SplitLineItem
import com.folioledger.app.domain.model.SupportedCurrencies
import com.folioledger.app.domain.model.TransactionBookmark
import com.folioledger.app.domain.model.TransactionItem
import com.folioledger.app.domain.model.TransactionType
import com.folioledger.app.domain.repository.AccountRepository
import com.folioledger.app.domain.repository.CategoryAndTagRepository
import com.folioledger.app.domain.repository.SettingsAndBackupRepository
import com.folioledger.app.domain.repository.TransactionRepository
import com.folioledger.app.domain.util.MoneyFormatterAndCalculator
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class InstallmentPreviewItem(
    val installmentIndex: Int,
    val totalInstallments: Int,
    val dueDateEpochDay: Long,
    val amount: MinorMoney,
)

@Immutable
data class SplitValidationSummary(
    val isValid: Boolean,
    val allocatedTotal: MinorMoney,
    val remainingUnallocated: MinorMoney,
    val errorMessage: String? = null
)

object AccountingRulesEngine {

    fun validateSplitTransaction(
        parentTotal: MinorMoney,
        splits: List<SplitLineItem>
    ): SplitValidationSummary {
        if (!parentTotal.isPositive) {
            return SplitValidationSummary(
                isValid = false,
                allocatedTotal = MinorMoney.ZERO,
                remainingUnallocated = parentTotal,
                errorMessage = "Enter a positive transaction total before splitting."
            )
        }
        if (splits.size < 2) {
            val allocated = splits.fold(MinorMoney.ZERO) { acc, item -> acc + item.amount }
            return SplitValidationSummary(
                isValid = false,
                allocatedTotal = allocated,
                remainingUnallocated = parentTotal - allocated,
                errorMessage = "A split transaction requires at least 2 split rows."
            )
        }

        var allocatedMinor = 0L
        splits.forEach { line ->
            if (line.categoryId <= 0L) {
                return SplitValidationSummary(
                    isValid = false,
                    allocatedTotal = MinorMoney(allocatedMinor),
                    remainingUnallocated = MinorMoney(parentTotal.amountMinor - allocatedMinor),
                    errorMessage = "Every split line must have a category selected."
                )
            }
            if (!line.amount.isPositive) {
                return SplitValidationSummary(
                    isValid = false,
                    allocatedTotal = MinorMoney(allocatedMinor),
                    remainingUnallocated = MinorMoney(parentTotal.amountMinor - allocatedMinor),
                    errorMessage = "Every split line amount must be strictly greater than zero."
                )
            }
            allocatedMinor = Math.addExact(allocatedMinor, line.amount.amountMinor)
        }

        val allocated = MinorMoney(allocatedMinor)
        val remaining = parentTotal - allocated
        return if (remaining.isZero) {
            SplitValidationSummary(
                isValid = true,
                allocatedTotal = allocated,
                remainingUnallocated = MinorMoney.ZERO,
                errorMessage = null
            )
        } else {
            SplitValidationSummary(
                isValid = false,
                allocatedTotal = allocated,
                remainingUnallocated = remaining,
                errorMessage = if (remaining.isPositive) {
                    "Split lines are under-allocated."
                } else {
                    "Split lines exceed the transaction total."
                }
            )
        }
    }

    fun generateInstallmentSchedule(
        totalAmount: MinorMoney,
        monthsCount: Int,
        startDateEpochDay: Long
    ): List<InstallmentPreviewItem> {
        val safeMonths = monthsCount.coerceIn(2, 60)
        if (!totalAmount.isPositive) return emptyList()

        val baseMinor = totalAmount.amountMinor / safeMonths
        val remainderMinor = totalAmount.amountMinor % safeMonths
        val startLocalDate = LocalDate.ofEpochDay(startDateEpochDay)

        return (1..safeMonths).map { index ->
            val installmentMinor = if (index == 1) {
                baseMinor + remainderMinor
            } else {
                baseMinor
            }
            val installmentDate = startLocalDate.plusMonths((index - 1).toLong())
            InstallmentPreviewItem(
                installmentIndex = index,
                totalInstallments = safeMonths,
                dueDateEpochDay = installmentDate.toEpochDay(),
                amount = MinorMoney(installmentMinor)
            )
        }
    }
}

@Immutable
data class SplitLineDraft(
    val localId: Long,
    val categoryId: Long = 0L,
    val categoryName: String = "Select Category",
    val categoryColorHex: String = "#059669",
    val categoryIconName: String = "category",
    val amountInput: String = "",
    val evaluatedAmount: MinorMoney = MinorMoney.ZERO,
    val note: String = ""
)

@Immutable
data class TransactionEntryUiState(
    val editingTransactionId: Long = 0L,
    val mode: TransactionType = TransactionType.EXPENSE,
    val ledgerDateEpochDay: Long = LocalDate.now().toEpochDay(),
    val timestampEpochMillis: Long = System.currentTimeMillis(),
    val expressionBuffer: String = "0",
    val evaluatedAmount: MinorMoney = MinorMoney.ZERO,
    val expressionHasOperator: Boolean = false,
    val activeAccounts: List<AccountSummary> = emptyList(),
    val selectedAccount: AccountSummary? = null,
    val transferToAccount: AccountSummary? = null,
    val transferFeeInput: String = "",
    val expenseCategories: List<CategoryNode> = emptyList(),
    val incomeCategories: List<CategoryNode> = emptyList(),
    val selectedCategoryId: Long? = null,
    val selectedCategoryName: String? = null,
    val selectedParentCategoryName: String? = null,
    val selectedCategoryColorHex: String? = null,
    val selectedCategoryIconName: String? = null,
    val isSplit: Boolean = false,
    val splitDrafts: List<SplitLineDraft> = emptyList(),
    val splitValidation: SplitValidationSummary = SplitValidationSummary(
        isValid = false,
        allocatedTotal = MinorMoney.ZERO,
        remainingUnallocated = MinorMoney.ZERO
    ),
    val isInstallmentEnabled: Boolean = false,
    val installmentMonthsCount: Int = 3,
    val installmentSchedulePreview: List<InstallmentPreviewItem> = emptyList(),
    val payeeOrDescription: String = "",
    val note: String = "",
    val selectedTags: List<String> = emptyList(),
    val availableTags: List<String> = emptyList(),
    val bookmarks: List<TransactionBookmark> = emptyList(),
    val currencySpec: CurrencySpec = SupportedCurrencies.INR,
    val numberGrouping: NumberGroupingSystem = NumberGroupingSystem.INDIAN,
    val subcategoriesEnabled: Boolean = true,
    val isKeypadVisible: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val savedSuccessfullyCounter: Int = 0
) {
    val activeCategoryTree: List<CategoryNode>
        get() = when (mode) {
            TransactionType.INCOME -> incomeCategories
            TransactionType.EXPENSE, TransactionType.REFUND,
            TransactionType.TRANSFER, TransactionType.CC_SETTLEMENT -> expenseCategories
        }

    val isCreditCardExpense: Boolean
        get() = mode == TransactionType.EXPENSE &&
                selectedAccount?.groupType == AccountGroup.CREDIT_CARD
}

private data class EntryFormControlState(
    val editingTransactionId: Long = 0L,
    val mode: TransactionType = TransactionType.EXPENSE,
    val ledgerDateEpochDay: Long = LocalDate.now().toEpochDay(),
    val timestampEpochMillis: Long = System.currentTimeMillis(),
    val expressionBuffer: String = "0",
    val selectedAccountId: Long? = null,
    val transferToAccountId: Long? = null,
    val transferFeeInput: String = "",
    val selectedCategoryId: Long? = null,
    val selectedCategoryName: String? = null,
    val selectedParentCategoryName: String? = null,
    val selectedCategoryColorHex: String? = null,
    val selectedCategoryIconName: String? = null,
    val isSplit: Boolean = false,
    val splitDrafts: List<SplitLineDraft> = emptyList(),
    val isInstallmentEnabled: Boolean = false,
    val installmentMonthsCount: Int = 3,
    val payeeOrDescription: String = "",
    val note: String = "",
    val selectedTags: List<String> = emptyList(),
    val isKeypadVisible: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val savedSuccessfullyCounter: Int = 0
)

class TransactionEntryViewModel(
    private val transactionRepository: TransactionRepository,
    accountRepository: AccountRepository,
    private val categoryAndTagRepository: CategoryAndTagRepository,
    settingsAndBackupRepository: SettingsAndBackupRepository
) : ViewModel() {

    private var nextSplitLocalId = 1L
    private var lastUsedLedgerDateEpochDay: Long = LocalDate.now().toEpochDay()
    private val formState = MutableStateFlow(EntryFormControlState())

    private val referenceDataFlow = combine(
        accountRepository.observeActiveAccounts(LocalDate.now().toEpochDay()),
        categoryAndTagRepository.observeCategoryTree(CategoryType.EXPENSE),
        categoryAndTagRepository.observeCategoryTree(CategoryType.INCOME),
        categoryAndTagRepository.observeAllTags(),
        categoryAndTagRepository.observeBookmarks()
    ) { accounts, expenseCats, incomeCats, tags, bookmarks ->
        ReferenceBundle(
            accounts = accounts,
            expenseCategories = expenseCats,
            incomeCategories = incomeCats,
            tags = tags,
            bookmarks = bookmarks
        )
    }

    val uiState: StateFlow<TransactionEntryUiState> = combine(
        formState,
        referenceDataFlow,
        settingsAndBackupRepository.observeUserPreferences()
    ) { form, refs, prefs ->
        val selectedAccount = refs.accounts.firstOrNull { it.id == form.selectedAccountId }
            ?: refs.accounts.firstOrNull()
        val transferToAccount = refs.accounts.firstOrNull { it.id == form.transferToAccountId }
            ?: refs.accounts.firstOrNull { it.id != selectedAccount?.id }

        val currencySpec = SupportedCurrencies.fromCode(
            selectedAccount?.currencyCode ?: prefs.primaryCurrencyCode
        )
        val evaluatedMoney = evaluateBufferSafely(
            rawExpression = form.expressionBuffer,
            fractionDigits = currencySpec.fractionDigits
        )

        val treeForMode = if (form.mode == TransactionType.INCOME) {
            refs.incomeCategories
        } else {
            refs.expenseCategories
        }
        val defaultCategory = treeForMode.firstOrNull()

        val resolvedCategoryId = form.selectedCategoryId ?: defaultCategory?.id
        val resolvedCategoryNode = findCategoryNodeById(treeForMode, resolvedCategoryId)
        val resolvedParentName = findParentCategoryName(treeForMode, resolvedCategoryId)

        val resolvedSplits = form.splitDrafts.map { draft ->
            val lineMoney = evaluateBufferSafely(draft.amountInput, currencySpec.fractionDigits)
            draft.copy(evaluatedAmount = lineMoney)
        }

        val domainSplits = resolvedSplits.map { draft ->
            SplitLineItem(
                id = draft.localId,
                categoryId = draft.categoryId,
                categoryName = draft.categoryName,
                amount = draft.evaluatedAmount,
                note = draft.note
            )
        }

        val splitValidation = AccountingRulesEngine.validateSplitTransaction(
            parentTotal = evaluatedMoney,
            splits = domainSplits
        )

        val isCCExpense = form.mode == TransactionType.EXPENSE &&
                selectedAccount?.groupType == AccountGroup.CREDIT_CARD
        val effectiveInstallmentEnabled = form.isInstallmentEnabled && isCCExpense && !form.isSplit

        val schedulePreview = if (effectiveInstallmentEnabled) {
            AccountingRulesEngine.generateInstallmentSchedule(
                totalAmount = evaluatedMoney,
                monthsCount = form.installmentMonthsCount,
                startDateEpochDay = form.ledgerDateEpochDay
            )
        } else {
            emptyList()
        }

        val hasOperator = form.expressionBuffer.drop(1).any { ch ->
            ch == '+' || ch == '-' || ch == '×' || ch == '÷' || ch == '*' || ch == '/'
        }

        TransactionEntryUiState(
            editingTransactionId = form.editingTransactionId,
            mode = form.mode,
            ledgerDateEpochDay = form.ledgerDateEpochDay,
            timestampEpochMillis = form.timestampEpochMillis,
            expressionBuffer = form.expressionBuffer,
            evaluatedAmount = evaluatedMoney,
            expressionHasOperator = hasOperator,
            activeAccounts = refs.accounts,
            selectedAccount = selectedAccount,
            transferToAccount = transferToAccount,
            transferFeeInput = form.transferFeeInput,
            expenseCategories = refs.expenseCategories,
            incomeCategories = refs.incomeCategories,
            selectedCategoryId = resolvedCategoryNode?.id ?: form.selectedCategoryId,
            selectedCategoryName = resolvedCategoryNode?.name ?: form.selectedCategoryName,
            selectedParentCategoryName = resolvedParentName ?: form.selectedParentCategoryName,
            selectedCategoryColorHex = resolvedCategoryNode?.colorHex ?: form.selectedCategoryColorHex,
            selectedCategoryIconName = resolvedCategoryNode?.iconName ?: form.selectedCategoryIconName,
            isSplit = form.isSplit && (form.mode == TransactionType.EXPENSE || form.mode == TransactionType.INCOME),
            splitDrafts = resolvedSplits,
            splitValidation = splitValidation,
            isInstallmentEnabled = effectiveInstallmentEnabled,
            installmentMonthsCount = form.installmentMonthsCount,
            installmentSchedulePreview = schedulePreview,
            payeeOrDescription = form.payeeOrDescription,
            note = form.note,
            selectedTags = form.selectedTags,
            availableTags = refs.tags,
            bookmarks = refs.bookmarks,
            currencySpec = currencySpec,
            numberGrouping = prefs.numberGrouping,
            subcategoriesEnabled = prefs.subcategoriesEnabled,
            isKeypadVisible = form.isKeypadVisible,
            isSaving = form.isSaving,
            errorMessage = form.errorMessage,
            savedSuccessfullyCounter = form.savedSuccessfullyCounter
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = TransactionEntryUiState()
    )

    fun resetForNewTransaction(
        defaultMode: TransactionType = TransactionType.EXPENSE,
        preselectedAccountId: Long? = null,
        initialEpochDay: Long? = null
    ) {
        val targetEpochDay = initialEpochDay ?: lastUsedLedgerDateEpochDay
        lastUsedLedgerDateEpochDay = targetEpochDay
        formState.update { current ->
            EntryFormControlState(
                editingTransactionId = 0L,
                mode = defaultMode,
                ledgerDateEpochDay = targetEpochDay,
                timestampEpochMillis = System.currentTimeMillis(),
                expressionBuffer = "0",
                selectedAccountId = preselectedAccountId ?: current.selectedAccountId,
                isKeypadVisible = true,
                savedSuccessfullyCounter = current.savedSuccessfullyCounter
            )
        }
    }

    fun loadTransactionItemForEdit(tx: TransactionItem) {
        val spec = SupportedCurrencies.fromCode(tx.currencyCode)
        val plainAmount = formatMinorPlain(tx.amount.abs(), spec.fractionDigits)
        val draftSplits = tx.splits.map { split ->
            SplitLineDraft(
                localId = nextSplitLocalId++,
                categoryId = split.categoryId,
                categoryName = split.categoryName.ifBlank { "Category" },
                amountInput = formatMinorPlain(split.amount.abs(), spec.fractionDigits),
                evaluatedAmount = split.amount.abs(),
                note = split.note
            )
        }
        lastUsedLedgerDateEpochDay = tx.ledgerDateEpochDay

        formState.update { current ->
            current.copy(
                editingTransactionId = tx.id,
                mode = tx.type,
                ledgerDateEpochDay = tx.ledgerDateEpochDay,
                timestampEpochMillis = tx.timestampEpochMillis,
                expressionBuffer = plainAmount,
                selectedAccountId = tx.accountId,
                transferToAccountId = tx.transferToAccountId,
                transferFeeInput = if (tx.transferFee.isPositive) {
                    formatMinorPlain(tx.transferFee, spec.fractionDigits)
                } else {
                    ""
                },
                selectedCategoryId = tx.categoryId,
                selectedCategoryName = tx.categoryName,
                selectedParentCategoryName = tx.parentCategoryName,
                selectedCategoryColorHex = tx.categoryColorHex,
                selectedCategoryIconName = tx.categoryIconName,
                isSplit = tx.isSplit,
                splitDrafts = draftSplits,
                isInstallmentEnabled = false,
                payeeOrDescription = tx.payeeOrDescription,
                note = tx.note,
                selectedTags = tx.tags,
                isKeypadVisible = true,
                errorMessage = null
            )
        }
    }

    @Suppress("unused")
    fun loadTransactionForEdit(transactionId: Long) {
        viewModelScope.launch {
            val tx = transactionRepository.observeTransactionById(transactionId).firstOrNull() ?: return@launch
            loadTransactionItemForEdit(tx)
        }
    }

    fun setMode(mode: TransactionType) {
        formState.update { current ->
            current.copy(
                mode = mode,
                selectedCategoryId = null,
                selectedCategoryName = null,
                selectedParentCategoryName = null,
                isSplit = (mode == TransactionType.EXPENSE || mode == TransactionType.INCOME) && current.isSplit,
                isInstallmentEnabled = (mode == TransactionType.EXPENSE) && current.isInstallmentEnabled,
                errorMessage = null
            )
        }
    }

    fun setLedgerDateEpochDay(epochDay: Long) {
        lastUsedLedgerDateEpochDay = epochDay
        formState.update { it.copy(ledgerDateEpochDay = epochDay, errorMessage = null) }
    }

    fun stepLedgerDateByDays(deltaDays: Long) {
        formState.update {
            val nextDate = it.ledgerDateEpochDay + deltaDays
            lastUsedLedgerDateEpochDay = nextDate
            it.copy(
                ledgerDateEpochDay = nextDate,
                errorMessage = null
            )
        }
    }

    fun selectSourceAccount(accountId: Long) {
        formState.update { current ->
            val nextTransferTo = if (current.transferToAccountId == accountId) {
                uiState.value.activeAccounts.firstOrNull { it.id != accountId }?.id
            } else {
                current.transferToAccountId
            }
            current.copy(
                selectedAccountId = accountId,
                transferToAccountId = nextTransferTo,
                errorMessage = null
            )
        }
    }

    fun selectTransferDestinationAccount(accountId: Long) {
        formState.update { it.copy(transferToAccountId = accountId, errorMessage = null) }
    }

    fun updateTransferFeeInput(feeInput: String) {
        formState.update { it.copy(transferFeeInput = feeInput, errorMessage = null) }
    }

    fun selectCategory(category: CategoryNode, parentName: String? = null) {
        formState.update {
            it.copy(
                selectedCategoryId = category.id,
                selectedCategoryName = category.name,
                selectedParentCategoryName = parentName,
                selectedCategoryColorHex = category.colorHex,
                selectedCategoryIconName = category.iconName,
                errorMessage = null
            )
        }
    }

    fun setKeypadVisible(visible: Boolean) {
        formState.update { it.copy(isKeypadVisible = visible) }
    }

    fun onKeypadKeyPress(key: String) {
        formState.update { current ->
            val buf = current.expressionBuffer
            val fractionDigits = uiState.value.currencySpec.fractionDigits
            val nextBuffer = when (key) {
                "C" -> "0"
                "⌫" -> {
                    if (buf.length <= 1) "0" else buf.dropLast(1)
                }
                "=" -> {
                    val evaluated = evaluateBufferSafely(buf, fractionDigits)
                    formatMinorPlain(evaluated, fractionDigits)
                }
                "+", "-", "×", "÷" -> {
                    if (buf.isEmpty() || buf == "0") {
                        buf
                    } else if (buf.last() in charArrayOf('+', '-', '×', '÷', '*', '/')) {
                        buf.dropLast(1) + key
                    } else {
                        buf + key
                    }
                }
                "." -> {
                    val lastNumberToken = buf.split('+', '-', '×', '÷', '*', '/').lastOrNull().orEmpty()
                    if (fractionDigits == 0 || lastNumberToken.contains('.')) {
                        buf
                    } else {
                        "$buf."
                    }
                }
                "00" -> {
                    if (buf == "0") "0" else "${buf}00"
                }
                else -> {
                    if (buf == "0") key else buf + key
                }
            }
            current.copy(expressionBuffer = nextBuffer, errorMessage = null)
        }
    }

    fun toggleSplitMode() {
        val currentUi = uiState.value
        val defaultCat = currentUi.activeCategoryTree.firstOrNull()
        formState.update { current ->
            val enabling = !current.isSplit
            val initialDrafts = if (enabling && current.splitDrafts.isEmpty()) {
                val halfMinor = currentUi.evaluatedAmount.amountMinor / 2L
                val remainderMinor = currentUi.evaluatedAmount.amountMinor - halfMinor
                val digits = currentUi.currencySpec.fractionDigits
                listOf(
                    SplitLineDraft(
                        localId = nextSplitLocalId++,
                        categoryId = currentUi.selectedCategoryId ?: defaultCat?.id ?: 0L,
                        categoryName = currentUi.selectedCategoryName ?: defaultCat?.name ?: "Category 1",
                        categoryColorHex = currentUi.selectedCategoryColorHex ?: defaultCat?.colorHex ?: "#059669",
                        categoryIconName = currentUi.selectedCategoryIconName ?: defaultCat?.iconName ?: "category",
                        amountInput = formatMinorPlain(MinorMoney(halfMinor), digits)
                    ),
                    SplitLineDraft(
                        localId = nextSplitLocalId++,
                        categoryId = defaultCat?.id ?: 0L,
                        categoryName = defaultCat?.name ?: "Category 2",
                        categoryColorHex = defaultCat?.colorHex ?: "#2563EB",
                        categoryIconName = defaultCat?.iconName ?: "category",
                        amountInput = formatMinorPlain(MinorMoney(remainderMinor), digits)
                    )
                )
            } else {
                current.splitDrafts
            }

            current.copy(
                isSplit = enabling,
                isInstallmentEnabled = !enabling && current.isInstallmentEnabled,
                splitDrafts = initialDrafts,
                errorMessage = null
            )
        }
    }

    fun addSplitLine() {
        val currentUi = uiState.value
        val defaultCat = currentUi.activeCategoryTree.firstOrNull()
        val remaining = currentUi.splitValidation.remainingUnallocated
        val initialInput = if (remaining.isPositive) {
            formatMinorPlain(remaining, currentUi.currencySpec.fractionDigits)
        } else {
            "0"
        }

        formState.update { current ->
            current.copy(
                splitDrafts = current.splitDrafts + SplitLineDraft(
                    localId = nextSplitLocalId++,
                    categoryId = defaultCat?.id ?: 0L,
                    categoryName = defaultCat?.name ?: "Select Category",
                    categoryColorHex = defaultCat?.colorHex ?: "#059669",
                    categoryIconName = defaultCat?.iconName ?: "category",
                    amountInput = initialInput
                ),
                errorMessage = null
            )
        }
    }

    fun updateSplitLineAmount(localId: Long, amountInput: String) {
        formState.update { current ->
            current.copy(
                splitDrafts = current.splitDrafts.map { draft ->
                    if (draft.localId == localId) draft.copy(amountInput = amountInput) else draft
                },
                errorMessage = null
            )
        }
    }

    fun updateSplitLineCategory(localId: Long, category: CategoryNode) {
        formState.update { current ->
            current.copy(
                splitDrafts = current.splitDrafts.map { draft ->
                    if (draft.localId == localId) {
                        draft.copy(
                            categoryId = category.id,
                            categoryName = category.name,
                            categoryColorHex = category.colorHex,
                            categoryIconName = category.iconName
                        )
                    } else {
                        draft
                    }
                },
                errorMessage = null
            )
        }
    }

    @Suppress("unused")
    fun updateSplitLineNote(localId: Long, note: String) {
        formState.update { current ->
            current.copy(
                splitDrafts = current.splitDrafts.map { draft ->
                    if (draft.localId == localId) draft.copy(note = note) else draft
                }
            )
        }
    }

    fun assignRemainingToSplitLine(localId: Long) {
        val currentUi = uiState.value
        val targetDraft = currentUi.splitDrafts.firstOrNull { it.localId == localId } ?: return
        val newAmount = targetDraft.evaluatedAmount + currentUi.splitValidation.remainingUnallocated
        if (!newAmount.isPositive) return
        val formatted = formatMinorPlain(newAmount, currentUi.currencySpec.fractionDigits)
        updateSplitLineAmount(localId, formatted)
    }

    fun removeSplitLine(localId: Long) {
        formState.update { current ->
            if (current.splitDrafts.size <= 2) {
                current.copy(errorMessage = "Split transactions require at least 2 split lines.")
            } else {
                current.copy(
                    splitDrafts = current.splitDrafts.filterNot { it.localId == localId },
                    errorMessage = null
                )
            }
        }
    }

    fun toggleInstallmentEnabled() {
        formState.update { current ->
            val next = !current.isInstallmentEnabled
            current.copy(
                isInstallmentEnabled = next,
                isSplit = !next && current.isSplit,
                errorMessage = null
            )
        }
    }

    fun setInstallmentMonthsCount(months: Int) {
        formState.update {
            it.copy(installmentMonthsCount = months.coerceIn(2, 60), errorMessage = null)
        }
    }

    fun updatePayeeOrDescription(value: String) {
        formState.update { it.copy(payeeOrDescription = value, errorMessage = null) }
    }

    fun updateNote(value: String) {
        formState.update { it.copy(note = value, errorMessage = null) }
    }

    fun toggleTagSelection(tag: String) {
        formState.update { current ->
            val nextTags = if (tag in current.selectedTags) {
                current.selectedTags - tag
            } else {
                current.selectedTags + tag
            }
            current.copy(selectedTags = nextTags)
        }
    }

    fun createAndSelectTag(rawTagName: String) {
        val cleaned = rawTagName.trim().removePrefix("#").trim()
        if (cleaned.isEmpty()) return
        viewModelScope.launch {
            categoryAndTagRepository.createTag(cleaned)
            formState.update { current ->
                if (cleaned in current.selectedTags) {
                    current
                } else {
                    current.copy(selectedTags = current.selectedTags + cleaned)
                }
            }
        }
    }

    fun applyBookmark(bookmark: TransactionBookmark) {
        val currentUi = uiState.value
        val plainAmount = formatMinorPlain(bookmark.amount, currentUi.currencySpec.fractionDigits)
        formState.update { current ->
            current.copy(
                mode = bookmark.type,
                expressionBuffer = plainAmount,
                selectedAccountId = bookmark.accountId,
                transferToAccountId = bookmark.transferToAccountId,
                selectedCategoryId = bookmark.categoryId,
                payeeOrDescription = bookmark.payeeOrDescription,
                note = bookmark.note,
                selectedTags = bookmark.tags,
                isSplit = false,
                errorMessage = null
            )
        }
    }

    fun saveCurrentAsBookmark(title: String) {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isEmpty()) return
        val currentUi = uiState.value
        val account = currentUi.selectedAccount ?: return
        val bookmark = TransactionBookmark(
            id = 0L,
            title = trimmedTitle,
            type = currentUi.mode,
            amount = currentUi.evaluatedAmount,
            accountId = account.id,
            transferToAccountId = currentUi.transferToAccount?.id,
            categoryId = currentUi.selectedCategoryId,
            payeeOrDescription = currentUi.payeeOrDescription.trim(),
            note = currentUi.note.trim(),
            tags = currentUi.selectedTags
        )
        viewModelScope.launch {
            categoryAndTagRepository.upsertBookmark(bookmark)
        }
    }

    fun deleteBookmark(bookmarkId: Long) {
        viewModelScope.launch {
            categoryAndTagRepository.deleteBookmark(bookmarkId)
        }
    }

    @Suppress("unused")
    fun clearErrorMessage() {
        formState.update { it.copy(errorMessage = null) }
    }

    fun saveTransaction(onSuccess: (() -> Unit)? = null) {
        val state = uiState.value
        val amount = state.evaluatedAmount
        if (!amount.isPositive) {
            formState.update { it.copy(errorMessage = "Enter an amount greater than zero.") }
            return
        }

        val sourceAccount = state.selectedAccount
        if (sourceAccount == null) {
            formState.update { it.copy(errorMessage = "Please select an account.") }
            return
        }

        if (state.mode == TransactionType.TRANSFER || state.mode == TransactionType.CC_SETTLEMENT) {
            val destAccount = state.transferToAccount
            if (destAccount == null || destAccount.id == sourceAccount.id) {
                formState.update {
                    it.copy(errorMessage = "Select a different destination account for the transfer.")
                }
                return
            }
        } else if (!state.isSplit && state.selectedCategoryId == null) {
            formState.update { it.copy(errorMessage = "Please select a category.") }
            return
        }

        if (state.isSplit && !state.splitValidation.isValid) {
            formState.update {
                it.copy(
                    errorMessage = state.splitValidation.errorMessage
                        ?: "Split amounts must match the transaction total."
                )
            }
            return
        }

        viewModelScope.launch {
            formState.update { it.copy(isSaving = true, errorMessage = null) }

            val existingId = state.editingTransactionId
            if (existingId > 0L) {
                transactionRepository.deleteTransaction(existingId)
            }

            val baseTx = TransactionItem(
                id = 0L,
                type = state.mode,
                timestampEpochMillis = state.timestampEpochMillis,
                ledgerDateEpochDay = state.ledgerDateEpochDay,
                amount = amount,
                currencyCode = sourceAccount.currencyCode,
                accountId = sourceAccount.id,
                accountName = sourceAccount.name,
                transferToAccountId = state.transferToAccount?.id,
                transferToAccountName = state.transferToAccount?.name,
                categoryId = state.selectedCategoryId,
                categoryName = state.selectedCategoryName,
                parentCategoryName = state.selectedParentCategoryName,
                categoryColorHex = state.selectedCategoryColorHex,
                categoryIconName = state.selectedCategoryIconName,
                isSplit = state.isSplit,
                splits = state.splitDrafts.map { draft ->
                    SplitLineItem(
                        id = 0L,
                        categoryId = draft.categoryId,
                        categoryName = draft.categoryName,
                        amount = draft.evaluatedAmount,
                        note = draft.note.trim()
                    )
                },
                tags = state.selectedTags,
                payeeOrDescription = state.payeeOrDescription.trim(),
                note = state.note.trim()
            )

            val saveResult: Result<Any> = when {
                state.isInstallmentEnabled && state.isCreditCardExpense -> {
                    transactionRepository.createInstallmentPlanTransactions(
                        baseTransaction = baseTx,
                        totalInstallmentAmount = amount,
                        monthsCount = state.installmentMonthsCount,
                        startDateEpochDay = state.ledgerDateEpochDay
                    )
                }

                state.mode == TransactionType.TRANSFER || state.mode == TransactionType.CC_SETTLEMENT -> {
                    val feeMinor = evaluateBufferSafely(
                        rawExpression = state.transferFeeInput,
                        fractionDigits = state.currencySpec.fractionDigits
                    )
                    transactionRepository.recordTransfer(
                        fromAccountId = sourceAccount.id,
                        toAccountId = state.transferToAccount!!.id,
                        amount = amount,
                        transferFee = feeMinor,
                        timestampEpochMillis = state.timestampEpochMillis,
                        ledgerDateEpochDay = state.ledgerDateEpochDay,
                        note = state.note.trim().ifBlank { state.payeeOrDescription.trim() },
                        tags = state.selectedTags
                    )
                }

                state.mode == TransactionType.REFUND -> {
                    transactionRepository.recordRefund(
                        accountId = sourceAccount.id,
                        categoryId = state.selectedCategoryId,
                        refundAmount = amount,
                        timestampEpochMillis = state.timestampEpochMillis,
                        ledgerDateEpochDay = state.ledgerDateEpochDay,
                        originalTransactionId = null,
                        payeeOrDescription = state.payeeOrDescription.trim(),
                        note = state.note.trim()
                    )
                }

                state.isSplit -> {
                    transactionRepository.upsertSplitTransaction(baseTx)
                }

                else -> {
                    transactionRepository.upsertSingleTransaction(baseTx)
                }
            }

            saveResult.fold(
                onSuccess = {
                    lastUsedLedgerDateEpochDay = state.ledgerDateEpochDay
                    formState.update { current ->
                        EntryFormControlState(
                            editingTransactionId = 0L,
                            mode = current.mode,
                            selectedAccountId = current.selectedAccountId,
                            ledgerDateEpochDay = state.ledgerDateEpochDay,
                            savedSuccessfullyCounter = current.savedSuccessfullyCounter + 1
                        )
                    }
                    onSuccess?.invoke()
                },
                onFailure = { error ->
                    formState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = error.message ?: "Unable to save transaction."
                        )
                    }
                }
            )
        }
    }

    private fun evaluateBufferSafely(rawExpression: String, fractionDigits: Int): MinorMoney {
        val trimmed = rawExpression.trim()
        if (trimmed.isEmpty() || trimmed == "0") return MinorMoney.ZERO

        val direct = MoneyFormatterAndCalculator.evaluateKeypadExpressionToMinor(
            expression = trimmed,
            fractionDigits = fractionDigits
        ).getOrNull()
        if (direct != null) return direct

        val withoutTrailingOp = trimmed.trimEnd('+', '-', '×', '÷', '*', '/', '.')
        if (withoutTrailingOp.isEmpty()) return MinorMoney.ZERO

        return MoneyFormatterAndCalculator.evaluateKeypadExpressionToMinor(
            expression = withoutTrailingOp,
            fractionDigits = fractionDigits
        ).getOrDefault(MinorMoney.ZERO)
    }

    private fun formatMinorPlain(money: MinorMoney, fractionDigits: Int): String {
        if (!money.isPositive) return "0"
        val formatted = MoneyFormatterAndCalculator.formatMinorMoney(
            money = money,
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

    private fun findCategoryNodeById(tree: List<CategoryNode>, id: Long?): CategoryNode? {
        if (id == null) return null
        return tree.firstNotNullOfOrNull { parent ->
            if (parent.id == id) parent else parent.subcategories.firstOrNull { it.id == id }
        }
    }

    private fun findParentCategoryName(tree: List<CategoryNode>, id: Long?): String? {
        if (id == null) return null
        return tree.firstOrNull { parent -> parent.subcategories.any { it.id == id } }?.name
    }

    private data class ReferenceBundle(
        val accounts: List<AccountSummary>,
        val expenseCategories: List<CategoryNode>,
        val incomeCategories: List<CategoryNode>,
        val tags: List<String>,
        val bookmarks: List<TransactionBookmark>
    )
}