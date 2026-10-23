# FOLIO LEDGER — AI CONTEXT HANDOVER CARD (v1.2.0+)

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
4. **Credit Card Engine (`CREDIT_CARD`):** Tracks `creditLimit`, `settlementDayOfMonth (1..31)`, `paymentDueDayOfMonth (1..31)`, Statement Payable vs. Unbilled Spend (`initialUnbilledMinor`), and 1-tap `CC_SETTLEMENT` ("Pay Bill") which reduces bank asset and CC liability without double-counting expense.
5. **Split, Refund & Installments:** Supports 1-to-N `SplitLineItem` transactions, `REFUND` transactions (offset category spend without inflating income), and remainder-safe Credit Card `InstallmentPlan` generation (first month absorbs `total % months`).
6. **Data Protection & Migrations:** `FolioDatabase` is at Version 2 with explicit `MIGRATION_1_2` (`ALTER TABLE accounts ADD COLUMN initialUnbilledMinor`). Destructive fallback is guarded by `BuildConfig.DEBUG`. Includes `DatabaseBackupManager` for automatic pre-migration `.pre_migration_bak` SQLite backups with fail-safe automatic rollback.
7. **Strict Transaction Isolation & Field Integrity:**
   - Non-transfer types (`EXPENSE`, `INCOME`, `REFUND`) strictly enforce `transferToAccountId = null`, `transferToAccountName = null`, `transferFee = 0L`.
   - Transfer types (`TRANSFER`, `CC_SETTLEMENT`) strictly enforce `categoryId = null`.
   - Passbook statements filter strictly by matching transaction types and exclude zero-delta entries (`deltaMinor != 0L`).
8. **Android 15 Edge-to-Edge System Bar Synchronization:**
   - Native night XML resource (`res/values-night/themes.xml`) sets `windowLightStatusBar = false` and `windowLightNavigationBar = false`.
   - `FolioTheme.kt` uses `Context.findActivity()` extension helper to call `ComponentActivity.enableEdgeToEdge(...)` with explicit `SystemBarStyle.dark` / `SystemBarStyle.light` and `WindowCompat.getInsetsController(window, window.decorView)` inside `SideEffect`.
   - `MainActivity.kt` re-asserts status bar icon appearance when splash screen exits and guards frame 1 composition.

## 3. UI, Design System & Theme Engine Standards
1. **Header Layout Alignment Standard:** All top-level screen headers use `headlineSmall` (24sp Bold) with `padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp)` to align across `AccountsScreen`, `TransactionsScreen`, `StatsAndBudgetsScreen`, and `SettingsScreen`.
2. **Card Enclosure Standard:** All grouped cards use `surfaceContainerLow` with `BorderStroke(1.dp, semanticColors.cardStrokeSubtle)` and `18.dp` or `16.dp` rounded corners. This ensures cards maintain crisp, enclosed visual boundaries in both Light Theme (where background contrast is low) and Dark Theme.
3. **Transaction FAB Visibility Scoping (`FolioMainShell.kt`):** Restricted `FloatingActionButton` (+ New Transaction) strictly to `TRANSACTIONS` and `STATS` tabs using `AnimatedVisibility`. Hidden on `ACCOUNTS` and `SETTINGS`.
4. **Category Typography & Accordion Standard:** Parent titles = `titleMedium` (16sp Bold); Subcategory counts = `bodySmall` (12sp); Subcategory items = `bodyLarge` (15sp–16sp SemiBold); Subcategory icon badges = `32.dp`. 16.dp chevron IconButton (`ExpandMore`/`ExpandLess`) rendered on **ALL main categories** (even empty ones) to reveal subcategories or `+ Add Subcategory under [Parent]`.
5. **Inline Category Creation Auto-Expansion (`TransactionEntrySheet.kt`):** Creating a category inline keeps sheet open (`showCategorySheet = true`), auto-selects new category, and auto-expands accordion (`expandedCategoryIds += createdNode.id`).
6. **Complete 6-Theme Mode Engine (`FolioTheme.kt`):**
   - `SYSTEM`: System Default (syncs with OS setting).
   - `LIGHT`: Clean Crisp White (`#FFFFFF`).
   - `SEPIA`: Warm Sand parchment paper cream (`#F7F3E9`) for low eye-strain daytime viewing.
   - `SLATE_GRAY`: Charcoal Gray matching Google Chrome's Dark Theme palette (`#202124` background, `#292A2D` surface, `#35363A` container, `#E8EAED` high-contrast text, `#9AA0A6` secondary text, `#3C4043` border outline, lightened `#8E96A0` OFF-switch thumb).
   - `DARK`: Midnight Navy (`#0F172A`).
   - `AMOLED_BLACK`: Pure Pitch Black (`#000000`) for maximum OLED battery savings.
