# Folio Ledger — Production Release Roadmap & TODO List

---

## ✅ Completed Feature Milestones (v1.3.0 – v1.5.0)

- `[x]` **M10: Secondary Currencies, PDF Statements & Auto-Backup Engine**
  - `[x]` Multi-currency engine with manual exchange rates and dynamic net worth valuation.
  - `[x]` Zero-dependency Skia vector PDF generator for branded passbooks and universal annual tax statements.
  - `[x]` Automated rolling session backup engine with 30-day tiered retention and local/SAF dual restore.
  - `[x]` Modular data management bottom sheets (`Spreadsheets`, `Transfer & Manual Backup`, `Auto-Backup`).

- `[x]` **M11: Credit Cards Cockpit & Predictive Financial Intelligence Suite**
  - `[x]` Credit Cards Cockpit (`StatsSubTab.CREDIT_CARDS`): Statement status pipeline, bureau threshold calibration (30% benchmark), smart swipe headroom, and celebratory debt-free milestone banner.
  - `[x]` Phase 1: Net Worth Velocity & Emergency Runway Cockpit (monthly trajectory delta, burn rate, runway in months).
  - `[x]` Phase 2: Merchant & Payee Intelligence (dual sort by Outlay vs Frequency, 6-month trends, cadence detection, drill-down sheet).
  - `[x]` Phase 3: Events, Trips & Reimbursements Hub in Tags (smart mode detection: reimbursable claim netting vs trip burn rate, 1-tap receipt drill-down).
  - `[x]` Phase 4: Safe-to-Spend & 14-Day Overdraft Shortfall Radar (unencumbered discretionary cash protecting card debt and recurring commitments, proactive shortfall warnings).

- `[x]` **M12: Interactive Demo Mode, Auto-Stash Sandbox Architecture & Codebase Modularization**
  - `[x]` Express Onboarding bottom sheet (`WelcomeExpressBottomSheet`) for currency, number grouping selection, and interactive demo seeding.
  - `[x]` Headless `DemoModeManager` & `DemoDataSeeder` with atomic auto-stash to `noBackupFilesDir/demo_stash/pre_demo_real_ledger.zip` and reverse-topological table resets.
  - `[x]` Comprehensive 26-Phase architectural decomposition of ViewModels, bottom sheets, dialogs, and domain calculators.
  - `[x]` Zero-allocation canvas chart rendering for 60/120fps UI responsiveness.
  - `[x]` 243 passing JVM unit tests covering persistence, demo mode, backup retention, PDF preset resolution, domain calculators, ViewModels, and predictive intelligence.

- `[x]` **M13: Local Bill & Credit Card Due Reminders (Zero-Network Push Engine)**
  - `[x]` Native Android `WorkManager` daily background worker (`DueRemindersWorker` & `DueRemindersScheduler`) scheduled at user's preferred time (default 09:00 AM).
  - `[x]` Isolated domain calculation engine (`DueRemindersEvaluator`) with unit tests covering overdue alerts, notice windows, and zero-balance skip rules.
  - `[x]` Dedicated Android Notification Channels (`folio_channel_credit_cards`, `folio_channel_recurring_bills`) with Android 13+ `POST_NOTIFICATIONS` runtime permission flow.
  - `[x]` Deep-linking actions: 1-tap "Settle Now" directly opens `CreditCardSettlementSheet` pre-filled with the current amount due, and "Log Payment" opens `TransactionEntrySheet`.
  - `[x]` Settings UI opt-in toggle, customizable notice lead times (credit cards & recurring bills), preferred reminder time picker, and lock-screen privacy mask.
  - `[x]` Full biometric guard compatibility: prevents unauthenticated access to settlements or ledger data when app lock is active.


---

## 🔮 Upcoming Feature Milestones

