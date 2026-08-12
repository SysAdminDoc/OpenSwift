package com.openswift.keyboard.integrations

import com.openswift.keyboard.BuildConfig
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.nio.ByteBuffer
import java.security.GeneralSecurityException
import java.security.SecureRandom
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/** The only data classes allowed in a sync payload. Editor/app metadata and analytics are excluded. */
data class SyncPayload(
    val userDictionaries: Map<String, Map<String, Int>>,
    val snippets: List<SyncSnippet>,
    val themes: List<SyncTheme>,
    val timestamp: Long,
)

data class SyncSnippet(val trigger: String, val expansion: String)

data class SyncTheme(
    val id: String,
    val name: String,
    val colors: Map<String, Int>,
)

/** A transport never receives plaintext or key material. Authentication belongs to its implementation. */
interface EncryptedSyncTransport {
    suspend fun upload(encryptedEnvelope: ByteArray): Result<Unit>
    suspend fun download(): Result<ByteArray>
}

class SyncFeatureUnavailableException : IllegalStateException(
    "Encrypted sync is not enabled in this build.",
)

class SyncCryptoException(message: String, cause: Throwable? = null) :
    GeneralSecurityException(message, cause)

/**
 * Versioned passphrase envelope using PBKDF2-HMAC-SHA256 and AES-256-GCM.
 *
 * Passphrases are never stored. Callers own the supplied [CharArray] and must clear it after use.
 */
