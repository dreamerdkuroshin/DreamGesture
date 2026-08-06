package com.gestureshare.core.domain.model

data class CameraConfig(
    val targetFps: Int = 15,
    val resolution: CameraResolution = CameraResolution.HD_720,
    val lensFacing: LensFacing = LensFacing.FRONT,
    val enableGpuDelegate: Boolean = true,
    val enableNnapi: Boolean = true
)

enum class CameraResolution(val width: Int, val height: Int) {
    SD_480(640, 480),
    HD_720(1280, 720),
    FHD_1080(1920, 1080)
}

enum class LensFacing { FRONT, BACK }

data class InferenceResult(
    val gesture: Gesture?,
    val inferenceTimeMs: Long,
    val modelUsed: String = "rule_based"
)
