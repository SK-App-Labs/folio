package com.folioledger.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

enum class FolioThemeMode(val key: String, val displayName: String) {
    SYSTEM("SYSTEM", "System Default"),
    LIGHT("LIGHT", "Light"),
    SEPIA("SEPIA", "Warm Sand"),
    SLATE_GRAY("SLATE_GRAY", "Charcoal Gray"),
    DARK("DARK", "Midnight Navy"),
    AMOLED_BLACK("AMOLED_BLACK", "AMOLED Pitch Black");

    companion object {
        fun fromKey(key: String): FolioThemeMode =
            entries.firstOrNull { it.key.equals(key.trim(), ignoreCase = true) } ?: SYSTEM
    }
}

enum class FolioAccentPalette(
    val key: String,
    val displayName: String,
    val seedHex: String,
    val previewColor: Color,
) {
    EMERALD("EMERALD", "Emerald Ledger", "#059669", Color(0xFF059669)),
    COBALT("COBALT", "Electric Cobalt", "#2563EB", Color(0xFF2563EB)),
    INDIGO("INDIGO", "Royal Indigo", "#4F46E5", Color(0xFF4F46E5)),
    PURPLE("PURPLE", "Vivid Violet", "#7C3AED", Color(0xFF7C3AED)),
    ROSE("ROSE", "Rose Gold", "#F43F5E", Color(0xFFF43F5E)),
    LIME("LIME", "Fresh Lime", "#65A30D", Color(0xFF65A30D)),
    CYAN("CYAN", "Electric Cyan", "#06B6D4", Color(0xFF06B6D4)),
    BRONZE("BRONZE", "Golden Bronze", "#B45309", Color(0xFFB45309)),
    TEAL("TEAL", "Oceanic Teal", "#0D9488", Color(0xFF0D9488)),
    CRIMSON("CRIMSON", "Ruby Crimson", "#E11D48", Color(0xFFE11D48)),
    AMBER("AMBER", "Saffron Amber", "#D97706", Color(0xFFD97706)),
    SLATE("SLATE", "Monochrome Slate", "#475569", Color(0xFF475569));

    companion object {
        fun fromKey(key: String): FolioAccentPalette =
            entries.firstOrNull { it.key.equals(key.trim(), ignoreCase = true) } ?: EMERALD
    }
}

@Immutable
data class FolioSemanticColors(
    val incomeEmerald: Color,
    val incomeEmeraldContainer: Color,
    val onIncomeEmeraldContainer: Color,
    val expenseCoral: Color,
    val expenseCoralContainer: Color,
    val onExpenseCoralContainer: Color,
    val transferBlue: Color,
    val transferBlueContainer: Color,
    val onTransferBlueContainer: Color,
    val warningAmber: Color,
    val warningAmberContainer: Color,
    val onWarningAmberContainer: Color,
    val neutralInk: Color,
    val subtleText: Color,
    val chartGridLine: Color,
    val cardStrokeSubtle: Color,
    val isDark: Boolean,
    val isAmoled: Boolean,
)

val LightFolioSemanticColors = FolioSemanticColors(
    incomeEmerald = Color(0xFF059669),
    incomeEmeraldContainer = Color(0xFFD1FAE5),
    onIncomeEmeraldContainer = Color(0xFF064E3B),
    expenseCoral = Color(0xFFE11D48),
    expenseCoralContainer = Color(0xFFFFE4E6),
    onExpenseCoralContainer = Color(0xFF881337),
    transferBlue = Color(0xFF2563EB),
    transferBlueContainer = Color(0xFFDBEAFE),
    onTransferBlueContainer = Color(0xFF1E3A8A),
    warningAmber = Color(0xFFD97706),
    warningAmberContainer = Color(0xFFFEF3C7),
    onWarningAmberContainer = Color(0xFF78350F),
    neutralInk = Color(0xFF0F172A),
    subtleText = Color(0xFF64748B),
    chartGridLine = Color(0xFFE2E8F0),
    cardStrokeSubtle = Color(0xFFE2E8F0),
    isDark = false,
    isAmoled = false,
)

