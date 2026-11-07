package com.folioledger.app.ui.settings.sheets.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.folioledger.app.domain.model.CategoryNode
import com.folioledger.app.ui.components.CategoryIconBadge
import com.folioledger.app.ui.theme.LocalFolioSemanticColors
import com.folioledger.app.ui.theme.tabularMoneyStyle

/**
 * Accordion card item for a main category, supporting subcategory expansion,
 * action menus (Add Subcategory, Edit, Delete), and subcategory management rows.
 */
@Composable
fun CategoryParentCard(
    parent: CategoryNode,
    subcategoriesEnabled: Boolean,
    isExpanded: Boolean,
    isSearching: Boolean,
    onToggleExpand: () -> Unit,
    onAddSubcategory: (CategoryNode) -> Unit,
    onEditCategory: (CategoryNode, String?) -> Unit,
    onDeleteCategory: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val semanticColors = LocalFolioSemanticColors.current
    var showMenu by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
        modifier = modifier.fillMaxWidth()
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
                                onToggleExpand()
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
