package com.folioledger.app.ui.settings

import androidx.compose.runtime.Immutable
import com.folioledger.app.data.pdf.PdfExportResult
import com.folioledger.app.domain.model.AccountSummary
import com.folioledger.app.domain.model.CategoryNode
import com.folioledger.app.domain.model.CategoryType
import com.folioledger.app.domain.model.CurrencySpec
import com.folioledger.app.domain.model.ImportCsvPreview
import com.folioledger.app.domain.model.ImportCsvResult
import com.folioledger.app.domain.model.InstallmentPlanWithProgress
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.RecurringCommitmentItem
import com.folioledger.app.domain.model.SupportedCurrencies
import com.folioledger.app.domain.model.TransactionBookmark
import com.folioledger.app.domain.model.UserPreferences
import java.io.File

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
    val isSmartIconMatched: Boolean = false,
    val isIconManuallyOverridden: Boolean = false,
    val validationError: String? = null
)

@Immutable
data class SettingsUiState(
    val userPreferences: UserPreferences = UserPreferences(),
    val currencySpec: CurrencySpec = SupportedCurrencies.INR,
    val accounts: List<AccountSummary> = emptyList(),
    val expenseCategories: List<CategoryNode> = emptyList(),
    val incomeCategories: List<CategoryNode> = emptyList(),
    val tags: List<String> = emptyList(),
    val bookmarks: List<TransactionBookmark> = emptyList(),
    val recurringRules: List<RecurringCommitmentItem> = emptyList(),
    val installmentPlans: List<InstallmentPlanWithProgress> = emptyList(),
    val activeRecurringCount: Int = 0,
    val activeMonthlyRecurringBurden: MinorMoney = MinorMoney.ZERO,
    val activeMonthlyTransfersBurden: MinorMoney = MinorMoney.ZERO,
    val activeMonthlyInstallmentsBurden: MinorMoney = MinorMoney.ZERO,
    val totalMonthlyFixedBurden: MinorMoney = MinorMoney.ZERO,
    val formattedMonthlyBurden: String = "",
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
    val autoBackupFiles: List<File> = emptyList()
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
