package com.openswift.keyboard

import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodSubtype
import android.widget.FrameLayout
import android.os.Vibrator
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.openswift.keyboard.data.Settings
import com.openswift.keyboard.data.ClipboardHistory
import com.openswift.keyboard.data.KeyboardLanguages
import com.openswift.keyboard.engine.WordList
import com.openswift.keyboard.engine.UserDictionary
import com.openswift.keyboard.engine.MultilingualPredictor
import com.openswift.keyboard.engine.LanguageDetector
import com.openswift.keyboard.engine.WordCommitPolicy
import com.openswift.keyboard.layout.KeyCode as KC
import com.openswift.keyboard.layout.Layouts
import com.openswift.keyboard.privacy.InputPrivacyPolicy
import com.openswift.keyboard.theme.Themes
import com.openswift.keyboard.view.KeyboardView
import com.openswift.keyboard.ui.EmojiView

class OpenSwiftIME : InputMethodService() {

    private lateinit var settings: Settings
    private lateinit var clipboard: ClipboardHistory
    private lateinit var wordList: WordList
    private lateinit var userDict: UserDictionary
    private lateinit var predictor: MultilingualPredictor
    private lateinit var languageDetector: LanguageDetector
    private lateinit var snippets: com.openswift.keyboard.data.SnippetManager
    private lateinit var keyboardView: KeyboardView
    private lateinit var emojiView: EmojiView
    private lateinit var clipboardView: com.openswift.keyboard.ui.ClipboardView
    private lateinit var keyboardInputView: View
    private lateinit var emojiInputView: View
    private lateinit var clipboardInputView: View
    private lateinit var numberRowView: com.openswift.keyboard.view.NumberRowView
    private lateinit var vibrator: Vibrator

    private var currentLayout = Layouts.Qwerty
    private var shiftActive = false
    private var symbolsActive = false
    private var emojiMode = false
    private var clipboardMode = false
    private var numberRowShown = false
    private var previousWord = ""
    private var currentWord = StringBuilder()
    private var activeLanguageCode = KeyboardLanguages.English.code
    private val languageContext = ArrayDeque<String>()
    private var refreshingLanguage = false
    private var privacyModeActive = false

    override fun onCreate() {
        super.onCreate()
        settings = Settings(this)
        clipboard = ClipboardHistory(this)
        predictor = MultilingualPredictor(this)
        languageDetector = LanguageDetector(predictor.supportedLanguages()) { language, word ->
            predictor.frequency(language, word)
        }
        refreshLanguageState()
        snippets = com.openswift.keyboard.data.SnippetManager(this)
        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
    }

    override fun onCreateInputView(): View {
        refreshLanguageState()
        currentLayout = Layouts.byId(settings.layout)
        keyboardView = KeyboardView(this, settings, wordList, userDict, currentLayout)
        keyboardView.setPredictionEnabled(!privacyModeActive)
        keyboardView.setOnKeyListener { code, label ->
            onKeyPressed(code, label)
        }
        keyboardView.setOnGlideListener { word ->
            commitWord(word)
        }
        keyboardInputView = withNavigationBarInset(keyboardView, Themes.byId(settings.theme).background)
        
        emojiView = EmojiView(this)
        emojiView.onEmojiSelected = { emoji ->
            currentInputConnection?.commitText(emoji, 1)
            emojiMode = false
            showKeyboardView()
        }
        emojiInputView = withNavigationBarInset(emojiView, Themes.Amoled.background)

        clipboardView = com.openswift.keyboard.ui.ClipboardView(this)
        clipboardView.onItemSelected = { item ->
            currentInputConnection?.commitText(item, 1)
            clipboardMode = false
            showKeyboardView()
        }
        clipboardView.onItemDeleted = { _ ->
            clipboardView.refresh()
        }
        clipboardView.onClose = {
            clipboardMode = false
            showKeyboardView()
        }
        clipboardInputView = withNavigationBarInset(clipboardView, Themes.Amoled.background)

        numberRowView = com.openswift.keyboard.view.NumberRowView(this)
        numberRowView.onKeyListener = { code, label ->
            onKeyPressed(code, label)
        }
        
        return keyboardInputView
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        privacyModeActive = InputPrivacyPolicy.shouldUseIncognito(info, settings.incognitoMode)
        currentWord.clear()
        languageContext.clear()
        previousWord = ""
        refreshLanguageState()
        if (::keyboardView.isInitialized) {
            keyboardView.setPredictionEnabled(!privacyModeActive)
        }
        clipboard.captureSystem(
            ctx = this,
            enabled = settings.clipboardEnabled,
            privateField = privacyModeActive
        )
        shiftActive = settings.autoCapitalize // Start with shift active if auto-capitalize is on
        symbolsActive = false
        emojiMode = false
        clipboardMode = false
        numberRowShown = false
        updateSuggestions()
    }

