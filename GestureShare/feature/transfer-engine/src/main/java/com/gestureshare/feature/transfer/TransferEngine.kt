package com.gestureshare.feature.transfer

import com.gestureshare.core.domain.model.TransferSession
import com.gestureshare.core.domain.model.TransferState
import com.gestureshare.core.security.CryptoManager
import com.gestureshare.core.security.EncryptedPayload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.security.SecureRandom
import java.util.zip.Deflater
import java.util.zip.Inflater
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransferEngine @Inject constructor(
    private val cryptoManager: CryptoManager
) {
    companion object {
        private const val CHUNK_SIZE = 64 * 1024
        private const val MAX_RETRIES = 3
        private const val RETRY_DELAY_MS = 500L
        private const val COMPRESSION_LEVEL = Deflater.BEST_SPEED
    }

    private val secureRandom = SecureRandom()

    suspend fun send(
        session: TransferSession,
        data: ByteArray
    ): Flow<TransferState> = flow {
        emit(TransferState.Transferring(0f, 0, data.size.toLong()))

        try {
            val compressed = compress(data)
            val key = SecretKeySpec(session.encryptionKey, "AES")
            val encrypted = cryptoManager.encrypt(compressed, key)
            val hash = cryptoManager.sha256(data)

            val totalChunks = ((encrypted.ciphertext.size + CHUNK_SIZE - 1) / CHUNK_SIZE).toInt()
            var bytesTransferred = 0L

            for (chunkIndex in 0 until totalChunks) {
                val start = chunkIndex * CHUNK_SIZE
                val end = minOf(start + CHUNK_SIZE, encrypted.ciphertext.size)
                val chunk = encrypted.ciphertext.copyOfRange(start, end)

                val success = sendChunkWithRetry(chunk, chunkIndex, totalChunks, session)
                if (!success) {
                    emit(TransferState.Failed("Transfer failed at chunk $chunkIndex"))
                    return@flow
                }

                bytesTransferred += chunk.size
                val progress = bytesTransferred.toFloat() / encrypted.ciphertext.size
                emit(TransferState.Transferring(progress, bytesTransferred, encrypted.ciphertext.size.toLong()))
            }

            emit(TransferState.Completed(0))
        } catch (e: Exception) {
            emit(TransferState.Failed(e.message ?: "Unknown transfer error"))
        }
    }.flowOn(Dispatchers.IO)

    suspend fun receive(
        session: TransferSession,
        encryptedData: EncryptedPayload
    ): ByteArray = withContext(Dispatchers.IO) {
        val key = SecretKeySpec(session.encryptionKey, "AES")
        val decrypted = cryptoManager.decrypt(encryptedData, key)
        decompress(decrypted)
    }

    suspend fun verifyIntegrity(data: ByteArray, expectedHash: ByteArray): Boolean {
        return cryptoManager.verifyHash(data, expectedHash)
    }

    private suspend fun sendChunkWithRetry(
        chunk: ByteArray,
        chunkIndex: Int,
        totalChunks: Int,
        session: TransferSession
    ): Boolean {
        var retries = 0
        while (retries < MAX_RETRIES) {
            try {
                val success = transmitChunk(chunk, chunkIndex, totalChunks, session)
                if (success) return true
            } catch (e: Exception) {
                retries++
                if (retries < MAX_RETRIES) delay(RETRY_DELAY_MS * retries)
            }
        }
        return false
    }

    private suspend fun transmitChunk(
        chunk: ByteArray,
        chunkIndex: Int,
        totalChunks: Int,
        session: TransferSession
    ): Boolean {
        delay(10)
        return true
    }

    private fun compress(data: ByteArray): ByteArray {
        val deflater = Deflater(COMPRESSION_LEVEL)
        deflater.setInput(data)
        deflater.finish()

        val outputStream = ByteArrayOutputStream(data.size)
        val buffer = ByteArray(1024)

        while (!deflater.finished()) {
            val count = deflater.deflate(buffer)
            outputStream.write(buffer, 0, count)
        }

        deflater.end()
        return outputStream.toByteArray()
    }

    private fun decompress(data: ByteArray): ByteArray {
        val inflater = Inflater()
        inflater.setInput(data)

        val outputStream = ByteArrayOutputStream()
        val buffer = ByteArray(1024)

        while (!inflater.finished()) {
            val count = inflater.inflate(buffer)
            outputStream.write(buffer, 0, count)
        }

        inflater.end()
        return outputStream.toByteArray()
    }

    fun generateChunkHash(chunk: ByteArray): ByteArray {
        return cryptoManager.sha256(chunk)
    }

    fun createTransferMetadata(
        fileName: String,
        fileSize: Long,
        mimeType: String = "image/png"
    ): TransferMetadata {
        return TransferMetadata(
            fileName = fileName,
            fileSize = fileSize,
            mimeType = mimeType,
            chunkSize = CHUNK_SIZE,
            totalChunks = ((fileSize + CHUNK_SIZE - 1) / CHUNK_SIZE).toInt(),
            sessionId = java.util.UUID.randomUUID().toString()
        )
    }
}

data class TransferMetadata(
    val fileName: String,
    val fileSize: Long,
    val mimeType: String,
    val chunkSize: Int,
    val totalChunks: Int,
    val sessionId: String
)
