package com.folioledger.app.ui.settings

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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.folioledger.app.domain.model.CurrencySpec
import com.folioledger.app.R
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
                    "Tap the Edit (✎) icon on any account card at any time to adjust its name, credit limit, icon, or Net Worth inclusion."
                )
            ),
            GuideTopicItem(
                id = "home_5_views",
                category = GuideCategoryTab.GETTING_STARTED,
                title = "2. The 5-View Home Ledger & Account Scoping",
                summary = "Daily, Calendar, Weekly, Monthly & Summary views",
                icon = Icons.AutoMirrored.Filled.MenuBook,
                bulletPoints = listOf(
                    "Daily View: Chronological list grouped by date with daily Income and Expense subtotals.",
                    "Calendar View: Visual monthly grid showing daily cashflow. Tap any date cell to inspect that day's transactions or tap '+ Add on this date' to backdate an entry effortlessly.",
                    "Weekly & Monthly Views: Aggregated weekly buckets and 12-month financial year digests.",
                    "Summary View: Displays your period Savings Rate %, Carry-Over surplus/deficit, top categories, budget status, and 1-tap CSV export.",
                    "Account Scoping: Tap any account card in the 'Accounts' tab to open the 'Trans.' tab filtered exclusively to that account across all 5 views."
                )
            ),
            GuideTopicItem(
                id = "calculator_splits_bookmarks",
                category = GuideCategoryTab.FEATURES,
                title = "3. Fast Calculator Keypad, Splits, Refunds & Bookmarks (★)",
                summary = "Arithmetic entry (+ - × ÷), multi-category splits & 1-tap templates",
                icon = Icons.Filled.Calculate,
                bulletPoints = listOf(
                    "Built-in Calculator Keypad: Type expressions like '120+45.50' or '480÷3' directly on the custom numeric keypad. Tap '=' to evaluate or 'Save' to record immediately.",
                    "Minimize Keypad: Tap 'Hide Keypad ⌄' on the keypad toolbar (or tap the top Amount banner) whenever you want a full-screen view of the form.",
                    "Split Transactions: Enable the 'Split' chip when a single receipt spans multiple categories (e.g., Supermarket bill split between Groceries and Household). Use the 'Fill' button on any row to allocate the exact remaining balance.",
                    "Refunds: Choose the 'Refund' mode when a merchant returns money—it credits your account and offsets the original expense category.",
                    "Bookmarks (★): Tap the star icon at the top of the entry sheet to save frequent transactions (like Morning Coffee, Metro Recharge, or Rent) for 1-tap pre-filling."
                )
            ),
            GuideTopicItem(
                id = "cc_cycles_explained",
                category = GuideCategoryTab.CREDIT_CARDS,
                title = "4. Credit Card Statement Cycles (1–31) & Unbilled Spend",
                summary = "How Statement Closing Day, Due Day, and Bill Settlements work",
                icon = Icons.Filled.CreditCard,
                bulletPoints = listOf(
                    "Statement Day (1–31): The day of the month your bank generates your monthly bill (e.g., Day 18). Months with 28, 29, or 30 days automatically clamp to the last day of that month.",
                    "Payment Due Day (1–31): The deadline in the following month to pay your closed statement (e.g., Day 5).",
                    "Balance Payable vs. Unbilled Spend: Purchases made on or before your last Statement Day appear in 'Balance Payable (Statement Due)'. Purchases made after your Statement Day roll into 'Active Unbilled Cycle Spend'.",
                    "Installment Plans (Inst.): When logging a Credit Card expense, tap the 'Inst.' chip to divide a large purchase across 3, 6, 9, 12, 18, or 24 monthly installments with automatic rounding remainder allocation on Month 1.",
                    "1-Tap Bill Settlement: Tap 'Pay Bill / Settle' on any Credit Card to transfer money from your Bank Account to the card. Folio records a 'CC_SETTLEMENT' transfer—reducing your bank balance and card debt without double-counting your expenses!"
                )
            ),
            GuideTopicItem(
                id = "stats_budgets_rankings",
                category = GuideCategoryTab.FEATURES,
                title = "5. Multi-Period Stats, Drill-Down & Budgets",
                summary = "Weekly, Monthly, Annual & Custom analytics + 80% budget alerts",
                icon = Icons.Filled.PieChart,
                bulletPoints = listOf(
                    "Flexible Time Windows: Switch between Weekly, Monthly, Annually, or Custom date ranges at the top of the 'Stats' tab.",
                    "Category Drill-Down: Tap any slice of the interactive Donut Chart or any ranked category row to inspect every transaction in that category along with its 6-month trend.",
                    "Overall & Category Budgets: In the 'Budgets' sub-tab, set a master monthly budget and individual category limits. Progress bars automatically shift from Emerald to Amber at your warning threshold (default 80%) and Coral Red at 100%.",
                    "#Tag & Payee Rankings: Open 'Tags & Notes' to rank cross-category spending by #tag (e.g., #vacation, #medical, #tax) or merchant/payee name."
                )
            ),
            GuideTopicItem(
                id = "faq_backups_privacy",
                category = GuideCategoryTab.FAQS,
                title = "6. FAQs: Portable ZIP Backups, CSV Export & Privacy",
                summary = "How to move data to a new phone and why Folio is 100% offline",
                icon = Icons.Filled.Backup,
                bulletPoints = listOf(
                    "Q: Does Folio read my SMS, connect to bank servers, or upload my data? — No. Folio is 100% offline-first. Your database lives exclusively in private app storage on your phone with zero cloud telemetry or ads.",
                    "Q: How do I transfer my ledger to a new phone? — Go to Settings → 'Create Portable ZIP Backup' and save the .zip file to Google Drive or local storage. On your new phone, tap 'Restore ZIP' and select that file.",
                    "Q: Why doesn't a Transfer or Credit Card Bill Payment increase my monthly Expenses? — In double-entry personal accounting, moving money from your Bank Account to Cash or paying off a Credit Card bill is an asset-to-asset or asset-to-liability transfer; the expense was already counted when you bought the item.",
                    "Q: How do I align Folio with my salary payday? — In Settings → 'Financial Month Start Day', pick any day from 1 to 31 (e.g., Day 25) so all monthly ledgers and budgets follow your exact pay cycle."
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
        containerColor = MaterialTheme.colorScheme.surface
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
    val skuId: String,
    val title: String,
    val subtitle: String,
    val inrPriceDisplay: String,
    val usdPriceDisplay: String,
    val icon: ImageVector,
    val accentHex: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TipJarBottomSheet(
    currencySpec: CurrencySpec,
    isSupporter: Boolean = false,
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
                skuId = "folio_tip_coffee",
                title = "Coffee / Chai Boost",
                subtitle = "Fuel a late-night coding & bug-squashing session",
                inrPriceDisplay = "₹99",
                usdPriceDisplay = "$1.99",
                icon = Icons.Filled.LocalCafe,
                accentHex = "#D97706"
            ),
            TipTierOption(
                skuId = "folio_tip_supporter",
                title = "Generous Supporter",
                subtitle = "Support new chart visualizations & offline power tools",
                inrPriceDisplay = "₹299",
                usdPriceDisplay = "$4.99",
                icon = Icons.Filled.Favorite,
                accentHex = "#059669"
            ),
            TipTierOption(
                skuId = "folio_tip_patron",
                title = "Folio Patron",
                subtitle = "Champion independent, privacy-first software craftsmanship",
                inrPriceDisplay = "₹999",
                usdPriceDisplay = "$14.99",
                icon = Icons.Filled.WorkspacePremium,
                accentHex = "#4F46E5"
            )
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
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
                    Icon(imageVector = Icons.Filled.Close, contentDescription = "Close Tip Jar")
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

            if (isSupporter || selectedSkuConfirmation != null) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = semanticColors.incomeEmeraldContainer,
                    border = BorderStroke(1.dp, semanticColors.incomeEmerald),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Verified,
                            contentDescription = null,
                            tint = semanticColors.incomeEmerald,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Supporter Badge Unlocked! ★",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = semanticColors.onIncomeEmeraldContainer
                            )
                            Text(
                                text = "Thank you for supporting independent, offline-first personal finance software.",
                                style = MaterialTheme.typography.labelSmall,
                                color = semanticColors.onIncomeEmeraldContainer
                            )
                        }
                    }
                }
            }

            tipTiers.forEach { tier ->
                val priceLabel = if (isIndianRupee) tier.inrPriceDisplay else tier.usdPriceDisplay
                val tierColor = when (tier.skuId) {
                    "folio_tip_coffee" -> Color(0xFFD97706)
                    "folio_tip_supporter" -> Color(0xFF059669)
                    else -> Color(0xFF4F46E5)
                }

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
        }
    }
}