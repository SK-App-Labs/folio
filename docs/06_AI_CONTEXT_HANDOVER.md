# FOLIO LEDGER — AI CONTEXT HANDOVER CARD (v1.5.0+)

## 1. Project Identity & Tech Stack
- **App Name:** Folio: Expense & Budget Ledger
- **Play Store Application ID (`applicationId`):** `com.skal.folio`
- **Kotlin Source Package / Namespace:** `com.folioledger.app`
- **Architecture:** 100% Offline-First, Single-Activity MVVM, Jetpack Compose (Material 3), Room SQLite (`FolioDatabase` v2), Preferences DataStore, Manual DI (`AppContainer` + `FolioViewModelFactory`).
- **Build Config:** Min SDK 29 (Android 10), Target/Compile SDK 37, Kotlin 2.x, KSP, `activityCompose = "1.10.1"`, AndroidX `core-splashscreen = "1.0.1"`.
- **Build & Compilation Verification Command:** `./gradlew app:compileDebugKotlin`
- **Unit Test Execution Command:** `$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'; .\gradlew testDebugUnitTest` (243/243 passing tests)
- **Strict Coding Rule:** Always output 100% complete replacement files from line 1 (`// File: ...`) to the final closing brace (`Ctrl + A -> Delete -> Paste`). Never output partial snippets or `TODO()` placeholders.

## 2. Core Accounting & Domain Invariants
1. **Minor-Unit Money (`MinorMoney`):** `@JvmInline value class MinorMoney(val amountMinor: Long)` using `Math.addExact` / `Math.subtractExact`. Never use `Double`/`Float` for currency math.
2. **Transfer Neutrality:** `TRANSFER` transactions move balance between accounts; only `transferFee` counts as period expense.
3. **Linked Debit Cards (`DEBIT_CARD`):** Linked via `parentAccountId` to a parent Bank Account. Expenses deduct directly from the parent bank balance while tracking card-specific period spend.
4. **Credit Card Engine (`CREDIT_CARD`):** Tracks `creditLimit`, `settlementDayOfMonth (1..31)`, `paymentDueDayOfMonth (1..31)`, Statement Payable vs. Unbilled Spend (`initialUnbilledMinor`), and 1-tap `CC_SETTLEMENT` ("Pay Bill") which reduces bank asset and CC liability without double-counting expense. Auto-calculates `Total Debt Owed = Billed Payable + Unbilled Spend` live during creation/editing.
5. **Credit Bureau Utilization Calibration:** Classifies credit cards into 4 bureau risk tiers: `SAFE` ($<30\%$), `CAUTION` ($30\%-50\%$), `WARNING` ($50\%-75\%$), and `CRITICAL` ($\ge 75\%$). Provides **Smart Swipe Headroom** metric calculating the unencumbered spend capacity before hitting the 30% credit penalty threshold.
6. **Split, Refund & Installments:** Supports 1-to-N `SplitLineItem` transactions, `REFUND` transactions (offset category spend without inflating income), and remainder-safe Credit Card `InstallmentPlan` generation (first month absorbs `total % months`).
7. **Data Protection & Migrations:** `FolioDatabase` is at Version 2 with explicit `MIGRATION_1_2` (`ALTER TABLE accounts ADD COLUMN initialUnbilledMinor`). Destructive fallback is guarded by `BuildConfig.DEBUG`. Includes `DatabaseBackupManager` for automatic pre-migration `.pre_migration_bak` SQLite backups with fail-safe automatic rollback.
8. **Strict Ledger Deletion Safety Rules:**
   - Unused accounts with 0 transactions can be permanently deleted after user confirmation (`[ Delete Account ]`).
   - Accounts with 1+ linked transactions cannot be deleted to protect historical reports and ledger integrity. The app informs the user and guides them to **Archive Account** instead.
9. **Strict Transaction Isolation, System Types & Field Integrity:**
   - Non-transfer types (`EXPENSE`, `INCOME`, `REFUND`, `OPENING_BALANCE`, `RECONCILIATION`) strictly enforce `transferToAccountId = null`, `transferToAccountName = null`, `transferFee = 0L`.
   - Transfer types (`TRANSFER`, `CC_SETTLEMENT`) strictly enforce `categoryId = null`.
   - System entries (`OPENING_BALANCE`, `RECONCILIATION`) represent capital anchors and balance adjustments. They are strictly isolated from operating cashflow, category spend stats, and budget progress calculations.
   - Passbook statements filter strictly by matching transaction types and exclude zero-delta entries (`deltaMinor != 0L`).
10. **Financial Month Calculations (Days 1–31):**
    - `AccountingRulesEngine.computeFinancialMonthRange` supports custom financial start days from `1` to `31`.
    - Clamps desired start days to `minOf(desiredDay, yearMonth.lengthOfMonth())`, preventing runtime exceptions on short months (February, April, June, September, November) and leap years.
