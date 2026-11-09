# Folio — System Architecture & Database Schema (SSOT #2)

## 1. Package Ownership & Architecture Boundaries
Root package: `com.folioledger.app`

- `com.folioledger.app.domain.*` -> Pure Kotlin domain models, value classes (`MinorMoney`), enums, use cases, and repository interfaces. Free of Android framework dependencies.
- `com.folioledger.app.data.*` -> Room database, entities, DAOs, TypeConverters, repository implementations, DataStore preferences, PDF exporter, and backup engines.
- `com.folioledger.app.ui.*` -> Jetpack Compose UI, Material 3 design system, ViewModels, pure calculation engines (`StatsPredictiveCalculations`), and modular bottom sheets.
- `com.folioledger.app.di.*` & `com.folioledger.app.navigation.*` -> Manual dependency injection container (`AppContainer`), ViewModel factory (`FolioViewModelFactory`), and shell navigation.

## 2. Dependency Injection Strategy
To ensure 100% compatibility with AGP 9.4+ Built-in Kotlin and lightning-fast compilation, Folio uses Google's official **Manual Constructor DI (`AppContainer` + `FolioViewModelFactory`)**. No Dagger/Hilt or reflection-based DI frameworks are used.

## 3. Room SQLite Database Schema (`FolioDatabase`, Version = 2)
`FolioDatabase` is at Version 2. Uses explicit `MIGRATION_1_2` (`ALTER TABLE accounts ADD COLUMN initialUnbilledMinor INTEGER DEFAULT NULL`) and pre-migration backup engine (`DatabaseBackupManager.createPreMigrationBackup`). Destructive fallback is guarded by `if (BuildConfig.DEBUG)`.

### Table 1: `accounts`
- `id`: `Long` (PrimaryKey, autoGenerate = true)
- `name`: `String`
- `groupType`: `String` (Enum `AccountGroup`)
- `currencyCode`: `String` (ISO 4217, e.g. `"INR"`, `"USD"`)
- `openingBalanceMinor`: `Long` (Can be positive, zero, or negative)
- `initialUnbilledMinor`: `Long?` (Added in v2 for credit card unbilled allocation)
- `openingBalanceDateEpochDay`: `Long`
- `parentAccountId`: `Long?` (FK -> `accounts.id` ON DELETE SET NULL; indexed; used by `DEBIT_CARD`)
- `creditLimitMinor`: `Long?`
- `settlementDayOfMonth`: `Int?`
- `paymentDueDayOfMonth`: `Int?`
- `linkedPaymentAccountId`: `Long?`
- `includeInNetWorth`: `Boolean` (default `true`)
- `isArchived`: `Boolean` (default `false`)
- `sortOrder`: `Int`
- `colorHex`: `String`
- `iconName`: `String`

### Table 2: `categories`
- `id`: `Long` (PrimaryKey, autoGenerate = true)
- `name`: `String`
- `type`: `String` (Enum `CategoryType`: `INCOME`, `EXPENSE`)
- `parentId`: `Long?` (FK -> `categories.id` ON DELETE CASCADE; indexed; `null` = Main Category, non-null = Subcategory)
- `iconName`: `String`
- `colorHex`: `String`
- `isArchived`: `Boolean` (default `false`)
- `sortOrder`: `Int`

### Table 3: `transactions`
- `id`: `Long` (PrimaryKey, autoGenerate = true)
- `type`: `String` (Enum `TransactionType`: `INCOME`, `EXPENSE`, `TRANSFER`, `REFUND`, `CC_SETTLEMENT`)
- `timestampEpochMillis`: `Long`
- `ledgerDateEpochDay`: `Long` (Indexed for fast date-range queries)
- `amountMinor`: `Long` (Strictly > 0)
- `currencyCode`: `String`
- `accountId`: `Long` (FK -> `accounts.id` ON DELETE RESTRICT; indexed)
- `transferToAccountId`: `Long?` (FK -> `accounts.id` ON DELETE RESTRICT; indexed)
- `transferFeeMinor`: `Long` (default `0L`)
- `categoryId`: `Long?` (FK -> `categories.id` ON DELETE SET NULL; indexed)
- `isSplit`: `Boolean` (default `false`)
- `payeeOrDescription`: `String`
- `note`: `String` (Indexed for Note Stats aggregation)
- `installmentPlanId`: `Long?` (Indexed)
- `installmentIndex`: `Int?`
- `installmentTotalCount`: `Int?`
- `recurringRuleId`: `Long?` (Indexed)
- `createdAtEpochMillis`: `Long`
- `updatedAtEpochMillis`: `Long`

