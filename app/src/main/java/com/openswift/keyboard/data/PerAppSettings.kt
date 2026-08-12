package com.openswift.keyboard.data

import android.content.Context
import android.content.SharedPreferences

internal interface PerAppSettingsStore {
    val keys: Set<String>
    fun getBoolean(key: String, defaultValue: Boolean): Boolean
    fun getInt(key: String, defaultValue: Int): Int
    fun update(values: Map<String, Any?>)
}

private class SharedPreferencesPerAppStore(
    private val preferences: SharedPreferences,
) : PerAppSettingsStore {
    override val keys: Set<String>
        get() = preferences.all.keys

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean =
        preferences.getBoolean(key, defaultValue)

    override fun getInt(key: String, defaultValue: Int): Int =
        preferences.getInt(key, defaultValue)

    override fun update(values: Map<String, Any?>) {
        val editor = preferences.edit()
        values.forEach { (key, value) ->
            when (value) {
                is Boolean -> editor.putBoolean(key, value)
                is Int -> editor.putInt(key, value)
                null -> editor.remove(key)
                else -> error("Unsupported per-app preference value for $key")
            }
        }
        editor.apply()
    }
}

internal class MutableMapPerAppSettingsStore(
    private val values: MutableMap<String, Any> = mutableMapOf(),
) : PerAppSettingsStore {
    override val keys: Set<String>
        get() = values.keys.toSet()

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean =
        values[key] as? Boolean ?: defaultValue

    override fun getInt(key: String, defaultValue: Int): Int =
        values[key] as? Int ?: defaultValue

    override fun update(values: Map<String, Any?>) {
        values.forEach { (key, value) ->
            if (value == null) this.values.remove(key) else this.values[key] = value
        }
    }
}

/** Per-editor-app overrides resolved from [android.view.inputmethod.EditorInfo.packageName]. */
class PerAppSettings internal constructor(private val store: PerAppSettingsStore) {

    constructor(ctx: Context) : this(
        SharedPreferencesPerAppStore(SecurePreferences.open(ctx, TypedDataStores.PER_APP)),
    )

    data class AppConfig(
        val packageName: String,
        val predictionsDisabled: Boolean = false,
        val glideDisabled: Boolean = false,
        val keyHeightOverride: Int? = null,
    ) {
        val hasOverrides: Boolean
            get() = predictionsDisabled || glideDisabled || keyHeightOverride != null
    }

    data class EffectiveConfig(
        val predictionsEnabled: Boolean,
        val glideEnabled: Boolean,
        val keyHeightDp: Int,
    )

    fun saveAppConfig(config: AppConfig): String? {
        val packageName = config.packageName.trim()
        validate(packageName, config.keyHeightOverride)?.let { return it }
        if (!config.hasOverrides) {
            removeAppConfig(packageName)
            return null
        }
        store.update(
            mapOf(
                predictionKey(packageName) to config.predictionsDisabled,
                glideKey(packageName) to config.glideDisabled,
                heightKey(packageName) to (config.keyHeightOverride ?: NO_HEIGHT_OVERRIDE),
            ),
        )
        return null
    }

    fun setAppConfig(
        packageName: String,
        glideDisabled: Boolean = false,
        keyHeight: Int? = null,
        predictionsDisabled: Boolean = false,
    ) {
        val error = saveAppConfig(
            AppConfig(
                packageName = packageName,
                predictionsDisabled = predictionsDisabled,
                glideDisabled = glideDisabled,
                keyHeightOverride = keyHeight,
            ),
        )
        require(error == null) { error.orEmpty() }
    }

    fun getAppConfig(packageName: String): AppConfig {
        val normalizedPackage = packageName.trim()
        val height = store.getInt(heightKey(normalizedPackage), NO_HEIGHT_OVERRIDE)
        return AppConfig(
            packageName = normalizedPackage,
            predictionsDisabled = store.getBoolean(predictionKey(normalizedPackage), false),
            glideDisabled = store.getBoolean(glideKey(normalizedPackage), false),
            keyHeightOverride = height.takeIf { it in MIN_KEY_HEIGHT_DP..MAX_KEY_HEIGHT_DP },
        )
    }

    fun removeAppConfig(packageName: String) {
        val normalizedPackage = packageName.trim()
        store.update(
            mapOf(
                predictionKey(normalizedPackage) to null,
                glideKey(normalizedPackage) to null,
                heightKey(normalizedPackage) to null,
            ),
        )
    }

    fun resetAll() {
        store.update(store.keys.associateWith { null })
    }

    fun getAllAppConfigs(): List<AppConfig> {
        val packageNames = store.keys.mapNotNull(::packageNameFromKey).toSortedSet()
        return packageNames.map(::getAppConfig).filter(AppConfig::hasOverrides)
    }

    companion object {
        const val MIN_KEY_HEIGHT_DP = 48
        const val MAX_KEY_HEIGHT_DP = 72
        private const val NO_HEIGHT_OVERRIDE = -1
        private const val PREDICTION_PREFIX = "prediction_off_"
        private const val GLIDE_PREFIX = "glide_off_"
        private const val HEIGHT_PREFIX = "height_"
        private val PACKAGE_NAME = Regex("^[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z][A-Za-z0-9_]*)+$")

        fun resolve(
            config: AppConfig,
            globalGlideEnabled: Boolean,
            globalKeyHeightDp: Int,
        ): EffectiveConfig = EffectiveConfig(
            predictionsEnabled = !config.predictionsDisabled,
            glideEnabled = globalGlideEnabled && !config.glideDisabled,
            keyHeightDp = config.keyHeightOverride ?: globalKeyHeightDp,
        )

        private fun validate(packageName: String, keyHeight: Int?): String? = when {
            !PACKAGE_NAME.matches(packageName) -> "Enter a valid app package name, such as com.example.app."
            keyHeight != null && keyHeight !in MIN_KEY_HEIGHT_DP..MAX_KEY_HEIGHT_DP ->
                "Key height must be between $MIN_KEY_HEIGHT_DP and $MAX_KEY_HEIGHT_DP dp."
            else -> null
        }

        private fun predictionKey(packageName: String) = "$PREDICTION_PREFIX$packageName"
        private fun glideKey(packageName: String) = "$GLIDE_PREFIX$packageName"
        private fun heightKey(packageName: String) = "$HEIGHT_PREFIX$packageName"

        private fun packageNameFromKey(key: String): String? = when {
            key.startsWith(PREDICTION_PREFIX) -> key.removePrefix(PREDICTION_PREFIX)
            key.startsWith(GLIDE_PREFIX) -> key.removePrefix(GLIDE_PREFIX)
            key.startsWith(HEIGHT_PREFIX) -> key.removePrefix(HEIGHT_PREFIX)
            else -> null
        }
    }
}
