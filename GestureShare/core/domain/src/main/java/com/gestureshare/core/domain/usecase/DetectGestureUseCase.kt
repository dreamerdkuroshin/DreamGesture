package com.gestureshare.core.domain.usecase

import com.gestureshare.core.domain.model.Gesture
import com.gestureshare.core.domain.repository.GestureRepository
import javax.inject.Inject

class DetectGestureUseCase @Inject constructor(
    private val gestureRepository: GestureRepository
) {
    suspend operator fun invoke(frameData: ByteArray, width: Int, height: Int): Gesture? {
        return gestureRepository.detectGesture(frameData, width, height)
    }
}
