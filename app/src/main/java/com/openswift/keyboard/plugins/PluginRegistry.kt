package com.openswift.keyboard.plugins

/**
 * Plugin system placeholder: extensibility for community contributions.
 * Future version (v0.5+) will support:
 *   - Custom input methods
 *   - Theme plugins
 *   - Prediction engine swapping
 *   - Keyboard layout packs
 */

interface KeyboardPlugin {
    fun name(): String
    fun version(): String
    fun onLoad()
    fun onUnload()
}

interface PredictionPlugin : KeyboardPlugin {
    fun suggest(prefix: String, previousWord: String?): List<String>
    fun autoCorrect(typed: String): String
}

interface ThemePlugin : KeyboardPlugin {
    fun theme(): com.openswift.keyboard.theme.KbTheme
}

interface LayoutPlugin : KeyboardPlugin {
    fun layout(): com.openswift.keyboard.layout.KeyLayout
}

/**
 * Plugin registry: discover and load plugins at runtime.
 * Requires plugin interface + ServiceLoader integration.
 */
class PluginRegistry {
    private val plugins: MutableMap<String, KeyboardPlugin> = mutableMapOf()

    fun register(plugin: KeyboardPlugin) {
        plugins[plugin.name()] = plugin
        plugin.onLoad()
    }

    fun unregister(name: String) {
        plugins[name]?.onUnload()
        plugins.remove(name)
    }

    fun get(name: String): KeyboardPlugin? = plugins[name]

    fun listPlugins(): List<String> = plugins.keys.toList()
}
