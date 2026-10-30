package com.folioledger.app.data.pdf

import com.folioledger.app.domain.model.CurrencySpec
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.NumberGroupingSystem
import com.folioledger.app.domain.model.SecondaryCurrency
import com.folioledger.app.ui.accounts.AccountPassbookEntryUiModel
import java.io.File

/**
 * Supported fiscal year calendar definitions for universal tax reporting.
 */
enum class FiscalYearType(val displayName: String, val description: String) {
    CALENDAR_YEAR("Calendar Year (Jan – Dec)", "Standard for USA, Europe, Latin America, East Asia"),
    APRIL_FISCAL_YEAR("Fiscal Year (Apr – Mar)", "Standard for India, UK, corporate Japan & Canada"),
    JULY_FISCAL_YEAR("Fiscal Year (Jul – Jun)", "Standard for Australia, New Zealand, Egypt"),
    CUSTOM_RANGE("Custom Date Range", "Custom start and end dates for non-standard fiscal audits")
}

/**
 * Period presets for account passbook statement exports.
 */
enum class PassbookPeriodPreset(val displayName: String) {
    CURRENT_MONTH("This Month"),
    LAST_MONTH("Last Month"),
    LAST_THREE_MONTHS("Last 3 Months"),
    YEAR_TO_DATE("Year to Date"),
    ALL_TIME("All Time"),
    CUSTOM_RANGE("Custom Range")
}

/**
 * Data bundle for rendering an official Account Passbook Statement PDF.
 */
data class AccountStatementPdfData(
    val accountName: String,
    val accountGroupTitle: String,
    val institutionOrDetails: String?,
    val periodTitle: String,
    val periodSubtitle: String,
    val currencySpec: CurrencySpec,
    val numberGrouping: NumberGroupingSystem,
    val openingBalance: MinorMoney,
    val totalInflows: MinorMoney,
    val totalOutflows: MinorMoney,
    val netPeriodFlow: MinorMoney,
    val closingBalance: MinorMoney,
    val transactionCount: Int,
    val includeRunningBalance: Boolean = true,
    val includeNotesAndTags: Boolean = true,
    val isCreditCard: Boolean = false,
    val creditCardLimit: MinorMoney? = null,
    val creditCardStatementPayable: MinorMoney? = null,
    val creditCardCycleText: String? = null,
    val creditCardDueDateText: String? = null,
    val entries: List<AccountPassbookEntryUiModel> = emptyList(),
    val generatedTimestampText: String
)

/**
 * Category breakdown item for annual tax/financial statements.
 */
data class CategoryAggregateItem(
    val categoryName: String,
    val count: Int,
    val amountMinor: Long,
    val convertedAmountMinor: Long,
    val percentageOfTotal: Float
)

/**
 * Tax-deductible transaction line item.
 */
data class TaxDeductionPdfItem(
    val dateText: String,
    val payee: String,
    val categoryName: String,
    val tagOrDeductionType: String,
    val note: String?,
    val amountMinor: Long,
    val currencyCode: String,
    val convertedMinor: Long
)

/**
 * Account balance snapshot at fiscal year end.
 */
data class AccountSnapshotPdfItem(
    val accountName: String,
    val groupName: String,
    val currencyCode: String,
    val nativeBalanceMinor: Long,
    val convertedBalanceMinor: Long,
    val isAsset: Boolean
)

/**
 * Data bundle for rendering a Universal Annual Financial & Tax Statement PDF.
 */
data class AnnualTaxPdfData(
    val fiscalPeriodTitle: String,
    val fiscalPeriodSubtitle: String,
    val fiscalYearType: FiscalYearType,
    val primaryCurrencySpec: CurrencySpec,
    val secondaryCurrencies: List<SecondaryCurrency> = emptyList(),
    val numberGrouping: NumberGroupingSystem,
    val grossInflowsMinor: Long,
    val totalExpensesMinor: Long,
    val netSavingsMinor: Long,
    val savingsRatePercent: Float,
    val incomeSources: List<CategoryAggregateItem> = emptyList(),
    val expenseCategories: List<CategoryAggregateItem> = emptyList(),
    val taxDeductions: List<TaxDeductionPdfItem> = emptyList(),
    val totalTaxDeductionsMinor: Long,
    val accountsSnapshot: List<AccountSnapshotPdfItem> = emptyList(),
    val totalAssetsMinor: Long,
    val totalLiabilitiesMinor: Long,
    val netWorthMinor: Long,
    val valuationExchangeNotes: List<String> = emptyList(),
    val generatedTimestampText: String
)

/**
 * Result bundle after generating a PDF.
 */
data class PdfExportResult(
    val file: File,
    val publicDisplayPath: String,
    val fileName: String,
    val fileSizeBytes: Long,
    val pageCount: Int
)
