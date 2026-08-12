package com.openswift.keyboard.plugins

import com.openswift.keyboard.BuildConfig
import com.openswift.keyboard.layout.KeyCode
import com.openswift.keyboard.layout.KeyLayout
import com.openswift.keyboard.theme.KbTheme

object PluginApi {
    const val VERSION = 1
}

enum class PluginCapability { PREDICTION, THEME, LAYOUT }

data class PluginDescriptor(
    val id: String,
    val name: String,
    val version: String,
    val apiVersion: Int = PluginApi.VERSION,
    val capabilities: Set<PluginCapability>,
)

data class PredictionRequest(val prefix: String, val previousWord: String?)

interface KeyboardPlugin {
    val descriptor: PluginDescriptor
    fun onLoad() = Unit
    fun onUnload() = Unit
}

interface PredictionPlugin : KeyboardPlugin {
    fun suggest(request: PredictionRequest): List<String>
    fun autoCorrect(typed: String): String
}

interface ThemePlugin : KeyboardPlugin {
    fun theme(): KbTheme
}

interface LayoutPlugin : KeyboardPlugin {
    fun layout(): KeyLayout
}

enum class PluginStatus { ACTIVE, FAILED }

enum class PluginRegistrationResult { REGISTERED, LOAD_FAILED }

class PluginFeatureUnavailableException : IllegalStateException(
    "Runtime plugins are not enabled in this build.",
)

class PluginContractException(message: String) : IllegalArgumentException(message)

/**
 * In-process, explicitly registered plugin boundary.
 *
 * There is deliberately no ServiceLoader, APK loading, reflection discovery, Context, or file/network
 * access in this API. Every operation is validated and isolated before its result reaches the IME.
 */
class PluginRegistry private constructor(private val enabled: Boolean) {
    private data class Entry(val plugin: KeyboardPlugin, val descriptor: PluginDescriptor)

    private val active = linkedMapOf<String, Entry>()
    private val failed = linkedMapOf<String, String>()

    fun register(plugin: KeyboardPlugin): PluginRegistrationResult {
        requireEnabled()
        val descriptor = plugin.descriptor
        validateDescriptor(plugin, descriptor)
        if (active.size >= MAX_PLUGINS) {
            throw PluginContractException("At most $MAX_PLUGINS plugins can be active.")
        }
        if (descriptor.id in active || descriptor.id in failed) {
            throw PluginContractException("Plugin id ${descriptor.id} is already registered.")
        }
        return try {
            plugin.onLoad()
            active[descriptor.id] = Entry(plugin, descriptor)
            PluginRegistrationResult.REGISTERED
        } catch (error: Throwable) {
            rethrowFatal(error)
            failed[descriptor.id] = safeMessage(error)
            runCatching { plugin.onUnload() }
            PluginRegistrationResult.LOAD_FAILED
        }
    }

    fun unregister(id: String) {
        requireEnabled()
        val entry = active.remove(id)
        failed.remove(id)
        if (entry != null) {
            try {
                entry.plugin.onUnload()
            } catch (error: Throwable) {
                rethrowFatal(error)
            }
        }
    }

    fun listPlugins(): List<PluginDescriptor> {
        requireEnabled()
        return active.values.map(Entry::descriptor).sortedBy(PluginDescriptor::id)
    }

    fun status(id: String): PluginStatus? {
        requireEnabled()
        return when (id) {
            in active -> PluginStatus.ACTIVE
            in failed -> PluginStatus.FAILED
            else -> null
        }
    }

    fun failureReason(id: String): String? {
        requireEnabled()
        return failed[id]
    }

    fun suggest(id: String, request: PredictionRequest): List<String> {
        requireEnabled()
        validateText(request.prefix, "Prediction prefix")
        request.previousWord?.let { validateText(it, "Previous word") }
        val plugin = active[id]?.plugin as? PredictionPlugin ?: return emptyList()
        return isolate(id, emptyList()) {
            val raw = plugin.suggest(request)
            if (raw.size > MAX_RAW_SUGGESTIONS) {
                throw PluginContractException(
                    "Prediction plugin returned more than $MAX_RAW_SUGGESTIONS candidates.",
                )
            }
            raw.asSequence()
                .map(String::trim)
                .onEach { validateText(it, "Prediction candidate") }
                .filter(String::isNotEmpty)
                .distinct()
                .take(MAX_SUGGESTIONS)
                .toList()
        }
    }

    fun autoCorrect(id: String, typed: String): String {
        requireEnabled()
        validateText(typed, "Typed word")
        val plugin = active[id]?.plugin as? PredictionPlugin ?: return typed
        return isolate(id, typed) {
            plugin.autoCorrect(typed).also { validateText(it, "Correction") }
        }
    }

    fun theme(id: String): KbTheme? {
        requireEnabled()
        val entry = active[id] ?: return null
        val plugin = entry.plugin as? ThemePlugin ?: return null
        return isolate(id, null) {
            plugin.theme().also { validateTheme(entry.descriptor, it) }
        }
    }

    fun layout(id: String): KeyLayout? {
        requireEnabled()
        val entry = active[id] ?: return null
        val plugin = entry.plugin as? LayoutPlugin ?: return null
        return isolate(id, null) {
            plugin.layout().also { validateLayout(entry.descriptor, it) }
        }
    }

