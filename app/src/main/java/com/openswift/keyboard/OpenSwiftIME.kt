package com.openswift.keyboard

import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.os.Vibrator
import com.openswift.keyboard.data.Settings
import com.openswift.keyboard.data.ClipboardHistory
import com.openswift.keyboard.engine.WordList
import com.openswift.keyboard.engine.UserDictionary
import com.openswift.keyboard.engine.Predictor
import com.openswift.keyboard.layout.KeyCode as KC
import com.openswift.keyboard.layout.Layouts
import com.openswift.keyboard.theme.Themes
import com.openswift.keyboard.view.KeyboardView
import com.openswift.keyboard.ui.EmojiView

class OpenSwiftIME : InputMethodService() {

    private lateinit var settings: Settings
    private lateinit var clipboard: ClipboardHistory
    private lateinit var wordList: WordList
    private lateinit var userDict: UserDictionary
    private lateinit var predictor: Predictor
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

    override fun onCreate() {
        super.onCreate()
        settings = Settings(this)
        clipboard = ClipboardHistory(this)
        wordList = WordList(this)
        userDict = UserDictionary(this)
        predictor = Predictor(wordList, userDict)
        snippets = com.openswift.keyboard.data.SnippetManager(this)
        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
    }

    override fun onCreateInputView(): View {
        currentLayout = Layouts.byId(settings.layout)
        keyboardView = KeyboardView(this, settings, predictor, userDict, currentLayout)
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
        clipboard.captureSystem(this)
        currentWord.clear()
        previousWord = ""
        shiftActive = false
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
                    val corrected = predictor.autoCorrect(currentWord.toString(), previousWord)
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
                    val corrected = predictor.autoCorrect(currentWord.toString(), previousWord)
                    userDict.learn(previousWord.ifEmpty { null }, corrected)
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
        val sugg = predictor.suggest(currentWord.toString(), previousWord, limit = 3)
        keyboardView.setSuggestions(sugg)
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
            userDict.learn(previousWord.ifEmpty { null }, currentWord.toString())
            userDict.save()
        }
    }
}

