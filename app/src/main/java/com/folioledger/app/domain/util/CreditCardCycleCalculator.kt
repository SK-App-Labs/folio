// File: app/src/main/java/com/folioledger/app/domain/util/CreditCardCycleCalculator.kt
package com.folioledger.app.domain.util

import androidx.compose.runtime.Immutable
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Immutable
data class CreditCardCycleWindow(
    val statementDayOfMonth: Int,
    val paymentDueDayOfMonth: Int,
    val lastStatementStart: LocalDate,
    val lastStatementEnd: LocalDate,
    val lastStatementDueDate: LocalDate,
    val activeCycleStart: LocalDate,
    val activeCycleEnd: LocalDate,
    val activeCycleNextDueDate: LocalDate
) {
    val lastStatementRangeShortText: String
        get() = "${lastStatementStart.format(shortFormatter)} – ${lastStatementEnd.format(shortFormatter)}"

    val lastStatementRangeFullText: String
        get() = "${lastStatementStart.format(fullFormatter)} – ${lastStatementEnd.format(fullFormatter)}"

    val lastStatementDueShortText: String
        get() = lastStatementDueDate.format(shortFormatter)

    val lastStatementDueFullText: String
        get() = lastStatementDueDate.format(fullFormatter)

    val activeCycleRangeShortText: String
        get() = "${activeCycleStart.format(shortFormatter)} – ${activeCycleEnd.format(shortFormatter)}"

    val activeCycleRangeFullText: String
        get() = "${activeCycleStart.format(fullFormatter)} – ${activeCycleEnd.format(fullFormatter)}"

    @Suppress("unused")
    val activeCycleNextDueFullText: String
        get() = activeCycleNextDueDate.format(fullFormatter)

    companion object {
        private val shortFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM", Locale.US)
        private val fullFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.US)
    }
}

object CreditCardCycleCalculator {

    fun resolveClampedDate(yearMonth: YearMonth, desiredDayOfMonth: Int): LocalDate {
        val safeDay = desiredDayOfMonth.coerceIn(1, 31)
        val clampedDay = minOf(safeDay, yearMonth.lengthOfMonth())
        return yearMonth.atDay(clampedDay)
    }

    fun computeCycleWindow(
        referenceDateEpochDay: Long,
        settlementDayOfMonth: Int,
        paymentDueDayOfMonth: Int
    ): CreditCardCycleWindow {
        val safeStatementDay = settlementDayOfMonth.coerceIn(1, 31)
        val safeDueDay = paymentDueDayOfMonth.coerceIn(1, 31)
        val refDate = LocalDate.ofEpochDay(referenceDateEpochDay)
        val refYearMonth = YearMonth.from(refDate)
        val statementDateInRefMonth = resolveClampedDate(refYearMonth, safeStatementDay)

        val lastStatementEnd: LocalDate = if (refDate.isAfter(statementDateInRefMonth)) {
            statementDateInRefMonth
        } else {
            resolveClampedDate(refYearMonth.minusMonths(1), safeStatementDay)
        }

        val prevStatementEnd = resolveClampedDate(
            YearMonth.from(lastStatementEnd).minusMonths(1),
            safeStatementDay
        )
        val lastStatementStart = prevStatementEnd.plusDays(1)

        val activeCycleStart = lastStatementEnd.plusDays(1)
        val activeCycleEnd = resolveClampedDate(
            YearMonth.from(lastStatementEnd).plusMonths(1),
            safeStatementDay
        )

        val lastStatementDueDate = computeDueDateAfterStatementClose(
            statementCloseDate = lastStatementEnd,
            dueDayOfMonth = safeDueDay
        )
        val activeCycleNextDueDate = computeDueDateAfterStatementClose(
            statementCloseDate = activeCycleEnd,
            dueDayOfMonth = safeDueDay
        )

        return CreditCardCycleWindow(
            statementDayOfMonth = safeStatementDay,
            paymentDueDayOfMonth = safeDueDay,
            lastStatementStart = lastStatementStart,
            lastStatementEnd = lastStatementEnd,
            lastStatementDueDate = lastStatementDueDate,
            activeCycleStart = activeCycleStart,
            activeCycleEnd = activeCycleEnd,
            activeCycleNextDueDate = activeCycleNextDueDate
        )
    }

    private fun computeDueDateAfterStatementClose(
        statementCloseDate: LocalDate,
        dueDayOfMonth: Int
    ): LocalDate {
        val closeYearMonth = YearMonth.from(statementCloseDate)
        val sameMonthCandidate = resolveClampedDate(closeYearMonth, dueDayOfMonth)
        return if (sameMonthCandidate.isAfter(statementCloseDate)) {
            sameMonthCandidate
        } else {
            resolveClampedDate(closeYearMonth.plusMonths(1), dueDayOfMonth)
        }
    }
}
