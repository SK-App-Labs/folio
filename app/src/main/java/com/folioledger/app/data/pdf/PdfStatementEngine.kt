package com.folioledger.app.data.pdf

import android.content.ContentValues
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.folioledger.app.domain.model.CurrencySpec
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.NumberGroupingSystem
import com.folioledger.app.domain.util.MoneyFormatterAndCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.ceil

/**
 * High-performance, 100% offline Skia vector PDF generation engine for Folio.
 * Generates branded Account Statements and Universal Annual Financial & Tax Reports
 * with zero external library dependencies and zero APK size bloat.
 */
object PdfStatementEngine {

    private const val PAGE_WIDTH = 595f // A4 width at 72 DPI
    private const val PAGE_HEIGHT = 842f // A4 height at 72 DPI
    private const val MARGIN_LEFT = 36f
    private const val MARGIN_RIGHT = 36f
    private const val MARGIN_TOP = 36f
    private const val MARGIN_BOTTOM = 36f
    private const val USABLE_WIDTH = PAGE_WIDTH - MARGIN_LEFT - MARGIN_RIGHT // 523f
    private const val USABLE_BOTTOM = PAGE_HEIGHT - MARGIN_BOTTOM // 806f
    private const val FOOTER_Y = 818f
    private const val FOOTER_RULE_Y = 804f

    // Theme Colors
    private val COLOR_PRIMARY_EMERALD = Color.rgb(5, 150, 105) // #059669
    private val COLOR_TEXT_PRIMARY = Color.rgb(15, 23, 42) // #0F172A
    private val COLOR_TEXT_MUTED = Color.rgb(100, 116, 139) // #64748B
    private val COLOR_CARD_BG = Color.rgb(248, 250, 252) // #F8FAFC
    private val COLOR_ZEBRA_BG = Color.rgb(241, 245, 249) // #F1F5F9
    private val COLOR_BORDER = Color.rgb(226, 232, 240) // #E2E8F0
    private val COLOR_HEADER_BG = Color.rgb(30, 41, 59) // #1E293B
    private val COLOR_INFLOW = Color.rgb(4, 120, 87) // #047857
    private val COLOR_OUTFLOW = Color.rgb(15, 23, 42) // #0F172A
    private val COLOR_ACCENT_SUBTLE = Color.rgb(209, 250, 229) // #D1FAE5

