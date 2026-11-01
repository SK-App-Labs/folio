package com.folioledger.app.macrobenchmark

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class BaselineProfileGenerator {

    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generateBaselineProfile() {
        baselineProfileRule.collect(
            packageName = PACKAGE_NAME,
            includeInStartupProfile = true
        ) {
            // 1. Cold launch the app
            pressHome()
            startActivityAndWait()

            // 2. Wait until main screen UI loads
            device.waitForIdle()

            // 3. Perform fling scroll journey on transaction feed
            val scrollableList = device.findObject(By.scrollable(true))
            scrollableList?.let {
                it.setGestureMargin(device.displayWidth / 10)
                it.fling(Direction.DOWN)
                device.waitForIdle()
                it.fling(Direction.UP)
                device.waitForIdle()
            }
        }
    }

    companion object {
        private const val PACKAGE_NAME = "com.skal.folio"
    }
}
