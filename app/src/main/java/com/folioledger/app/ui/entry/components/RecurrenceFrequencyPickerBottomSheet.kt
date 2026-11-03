// File: app/src/main/java/com/folioledger/app/ui/entry/components/RecurrenceFrequencyPickerBottomSheet.kt
package com.folioledger.app.ui.entry.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.folioledger.app.domain.model.RepeatFrequency
import com.folioledger.app.domain.util.RecurringScheduleCalculator
import com.folioledger.app.ui.theme.LocalFolioSemanticColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private data class FrequencySection(
    val title: String,
    val frequencies: List<RepeatFrequency>
)

private val FREQUENCY_SECTIONS = listOf(
    FrequencySection(
        title = "Frequent",
        frequencies = listOf(
            RepeatFrequency.EVERY_DAY,
            RepeatFrequency.WEEKDAYS,
            RepeatFrequency.WEEKENDS,
            RepeatFrequency.EVERY_WEEK,
            RepeatFrequency.EVERY_2_WEEKS,
            RepeatFrequency.EVERY_4_WEEKS
        )
    ),
    FrequencySection(
        title = "Monthly Cycles",
        frequencies = listOf(
            RepeatFrequency.EVERY_MONTH,
            RepeatFrequency.END_OF_MONTH,
            RepeatFrequency.EVERY_2_MONTHS
        )
    ),
    FrequencySection(
        title = "Multi-Month & Annual",
        frequencies = listOf(
            RepeatFrequency.EVERY_3_MONTHS,
            RepeatFrequency.EVERY_4_MONTHS,
            RepeatFrequency.EVERY_6_MONTHS,
            RepeatFrequency.ANNUALLY
        )
    )
)

private val previewDateFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy", Locale.US)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurrenceFrequencyPickerBottomSheet(
    selectedFrequency: RepeatFrequency,
    isRecurringActive: Boolean,
    baseDate: LocalDate,
    onSelectFrequency: (RepeatFrequency) -> Unit,
    onDisableRecurring: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val semanticColors = LocalFolioSemanticColors.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.EventRepeat,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(8.dp)
                                .size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Repeat Transaction",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Choose an automated calendar frequency",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(
                color = semanticColors.cardStrokeSubtle,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Sections List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                FREQUENCY_SECTIONS.forEach { section ->
                    item(key = section.title) {
                        Text(
                            text = section.title.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp, top = 4.dp)
                        )
                    }

                    items(section.frequencies, key = { it.name }) { freq ->
                        val isSelected = isRecurringActive && freq == selectedFrequency
                        val nextOccurrence = RecurringScheduleCalculator.computeNextOccurrence(
                            currentDate = baseDate,
                            frequency = freq
                        )

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            } else {
                                MaterialTheme.colorScheme.surfaceContainer
                            },
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else semanticColors.cardStrokeSubtle
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    onSelectFrequency(freq)
                                    onDismiss()
                                }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = freq.displayName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Next: ${nextOccurrence.format(previewDateFormatter)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        onSelectFrequency(freq)
                                        onDismiss()
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Bottom action if repeat is active: Disable Repeat
            if (isRecurringActive) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = {
                        onDisableRecurring()
                        onDismiss()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Turn Off Repeat",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
