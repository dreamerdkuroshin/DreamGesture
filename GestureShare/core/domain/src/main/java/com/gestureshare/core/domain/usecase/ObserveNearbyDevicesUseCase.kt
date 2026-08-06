package com.gestureshare.core.domain.usecase

import com.gestureshare.core.domain.model.NearbyDevice
import com.gestureshare.core.domain.repository.DeviceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveNearbyDevicesUseCase @Inject constructor(
    private val deviceRepository: DeviceRepository
) {
    operator fun invoke(): Flow<List<NearbyDevice>> {
        return deviceRepository.discoverDevices()
    }
}
