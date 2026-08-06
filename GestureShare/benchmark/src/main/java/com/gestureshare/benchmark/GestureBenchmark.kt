package com.gestureshare.benchmark

import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class GestureBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun gestureRecognitionLatency() {
        benchmarkRule.measureRepeated(
            packageName = "com.gestureshare",
            metrics = listOf(
                androidx.benchmark.macro.FrameTimingMetric()
            ),
            iterations = 5,
            setupBlock = {
                pressHome()
            }
        ) {
            startActivityAndWait()
        }
    }
}
