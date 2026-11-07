package com.folioledger.app.ui.accounts

import androidx.compose.runtime.Immutable
import com.folioledger.app.domain.model.AccountGroup
import com.folioledger.app.domain.model.AccountSummary
import com.folioledger.app.domain.model.CurrencySpec
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.NetWorthOverview
import com.folioledger.app.domain.model.NumberGroupingSystem
import com.folioledger.app.domain.model.SecondaryCurrency
import com.folioledger.app.domain.model.SupportedCurrencies
import com.folioledger.app.domain.model.TransactionItem
import java.time.LocalDate

@Immutable
data class PassbookPeriodRange(
    val startEpochDay: Long,
    val endEpochDay: Long,
    val periodTitle: String,
    val periodSubtitle: String
)

@Immutable
data class AccountPassbookEntryUiModel(
    val transaction: TransactionItem,
    val signedDelta: MinorMoney,
    val isInflow: Boolean,
    val runningBalanceAfter: MinorMoney,
    val viaLinkedDebitCardName: String? = null
)

@Immutable
data class AccountPassbookDayGroup(
    val epochDay: Long,
    val dateHeaderTitle: String,
    val dayOfMonthText: String,
    val dayOfWeekText: String,
    val monthYearText: String,
    val isToday: Boolean,
    val dayInflowTotal: MinorMoney,
    val dayOutflowTotal: MinorMoney,
    val dayEndRunningBalance: MinorMoney,
    val entries: List<AccountPassbookEntryUiModel>
)

@Immutable
data class AccountPassbookUiModel(
    val account: AccountSummary,
    val parentBankAccount: AccountSummary? = null,
    val linkedDebitCards: List<AccountSummary> = emptyList(),
    val monthOffset: Int = 0,
    val startEpochDay: Long = 0L,
    val endEpochDay: Long = 0L,
    val periodTitle: String = "",
    val periodSubtitle: String = "",
    val currentLiveBalance: MinorMoney = MinorMoney.ZERO,
    val periodOpeningBalance: MinorMoney = MinorMoney.ZERO,
    val periodInflows: MinorMoney = MinorMoney.ZERO,
    val periodOutflows: MinorMoney = MinorMoney.ZERO,
    val periodNetFlow: MinorMoney = MinorMoney.ZERO,
    val periodClosingBalance: MinorMoney = MinorMoney.ZERO,
    val totalPeriodTransactionCount: Int = 0,
    val dayGroups: List<AccountPassbookDayGroup> = emptyList(),
    val entries: List<AccountPassbookEntryUiModel> = emptyList(),
    val investedPrincipal: MinorMoney? = null,
    val unrealizedPnl: MinorMoney? = null,
    val returnPercentage: Float? = null
)

@Immutable
data class AccountEditorDialogState(
    val isEditing: Boolean = false,
    val accountId: Long = 0L,
    val name: String = "",
    val groupType: AccountGroup = AccountGroup.BANK_ACCOUNT,
    val currencyCode: String = "INR",
    val openingBalanceInput: String = "0",
    val openingDateEpochDay: Long = LocalDate.now().toEpochDay(),
    val creditLimitInput: String = "",
    val statementPayableInput: String = "",
    val unbilledAmountInput: String = "",
    val settlementDayOfMonth: Int = 18,
    val paymentDueDayOfMonth: Int = 5,
    val parentAccountId: Long? = null,
    val includeInNetWorth: Boolean = true,
    val isPinned: Boolean = false,
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
data class ReconcileBalanceDialogState(
    val accountId: Long,
    val accountName: String,
    val currencyCode: String,
    val recordedBalance: MinorMoney,
    val targetBalanceInput: String = "",
    val reconcileDateEpochDay: Long = LocalDate.now().toEpochDay(),
    val note: String = "",
    val validationError: String? = null,
    val isLiability: Boolean = false,
    val isInvestment: Boolean = false
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
    val inspectedAccount: AccountSummary? = null,
    val passbookMonthOffset: Int = 0,
    val accountPassbook: AccountPassbookUiModel? = null,
    val currencySpec: CurrencySpec = SupportedCurrencies.INR,
    val primaryCurrencyCode: String = "INR",
    val secondaryCurrencies: List<SecondaryCurrency> = emptyList(),
    val numberGrouping: NumberGroupingSystem = NumberGroupingSystem.INDIAN,
    val referenceDateEpochDay: Long = LocalDate.now().toEpochDay(),
    val showArchivedAccounts: Boolean = false,
    val collapsedGroups: Set<AccountGroup> = emptySet(),
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
