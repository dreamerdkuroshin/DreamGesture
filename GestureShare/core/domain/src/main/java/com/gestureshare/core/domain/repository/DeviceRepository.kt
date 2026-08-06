package com.gestureshare.core.domain.repository

import com.gestureshare.core.domain.model.ConnectionProtocol
import com.gestureshare.core.domain.model.NearbyDevice
import com.gestureshare.core.domain.model.TransferSession
import com.gestureshare.core.domain.model.TransferState
import kotlinx.coroutines.flow.Flow

interface DeviceRepository {
    fun discoverDevices(): Flow<List<NearbyDevice>>
    fun observeTransferState(): Flow<TransferState>
    suspend fun connect(device: NearbyDevice): Result<TransferSession>
    suspend fun disconnect(sessionId: String)
    suspend fun sendScreenshot(session: TransferSession, screenshotId: String): Flow<TransferState>
    suspend fun selectBestProtocol(available: Set<ConnectionProtocol>): ConnectionProtocol
    suspend fun estimateTargetDirection(gesture: Gesture, candidates: List<NearbyDevice>): NearbyDevice?
}
