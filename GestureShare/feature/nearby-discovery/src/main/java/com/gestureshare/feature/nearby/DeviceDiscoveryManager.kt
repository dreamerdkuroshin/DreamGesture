package com.gestureshare.feature.nearby

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import com.gestureshare.core.domain.model.ConnectionProtocol
import com.gestureshare.core.domain.model.DeviceCapability
import com.gestureshare.core.domain.model.NearbyDevice
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.MulticastSocket
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceDiscoveryManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val SERVICE_TYPE = "_gestureshare._tcp"
        private const val DISCOVERY_PORT = 55771
        private const val BROADCAST_INTERVAL_MS = 3000L
        private const val DISCOVERY_TIMEOUT_MS = 30000L
    }

    private val _devices = MutableSharedFlow<List<NearbyDevice>>(
        replay = 1,
        extraBufferCapacity = 4,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val devices: SharedFlow<List<NearbyDevice>> = _devices.asSharedFlow()

    private val discoveredDevices = mutableMapOf<String, NearbyDevice>()
    private var wifiP2pManager: WifiP2pManager? = null
    private var nsdManager: NsdManager? = null
    private var isDiscovering = false

    fun startDiscovery() {
        if (isDiscovering) return
        isDiscovering = true

        startWifiDirectDiscovery()
        startNsdDiscovery()
        startUdpBroadcast()
    }

    fun stopDiscovery() {
        isDiscovering = false
        stopNsdDiscovery()
        stopWifiDirectDiscovery()
        discoveredDevices.clear()
    }

    private fun startWifiDirectDiscovery() {
        wifiP2pManager = context.getSystemService(Context.WIFI_P2P_SERVICE) as? WifiP2pManager
        val channel = wifiP2pManager?.initialize(context, context.mainLooper, null) ?: return

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION == intent.action) {
                    @Suppress("DEPRECATION")
                    wifiP2pManager?.requestPeers(channel) { peers ->
                        handleWifiP2pPeers(peers.deviceList.toList())
                    }
                }
            }
        }

        val filter = IntentFilter(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        context.registerReceiver(receiver, filter)
        @Suppress("DEPRECATION")
        wifiP2pManager?.discoverPeers(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {}
            override fun onFailure(reason: Int) {}
        })
    }

    private fun stopWifiDirectDiscovery() {
        wifiP2pManager = null
    }

    private fun startNsdDiscovery() {
        nsdManager = context.getSystemService(Context.NSD_SERVICE) as? NsdManager

        val discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {}
            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {}
            override fun onDiscoveryStarted(serviceType: String) {}
            override fun onDiscoveryStopped(serviceType: String) {}
            override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                val device = NearbyDevice(
                    name = serviceInfo.serviceName,
                    endpointId = serviceInfo.serviceName,
                    protocol = ConnectionProtocol.MULTICAST_DNS,
                    lastSeen = System.currentTimeMillis()
                )
                addOrUpdateDevice(device)
                resolveNsdService(serviceInfo)
            }

            override fun onServiceLost(serviceInfo: NsdServiceInfo) {
                discoveredDevices.remove(serviceInfo.serviceName)
                emitDevices()
            }
        }

        nsdManager?.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
    }

    private fun stopNsdDiscovery() {
        nsdManager = null
    }

    private fun startUdpBroadcast() {
        Thread {
            try {
                val socket = MulticastSocket(DISCOVERY_PORT)
                socket.joinGroup(InetAddress.getByName("224.0.0.1"))
                socket.broadcast = true

                val deviceName = "${Build.MODEL}|${Build.DISCOVERER_ID}"
                val message = "GESTURE_SHARE_DISCOVER:$deviceName"
                val broadcastAddr = InetAddress.getByName("255.255.255.255")

                while (isDiscovering) {
                    val packet = DatagramPacket(
                        message.toByteArray(),
                        message.length,
                        broadcastAddr,
                        DISCOVERY_PORT
                    )
                    socket.send(packet)

                    val buffer = ByteArray(1024)
                    val response = DatagramPacket(buffer, buffer.size)
                    socket.soTimeout = 1000
                    try {
                        socket.receive(response)
                        val received = String(response.data, 0, response.length)
                        if (received.startsWith("GESTURE_SHARE_DISCOVER:")) {
                            val parts = received.removePrefix("GESTURE_SHARE_DISCOVER:").split("|")
                            if (parts.isNotEmpty()) {
                                val device = NearbyDevice(
                                    name = parts[0],
                                    endpointId = "${response.address.hostAddress}:$DISCOVERY_PORT",
                                    protocol = ConnectionProtocol.UDP_BROADCAST,
                                    ipAddress = response.address.hostAddress,
                                    port = DISCOVERY_PORT,
                                    lastSeen = System.currentTimeMillis()
                                )
                                addOrUpdateDevice(device)
                            }
                        }
                    } catch (e: java.net.SocketTimeoutException) {}

                    Thread.sleep(BROADCAST_INTERVAL_MS)
                }
                socket.close()
            } catch (e: Exception) {
                // Discovery stopped
            }
        }.apply {
            isDaemon = true
            start()
        }
    }

    private fun resolveNsdService(serviceInfo: NsdServiceInfo) {
        nsdManager?.resolveService(serviceInfo, object : NsdManager.ResolveListener {
            override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {}
            override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                val device = NearbyDevice(
                    name = serviceInfo.serviceName,
                    endpointId = "${serviceInfo.host.hostAddress}:${serviceInfo.port}",
                    protocol = ConnectionProtocol.MULTICAST_DNS,
                    ipAddress = serviceInfo.host.hostAddress,
                    port = serviceInfo.port,
                    lastSeen = System.currentTimeMillis()
                )
                addOrUpdateDevice(device)
            }
        })
    }

    private fun handleWifiP2pPeers(peers: List<WifiP2pDevice>) {
        peers.forEach { peer ->
            val device = NearbyDevice(
                name = peer.deviceName,
                endpointId = peer.deviceAddress,
                protocol = ConnectionProtocol.WIFI_DIRECT,
                lastSeen = System.currentTimeMillis()
            )
            addOrUpdateDevice(device)
        }
    }

    private fun addOrUpdateDevice(device: NearbyDevice) {
        discoveredDevices[device.endpointId] = device
        emitDevices()
    }

    private fun emitDevices() {
        _devices.tryEmit(discoveredDevices.values.toList())
    }

    fun selectBestProtocol(devices: List<NearbyDevice>): ConnectionProtocol {
        val protocols = devices.map { it.protocol }.toSet()
        return when {
            protocols.contains(ConnectionProtocol.WIFI_DIRECT) -> ConnectionProtocol.WIFI_DIRECT
            protocols.contains(ConnectionProtocol.MULTICAST_DNS) -> ConnectionProtocol.MULTICAST_DNS
            protocols.contains(ConnectionProtocol.UDP_BROADCAST) -> ConnectionProtocol.UDP_BROADCAST
            else -> ConnectionProtocol.BLE
        }
    }
}
