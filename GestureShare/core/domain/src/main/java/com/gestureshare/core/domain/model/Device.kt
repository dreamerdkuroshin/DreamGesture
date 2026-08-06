package com.gestureshare.core.domain.model

import java.util.UUID

enum class ConnectionProtocol {
    WIFI_DIRECT,
    WIFI_AWARE,
    BLE,
    NEARBY_CONNECTIONS,
    MULTICAST_DNS,
    UDP_BROADCAST
}

data class NearbyDevice(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val endpointId: String,
    val protocol: ConnectionProtocol,
    val signalStrength: Int = 0,
    val isConnected: Boolean = false,
    val capabilities: Set<DeviceCapability> = emptySet(),
    val lastSeen: Long = System.currentTimeMillis(),
    val ipAddress: String? = null,
    val port: Int? = null
)

enum class DeviceCapability {
    SCREENSHOT_RECEIVE,
    SCREENSHOT_SEND,
    HIGH_BANDWIDTH,
    LOW_LATENCY
}

data class TransferSession(
    val sessionId: String = UUID.randomUUID().toString(),
    val sourceDevice: NearbyDevice,
    val targetDevice: NearbyDevice,
    val protocol: ConnectionProtocol,
    val encryptionKey: ByteArray,
    val createdAt: Long = System.currentTimeMillis()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TransferSession) return false
        return sessionId == other.sessionId
    }

    override fun hashCode(): Int = sessionId.hashCode()
}

sealed class TransferState {
    object Idle : TransferState()
    data class Discovering(val devices: List<NearbyDevice>) : TransferState()
    data class Selecting(val gesture: Gesture, val candidates: List<NearbyDevice>) : TransferState()
    data class Connecting(val device: NearbyDevice) : TransferState()
    data class Transferring(val progress: Float, val bytesTransferred: Long, val totalBytes: Long) : TransferState()
    data class Completed(val durationMs: Long) : TransferState()
    data class Failed(val error: String) : TransferState()
}
