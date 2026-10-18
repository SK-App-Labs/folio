// File: app/src/main/java/com/folioledger/app/ui/settings/SettingsViewModel.kt
package com.folioledger.app.ui.settings

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.folioledger.app.domain.model.CategoryNode
import com.folioledger.app.domain.model.CategoryType
import com.folioledger.app.domain.model.CurrencySpec
import com.folioledger.app.domain.model.NumberGroupingSystem
import com.folioledger.app.domain.model.SupportedCurrencies
import com.folioledger.app.domain.model.TransactionBookmark
import com.folioledger.app.domain.model.UserPreferences
import com.folioledger.app.domain.repository.CategoryAndTagRepository
import com.folioledger.app.domain.repository.SettingsAndBackupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class CategoryEditorDialogState(
    val isEditing: Boolean = false,
    val categoryId: Long = 0L,
    val name: String = "",
    val type: CategoryType = CategoryType.EXPENSE,
    val parentId: Long? = null,
    val parentCategoryName: String? = null,
    val iconName: String = "category",
    val colorHex: String = "#059669",
    val validationError: String? = null
)

@Immutable
data class SettingsUiState(
    val userPreferences: UserPreferences = UserPreferences(),
    val currencySpec: CurrencySpec = SupportedCurrencies.INR,
    val expenseCategories: List<CategoryNode> = emptyList(),
    val incomeCategories: List<CategoryNode> = emptyList(),
    val tags: List<String> = emptyList(),
    val bookmarks: List<TransactionBookmark> = emptyList(),
    val isCategoryManagerOpen: Boolean = false,
    val selectedCategoryManagerType: CategoryType = CategoryType.EXPENSE,
    val categoryEditorState: CategoryEditorDialogState? = null,
    val isTagManagerOpen: Boolean = false,
    val isBookmarkManagerOpen: Boolean = false,
    val pendingRestoreUriString: String? = null,
    val isBusyWithFileOperation: Boolean = false,
    val fileOperationStatusLabel: String? = null,
    val snackbarMessage: String? = null
) {
    val activeManagedCategories: List<CategoryNode>
        get() = when (selectedCategoryManagerType) {
            CategoryType.EXPENSE -> expenseCategories
            CategoryType.INCOME -> incomeCategories
        }

    val totalExpenseCategoryCount: Int
        get() = expenseCategories.sumOf { 1 + it.subcategories.size }

    val totalIncomeCategoryCount: Int
        get() = incomeCategories.sumOf { 1 + it.subcategories.size }
}

private data class SettingsInternalControlState(
    val isCategoryManagerOpen: Boolean = false,
    val selectedCategoryManagerType: CategoryType = CategoryType.EXPENSE,
    val categoryEditorState: CategoryEditorDialogState? = null,
    val isTagManagerOpen: Boolean = false,
    val isBookmarkManagerOpen: Boolean = false,
    val pendingRestoreUriString: String? = null,
    val isBusyWithFileOperation: Boolean = false,
    val fileOperationStatusLabel: String? = null,
    val snackbarMessage: String? = null
)

