package com.folioledger.app.ui.entry

import androidx.compose.runtime.Immutable
import com.folioledger.app.domain.model.AccountGroup
import com.folioledger.app.domain.model.AccountSummary
import com.folioledger.app.domain.model.CategoryNode
import com.folioledger.app.domain.model.CurrencySpec
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.NumberGroupingSystem
import com.folioledger.app.domain.model.RepeatFrequency
import com.folioledger.app.domain.model.SupportedCurrencies
import com.folioledger.app.domain.model.TransactionBookmark
import com.folioledger.app.domain.model.TransactionType
import java.time.LocalDate

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
    val isRecurring: Boolean = false,
    val recurringFrequency: RepeatFrequency = RepeatFrequency.EVERY_MONTH,
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
            TransactionType.TRANSFER, TransactionType.CC_SETTLEMENT,
            TransactionType.OPENING_BALANCE, TransactionType.RECONCILIATION -> expenseCategories
        }

    val isCreditCardExpense: Boolean
        get() = mode == TransactionType.EXPENSE &&
                selectedAccount?.groupType == AccountGroup.CREDIT_CARD
}
