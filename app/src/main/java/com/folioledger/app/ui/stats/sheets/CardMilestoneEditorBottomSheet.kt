package com.folioledger.app.ui.stats.sheets

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.folioledger.app.domain.model.AccountSummary
import com.folioledger.app.domain.model.CardMilestoneConfig
import com.folioledger.app.domain.model.CategoryType
import com.folioledger.app.domain.model.CurrencySpec
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.NumberGroupingSystem
import com.folioledger.app.domain.model.SupportedCurrencies
import com.folioledger.app.domain.util.CardMilestoneCalculator
import com.folioledger.app.domain.util.MoneyFormatterAndCalculator
import com.folioledger.app.ui.components.CategoryIconBadge
import com.folioledger.app.ui.components.EmptyStateCard
import com.folioledger.app.ui.components.MoneyColorMode
import com.folioledger.app.ui.components.MoneyText
import com.folioledger.app.ui.components.SixMonthSmoothTrendAreaChart
import com.folioledger.app.ui.components.parseHexColorSafely
import com.folioledger.app.ui.stats.*
import com.folioledger.app.ui.stats.components.*
import com.folioledger.app.ui.stats.dialogs.*
import com.folioledger.app.ui.stats.sheets.*
import com.folioledger.app.ui.theme.LocalFolioSemanticColors
import com.folioledger.app.ui.theme.tabularMoneyStyle
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin


private val shortDateFormatter = DateTimeFormatter.ofPattern("dd MMM", Locale.US)
private val fullDateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.US)

