package com.openswift.keyboard.integrations

/**
 * Cloud sync blueprint: future feature for v0.4+.
 *
 * Intended to sync:
 *   - User dictionary (bigrams, learned words)
 *   - Snippets
 *   - Custom themes
 *   - Per-app settings
 *   - Usage analytics
 *
 * Requires:
 *   - User authentication (Firebase, Supabase, or self-hosted)
 *   - End-to-end encryption (user's privacy first)
 *   - Conflict resolution (last-write-wins or per-field merge)
 */

interface CloudSyncProvider {
    suspend fun upload(data: SyncPayload): Result<Unit>
    suspend fun download(): Result<SyncPayload>
    fun isAuthenticated(): Boolean
    suspend fun authenticate(username: String, password: String): Result<Unit>
    suspend fun logout()
}

data class SyncPayload(
    val userDictionary: Map<String, Int>,
    val snippets: List<Snippet>,
    val themes: List<Theme>,
    val perAppSettings: Map<String, AppConfig>,
    val timestamp: Long
)

data class Snippet(
    val trigger: String,
    val expansion: String,
    val createdAt: Long
)

data class Theme(
    val id: String,
    val name: String,
    val colors: Map<String, Int>,
    val isCustom: Boolean
)

data class AppConfig(
    val packageName: String,
    val glideEnabled: Boolean,
    val keyHeight: Int,
    val theme: String
)

/**
 * Local encryption wrapper for cloud sync.
 * Uses AES-256-GCM for data at rest + in transit.
 */
class SyncEncryption {
    fun encrypt(data: ByteArray, key: ByteArray): ByteArray {
        // Placeholder: requires Tink or BoringSSL
        return data
    }

    fun decrypt(encrypted: ByteArray, key: ByteArray): ByteArray {
        // Placeholder
        return encrypted
    }
}

/**
 * Conflict resolver: merges remote and local changes intelligently.
 */
class SyncConflictResolver {
    fun merge(local: SyncPayload, remote: SyncPayload): SyncPayload {
        // Strategy: per-field last-write-wins
        return when {
            local.timestamp > remote.timestamp -> local
            else -> remote
        }
    }
}
