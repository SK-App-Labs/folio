// File: app/src/main/java/com/folioledger/app/ui/settings/sheets/RecurringManagerBottomSheet.kt
package com.folioledger.app.ui.settings.sheets

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.folioledger.app.domain.model.InstallmentPlanWithProgress
import com.folioledger.app.domain.model.RecurringCommitmentItem
import com.folioledger.app.ui.settings.SettingsUiState
import com.folioledger.app.ui.settings.sheets.components.EditRecurringRuleDialog
import com.folioledger.app.ui.settings.sheets.components.InstallmentsTabContent
import com.folioledger.app.ui.settings.sheets.components.MonthlyBurdenHeroCard
import com.folioledger.app.ui.settings.sheets.components.SubscriptionsTabContent

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
            MonthlyBurdenHeroCard(
                uiState = uiState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
            )

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
