package com.folioledger.app.domain.parser

import androidx.compose.runtime.Immutable
import com.folioledger.app.domain.model.AccountSummary
import com.folioledger.app.domain.model.CategoryNode
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.TransactionType

/**
 * Raw extraction results from parsing financial text, SMS, or clipboard snippet.
 */
@Immutable
data class ParsedTransactionResult(
    val rawText: String,
    val amountMinor: MinorMoney? = null,
    val rawAmountString: String? = null,
    val currencyCode: String? = null,
    val type: TransactionType = TransactionType.EXPENSE,
    val typeTriggerWord: String? = null,
    val payeeOrDescription: String = "",
    val rawVendorSnippet: String? = null,
    val accountSuffix: String? = null,
    val bankInstitution: String? = null,
    val dateEpochDay: Long? = null,
    val discardedBalanceSnippet: String? = null,
    val confidence: Float = 0f
) {
    val isValid: Boolean get() = amountMinor != null && amountMinor.isPositive
}

/**
 * Resolved form values after matching raw parser hints against existing accounts,
 * categories, and user preferences.
 */
@Immutable
data class ResolvedTransactionFormValues(
    val parsedResult: ParsedTransactionResult,
    val matchedAccount: AccountSummary? = null,
    val matchedCategory: CategoryNode? = null,
    val matchedParentCategoryName: String? = null,
    val categoryMatchSource: CategoryMatchSource = CategoryMatchSource.NONE
)

enum class CategoryMatchSource {
    NONE,
    HISTORICAL,
    SEMANTIC_KEYWORD
}