    private fun onKeyPressed(code: Int, label: String) {
        val ic = currentInputConnection ?: return
        when (code) {
            KC.SPACE -> {
                if (currentWord.isEmpty()) {
                    ic.commitText(" ", 1)
                } else {
                    commitWord(correctedCurrentWord())
                    ic.commitText(" ", 1)
                    updateSuggestions()
                }
                if (settings.autoCapitalize) requestShift(true)
            }
            KC.ENTER -> {
                if (currentWord.isNotEmpty()) commitWord(correctedCurrentWord())
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
                currentWord.clear()
                previousWord = ""
                updateSuggestions()
                if (settings.autoCapitalize) requestShift(true)
            }
            KC.DELETE -> {
                if (currentWord.isNotEmpty()) {
                    currentWord.deleteCharAt(currentWord.length - 1)
                    ic.deleteSurroundingText(1, 0)
                } else {
                    ic.deleteSurroundingText(1, 0)
                }
                updateSuggestions()
            }
            KC.SHIFT -> {
                shiftActive = !shiftActive
                keyboardView.setShift(shiftActive)
            }
            KC.SYMBOLS -> {
                symbolsActive = true
                currentLayout = Layouts.Symbols
                keyboardView.updateLayout(currentLayout)
            }
            KC.ABC -> {
                symbolsActive = false
                numberRowShown = false
                currentLayout = Layouts.byId(settings.layout)
                keyboardView.updateLayout(currentLayout)
            }
            KC.SHIFT_SYMBOLS -> {
                currentLayout = Layouts.SymbolsShift
                keyboardView.updateLayout(currentLayout)
            }
            KC.EMOJI -> {
                emojiMode = true
                showEmojiView()
            }
            KC.CLIPBOARD -> showClipboardView()
            KC.SETTINGS -> {
                startActivity(android.content.Intent(this, com.openswift.keyboard.ui.MainActivity::class.java))
            }
            KC.COMMA, KC.PERIOD -> {
                if (currentWord.isNotEmpty()) commitWord(correctedCurrentWord())
                val ch = label[0]
                ic.commitText(ch.toString(), 1)
                shiftActive = false
                keyboardView.setShift(false)
                updateSuggestions()
            }
            else -> {
                if (label.length == 1) {
                    val ch = label[0]
                    if (ch.isDigit()) {
                        // Check for snippet expansion
                        val expanded = if (privacyModeActive) null else snippets.expand(ch.toString())
                        if (expanded != null) {
                            ic.commitText(expanded, 1)
                        } else {
                            currentWord.append(ch)
                            ic.commitText(ch.toString(), 1)
                        }
                    } else {
                        currentWord.append(ch)
                        val text = if (shiftActive) ch.uppercase() else ch.toString()
                        ic.commitText(text, 1)
                        shiftActive = false
                        keyboardView.setShift(false)
                    }
                    updateSuggestions()
                }
            }
        }
        if (settings.hapticFeedback) vibrator.vibrate(20)
    }

    private fun commitWord(word: String) {
        val ic = currentInputConnection ?: return
        val typedWord = currentWord.toString()
        if (!privacyModeActive) {
            userDict.learn(previousWord.ifEmpty { null }, word)
            rememberLanguageToken(word)
        }
        val plan = WordCommitPolicy.plan(typedWord, word, shiftActive)
        if (plan.charactersToDelete > 0) {
            ic.deleteSurroundingText(plan.charactersToDelete, 0)
        }
        plan.textToCommit?.let { ic.commitText(it, 1) }
        currentWord.clear()
        previousWord = if (privacyModeActive) "" else word
        shiftActive = false
        keyboardView.setShift(false)
    }

