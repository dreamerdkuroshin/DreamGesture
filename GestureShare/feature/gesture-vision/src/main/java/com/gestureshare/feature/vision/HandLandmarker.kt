package com.gestureshare.feature.vision

import android.content.Context
import android.graphics.Bitmap
import com.gestureshare.core.domain.model.HandLandmark
import com.gestureshare.core.domain.model.HandPose
import com.gestureshare.core.domain.model.HandSide
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HandLandmarker @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val MODEL_PATH = "models/hand_landmarker.task"
        private const val MAX_NUM_HANDS = 2
        private const val MIN_DETECTION_CONFIDENCE = 0.5f
        private const val MIN_TRACKING_CONFIDENCE = 0.5f
        private const val MIN_PRESENCE_CONFIDENCE = 0.5f
    }

    private val landmarkerRef = AtomicReference<HandLandmarker?>(null)
    private var isInitialized = false

    suspend fun initialize() = withContext(Dispatchers.Default) {
        if (isInitialized) return@withContext

        try {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath(MODEL_PATH)
                .setDelegate(Delegate.GPU)
                .build()

            val options = HandLandmarker.HandLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setRunningMode(RunningMode.IMAGE)
                .setNumHands(MAX_NUM_HANDS)
                .setMinHandDetectionConfidence(MIN_DETECTION_CONFIDENCE)
                .setMinTrackingConfidence(MIN_TRACKING_CONFIDENCE)
                .setMinHandPresenceConfidence(MIN_PRESENCE_CONFIDENCE)
                .build()

            val landmarker = HandLandmarker.createFromOptions(context, options)
            landmarkerRef.set(landmarker)
            isInitialized = true
        } catch (e: Exception) {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath(MODEL_PATH)
                .setDelegate(Delegate.CPU)
                .build()

            val options = HandLandmarker.HandLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setRunningMode(RunningMode.IMAGE)
                .setNumHands(MAX_NUM_HANDS)
                .setMinHandDetectionConfidence(MIN_DETECTION_CONFIDENCE)
                .setMinTrackingConfidence(MIN_TRACKING_CONFIDENCE)
                .setMinHandPresenceConfidence(MIN_PRESENCE_CONFIDENCE)
                .build()

            val landmarker = HandLandmarker.createFromOptions(context, options)
            landmarkerRef.set(landmarker)
            isInitialized = true
        }
    }

    suspend fun detect(bitmap: Bitmap): List<HandPose> = withContext(Dispatchers.Default) {
        val landmarker = landmarkerRef.get() ?: return@withContext emptyList()

        try {
            val mpImage = BitmapImageBuilder(bitmap).build()
            val result = landmarker.detect(mpImage)
            result.toHandPoses()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun detectAsync(bitmap: Bitmap): List<HandPose> = withContext(Dispatchers.Default) {
        val landmarker = landmarkerRef.get() ?: return@withContext emptyList()

        try {
            val mpImage = BitmapImageBuilder(bitmap).build()
            val result = landmarker.detectAsync(mpImage, System.currentTimeMillis())
            result?.toHandPoses() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun HandLandmarkerResult.toHandPoses(): List<HandPose> {
        val poses = mutableListOf<HandPose>()

        for (i in handLandmarks().indices) {
            val landmarks = handLandmarks()[i].map { lm ->
                HandLandmark(
                    x = lm.x(),
                    y = lm.y(),
                    z = lm.z(),
                    visibility = if (lm.hasVisibility()) lm.visibility() else 1f
                )
            }

            val side = if (handednesses().isNotEmpty() && handednesses()[i].isNotEmpty()) {
                if (handednesses()[i][0].categoryName() == "Right") HandSide.RIGHT else HandSide.LEFT
            } else {
                HandSide.LEFT
            }

            poses.add(
                HandPose(
                    side = side,
                    landmarks = landmarks,
                    confidence = if (handednesses().isNotEmpty() && handednesses()[i].isNotEmpty())
                        handednesses()[i][0].score() else 0.5f
                )
            )
        }

        return poses
    }

    fun close() {
        landmarkerRef.get()?.close()
        landmarkerRef.set(null)
        isInitialized = false
    }
}
