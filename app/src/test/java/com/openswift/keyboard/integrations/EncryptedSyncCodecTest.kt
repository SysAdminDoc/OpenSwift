package com.openswift.keyboard.integrations

import com.openswift.keyboard.BuildConfig
import java.security.SecureRandom
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Test

class EncryptedSyncCodecTest {
    private val codec = EncryptedSyncCodec(
        secureRandom = SecureRandom(),
        encryptionIterations = 100_000,
    )

    @Test
    fun encryptedEnvelopeRoundTripsAndUsesFreshRandomness() {
        val plaintext = "dictionary snippets themes".toByteArray()
        val passphrase = "correct horse battery".toCharArray()

        val first = codec.encrypt(plaintext, passphrase)
        val second = codec.encrypt(plaintext, passphrase)

        assertFalse(first.contentEquals(plaintext))
        assertFalse(first.contentEquals(second))
        assertArrayEquals(plaintext, codec.decrypt(first, passphrase))
        passphrase.fill('\u0000')
    }

    @Test
    fun alteredCiphertextFailsAuthentication() {
        val passphrase = "correct horse battery".toCharArray()
        val encrypted = codec.encrypt("private words".toByteArray(), passphrase)
        encrypted[encrypted.lastIndex] = (encrypted.last().toInt() xor 1).toByte()

        val error = assertThrows(SyncCryptoException::class.java) {
            codec.decrypt(encrypted, passphrase)
        }

        assertTrue(error.message.orEmpty().contains("Wrong passphrase or altered"))
        passphrase.fill('\u0000')
    }

    @Test
    fun wrongPassphraseAndTruncatedFilesFailClosed() {
        val encrypted = codec.encrypt(
            "private words".toByteArray(),
            "correct horse battery".toCharArray(),
        )

        assertThrows(SyncCryptoException::class.java) {
            codec.decrypt(encrypted, "incorrect horse pass".toCharArray())
        }
        assertThrows(SyncCryptoException::class.java) {
            codec.decrypt(encrypted.copyOf(12), "correct horse battery".toCharArray())
        }
    }

    @Test
    fun passphraseLengthIsBounded() {
        assertThrows(IllegalArgumentException::class.java) {
            codec.encrypt(byteArrayOf(1), CharArray(EncryptedSyncCodec.MIN_PASSPHRASE_CHARACTERS - 1))
        }
        assertThrows(IllegalArgumentException::class.java) {
            codec.encrypt(byteArrayOf(1), CharArray(EncryptedSyncCodec.MAX_PASSPHRASE_CHARACTERS + 1))
        }
    }

    @Test
    fun productionSyncEntryPointIsCompileTimeDisabledAndDoesNotReachTransport() {
        val transport = RecordingTransport()
        val client = EncryptedSyncClient.create(transport)

        val result = runSuspend {
            client.upload("plaintext".toByteArray(), "correct horse battery".toCharArray())
        }

        assertFalse(BuildConfig.ENABLE_EXPERIMENTAL_SYNC)
        assertTrue(result.exceptionOrNull() is SyncFeatureUnavailableException)
        assertFalse(transport.uploadCalled)
    }

    @Test
    fun scopedPayloadContainsOnlyDictionarySnippetAndThemeData() {
        val fields = SyncPayload::class.java.declaredFields.map { it.name }.toSet()

        assertTrue(fields.containsAll(setOf("userDictionaries", "snippets", "themes", "timestamp")))
        assertFalse(fields.any { it.contains("app", ignoreCase = true) })
        assertFalse(fields.any { it.contains("analytic", ignoreCase = true) })
        assertNotEquals(0, fields.size)
    }

    @Test
    fun encryptedSnapshotRoundTripsUtf8Json() {
        val snapshot = EncryptedSyncSnapshot(codec)
        val passphrase = "correct horse battery".toCharArray()
        val json = """{"schemaVersion":1,"snippets":[{"x":"café"}]}"""

        val envelope = snapshot.encryptJson(json, passphrase)

        assertEquals(json, snapshot.decryptJson(envelope, passphrase))
        passphrase.fill('\u0000')
        envelope.fill(0)
    }

    @Test
    fun encryptedSnapshotRejectsMalformedUtf8() {
        val passphrase = "correct horse battery".toCharArray()
        val envelope = codec.encrypt(byteArrayOf(0xC3.toByte(), 0x28), passphrase)

        val error = assertThrows(SyncCryptoException::class.java) {
            EncryptedSyncSnapshot(codec).decryptJson(envelope, passphrase)
        }

        assertTrue(error.message.orEmpty().contains("UTF-8"))
        passphrase.fill('\u0000')
        envelope.fill(0)
    }

    private class RecordingTransport : EncryptedSyncTransport {
        var uploadCalled = false

        override suspend fun upload(encryptedEnvelope: ByteArray): Result<Unit> {
            uploadCalled = true
            return Result.success(Unit)
        }

        override suspend fun download(): Result<ByteArray> = Result.success(byteArrayOf())
    }

    private fun <T> runSuspend(block: suspend () -> T): T {
        var completed: Result<T>? = null
        block.startCoroutine(
            object : Continuation<T> {
                override val context = EmptyCoroutineContext

                override fun resumeWith(result: Result<T>) {
                    completed = result
                }
            },
        )
        return checkNotNull(completed) { "Test coroutine unexpectedly suspended" }.getOrThrow()
    }
}
