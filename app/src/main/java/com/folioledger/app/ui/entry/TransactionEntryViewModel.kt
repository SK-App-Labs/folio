// File: app/src/main/java/com/folioledger/app/ui/entry/TransactionEntryViewModel.kt
package com.folioledger.app.ui.entry

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.folioledger.app.domain.parser.SmartTextParserEngine
import com.folioledger.app.domain.parser.ParsedTransactionResolver
import com.folioledger.app.domain.parser.ResolvedTransactionFormValues
import java.util.Locale
import com.folioledger.app.domain.model.AccountGroup
import com.folioledger.app.domain.model.AccountSummary
import com.folioledger.app.domain.model.CategoryNode
import com.folioledger.app.domain.model.CategoryType
import com.folioledger.app.domain.model.CurrencySpec
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.NumberGroupingSystem
import com.folioledger.app.domain.model.RepeatFrequency
import com.folioledger.app.domain.model.SplitLineItem
import com.folioledger.app.domain.model.SupportedCurrencies
import com.folioledger.app.domain.model.TransactionBookmark
import com.folioledger.app.domain.model.TransactionItem
import com.folioledger.app.domain.model.TransactionType
import com.folioledger.app.domain.model.UserPreferences
import com.folioledger.app.domain.repository.AccountRepository
import com.folioledger.app.domain.repository.AnalyticsAndBudgetRepository
import com.folioledger.app.domain.repository.CategoryAndTagRepository
import com.folioledger.app.domain.repository.SettingsAndBackupRepository
import com.folioledger.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.first
import com.folioledger.app.domain.util.MoneyFormatterAndCalculator
import com.folioledger.app.domain.util.TransactionEntryRulesEngine
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
    val isRecurring: Boolean = false,
    val recurringFrequency: RepeatFrequency = RepeatFrequency.EVERY_MONTH,
    val payeeOrDescription: String = "",
    val note: String = "",
    val selectedTags: List<String> = emptyList(),
    val isKeypadVisible: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val savedSuccessfullyCounter: Int = 0,
    val dismissedClipboardSnippetHashes: Set<Int> = emptySet(),
    val pendingClipboardTransaction: ResolvedTransactionFormValues? = null
)

