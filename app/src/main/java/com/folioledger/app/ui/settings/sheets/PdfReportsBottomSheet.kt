package com.folioledger.app.ui.settings.sheets

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.folioledger.app.data.pdf.FiscalYearType
import com.folioledger.app.data.pdf.PassbookPeriodPreset
import com.folioledger.app.data.pdf.PdfExportResult
import com.folioledger.app.domain.model.AccountSummary
import com.folioledger.app.domain.model.SupportedCurrencies
import com.folioledger.app.domain.model.UserPreferences
import com.folioledger.app.ui.components.CategoryIconBadge
import com.folioledger.app.ui.theme.LocalFolioSemanticColors
import java.io.File
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private enum class PdfReportTab {
    TAX_REPORT,
    ACCOUNT_STATEMENT
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PdfReportsBottomSheet(
    prefs: UserPreferences,
    accounts: List<AccountSummary>,
    isGeneratingPdf: Boolean,
    lastGeneratedPdfResult: PdfExportResult?,
    onGenerateAccountStatement: (
        accountId: Long,
        startEpochDay: Long,
        endEpochDay: Long,
        includeNotesAndTags: Boolean,
        includeRunningBalance: Boolean
    ) -> Unit,
    onGenerateAnnualTaxStatement: (
        fiscalYearType: FiscalYearType,
        anchorYear: Int,
        customStartEpochDay: Long?,
        customEndEpochDay: Long?,
        selectedTaxTags: Set<String>
    ) -> Unit,
    onClearLastResult: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val semanticColors = LocalFolioSemanticColors.current
    val context = LocalContext.current

    var selectedTab by remember { mutableStateOf(PdfReportTab.TAX_REPORT) }

    // Annual Tax Report State
    var selectedFiscalYearType by remember { mutableStateOf(FiscalYearType.CALENDAR_YEAR) }
    var anchorYear by remember { mutableIntStateOf(LocalDate.now().year) }
    val defaultTaxTags = remember { listOf("tax", "business", "medical", "deductible", "donation", "charity", "work") }
    val selectedTaxTags = remember { mutableStateListOf<String>().apply { addAll(defaultTaxTags) } }

    // Account Statement State
    val activeAccounts = remember(accounts) { accounts.filter { !it.isArchived } }
    var selectedAccountId by remember(activeAccounts) {
        mutableStateOf(activeAccounts.firstOrNull()?.id ?: 0L)
    }
    var selectedPeriodPreset by remember { mutableStateOf(PassbookPeriodPreset.CURRENT_MONTH) }
    var includeRunningBalance by remember { mutableStateOf(true) }
    var includeNotesAndTags by remember { mutableStateOf(true) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(semanticColors.incomeEmerald.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            tint = semanticColors.incomeEmerald,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "PDF Statements & Reports",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Branded, publication-ready A4 reports • 100% Offline",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Navigation Tabs
            PrimaryTabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = { HorizontalDivider(color = semanticColors.cardStrokeSubtle) }
            ) {
                Tab(
                    selected = selectedTab == PdfReportTab.TAX_REPORT,
                    onClick = { selectedTab = PdfReportTab.TAX_REPORT },
                    text = { Text("Annual Tax Statement", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == PdfReportTab.ACCOUNT_STATEMENT,
                    onClick = { selectedTab = PdfReportTab.ACCOUNT_STATEMENT },
                    text = { Text("Account Statement", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (selectedTab) {
                    PdfReportTab.TAX_REPORT -> {
                        TaxReportSection(
                            prefs = prefs,
                            selectedFiscalYearType = selectedFiscalYearType,
                            onSelectFiscalYearType = { selectedFiscalYearType = it },
                            anchorYear = anchorYear,
                            onPreviousYear = { anchorYear-- },
                            onNextYear = { anchorYear++ },
                            allTaxTags = defaultTaxTags,
                            selectedTaxTags = selectedTaxTags,
                            onToggleTag = { tag ->
                                if (selectedTaxTags.contains(tag)) {
                                    selectedTaxTags.remove(tag)
                                } else {
                                    selectedTaxTags.add(tag)
                                }
                            },
                            isGenerating = isGeneratingPdf,
                            onGenerate = {
                                onGenerateAnnualTaxStatement(
                                    selectedFiscalYearType,
                                    anchorYear,
                                    null,
                                    null,
                                    selectedTaxTags.toSet()
                                )
                            }
                        )
                    }

                    PdfReportTab.ACCOUNT_STATEMENT -> {
                        AccountStatementSection(
                            accounts = activeAccounts,
                            selectedAccountId = selectedAccountId,
                            onSelectAccount = { selectedAccountId = it },
                            selectedPeriodPreset = selectedPeriodPreset,
                            onSelectPeriodPreset = { selectedPeriodPreset = it },
                            includeRunningBalance = includeRunningBalance,
                            onToggleRunningBalance = { includeRunningBalance = it },
                            includeNotesAndTags = includeNotesAndTags,
                            onToggleNotesAndTags = { includeNotesAndTags = it },
                            isGenerating = isGeneratingPdf,
                            onGenerate = {
                                val (startDay, endDay) = resolvePresetRange(selectedPeriodPreset)
                                onGenerateAccountStatement(
                                    selectedAccountId,
                                    startDay,
                                    endDay,
                                    includeNotesAndTags,
                                    includeRunningBalance
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    // Success Dialog on PDF Completion
    if (lastGeneratedPdfResult != null) {
        PdfExportSuccessDialog(
            result = lastGeneratedPdfResult,
            onDismiss = onClearLastResult
        )
    }
}

// -------------------------------------------------------------------------------------------------
// TAB 1: ANNUAL TAX STATEMENT SECTION
// -------------------------------------------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TaxReportSection(
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
    onGenerate: () -> Unit
) {
    val semanticColors = LocalFolioSemanticColors.current

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
                FiscalYearType.values().filter { it != FiscalYearType.CUSTOM_RANGE }.forEach { type ->
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

// -------------------------------------------------------------------------------------------------
// TAB 2: ACCOUNT STATEMENT SECTION
// -------------------------------------------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AccountStatementSection(
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
    onGenerate: () -> Unit
) {
    val semanticColors = LocalFolioSemanticColors.current

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
                PassbookPeriodPreset.values().filter { it != PassbookPeriodPreset.CUSTOM_RANGE }.forEach { preset ->
                    FilterChip(
                        selected = selectedPeriodPreset == preset,
                        onClick = { onSelectPeriodPreset(preset) },
                        label = { Text(preset.displayName, fontSize = 12.sp) }
                    )
                }
            }

            val (startDay, endDay) = resolvePresetRange(selectedPeriodPreset)
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

// -------------------------------------------------------------------------------------------------
// COMPLETION HUB: SUCCESS DIALOG WITH VIEW, SHARE & SAF PICKER
// -------------------------------------------------------------------------------------------------

@Composable
private fun PdfExportSuccessDialog(
    result: PdfExportResult,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val semanticColors = LocalFolioSemanticColors.current

    // SAF Document Picker launcher for custom destination folder
    val saveAsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { destinationUri ->
        if (destinationUri != null) {
            runCatching {
                context.contentResolver.openOutputStream(destinationUri)?.use { out ->
                    result.file.inputStream().use { input ->
                        input.copyTo(out)
                    }
                }
                Toast.makeText(context, "Saved to selected location", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(context, "Failed to save: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = semanticColors.incomeEmerald,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Statement Generated!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // File Summary Card
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = result.fileName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${result.fileSizeBytes / 1024} KB • ${result.pageCount} Pages • Saved to ${result.publicDisplayPath}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Quick Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewOrPrintPdfFile(context, result.file) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("View / Print", fontSize = 13.sp)
                    }

                    FilledTonalButton(
                        onClick = { sharePdfFile(context, result.file, result.fileName) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share", fontSize = 13.sp)
                    }
                }

                OutlinedButton(
                    onClick = { saveAsLauncher.launch(result.fileName) },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save to Custom Location...", fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

// -------------------------------------------------------------------------------------------------
// INTENT HELPERS: VIEW & SHARE
// -------------------------------------------------------------------------------------------------

private fun viewOrPrintPdfFile(context: Context, file: File) {
    runCatching {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(viewIntent, "Open Statement"))
    }.onFailure {
        Toast.makeText(context, "No PDF viewer found on device", Toast.LENGTH_SHORT).show()
    }
}

private fun sharePdfFile(context: Context, file: File, title: String) {
    runCatching {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Statement"))
    }.onFailure {
        Toast.makeText(context, "Failed to open share sheet", Toast.LENGTH_SHORT).show()
    }
}

// -------------------------------------------------------------------------------------------------
// DATE RANGE RESOLVER HELPER
// -------------------------------------------------------------------------------------------------

private fun resolvePresetRange(preset: PassbookPeriodPreset): Pair<Long, Long> {
    val today = LocalDate.now()
    return when (preset) {
        PassbookPeriodPreset.CURRENT_MONTH -> {
            val start = today.withDayOfMonth(1).toEpochDay()
            val end = today.toEpochDay()
            start to end
        }
        PassbookPeriodPreset.LAST_MONTH -> {
            val lastMonth = today.minusMonths(1)
            val start = lastMonth.withDayOfMonth(1).toEpochDay()
            val end = lastMonth.withDayOfMonth(lastMonth.lengthOfMonth()).toEpochDay()
            start to end
        }
        PassbookPeriodPreset.LAST_THREE_MONTHS -> {
            val start = today.minusMonths(3).withDayOfMonth(1).toEpochDay()
            val end = today.toEpochDay()
            start to end
        }
        PassbookPeriodPreset.YEAR_TO_DATE -> {
            val start = today.withDayOfYear(1).toEpochDay()
            val end = today.toEpochDay()
            start to end
        }
        PassbookPeriodPreset.ALL_TIME -> {
            val start = LocalDate.of(2000, 1, 1).toEpochDay()
            val end = today.toEpochDay()
            start to end
        }
        PassbookPeriodPreset.CUSTOM_RANGE -> {
            today.minusMonths(1).toEpochDay() to today.toEpochDay()
        }
    }
}
