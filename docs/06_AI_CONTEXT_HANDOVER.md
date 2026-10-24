# FOLIO LEDGER — AI CONTEXT HANDOVER CARD (v1.3.0+)

## 1. Project Identity & Tech Stack
- **App Name:** Folio: Expense & Budget Ledger
- **Play Store Application ID (`applicationId`):** `com.skal.folio`
- **Kotlin Source Package / Namespace:** `com.folioledger.app`
- **Architecture:** 100% Offline-First, Single-Activity MVVM, Jetpack Compose (Material 3), Room SQLite (`FolioDatabase` v2), Preferences DataStore, Manual DI (`AppContainer` + `FolioViewModelFactory`).
- **Build Config:** Min SDK 29 (Android 10), Target/Compile SDK 37, Kotlin 2.x, KSP, `activityCompose = "1.10.1"`, AndroidX `core-splashscreen = "1.0.1"`.
- **Build & Compilation Verification Command:** `./gradlew app:compileDebugKotlin`
- **Strict Coding Rule:** Always output 100% complete replacement files from line 1 (`// File: ...`) to the final closing brace (`Ctrl + A -> Delete -> Paste`). Never output partial snippets or `TODO()` placeholders.

## 2. Core Accounting & Domain Invariants
1. **Minor-Unit Money (`MinorMoney`):** `@JvmInline value class MinorMoney(val amountMinor: Long)` using `Math.addExact` / `Math.subtractExact`. Never use `Double`/`Float` for currency math.
2. **Transfer Neutrality:** `TRANSFER` transactions move balance between accounts; only `transferFee` counts as period expense.
3. **Linked Debit Cards (`DEBIT_CARD`):** Linked via `parentAccountId` to a parent Bank Account. Expenses deduct directly from the parent bank balance while tracking card-specific period spend.
4. **Credit Card Engine (`CREDIT_CARD`):** Tracks `creditLimit`, `settlementDayOfMonth (1..31)`, `paymentDueDayOfMonth (1..31)`, Statement Payable vs. Unbilled Spend (`initialUnbilledMinor`), and 1-tap `CC_SETTLEMENT` ("Pay Bill") which reduces bank asset and CC liability without double-counting expense. Auto-calculates `Total Debt Owed = Billed Payable + Unbilled Spend` live during creation/editing.
5. **Split, Refund & Installments:** Supports 1-to-N `SplitLineItem` transactions, `REFUND` transactions (offset category spend without inflating income), and remainder-safe Credit Card `InstallmentPlan` generation (first month absorbs `total % months`).
6. **Data Protection & Migrations:** `FolioDatabase` is at Version 2 with explicit `MIGRATION_1_2` (`ALTER TABLE accounts ADD COLUMN initialUnbilledMinor`). Destructive fallback is guarded by `BuildConfig.DEBUG`. Includes `DatabaseBackupManager` for automatic pre-migration `.pre_migration_bak` SQLite backups with fail-safe automatic rollback.
7. **Strict Ledger Deletion Safety Rules:**
   - Unused accounts with 0 transactions can be permanently deleted after user confirmation (`[ Delete Account ]`).
   - Accounts with 1+ linked transactions cannot be deleted to protect historical reports and ledger integrity. The app informs the user and guides them to **Archive Account** instead.
8. **Strict Transaction Isolation & Field Integrity:**
   - Non-transfer types (`EXPENSE`, `INCOME`, `REFUND`) strictly enforce `transferToAccountId = null`, `transferToAccountName = null`, `transferFee = 0L`.
   - Transfer types (`TRANSFER`, `CC_SETTLEMENT`) strictly enforce `categoryId = null`.
   - Passbook statements filter strictly by matching transaction types and exclude zero-delta entries (`deltaMinor != 0L`).

