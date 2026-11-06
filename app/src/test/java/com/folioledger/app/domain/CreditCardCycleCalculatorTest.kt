// File: app/src/test/java/com/folioledger/app/domain/CreditCardCycleCalculatorTest.kt
package com.folioledger.app.domain

import com.folioledger.app.domain.util.CreditCardCycleCalculator
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CreditCardCycleCalculatorTest {

    @Test
    fun testCycleWindow_beforeStatementClose() {
        // Statement closes on 15th, payment due on 5th of following month.
        // Today is 2026-10-10 (before statement close).
        val refDate = LocalDate.of(2026, 10, 10)
        val cycle = CreditCardCycleCalculator.computeCycleWindow(
            referenceDateEpochDay = refDate.toEpochDay(),
            settlementDayOfMonth = 15,
            paymentDueDayOfMonth = 5
        )

        // Last closed statement was September 15th
        assertEquals(LocalDate.of(2026, 9, 15), cycle.lastStatementEnd)
        // Active cycle ends October 15th
        assertEquals(LocalDate.of(2026, 10, 15), cycle.activeCycleEnd)
        // Next due date for active cycle is November 5th
        assertEquals(LocalDate.of(2026, 11, 5), cycle.activeCycleNextDueDate)
    }

    @Test
    fun testCycleWindow_afterStatementClose() {
        // Statement closes on 15th, payment due on 5th of following month.
        // Today is 2026-10-20 (after statement close).
        val refDate = LocalDate.of(2026, 10, 20)
        val cycle = CreditCardCycleCalculator.computeCycleWindow(
            referenceDateEpochDay = refDate.toEpochDay(),
            settlementDayOfMonth = 15,
            paymentDueDayOfMonth = 5
        )

        // Last closed statement was October 15th
        assertEquals(LocalDate.of(2026, 10, 15), cycle.lastStatementEnd)
        // Due date for the last statement is November 5th
        assertEquals(LocalDate.of(2026, 11, 5), cycle.lastStatementDueDate)
        // Active cycle ends November 15th
        assertEquals(LocalDate.of(2026, 11, 15), cycle.activeCycleEnd)
    }

    @Test
    fun testCycleWindow_monthEndClamping() {
        // Statement day set to 31.
        // In April (30 days), it should clamp to 30.
        val refDate = LocalDate.of(2026, 4, 10)
        val cycle = CreditCardCycleCalculator.computeCycleWindow(
            referenceDateEpochDay = refDate.toEpochDay(),
            settlementDayOfMonth = 31,
            paymentDueDayOfMonth = 20
        )

        // March has 31 days
        assertEquals(LocalDate.of(2026, 3, 31), cycle.lastStatementEnd)
        // April has 30 days -> clamped to 30
        assertEquals(LocalDate.of(2026, 4, 30), cycle.activeCycleEnd)
    }
}