11. **Multi-Currency Secondary Conversion:** Foreign account balances and transactions dynamically convert into primary currency using deterministic manual exchange rates stored in DataStore (`prefs.secondaryCurrencies`). Eliminates distorted 1:1 cross-currency additions across Net Worth, Category Spend, Budgets, and Payee rankings.
12. **Predictive Financial Intelligence Suite:**
    - *Net Worth Velocity & Emergency Runway*: Computes month-over-month wealth delta, 3-month trailing expense burn rate, and liquid cash emergency runway in months.
    - *Merchant & Payee Intelligence*: Dual sorting (Outlay vs. Frequency) with recurring cadence detection.
    - *Events, Trips & Reimbursements*: Smart mode detection differentiating reimbursable claims (computing net pending owed) from event budgets (computing daily burn rate and date span).
    - *Safe-to-Spend & 14-Day Overdraft Radar*: Calculates unencumbered liquid cash deducting credit card debt and upcoming 14-day recurring commitments. Fires `AccountShortfallAlert` when any account faces a projected negative balance.
13. **Interactive Demo Mode & Auto-Stash Sandbox Architecture:**
    - Express onboarding via `WelcomeExpressBottomSheet` allows immediate preview with live sample data or starting fresh.
    - Headless `DemoModeManager` manages state, while `DemoDataSeeder` seeds ~45 transactions across 90 days relative to `LocalDate.now()` with currency scaling (`scaleMinor`).
    - Pre-demo real data is stashed to `noBackupFilesDir/demo_stash/pre_demo_real_ledger.zip` via an atomic `.tmp` swap. Table wipe occurs in strict reverse-topological order. `AutoBackupManager` is paused during demo mode. Real user data is seamlessly restored on exiting demo mode.

## 3. UI, Design System & Theme Engine Standards
1. **Header Layout Alignment Standard:** All top-level screen headers use `headlineSmall` (24sp Bold) with `padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp)` to align across `AccountsScreen`, `TransactionsScreen`, `StatsAndBudgetsScreen`, and `SettingsScreen`.
2. **Card Enclosure Standard:** All grouped cards use `surfaceContainerLow` with `BorderStroke(1.dp, semanticColors.cardStrokeSubtle)` and `18.dp` or `16.dp` rounded corners. Ensures cards maintain crisp, enclosed visual boundaries in Light Theme and Dark Theme.
3. **Transaction FAB Visibility Scoping (`FolioMainShell.kt`):** Restricted `FloatingActionButton` (+ New Transaction) strictly to the `TRANSACTIONS` tab using `AnimatedVisibility`. Hidden on `STATS`, `ACCOUNTS`, and `SETTINGS` to keep dashboards and drill-downs completely unblocked.
4. **Bottom Navigation Inset Calibration:** Avoids double window insets padding between the bottom bar and list viewports, guaranteeing uniform, comfortable bottom list clearance across all devices and gesture navigation bars.
5. **Stats Tab Architecture (4 Sub-Tabs):**
   - **`Overview` (`StatsSubTab.STATS`)**: Donut Chart, Category Rollup with category-specific 6-month trend, Cashflow with Safe-to-Spend Card & Shortfall Warning Alert, and Net Worth with Net Worth Velocity Cockpit.
   - **`Cards` (`StatsSubTab.CREDIT_CARDS`)**: Credit Cards Cockpit, Statement Status Pipeline, 30% Bureau Threshold Calibration, Smart Swipe Headroom, and Celebratory debt-free milestone banner.
   - **`Budgets` (`StatsSubTab.BUDGETS`)**: Monthly-locked budget progress bars with usage warnings.
   - **`Tags` (`StatsSubTab.TAGS_AND_NOTES`)**: Events, Trips & Reimbursements Hub with mode detection, Payee Intelligence with dual sort, and Note Rankings.
6. **Accounts Tab Design System:**
   - Section headers match `SettingsGroupCard` pattern (`labelLarge` uppercase primary titles). Whole-header row tap toggles expand/collapse without chevrons.
   - **Credit Cards**: Compact 2-column layout (`DUE NOW` & `UNBILLED`) with `Due [Date]` subtext. Smart color coding.
   - **Debit Cards**: Consolidated subtext with positive parent bank balance in `incomeEmerald`.
   - **Account Editor Sheet (`AccountEditorBottomSheet`)**: `Opening Balance` directly below `Account Name`. 10 account groups across 3 Segment Tabs (`Liquid` | `Cards` | `Savings & Debt`).
   - **Account Passbook Sheet (`AccountPassbookBottomSheet`)**: Header uncluttered with Quick Action Bar (`Add Tx` | `Settle Bill` | `Edit Account`).
7. **Modular Settings Architecture (`ui/settings/sheets/`):**
   - Extracted into focused bottom sheets: `AppearanceBottomSheet`, `RegionalFormatBottomSheet`, `DataAndBackupBottomSheet`, `CategoryManagerBottomSheet`, `TagsAndBookmarksBottomSheet`, `AutoBackupBottomSheet`, `TransferAndManualBackupBottomSheet`, `SpreadsheetsBottomSheet`, `SecondaryCurrenciesBottomSheet`, and `PdfStatementExportBottomSheet`.