// -------------------------------------------------------------------------
// MODAL BOTTOM SHEET: CARD MILESTONE & ANNUAL FEE WAIVER EDITOR
// -------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CardMilestoneEditorBottomSheet(
    card: CreditCardAnalyticsItem,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem,
    onDismiss: () -> Unit,
    onSave: (title: String, startDateEpoch: Long, targetMinor: Long, feeMinor: Long?, autoRenew: Boolean) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val semanticColors = LocalFolioSemanticColors.current
    val cardSpec = remember(card.account.currencyCode) {
        SupportedCurrencies.getByCode(card.account.currencyCode)
            ?: SupportedCurrencies.fromCode(card.account.currencyCode)
    }

    var milestoneTitle by remember {
        mutableStateOf(card.milestoneInfo?.milestoneTitle ?: "Annual Fee Waiver")
    }
    var startDateEpochDay by remember {
        mutableLongStateOf(card.milestoneInfo?.cycleStartDate?.toEpochDay() ?: LocalDate.now().toEpochDay())
    }
    var targetAmountInput by remember {
        val currentTarget = card.milestoneInfo?.targetAmount?.amountMinor ?: 100_000_00L
        mutableStateOf((currentTarget / 100L).toString())
    }
    var feeSavedInput by remember {
        val currentFee = card.milestoneInfo?.annualFeeSaved?.amountMinor ?: 1_000_00L
        mutableStateOf((currentFee / 100L).toString())
    }
    var isAutoRenewing by remember {
        mutableStateOf(card.milestoneInfo?.isAutoRenewing ?: true)
    }
    var showDatePickerDialog by remember { mutableStateOf(false) }

    val parsedTargetMinor = (targetAmountInput.toDoubleOrNull() ?: 0.0) * 100.0
    val parsedFeeMinor = feeSavedInput.toDoubleOrNull()?.let { (it * 100.0).toLong() }

    // Live preview evaluation
    val liveConfig = remember(milestoneTitle, startDateEpochDay, parsedTargetMinor, parsedFeeMinor, isAutoRenewing) {
        CardMilestoneConfig(
            accountId = card.account.id,
            milestoneTitle = milestoneTitle.ifBlank { "Annual Fee Waiver" },
            cycleStartDateEpochDay = startDateEpochDay,
            targetAmountMinor = parsedTargetMinor.toLong(),
            annualFeeSavedMinor = parsedFeeMinor,
            isAutoRenewing = isAutoRenewing
        )
    }
    val livePreviewInfo = remember(liveConfig, card.ytdSpend) {
        CardMilestoneCalculator.evaluateMilestone(
            config = liveConfig,
            qualifyingSpendMinor = card.ytdSpend.amountMinor,
            referenceDate = LocalDate.now()
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CategoryIconBadge(
                        iconName = card.account.iconName,
                        colorHex = card.account.colorHex,
                        size = 36.dp,
                        iconSize = 18.dp
                    )
                    Column {
                        Text(
                            text = "${card.account.name} Milestone",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Track fee waivers and spend bonuses",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Filled.Close, contentDescription = "Close")
                }
            }

            // Milestone Title
            OutlinedTextField(
                value = milestoneTitle,
                onValueChange = { milestoneTitle = it },
                label = { Text("Milestone Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // START DATE / ANNIVERSARY DATE PICKER
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { showDatePickerDialog = true }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "CYCLE / ANNIVERSARY START DATE",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = LocalDate.ofEpochDay(startDateEpochDay).format(fullDateFormatter),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.CalendarToday,
                        contentDescription = "Pick Date",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Target Spend & Annual Fee Inputs
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = targetAmountInput,
                    onValueChange = { targetAmountInput = it.filter { char -> char.isDigit() } },
                    label = { Text("Target Spend (${cardSpec.symbol})") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = feeSavedInput,
                    onValueChange = { feeSavedInput = it.filter { char -> char.isDigit() } },
                    label = { Text("Fee Saved (${cardSpec.symbol})") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            // Auto-Renewal Switch (Card Anniversary)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auto-renew each anniversary year",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Automatically advances by 1 year each card anniversary.",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isAutoRenewing,
                        onCheckedChange = { isAutoRenewing = it }
                    )
                }
            }

            // Live Calculated Insights Box
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Active Window:", style = MaterialTheme.typography.bodySmall)
                        Text(
                            "${livePreviewInfo.cycleStartDate.format(shortDateFormatter)} – ${livePreviewInfo.cycleEndDate.format(fullDateFormatter)}",
                            style = tabularMoneyStyle(MaterialTheme.typography.bodySmall),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Current Spend:", style = MaterialTheme.typography.bodySmall)
                        MoneyText(
                            money = livePreviewInfo.qualifyingSpend,
                            currencySpec = cardSpec,
                            grouping = grouping,
                            colorMode = MoneyColorMode.INCOME,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Remaining to Waive:", style = MaterialTheme.typography.bodySmall)
                        MoneyText(
                            money = livePreviewInfo.remainingSpend,
                            currencySpec = cardSpec,
                            grouping = grouping,
                            colorMode = MoneyColorMode.EXPENSE,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Days Remaining:", style = MaterialTheme.typography.bodySmall)
                        Text(
                            "${livePreviewInfo.daysRemaining} Days",
                            style = tabularMoneyStyle(MaterialTheme.typography.bodySmall),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Required Daily Pace:", style = MaterialTheme.typography.bodySmall)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            MoneyText(
                                money = livePreviewInfo.requiredDailySpend,
                                currencySpec = cardSpec,
                                grouping = grouping,
                                colorMode = MoneyColorMode.NEUTRAL,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = " / day",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            // Save Button
            Button(
                onClick = {
                    onSave(
                        milestoneTitle.ifBlank { "Annual Fee Waiver" },
                        startDateEpochDay,
                        parsedTargetMinor.toLong(),
                        parsedFeeMinor,
                        isAutoRenewing
                    )
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "Save Milestone Target",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    if (showDatePickerDialog) {
        val initialMillis = remember(startDateEpochDay) {
            LocalDate.ofEpochDay(startDateEpochDay)
                .atStartOfDay(ZoneId.of("UTC"))
                .toInstant()
                .toEpochMilli()
        }
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val picked = datePickerState.selectedDateMillis
                        if (picked != null) {
                            val epochDay = Instant.ofEpochMilli(picked)
                                .atZone(ZoneId.of("UTC"))
                                .toLocalDate()
                                .toEpochDay()
                            startDateEpochDay = epochDay
                        }
                        showDatePickerDialog = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
