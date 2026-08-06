package com.gestureshare.core.security

import org.junit.Before
import org.junit.Test
import com.google.common.truth.Truth.assertThat

class CryptoManagerTest {

    private lateinit var cryptoManager: CryptoManager

    @Before
    fun setup() {
        cryptoManager = CryptoManager()
    }

    @Test
    fun `generateSymmetricKey returns 256-bit key`() {
        val key = cryptoManager.generateSymmetricKey()
        assertThat(key.encoded.size).isEqualTo(32)
    }

    @Test
    fun `encrypt and decrypt roundtrip preserves data`() {
        val key = cryptoManager.generateSymmetricKey()
        val original = "Hello, GestureShare!".toByteArray()

        val encrypted = cryptoManager.encrypt(original, key)
        val decrypted = cryptoManager.decrypt(encrypted, key)

        assertThat(decrypted).isEqualTo(original)
    }

    @Test
    fun `sha256 produces 32-byte hash`() {
        val data = "test data".toByteArray()
        val hash = cryptoManager.sha256(data)
        assertThat(hash.size).isEqualTo(32)
    }

    @Test
    fun `verifyHash returns true for matching data`() {
        val data = "test data".toByteArray()
        val hash = cryptoManager.sha256(data)
        assertThat(cryptoManager.verifyHash(data, hash)).isTrue()
    }

    @Test
    fun `verifyHash returns false for mismatched data`() {
        val data = "test data".toByteArray()
        val other = "other data".toByteArray()
        val hash = cryptoManager.sha256(data)
        assertThat(cryptoManager.verifyHash(other, hash)).isFalse()
    }

    @Test
    fun `generateSessionToken returns 64-character hex string`() {
        val token = cryptoManager.generateSessionToken()
        assertThat(token.length).isEqualTo(64)
        assertThat(token).matches("[0-9a-f]+")
    }
}
