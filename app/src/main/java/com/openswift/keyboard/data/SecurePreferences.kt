package com.openswift.keyboard.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/** Opens encrypted preference stores and migrates matching legacy plaintext entries once. */
object SecurePreferences {
    private const val SECURE_PREFIX = "openswift_secure_"

    internal data class MigrationPlan(
        val entriesToWrite: Map<String, Any>,
        val legacyKeysToRemove: Set<String>
    )

    fun open(
        context: Context,
        storeName: String,
        legacyPreferences: SharedPreferences = context.getSharedPreferences(
            storeName,
            Context.MODE_PRIVATE
        ),
        migrateKey: (String) -> Boolean = { true }
    ): SharedPreferences {
        val appContext = context.applicationContext
        val masterKey = MasterKey.Builder(appContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        val encrypted = EncryptedSharedPreferences.create(
            appContext,
            SECURE_PREFIX + storeName,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        migrateLegacy(legacyPreferences, encrypted, migrateKey)
        return encrypted
    }

    fun clear(
        context: Context,
        storeName: String,
        legacyPreferences: SharedPreferences = context.getSharedPreferences(
            storeName,
            Context.MODE_PRIVATE
        ),
        migrateKey: (String) -> Boolean = { true }
    ) {
        open(context, storeName, legacyPreferences, migrateKey).edit().clear().commit()
        val legacyEditor = legacyPreferences.edit()
        legacyPreferences.all.keys.filter(migrateKey).forEach(legacyEditor::remove)
        legacyEditor.commit()
    }

    internal fun migrationPlan(
        legacyEntries: Map<String, *>,
        encryptedKeys: Set<String>,
        migrateKey: (String) -> Boolean = { true }
    ): MigrationPlan {
        val supported = legacyEntries
            .filterKeys(migrateKey)
            .mapNotNull { (key, value) -> normalizeValue(value)?.let { key to it } }
            .toMap()
        return MigrationPlan(
            entriesToWrite = supported.filterKeys { it !in encryptedKeys },
            legacyKeysToRemove = supported.keys
        )
    }

    private fun migrateLegacy(
        legacy: SharedPreferences,
        encrypted: SharedPreferences,
        migrateKey: (String) -> Boolean
    ) {
        val plan = migrationPlan(legacy.all, encrypted.all.keys, migrateKey)
        if (plan.legacyKeysToRemove.isEmpty()) return

        val encryptedEditor = encrypted.edit()
        plan.entriesToWrite.forEach { (key, value) -> encryptedEditor.putValue(key, value) }
        if (!encryptedEditor.commit()) return

        val legacyEditor = legacy.edit()
        plan.legacyKeysToRemove.forEach(legacyEditor::remove)
        legacyEditor.commit()
    }

    private fun normalizeValue(value: Any?): Any? = when (value) {
        is String, is Int, is Long, is Float, is Boolean -> value
        is Set<*> -> value.filterIsInstance<String>().toSet()
        else -> null
    }

    @Suppress("UNCHECKED_CAST")
    private fun SharedPreferences.Editor.putValue(key: String, value: Any) {
        when (value) {
            is String -> putString(key, value)
            is Int -> putInt(key, value)
            is Long -> putLong(key, value)
            is Float -> putFloat(key, value)
            is Boolean -> putBoolean(key, value)
            is Set<*> -> putStringSet(key, value as Set<String>)
        }
    }
}
