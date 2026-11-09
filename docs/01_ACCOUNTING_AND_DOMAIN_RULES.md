# Folio — Accounting & Domain Rules Specification (SSOT #1)

## 1. Universal Monetary Representation (`MinorMoney`)
- **Zero Floating-Point Rule:** `Float` and `Double` are strictly forbidden for monetary storage or arithmetic.
- **Storage Type:** All monetary amounts are stored in SQLite and processed in Kotlin as 64-bit signed integers (`Long`) representing the currency's minor unit (`amountMinor`).
- **Currency Scale Awareness:**
    - 2-decimal currencies (`INR` paise, `USD` cents, `EUR` cents, `GBP` pence): $1 \text{ Major} = 100 \text{ Minor}$ (e.g., `₹1,250.50` = `125050L`).
    - 0-decimal currencies (`JPY`, `KRW`): $1 \text{ Major} = 1 \text{ Minor}$.
    - 3-decimal currencies (`KWD`, `BHD`, `OMR`): $1 \text{ Major} = 1000 \text{ Minor}$.
- **Percentage & Division Rounding:** Only `java.math.BigDecimal` with `RoundingMode.HALF_UP` may be used for percentage calculations before converting immediately back to `Long` minor units.
- **Locale-Aware Formatting:** Numbers format according to the user's selected numbering system:
    - `INDIAN` (`en-IN`): `12,34,567.89` (Lakhs / Crores grouping).
    - `INTERNATIONAL` (`en-US` / ISO): `1,234,567.89` (Thousands / Millions grouping).

## 2. Account Groups & Net Worth Classification
Every account belongs to an `AccountGroup`:
1. `CASH` (Asset)
2. `BANK_ACCOUNT` (Asset)
3. `DEBIT_CARD` (Linked Proxy Asset — see Rule 5)
4. `CREDIT_CARD` (Liability — see Rule 4)
5. `SAVINGS` (Asset)
6. `PREPAID_WALLET` (Asset)
7. `INVESTMENT` (Asset)
8. `OVERDRAFT` (Liability)
9. `LOAN` (Liability)
10. `OTHER` (User-selectable Asset or Liability)

### Top Net Worth Header Formula
For all non-archived accounts where `includeInNetWorth == true` (excluding `DEBIT_CARD` proxy rows to avoid double-counting the parent bank account):
- **Total Assets** = $\sum \max(0, \text{Balance}_a)$ for Asset accounts + $\sum \max(0, \text{Balance}_l)$ for Liability accounts with positive credit/overpayment.
- **Total Liabilities** = $\sum \vert{}\min(0, \text{Balance}_l)\vert{}$ for Liability accounts + $\sum \vert{}\min(0, \text{Balance}_a)\vert{}$ for overdrawn Asset accounts.
- **Net Worth (Total)** = $\text{Total Assets} - \text{Total Liabilities}$.

## 3. Transaction Types & Ledger Impact
Every transaction has a `TransactionType`:
1. **`INCOME`**: Increases `accountId` balance by `amountMinor`. Increases Period Income by `amountMinor`.
2. **`EXPENSE`**: Decreases `accountId` balance by `amountMinor`. Increases Period Expense by `amountMinor`.
3. **`TRANSFER`**: Decreases `accountId` (Source) by `amountMinor + transferFeeMinor`. Increases `transferToAccountId` (Destination) by `amountMinor`.
    - **Transfer Neutrality:** `amountMinor` is excluded from Period Income and Period Expense. If `transferFeeMinor > 0`, only the fee is counted in Period Expense.
4. **`CC_SETTLEMENT` (Credit Card Bill Payment)**: A specialized transfer where `accountId` is a Bank/Cash Asset (`-amountMinor`) and `transferToAccountId` is a `CREDIT_CARD` Liability (`+amountMinor`, reducing debt owed). Excluded from Period Expense to prevent double-counting.
5. **`REFUND`**: Increases `accountId` balance by `amountMinor`. Linked to an Expense `categoryId`.
    - **Refund Netting:** Subtracts `amountMinor` from that category's net spending and from Period Expense (never inflates Period Income).

## 4. Credit Card Statement Cycle & Installment Rules
Each `CREDIT_CARD` account defines:
- `settlementDayOfMonth` ($1..31$, statement closing day)
- `paymentDueDayOfMonth` ($1..31$, bill due day)
- `creditLimitMinor` (optional credit limit)
- `initialUnbilledMinor` (initial unbilled spend upon card creation/edit)

