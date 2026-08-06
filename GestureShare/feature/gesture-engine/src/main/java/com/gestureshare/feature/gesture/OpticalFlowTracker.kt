package com.gestureshare.feature.gesture

import com.gestureshare.core.domain.model.HandLandmark
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.sqrt

class OpticalFlowTracker {

    data class FlowVector(val dx: Float, val dy: Float, val magnitude: Float, val angle: Float)

    private var previousFrame: List<HandLandmark>? = null
    private val flowHistory = mutableListOf<List<FlowVector>>()

    companion object {
        private const val FLOW_HISTORY_SIZE = 10
        private const val MOTION_THRESHOLD = 0.005f
    }

    fun computeFlow(currentFrame: List<HandLandmark>): List<FlowVector> {
        val prev = previousFrame ?: run {
            previousFrame = currentFrame
            return List(currentFrame.size) { FlowVector(0f, 0f, 0f, 0f) }
        }

        val flows = mutableListOf<FlowVector>()
        val size = minOf(currentFrame.size, prev.size)

        for (i in 0 until size) {
            val dx = currentFrame[i].x - prev[i].x
            val dy = currentFrame[i].y - prev[i].y
            val magnitude = hypot(dx, dy)
            val angle = if (magnitude > MOTION_THRESHOLD) {
                kotlin.math.atan2(dy, dx)
            } else 0f
            flows.add(FlowVector(dx, dy, magnitude, angle))
        }

        flowHistory.add(flows)
        if (flowHistory.size > FLOW_HISTORY_SIZE) flowHistory.removeAt(0)

        previousFrame = currentFrame
        return flows
    }

    fun getAverageVelocity(): Pair<Float, Float> {
        if (flowHistory.isEmpty()) return 0f to 0f

        var totalDx = 0f
        var totalDy = 0f
        var count = 0

        flowHistory.last().forEach { flow ->
            totalDx += flow.dx
            totalDy += flow.dy
            count++
        }

        return if (count > 0) (totalDx / count) to (totalDy / count) else 0f to 0f
    }

    fun getMotionConsistency(): Float {
        if (flowHistory.size < 3) return 0f

        val recent = flowHistory.takeLast(3)
        var consistentFrames = 0

        for (i in 1 until recent.size) {
            val prev = recent[i - 1]
            val curr = recent[i]
            val similarity = computeSimilarity(prev, curr)
            if (similarity > 0.7f) consistentFrames++
        }

        return consistentFrames.toFloat() / (recent.size - 1)
    }

    private fun computeSimilarity(a: List<FlowVector>, b: List<FlowVector>): Float {
        if (a.isEmpty() || b.isEmpty() || a.size != b.size) return 0f
        var matchCount = 0
        for (i in a.indices) {
            val angleDiff = abs(a[i].angle - b[i].angle)
            if (angleDiff < 0.5f && abs(a[i].magnitude - b[i].magnitude) < 0.02f) {
                matchCount++
            }
        }
        return matchCount.toFloat() / a.size
    }

    fun reset() {
        previousFrame = null
        flowHistory.clear()
    }
}