7. **12-Accent Color Palette Suite in Guaranteed 6x2 Grid (`FolioTheme.kt`):**
   - ❇️ `EMERALD` (`#059669`), ⚡ `COBALT` (`#2563EB`), 💜 `INDIGO` (`#4F46E5`), 🍇 `PURPLE` (`#7C3AED`), 🌸 `ROSE` (`#F43F5E`), 🍏 `LIME` (`#65A30D`), 🧊 `CYAN` (`#06B6D4`), 🍂 `BRONZE` (`#B45309`), 🌊 `TEAL` (`#0D9488`), 🔴 `CRIMSON` (`#E11D48`), 🍊 `AMBER` (`#D97706`), 🩶 `SLATE` (`#475569`).
   - Displayed in `AppearanceBottomSheet` as a guaranteed 2-row x 6-column grid using `take(6)` and `drop(6)`.
8. **Regional & Number Formatting (`RegionalFormatBottomSheet`):**
   - Number Grouping: 1-line chips (`"Indian (12,34,567.89)"` vs `"Standard (1,234,567.89)"`).
   - Primary Currency Selector: Real-time search bar (`"Search currency"`), `⚡ POPULAR CURRENCIES` horizontal `LazyRow` chips (`INR`, `USD`, `EUR`, `GBP`, `AED`, `SAR`), and full-width currency cards showing symbol badge (`₹`, `$`, `€`, `£`, `¥`, `SAR`), full name, code, and selection checkmark.
9. **Unified Tags & Bookmarks (`TagsAndBookmarksBottomSheet`):**
   - `PrimaryTabRow` with Tab 1 (`#Tags`) and Tab 2 (`Bookmarks ★`).

## 4. Package & File Map (`app/src/main/java/com/folioledger/app/`)
- **Root & DI:**
    - `FolioApplication.kt`, `MainActivity.kt` (FragmentActivity with `core-splashscreen` API, data-sync splash hold, morph-to-header exit animation, biometric gate, and Android 15 edge-to-edge re-assertion)
    - `di/AppContainer.kt`, `di/FolioViewModelFactory.kt`
- **Domain Layer (`domain/`):**
    - `model/DomainEnumsAndMoney.kt`, `model/DomainModels.kt`
    - `util/MoneyFormatterAndCalculator.kt`
    - `usecase/AccountingRulesEngine.kt`
    - `repository/DomainRepositories.kt`
- **Data Layer (`data/`):**
    - `local/FolioDatabase.kt` (v2, `MIGRATION_1_2`, `DatabaseBackupManager`), `local/RoomTypeConverters.kt`, `local/DefaultCategorySeeder.kt`
    - `local/entity/RoomEntities.kt`, `local/dao/RoomDaos.kt`
    - `repository/AccountAndTransactionRepositoriesImpl.kt`
    - `repository/AnalyticsAndBudgetRepositoryImpl.kt`
    - `repository/SettingsAndCategoryRepositoriesImpl.kt`
- **UI Layer (`ui/`):**
    - `theme/FolioTheme.kt` (6 Theme Modes, 12 Accent Palettes in 6x2 grid, Chrome Dark `#202124` palette, lightened OFF-switch thumb)
    - `components/FolioCommonComponents.kt` (`CategoryIconBadge`, `resolveFolioIcon`)
    - `shell/FolioMainShell.kt` (4 tabs: `TRANSACTIONS`, `STATS`, `ACCOUNTS`, `SETTINGS` + FAB scoped to `TRANSACTIONS` and `STATS` tabs)
    - `ledger/` (Decomposed into 5 sub-files): `LedgerHomeScreen.kt`, `LedgerHeaderBanners.kt`, `LedgerItemCards.kt`, `LedgerSubTabs.kt`, `LedgerSheetsAndDialogs.kt`
    - `entry/TransactionEntryViewModel.kt`, `entry/TransactionEntrySheet.kt` (`CategoryPickerBottomSheet` with real-time search, frequent row, scaled typography, always-present chevrons, and inline creation auto-expansion)
    - `accounts/AccountsViewModel.kt`, `accounts/AccountsScreen.kt` (2-column Statement Due vs. Current Unbilled Credit Card cards, de-cluttered `AccountPassbookBottomSheet` with top header edit button, period header "View in Ledger" chip, and strict type-aware passbook isolation)
    - `stats/StatsAndBudgetsViewModel.kt`, `stats/StatsAndBudgetsScreen.kt`
    - `settings/SettingsViewModel.kt`, `settings/SettingsScreen.kt` (3 grouped cards with `cardStrokeSubtle` borders; `AppearanceBottomSheet` with Live Theme Preview Card, guaranteed 6x2 accent swatch grid; `RegionalFormatBottomSheet` with currency search; `CategoryManagerBottomSheet`; `TagsAndBookmarksBottomSheet`; `DataAndBackupBottomSheet`)