## 3. UI, Design System & Theme Engine Standards
1. **Header Layout Alignment Standard:** All top-level screen headers use `headlineSmall` (24sp Bold) with `padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp)` to align across `AccountsScreen`, `TransactionsScreen`, `StatsAndBudgetsScreen`, and `SettingsScreen`.
2. **Card Enclosure Standard:** All grouped cards use `surfaceContainerLow` with `BorderStroke(1.dp, semanticColors.cardStrokeSubtle)` and `18.dp` or `16.dp` rounded corners. This ensures cards maintain crisp, enclosed visual boundaries in both Light Theme (where background contrast is low) and Dark Theme.
3. **Transaction FAB Visibility Scoping (`FolioMainShell.kt`):** Restricted `FloatingActionButton` (+ New Transaction) strictly to `TRANSACTIONS` and `STATS` tabs using `AnimatedVisibility`. Hidden on `ACCOUNTS` and `SETTINGS`.
4. **Accounts Tab Design System:**
   - Section headers match `SettingsGroupCard` pattern (`labelLarge` uppercase primary titles: `CASH (1)`, `SAVINGS (2)`, `DEBIT CARDS (2)`). Whole-header row tap toggles expand/collapse without chevron buttons.
   - **Credit Cards**: Compact 2-column layout (`DUE NOW` & `UNBILLED`) with `Due [Date]` subtext. Smart color coding (`DUE NOW` in `expenseCoral` when > 0, `incomeEmerald` when <= 0; `UNBILLED` in `onSurfaceVariant`).
   - **Debit Cards**: Consolidated subtext (`HDFC Bank · Bal: ₹1,20,000.00`) with positive parent bank balance in `incomeEmerald` (or overdrawn in `expenseCoral`).
   - **Account Editor Sheet (`AccountEditorBottomSheet`)**: `Opening Balance` sits directly below `Account Name`. 10 account groups categorized under 3 Segment Tabs (`Liquid` | `Cards` | `Savings & Debt`). Auto-calculates Credit Card Total Debt in real time. Collapsible foreign currency picker and collapsible appearance customizer.
   - **Account Passbook Sheet (`AccountPassbookBottomSheet`)**: Header uncluttered by moving `Edit Account` to the Quick Action Bar (`Add Tx` | `Settle Bill` | `Edit Account`). Removed repeated `"Day End Bal:"` label prefix from day-grouped transaction section headers.
5. **Settings Package Extraction (`ui/settings/sheets/`):**
   - Extracted bottom sheets into `com.folioledger.app.ui.settings.sheets` package (`AppearanceBottomSheet`, `RegionalFormatBottomSheet`, `DataAndBackupBottomSheet`, `CategoryManagerBottomSheet`, `TagsAndBookmarksBottomSheet`), shrinking `SettingsScreen.kt` to ~600 lines.
6. **Complete 6-Theme Mode Engine (`FolioTheme.kt`):**
   - `SYSTEM`, `LIGHT`, `SEPIA`, `SLATE_GRAY` (Chrome Dark `#202124` palette), `DARK`, `AMOLED_BLACK`.
7. **12-Accent Color Palette Suite in Guaranteed 6x2 Grid (`FolioTheme.kt`):**
   - `EMERALD`, `COBALT`, `INDIGO`, `PURPLE`, `ROSE`, `LIME`, `CYAN`, `BRONZE`, `TEAL`, `CRIMSON`, `AMBER`, `SLATE`.

## 4. Package & File Map (`app/src/main/java/com/folioledger/app/`)
- **Root & DI:** `FolioApplication.kt`, `MainActivity.kt`, `di/AppContainer.kt`, `di/FolioViewModelFactory.kt`
- **Domain Layer (`domain/`):** `model/DomainEnumsAndMoney.kt`, `model/DomainModels.kt`, `util/MoneyFormatterAndCalculator.kt`, `usecase/AccountingRulesEngine.kt`, `repository/DomainRepositories.kt`
- **Data Layer (`data/`):** `local/FolioDatabase.kt` (v2, `MIGRATION_1_2`), `local/RoomTypeConverters.kt`, `local/entity/RoomEntities.kt`, `local/dao/RoomDaos.kt`, `repository/AccountAndTransactionRepositoriesImpl.kt`, `repository/AnalyticsAndBudgetRepositoryImpl.kt`, `repository/SettingsAndCategoryRepositoriesImpl.kt`
- **UI Layer (`ui/`):**
    - `theme/FolioTheme.kt` (6 Theme Modes, 12 Accent Palettes in 6x2 grid)
    - `components/FolioCommonComponents.kt` (`CategoryIconBadge`, `MoneyText`, `resolveFolioIcon`)
    - `shell/FolioMainShell.kt` (4 tabs + FAB scoping)
    - `ledger/` (`LedgerHomeScreen.kt`, `LedgerHeaderBanners.kt`, `LedgerItemCards.kt`, `LedgerSubTabs.kt`, `LedgerSheetsAndDialogs.kt`)
    - `entry/TransactionEntryViewModel.kt`, `entry/TransactionEntrySheet.kt`
    - `accounts/AccountsViewModel.kt`, `accounts/AccountsScreen.kt`, `accounts/components/AccountGroupSectionCard.kt`, `accounts/components/AccountRows.kt`, `accounts/sheets/AccountEditorBottomSheet.kt`, `accounts/sheets/AccountPassbookBottomSheet.kt`, `accounts/sheets/CreditCardSettlementBottomSheet.kt`
    - `stats/StatsAndBudgetsViewModel.kt`, `stats/StatsAndBudgetsScreen.kt`
    - `settings/SettingsViewModel.kt`, `settings/SettingsScreen.kt`, `settings/FolioGuideAndSupportSheets.kt`, `settings/sheets/AppearanceBottomSheet.kt`, `settings/sheets/RegionalFormatBottomSheet.kt`, `settings/sheets/DataAndBackupBottomSheet.kt`, `settings/sheets/CategoryManagerBottomSheet.kt`, `settings/sheets/TagsAndBookmarksBottomSheet.kt`