val DarkFolioSemanticColors = FolioSemanticColors(
    incomeEmerald = Color(0xFF34D399),
    incomeEmeraldContainer = Color(0xFF064E3B),
    onIncomeEmeraldContainer = Color(0xFFA7F3D0),
    expenseCoral = Color(0xFFFB7185),
    expenseCoralContainer = Color(0xFF881337),
    onExpenseCoralContainer = Color(0xFFFECDD3),
    transferBlue = Color(0xFF60A5FA),
    transferBlueContainer = Color(0xFF1E3A8A),
    onTransferBlueContainer = Color(0xFFBFDBFE),
    warningAmber = Color(0xFFFBBF24),
    warningAmberContainer = Color(0xFF78350F),
    onWarningAmberContainer = Color(0xFFFDE68A),
    neutralInk = Color(0xFFF8FAFC),
    subtleText = Color(0xFF94A3B8),
    chartGridLine = Color(0xFF1E293B),
    cardStrokeSubtle = Color(0xFF1E293B),
    isDark = true,
    isAmoled = false,
)

val AmoledFolioSemanticColors = DarkFolioSemanticColors.copy(
    chartGridLine = Color(0xFF171717),
    cardStrokeSubtle = Color(0xFF1F1F1F),
    isAmoled = true,
)

val SepiaFolioSemanticColors = LightFolioSemanticColors.copy(
    chartGridLine = Color(0xFFD8D0BE),
    cardStrokeSubtle = Color(0xFFD8D0BE),
)

val SlateGrayFolioSemanticColors = DarkFolioSemanticColors.copy(
    chartGridLine = Color(0xFF3C3F43),
    cardStrokeSubtle = Color(0xFF3C3F43),
)

val LocalFolioSemanticColors = staticCompositionLocalOf { LightFolioSemanticColors }