    // Paints
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = COLOR_TEXT_PRIMARY
        textSize = 9f
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
    }

    private val boldTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = COLOR_TEXT_PRIMARY
        textSize = 9f
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    }

    private val mutedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = COLOR_TEXT_MUTED
        textSize = 8f
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
    }

    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = COLOR_TEXT_PRIMARY
        textSize = 18f
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    }

    private val brandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = COLOR_PRIMARY_EMERALD
        textSize = 14f
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    }

    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = COLOR_BORDER
        style = Paint.Style.STROKE
        strokeWidth = 0.8f
    }

    private val accentRulePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = COLOR_PRIMARY_EMERALD
        style = Paint.Style.STROKE
        strokeWidth = 2.5f
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    // ---------------------------------------------------------------------------------------------
    // REPORT 1: ACCOUNT PASSBOOK STATEMENT
    // ---------------------------------------------------------------------------------------------

    suspend fun generateAccountStatementPdf(
        context: Context,
        data: AccountStatementPdfData
    ): Result<PdfExportResult> = withContext(Dispatchers.IO) {
        runCatching {
            val pdfDocument = PdfDocument()

            // Pre-calculate pagination
            val rowHeight = if (data.includeNotesAndTags) 25f else 18f
            val summaryHeight = if (data.isCreditCard) 130f else 105f
            val headerAreaHeight = 70f + summaryHeight + 25f // header + card + table header
            val page1AvailableForRows = USABLE_BOTTOM - MARGIN_TOP - headerAreaHeight
            val rowsOnPage1 = (page1AvailableForRows / rowHeight).toInt().coerceAtLeast(1)
            val otherPageAvailableForRows = USABLE_BOTTOM - MARGIN_TOP - 40f // mini header + table header
            val rowsOnOtherPages = (otherPageAvailableForRows / rowHeight).toInt().coerceAtLeast(1)

            val totalEntries = data.entries.size
            val totalPages = if (totalEntries <= rowsOnPage1) {
                1
            } else {
                1 + ceil((totalEntries - rowsOnPage1).toDouble() / rowsOnOtherPages).toInt()
            }

            var currentPageNumber = 1
            var page = pdfDocument.startPage(
                PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), currentPageNumber).create()
            )
            var canvas = page.canvas
            var currentY = MARGIN_TOP

            // Page 1: Brand & Report Title
            currentY = drawBrandAndDocumentTitle(
                canvas = canvas,
                startY = currentY,
                title = "ACCOUNT PASSBOOK STATEMENT",
                subtitle = "${data.accountName} (${data.accountGroupTitle}) • ${data.periodTitle}",
                badgeText = "CONFIDENTIAL"
            )

            // Executive Summary Card
            currentY = drawPassbookSummaryCard(
                canvas = canvas,
                startY = currentY,
                data = data
            )

            // Table Header
            currentY = drawPassbookTableHeader(canvas, currentY, data)

            var entryIndex = 0
            while (entryIndex < data.entries.size) {
                val entry = data.entries[entryIndex]

                if (currentY + rowHeight > USABLE_BOTTOM) {
                    drawPageFooter(canvas, currentPageNumber, totalPages, data.generatedTimestampText)
                    pdfDocument.finishPage(page)

                    currentPageNumber++
                    page = pdfDocument.startPage(
                        PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), currentPageNumber).create()
                    )
                    canvas = page.canvas
                    currentY = MARGIN_TOP

                    // Mini-header for continuation pages
                    currentY = drawContinuationHeader(
                        canvas = canvas,
                        startY = currentY,
                        title = "Account Statement: ${data.accountName} (${data.periodTitle})"
                    )
                    currentY = drawPassbookTableHeader(canvas, currentY, data)
                }

                val isZebra = entryIndex % 2 == 1
                drawPassbookEntryRow(
                    canvas = canvas,
                    y = currentY,
                    rowHeight = rowHeight,
                    entry = entry,
                    isZebra = isZebra,
                    currencySpec = data.currencySpec,
                    grouping = data.numberGrouping,
                    includeNotes = data.includeNotesAndTags,
                    includeRunningBalance = data.includeRunningBalance
                )

                currentY += rowHeight
                entryIndex++
            }

            // Draw final page footer
            drawPageFooter(canvas, currentPageNumber, totalPages, data.generatedTimestampText)
            pdfDocument.finishPage(page)

            val sanitizedAccountName = data.accountName.replace(Regex("[^a-zA-Z0-9_-]"), "_")
            val sanitizedPeriod = data.periodTitle.replace(Regex("[^a-zA-Z0-9_-]"), "_")
            val fileName = "Folio_Statement_${sanitizedAccountName}_$sanitizedPeriod.pdf"

            val result = savePdfToStorage(context, pdfDocument, fileName, currentPageNumber)
            pdfDocument.close()
            result
        }
    }

    // ---------------------------------------------------------------------------------------------
    // REPORT 2: UNIVERSAL ANNUAL FINANCIAL & TAX STATEMENT
    // ---------------------------------------------------------------------------------------------

    suspend fun generateAnnualTaxStatementPdf(
        context: Context,
        data: AnnualTaxPdfData
    ): Result<PdfExportResult> = withContext(Dispatchers.IO) {
        runCatching {
            val pdfDocument = PdfDocument()

            // Dynamic Page Management
            var currentPageNumber = 1
            var page = pdfDocument.startPage(
                PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), currentPageNumber).create()
            )
            var canvas = page.canvas
            var currentY = MARGIN_TOP

            fun ensureVerticalSpace(neededHeight: Float, onNewPageHeader: (Canvas) -> Float) {
                if (currentY + neededHeight > USABLE_BOTTOM) {
                    drawPageFooter(canvas, currentPageNumber, 0, data.generatedTimestampText)
                    pdfDocument.finishPage(page)

                    currentPageNumber++
                    page = pdfDocument.startPage(
                        PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), currentPageNumber).create()
                    )
                    canvas = page.canvas
                    currentY = MARGIN_TOP
                    currentY = onNewPageHeader(canvas)
                }
            }

            // 1. Title Banner
            currentY = drawBrandAndDocumentTitle(
                canvas = canvas,
                startY = currentY,
                title = "ANNUAL FINANCIAL & TAX STATEMENT",
                subtitle = "${data.fiscalPeriodTitle} • ${data.fiscalPeriodSubtitle}",
                badgeText = "OFFICIAL AUDIT REPORT"
            )

            // 2. Executive Financial Summary
            currentY = drawAnnualFinancialSummaryCard(canvas, currentY, data)

            // 3. Multi-Currency Valuation Footnotes (if applicable)
            if (data.valuationExchangeNotes.isNotEmpty()) {
                currentY = drawValuationNotesBox(canvas, currentY, data.valuationExchangeNotes)
            }

            // 4. Itemized Tax Deductions & Business Schedule
            currentY = drawTaxDeductionsSection(
                canvas = canvas,
                startY = currentY,
                data = data,
                onEnsureSpace = { needed ->
                    ensureVerticalSpace(needed) { newCanvas ->
                        drawContinuationHeader(newCanvas, MARGIN_TOP, "Itemized Tax Deductions & Business Schedule (Cont.)")
                    }
                }
            )

            // 5. Income Sources Schedule
            currentY = drawCategoryScheduleSection(
                canvas = canvas,
                startY = currentY,
                sectionTitle = "INCOME BREAKDOWN BY SOURCE",
                items = data.incomeSources,
                primaryCurrency = data.primaryCurrencySpec,
                grouping = data.numberGrouping,
                isInflow = true,
                onEnsureSpace = { needed ->
                    ensureVerticalSpace(needed) { newCanvas ->
                        drawContinuationHeader(newCanvas, MARGIN_TOP, "Income Sources Schedule (Cont.)")
                    }
                }
            )

            // 6. Expense Category Schedule
            currentY = drawCategoryScheduleSection(
                canvas = canvas,
                startY = currentY,
                sectionTitle = "EXPENSES BY CATEGORY",
                items = data.expenseCategories,
                primaryCurrency = data.primaryCurrencySpec,
                grouping = data.numberGrouping,
                isInflow = false,
                onEnsureSpace = { needed ->
                    ensureVerticalSpace(needed) { newCanvas ->
                        drawContinuationHeader(newCanvas, MARGIN_TOP, "Expense Category Schedule (Cont.)")
                    }
                }
            )

            // 7. Year-End Balance Sheet Snapshot
            currentY = drawBalanceSheetSection(
                canvas = canvas,
                startY = currentY,
                data = data,
                onEnsureSpace = { needed ->
                    ensureVerticalSpace(needed) { newCanvas ->
                        drawContinuationHeader(newCanvas, MARGIN_TOP, "Year-End Balance Sheet Snapshot (Cont.)")
                    }
                }
            )

            // Finish the final page
            drawPageFooter(canvas, currentPageNumber, 0, data.generatedTimestampText)
            pdfDocument.finishPage(page)

            val sanitizedTitle = data.fiscalPeriodTitle.replace(Regex("[^a-zA-Z0-9_-]"), "_")
            val fileName = "Folio_Tax_Report_$sanitizedTitle.pdf"

            val result = savePdfToStorage(context, pdfDocument, fileName, currentPageNumber)
            pdfDocument.close()
            result
        }
    }

    // ---------------------------------------------------------------------------------------------
    // DRAWING HELPERS: HEADERS & BRANDING
    // ---------------------------------------------------------------------------------------------

    private fun drawBrandAndDocumentTitle(
        canvas: Canvas,
        startY: Float,
        title: String,
        subtitle: String,
        badgeText: String
    ): Float {
        var y = startY

        // Top Brand Row: FOLIO + Badge
        canvas.drawText("FOLIO", MARGIN_LEFT, y + 12f, brandPaint)

        // Badge pill on right
        val badgeWidth = boldTextPaint.measureText(badgeText) + 14f
        val badgeX = PAGE_WIDTH - MARGIN_RIGHT - badgeWidth
        fillPaint.color = COLOR_ACCENT_SUBTLE
        canvas.drawRoundRect(badgeX, y, badgeX + badgeWidth, y + 16f, 4f, 4f, fillPaint)
        boldTextPaint.color = COLOR_PRIMARY_EMERALD
        canvas.drawText(badgeText, badgeX + 7f, y + 11.5f, boldTextPaint)
        boldTextPaint.color = COLOR_TEXT_PRIMARY // reset

        y += 24f

        // Document Title
        canvas.drawText(title, MARGIN_LEFT, y + 14f, titlePaint)
        y += 20f

        // Subtitle
        mutedPaint.textSize = 9.5f
        canvas.drawText(subtitle, MARGIN_LEFT, y + 8f, mutedPaint)
        mutedPaint.textSize = 8f // reset
        y += 14f

        // Emerald Accent Rule
        canvas.drawLine(MARGIN_LEFT, y, PAGE_WIDTH - MARGIN_RIGHT, y, accentRulePaint)
        y += 10f

        return y
    }

    private fun drawContinuationHeader(canvas: Canvas, startY: Float, title: String): Float {
        var y = startY
        boldTextPaint.textSize = 10f
        canvas.drawText(title, MARGIN_LEFT, y + 10f, boldTextPaint)
        boldTextPaint.textSize = 9f
        y += 16f
        canvas.drawLine(MARGIN_LEFT, y, PAGE_WIDTH - MARGIN_RIGHT, y, strokePaint)
        y += 8f
        return y
    }

    private fun drawPageFooter(canvas: Canvas, pageNumber: Int, totalPages: Int, timestampText: String) {
        // Thin rule above footer
        canvas.drawLine(MARGIN_LEFT, FOOTER_RULE_Y, PAGE_WIDTH - MARGIN_RIGHT, FOOTER_RULE_Y, strokePaint)

        // Left disclaimer
        canvas.drawText(
            "Folio Personal Ledger • 100% Offline & Private • Generated $timestampText",
            MARGIN_LEFT,
            FOOTER_Y,
            mutedPaint
        )

        // Right page count
        val pageText = if (totalPages > 0) "Page $pageNumber of $totalPages" else "Page $pageNumber"
        val textWidth = mutedPaint.measureText(pageText)
        canvas.drawText(pageText, PAGE_WIDTH - MARGIN_RIGHT - textWidth, FOOTER_Y, mutedPaint)
    }

    // ---------------------------------------------------------------------------------------------
    // DRAWING HELPERS: PASSBOOK STATEMENT
    // ---------------------------------------------------------------------------------------------

    private fun drawPassbookSummaryCard(
        canvas: Canvas,
        startY: Float,
        data: AccountStatementPdfData
    ): Float {
        val cardHeight = if (data.isCreditCard) 100f else 78f
        val cardRect = RectF(MARGIN_LEFT, startY, PAGE_WIDTH - MARGIN_RIGHT, startY + cardHeight)

        fillPaint.color = COLOR_CARD_BG
        canvas.drawRoundRect(cardRect, 6f, 6f, fillPaint)
        canvas.drawRoundRect(cardRect, 6f, 6f, strokePaint)

        val cellWidth = USABLE_WIDTH / 4f
        val row1Y = startY + 16f
        val row1ValY = row1Y + 14f

        // Cell 1: Opening Balance
        canvas.drawText("OPENING BALANCE", MARGIN_LEFT + 10f, row1Y, mutedPaint)
        val openingText = MoneyFormatterAndCalculator.formatMinorMoney(
            data.openingBalance, data.currencySpec, data.numberGrouping
        )
        canvas.drawText(openingText, MARGIN_LEFT + 10f, row1ValY, boldTextPaint)

        // Cell 2: Total Inflows (+)
        val cell2X = MARGIN_LEFT + cellWidth
        canvas.drawText("TOTAL INFLOWS (+)", cell2X, row1Y, mutedPaint)
        val inflowsText = MoneyFormatterAndCalculator.formatMinorMoney(
            data.totalInflows, data.currencySpec, data.numberGrouping, showPlusSignForPositive = true
        )
        val prevColor = textPaint.color
        textPaint.color = COLOR_INFLOW
        canvas.drawText(inflowsText, cell2X, row1ValY, textPaint)
        textPaint.color = prevColor

        // Cell 3: Total Outflows (-)
        val cell3X = MARGIN_LEFT + (cellWidth * 2)
        canvas.drawText("TOTAL OUTFLOWS (-)", cell3X, row1Y, mutedPaint)
        val outflowsText = MoneyFormatterAndCalculator.formatMinorMoney(
            data.totalOutflows, data.currencySpec, data.numberGrouping
        )
        canvas.drawText(outflowsText, cell3X, row1ValY, textPaint)

        // Cell 4: Closing Balance
        val cell4X = MARGIN_LEFT + (cellWidth * 3)
        canvas.drawText("CLOSING BALANCE", cell4X, row1Y, mutedPaint)
        val closingText = MoneyFormatterAndCalculator.formatMinorMoney(
            data.closingBalance, data.currencySpec, data.numberGrouping
        )
        boldTextPaint.textSize = 10f
        canvas.drawText(closingText, cell4X, row1ValY, boldTextPaint)
        boldTextPaint.textSize = 9f

        // Credit Card Extras Row
        if (data.isCreditCard) {
            val dividerY = startY + 44f
            canvas.drawLine(MARGIN_LEFT + 10f, dividerY, PAGE_WIDTH - MARGIN_RIGHT - 10f, dividerY, strokePaint)

            val row2Y = dividerY + 16f
            val row2ValY = row2Y + 14f

            // Total Debt
            canvas.drawText("TOTAL DEBT", MARGIN_LEFT + 10f, row2Y, mutedPaint)
            val debtText = MoneyFormatterAndCalculator.formatMinorMoney(
                data.closingBalance, data.currencySpec, data.numberGrouping
            )
            canvas.drawText(debtText, MARGIN_LEFT + 10f, row2ValY, boldTextPaint)

            // Credit Limit
            canvas.drawText("CREDIT LIMIT", cell2X, row2Y, mutedPaint)
            val limitText = data.creditCardLimit?.let {
                MoneyFormatterAndCalculator.formatMinorMoney(it, data.currencySpec, data.numberGrouping)
            } ?: "N/A"
            canvas.drawText(limitText, cell2X, row2ValY, textPaint)

            // Statement Payable
            canvas.drawText("STATEMENT PAYABLE", cell3X, row2Y, mutedPaint)
            val payableText = data.creditCardStatementPayable?.let {
                MoneyFormatterAndCalculator.formatMinorMoney(it, data.currencySpec, data.numberGrouping)
            } ?: "₹0.00"
            canvas.drawText(payableText, cell3X, row2ValY, textPaint)

            // Payment Due Date
            canvas.drawText("PAYMENT DUE", cell4X, row2Y, mutedPaint)
            val dueText = data.creditCardDueDateText ?: "N/A"
            canvas.drawText(dueText, cell4X, row2ValY, textPaint)
        } else {
            // Net Flow & Entry Count summary line
            val netFlowText = MoneyFormatterAndCalculator.formatMinorMoney(
                data.netPeriodFlow, data.currencySpec, data.numberGrouping, showPlusSignForPositive = true
            )
            val summaryLine = "Net Period Change: $netFlowText • Total Entries: ${data.transactionCount}"
            canvas.drawText(summaryLine, MARGIN_LEFT + 10f, startY + 58f, mutedPaint)
        }

        return startY + cardHeight + 12f
    }

    private fun drawPassbookTableHeader(
        canvas: Canvas,
        startY: Float,
        data: AccountStatementPdfData
    ): Float {
        val headerHeight = 20f
        fillPaint.color = COLOR_HEADER_BG
        canvas.drawRect(MARGIN_LEFT, startY, PAGE_WIDTH - MARGIN_RIGHT, startY + headerHeight, fillPaint)

        boldTextPaint.color = Color.WHITE
        val y = startY + 13.5f

        // Col 1: Date (55pt)
        canvas.drawText("DATE", MARGIN_LEFT + 6f, y, boldTextPaint)

        // Col 2: Payee / Description (190pt)
        canvas.drawText("PAYEE & DETAILS", MARGIN_LEFT + 60f, y, boldTextPaint)

        // Col 3: Category (90pt)
        canvas.drawText("CATEGORY", MARGIN_LEFT + 255f, y, boldTextPaint)

        // Col 4: Inflow (+) (60pt right)
        drawTextRight(canvas, "INFLOW (+)", MARGIN_LEFT + 395f, y, boldTextPaint)

        // Col 5: Outflow (-) (60pt right)
        drawTextRight(canvas, "OUTFLOW (-)", MARGIN_LEFT + 460f, y, boldTextPaint)

        // Col 6: Balance (60pt right)
        if (data.includeRunningBalance) {
            drawTextRight(canvas, "BALANCE", PAGE_WIDTH - MARGIN_RIGHT - 6f, y, boldTextPaint)
        }

        boldTextPaint.color = COLOR_TEXT_PRIMARY // reset
        return startY + headerHeight
    }

    private fun drawPassbookEntryRow(
        canvas: Canvas,
        y: Float,
        rowHeight: Float,
        entry: com.folioledger.app.ui.accounts.AccountPassbookEntryUiModel,
        isZebra: Boolean,
        currencySpec: CurrencySpec,
        grouping: NumberGroupingSystem,
        includeNotes: Boolean,
        includeRunningBalance: Boolean
    ) {
        if (isZebra) {
            fillPaint.color = COLOR_ZEBRA_BG
            canvas.drawRect(MARGIN_LEFT, y, PAGE_WIDTH - MARGIN_RIGHT, y + rowHeight, fillPaint)
        }

        val textY = y + 13f
        val tx = entry.transaction

        // Date (DD MMM)
        val localDate = java.time.LocalDate.ofEpochDay(tx.ledgerDateEpochDay)
        val dateText = localDate.format(DateTimeFormatter.ofPattern("dd MMM", Locale.US))
        canvas.drawText(dateText, MARGIN_LEFT + 6f, textY, textPaint)

        // Payee & Description
        val payeeText = tx.payeeOrDescription.ifBlank { tx.categoryName ?: "Transaction" }
        val truncatedPayee = truncateText(payeeText, boldTextPaint, 185f)
        canvas.drawText(truncatedPayee, MARGIN_LEFT + 60f, textY, boldTextPaint)

        if (includeNotes) {
            val subDetails = buildString {
                if (tx.note.isNotBlank()) append(tx.note)
                if (tx.tags.isNotEmpty()) {
                    if (isNotEmpty()) append(" • ")
                    append(tx.tags.joinToString(" ") { "#$it" })
                }
            }
            if (subDetails.isNotBlank()) {
                val truncatedSub = truncateText(subDetails, mutedPaint, 185f)
                canvas.drawText(truncatedSub, MARGIN_LEFT + 60f, textY + 9f, mutedPaint)
            }
        }

        // Category
        val catText = tx.categoryName ?: "Uncategorized"
        val truncatedCat = truncateText(catText, textPaint, 85f)
        canvas.drawText(truncatedCat, MARGIN_LEFT + 255f, textY, textPaint)

        // Inflow (+)
        if (entry.isInflow) {
            val inflowStr = MoneyFormatterAndCalculator.formatMinorMoney(
                entry.signedDelta, currencySpec, grouping, showPlusSignForPositive = false
            )
            val prevColor = textPaint.color
            textPaint.color = COLOR_INFLOW
            drawTextRight(canvas, inflowStr, MARGIN_LEFT + 395f, textY, textPaint)
            textPaint.color = prevColor
        } else {
            drawTextRight(canvas, "—", MARGIN_LEFT + 395f, textY, mutedPaint)
        }

        // Outflow (-)
        if (!entry.isInflow) {
            val outflowMoney = MinorMoney(kotlin.math.abs(entry.signedDelta.amountMinor))
            val outflowStr = MoneyFormatterAndCalculator.formatMinorMoney(
                outflowMoney, currencySpec, grouping, showPlusSignForPositive = false
            )
            val outflowTargetX = if (currencySpec.symbol.length > 2) MARGIN_LEFT + 475f else MARGIN_LEFT + 460f
            drawTextRight(canvas, outflowStr, outflowTargetX, textY, textPaint)
        } else {
            drawTextRight(canvas, "—", MARGIN_LEFT + 460f, textY, mutedPaint)
        }

        // Running Balance
        if (includeRunningBalance) {
            val balanceStr = MoneyFormatterAndCalculator.formatMinorMoney(
                entry.runningBalanceAfter, currencySpec, grouping
            )
            drawTextRight(canvas, balanceStr, PAGE_WIDTH - MARGIN_RIGHT - 6f, textY, boldTextPaint)
        }

        // Subtle row bottom line
        canvas.drawLine(MARGIN_LEFT, y + rowHeight, PAGE_WIDTH - MARGIN_RIGHT, y + rowHeight, strokePaint)
    }

    // ---------------------------------------------------------------------------------------------
    // DRAWING HELPERS: ANNUAL TAX & FINANCIAL REPORT
    // ---------------------------------------------------------------------------------------------

    private fun drawAnnualFinancialSummaryCard(
        canvas: Canvas,
        startY: Float,
        data: AnnualTaxPdfData
    ): Float {
        val cardHeight = 84f
        val cardRect = RectF(MARGIN_LEFT, startY, PAGE_WIDTH - MARGIN_RIGHT, startY + cardHeight)

        fillPaint.color = COLOR_CARD_BG
        canvas.drawRoundRect(cardRect, 6f, 6f, fillPaint)
        canvas.drawRoundRect(cardRect, 6f, 6f, strokePaint)

        val cellWidth = USABLE_WIDTH / 4f
        val row1Y = startY + 16f
        val row1ValY = row1Y + 14f

        // 1. Gross Inflows
        canvas.drawText("GROSS INFLOWS", MARGIN_LEFT + 10f, row1Y, mutedPaint)
        val inflowsText = MoneyFormatterAndCalculator.formatMinorMoney(
            MinorMoney(data.grossInflowsMinor), data.primaryCurrencySpec, data.numberGrouping
        )
        val prevColor = textPaint.color
        textPaint.color = COLOR_INFLOW
        canvas.drawText(inflowsText, MARGIN_LEFT + 10f, row1ValY, textPaint)
        textPaint.color = prevColor

        // 2. Total Expenditures
        val cell2X = MARGIN_LEFT + cellWidth
        canvas.drawText("TOTAL EXPENDITURE", cell2X, row1Y, mutedPaint)
        val expText = MoneyFormatterAndCalculator.formatMinorMoney(
            MinorMoney(data.totalExpensesMinor), data.primaryCurrencySpec, data.numberGrouping
        )
        canvas.drawText(expText, cell2X, row1ValY, textPaint)

        // 3. Net Savings / Surplus
        val cell3X = MARGIN_LEFT + (cellWidth * 2)
        canvas.drawText("NET SAVINGS (SURPLUS)", cell3X, row1Y, mutedPaint)
        val netSavingsText = MoneyFormatterAndCalculator.formatMinorMoney(
            MinorMoney(data.netSavingsMinor), data.primaryCurrencySpec, data.numberGrouping, showPlusSignForPositive = true
        )
        canvas.drawText(netSavingsText, cell3X, row1ValY, boldTextPaint)

        // 4. Year-End Net Worth
        val cell4X = MARGIN_LEFT + (cellWidth * 3)
        canvas.drawText("YEAR-END NET WORTH", cell4X, row1Y, mutedPaint)
        val netWorthText = MoneyFormatterAndCalculator.formatMinorMoney(
            MinorMoney(data.netWorthMinor), data.primaryCurrencySpec, data.numberGrouping
        )
        boldTextPaint.textSize = 10f
        canvas.drawText(netWorthText, cell4X, row1ValY, boldTextPaint)
        boldTextPaint.textSize = 9f

        // Bottom sub-row: Savings Rate % and Total Deductions
        val dividerY = startY + 46f
        canvas.drawLine(MARGIN_LEFT + 10f, dividerY, PAGE_WIDTH - MARGIN_RIGHT - 10f, dividerY, strokePaint)

        val deductionsFormatted = MoneyFormatterAndCalculator.formatMinorMoney(
            MinorMoney(data.totalTaxDeductionsMinor), data.primaryCurrencySpec, data.numberGrouping
        )
        val subLine = "Savings Rate: ${String.format(Locale.US, "%.1f", data.savingsRatePercent)}% • Total Tax Deductions / Business Outflows: $deductionsFormatted"
        canvas.drawText(subLine, MARGIN_LEFT + 10f, startY + 68f, boldTextPaint)

        return startY + cardHeight + 12f
    }

    private fun drawValuationNotesBox(
        canvas: Canvas,
        startY: Float,
        notes: List<String>
    ): Float {
        val boxHeight = 16f + (notes.size * 12f)
        val boxRect = RectF(MARGIN_LEFT, startY, PAGE_WIDTH - MARGIN_RIGHT, startY + boxHeight)

        fillPaint.color = Color.rgb(254, 243, 199) // subtle amber #FEF3C7
        canvas.drawRoundRect(boxRect, 4f, 4f, fillPaint)

        var y = startY + 12f
        boldTextPaint.textSize = 8f
        boldTextPaint.color = Color.rgb(146, 64, 14) // amber text
        canvas.drawText("MULTI-CURRENCY VALUATION BASIS:", MARGIN_LEFT + 8f, y, boldTextPaint)
        boldTextPaint.textSize = 9f
        boldTextPaint.color = COLOR_TEXT_PRIMARY // reset

        mutedPaint.textSize = 8f
        for (note in notes) {
            y += 12f
            canvas.drawText("• $note", MARGIN_LEFT + 8f, y, mutedPaint)
        }
        mutedPaint.textSize = 8f

        return startY + boxHeight + 10f
    }

    private fun drawTaxDeductionsSection(
        canvas: Canvas,
        startY: Float,
        data: AnnualTaxPdfData,
        onEnsureSpace: (Float) -> Unit
    ): Float {
        var y = startY

        onEnsureSpace(50f)
        boldTextPaint.textSize = 11f
        canvas.drawText("ITEMIZED TAX DEDUCTIONS & BUSINESS EXPENSES", MARGIN_LEFT, y + 10f, boldTextPaint)
        boldTextPaint.textSize = 9f
        y += 16f

        // Table Header
        val headerHeight = 18f
        fillPaint.color = COLOR_HEADER_BG
        canvas.drawRect(MARGIN_LEFT, y, PAGE_WIDTH - MARGIN_RIGHT, y + headerHeight, fillPaint)
        boldTextPaint.color = Color.WHITE
        val headerY = y + 12f

        canvas.drawText("DATE", MARGIN_LEFT + 6f, headerY, boldTextPaint)
        canvas.drawText("PAYEE / RECEIPT", MARGIN_LEFT + 65f, headerY, boldTextPaint)
        canvas.drawText("CATEGORY", MARGIN_LEFT + 225f, headerY, boldTextPaint)
        canvas.drawText("TAX TAG / NOTE", MARGIN_LEFT + 320f, headerY, boldTextPaint)
        drawTextRight(canvas, "AMOUNT (${data.primaryCurrencySpec.code})", PAGE_WIDTH - MARGIN_RIGHT - 6f, headerY, boldTextPaint)

        boldTextPaint.color = COLOR_TEXT_PRIMARY
        y += headerHeight

        if (data.taxDeductions.isEmpty()) {
            fillPaint.color = COLOR_CARD_BG
            canvas.drawRect(MARGIN_LEFT, y, PAGE_WIDTH - MARGIN_RIGHT, y + 24f, fillPaint)
            canvas.drawText("No transactions tagged with tax or business deductions found in this fiscal period.", MARGIN_LEFT + 10f, y + 15f, mutedPaint)
            y += 24f
            canvas.drawLine(MARGIN_LEFT, y, PAGE_WIDTH - MARGIN_RIGHT, y, strokePaint)
            return y + 12f
        }

        val rowHeight = 20f
        var index = 0
        for (item in data.taxDeductions) {
            onEnsureSpace(rowHeight)

            if (index % 2 == 1) {
                fillPaint.color = COLOR_ZEBRA_BG
                canvas.drawRect(MARGIN_LEFT, y, PAGE_WIDTH - MARGIN_RIGHT, y + rowHeight, fillPaint)
            }

            val textY = y + 13f
            canvas.drawText(item.dateText, MARGIN_LEFT + 6f, textY, textPaint)

            val payeeText = truncateText(item.payee, boldTextPaint, 155f)
            canvas.drawText(payeeText, MARGIN_LEFT + 65f, textY, boldTextPaint)

            val catText = truncateText(item.categoryName, textPaint, 90f)
            canvas.drawText(catText, MARGIN_LEFT + 225f, textY, textPaint)

            val tagOrNote = truncateText(item.tagOrDeductionType + (item.note?.let { " • $it" } ?: ""), mutedPaint, 110f)
            canvas.drawText(tagOrNote, MARGIN_LEFT + 320f, textY, mutedPaint)

            val amountStr = MoneyFormatterAndCalculator.formatMinorMoney(
                MinorMoney(item.convertedMinor), data.primaryCurrencySpec, data.numberGrouping
            )
            drawTextRight(canvas, amountStr, PAGE_WIDTH - MARGIN_RIGHT - 6f, textY, boldTextPaint)

            canvas.drawLine(MARGIN_LEFT, y + rowHeight, PAGE_WIDTH - MARGIN_RIGHT, y + rowHeight, strokePaint)
            y += rowHeight
            index++
        }

        // Total Deductions Footer Row
        val totalRowHeight = 22f
        fillPaint.color = COLOR_ACCENT_SUBTLE
        canvas.drawRect(MARGIN_LEFT, y, PAGE_WIDTH - MARGIN_RIGHT, y + totalRowHeight, fillPaint)
        boldTextPaint.color = COLOR_PRIMARY_EMERALD
        canvas.drawText("TOTAL DEDUCTIONS & AUDIT EXPENSES", MARGIN_LEFT + 10f, y + 15f, boldTextPaint)
        val totalDeductionStr = MoneyFormatterAndCalculator.formatMinorMoney(
            MinorMoney(data.totalTaxDeductionsMinor), data.primaryCurrencySpec, data.numberGrouping
        )
        drawTextRight(canvas, totalDeductionStr, PAGE_WIDTH - MARGIN_RIGHT - 6f, y + 15f, boldTextPaint)
        boldTextPaint.color = COLOR_TEXT_PRIMARY
        y += totalRowHeight + 14f

        return y
    }

    private fun drawCategoryScheduleSection(
        canvas: Canvas,
        startY: Float,
        sectionTitle: String,
        items: List<CategoryAggregateItem>,
        primaryCurrency: CurrencySpec,
        grouping: NumberGroupingSystem,
        isInflow: Boolean,
        onEnsureSpace: (Float) -> Unit
    ): Float {
        var y = startY
        onEnsureSpace(50f)

        boldTextPaint.textSize = 11f
        canvas.drawText(sectionTitle, MARGIN_LEFT, y + 10f, boldTextPaint)
        boldTextPaint.textSize = 9f
        y += 16f

        // Header
        val headerHeight = 18f
        fillPaint.color = COLOR_HEADER_BG
        canvas.drawRect(MARGIN_LEFT, y, PAGE_WIDTH - MARGIN_RIGHT, y + headerHeight, fillPaint)
        boldTextPaint.color = Color.WHITE
        val headerY = y + 12f

        canvas.drawText("CATEGORY", MARGIN_LEFT + 6f, headerY, boldTextPaint)
        canvas.drawText("ENTRIES", MARGIN_LEFT + 220f, headerY, boldTextPaint)
        drawTextRight(canvas, "% OF TOTAL", MARGIN_LEFT + 360f, headerY, boldTextPaint)
        drawTextRight(canvas, "TOTAL AMOUNT (${primaryCurrency.code})", PAGE_WIDTH - MARGIN_RIGHT - 6f, headerY, boldTextPaint)

        boldTextPaint.color = COLOR_TEXT_PRIMARY
        y += headerHeight

        if (items.isEmpty()) {
            fillPaint.color = COLOR_CARD_BG
            canvas.drawRect(MARGIN_LEFT, y, PAGE_WIDTH - MARGIN_RIGHT, y + 20f, fillPaint)
            canvas.drawText("No items recorded in this schedule.", MARGIN_LEFT + 10f, y + 14f, mutedPaint)
            y += 20f
            return y + 12f
        }

        val rowHeight = 18f
        var index = 0
        for (item in items) {
            onEnsureSpace(rowHeight)

            if (index % 2 == 1) {
                fillPaint.color = COLOR_ZEBRA_BG
                canvas.drawRect(MARGIN_LEFT, y, PAGE_WIDTH - MARGIN_RIGHT, y + rowHeight, fillPaint)
            }

            val textY = y + 12.5f
            canvas.drawText(item.categoryName, MARGIN_LEFT + 6f, textY, boldTextPaint)
            canvas.drawText("${item.count} txns", MARGIN_LEFT + 220f, textY, mutedPaint)

            val pctStr = String.format(Locale.US, "%.1f%%", item.percentageOfTotal)
            drawTextRight(canvas, pctStr, MARGIN_LEFT + 360f, textY, textPaint)

            val amountStr = MoneyFormatterAndCalculator.formatMinorMoney(
                MinorMoney(item.convertedAmountMinor), primaryCurrency, grouping
            )
            if (isInflow) {
                val prev = textPaint.color
                textPaint.color = COLOR_INFLOW
                drawTextRight(canvas, amountStr, PAGE_WIDTH - MARGIN_RIGHT - 6f, textY, textPaint)
                textPaint.color = prev
            } else {
                drawTextRight(canvas, amountStr, PAGE_WIDTH - MARGIN_RIGHT - 6f, textY, textPaint)
            }

            canvas.drawLine(MARGIN_LEFT, y + rowHeight, PAGE_WIDTH - MARGIN_RIGHT, y + rowHeight, strokePaint)
            y += rowHeight
            index++
        }

        return y + 12f
    }

    private fun drawBalanceSheetSection(
        canvas: Canvas,
        startY: Float,
        data: AnnualTaxPdfData,
        onEnsureSpace: (Float) -> Unit
    ): Float {
        var y = startY
        onEnsureSpace(50f)

        boldTextPaint.textSize = 11f
        canvas.drawText("YEAR-END ASSETS, LIABILITIES & WEALTH SNAPSHOT", MARGIN_LEFT, y + 10f, boldTextPaint)
        boldTextPaint.textSize = 9f
        y += 16f

        val headerHeight = 18f
        fillPaint.color = COLOR_HEADER_BG
        canvas.drawRect(MARGIN_LEFT, y, PAGE_WIDTH - MARGIN_RIGHT, y + headerHeight, fillPaint)
        boldTextPaint.color = Color.WHITE
        val headerY = y + 12f

        canvas.drawText("ACCOUNT NAME", MARGIN_LEFT + 6f, headerY, boldTextPaint)
        canvas.drawText("TYPE", MARGIN_LEFT + 200f, headerY, boldTextPaint)
        drawTextRight(canvas, "CLOSING BALANCE", MARGIN_LEFT + 370f, headerY, boldTextPaint)
        drawTextRight(canvas, "CONVERTED (${data.primaryCurrencySpec.code})", PAGE_WIDTH - MARGIN_RIGHT - 6f, headerY, boldTextPaint)

        boldTextPaint.color = COLOR_TEXT_PRIMARY
        y += headerHeight

        val rowHeight = 18f
        var index = 0
        for (acc in data.accountsSnapshot) {
            onEnsureSpace(rowHeight)

            if (index % 2 == 1) {
                fillPaint.color = COLOR_ZEBRA_BG
                canvas.drawRect(MARGIN_LEFT, y, PAGE_WIDTH - MARGIN_RIGHT, y + rowHeight, fillPaint)
            }

            val textY = y + 12.5f
            canvas.drawText(acc.accountName, MARGIN_LEFT + 6f, textY, boldTextPaint)
            canvas.drawText(acc.groupName, MARGIN_LEFT + 200f, textY, mutedPaint)

            // Raw native balance
            val nativeText = "${acc.currencyCode} ${acc.nativeBalanceMinor / 100.0}"
            drawTextRight(canvas, nativeText, MARGIN_LEFT + 370f, textY, textPaint)

            // Converted balance
            val convStr = MoneyFormatterAndCalculator.formatMinorMoney(
                MinorMoney(acc.convertedBalanceMinor), data.primaryCurrencySpec, data.numberGrouping
            )
            drawTextRight(canvas, convStr, PAGE_WIDTH - MARGIN_RIGHT - 6f, textY, boldTextPaint)

            canvas.drawLine(MARGIN_LEFT, y + rowHeight, PAGE_WIDTH - MARGIN_RIGHT, y + rowHeight, strokePaint)
            y += rowHeight
            index++
        }

        // Balance sheet final totals
        val totalsHeight = 22f
        fillPaint.color = COLOR_CARD_BG
        canvas.drawRect(MARGIN_LEFT, y, PAGE_WIDTH - MARGIN_RIGHT, y + totalsHeight, fillPaint)

        val netWorthStr = MoneyFormatterAndCalculator.formatMinorMoney(
            MinorMoney(data.netWorthMinor), data.primaryCurrencySpec, data.numberGrouping
        )
        boldTextPaint.color = COLOR_PRIMARY_EMERALD
        canvas.drawText("NET WORTH AT FISCAL YEAR END", MARGIN_LEFT + 10f, y + 15f, boldTextPaint)
        drawTextRight(canvas, netWorthStr, PAGE_WIDTH - MARGIN_RIGHT - 6f, y + 15f, boldTextPaint)
        boldTextPaint.color = COLOR_TEXT_PRIMARY
        y += totalsHeight

        return y + 16f
    }

    // ---------------------------------------------------------------------------------------------
    // STORAGE & PERSISTENCE
    // ---------------------------------------------------------------------------------------------

    private fun savePdfToStorage(
        context: Context,
        pdfDocument: PdfDocument,
        fileName: String,
        pageCount: Int
    ): PdfExportResult {
        // 1. Internal storage file for 1-tap FileProvider sharing and printing
        val statementsDir = File(context.getExternalFilesDir(null) ?: context.filesDir, "FolioStatements").apply {
            mkdirs()
        }
        val localFile = File(statementsDir, fileName)
        FileOutputStream(localFile).use { out ->
            pdfDocument.writeTo(out)
        }

        // 2. Public Downloads/Folio/Statements copy via MediaStore (API 29+) or direct write
        val publicDisplayPath = runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        "${Environment.DIRECTORY_DOWNLOADS}/Folio/Statements"
                    )
                }
                val uri = context.contentResolver.insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    values
                )
                uri?.let { destUri ->
                    context.contentResolver.openOutputStream(destUri)?.use { destStream ->
                        localFile.inputStream().use { inStream ->
                            inStream.copyTo(destStream)
                        }
                    }
                }
                "Downloads/Folio/Statements/$fileName"
            } else {
                @Suppress("DEPRECATION")
                val pubDir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "Folio/Statements"
                ).apply { mkdirs() }
                val pubFile = File(pubDir, fileName)
                localFile.copyTo(pubFile, overwrite = true)
                pubFile.absolutePath
            }
        }.getOrDefault("Downloads/Folio/Statements/$fileName")

        return PdfExportResult(
            file = localFile,
            publicDisplayPath = publicDisplayPath,
            fileName = fileName,
            fileSizeBytes = localFile.length(),
            pageCount = pageCount
        )
    }

    // ---------------------------------------------------------------------------------------------
    // UTILITY METHODS
    // ---------------------------------------------------------------------------------------------

    private fun drawTextRight(canvas: Canvas, text: String, rightX: Float, y: Float, paint: Paint) {
        val width = paint.measureText(text)
        canvas.drawText(text, rightX - width, y, paint)
    }

    private fun truncateText(text: String, paint: Paint, maxWidth: Float): String {
        if (paint.measureText(text) <= maxWidth) return text
        var low = 0
        var high = text.length
        var best = ""
        while (low <= high) {
            val mid = (low + high) / 2
            val candidate = text.substring(0, mid) + "…"
            if (paint.measureText(candidate) <= maxWidth) {
                best = candidate
                low = mid + 1
            } else {
                high = mid - 1
            }
        }
        return best
    }
}
