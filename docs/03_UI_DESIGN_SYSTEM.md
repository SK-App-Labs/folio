# Folio — UI/UX & Material 3 Design System (SSOT #3)

## 1. Visual Identity & Theme Engine
- **Aesthetic:** Minimalist editorial finance ledger — generous whitespace, crisp tabular numerals (`fontFeatureSettings = "tnum"`), subtle surface elevation, enclosed card borders, and zero visual clutter.
- **6 Supported Theme Modes (`FolioThemeMode`):**
    1. `SYSTEM` (Follows Android Light/Dark toggle automatically)
    2. `LIGHT` (Clean crisp white `#FFFFFF` surface)
    3. `SEPIA` (Warm Sand parchment paper cream `#F7F3E9` for low eye-strain daytime viewing)
    4. `SLATE_GRAY` (Charcoal Gray matching Google Chrome's Dark Theme palette: `#202124` background, `#292A2D` surface, `#35363A` container, `#E8EAED` text, `#9AA0A6` secondary text, `#3C4043` border outline, lightened `#8E96A0` OFF-switch thumb)
    5. `DARK` (Midnight Navy `#0F172A` deep slate)
    6. `AMOLED_BLACK` (Pure `#000000` background for maximum OLED battery savings)
- **12 Dynamic & Custom Accent Color Palettes (`FolioAccentPalette` in Guaranteed 6x2 Grid):**
    - Supports Android 12+ Material You (`dynamicColor`) toggle.
    - Includes 12 curated financial accent palettes:
      1. **Emerald Ledger** (`#059669` - Default)
      2. **Electric Cobalt** (`#2563EB` - #1 Global Fintech Trust Blue used by Wise & PayPal)
      3. **Royal Indigo** (`#4F46E5`)
      4. **Vivid Violet** (`#7C3AED` - Modern fintech purple used by NuBank & Revolut)
      5. **Rose Gold** (`#F43F5E` - Warm luxury coral rose)
      6. **Fresh Lime** (`#65A30D` - High-energy neon green)
      7. **Electric Cyan** (`#06B6D4` - Glacier blue turquoise)
      8. **Golden Bronze** (`#B45309` - Warm terracota earth tone)
      9. **Oceanic Teal** (`#0D9488`)
      10. **Ruby Crimson** (`#E11D48`)
      11. **Saffron Amber** (`#D97706`)
      12. **Monochrome Slate** (`#475569`)
- **Cold Start & Splash Screen:** Integrates AndroidX `core-splashscreen` (`Theme.Folio.Starting`) with data-sync splash hold and 380ms morph-to-header exit animation.

## 2. Semantic Financial Colors (Consistent Across All Screens)
- **Income / Asset Positive:** Emerald (`#10B981` Dark / `#059669` Light)
- **Expense / Liability Outflow:** Coral Red (`#F43F5E` Dark / `#E11D48` Light)
- **Transfer / Settlement / Neutral:** Slate Blue (`#3B82F6` Dark / `#2563EB` Light)
- **Budget Safe (<80%):** Primary Accent
- **Budget Warning (80%–99%):** Amber (`#F59E0B`)
- **Budget Exceeded (100%+):** Coral Red (`#E11D48`)

## 3. Shape & Typography Rules
- **Header Standardization:** Screen top headers across all tabs use `headlineSmall` (24sp Bold) with `padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp)` to align perfectly across `AccountsScreen`, `TransactionsScreen`, `StatsAndBudgetsScreen`, and `SettingsScreen`.
- **Card Enclosure Standard:** `SettingsGroupCard`, `AccountGroupSectionCard`, and list cards specify `border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle)` with `RoundedCornerShape(18.dp)` or `RoundedCornerShape(16.dp)` so card boundaries stay crisp and enclosed in both Light and Dark themes.
- **Tabular Numbers:** Every currency amount component (`MoneyText`) applies `fontFeatureSettings = "tnum"` so digits align vertically and never jitter during animations.
- **Category Typography Standard:** Parent titles = `titleMedium` (16sp Bold); Subcategory counts = `bodySmall` (12sp); Subcategory items = `bodyLarge` (15sp–16sp SemiBold); Subcategory icon badges = `32.dp`.
- **Accordion Chevron Rule:** 16.dp IconButton (`ExpandMore`/`ExpandLess`) is rendered on ALL main category cards (including empty ones) to reveal subcategories or the `+ Add Subcategory under [Parent]` button with empty state guidance.
- **Edge-to-Edge Navigation Bar Insets:** Avoids double window insets padding between the bottom bar and list viewports, guaranteeing uniform, comfortable bottom list clearance across all devices.

## 4. Navigation Architecture (4 Primary Bottom Tabs + Scoped FAB)
1. **Tab 1: `Trans.` (Ledger Home)**
    - Top Period Selector (`< Oct 2026 >`) + Search, Filter, and Bookmark (`★`) icons.
    - Decomposed into 5 domain sub-files (`LedgerHeaderBanners`, `LedgerItemCards`, `LedgerSubTabs`, `LedgerSheetsAndDialogs`, `LedgerHomeScreen`).
    - Sticky daily date headers with scroll-fading daily totals.
    - 5 Horizontal Sub-Tabs: `Daily` | `Calendar` | `Weekly` | `Monthly` | `Summary`.
2. **Tab 2: `Stats` (Analytics, Intelligence & Budgets)**
    - Top-bar chip dropdown granularity selector (`StatsPeriodGranularityDropdownChip`).
    - 4 Horizontal Sub-Tabs:
      - **`Overview` (`StatsSubTab.STATS`)**:
        - Donut chart + Ranked main category list with drill-down breakdown and category-specific 6-month historical trend.
        - Cashflow section featuring **Safe-to-Spend Card** (`SafeToSpendCard`) and 14-day proactive **Shortfall Alert Radar** (`ShortfallAlertCard`).
        - Net Worth section featuring **Net Worth Velocity Cockpit** (`NetWorthVelocityCockpit`), monthly burn rate, and liquid cash emergency runway.
      - **`Cards` (`StatsSubTab.CREDIT_CARDS`)**:
        - Credit Cards Cockpit with Statement Status Pipeline (`OVERDUE`, `DUE_SOON`, `ISSUED`, `CURRENT`, `SETTLED`).
        - Credit Bureau 30% Threshold Calibration and **Smart Swipe Headroom** indicator.
        - Celebratory milestone banner when all card dues are zero.
      - **`Budgets` (`StatsSubTab.BUDGETS`)**:
        - Monthly-locked budget progress bars (Overall + Category-specific) with dynamic threshold color alerts.
      - **`Tags` (`StatsSubTab.TAGS_AND_NOTES`)**:
        - **Events, Trips & Reimbursements Hub**: Smart mode detection (Reimbursable claims with pending owed balance vs Trip/Event passbook with burn rate) + 1-tap receipt drill-down sheet (`TagProjectDrillDownBottomSheet`).
        - **Merchant & Payee Intelligence**: Dual sort (Total Outlay vs Frequency) + Payee drill-down sheet (`PayeeDrillDownBottomSheet`).
        - Note Rankings.
3. **Tab 3: `Accounts` (Assets, Liabilities & Cards)**
    - Top 3-Column Net Worth Banner: `Assets` | `Liabilities` | `Net Worth`.
    - Grouped Account Cards matching `SettingsGroupCard` pattern (`labelLarge` uppercase primary titles: `CASH (1)`, `SAVINGS (2)`, `DEBIT CARDS (2)`, etc.). Whole-header row tap toggles expand/collapse without chevron icons.
    - **Compact Credit Card Rows**: 2-column layout (`DUE NOW` & `UNBILLED`) with `Due [Date]` subtext. Smart color coding (`DUE NOW` in `expenseCoral` when > 0, `incomeEmerald` when <= 0; `UNBILLED` in `onSurfaceVariant`). Optimized column widths (`78.dp` & `72.dp`), returning `+52.dp` extra horizontal width to card names.
    - **Streamlined Debit Card Rows**: Consolidated subtext (`HDFC Bank · Bal: ₹1,20,000.00`) with positive parent bank balance in `incomeEmerald` (or overdrawn in `expenseCoral`). Reduced row height to ~52.dp.
    - **Streamlined Account Editor Sheet (`AccountEditorBottomSheet`)**: `Opening Balance` sits directly below `Account Name`. 10 account groups categorized under 3 Segment Tabs (`Liquid` | `Cards` | `Savings & Debt`). Auto-calculates Credit Card Total Debt in real time (`Billed Due + Unbilled`). Collapsible foreign currency picker and collapsible appearance customizer.
    - **Strict Ledger Safety Deletion Rule**: Accounts with 0 transactions can be permanently deleted after confirmation. Accounts with 1+ linked transactions cannot be deleted to protect historical reports, guiding the user to **Archive Account** instead.
    - **Decluttered Account Passbook Sheet (`AccountPassbookBottomSheet`)**: Header uncluttered by moving `Edit Account` to the Quick Action Bar (`Add Tx` | `Settle Bill` | `Edit Account`). Removed repeated `"Day End Bal:"` label prefix from day-grouped transaction section headers.
4. **Tab 4: `Settings` (Configuration & Data)**
    - Modularized into `com.folioledger.app.ui.settings.sheets` package (`AppearanceBottomSheet`, `RegionalFormatBottomSheet`, `DataAndBackupBottomSheet`, `CategoryManagerBottomSheet`, `TagsAndBookmarksBottomSheet`, `AutoBackupBottomSheet`, `TransferAndManualBackupBottomSheet`, `SpreadsheetsBottomSheet`, `SecondaryCurrenciesBottomSheet`, `PdfStatementExportBottomSheet`).
    - 4 Grouped Cards (`ORGANIZATION & TAXONOMY`, `APPEARANCE & SECURITY`, `DATA & BACKUPS`, `HELP & COMMUNITY`).
    - Currency & Number Formatting Sheet (`RegionalFormatBottomSheet` with 1-line `Standard` vs `Indian` grouping, real-time currency search bar, popular currency chips, and full-width currency cards).
    - Secondary Currencies & Exchange Rates (`SecondaryCurrenciesBottomSheet` with active rate editing and 170+ currency picker).
    - PDF Account Statement & Tax Reports (`PdfStatementExportBottomSheet` with universal tax schedule and branded passbook exports).
    - Automated Backup Engine (`AutoBackupBottomSheet` with 30-day tiered retention and local/SAF dual restore).
    - Category & Subcategory Manager (`CategoryManagerBottomSheet` with scaled typography and always-present chevrons).
    - Unified Tags & Bookmarks (`TagsAndBookmarksBottomSheet` with `#Tags` and `Bookmarks ★` tabs).
    - Appearance & Theme Sheet (`AppearanceBottomSheet` with Live Theme & Accent Preview card, 6 visual theme mode cards, and guaranteed 6x2 accent swatch grid).
- **Floating Action Button (`+`):** Scoped strictly to the `TRANSACTIONS` tab (`FolioMainTab.TRANSACTIONS`) using `AnimatedVisibility`. Completely unblocks the bottom-right corner of the `Stats`, `Accounts`, and `Settings` tabs. Opens `TransactionEntrySheet` with calculator keypad, mode tabs, Splits, Bookmarks, and `CategoryPickerBottomSheet`.