private fun buildLightSchemeForAccent(accent: FolioAccentPalette): ColorScheme {
    val baseBackground = Color(0xFFF8FAFC)
    val baseSurface = Color(0xFFFFFFFF)
    val baseSurfaceVariant = Color(0xFFF1F5F9)
    val onBackground = Color(0xFF0F172A)
    val onSurfaceVariant = Color(0xFF475569)
    val outline = Color(0xFF94A3B8)
    val outlineVariant = Color(0xFFCBD5E1)

    return when (accent) {
        FolioAccentPalette.EMERALD -> lightColorScheme(
            primary = Color(0xFF059669),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFD1FAE5),
            onPrimaryContainer = Color(0xFF064E3B),
            secondary = Color(0xFF0F766E),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFCCFBF1),
            onSecondaryContainer = Color(0xFF134E4A),
            tertiary = Color(0xFF0284C7),
            onTertiary = Color.White,
            tertiaryContainer = Color(0xFFE0F2FE),
            onTertiaryContainer = Color(0xFF0C4A6E),
            background = baseBackground,
            onBackground = onBackground,
            surface = baseSurface,
            onSurface = onBackground,
            surfaceVariant = baseSurfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            surfaceContainerLowest = Color.White,
            surfaceContainerLow = Color(0xFFF8FAFC),
            surfaceContainer = Color(0xFFF1F5F9),
            surfaceContainerHigh = Color(0xFFE2E8F0),
            surfaceContainerHighest = Color(0xFFE2E8F0),
            outline = outline,
            outlineVariant = outlineVariant,
        )

        FolioAccentPalette.COBALT -> lightColorScheme(
            primary = Color(0xFF2563EB),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFDBEAFE),
            onPrimaryContainer = Color(0xFF1E3A8A),
            secondary = Color(0xFF1D4ED8),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFEFF6FF),
            onSecondaryContainer = Color(0xFF172554),
            tertiary = Color(0xFF0D9488),
            onTertiary = Color.White,
            tertiaryContainer = Color(0xFFCCFBF1),
            onTertiaryContainer = Color(0xFF134E4A),
            background = baseBackground,
            onBackground = onBackground,
            surface = baseSurface,
            onSurface = onBackground,
            surfaceVariant = baseSurfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            surfaceContainerLowest = Color.White,
            surfaceContainerLow = Color(0xFFF8FAFC),
            surfaceContainer = Color(0xFFF1F5F9),
            surfaceContainerHigh = Color(0xFFE2E8F0),
            surfaceContainerHighest = Color(0xFFE2E8F0),
            outline = outline,
            outlineVariant = outlineVariant,
        )

        FolioAccentPalette.INDIGO -> lightColorScheme(
            primary = Color(0xFF4F46E5),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFE0E7FF),
            onPrimaryContainer = Color(0xFF1E1B4B),
            secondary = Color(0xFF4338CA),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFEEF2FF),
            onSecondaryContainer = Color(0xFF312E81),
            tertiary = Color(0xFF0891B2),
            onTertiary = Color.White,
            tertiaryContainer = Color(0xFFCFFAFE),
            onTertiaryContainer = Color(0xFF164E63),
            background = baseBackground,
            onBackground = onBackground,
            surface = baseSurface,
            onSurface = onBackground,
            surfaceVariant = baseSurfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            surfaceContainerLowest = Color.White,
            surfaceContainerLow = Color(0xFFF8FAFC),
            surfaceContainer = Color(0xFFF1F5F9),
            surfaceContainerHigh = Color(0xFFE2E8F0),
            surfaceContainerHighest = Color(0xFFE2E8F0),
            outline = outline,
            outlineVariant = outlineVariant,
        )

        FolioAccentPalette.PURPLE -> lightColorScheme(
            primary = Color(0xFF7C3AED),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFEDE9FE),
            onPrimaryContainer = Color(0xFF4C1D95),
            secondary = Color(0xFF6D28D9),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFF5F3FF),
            onSecondaryContainer = Color(0xFF3B0764),
            tertiary = Color(0xFF2563EB),
            onTertiary = Color.White,
            tertiaryContainer = Color(0xFFDBEAFE),
            onTertiaryContainer = Color(0xFF1E3A8A),
            background = baseBackground,
            onBackground = onBackground,
            surface = baseSurface,
            onSurface = onBackground,
            surfaceVariant = baseSurfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            surfaceContainerLowest = Color.White,
            surfaceContainerLow = Color(0xFFF8FAFC),
            surfaceContainer = Color(0xFFF1F5F9),
            surfaceContainerHigh = Color(0xFFE2E8F0),
            surfaceContainerHighest = Color(0xFFE2E8F0),
            outline = outline,
            outlineVariant = outlineVariant,
        )

        FolioAccentPalette.ROSE -> lightColorScheme(
            primary = Color(0xFFF43F5E),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFFFE4E6),
            onPrimaryContainer = Color(0xFF881337),
            secondary = Color(0xFFE11D48),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFFECDD3),
            onSecondaryContainer = Color(0xFF4C0519),
            tertiary = Color(0xFF2563EB),
            onTertiary = Color.White,
            tertiaryContainer = Color(0xFFDBEAFE),
            onTertiaryContainer = Color(0xFF1E3A8A),
            background = baseBackground,
            onBackground = onBackground,
            surface = baseSurface,
            onSurface = onBackground,
            surfaceVariant = baseSurfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            surfaceContainerLowest = Color.White,
            surfaceContainerLow = Color(0xFFF8FAFC),
            surfaceContainer = Color(0xFFF1F5F9),
            surfaceContainerHigh = Color(0xFFE2E8F0),
            surfaceContainerHighest = Color(0xFFE2E8F0),
            outline = outline,
            outlineVariant = outlineVariant,
        )

        FolioAccentPalette.LIME -> lightColorScheme(
            primary = Color(0xFF65A30D),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFECFCCB),
            onPrimaryContainer = Color(0xFF1A2E05),
            secondary = Color(0xFF4D7C0F),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFF7FEE7),
            onSecondaryContainer = Color(0xFF365314),
            tertiary = Color(0xFF0284C7),
            onTertiary = Color.White,
            tertiaryContainer = Color(0xFFE0F2FE),
            onTertiaryContainer = Color(0xFF0C4A6E),
            background = baseBackground,
            onBackground = onBackground,
            surface = baseSurface,
            onSurface = onBackground,
            surfaceVariant = baseSurfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            surfaceContainerLowest = Color.White,
            surfaceContainerLow = Color(0xFFF8FAFC),
            surfaceContainer = Color(0xFFF1F5F9),
            surfaceContainerHigh = Color(0xFFE2E8F0),
            surfaceContainerHighest = Color(0xFFE2E8F0),
            outline = outline,
            outlineVariant = outlineVariant,
        )

        FolioAccentPalette.CYAN -> lightColorScheme(
            primary = Color(0xFF0891B2),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFCFFAFE),
            onPrimaryContainer = Color(0xFF164E63),
            secondary = Color(0xFF0284C7),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFE0F2FE),
            onSecondaryContainer = Color(0xFF0C4A6E),
            tertiary = Color(0xFF4F46E5),
            onTertiary = Color.White,
            tertiaryContainer = Color(0xFFE0E7FF),
            onTertiaryContainer = Color(0xFF1E1B4B),
            background = baseBackground,
            onBackground = onBackground,
            surface = baseSurface,
            onSurface = onBackground,
            surfaceVariant = baseSurfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            surfaceContainerLowest = Color.White,
            surfaceContainerLow = Color(0xFFF8FAFC),
            surfaceContainer = Color(0xFFF1F5F9),
            surfaceContainerHigh = Color(0xFFE2E8F0),
            surfaceContainerHighest = Color(0xFFE2E8F0),
            outline = outline,
            outlineVariant = outlineVariant,
        )

        FolioAccentPalette.BRONZE -> lightColorScheme(
            primary = Color(0xFFB45309),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFFEF3C7),
            onPrimaryContainer = Color(0xFF78350F),
            secondary = Color(0xFF92400E),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFFDE68A),
            onSecondaryContainer = Color(0xFF451A03),
            tertiary = Color(0xFF059669),
            onTertiary = Color.White,
            tertiaryContainer = Color(0xFFD1FAE5),
            onTertiaryContainer = Color(0xFF064E3B),
            background = baseBackground,
            onBackground = onBackground,
            surface = baseSurface,
            onSurface = onBackground,
            surfaceVariant = baseSurfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            surfaceContainerLowest = Color.White,
            surfaceContainerLow = Color(0xFFF8FAFC),
            surfaceContainer = Color(0xFFF1F5F9),
            surfaceContainerHigh = Color(0xFFE2E8F0),
            surfaceContainerHighest = Color(0xFFE2E8F0),
            outline = outline,
            outlineVariant = outlineVariant,
        )

        FolioAccentPalette.AMBER -> lightColorScheme(
            primary = Color(0xFFD97706),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFFEF3C7),
            onPrimaryContainer = Color(0xFF78350F),
            secondary = Color(0xFFB45309),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFFDE68A),
            onSecondaryContainer = Color(0xFF451A03),
            tertiary = Color(0xFF059669),
            onTertiary = Color.White,
            tertiaryContainer = Color(0xFFD1FAE5),
            onTertiaryContainer = Color(0xFF064E3B),
            background = baseBackground,
            onBackground = onBackground,
            surface = baseSurface,
            onSurface = onBackground,
            surfaceVariant = baseSurfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            surfaceContainerLowest = Color.White,
            surfaceContainerLow = Color(0xFFF8FAFC),
            surfaceContainer = Color(0xFFF1F5F9),
            surfaceContainerHigh = Color(0xFFE2E8F0),
            surfaceContainerHighest = Color(0xFFE2E8F0),
            outline = outline,
            outlineVariant = outlineVariant,
        )

        FolioAccentPalette.TEAL -> lightColorScheme(
            primary = Color(0xFF0D9488),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFCCFBF1),
            onPrimaryContainer = Color(0xFF134E4A),
            secondary = Color(0xFF0284C7),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFE0F2FE),
            onSecondaryContainer = Color(0xFF0C4A6E),
            tertiary = Color(0xFF4F46E5),
            onTertiary = Color.White,
            tertiaryContainer = Color(0xFFE0E7FF),
            onTertiaryContainer = Color(0xFF1E1B4B),
            background = baseBackground,
            onBackground = onBackground,
            surface = baseSurface,
            onSurface = onBackground,
            surfaceVariant = baseSurfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            surfaceContainerLowest = Color.White,
            surfaceContainerLow = Color(0xFFF8FAFC),
            surfaceContainer = Color(0xFFF1F5F9),
            surfaceContainerHigh = Color(0xFFE2E8F0),
            surfaceContainerHighest = Color(0xFFE2E8F0),
            outline = outline,
            outlineVariant = outlineVariant
        )

        FolioAccentPalette.CRIMSON -> lightColorScheme(
            primary = Color(0xFFE11D48),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFFFE4E6),
            onPrimaryContainer = Color(0xFF881337),
            secondary = Color(0xFFBE123C),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFFECDD3),
            onSecondaryContainer = Color(0xFF4C0519),
            tertiary = Color(0xFF2563EB),
            onTertiary = Color.White,
            tertiaryContainer = Color(0xFFDBEAFE),
            onTertiaryContainer = Color(0xFF1E3A8A),
            background = baseBackground,
            onBackground = onBackground,
            surface = baseSurface,
            onSurface = onBackground,
            surfaceVariant = baseSurfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            surfaceContainerLowest = Color.White,
            surfaceContainerLow = Color(0xFFF8FAFC),
            surfaceContainer = Color(0xFFF1F5F9),
            surfaceContainerHigh = Color(0xFFE2E8F0),
            surfaceContainerHighest = Color(0xFFE2E8F0),
            outline = outline,
            outlineVariant = outlineVariant
        )

        FolioAccentPalette.SLATE -> lightColorScheme(
            primary = Color(0xFF334155),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFE2E8F0),
            onPrimaryContainer = Color(0xFF0F172A),
            secondary = Color(0xFF475569),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFF1F5F9),
            onSecondaryContainer = Color(0xFF1E293B),
            tertiary = Color(0xFF0D9488),
            onTertiary = Color.White,
            tertiaryContainer = Color(0xFFCCFBF1),
            onTertiaryContainer = Color(0xFF134E4A),
            background = baseBackground,
            onBackground = onBackground,
            surface = baseSurface,
            onSurface = onBackground,
            surfaceVariant = baseSurfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            surfaceContainerLowest = Color.White,
            surfaceContainerLow = Color(0xFFF8FAFC),
            surfaceContainer = Color(0xFFF1F5F9),
            surfaceContainerHigh = Color(0xFFE2E8F0),
            surfaceContainerHighest = Color(0xFFE2E8F0),
            outline = outline,
            outlineVariant = outlineVariant
        )
    }
}

