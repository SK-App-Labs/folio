# Changelog

All notable changes to the Folio Android project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.2.0] - Unreleased (Upcoming Play Store Release)

### Added
- **Waterfall Net Worth Chart & 3 Trend Analytics Segment Chips**: Added 3-way segment chips (`[ 📊 Cashflow ]` | `[ 📈 Net Worth ]` | `[ 💳 Credit Health ]`) in `StatsAndBudgetsScreen.kt`. Includes Waterfall Balance Sheet Canvas Chart (`NetWorthGrowthCanvasChart` with Green Assets Bar UP, Red Liabilities Bar DOWN, and connecting Net Worth Line) with metric filters (`All-in-One`, `Net Worth`, `Assets`, `Liabilities`) and drag-to-inspect tooltips driven by top granularity (`Monthly`, `Annually`, `Weekly`). Includes `CreditHealthDashboardCard` with overall credit utilization meter, per-card utilization progress bars (`0-30%` Emerald, `30-80%` Amber, `80-100%` Coral Red), available limits, and CIBIL credit score warnings.
- **Ledger Safety Deletion Rules**: Added permanent account deletion for unused accounts (0 transactions). For accounts with linked transactions, permanent deletion is blocked to protect historical ledger reports, guiding the user to **Archive Account** instead.
- **Auto-Calculated Credit Card Total Debt**: Credit card setup and editing now auto-calculates Total Debt Owed in real time (`Billed Due + Unbilled Spend`), eliminating manual calculation errors.
- **Segmented Account Group Selector**: Account Editor sheet categorizes 10 account types under 3 Segment Tabs (`Liquid` | `Cards` | `Savings & Debt`) for fast group picking.
- **Collapsible Foreign Currency & Appearance Customizer**: Account Editor sheet collapses currency selector and custom swatch/icon grid under expandable accordion cards.
- **Smart Default Icon & Color Matching**: Creating a new account automatically selects smart default icons and color swatches based on the chosen Account Group.
- **Unified Action Hub in Passbook Sheet**: Added `+ Add Transaction`, `💳 Settle Bill`, and `✏️ Edit Account` together in the Quick Action bar of `AccountPassbookBottomSheet`.
- **Complete 6-Theme Mode Suite**: `SYSTEM`, `LIGHT`, `SEPIA` (Warm Sand), `SLATE_GRAY` (Chrome Dark `#202124`), `DARK`, `AMOLED_BLACK`.
- **12-Accent Color Palette Suite**: Guaranteed 6x2 grid (`EMERALD`, `COBALT`, `INDIGO`, `PURPLE`, `ROSE`, `LIME`, `CYAN`, `BRONZE`, `TEAL`, `CRIMSON`, `AMBER`, `SLATE`).
- **Regional Format Selector**: Sheet with 1-line Indian (`12,34,567.89`) vs. Standard (`1,234,567.89`) grouping chips, currency search bar, and popular currency chips.
- **Unified Tags & Bookmarks**: `PrimaryTabRow` with `#Tags` and `Bookmarks ★` tabs.
- **Transactions & Stats Tab Streamlined Period Header**: Streamlined top period selector header across both `Trans.` and `Stats` tabs by removing subtext below the period title for maximum visual cleanliness.
- **Smart Back Navigation & Double-Tap Exit Protection**: Added intelligent `BackHandler` in `FolioMainShell.kt` returning from secondary tabs (`Stats`, `Accounts`, `Settings`) and account inspection sub-views back to the primary `Trans.` tab. On the primary tab, swiping Back shows a polite *"Press back again to exit"* toast to prevent accidental app closures.
- **Horizontal Pager Sub-Tab Swiping Across Trans & Stats**: Integrated Compose `HorizontalPager` with `beyondViewportPageCount = 1` pre-rendering, real-time `pagerState.currentPage` tab indicator tracking, and `settledPage` gesture synchronization in both `LedgerHomeScreen.kt` and `StatsAndBudgetsScreen.kt`, enabling 100% fluid, 0ms-latency left/right swipe gestures between sub-tabs (`Overview` ↔ `Budgets` ↔ `Rankings`). Added an animated floating `"Jump to Today ⤶"` action button that appears whenever browsing historical/future dates for 1-tap return to current date.

