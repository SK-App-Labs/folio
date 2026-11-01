package com.folioledger.app.macrobenchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class AnalyticsAndChartsBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun measureStatsChartsAndSubtabSwitching() {
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

            // 1. Open Stats Screen from bottom navigation
            device.findObject(By.text("Stats"))?.click()
            device.waitForIdle()

            // 2. Switch to Cards subtab (Credit Cards Cockpit with arc gauge canvas)
            device.findObject(By.text("Cards"))?.click()
            device.waitForIdle()

            // 3. Switch to Budgets subtab
            device.findObject(By.text("Budgets"))?.click()
            device.waitForIdle()

            // 4. Return to Overview subtab (Aggregates donut charts and trend bar charts)
            device.findObject(By.text("Overview"))?.click()
            device.waitForIdle()
        }
    }

    companion object {
        private const val PACKAGE_NAME = "com.skal.folio"
    }
}