### Credit Card Metrics (Symmetrical Billed vs Unbilled)
1. **Balance Payable (Closed Statement Due):** Net unpaid balance of transactions whose `ledgerDateEpochDay` falls on or before the most recent `settlementDayOfMonth`, minus payments (`CC_SETTLEMENT`) made since statement close.
2. **Current Unbilled (Active Cycle):** Net purchases incurred after the last statement cutoff date, starting from `initialUnbilledMinor`.
3. **Outstanding Balance (Total Debt):** $\text{Balance Payable} + \text{Current Unbilled}$.
4. **Credit Utilization %:** $\text{Outstanding Debt} / \text{Credit Limit} \times 100$.

### Credit Card Installment Plans (`Inst.`)
When a purchase of `totalAmountMinor` is split across $N$ monthly installments (`monthsCount = N`) starting on date $D$:
- Base monthly installment: $B = \lfloor \text{totalAmountMinor} / N \rfloor$
- Remainder minor units: $R = \text{totalAmountMinor} \bmod N$
- Installment #1 (dated $D$) has amount $B + R$.
- Installments #2 through #$N$ (dated $D + 1\text{ month}, \dots, D + (N-1)\text{ months}$) each have amount $B$.
- **Invariant:** $\sum_{k=1}^{N} \text{Installment}_k = \text{totalAmountMinor}$ (zero rounding loss).

## 5. Linked Debit Card Mechanics
A `DEBIT_CARD` account requires a non-null `parentAccountId` pointing to a `BANK_ACCOUNT`:
- Recording an `EXPENSE` or `REFUND` on the Debit Card directly adjusts the parent `BANK_ACCOUNT` balance.
- On the Accounts screen, the Debit Card displays its own **Current Period Spend Total** (which resets each financial month), while the actual cash balance lives on the parent Bank Account.

## 6. Split Transactions
When `isSplit == true` on an `EXPENSE` or `INCOME` transaction:
- `transactions.categoryId` is `null`.
- 2 or more rows exist in `transaction_splits` with `transactionId == transactions.id`.
- **Atomic Invariant:** $\sum \text{split.amountMinor} == \text{transaction.amountMinor}$. Any mismatch throws `InvalidSplitSumException` and rolls back the SQLite transaction.

## 7. Financial Period & Carry-Over Rules
- **Custom Monthly Start Day (`monthStartDay` = $1..28$, default `1`):**
    - If `monthStartDay == 25`, the "October 2026" financial month spans `2026-09-25` through `2026-10-24` inclusive.
- **Carry-Over Toggle (`carryOverEnabled`):**
    - When enabled, the previous financial period's net cashflow ($\text{Income} - \text{Expense}$) is displayed as a distinct **"Carry-Over Balance"** line item in the Summary view without polluting current-month Category Income or Category Expense charts.

## 8. Secondary Currencies & Deterministic Conversion Rules
- **Manual Exchange Rate Storage:** Secondary currencies are stored as key-value pairs in Android DataStore (`secondaryCurrencies: Map<String, Double>`), mapping ISO 4217 currency code to units of primary currency (e.g. `"USD": 85.0`).
- **Precision Preservation:** Converted minor money calculations account for differing decimal scales between currencies:
  $$\text{ConvertedMinor} = \left\lfloor \frac{\text{amountMinor}}{10^{\text{sourceDecimals}}} \times \text{rate} \times 10^{\text{targetDecimals}} + 0.5 \right\rfloor$$
- **Unified Valuation:** All multi-account aggregations (Net Worth header, Stats Donut, Budget Progress, Payee rankings, and Tag aggregations) evaluate foreign accounts through their manual exchange rate, eliminating distorted 1:1 cross-currency additions.

## 9. Credit Card Cockpit & Bureau Utilization Dynamics
- **Credit Bureau Risk Tiers (30% Benchmark):**
  - **SAFE** ($< 30\%$): Low credit risk, optimal for credit score preservation.
  - **CAUTION** ($30\% - 49.9\%$): Moderate utilization, approaching bureau penalty threshold.
  - **WARNING** ($50\% - 74.9\%$): Elevated risk of credit rating downgrade.
  - **CRITICAL** ($\ge 75\%$): Maxed-out utilization penalty zone.
- **Smart Swipe Headroom:**
  $$\text{Headroom} = \max\left(0L, \left\lfloor \text{CreditLimit} \times 0.30 \right\rfloor - \text{TotalCurrentDebt}\right)$$
  Quantifies the exact unencumbered spending capacity remaining on credit cards before exceeding the 30% credit bureau threshold.
- **Statement Status Pipeline:**
  - `OVERDUE`: Current date $>$ `paymentDueDay` and statement balance $> 0$.
  - `DUE_SOON`: Current date within 3 days of `paymentDueDay` and statement balance $> 0$.
  - `ISSUED`: Statement closed, bill due within the month.
  - `CURRENT`: Unbilled activity only; previous statement settled.
  - `SETTLED`: All statement dues and unbilled balances are zero. Triggers celebratory emerald milestone card.

