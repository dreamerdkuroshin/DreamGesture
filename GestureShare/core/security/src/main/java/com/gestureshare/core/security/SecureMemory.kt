package com.gestureshare.core.security

import java.nio.ByteBuffer
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureMemory @Inject constructor() {

    private val random = SecureRandom()

    fun clearByteArray(array: ByteArray) {
        random.nextBytes(array)
        for (i in array.indices) {
            array[i] = 0
        }
    }

    fun clearByteBuffer(buffer: ByteBuffer) {
        if (buffer.hasArray()) {
            clearByteArray(buffer.array())
        }
    }

    fun secureCopy(source: ByteArray): ByteArray {
        return source.copyOf()
    }
}
