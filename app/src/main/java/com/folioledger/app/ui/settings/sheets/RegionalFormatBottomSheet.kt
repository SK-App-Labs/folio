package com.folioledger.app.ui.settings.sheets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.folioledger.app.domain.model.CurrencySpec
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.NumberGroupingSystem
import com.folioledger.app.domain.model.SecondaryCurrency
import com.folioledger.app.domain.model.SupportedCurrencies
import com.folioledger.app.domain.model.UserPreferences
import com.folioledger.app.domain.util.MoneyFormatterAndCalculator
import com.folioledger.app.ui.theme.LocalFolioSemanticColors
import com.folioledger.app.ui.theme.tabularMoneyStyle

typealias NumberGroupingStyle = NumberGroupingSystem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegionalFormatBottomSheet(
    prefs: UserPreferences,
    onSelectCurrency: (String) -> Unit,
    onSelectGroupingStyle: (NumberGroupingStyle) -> Unit,
    onAddOrUpdateSecondaryCurrency: (code: String, rate: Double) -> Unit,
    onRemoveSecondaryCurrency: (code: String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val activeCurrencySpec = SupportedCurrencies.fromCode(prefs.primaryCurrencyCode)
    val semanticColors = LocalFolioSemanticColors.current

    var searchCurrencyQuery by rememberSaveable { mutableStateOf("") }
    var isBrowseAllExpanded by rememberSaveable { mutableStateOf(false) }

    var editingSecondaryCurrency by remember { mutableStateOf<SecondaryCurrency?>(null) }
    var isAddingSecondaryCurrency by rememberSaveable { mutableStateOf(false) }

    val allCurrencies = remember { SupportedCurrencies.ALL }
    val popularCurrencies = remember { SupportedCurrencies.POPULAR }

    val filteredCurrencies = remember(allCurrencies, popularCurrencies, searchCurrencyQuery, isBrowseAllExpanded) {
        if (searchCurrencyQuery.isNotBlank()) {
            allCurrencies.filter { currency ->
                currency.code.contains(searchCurrencyQuery, ignoreCase = true) ||
                currency.displayName.contains(searchCurrencyQuery, ignoreCase = true) ||
                currency.symbol.contains(searchCurrencyQuery, ignoreCase = true)
            }
        } else if (isBrowseAllExpanded) {
            allCurrencies
        } else {
            popularCurrencies
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
                .fillMaxHeight(0.88f)
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp, top = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Regional & Formatting",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Live Formatted Preview Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "LIVE NUMBER FORMAT PREVIEW",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = MoneyFormatterAndCalculator.formatMinorMoney(
                            money = MinorMoney(123456789L),
                            currencySpec = activeCurrencySpec,
                            grouping = prefs.numberGrouping,
                            includeSymbol = true
                        ),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "${activeCurrencySpec.displayName} (${if (prefs.numberGrouping == NumberGroupingSystem.INDIAN) "Indian" else "Standard"} Grouping)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            // Number Grouping System Selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "NUMBER GROUPING STYLE",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val isIndian = prefs.numberGrouping == NumberGroupingSystem.INDIAN
                    val isStandard = prefs.numberGrouping == NumberGroupingSystem.INTERNATIONAL

                    FilterChip(
                        selected = isIndian,
                        onClick = { onSelectGroupingStyle(NumberGroupingSystem.INDIAN) },
                        label = { Text("Indian (12,34,567.89)") },
                        leadingIcon = if (isIndian) {
                            { Icon(Icons.Default.Check, contentDescription = null, Modifier.size(16.dp)) }
                        } else null,
                        modifier = Modifier.weight(1f)
                    )

                    FilterChip(
                        selected = isStandard,
                        onClick = { onSelectGroupingStyle(NumberGroupingSystem.INTERNATIONAL) },
                        label = { Text("Standard (1,234,567.89)") },
                        leadingIcon = if (isStandard) {
                            { Icon(Icons.Default.Check, contentDescription = null, Modifier.size(16.dp)) }
                        } else null,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            HorizontalDivider()

            // Primary Currency Section
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "PRIMARY LEDGER CURRENCY",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Search Currency Field
                OutlinedTextField(
                    value = searchCurrencyQuery,
                    onValueChange = { searchCurrencyQuery = it },
                    placeholder = { Text("Search currency") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchCurrencyQuery.isNotBlank()) {
                            IconButton(onClick = { searchCurrencyQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search"
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick / Popular Currencies Chips
                if (popularCurrencies.isNotEmpty() && searchCurrencyQuery.isBlank()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "⚡ POPULAR CURRENCIES",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(popularCurrencies, key = { it.code }) { currency ->
                                val isSelected = prefs.primaryCurrencyCode.equals(currency.code, ignoreCase = true)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onSelectCurrency(currency.code) },
                                    label = { Text("${currency.code} (${currency.symbol})") },
                                    leadingIcon = if (isSelected) {
                                        { Icon(Icons.Default.Check, contentDescription = null, Modifier.size(16.dp)) }
                                    } else null
                                )
                            }
                        }
                    }
                }

                // Currency Selectable Cards List
                if (filteredCurrencies.isEmpty()) {
                    Text(
                        text = "No currency found matching \"$searchCurrencyQuery\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        filteredCurrencies.forEach { currency ->
                            val isSelected = prefs.primaryCurrencyCode.equals(currency.code, ignoreCase = true)
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) {
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceContainerLow
                                    }
                                ),
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else semanticColors.cardStrokeSubtle
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { onSelectCurrency(currency.code) }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest,
                                            modifier = Modifier.size(38.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = currency.symbol,
                                                    style = tabularMoneyStyle(MaterialTheme.typography.titleMedium),
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "${currency.displayName} (${currency.code})",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                        }

                        if (searchCurrencyQuery.isBlank()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { isBrowseAllExpanded = !isBrowseAllExpanded }
                                    .padding(vertical = 12.dp, horizontal = 14.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = if (isBrowseAllExpanded) {
                                            "Show Top Currencies Only"
                                        } else {
                                            "Browse All ${allCurrencies.size} World Currencies ⌄"
                                        },
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider()

            // Secondary Currencies Section
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Column {
                    Text(
                        text = "SECONDARY CURRENCIES & RATES",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Currencies you hold with manual exchange rates relative to 1 ${prefs.primaryCurrencyCode}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (prefs.secondaryCurrencies.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "No secondary currencies added yet. Add currencies like USD or EUR to track foreign accounts and calculate converted Net Worth.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        prefs.secondaryCurrencies.forEach { sec ->
                            val secSpec = SupportedCurrencies.fromCode(sec.code)
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                ),
                                border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = secSpec.symbol,
                                                    style = tabularMoneyStyle(MaterialTheme.typography.titleSmall),
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "${secSpec.displayName} (${sec.code})",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "1 ${sec.code} = ${sec.exchangeRateToPrimary} ${prefs.primaryCurrencyCode}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = { editingSecondaryCurrency = sec }) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit Rate",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        IconButton(onClick = { onRemoveSecondaryCurrency(sec.code) }) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Remove Currency",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                FilledTonalButton(
                    onClick = { isAddingSecondaryCurrency = true },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Secondary Currency & Rate")
                }
            }
        }

        if (isAddingSecondaryCurrency) {
            AddOrEditSecondaryCurrencyDialog(
                primaryCurrencyCode = prefs.primaryCurrencyCode,
                initialCurrencyCode = null,
                initialRate = null,
                existingCodes = prefs.secondaryCurrencies.map { it.code }.toSet(),
                onSave = onAddOrUpdateSecondaryCurrency,
                onDismiss = { isAddingSecondaryCurrency = false }
            )
        }

        editingSecondaryCurrency?.let { sec ->
            AddOrEditSecondaryCurrencyDialog(
                primaryCurrencyCode = prefs.primaryCurrencyCode,
                initialCurrencyCode = sec.code,
                initialRate = sec.exchangeRateToPrimary,
                existingCodes = prefs.secondaryCurrencies.map { it.code }.toSet(),
                onSave = onAddOrUpdateSecondaryCurrency,
                onDismiss = { editingSecondaryCurrency = null }
            )
        }
    }
}

@Composable
private fun AddOrEditSecondaryCurrencyDialog(
    primaryCurrencyCode: String,
    initialCurrencyCode: String? = null,
    initialRate: Double? = null,
    existingCodes: Set<String>,
    onSave: (code: String, rate: Double) -> Unit,
    onDismiss: () -> Unit
) {
    val isEditing = initialCurrencyCode != null
    var selectedCode by rememberSaveable { mutableStateOf(initialCurrencyCode ?: "USD") }
    var rateInput by rememberSaveable { mutableStateOf(initialRate?.toString() ?: "") }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }

    val allCurrencies = remember { SupportedCurrencies.ALL }
    val availableCurrencies = remember(primaryCurrencyCode, existingCodes, isEditing, initialCurrencyCode) {
        allCurrencies.filter {
            it.code != primaryCurrencyCode && (it.code == initialCurrencyCode || it.code !in existingCodes)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEditing) "Edit Secondary Currency Rate" else "Add Secondary Currency",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Configure manual exchange rate relative to your primary currency ($primaryCurrencyCode).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (!isEditing) {
                    Text(
                        text = "SELECT CURRENCY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    val popularSecondaries = remember(availableCurrencies) {
                        val popularCodes = listOf("USD", "EUR", "GBP", "AED", "SGD", "CAD", "AUD", "JPY", "SAR")
                        availableCurrencies.filter { it.code in popularCodes }
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        popularSecondaries.take(6).forEach { spec ->
                            FilterChip(
                                selected = selectedCode == spec.code,
                                onClick = { selectedCode = spec.code },
                                label = { Text("${spec.symbol} ${spec.code}") }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = rateInput,
                    onValueChange = {
                        rateInput = it
                        errorMessage = null
                    },
                    label = { Text("1 $selectedCode = ? $primaryCurrencyCode") },
                    placeholder = { Text("e.g. 85.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    isError = errorMessage != null,
                    supportingText = errorMessage?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth()
                )

                val selectedSpec = SupportedCurrencies.fromCode(selectedCode)
                val primarySpec = SupportedCurrencies.fromCode(primaryCurrencyCode)
                val parsedRate = rateInput.toDoubleOrNull()
                if (parsedRate != null && parsedRate > 0.0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Conversion preview: 1 ${selectedSpec.displayName} (${selectedSpec.symbol}) = $parsedRate ${primarySpec.displayName} (${primarySpec.symbol})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val rate = rateInput.toDoubleOrNull()
                    if (rate == null || rate <= 0.0) {
                        errorMessage = "Please enter a valid rate greater than 0"
                    } else {
                        onSave(selectedCode, rate)
                        onDismiss()
                    }
                }
            ) {
                Text(if (isEditing) "Save Rate" else "Add Currency")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}