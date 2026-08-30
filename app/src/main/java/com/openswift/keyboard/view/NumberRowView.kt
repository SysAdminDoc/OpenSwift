package com.openswift.keyboard.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.openswift.keyboard.layout.NumberRowLayout
import com.openswift.keyboard.theme.Themes

class NumberRowView @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null,
) : View(ctx, attrs) {

    var onKeyListener: ((Int, String) -> Unit)? = null

    private val keys = NumberRowLayout.toLayout().rows.first()
    private val theme = Themes.Amoled
    private val keyBounds = ArrayList<Rect>(keys.size)
    private val keyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 18f
        color = theme.keyText
    }
    private val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = theme.keyAccent
        strokeWidth = 1f
    }

    init {
        isClickable = true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val measuredWidth = MeasureSpec.getSize(widthMeasureSpec)
        val measuredHeight = (56 * resources.displayMetrics.density).toInt()
        setMeasuredDimension(measuredWidth, measuredHeight)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val totalWeight = keys.sumOf { it.widthWeight.toDouble() }
        var x = 0.0
        keyBounds.clear()
        keys.forEachIndexed { index, key ->
            val keyWidth = (w.toDouble() / totalWeight) * key.widthWeight.toDouble()
            val right = if (index == keys.lastIndex) w else (x + keyWidth).toInt()
            keyBounds.add(Rect(x.toInt(), 0, right, h))
            x += keyWidth
        }
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(theme.keyBackground)
        val viewHeight = height.toFloat()
        for (index in keys.indices) {
            val key = keys[index]
            val bounds = keyBounds.getOrNull(index) ?: continue
            canvas.drawLine(bounds.left.toFloat(), 0f, bounds.right.toFloat(), 0f, dividerPaint)
            canvas.drawLine(
                bounds.left.toFloat(),
                viewHeight - 1f,
                bounds.right.toFloat(),
                viewHeight - 1f,
                dividerPaint,
            )
            canvas.drawText(key.label, bounds.exactCenterX(), viewHeight * 0.6f, keyPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean = when (event.actionMasked) {
        MotionEvent.ACTION_DOWN -> keyIndexAt(event.x, event.y) != null
        MotionEvent.ACTION_UP -> {
            keyIndexAt(event.x, event.y)?.let { index ->
                val key = keys[index]
                performClick()
                onKeyListener?.invoke(key.code, key.label)
            }
            true
        }
        MotionEvent.ACTION_CANCEL -> true
        else -> true
    }

    override fun performClick(): Boolean = super.performClick()

    private fun keyIndexAt(x: Float, y: Float): Int? {
        val xCoordinate = x.toInt()
        val yCoordinate = y.toInt()
        return keyBounds.indexOfFirst { it.contains(xCoordinate, yCoordinate) }
            .takeIf { it >= 0 }
    }
}
