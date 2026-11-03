package com.folioledger.app.data

import com.folioledger.app.data.repository.runCatchingNonCancellation
import kotlinx.coroutines.CancellationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.IOException

class RepositoryExtensionsTest {

    @Test
    fun `runCatchingNonCancellation returns Result success on regular return`() {
        val result = runCatchingNonCancellation {
            42
        }
        assertTrue(result.isSuccess)
        assertEquals(42, result.getOrNull())
    }

    @Test
    fun `runCatchingNonCancellation returns Result failure on standard exception`() {
        val result = runCatchingNonCancellation {
            throw IOException("Disk error")
        }
        assertTrue(result.isFailure)
        assertEquals("Disk error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `runCatchingNonCancellation rethrows CancellationException for coroutine structured concurrency`() {
        try {
            runCatchingNonCancellation {
                throw CancellationException("Job was cancelled")
            }
            fail("Expected CancellationException to be rethrown, but it was caught")
        } catch (e: CancellationException) {
            assertEquals("Job was cancelled", e.message)
        }
    }
}
