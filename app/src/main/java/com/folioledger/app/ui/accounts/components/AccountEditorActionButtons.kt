package com.folioledger.app.ui.accounts.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.folioledger.app.ui.theme.LocalFolioSemanticColors

@Composable
fun AccountEditorActionButtons(
    isEditing: Boolean,
    isArchived: Boolean,
    isSaving: Boolean,
    onDelete: () -> Unit,
    onToggleArchive: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val semanticColors = LocalFolioSemanticColors.current

    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        if (isEditing) {
            TextButton(
                onClick = onDelete,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = semanticColors.expenseCoral
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Delete", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onToggleArchive,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (isArchived) {
                        Icons.Filled.Unarchive
                    } else {
                        Icons.Filled.Archive
                    },
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isArchived) "Unarchive" else "Archive")
            }
        }

        Button(
            onClick = onSave,
            enabled = !isSaving,
            modifier = Modifier.weight(1f)
        ) {
            Text(if (isEditing) "Save Changes" else "Create Account")
        }
    }
}
