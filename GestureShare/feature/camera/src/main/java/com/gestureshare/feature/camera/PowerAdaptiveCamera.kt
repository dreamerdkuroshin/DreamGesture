package com.gestureshare.feature.camera

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PowerAdaptiveCamera @Inject constructor(
    private val cameraManager: CameraManager
) {
    private var consecutiveIdleFrames = 0
    private var consecutiveActiveFrames = 0
    private var isHighPowerMode = false

    companion object {
        private const val IDLE_THRESHOLD_FRAMES = 30
        private const val ACTIVE_THRESHOLD_FRAMES = 3
    }

    fun onFrameProcessed(handDetected: Boolean) {
        if (handDetected) {
            consecutiveActiveFrames++
            consecutiveIdleFrames = 0
            if (consecutiveActiveFrames >= ACTIVE_THRESHOLD_FRAMES && !isHighPowerMode) {
                isHighPowerMode = true
                cameraManager.setActiveMode(true)
            }
        } else {
            consecutiveIdleFrames++
            consecutiveActiveFrames = 0
            if (consecutiveIdleFrames >= IDLE_THRESHOLD_FRAMES && isHighPowerMode) {
                isHighPowerMode = false
                cameraManager.setActiveMode(false)
            }
        }
    }

    fun reset() {
        consecutiveIdleFrames = 0
        consecutiveActiveFrames = 0
        isHighPowerMode = false
        cameraManager.setActiveMode(false)
    }
}
