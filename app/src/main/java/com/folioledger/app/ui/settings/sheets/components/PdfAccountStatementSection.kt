package com.folioledger.app.ui.settings.sheets.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.folioledger.app.data.pdf.PassbookPeriodPreset
import com.folioledger.app.data.pdf.PassbookPeriodPresetResolver
import com.folioledger.app.domain.model.AccountSummary
import com.folioledger.app.ui.components.CategoryIconBadge
import com.folioledger.app.ui.theme.LocalFolioSemanticColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun PdfAccountStatementSection(
    accounts: List<AccountSummary>,
    selectedAccountId: Long,
    onSelectAccount: (Long) -> Unit,
    selectedPeriodPreset: PassbookPeriodPreset,
    onSelectPeriodPreset: (PassbookPeriodPreset) -> Unit,
    includeRunningBalance: Boolean,
    onToggleRunningBalance: (Boolean) -> Unit,
    includeNotesAndTags: Boolean,
    onToggleNotesAndTags: (Boolean) -> Unit,
    isGenerating: Boolean,
    onGenerate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val semanticColors = LocalFolioSemanticColors.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Card 1: Account Picker
        OutlinedCard(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
            border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "SELECT ACCOUNT",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(accounts) { acc ->
                        val isSelected = acc.id == selectedAccountId
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectAccount(acc.id) },
                            label = {
                                Text("${acc.name} (${acc.currencyCode})", fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            },
                            leadingIcon = {
                                CategoryIconBadge(
                                    iconName = acc.iconName,
                                    colorHex = acc.colorHex,
                                    size = 20.dp,
                                    iconSize = 12.dp
                                )
                            }
                        )
                    }
                }
            }
        }

        // Card 2: Period Preset
        OutlinedCard(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
            border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "STATEMENT PERIOD",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PassbookPeriodPreset.entries.filter { it != PassbookPeriodPreset.CUSTOM_RANGE }.forEach { preset ->
                        FilterChip(
                            selected = selectedPeriodPreset == preset,
                            onClick = { onSelectPeriodPreset(preset) },
                            label = { Text(preset.displayName, fontSize = 12.sp) }
                        )
                    }
                }

                val (startDay, endDay) = PassbookPeriodPresetResolver.resolvePresetRange(selectedPeriodPreset)
                val dFmt = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.US)
                Text(
                    text = "Range: ${LocalDate.ofEpochDay(startDay).format(dFmt)} – ${LocalDate.ofEpochDay(endDay).format(dFmt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Card 3: Customization Toggles
        OutlinedCard(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
            border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle)
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Running Balance Column", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text("Prints live balance after each transaction", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = includeRunningBalance, onCheckedChange = onToggleRunningBalance)
                }

                HorizontalDivider(color = semanticColors.cardStrokeSubtle)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Include Notes & Tags", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text("Prints transaction remarks and labels", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = includeNotesAndTags, onCheckedChange = onToggleNotesAndTags)
                }
            }
        }

        // Generate Button
        Button(
            onClick = onGenerate,
            enabled = !isGenerating && accounts.isNotEmpty(),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (isGenerating) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generating PDF Statement...", fontWeight = FontWeight.Bold)
            } else {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate Account Statement (PDF)", fontWeight = FontWeight.Bold)
            }
        }
    }
}
