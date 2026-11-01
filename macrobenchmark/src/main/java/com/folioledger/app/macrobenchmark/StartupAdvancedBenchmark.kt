package com.folioledger.app.macrobenchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class StartupAdvancedBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    // 1. Cold Startup WITHOUT Baseline Profile
    @Test
    fun coldStartupNoCompilation() = measureStartup(
        compilationMode = CompilationMode.None(),
        startupMode = StartupMode.COLD
    )

    // 2. Cold Startup WITH Baseline Profile (Pre-compiled)
    @Test
    fun coldStartupBaselineProfile() = measureStartup(
        compilationMode = CompilationMode.Partial(),
        startupMode = StartupMode.COLD
    )

    // 3. Warm Startup (App resuming from background)
    @Test
    fun warmStartupBaselineProfile() = measureStartup(
        compilationMode = CompilationMode.Partial(),
        startupMode = StartupMode.WARM
    )

    // 4. Hot Startup (App brought from backstack)
    @Test
    fun hotStartupBaselineProfile() = measureStartup(
        compilationMode = CompilationMode.Partial(),
        startupMode = StartupMode.HOT
    )

    private fun measureStartup(compilationMode: CompilationMode, startupMode: StartupMode) {
        benchmarkRule.measureRepeated(
            packageName = PACKAGE_NAME,
            metrics = listOf(StartupTimingMetric(), FrameTimingMetric()),
            compilationMode = compilationMode,
            startupMode = startupMode,
            iterations = 5
        ) {
            pressHome()
            startActivityAndWait()
        }
    }

    companion object {
        private const val PACKAGE_NAME = "com.skal.folio"
    }
}
