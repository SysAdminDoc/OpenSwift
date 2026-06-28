package com.openswift.keyboard

import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodSubtype
import android.os.Vibrator
import com.openswift.keyboard.data.Settings
import com.openswift.keyboard.data.ClipboardHistory
import com.openswift.keyboard.data.KeyboardLanguages
import com.openswift.keyboard.engine.WordList
import com.openswift.keyboard.engine.UserDictionary
import com.openswift.keyboard.engine.MultilingualPredictor
import com.openswift.keyboard.engine.LanguageDetector
import com.openswift.keyboard.layout.KeyCode as KC
import com.openswift.keyboard.layout.Layouts
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
        keyboardView.setOnKeyListener { code, label ->
            onKeyPressed(code, label)
        }
        keyboardView.setOnGlideListener { word ->
            commitWord(word)
        }
        
        emojiView = EmojiView(this)
        emojiView.onEmojiSelected = { emoji ->
            currentInputConnection?.commitText(emoji, 1)
            emojiMode = false
            showKeyboardView()
        }

        clipboardView = com.openswift.keyboard.ui.ClipboardView(this)
        clipboardView.onItemSelected = { item ->
            currentInputConnection?.commitText(item, 1)
            clipboardMode = false
            showKeyboardView()
        }
        clipboardView.onItemDeleted = { _ ->
            clipboardView.refresh()
        }

        numberRowView = com.openswift.keyboard.view.NumberRowView(this)
        numberRowView.onKeyListener = { code, label ->
            onKeyPressed(code, label)
        }
        
        return keyboardView
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        refreshLanguageState()
        clipboard.captureSystem(this)
        currentWord.clear()
        languageContext.clear()
        previousWord = ""
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
                    detectLanguageForCurrentContext(currentWord.toString())
                    val corrected = predictor.autoCorrect(activeLanguageCode, currentWord.toString(), previousWord)
                    commitWord(corrected)
                    previousWord = corrected
                    ic.commitText(" ", 1)
                    currentWord.clear()
                    updateSuggestions()
                }
                if (settings.autoCapitalize) requestShift(true)
            }
            KC.ENTER -> {
                if (currentWord.isNotEmpty()) {
                    detectLanguageForCurrentContext(currentWord.toString())
                    val corrected = predictor.autoCorrect(activeLanguageCode, currentWord.toString(), previousWord)
                    userDict.learn(previousWord.ifEmpty { null }, corrected)
                    rememberLanguageToken(corrected)
                    previousWord = corrected
                }
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
            KC.SETTINGS -> {
                startActivity(android.content.Intent(this, com.openswift.keyboard.ui.MainActivity::class.java))
            }
            KC.COMMA, KC.PERIOD -> {
                val ch = label[0]
                currentWord.append(ch)
                val text = if (shiftActive) ch.uppercase() else ch.toString()
                ic.commitText(text, 1)
                shiftActive = false
                keyboardView.setShift(false)
                updateSuggestions()
            }
            else -> {
                if (label.length == 1) {
                    val ch = label[0]
                    if (ch.isDigit()) {
                        // Check for snippet expansion
                        val expanded = snippets.expand(ch.toString())
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
        userDict.learn(previousWord.ifEmpty { null }, word)
        rememberLanguageToken(word)
        val text = if (shiftActive && currentWord.isNotEmpty()) word.replaceFirstChar { it.uppercase() } else word
        ic.commitText(text, 1)
        currentWord.clear()
        previousWord = word
        shiftActive = false
        keyboardView.setShift(false)
    }

    private fun requestShift(on: Boolean) {
        shiftActive = on
        keyboardView.setShift(on)
    }

    private fun updateSuggestions() {
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
        setInputView(emojiView)
    }

    private fun showClipboardView() {
        clipboardMode = true
        setInputView(clipboardView)
    }

    private fun showKeyboardView() {
        setInputView(keyboardView)
    }

    override fun onFinishInput() {
        super.onFinishInput()
        if (currentWord.isNotEmpty()) {
            detectLanguageForCurrentContext(currentWord.toString())
            userDict.learn(previousWord.ifEmpty { null }, currentWord.toString())
            rememberLanguageToken(currentWord.toString())
            userDict.save()
        }
    }

    companion object {
        private const val LANGUAGE_CONTEXT_LIMIT = 8
    }
}

