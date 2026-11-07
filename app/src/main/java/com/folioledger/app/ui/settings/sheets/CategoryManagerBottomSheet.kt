package com.folioledger.app.ui.settings.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.folioledger.app.domain.model.CategoryNode
import com.folioledger.app.domain.model.CategoryType
import com.folioledger.app.domain.util.CategoryDrillDownCalculator
import com.folioledger.app.ui.settings.CategoryEditorDialogState
import com.folioledger.app.ui.settings.SettingsUiState
import com.folioledger.app.ui.settings.sheets.components.CategoryEditorDialog as ComponentCategoryEditorDialog
import com.folioledger.app.ui.settings.sheets.components.CategoryParentCard

/**
 * Backward-compatible forwarding delegator for [ComponentCategoryEditorDialog].
 */
@Composable
fun CategoryEditorDialog(
    state: CategoryEditorDialogState,
    onNameChange: (String) -> Unit,
    onUpdateState: ((CategoryEditorDialogState) -> CategoryEditorDialogState) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    ComponentCategoryEditorDialog(
        state = state,
        onNameChange = onNameChange,
        onUpdateState = onUpdateState,
        onSave = onSave,
        onDelete = onDelete,
        onDismiss = onDismiss
    )
}

/**
 * Bottom sheet dedicated to managing categories and hierarchical subcategories.
 */
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
    val categories = uiState.activeManagedCategories
    val subcategoriesEnabled = uiState.userPreferences.subcategoriesEnabled

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var expandedCategoryIds by rememberSaveable { mutableStateOf(setOf<Long>()) }

    // Filter categories dynamically using domain utility
    val filteredCategories = remember(categories, searchQuery) {
        CategoryDrillDownCalculator.filterCategoryTree(categories, searchQuery)
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
                        val isSearching = searchQuery.isNotBlank()
                        val isExpanded = isSearching || expandedCategoryIds.contains(parent.id)

                        CategoryParentCard(
                            parent = parent,
                            subcategoriesEnabled = subcategoriesEnabled,
                            isExpanded = isExpanded,
                            isSearching = isSearching,
                            onToggleExpand = {
                                expandedCategoryIds = if (isExpanded) {
                                    expandedCategoryIds - parent.id
                                } else {
                                    expandedCategoryIds + parent.id
                                }
                            },
                            onAddSubcategory = onAddSubcategory,
                            onEditCategory = onEditCategory,
                            onDeleteCategory = onDeleteCategory
                        )
                    }
                }
            }
        }
    }
}
