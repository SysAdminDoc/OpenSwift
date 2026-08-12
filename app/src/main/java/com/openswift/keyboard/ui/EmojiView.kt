package com.openswift.keyboard.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.openswift.keyboard.data.SecurePreferences
import com.openswift.keyboard.data.TypedDataStores
import com.openswift.keyboard.theme.Themes
import kotlin.math.max

class EmojiView @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null
) : View(ctx, attrs) {

    var onEmojiSelected: ((String) -> Unit)? = null

    private val prefs = SecurePreferences.open(ctx, TypedDataStores.EMOJI_PICKER)
    private val theme = Themes.Amoled
    private val density = resources.displayMetrics.density
    private val categoryBounds = mutableMapOf<String, RectF>()
    private val emojiBounds = mutableMapOf<String, Rect>()
    private val keyboardBounds = mutableMapOf<String, RectF>()
    private val actionBounds = mutableMapOf<String, RectF>()
    private val recents = loadList("recents").toMutableList()
    private val favorites = loadList("favorites").toMutableList()
    private val searchRows = listOf("qwertyuiop", "asdfghjkl", "zxcvbnm")

    private var selectedCategory = if (recents.isEmpty()) "Smile" else EmojiCatalog.RECENTS
    private var query = ""
    private var searchActive = false
    private var scrollOffset = 0f
    private var lastY = 0f
    private var downEmoji: String? = null
    private var downTime = 0L
    private var moved = false

    private val backgroundPaint = Paint().apply { color = theme.background }
    private val panelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = theme.keyBackground }
    private val selectedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = theme.keyAccent }
    private val mutedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = theme.keyModifierBackground }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = theme.keyText
        textAlign = Paint.Align.CENTER
    }
    private val subtleTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = theme.suggestionText
        textAlign = Paint.Align.CENTER
    }
    private val starPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = theme.keyAccent
        textAlign = Paint.Align.CENTER
    }
    private val emojiPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val desiredHeight = (360f * density).toInt()
        val height = resolveSize(desiredHeight, heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(theme.background)
        val categoryHeight = 42f * density
        val searchHeight = 44f * density
        val keyboardHeight = if (searchActive) 112f * density else 0f
        val gridTop = categoryHeight + searchHeight + keyboardHeight

        drawCategories(canvas, categoryHeight)
        drawSearch(canvas, categoryHeight, searchHeight)
        if (searchActive) {
            drawSearchKeyboard(canvas, categoryHeight + searchHeight)
        }
        drawEmojiGrid(canvas, gridTop)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastY = event.y
                moved = false
                downEmoji = findEmoji(event.x, event.y)
                downTime = System.currentTimeMillis()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dy = event.y - lastY
                if (kotlin.math.abs(dy) > 4f * density) {
                    moved = true
                    scrollOffset = (scrollOffset - dy).coerceIn(0f, maxScroll())
                    lastY = event.y
                    invalidate()
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (!moved && handleTap(event.x, event.y)) return true
                downEmoji = null
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun drawCategories(canvas: Canvas, height: Float) {
        categoryBounds.clear()
        val tabWidth = width.toFloat() / EmojiCatalog.categories.size
        textPaint.textSize = 10f * density
        EmojiCatalog.categories.forEachIndexed { index, category ->
            val left = index * tabWidth
            val rect = RectF(left + 2f, 4f, left + tabWidth - 2f, height - 4f)
            categoryBounds[category] = rect
            val selected = category == selectedCategory && query.isBlank()
            canvas.drawRoundRect(rect, 8f * density, 8f * density, if (selected) selectedPaint else mutedPaint)
            canvas.drawText(category, rect.centerX(), rect.centerY() + 4f * density, textPaint)
        }
    }

    private fun drawSearch(canvas: Canvas, top: Float, height: Float) {
        actionBounds.clear()
        val margin = 8f * density
        val field = RectF(margin, top + 5f * density, width - (92f * density), top + height - 5f * density)
        val backspace = RectF(width - (84f * density), field.top, width - (48f * density), field.bottom)
        val clear = RectF(width - (42f * density), field.top, width - margin, field.bottom)
        actionBounds["search"] = field
        actionBounds["backspace"] = backspace
        actionBounds["clear"] = clear

        canvas.drawRoundRect(field, 8f * density, 8f * density, panelPaint)
        canvas.drawRoundRect(backspace, 8f * density, 8f * density, mutedPaint)
        canvas.drawRoundRect(clear, 8f * density, 8f * density, mutedPaint)

        subtleTextPaint.textSize = 14f * density
        val label = if (query.isBlank()) "Search emoji" else query
        canvas.drawText(label, field.centerX(), field.centerY() + 5f * density, subtleTextPaint)
        canvas.drawText("Del", backspace.centerX(), backspace.centerY() + 5f * density, textPaint)
        canvas.drawText("X", clear.centerX(), clear.centerY() + 5f * density, textPaint)
    }

    private fun drawSearchKeyboard(canvas: Canvas, top: Float) {
        keyboardBounds.clear()
        textPaint.textSize = 15f * density
        var y = top + 6f * density
        searchRows.forEach { row ->
            val keyWidth = width.toFloat() / row.length
            row.forEachIndexed { index, char ->
                val left = index * keyWidth
                val rect = RectF(left + 2f, y, left + keyWidth - 2f, y + 30f * density)
                keyboardBounds[char.toString()] = rect
                canvas.drawRoundRect(rect, 6f * density, 6f * density, panelPaint)
                canvas.drawText(char.toString(), rect.centerX(), rect.centerY() + 5f * density, textPaint)
            }
            y += 34f * density
        }
    }

    private fun drawEmojiGrid(canvas: Canvas, top: Float) {
        emojiBounds.clear()
        val results = currentEntries()
        val columns = 8
        val cell = width.toFloat() / columns
        val visibleBottom = height.toFloat()
        emojiPaint.textSize = 30f * density
        textPaint.textSize = 13f * density
        starPaint.textSize = 13f * density

        if (results.isEmpty()) {
            canvas.drawText("No emoji", width / 2f, top + 44f * density, subtleTextPaint)
            return
        }

        results.forEachIndexed { index, entry ->
            val row = index / columns
            val col = index % columns
            val x = col * cell
            val y = top + row * cell - scrollOffset
            if (y > visibleBottom || y + cell < top) return@forEachIndexed
            val rect = Rect(x.toInt(), y.toInt(), (x + cell).toInt(), (y + cell).toInt())
            emojiBounds[entry.value] = rect
            canvas.drawText(entry.value, x + cell / 2, y + cell * 0.68f, emojiPaint)
            if (favorites.contains(entry.value)) {
                canvas.drawText("*", x + cell - 10f * density, y + 14f * density, starPaint)
            }
        }
    }

    private fun handleTap(x: Float, y: Float): Boolean {
        categoryBounds.entries.firstOrNull { it.value.contains(x, y) }?.let {
            selectedCategory = it.key
            query = ""
            searchActive = false
            scrollOffset = 0f
            invalidate()
            return true
        }
        actionBounds["search"]?.takeIf { it.contains(x, y) }?.let {
            searchActive = true
            invalidate()
            return true
        }
        actionBounds["backspace"]?.takeIf { it.contains(x, y) }?.let {
            if (query.isNotEmpty()) query = query.dropLast(1)
            searchActive = true
            scrollOffset = 0f
            invalidate()
            return true
        }
        actionBounds["clear"]?.takeIf { it.contains(x, y) }?.let {
            query = ""
            searchActive = false
            scrollOffset = 0f
            invalidate()
            return true
        }
        keyboardBounds.entries.firstOrNull { it.value.contains(x, y) }?.let {
            query += it.key
            selectedCategory = EmojiCatalog.RECENTS
            scrollOffset = 0f
            invalidate()
            return true
        }
        val emoji = findEmoji(x, y) ?: return false
        if (emoji == downEmoji && System.currentTimeMillis() - downTime >= LONG_PRESS_MS) {
            toggleFavorite(emoji)
        } else {
            addRecent(emoji)
            onEmojiSelected?.invoke(emoji)
        }
        downEmoji = null
        invalidate()
        return true
    }

    private fun findEmoji(x: Float, y: Float): String? {
        return emojiBounds.entries.firstOrNull { it.value.contains(x.toInt(), y.toInt()) }?.key
    }

    private fun currentEntries(): List<EmojiEntry> {
        if (query.isNotBlank()) return EmojiCatalog.search(query)
        return when (selectedCategory) {
            EmojiCatalog.RECENTS -> recents.mapNotNull { EmojiCatalog.byValue[it] }
            EmojiCatalog.FAVORITES -> favorites.mapNotNull { EmojiCatalog.byValue[it] }
            else -> EmojiCatalog.entries.filter { it.category == selectedCategory }
        }
    }

    private fun maxScroll(): Float {
        val columns = 8
        val rows = (currentEntries().size + columns - 1) / columns
        val keyboardHeight = if (searchActive) 112f * density else 0f
        val gridTop = 42f * density + 44f * density + keyboardHeight
        val contentHeight = rows * (width.toFloat() / columns)
        return max(0f, contentHeight - (height - gridTop))
    }

    private fun addRecent(emoji: String) {
        recents.remove(emoji)
        recents.add(0, emoji)
        while (recents.size > MAX_STORED) recents.removeAt(recents.lastIndex)
        saveList("recents", recents)
    }

    private fun toggleFavorite(emoji: String) {
        if (favorites.remove(emoji)) {
            saveList("favorites", favorites)
            return
        }
        favorites.add(0, emoji)
        while (favorites.size > MAX_STORED) favorites.removeAt(favorites.lastIndex)
        saveList("favorites", favorites)
    }

    private fun loadList(key: String): List<String> {
        return prefs.getString(key, "").orEmpty().split(SEPARATOR).filter { it.isNotBlank() }
    }

    private fun saveList(key: String, values: List<String>) {
        prefs.edit().putString(key, values.joinToString(SEPARATOR)).apply()
    }

    companion object {
        private const val LONG_PRESS_MS = 450L
        private const val MAX_STORED = 24
        private const val SEPARATOR = "\u001F"
    }
}
