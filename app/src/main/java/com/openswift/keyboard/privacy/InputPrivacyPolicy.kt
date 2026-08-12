package com.openswift.keyboard.privacy

import android.text.InputType
import android.view.inputmethod.EditorInfo

/** Derives the IME's runtime privacy state from the active editor and user setting. */
object InputPrivacyPolicy {

    fun shouldUseIncognito(editorInfo: EditorInfo?, userIncognito: Boolean): Boolean {
        return shouldUseIncognito(
            inputType = editorInfo?.inputType ?: InputType.TYPE_NULL,
            imeOptions = editorInfo?.imeOptions ?: 0,
            userIncognito = userIncognito
        )
    }

    fun shouldUseIncognito(
        inputType: Int,
        imeOptions: Int,
        userIncognito: Boolean = false
    ): Boolean {
        if (userIncognito) return true
        if (imeOptions and EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING != 0) return true

        val inputClass = inputType and InputType.TYPE_MASK_CLASS
        val variation = inputType and InputType.TYPE_MASK_VARIATION

        if (inputClass == InputType.TYPE_CLASS_TEXT) {
            if (inputType and InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS != 0) return true
            if (variation in TEXT_PASSWORD_VARIATIONS) return true
        }

        return inputClass == InputType.TYPE_CLASS_NUMBER &&
            variation == InputType.TYPE_NUMBER_VARIATION_PASSWORD
    }

    private val TEXT_PASSWORD_VARIATIONS = setOf(
        InputType.TYPE_TEXT_VARIATION_PASSWORD,
        InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD,
        InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD
    )
}
