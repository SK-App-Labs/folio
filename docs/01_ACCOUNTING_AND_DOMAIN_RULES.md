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
