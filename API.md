# API Reference

OpenSwift's core APIs for developers integrating with the keyboard or extending it.

## Core Services

### OpenSwiftIME

Main `InputMethodService` entry point. Manages lifecycle, input routing, and feature coordination.

```kotlin
class OpenSwiftIME : InputMethodService() {
    // Lifecycle
    override fun onCreate()
    override fun onCreateInputView(): View
    override fun onStartInputView(info: EditorInfo, restarting: Boolean)
    override fun onFinishInputView(finishingInput: Boolean)
    
    // Input
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean
    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean
    
    // IME Methods
    fun onKeyPressed(keyCode: String, isLongPress: Boolean = false)
    fun insertText(text: String)
    fun deleteCharacter()
    fun commitText(text: String)
}
```

### KeyboardView

Custom View rendering the keyboard and handling touch events.

```kotlin
class KeyboardView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    // Setup
    fun setLayout(layout: KeyLayout)
    fun setTheme(theme: KbTheme)
    fun setListener(listener: KeyboardListener)
    
    // State
    fun setShiftActive(active: Boolean)
    fun setSymbolsActive(active: Boolean)
    fun showSuggestions(suggestions: List<String>)
    fun clearGlideTrail()
    
    // Touch events (internal)
    override fun onTouchEvent(event: MotionEvent): Boolean
    
    // Listeners
    interface KeyboardListener {
        fun onKeyPressed(keyCode: String)
        fun onGlide(word: String)
        fun onSuggestionSelected(suggestion: String)
    }
}
```

## Prediction Engine

### Predictor

Scores and ranks word candidates.

```kotlin
class Predictor(
    wordList: WordList,
    userDictionary: UserDictionary
) {
    // Core methods
    fun suggest(prefix: String, previousWord: String? = null): List<String>
    fun autoCorrect(typed: String): String?
    
    // Scoring
    fun scoreCandidate(
        candidate: String,
        prefix: String,
        previousWord: String?
    ): Double
    
    // Edit distance
    fun damerauLevenshtein(s1: String, s2: String): Int
}
```

### GlideDecoder

Converts gesture polylines to words.

```kotlin
class GlideDecoder(wordList: WordList) {
    // Main API
    fun decode(path: List<Pair<Float, Float>>): String?
    
    // Internals
    fun pickAnchors(path: List<Pair<Float, Float>>): List<KeyPos>
    fun scoreGlide(anchors: List<KeyPos>, candidate: String): Double
    fun isSubsequence(anchors: List<KeyPos>, candidate: String): Boolean
}
```

### WordList

Loads and queries the static dictionary.

```kotlin
class WordList(context: Context) {
    fun getWords(): List<String>
    fun getFrequency(word: String): Int
    fun contains(word: String): Boolean
    fun startsWith(prefix: String): List<String>
    fun similar(word: String, maxDistance: Int): List<String>
}
```

### UserDictionary

Persistent user learning (bigrams, custom words).

```kotlin
class UserDictionary(context: Context) {
    // Learning
    fun learn(word: String)
    fun learnBigram(prevWord: String, nextWord: String)
    
    // Querying
    fun getFrequency(word: String): Int
    fun nextAfter(prevWord: String): List<String>
    fun getBigrams(): Map<String, Map<String, Int>>
    
    // Persistence
    fun save()
    fun clear()
}
```

## Layouts & Themes

### KeyLayout

Represents a keyboard layout (QWERTY, etc.).

```kotlin
data class KeyLayout(
    val id: String,
    val name: String,
    val rows: List<List<KeyDef>>
) {
    // Long-press accent popups
    val popups: Map<Char, String>
    
    // Helpers
    fun getKey(keyCode: String): KeyDef?
    fun getKeyAtPosition(x: Float, y: Float): KeyDef?
}

data class KeyDef(
    val code: String,           // "a", "Shift", "Backspace"
    val displayText: String,    // "a", "⇧", "⌫"
    val width: Float = 1.0f,    // Key width multiplier
    val isModifier: Boolean = false,
    val type: KeyType = KeyType.NORMAL
)

enum class KeyType {
    NORMAL, MODIFIER, ACTION, SPECIAL
}
```

### KbTheme

Defines colors and styling.

```kotlin
data class KbTheme(
    val id: String,
    val name: String,
    val background: Int,            // 0xAARRGGBB
    val keyBackground: Int,
    val keyModifierBackground: Int,
    val keyText: Int,
    val keyAccent: Int,
    val suggestionBg: Int,
    val suggestionText: Int,
    val gestureTrail: Int
)
```

## Settings & Persistence

### Settings

SharedPreferences wrapper for keyboard preferences.