## 10. Predictive Financial Intelligence Suite
Pure-calculation functions reside in `StatsPredictiveCalculations.kt` running asynchronously on `Dispatchers.Default`:

### 10.1 Net Worth Velocity & Emergency Runway
- **Net Worth Velocity:** Compares current net worth with trailing periods:
  $$\Delta\text{NetWorth} = \text{NetWorth}_{\text{Current}} - \text{NetWorth}_{\text{PreviousMonth}}$$
- **Monthly Burn Rate:** Average monthly expense across the last 3 closed financial months:
  $$\text{MonthlyBurnRate} = \frac{\sum_{m=1}^{3} \text{Expense}_m}{3}$$
- **Emergency Runway (Months):**
  $$\text{RunwayMonths} = \begin{cases} \infty & \text{if } \text{MonthlyBurnRate} \le 0 \\ \frac{\text{LiquidCashAssets}}{\text{MonthlyBurnRate}} & \text{otherwise} \end{cases}$$
  Liquid cash includes `CASH`, `BANK_ACCOUNT`, `SAVINGS`, and `PREPAID_WALLET`. Excludes investments and fixed assets.

### 10.2 Merchant & Payee Intelligence
- **Dual Ranking Modes:**
  - *Outlay Mode*: Sorts payees descending by total aggregate spend ($\sum \text{amountMinor}$).
  - *Frequency Mode*: Sorts payees descending by transaction count ($\text{count}$), surfacing low-ticket high-frequency habits.
- **Cadence Detection:** Evaluates consecutive transaction intervals: if the standard deviation of inter-transaction days is $< 4$ days and count $\ge 3$, classifies payee as recurring (e.g., `"Recurring: ~Every 30 days"`).

### 10.3 Reimbursable Claims vs. Event Project Netting
- **Mode Classification:** If transactions under a tag contain `REFUND` or `INCOME` types, or tag matches `[reimbursable, claim, split, office, work, bill]`, tag activates **Reimbursable Mode**. Otherwise, defaults to **Event / Trip Mode**.
- **Reimbursable Claim Netting:**
  $$\text{TotalSpent} = \sum \text{ExpenseTransactions}$$
  $$\text{TotalReimbursed} = \sum \text{InflowTransactions}$$
  $$\text{NetPendingOwed} = \max(0L, \text{TotalSpent} - \text{TotalReimbursed})$$
  Displays `✓ Fully Settled` when $\text{NetPendingOwed} == 0$.
- **Trip / Event Burn Rate:**
  $$\text{DateSpanDays} = \max(1, \text{LatestEpochDay} - \text{EarliestEpochDay} + 1)$$
  $$\text{DailyAverageSpend} = \frac{\text{TotalSpent}}{\text{DateSpanDays}}$$

### 10.4 Safe-to-Spend & 14-Day Overdraft Shortfall Radar
- **Safe-to-Spend (Unencumbered Liquid Cash):**
  $$\text{SafeToSpend} = \text{LiquidAssets} - \text{CreditCardDebtDue} - \text{14DayRecurringCommitments}$$
- **14-Day Account Shortfall Warning:**
  For each individual liquid bank account $A$, projects future balance over rolling 14 days:
  $$\text{ProjectedBalance}(A) = \text{CurrentBalance}(A) - \sum \text{CommitmentsIn14Days}(A)$$
  If $\text{ProjectedBalance}(A) < 0$, generates `AccountShortfallAlert` notifying the user of exact shortfall and due date before bank overdraft fees occur.

## 11. Interactive Demo Mode & Auto-Stash Sandbox Rules
- **Express Onboarding Selection**: Express onboarding enables first-time users to set their primary currency and grouping format (`International` vs `Indian`) before initializing their ledger or launching interactive demo mode.
- **Dynamic Minor Unit Scaling (`scaleMinor`)**: Seeded transactions automatically scale minor units according to currency fraction digits ($100$ for 2-decimal USD/INR/EUR, $1$ for 0-decimal JPY, $1000$ for 3-decimal KWD).
- **Atomic Real Data Stashing**: Before seeding demo data, existing user data is atomically backed up to `noBackupFilesDir/demo_stash/pre_demo_real_ledger.zip` via a `.tmp` file swap.
- **Strict Reverse-Topological Clearing**: Table resets occur in strict reverse dependency order (`transaction_tags` $\rightarrow$ `transaction_splits` $\rightarrow$ `transactions` $\rightarrow$ `bookmarks` $\rightarrow$ `budgets` $\rightarrow$ `recurring_rules` $\rightarrow$ `installment_plans` $\rightarrow$ `accounts` $\rightarrow$ `categories`) ensuring zero foreign-key constraint violations and zero Room schema migrations.
- **Backup Engine Safeguard**: `AutoBackupManager` is paused while demo mode is active to prevent demo data from contaminating rolling session backups.