### Table 4: `transaction_splits`
- `id`: `Long` (PrimaryKey, autoGenerate = true)
- `transactionId`: `Long` (FK -> `transactions.id` ON DELETE CASCADE; indexed)
- `categoryId`: `Long` (FK -> `categories.id` ON DELETE RESTRICT; indexed)
- `amountMinor`: `Long` (Strictly > 0)
- `note`: `String`

### Table 5: `tags` & Table 6: `transaction_tags`
- `tags`: `id: Long` (PK), `name: String` (Unique Index), `colorHex: String`
- `transaction_tags`: Composite PK (`transactionId: Long`, `tagId: Long`), both indexed with CASCADE delete.

### Table 7: `bookmarks` (Quick-Entry Templates)
- `id`: `Long` (PK, autoGenerate = true)
- `title`: `String`
- `type`: `String` (`TransactionType`)
- `amountMinor`: `Long?`
- `accountId`: `Long?`
- `transferToAccountId`: `Long?`
- `categoryId`: `Long?`
- `payeeOrDescription`: `String`
- `note`: `String`
- `sortOrder`: `Int`

### Table 8: `budgets`
- `id`: `Long` (PK, autoGenerate = true)
- `categoryId`: `Long?` (`null` = Overall Monthly Budget; indexed)
- `limitMinor`: `Long`
- `currencyCode`: `String`
- `yearMonth`: `Int?` (`null` = recurring default; or `YYYYMM`)
- `alertThresholdPercent`: `Int` (default `80`)

### Table 9: `recurring_rules`
- `id`: `Long` (PK, autoGenerate = true)
- `title`: `String`
- `type`: `String` (`TransactionType`)
- `amountMinor`: `Long`
- `currencyCode`: `String`
- `accountId`: `Long` (FK -> `accounts.id` ON DELETE CASCADE; indexed)
- `transferToAccountId`: `Long?` (FK -> `accounts.id` ON DELETE SET NULL; indexed)
- `categoryId`: `Long?` (FK -> `categories.id` ON DELETE SET NULL; indexed)
- `frequency`: `String` (`DAILY`, `WEEKLY`, `BIWEEKLY`, `MONTHLY`, `YEARLY`)
- `startDateEpochDay`: `Long`
- `nextOccurrenceEpochDay`: `Long` (Indexed with `isActive`)
- `endDateEpochDay`: `Long?`
- `payeeOrDescription`: `String`
- `note`: `String`
- `isActive`: `Boolean` (default `true`)

### Table 10: `installment_plans`
- `id`: `Long` (PK, autoGenerate = true)
- `description`: `String`
- `creditCardAccountId`: `Long` (FK -> `accounts.id` ON DELETE CASCADE; indexed)
- `categoryId`: `Long` (FK -> `categories.id` ON DELETE RESTRICT)
- `totalAmountMinor`: `Long`
- `monthsCount`: `Int`
- `startDateEpochDay`: `Long`
- `createdAtEpochMillis`: `Long`