class TransactionEntryViewModel(
    private val transactionRepository: TransactionRepository,
    accountRepository: AccountRepository,
    private val categoryAndTagRepository: CategoryAndTagRepository,
    settingsAndBackupRepository: SettingsAndBackupRepository,
    private val analyticsAndBudgetRepository: AnalyticsAndBudgetRepository? = null
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

    private val userPreferencesFlow = settingsAndBackupRepository.observeUserPreferences()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = UserPreferences()
        )

    val uiState: StateFlow<TransactionEntryUiState> = combine(
        formState,
        referenceDataFlow,
        userPreferencesFlow
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

        val splitValidation = TransactionEntryRulesEngine.validateSplitTransaction(
            parentTotal = evaluatedMoney,
            splits = domainSplits
        )

        val isCCExpense = form.mode == TransactionType.EXPENSE &&
                selectedAccount?.groupType == AccountGroup.CREDIT_CARD
        val effectiveInstallmentEnabled = form.isInstallmentEnabled && isCCExpense && !form.isSplit

        val schedulePreview = if (effectiveInstallmentEnabled) {
            TransactionEntryRulesEngine.generateInstallmentSchedule(
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
            isRecurring = form.isRecurring,
            recurringFrequency = form.recurringFrequency,
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
            savedSuccessfullyCounter = form.savedSuccessfullyCounter,
            smartClipboardEnabled = prefs.smartClipboardEnabled,
            pendingClipboardTransaction = form.pendingClipboardTransaction
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
                savedSuccessfullyCounter = current.savedSuccessfullyCounter,
                dismissedClipboardSnippetHashes = current.dismissedClipboardSnippetHashes
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
            val isTransferMode = mode == TransactionType.TRANSFER || mode == TransactionType.CC_SETTLEMENT
            val sanitizedPayee = if (!isTransferMode && current.payeeOrDescription.contains(" → ")) {
                ""
            } else {
                current.payeeOrDescription
            }
            current.copy(
                mode = mode,
                transferToAccountId = if (isTransferMode) current.transferToAccountId else null,
                transferFeeInput = if (isTransferMode) current.transferFeeInput else "",
                selectedCategoryId = if (isTransferMode) null else current.selectedCategoryId,
                selectedCategoryName = if (isTransferMode) null else current.selectedCategoryName,
                selectedParentCategoryName = if (isTransferMode) null else current.selectedParentCategoryName,
                isSplit = (mode == TransactionType.EXPENSE || mode == TransactionType.INCOME) && current.isSplit,
                isInstallmentEnabled = (mode == TransactionType.EXPENSE) && current.isInstallmentEnabled,
                payeeOrDescription = sanitizedPayee,
                errorMessage = null
            )
        }
    }

    fun setLedgerDateEpochDay(epochDay: Long) {
        lastUsedLedgerDateEpochDay = epochDay
        formState.update { current ->
            val zone = ZoneId.systemDefault()
            val currentTime = Instant.ofEpochMilli(current.timestampEpochMillis).atZone(zone).toLocalTime()
            val newDate = LocalDate.ofEpochDay(epochDay)
            val newTimestamp = newDate.atTime(currentTime).atZone(zone).toInstant().toEpochMilli()
            current.copy(
                ledgerDateEpochDay = epochDay,
                timestampEpochMillis = newTimestamp,
                errorMessage = null
            )
        }
    }

    fun stepLedgerDateByDays(deltaDays: Long) {
        formState.update { current ->
            val nextDateEpochDay = current.ledgerDateEpochDay + deltaDays
            lastUsedLedgerDateEpochDay = nextDateEpochDay
            val zone = ZoneId.systemDefault()
            val currentTime = Instant.ofEpochMilli(current.timestampEpochMillis).atZone(zone).toLocalTime()
            val newDate = LocalDate.ofEpochDay(nextDateEpochDay)
            val newTimestamp = newDate.atTime(currentTime).atZone(zone).toInstant().toEpochMilli()
            current.copy(
                ledgerDateEpochDay = nextDateEpochDay,
                timestampEpochMillis = newTimestamp,
                errorMessage = null
            )
        }
    }

    fun setLedgerTime(hour: Int, minute: Int) {
        formState.update { current ->
            val date = LocalDate.ofEpochDay(current.ledgerDateEpochDay)
            val time = LocalTime.of(hour, minute)
            val zone = ZoneId.systemDefault()
            val newTimestamp = date.atTime(time).atZone(zone).toInstant().toEpochMilli()
            current.copy(
                timestampEpochMillis = newTimestamp,
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

    fun createAndSelectCategory(
        name: String,
        parentId: Long? = null,
        parentName: String? = null,
        iconName: String,
        colorHex: String,
        splitLineLocalId: Long? = null,
        onSuccess: ((CategoryNode) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val type = if (formState.value.mode == TransactionType.INCOME) CategoryType.INCOME else CategoryType.EXPENSE
            val newNode = CategoryNode(
                id = 0L,
                name = name.trim(),
                type = type,
                parentId = parentId,
                iconName = iconName,
                colorHex = colorHex
            )
            val result = categoryAndTagRepository.upsertCategory(newNode)
            result.onSuccess { createdId ->
                val createdNode = CategoryNode(
                    id = createdId,
                    name = name.trim(),
                    type = type,
                    parentId = parentId,
                    iconName = iconName,
                    colorHex = colorHex
                )
                if (splitLineLocalId != null) {
                    updateSplitLineCategory(splitLineLocalId, createdNode)
                } else {
                    selectCategory(createdNode, parentName)
                }
                onSuccess?.invoke(createdNode)
            }
        }
    }

    fun setKeypadVisible(visible: Boolean) {
        formState.update { it.copy(isKeypadVisible = visible) }
    }

    fun updateAmountInput(rawInput: String) {
        val sanitized = rawInput.filter { it.isDigit() || it == '.' || it in "+-×÷*/" }
        val cleaned = when {
            sanitized.isEmpty() -> "0"
            sanitized == "." -> "0."
            sanitized.length > 1 && sanitized.startsWith("0") && !sanitized.startsWith("0.") -> {
                sanitized.dropWhile { it == '0' }.ifEmpty { "0" }
            }
            else -> sanitized
        }
        formState.update { current ->
            current.copy(expressionBuffer = cleaned, errorMessage = null)
        }
    }

    fun onKeypadKeyPress(key: String) {
        val selectedAccount = uiState.value.selectedAccount
        val currencyCode = selectedAccount?.currencyCode ?: userPreferencesFlow.value.primaryCurrencyCode
        val fractionDigits = SupportedCurrencies.fromCode(currencyCode).fractionDigits
        formState.update { current ->
            val buf = current.expressionBuffer
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
                isRecurring = if (next) false else current.isRecurring,
                isSplit = !next && current.isSplit,
                errorMessage = null
            )
        }
    }

    fun toggleRecurringMode() {
        formState.update { current ->
            val next = !current.isRecurring
            current.copy(
                isRecurring = next,
                isInstallmentEnabled = if (next) false else current.isInstallmentEnabled,
                errorMessage = null
            )
        }
    }

    fun setRecurringFrequency(frequency: RepeatFrequency) {
        formState.update { current ->
            current.copy(
                isRecurring = true,
                recurringFrequency = frequency,
                isInstallmentEnabled = false,
                errorMessage = null
            )
        }
    }

    fun disableRecurringMode() {
        formState.update { it.copy(isRecurring = false, errorMessage = null) }
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

    fun loadRecurringRuleForEntry(ruleId: Long) {
        val repo = analyticsAndBudgetRepository ?: return
        viewModelScope.launch {
            val rules = repo.observeAllRecurringRules().first()
            val rule = rules.firstOrNull { it.id == ruleId } ?: return@launch
            val currentUi = uiState.value
            val isTransferMode = rule.type == TransactionType.TRANSFER || rule.type == TransactionType.CC_SETTLEMENT
            val plainAmount = formatMinorPlain(rule.amount, currentUi.currencySpec.fractionDigits)
            formState.update { current ->
                current.copy(
                    editingTransactionId = 0L,
                    mode = rule.type,
                    expressionBuffer = plainAmount,
                    selectedAccountId = rule.accountId,
                    transferToAccountId = if (isTransferMode) rule.transferToAccountId else null,
                    selectedCategoryId = if (isTransferMode) null else rule.categoryId,
                    payeeOrDescription = rule.payeeOrDescription.ifEmpty { rule.title },
                    note = rule.note,
                    ledgerDateEpochDay = rule.nextOccurrenceEpochDay,
                    timestampEpochMillis = System.currentTimeMillis(),
                    isSplit = false,
                    isKeypadVisible = false,
                    errorMessage = null
                )
            }
        }
    }

    fun applyBookmark(bookmark: TransactionBookmark) {
        val currentUi = uiState.value
        val plainAmount = formatMinorPlain(bookmark.amount, currentUi.currencySpec.fractionDigits)
        val isTransferMode = bookmark.type == TransactionType.TRANSFER || bookmark.type == TransactionType.CC_SETTLEMENT
        formState.update { current ->
            current.copy(
                mode = bookmark.type,
                expressionBuffer = plainAmount,
                selectedAccountId = bookmark.accountId,
                transferToAccountId = if (isTransferMode) bookmark.transferToAccountId else null,
                selectedCategoryId = if (isTransferMode) null else bookmark.categoryId,
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
        val isTransferMode = currentUi.mode == TransactionType.TRANSFER || currentUi.mode == TransactionType.CC_SETTLEMENT
        val bookmark = TransactionBookmark(
            id = 0L,
            title = trimmedTitle,
            type = currentUi.mode,
            amount = currentUi.evaluatedAmount,
            accountId = account.id,
            transferToAccountId = if (isTransferMode) currentUi.transferToAccount?.id else null,
            categoryId = if (isTransferMode) null else currentUi.selectedCategoryId,
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

    fun processClipboardText(rawText: String) {
        val trimmed = rawText.trim()
        if (trimmed.isEmpty()) return
        val textHash = trimmed.hashCode()
        if (formState.value.dismissedClipboardSnippetHashes.contains(textHash)) return

        val parsed = SmartTextParserEngine.parse(trimmed)
        if (!parsed.isValid) return

        viewModelScope.launch {
            val refs = referenceDataFlow.first()
            val categories = if (parsed.type == TransactionType.INCOME) refs.incomeCategories else refs.expenseCategories

            // Tier 1 Historical Payee Learning: fetch recent transactions to map payee names to past categories
            val today = LocalDate.now().toEpochDay()
            val historicalPayeeMap = runCatching {
                transactionRepository.observeTransactionsInRange(startEpochDay = today - 90, endEpochDay = today)
                    .first()
                    .filter { it.payeeOrDescription.isNotBlank() && it.categoryId != null }
                    .associate { it.payeeOrDescription.lowercase(Locale.US).trim() to it.categoryId!! }
            }.getOrDefault(emptyMap())

            val resolved = ParsedTransactionResolver.resolve(
                parsed = parsed,
                accounts = refs.accounts,
                categories = categories,
                historicalPayeeCategoryMap = historicalPayeeMap
            )
            formState.update { current ->
                current.copy(pendingClipboardTransaction = resolved)
            }
        }
    }

    fun dismissClipboardSuggestion() {
        formState.update { current ->
            val pendingHash = current.pendingClipboardTransaction?.parsedResult?.rawText?.trim()?.hashCode()
            val newDismissed = if (pendingHash != null) {
                current.dismissedClipboardSnippetHashes + pendingHash
            } else {
                current.dismissedClipboardSnippetHashes
            }
            current.copy(
                pendingClipboardTransaction = null,
                dismissedClipboardSnippetHashes = newDismissed
            )
        }
    }

    fun applyResolvedTransaction(resolved: ResolvedTransactionFormValues) {
        formState.update { current ->
            val parsed = resolved.parsedResult
            val formattedAmount = if (parsed.amountMinor != null) {
                val major = parsed.amountMinor.amountMinor / 100L
                val minor = parsed.amountMinor.amountMinor % 100L
                if (minor == 0L) major.toString() else String.format(Locale.US, "%d.%02d", major, minor)
            } else current.expressionBuffer

            val modeChanged = current.mode != parsed.type
            val resolvedCategoryId = resolved.matchedCategory?.id ?: if (modeChanged) null else current.selectedCategoryId
            val resolvedCategoryName = resolved.matchedCategory?.name ?: if (modeChanged) null else current.selectedCategoryName
            val resolvedParentCategoryName = resolved.matchedParentCategoryName ?: if (modeChanged) null else current.selectedParentCategoryName
            val resolvedCategoryColorHex = resolved.matchedCategory?.colorHex ?: if (modeChanged) null else current.selectedCategoryColorHex
            val resolvedCategoryIconName = resolved.matchedCategory?.iconName ?: if (modeChanged) null else current.selectedCategoryIconName

            current.copy(
                mode = parsed.type,
                expressionBuffer = formattedAmount,
                selectedAccountId = resolved.matchedAccount?.id ?: current.selectedAccountId,
                selectedCategoryId = resolvedCategoryId,
                selectedCategoryName = resolvedCategoryName,
                selectedParentCategoryName = resolvedParentCategoryName,
                selectedCategoryColorHex = resolvedCategoryColorHex,
                selectedCategoryIconName = resolvedCategoryIconName,
                payeeOrDescription = if (parsed.payeeOrDescription.isNotBlank()) parsed.payeeOrDescription else current.payeeOrDescription,
                ledgerDateEpochDay = parsed.dateEpochDay ?: current.ledgerDateEpochDay,
                pendingClipboardTransaction = null
            )
        }
    }

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
            val isTransferMode = state.mode == TransactionType.TRANSFER || state.mode == TransactionType.CC_SETTLEMENT
            val isExpenseOrIncome = state.mode == TransactionType.EXPENSE || state.mode == TransactionType.INCOME

            val transferFee = if (isTransferMode) {
                evaluateBufferSafely(
                    rawExpression = state.transferFeeInput,
                    fractionDigits = state.currencySpec.fractionDigits
                )
            } else {
                MinorMoney.ZERO
            }

            val defaultPayee = when {
                isTransferMode -> "${sourceAccount.name} → ${state.transferToAccount?.name ?: ""}"
                state.mode == TransactionType.REFUND -> "Refund"
                else -> ""
            }

            val baseTx = TransactionItem(
                id = existingId,
                type = state.mode,
                timestampEpochMillis = state.timestampEpochMillis,
                ledgerDateEpochDay = state.ledgerDateEpochDay,
                amount = amount,
                currencyCode = sourceAccount.currencyCode,
                accountId = sourceAccount.id,
                accountName = sourceAccount.name,
                transferToAccountId = if (isTransferMode) state.transferToAccount?.id else null,
                transferToAccountName = if (isTransferMode) state.transferToAccount?.name else null,
                transferFee = transferFee,
                categoryId = if (isTransferMode) null else state.selectedCategoryId,
                categoryName = if (isTransferMode) null else state.selectedCategoryName,
                parentCategoryName = if (isTransferMode) null else state.selectedParentCategoryName,
                categoryColorHex = if (isTransferMode) null else state.selectedCategoryColorHex,
                categoryIconName = if (isTransferMode) null else state.selectedCategoryIconName,
                isSplit = isExpenseOrIncome && state.isSplit,
                splits = if (isExpenseOrIncome && state.isSplit) {
                    state.splitDrafts.map { draft ->
                        SplitLineItem(
                            id = 0L,
                            categoryId = draft.categoryId,
                            categoryName = draft.categoryName,
                            amount = draft.evaluatedAmount,
                            note = draft.note.trim()
                        )
                    }
                } else {
                    emptyList()
                },
                tags = state.selectedTags,
                payeeOrDescription = state.payeeOrDescription.trim().ifEmpty { defaultPayee },
                note = state.note.trim()
            )

            val saveResult: Result<Any> = when {
                state.isInstallmentEnabled && state.isCreditCardExpense -> {
                    if (existingId > 0L) {
                        transactionRepository.deleteTransaction(existingId)
                    }
                    transactionRepository.createInstallmentPlanTransactions(
                        baseTransaction = baseTx,
                        totalInstallmentAmount = amount,
                        monthsCount = state.installmentMonthsCount,
                        startDateEpochDay = state.ledgerDateEpochDay
                    )
                }

                state.isRecurring -> {
                    transactionRepository.upsertRecurringTransaction(
                        transaction = baseTx,
                        frequency = state.recurringFrequency
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