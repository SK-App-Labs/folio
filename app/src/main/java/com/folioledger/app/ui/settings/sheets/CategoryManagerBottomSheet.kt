package com.folioledger.app.ui.settings.sheets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
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
import androidx.compose.ui.unit.dp
import com.folioledger.app.domain.model.CategoryNode
import com.folioledger.app.domain.model.CategoryType
import com.folioledger.app.ui.components.ALL_CATEGORY_ICON_KEYS
import com.folioledger.app.ui.components.CategoryIconBadge
import com.folioledger.app.ui.components.parseHexColorSafely
import com.folioledger.app.ui.settings.CategoryEditorDialogState
import com.folioledger.app.ui.settings.SettingsUiState
import com.folioledger.app.ui.theme.LocalFolioSemanticColors
import com.folioledger.app.ui.theme.tabularMoneyStyle

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CategoryEditorDialog(
    state: CategoryEditorDialogState,
    onNameChange: (String) -> Unit,
    onUpdateState: ((CategoryEditorDialogState) -> CategoryEditorDialogState) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val swatches = remember {
        listOf(
            "#059669", "#10B981", "#0D9488", "#0284C7",
            "#2563EB", "#4F46E5", "#7C3AED", "#EC4899",
            "#E11D48", "#F97316", "#EA580C", "#D97706",
            "#334155"
        )
    }
    val icons = remember { ALL_CATEGORY_ICON_KEYS }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CategoryIconBadge(
                    iconName = state.iconName,
                    colorHex = state.colorHex,
                    size = 38.dp,
                    iconSize = 20.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = when {
                            state.isEditing -> "Edit Category"
                            state.parentId != null -> "New Subcategory"
                            else -> "New Main Category"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (!state.parentCategoryName.isNullOrBlank()) {
                        Text(
                            text = "Under ${state.parentCategoryName}",
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
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                if (!state.validationError.isNullOrBlank()) {
                    Text(
                        text = state.validationError,
                        style = MaterialTheme.typography.labelMedium,
                        color = LocalFolioSemanticColors.current.expenseCoral
                    )
                }

                OutlinedTextField(
                    value = state.name,
                    onValueChange = onNameChange,
                    label = { Text("Category Name") },
                    placeholder = { Text("e.g., Filter Coffee, Petrol, Swiggy, SIP") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                AnimatedVisibility(visible = state.isSmartIconMatched) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "✨ Auto-matched icon & color from category name",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Text(
                    text = "Badge Color (${swatches.size} Swatches)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    swatches.forEach { hex ->
                        val color = parseHexColorSafely(hex)
                        val selected = state.colorHex.equals(hex, ignoreCase = true)
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { onUpdateState { it.copy(colorHex = hex) } }
                        ) {
                            if (selected) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "Select Icon (${icons.size} Icons)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    icons.forEach { iconKey ->
                        val selected = state.iconName == iconKey
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerLow
                            },
                            border = BorderStroke(
                                width = if (selected) 1.5.dp else 1.dp,
                                color = if (selected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    LocalFolioSemanticColors.current.cardStrokeSubtle
                                }
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onUpdateState { it.copy(iconName = iconKey) } }
                                .padding(6.dp)
                        ) {
                            CategoryIconBadge(
                                iconName = iconKey,
                                colorHex = state.colorHex,
                                size = 28.dp,
                                iconSize = 16.dp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onSave) {
                Text("Save")
            }
        },
        dismissButton = {
            Row {
                if (state.isEditing && state.categoryId > 0L) {
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = LocalFolioSemanticColors.current.expenseCoral
                        )
                    ) {
                        Text("Delete")
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagerBottomSheet(
    uiState: SettingsUiState,
    onSelectCategoryType: (CategoryType) -> Unit,
    onAddMainCategory: () -> Unit,
    onAddSubcategory: (CategoryNode) -> Unit,
    onEditCategory: (CategoryNode, String?) -> Unit,
    onDeleteCategory: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val semanticColors = LocalFolioSemanticColors.current
    val categories = uiState.activeManagedCategories
    val subcategoriesEnabled = uiState.userPreferences.subcategoriesEnabled

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var expandedCategoryIds by rememberSaveable { mutableStateOf(setOf<Long>()) }

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
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(bottom = 24.dp)
        ) {
            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (subcategoriesEnabled) "Category & Subcategory Manager" else "Category Manager",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (subcategoriesEnabled) "Add, edit, or organize 2-level categories" else "Add or edit main categories",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                FilledTonalButton(
                    onClick = onAddMainCategory,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Main Category", style = MaterialTheme.typography.labelMedium)
                }
            }

            // Real-Time Search Bar
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

            // Primary Tab Row
            PrimaryTabRow(
                selectedTabIndex = if (uiState.selectedCategoryManagerType == CategoryType.EXPENSE) 0 else 1,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = uiState.selectedCategoryManagerType == CategoryType.EXPENSE,
                    onClick = { onSelectCategoryType(CategoryType.EXPENSE) },
                    text = {
                        Text(
                            text = "Expenses (${uiState.expenseCategories.size})",
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
                Tab(
                    selected = uiState.selectedCategoryManagerType == CategoryType.INCOME,
                    onClick = { onSelectCategoryType(CategoryType.INCOME) },
                    text = {
                        Text(
                            text = "Income (${uiState.incomeCategories.size})",
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
            }

            if (filteredCategories.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isNotBlank()) "No categories found for \"$searchQuery\"" else "No categories found.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(filteredCategories, key = { it.id }) { parent ->
                        var showMenu by remember { mutableStateOf(false) }
                        val isSearching = searchQuery.isNotBlank()
                        val isExpanded = isSearching || expandedCategoryIds.contains(parent.id)

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            ),
                            border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                // Accordion Parent Row
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
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

                                    // Parent Overflow Menu (⋮)
                                    Box {
                                        IconButton(onClick = { showMenu = true }) {
                                            Icon(
                                                imageVector = Icons.Default.MoreVert,
                                                contentDescription = "Options for ${parent.name}",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        DropdownMenu(
                                            expanded = showMenu,
                                            onDismissRequest = { showMenu = false }
                                        ) {
                                            if (subcategoriesEnabled) {
                                                DropdownMenuItem(
                                                    text = { Text("Add Subcategory") },
                                                    leadingIcon = {
                                                        Icon(Icons.Default.Add, contentDescription = null)
                                                    },
                                                    onClick = {
                                                        showMenu = false
                                                        onAddSubcategory(parent)
                                                    }
                                                )
                                            }
                                            DropdownMenuItem(
                                                text = { Text("Edit Main Category") },
                                                leadingIcon = {
                                                    Icon(Icons.Default.Edit, contentDescription = null)
                                                },
                                                onClick = {
                                                    showMenu = false
                                                    onEditCategory(parent, null)
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Delete Category", color = semanticColors.expenseCoral) },
                                                leadingIcon = {
                                                    Icon(Icons.Default.Delete, contentDescription = null, tint = semanticColors.expenseCoral)
                                                },
                                                onClick = {
                                                    showMenu = false
                                                    onDeleteCategory(parent.id)
                                                }
                                            )
                                        }
                                    }

                                    // Dedicated Chevron IconButton to toggle expansion!
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
                                                contentDescription = if (isExpanded) "Collapse ${parent.name}" else "Expand ${parent.name}",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                // Expanded Subcategory Tree List
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
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(start = 20.dp, top = 4.dp, bottom = 4.dp, end = 4.dp)
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .padding(horizontal = 8.dp, vertical = 6.dp)
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
                                                            fontWeight = FontWeight.SemiBold,
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                        IconButton(
                                                            onClick = { onEditCategory(sub, parent.name) },
                                                            modifier = Modifier.size(28.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Edit,
                                                                contentDescription = "Edit ${sub.name}",
                                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                                modifier = Modifier.size(15.dp)
                                                            )
                                                        }
                                                        IconButton(
                                                            onClick = { onDeleteCategory(sub.id) },
                                                            modifier = Modifier.size(28.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Delete,
                                                                contentDescription = "Delete ${sub.name}",
                                                                tint = semanticColors.expenseCoral,
                                                                modifier = Modifier.size(15.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            TextButton(
                                                onClick = { onAddSubcategory(parent) },
                                                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Add Subcategory", style = MaterialTheme.typography.labelMedium)
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