```kotlin
class Settings(context: Context) {
    // Theme
    fun getThemeId(): String
    fun setThemeId(id: String)
    
    // Layout
    fun getLayoutId(): String
    fun setLayoutId(id: String)
    
    // Features
    fun isGlideEnabled(): Boolean
    fun setGlideEnabled(enabled: Boolean)
    
    fun isAutoCorrectionEnabled(): Boolean
    fun setAutoCorrectionEnabled(enabled: Boolean)
    
    fun isAutoCapitalizeEnabled(): Boolean
    fun setAutoCapitalizeEnabled(enabled: Boolean)
    
    fun isHapticEnabled(): Boolean
    fun setHapticEnabled(enabled: Boolean)
    
    fun isSoundEnabled(): Boolean
    fun setSoundEnabled(enabled: Boolean)
    
    fun getKeyHeight(): Int  // dp
    fun setKeyHeight(height: Int)
}
```

### ClipboardHistory

Tracks recent clipboard items.

```kotlin
class ClipboardHistory(context: Context) {
    fun addItem(text: String)
    fun getItems(): List<String>
    fun deleteItem(text: String)
    fun clear()
}
```

### SnippetManager

Text expansion (trigger → expansion).

```kotlin
data class Snippet(
    val trigger: String,
    val expansion: String,
    val id: String = UUID.randomUUID().toString()
)

class SnippetManager(context: Context) {
    fun addSnippet(trigger: String, expansion: String): Snippet
    fun getSnippets(): List<Snippet>
    fun getSnippet(trigger: String): Snippet?
    fun deleteSnippet(id: String)
    fun expandSnippet(trigger: String): String?
}
```

## Analytics & Tracking

### UsageAnalytics

Local-only keystroke and correction tracking.

```kotlin
class UsageAnalytics(context: Context) {
    fun trackKeyPress(keyCode: String)
    fun trackWord(word: String)
    fun trackGlide(word: String)
    fun trackCorrection(original: String, corrected: String)
    
    fun getStats(): AnalyticsData
    fun clear()
}

data class AnalyticsData(
    val totalKeyPresses: Long,
    val totalWords: Long,
    val glideUsagePercent: Float,
    val correctionRate: Float,
    val mostUsedWords: List<Pair<String, Int>>
)
```

## Advanced Features

### VoiceRecognizer

Speech-to-text wrapper.

```kotlin
class VoiceRecognizer(context: Context) {
    fun start(listener: SpeechListener)
    fun stop()
    fun cancel()
    
    interface SpeechListener {
        fun onPartialResult(text: String)
        fun onFinalResult(text: String)
        fun onError(error: String)
        fun onReady()
    }
}
```

### AccessibilityHelper

TalkBack integration.

```kotlin
class AccessibilityHelper(context: Context) {
    fun announce(text: String)
    fun announceKey(keyName: String)
    fun announceSuggestion(suggestion: String)
    fun isTalkBackEnabled(): Boolean
}
```

### PerAppSettings

Per-application configuration.

```kotlin
data class AppConfig(
    val packageName: String,
    val glideEnabled: Boolean = true,
    val keyHeight: Int = 56,
    val themeId: String = "amoled"
)

class PerAppSettings(context: Context) {
    fun getConfig(packageName: String): AppConfig
    fun setConfig(packageName: String, config: AppConfig)
    fun deleteConfig(packageName: String)
}
```

## Examples

### Adding a Word to User Dictionary

```kotlin
val userDictionary = UserDictionary(context)
userDictionary.learn("example")
userDictionary.learnBigram("previous", "example")
userDictionary.save()
```

### Creating a Custom Theme

```kotlin
val customTheme = KbTheme(
    id = "my-custom",
    name = "My Theme",
    background = 0xFF0A0E27,
    keyBackground = 0xFF1A1F3A,
    keyModifierBackground = 0xFF050712,
    keyText = 0xFF00FF88,
    keyAccent = 0xFFFF00FF,
    suggestionBg = 0xFF0F1429,
    suggestionText = 0xFF00FFDD,
    gestureTrail = 0xFFFF00FF
)

val settings = Settings(context)
// Save and apply theme (UI integration in development)
```

### Implementing a Custom Prediction Engine

Create a `PredictionPlugin` (see [plugins/PluginRegistry.kt](../app/src/main/java/com/openswift/keyboard/plugins/PluginRegistry.kt)):

```kotlin
class CustomPredictor : PredictionPlugin {
    override fun name() = "my-predictor"
    override fun version() = "1.0.0"
    
    override fun onLoad() {
        // Initialize
    }
    
    override fun onUnload() {
        // Cleanup
    }
    
    override fun suggest(prefix: String, previousWord: String?): List<String> {
        // Your prediction logic
        return emptyList()
    }
    
    override fun autoCorrect(typed: String): String {
        // Your correction logic
        return typed
    }
}
```

---

For architecture details, see [CONTRIBUTING.md](CONTRIBUTING.md). For user-facing features, see [GUIDE.md](GUIDE.md).
