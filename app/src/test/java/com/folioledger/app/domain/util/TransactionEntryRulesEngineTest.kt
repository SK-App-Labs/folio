package com.folioledger.app.domain.util

import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.SplitLineItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class TransactionEntryRulesEngineTest {

    @Test
    fun validateSplitTransaction_zeroParentTotal_returnsInvalid() {
        val splits = listOf(
            SplitLineItem(categoryId = 1L, categoryName = "Food", amount = MinorMoney(5000L)),
            SplitLineItem(categoryId = 2L, categoryName = "Fuel", amount = MinorMoney(5000L))
        )
        val result = TransactionEntryRulesEngine.validateSplitTransaction(MinorMoney.ZERO, splits)
        assertFalse(result.isValid)
        assertEquals("Enter a positive transaction total before splitting.", result.errorMessage)
    }

    @Test
    fun validateSplitTransaction_lessThanTwoSplits_returnsInvalid() {
        val splits = listOf(
            SplitLineItem(categoryId = 1L, categoryName = "Food", amount = MinorMoney(10000L))
        )
        val result = TransactionEntryRulesEngine.validateSplitTransaction(MinorMoney(10000L), splits)
        assertFalse(result.isValid)
        assertEquals("A split transaction requires at least 2 split rows.", result.errorMessage)
    }

    @Test
    fun validateSplitTransaction_missingCategory_returnsInvalid() {
        val splits = listOf(
            SplitLineItem(categoryId = 0L, categoryName = "", amount = MinorMoney(5000L)),
            SplitLineItem(categoryId = 2L, categoryName = "Fuel", amount = MinorMoney(5000L))
        )
        val result = TransactionEntryRulesEngine.validateSplitTransaction(MinorMoney(10000L), splits)
        assertFalse(result.isValid)
        assertEquals("Every split line must have a category selected.", result.errorMessage)
    }

    @Test
    fun validateSplitTransaction_zeroLineAmount_returnsInvalid() {
        val splits = listOf(
            SplitLineItem(categoryId = 1L, categoryName = "Food", amount = MinorMoney.ZERO),
            SplitLineItem(categoryId = 2L, categoryName = "Fuel", amount = MinorMoney(10000L))
        )
        val result = TransactionEntryRulesEngine.validateSplitTransaction(MinorMoney(10000L), splits)
        assertFalse(result.isValid)
        assertEquals("Every split line amount must be strictly greater than zero.", result.errorMessage)
    }

    @Test
    fun validateSplitTransaction_underAllocated_returnsInvalid() {
        val splits = listOf(
            SplitLineItem(categoryId = 1L, categoryName = "Food", amount = MinorMoney(4000L)),
            SplitLineItem(categoryId = 2L, categoryName = "Fuel", amount = MinorMoney(4000L))
        )
        val result = TransactionEntryRulesEngine.validateSplitTransaction(MinorMoney(10000L), splits)
        assertFalse(result.isValid)
        assertEquals("Split lines are under-allocated.", result.errorMessage)
        assertEquals(2000L, result.remainingUnallocated.amountMinor)
    }

    @Test
    fun validateSplitTransaction_overAllocated_returnsInvalid() {
        val splits = listOf(
            SplitLineItem(categoryId = 1L, categoryName = "Food", amount = MinorMoney(6000L)),
            SplitLineItem(categoryId = 2L, categoryName = "Fuel", amount = MinorMoney(5000L))
        )
        val result = TransactionEntryRulesEngine.validateSplitTransaction(MinorMoney(10000L), splits)
        assertFalse(result.isValid)
        assertEquals("Split lines exceed the transaction total.", result.errorMessage)
        assertEquals(-1000L, result.remainingUnallocated.amountMinor)
    }

    @Test
    fun validateSplitTransaction_exactMatch_returnsValid() {
        val splits = listOf(
            SplitLineItem(categoryId = 1L, categoryName = "Food", amount = MinorMoney(6000L)),
            SplitLineItem(categoryId = 2L, categoryName = "Fuel", amount = MinorMoney(4000L))
        )
        val result = TransactionEntryRulesEngine.validateSplitTransaction(MinorMoney(10000L), splits)
        assertTrue(result.isValid)
        assertNull(result.errorMessage)
        assertEquals(10000L, result.allocatedTotal.amountMinor)
        assertEquals(0L, result.remainingUnallocated.amountMinor)
    }

    @Test
    fun generateInstallmentSchedule_dividesAmountWithRemainderOnFirst() {
        val startEpoch = LocalDate.of(2026, 6, 1).toEpochDay()
        val total = MinorMoney(10000L) // 100.00 split over 3 months -> 33.34, 33.33, 33.33

        val schedule = TransactionEntryRulesEngine.generateInstallmentSchedule(
            totalAmount = total,
            monthsCount = 3,
            startDateEpochDay = startEpoch
        )

        assertEquals(3, schedule.size)
        assertEquals(3334L, schedule[0].amount.amountMinor)
        assertEquals(3333L, schedule[1].amount.amountMinor)
        assertEquals(3333L, schedule[2].amount.amountMinor)

        // Verify dates progression
        assertEquals(LocalDate.of(2026, 6, 1).toEpochDay(), schedule[0].dueDateEpochDay)
        assertEquals(LocalDate.of(2026, 7, 1).toEpochDay(), schedule[1].dueDateEpochDay)
        assertEquals(LocalDate.of(2026, 8, 1).toEpochDay(), schedule[2].dueDateEpochDay)
    }
}