    private fun correctedCurrentWord(): String {
        val typedWord = currentWord.toString()
        if (privacyModeActive || !settings.autoCorrect) return typedWord
        detectLanguageForCurrentContext(typedWord)
        return predictor.autoCorrect(activeLanguageCode, typedWord, previousWord)
    }

    private fun requestShift(on: Boolean) {
        shiftActive = on
        keyboardView.setShift(on)
    }

    private fun updateSuggestions() {
        if (privacyModeActive) {
            keyboardView.setSuggestions(emptyList())
            return
        }
        if (!refreshingLanguage) {
            detectLanguageForCurrentContext(currentWord.toString())
        }
        val sugg = predictor.suggest(activeLanguageCode, currentWord.toString(), previousWord, limit = 3)
        keyboardView.setSuggestions(sugg)
    }

    override fun onCurrentInputMethodSubtypeChanged(newSubtype: InputMethodSubtype?) {
        super.onCurrentInputMethodSubtypeChanged(newSubtype)
        val locale = newSubtype?.languageTag?.takeIf { it.isNotBlank() } ?: newSubtype?.locale
        if (!locale.isNullOrBlank()) {
            settings.language = KeyboardLanguages.byLocale(locale).code
            refreshLanguageState()
        }
    }

    private fun refreshLanguageState() {
        refreshingLanguage = true
        val language = KeyboardLanguages.byCode(settings.language)
        activeLanguageCode = language.code
        wordList = predictor.wordList(language.code)
        userDict = predictor.userDictionary(language.code)
        if (!symbolsActive) {
            currentLayout = Layouts.byId(settings.layout)
        }
        if (::keyboardView.isInitialized) {
            keyboardView.updateDictionary(wordList, userDict)
            if (!symbolsActive) {
                keyboardView.updateLayout(currentLayout)
            }
            updateSuggestions()
        }
        refreshingLanguage = false
    }

    private fun detectLanguageForCurrentContext(currentToken: String) {
        if (!settings.languageDetection) return
        val tokens = languageContext + listOf(currentToken)
        val detected = languageDetector.detect(tokens, activeLanguageCode) ?: return
        settings.language = detected.languageCode
        refreshLanguageState()
    }

    private fun rememberLanguageToken(word: String) {
        val token = word.lowercase().filter { it.isLetter() || it == '¡' || it == '¿' }
        if (token.length < 2) return
        languageContext.addLast(token)
        while (languageContext.size > LANGUAGE_CONTEXT_LIMIT) {
            languageContext.removeFirst()
        }
    }

    private fun showEmojiView() {
        setInputView(emojiInputView)
    }

    private fun showClipboardView() {
        if (privacyModeActive) return
        clipboardMode = true
        clipboardView.refresh()
        setInputView(clipboardInputView)
    }

    private fun showKeyboardView() {
        setInputView(keyboardInputView)
    }

    private fun withNavigationBarInset(content: View, backgroundColor: Int): View =
        FrameLayout(this).apply {
            setBackgroundColor(backgroundColor)
            addView(
                content,
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                ),
            )
            ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
                val bottomInset = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
                if (view.paddingBottom != bottomInset) {
                    view.setPadding(0, 0, 0, bottomInset)
                }
                insets
            }
        }

    override fun onFinishInput() {
        super.onFinishInput()
        if (currentWord.isNotEmpty() && !privacyModeActive) {
            detectLanguageForCurrentContext(currentWord.toString())
            userDict.learn(previousWord.ifEmpty { null }, currentWord.toString())
            rememberLanguageToken(currentWord.toString())
            userDict.save()
        }
        currentWord.clear()
        languageContext.clear()
        previousWord = ""
    }

    companion object {
        private const val LANGUAGE_CONTEXT_LIMIT = 8
    }
}

