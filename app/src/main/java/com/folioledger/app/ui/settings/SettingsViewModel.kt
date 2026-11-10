// File: app/src/main/java/com/folioledger/app/ui/settings/SettingsViewModel.kt
package com.folioledger.app.ui.settings

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.folioledger.app.data.pdf.FiscalYearType
import com.folioledger.app.data.pdf.PdfExportResult
import com.folioledger.app.domain.model.AccountSummary
import com.folioledger.app.domain.model.AutoBackupRetention
import com.folioledger.app.domain.model.CategoryNode
import com.folioledger.app.domain.model.CategoryType
import com.folioledger.app.domain.model.CurrencySpec
import com.folioledger.app.domain.model.ImportCsvPreview
import com.folioledger.app.domain.model.ImportCsvResult
import com.folioledger.app.domain.model.InstallmentPlanWithProgress
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.NumberGroupingSystem
import com.folioledger.app.domain.model.RecurringCommitmentItem
import com.folioledger.app.domain.model.RepeatFrequency
import com.folioledger.app.domain.model.SecondaryCurrency
import com.folioledger.app.domain.model.SupportedCurrencies
import com.folioledger.app.domain.model.TransactionBookmark
import com.folioledger.app.domain.model.TransactionType
import com.folioledger.app.domain.model.UserPreferences
import com.folioledger.app.domain.repository.AccountRepository
import com.folioledger.app.domain.repository.AnalyticsAndBudgetRepository
import com.folioledger.app.domain.repository.CategoryAndTagRepository
import com.folioledger.app.domain.repository.SettingsAndBackupRepository
import com.folioledger.app.domain.util.MoneyFormatterAndCalculator
import com.folioledger.app.domain.util.RecurringBurdenCalculator
import com.folioledger.app.ui.components.suggestIconAndColorForCategoryName
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate

private data class SettingsInternalControlState(
    val isRecurringManagerOpen: Boolean = false,
    val isCategoryManagerOpen: Boolean = false,
    val selectedCategoryManagerType: CategoryType = CategoryType.EXPENSE,
    val categoryEditorState: CategoryEditorDialogState? = null,
    val isTagManagerOpen: Boolean = false,
    val isBookmarkManagerOpen: Boolean = false,
    val isPdfReportsSheetOpen: Boolean = false,
    val lastGeneratedPdfResult: PdfExportResult? = null,
    val isGeneratingPdf: Boolean = false,
    val pendingRestoreUriString: String? = null,
    val csvPreview: ImportCsvPreview? = null,
    val csvImportResult: ImportCsvResult? = null,
    val isBusyWithFileOperation: Boolean = false,
    val fileOperationStatusLabel: String? = null,
    val snackbarMessage: String? = null,
    val lastCompletedFileForShare: File? = null,
    val lastExportedFileSuccess: FileExportSuccessUiModel? = null,
    val autoBackupFiles: List<File> = emptyList()
)

