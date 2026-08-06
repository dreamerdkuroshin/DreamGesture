package com.gestureshare.core.domain.usecase

import com.gestureshare.core.domain.model.Gesture
import com.gestureshare.core.domain.model.NearbyDevice
import com.gestureshare.core.domain.model.TransferState
import com.gestureshare.core.domain.repository.DeviceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TransferScreenshotUseCase @Inject constructor(
    private val deviceRepository: DeviceRepository
) {
    suspend fun selectTarget(gesture: Gesture, candidates: List<NearbyDevice>): NearbyDevice? {
        return if (candidates.size == 1) {
            candidates.first()
        } else {
            deviceRepository.estimateTargetDirection(gesture, candidates)
        }
    }

    suspend fun transfer(device: NearbyDevice, screenshotId: String): Flow<TransferState> {
        val session = deviceRepository.connect(device).getOrThrow()
        return deviceRepository.sendScreenshot(session, screenshotId)
    }

    fun observeTransferState(): Flow<TransferState> = deviceRepository.observeTransferState()
}
