package com.folioledger.app.ui.entry.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.folioledger.app.domain.model.CategoryNode
import com.folioledger.app.domain.model.CategoryType
import com.folioledger.app.ui.components.CategoryIconBadge
import com.folioledger.app.ui.theme.LocalFolioSemanticColors

@Composable
fun DockedCategoryPicker(
    categories: List<CategoryNode>,
    selectedCategoryId: Long?,
    categoryType: CategoryType = CategoryType.EXPENSE,
    onCategorySelected: (CategoryNode, String?) -> Unit,
    onCreateNewCategoryClick: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val semanticColors = LocalFolioSemanticColors.current

    val stopParentScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                // Consume unconsumed vertical scroll so it NEVER passes up to parent ModalBottomSheet!
                return Offset(0f, available.y)
            }
        }
    }

    // Active selected parent node for split view
    var activeParentId by remember(categories, selectedCategoryId) {
        val initialParent = categories.firstOrNull { parent ->
            parent.id == selectedCategoryId || parent.subcategories.any { it.id == selectedCategoryId }
        } ?: categories.firstOrNull()
        mutableStateOf(initialParent?.id)
    }

    val activeParent = categories.firstOrNull { it.id == activeParentId } ?: categories.firstOrNull()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .nestedScroll(stopParentScrollConnection)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        // Dock Header: Title & Close Button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
        ) {
            Text(
                text = if (categoryType == CategoryType.INCOME) "Income Category" else "Expense Category",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(
                    onClick = onCreateNewCategoryClick,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("New", style = MaterialTheme.typography.labelMedium)
                }

                IconButton(
                    onClick = onCloseClick,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Dock",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Split-Pane List (Left: Parent Categories | Right: Subcategories & Main Option)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // Left Pane: Parent Categories
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxHeight()
                    .padding(end = 4.dp)
            ) {
                items(categories, key = { it.id }) { parent ->
                    val isParentActive = parent.id == activeParentId
                    val isParentSelected = parent.id == selectedCategoryId

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isParentActive) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        },
                        border = BorderStroke(
                            1.dp,
                            if (isParentActive) MaterialTheme.colorScheme.primary else semanticColors.cardStrokeSubtle
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                // Tapping left pane activates this parent to inspect subcategories on the right
                                activeParentId = parent.id
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                        ) {
                            CategoryIconBadge(
                                iconName = parent.iconName,
                                colorHex = parent.colorHex,
                                size = 26.dp,
                                iconSize = 14.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = parent.name,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isParentActive) FontWeight.Bold else FontWeight.Medium,
                                color = if (isParentActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            if (isParentSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Right Pane: Main Parent Option & Subcategories under Active Parent
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(start = 4.dp)
            ) {
                if (activeParent != null) {
                    // Top Option: Select the Main Parent Category directly (e.g. "Dining (Main)")
                    item(key = "main_${activeParent.id}") {
                        val isMainSelected = activeParent.id == selectedCategoryId
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isMainSelected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceContainer
                            },
                            border = BorderStroke(
                                1.dp,
                                if (isMainSelected) MaterialTheme.colorScheme.primary else semanticColors.cardStrokeSubtle
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onCategorySelected(activeParent, null)
                                }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                            ) {
                                CategoryIconBadge(
                                    iconName = activeParent.iconName,
                                    colorHex = activeParent.colorHex,
                                    size = 24.dp,
                                    iconSize = 13.dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${activeParent.name} (Main)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isMainSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                if (isMainSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Subcategories List under Active Parent
                    val subs = activeParent.subcategories
                    items(subs, key = { it.id }) { sub ->
                        val isSubSelected = sub.id == selectedCategoryId
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSubSelected) {
                                MaterialTheme.colorScheme.secondaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerLow
                            },
                            border = BorderStroke(
                                1.dp,
                                if (isSubSelected) MaterialTheme.colorScheme.secondary else semanticColors.cardStrokeSubtle
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onCategorySelected(sub, activeParent.name)
                                }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                            ) {
                                CategoryIconBadge(
                                    iconName = sub.iconName,
                                    colorHex = sub.colorHex,
                                    size = 24.dp,
                                    iconSize = 13.dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = sub.name,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSubSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSubSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                if (isSubSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(14.dp)
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
