package com.openswift.keyboard.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.openswift.keyboard.theme.Themes

class EmojiView @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null
) : View(ctx, attrs) {

    var onEmojiSelected: ((String) -> Unit)? = null
    private val theme = Themes.Amoled
    private val emojis = listOf(
        "😀", "😁", "😂", "🤣", "😃", "😄", "😅", "😆", "😉", "😊",
        "😇", "🙂", "🙃", "😌", "😍", "🥰", "😘", "😗", "😚", "😙",
        "😋", "😛", "😜", "🤪", "😝", "😑", "😐", "😶", "😏", "😒",
        "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍", "🤎", "❤️‍🔥",
        "👍", "👎", "👌", "✌️", "🤞", "🤟", "🤘", "🤙", "🤚", "🤛",
        "🎉", "🎊", "🎈", "🎁", "🎀", "🎂", "🍰", "🧁", "🍪", "🍩"
    )

    private val emojiSize = 48f
    private val cols = 10
    private val emojiBounds = mutableMapOf<String, Rect>()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val rows = (emojis.size + cols - 1) / cols
        val h = (rows * 56 * resources.displayMetrics.density).toInt()
        setMeasuredDimension(w, h)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(theme.background)
        val w = width.toFloat()
        val cellSize = w / cols

        emojiBounds.clear()
        var idx = 0
        for (row in 0 until (emojis.size + cols - 1) / cols) {
            for (col in 0 until cols) {
                if (idx >= emojis.size) break
                val emoji = emojis[idx]
                val x = col * cellSize
                val y = row * cellSize
                val rect = Rect(x.toInt(), y.toInt(), (x + cellSize).toInt(), (y + cellSize).toInt())
                emojiBounds[emoji] = rect

                val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    textAlign = Paint.Align.CENTER
                    textSize = emojiSize
                }
                canvas.drawText(emoji, x + cellSize / 2, y + cellSize * 0.7f, paint)
                idx++
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            for ((emoji, rect) in emojiBounds) {
                if (rect.contains(event.x.toInt(), event.y.toInt())) {
                    onEmojiSelected?.invoke(emoji)
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }
}
