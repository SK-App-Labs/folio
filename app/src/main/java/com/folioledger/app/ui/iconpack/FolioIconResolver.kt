// File: app/src/main/java/com/folioledger/app/ui/iconpack/FolioIconResolver.kt
package com.folioledger.app.ui.iconpack

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.ui.graphics.vector.ImageVector
import java.util.Locale

object FolioIconResolver {

    fun resolve(iconName: String?, theme: FolioIconPackTheme): ImageVector {
        if (iconName.isNullOrBlank()) return Icons.Filled.Category
        val key = iconName.trim().lowercase(Locale.US)

        return when (theme) {
            FolioIconPackTheme.MATERIAL_FILLED -> MaterialIconResolver.resolve(key)
            FolioIconPackTheme.PHOSPHOR_ROUNDED -> PhosphorIconResolver.resolve(key) ?: MaterialIconResolver.resolve(key)
            FolioIconPackTheme.LUCIDE_OUTLINE -> LucideIconResolver.resolve(key) ?: MaterialIconResolver.resolve(key)
            FolioIconPackTheme.EMOJI_EXPRESSIVE -> MaterialIconResolver.resolve(key)
        }
    }
}
