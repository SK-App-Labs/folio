// File: app/src/main/java/com/folioledger/app/ui/iconpack/FolioIconPackTheme.kt
package com.folioledger.app.ui.iconpack

import androidx.compose.runtime.staticCompositionLocalOf

enum class FolioIconPackTheme(
    val key: String,
    val displayName: String,
    val subtitle: String
) {
    MATERIAL_FILLED(
        key = "MATERIAL_FILLED",
        displayName = "Material Classic",
        subtitle = "Solid, bold silhouettes natively styled for Android"
    ),
    PHOSPHOR_ROUNDED(
        key = "PHOSPHOR_ROUNDED",
        displayName = "Phosphor Modern",
        subtitle = "Contemporary rounded silhouettes with a friendly fintech feel"
    ),
    LUCIDE_OUTLINE(
        key = "LUCIDE_OUTLINE",
        displayName = "Lucide Minimalist",
        subtitle = "Crisp, refined 2px stroke vectors for a modern neobank look"
    ),
    EMOJI_EXPRESSIVE(
        key = "EMOJI_EXPRESSIVE",
        displayName = "Emoji Expressive",
        subtitle = "Vibrant full-color emojis on modern squircle badges"
    );

    val isOutlined: Boolean
        get() = this == LUCIDE_OUTLINE

    val isEmoji: Boolean
        get() = this == EMOJI_EXPRESSIVE

    val isSquircle: Boolean
        get() = this == EMOJI_EXPRESSIVE

    companion object {
        fun fromKey(key: String?): FolioIconPackTheme =
            entries.firstOrNull { it.key.equals(key?.trim(), ignoreCase = true) } ?: MATERIAL_FILLED
    }
}

val LocalFolioIconPackTheme = staticCompositionLocalOf { FolioIconPackTheme.MATERIAL_FILLED }
