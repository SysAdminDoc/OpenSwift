package com.openswift.keyboard.plugins

import com.openswift.keyboard.BuildConfig
import com.openswift.keyboard.layout.KeyLayout
import com.openswift.keyboard.layout.Layouts
import com.openswift.keyboard.theme.KbTheme
import com.openswift.keyboard.theme.Themes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Test

class PluginRegistryTest {

    @Test
    fun productionRegistryIsCompileTimeDisabled() {
        val registry = PluginRegistry.create()

        assertFalse(BuildConfig.ENABLE_EXPERIMENTAL_PLUGINS)
        assertThrows(PluginFeatureUnavailableException::class.java) {
            registry.register(TestPredictionPlugin())
        }
    }

    @Test
    fun predictionResultsAreBoundedDeduplicatedAndTrimmed() {
        val registry = PluginRegistry.createForTesting(enabled = true)
        val plugin = TestPredictionPlugin(
            suggestions = listOf(" hello ", "hello", "help", "held", "helmet", "helium"),
        )
        assertEquals(PluginRegistrationResult.REGISTERED, registry.register(plugin))

        val suggestions = registry.suggest(
            plugin.descriptor.id,
            PredictionRequest(prefix = "hel", previousWord = "say"),
        )

        assertEquals(listOf("hello", "help", "held", "helmet", "helium"), suggestions)
        assertEquals("HELLO", registry.autoCorrect(plugin.descriptor.id, "hello"))
        assertEquals(PluginStatus.ACTIVE, registry.status(plugin.descriptor.id))
    }

    @Test
    fun runtimeFailureDisablesOnlyTheFailingPluginAndCallsUnload() {
        val registry = PluginRegistry.createForTesting(enabled = true)
        val failing = TestPredictionPlugin(throwOnSuggest = true)
        val healthy = TestPredictionPlugin(id = "healthy.plugin")
        registry.register(failing)
        registry.register(healthy)

        assertTrue(
            registry.suggest(failing.descriptor.id, PredictionRequest("a", null)).isEmpty(),
        )

        assertEquals(PluginStatus.FAILED, registry.status(failing.descriptor.id))
        assertTrue(failing.unloaded)
        assertEquals(PluginStatus.ACTIVE, registry.status(healthy.descriptor.id))
        assertEquals(listOf("alpha"), registry.suggest(healthy.descriptor.id, PredictionRequest("a", null)))
    }

    @Test
    fun loadFailureIsContainedAndNeverBecomesActive() {
        val registry = PluginRegistry.createForTesting(enabled = true)
        val plugin = TestPredictionPlugin(throwOnLoad = true)

        val result = registry.register(plugin)

        assertEquals(PluginRegistrationResult.LOAD_FAILED, result)
        assertEquals(PluginStatus.FAILED, registry.status(plugin.descriptor.id))
        assertTrue(plugin.unloaded)
        assertTrue(registry.listPlugins().isEmpty())
    }

    @Test
    fun declaredCapabilitiesMustMatchImplementedInterfaces() {
        val registry = PluginRegistry.createForTesting(enabled = true)
        val plugin = TestPredictionPlugin(
            capabilities = setOf(PluginCapability.PREDICTION, PluginCapability.THEME),
        )

        val error = assertThrows(PluginContractException::class.java) {
            registry.register(plugin)
        }

        assertTrue(error.message.orEmpty().contains("exactly match"))
    }

    @Test
    fun themeAndLayoutResultsStayInsidePluginNamespace() {
        val registry = PluginRegistry.createForTesting(enabled = true)
        val plugin = VisualPlugin()
        registry.register(plugin)

        assertEquals("plugin_visual.plugin_dark", registry.theme(plugin.descriptor.id)?.id)
        assertEquals("plugin_visual.plugin_qwerty", registry.layout(plugin.descriptor.id)?.id)
    }

    @Test
    fun invalidVisualResultFailsThePluginWithoutEscaping() {
        val registry = PluginRegistry.createForTesting(enabled = true)
        val plugin = VisualPlugin(invalidThemeId = true)
        registry.register(plugin)

        assertNull(registry.theme(plugin.descriptor.id))
        assertEquals(PluginStatus.FAILED, registry.status(plugin.descriptor.id))
        assertTrue(plugin.unloaded)
    }

    private class TestPredictionPlugin(
        id: String = "test.plugin",
        private val suggestions: List<String> = listOf("alpha"),
        private val throwOnSuggest: Boolean = false,
        private val throwOnLoad: Boolean = false,
        capabilities: Set<PluginCapability> = setOf(PluginCapability.PREDICTION),
    ) : PredictionPlugin {
        override val descriptor = PluginDescriptor(
            id = id,
            name = "Test prediction",
            version = "1.0.0",
            capabilities = capabilities,
        )
        var unloaded = false

        override fun onLoad() {
            if (throwOnLoad) error("load failed")
        }

        override fun onUnload() {
            unloaded = true
        }

        override fun suggest(request: PredictionRequest): List<String> {
            if (throwOnSuggest) error("suggest failed")
            return suggestions
        }

        override fun autoCorrect(typed: String): String = typed.uppercase()
    }

    private class VisualPlugin(private val invalidThemeId: Boolean = false) : ThemePlugin, LayoutPlugin {
        override val descriptor = PluginDescriptor(
            id = "visual.plugin",
            name = "Visual plugin",
            version = "1.0.0",
            capabilities = setOf(PluginCapability.THEME, PluginCapability.LAYOUT),
        )
        var unloaded = false

        override fun onUnload() {
            unloaded = true
        }

        override fun theme(): KbTheme = Themes.Amoled.copy(
            id = if (invalidThemeId) "global_theme" else "plugin_visual.plugin_dark",
        )

        override fun layout(): KeyLayout = Layouts.Qwerty.copy(id = "plugin_visual.plugin_qwerty")
    }
}
