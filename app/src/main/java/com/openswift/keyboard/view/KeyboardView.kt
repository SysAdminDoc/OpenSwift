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
    
    // Additional Paint objects (allocated once, reused per frame)
    private val suggestionBgPaint = Paint().apply { color = theme.suggestionBg }
    private val pillPaint = Paint().apply { 
        style = Paint.Style.FILL
        color = theme.keyBackground
    }
    private val pillBorderPaint = Paint().apply { 
        style = Paint.Style.STROKE
        strokeWidth = 2f
        color = theme.keyAccent
    }
    private val previewPaint = Paint().apply {
        textSize = 10f
        color = theme.suggestionText
        alpha = 180
    }
    private val trailPaint = Paint().apply {
        strokeWidth = 6f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        color = theme.gestureTrail
    }
    private val ripplePaint = Paint().apply {
        style = Paint.Style.FILL
        color = theme.keyAccent
    }
    private val keyModifierBgPaint = Paint().apply { color = theme.keyModifierBackground }
    private val keyBgPaint = Paint().apply { color = theme.keyBackground }
    private val shiftHighlightPaint = Paint().apply {
        color = theme.keyAccent
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }

    private var isGliding = false
    private var glideStartTime = 0L
    private val glideSamples = mutableListOf<GlideDecoder.Sample>()
    private val suggestionBounds = mutableMapOf<String, Rect>()
    
    // Ripple effect tracking
    private data class Ripple(val x: Float, val y: Float, val startTime: Long)
    private val ripples = mutableListOf<Ripple>()
    private val rippleAnimDuration = 400L
    private val maxRipples = 20 // Cap concurrent ripples to prevent memory bloat
    
    // Glide trail gradient
    private data class TrailPoint(val x: Float, val y: Float, val time: Long)
    private val glideTrail = mutableListOf<TrailPoint>()
    private val trailFadeMs = 300L

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

        // Draw suggestions row as pills with preview
        var y = keyH * 0.1f
        canvas.drawRect(0f, 0f, w, suggestionH, suggestionBgPaint)
        var x = 20f
        suggestionBounds.clear()
        
        for (sugg in suggestions.take(3)) {
            val suggWidth = suggestionPaint.measureText(sugg) + 20f
            val pillHeight = suggestionH - 8f
            val pillRadius = pillHeight / 2f
            
            val rect = Rect(x.toInt(), y.toInt(), (x + suggWidth).toInt(), (y + suggestionH).toInt())
            suggestionBounds[sugg] = rect
            
            // Draw pill background (rounded rectangle)
            canvas.drawRoundRect(
                x, y + 4f, x + suggWidth, y + pillHeight + 4f,
                pillRadius, pillRadius,
                pillPaint
            )
            
            // Draw pill border
            canvas.drawRoundRect(
                x, y + 4f, x + suggWidth, y + pillHeight + 4f,
                pillRadius, pillRadius,
                pillBorderPaint
            )
            
            // Draw main suggestion text
            canvas.drawText(sugg, x + suggWidth / 2, y + keyH * 0.55f, suggestionPaint)
            
            // Draw small preview text (first few chars of next word)
            if (sugg.length > 1) {
                val preview = sugg.substring(0, minOf(3, sugg.length))
                canvas.drawText(preview, x + suggWidth / 2, y + pillHeight, previewPaint)
            }
            
            x += suggWidth + 10f
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
                val bgPaint = if (key.isModifier) keyModifierBgPaint else keyBgPaint
                bgPaint.color = bgColor
                canvas.drawRect(
                    x2, y, x2 + kw.toFloat(), y + keyH,
                    bgPaint
                )
                if (shiftActive && key.code == KC.SHIFT) {
                    canvas.drawRect(
                        x2 + 2, y + 2, x2 + kw.toFloat() - 2, y + keyH - 2,
                        shiftHighlightPaint
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

        // Draw glide trail with fade gradient
        if (glideTrail.isNotEmpty()) {
            val now = System.currentTimeMillis()
            for (i in 0 until glideTrail.size - 1) {
                val p1 = glideTrail[i]
                val p2 = glideTrail[i + 1]
                val age = (now - p1.time).toFloat().coerceAtLeast(0f)
                val progress = (age / trailFadeMs).coerceIn(0f, 1f)
                val alpha = ((1f - progress) * 255).toInt()
                trailPaint.color = theme.gestureTrail
                trailPaint.alpha = alpha
                canvas.drawLine(p1.x, p1.y, p2.x, p2.y, trailPaint)
            }
        }

        // Draw ripples
        val now = System.currentTimeMillis()
        val expiredIndices = mutableListOf<Int>()
        for ((idx, ripple) in ripples.withIndex()) {
            val elapsed = now - ripple.startTime
            val progress = (elapsed.toFloat() / rippleAnimDuration).coerceIn(0f, 1f)
            
            if (progress >= 1f) {
                expiredIndices.add(idx)
                continue
            }
            
            val radius = 2f + (48f * progress * resources.displayMetrics.density)
            val alpha = ((1f - progress) * 255).toInt()
            ripplePaint.color = theme.keyAccent
            ripplePaint.alpha = alpha
            canvas.drawCircle(ripple.x, ripple.y, radius, ripplePaint)
        }
        
        for (idx in expiredIndices.reversed()) {
            ripples.removeAt(idx)
        }
        
        if (ripples.isNotEmpty() || glideTrail.isNotEmpty()) {
            postInvalidateOnAnimation()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                glideStartTime = System.currentTimeMillis()
                isGliding = false
                glideSamples.clear()
                glideTrail.clear()
                
                // Add ripple on tap (cap to maxRipples to prevent memory bloat)
                if (ripples.size < maxRipples) {
                    ripples.add(Ripple(event.x, event.y, glideStartTime))
                    postInvalidateOnAnimation()
                }
                
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
                    // Append trail point for gradient fade
                    glideTrail.add(TrailPoint(event.x, event.y, System.currentTimeMillis()))
                    postInvalidateOnAnimation()
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
                // Trail will fade naturally via alpha in onDraw
                postInvalidateOnAnimation()
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
