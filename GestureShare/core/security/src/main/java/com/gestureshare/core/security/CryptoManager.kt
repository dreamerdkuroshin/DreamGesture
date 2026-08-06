package com.gestureshare.core.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.security.spec.ECGenParameterSpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyAgreement
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CryptoManager @Inject constructor() {

    companion object {
        private const val KEYSTORE_ALIAS = "gesture_share_ecdh"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val CURVE_NAME = "secp384r1"
        private const val AES_GCM_TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH = 128
        private const val GCM_IV_LENGTH = 12
    }

    private val secureRandom = SecureRandom()

    fun generateEphemeralKeyPair(): KeyPair {
        val generator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_EC,
            ANDROID_KEYSTORE
        )
        val spec = KeyGenParameterSpec.Builder(
            "${KEYSTORE_ALIAS}_${System.currentTimeMillis()}",
            KeyProperties.PURPOSE_AGREE_KEY
        )
            .setAlgorithmParameterSpec(ECGenParameterSpec(CURVE_NAME))
            .setUserAuthenticationRequired(false)
            .build()
        generator.initialize(spec)
        return generator.generateKeyPair()
    }

    fun deriveSharedSecret(privateKey: PrivateKey, peerPublicKey: PublicKey): SecretKey {
        val agreement = KeyAgreement.getInstance("ECDH")
        agreement.init(privateKey)
        agreement.doPhase(peerPublicKey, true)
        val sharedSecret = agreement.generateSecret()
        return SecretKeySpec(sharedSecret, "AES")
    }

    fun generateSymmetricKey(): SecretKey {
        val keyBytes = ByteArray(32)
        secureRandom.nextBytes(keyBytes)
        return SecretKeySpec(keyBytes, "AES")
    }

    fun encrypt(data: ByteArray, key: SecretKey): EncryptedPayload {
        val iv = ByteArray(GCM_IV_LENGTH)
        secureRandom.nextBytes(iv)
        val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        val ciphertext = cipher.doFinal(data)
        return EncryptedPayload(iv, ciphertext)
    }

    fun decrypt(payload: EncryptedPayload, key: SecretKey): ByteArray {
        val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, payload.iv))
        return cipher.doFinal(payload.ciphertext)
    }

    fun decrypt(ciphertext: ByteArray, iv: ByteArray, key: SecretKey): ByteArray {
        val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        return cipher.doFinal(ciphertext)
    }

    fun generateSessionToken(): String {
        val token = ByteArray(32)
        secureRandom.nextBytes(token)
        return token.joinToString("") { "%02x".format(it) }
    }

    fun sha256(data: ByteArray): ByteArray {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        return digest.digest(data)
    }

    fun verifyHash(data: ByteArray, expectedHash: ByteArray): Boolean {
        return sha256(data).contentEquals(expectedHash)
    }
}

data class EncryptedPayload(
    val iv: ByteArray,
    val ciphertext: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EncryptedPayload) return false
        return iv.contentEquals(other.iv) && ciphertext.contentEquals(other.ciphertext)
    }

    override fun hashCode(): Int = iv.contentHashCode() * 31 + ciphertext.contentHashCode()
}
