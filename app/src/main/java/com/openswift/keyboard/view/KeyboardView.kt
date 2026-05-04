package com.openswift.keyboard.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.openswift.keyboard.data.Settings
import com.openswift.keyboard.engine.Predictor
import com.openswift.keyboard.engine.UserDictionary
import com.openswift.keyboard.engine.GlideDecoder
import com.openswift.keyboard.layout.Key
import com.openswift.keyboard.layout.KeyLayout
import com.openswift.keyboard.layout.KeyCode as KC
import com.openswift.keyboard.theme.Themes

class KeyboardView(
    ctx: Context,
    private val settings: Settings,
    private val predictor: Predictor,
    private val userDict: UserDictionary,
    private var keyLayout: KeyLayout,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(ctx, attrs, defStyle) {

    private var onKeyListener: ((Int, String) -> Unit)? = null
    private var onGlideListener: ((String) -> Unit)? = null
    private var suggestions: List<String> = emptyList()
    private var shiftActive = false
    private val wordList = com.openswift.keyboard.engine.WordList(ctx)
    private val glideDecoder = GlideDecoder(wordList, userDict)

    private val theme = Themes.byId(settings.theme)
    private val keyBounds = mutableMapOf<Key, Rect>()
    private val keyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 18f
    }
    private val keyOutline = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1f
        color = theme.keyAccent
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 18f
        color = theme.keyText
    }
    private val suggestionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 14f
        color = theme.suggestionText
    }

    private var isGliding = false
    private var glideStartTime = 0L
    private val glideSamples = mutableListOf<GlideDecoder.Sample>()
    private val suggestionBounds = mutableMapOf<String, Rect>()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = (settings.keyHeightDp * 4.5f * resources.displayMetrics.density).toInt()
        setMeasuredDimension(w, h)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(theme.background)

        val w = width.toFloat()
        val h = height.toFloat()
        val keyH = (h * 0.22f)
        val suggestionH = keyH * 1.2f

        // Draw suggestions row
        var y = keyH * 0.1f
        canvas.drawRect(0f, 0f, w, suggestionH, Paint().apply { color = theme.suggestionBg })
        var x = 20f
        suggestionBounds.clear()
        for (sugg in suggestions.take(3)) {
            val w2 = suggestionPaint.measureText(sugg) + 20f
            val rect = Rect(x.toInt(), y.toInt(), (x + w2).toInt(), (y + suggestionH).toInt())
            suggestionBounds[sugg] = rect
            canvas.drawRect(x, y + 4f, x + w2, y + suggestionH - 4f, 
                Paint().apply { color = theme.keyBackground; style = Paint.Style.STROKE; strokeWidth = 2f })
            canvas.drawText(sugg, x + w2 / 2, y + keyH * 0.6f, suggestionPaint)
            x += w2 + 10f
        }

        // Draw keyboard rows
        y = suggestionH + keyH * 0.1f
        keyBounds.clear()
        for (row in keyLayout.rows) {
            val totalWeight = row.sumOf { it.widthWeight.toDouble() }
            var x2 = 0f
            for (key in row) {
                val kw = (w.toDouble() / totalWeight) * key.widthWeight.toDouble()
                val rect = Rect(x2.toInt(), y.toInt(), (x2 + kw).toInt(), (y + keyH).toInt())
                keyBounds[key] = rect

                val bgColor = if (key.isModifier) theme.keyModifierBackground else theme.keyBackground
                canvas.drawRect(
                    x2, y, x2 + kw.toFloat(), y + keyH,
                    Paint().apply { color = bgColor }
                )
                if (shiftActive && key.code == KC.SHIFT) {
                    canvas.drawRect(
                        x2 + 2, y + 2, x2 + kw.toFloat() - 2, y + keyH - 2,
                        Paint().apply { color = theme.keyAccent; style = Paint.Style.STROKE; strokeWidth = 3f }
                    )
                }
                canvas.drawText(
                    key.label, x2 + kw.toFloat() / 2, y + keyH * 0.6f,
                    textPaint
                )
                x2 += kw.toFloat()
            }
            y += keyH
        }

        // Draw glide trail
        if (glideSamples.size > 1) {
            val trail = Paint().apply {
                color = theme.gestureTrail
                strokeWidth = 6f
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
            }
            for (i in 0 until glideSamples.size - 1) {
                val s1 = glideSamples[i]
                val s2 = glideSamples[i + 1]
                canvas.drawLine(s1.x, s1.y, s2.x, s2.y, trail)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                glideStartTime = System.currentTimeMillis()
                isGliding = false
                glideSamples.clear()
                val sample = sampleAt(event.x, event.y)
                if (sample.char != ' ') glideSamples.add(sample)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val elapsed = System.currentTimeMillis() - glideStartTime
                if (!isGliding && elapsed > 80L && settings.glideEnabled) {
                    isGliding = true
                }
                if (isGliding) {
                    val sample = sampleAt(event.x, event.y)
                    if (sample.char != ' ' && (glideSamples.isEmpty() || sample.char != glideSamples.last().char)) {
                        glideSamples.add(sample)
                    }
                    invalidate()
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (isGliding && glideSamples.size >= 2) {
                    val decoded = glideDecoder.decode(glideSamples)
                    if (decoded.isNotEmpty()) {
                        onGlideListener?.invoke(decoded.first())
                    }
                } else if (!isGliding && glideSamples.isNotEmpty()) {
                    // Tap on a single key
                    val key = findKeyAt(event.x, event.y)
                    if (key != null) {
                        onKeyListener?.invoke(key.code, key.label)
                    }
                } else if (!isGliding) {
                    // Check if suggestion was tapped
                    for ((sugg, rect) in suggestionBounds) {
                        if (rect.contains(event.x.toInt(), event.y.toInt())) {
                            onGlideListener?.invoke(sugg)
                            break
                        }
                    }
                }
                isGliding = false
                glideSamples.clear()
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun findKeyAt(x: Float, y: Float): Key? {
        for ((key, rect) in keyBounds) {
            if (rect.contains(x.toInt(), y.toInt())) {
                return key
            }
        }
        return null
    }

    private fun sampleAt(x: Float, y: Float): GlideDecoder.Sample {
        for ((key, rect) in keyBounds) {
            if (rect.contains(x.toInt(), y.toInt())) {
                return GlideDecoder.Sample(key.label[0], x, y)
            }
        }
        return GlideDecoder.Sample(' ', x, y)
    }

    fun setOnKeyListener(listener: (Int, String) -> Unit) {
        onKeyListener = listener
    }

    fun setOnGlideListener(listener: (String) -> Unit) {
        onGlideListener = listener
    }

    fun setSuggestions(sugg: List<String>) {
        suggestions = sugg
        invalidate()
    }

    fun setShift(active: Boolean) {
        shiftActive = active
        invalidate()
    }

    fun updateLayout(layout: KeyLayout) {
        keyLayout = layout
        invalidate()
    }
}