class SettingsViewModel(
    private val settingsAndBackupRepository: SettingsAndBackupRepository,
    private val categoryAndTagRepository: CategoryAndTagRepository,
    private val accountRepository: AccountRepository,
    private val analyticsAndBudgetRepository: AnalyticsAndBudgetRepository
) : ViewModel() {

    private val controlState = MutableStateFlow(SettingsInternalControlState())

    private val categoriesAndMetadataFlow = combine(
        categoryAndTagRepository.observeCategoryTree(CategoryType.EXPENSE),
        categoryAndTagRepository.observeCategoryTree(CategoryType.INCOME),
        categoryAndTagRepository.observeAllTags(),
        categoryAndTagRepository.observeBookmarks()
    ) { expenseCats, incomeCats, tags, bookmarks ->
        CategoriesAndMetadataBundle(
            expenseCategories = expenseCats,
            incomeCategories = incomeCats,
            tags = tags,
            bookmarks = bookmarks
        )
    }

    private val activeAccountsFlow = accountRepository.observeActiveAccounts(LocalDate.now().toEpochDay())

    private val recurringAndInstallmentsFlow = combine(
        analyticsAndBudgetRepository.observeAllRecurringRules(),
        analyticsAndBudgetRepository.observeInstallmentPlansWithProgress()
    ) { rules, plans ->
        rules to plans
    }

    val userPreferences: StateFlow<UserPreferences> = settingsAndBackupRepository
        .observeUserPreferences()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UserPreferences()
        )

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsAndBackupRepository.observeUserPreferences(),
        categoriesAndMetadataFlow,
        activeAccountsFlow,
        recurringAndInstallmentsFlow,
        controlState
    ) { prefs, metadata, accounts, (recurringRules, installmentPlans), ctrl ->
        val currencySpec = SupportedCurrencies.fromCode(prefs.primaryCurrencyCode)
        val burden = RecurringBurdenCalculator.computeMonthlyBurden(recurringRules, installmentPlans)
        val formattedMonthly = MoneyFormatterAndCalculator.formatMinorMoney(
            money = burden.totalMonthlyFixedBurden,
            currencySpec = currencySpec,
            grouping = prefs.numberGrouping,
            includeSymbol = true
        )

        SettingsUiState(
            userPreferences = prefs,
            currencySpec = currencySpec,
            accounts = accounts,
            expenseCategories = metadata.expenseCategories,
            incomeCategories = metadata.incomeCategories,
            tags = metadata.tags,
            bookmarks = metadata.bookmarks,
            recurringRules = recurringRules,
            installmentPlans = installmentPlans,
            activeRecurringCount = burden.activeRecurringCount,
            activeMonthlyRecurringBurden = burden.monthlyRecurringExpense,
            activeMonthlyTransfersBurden = burden.monthlyRecurringTransfer,
            activeMonthlyInstallmentsBurden = burden.monthlyInstallments,
            totalMonthlyFixedBurden = burden.totalMonthlyFixedBurden,
            formattedMonthlyBurden = formattedMonthly,
            isRecurringManagerOpen = ctrl.isRecurringManagerOpen,
            isCategoryManagerOpen = ctrl.isCategoryManagerOpen,
            selectedCategoryManagerType = ctrl.selectedCategoryManagerType,
            categoryEditorState = ctrl.categoryEditorState,
            isTagManagerOpen = ctrl.isTagManagerOpen,
            isBookmarkManagerOpen = ctrl.isBookmarkManagerOpen,
            isPdfReportsSheetOpen = ctrl.isPdfReportsSheetOpen,
            lastGeneratedPdfResult = ctrl.lastGeneratedPdfResult,
            isGeneratingPdf = ctrl.isGeneratingPdf,
            pendingRestoreUriString = ctrl.pendingRestoreUriString,
            csvPreview = ctrl.csvPreview,
            csvImportResult = ctrl.csvImportResult,
            isBusyWithFileOperation = ctrl.isBusyWithFileOperation,
            fileOperationStatusLabel = ctrl.fileOperationStatusLabel,
            snackbarMessage = ctrl.snackbarMessage,
            lastCompletedFileForShare = ctrl.lastCompletedFileForShare,
            lastExportedFileSuccess = ctrl.lastExportedFileSuccess,
            autoBackupFiles = ctrl.autoBackupFiles
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = SettingsUiState()
    )

    init {
        refreshAutoBackupFiles()
    }

    fun setRecurringManagerOpen(isOpen: Boolean) {
        controlState.update { it.copy(isRecurringManagerOpen = isOpen) }
    }

    fun toggleRecurringRuleActive(ruleId: Long, isActive: Boolean) {
        viewModelScope.launch {
            analyticsAndBudgetRepository.updateRecurringRuleActive(ruleId, isActive)
        }
    }

    fun deleteRecurringRule(ruleId: Long) {
        viewModelScope.launch {
            analyticsAndBudgetRepository.deleteRecurringRule(ruleId)
        }
    }

    fun upsertRecurringRule(rule: RecurringCommitmentItem) {
        viewModelScope.launch {
            analyticsAndBudgetRepository.upsertRecurringRule(rule)
        }
    }

    fun deleteInstallmentPlan(planId: Long) {
        viewModelScope.launch {
            analyticsAndBudgetRepository.deleteInstallmentPlan(planId)
        }
    }

    // UserPreferences Update Actions

    fun setPrimaryCurrency(currencyCode: String) {
        val spec = SupportedCurrencies.fromCode(currencyCode)
        viewModelScope.launch {
            settingsAndBackupRepository.updateUserPreferences { current ->
                val cleanSecondaries = current.secondaryCurrencies.filter { it.code != spec.code }
                current.copy(
                    primaryCurrencyCode = spec.code,
                    secondaryCurrencies = cleanSecondaries,
                    numberGrouping = spec.defaultGrouping
                )
            }
            controlState.update {
                it.copy(snackbarMessage = "Primary currency set to ${spec.displayName} (${spec.symbol})")
            }
        }
    }

    fun addOrUpdateSecondaryCurrency(code: String, rate: Double) {
        val trimmedCode = code.trim().uppercase()
        if (trimmedCode.isEmpty() || rate <= 0.0) return
        viewModelScope.launch {
            settingsAndBackupRepository.updateUserPreferences { current ->
                if (trimmedCode == current.primaryCurrencyCode.uppercase()) {
                    return@updateUserPreferences current
                }
                val updatedList = current.secondaryCurrencies
                    .filter { it.code != trimmedCode }
                    .plus(SecondaryCurrency(code = trimmedCode, exchangeRateToPrimary = rate))
                    .sortedBy { it.code }
                current.copy(secondaryCurrencies = updatedList)
            }
            controlState.update {
                it.copy(snackbarMessage = "Secondary currency $trimmedCode updated (1 $trimmedCode = $rate)")
            }
        }
    }

    fun removeSecondaryCurrency(code: String) {
        val trimmedCode = code.trim().uppercase()
        viewModelScope.launch {
            settingsAndBackupRepository.updateUserPreferences { current ->
                val updatedList = current.secondaryCurrencies.filter { it.code != trimmedCode }
                current.copy(secondaryCurrencies = updatedList)
            }
            controlState.update {
                it.copy(snackbarMessage = "Removed $trimmedCode from secondary currencies")
            }
        }
    }

    fun setNumberGrouping(grouping: NumberGroupingSystem) {
        viewModelScope.launch {
            settingsAndBackupRepository.updateUserPreferences { current ->
                current.copy(numberGrouping = grouping)
            }
        }
    }

    fun setMonthStartDay(day: Int) {
        val safeDay = day.coerceIn(1, 31)
        viewModelScope.launch {
            settingsAndBackupRepository.updateUserPreferences { current ->
                current.copy(monthStartDay = safeDay)
            }
            controlState.update {
                it.copy(snackbarMessage = "Financial month start day set to Day $safeDay")
            }
        }
    }

    fun setWeekStartDayIso(dayIso: Int) {
        val safeIso = dayIso.coerceIn(1, 7)
        viewModelScope.launch {
            settingsAndBackupRepository.updateUserPreferences { current ->
                current.copy(weekStartDayIso = safeIso)
            }
        }
    }

    fun setSubcategoriesEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsAndBackupRepository.updateUserPreferences { current ->
                current.copy(subcategoriesEnabled = enabled)
            }
        }
    }

    fun setCarryOverEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsAndBackupRepository.updateUserPreferences { current ->
                current.copy(carryOverEnabled = enabled)
            }
        }
    }

    fun setSmartClipboardEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsAndBackupRepository.updateUserPreferences { current ->
                current.copy(smartClipboardEnabled = enabled)
            }
        }
    }

    fun setThemeMode(themeMode: String) {
        viewModelScope.launch {
            settingsAndBackupRepository.updateUserPreferences { current ->
                current.copy(themeMode = themeMode)
            }
        }
    }

    fun setDynamicColorEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsAndBackupRepository.updateUserPreferences { current ->
                current.copy(dynamicColorEnabled = enabled)
            }
        }
    }

    fun setIconPackTheme(iconPackTheme: String) {
        viewModelScope.launch {
            settingsAndBackupRepository.updateUserPreferences { current ->
                current.copy(iconPackTheme = iconPackTheme)
            }
        }
    }

    fun setAccentPalette(accentPalette: String) {
        viewModelScope.launch {
            settingsAndBackupRepository.updateUserPreferences { current ->
                current.copy(accentPalette = accentPalette)
            }
        }
    }

    fun setBiometricLockEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsAndBackupRepository.updateUserPreferences { current ->
                current.copy(biometricLockEnabled = enabled)
            }
            controlState.update {
                it.copy(
                    snackbarMessage = if (enabled) {
                        "Biometric / Device Credential app lock enabled"
                    } else {
                        "App lock disabled"
                    }
                )
            }
        }
    }

    fun setRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsAndBackupRepository.updateUserPreferences { current ->
                current.copy(remindersEnabled = enabled)
            }
            controlState.update {
                it.copy(
                    snackbarMessage = if (enabled) {
                        "Payment & bill reminders enabled"
                    } else {
                        "Payment & bill reminders disabled"
                    }
                )
            }
        }
    }

    fun setCcReminderLeadDays(days: Int) {
        val safeDays = days.coerceIn(0, 14)
        viewModelScope.launch {
            settingsAndBackupRepository.updateUserPreferences { current ->
                current.copy(ccReminderLeadDays = safeDays)
            }
        }
    }

    fun setBillReminderLeadDays(days: Int) {
        val safeDays = days.coerceIn(0, 7)
        viewModelScope.launch {
            settingsAndBackupRepository.updateUserPreferences { current ->
                current.copy(billReminderLeadDays = safeDays)
            }
        }
    }

    fun setReminderTime(hourOfDay: Int, minute: Int) {
        val safeHour = hourOfDay.coerceIn(0, 23)
        val safeMin = minute.coerceIn(0, 59)
        viewModelScope.launch {
            settingsAndBackupRepository.updateUserPreferences { current ->
                current.copy(
                    reminderHourOfDay = safeHour,
                    reminderMinute = safeMin
                )
            }
        }
    }

    fun setHideAmountsOnLockScreen(hide: Boolean) {
        viewModelScope.launch {
            settingsAndBackupRepository.updateUserPreferences { current ->
                current.copy(hideAmountsOnLockScreen = hide)
            }
        }
    }

    fun unlockSupporterBadge(tierTitle: String) {
        viewModelScope.launch {
            settingsAndBackupRepository.updateUserPreferences { current ->
                current.copy(
                    isSupporter = true,
                    supporterTier = tierTitle
                )
            }
            controlState.update {
                it.copy(
                    snackbarMessage = "Thank you for your $tierTitle support! ★ Supporter badge unlocked."
                )
            }
        }
    }

    // 2-Level Category & Subcategory CRUD Actions

    fun setCategoryManagerOpen(
        open: Boolean,
        initialType: CategoryType? = null
    ) {
        controlState.update { current ->
            current.copy(
                isCategoryManagerOpen = open,
                selectedCategoryManagerType = initialType ?: current.selectedCategoryManagerType,
                categoryEditorState = if (!open) null else current.categoryEditorState
            )
        }
    }

    fun selectCategoryManagerType(type: CategoryType) {
        controlState.update { it.copy(selectedCategoryManagerType = type) }
    }

    fun openCreateMainCategoryDialog(
        type: CategoryType? = null
    ) {
        controlState.update { current ->
            val resolvedType = type ?: current.selectedCategoryManagerType
            val defaultColor = if (resolvedType == CategoryType.EXPENSE) "#E11D48" else "#059669"
            val defaultIcon = if (resolvedType == CategoryType.EXPENSE) "shopping_cart" else "payments"
            current.copy(
                categoryEditorState = CategoryEditorDialogState(
                    isEditing = false,
                    categoryId = 0L,
                    name = "",
                    type = resolvedType,
                    parentId = null,
                    parentCategoryName = null,
                    iconName = defaultIcon,
                    colorHex = defaultColor,
                    isSmartIconMatched = false,
                    isIconManuallyOverridden = false,
                    validationError = null
                )
            )
        }
    }

    fun openCreateSubcategoryDialog(parentCategory: CategoryNode) {
        controlState.update {
            it.copy(
                categoryEditorState = CategoryEditorDialogState(
                    isEditing = false,
                    categoryId = 0L,
                    name = "",
                    type = parentCategory.type,
                    parentId = parentCategory.id,
                    parentCategoryName = parentCategory.name,
                    iconName = parentCategory.iconName,
                    colorHex = parentCategory.colorHex,
                    isSmartIconMatched = false,
                    isIconManuallyOverridden = false,
                    validationError = null
                )
            )
        }
    }

    fun openEditCategoryDialog(
        category: CategoryNode,
        parentCategoryName: String? = null
    ) {
        controlState.update {
            it.copy(
                categoryEditorState = CategoryEditorDialogState(
                    isEditing = true,
                    categoryId = category.id,
                    name = category.name,
                    type = category.type,
                    parentId = category.parentId,
                    parentCategoryName = parentCategoryName,
                    iconName = category.iconName,
                    colorHex = category.colorHex,
                    isSmartIconMatched = false,
                    isIconManuallyOverridden = true,
                    validationError = null
                )
            )
        }
    }

    fun onCategoryNameInputChanged(newName: String) {
        controlState.update { current ->
            val existing = current.categoryEditorState ?: return@update current
            if (!existing.isEditing || !existing.isIconManuallyOverridden) {
                val suggestion = suggestIconAndColorForCategoryName(newName, existing.type)
                if (suggestion != null) {
                    current.copy(
                        categoryEditorState = existing.copy(
                            name = newName,
                            iconName = suggestion.first,
                            colorHex = suggestion.second,
                            isSmartIconMatched = true,
                            validationError = null
                        )
                    )
                } else {
                    current.copy(
                        categoryEditorState = existing.copy(
                            name = newName,
                            isSmartIconMatched = newName.isNotBlank() && existing.isSmartIconMatched,
                            validationError = null
                        )
                    )
                }
            } else {
                current.copy(
                    categoryEditorState = existing.copy(
                        name = newName,
                        validationError = null
                    )
                )
            }
        }
    }

    fun updateCategoryEditorState(
        transform: (CategoryEditorDialogState) -> CategoryEditorDialogState
    ) {
        controlState.update { current ->
            val existing = current.categoryEditorState ?: return@update current
            val updated = transform(existing)
            val iconOrColorChangedManually = updated.iconName != existing.iconName ||
                    updated.colorHex != existing.colorHex
            current.copy(
                categoryEditorState = updated.copy(
                    isIconManuallyOverridden = iconOrColorChangedManually || updated.isIconManuallyOverridden,
                    isSmartIconMatched = !iconOrColorChangedManually && updated.isSmartIconMatched,
                    validationError = null
                )
            )
        }
    }

    fun dismissCategoryEditorDialog() {
        controlState.update { it.copy(categoryEditorState = null) }
    }

    fun saveCategoryFromDialog() {
        val editor = controlState.value.categoryEditorState ?: return
        val trimmedName = editor.name.trim()
        if (trimmedName.isEmpty()) {
            updateCategoryEditorState {
                it.copy(validationError = "Category name cannot be empty.")
            }
            return
        }

        val categoryNode = CategoryNode(
            id = editor.categoryId,
            name = trimmedName,
            type = editor.type,
            parentId = editor.parentId,
            iconName = editor.iconName,
            colorHex = editor.colorHex,
            subcategories = emptyList()
        )

        viewModelScope.launch {
            val result = categoryAndTagRepository.upsertCategory(categoryNode)
            result.fold(
                onSuccess = {
                    controlState.update {
                        it.copy(
                            categoryEditorState = null,
                            snackbarMessage = if (editor.isEditing) {
                                "Updated category \"${categoryNode.name}\""
                            } else {
                                "Created category \"${categoryNode.name}\""
                            }
                        )
                    }
                },
                onFailure = { error ->
                    updateCategoryEditorState {
                        it.copy(validationError = error.message ?: "Failed to save category.")
                    }
                }
            )
        }
    }

    fun deleteCategory(categoryId: Long, reassignToCategoryId: Long? = null) {
        viewModelScope.launch {
            val result = categoryAndTagRepository.deleteCategory(
                categoryId = categoryId,
                reassignToCategoryId = reassignToCategoryId
            )
            result.fold(
                onSuccess = {
                    controlState.update {
                        it.copy(
                            categoryEditorState = null,
                            snackbarMessage = "Category deleted"
                        )
                    }
                },
                onFailure = { error ->
                    controlState.update {
                        it.copy(snackbarMessage = error.message ?: "Unable to delete category")
                    }
                }
            )
        }
    }

    // Tag & Bookmark Management Actions

    fun setTagManagerOpen(open: Boolean) {
        controlState.update { it.copy(isTagManagerOpen = open) }
    }

    fun createTag(rawTagName: String) {
        val cleaned = rawTagName.trim().removePrefix("#").trim()
        if (cleaned.isEmpty()) return
        viewModelScope.launch {
            val result = categoryAndTagRepository.createTag(cleaned)
            result.fold(
                onSuccess = {
                    controlState.update { it.copy(snackbarMessage = "Created tag #$cleaned") }
                },
                onFailure = { error ->
                    controlState.update {
                        it.copy(snackbarMessage = error.message ?: "Failed to create tag")
                    }
                }
            )
        }
    }

    fun renameTag(oldTagName: String, newTagNameRaw: String) {
        val cleanedNew = newTagNameRaw.trim().removePrefix("#").trim()
        if (cleanedNew.isEmpty() || cleanedNew == oldTagName) return
        viewModelScope.launch {
            val result = categoryAndTagRepository.renameTag(oldTagName, cleanedNew)
            result.fold(
                onSuccess = {
                    controlState.update {
                        it.copy(snackbarMessage = "Renamed #$oldTagName to #$cleanedNew")
                    }
                },
                onFailure = { error ->
                    controlState.update {
                        it.copy(snackbarMessage = error.message ?: "Failed to rename tag")
                    }
                }
            )
        }
    }

    fun deleteTag(tagName: String) {
        viewModelScope.launch {
            val result = categoryAndTagRepository.deleteTag(tagName)
            result.fold(
                onSuccess = {
                    controlState.update { it.copy(snackbarMessage = "Deleted tag #$tagName") }
                },
                onFailure = { error ->
                    controlState.update {
                        it.copy(snackbarMessage = error.message ?: "Failed to delete tag")
                    }
                }
            )
        }
    }

    fun setBookmarkManagerOpen(open: Boolean) {
        controlState.update { it.copy(isBookmarkManagerOpen = open) }
    }

    fun deleteBookmark(bookmarkId: Long) {
        viewModelScope.launch {
            val result = categoryAndTagRepository.deleteBookmark(bookmarkId)
            result.fold(
                onSuccess = {
                    controlState.update { it.copy(snackbarMessage = "Bookmark template deleted") }
                },
                onFailure = { error ->
                    controlState.update {
                        it.copy(snackbarMessage = error.message ?: "Unable to delete bookmark")
                    }
                }
            )
        }
    }

    // Backup, Restore & CSV Export Actions

    fun createPortableZipBackup(destinationUriString: String) {
        viewModelScope.launch {
            controlState.update {
                it.copy(
                    isBusyWithFileOperation = true,
                    fileOperationStatusLabel = "Creating portable ZIP backup..."
                )
            }
            val result = settingsAndBackupRepository.createPortableZipBackup(destinationUriString)
            result.fold(
                onSuccess = {
                    val latestFile = settingsAndBackupRepository.getLatestBackupFile()
                    val cleanFileName = destinationUriString
                        .substringAfterLast('/')
                        .substringAfterLast('\\')
                        .ifBlank { "Folio_Backup.zip" }
                    controlState.update {
                        it.copy(
                            isBusyWithFileOperation = false,
                            fileOperationStatusLabel = null,
                            lastCompletedFileForShare = null,
                            lastExportedFileSuccess = FileExportSuccessUiModel(
                                title = "Backup Created!",
                                subtitle = "Portable ZIP backup archive saved to your device",
                                fileName = cleanFileName,
                                fileSizeBytes = latestFile?.length() ?: 0L,
                                file = latestFile,
                                publicDisplayPath = "Downloads/Folio/$cleanFileName"
                            ),
                            snackbarMessage = null
                        )
                    }
                    refreshAutoBackupFiles()
                },
                onFailure = { error ->
                    controlState.update {
                        it.copy(
                            isBusyWithFileOperation = false,
                            fileOperationStatusLabel = null,
                            lastCompletedFileForShare = null,
                            lastExportedFileSuccess = null,
                            snackbarMessage = error.message ?: "Backup creation failed"
                        )
                    }
                }
            )
        }
    }

    fun getLatestBackupFile(): File? = settingsAndBackupRepository.getLatestBackupFile()

    fun clearLastCompletedFileForShare() {
        controlState.update { it.copy(lastCompletedFileForShare = null) }
    }

    fun clearLastExportedFileSuccess() {
        controlState.update { it.copy(lastExportedFileSuccess = null) }
    }

    fun requestRestoreConfirmation(sourceUriString: String) {
        controlState.update { it.copy(pendingRestoreUriString = sourceUriString) }
    }

    fun cancelPendingRestore() {
        controlState.update { it.copy(pendingRestoreUriString = null) }
    }

    fun confirmRestoreFromZipBackup() {
        val sourceUri = controlState.value.pendingRestoreUriString ?: return
        viewModelScope.launch {
            controlState.update {
                it.copy(
                    pendingRestoreUriString = null,
                    isBusyWithFileOperation = true,
                    fileOperationStatusLabel = "Restoring ledger database from ZIP backup..."
                )
            }
            val result = settingsAndBackupRepository.restoreFromZipBackup(sourceUri)
            result.fold(
                onSuccess = {
                    controlState.update {
                        it.copy(
                            isBusyWithFileOperation = false,
                            fileOperationStatusLabel = null,
                            snackbarMessage = "Ledger database & settings restored successfully"
                        )
                    }
                    refreshAutoBackupFiles()
                },
                onFailure = { error ->
                    controlState.update {
                        it.copy(
                            isBusyWithFileOperation = false,
                            fileOperationStatusLabel = null,
                            snackbarMessage = error.message ?: "Restore from ZIP backup failed"
                        )
                    }
                }
            )
        }
    }

    fun setAutoBackupEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsAndBackupRepository.setAutoBackupEnabled(enabled)
            refreshAutoBackupFiles()
        }
    }

    fun setAutoBackupRetention(retention: AutoBackupRetention) {
        viewModelScope.launch {
            settingsAndBackupRepository.setAutoBackupRetention(retention)
            refreshAutoBackupFiles()
        }
    }

    fun refreshAutoBackupFiles() {
        viewModelScope.launch {
            val files = settingsAndBackupRepository.listAutoBackupFiles()
            controlState.update { it.copy(autoBackupFiles = files) }
        }
    }

    fun exportAllTransactionsToCsv(destinationUriString: String) {
        viewModelScope.launch {
            controlState.update {
                it.copy(
                    isBusyWithFileOperation = true,
                    fileOperationStatusLabel = "Exporting full ledger to CSV..."
                )
            }
            val result = settingsAndBackupRepository.exportTransactionsToCsv(
                destinationUriString = destinationUriString,
                startEpochDay = null,
                endEpochDay = null
            )
            result.fold(
                onSuccess = { count ->
                    val cleanFileName = destinationUriString
                        .substringAfterLast('/')
                        .substringAfterLast('\\')
                        .ifBlank { "Folio_Ledger_Export.csv" }
                    val exportedFile = settingsAndBackupRepository.getExportedFile(cleanFileName)
                    controlState.update {
                        it.copy(
                            isBusyWithFileOperation = false,
                            fileOperationStatusLabel = null,
                            lastCompletedFileForShare = null,
                            lastExportedFileSuccess = FileExportSuccessUiModel(
                                title = "Ledger Export Complete!",
                                subtitle = "$count transactions exported to CSV",
                                fileName = cleanFileName,
                                fileSizeBytes = exportedFile?.length() ?: 0L,
                                file = exportedFile,
                                publicDisplayPath = "Downloads/Folio/$cleanFileName"
                            ),
                            snackbarMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    controlState.update {
                        it.copy(
                            isBusyWithFileOperation = false,
                            fileOperationStatusLabel = null,
                            lastCompletedFileForShare = null,
                            lastExportedFileSuccess = null,
                            snackbarMessage = error.message ?: "CSV export failed"
                        )
                    }
                }
            )
        }
    }

    fun downloadCsvTemplate(destinationFileName: String = "Folio_Import_Template.csv") {
        viewModelScope.launch {
            controlState.update {
                it.copy(
                    isBusyWithFileOperation = true,
                    fileOperationStatusLabel = "Generating CSV import template..."
                )
            }
            val result = settingsAndBackupRepository.exportCsvTemplate(destinationFileName)
            result.fold(
                onSuccess = { file ->
                    val cleanFileName = destinationFileName
                        .substringAfterLast('/')
                        .substringAfterLast('\\')
                        .ifBlank { "Folio_Import_Template.csv" }
                    val exportedFile = file ?: settingsAndBackupRepository.getExportedFile(cleanFileName)
                    controlState.update {
                        it.copy(
                            isBusyWithFileOperation = false,
                            fileOperationStatusLabel = null,
                            lastCompletedFileForShare = null,
                            lastExportedFileSuccess = FileExportSuccessUiModel(
                                title = "Template Downloaded!",
                                subtitle = "Side-by-side CSV template ready for entry",
                                fileName = cleanFileName,
                                fileSizeBytes = exportedFile?.length() ?: 0L,
                                file = exportedFile,
                                publicDisplayPath = "Downloads/Folio/$cleanFileName"
                            ),
                            snackbarMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    controlState.update {
                        it.copy(
                            isBusyWithFileOperation = false,
                            fileOperationStatusLabel = null,
                            lastCompletedFileForShare = null,
                            lastExportedFileSuccess = null,
                            snackbarMessage = error.message ?: "Failed to generate CSV template"
                        )
                    }
                }
            )
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            controlState.update {
                it.copy(
                    isBusyWithFileOperation = true,
                    fileOperationStatusLabel = "Clearing all local data..."
                )
            }
            val result = settingsAndBackupRepository.clearAllData()
            result.fold(
                onSuccess = {
                    controlState.update {
                        it.copy(
                            isBusyWithFileOperation = false,
                            fileOperationStatusLabel = null,
                            snackbarMessage = "All ledger data cleared and reset to default state"
                        )
                    }
                },
                onFailure = { error ->
                    controlState.update {
                        it.copy(
                            isBusyWithFileOperation = false,
                            fileOperationStatusLabel = null,
                            snackbarMessage = error.message ?: "Failed to clear local data"
                        )
                    }
                }
            )
        }
    }

    fun dismissWelcomeSheet() {
        viewModelScope.launch {
            settingsAndBackupRepository.updateUserPreferences { it.copy(hasSeenWelcomeSheet = true) }
        }
    }

    fun enterDemoMode(
        currencyCode: String? = null,
        numberGrouping: NumberGroupingSystem? = null,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            if (numberGrouping != null) {
                settingsAndBackupRepository.updateUserPreferences { it.copy(numberGrouping = numberGrouping) }
            }
            controlState.update {
                it.copy(
                    isBusyWithFileOperation = true,
                    fileOperationStatusLabel = "Preparing interactive demo sandbox..."
                )
            }
            val result = settingsAndBackupRepository.enterDemoMode(currencyCode)
            result.fold(
                onSuccess = {
                    controlState.update {
                        it.copy(
                            isBusyWithFileOperation = false,
                            fileOperationStatusLabel = null,
                            snackbarMessage = "🧪 Demo Sandbox active! Feel free to test."
                        )
                    }
                    onComplete()
                },
                onFailure = { error ->
                    controlState.update {
                        it.copy(
                            isBusyWithFileOperation = false,
                            fileOperationStatusLabel = null,
                            snackbarMessage = error.message ?: "Failed to enter demo mode"
                        )
                    }
                }
            )
        }
    }

    fun exitDemoMode(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            controlState.update {
                it.copy(
                    isBusyWithFileOperation = true,
                    fileOperationStatusLabel = "Restoring your real ledger..."
                )
            }
            val result = settingsAndBackupRepository.exitDemoMode()
            result.fold(
                onSuccess = {
                    controlState.update {
                        it.copy(
                            isBusyWithFileOperation = false,
                            fileOperationStatusLabel = null,
                            snackbarMessage = "Ledger restored successfully"
                        )
                    }
                    onComplete()
                },
                onFailure = { error ->
                    controlState.update {
                        it.copy(
                            isBusyWithFileOperation = false,
                            fileOperationStatusLabel = null,
                            snackbarMessage = error.message ?: "Failed to restore ledger"
                        )
                    }
                }
            )
        }
    }

    fun loadCsvPreview(uriString: String) {
        viewModelScope.launch {
            controlState.update {
                it.copy(
                    isBusyWithFileOperation = true,
                    fileOperationStatusLabel = "Analyzing CSV file..."
                )
            }
            val result = settingsAndBackupRepository.previewTransactionsCsv(uriString)
            result.fold(
                onSuccess = { preview ->
                    controlState.update {
                        it.copy(
                            isBusyWithFileOperation = false,
                            fileOperationStatusLabel = null,
                            csvPreview = preview
                        )
                    }
                },
                onFailure = { error ->
                    controlState.update {
                        it.copy(
                            isBusyWithFileOperation = false,
                            fileOperationStatusLabel = null,
                            snackbarMessage = error.message ?: "Failed to read CSV file"
                        )
                    }
                }
            )
        }
    }

    fun confirmCsvImport(skipDuplicates: Boolean = true) {
        val preview = controlState.value.csvPreview ?: return
        viewModelScope.launch {
            controlState.update {
                it.copy(
                    csvPreview = null,
                    isBusyWithFileOperation = true,
                    fileOperationStatusLabel = "Importing transactions to ledger..."
                )
            }
            val result = settingsAndBackupRepository.importTransactionsFromCsv(
                sourceUriString = preview.sourceUriString,
                skipDuplicates = skipDuplicates
            )
            result.fold(
                onSuccess = { importResult ->
                    controlState.update {
                        it.copy(
                            isBusyWithFileOperation = false,
                            fileOperationStatusLabel = null,
                            csvImportResult = importResult,
                            snackbarMessage = "Imported ${importResult.importedCount} transactions"
                        )
                    }
                },
                onFailure = { error ->
                    controlState.update {
                        it.copy(
                            isBusyWithFileOperation = false,
                            fileOperationStatusLabel = null,
                            snackbarMessage = error.message ?: "CSV import failed"
                        )
                    }
                }
            )
        }
    }

    fun dismissCsvPreview() {
        controlState.update { it.copy(csvPreview = null) }
    }

    fun dismissCsvImportResult() {
        controlState.update { it.copy(csvImportResult = null) }
    }

    fun clearSnackbarMessage() {
        controlState.update { it.copy(snackbarMessage = null) }
    }

    // ---------------------------------------------------------------------------------------------
    // PDF Statements & Tax Reports Actions
    // ---------------------------------------------------------------------------------------------

    fun openPdfReportsSheet() {
        controlState.update { it.copy(isPdfReportsSheetOpen = true) }
    }

    fun closePdfReportsSheet() {
        controlState.update { it.copy(isPdfReportsSheetOpen = false) }
    }

    fun clearLastGeneratedPdfResult() {
        controlState.update { it.copy(lastGeneratedPdfResult = null) }
    }

    fun generateAccountStatementPdf(
        accountId: Long,
        startEpochDay: Long,
        endEpochDay: Long,
        includeNotesAndTags: Boolean = true,
        includeRunningBalance: Boolean = true
    ) {
        viewModelScope.launch {
            controlState.update {
                it.copy(
                    isBusyWithFileOperation = true,
                    isGeneratingPdf = true,
                    fileOperationStatusLabel = "Generating PDF Statement..."
                )
            }
            val result = settingsAndBackupRepository.generateAccountStatementPdf(
                accountId = accountId,
                startEpochDay = startEpochDay,
                endEpochDay = endEpochDay,
                includeNotesAndTags = includeNotesAndTags,
                includeRunningBalance = includeRunningBalance
            )
            result.fold(
                onSuccess = { pdfResult ->
                    controlState.update {
                        it.copy(
                            isBusyWithFileOperation = false,
                            isGeneratingPdf = false,
                            fileOperationStatusLabel = null,
                            lastGeneratedPdfResult = pdfResult,
                            lastCompletedFileForShare = pdfResult.file,
                            snackbarMessage = "Saved to ${pdfResult.publicDisplayPath}"
                        )
                    }
                },
                onFailure = { error ->
                    controlState.update {
                        it.copy(
                            isBusyWithFileOperation = false,
                            isGeneratingPdf = false,
                            fileOperationStatusLabel = null,
                            snackbarMessage = error.message ?: "Failed to generate PDF statement"
                        )
                    }
                }
            )
        }
    }

    fun generateAnnualTaxStatementPdf(
        fiscalYearType: FiscalYearType,
        anchorYear: Int,
        customStartEpochDay: Long? = null,
        customEndEpochDay: Long? = null,
        selectedTaxTags: Set<String> = emptySet()
    ) {
        viewModelScope.launch {
            controlState.update {
                it.copy(
                    isBusyWithFileOperation = true,
                    isGeneratingPdf = true,
                    fileOperationStatusLabel = "Generating Tax & Financial Report..."
                )
            }
            val result = settingsAndBackupRepository.generateAnnualTaxStatementPdf(
                fiscalYearType = fiscalYearType,
                anchorYear = anchorYear,
                customStartEpochDay = customStartEpochDay,
                customEndEpochDay = customEndEpochDay,
                selectedTaxTags = selectedTaxTags
            )
            result.fold(
                onSuccess = { pdfResult ->
                    controlState.update {
                        it.copy(
                            isBusyWithFileOperation = false,
                            isGeneratingPdf = false,
                            fileOperationStatusLabel = null,
                            lastGeneratedPdfResult = pdfResult,
                            lastCompletedFileForShare = pdfResult.file,
                            snackbarMessage = "Saved to ${pdfResult.publicDisplayPath}"
                        )
                    }
                },
                onFailure = { error ->
                    controlState.update {
                        it.copy(
                            isBusyWithFileOperation = false,
                            isGeneratingPdf = false,
                            fileOperationStatusLabel = null,
                            snackbarMessage = error.message ?: "Failed to generate Tax Report"
                        )
                    }
                }
            )
        }
    }

    private data class CategoriesAndMetadataBundle(
        val expenseCategories: List<CategoryNode>,
        val incomeCategories: List<CategoryNode>,
        val tags: List<String>,
        val bookmarks: List<TransactionBookmark>
    )
}