private fun buildDarkSchemeForAccent(
    accent: FolioAccentPalette,
    amoledPitchBlack: Boolean,
): ColorScheme {
    val background = if (amoledPitchBlack) Color(0xFF000000) else Color(0xFF0B1120)
    val surface = if (amoledPitchBlack) Color(0xFF000000) else Color(0xFF0F172A)
    val surfaceVariant = if (amoledPitchBlack) Color(0xFF121212) else Color(0xFF1E293B)
    val surfaceLowest = if (amoledPitchBlack) Color(0xFF000000) else Color(0xFF070B14)
    val surfaceLow = if (amoledPitchBlack) Color(0xFF080808) else Color(0xFF0F172A)
    val surfaceContainer = if (amoledPitchBlack) Color(0xFF0E0E0E) else Color(0xFF1E293B)
    val surfaceHigh = if (amoledPitchBlack) Color(0xFF161616) else Color(0xFF283548)
    val surfaceHighest = if (amoledPitchBlack) Color(0xFF222222) else Color(0xFF334155)
    val onBackground = Color(0xFFF8FAFC)
    val onSurfaceVariant = Color(0xFF94A3B8)
    val outline = if (amoledPitchBlack) Color(0xFF2E2E2E) else Color(0xFF475569)
    val outlineVariant = if (amoledPitchBlack) Color(0xFF1F1F1F) else Color(0xFF1E293B)

    val baseScheme = when (accent) {
        FolioAccentPalette.EMERALD -> darkColorScheme(
            primary = Color(0xFF34D399),
            onPrimary = Color(0xFF022C22),
            primaryContainer = Color(0xFF065F46),
            onPrimaryContainer = Color(0xFFD1FAE5),
            secondary = Color(0xFF2DD4BF),
            onSecondary = Color(0xFF042F2E),
            secondaryContainer = Color(0xFF115E59),
            onSecondaryContainer = Color(0xFFCCFBF1),
            tertiary = Color(0xFF38BDF8),
            onTertiary = Color(0xFF082F49),
            tertiaryContainer = Color(0xFF0369A1),
            onTertiaryContainer = Color(0xFFE0F2FE)
        )

        FolioAccentPalette.COBALT -> darkColorScheme(
            primary = Color(0xFF60A5FA),
            onPrimary = Color(0xFF172554),
            primaryContainer = Color(0xFF1E40AF),
            onPrimaryContainer = Color(0xFFDBEAFE),
            secondary = Color(0xFF93C5FD),
            onSecondary = Color(0xFF1E3A8A),
            secondaryContainer = Color(0xFF1E3A8A),
            onSecondaryContainer = Color(0xFFEFF6FF),
            tertiary = Color(0xFF2DD4BF),
            onTertiary = Color(0xFF042F2E),
            tertiaryContainer = Color(0xFF115E59),
            onTertiaryContainer = Color(0xFFCCFBF1)
        )

        FolioAccentPalette.PURPLE -> darkColorScheme(
            primary = Color(0xFFA78BFA),
            onPrimary = Color(0xFF3B0764),
            primaryContainer = Color(0xFF5B21B6),
            onPrimaryContainer = Color(0xFFEDE9FE),
            secondary = Color(0xFFC4B5FD),
            onSecondary = Color(0xFF4C1D95),
            secondaryContainer = Color(0xFF4C1D95),
            onSecondaryContainer = Color(0xFFF5F3FF),
            tertiary = Color(0xFF38BDF8),
            onTertiary = Color(0xFF082F49),
            tertiaryContainer = Color(0xFF0369A1),
            onTertiaryContainer = Color(0xFFE0F2FE)
        )

        FolioAccentPalette.ROSE -> darkColorScheme(
            primary = Color(0xFFFB7185),
            onPrimary = Color(0xFF4C0519),
            primaryContainer = Color(0xFF881337),
            onPrimaryContainer = Color(0xFFFFE4E6),
            secondary = Color(0xFFFDA4AF),
            onSecondary = Color(0xFF881337),
            secondaryContainer = Color(0xFF9F1239),
            onSecondaryContainer = Color(0xFFFECDD3),
            tertiary = Color(0xFF38BDF8),
            onTertiary = Color(0xFF082F49),
            tertiaryContainer = Color(0xFF0369A1),
            onTertiaryContainer = Color(0xFFE0F2FE)
        )

        FolioAccentPalette.LIME -> darkColorScheme(
            primary = Color(0xFFA3E635),
            onPrimary = Color(0xFF1A2E05),
            primaryContainer = Color(0xFF365314),
            onPrimaryContainer = Color(0xFFECFCCB),
            secondary = Color(0xFFBEF264),
            onSecondary = Color(0xFF365314),
            secondaryContainer = Color(0xFF4D7C0F),
            onSecondaryContainer = Color(0xFFF7FEE7),
            tertiary = Color(0xFF38BDF8),
            onTertiary = Color(0xFF082F49),
            tertiaryContainer = Color(0xFF0369A1),
            onTertiaryContainer = Color(0xFFE0F2FE)
        )

        FolioAccentPalette.CYAN -> darkColorScheme(
            primary = Color(0xFF22D3EE),
            onPrimary = Color(0xFF083344),
            primaryContainer = Color(0xFF155E75),
            onPrimaryContainer = Color(0xFFCFFAFE),
            secondary = Color(0xFF67E8F9),
            onSecondary = Color(0xFF164E63),
            secondaryContainer = Color(0xFF0891B2),
            onSecondaryContainer = Color(0xFFE0F2FE),
            tertiary = Color(0xFF818CF8),
            onTertiary = Color(0xFF1E1B4B),
            tertiaryContainer = Color(0xFF3730A3),
            onTertiaryContainer = Color(0xFFE0E7FF)
        )

        FolioAccentPalette.BRONZE -> darkColorScheme(
            primary = Color(0xFFFBBF24),
            onPrimary = Color(0xFF451A03),
            primaryContainer = Color(0xFF78350F),
            onPrimaryContainer = Color(0xFFFEF3C7),
            secondary = Color(0xFFF59E0B),
            onSecondary = Color(0xFF451A03),
            secondaryContainer = Color(0xFF92400E),
            onSecondaryContainer = Color(0xFFFDE68A),
            tertiary = Color(0xFF34D399),
            onTertiary = Color(0xFF022C22),
            tertiaryContainer = Color(0xFF065F46),
            onTertiaryContainer = Color(0xFFD1FAE5)
        )

        FolioAccentPalette.INDIGO -> darkColorScheme(
            primary = Color(0xFF818CF8),
            onPrimary = Color(0xFF1E1B4B),
            primaryContainer = Color(0xFF3730A3),
            onPrimaryContainer = Color(0xFFE0E7FF),
            secondary = Color(0xFFA5B4FC),
            onSecondary = Color(0xFF1E1B4B),
            secondaryContainer = Color(0xFF312E81),
            onSecondaryContainer = Color(0xFFEEF2FF),
            tertiary = Color(0xFF22D3EE),
            onTertiary = Color(0xFF083344),
            tertiaryContainer = Color(0xFF155E75),
            onTertiaryContainer = Color(0xFFCFFAFE),
        )

        FolioAccentPalette.AMBER -> darkColorScheme(
            primary = Color(0xFFFBBF24),
            onPrimary = Color(0xFF451A03),
            primaryContainer = Color(0xFF92400E),
            onPrimaryContainer = Color(0xFFFEF3C7),
            secondary = Color(0xFFF59E0B),
            onSecondary = Color(0xFF451A03),
            secondaryContainer = Color(0xFF78350F),
            onSecondaryContainer = Color(0xFFFDE68A),
            tertiary = Color(0xFF34D399),
            onTertiary = Color(0xFF022C22),
            tertiaryContainer = Color(0xFF065F46),
            onTertiaryContainer = Color(0xFFD1FAE5),
        )

        FolioAccentPalette.TEAL -> darkColorScheme(
            primary = Color(0xFF2DD4BF),
            onPrimary = Color(0xFF042F2E),
            primaryContainer = Color(0xFF115E59),
            onPrimaryContainer = Color(0xFFCCFBF1),
            secondary = Color(0xFF38BDF8),
            onSecondary = Color(0xFF082F49),
            secondaryContainer = Color(0xFF0369A1),
            onSecondaryContainer = Color(0xFFE0F2FE),
            tertiary = Color(0xFF818CF8),
            onTertiary = Color(0xFF1E1B4B),
            tertiaryContainer = Color(0xFF3730A3),
            onTertiaryContainer = Color(0xFFE0E7FF),
        )

        FolioAccentPalette.CRIMSON -> darkColorScheme(
            primary = Color(0xFFFB7185),
            onPrimary = Color(0xFF4C0519),
            primaryContainer = Color(0xFF9F1239),
            onPrimaryContainer = Color(0xFFFFE4E6),
            secondary = Color(0xFFF43F5E),
            onSecondary = Color(0xFF4C0519),
            secondaryContainer = Color(0xFF881337),
            onSecondaryContainer = Color(0xFFFECDD3),
            tertiary = Color(0xFF60A5FA),
            onTertiary = Color(0xFF172554),
            tertiaryContainer = Color(0xFF1D4ED8),
            onTertiaryContainer = Color(0xFFDBEAFE),
        )

        FolioAccentPalette.SLATE -> darkColorScheme(
            primary = Color(0xFFCBD5E1),
            onPrimary = Color(0xFF0F172A),
            primaryContainer = Color(0xFF334155),
            onPrimaryContainer = Color(0xFFF1F5F9),
            secondary = Color(0xFF94A3B8),
            onSecondary = Color(0xFF0F172A),
            secondaryContainer = Color(0xFF1E293B),
            onSecondaryContainer = Color(0xFFE2E8F0),
            tertiary = Color(0xFF2DD4BF),
            onTertiary = Color(0xFF042F2E),
            tertiaryContainer = Color(0xFF115E59),
            onTertiaryContainer = Color(0xFFCCFBF1),
        )
    }

    return baseScheme.copy(
        background = background,
        onBackground = onBackground,
        surface = surface,
        onSurface = onBackground,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariant,
        surfaceContainerLowest = surfaceLowest,
        surfaceContainerLow = surfaceLow,
        surfaceContainer = surfaceContainer,
        surfaceContainerHigh = surfaceHigh,
        surfaceContainerHighest = surfaceHighest,
        outline = outline,
        outlineVariant = outlineVariant,
    )
}