    private fun validateDescriptor(plugin: KeyboardPlugin, descriptor: PluginDescriptor) {
        if (!PLUGIN_ID.matches(descriptor.id)) {
            throw PluginContractException(
                "Plugin id must be 3-64 lowercase letters, numbers, dots, underscores, or hyphens.",
            )
        }
        if (descriptor.name.isBlank() || descriptor.name.length > 64) {
            throw PluginContractException("Plugin name must contain 1-64 characters.")
        }
        if (descriptor.version.isBlank() || descriptor.version.length > 32) {
            throw PluginContractException("Plugin version must contain 1-32 characters.")
        }
        if (descriptor.apiVersion != PluginApi.VERSION) {
            throw PluginContractException(
                "Plugin API ${descriptor.apiVersion} is incompatible with host API ${PluginApi.VERSION}.",
            )
        }
        val implemented = buildSet {
            if (plugin is PredictionPlugin) add(PluginCapability.PREDICTION)
            if (plugin is ThemePlugin) add(PluginCapability.THEME)
            if (plugin is LayoutPlugin) add(PluginCapability.LAYOUT)
        }
        if (descriptor.capabilities.isEmpty() || descriptor.capabilities != implemented) {
            throw PluginContractException(
                "Declared capabilities must exactly match implemented plugin interfaces.",
            )
        }
    }

    private fun validateTheme(descriptor: PluginDescriptor, theme: KbTheme) {
        if (!theme.id.startsWith("plugin_${descriptor.id}_")) {
            throw PluginContractException(
                "Theme id must start with plugin_${descriptor.id}_.",
            )
        }
        if (theme.name.isBlank() || theme.name.length > 48) {
            throw PluginContractException("Plugin theme name must contain 1-48 characters.")
        }
    }

    private fun validateLayout(descriptor: PluginDescriptor, layout: KeyLayout) {
        if (!layout.id.startsWith("plugin_${descriptor.id}_")) {
            throw PluginContractException(
                "Layout id must start with plugin_${descriptor.id}_.",
            )
        }
        if (layout.rows.size !in 3..6) {
            throw PluginContractException("Plugin layout must contain 3-6 rows.")
        }
        val keys = layout.rows.flatMap { row ->
            if (row.size !in 2..16) {
                throw PluginContractException("Every plugin layout row must contain 2-16 keys.")
            }
            row
        }
        if (keys.size !in 15..64) {
            throw PluginContractException("Plugin layout must contain 15-64 keys.")
        }
        val characterLabels = mutableSetOf<String>()
        keys.forEach { key ->
            if (!key.widthWeight.isFinite() || key.widthWeight !in 0.25f..5f) {
                throw PluginContractException("Plugin key width must be between 0.25 and 5.")
            }
            if (key.code >= 0) {
                if (key.label.length != 1 || !characterLabels.add(key.label)) {
                    throw PluginContractException(
                        "Plugin character keys must have unique single-character labels.",
                    )
                }
            } else if (key.code !in SUPPORTED_SPECIAL_CODES) {
                throw PluginContractException("Plugin layout contains unsupported key code ${key.code}.")
            }
        }
        REQUIRED_LAYOUT_CODES.forEach { (code, name) ->
            if (keys.count { it.code == code } != 1) {
                throw PluginContractException("Plugin layout must contain exactly one $name key.")
            }
        }
    }

    private fun validateText(value: String, label: String) {
        if (value.length > MAX_TEXT_LENGTH || value.any(Char::isISOControl)) {
            throw PluginContractException(
                "$label must be at most $MAX_TEXT_LENGTH characters without control characters.",
            )
        }
    }

    private fun fail(id: String, error: Throwable) {
        val entry = active.remove(id) ?: return
        failed[id] = safeMessage(error)
        try {
            entry.plugin.onUnload()
        } catch (unloadError: Throwable) {
            rethrowFatal(unloadError)
        }
    }

    private inline fun <T> isolate(id: String, fallback: T, operation: () -> T): T = try {
        operation()
    } catch (error: Throwable) {
        rethrowFatal(error)
        fail(id, error)
        fallback
    }

    private fun requireEnabled() {
        if (!enabled) throw PluginFeatureUnavailableException()
    }

    private fun rethrowFatal(error: Throwable) {
        if (error is VirtualMachineError || error is ThreadDeath) throw error
        if (error is InterruptedException) Thread.currentThread().interrupt()
    }

    private fun safeMessage(error: Throwable): String =
        error.message?.take(MAX_FAILURE_LENGTH) ?: error::class.java.simpleName

    companion object {
        private const val MAX_PLUGINS = 8
        private const val MAX_RAW_SUGGESTIONS = 32
        private const val MAX_SUGGESTIONS = 5
        private const val MAX_TEXT_LENGTH = 64
        private const val MAX_FAILURE_LENGTH = 160
        private const val SPACER_CODE = -100
        private val PLUGIN_ID = Regex("^[a-z][a-z0-9_.-]{2,63}$")
        private val SUPPORTED_SPECIAL_CODES = setOf(
            KeyCode.SHIFT,
            KeyCode.DELETE,
            KeyCode.ENTER,
            KeyCode.SPACE,
            KeyCode.SYMBOLS,
            KeyCode.CLIPBOARD,
            KeyCode.COMMA,
            KeyCode.PERIOD,
            KeyCode.EMOJI,
            KeyCode.SETTINGS,
            SPACER_CODE,
        )
        private val REQUIRED_LAYOUT_CODES = linkedMapOf(
            KeyCode.SHIFT to "Shift",
            KeyCode.DELETE to "Delete",
            KeyCode.ENTER to "Enter",
            KeyCode.SPACE to "Space",
            KeyCode.SYMBOLS to "Symbols",
        )

        fun create(): PluginRegistry = PluginRegistry(BuildConfig.ENABLE_EXPERIMENTAL_PLUGINS)

        internal fun createForTesting(enabled: Boolean): PluginRegistry = PluginRegistry(enabled)
    }
}
