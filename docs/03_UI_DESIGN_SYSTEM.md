# Folio — UI/UX & Material 3 Design System (SSOT #3)

## 1. Visual Identity & Theme Engine
- **Aesthetic:** Minimalist editorial finance ledger — generous whitespace, crisp tabular numerals (`fontFeatureSettings = "tnum"`), subtle surface elevation, enclosed card borders, and zero visual clutter.
- **6 Supported Theme Modes (`FolioThemeMode`):**
    1. `SYSTEM` (Follows Android Light/Dark toggle automatically)
    2. `LIGHT` (Clean crisp white `#FFFFFF` surface)
    3. `SEPIA` (Warm Sand parchment paper cream `#F7F3E9` for low eye-strain daytime viewing)
    4. `SLATE_GRAY` (Charcoal Gray matching Google Chrome's Dark Theme palette: `#202124` background, `#292A2D` surface, `#35363A` container, `#E8EAED` text, `#9AA0A6` secondary text, `#3C4043` outline)
    5. `DARK` (Midnight Navy `#0F172A` deep slate)
    6. `AMOLED_BLACK` (Pure `#000000` background for maximum OLED battery savings)
- **8 Dynamic & Custom Accent Color Palettes (`FolioAccentPalette`):**
    - Supports Android 12+ Material You (`dynamicColor`) toggle.
    - Includes 8 curated financial accent palettes:
      1. **Emerald Ledger** (`#059669` - Default)
      2. **Electric Cobalt** (`#2563EB` - #1 Global Fintech Trust Blue used by Wise & PayPal)
      3. **Royal Indigo** (`#4F46E5`)
      4. **Vivid Violet** (`#7C3AED` - Modern fintech purple used by NuBank & Revolut)
      5. **Oceanic Teal** (`#0D9488`)
      6. **Ruby Crimson** (`#E11D48`)
      7. **Saffron Amber** (`#D97706`)
      8. **Monochrome Slate** (`#475569`)
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

## 4. Navigation Architecture (4 Primary Bottom Tabs + Scoped FAB)
1. **Tab 1: `Trans.` (Ledger Home)**
    - Top Period Selector (`< Oct 2026 >`) + Search, Filter, and Bookmark (`★`) icons.
    - Decomposed into 5 domain sub-files (`LedgerHeaderBanners`, `LedgerItemCards`, `LedgerSubTabs`, `LedgerSheetsAndDialogs`, `LedgerHomeScreen`).
    - Sticky daily date headers with scroll-fading daily totals.
    - 5 Horizontal Sub-Tabs: `Daily` | `Calendar` | `Weekly` | `Monthly` | `Summary`.
2. **Tab 2: `Stats` (Analytics & Budgets)**
    - Top-bar chip dropdown granularity selector (`StatsPeriodGranularityDropdownChip`).
    - Main parent category donut & ranked list rollup.
    - 3 Horizontal Sub-Tabs: `Stats` (Interactive Donut Chart + Main Category Rank + Drill-down Subcategory Breakdown & `SixMonthSmoothTrendAreaChart`) | `Budget` (Overall + Category Progress Bars) | `Note & Tags` (Ranked aggregation).
3. **Tab 3: `Accounts` (Assets, Liabilities & Cards)**
    - Top 3-Column Net Worth Banner: `Assets` | `Liabilities` | `Net Worth`.
    - Grouped Account Cards (`Cash`, `Bank Accounts`, `Credit Cards` with 2-column `Balance Payable` vs `Current Unbilled` + Utilization bar, `Debit Cards` showing monthly spend).
4. **Tab 4: `Settings` (Configuration & Data)**
    - 3 Grouped Cards (`LEDGER & TAXONOMY`, `APPEARANCE & SECURITY`, `DATA, BACKUPS & HELP`).
    - Currency & Number Formatting Sheet (`RegionalFormatBottomSheet` with 1-line `Standard` vs `Indian` grouping, real-time currency search bar, popular currency chips, and full-width currency cards).
    - Category & Subcategory Manager (`CategoryManagerBottomSheet` with scaled typography and always-present chevrons).
    - Unified Tags & Bookmarks (`TagsAndBookmarksBottomSheet` with `#Tags` and `Bookmarks ★` tabs).
    - Appearance & Theme Sheet (`AppearanceBottomSheet` with Live Theme & Accent Preview card, 6 visual theme mode cards, and 8 accent swatch grid).
    - Backup/Restore (`DataAndBackupBottomSheet` for portable .zip and CSV export), App Lock, User Guide & Tip Jar.
- **Floating Action Button (`+`):** Scoped strictly to `TRANSACTIONS` and `STATS` tabs using `AnimatedVisibility` (hidden on `ACCOUNTS` and `SETTINGS`). Opens `TransactionEntrySheet` with calculator keypad, mode tabs, Splits, Bookmarks, and `CategoryPickerBottomSheet`.
