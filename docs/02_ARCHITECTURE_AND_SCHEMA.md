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
- All financial aggregation logic for Net Worth Velocity, Emergency Runway, Merchant Intelligence, Reimbursable Project Netting, and Safe-to-Spend is isolated into **`StatsPredictiveCalculations.kt`** as pure Kotlin functions.
- ViewModels invoke these calculations via `withContext(Dispatchers.Default)` to ensure the Android Main Thread remains unblocked during heavy multi-month or multi-account calculations.
- Accompanied by dedicated unit test suites:
  - `NetWorthVelocityCalculationsTest.kt`
  - `PayeeIntelligenceCalculationsTest.kt`
  - `TagProjectIntelligenceCalculationsTest.kt`
  - `SafeToSpendCalculationsTest.kt`
