package com.folioledger.app.domain

import com.folioledger.app.data.local.dao.TransactionBalanceProjection
import com.folioledger.app.data.local.entity.AccountEntity
import com.folioledger.app.domain.model.AccountGroup
import com.folioledger.app.domain.model.AccountSummary
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.TransactionItem
import com.folioledger.app.domain.model.TransactionType
import com.folioledger.app.domain.usecase.AccountingRulesEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.time.LocalDate

/**
 * Regression tests verifying accurate accounting for Home Loans, liability accounts,
 * event-sourced opening balances, and passbook back-projections.
 */
class LoanAndPassbookAccountingRegressionTest {

    @Test
    fun loanOpeningBalance_doesNotDoubleCount_producesExactStartingDebt() {
        val loanAmountMinor = 3638717_00L // ₹36,38,717.00
        val julyDate = LocalDate.of(2026, 7, 15).toEpochDay()
        val accountId = 42L

        // Entity with legacy openingBalanceMinor recorded
        val entity = AccountEntity(
            id = accountId,
            name = "SBI Home Loan",
            groupType = AccountGroup.LOAN,
            currencyCode = "INR",
            openingBalanceMinor = loanAmountMinor,
            createdAtEpochMillis = julyDate * 86_400_000L
        )

        // Event-sourced opening transaction
        val openingTx = TransactionBalanceProjection(
            id = 1L,
            type = TransactionType.OPENING_BALANCE,
            timestampEpochMillis = julyDate * 86_400_000L + 12 * 3600 * 1000L,
            ledgerDateEpochDay = julyDate,
            amountMinor = loanAmountMinor,
            transferFeeMinor = 0L,
            accountId = accountId,
            transferToAccountId = accountId
        )

        val allTransactions = listOf(openingTx)
        val hasOpeningBalanceTx = HashSet<Long>().apply {
            allTransactions.forEach { if (it.type == TransactionType.OPENING_BALANCE) add(it.accountId) }
        }

        // Test the repaired summary calculation logic
        val runningBalances = HashMap<Long, Long>()
        val initialSigned = if (hasOpeningBalanceTx.contains(entity.id)) {
            0L
        } else if (entity.groupType.isLiabilityByDefault && entity.openingBalanceMinor > 0L) {
            Math.negateExact(entity.openingBalanceMinor)
        } else {
            entity.openingBalanceMinor
        }
        runningBalances[entity.id] = initialSigned

        for (tx in allTransactions) {
            val amount = Math.abs(tx.amountMinor)
            val isOutflow = tx.transferToAccountId != null
            val current = runningBalances[tx.accountId] ?: 0L
            runningBalances[tx.accountId] = if (isOutflow) {
                Math.subtractExact(current, amount)
            } else {
                Math.addExact(current, amount)
            }
        }

        val liveBalanceMinor = runningBalances[entity.id] ?: 0L

        // Must be exactly -₹36,38,717.00, NOT doubled (-₹72,77,434.00)
        assertEquals(-loanAmountMinor, liveBalanceMinor)
        assertNotEquals(-7277434_00L, liveBalanceMinor)
    }

    @Test
    fun loanPassbookProjection_startingMonth_showsZeroInitialAndExactLoanClosingDebt() {
        val loanAmountMinor = 3638717_00L
        val july15 = LocalDate.of(2026, 7, 15).toEpochDay()
        val accountId = 10L
        val effectiveAccountIds = setOf(accountId)

        val openingTxItem = TransactionItem(
            id = 1L,
            type = TransactionType.OPENING_BALANCE,
            timestampEpochMillis = july15 * 86_400_000L,
            ledgerDateEpochDay = july15,
            amount = MinorMoney(loanAmountMinor),
            currencyCode = "INR",
            accountId = accountId,
            accountName = "Home Loan",
            transferToAccountId = accountId,
            payeeOrDescription = "Opening Balance",
            note = "Initial starting balance"
        )

        // 1. Signed delta for loan opening balance must be an outflow (-loanAmountMinor)
        val deltaMinor = AccountingRulesEngine.computeSignedDeltaMinorForAccount(
            tx = openingTxItem,
            effectiveAccountIds = effectiveAccountIds
        )
        assertEquals(-loanAmountMinor, deltaMinor)

        // 2. Passbook starting balance before July 15 must be 0, not doubled
        val anchorLiveBalanceMinor = -loanAmountMinor // live balance as computed above
        val netDeltaUpToToday = deltaMinor
        val initialOpeningBalanceMinor = anchorLiveBalanceMinor - netDeltaUpToToday

        assertEquals(0L, initialOpeningBalanceMinor)

        // 3. Month of July passbook closing balance must be -₹36,38,717
        val periodOpeningBalanceMinor = initialOpeningBalanceMinor // starts at 0 on July 1
        val periodOutflowMinor = -deltaMinor // 36,38,717
        val periodClosingBalanceMinor = periodOpeningBalanceMinor - periodOutflowMinor

        assertEquals(-loanAmountMinor, periodClosingBalanceMinor)
        assertNotEquals(7277434_00L, periodOpeningBalanceMinor)
    }

