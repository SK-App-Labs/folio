package com.folioledger.app.ui.settings.sheets.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.folioledger.app.data.pdf.FiscalYearType
import com.folioledger.app.domain.model.SupportedCurrencies
import com.folioledger.app.domain.model.UserPreferences
import com.folioledger.app.ui.theme.LocalFolioSemanticColors

@Composable
fun PdfTaxReportSection(
    prefs: UserPreferences,
    selectedFiscalYearType: FiscalYearType,
    onSelectFiscalYearType: (FiscalYearType) -> Unit,
    anchorYear: Int,
    onPreviousYear: () -> Unit,
    onNextYear: () -> Unit,
    allTaxTags: List<String>,
    selectedTaxTags: List<String>,
    onToggleTag: (String) -> Unit,
    isGenerating: Boolean,
    onGenerate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val semanticColors = LocalFolioSemanticColors.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Card 1: Fiscal Year Calendar Type
        OutlinedCard(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
            border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "FISCAL YEAR CALENDAR",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FiscalYearType.entries.filter { it != FiscalYearType.CUSTOM_RANGE }.forEach { type ->
                        FilterChip(
                            selected = selectedFiscalYearType == type,
                            onClick = { onSelectFiscalYearType(type) },
                            label = { Text(type.displayName, fontSize = 12.sp) }
                        )
                    }
                }

                Text(
                    text = selectedFiscalYearType.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                HorizontalDivider(color = semanticColors.cardStrokeSubtle)

                // Year Stepper Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tax Year / Audit Year:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onPreviousYear) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Year", modifier = Modifier.size(18.dp))
                        }
                        Text(
                            text = anchorYear.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = onNextYear) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Year", modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        // Card 2: Multi-Currency Valuation Basis
        OutlinedCard(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
            border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "CURRENCY VALUATION & REPORTING BASIS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                val primSpec = SupportedCurrencies.fromCode(prefs.primaryCurrencyCode)
                Text(
                    text = "Baseline: ${primSpec.displayName} (${primSpec.symbol} ${primSpec.code})",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                if (prefs.secondaryCurrencies.isNotEmpty()) {
                    Text(
                        text = "Foreign accounts evaluated at configured exchange rates:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        prefs.secondaryCurrencies.forEach { sec ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = semanticColors.incomeEmerald.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "1 ${sec.code} = ${sec.exchangeRateToPrimary} ${primSpec.code}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = semanticColors.incomeEmerald,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = "All accounts are recorded in your primary currency.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Card 3: Deductible Expense Tags Filter
        OutlinedCard(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
            border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "TAX DEDUCTION & AUDIT TAGS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Expenses tagged with these tags will be grouped into the audit schedule:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    allTaxTags.forEach { tag ->
                        val isSelected = selectedTaxTags.contains(tag)
                        FilterChip(
                            selected = isSelected,
                            onClick = { onToggleTag(tag) },
                            label = { Text("#$tag") },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            } else null
                        )
                    }
                }
            }
        }

        // Generate Button
        Button(
            onClick = onGenerate,
            enabled = !isGenerating,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (isGenerating) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generating PDF Report...", fontWeight = FontWeight.Bold)
            } else {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate Annual Tax Statement (PDF)", fontWeight = FontWeight.Bold)
            }
        }
    }
}
