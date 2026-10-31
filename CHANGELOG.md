# Changelog

All notable changes to the Folio Android project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---
## [1.5.0] - Unreleased
### Added
- **Global Localization & Tier-1 Language Architecture (Phase 1: String Extraction & Android 13+ Per-App Locale Setup)**:
  - Engineered native Android 13+ Per-App Language Preferences (`res/xml/locales_config.xml` & `android:localeConfig`), enabling system-level and in-app language switching across 13 major global locales.
  - Extracted centralized XML string catalogs in `res/values/`:
    - `strings_common.xml`: Shared actions (Save, Cancel, Delete, Edit, Archive, Clear) and filter/search placeholders.
    - `strings_nav.xml`: Bottom navigation tabs (`Trans.`, `Stats`, `Accounts`, `Settings`) and accessibility descriptions.
    - `strings_ledger.xml`: Ledger subtabs (`Daily`, `Calendar`, `Weekly`, `Monthly`, `Summary`) and header banners.
    - `strings_stats.xml`: Stats subtabs (`Overview`, `Cards`, `Budgets`, `Tags`), Safe-to-Spend metrics, Shortfall warnings, and Net Worth Velocity labels.
    - `strings_accounts.xml`: Net Worth banners, Account Groups (`Cash`, `Bank`, `Debit Cards`, `Credit Cards`, etc.), and Credit Card action labels.
    - `strings_entry.xml`: Transaction entry modes, chips (`Split`, `Inst.`, `Repeat`), and input hints.
    - `strings_settings.xml`: Settings group headers and navigation rows.
  - Converted hardcoded Compose literals in `FolioMainShell.kt`, `StatsAndBudgetsScreen.kt`, `LedgerHomeScreen.kt`, and `AccountGroupSectionCard.kt` to dynamic `stringResource(R.string.*)` lookups.
- **Interactive In-App Language Picker (Phase 2)**:
  - Added `AppLanguageBottomSheet.kt` in `ui/settings/sheets/` featuring all 13 Tier-1 languages with native autonyms, national flags, and real-time search filtering.
  - Placed the **App Language** setting row in `SettingsScreen.kt` under **`APPEARANCE & SECURITY`** directly below **Theme & Accent Palette**, creating a perfectly balanced 3-row card.
  - Displays the active language's native name directly on the row's glanceable badge (e.g. `English`, `Español`, `Deutsch`, `日本語`, `हिन्दी`, `தமிழ்`).
  - Wired 1-tap switching via `AppCompatDelegate.setApplicationLocales` with instant activity configuration recreation.
