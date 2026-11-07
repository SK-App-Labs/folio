package com.folioledger.app.ui.accounts.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.folioledger.app.domain.model.AccountGroup
import com.folioledger.app.domain.model.SecondaryCurrency
import com.folioledger.app.domain.model.SupportedCurrencies
import com.folioledger.app.ui.accounts.AccountsViewModel
import com.folioledger.app.ui.components.CategoryIconBadge
import com.folioledger.app.ui.components.parseHexColorSafely
import com.folioledger.app.ui.theme.LocalFolioSemanticColors

@Composable
fun AccountTypeQuickPickTile(
    group: AccountGroup,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val semanticColors = LocalFolioSemanticColors.current
    val groupColorHex = AccountsViewModel.defaultColorForGroup(group)
    val groupIcon = remember(group) { AccountsViewModel.defaultIconForGroup(group) }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f)
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        },
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else semanticColors.cardStrokeSubtle
        ),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp)
        ) {
            CategoryIconBadge(
                iconName = groupIcon,
                colorHex = groupColorHex,
                size = 32.dp,
                iconSize = 18.dp
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formatAccountTypeSingularTitle(group),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = when {
                        group.isLiabilityByDefault -> "Liability"
                        group == AccountGroup.DEBIT_CARD -> "Linked Asset"
                        else -> "Asset"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (group.isLiabilityByDefault) {
                        semanticColors.expenseCoral
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

fun formatAccountTypeSingularTitle(group: AccountGroup): String = when (group) {
    AccountGroup.CASH -> "Cash"
    AccountGroup.BANK_ACCOUNT -> "Bank Account"
    AccountGroup.DEBIT_CARD -> "Debit Card"
    AccountGroup.CREDIT_CARD -> "Credit Card"
    AccountGroup.SAVINGS -> "Savings"
    AccountGroup.PREPAID_WALLET -> "Prepaid Wallet"
    AccountGroup.INVESTMENT -> "Investments"
    AccountGroup.OVERDRAFT -> "Overdraft"
    AccountGroup.LOAN -> "Loan"
    AccountGroup.OTHER -> "Other"
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AccountCurrencyPickerCard(
    currencyCode: String,
    primaryCurrencyCode: String,
    secondaryCurrencies: List<SecondaryCurrency>,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    onSelectCurrency: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val semanticColors = LocalFolioSemanticColors.current

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Public,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Account Currency: $currencyCode",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Curated Active Currencies: Primary + Configured Secondaries
            val activeSpecs = remember(primaryCurrencyCode, secondaryCurrencies, currencyCode) {
                val prim = SupportedCurrencies.fromCode(primaryCurrencyCode)
                val secs = secondaryCurrencies.map { SupportedCurrencies.fromCode(it.code) }
                val current = SupportedCurrencies.fromCode(currencyCode)
                (listOf(prim) + secs + listOf(current)).distinctBy { it.code.uppercase() }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                activeSpecs.forEach { spec ->
                    val isSelected = currencyCode.equals(spec.code, ignoreCase = true)
                    val isPrimary = spec.code.equals(primaryCurrencyCode, ignoreCase = true)
                    val secMatch = secondaryCurrencies.firstOrNull { it.code.equals(spec.code, ignoreCase = true) }

                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectCurrency(spec.code) },
                        label = {
                            val labelText = when {
                                isPrimary -> "${spec.symbol} ${spec.code} (Primary)"
                                secMatch != null -> "${spec.symbol} ${spec.code} (1 = ${secMatch.exchangeRateToPrimary})"
                                else -> "${spec.symbol} ${spec.code}"
                            }
                            Text(labelText)
                        }
                    )
                }
            }

            // Collapsible "More Currencies" toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpanded)
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isExpanded) "Hide other currencies" else "Choose another currency... ⌄",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 4.dp)) {
                    val popular = SupportedCurrencies.POPULAR
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        popular.forEach { spec ->
                            FilterChip(
                                selected = currencyCode.equals(spec.code, ignoreCase = true),
                                onClick = { onSelectCurrency(spec.code) },
                                label = { Text("${spec.symbol} ${spec.code}") }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AccountAppearanceCustomizerCard(
    colorHex: String,
    iconName: String,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    onSelectColor: (String) -> Unit,
    onSelectIcon: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val semanticColors = LocalFolioSemanticColors.current
    val swatchPresets = remember {
        listOf("#059669", "#2563EB", "#4F46E5", "#D97706", "#E11D48", "#0D9488", "#7C3AED", "#334155")
    }
    val iconPresets = remember {
        listOf(
            "account_balance",
            "account_balance_wallet",
            "credit_card",
            "savings",
            "trending_up",
            "payments",
            "security",
            "work"
        )
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onToggleExpanded)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Palette,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Customize Icon & Badge Color",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(top = 10.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        swatchPresets.forEach { hex ->
                            val swatch = parseHexColorSafely(hex)
                            val selected = colorHex.equals(hex, ignoreCase = true)
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(swatch)
                                    .clickable { onSelectColor(hex) }
                            ) {
                                if (selected) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        iconPresets.forEach { iconKey ->
                            val isSelected = iconName == iconKey
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainer
                                },
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        semanticColors.cardStrokeSubtle
                                    }
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onSelectIcon(iconKey) }
                                    .padding(8.dp)
                            ) {
                                CategoryIconBadge(
                                    iconName = iconKey,
                                    colorHex = colorHex,
                                    size = 32.dp,
                                    iconSize = 18.dp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
