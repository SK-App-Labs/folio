package com.folioledger.app.ui.settings.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.folioledger.app.data.pdf.FiscalYearType
import com.folioledger.app.data.pdf.PassbookPeriodPreset
import com.folioledger.app.data.pdf.PassbookPeriodPresetResolver
import com.folioledger.app.data.pdf.PdfExportResult
import com.folioledger.app.domain.model.AccountSummary
import com.folioledger.app.domain.model.UserPreferences
import com.folioledger.app.ui.settings.sheets.components.PdfAccountStatementSection
import com.folioledger.app.ui.settings.sheets.components.PdfExportSuccessDialog
import com.folioledger.app.ui.settings.sheets.components.PdfTaxReportSection
import com.folioledger.app.ui.theme.LocalFolioSemanticColors
import java.time.LocalDate

private enum class PdfReportTab {
    TAX_REPORT,
    ACCOUNT_STATEMENT
}

@OptIn(ExperimentalMaterial3Api::class)
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
                        PdfTaxReportSection(
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
                        PdfAccountStatementSection(
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
                                val (startDay, endDay) = PassbookPeriodPresetResolver.resolvePresetRange(selectedPeriodPreset)
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
