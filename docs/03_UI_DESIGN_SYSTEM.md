# Folio — UI/UX & Material 3 Design System (SSOT #3)

## 1. Visual Identity & Theme Modes
- **Aesthetic:** Minimalist editorial finance ledger — generous whitespace, crisp tabular numerals (`fontFeatureSettings = "tnum"`), subtle surface elevation, and zero visual clutter.
- **Supported Theme Modes:**
    1. `SYSTEM` (Follows Android Light/Dark toggle)
    2. `LIGHT` (Warm Alabaster surface `#F8F9FA`, Deep Slate ink `#111827`)
    3. `DARK` (Obsidian surface `#0F141C`)
    4. `AMOLED_BLACK` (Pure `#000000` background for battery saving)
- **Dynamic & Custom Accent Colors:**
    - Supports Android 12+ Material You (`dynamicColor`) toggle.
    - Includes 6 curated financial accent palettes: **Folio Emerald** (Default), **Royal Indigo**, **Amber Gold**, **Ocean Teal**, **Crimson Rose**, and **Monochrome Slate**.

## 2. Semantic Financial Colors (Consistent Across All Screens)
- **Income / Asset Positive:** Emerald (`#10B981` Dark / `#059669` Light)
- **Expense / Liability Outflow:** Coral Red (`#F43F5E` Dark / `#E11D48` Light)
- **Transfer / Settlement / Neutral:** Slate Blue (`#3B82F6` Dark / `#2563EB` Light)
- **Budget Safe (<80%):** Primary Accent
- **Budget Warning (80%–99%):** Amber (`#F59E0B`)
- **Budget Exceeded (100%+):** Coral Red (`#E11D48`)

## 3. Shape & Typography Rules
- **Cards & Containers:** `RoundedCornerShape(20.dp)` for hero summary cards, `RoundedCornerShape(16.dp)` for list group cards, `RoundedCornerShape(12.dp)` for keypad buttons and chips.
- **Tabular Numbers:** Every currency amount component (`MoneyText`) must apply `fontFeatureSettings = "tnum"` so digits align vertically in daily/calendar lists and never jitter during animations.

## 4. Navigation Architecture (4 Primary Bottom Tabs + FAB)
1. **Tab 1: `Trans.` (Ledger Home)**
    - Top Period Selector (`< Oct 2026 >`) + Search, Filter, and Bookmark (`★`) icons.
    - Persistent 3-Column Summary Banner: `Income` | `Expenses` | `Total (Net)`.
    - 5 Horizontal Sub-Tabs: `Daily` | `Calendar` | `Weekly` | `Monthly` | `Summary`.
2. **Tab 2: `Stats` (Analytics & Budgets)**
    - Income / Expense Toggle + Period Selector.
    - 3 Horizontal Sub-Tabs: `Stats` (Interactive Donut Chart + Category Rank + Drill-down 6M Trend) | `Budget` (Overall + Category Progress Bars) | `Note & Tags` (Ranked aggregation).
3. **Tab 3: `Accounts` (Assets, Liabilities & Cards)**
    - Top 3-Column Net Worth Banner: `Assets` | `Liabilities` | `Net Worth`.
    - Grouped Account Cards (`Cash`, `Bank Accounts`, `Credit Cards` showing `Balance Payable` vs `Outstanding Balance` + Utilization bar, `Debit Cards` showing monthly spend).
4. **Tab 4: `Settings` (Configuration & Data)**
    - Currency & Numbering Format (`1,234,567` vs `12,34,567`), Month Start Day, Subcategory Toggle, Carry-Over Toggle, Categories Manager, Recurring & Installment Manager, Backup/Restore, CSV Export, App Lock, Theme Picker.
- **Floating Action Button (`+`):** Opens the fast `TransactionEntrySheet` with built-in `+ - × ÷` calculator keypad, `Income | Expense | Transfer | Refund` mode tabs, Split builder, Bookmark picker, and `Rep. / Inst.` scheduler.