class EncryptedSyncCodec internal constructor(
    private val secureRandom: SecureRandom = SecureRandom(),
    private val encryptionIterations: Int = DEFAULT_PBKDF2_ITERATIONS,
) {
    fun encrypt(plaintext: ByteArray, passphrase: CharArray): ByteArray {
        requirePassphrase(passphrase)
        require(plaintext.size <= MAX_PLAINTEXT_BYTES) {
            "Sync payload exceeds the 4 MiB limit."
        }
        require(encryptionIterations in MIN_PBKDF2_ITERATIONS..MAX_PBKDF2_ITERATIONS) {
            "PBKDF2 iteration count is outside the supported range."
        }

        val salt = ByteArray(SALT_BYTES).also(secureRandom::nextBytes)
        val nonce = ByteArray(NONCE_BYTES).also(secureRandom::nextBytes)
        val ciphertextLength = plaintext.size + TAG_BYTES
        val header = header(encryptionIterations, salt.size, nonce.size, ciphertextLength)
        val keyBytes = deriveKey(passphrase, salt, encryptionIterations)
        return try {
            val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
            cipher.init(
                Cipher.ENCRYPT_MODE,
                SecretKeySpec(keyBytes, "AES"),
                GCMParameterSpec(TAG_BITS, nonce),
            )
            cipher.updateAAD(header)
            cipher.updateAAD(salt)
            cipher.updateAAD(nonce)
            val ciphertext = cipher.doFinal(plaintext)
            check(ciphertext.size == ciphertextLength)
            header + salt + nonce + ciphertext
        } catch (error: GeneralSecurityException) {
            throw SyncCryptoException("Unable to encrypt the sync payload.", error)
        } finally {
            keyBytes.fill(0)
        }
    }

    fun decrypt(envelope: ByteArray, passphrase: CharArray): ByteArray {
        requirePassphrase(passphrase)
        val parsed = parseEnvelope(envelope)
        val keyBytes = deriveKey(passphrase, parsed.salt, parsed.iterations)
        return try {
            val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
            cipher.init(
                Cipher.DECRYPT_MODE,
                SecretKeySpec(keyBytes, "AES"),
                GCMParameterSpec(TAG_BITS, parsed.nonce),
            )
            cipher.updateAAD(parsed.header)
            cipher.updateAAD(parsed.salt)
            cipher.updateAAD(parsed.nonce)
            cipher.doFinal(parsed.ciphertext).also { plaintext ->
                if (plaintext.size > MAX_PLAINTEXT_BYTES) {
                    throw SyncCryptoException("Decrypted sync payload exceeds the 4 MiB limit.")
                }
            }
        } catch (error: AEADBadTagException) {
            throw SyncCryptoException("Wrong passphrase or altered sync file.", error)
        } catch (error: SyncCryptoException) {
            throw error
        } catch (error: GeneralSecurityException) {
            throw SyncCryptoException("Unable to decrypt the sync payload.", error)
        } finally {
            keyBytes.fill(0)
        }
    }

    private fun parseEnvelope(envelope: ByteArray): ParsedEnvelope {
        if (envelope.size < HEADER_BYTES + SALT_BYTES + NONCE_BYTES + TAG_BYTES) {
            throw SyncCryptoException("Sync file is truncated or invalid.")
        }
        val buffer = ByteBuffer.wrap(envelope)
        val magic = ByteArray(MAGIC.size).also(buffer::get)
        if (!magic.contentEquals(MAGIC)) throw SyncCryptoException("Not an OpenSwift sync file.")
        val version = buffer.get().toInt() and 0xFF
        if (version != FORMAT_VERSION) {
            throw SyncCryptoException("Unsupported sync format version $version.")
        }
        val iterations = buffer.int
        if (iterations !in MIN_PBKDF2_ITERATIONS..MAX_PBKDF2_ITERATIONS) {
            throw SyncCryptoException("Sync file uses an unsupported key-derivation cost.")
        }
        val saltLength = buffer.get().toInt() and 0xFF
        val nonceLength = buffer.get().toInt() and 0xFF
        val ciphertextLength = buffer.int
        if (saltLength != SALT_BYTES || nonceLength != NONCE_BYTES) {
            throw SyncCryptoException("Sync file has invalid cryptographic parameters.")
        }
        if (ciphertextLength !in TAG_BYTES..(MAX_PLAINTEXT_BYTES + TAG_BYTES)) {
            throw SyncCryptoException("Sync file has an invalid payload length.")
        }
        if (buffer.remaining() != saltLength + nonceLength + ciphertextLength) {
            throw SyncCryptoException("Sync file is truncated or has trailing data.")
        }
        val header = envelope.copyOfRange(0, HEADER_BYTES)
        val salt = ByteArray(saltLength).also(buffer::get)
        val nonce = ByteArray(nonceLength).also(buffer::get)
        val ciphertext = ByteArray(ciphertextLength).also(buffer::get)
        return ParsedEnvelope(header, iterations, salt, nonce, ciphertext)
    }

    private fun deriveKey(passphrase: CharArray, salt: ByteArray, iterations: Int): ByteArray {
        val spec = PBEKeySpec(passphrase, salt, iterations, KEY_BITS)
        return try {
            SecretKeyFactory.getInstance(KDF_ALGORITHM).generateSecret(spec).encoded
        } catch (error: GeneralSecurityException) {
            throw SyncCryptoException("Unable to derive the sync encryption key.", error)
        } finally {
            spec.clearPassword()
        }
    }

    private fun requirePassphrase(passphrase: CharArray) {
        require(passphrase.size >= MIN_PASSPHRASE_CHARACTERS) {
            "Sync passphrase must contain at least $MIN_PASSPHRASE_CHARACTERS characters."
        }
    }

    private fun header(
        iterations: Int,
        saltLength: Int,
        nonceLength: Int,
        ciphertextLength: Int,
    ): ByteArray = ByteArrayOutputStream(HEADER_BYTES).use { bytes ->
        DataOutputStream(bytes).use { output ->
            output.write(MAGIC)
            output.writeByte(FORMAT_VERSION)
            output.writeInt(iterations)
            output.writeByte(saltLength)
            output.writeByte(nonceLength)
            output.writeInt(ciphertextLength)
        }
        bytes.toByteArray()
    }

    private data class ParsedEnvelope(
        val header: ByteArray,
        val iterations: Int,
        val salt: ByteArray,
        val nonce: ByteArray,
        val ciphertext: ByteArray,
    )

    companion object {
        const val MIN_PASSPHRASE_CHARACTERS = 12
        const val DEFAULT_PBKDF2_ITERATIONS = 600_000
        private const val MIN_PBKDF2_ITERATIONS = 100_000
        private const val MAX_PBKDF2_ITERATIONS = 1_000_000
        private const val MAX_PLAINTEXT_BYTES = 4 * 1024 * 1024
        private const val FORMAT_VERSION = 1
        private const val SALT_BYTES = 16
        private const val NONCE_BYTES = 12
        private const val TAG_BITS = 128
        private const val TAG_BYTES = TAG_BITS / 8
        private const val KEY_BITS = 256
        private const val CIPHER_TRANSFORMATION = "AES/GCM/NoPadding"
        private const val KDF_ALGORITHM = "PBKDF2WithHmacSHA256"
        private val MAGIC = byteArrayOf('O'.code.toByte(), 'S'.code.toByte(), 'W'.code.toByte(), 'S'.code.toByte(), 'Y'.code.toByte(), 'N'.code.toByte(), 'C'.code.toByte())
        private const val HEADER_BYTES = 7 + 1 + Int.SIZE_BYTES + 1 + 1 + Int.SIZE_BYTES
    }
}

/** Feature-gated orchestration; production callers cannot bypass the compile-time gate. */
class EncryptedSyncClient private constructor(
    private val transport: EncryptedSyncTransport,
    private val codec: EncryptedSyncCodec,
    private val enabled: Boolean,
) {
    suspend fun upload(plaintext: ByteArray, passphrase: CharArray): Result<Unit> = runCatching {
        requireEnabled()
        transport.upload(codec.encrypt(plaintext, passphrase)).getOrThrow()
    }

    suspend fun download(passphrase: CharArray): Result<ByteArray> = runCatching {
        requireEnabled()
        codec.decrypt(transport.download().getOrThrow(), passphrase)
    }

    private fun requireEnabled() {
        if (!enabled) throw SyncFeatureUnavailableException()
    }

    companion object {
        fun create(transport: EncryptedSyncTransport): EncryptedSyncClient =
            EncryptedSyncClient(
                transport = transport,
                codec = EncryptedSyncCodec(),
                enabled = BuildConfig.ENABLE_EXPERIMENTAL_SYNC,
            )

        internal fun createForTesting(
            transport: EncryptedSyncTransport,
            codec: EncryptedSyncCodec = EncryptedSyncCodec(),
            enabled: Boolean,
        ): EncryptedSyncClient = EncryptedSyncClient(transport, codec, enabled)
    }
}
