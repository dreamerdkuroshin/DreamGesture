package com.gestureshare.feature.vision

import android.content.Context
import com.gestureshare.core.domain.model.Gesture
import com.gestureshare.core.domain.model.GestureType
import com.gestureshare.core.domain.model.HandLandmark
import com.gestureshare.core.domain.model.HandPose
import com.gestureshare.core.domain.model.Direction3D
import dagger.hilt.android.qualifiers.ApplicationContext
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.acos
import kotlin.math.hypot
import kotlin.math.sqrt

@Singleton
class GestureRecognizer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val MODEL_PATH = "models/gesture_classifier.tflite"
        private const val NUM_LANDMARKS = 21
        private const val FEATURES_PER_LANDMARK = 4
        private const val INPUT_SIZE = NUM_LANDMARKS * FEATURES_PER_LANDMARK
        private const val NUM_CLASSES = 15
        private const val CONFIDENCE_THRESHOLD = 0.95f
    }

    private var tfliteInterpreter: Interpreter? = null
    private var gestureLabels: List<String> = emptyList()

    fun initialize() {
        try {
            val interpreterOptions = Interpreter.Options().apply {
                setNumThreads(4)
                setUseNNAPI(true)
            }

            val model = loadModelFile()
            tfliteInterpreter = Interpreter(model, interpreterOptions)
            gestureLabels = GestureType.values().map { it.name }
        } catch (e: Exception) {
            val interpreterOptions = Interpreter.Options().apply {
                setNumThreads(2)
            }
            val model = loadModelFile()
            tfliteInterpreter = Interpreter(model, interpreterOptions)
            gestureLabels = GestureType.values().map { it.name }
        }
    }

    fun recognize(pose: HandPose): Gesture? {
        if (pose.landmarks.size != NUM_LANDMARKS) return null

        val confidence = computeRuleBasedConfidence(pose)
        val type = classifyGesture(pose)

        if (type == null || confidence < CONFIDENCE_THRESHOLD) return null

        val direction = estimateDirection(pose)

        return Gesture(
            type = type,
            confidence = confidence,
            handPose = pose,
            velocity = 0f,
            direction = direction,
            timestamp = System.currentTimeMillis()
        )
    }

    private fun classifyGesture(pose: HandPose): GestureType? {
        val landmarks = pose.landmarks
        val wrist = landmarks[0]
        val thumbTip = landmarks[4]
        val indexTip = landmarks[8]
        val middleTip = landmarks[12]
        val ringTip = landmarks[16]
        val pinkyTip = landmarks[20]
        val indexMcp = landmarks[5]
        val pinkyMcp = landmarks[17]

        val thumbOpen = distance(wrist, thumbTip) > distance(wrist, indexMcp) * 0.6f
        val indexOpen = isFingerExtended(landmarks, 8, 5, 6, 7)
        val middleOpen = isFingerExtended(landmarks, 12, 9, 10, 11)
        val ringOpen = isFingerExtended(landmarks, 16, 13, 14, 15)
        val pinkyOpen = isFingerExtended(landmarks, 20, 17, 18, 19)

        val fingersExtended = listOf(indexOpen, middleOpen, ringOpen, pinkyOpen).count { it }

        return when {
            !thumbOpen && !indexOpen && !middleOpen && !ringOpen && !pinkyOpen -> GestureType.GRAB
            fingersExtended >= 4 && thumbOpen -> GestureType.OPEN_HAND
            fingersExtended >= 4 && !thumbOpen -> GestureType.PALM
            indexOpen && !middleOpen && !ringOpen && !pinkyOpen -> GestureType.POINT
            indexOpen && middleOpen && !ringOpen && !pinkyOpen -> GestureType.PEACE_SIGN
            thumbOpen && !indexOpen && !middleOpen && !ringOpen && !pinkyOpen -> {
                if (thumbTip.y < wrist.y) GestureType.THUMB_UP else GestureType.THUMB_DOWN
            }
            fingersExtended >= 3 && thumbOpen -> GestureType.THROW
            fingersExtended <= 1 && !thumbOpen -> GestureType.CLOSED_HAND
            else -> GestureType.OPEN_HAND
        }
    }

    private fun isFingerExtended(
        landmarks: List<HandLandmark>,
        tipIdx: Int,
        mcpIdx: Int,
        pipIdx: Int,
        dipIdx: Int
    ): Boolean {
        val wrist = landmarks[0]
        val tipDistance = distance(wrist, landmarks[tipIdx])
        val mcpDistance = distance(wrist, landmarks[mcpIdx])
        return tipDistance > mcpDistance * 1.2f
    }

    private fun computeRuleBasedConfidence(pose: HandPose): Float {
        if (pose.confidence < 0.5f) return pose.confidence

        val wrist = pose.landmarks[0]
        val spreadVariance = pose.landmarks.map {
            val dx = it.x - wrist.x
            val dy = it.y - wrist.y
            dx * dx + dy * dy
        }.average().toFloat()

        val spatialConfidence = 1f - (spreadVariance / 2f).coerceIn(0f, 0.3f)
        return (pose.confidence * 0.7f + spatialConfidence * 0.3f).coerceIn(0f, 1f)
    }

    private fun estimateDirection(pose: HandPose): Direction3D {
        val wrist = pose.landmarks[0]
        val middleTip = pose.landmarks[12]
        return Direction3D(
            x = middleTip.x - wrist.x,
            y = middleTip.y - wrist.y,
            z = middleTip.z - wrist.z
        )
    }

    private fun distance(a: HandLandmark, b: HandLandmark): Float {
        val dx = a.x - b.x
        val dy = a.y - b.y
        val dz = a.z - b.z
        return sqrt(dx * dx + dy * dy + dz * dz)
    }

    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(MODEL_PATH)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun close() {
        tfliteInterpreter?.close()
        tfliteInterpreter = null
    }
}