8. **Complete 6-Theme Mode Engine (`FolioTheme.kt`):**
   - `SYSTEM`, `LIGHT`, `SEPIA`, `SLATE_GRAY` (Chrome Dark `#202124` palette), `DARK`, `AMOLED_BLACK`.
9. **12-Accent Color Palette Suite in Guaranteed 6x2 Grid (`FolioTheme.kt`):**
   - `EMERALD`, `COBALT`, `INDIGO`, `PURPLE`, `ROSE`, `LIME`, `CYAN`, `BRONZE`, `TEAL`, `CRIMSON`, `AMBER`, `SLATE`.

## 4. Package & File Map (`app/src/main/java/com/folioledger/app/`)
- **Root & DI:** `FolioApplication.kt`, `MainActivity.kt`, `di/AppContainer.kt`, `di/FolioViewModelFactory.kt`
- **Domain Layer (`domain/`):**
  - `model/`: `DomainEnumsAndMoney.kt`, `DomainModels.kt`, `LedgerDomainModels.kt`, `StatsAndAnalyticsModels.kt`
  - `repository/`: `DomainRepositories.kt`
  - `usecase/`: `AccountingRulesEngine.kt`
  - `util/`: 17 pure calculators (`AccountPassbookCalculator.kt`, `BackupTimeFormatter.kt`, `CardMilestoneCalculator.kt`, `CategoryDrillDownCalculator.kt`, `CreditCardCockpitCalculator.kt`, `CreditCardCycleCalculator.kt`, `LedgerBucketingCalculator.kt`, `MerchantAndTagAnalyticsCalculator.kt`, `MoneyFormatterAndCalculator.kt`, `NetWorthPredictiveCalculator.kt`, `RecurringBurdenCalculator.kt`, `RecurringScheduleCalculator.kt`, `SafeToSpendCalculator.kt`, `SavingsGoalCalculator.kt`, `StatsPeriodCalculator.kt`, `TagDateSpanFormatter.kt`, `TransactionEntryRulesEngine.kt`)
- **Data Layer (`data/`):**
  - `local/`: `FolioDatabase.kt` (v2, `MIGRATION_1_2`), `DefaultCategorySeeder.kt`, `entity/FolioEntities.kt`, `dao/FolioDaos.kt`
  - `demo/`: `DemoModeManager.kt`, `DemoDataSeeder.kt`
  - `backup/`: `AutoBackupEngine.kt`
  - `pdf/`: `PassbookPeriodPresetResolver.kt`, `PdfReportModels.kt`, `PdfStatementEngine.kt`
  - `repository/`: `AccountRepositoryImpl.kt`, `AnalyticsAndBudgetRepositoryImpl.kt`, `AutoBackupManager.kt`, `CategoryAndTagRepositoryImpl.kt`, `CsvExportImportEngine.kt`, `DatabaseBackupEngine.kt`, `PdfStatementRepositoryHelper.kt`, `RepositoryExtensions.kt`, `SettingsAndBackupRepositoryImpl.kt`, `StorageAccessUtils.kt`, `TransactionRepositoryImpl.kt`, `UserPreferencesDataSource.kt`
- **UI Layer (`ui/`):**
  - `components/`: `CategoryIconBadge.kt`, `FolioBrandLogoBadge.kt`, `FolioCharts.kt`, `FolioCommonComponents.kt`, `NetWorthAnalyticsComponents.kt`, `PeriodSelectorHeader.kt`, `ThreeColumnSummaryBanner.kt`
  - `accounts/`: `AccountsScreen.kt`, `AccountsUiModels.kt`, `AccountsViewModel.kt`, `components/*`, `dialogs/*`, `sheets/*`
  - `entry/`: `TransactionEntrySheet.kt`, `TransactionEntryUiModels.kt`, `TransactionEntryViewModel.kt`, `components/*`
  - `iconpack/`: `EmojiIconResolver.kt`, `FolioIconPackTheme.kt`, `FolioIconResolver.kt`, `LucideIconResolver.kt`, `MaterialIconResolver.kt`, `PhosphorIconResolver.kt`
  - `ledger/`: `LedgerHeaderBanners.kt`, `LedgerHomeScreen.kt`, `LedgerHomeViewModel.kt`, `LedgerItemCards.kt`, `LedgerSheetsAndDialogs.kt`, `dialogs/*`, `sheets/*`, `subtabs/*`
  - `settings/`: `SettingsScreen.kt`, `SettingsUiModels.kt`, `SettingsViewModel.kt`, `FolioGuideAndSupportSheets.kt`, `dialogs/*`, `guides/*`, `sheets/*`, `components/*`
  - `stats/`: `StatsAndBudgetsScreen.kt`, `StatsAndBudgetsUiModels.kt`, `StatsAndBudgetsViewModel.kt`, `StatsPredictiveCalculations.kt`, `components/*`, `sheets/*`, `subtabs/*`
- **Unit Test Suites (`app/src/test/java/com/folioledger/app/`):**
  - 243 passing JVM unit tests covering data persistence, demo mode, backup retention, PDF preset resolution, domain calculators, ViewModels, icon pack resolution, and predictive calculations.