class SettingsViewModel(
    private val settingsAndBackupRepository: SettingsAndBackupRepository,
    private val categoryAndTagRepository: CategoryAndTagRepository
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

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsAndBackupRepository.observeUserPreferences(),
        categoriesAndMetadataFlow,
        controlState
    ) { prefs, metadata, ctrl ->
        val currencySpec = SupportedCurrencies.fromCode(prefs.primaryCurrencyCode)
        SettingsUiState(
            userPreferences = prefs,
            currencySpec = currencySpec,
            expenseCategories = metadata.expenseCategories,
            incomeCategories = metadata.incomeCategories,
            tags = metadata.tags,
            bookmarks = metadata.bookmarks,
            isCategoryManagerOpen = ctrl.isCategoryManagerOpen,
            selectedCategoryManagerType = ctrl.selectedCategoryManagerType,
            categoryEditorState = ctrl.categoryEditorState,
            isTagManagerOpen = ctrl.isTagManagerOpen,
            isBookmarkManagerOpen = ctrl.isBookmarkManagerOpen,
            pendingRestoreUriString = ctrl.pendingRestoreUriString,
            isBusyWithFileOperation = ctrl.isBusyWithFileOperation,
            fileOperationStatusLabel = ctrl.fileOperationStatusLabel,
            snackbarMessage = ctrl.snackbarMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = SettingsUiState()
    )

    // UserPreferences Update Actions

    fun setPrimaryCurrency(currencyCode: String) {
        val spec = SupportedCurrencies.fromCode(currencyCode)
        viewModelScope.launch {
            settingsAndBackupRepository.updateUserPreferences { current ->
                current.copy(
                    primaryCurrencyCode = spec.code,
                    numberGrouping = spec.defaultGrouping
                )
            }
            controlState.update {
                it.copy(snackbarMessage = "Primary currency set to ${spec.displayName} (${spec.symbol})")
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
        val safeDay = day.coerceIn(1, 28)
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

    // 2-Level Category & Subcategory CRUD Actions

    fun setCategoryManagerOpen(
        open: Boolean,
        initialType: CategoryType = uiState.value.selectedCategoryManagerType
    ) {
        controlState.update {
            it.copy(
                isCategoryManagerOpen = open,
                selectedCategoryManagerType = initialType,
                categoryEditorState = if (!open) null else it.categoryEditorState
            )
        }
    }

    fun selectCategoryManagerType(type: CategoryType) {
        controlState.update { it.copy(selectedCategoryManagerType = type) }
    }

    fun openCreateMainCategoryDialog(
        type: CategoryType = uiState.value.selectedCategoryManagerType
    ) {
        val defaultColor = if (type == CategoryType.EXPENSE) "#E11D48" else "#059669"
        val defaultIcon = if (type == CategoryType.EXPENSE) "shopping_cart" else "payments"
        controlState.update {
            it.copy(
                categoryEditorState = CategoryEditorDialogState(
                    isEditing = false,
                    categoryId = 0L,
                    name = "",
                    type = type,
                    parentId = null,
                    parentCategoryName = null,
                    iconName = defaultIcon,
                    colorHex = defaultColor,
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
                    validationError = null
                )
            )
        }
    }

    fun updateCategoryEditorState(
        transform: (CategoryEditorDialogState) -> CategoryEditorDialogState
    ) {
        controlState.update { current ->
            val existing = current.categoryEditorState ?: return@update current
            current.copy(categoryEditorState = transform(existing).copy(validationError = null))
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
                onSuccess = { summary ->
                    controlState.update {
                        it.copy(
                            isBusyWithFileOperation = false,
                            fileOperationStatusLabel = null,
                            snackbarMessage = summary.ifBlank {
                                "Portable ZIP backup created successfully"
                            }
                        )
                    }
                },
                onFailure = { error ->
                    controlState.update {
                        it.copy(
                            isBusyWithFileOperation = false,
                            fileOperationStatusLabel = null,
                            snackbarMessage = error.message ?: "Backup creation failed"
                        )
                    }
                }
            )
        }
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
                    controlState.update {
                        it.copy(
                            isBusyWithFileOperation = false,
                            fileOperationStatusLabel = null,
                            snackbarMessage = "Exported $count ledger transactions to CSV"
                        )
                    }
                },
                onFailure = { error ->
                    controlState.update {
                        it.copy(
                            isBusyWithFileOperation = false,
                            fileOperationStatusLabel = null,
                            snackbarMessage = error.message ?: "CSV export failed"
                        )
                    }
                }
            )
        }
    }

    fun clearSnackbarMessage() {
        controlState.update { it.copy(snackbarMessage = null) }
    }

    private data class CategoriesAndMetadataBundle(
        val expenseCategories: List<CategoryNode>,
        val incomeCategories: List<CategoryNode>,
        val tags: List<String>,
        val bookmarks: List<TransactionBookmark>
    )

    companion object {
        fun provideFactory(
            settingsAndBackupRepository: SettingsAndBackupRepository,
            categoryAndTagRepository: CategoryAndTagRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SettingsViewModel(
                    settingsAndBackupRepository = settingsAndBackupRepository,
                    categoryAndTagRepository = categoryAndTagRepository
                ) as T
            }
        }
    }
}