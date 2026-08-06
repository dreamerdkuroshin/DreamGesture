package com.gestureshare.feature.gesture

import com.gestureshare.core.domain.model.Gesture
import com.gestureshare.core.domain.model.GestureEvent
import com.gestureshare.core.domain.model.GestureSequence
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.LinkedList
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class GestureEngine @Inject constructor() {

    companion object {
        private const val MIN_CONFIDENCE = 0.95f
        private const val COOLDOWN_MS = 500L
        private const val SEQUENCE_TIMEOUT_MS = 2000L
        private const val MOTION_HISTORY_SIZE = 30
        private const val VELOCITY_THRESHOLD = 0.01f
        private const val STATIONARY_THRESHOLD_FRAMES = 10
    }

    private val _events = MutableSharedFlow<GestureEvent>(
        replay = 1,
        extraBufferCapacity = 8,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<GestureEvent> = _events.asSharedFlow()

    private val motionHistory = LinkedList<Gesture>()
    private val sequenceBuffer = LinkedList<Gesture>()
    private var lastTriggerTime = 0L
    private var lastGestureType: String? = null
    private var lastGestureTime = 0L
    private var stationaryFrames = 0
    private var lastLandmarkPosition: Triple<Float, Float, Float>? = null

    fun processGesture(gesture: Gesture): Boolean {
        if (gesture.confidence < MIN_CONFIDENCE) return false
        if (isCooldownActive()) return false
        if (isStationary(gesture)) return false

        motionHistory.addLast(gesture)
        if (motionHistory.size > MOTION_HISTORY_SIZE) motionHistory.removeFirst()

        sequenceBuffer.addLast(gesture)
        pruneOldSequenceEntries()

        val detectedEvent = detectSequenceOrGesture()
        if (detectedEvent != null) {
            _events.tryEmit(detectedEvent)
            updateTriggerState(gesture)
            return true
        }

        _events.tryEmit(GestureEvent.Detected(gesture))
        updateTriggerState(gesture)
        return true
    }

    private fun detectSequenceOrGesture(): GestureEvent? {
        if (sequenceBuffer.size < 2) return null

        val timeSpan = sequenceBuffer.last.timestamp - sequenceBuffer.first.timestamp
        if (timeSpan <= SEQUENCE_TIMEOUT_MS && sequenceBuffer.size >= 3) {
            val types = sequenceBuffer.map { it.type }.distinct()
            if (types.size >= 2) {
                val sequence = GestureSequence(
                    gestures = sequenceBuffer.toList(),
                    maxDurationMs = SEQUENCE_TIMEOUT_MS,
                    minConfidence = MIN_CONFIDENCE
                )
                if (sequence.isValid()) {
                    sequenceBuffer.clear()
                    return GestureEvent.SequenceCompleted(sequence)
                }
            }
        }

        val lastGesture = sequenceBuffer.last
        if (lastGestureType == lastGesture.type.name &&
            (lastGesture.timestamp - lastGestureTime) < 300
        ) {
            lastGestureTime = lastGesture.timestamp
            return GestureEvent.Detected(lastGesture.copy(
                metadata = mapOf("double" to "true")
            ))
        }

        return null
    }

    private fun isCooldownActive(): Boolean {
        return System.currentTimeMillis() - lastTriggerTime < COOLDOWN_MS
    }

    private fun isStationary(gesture: Gesture): Boolean {
        if (gesture.handPose.landmarks.isEmpty()) return false

        val currentPos = gesture.handPose.landmarks[0]
        val currentTriple = Triple(currentPos.x, currentPos.y, currentPos.z)

        lastLandmarkPosition?.let { last ->
            val dx = abs(currentTriple.first - last.first)
            val dy = abs(currentTriple.second - last.second)
            val dz = abs(currentTriple.third - last.third)

            if (dx < VELOCITY_THRESHOLD && dy < VELOCITY_THRESHOLD && dz < VELOCITY_THRESHOLD) {
                stationaryFrames++
                return stationaryFrames >= STATIONARY_THRESHOLD_FRAMES
            } else {
                stationaryFrames = 0
            }
        }

        lastLandmarkPosition = currentTriple
        return false
    }

    private fun pruneOldSequenceEntries() {
        val cutoff = System.currentTimeMillis() - SEQUENCE_TIMEOUT_MS
        sequenceBuffer.removeAll { it.timestamp < cutoff }
    }

    private fun updateTriggerState(gesture: Gesture) {
        lastTriggerTime = System.currentTimeMillis()
        lastGestureType = gesture.type.name
        lastGestureTime = gesture.timestamp
    }

    fun getMotionHistory(): List<Gesture> = motionHistory.toList()

    fun reset() {
        motionHistory.clear()
        sequenceBuffer.clear()
        lastTriggerTime = 0L
        lastGestureType = null
        lastGestureTime = 0L
        stationaryFrames = 0
        lastLandmarkPosition = null
    }
}
