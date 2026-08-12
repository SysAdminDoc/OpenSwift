package com.openswift.keyboard.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.openswift.keyboard.data.Settings
import com.openswift.keyboard.engine.UserDictionary
import com.openswift.keyboard.engine.GlideDecoder
import com.openswift.keyboard.engine.WordList
import com.openswift.keyboard.layout.Key
import com.openswift.keyboard.layout.KeyLayout
import com.openswift.keyboard.layout.KeyCode as KC
import com.openswift.keyboard.theme.Themes
import kotlin.math.ceil

class KeyboardView(
    ctx: Context,
    private val settings: Settings,
    private var wordList: WordList,
    private var userDict: UserDictionary,
    private var keyLayout: KeyLayout,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(ctx, attrs, defStyle) {

    private var onKeyListener: ((Int, String) -> Unit)? = null
    private var onGlideListener: ((String) -> Unit)? = null
    private var suggestions: List<String> = emptyList()
    private var shiftActive = false
    private var predictionEnabled = true
    private var glideEnabled = settings.glideEnabled
    private var keyHeightDp = settings.keyHeightDp
    private var glideDecoder = GlideDecoder(wordList, userDict)
    
    // Compute layout with number row if enabled
    private var effectiveLayout: KeyLayout = if (settings.numberRow) {
        // Prepend number row to the layout
        val numberRow = (1..10).map { i ->
            val ch = if (i == 10) '0' else (i + 48).toChar()
            Key(ch.toString(), ch.code)
        }
        KeyLayout(
            keyLayout.id + "_with_numbers",
            listOf(numberRow) + keyLayout.rows
        )
    } else {
        keyLayout
    }

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
        textSize = 24f
        color = theme.keyText
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
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
        val density = resources.displayMetrics.density
        
        // Mirror the drawing path exactly so the final row is never clipped.
        val keyHeightPx = keyHeightDp * density
        val numRows = effectiveLayout.rows.size // includes number row if enabled
        val rowSpacingPx = 2f * density
        val suggestionHeight = if (predictionEnabled) {
            (4f * density) + (keyHeightPx * 1.2f) + (4f * density)
        } else {
            0f
        }
        val rowsHeight = (keyHeightPx * numRows) +
            (rowSpacingPx * (numRows - 1).coerceAtLeast(0))
        val h = ceil(suggestionHeight + rowsHeight).toInt()
        
        setMeasuredDimension(w, h)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(theme.background)

        val w = width.toFloat()
        val h = height.toFloat()
        val density = resources.displayMetrics.density
        
        // Calculate key height in pixels
        val keyHeightPx = keyHeightDp * density
        val suggestionHeightPx = keyHeightPx * 1.2f
        val rowSpacingPx = 2f * density // small gap between rows
        
        // Update paint text sizes based on key height - MUCH LARGER for readability
        textPaint.textSize = (keyHeightPx * 0.60f) // 60% of key height - significantly larger
        suggestionPaint.textSize = (keyHeightPx * 0.40f)
        previewPaint.textSize = (keyHeightPx * 0.18f)
        
        // Draw suggestions row as pills with preview
        var y = 0f
        suggestionBounds.clear()

        if (predictionEnabled) {
            y = 4f * density // small top padding
            canvas.drawRect(0f, 0f, w, y + suggestionHeightPx, suggestionBgPaint)
            var x = 12f * density // responsive padding

            for (sugg in suggestions.take(3)) {
                val suggWidth = suggestionPaint.measureText(sugg) + (16f * density)
                val pillHeight = suggestionHeightPx - (8f * density)
                val pillRadius = pillHeight / 2f
                val pillY = y + (4f * density)

                // Prevent overflow: stop adding suggestions if they'd go off-screen
                if (x + suggWidth > w) break

                val rect = Rect(x.toInt(), pillY.toInt(), (x + suggWidth).toInt(), (pillY + suggestionHeightPx).toInt())
                suggestionBounds[sugg] = rect

                // Draw pill background (rounded rectangle)
                canvas.drawRoundRect(
                    x, pillY, x + suggWidth, pillY + pillHeight,
                    pillRadius, pillRadius,
                    pillPaint
                )

                // Draw pill border
                canvas.drawRoundRect(
                    x, pillY, x + suggWidth, pillY + pillHeight,
                    pillRadius, pillRadius,
                    pillBorderPaint
                )

                // Draw main suggestion text (centered vertically in pill)
                val textY = pillY + (pillHeight / 2) + (4f * density)
                canvas.drawText(sugg, x + suggWidth / 2, textY, suggestionPaint)

                x += suggWidth + (8f * density)
            }

            y += suggestionHeightPx + (4f * density)
        }

        // Draw keyboard rows
        keyBounds.clear()
        val keyPadding = 1.5f * density
        for (row in effectiveLayout.rows) {
            val totalWeight = row.sumOf { it.widthWeight.toDouble() }
            var x2 = 0f
            for (key in row) {
                val kw = (w.toDouble() / totalWeight) * key.widthWeight.toDouble()
                // Store bounds with padding applied (actual tappable area)
                val rect = Rect(
                    (x2 + keyPadding).toInt(), 
                    (y + keyPadding).toInt(), 
                    (x2 + kw.toFloat() - keyPadding).toInt(), 
                    (y + keyHeightPx - keyPadding).toInt()
                )
                keyBounds[key] = rect

                val bgColor = if (key.isModifier) theme.keyModifierBackground else theme.keyBackground
                val bgPaint = if (key.isModifier) keyModifierBgPaint else keyBgPaint
                bgPaint.color = bgColor
                
                // Draw key background with slight padding for spacing
                canvas.drawRect(
                    x2 + keyPadding, y + keyPadding, x2 + kw.toFloat() - keyPadding, y + keyHeightPx - keyPadding,
                    bgPaint
                )
                
                // Draw key outline (subtle border)
                keyOutline.color = theme.keyAccent
                keyOutline.alpha = (0.2f * 255).toInt()
                canvas.drawRect(
                    x2 + keyPadding, y + keyPadding, x2 + kw.toFloat() - keyPadding, y + keyHeightPx - keyPadding,
                    keyOutline
                )
                
                if (shiftActive && key.code == KC.SHIFT) {
                    canvas.drawRect(
                        x2 + 2, y + 2, x2 + kw.toFloat() - 2, y + keyHeightPx - 2,
                        shiftHighlightPaint
                    )
                }
                
                // Draw key text (centered both horizontally and vertically)
                val textX = x2 + kw.toFloat() / 2
                val textY = y + (keyHeightPx / 2) + (6f * density) // baseline adjustment
                
                // Convert to uppercase if shift is active and key is a letter
                val displayLabel = if (shiftActive && key.label.length == 1 && key.label[0].isLetter()) {
                    key.label.uppercase()
                } else {
                    key.label
                }
                
                val defaultTextSize = textPaint.textSize
                val availableTextWidth = (kw.toFloat() - (keyPadding * 2) - (8f * density)).coerceAtLeast(1f)
                val labelWidth = textPaint.measureText(displayLabel)
                if (labelWidth > availableTextWidth) {
                    textPaint.textSize = defaultTextSize * (availableTextWidth / labelWidth)
                }
                canvas.drawText(displayLabel, textX, textY, textPaint)
                textPaint.textSize = defaultTextSize
                x2 += kw.toFloat()
            }
            y += keyHeightPx + rowSpacingPx
        }

        // Draw glide trail with fade gradient (skip if reduced motion enabled)
        if (!settings.reducedMotion && glideTrail.isNotEmpty()) {
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

        // Draw ripples (skip if reduced motion enabled)
        if (!settings.reducedMotion) {
            val now = System.currentTimeMillis()
            val expiredIndices = mutableListOf<Int>()
            for ((idx, ripple) in ripples.withIndex()) {
                val elapsed = now - ripple.startTime
                val progress = (elapsed.toFloat() / rippleAnimDuration).coerceIn(0f, 1f)
                
                if (progress >= 1f) {
                    expiredIndices.add(idx)
                    continue
                }
                
                val radius = (2f + (48f * progress)) * density
                val alpha = ((1f - progress) * 255).toInt()
                ripplePaint.color = theme.keyAccent
                ripplePaint.alpha = alpha
                canvas.drawCircle(ripple.x, ripple.y, radius, ripplePaint)
            }
            
            for (idx in expiredIndices.reversed()) {
                ripples.removeAt(idx)
            }
        } else {
            ripples.clear()
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
                if (!isGliding && elapsed > 80L && glideEnabled) {
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
                } else if (!isGliding && predictionEnabled) {
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
                // Use the actual label character, respecting shift state for letters
                var char = key.label[0]
                if (shiftActive && char.isLetter()) {
                    char = char.uppercaseChar()
                }
                return GlideDecoder.Sample(char, x, y)
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
        suggestions = if (predictionEnabled) sugg else emptyList()
        invalidate()
    }

    fun setPredictionEnabled(enabled: Boolean) {
        if (predictionEnabled == enabled) return
        predictionEnabled = enabled
        if (!enabled) {
            suggestions = emptyList()
            suggestionBounds.clear()
        }
        requestLayout()
        invalidate()
    }

    fun setInputProfile(predictionsEnabled: Boolean, glideEnabled: Boolean, keyHeightDp: Int) {
        setPredictionEnabled(predictionsEnabled)
        if (this.glideEnabled != glideEnabled) {
            this.glideEnabled = glideEnabled
            if (!glideEnabled) {
                isGliding = false
                glideSamples.clear()
                glideTrail.clear()
            }
        }
        if (this.keyHeightDp != keyHeightDp) {
            this.keyHeightDp = keyHeightDp
            requestLayout()
        }
        if (!predictionsEnabled && !glideEnabled) {
            isGliding = false
            glideSamples.clear()
            glideTrail.clear()
        }
        invalidate()
    }

    fun setShift(active: Boolean) {
        shiftActive = active
        invalidate()
    }

    fun updateLayout(layout: KeyLayout) {
        keyLayout = layout
        // Recompute effective layout with number row if enabled
        effectiveLayout = if (settings.numberRow) {
            val numberRow = (1..10).map { i ->
                val ch = if (i == 10) '0' else (i + 48).toChar()
                Key(ch.toString(), ch.code)
            }
            KeyLayout(
                layout.id + "_with_numbers",
                listOf(numberRow) + layout.rows
            )
        } else {
            layout
        }
        invalidate()
    }

    fun updateDictionary(wordList: WordList, userDict: UserDictionary) {
        this.wordList = wordList
        this.userDict = userDict
        glideDecoder = GlideDecoder(wordList, userDict)
    }
}
