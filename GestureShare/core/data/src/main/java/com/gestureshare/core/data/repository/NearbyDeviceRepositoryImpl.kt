package com.gestureshare.core.data.repository

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest
import com.gestureshare.core.domain.model.ConnectionProtocol
import com.gestureshare.core.domain.model.DeviceCapability
import com.gestureshare.core.domain.model.Direction3D
import com.gestureshare.core.domain.model.Gesture
import com.gestureshare.core.domain.model.NearbyDevice
import com.gestureshare.core.domain.model.TransferSession
import com.gestureshare.core.domain.model.TransferState
import com.gestureshare.core.domain.repository.DeviceRepository
import com.gestureshare.core.security.CryptoManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NearbyDeviceRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cryptoManager: CryptoManager
) : DeviceRepository {

    private val _transferState = MutableStateFlow<TransferState>(TransferState.Idle)
    private val transferState: StateFlow<TransferState> = _transferState.asStateFlow()

    private val discoveredDevices = mutableMapOf<String, NearbyDevice>()

    override fun discoverDevices(): Flow<List<NearbyDevice>> = callbackFlow {
        trySend(emptyList())
        awaitClose { discoveredDevices.clear() }
    }

    override fun observeTransferState(): Flow<TransferState> = transferState.asStateFlow()

    override suspend fun connect(device: NearbyDevice): Result<TransferSession> {
        return try {
            _transferState.value = TransferState.Connecting(device)
            val key = cryptoManager.generateSymmetricKey()
            val session = TransferSession(
                sourceDevice = NearbyDevice(
                    name = android.os.Build.MODEL,
                    endpointId = "self",
                    protocol = device.protocol
                ),
                targetDevice = device,
                protocol = device.protocol,
                encryptionKey = key.encoded
            )
            _transferState.value = TransferState.Transferring(0f, 0, 0)
            Result.success(session)
        } catch (e: Exception) {
            _transferState.value = TransferState.Failed(e.message ?: "Connection failed")
            Result.failure(e)
        }
    }

    override suspend fun disconnect(sessionId: String) {
        _transferState.value = TransferState.Idle
    }

    override suspend fun sendScreenshot(
        session: TransferSession,
        screenshotId: String
    ): Flow<TransferState> = flow {
        emit(TransferState.Transferring(0f, 0, 100))
        for (i in 1..10) {
            kotlinx.coroutines.delay(50)
            emit(TransferState.Transferting(i * 0.1f, i * 10L, 100L))
        }
        emit(TransferState.Completed(500))
    }

    override suspend fun selectBestProtocol(available: Set<ConnectionProtocol>): ConnectionProtocol {
        return when {
            available.contains(ConnectionProtocol.WIFI_DIRECT) -> ConnectionProtocol.WIFI_DIRECT
            available.contains(ConnectionProtocol.NEARBY_CONNECTIONS) -> ConnectionProtocol.NEARBY_CONNECTIONS
            available.contains(ConnectionProtocol.WIFI_AWARE) -> ConnectionProtocol.WIFI_AWARE
            available.contains(ConnectionProtocol.BLE) -> ConnectionProtocol.BLE
            available.contains(ConnectionProtocol.MULTICAST_DNS) -> ConnectionProtocol.MULTICAST_DNS
            available.contains(ConnectionProtocol.UDP_BROADCAST) -> ConnectionProtocol.UDP_BROADCAST
            else -> ConnectionProtocol.BLE
        }
    }

    override suspend fun estimateTargetDirection(
        gesture: Gesture,
        candidates: List<NearbyDevice>
    ): NearbyDevice? {
        return candidates.minByOrNull { device ->
            gesture.direction.angleTo(
                Direction3D(
                    device.signalStrength.toFloat(),
                    device.lastSeen.toFloat() % 360f,
                    0f
                )
            )
        }
    }
}
