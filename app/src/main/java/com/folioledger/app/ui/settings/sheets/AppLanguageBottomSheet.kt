// File: app/src/main/java/com/folioledger/app/ui/settings/sheets/AppLanguageBottomSheet.kt
package com.folioledger.app.ui.settings.sheets

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import com.folioledger.app.ui.theme.LocalFolioSemanticColors

/**
 * Curated Tier-1 Global Language Spec for Folio.
 * Covers >85% of global personal finance users across Americas, Europe, Asia-Pacific, and India.
 */
data class SupportedLanguageItem(
    val tag: String,
    val nativeName: String,
    val englishName: String,
    val flagEmoji: String,
    val region: String
)

val TIER_1_LANGUAGES = listOf(
    SupportedLanguageItem(
        tag = "",
        nativeName = "System Default",
        englishName = "Follow Android system setting",
        flagEmoji = "🌐",
        region = "Default"
    ),
    // Americas & Europe
    SupportedLanguageItem("en", "English", "English", "🇺🇸", "Americas & Europe"),
    SupportedLanguageItem("es", "Español", "Spanish", "🇪🇸", "Americas & Europe"),
    SupportedLanguageItem("pt-BR", "Português (Brasil)", "Portuguese (Brazil)", "🇧🇷", "Americas & Europe"),
    SupportedLanguageItem("de", "Deutsch", "German", "🇩🇪", "Americas & Europe"),
    SupportedLanguageItem("fr", "Français", "French", "🇫🇷", "Americas & Europe"),
    SupportedLanguageItem("it", "Italiano", "Italian", "🇮🇹", "Americas & Europe"),
    // Asia-Pacific
    SupportedLanguageItem("ja", "日本語", "Japanese", "🇯🇵", "Asia-Pacific"),
    SupportedLanguageItem("ko", "한국어", "Korean", "🇰🇷", "Asia-Pacific"),
    SupportedLanguageItem("zh-CN", "简体中文", "Simplified Chinese", "🇨🇳", "Asia-Pacific"),
    SupportedLanguageItem("zh-TW", "繁體中文", "Traditional Chinese", "🇹🇼", "Asia-Pacific"),
    SupportedLanguageItem("id", "Bahasa Indonesia", "Indonesian", "🇮🇩", "Asia-Pacific"),
    // India
    SupportedLanguageItem("hi", "हिन्दी", "Hindi", "🇮🇳", "India"),
    SupportedLanguageItem("ta", "தமிழ்", "Tamil", "🇮🇳", "India")
)

/**
 * Returns the friendly native display name for the currently configured app language tag.
 */
fun resolveCurrentLanguageDisplayName(currentTag: String): String {
    if (currentTag.isBlank()) return "System"
    val match = TIER_1_LANGUAGES.firstOrNull { it.tag.equals(currentTag, ignoreCase = true) }
        ?: TIER_1_LANGUAGES.firstOrNull { it.tag.startsWith(currentTag.take(2), ignoreCase = true) }
    return match?.nativeName ?: currentTag
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppLanguageBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val semanticColors = LocalFolioSemanticColors.current
    var searchQuery by remember { mutableStateOf("") }

    val currentLocales = AppCompatDelegate.getApplicationLocales()
    val activeTag = if (currentLocales.isEmpty) "" else currentLocales[0]?.toLanguageTag() ?: ""

    val filteredLanguages = remember(searchQuery) {
        val query = searchQuery.trim().lowercase()
        if (query.isEmpty()) {
            TIER_1_LANGUAGES
        } else {
            TIER_1_LANGUAGES.filter {
                it.nativeName.lowercase().contains(query) ||
                    it.englishName.lowercase().contains(query) ||
                    it.tag.lowercase().contains(query) ||
                    it.region.lowercase().contains(query)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 8.dp)
                .navigationBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "App Language",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Choose your preferred interface language",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                placeholder = {
                    Text(
                        text = "Search language (e.g. Spanish, Deutsch, 日本語)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = semanticColors.cardStrokeSubtle
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Language List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
                    .padding(horizontal = 16.dp)
            ) {
                items(
                    items = filteredLanguages,
                    key = { it.tag }
                ) { item ->
                    val isSelected = if (item.tag.isEmpty()) {
                        activeTag.isEmpty()
                    } else {
                        activeTag.equals(item.tag, ignoreCase = true) ||
                            (activeTag.length >= 2 && item.tag.length >= 2 &&
                                activeTag.take(2).equals(item.tag.take(2), ignoreCase = true) &&
                                !activeTag.contains("-"))
                    }

                    LanguageRowItem(
                        item = item,
                        isSelected = isSelected,
                        onClick = {
                            val targetLocales = if (item.tag.isEmpty()) {
                                LocaleListCompat.getEmptyLocaleList()
                            } else {
                                LocaleListCompat.forLanguageTags(item.tag)
                            }
                            AppCompatDelegate.setApplicationLocales(targetLocales)
                            onDismissRequest()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LanguageRowItem(
    item: SupportedLanguageItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val semanticColors = LocalFolioSemanticColors.current

    Surface(
        onClick = onClick,
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        } else {
            Color.Transparent
        },
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.flagEmoji,
                    fontSize = 22.sp,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Column {
                    Text(
                        text = item.nativeName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                    Text(
                        text = item.englishName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}
