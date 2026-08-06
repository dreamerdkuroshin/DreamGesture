package com.gestureshare.feature.gesture

import kotlin.math.max
import kotlin.math.min

class KalmanFilter(
    private val processNoise: Float = 0.01f,
    private val measurementNoise: Float = 0.1f
) {
    private var x: Float = 0f
    private var y: Float = 0f
    private var z: Float = 0f
    private var p: Float = 1f
    private var initialized: Boolean = false

    fun update(measurementX: Float, measurementY: Float, measurementZ: Float): Triple<Float, Float, Float> {
        if (!initialized) {
            x = measurementX
            y = measurementY
            z = measurementZ
            p = 1f
            initialized = true
            return Triple(x, y, z)
        }

        val predictedP = p + processNoise
        val kalmanGain = predictedP / (predictedP + measurementNoise)

        x = x + kalmanGain * (measurementX - x)
        y = y + kalmanGain * (measurementY - y)
        z = z + kalmanGain * (measurementZ - z)

        p = (1 - kalmanGain) * predictedP

        p = max(0.001f, min(p, 10f))

        return Triple(x, y, z)
    }

    fun reset() {
        x = 0f
        y = 0f
        z = 0f
        p = 1f
        initialized = false
    }
}

class MultiLandmarkKalmanFilter(
    numLandmarks: Int,
    processNoise: Float = 0.01f,
    measurementNoise: Float = 0.1f
) {
    private val filters = Array(numLandmarks) { KalmanFilter(processNoise, measurementNoise) }

    fun updateAll(measurements: List<Triple<Float, Float, Float>>): List<Triple<Float, Float, Float>> {
        return measurements.mapIndexed { index, measurement ->
            if (index < filters.size) {
                filters[index].update(measurement.first, measurement.second, measurement.third)
            } else {
                measurement
            }
        }
    }

    fun reset() {
        filters.forEach { it.reset() }
    }
}