fun tabularMoneyStyle(baseStyle: TextStyle): TextStyle =
    baseStyle.copy(fontFeatureSettings = "tnum")

val FolioTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.5).sp,
        fontFeatureSettings = "tnum",
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.25).sp,
        fontFeatureSettings = "tnum"
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        fontFeatureSettings = "tnum"
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        fontFeatureSettings = "tnum"
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        fontFeatureSettings = "tnum"
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp,
        fontFeatureSettings = "tnum"
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.1.sp,
        fontFeatureSettings = "tnum"
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        fontFeatureSettings = "tnum"
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 19.sp,
        fontFeatureSettings = "tnum"
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontFeatureSettings = "tnum",
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.1.sp,
        fontFeatureSettings = "tnum",
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.2.sp,
        fontFeatureSettings = "tnum",
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.25.sp,
        fontFeatureSettings = "tnum"
    )
)



private fun buildSepiaSchemeForAccent(accent: FolioAccentPalette): ColorScheme {
    val lightBase = buildLightSchemeForAccent(accent)
    return lightBase.copy(
        background = Color(0xFFF7F3E9),
        surface = Color(0xFFF3EEDD),
        surfaceContainerLowest = Color(0xFFFAF7F0),
        surfaceContainerLow = Color(0xFFEFEAD8),
        surfaceContainer = Color(0xFFE7E1CC),
        surfaceContainerHigh = Color(0xFFDFD8C0),
        surfaceContainerHighest = Color(0xFFDFD8C0),
        onBackground = Color(0xFF2A241E),
        onSurface = Color(0xFF2A241E),
        onSurfaceVariant = Color(0xFF63584B),
        outline = Color(0xFFC4BBA8),
        outlineVariant = Color(0xFFD8D0BE)
    )
}

