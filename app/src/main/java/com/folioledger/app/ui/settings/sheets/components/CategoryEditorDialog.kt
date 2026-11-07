package com.folioledger.app.ui.settings.sheets.components

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.folioledger.app.ui.components.ALL_CATEGORY_ICON_KEYS
import com.folioledger.app.ui.components.CategoryIconBadge
import com.folioledger.app.ui.components.parseHexColorSafely
import com.folioledger.app.ui.settings.CategoryEditorDialogState
import com.folioledger.app.ui.theme.LocalFolioSemanticColors

/**
 * Dialog for creating or editing main categories and subcategories,
 * featuring color swatches, icon selection grid, and smart icon suggestions.
 */
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
