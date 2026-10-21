# FOLIO LEDGER — AI CONTEXT HANDOVER CARD (v1.0.0+)

## 1. Project Identity & Tech Stack
- **App Name:** Folio: Expense & Budget Ledger
- **Play Store Application ID (`applicationId`):** `com.skal.folio`
- **Kotlin Source Package / Namespace:** `com.folioledger.app`
- **Architecture:** 100% Offline-First, Single-Activity MVVM, Jetpack Compose (Material 3), Room SQLite (`FolioDatabase`), Preferences DataStore, Manual DI (`AppContainer` + `FolioViewModelFactory`).
- **Build Config:** Min SDK 29 (Android 10), Target/Compile SDK 35, Kotlin 2.x, KSP, `activityCompose = "1.10.1"`.
- **Strict Coding Rule:** Always output 100% complete replacement files from line 1 (`// File: ...`) to the final closing brace (`Ctrl + A -> Delete -> Paste`). Never output partial snippets or `TODO()` placeholders.

## 2. Core Accounting & Domain Invariants
1. **Minor-Unit Money (`MinorMoney`):** `@JvmInline value class MinorMoney(val amountMinor: Long)` using `Math.addExact` / `Math.subtractExact`. Never use `Double`/`Float` for currency math.
2. **Transfer Neutrality:** `TRANSFER` transactions move balance between accounts; only `transferFee` counts as period expense.
3. **Linked Debit Cards (`DEBIT_CARD`):** Linked via `parentAccountId` to a parent Bank Account. Expenses deduct directly from the parent bank balance while tracking card-specific period spend.
4. **Credit Card Engine (`CREDIT_CARD`):** Tracks `creditLimit`, `settlementDayOfMonth (1..31)`, `paymentDueDayOfMonth (1..31)`, Statement Payable vs. Unbilled Spend, and 1-tap `CC_SETTLEMENT` (reduces bank asset and CC liability without double-counting expense).
5. **Split, Refund & Installments:** Supports 1-to-N `SplitLineItem` transactions, `REFUND` transactions (offset category spend without inflating income), and remainder-safe Credit Card `InstallmentPlan` generation (first month absorbs `total % months`).

## 3. Package & File Map (`app/src/main/java/com/folioledger/app/`)
- **Root & DI:**
    - `FolioApplication.kt`, `MainActivity.kt` (FragmentActivity with BiometricPrompt gate)
    - `di/AppContainer.kt`, `di/FolioViewModelFactory.kt`
- **Domain Layer (`domain/`):**
    - `model/DomainEnumsAndMoney.kt`, `model/DomainModels.kt`
    - `util/MoneyFormatterAndCalculator.kt`
    - `usecase/AccountingRulesEngine.kt`
    - `repository/DomainRepositories.kt` (`AccountRepository`, `TransactionRepository`, `CategoryAndTagRepository`, `AnalyticsAndBudgetRepository`, `SettingsAndBackupRepository`)
- **Data Layer (`data/`):**
    - `local/FolioDatabase.kt`, `local/RoomTypeConverters.kt`, `local/DefaultCategorySeeder.kt`
    - `local/entity/RoomEntities.kt`, `local/dao/RoomDaos.kt`
    - `repository/AccountAndTransactionRepositoriesImpl.kt`
    - `repository/AnalyticsAndBudgetRepositoryImpl.kt`
    - `repository/SettingsAndCategoryRepositoriesImpl.kt` (Direct `MediaStore.Downloads` + internal `FolioBackups` ZIP/CSV engine)
- **UI Layer (`ui/`):**
    - `theme/FolioTheme.kt` (4 modes: SYSTEM, LIGHT, DARK, AMOLED_BLACK + 6 accent palettes)
    - `components/FolioCommonComponents.kt` (72+ icons + smart auto-icon matcher), `components/FolioCharts.kt`
    - `shell/FolioMainShell.kt` (4 tabs: `TRANSACTIONS`, `STATS`, `ACCOUNTS`, `SETTINGS` + `+` FAB)
    - `ledger/LedgerHomeViewModel.kt`, `ledger/LedgerHomeScreen.kt` (5 sub-tabs: Daily, Calendar, Weekly, Monthly, Summary + Account-scoped ledger + Month-bound multi-filter)
    - `entry/TransactionEntryViewModel.kt`, `entry/TransactionEntrySheet.kt` (Calculator keypad with minimize toggle, Splits, Installments, Bookmarks, in-place Edit)
    - `accounts/AccountsViewModel.kt`, `accounts/AccountsScreen.kt`
    - `stats/StatsAndBudgetsViewModel.kt`, `stats/StatsAndBudgetsScreen.kt` (Weekly/Monthly/Annual/Custom periods, Donut & 6-Month Trend charts, Budgets, Tag/Note rankings)
    - `settings/SettingsViewModel.kt`, `settings/SettingsScreen.kt` (2-Level Category Manager, 1-tap ZIP/CSV, User Guide & Tip Jar)