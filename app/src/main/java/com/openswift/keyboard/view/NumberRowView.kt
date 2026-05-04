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
    attrs: AttributeSet? = null
) : View(ctx, attrs) {

    var onKeyListener: ((Int, String) -> Unit)? = null

    private val layout = NumberRowLayout.toLayout()
    private val theme = Themes.Amoled
    private val keyBounds = mutableMapOf<String, Rect>()
    private val keyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 18f
        color = theme.keyText
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = (56 * resources.displayMetrics.density).toInt()
        setMeasuredDimension(w, h)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(theme.keyBackground)

        val w = width.toFloat()
        val h = height.toFloat()
        val row = layout.rows.first()
        val totalWeight = row.sumOf { it.widthWeight.toDouble() }
        var x = 0.0

        keyBounds.clear()
        for (key in row) {
            val kw = (w.toDouble() / totalWeight) * key.widthWeight.toDouble()
            val rect = Rect(x.toInt(), 0, (x + kw).toInt(), h.toInt())
            keyBounds[key.label] = rect

            canvas.drawRect(x.toFloat(), 0f, (x + kw).toFloat(), h, Paint().apply { color = theme.keyBackground })
            canvas.drawLine(x.toFloat(), 0f, (x + kw).toFloat(), 0f, Paint().apply { color = theme.keyAccent })
            canvas.drawLine(x.toFloat(), h - 1, (x + kw).toFloat(), h - 1, Paint().apply { color = theme.keyAccent })
            canvas.drawText(key.label, (x + kw / 2).toFloat(), h * 0.6f, keyPaint)
            x += kw
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            for ((label, rect) in keyBounds) {
                if (rect.contains(event.x.toInt(), event.y.toInt())) {
                    onKeyListener?.invoke(label[0].code, label)
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }
}
