package com.folioledger.app.macrobenchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.ExperimentalMetricApi
import androidx.benchmark.macro.MemoryUsageMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalMetricApi::class)
@RunWith(AndroidJUnit4::class)
@LargeTest
class MemoryAndTraceBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    // Measures peak RAM footprint after navigating primary navigation destinations
    @Test
    fun measureAppMemoryFootprint() {
        benchmarkRule.measureRepeated(
            packageName = PACKAGE_NAME,
            metrics = listOf(MemoryUsageMetric(MemoryUsageMetric.Mode.Last)),
            compilationMode = CompilationMode.Partial(),
            startupMode = StartupMode.COLD,
            iterations = 3
        ) {
            pressHome()
            startActivityAndWait()
            device.waitForIdle()

            // Navigate to Stats
            device.findObject(By.text("Stats"))?.click()
            device.waitForIdle()

            // Navigate to Accounts
            device.findObject(By.text("Accounts"))?.click()
            device.waitForIdle()

            // Return to Transactions
            device.findObject(By.text("Trans."))?.click()
            device.waitForIdle()
        }
    }

    companion object {
        private const val PACKAGE_NAME = "com.skal.folio"
    }
}
