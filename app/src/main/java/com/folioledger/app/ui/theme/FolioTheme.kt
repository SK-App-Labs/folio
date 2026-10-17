// File: app/src/main/java/com/folioledger/app/ui/theme/FolioTheme.kt
package com.folioledger.app.ui.theme

import android.app.Activity
import android.os.Build
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
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
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
    DARK("DARK", "Dark"),
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
    val previewColor: Color
) {
    EMERALD("EMERALD", "Emerald Ledger", "#059669", Color(0xFF059669)),
    INDIGO("INDIGO", "Royal Indigo", "#4F46E5", Color(0xFF4F46E5)),
    AMBER("AMBER", "Saffron Amber", "#D97706", Color(0xFFD97706)),
    TEAL("TEAL", "Oceanic Teal", "#0D9488", Color(0xFF0D9488)),
    CRIMSON("CRIMSON", "Ruby Crimson", "#E11D48", Color(0xFFE11D48)),
    SLATE("SLATE", "Monochrome Slate", "#334155", Color(0xFF334155));

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
    val isAmoled: Boolean
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
    isAmoled = false
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
    isAmoled = false
)

val AmoledFolioSemanticColors = DarkFolioSemanticColors.copy(
    chartGridLine = Color(0xFF171717),
    cardStrokeSubtle = Color(0xFF1F1F1F),
    isAmoled = true
)

val LocalFolioSemanticColors = staticCompositionLocalOf { LightFolioSemanticColors }

private fun buildLightSchemeForAccent(accent: FolioAccentPalette): ColorScheme {
    val baseBackground = Color(0xFFF8FAFC)
    val baseSurface = Color(0xFFFFFFFF)
    val baseSurfaceVariant = Color(0xFFF1F5F9)
    val onBackground = Color(0xFF0F172A)
    val onSurfaceVariant = Color(0xFF475569)
    val outline = Color(0xFFCBD5E1)
    val outlineVariant = Color(0xFFE2E8F0)

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
            surfaceContainerHighest = Color(0xFFCBD5E1),
            outline = outline,
            outlineVariant = outlineVariant
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
            surfaceContainerHighest = Color(0xFFCBD5E1),
            outline = outline,
            outlineVariant = outlineVariant
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
            surfaceContainerHighest = Color(0xFFCBD5E1),
            outline = outline,
            outlineVariant = outlineVariant
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
            surfaceContainerHighest = Color(0xFFCBD5E1),
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
            surfaceContainerHighest = Color(0xFFCBD5E1),
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
            surfaceContainerHighest = Color(0xFFCBD5E1),
            outline = outline,
            outlineVariant = outlineVariant
        )
    }
}

private fun buildDarkSchemeForAccent(
    accent: FolioAccentPalette,
    amoledPitchBlack: Boolean
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
            onTertiaryContainer = Color(0xFFCFFAFE)
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
            onTertiaryContainer = Color(0xFFD1FAE5)
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
            onTertiaryContainer = Color(0xFFE0E7FF)
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
            onTertiaryContainer = Color(0xFFDBEAFE)
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
            onTertiaryContainer = Color(0xFFCCFBF1)
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
        outlineVariant = outlineVariant
    )
}

fun tabularMoneyStyle(baseStyle: TextStyle): TextStyle =
    baseStyle.copy(fontFeatureSettings = "tnum")

fun TextStyle.withTabularNumerals(): TextStyle =
    copy(fontFeatureSettings = "tnum")

val FolioTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.5).sp,
        fontFeatureSettings = "tnum"
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
        fontFeatureSettings = "tnum"
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.1.sp,
        fontFeatureSettings = "tnum"
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.2.sp,
        fontFeatureSettings = "tnum"
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

object FolioThemeDefaults {
    val semanticColors: FolioSemanticColors
        @Composable
        @ReadOnlyComposable
        get() = LocalFolioSemanticColors.current
}

val MaterialTheme.folioSemanticColors: FolioSemanticColors
    @Composable
    @ReadOnlyComposable
    get() = LocalFolioSemanticColors.current

@Composable
fun FolioTheme(
    themeMode: String = "SYSTEM",
    accentPalette: String = "EMERALD",
    dynamicColorEnabled: Boolean = false,
    content: @Composable () -> Unit
) {
    val resolvedMode = FolioThemeMode.fromKey(themeMode)
    val resolvedAccent = FolioAccentPalette.fromKey(accentPalette)
    val systemDark = isSystemInDarkTheme()

    val useDarkTheme = when (resolvedMode) {
        FolioThemeMode.SYSTEM -> systemDark
        FolioThemeMode.LIGHT -> false
        FolioThemeMode.DARK -> true
        FolioThemeMode.AMOLED_BLACK -> true
    }

    val isAmoled = resolvedMode == FolioThemeMode.AMOLED_BLACK
    val context = LocalContext.current

    val baseColorScheme = when {
        dynamicColorEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
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
        useDarkTheme -> buildDarkSchemeForAccent(
            accent = resolvedAccent,
            amoledPitchBlack = isAmoled
        )
        else -> buildLightSchemeForAccent(accent = resolvedAccent)
    }

    val semanticColors = when {
        isAmoled -> AmoledFolioSemanticColors
        useDarkTheme -> DarkFolioSemanticColors
        else -> LightFolioSemanticColors
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                @Suppress("DEPRECATION")
                window.statusBarColor = baseColorScheme.surface.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !useDarkTheme
            }
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