- **Tier 1A & Tier 1B Financial-Grade Translations (Phase 3)**:
  - Deployed full localized string catalogs for all 12 global Tier-1 markets (alongside English base):
    - 🇪🇸 **Spanish (`res/values-es`)**: Latin America & Spain banking terminology (*Patrimonio neto, Activos/Pasivos, Remanente, Disponible para gastar*).
    - 🇧🇷 **Portuguese - Brazil (`res/values-pt-rBR`)**: Brazil fintech & budgeting standards (*Patrimônio Líquido, A Pagar/Não Faturado, Reserva de Emergência*).
    - 🇩🇪 **German (`res/values-de`)**: DACH region privacy & ledger terminology (*Nettovermögen, Fällig/Unabgerechnet, Notgroschen-Puffer*).
    - 🇮🇳 **Hindi (`res/values-hi`)**: Indian vernacular personal finance (*कुल संपत्ति, देय राशि, आपातकालीन फंड, सुरक्षित खर्च*).
    - 🇫🇷 **French (`res/values-fr`)**: European & global Francophone finance (*Valeur nette, Actifs/Passifs, Report à nouveau, Reste à dépenser*).
    - 🇮🇹 **Italian (`res/values-it`)**: Italian financial management (*Patrimonio netto, Attività/Passività, Fondo d'emergenza, Da saldare*).
    - 🇯🇵 **Japanese (`res/values-ja`)**: Japanese domestic household bookkeeping (*純資産, 資産/負債, 繰越金, 支出可能額, 締め日*).
    - 🇰🇷 **Korean (`res/values-ko`)**: Korean fintech & ledger standards (*순자산, 자산/부채, 이월액, 지출 가능 잔액, 비상금 버퍼*).
    - 🇨🇳 **Simplified Chinese (`res/values-zh-rCN`)**: Standard Mainland Chinese accounting (*净资产, 总资产/总负债, 前期结转, 今日安全可支配, 自由资金*).
    - 🇹🇼 **Traditional Chinese (`res/values-zh-rTW`)**: Traditional Taiwan/HK personal finance (*淨資產, 總資產/總負債, 前期結轉, 今日安全可支配額度, 自由資金*).
    - 🇮🇩 **Indonesian (`res/values-in`)**: Southeast Asian personal ledger terms (*Kekayaan Bersih, Aset/Liabilitas, Saldo Lalu, Dana Bebas, Landasan Dana Darurat*).
    - 🇮🇳 **Tamil (`res/values-ta`)**: Indian regional finance (*நிகர மதிப்பு, மொத்த சொத்துக்கள்/கடன்கள், முந்தைய இருப்பு, உபரி நிதி, பில் செலுத்து*).
### Fixed
- **In-App Language Switching Engine & Startup Crash Elimination**:
  - Replaced fragile `AppCompatActivity` dependencies with pure native Android platform architecture via `FolioLocaleManager`:
    - On Android 13+ (API 33+), calls native platform `android.app.LocaleManager` directly to update per-app language settings and system-level preferences with zero AppCompat theme friction.
    - On pre-Android 13 devices, persists language preferences locally and dynamically wraps `attachBaseContext(FolioLocaleManager.wrapContextIfNeeded(newBase))`.
    - Preserved `FragmentActivity` on `MainActivity`, completely eliminating the `IllegalStateException: You need to use a Theme.AppCompat theme` startup crash with `Theme.SplashScreen`.
  - Converted hardcoded section headers and navigation rows in `SettingsScreen.kt` to dynamic `stringResource(R.string.*)` lookups so that the settings screen reflects the selected language immediately.
  - Added bidirectional normalization for Indonesian locale tags (`id` and `in`) in `AppLanguageBottomSheet.kt`.

## [1.4.0] - 2026-10-31
### Fixed
- **Ledger Summary Subtab Polish & Data Integrity**:
  - *Scoped CSV Export Isolation*: Upgraded `exportTransactionsToCsv` and `exportTransactionsToCsvStream` in `SettingsAndBackupRepository` and `LedgerHomeViewModel` to filter strictly by `accountId` when an account filter is active, preventing transaction leakage from other accounts into scoped exports and personalizing the export filename (e.g. `Folio_HDFC_Bank_Oct_2026.csv`).
  - *Multi-Currency Accuracy in Analytics & Budgets*: Injected user preferences into `AnalyticsAndBudgetRepositoryImpl` to dynamically convert foreign currency transactions to the user's primary currency across `observeCategorySpendStats`, `observeBudgetProgressList`, `observeSixMonthTrendBars`, `observeTagSpendRankings`, and `observePayeeOrNoteRankings`, eliminating distorted totals when transacting across multiple currencies.
  - *Account Activity Breakdown Card*: Implemented the previously missing Account Breakdown card on the Summary subtab, displaying each active account with its transaction count, native currency inflows, outflows, and net cashflow changes.
  - *Intelligent Savings Rate Badging*: Fixed the misleading bright green `Savings Rate: 0.0%` badge when income is zero and expenses exist; now cleanly renders a neutral/warning `Deficit (No Income)` or `No Activity` badge with appropriate color mode.
  - *Transparent Multi-Category Budget Progress*: Replaced the arbitrary single-budget fallback with a unified budget display showing the overall monthly budget plus active category budgets (or up to 3 category budgets when no overall budget exists).
  - *Scoped Screen Depletion Prevention*: Added in-memory category spend calculation for scoped account views so top expenses and account flow summaries remain rich and informative rather than leaving the Summary subtab depleted.
- **Ledger Weekly Subtab Polish & Usability**:
  - *Contextual Transaction Date Badging*: Added `"EEE, dd MMM"` date badges (e.g., `Fri, 02 Oct`) alongside the account name on transaction cards within expanded weekly buckets, resolving missing date visibility across 7-day multi-day week spans.
  - *Smart Month-Aware Week Expansion*: In the active month containing today, automatically expands the current week (e.g., Week 5); when navigating to past or future months, all weeks start collapsed to present an immediate, clean high-level summary of the entire month's weekly totals.
  - *Clean Empty State Feedback*: Resolved phantom chevron behavior on empty weeks by rendering a subtle `"No transactions recorded for this week"` message instead of keeping the card body closed while the chevron points up.
  - *Stable Compose Keys*: Upgraded LazyColumn keys to composite strings (`"${bucket.startEpochDay}_${bucket.weekIndex}"`) to eliminate key collisions across month navigation and maintain optimal rendering performance.
- **Ledger Monthly Subtab Accordion & Drill-Down Usability**:
  - *Composite LazyColumn Keys*: Replaced simple integer keys (`1..12`) with composite keys (`"${startEpochDay}_${monthIndex}"`) to eliminate key collisions across annual navigation, ensuring buttery smooth scroll and animation performance.
  - *Smart Year-Aware Month Expansion*: Automatically expands the current active month when viewing the financial year containing today; when navigating to past or future years, all 12 months start collapsed for an immediate, uncluttered bird's-eye overview.
  - *1-Tap Drill-Down Navigation*: Added direct navigation from any month or week in the Monthly subtab (`"View in Daily →"` and clickable weekly breakdown rows) to jump immediately to that period in the Daily subtab without tedious date-stepping.
  - *Clean Empty Month State*: Replaced the redundant 5 empty week rows with a clear `"No transactions recorded for this month"` message when expanding inactive or future months.
  - *Compact Column Labels & Current Month Badge*: Added explicit `In`, `Out`, and `Net` column headers to the expanded weekly breakdown and a prominent `Current` pill badge to the active month card.
  - *Resolved Accordion Lock Bug*: Introduced nullable `expandedYearlyMonths: Set<Int>? = null` in `LedgerControlState` to distinguish initial default expansion from explicit user collapse (`emptySet()`), enabling 1-tap collapsing and expanding of any month with full weekly item loading.
- **Ledger Monthly Subtab Annual Summary Banner Alignment**:
  - Replaced the mismatched monthly period summary (`uiState.periodSummary`) on the Monthly subtab header with dedicated annual metrics (`yearlyIncomeTotal`, `yearlyExpenseTotal`, and `yearlyNetTotal`) in `ThreeColumnSummaryBanner`.
  - The top summary banner now displays *Annual Income*, *Annual Expenses*, and *Annual Net*, perfectly matching the sum of all 12 month cards in the financial year.
- **Multi-Currency Aggregation in Ledger Calculations**:
  - Upgraded `computeIncomeAndExpenseTotals`, `buildDailyGroups`, `buildCalendarGridCells`, and `buildWeeklyBucketsForPeriod` to convert non-primary account transactions into the user's configured primary currency using manual exchange rates from `prefs.secondaryCurrencies`.
  - Eliminates 1:1 raw summation errors when viewing multi-currency transactions across accounts in the aggregate view while preserving native currencies for single-account scopes.
- **UI Edge-to-Edge Insets & Gesture Navigation Calibration**:
  - *Eliminated Double Bottom Insets Gap*: Fixed redundant window insets padding between the bottom navigation bar and lazy lists, establishing uniform, calibrated scroll-to-bottom clearance across Transactions, Stats, Accounts, and Settings.
  - *Ledger Search & Filter Streamlining*: Simplified the search field placeholder to `"Search"` and slimmed the filter button for improved density on narrow phone viewports.
  - *Bottom Sheet Drag-to-Dismiss Gesture*: Corrected gesture handling in ledger filter and passbook bottom sheets to ensure smooth, natural pull-to-dismiss interactions.
- **Stats Tab Granularity & Budget Lock**:
  - *Category-Specific Drill-Down Trend*: Isolated the 6-month historical trend area chart in category drill-down to display strictly that category's spending rather than aggregate totals.
  - *Monthly Budget Enforcement*: Locked the Budgets subtab to the monthly financial horizon, eliminating confusing empty states on weekly or annual granularities.
  - *All-Time Rankings Toggle*: Added an all-time spend ranking mode in category breakdown alongside monthly rankings.

### Added
- **Predictive Personal Finance Intelligence Suite (Phases 1–4)**:
  - **Phase 1: Net Worth Velocity & Emergency Runway Cockpit**:
    - Embedded dynamic wealth velocity indicators directly into the Overview subtab's Net Worth view (`NetWorthVelocityCockpit`).
    - Calculates month-over-month wealth trajectory ($\Delta\text{Net Worth}$), 3-month trailing expense burn rate, and liquid cash emergency runway in months.
    - Features smart badging (`Surplus / Building Wealth`, `Runway > 12 Months`, or warning alerts when expenses exceed inflows).
  - **Phase 2: Merchant & Payee Intelligence**:
    - Upgraded Payee rankings with dual sorting modes: **Total Outlay** (highest spend) vs. **Frequency** (most frequent transactions).
    - Added an interactive `PayeeDrillDownBottomSheet` featuring 6-month outlay trends, cross-category distribution, and automated cadence detection (e.g., `"Recurring: ~Every 30 days"`).
  - **Phase 3: Events, Trips & Reimbursements Hub in Tags**:
    - Upgraded the Tags subtab (`StatsSubTab.TAGS_AND_NOTES`) with smart mode detection: automatically distinguishes reimbursable claims (e.g. `#office`, `#reimbursable`, `#split`, `#claim`) from trips or events (e.g. `#trip`, `#vacation`, `#wedding`).
    - *Reimbursable Mode*: Computes total spent, total reimbursed, net pending owed balance, and displays a prominent settlement badge (`✓ Fully Settled` or `Pending Claim: ₹X,XXX`).
    - *Trip / Event Mode*: Computes trip date duration span (e.g. `12 Days`), daily average spend burn rate, and cross-category expense distributions.
    - Added `TagProjectDrillDownBottomSheet` with 1-tap receipt inspection jumping directly to transaction details.
  - **Phase 4: Safe-to-Spend & 14-Day Overdraft Shortfall Radar**:
    - Integrated safe-to-spend intelligence into the Overview subtab's Cashflow view (`SafeToSpendCard`).
    - Computes unencumbered discretionary cash available for immediate spending: $\text{Safe-to-Spend} = \text{Liquid Cash} - \text{Unpaid Credit Card Debt} - \text{14-Day Active Recurring Commitments}$.
    - Added proactive `ShortfallAlertCard` radar: projects future cashflows per bank account over a 14-day rolling horizon and alerts the user immediately if any upcoming commitment exceeds projected account balance.
- **Credit Cards Cockpit (Stats Tab)**:
  - Elevated credit card analytics into a dedicated top-level subtab: `StatsSubTab.CREDIT_CARDS` ("Cards").
  - *Statement Status Pipeline*: Intelligently maps credit card cycles into 5 real-time states: `OVERDUE`, `DUE_SOON` ($\le 3$ days), `ISSUED`, `CURRENT`, and `SETTLED`.
  - *Credit Bureau Threshold Calibration*: Evaluates total credit utilization against official credit scoring standards: `SAFE` ($<30\%$), `CAUTION` ($30\%–50\%$), `WARNING` ($50\%–75\%$), and `CRITICAL` ($>75\%$).
  - *Smart Swipe Headroom Metric*: Displays the exact headroom in primary currency before the user breaches the 30% credit bureau threshold.
  - *Multi-Currency Card Valuation*: Converts foreign-currency credit card limits and balances into the primary currency using manual exchange rates.
  - *Celebratory Milestone Banner*: Surfaces an animated celebration card when all card statement balances and unbilled charges are settled to zero.
  - *Top Category Badge*: Displays the card's highest spend category with native category icon and color pill.
- **Branded PDF Account Statement & Universal Tax Report Export Engine**: Engineered a 100% offline, native Skia vector PDF generation engine with **0 KB APK overhead** using Android's native `android.graphics.pdf.PdfDocument`, providing publication-ready financial statements for accounting, tax filing, visa applications, and personal audits:
  - **Dedicated Settings Entry Point**: Positioned directly under the *DATA & BACKUPS* settings card via a dedicated row: `PDF Statements & Tax Reports`, keeping daily passbook interaction clutter-free and consolidating reporting tools in one authoritative location.
  - **Universal Annual Financial & Tax Statement**: Designed for universal applicability across global jurisdictions (USA, EU, India, UK, Australia, Singapore, etc.):
    - *Flexible Fiscal Year Presets*: Supports Calendar Year (`Jan 1 – Dec 31`), April Fiscal Year (`Apr 1 – Mar 31`), July Fiscal Year (`Jul 1 – Jun 30`), and custom audit date ranges with intuitive year steppers.
    - *Multi-Currency Basket & Valuation*: Evaluates foreign currency accounts into the primary currency using manual exchange rates configured in Secondary Currencies, rendering explicit audit footnotes (`1 USD = 85.00 INR`).
    - *Itemized Tax Deductions & Business Schedule*: Automatically filters and aggregates transactions bearing `#tax`, `#business`, `#medical`, `#donation`, `#charity`, or `#deductible` tags into an audit schedule with payee, receipt notes, date, and amounts.
    - *Income Streams & Expense Schedules*: Aggregates gross inflows by income source and itemizes expenditure by category with percentage-of-total breakdown and net savings rate %.
    - *Year-End Wealth Snapshot*: Generates a tabular balance sheet of closing balances across liquid assets, investments, and credit liabilities on the final day of the fiscal period with final net worth.
  - **Branded Account Passbook Statement**: Produces chronological account statements in the account's native currency (USD, EUR, INR, AED, JPY) featuring:
    - Executive summary card (Opening Balance ➔ Inflows ➔ Outflows ➔ Net Change ➔ Closing Balance).
    - For Credit Cards: Statement closing balance, payment due date, credit limit, available credit, and statement settlement cycle.
    - Multi-page chronological transaction ledger with zebra striping, green inflows, bold running balances, and note/tag descriptions.
  - **Smart Auto-Save & Dual Export Hub (Auto-Save + SAF Custom Picker)**:
    - Automatically saves generated PDFs to `Downloads/Folio/Statements/` via MediaStore for zero-friction 1-tap exports.
    - Completion modal surfaces immediate actions: `[ View / Print ]` (launches default PDF viewer or print spooler), `[ Share ]` (system share sheet via `FileProvider`), and `[ Save to Custom Location... ]` (launches Android Storage Access Framework document picker for saving directly to Google Drive, OneDrive, or custom folders).
- **Secondary Currencies & Manual Exchange Rate Engine**: Added universal multi-currency support allowing users to manage foreign accounts with deterministic, manual exchange rates and consolidated net worth calculations:
  - **Dedicated Settings Row & Bottom Sheet**: Introduced a dedicated *Secondary Currencies* option in the *ORGANIZATION & TAXONOMY* settings card (directly below *Currency & Number Format*). Opening it displays all active secondary currencies with live rates, 1-tap rate editing, deletion, and an intuitive *+ Add Secondary Currency* flow with 170+ currency search and popular chips.
  - **Curated Account Currency Picker**: In `AccountEditorBottomSheet.kt`, replaced the cluttered 170+ currency picker with a clean, active currency chip row displaying the Primary currency, each added Secondary currency (with its configured exchange rate), and an expandable drawer for other currencies.
  - **Converted Net Worth Engine**: Upgraded `observeNetWorthOverview` in `AccountRepositoryImpl.kt` and `MoneyFormatterAndCalculator.convertMinorMoney` to properly convert non-primary account balances into the primary currency using the user's manual exchange rate, resolving an existing raw-integer summation bug across differing currencies while maintaining fraction digit precision (handling JPY 0 decimals, USD/INR 2 decimals, KWD 3 decimals).
  - **DataStore & Backup Persistence**: Persisted secondary currencies and their rates in Android DataStore and the automated rolling JSON backup archive (`AutoBackupEngine.kt`), ensuring full portability across devices.
- **Intelligent CSV Duplicate Management & Interactive Inspection**: Elevated spreadsheet import safeguards with crystal-clear duplicate control in `ImportCsvPreviewDialog`:
  - **Explicit Inclusion / Exclusion Control**: Replaced ambiguous duplicate checkboxes with two prominent, selectable radio cards whenever duplicates are detected:
    - *Exclude Duplicates (Recommended)*: Skips rows already recorded in the ledger to avoid accidental double-counting from overlapping bank statements. Shows projected import count of new records.
    - *Include All Rows (Allow Duplicates)*: Imports every row including identical ones, protecting legitimate repeat purchases on the same day (such as multiple coffees or transit tickets).
  - **Interactive Duplicate Inspector**: Added an expandable inspection panel displaying sample duplicate records (Payee, Date, Account, and Amount) directly in the preview dialog before confirming import.
  - **Dynamic Action Button & Clear Badging**: Dynamically calculates and displays the exact record count on the confirmation button (`Import X Records`), prevents disabled button states when intentionally importing duplicates, and surfaces a green badge when all rows in the file are unique.
- **Dedicated 'DATA & BACKUPS' Settings Architecture & Focused Modular Sheets**: Re-architected Folio's data management UX to prevent cognitive overload and bottom sheet crowding, perfectly aligning it with the rest of Folio's card-based settings system:
  - **Dedicated Card Header (`DATA & BACKUPS`)**: Promoted Data & Backup into its own top-level settings card, cleanly separating database administration from community and help resources (`HELP & COMMUNITY`).
  - **Glanceable Live Status Indicators**: Enhanced `SettingsNavigationRow` with subtitles and dynamic status badges—displaying live auto-backup status (`[ ● 30 Days (Tiered) ]` or `[ Disabled ]`), manual backup recency (`[ ● Today, 1:15 PM ]`), and spreadsheet tools directly from the main settings view without opening any sheet.
  - **Modular, Focused Bottom Sheets**: Replaced the monolithic 8-card sheet with three concise, single-purpose sheets:
    - `AutoBackupBottomSheet`: Dedicated to session exit dirty tracking, retention chips, policy explanation, and snapshot history with 1-tap Restore and Share.
    - `TransferAndManualBackupBottomSheet`: Dedicated strictly to phone migration, featuring hero status banner, 1-tap share, portable `.zip` creation, cloud document picker restore, and local fast restore.
    - `SpreadsheetsBottomSheet`: Dedicated to CSV workflows (multi-section spreadsheet export for Excel/Google Sheets and bank statement CSV import with interactive preview).
  - **Intentional Danger Zone**: Positioned "Reset All Ledger Data" with distinct red styling at the bottom of the data section, triggering the confirmation dialog directly without being buried inside backup menus.
- **Smart Session Auto-Backup Engine & 30-Day Tiered Retention Model**: Engineered an automated, privacy-first rolling backup engine that safeguards user financial records with zero manual intervention and zero cloud dependencies:
  - **Smart Dirty Session Tracking (`SessionChangeTracker` & Room `InvalidationTracker`)**: Automatically flags sessions as dirty whenever mutations occur across any of the 9 database tables (`accounts`, `categories`, `transactions`, `transaction_splits`, `tags`, `budgets`, `recurring_rules`, `bookmarks`, `installment_plans`). Read-only sessions (viewing charts or checking balances) write 0 bytes to disk.
  - **Background Exit Execution**: Hooks into `MainActivity.onStop()` running asynchronously on `applicationScope` (`Dispatchers.IO`), capturing snapshot state safely when the user backgrounds or leaves the app without blocking the UI main thread. Includes a 60-second debounce mechanism to eliminate churn during rapid consecutive app switching.
  - **30-Day Tiered Retention Model (`AutoBackupRetentionEngine`)**: Intelligently manages storage footprint (~5 MB total) through a multi-tier pruning algorithm:
    - *Intra-day snapshots*: Keeps up to 3 session snapshots for the current day.
    - *Daily snapshots*: Retains 1 snapshot per day for the last 30 days (spanning a complete salary and credit card billing cycle).
    - *Monthly archives*: Preserves 1 snapshot per month for up to 3 months prior (quarterly audit history).
  - **User Configurable Retention Policies**: Exposes choices for `10 Sessions`, `30 Days (Tiered - Recommended)`, and `90 Days` directly in `DataAndBackupBottomSheet.kt` with live snapshot file counts, relative timestamps (`Today, 2:30 PM`, `Yesterday, 9:15 AM`), individual snapshot sizes in KB, and 1-tap **Restore** / **Share** actions.
  - **Dual-Restore Architecture & High-Impact Backup Upgrades**: Upgraded Folio's data management and backup system with comprehensive reliability and migration improvements:
  - **Dual-Restore (Local vs. SAF Document Picker)**: Added a native Android Storage Access Framework file picker (`ActivityResultContracts.OpenDocument`) for restoring backups from any cloud provider (Google Drive, OneDrive, Nextcloud, local folders), alongside the 1-tap quick restore from `Downloads/Folio`.
  - **Supporter Status & Tier Persistence**: Persisted `isSupporter: Boolean` and `supporterTier: String?` directly in the backup JSON archive, preventing loss of supporter recognition and badges when transferring to a new device.
  - **Pre-Restore Automatic Rollback Snapshot**: Automatically generates an internal safety rollback archive in app cache prior to executing database restore wipes, guarding against corrupted or incorrect backup selection.
  - **1-Tap Share Sheet Integration**: Configured `androidx.core.content.FileProvider` in `AndroidManifest.xml` and wired 1-tap sharing via `Intent.ACTION_SEND` on backup completion snackbars and in the Data Management bottom sheet.
  - **Dynamic 'Last Backup' Status Indicator**: Displayed reactive, formatted backup recency (`[ ● Today, 1:15 PM ]`, `[ 3d ago ]`) on the Settings row and inside `DataAndBackupBottomSheet.kt` backed by $O(1)$ DataStore lookups with zero disk-scanning overhead.
  - **Copy Polish**: Updated archive copy from misleading 'Encrypted archive' to 'Complete portable archive of database, settings & tier'.
- **Modernized User Guide & In-Depth CSV Import Documentation**: Completely updated the in-app user guide in `FolioGuideAndSupportSheets.kt` to accurately reflect current app behavior: corrected account edit gestures (tap opens Passbook, long-press opens editor) and the Gboard-first numeric entry with 1-tap calculator toggle. Added a comprehensive "Importing CSV Data & Spreadsheet Templates" guide covering standard headers (`Date`, `Type`, `Amount`, `Category`, `Account`, `Note`, `Transfer To`, `Payee`, `Tags`), supported date formats (`YYYY-MM-DD`, `DD/MM/YYYY`, etc.), auto-creation of missing accounts/categories, transfer syntax, and smart duplicate detection. Added dedicated topics for "Account Passbooks & 1-Tap Balance Reconciliation" and "Quick Gestures & Speed Shortcuts", and restructured FAQs into individual searchable cards (Privacy & 100% Offline, Phone Data Transfer via ZIP, Double-Entry Rules, Salary Payday month alignment, Bank CSV import, and Balance Reconciliation).
- **Ledger-Focused Floating Action Button (FAB)**: Restricted the `+ Add Transaction` FAB visibility exclusively to the `Trans` tab (`FolioMainTab.TRANSACTIONS`). Completely unblocks the bottom-right corner of the `Stats` tab, preventing accidental category drill-down mis-clicks and keeping the analytics dashboard clean and uncluttered.
- **Guided In-App Feedback & Feature Ideas**: Added a streamlined, 3-step feedback bottom sheet accessible from Settings (`DATA, BACKUPS & HELP`). Features 4 guided categories (Bug Report, Feature Idea, Currency/Format, General Note), dynamic contextual topic chips, and an optional note field. Pre-fills an email directed to `support.folioledger@gmail.com` via standard Android `Intent.ACTION_SENDTO` with device troubleshooting context (`Build.MANUFACTURER`, `Build.MODEL`, `Build.VERSION.RELEASE`, and `BuildConfig.VERSION_NAME`) while maintaining 100% offline device privacy and user transparency.
- **3-Tier Supporter Badge & Tip Jar Evolution (Roasted Mocha ➔ Emerald ➔ Radiant Sovereign Gold)**: Structured a 3-tier supporter progression (`SupporterTier`) with distinct titles and color identities: Rich Roasted Mocha `☕ Coffee Supporter` (`#8D4925`), Folio Signature Emerald `★ Generous Supporter` (`#059669`), and Radiant Sovereign Gold `👑 Folio Patron` (`#F59E0B`). The Settings row badge, Minimalist Footer edition tag, and in-sheet celebration banner adapt their typography, color dots, and icons to celebrate each contributor while keeping supporter state permanently persisted in Android DataStore.
- **UI De-Cluttering & Text Polish**: Streamlined screen headers, empty states, and settings items by removing redundant micro-copy ("Tap to open passbook • Long press to edit", "Customize themes...", "Tap row to drill down", and redundant button quotes in empty states) and eliminating repetitive right-side badges in Settings.
- **LeakCanary Memory Diagnostics (`debugImplementation`)**: Added Square's LeakCanary 2.14 in debug builds for automated, passive detection of retained Activities, unclosed bottom sheet coroutine scopes, and ViewModel lifecycle leaks with zero overhead on release builds.
- **Spotless & ktlint Code Hygiene Engine**: Integrated the Spotless code formatting plugin with ktlint rules, automating whitespace trimming, trailing line normalization, and style consistency across all Kotlin source files via `./gradlew spotlessApply`.
- **Passbook Bottom Sheet Height Anchoring**: Fixed the `AccountPassbookBottomSheet` container height to a stable 92% viewport height (`0.92f`) and weighted scroll list (`weight(1f)`), eliminating visual jumping and moving pagination touch targets when switching between empty and active transaction months.

## [1.3.0] - 2026-10-28
### Added
- **Instant Tab Switching & Pre-Warmed Data Pipelines**: Converted `StatsAndBudgetsViewModel` and `AccountsViewModel` state flows to `SharingStarted.Eagerly` and added shell-level state observation in `FolioMainShell.kt` via `collectAsStateWithLifecycle()`. Completely eliminated the cold-subscriber timeout previously caused by `WhileSubscribed(5_000L)` that unmounted upstream Room queries when away from the tab for $>5$ seconds. Tab switches to **Stats** and **Accounts** now render instantly with fully populated, pre-computed data ($0\text{ms}$ pop-in delay). Made database transaction querying in `AccountsViewModel` strictly lazy, only observing transactions when an account passbook is explicitly opened.
- **Scroll Jank Elimination & Recomposition Skipping in Transactions Ledger**:
  - Eliminated the continuous frame-by-frame linear list search inside `derivedStateOf` in `rememberHeaderAmountsAlpha` across `LedgerSubTabs.kt`.
  - Removed `Modifier.animateItem()` placement tracking from fast-scrolling list cards across `LedgerSubTabs.kt`, `AccountPassbookBottomSheet.kt`, and `StatsAndBudgetsScreen.kt` to eliminate physics layout churn during scroll passes.
  - Added `@Immutable` to all core domain models in `DomainModels.kt` (`TransactionItem`, `SplitLineItem`, `AccountSummary`, `NetWorthOverview`, `PeriodLedgerSummary`, `CategorySpendStat`, `BudgetProgress`, etc.), enabling 100% recomposition skipping for `DailyDateGroupHeaderRow`, `DailyGroupedTransactionsCard`, and `LedgerTransactionRow`.
  - Added transaction ID keyed composition (`key(tx.id)`) inside `DailyGroupedTransactionsCard` and cached string/color allocations (`resolvedColorMode`, `titleText`, `secondaryDescription`, `tagsText`) via `remember` inside `LedgerTransactionRow`.
  - Added static thread-safe caching (`ConcurrentHashMap`) for `parseHexColorSafely` and `resolveFolioIcon` in `FolioCommonComponents.kt`, eliminating regex/string lowercasing and branching during list composition.
  - Memoized tabular `TextStyle` creation in `MoneyText` via `remember(style, fontWeight)`, saving multiple heap allocations per money label on every scroll frame.
- **Main-Thread CPU Offloading & Viewport Tuning (Performance Batch 1)**: Offloaded all heavy ViewModel combiners across `LedgerHomeViewModel.kt`, `AccountsViewModel.kt`, and `StatsAndBudgetsViewModel.kt` to `Dispatchers.Default` using `.flowOn(Dispatchers.Default)`, ensuring passbook sorting, net worth trajectory math, and running balance calculations never block the Android UI Main thread. Added a 150ms debounce to the search query flow to prevent filter recalculations on mid-typing keystrokes while maintaining instant TextField input response. Optimized yearly overview computation to only calculate weekly buckets for expanded months. Removed duplicate `FolioTheme` tree wrapping in `FolioMainShell.kt` to eliminate redundant `CompositionLocalProvider` nodes. Tuned `beyondViewportPageCount = 0` in `HorizontalPager` across `LedgerHomeScreen.kt` and `StatsAndBudgetsScreen.kt` for true lazy subtab composition.
- **Scroll Jank Elimination & Draw-Phase Alpha (Performance Batch 2)**: Replaced primitive `Float` alpha parameter in `DailyDateGroupHeaderRow` with a deferred lambda provider `amountsAlpha: () -> Float`. Deferring sticky header alpha evaluation directly into `Modifier.graphicsLayer { alpha = amountsAlpha() }` allows Jetpack Compose to skip both Composition and Layout phases during list scrolling, locking scroll frame rates to 120 FPS / 60 FPS without scroll stutter. Added explicit `contentType` metadata (`day_group_header`, `day_group_card`, `transaction_row`, `weekly_bucket_card`, `yearly_month_card`) to `LazyColumn` items across all Ledger subtabs for item recycling and Compose node reuse.
- **Lightweight Scalar Projection for Account Balances (Performance Batch 3)**: Introduced `TransactionBalanceProjection` with 6 scalar columns in `FolioDaos.kt` and `AccountAndTransactionRepositoriesImpl.kt`. Replaced full `TransactionWithDetails` hydration in `observeAllAccounts()`, dropping memory usage by $>95\%$ and eliminating SQLite 999-parameter query chunking during account balance calculations while preserving double-entry accounting and credit card cycle rules.
- **Indexed Month-Bounded SQLite Search (Performance Batch 3)**: Optimized `searchAndFilterTransactions()` in `AccountAndTransactionRepositoriesImpl.kt` to route bounded period searches through `observeTransactionsInRange(startDay, endDay)` so SQLite filters via the `ledgerDateEpochDay` index, bypassing multi-year full-table scans.
- **Asynchronous Room SQLite Warmup & Cold-Start Backup (Performance Batch 3)**: Offloaded `FolioDatabase.openHelper.writableDatabase` and pre-migration backup in `MainActivity.kt` to `lifecycleScope.launch(Dispatchers.IO)` to prevent UI thread blocking during cold launch.
- **Subscribed-Only Pipeline Lifecycle (Performance Batch 3)**: Switched `AccountsViewModel` and `StatsAndBudgetsViewModel` state flows from `SharingStarted.Eagerly` to `SharingStarted.WhileSubscribed(5_000L)`, eliminating database contention and CPU processing during initial startup.
- **Canvas Allocation Hoisting & Draw Loop Optimization (Performance Batch 4)**: Eliminated all per-frame heap allocations (`PathEffect.dashPathEffect`, `Path()`, `CornerRadius`) inside Canvas rendering blocks in `FolioCharts.kt` (`IncomeExpenseTrendBarChart`, `SixMonthTrendBarChart`) and `NetWorthAnalyticsComponents.kt` (`NetWorthGrowthCanvasChart`). Cached drawing objects outside the Canvas scope using `remember`, invoking `.reset()` on reusable paths to prevent Garbage Collection (GC) pauses and micro-stutters during chart animations and scroll gestures.
- **Room SQLite Composite Indices & v2 -> v3 Migration (Performance Batch 4)**: Added composite indices `(accountId, ledgerDateEpochDay)` and `(transferToAccountId, ledgerDateEpochDay)` on `TransactionEntity` in `FolioEntities.kt`. Incremented database version to 3 with automated non-destructive `MIGRATION_2_3` in `FolioDatabase.kt` and exported schema `3.json`, speeding up account passbook date-range scans and credit card cycle queries.
- **Compose Compiler Stability Configuration (Performance Batch 4)**: Created `app/compose_stability_config.conf` marking `java.time.*`, `kotlin.collections.*` (`List`, `Map`, `Set`), and `com.folioledger.app.domain.model.*` as stable types. Configured `composeCompiler { stabilityConfigurationFiles.add(...) }` in `app/build.gradle.kts`, empowering the Compose compiler to skip recomposition for UI nodes when input models are unchanged.
- **Fluid List Recycling & Subtab Content Types**: Added explicit `contentType` metadata across `AccountPassbookBottomSheet.kt` (`passbook_hero`, `passbook_actions`, `passbook_summary`, `passbook_header`, `passbook_day_header`, `passbook_entry`), `AccountsScreen.kt` (`accounts_summary_banner`, `account_group_card`), and `StatsAndBudgetsScreen.kt` (`stats_summary_banner`, `donut_chart`, `category_list_header`, `category_stat_row`, `budget_progress_card`), preventing Compose layout node destruction and re-allocation during fast scrolling across passbooks and stats.
- **Physics-Based List Item Animations (`Modifier.animateItem()`)**: Added `Modifier.animateItem()` to daily transaction cards and filtered list cards in `LedgerSubTabs.kt` / `LedgerItemCards.kt`, passbook rows in `AccountPassbookBottomSheet.kt`, and ranked category & budget progress cards in `StatsAndBudgetsScreen.kt`. Transaction inserts, edits, reconciliations, and deletions now animate smoothly with native spring physics instead of abrupt positional snaps.
- **Tactile Haptic Feedback Suite (`LocalHapticFeedback`)**: Integrated subtle tactile haptic feedback (`HapticFeedbackType.TextHandleMove`) on every numeric key, operator, backspace, and navigation tap on the calculator keypad in `CalculatorNumericKeypad.kt`, and bottom navigation tab re-taps / jump-to-today in `FolioMainShell.kt`. Added solid confirmation haptics (`HapticFeedbackType.LongPress`) on transaction save and balance reconciliation approval.

### Changed
- **Stats Tab UI Uniformity & Card Corner Standardization**: Standardized all primary cards in `StatsAndBudgetsScreen.kt` and `NetWorthAnalyticsComponents.kt` to `RoundedCornerShape(16.dp)` (standardizing `BudgetSummaryProgressCard` from 18.dp, and category drill-down cards from 14.dp). Unified list vertical spacing across all Stats sub-tabs (`StatsBreakdownSubTabContent`, `BudgetsManagerSubTabContent`, `CreditHealthDashboardCard`, and `BudgetEditorDialog`) to `12.dp`, perfectly harmonizing with Ledger and Accounts tabs. Refined Expenses/Income toggle chips with standard 1.0dp subtle borders matching Material 3 filter chip tokens.
- **1-Tap Navigation Return to Today & Header Badge**: Removed the floating "Jump to Today ⤶" extended floating action button from `LedgerHomeScreen.kt` to prevent occlusion of bottom transactions. Replaced with an active-tab re-tap gesture on the `Transactions` bottom navigation item in `FolioMainShell.kt` that triggers `jumpToToday()` and smoothly scrolls the transaction list to top. Added a subtle Material 3 "Today" indicator badge in `PeriodSelectorHeader.kt` next to period navigation when browsing past or future dates.
- **Native Gboard Amount Entry & Direct 1-Tap Save Progression**: Upgraded `TransactionEntrySheet.kt` to open the native software keyboard (Gboard) by default with `KeyboardType.Decimal` on sheet launch, replacing the mandatory calculator dock. Added an interactive calculator toggle button (`Icons.Filled.Calculate`) in the amount row to switch to `CalculatorNumericKeypad` on demand for arithmetic. Streamlined the step-by-step entry flow (`Amount` → `Account` → `Category`), eliminating the redundant reset to the calculator keypad upon category selection; selecting a category now automatically hides the dock and surfaces the anchored Save action bar directly for instant 1-tap saving.



## [1.2.0] - released

### Added
- **1-Tap Account Balance Reconciliation Tool**: Added a dedicated `Reconcile` action in `AccountPassbookBottomSheet.kt` and `reconcileAccountBalance()` in repository layer. Computes balance discrepancies ($\Delta$) and inserts a `TransactionType.RECONCILIATION` system entry without corrupting historical ledger data or operating cash flow stats.
- **Starting Balance Date Anchor Picker**: Added an interactive Starting Balance Date selector in `AccountEditorBottomSheet.kt` for backdating initial account balances.
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
- **Credit Card Specific Statement Billing Cycle Synchronization**: Refactored passbook period calculations in `AccountsViewModel.kt` (`PassbookPeriodCalculator`) for credit cards to anchor to the card's actual `settlementDayOfMonth` rather than generic global month start preferences. Navigating statement periods now advances strictly cycle-by-cycle (e.g. `19 Sep – 18 Oct`), dynamically labeling periods as `"${Month Year} Statement"` for closed statements or `"${Month Year} Cycle (Active)"` for current unbilled activity.
- **Credit Card Passbook Unified Hero Hub & Dual Action Row**: Overhauled credit card passbook layout in `AccountPassbookBottomSheet.kt`. Replaced dual stacked cards with a single `CreditCardPassbookHeroCard` combining live debt, tier-colored credit utilization meter, available limits, and a 2-column billing cycle split (Billed Statement Due with dynamic due-countdown chips vs Active Unbilled Spends). Relocated management actions (`Reconcile` and `Edit Card`) to the top header toolbar, creating a dedicated, spacious 50/50 touch action row for `[ 💳 Settle Bill ]` and `[ + Add Expense ]`.
- **Trend Analytics & Net Worth Trajectory with Dynamic Zero Baseline**: Refactored `NetWorthGrowthCanvasChart` in `NetWorthAnalyticsComponents.kt` to support dynamic range scaling across positive and negative net worth, replacing fixed 50% baselines and zero-clamping with an adaptive zero-baseline that accurately maps negative net worth. Enhanced net worth trajectory with smooth cubic Bézier curves and vertical gradient area fills. In `StatsAndBudgetsViewModel.kt`, updated trajectory computation to calculate 12 monthly snapshots (Jan–Dec) when annual granularity (`ANNUALLY`) is selected, alongside adaptive month X-axis labeling.
- **Credit Health Semi-Circular Dial & Live Snapshot Badge**: Upgraded `CreditHealthDashboardCard` with a custom semi-circular arc gauge (`CreditUtilizationGauge`) spanning 4 health zones (Optimal $<10\%$, Good $10-30\%$, High $30-50\%$, Risky $>50\%$) with tick marks and centered utilization percentage. Added a prominent `[ ⏱ Live Snapshot ]` header badge clarifying that credit utilization is a real-time account snapshot independent of period navigation.
- **Proactive Budget Warnings, Spend Velocity & Daily Safe Spend Allowance**: Integrated an immediate top alert banner in `BudgetsManagerSubTabContent` highlighting any budgets exceeding 100% or crossing their 80% alert threshold. Upgraded `BudgetProgressCard` with semantic status badges (`ON TRACK`, `NEAR LIMIT (80%)`, `OVERSPENT`), real-time **Spend Velocity / Burn Rate** ratio comparing current spending pace against days elapsed in the month, and a **Daily Safe Spend Remaining** allowance counter indicating safe daily spending limits for remaining days.
- **Credit Card Account Editor Streamlining**: Removed the redundant and bulky live billing cycle preview block below the Total Debt and Available Credit row in `AccountEditorBottomSheet.kt` while preserving the clean standalone 2-column cycle preview above.
- **Stats Tab UI Uniformity & Card Corner Standardization**: Standardized all primary cards in `StatsAndBudgetsScreen.kt` and `NetWorthAnalyticsComponents.kt` to `RoundedCornerShape(16.dp)` (standardizing `BudgetSummaryProgressCard` from 18.dp, and category drill-down cards from 14.dp). Unified list vertical spacing across all Stats sub-tabs (`StatsBreakdownSubTabContent`, `BudgetsManagerSubTabContent`, `CreditHealthDashboardCard`, and `BudgetEditorDialog`) to `12.dp`, perfectly harmonizing with Ledger and Accounts tabs. Refined Expenses/Income toggle chips with standard 1.0dp subtle borders matching Material 3 filter chip tokens.
- **1-Tap Navigation Return to Today & Header Badge**: Removed the floating "Jump to Today ⤶" extended floating action button from `LedgerHomeScreen.kt` to prevent occlusion of bottom transactions. Replaced with an active-tab re-tap gesture on the `Transactions` bottom navigation item in `FolioMainShell.kt` that triggers `jumpToToday()` and smoothly scrolls the transaction list to top. Added a subtle Material 3 "Today" indicator badge in `PeriodSelectorHeader.kt` next to period navigation when browsing past or future dates.
- **Native Gboard Amount Entry & Direct 1-Tap Save Progression**: Upgraded `TransactionEntrySheet.kt` to open the native software keyboard (Gboard) by default with `KeyboardType.Decimal` on sheet launch, replacing the mandatory calculator dock. Added an interactive calculator toggle button (`Icons.Filled.Calculate`) in the amount row to switch to `CalculatorNumericKeypad` on demand for arithmetic. Streamlined the step-by-step entry flow (`Amount` → `Account` → `Category`), eliminating the redundant reset to the calculator keypad upon category selection; selecting a category now automatically hides the dock and surfaces the anchored Save action bar directly for instant 1-tap saving.
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
- **Financial Month Custom Start Days (Days 29–31)**: Relaxed domain `AccountingRulesEngine.computeFinancialMonthRange` to support custom start days 1..31 with month-length clamping (`minOf(desiredDay, yearMonth.lengthOfMonth())`), resolving `IllegalArgumentException` and `DateTimeException` crashes during February and 30-day months.
- **Debit Card Passbook Desynchronization**: Included `parentAccountId` in `effectiveAccountIds` when building debit card passbook views in `AccountsViewModel.kt` so running balances accurately track live parent bank account activity.
- **Triple Shadowing Refactoring**: Renamed presentation-layer helper objects in `AccountsViewModel.kt` (`PassbookPeriodCalculator`), `LedgerHomeViewModel.kt` (`LedgerPeriodCalculator`), and `TransactionEntryViewModel.kt` (`TransactionEntryRulesEngine`) to eliminate class shadowing of domain `AccountingRulesEngine`.
- **System Transaction Cash Flow Isolation**: Enforced strict isolation of `TransactionType.OPENING_BALANCE` and `TransactionType.RECONCILIATION` system entries in `AnalyticsAndBudgetRepositoryImpl.kt` so capital introductions and balance reconciliations never pollute monthly income, expense, or budget statistics.
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
