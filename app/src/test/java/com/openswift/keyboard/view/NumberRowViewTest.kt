package com.openswift.keyboard.view

import android.view.MotionEvent
import android.view.View
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class NumberRowViewTest {

    @Test
    fun touchSequenceEmitsTheSelectedNumber() {
        val view = measuredView(width = 1_000)
        val emitted = mutableListOf<Pair<Int, String>>()
        view.onKeyListener = { code, label -> emitted += code to label }

        assertTrue(view.onTouchEvent(event(MotionEvent.ACTION_DOWN, x = 50f)))
        assertTrue(view.onTouchEvent(event(MotionEvent.ACTION_UP, x = 50f)))

        assertEquals(listOf('1'.code to "1"), emitted)
    }

    @Test
    fun finalNumberOwnsTheLastPixelInTheRow() {
        val view = measuredView(width = 997)
        val emitted = mutableListOf<String>()
        view.onKeyListener = { _, label -> emitted += label }

        assertTrue(view.onTouchEvent(event(MotionEvent.ACTION_DOWN, x = 996f)))
        assertTrue(view.onTouchEvent(event(MotionEvent.ACTION_UP, x = 996f)))

        assertEquals(listOf("0"), emitted)
    }

    private fun measuredView(width: Int): NumberRowView {
        val view = NumberRowView(RuntimeEnvironment.getApplication())
        view.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(200, View.MeasureSpec.AT_MOST),
        )
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        return view
    }

    private fun event(action: Int, x: Float): MotionEvent = MotionEvent.obtain(
        0L,
        10L,
        action,
        x,
        10f,
        0,
    )
}
