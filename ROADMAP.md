# Folio Ledger — Production Release Roadmap & TODO List

---

## 🚀 Phase 2: Google Play Billing Setup (Post-Closed Testing / Pre-Launch)

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

- `[ ]` **3.1 "Rate Folio on Google Play" Entry**
  - `[ ]` Add a 1-tap shortcut to open `market://details?id=com.folioledger.app` directly on the device's Play Store.
  - `[ ]` Allow satisfied users who don't wish to spend money to leave 5-star ratings to boost store ranking.

- `[ ]` **3.2 Cosmetic Supporter Perk**
  - `[ ]` Automatically unlock an exclusive "Patron Gold" accent palette in `AppearanceBottomSheet.kt` as an extra thank-you.

- `[ ]` **3.3 Google Play Purchase Restoration**
  - `[ ]` Provide a "Restore Purchases" button in the Tip Jar sheet to query Google Play and automatically re-unlock the Supporter Badge if the user changes devices.
