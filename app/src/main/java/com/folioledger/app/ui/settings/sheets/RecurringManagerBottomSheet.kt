// File: app/src/main/java/com/folioledger/app/ui/settings/sheets/RecurringManagerBottomSheet.kt
package com.folioledger.app.ui.settings.sheets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.folioledger.app.domain.model.CurrencySpec
import com.folioledger.app.domain.model.InstallmentPlanWithProgress
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.RecurringCommitmentItem
import com.folioledger.app.domain.model.RepeatFrequency
import com.folioledger.app.domain.model.TransactionType
import com.folioledger.app.domain.util.MoneyFormatterAndCalculator
import com.folioledger.app.domain.util.RecurringScheduleCalculator
import com.folioledger.app.ui.components.CategoryIconBadge
import com.folioledger.app.ui.components.MoneyColorMode
import com.folioledger.app.ui.components.MoneyText
import com.folioledger.app.ui.components.fromTransactionType
import com.folioledger.app.ui.settings.SettingsUiState
import com.folioledger.app.ui.theme.LocalFolioSemanticColors
import com.folioledger.app.ui.theme.tabularMoneyStyle
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.US)

private fun formatMinorPlainForInput(money: MinorMoney, fractionDigits: Int): String {
    val divisor = BigDecimal.TEN.pow(fractionDigits)
    return BigDecimal.valueOf(money.amountMinor)
        .divide(divisor, fractionDigits, RoundingMode.HALF_UP)
        .toPlainString()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringManagerBottomSheet(
    uiState: SettingsUiState,
    onToggleRecurringActive: (Long, Boolean) -> Unit,
    onDeleteRecurringRule: (Long) -> Unit,
    onUpdateRecurringRule: (RecurringCommitmentItem) -> Unit,
    onDeleteInstallmentPlan: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val semanticColors = LocalFolioSemanticColors.current
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    var ruleToDelete by remember { mutableStateOf<RecurringCommitmentItem?>(null) }
    var ruleToEdit by remember { mutableStateOf<RecurringCommitmentItem?>(null) }
    var planToDelete by remember { mutableStateOf<InstallmentPlanWithProgress?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Autorenew,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(9.dp)
                                .size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Recurring & Subscriptions",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Manage repeat commitments & active EMIs",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Hero Summary Card: Monthly Fixed Burden
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "TOTAL MONTHLY FIXED COMMITMENT",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        MoneyText(
                            money = uiState.totalMonthlyFixedBurden,
                            currencySpec = uiState.currencySpec,
                            grouping = uiState.userPreferences.numberGrouping,
                            style = tabularMoneyStyle(MaterialTheme.typography.headlineMedium),
                            colorMode = MoneyColorMode.NEUTRAL
                        )
                        Text(
                            text = "/ month",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    HorizontalDivider(
                        color = semanticColors.cardStrokeSubtle,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Subscriptions Breakdown
                        val activeSubscriptionsCount = uiState.recurringRules.count { it.isActive && it.type == TransactionType.EXPENSE }
                        Column {
                            Text(
                                text = "Subscriptions ($activeSubscriptionsCount active)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            MoneyText(
                                money = uiState.activeMonthlyRecurringBurden,
                                currencySpec = uiState.currencySpec,
                                grouping = uiState.userPreferences.numberGrouping,
                                style = tabularMoneyStyle(MaterialTheme.typography.bodyMedium),
                                colorMode = MoneyColorMode.NEUTRAL
                            )
                        }

                        // Auto-Savings / Transfers Breakdown (if any active transfers exist)
                        val activeTransfersCount = uiState.recurringRules.count { it.isActive && it.type == TransactionType.TRANSFER }
                        if (activeTransfersCount > 0) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Auto-Save ($activeTransfersCount active)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                MoneyText(
                                    money = uiState.activeMonthlyTransfersBurden,
                                    currencySpec = uiState.currencySpec,
                                    grouping = uiState.userPreferences.numberGrouping,
                                    style = tabularMoneyStyle(MaterialTheme.typography.bodyMedium),
                                    colorMode = MoneyColorMode.NEUTRAL
                                )
                            }
                        }

                        // EMIs Breakdown
                        val activePlansCount = uiState.installmentPlans.count { it.remainingInstallmentsCount > 0 }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Credit Card EMIs ($activePlansCount active)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            MoneyText(
                                money = uiState.activeMonthlyInstallmentsBurden,
                                currencySpec = uiState.currencySpec,
                                grouping = uiState.userPreferences.numberGrouping,
                                style = tabularMoneyStyle(MaterialTheme.typography.bodyMedium),
                                colorMode = MoneyColorMode.NEUTRAL
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Primary Tabs
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "Recurring Rules (${uiState.recurringRules.size})",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "Active EMIs (${uiState.installmentPlans.size})",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tab Content
            when (selectedTab) {
                0 -> {
                    SubscriptionsTabContent(
                        rules = uiState.recurringRules,
                        uiState = uiState,
                        onToggleActive = onToggleRecurringActive,
                        onEditRule = { ruleToEdit = it },
                        onDeleteRule = { ruleToDelete = it },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 20.dp)
                    )
                }

                1 -> {
                    InstallmentsTabContent(
                        plans = uiState.installmentPlans,
                        uiState = uiState,
                        onDeletePlan = { planToDelete = it },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 20.dp)
                    )
                }
            }
        }
    }

    // Delete Recurring Rule Confirmation Dialog
    ruleToDelete?.let { rule ->
        AlertDialog(
            onDismissRequest = { ruleToDelete = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Delete Recurring Rule?") },
            text = {
                Text("Are you sure you want to delete \"${rule.title}\"? Future automated transactions will no longer be generated. Past transactions will remain unchanged.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteRecurringRule(rule.id)
                        ruleToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { ruleToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Installment Plan Confirmation Dialog
    planToDelete?.let { plan ->
        AlertDialog(
            onDismissRequest = { planToDelete = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Cancel Installment Plan?") },
            text = {
                Text("Are you sure you want to remove the installment plan for \"${plan.title}\"? Any remaining future scheduled installments will be removed.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteInstallmentPlan(plan.id)
                        planToDelete = null
                    }
                ) {
                    Text("Cancel Plan", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { planToDelete = null }) {
                    Text("Keep Plan")
                }
            }
        )
    }

    // Edit Recurring Rule Dialog
    ruleToEdit?.let { rule ->
        EditRecurringRuleDialog(
            rule = rule,
            currencySpec = uiState.currencySpec,
            onDismiss = { ruleToEdit = null },
            onSave = { updatedRule ->
                onUpdateRecurringRule(updatedRule)
                ruleToEdit = null
            }
        )
    }
}

@Composable
private fun SubscriptionsTabContent(
    rules: List<RecurringCommitmentItem>,
    uiState: SettingsUiState,
    onToggleActive: (Long, Boolean) -> Unit,
    onEditRule: (RecurringCommitmentItem) -> Unit,
    onDeleteRule: (RecurringCommitmentItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val semanticColors = LocalFolioSemanticColors.current
    val today = LocalDate.now()

    if (rules.isEmpty()) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
            modifier = modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.EventRepeat,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No Recurring Rules Set",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "When creating an expense or income in Add Transaction, tap [Repeat] to schedule recurring payments.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = modifier.fillMaxWidth()
        ) {
            items(rules, key = { it.id }) { rule ->
                val nextOccurrenceDate = LocalDate.ofEpochDay(rule.nextOccurrenceEpochDay)
                val daysUntil = ChronoUnit.DAYS.between(today, nextOccurrenceDate)
                val nextDueLabel = when {
                    daysUntil < 0L -> "Overdue (${-daysUntil}d ago)"
                    daysUntil == 0L -> "Due Today"
                    daysUntil == 1L -> "Due Tomorrow"
                    else -> "Next: ${nextOccurrenceDate.format(dateFormatter)} (in ${daysUntil}d)"
                }

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (rule.isActive) {
                            MaterialTheme.colorScheme.surfaceContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerLowest
                        }
                    ),
                    border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Top Row: Category Badge + Title + Active Switch
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (rule.type == TransactionType.TRANSFER) {
                                CategoryIconBadge(
                                    iconName = "swap_horiz",
                                    colorHex = "#2563EB",
                                    size = 36.dp,
                                    iconSize = 18.dp
                                )
                            } else {
                                CategoryIconBadge(
                                    iconName = rule.categoryIconName ?: "autorenew",
                                    colorHex = rule.categoryColorHex ?: "#059669",
                                    size = 36.dp,
                                    iconSize = 18.dp
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = rule.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val accountDisplay = if (rule.type == TransactionType.TRANSFER && rule.transferToAccountName != null) {
                                        "${rule.accountName} → ${rule.transferToAccountName}"
                                    } else {
                                        rule.accountName
                                    }
                                    Text(
                                        text = accountDisplay,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                    ) {
                                        Text(
                                            text = rule.frequency.shortLabel,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            // Active / Pause Switch
                            Switch(
                                checked = rule.isActive,
                                onCheckedChange = { onToggleActive(rule.id, it) },
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        // Middle Row: Amount & Next Date
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            MoneyText(
                                money = rule.amount,
                                currencySpec = uiState.currencySpec,
                                grouping = uiState.userPreferences.numberGrouping,
                                style = tabularMoneyStyle(MaterialTheme.typography.titleMedium),
                                colorMode = MoneyColorMode.fromTransactionType(rule.type)
                            )

                            Text(
                                text = nextDueLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (daysUntil <= 3L && rule.isActive) FontWeight.Bold else FontWeight.Normal,
                                color = if (daysUntil <= 3L && rule.isActive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Bottom Actions: Edit & Delete
                        Row(
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextButton(
                                onClick = { onEditRule(rule) },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Edit", style = MaterialTheme.typography.labelMedium)
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            TextButton(
                                onClick = { onDeleteRule(rule) },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Delete",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InstallmentsTabContent(
    plans: List<InstallmentPlanWithProgress>,
    uiState: SettingsUiState,
    onDeletePlan: (InstallmentPlanWithProgress) -> Unit,
    modifier: Modifier = Modifier
) {
    val semanticColors = LocalFolioSemanticColors.current

    if (plans.isEmpty()) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
            modifier = modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.CreditCard,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No Active Installment Plans",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "When recording an expense with a Credit Card in Add Transaction, tap [Inst.] to spread the purchase into monthly EMIs.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = modifier.fillMaxWidth()
        ) {
            items(plans, key = { it.id }) { plan ->
                val progressFraction = if (plan.monthsCount > 0) {
                    (plan.paidInstallmentsCount.toFloat() / plan.monthsCount.toFloat()).coerceIn(0f, 1f)
                } else 1f

                val isCompleted = plan.remainingInstallmentsCount == 0

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCompleted) {
                            MaterialTheme.colorScheme.surfaceContainerLowest
                        } else {
                            MaterialTheme.colorScheme.surfaceContainer
                        }
                    ),
                    border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Header: Title + Card Name + Delete
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CreditCard,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = plan.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${plan.accountName} • ${plan.paidInstallmentsCount} of ${plan.monthsCount} paid",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(
                                onClick = { onDeletePlan(plan) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Cancel Plan",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Progress Bar
                        LinearProgressIndicator(
                            progress = { progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )

                        // Financial Summary Row
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = "Monthly EMI",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                MoneyText(
                                    money = plan.monthlyAmount,
                                    currencySpec = uiState.currencySpec,
                                    grouping = uiState.userPreferences.numberGrouping,
                                    style = tabularMoneyStyle(MaterialTheme.typography.bodyMedium),
                                    colorMode = MoneyColorMode.NEUTRAL
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (isCompleted) "Fully Paid" else "Remaining",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                MoneyText(
                                    money = plan.remainingAmount,
                                    currencySpec = uiState.currencySpec,
                                    grouping = uiState.userPreferences.numberGrouping,
                                    style = tabularMoneyStyle(MaterialTheme.typography.bodyMedium),
                                    colorMode = if (isCompleted) MoneyColorMode.NEUTRAL else MoneyColorMode.EXPENSE
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditRecurringRuleDialog(
    rule: RecurringCommitmentItem,
    currencySpec: CurrencySpec,
    onDismiss: () -> Unit,
    onSave: (RecurringCommitmentItem) -> Unit
) {
    var title by remember { mutableStateOf(rule.title) }
    val initialAmount = remember(rule.amount, currencySpec.fractionDigits) {
        formatMinorPlainForInput(rule.amount, currencySpec.fractionDigits)
    }
    var amountInput by remember(initialAmount) { mutableStateOf(initialAmount) }
    var selectedFrequency by remember { mutableStateOf(rule.frequency) }
    var frequencyExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit Recurring Rule",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (rule.type == TransactionType.TRANSFER && rule.transferToAccountName != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Transfer: ${rule.accountName} → ${rule.transferToAccountName}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title / Payee") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = { Text("Amount (${currencySpec.symbol})") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = frequencyExpanded,
                    onExpandedChange = { frequencyExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedFrequency.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Repeat Frequency") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = frequencyExpanded) },
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = frequencyExpanded,
                        onDismissRequest = { frequencyExpanded = false }
                    ) {
                        RepeatFrequency.entries.forEach { freq ->
                            DropdownMenuItem(
                                text = { Text(freq.displayName) },
                                onClick = {
                                    selectedFrequency = freq
                                    frequencyExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val evaluatedResult = MoneyFormatterAndCalculator.evaluateKeypadExpressionToMinor(
                        expression = amountInput,
                        fractionDigits = currencySpec.fractionDigits
                    )
                    val evaluatedMinor = evaluatedResult.getOrNull()?.amountMinor ?: 0L
                    if (evaluatedMinor > 0L && title.isNotBlank()) {
                        val nextDate = RecurringScheduleCalculator.computeNextOccurrence(
                            currentDate = LocalDate.now(),
                            frequency = selectedFrequency
                        )
                        onSave(
                            rule.copy(
                                title = title.trim(),
                                amount = MinorMoney(evaluatedMinor),
                                frequency = selectedFrequency,
                                nextOccurrenceEpochDay = nextDate.toEpochDay()
                            )
                        )
                    }
                }
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