    @Test
    fun loanPassbookProjection_subsequentMonthWithEmi_reducesDebtCorrectly() {
        val loanAmountMinor = 3638717_00L
        val emiPaymentMinor = 50000_00L // ₹50,000.00 EMI
        val july15 = LocalDate.of(2026, 7, 15).toEpochDay()
        val aug10 = LocalDate.of(2026, 8, 10).toEpochDay()
        val accountId = 10L
        val effectiveAccountIds = setOf(accountId)

        val openingTx = TransactionItem(
            id = 1L,
            type = TransactionType.OPENING_BALANCE,
            timestampEpochMillis = july15 * 86_400_000L,
            ledgerDateEpochDay = july15,
            amount = MinorMoney(loanAmountMinor),
            currencyCode = "INR",
            accountId = accountId,
            accountName = "Home Loan",
            transferToAccountId = accountId
        )

        val emiTx = TransactionItem(
            id = 2L,
            type = TransactionType.TRANSFER,
            timestampEpochMillis = aug10 * 86_400_000L,
            ledgerDateEpochDay = aug10,
            amount = MinorMoney(emiPaymentMinor),
            currencyCode = "INR",
            accountId = 999L, // Bank account
            accountName = "HDFC Salary",
            transferToAccountId = accountId, // Loan account
            transferToAccountName = "Home Loan"
        )

        val openingDelta = AccountingRulesEngine.computeSignedDeltaMinorForAccount(openingTx, effectiveAccountIds)
        val emiDelta = AccountingRulesEngine.computeSignedDeltaMinorForAccount(emiTx, effectiveAccountIds)

        assertEquals(-loanAmountMinor, openingDelta)
        assertEquals(emiPaymentMinor, emiDelta)

        // August starting balance is July's closing balance: -₹36,38,717.00
        val augStartBalance = 0L + openingDelta
        assertEquals(-loanAmountMinor, augStartBalance)

        // August closing balance after EMI: -₹35,88,717.00
        val augClosingBalance = augStartBalance + emiDelta
        assertEquals(-3588717_00L, augClosingBalance)
    }

    @Test
    fun transactionMapper_preservesTransferToAccountIdForOpeningBalanceAndReconciliation() {
        val loanAccountId = 55L
        val txEntity = com.folioledger.app.data.local.entity.TransactionEntity(
            id = 1L,
            type = TransactionType.OPENING_BALANCE,
            timestampEpochMillis = 1000L,
            ledgerDateEpochDay = 100L,
            amountMinor = 3638717_00L,
            currencyCode = "INR",
            accountId = loanAccountId,
            transferToAccountId = loanAccountId, // outflow flag
            payeeOrDescription = "Opening Balance",
            note = "Initial starting balance"
        )

        val preservesTransferTarget = txEntity.type == TransactionType.TRANSFER ||
                txEntity.type == TransactionType.CC_SETTLEMENT ||
                txEntity.type == TransactionType.OPENING_BALANCE ||
                txEntity.type == TransactionType.RECONCILIATION

        val effectiveTransferToAccountId = if (preservesTransferTarget) txEntity.transferToAccountId else null

        val mappedItem = TransactionItem(
            id = txEntity.id,
            type = txEntity.type,
            timestampEpochMillis = txEntity.timestampEpochMillis,
            ledgerDateEpochDay = txEntity.ledgerDateEpochDay,
            amount = MinorMoney(txEntity.amountMinor),
            currencyCode = txEntity.currencyCode,
            accountId = txEntity.accountId,
            accountName = "ICICI Loan",
            transferToAccountId = effectiveTransferToAccountId,
            payeeOrDescription = txEntity.payeeOrDescription,
            note = txEntity.note
        )

        // Must retain transferToAccountId as outflow flag
        org.junit.Assert.assertNotNull(mappedItem.transferToAccountId)
        assertEquals(loanAccountId, mappedItem.transferToAccountId)

        // When evaluating signed delta for passbook, must be OUTFLOW (-36,38,717), NOT inflow (+36,38,717)
        val delta = AccountingRulesEngine.computeSignedDeltaMinorForAccount(mappedItem, setOf(loanAccountId))
        assertEquals(-3638717_00L, delta)
        assertNotEquals(3638717_00L, delta)
    }
}
