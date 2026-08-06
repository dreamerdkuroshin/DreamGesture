package com.gestureshare.core.domain.model

enum class GestureType {
    PALM,
    OPEN_HAND,
    CLOSED_HAND,
    POINT,
    SWIPE_LEFT,
    SWIPE_RIGHT,
    PUSH,
    PULL,
    GRAB,
    THROW,
    PINCH,
    PEACE_SIGN,
    THUMB_UP,
    THUMB_DOWN,
    WAVE,
    CUSTOM
}

enum class HandSide { LEFT, RIGHT }

data class HandLandmark(
    val x: Float,
    val y: Float,
    val z: Float,
    val visibility: Float = 1f
)

data class HandPose(
    val side: HandSide,
    val landmarks: List<HandLandmark>,
    val timestamp: Long = System.currentTimeMillis(),
    val confidence: Float = 0f
)

data class Gesture(
    val type: GestureType,
    val confidence: Float,
    val handPose: HandPose,
    val velocity: Float = 0f,
    val direction: Direction3D = Direction3D.ZERO,
    val acceleration: Float = 0f,
    val rotation: Float = 0f,
    val timestamp: Long = System.currentTimeMillis(),
    val metadata: Map<String, String> = emptyMap()
)

data class Direction3D(val x: Float, val y: Float, val z: Float) {
    companion object {
        val ZERO = Direction3D(0f, 0f, 0f)
    }

    fun angleTo(other: Direction3D): Float {
        val dot = x * other.x + y * other.y + z * other.z
        val magA = kotlin.math.sqrt(x * x + y * y + z * z)
        val magB = kotlin.math.sqrt(other.x * other.x + other.y * other.y + other.z * other.z)
        if (magA == 0f || magB == 0f) return 180f
        val cosAngle = (dot / (magA * magB)).coerceIn(-1f, 1f)
        return Math.toDegrees(kotlin.math.acos(cosAngle).toDouble()).toFloat()
    }
}

data class GestureSequence(
    val gestures: List<Gesture>,
    val maxDurationMs: Long = 2000L,
    val minConfidence: Float = 0.95f
) {
    fun isValid(): Boolean {
        if (gestures.isEmpty()) return false
        val timeSpan = gestures.last().timestamp - gestures.first().timestamp
        if (timeSpan > maxDurationMs) return false
        return gestures.all { it.confidence >= minConfidence }
    }
}

sealed class GestureEvent {
    data class Detected(val gesture: Gesture) : GestureEvent()
    data class SequenceCompleted(val sequence: GestureSequence) : GestureEvent()
    object Idle : GestureEvent()
}
