package com.gestureshare.ffi

class NativeBridge {

    companion object {
        init {
            System.loadLibrary("gesture_protocol")
        }

        @JvmStatic
        external fun nativeInit(): Long

        @JvmStatic
        external fun nativeStartDiscovery(handle: Long, port: Int): Boolean

        @JvmStatic
        external fun nativeStopDiscovery(handle: Long)

        @JvmStatic
        external fun nativeEncrypt(
            handle: Long,
            data: ByteArray,
            key: ByteArray,
            nonce: ByteArray
        ): ByteArray?

        @JvmStatic
        external fun nativeDecrypt(
            handle: Long,
            data: ByteArray,
            key: ByteArray,
            nonce: ByteArray
        ): ByteArray?

        @JvmStatic
        external fun nativeSha256(data: ByteArray): ByteArray

        @JvmStatic
        external fun nativeGenerateSessionToken(): String

        @JvmStatic
        external fun nativeFree(handle: Long)
    }

    private var nativeHandle: Long = 0

    fun initialize(): Boolean {
        nativeHandle = NativeBridge.nativeInit()
        return nativeHandle != 0L
    }

    fun startDiscovery(port: Int): Boolean {
        return NativeBridge.nativeStartDiscovery(nativeHandle, port)
    }

    fun stopDiscovery() {
        NativeBridge.nativeStopDiscovery(nativeHandle)
    }

    fun encrypt(data: ByteArray, key: ByteArray, nonce: ByteArray): ByteArray? {
        return NativeBridge.nativeEncrypt(nativeHandle, data, key, nonce)
    }

    fun decrypt(data: ByteArray, key: ByteArray, nonce: ByteArray): ByteArray? {
        return NativeBridge.nativeDecrypt(nativeHandle, data, key, nonce)
    }

    fun sha256(data: ByteArray): ByteArray {
        return NativeBridge.nativeSha256(data)
    }

    fun generateToken(): String {
        return NativeBridge.nativeGenerateSessionToken()
    }

    fun release() {
        if (nativeHandle != 0L) {
            NativeBridge.nativeFree(nativeHandle)
            nativeHandle = 0
        }
    }

    protected fun finalize() {
        release()
    }
}
