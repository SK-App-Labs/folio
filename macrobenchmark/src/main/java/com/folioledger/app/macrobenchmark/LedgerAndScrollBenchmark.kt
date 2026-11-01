package com.folioledger.app.macrobenchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class LedgerAndScrollBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun measureLedgerFeedScrollJank() {
        benchmarkRule.measureRepeated(
            packageName = PACKAGE_NAME,
            metrics = listOf(FrameTimingMetric()),
            compilationMode = CompilationMode.Partial(),
            startupMode = StartupMode.WARM,
            iterations = 5
        ) {
            pressHome()
            startActivityAndWait()
            device.waitForIdle()

            // Fling scroll down and up on the active ledger list
            val list = device.findObject(By.scrollable(true))
            list?.let {
                it.setGestureMargin(device.displayWidth / 10)
                it.fling(Direction.DOWN)
                device.waitForIdle()
                it.fling(Direction.UP)
                device.waitForIdle()
            }
        }
    }

    @Test
    fun measureLedgerSubtabSwitchingJank() {
        benchmarkRule.measureRepeated(
            packageName = PACKAGE_NAME,
            metrics = listOf(FrameTimingMetric()),
            compilationMode = CompilationMode.Partial(),
            startupMode = StartupMode.WARM,
            iterations = 5
        ) {
            pressHome()
            startActivityAndWait()
            device.waitForIdle()

            // Switch subtabs in the Ledger header
            device.findObject(By.text("Calendar"))?.click()
            device.waitForIdle()

            device.findObject(By.text("Weekly"))?.click()
            device.waitForIdle()

            device.findObject(By.text("Monthly"))?.click()
            device.waitForIdle()

            device.findObject(By.text("Summary"))?.click()
            device.waitForIdle()

            device.findObject(By.text("Daily"))?.click()
            device.waitForIdle()
        }
    }

    companion object {
        private const val PACKAGE_NAME = "com.skal.folio"
    }
}