private fun buildCharcoalSchemeForAccent(accent: FolioAccentPalette): ColorScheme {
    val darkBase = buildDarkSchemeForAccent(accent, amoledPitchBlack = false)
    return darkBase.copy(
        background = Color(0xFF202124),       // Google Chrome Dark background
        surface = Color(0xFF292A2D),          // Google Chrome Dark surface
        surfaceContainerLowest = Color(0xFF1B1C1E),
        surfaceContainerLow = Color(0xFF292A2D),
        surfaceContainer = Color(0xFF313337),
        surfaceContainerHigh = Color(0xFF3C3F43),
        surfaceContainerHighest = Color(0xFF4A4D51), // Lightened OFF switch track
        onBackground = Color(0xFFE8EAED),     // Google Chrome high-contrast text
        onSurface = Color(0xFFE8EAED),
        onSurfaceVariant = Color(0xFF9AA0A6), // Google Chrome secondary text
        outline = Color(0xFF8E96A0),          // High-contrast OFF switch thumb & border
        outlineVariant = Color(0xFF5F6368)    // Crisp Chrome border outline
    )
}

@Composable
fun FolioTheme(
    themeMode: String = "SYSTEM",
    accentPalette: String = "EMERALD",
    dynamicColorEnabled: Boolean = false,
    content: @Composable () -> Unit,
) {
    val resolvedMode = FolioThemeMode.fromKey(themeMode)
    val resolvedAccent = FolioAccentPalette.fromKey(accentPalette)
    val systemDark = isSystemInDarkTheme()

    val useDarkTheme = when (resolvedMode) {
        FolioThemeMode.SYSTEM -> systemDark
        FolioThemeMode.LIGHT -> false
        FolioThemeMode.SEPIA -> false
        FolioThemeMode.SLATE_GRAY -> true
        FolioThemeMode.DARK -> true
        FolioThemeMode.AMOLED_BLACK -> true
    }

    val isAmoled = resolvedMode == FolioThemeMode.AMOLED_BLACK
    val context = LocalContext.current

    val baseColorScheme = when {
        dynamicColorEnabled && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) -> {
            if (useDarkTheme) {
                val dynamicDark = dynamicDarkColorScheme(context)
                if (isAmoled) {
                    dynamicDark.copy(
                        background = Color.Black,
                        surface = Color.Black,
                        surfaceContainerLowest = Color.Black,
                        surfaceContainerLow = Color(0xFF0A0A0A),
                        surfaceContainer = Color(0xFF121212),
                        surfaceContainerHigh = Color(0xFF1A1A1A),
                        surfaceContainerHighest = Color(0xFF242424)
                    )
                } else {
                    dynamicDark
                }
            } else {
                dynamicLightColorScheme(context)
            }
        }
        resolvedMode == FolioThemeMode.SEPIA -> buildSepiaSchemeForAccent(resolvedAccent)
        resolvedMode == FolioThemeMode.SLATE_GRAY -> buildCharcoalSchemeForAccent(resolvedAccent)
        useDarkTheme -> buildDarkSchemeForAccent(
            accent = resolvedAccent,
            amoledPitchBlack = isAmoled
        )
        else -> buildLightSchemeForAccent(accent = resolvedAccent)
    }

    val semanticColors = when {
        resolvedMode == FolioThemeMode.SEPIA -> SepiaFolioSemanticColors
        resolvedMode == FolioThemeMode.SLATE_GRAY -> SlateGrayFolioSemanticColors
        isAmoled -> AmoledFolioSemanticColors
        useDarkTheme -> DarkFolioSemanticColors
        else -> LightFolioSemanticColors
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context.findActivity() ?: return@SideEffect
            val window = activity.window
            if (activity is ComponentActivity) {
                val statusBarStyle = if (useDarkTheme) {
                    SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT
                    )
                }
                val navBarStyle = if (useDarkTheme) {
                    SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT
                    )
                }
                activity.enableEdgeToEdge(
                    statusBarStyle = statusBarStyle,
                    navigationBarStyle = navBarStyle
                )
            }
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            controller.isAppearanceLightStatusBars = !useDarkTheme
            controller.isAppearanceLightNavigationBars = !useDarkTheme

            val bgArgb = baseColorScheme.background.toArgb()
            window.decorView.setBackgroundColor(bgArgb)
            window.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(bgArgb))
        }
    }

    CompositionLocalProvider(LocalFolioSemanticColors provides semanticColors) {
        MaterialTheme(
            colorScheme = baseColorScheme,
            typography = FolioTypography,
            content = content
        )
    }
}

private tailrec fun android.content.Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is android.content.ContextWrapper -> baseContext.findActivity()
    else -> null
}