### Changed
- **Pure Neutral Card Container Standardization**: Standardized `tonalElevation = 0.dp` across `SettingsGroupCard` and `AccountGroupSectionCard` to remove primary color surface tinting, ensuring cards across `Trans.`, `Accounts`, and `Settings` tabs render with 100% identical pure neutral container colors in all theme modes.
- **Grouped Daily Transaction Cards**: Refactored `DailyLedgerSubTabContent` and `LedgerItemCards.kt` to group all transactions occurring on the same day inside **1 Enclosed Surface Card** (`DailyGroupedTransactionsCard`), eliminating scattered gaps between same-day transactions and saving ~40% vertical screen space.
- **App-Wide Divider Line Standardization**: Standardized all row divider lines in `SettingsScreen.kt` and settings sheets to explicitly pass `color = semanticColors.cardStrokeSubtle`, achieving 100% theme uniformity with Accounts and Ledger tabs.
- **Settings Tab Modularization & Extraction**: Extracted bottom sheets into `com.folioledger.app.ui.settings.sheets` package (`AppearanceBottomSheet`, `RegionalFormatBottomSheet`, `DataAndBackupBottomSheet`, `CategoryManagerBottomSheet`, `TagsAndBookmarksBottomSheet`), and extracted `FolioGuideAndSupportSheets.kt`, shrinking `SettingsScreen.kt` from ~2,300 lines down to ~600 lines.
- **Transaction Entry Sheet Modularization & Full-Screen Layout**: Extracted dialogs, bottom sheets, and keypad cards into `com.folioledger.app.ui.entry.components` package (`CalculatorNumericKeypad`, `CategoryPickerBottomSheet`, `AccountPickerDialog`, `BookmarksTemplateDialog`, `SplitTransactionEditorCard`, `InstallmentPlanCard`), shrinking `TransactionEntrySheet.kt` from ~2,110 lines down to ~550 lines. Updated sheet to 100% full-screen height with a 2-part split layout (upper ~53% scrollable form requirements area + lower ~47% keyboard/keypad entry dock).
- **Unified Date, Time & Split Bar**: Relocated date stepper, time picker chip (`🕒 hh:mm AM/PM`), and `Split` / `Inst.` feature chips together onto **1 unified line** directly above the Amount Banner in `TransactionEntrySheet.kt`. Combined date and time selection updates `timestampEpochMillis` in real time, automatically ordering same-day transactions chronologically in the Transactions Tab.
- **Anchored Save Bar, Docked Pickers & Split-Pane Category Exploration**: Anchored `[✓ Save]` and `[ Continue ]` (batch entry) action bar at the bottom of upper form section directly above the lower dock. Implemented in-dock `DockedAccountPicker` with a **Change View** toggle (3-column compact grid vs. 2-pane split group view for Cash, Banks, Cards, Investments) and `DockedCategoryPicker` with 2-pane exploration (left pane activates parent categories; right pane presents `Main Category` option at top followed by subcategories list). Included nested scroll isolation connections (`stopParentScrollConnection`) preventing scroll leakage to parent ModalBottomSheet or outer screen. Enabled 1-tap guided progression (`Amount` → `Next: Account` → `Account Grid/Groups` → `Category List` → `Save/Continue`).
- **Pre-Warmed ViewModels & Material 3 Crossfade Tab Transitions**: Configured `StatsAndBudgetsViewModel` and `AccountsViewModel` with `SharingStarted.Eagerly` to keep UI State, charts, and budget progress pre-calculated in background memory. Integrated `SaveableStateHolder` and Material 3 `Crossfade` (180ms `FastOutSlowInEasing`) in `FolioMainShell.kt` to deliver buttery-smooth, instant 0ms latency tab switching across `Trans.`, `Stats`, `Accounts`, and `Settings`.
- **Pixel-Perfect Screen Header Alignment Across Tabs**: Standardized header container padding, top offsets, and `Alignment.Top` text baseline anchoring in `AccountsScreen.kt` and `SettingsScreen.kt`, eliminating vertical jumps and achieving 100% pixel-perfect title alignment when switching between Accounts and Settings tabs.
- **Accounts Tab Card Design Alignment**: Section headers in `AccountGroupSectionCard.kt` match `SettingsGroupCard` pattern (`labelLarge` uppercase primary titles). Whole-header row tap toggles expand/collapse without chevron icons or `+` buttons.
- **Compact Credit Card Rows**: Compact 2-column layout (`DUE NOW` & `UNBILLED`) with `Due [Date]` subtext. Smart color coding (`DUE NOW` in `expenseCoral` when > 0, `incomeEmerald` when <= 0). Optimized column widths (`78.dp` & `72.dp`), returning `+52.dp` extra horizontal width to card names.
- **Streamlined Debit Card Rows**: Consolidated subtext (`HDFC Bank · Bal: ₹1,20,000.00`) with positive parent bank balance in `incomeEmerald` (or `expenseCoral` if overdrawn). Reduced row height to ~52.dp.
- **Simplified Account Group Titles**:
  - `Cash Wallets` → `Cash`
  - `Savings & Deposits` → `Savings`
  - `Overdraft Accounts` → `Overdraft`
  - `Loans & Mortgages` → `Loans`
