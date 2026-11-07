package com.folioledger.app.ui.settings.guides

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector

enum class GuideCategoryTab(val title: String) {
    ALL("All"),
    GETTING_STARTED("Getting Started"),
    FEATURES("Features"),
    CREDIT_CARDS("Credit Cards"),
    FAQS("FAQs")
}

@Immutable
data class GuideTopicItem(
    val id: String,
    val category: GuideCategoryTab,
    val title: String,
    val summary: String,
    val icon: ImageVector,
    val bulletPoints: List<String>
)

object FolioGuideTopics {
    val allTopics: List<GuideTopicItem> = listOf(
        // --- GETTING STARTED ---
        GuideTopicItem(
            id = "getting_started_setup",
            category = GuideCategoryTab.GETTING_STARTED,
            title = "1. Setting Up Accounts & Opening Balances",
            summary = "Create Cash, Bank, Credit Card, and linked Debit Card accounts",
            icon = Icons.Filled.AccountBalanceWallet,
            bulletPoints = listOf(
                "Open the 'Accounts' tab and tap '+ Add Account' to create your real-world Bank Accounts, Cash Wallets, Savings, Investments, and Credit Cards.",
                "Enter your current bank or wallet balance in 'Current / Opening Balance' so your Net Worth is accurate from day one.",
                "Have a Debit Card? Choose the 'Debit Cards' group and link it directly to its Parent Bank Account—every swipe on that Debit Card automatically deducts from the parent bank balance while tracking monthly card spend separately.",
                "Tap any account card to open its dedicated Monthly Passbook (with opening/closing balance and running totals). Long-press any account card (or tap 'Edit Account' from the Passbook header) to adjust its name, credit limit, icon, color, or Net Worth inclusion."
            )
        ),
        GuideTopicItem(
            id = "quick_gestures_shortcuts",
            category = GuideCategoryTab.GETTING_STARTED,
            title = "2. Quick Gestures & Speed Shortcuts",
            summary = "Handy gestures to navigate and edit faster across Folio",
            icon = Icons.Filled.TouchApp,
            bulletPoints = listOf(
                "Tap vs. Long-Press Account: Tap any account card to open its monthly Passbook. Long-press any account card to quickly edit its settings, credit limit, or archive it.",
                "Calendar Quick-Add: In the Trans tab's Calendar view, tap any day cell and tap '+ Add on this date' to backdate an entry in seconds.",
                "Calculator Toggle: On the transaction entry sheet, tap the small calculator icon next to the amount to instantly switch between the standard keyboard and the math keypad.",
                "Reconcile Tool: In any Account Passbook, tap the 'Reconcile' button to enter your actual statement closing balance—Folio will auto-calculate the difference and generate an adjustment entry with 1 tap.",
                "1-Tap Bookmarks (★): Frequently log the same morning coffee, metro pass, or grocery stop? Save it as a Bookmark and tap it from the top of the entry sheet to auto-fill amount, account, and category instantly."
            )
        ),
        GuideTopicItem(
            id = "home_5_views",
            category = GuideCategoryTab.GETTING_STARTED,
            title = "3. The 5-View Home Ledger & Monthly Digest",
            summary = "Daily, Calendar, Weekly, Monthly & Summary views",
            icon = Icons.AutoMirrored.Filled.MenuBook,
            bulletPoints = listOf(
                "Daily View: Chronological list grouped by date with daily Income and Expense subtotals and running daily net flow.",
                "Calendar View: Visual monthly calendar showing net daily cashflow dots. Tap any date cell to inspect that day's transactions or tap '+ Add on this date' to record an entry directly on that date.",
                "Weekly & Monthly Views: Aggregated weekly buckets and 12-month financial year digests with carry-over surplus/deficit.",
                "Summary View: Displays your period Savings Rate %, Income vs. Expense bar comparisons, top expense categories, budget health, and 1-tap CSV export.",
                "Month Navigation: Use the header arrows (◀ / ▶) or tap the month banner to jump to any past or future month at any time."
            )
        ),

        // --- FEATURES ---
        GuideTopicItem(
            id = "calculator_splits_bookmarks",
            category = GuideCategoryTab.FEATURES,
            title = "4. Smart Entry: Gboard, Calculator, Splits & Bookmarks",
            summary = "Arithmetic expressions, multi-category splits & 1-tap templates",
            icon = Icons.Filled.Calculate,
            bulletPoints = listOf(
                "Native Decimal Keyboard: By default, Folio opens your fast native Gboard decimal pad so entering amounts is seamless and lightning quick.",
                "Built-in Calculator Keypad: Need to do math? Tap the calculator icon (🖩) in the top amount banner to switch to Folio's full arithmetic keypad. Enter expressions like '120+45.50' or '480÷3' and tap '=' to evaluate.",
                "Split Receipts Across Categories: Enable the 'Split' chip when a single receipt covers multiple purposes (e.g., $150 supermarket bill split into $100 Groceries and $50 Household). Use 'Fill Remaining' to allocate balances automatically.",
                "Refunds & Reversals: Select the 'Refund' type when a merchant returns money—it restores your account balance and offsets the original expense category rather than inflating your income.",
                "Bookmarks (★): Tap the star icon on any entry to save it as a reusable template. Access saved bookmarks with 1 tap at the top of the entry sheet."
            )
        ),
        GuideTopicItem(
            id = "passbook_and_reconciliation",
            category = GuideCategoryTab.FEATURES,
            title = "5. Account Passbooks & 1-Tap Balance Reconciliation",
            summary = "Opening & closing monthly totals, per-row running balance & auto-balancing",
            icon = Icons.AutoMirrored.Filled.ReceiptLong,
            bulletPoints = listOf(
                "Monthly Account Passbook: Tap any account card in the Accounts tab to open its dedicated Passbook. It isolates transactions for that specific account and shows Month Opening Balance, Total Inflows, Total Outflows, and Closing Balance.",
                "Per-Row Running Balance: Every transaction card in the Passbook displays the exact account balance at that moment in time, matching your real-world physical bank passbook or e-statement.",
                "1-Tap Balance Reconciliation: Found a discrepancy between your bank app and Folio? Tap the 'Reconcile' button at the top of the Passbook, enter your bank's exact closing balance, and Folio will calculate the difference (Δ) and create an automatic balancing adjustment entry.",
                "Credit Card Statement View: For credit cards, the Passbook shows your Active Unbilled Spend, Last Statement Balance, and Due Date, with 1-tap 'Pay Bill / Settle'."
            )
        ),
        GuideTopicItem(
            id = "csv_import_guide",
            category = GuideCategoryTab.FEATURES,
            title = "6. Importing CSV Data & Spreadsheet Templates",
            summary = "Format guidelines, columns, date formats, auto-creation & duplicate skipping",
            icon = Icons.Filled.FileUpload,
            bulletPoints = listOf(
                "How to Import: Go to Settings → 'Import Transactions from CSV' and select your CSV file from local storage or cloud drive. Folio analyzes the file and shows an Import Preview summary with count of valid transactions, auto-created accounts/categories, and duplicate detection.",
                "Standard Column Headers: The CSV must include a header row with these column names (case-insensitive): Date, Type, Amount, Category, Account, Note, Transfer To, Payee, Tags.",
                "Supported Date Formats: Recommended is 'YYYY-MM-DD' (e.g., 2026-10-25). Folio also intelligently recognizes 'DD/MM/YYYY', 'DD-MM-YYYY', 'MM/DD/YYYY', 'YYYY/MM/DD', and 'dd MMM yyyy'.",
                "Transaction Types: Supported values in the 'Type' column are 'EXPENSE' (or 'DEBIT'), 'INCOME' (or 'CREDIT'), 'TRANSFER', 'CC_SETTLEMENT', and 'REFUND'.",
                "Amounts & Currency: Amounts can be plain numbers (e.g., 149.50) or formatted with commas and symbols (e.g., $1,250.00 or ₹4,500.00). Folio parses exact decimal values with zero rounding errors.",
                "Transfers & CC Settlements: For a TRANSFER or CC_SETTLEMENT, specify the source account in 'Account' and destination in 'Transfer To'. Leave 'Category' empty for transfers.",
                "Automatic Account & Category Creation: If an Account or Category listed in your CSV doesn't exist yet, Folio automatically creates it for you during import so no data is lost.",
                "Smart Duplicate Skipping: Folio prevents duplicate entries by checking if an identical transaction (same date, amount, account, and type) already exists. You can review the duplicate count in the preview before confirming.",
                "Full Backup CSVs ([ACCOUNTS], [CATEGORIES], [TRANSACTIONS]): Folio also seamlessly imports its own multi-section exported CSVs containing accounts, color palettes, categories, budgets, and transactions in a single file."
            )
        ),
        GuideTopicItem(
            id = "stats_budgets_rankings",
            category = GuideCategoryTab.FEATURES,
            title = "7. Multi-Period Analytics, Budgets & Spending Insights",
            summary = "Weekly, Monthly, Annual analytics, category drill-down & budget alerts",
            icon = Icons.Filled.PieChart,
            bulletPoints = listOf(
                "Flexible Time Windows: Switch between Weekly, Monthly, Annually, or Custom date ranges at the top of the 'Stats' tab to analyze your financial health across any horizon.",
                "Category Drill-Down: Tap any slice of the interactive Donut Chart or any category in the ranked breakdown to view its 6-month historical trend and inspect individual transactions.",
                "Monthly Overall & Category Budgets: In the 'Budgets' tab, set an overall monthly spending ceiling as well as individual category targets. Visual progress indicators shift from Emerald to Amber at 80% (configurable warning) and Coral Red at 100%.",
                "#Tag & Merchant Rankings: Open 'Tags & Notes' to rank your spending by custom #tags (e.g., #travel, #groceries, #tax) or top payees and merchants."
            )
        ),
        GuideTopicItem(
            id = "data_management_and_backups",
            category = GuideCategoryTab.FEATURES,
            title = "8. Data Management, Automated Backups & Safe Recovery",
            summary = "Hands-off session auto-backups, 30-day retention, cloud restore & safety snapshots",
            icon = Icons.Filled.Backup,
            bulletPoints = listOf(
                "Automatic Rolling Backups: Folio automatically creates a background backup whenever you finish a session where data was modified. If you just viewed charts or balances, 0 bytes are written to disk.",
                "Complete Lifetime Ledger Guarantee: Each backup snapshot contains your complete lifetime ledger, all accounts, categories, preferences, and supporter tier. The 30-Day Tiered Retention keeps up to 3 session snapshots today, 1 daily snapshot for the last 30 days, and 1 monthly snapshot for up to 3 months (~5 MB total).",
                "1-Tap Restore on Same Device: Need to undo an accidental deletion? Open Settings → 'Data Management & Backup' and tap 'Restore' on any recent snapshot, or tap 'Restore Latest Local Backup' for instant 1-tap recovery.",
                "Transferring to a New Phone: Tap 'Create Backup Archive (.zip)' and share it directly to Google Drive or chat. On your new phone, tap 'Restore from Cloud or File (.zip)' to pick the file directly from Google Drive or Downloads.",
                "Pre-Restore Safety Rollback: Before any restore wipes existing data, Folio automatically saves a hidden safety snapshot in cache. If an invalid or corrupted file was chosen, your existing ledger is preserved.",
                "Spreadsheet CSV Export & Import: Tap 'Export Full Ledger to CSV' for a multi-section spreadsheet (Accounts, Categories, Ledger) compatible with Excel and Google Sheets, or tap 'Import Data from CSV / Excel' to import bank statements with interactive preview."
            )
        ),

        // --- CREDIT CARDS ---
        GuideTopicItem(
            id = "cc_cycles_explained",
            category = GuideCategoryTab.CREDIT_CARDS,
            title = "9. Credit Card Cycles (1–31) & Bill Settlements",
            summary = "Statement day, due day, unbilled spend, and settlements explained",
            icon = Icons.Filled.CreditCard,
            bulletPoints = listOf(
                "Statement Closing Day (1–31): The calendar day each month your credit card issuer closes your billing cycle (e.g., Day 18). Months with fewer days automatically clamp to the last day of that month.",
                "Payment Due Day (1–31): The deadline date in the subsequent month to pay off the billed balance without interest (e.g., Day 5).",
                "Balance Payable vs. Unbilled Spend: Transactions made on or before your last Statement Day appear under 'Statement Due'. Transactions made after the statement closing day automatically roll into 'Active Unbilled Spend'.",
                "1-Tap Bill Settlement: Tap 'Pay Bill / Settle' on any Credit Card to record a payment from your Bank Account to the card. Folio records this as a CC_SETTLEMENT transfer—reducing your bank balance and paying down card debt without falsely counting it as an expense!",
                "Credit Card Health & Utilization: In the 'Stats' tab under Credit Card Health, track your total credit limit, total utilized debt, and overall credit utilization ratio.",
                "Installments (Inst.): When logging a large credit card purchase, tap 'Inst.' to spread the expense over 3, 6, 9, 12, 18, or 24 monthly billing cycles with exact rounding allocation."
            )
        ),

        // --- FAQS ---
        GuideTopicItem(
            id = "faq_privacy_offline",
            category = GuideCategoryTab.FAQS,
            title = "Privacy & Offline Architecture",
            summary = "100% offline, zero cloud servers, zero ads, zero SMS reading",
            icon = Icons.Filled.Security,
            bulletPoints = listOf(
                "100% Local & Offline: Folio does not upload your financial records to any remote server or cloud database. Your entire financial history is stored strictly on your device inside SQLite local storage.",
                "Zero SMS Reading: Folio never asks for SMS reading permissions. You maintain full, deliberate control over every transaction entered.",
                "Zero Advertising & Telemetry: Folio contains no ad trackers, no third-party behavioral analytics, and no subscription paywalls."
            )
        ),
        GuideTopicItem(
            id = "faq_backup_transfer_phone",
            category = GuideCategoryTab.FAQS,
            title = "Transferring Data to a New Phone",
            summary = "Portable ZIP backup creation, cloud sharing and document picker restore",
            icon = Icons.Filled.Backup,
            bulletPoints = listOf(
                "Step 1 (Old Phone): Open Settings → 'Data Management & Backup' → tap 'Create Backup Archive (.zip)'. Tap 'Share' on the completion snackbar to send the archive directly to Google Drive, Telegram, or email.",
                "Step 2 (New Phone): Install Folio on your new phone → go to Settings → 'Data Management & Backup' → tap 'Restore from Cloud or File (.zip)' → select your backup file from Google Drive or Downloads.",
                "Complete Fidelity & Rollback Protection: Restores your complete database, accounts, categories, budgets, preferences, and supporter tier. A pre-restore safety snapshot protects against corrupted or incorrect file selection."
            )
        ),
        GuideTopicItem(
            id = "faq_automated_rolling_backups",
            category = GuideCategoryTab.FAQS,
            title = "How do Automated Rolling Backups Work?",
            summary = "Hands-off session exit snapshots, dirty tracking & 30-day tiered retention",
            icon = Icons.Filled.History,
            bulletPoints = listOf(
                "Hands-Off Background Safety: Folio automatically creates a rolling snapshot when you exit or leave the app, but only if you actually modified data (0 disk writes if you were just viewing charts or balances).",
                "Complete Lifetime Ledger: Each snapshot contains your complete lifetime ledger. The '30 Days (Tiered)' policy retains up to 3 session snapshots today, 1 daily snapshot for the last 30 days, and 1 monthly snapshot for up to 3 months prior (~5 MB total).",
                "1-Tap Recovery & Sharing: You can restore directly from any recent snapshot in the bottom sheet with 1 tap, or tap the share icon to upload any snapshot to Google Drive."
            )
        ),
        GuideTopicItem(
            id = "faq_double_entry_transfers",
            category = GuideCategoryTab.FAQS,
            title = "Transfers & Credit Card Bill Payments",
            summary = "Why moving money between accounts doesn't inflate your monthly expenses",
            icon = Icons.Filled.SwapHoriz,
            bulletPoints = listOf(
                "Accounting Principle: When you withdraw cash from an ATM, transfer funds to a savings account, or pay off a credit card bill, money is merely moving from one asset/liability pocket to another. Your total Net Worth is unchanged.",
                "Credit Card Purchases vs. Payments: When you buy dinner using a credit card, Folio records the Expense on that date under your Food category. If paying the card bill was also counted as an expense, that dinner would be counted twice!",
                "Where Transfers Appear: Transfers and CC settlements are tracked in your Account Passbooks, Home Ledger Daily/Monthly totals, and Cashflow breakdown, but are excluded from spending expense charts."
            )
        ),
        GuideTopicItem(
            id = "faq_payday_financial_month",
            category = GuideCategoryTab.FAQS,
            title = "Aligning Month Start with Salary Payday",
            summary = "Configure your financial month start day from 1 to 31",
            icon = Icons.Filled.CalendarMonth,
            bulletPoints = listOf(
                "Set Custom Payday: In Settings → 'Financial Month Start Day', select the day of the month you receive your paycheck (e.g., 25th or 1st).",
                "Automatic Re-Alignment: When set to the 25th, your monthly ledger, monthly budgets, and monthly stats run from the 25th of the prior month through the 24th of the current month.",
                "Consistent Budgeting: This ensures your monthly budget reflects the exact cashflow cycle of your salary."
            )
        ),
        GuideTopicItem(
            id = "faq_csv_spreadsheet_import",
            category = GuideCategoryTab.FAQS,
            title = "Importing from Bank CSVs & Excel",
            summary = "How to prepare and import your external spreadsheet data",
            icon = Icons.Filled.FileUpload,
            bulletPoints = listOf(
                "Yes! Folio supports importing standard CSV files. In your spreadsheet application (Excel, Google Sheets), ensure your columns are named: Date, Type, Amount, Category, Account, Note, Transfer To, Payee, Tags.",
                "Export to CSV: Save or export your sheet as a .csv file.",
                "Import & Preview: In Folio, go to Settings → 'Import Transactions from CSV'. Folio previews the number of valid rows, auto-creates any missing accounts/categories, and alerts you to any duplicate rows before writing to your ledger."
            )
        ),
        GuideTopicItem(
            id = "faq_reconciliation_passbook",
            category = GuideCategoryTab.FAQS,
            title = "What is Balance Reconciliation?",
            summary = "How to quickly balance your Folio accounts with real-world bank statements",
            icon = Icons.Filled.AccountBalance,
            bulletPoints = listOf(
                "Why Reconcile: Over time, small forgotten expenses or bank fees might cause a slight difference between your actual bank app balance and your Folio account balance.",
                "How to Reconcile: Open Accounts → tap the account to view its Passbook → tap 'Reconcile'. Enter the balance shown on your real-world bank statement.",
                "Automatic Adjustment: Folio instantly calculates the discrepancy (Δ). With 1 tap, it records an 'Adjustment' entry in that account to bring your Folio ledger into 100% agreement with your bank."
            )
        )
    )
}