- **Phase 4: Loan EMI Auto-Balancing Split & Native Recurring Commitments**
  - `[x]` **Native `[🔁 Repeat]` Commitment Toggle**: Added repeat frequency selection in `TransactionEntrySheet.kt` wired to `RecurringScheduleCalculator` and recurring rule creation with 0 database migrations.
  - `[x]` **Active Commitments Management Hub**: Provided dedicated `RecurringManagerBottomSheet.kt` in Settings to inspect, pause, resume, edit, and delete recurring commitments.
  - `[ ]` **Auto-Balancing 1-Tap Loan Transfer Split**: When transferring from Bank to `AccountGroup.LOAN`, auto-calculate Principal paydown ($P = E - I$) from entered Interest ($I$), recording both Transfer and Expense atomically (manual split available via `[✂ Split]`).

- **Phase 5: Two-Tier Category Emoji & Custom Icon Resolver**
  - `[ ]` **Tier 1 — Reverse Emoji to Icon Pack Vector Mapping**:
    - When a user types or pastes an emoji (e.g., 🍔, ☕, 🚗, ✈️, 🎮) in the Category Editor dialog, perform reverse lookup to the matching semantic key (`fastfood`, `local_cafe`, `directions_car`, etc.).
    - Renders the corresponding vector icon in whatever active icon pack is selected (Material Filled, Phosphor Rounded, Lucide Outline).
  - `[ ]` **Tier 2 — Direct Unicode Emoji Fallback ("Infinite Icons")**:
    - If the emoji has no direct vector equivalent (e.g., 🦄, 🛹, 🎸, 🪴, 🍣), store the emoji directly as the category's `iconName`.
    - Renders the full-color Unicode emoji inside the category's tinted badge container using existing `CategoryIconBadge.kt` direct emoji rendering.
  - `[ ]` **Category Editor UI Enhancement**:
    - Add an "Or Enter Any Emoji" input field / quick-picker in `CategoryEditorDialog.kt` alongside the 64-icon grid.
    - Provide real-time badge preview in dialog header showing the matched vector or custom emoji before saving.

---

## 🚀 Phase 2: Google Play Billing Setup (Post-Launch v1.6.0)

**Goal:** Wire Google Play’s official billing engine so users can make real micro-transactions using their local currencies.

- `[ ]` **2.1 Google Play Console Setup**
  - `[ ]` Complete Merchant Account registration in Google Play Console (link bank account).
  - `[ ]` Register 3 existing SKUs in *Monetize with Play → Products → In-app products*:
    - `folio_tip_coffee`
    - `folio_tip_supporter`
    - `folio_tip_patron`
  - `[ ]` Set default pricing (Google will auto-calculate regional prices for 170+ countries).

- `[ ]` **2.2 Implement `billing-ktx` Client**
  - `[ ]` Add `implementation("com.android.billingclient:billing-ktx:7.1.1")` in `app/build.gradle.kts`.
  - `[ ]` Create `PlayBillingManager.kt`:
    - Connect to Google Play Store cache.
    - Query localized dynamic prices (e.g., €1.99, £1.79, ₹99.00).
    - Launch native Google Play purchase bottom sheet (`launchBillingFlow`).
    - Consume tips (`consumeAsync`) so users can tip again, or acknowledge the permanent badge.

- `[ ]` **2.3 License Testing Verification**
  - `[ ]` Add tester Gmail accounts to *Setup → License testing* in Play Console.
  - `[ ]` Verify test purchases using Google's *"Always approves"* test payment instrument without spending real money.

---

## 🌟 Phase 3: Public Production Launch & Review Loop

**Goal:** Maximize organic store discovery and long-term user appreciation.

- `[x]` **3.1 "Rate Folio on Google Play" Entry**
  - `[x]` Added 1-tap shortcut in `SettingsScreen.kt` under Help & Community to launch `market://details?id=${context.packageName}` with web fallback and unpublished toast guard.
  - `[x]` Allows satisfied users to leave 5-star ratings to boost store ranking.

- `[ ]` **3.2 Cosmetic Supporter Perk**
  - `[ ]` Automatically unlock an exclusive "Patron Gold" accent palette in `AppearanceBottomSheet.kt` as an extra thank-you.

- `[ ]` **3.3 Google Play Purchase Restoration**
  - `[ ]` Provide a "Restore Purchases" button in the Tip Jar sheet to query Google Play and automatically re-unlock the Supporter Badge if the user changes devices.