- **Passbook Summary Card & Header Streamlining**: Refactored `PassbookTwoTierSummaryCard` into a spacious 3-column metric layout (`Opening Bal` → `Net Change` → `Closing Bal`) with a symmetrical left-aligned `+ Inflows` and right-aligned `- Outflows` subline.
- **Compact Credit Card 2x2 Breakdown Card**: Streamlined `CreditCardStatementBreakdownCard` into a single compact 2x2 card containing all 4 credit metrics (`Billed Due`, `Unbilled Spend`, `Total Debt`, and `Available Credit Limit`), with `IntrinsicSize.Min` column height alignment, centered column content (`horizontalAlignment = Alignment.CenterHorizontally`), and subtle `VerticalDivider` separation.
- **Passbook Header & Day Headers**: Removed `Edit Account` from top header row. Removed repeated `"Day End Bal:"` label prefix from day-grouped transaction section headers. Removed redundant `"Amount & Running Bal"` text from statement headers.
- **Header Standardization**: Standardized top screen headers across all 4 tabs to `headlineSmall` (24sp Bold) with `padding(horizontal = 16.dp, top = 12.dp, bottom = 4.dp)`.
- **Scoped FAB**: Scoped Floating Action Button (`+`) strictly to `TRANSACTIONS` and `STATS` tabs.

### Fixed
- Fixed Compose flow state binding in `AccountsViewModel.kt` for `pendingDeletionAccountId` alerts.
- Fixed Credit Card balance calculation in `AccountsViewModel.kt` (`effectiveOpeningMagnitude`).
- **App Switching Wallpaper Bleed-Through & Transition Fix**: Added explicit `windowIsTranslucent = false` and `windowShowWallpaper = false` in `themes.xml` (both `Theme.Folio` and `Theme.Folio.Starting`), cleared `FLAG_SHOW_WALLPAPER` in `MainActivity.kt`, and synchronously painted `window.decorView` background & `windowBackground` inside `FolioTheme` SideEffect, ensuring 100% opaque window rendering and eliminating wallpaper bleed-through during Recents task switcher gestures.

---

## [1.1.0] - 2025-04-15

### Added
- **Room SQLite v2 Migration**: Explicit `MIGRATION_1_2` adding `initialUnbilledMinor` for credit cards.
- **Lossless ZIP Backup & Restore Engine**: `DataAndBackupBottomSheet` for portable backup archives.
- **Pre-Migration SQLite Backups**: `DatabaseBackupManager.createPreMigrationBackup` with automatic fail-safe rollback.
- **Stats & Budget Analytics**: Main category donut rollups, subcategory distribution charts, multi-level budget alerts, and note/tag rankings.

---

## [1.0.1] - 2025-04-05

### Added
- **Dedicated Account Passbook View**: Introduced `AccountPassbookBottomSheet` with monthly period switcher, 2-tier opening/closing summary, linked debit card roll-up, and per-transaction running balance calculation.

### Fixed
- **Critical SQLite Cascade Deletion Fix**: Fixed Room's `OnConflictStrategy.REPLACE` bug where editing an Account or Category caused SQLite `FOREIGN KEY ... ON DELETE CASCADE` to accidentally delete linked transactions and splits. Replaced `REPLACE` with safe in-place `@Update` and raw `@Insert` routines in `AccountDao` and `CategoryDao`.
- **UI & Passbook Isolation Fixes**: Enforced strict transaction type isolation and excluded zero-delta entries.

---

## [1.0.0] - 2025-04-01

### Added
- **Initial Production Release** of Folio: Expense & Budget Ledger.
- 100% offline MVVM architecture with Room SQLite database.
- 5-View Home Ledger (`Daily`, `Calendar`, `Weekly`, `Monthly`, `Summary`).
- Credit card cycle calculator, split transactions, refunds, and installment plans.
