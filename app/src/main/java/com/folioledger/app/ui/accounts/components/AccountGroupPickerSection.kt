package com.folioledger.app.ui.accounts.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.folioledger.app.domain.model.AccountGroup

@Composable
fun AccountGroupPickerSection(
    selectedGroup: AccountGroup,
    onGroupSelected: (AccountGroup) -> Unit,
    modifier: Modifier = Modifier
) {
    val top4Groups = remember {
        listOf(
            AccountGroup.BANK_ACCOUNT,
            AccountGroup.CREDIT_CARD,
            AccountGroup.CASH,
            AccountGroup.DEBIT_CARD
        )
    }
    val secondaryGroups = remember {
        listOf(
            AccountGroup.SAVINGS,
            AccountGroup.PREPAID_WALLET,
            AccountGroup.INVESTMENT,
            AccountGroup.OVERDRAFT,
            AccountGroup.LOAN,
            AccountGroup.OTHER
        )
    }
    var isMoreTypesExpanded by rememberSaveable {
        mutableStateOf(secondaryGroups.contains(selectedGroup))
    }
    val haptic = LocalHapticFeedback.current

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Account Type",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = if (top4Groups.contains(selectedGroup)) "1-Tap Quick Pick" else "Custom Type",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Top 4 Quick-Pick 2x2 Grid
        top4Groups.chunked(2).forEach { rowPair ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                rowPair.forEach { group ->
                    AccountTypeQuickPickTile(
                        group = group,
                        isSelected = selectedGroup == group,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onGroupSelected(group)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Collapsible More Types Expander
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.45f),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    isMoreTypesExpanded = !isMoreTypesExpanded
                }
                .padding(vertical = 8.dp, horizontal = 12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isMoreTypesExpanded) {
                        "Show Less Types"
                    } else {
                        "More Types (Savings, Loan, Investment...)"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = if (isMoreTypesExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        AnimatedVisibility(visible = isMoreTypesExpanded) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                secondaryGroups.chunked(2).forEach { rowPair ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        rowPair.forEach { group ->
                            AccountTypeQuickPickTile(
                                group = group,
                                isSelected = selectedGroup == group,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onGroupSelected(group)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowPair.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
