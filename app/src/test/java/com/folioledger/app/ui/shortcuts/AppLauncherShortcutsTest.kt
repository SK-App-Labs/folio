package com.folioledger.app.ui.shortcuts

import com.folioledger.app.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Locale

class AppLauncherShortcutsTest {

    private fun extractShortcutAction(actionStr: String?): TransactionType? {
        if (actionStr.isNullOrBlank()) return null
        return when (actionStr.uppercase(Locale.US)) {
            "EXPENSE" -> TransactionType.EXPENSE
            "INCOME" -> TransactionType.INCOME
            "TRANSFER" -> TransactionType.TRANSFER
            else -> null
        }
    }

    @Test
    fun extractShortcutAction_validActions_mappedToCorrectTransactionType() {
        assertEquals(TransactionType.EXPENSE, extractShortcutAction("EXPENSE"))
        assertEquals(TransactionType.EXPENSE, extractShortcutAction("expense"))
        assertEquals(TransactionType.INCOME, extractShortcutAction("INCOME"))
        assertEquals(TransactionType.INCOME, extractShortcutAction("income"))
        assertEquals(TransactionType.TRANSFER, extractShortcutAction("TRANSFER"))
        assertEquals(TransactionType.TRANSFER, extractShortcutAction("transfer"))
    }

    @Test
    fun extractShortcutAction_invalidOrNull_returnsNull() {
        assertNull(extractShortcutAction(null))
        assertNull(extractShortcutAction(""))
        assertNull(extractShortcutAction("   "))
        assertNull(extractShortcutAction("SETTINGS"))
        assertNull(extractShortcutAction("UNKNOWN"))
    }
}
