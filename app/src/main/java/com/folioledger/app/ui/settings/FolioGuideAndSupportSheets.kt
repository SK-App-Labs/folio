package com.folioledger.app.ui.settings

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.folioledger.app.domain.model.CurrencySpec
import com.folioledger.app.domain.model.SupporterTier
import com.folioledger.app.R
import com.folioledger.app.ui.components.parseHexColorSafely
import com.folioledger.app.ui.theme.LocalFolioSemanticColors
import com.folioledger.app.ui.theme.tabularMoneyStyle

/**
 * Renders Folio's signature "Architectural Ledger 'F'" app icon
 * using the official res/drawable app icon assets (ic_launcher_background & ic_launcher_foreground).
 */
@Composable
fun FolioBrandLogoBadge(
    modifier: Modifier = Modifier,
    size: Dp = 60.dp
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.26f))
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_background),
            contentDescription = null,
            modifier = Modifier.matchParentSize()
        )
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "Folio App Icon",
            modifier = Modifier.matchParentSize()
        )
    }
}

enum class GuideCategoryTab(val title: String) {
    ALL("All"),
    GETTING_STARTED("Getting Started"),
    FEATURES("Features"),
    CREDIT_CARDS("Credit Cards"),
    FAQS("FAQs")
}

@Immutable
private data class GuideTopicItem(
    val id: String,
    val category: GuideCategoryTab,
    val title: String,
    val summary: String,
    val icon: ImageVector,
    val bulletPoints: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserGuideBottomSheet(
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val semanticColors = LocalFolioSemanticColors.current

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedTab by rememberSaveable { mutableStateOf(GuideCategoryTab.ALL) }
    var expandedTopicIds by rememberSaveable {
        mutableStateOf(setOf("getting_started_setup", "cc_cycles_explained"))
    }

    val allTopics = remember {
        listOf(
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

            // --- CREDIT CARDS ---
            GuideTopicItem(
                id = "cc_cycles_explained",
                category = GuideCategoryTab.CREDIT_CARDS,
                title = "8. Credit Card Cycles (1–31) & Bill Settlements",
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
                summary = "Portable ZIP backup creation and 1-tap restore",
                icon = Icons.Filled.Backup,
                bulletPoints = listOf(
                    "Step 1 (Old Phone): Open Settings → scroll to Data, Backups & Help → tap 'Create Portable ZIP Backup'. Save the generated .zip file to Google Drive, a USB drive, or send it to yourself via email/messaging.",
                    "Step 2 (New Phone): Install Folio on your new phone → go to Settings → tap 'Restore ZIP Backup' → select your saved backup file.",
                    "Complete Fidelity: The portable ZIP preserves your full database, all accounts, custom categories, icons, budgets, supporter status, and preferences intact."
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

    val filteredTopics = remember(allTopics, selectedTab, searchQuery) {
        allTopics.filter { topic ->
            val matchesTab = selectedTab == GuideCategoryTab.ALL || topic.category == selectedTab
            val q = searchQuery.trim()
            val matchesQuery = q.isEmpty() ||
                    topic.title.contains(q, ignoreCase = true) ||
                    topic.summary.contains(q, ignoreCase = true) ||
                    topic.bulletPoints.any { it.contains(q, ignoreCase = true) }
            matchesTab && matchesQuery
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 28.dp)
        ) {
            // Sheet Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FolioBrandLogoBadge(size = 40.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Folio User Guide & FAQs",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Step-by-step instructions & accounting tips",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Filled.Close, contentDescription = "Close Guide")
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search guide (e.g. credit card, split, backup)...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Filled.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Filled.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            )

            // Category Filter Tabs
            PrimaryScrollableTabRow(
                selectedTabIndex = selectedTab.ordinal,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                divider = {}
            ) {
                GuideCategoryTab.entries.forEach { tab ->
                    val selected = selectedTab == tab
                    Tab(
                        selected = selected,
                        onClick = { selectedTab = tab },
                        text = {
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                }
            }

            HorizontalDivider(color = semanticColors.cardStrokeSubtle)

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (filteredTopics.isEmpty()) {
                    item {
                        Text(
                            text = "No guide topics matched \"$searchQuery\". Try another keyword.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 28.dp)
                        )
                    }
                } else {
                    items(filteredTopics, key = { it.id }) { topic ->
                        val isExpanded = topic.id in expandedTopicIds || searchQuery.isNotBlank()

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            ),
                            border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            expandedTopicIds = if (topic.id in expandedTopicIds) {
                                                expandedTopicIds - topic.id
                                            } else {
                                                expandedTopicIds + topic.id
                                            }
                                        }
                                        .padding(14.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                                )
                                        ) {
                                            Icon(
                                                imageVector = topic.icon,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = topic.title,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = topic.summary,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Icon(
                                        imageVector = if (isExpanded) {
                                            Icons.Filled.ExpandLess
                                        } else {
                                            Icons.Filled.ExpandMore
                                        },
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }

                                AnimatedVisibility(
                                    visible = isExpanded,
                                    enter = expandVertically(),
                                    exit = shrinkVertically()
                                ) {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 16.dp, end = 16.dp, bottom = 14.dp)
                                    ) {
                                        HorizontalDivider(color = semanticColors.cardStrokeSubtle)
                                        topic.bulletPoints.forEach { point ->
                                            Row(
                                                verticalAlignment = Alignment.Top,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = "•",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.padding(end = 8.dp)
                                                )
                                                Text(
                                                    text = point,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Immutable
private data class TipTierOption(
    val tier: SupporterTier,
    val subtitle: String,
    val inrPriceDisplay: String,
    val usdPriceDisplay: String,
    val icon: ImageVector
) {
    val skuId: String get() = tier.skuId
    val title: String get() = tier.title
    val accentHex: String get() = tier.accentHex
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TipJarBottomSheet(
    currencySpec: CurrencySpec,
    isSupporter: Boolean = false,
    supporterTier: String? = null,
    onTipSelected: (skuId: String, tierTitle: String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val semanticColors = LocalFolioSemanticColors.current
    val isIndianRupee = currencySpec.code.equals("INR", ignoreCase = true)

    var selectedSkuConfirmation by rememberSaveable { mutableStateOf<String?>(null) }

    val tipTiers = remember {
        listOf(
            TipTierOption(
                tier = SupporterTier.COFFEE,
                subtitle = "Fuel a late-night coding & bug-squashing session",
                inrPriceDisplay = "₹99",
                usdPriceDisplay = "$1.99",
                icon = Icons.Filled.LocalCafe
            ),
            TipTierOption(
                tier = SupporterTier.GENEROUS_SUPPORTER,
                subtitle = "Support new chart visualizations & offline power tools",
                inrPriceDisplay = "₹299",
                usdPriceDisplay = "$4.99",
                icon = Icons.Filled.Verified
            ),
            TipTierOption(
                tier = SupporterTier.PATRON,
                subtitle = "Champion independent, privacy-first software craftsmanship",
                inrPriceDisplay = "₹999",
                usdPriceDisplay = "$14.99",
                icon = Icons.Filled.WorkspacePremium
            )
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, bottom = 32.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(semanticColors.expenseCoralContainer)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = null,
                            tint = semanticColors.expenseCoral,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Support Folio Development",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "100% Free • Zero Ads • Zero Subscriptions",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Filled.Close, contentDescription = "Close Support Folio")
                }
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Every feature in Folio—unlimited accounts, credit card cycle tracking, split transactions, custom budgets, and portable ZIP backups—is unlocked for everyone with zero paywalls and zero tracking. If Folio brings clarity to your finances, you can leave a voluntary tip below!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(14.dp)
                )
            }

            val activeTier = SupporterTier.fromSkuOrTitle(supporterTier ?: selectedSkuConfirmation)
                ?: if (isSupporter) SupporterTier.GENEROUS_SUPPORTER else null

            if (activeTier != null) {
                val tierAccentColor = parseHexColorSafely(activeTier.accentHex)
                val tierIcon = when (activeTier) {
                    SupporterTier.COFFEE -> Icons.Filled.LocalCafe
                    SupporterTier.GENEROUS_SUPPORTER -> Icons.Filled.Verified
                    SupporterTier.PATRON -> Icons.Filled.WorkspacePremium
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = tierAccentColor.copy(alpha = 0.14f),
                    border = BorderStroke(1.5.dp, tierAccentColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(tierAccentColor.copy(alpha = 0.22f))
                        ) {
                            Icon(
                                imageVector = tierIcon,
                                contentDescription = null,
                                tint = tierAccentColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${activeTier.badgeLabel} Unlocked!",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Thank you for supporting independent, offline-first personal finance software.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            tipTiers.forEach { tier ->
                val priceLabel = if (isIndianRupee) tier.inrPriceDisplay else tier.usdPriceDisplay
                val tierColor = parseHexColorSafely(tier.accentHex)

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    border = BorderStroke(
                        width = if (selectedSkuConfirmation == tier.skuId) 2.dp else 1.dp,
                        color = if (selectedSkuConfirmation == tier.skuId) {
                            tierColor
                        } else {
                            semanticColors.cardStrokeSubtle
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            selectedSkuConfirmation = tier.skuId
                            onTipSelected(tier.skuId, tier.title)
                        }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(tierColor.copy(alpha = 0.15f))
                            ) {
                                Icon(
                                    imageVector = tier.icon,
                                    contentDescription = null,
                                    tint = tierColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = tier.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = tier.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        FilledTonalButton(
                            onClick = {
                                selectedSkuConfirmation = tier.skuId
                                onTipSelected(tier.skuId, tier.title)
                            },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = priceLabel,
                                style = tabularMoneyStyle(MaterialTheme.typography.titleSmall),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Free Way to Support: Share Folio
            val context = LocalContext.current
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Share Folio with Friends",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Help friends take control of their money with 100% offline privacy.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    OutlinedButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "I recommend Folio for managing expenses and budgets! It is 100% offline, privacy-first, with zero cloud and zero ads. Check it out on Google Play: https://play.google.com/store/apps/details?id=com.folioledger.app"
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Folio"))
                        },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}