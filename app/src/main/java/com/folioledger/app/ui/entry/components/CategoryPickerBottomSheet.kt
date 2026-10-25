package com.folioledger.app.ui.entry.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import com.folioledger.app.domain.model.CategoryNode
import com.folioledger.app.domain.model.CategoryType
import com.folioledger.app.ui.components.CategoryIconBadge
import com.folioledger.app.ui.components.parseHexColorSafely
import com.folioledger.app.ui.components.suggestIconAndColorForCategoryName
import com.folioledger.app.ui.theme.LocalFolioSemanticColors
import com.folioledger.app.ui.theme.tabularMoneyStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryPickerBottomSheet(
    categories: List<CategoryNode>,
    selectedCategoryId: Long?,
    subcategoriesEnabled: Boolean = true,
    categoryType: CategoryType = CategoryType.EXPENSE,
    onCategorySelected: (CategoryNode, String?) -> Unit,
    onCreateNewCategory: (name: String, parentId: Long?, parentName: String?, iconName: String, colorHex: String, onCreated: (CategoryNode) -> Unit) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val semanticColors = LocalFolioSemanticColors.current

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var expandedCategoryIds by rememberSaveable { mutableStateOf(setOf<Long>()) }

    var showCreateDialog by rememberSaveable { mutableStateOf(false) }
    var createParentId by rememberSaveable { mutableStateOf<Long?>(null) }
    var createParentName by rememberSaveable { mutableStateOf<String?>(null) }
    var initialNewCategoryName by rememberSaveable { mutableStateOf("") }

    // Top 6 Frequent categories
    val frequentCategories = remember(categories) {
        val flatList = mutableListOf<Pair<CategoryNode, String?>>()
        categories.take(4).forEach { parent ->
            flatList.add(parent to null)
            parent.subcategories.take(2).forEach { sub ->
                flatList.add(sub to parent.name)
            }
        }
        flatList.take(6)
    }

    // Filter categories dynamically based on searchQuery
    val filteredCategories = remember(categories, searchQuery) {
        if (searchQuery.isBlank()) {
            categories
        } else {
            categories.mapNotNull { parent ->
                val parentMatches = parent.name.contains(searchQuery, ignoreCase = true)
                val matchingSubs = parent.subcategories.filter { sub ->
                    sub.name.contains(searchQuery, ignoreCase = true)
                }
                if (parentMatches || matchingSubs.isNotEmpty()) {
                    parent.copy(
                        subcategories = if (parentMatches) parent.subcategories else matchingSubs
                    )
                } else null
            }
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
                .fillMaxHeight(0.88f)
                .padding(bottom = 24.dp)
        ) {
            // Header Row: Title & "+ New Category" Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (subcategoriesEnabled) "Select Category & Subcategory" else "Select Category",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (categoryType == CategoryType.EXPENSE) "Expense Categories" else "Income Categories",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                FilledTonalButton(
                    onClick = {
                        createParentId = null
                        createParentName = null
                        initialNewCategoryName = searchQuery
                        showCreateDialog = true
                    },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Category", style = MaterialTheme.typography.labelMedium)
                }
            }

            // Real-Time Search Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search categories or subcategories...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search"
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
            )

            // ⚡ Frequent Categories Quick-Access Row
            if (frequentCategories.isNotEmpty() && searchQuery.isBlank()) {
                Column(modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)) {
                    Text(
                        text = "⚡ FREQUENT",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(frequentCategories, key = { "${it.first.id}_${it.second}" }) { (node, parentName) ->
                            val isSelected = node.id == selectedCategoryId
                            FilterChip(
                                selected = isSelected,
                                onClick = { onCategorySelected(node, parentName) },
                                leadingIcon = {
                                    CategoryIconBadge(
                                        iconName = node.iconName,
                                        colorHex = node.colorHex,
                                        size = 20.dp,
                                        iconSize = 12.dp
                                    )
                                },
                                label = {
                                    Text(
                                        text = if (parentName != null) "$parentName › ${node.name}" else node.name,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                                    )
                                }
                            )
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Fallback Create Card if Search Query has no matches
            if (filteredCategories.isEmpty()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "No categories found matching \"$searchQuery\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            createParentId = null
                            createParentName = null
                            initialNewCategoryName = searchQuery
                            showCreateDialog = true
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Create \"$searchQuery\" as New Category")
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(filteredCategories, key = { it.id }) { parent ->
                        val isSearching = searchQuery.isNotBlank()
                        val isExpanded = isSearching || expandedCategoryIds.contains(parent.id)
                        val isParentSelected = parent.id == selectedCategoryId

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isParentSelected) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerLow
                                }
                            ),
                            border = BorderStroke(
                                width = if (isParentSelected) 2.dp else 1.dp,
                                color = if (isParentSelected) MaterialTheme.colorScheme.primary else semanticColors.cardStrokeSubtle
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                // Parent Category Header Row
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onCategorySelected(parent, null) }
                                        .padding(4.dp)
                                ) {
                                    CategoryIconBadge(
                                        iconName = parent.iconName,
                                        colorHex = parent.colorHex,
                                        size = 38.dp,
                                        iconSize = 19.dp
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = parent.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (subcategoriesEnabled) {
                                            Text(
                                                text = "${parent.subcategories.size} subcategories",
                                                style = tabularMoneyStyle(MaterialTheme.typography.bodySmall),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    if (isParentSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }

                                    if (subcategoriesEnabled) {
                                        IconButton(
                                            onClick = {
                                                if (!isSearching) {
                                                    expandedCategoryIds = if (isExpanded) {
                                                        expandedCategoryIds - parent.id
                                                    } else {
                                                        expandedCategoryIds + parent.id
                                                    }
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                contentDescription = if (isExpanded) "Collapse" else "Expand",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                // Expanded Subcategory List
                                if (subcategoriesEnabled && isExpanded) {
                                    AnimatedVisibility(visible = isExpanded) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 10.dp)
                                        ) {
                                            HorizontalDivider(color = semanticColors.cardStrokeSubtle)
                                            Spacer(modifier = Modifier.height(8.dp))

                                            if (parent.subcategories.isEmpty()) {
                                                Text(
                                                    text = "No subcategories created yet under ${parent.name}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 4.dp)
                                                )
                                            } else {
                                                parent.subcategories.forEach { sub ->
                                                    val isSubSelected = sub.id == selectedCategoryId
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(start = 20.dp, top = 2.dp, bottom = 2.dp)
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(
                                                                if (isSubSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                                                            )
                                                            .clickable { onCategorySelected(sub, parent.name) }
                                                            .padding(horizontal = 10.dp, vertical = 8.dp)
                                                    ) {
                                                        CategoryIconBadge(
                                                            iconName = sub.iconName,
                                                            colorHex = sub.colorHex,
                                                            size = 32.dp,
                                                            iconSize = 16.dp
                                                        )
                                                        Spacer(modifier = Modifier.width(10.dp))
                                                        Text(
                                                            text = sub.name,
                                                            style = MaterialTheme.typography.bodyLarge,
                                                            fontWeight = if (isSubSelected) FontWeight.Bold else FontWeight.SemiBold,
                                                            color = if (isSubSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                        if (isSubSelected) {
                                                            Icon(
                                                                imageVector = Icons.Default.Check,
                                                                contentDescription = "Selected",
                                                                tint = MaterialTheme.colorScheme.primary,
                                                                modifier = Modifier.size(18.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            TextButton(
                                                onClick = {
                                                    createParentId = parent.id
                                                    createParentName = parent.name
                                                    initialNewCategoryName = ""
                                                    showCreateDialog = true
                                                },
                                                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Add Subcategory under ${parent.name}",
                                                    style = MaterialTheme.typography.labelMedium
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

    if (showCreateDialog) {
        InlineCategoryCreateDialog(
            initialName = initialNewCategoryName,
            parentId = createParentId,
            parentName = createParentName,
            categoryType = categoryType,
            onCreateCategory = { name, parentId, parentName, iconName, colorHex ->
                onCreateNewCategory(name, parentId, parentName, iconName, colorHex) { createdNode ->
                    val targetExpandId = parentId ?: createdNode.id
                    expandedCategoryIds = expandedCategoryIds + targetExpandId
                }
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false }
        )
    }
}

@Composable
private fun InlineCategoryCreateDialog(
    initialName: String,
    parentId: Long?,
    parentName: String?,
    categoryType: CategoryType,
    onCreateCategory: (name: String, parentId: Long?, parentName: String?, iconName: String, colorHex: String) -> Unit,
    onDismiss: () -> Unit
) {
    var nameInput by remember { mutableStateOf(initialName) }
    var selectedColorHex by remember {
        mutableStateOf(if (categoryType == CategoryType.EXPENSE) "#E11D48" else "#059669")
    }
    var selectedIconName by remember {
        mutableStateOf(if (categoryType == CategoryType.EXPENSE) "shopping_cart" else "payments")
    }

    val swatches = remember {
        listOf(
            "#059669", "#10B981", "#0D9488", "#0284C7",
            "#2563EB", "#4F46E5", "#7C3AED", "#EC4899",
            "#E11D48", "#F97316", "#EA580C", "#D97706",
            "#334155"
        )
    }

    LaunchedEffect(nameInput) {
        if (nameInput.isNotBlank()) {
            val suggestion = suggestIconAndColorForCategoryName(nameInput, categoryType)
            if (suggestion != null) {
                selectedIconName = suggestion.first
                selectedColorHex = suggestion.second
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CategoryIconBadge(
                    iconName = selectedIconName,
                    colorHex = selectedColorHex,
                    size = 38.dp,
                    iconSize = 20.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (parentId != null) "New Subcategory" else "New Category",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (!parentName.isNullOrBlank()) {
                        Text(
                            text = "Under $parentName",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Category Name") },
                    placeholder = { Text("e.g. Filter Coffee, Swiggy, Fuel") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Color Badge",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    swatches.forEach { hex ->
                        val color = parseHexColorSafely(hex)
                        val isSelected = selectedColorHex.equals(hex, ignoreCase = true)
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { selectedColorHex = hex }
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nameInput.isNotBlank()) {
                        onCreateCategory(
                            nameInput.trim(),
                            parentId,
                            parentName,
                            selectedIconName,
                            selectedColorHex
                        )
                    }
                },
                enabled = nameInput.isNotBlank()
            ) {
                Text("Create & Select")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