## 4. Pure Analytics & Predictive Intelligence Architecture
To guarantee testability, maintain responsiveness, and prevent ViewModel lifecycle leaks:
- All financial aggregation logic for Net Worth Velocity, Emergency Runway, Merchant Intelligence, Reimbursable Project Netting, and Safe-to-Spend is isolated into **`StatsPredictiveCalculations.kt`** and specialized domain utilities under **`domain/util/`** as pure Kotlin functions.
- ViewModels invoke these calculations via `withContext(Dispatchers.Default)` to ensure the Android Main Thread remains unblocked during heavy multi-month or multi-account calculations.
- Decomposed domain calculation utilities in `domain/util/`:
  1. `AccountPassbookCalculator.kt` (Running balances, day grouping, debit card rollups)
  2. `BackupTimeFormatter.kt` (Relative timestamp labels for backup management)
  3. `CardMilestoneCalculator.kt` (Credit card anniversary milestones & spend tracking)
  4. `CategoryDrillDownCalculator.kt` (Category tree filtering & subcategory breakdowns)
  5. `CreditCardCockpitCalculator.kt` (Single-pass credit card cockpit spend processing)
  6. `CreditCardCycleCalculator.kt` (Billed statement closing vs unbilled calculation)
  7. `LedgerBucketingCalculator.kt` (Daily/Weekly/Monthly/Calendar ledger grouping)
  8. `MerchantAndTagAnalyticsCalculator.kt` (Payee frequency/outlay and tag date span calculation)
  9. `MoneyFormatterAndCalculator.kt` (Minor money arithmetic & currency formatting)
  10. `NetWorthPredictiveCalculator.kt` (Pre-grouped O(1) net worth trajectory trend calculation)
  11. `RecurringBurdenCalculator.kt` (Monthly fixed commitment burden calculation)
  12. `RecurringScheduleCalculator.kt` (Recurrence frequency next occurrence projection)
  13. `SafeToSpendCalculator.kt` (Discretionary cash & 14-day overdraft radar)
  14. `SavingsGoalCalculator.kt` (Wealth vault & surplus progress calculations)
  15. `StatsPeriodCalculator.kt` (Financial period stepping with clamped month boundaries)
  16. `TagDateSpanFormatter.kt` (Trip & event active date span formatting)
  17. `TransactionEntryRulesEngine.kt` (Split transaction validation & installment schedules)
- Accompanied by 243 passing unit tests across `app/src/test/java/com/folioledger/app/`.

## 5. Interactive Demo Mode & Auto-Stash Sandbox Architecture
- **Express Onboarding (`WelcomeExpressBottomSheet`)**: Allows first-time users to select primary currency, number grouping system (`International` vs `Indian`), and immediately preview Folio with rich sample data or start fresh.
- **Headless `DemoModeManager`**:
  - Controls demo state (`isDemoModeActive`).
  - Pauses `AutoBackupManager` while demo mode is active to prevent demo data contamination in automated user backups.
  - Paired with an atomic Auto-Stash & Restore mechanism:
    - Atomically stashes active real user data to `context.noBackupFilesDir/demo_stash/pre_demo_real_ledger.zip` via an atomic `.tmp` swap before seeding sample data.
    - Table wiping occurs in strict reverse-topological order (`transaction_tags`, `transaction_splits`, `transactions`, `bookmarks`, `budgets`, `recurring_rules`, `installment_plans`, `accounts`, `categories`) to guarantee zero foreign-key constraint violations and zero Room schema migrations.
    - Safely restores stashed real data or resets to a clean empty ledger upon exiting demo mode.
- **`DemoDataSeeder`**:
  - Dynamically generates ~45 transactions across 90 days relative to `LocalDate.now()` with currency scaling (`scaleMinor` for USD, INR, JPY, EUR, etc.).
  - Populates 8 account groups (Cash, Salary Bank, Bill Checking, Savings Vault, 2 Credit Cards, Index Fund, and Home Loan).
  - Seeds Credit Card 1 (billed statement due in 12 days) and Credit Card 2 (statement closing in 25 days) to showcase the Smart Swipe Advisor float optimizer, Grace Period countdown, and Card Milestones.
  - Seeds 3 recurring commitments within the next 14 days to activate the 14-Day Overdraft Warning and Safe-to-Spend Today calculations.
  - Seeds 4 budgets (including an ~85% threshold warning on Food & Dining), 3 bookmarks, and tags (`#vacation`, `#reimbursable`).

## 6. Zero-Allocation Canvas Chart Performance Architecture
- **Zero-Allocation Canvas Chart Drawing (`FolioCharts.kt` & `NetWorthAnalyticsComponents.kt`)**:
  - In `SixMonthSmoothTrendAreaChart`: `Brush.verticalGradient` allocations are hoisted outside `Canvas` with `remember`, and per-frame `ArrayList<Offset>` allocations are replaced with reusable `FloatArray` buffers (`pointsX` and `pointsY`), eliminating garbage collection churn during 60/120fps entry animations and touch scrubbing.
  - In `NetWorthGrowthCanvasChart`: pre-computed usable canvas dimensions, cached `Brush.verticalGradient`, and eliminated per-frame local function closure allocations inside `Canvas`.

