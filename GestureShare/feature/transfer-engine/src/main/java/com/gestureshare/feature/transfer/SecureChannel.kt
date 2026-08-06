package com.gestureshare.feature.transfer

import com.gestureshare.core.security.CryptoManager
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readBytes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.security.KeyPair
import javax.crypto.SecretKey
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureChannel @Inject constructor(
    private val cryptoManager: CryptoManager
) {
    private var keyPair: KeyPair? = null
    private var sharedSecret: SecretKey? = null
    private var webSocketSession: WebSocketSession? = null

    private val httpClient = HttpClient(OkHttp) {
        install(WebSockets)
    }

    suspend fun performKeyExchange(remotePublicKeyBytes: ByteArray): ByteArray {
        keyPair = cryptoManager.generateEphemeralKeyPair()
        val remotePublicKey = java.security.KeyFactory.getInstance("EC")
            .generatePublic(java.security.spec.X509EncodedKeySpec(remotePublicKeyBytes))
        sharedSecret = cryptoManager.deriveSharedSecret(keyPair!!.private, remotePublicKey)
        return keyPair!!.public.encoded
    }

    suspend fun connect(host: String, port: Int) {
        httpClient.webSocket(host = host, port = port, path = "/transfer") {
            webSocketSession = this
        }
    }

    suspend fun sendEncrypted(data: ByteArray) {
        val secret = sharedSecret ?: throw IllegalStateException("No shared secret")
        val payload = cryptoManager.encrypt(data, secret)
        val frameData = payload.iv + payload.ciphertext
        webSocketSession?.send(Frame.Binary(true, frameData))
    }

    suspend fun receiveDecrypted(): ByteArray {
        val secret = sharedSecret ?: throw IllegalStateException("No shared secret")
        val frame = webSocketSession?.incoming?.receive() as? Frame.Binary ?: return ByteArray(0)
        val data = frame.readBytes()
        val iv = data.copyOfRange(0, 12)
        val ciphertext = data.copyOfRange(12, data.size)
        return cryptoManager.decrypt(com.gestureshare.core.security.EncryptedPayload(iv, ciphertext), secret)
    }

    fun observeIncoming(): Flow<ByteArray> = flow {
        while (true) {
            try {
                val data = receiveDecrypted()
                if (data.isNotEmpty()) emit(data)
            } catch (e: Exception) {
                break
            }
        }
    }

    suspend fun close() {
        webSocketSession?.close()
        webSocketSession = null
        sharedSecret = null
        keyPair = null
    }
}
