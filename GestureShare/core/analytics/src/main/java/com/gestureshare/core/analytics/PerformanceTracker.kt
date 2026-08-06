package com.gestureshare.core.analytics

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PerformanceTracker @Inject constructor() {

    private val metrics = mutableMapOf<String, Long>()

    fun startTimer(label: String) {
        metrics["${label}_start"] = System.nanoTime()
    }

    fun stopTimer(label: String): Long {
        val start = metrics["${label}_start"] ?: return -1
        val elapsed = (System.nanoTime() - start) / 1_000_000
        metrics[label] = elapsed
        return elapsed
    }

    fun recordMetric(name: String, value: Long) {
        metrics[name] = value
    }

    fun getMetric(name: String): Long? = metrics[name]

    fun getAllMetrics(): Map<String, Long> = metrics.toMap()

    fun reset() {
        metrics.clear()
    }
}
