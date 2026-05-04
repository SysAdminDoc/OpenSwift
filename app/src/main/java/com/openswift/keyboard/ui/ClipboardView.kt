package com.openswift.keyboard.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.openswift.keyboard.data.ClipboardHistory
import com.openswift.keyboard.theme.Themes

class ClipboardView @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null
) : View(ctx, attrs) {

    var onItemSelected: ((String) -> Unit)? = null
    var onItemDeleted: ((String) -> Unit)? = null
    var clipboard = ClipboardHistory(ctx)

    private val theme = Themes.Amoled
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.LEFT
        textSize = 14f
        color = theme.keyText
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.LEFT
        textSize = 12f
        color = theme.keyText.and(0x99FFFFFF)
    }
    private val deletePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 16f
        color = theme.keyAccent
    }

    private val itemBounds = mutableMapOf<String, Rect>()
    private val deleteBounds = mutableMapOf<String, Rect>()
    private val rowHeight = 52f

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val items = clipboard.items()
        val h = (items.size * rowHeight * resources.displayMetrics.density).toInt().coerceAtLeast(48)
        setMeasuredDimension(w, h)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(theme.background)
        val items = clipboard.items()
        val w = width.toFloat()
        val deleteWidth = 50f

        itemBounds.clear()
        deleteBounds.clear()
        var y = 0f
        for (item in items) {
            val preview = item.take(48).replace("\n", " ")
            val itemRect = Rect(0, y.toInt(), (w - deleteWidth).toInt(), (y + rowHeight).toInt())
            itemBounds[item] = itemRect

            val deleteRect = Rect((w - deleteWidth).toInt(), y.toInt(), w.toInt(), (y + rowHeight).toInt())
            deleteBounds[item] = deleteRect

            canvas.drawRect(itemRect.left.toFloat(), itemRect.top.toFloat(),
                itemRect.right.toFloat(), itemRect.bottom.toFloat(),
                Paint().apply { color = theme.keyBackground; style = Paint.Style.FILL })
            canvas.drawRect(deleteRect.left.toFloat(), deleteRect.top.toFloat(),
                deleteRect.right.toFloat(), deleteRect.bottom.toFloat(),
                Paint().apply { color = theme.keyModifierBackground; style = Paint.Style.FILL })

            canvas.drawText(preview, 12f, y + rowHeight * 0.65f, textPaint)
            canvas.drawText("×", w - deleteWidth / 2, y + rowHeight * 0.65f, deletePaint)

            y += rowHeight
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            // Check delete button
            for ((item, deleteRect) in deleteBounds) {
                if (deleteRect.contains(event.x.toInt(), event.y.toInt())) {
                    clipboard.remove(item)
                    onItemDeleted?.invoke(item)
                    invalidate()
                    return true
                }
            }
            // Check item tap
            for ((item, itemRect) in itemBounds) {
                if (itemRect.contains(event.x.toInt(), event.y.toInt())) {
                    onItemSelected?.invoke(item)
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }

    fun refresh() {
        invalidate()
    }
}
