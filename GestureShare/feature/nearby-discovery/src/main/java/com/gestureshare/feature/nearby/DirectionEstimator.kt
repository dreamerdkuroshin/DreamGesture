package com.gestureshare.feature.nearby

import com.gestureshare.core.domain.model.Gesture
import com.gestureshare.core.domain.model.Direction3D
import com.gestureshare.core.domain.model.NearbyDevice
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Singleton
class DirectionEstimator @Inject constructor() {

    data class SpatialPosition(val azimuth: Float, val elevation: Float, val distance: Float)

    fun estimateTarget(
        gesture: Gesture,
        candidates: List<NearbyDevice>,
        devicePositions: Map<String, SpatialPosition> = emptyMap()
    ): NearbyDevice? {
        if (candidates.isEmpty()) return null
        if (candidates.size == 1) return candidates.first()

        val gestureDirection = normalizeDirection(gesture.direction)

        return if (devicePositions.isNotEmpty()) {
            selectByPosition(gestureDirection, candidates, devicePositions)
        } else {
            selectBySignal(gesture, candidates)
        }
    }

    fun estimateDevicePosition(
        signalStrength: Int,
        gestureDirection: Direction3D
    ): SpatialPosition {
        val rssiDistance = rssiToDistance(signalStrength)
        val azimuth = atan2(gestureDirection.x, gestureDirection.y)
        val elevation = atan2(
            gestureDirection.z,
            sqrt(gestureDirection.x * gestureDirection.x + gestureDirection.y * gestureDirection.y)
        )
        return SpatialPosition(azimuth, elevation, rssiDistance)
    }

    private fun selectByPosition(
        gestureDirection: Direction3D,
        candidates: List<NearbyDevice>,
        positions: Map<String, SpatialPosition>
    ): NearbyDevice? {
        return candidates.minByOrNull { device ->
            val position = positions[device.endpointId] ?: return@minByOrNull Float.MAX_VALUE
            angularDistance(gestureDirection, position)
        }
    }

    private fun selectBySignal(gesture: Gesture, candidates: List<NearbyDevice>): NearbyDevice? {
        return candidates.maxByOrNull { device ->
            val signalScore = (device.signalStrength + 100).coerceIn(0, 50) / 50f
            val protocolScore = when (device.protocol) {
                com.gestureshare.core.domain.model.ConnectionProtocol.WIFI_DIRECT -> 1.0f
                com.gestureshare.core.domain.model.ConnectionProtocol.WIFI_AWARE -> 0.9f
                com.gestureshare.core.domain.model.ConnectionProtocol.NEARBY_CONNECTIONS -> 0.8f
                com.gestureshare.core.domain.model.ConnectionProtocol.MULTICAST_DNS -> 0.7f
                com.gestureshare.core.domain.model.ConnectionProtocol.BLE -> 0.5f
                com.gestureshare.core.domain.model.ConnectionProtocol.UDP_BROADCAST -> 0.4f
            }
            signalScore * 0.6f + protocolScore * 0.4f
        }
    }

    private fun angularDistance(direction: Direction3D, position: SpatialPosition): Float {
        val gestureAzimuth = atan2(direction.x, direction.y)
        val gestureElevation = atan2(
            direction.z,
            sqrt(direction.x * direction.x + direction.y * direction.y)
        )

        var azimuthDiff = kotlin.math.abs(gestureAzimuth - position.azimuth)
        if (azimuthDiff > Math.PI) azimuthDiff = (2 * Math.PI - azimuthDiff).toFloat()

        val elevationDiff = kotlin.math.abs(gestureElevation - position.elevation)

        return sqrt(azimuthDiff * azimuthDiff + elevationDiff * elevationDiff)
    }

    private fun normalizeDirection(direction: Direction3D): Direction3D {
        val magnitude = sqrt(direction.x * direction.x + direction.y * direction.y + direction.z * direction.z)
        return if (magnitude > 0.001f) {
            Direction3D(
                direction.x / magnitude,
                direction.y / magnitude,
                direction.z / magnitude
            )
        } else direction
    }

    private fun rssiToDistance(rssi: Int): Float {
        val txPower = -59f
        if (rssi == 0) return -1f
        val ratio = rssi.toFloat() * 1.0f / txPower
        return if (ratio < 1.0f) {
            ratio * ratio * ratio
        } else {
            0.89976f * ratio * ratio * ratio + 0.111f * ratio * ratio
        }
    }
}
