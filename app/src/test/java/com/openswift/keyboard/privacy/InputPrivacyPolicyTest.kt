package com.openswift.keyboard.privacy

import android.text.InputType
import android.view.inputmethod.EditorInfo
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InputPrivacyPolicyTest {

    @Test
    fun passwordVariationsAlwaysUseIncognito() {
        val passwordTypes = listOf(
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD,
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD,
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD,
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
        )

        passwordTypes.forEach { inputType ->
            assertTrue(InputPrivacyPolicy.shouldUseIncognito(inputType, imeOptions = 0))
        }
    }

    @Test
    fun noSuggestionsAndNoLearningFlagsUseIncognito() {
        assertTrue(
            InputPrivacyPolicy.shouldUseIncognito(
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS,
                imeOptions = 0
            )
        )
        assertTrue(
            InputPrivacyPolicy.shouldUseIncognito(
                InputType.TYPE_CLASS_TEXT,
                imeOptions = EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING
            )
        )
    }

    @Test
    fun userIncognitoOverridesOrdinaryTextFields() {
        assertTrue(
            InputPrivacyPolicy.shouldUseIncognito(
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL,
                imeOptions = 0,
                userIncognito = true
            )
        )
    }

    @Test
    fun ordinaryTextAndNumericFieldsKeepPredictionsEnabled() {
        assertFalse(
            InputPrivacyPolicy.shouldUseIncognito(
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL,
                imeOptions = 0
            )
        )
        assertFalse(
            InputPrivacyPolicy.shouldUseIncognito(
                InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL,
                imeOptions = 0
            )
        )
    }
}
