package com.openswift.keyboard.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import com.openswift.keyboard.R
import com.openswift.keyboard.data.ClipboardHistory
import com.openswift.keyboard.theme.Themes

class ClipboardView @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null
) : View(ctx, attrs) {

    var onItemSelected: ((String) -> Unit)? = null
    var onItemDeleted: ((String) -> Unit)? = null
    var onClose: (() -> Unit)? = null
    var clipboard = ClipboardHistory(ctx)

    private val theme = Themes.Amoled
    private val density = resources.displayMetrics.density
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.LEFT
        textSize = 14f * density
        color = theme.keyText
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 12f * density
        color = theme.keyText.and(0x99FFFFFF.toInt())
    }
    private val deleteIcon = AppCompatResources.getDrawable(ctx, R.drawable.ic_close)?.mutate()?.apply {
        setTint(theme.keyAccent)
    }
    private val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 14f * density
        color = theme.keyText
    }
    private val itemBackgroundPaint = Paint().apply {
        color = theme.keyBackground
        style = Paint.Style.FILL
    }
    private val actionBackgroundPaint = Paint().apply {
        color = theme.keyModifierBackground
        style = Paint.Style.FILL
    }

    private val itemBounds = mutableMapOf<String, Rect>()
    private val deleteBounds = mutableMapOf<String, Rect>()
    private val closeBounds = Rect()
    private val clearAllBounds = Rect()
    private val headerHeight = 52f * density
    private val rowHeight = 52f * density
    private val emptyHeight = 72f * density

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val items = clipboard.items()
        val contentHeight = if (items.isEmpty()) emptyHeight else items.size * rowHeight
        val h = (headerHeight + contentHeight).toInt()
        setMeasuredDimension(w, h)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(theme.background)
        val items = clipboard.items()
        val w = width.toFloat()
        val deleteWidth = 50f * density
        val closeWidth = 84f * density
        val clearWidth = 112f * density

        itemBounds.clear()
        deleteBounds.clear()
        closeBounds.set(0, 0, closeWidth.toInt(), headerHeight.toInt())
        clearAllBounds.set((w - clearWidth).toInt(), 0, w.toInt(), headerHeight.toInt())
        canvas.drawRect(0f, 0f, w, headerHeight, actionBackgroundPaint)
        canvas.drawText("ABC", closeWidth / 2f, headerHeight * 0.64f, headerPaint)
        headerPaint.alpha = if (items.isEmpty()) 100 else 255
        canvas.drawText("Clear all", w - clearWidth / 2f, headerHeight * 0.64f, headerPaint)
        headerPaint.alpha = 255

        if (items.isEmpty()) {
            canvas.drawText("Clipboard is empty", w / 2f, headerHeight + emptyHeight * 0.55f, labelPaint)
            return
        }

        var y = headerHeight
        for (item in items) {
            val preview = item.take(48).replace("\n", " ")
            val itemRect = Rect(0, y.toInt(), (w - deleteWidth).toInt(), (y + rowHeight).toInt())
            itemBounds[item] = itemRect

            val deleteRect = Rect((w - deleteWidth).toInt(), y.toInt(), w.toInt(), (y + rowHeight).toInt())
            deleteBounds[item] = deleteRect

            canvas.drawRect(itemRect.left.toFloat(), itemRect.top.toFloat(),
                itemRect.right.toFloat(), itemRect.bottom.toFloat(),
                itemBackgroundPaint)
            canvas.drawRect(deleteRect.left.toFloat(), deleteRect.top.toFloat(),
                deleteRect.right.toFloat(), deleteRect.bottom.toFloat(),
                actionBackgroundPaint)

            canvas.drawText(preview, 12f * density, y + rowHeight * 0.65f, textPaint)
            val iconSize = (20f * density).toInt()
            val iconCenterX = (w - deleteWidth / 2f).toInt()
            val iconCenterY = (y + rowHeight / 2f).toInt()
            deleteIcon?.setBounds(
                iconCenterX - iconSize / 2,
                iconCenterY - iconSize / 2,
                iconCenterX + iconSize / 2,
                iconCenterY + iconSize / 2,
            )
            deleteIcon?.draw(canvas)

            y += rowHeight
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) return true
        if (event.action == MotionEvent.ACTION_UP) {
            performClick()
            if (closeBounds.contains(event.x.toInt(), event.y.toInt())) {
                onClose?.invoke()
                return true
            }
            if (clearAllBounds.contains(event.x.toInt(), event.y.toInt()) && clipboard.items().isNotEmpty()) {
                clipboard.clear()
                refresh()
                return true
            }
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
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    fun refresh() {
        requestLayout()
        invalidate()
    }
}
