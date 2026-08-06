package com.gestureshare.feature.gesture

import org.junit.Before
import org.junit.Test
import com.google.common.truth.Truth.assertThat
import kotlin.math.abs

class KalmanFilterTest {

    private lateinit var filter: KalmanFilter

    @Before
    fun setup() {
        filter = KalmanFilter(processNoise = 0.01f, measurementNoise = 0.1f)
    }

    @Test
    fun `first update returns measurement directly`() {
        val result = filter.update(1.0f, 2.0f, 3.0f)
        assertThat(result.first).isWithin(0.01f).of(1.0f)
        assertThat(result.second).isWithin(0.01f).of(2.0f)
        assertThat(result.third).isWithin(0.01f).of(3.0f)
    }

    @Test
    fun `filter smooths noisy measurements`() {
        filter.update(1.0f, 1.0f, 1.0f)
        filter.update(1.1f, 0.9f, 1.05f)
        filter.update(0.95f, 1.05f, 0.98f)
        filter.update(1.02f, 0.97f, 1.01f)

        val result = filter.update(1.0f, 1.0f, 1.0f)
        assertThat(abs(result.first - 1.0f)).isLessThan(0.1f)
        assertThat(abs(result.second - 1.0f)).isLessThan(0.1f)
        assertThat(abs(result.third - 1.0f)).isLessThan(0.1f)
    }

    @Test
    fun `reset clears filter state`() {
        filter.update(5.0f, 5.0f, 5.0f)
        filter.reset()
        val result = filter.update(1.0f, 1.0f, 1.0f)
        assertThat(result.first).isWithin(0.01f).of(1.0f)
    }
}
