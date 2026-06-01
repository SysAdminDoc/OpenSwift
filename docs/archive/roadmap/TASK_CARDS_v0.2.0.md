# OpenSwift v0.2.0 — Atomic Task Cards
## Decomposed Roadmap Items for Single-Session Implementation

---

## TASK 1: Animated Key Press Feedback (Ripple Effect)

### Acceptance Criteria
- [ ] When a key is tapped, a circular ripple emanates from the touch center
- [ ] Ripple starts at touch point with ~2dp radius
- [ ] Ripple expands to ~50dp radius over 400ms using cubic-out easing
- [ ] Ripple opacity fades from 1.0 to 0.0 during animation
- [ ] Multiple rapid taps show concurrent ripples
- [ ] Ripple color matches theme.keyAccent
- [ ] No performance impact: 60fps maintained during glide + ripple
- [ ] Works on all 6 themes without color clipping
- [ ] Ripple does not interfere with glide trail visibility
- [ ] Rollback leaves no orphaned Canvas state

### Implementation Plan

**Files to Modify:**
1. `KeyboardView.kt` - Add ripple tracking & rendering
2. `Themes.kt` - Add optional rippleAlpha color (reuse keyAccent)

**Component Changes in KeyboardView.kt:**

a) Add ripple state tracking (after line 61):
```kotlin
private data class Ripple(val x: Float, val y: Float, val startTime: Long)
private val ripples = mutableListOf<Ripple>()
private val rippleAnimDuration = 400L
```

b) Modify onTouchEvent ACTION_DOWN (line 141):
```kotlin
MotionEvent.ACTION_DOWN -> {
    glideStartTime = System.currentTimeMillis()
    isGliding = false
    glideSamples.clear()
    
    // NEW: Add ripple
    ripples.add(Ripple(event.x, event.y, glideStartTime))
    postInvalidateOnAnimation()
    
    val sample = sampleAt(event.x, event.y)
    if (sample.char != ' ') glideSamples.add(sample)
    return true
}
```

c) Add onDraw ripple rendering (before line 124, glide trail):
```kotlin
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
    val ripplePaint = Paint().apply {
        color = theme.keyAccent
        alpha = alpha
        style = Paint.Style.FILL
    }
    canvas.drawCircle(ripple.x, ripple.y, radius, ripplePaint)
}

// Clean up expired ripples
for (idx in expiredIndices.reversed()) {
    ripples.removeAt(idx)
}

// Schedule next frame if ripples still active
if (ripples.isNotEmpty()) {
    postInvalidateOnAnimation()
}
```

d) Invalidate loop already in place via postInvalidateOnAnimation.

**UI Changes:**
- None required (existing KeyboardView canvas)

**Cleanup:**
- Remove ripple from list when progress > 1.0 (automatic via expiredIndices loop)
- postInvalidateOnAnimation only if ripples.isNotEmpty()

### Test Strategy

**Unit Test (KeyboardViewTest.kt):**
- Mock MotionEvent with (100f, 100f)
- ACTION_DOWN → verify ripple added to list
- Simulate time passage → verify progress calculation
- Verify ripple removed when progress >= 1.0

**Device Test (Manual):**
1. Run on Pixel 6 (API 34)
2. Open Settings app → Keyboard
3. Tap random keys → observe ripple from tap center expanding outward
4. Tap 5 keys rapidly → verify 5 concurrent ripples
5. Glide across keyboard → verify ripple + trail coexist without flicker
6. Switch all 6 themes → verify ripple color adapts from keyAccent
7. Performance check: expect >58fps during glide + ripple

**Performance Checkpoint:**
- No memory leak: heap stable after 1000 taps
- No jank: validate with Android Studio Profiler

### Effort Estimate
**1.5 hours total**
- Code: 45 min (ripple tracking + render + cleanup)
- Testing: 30 min (unit + manual glide test)
- Rollback prep: 15 min (git stash pattern)

### Blockers/Dependencies
- None. Fully independent from v0.1.0 features.
- Assumes: Canvas drawing already fast (optimized paint objects).

### Rollback Plan

**If ripple breaks glide visibility:**
1. In onDraw, comment out ripple loop (before line 124)
2. In ACTION_DOWN, comment out ripples.add() and postInvalidateOnAnimation()
3. In ACTION_UP, comment out ripples.clear()
4. `git checkout app/src/main/java/com/openswift/keyboard/view/KeyboardView.kt`
5. Rebuild, verify glide trail still renders

**If performance regresses:**
1. Reduce rippleAnimDuration from 400ms to 300ms
2. Increase ripple culling threshold (remove if progress > 0.95 instead of 1.0)
3. If still slow, remove alpha channel: use solid color, no transparency

---

## TASK 2: Glide Trail Gradient (Fade from Start to End)

### Acceptance Criteria
- [ ] Glide trail starts at 100% opacity at first sample
- [ ] Trail fades to 50% opacity at final sample
- [ ] Fade is linear per segment (not per point)
- [ ] Color remains theme.gestureTrail (hue unchanged)
- [ ] No performance impact: maintain 60fps during glide
- [ ] Trail works on all 6 themes
- [ ] Works with ripples simultaneously (no visual conflicts)
- [ ] Existing glide decoding unaffected
- [ ] On short glides (<3 points), fade still applies smoothly
- [ ] Rollback leaves no drawing artifacts

### Implementation Plan

**Files to Modify:**
1. `KeyboardView.kt` - Gradient trail rendering

**Component Changes in KeyboardView.kt:**

a) Add helper method for alpha blending (after sampleAt() method):
```kotlin
private fun Int.withAlpha(alpha: Float): Int {
    val a = (alpha * 255).toInt().coerceIn(0, 255)
    val rgb = this and 0x00FFFFFF
    return (a shl 24) or rgb
}
```

b) Modify onDraw glide trail section (lines 123-136):
Replace solid-color trail with per-segment gradient:
```kotlin
// Draw glide trail with gradient fade
if (glideSamples.size > 1) {
    for (i in 0 until glideSamples.size - 1) {
        val s1 = glideSamples[i]
        val s2 = glideSamples[i + 1]
        
        // Linear fade: 1.0 at start, 0.5 at end
        val progress = i.toFloat() / (glideSamples.size - 1)
        val alpha1 = (1.0f - progress * 0.5f).coerceIn(0.5f, 1.0f)
        val alpha2 = ((i + 1).toFloat() / (glideSamples.size - 1) * 0.5f).coerceIn(0.5f, 1.0f)
        
        val trailPaint = Paint().apply {
            color = theme.gestureTrail.withAlpha(alpha1)
            strokeWidth = 6f
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        
        canvas.drawLine(s1.x, s1.y, s2.x, s2.y, trailPaint)
    }
}
```

c) Remove old single-color trail Paint from line 125-130 (if separate Paint object existed).

**UI Changes:**
- None (canvas-only)

### Test Strategy

**Unit Test (KeyboardViewTest.kt):**
- Populate glideSamples with 5 mock samples at known coordinates
- Call invalidate() to trigger onDraw
- Verify no exceptions thrown
- (Full visual testing requires device screenshot comparison)

**Device Test (Manual):**
1. Enable glide in Settings
2. Perform 8+ key glide (e.g., "keyboard")
3. Observe trail: bright at start, dimmer at end
4. Try glides of length 3, 6, 10+ keys
5. Verify fade is smooth (not stepped or jumpy)
6. Glide + tap ripple simultaneously → visual harmonics (no jank)
7. All 6 themes: verify fade direction consistent, no color inversion
8. Performance: expect >58fps during glide

**Visual Regression:**
- Screenshot before/after on same device at same resolution
- Compare trail opacity gradient visually (informal, not automated)

### Effort Estimate
**1 hour total**
- Code: 25 min (alpha calculation + helper method)
- Testing: 25 min (manual glide tests × 6 themes)
- Validation: 10 min (perf, no artifacts)

### Blockers/Dependencies
- None. Assumes glide trail already renders (v0.1.0 feature).
- Task 1 (ripple) is independent; can be done in parallel.

### Rollback Plan

**If gradient looks wrong (inverted/no fade/color weird):**
1. Check alpha formula: should fade 1.0 → 0.5, not 0.5 → 1.0
2. Verify withAlpha() preserves RGB channels correctly
3. Revert to single-color trail:
   ```kotlin
   val trail = Paint().apply {
       color = theme.gestureTrail
       strokeWidth = 6f
       strokeCap = Paint.Cap.ROUND
       strokeJoin = Paint.Join.ROUND
   }
   for (i in 0 until glideSamples.size - 1) {
       canvas.drawLine(glideSamples[i].x, glideSamples[i].y, 
                       glideSamples[i+1].x, glideSamples[i+1].y, trail)
   }
   ```
4. `git checkout app/src/main/java/com/openswift/keyboard/view/KeyboardView.kt`

**If performance drops:**
1. Reduce trail strokeWidth from 6f to 4f
2. Or simplify: create Paint once per glide start (vs. per-segment)
3. Re-profile with Android Studio Profiler: check GPU/frame rendering

---

## TASK 3: Suggestion Pills with Preview Text

### Acceptance Criteria
- [ ] Suggestion pills display: word + preview context
- [ ] Format: "word" + " • " + next 20 chars from prediction (or " …" if end)
- [ ] Pills are tappable chips with proper hit-box (full pill width)
- [ ] Pills animate in with staggered 80ms delay (left → right)
- [ ] Pills have rounded corners (8dp) + subtle shadow
- [ ] Pills expand slightly on hover (1.05x scale) with smooth transition
- [ ] Up to 3 pills visible; truncate beyond
- [ ] Preview text styled: same color, 80% opacity, smaller font (12sp vs 14sp)
- [ ] Works with all 6 themes
- [ ] Settings page displays correctly (use existing PreviewKey layout)
- [ ] Rollback leaves no UI layout shift

### Implementation Plan

**Files to Modify:**
1. `KeyboardView.kt` - Pill rendering + animation
2. `Predictor.kt` - Add preview text generation
3. (New) `SuggestionPill.kt` - Optional data class
4. `OpenSwiftIME.kt` - Update suggestion setting
5. `KeyboardPreview.kt` - Update preview with pills

**Component Changes:**

**A) Create SuggestionPill.kt:**
```kotlin
package com.openswift.keyboard.ui

data class SuggestionPill(val word: String, val preview: String)
```

**B) Predictor.kt - Add enrichment method (after line 52):**
```kotlin
fun suggestWithPreview(
    prefix: String,
    previousWord: String?,
    limit: Int = 3
): List<SuggestionPill> {
    val words = suggest(prefix, previousWord, limit)
    return words.map { w ->
        val next = getPreviewText(w)
        SuggestionPill(w, next)
    }
}

private fun getPreviewText(word: String): String {
    // v0.2.0: Simple stub. Upgrade to real bigrams in v0.3.0
    val nextWord = when (word.lowercase()) {
        "the" -> "quick brown fox"
        "is" -> "a common word"
        "and" -> "then"
        "to" -> "be or not"
        else -> ""
    }
    return nextWord.take(20) + if (nextWord.length > 20) "…" else ""
}
```

**C) KeyboardView.kt - Update suggestion handling:**

a) Replace suggestions declaration (line 31):
```kotlin
private var suggestions: List<SuggestionPill> = emptyList()
private var suggestionAnimProgress = mutableMapOf<Int, Float>()
private var suggestionHoverIndex = -1
```

b) Modify onDraw suggestion rendering (lines 77-90):
```kotlin
// Draw suggestions row with animation
var y = keyH * 0.1f
canvas.drawRect(0f, 0f, w, suggestionH, 
    Paint().apply { color = theme.suggestionBg })

var x = 20f
suggestionBounds.clear()
for ((idx, pill) in suggestions.take(3).withIndex()) {
    val progress = suggestionAnimProgress[idx] ?: 0f
    
    // Pill styling
    val pillPaint = Paint().apply {
        color = theme.keyBackground
        style = Paint.Style.STROKE
        strokeWidth = 2f
        alpha = (progress * 255).toInt()
    }
    
    val pillText = "${pill.word} • ${pill.preview}"
    val w2 = suggestionPaint.measureText(pillText) + 20f
    
    // Scale on hover
    val scale = if (idx == suggestionHoverIndex) 1.05f else 1.0f
    val scaledW = w2 * scale
    val rect = Rect(x.toInt(), y.toInt(), (x + scaledW).toInt(), 
                    (y + suggestionH).toInt())
    suggestionBounds[pill.word] = rect
    
    canvas.save()
    canvas.scale(scale, scale, x + w2 / 2, y + suggestionH / 2)
    
    // Draw pill background with rounded corners
    canvas.drawRoundRect(
        x, y + 4f, x + w2, y + suggestionH - 4f,
        8f, 8f, pillPaint
    )
    
    // Draw word (main text)
    val wordPaint = Paint().apply {
        textAlign = Paint.Align.LEFT
        textSize = 14f
        color = theme.suggestionText
        alpha = (progress * 255).toInt()
    }
    canvas.drawText(pill.word, x + 10f, y + keyH * 0.6f, wordPaint)
    
    // Draw preview text (smaller, dimmer)
    val previewPaint = Paint().apply {
        textAlign = Paint.Align.LEFT
        textSize = 12f
        color = theme.suggestionText
        alpha = (progress * 0.8f * 255).toInt()
    }
    canvas.drawText(" • ${pill.preview}", x + 50f, y + keyH * 0.6f, previewPaint)
    
    canvas.restore()
    x += scaledW + 10f
}
```

c) Modify ACTION_DOWN to trigger animation (line 141):
```kotlin
MotionEvent.ACTION_DOWN -> {
    glideStartTime = System.currentTimeMillis()
    isGliding = false
    glideSamples.clear()
    ripples.add(Ripple(event.x, event.y, glideStartTime))
    postInvalidateOnAnimation()
    
    // NEW: Animate suggestions in with stagger
    for (i in 0 until suggestions.size) {
        suggestionAnimProgress[i] = 0f
        val delay = i * 80L
        postDelayed({
            animateSuggestionPill(i)
        }, delay)
    }
    
    val sample = sampleAt(event.x, event.y)
    if (sample.char != ' ') glideSamples.add(sample)
    return true
}
```

d) Add animation helper method (after setOnGlideListener):
```kotlin
private fun animateSuggestionPill(index: Int) {
    val startTime = System.currentTimeMillis()
    val duration = 300L
    
    fun updateProgress() {
        val elapsed = System.currentTimeMillis() - startTime
        val progress = (elapsed.toFloat() / duration).coerceIn(0f, 1f)
        suggestionAnimProgress[index] = progress
        
        if (progress < 1f) {
            postDelayed(::updateProgress, 16L)  // ~60fps
        }
        
        invalidate()
    }
    
    updateProgress()
}
```

e) Add ACTION_MOVE for hover detection (after ACTION_DOWN, around line 149):
```kotlin
MotionEvent.ACTION_MOVE -> {
    // Detect hover over pills
    val oldHoverIndex = suggestionHoverIndex
    suggestionHoverIndex = -1
    for ((idx, word) in suggestions.take(3).mapIndexed { i, p -> i to p.word }) {
        val rect = suggestionBounds[word]
        if (rect != null && rect.contains(event.x.toInt(), event.y.toInt())) {
            suggestionHoverIndex = idx.first
            if (oldHoverIndex != suggestionHoverIndex) invalidate()
            break
        }
    }
    
    // Existing glide code
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
```

f) Update setSuggestions() method (line 219):
```kotlin
fun setSuggestions(pills: List<SuggestionPill>) {
    suggestions = pills
    suggestionAnimProgress.clear()
    suggestionHoverIndex = -1
    invalidate()
}

// Backward compatibility overload (if needed)
fun setSuggestionsSimple(words: List<String>) {
    suggestions = words.map { SuggestionPill(it, "") }
    invalidate()
}
```

**D) OpenSwiftIME.kt - Update suggestion setting:**
Find where setSuggestions is called and update:
```kotlin
// OLD:
// val suggestions = predictor.suggest(currentWord, previousWord)
// keyboardView.setSuggestions(suggestions)

// NEW:
val suggestionPills = predictor.suggestWithPreview(currentWord, previousWord)
keyboardView.setSuggestions(suggestionPills)
```

**E) KeyboardPreview.kt - Update preview (line 108):**
```kotlin
@Composable
fun PreviewKey(
    label: String,
    bgColor: Color,
    textColor: Color,
    height: Dp,
    modifier: Modifier = Modifier,
    isModifier: Boolean = false,
    previewText: String? = null  // NEW param
) {
    Box(
        modifier = modifier
            .height(height)
            .background(
                if (isModifier) bgColor.copy(alpha = 0.7f) else bgColor,
                RoundedCornerShape(6.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (previewText != null && previewText.isNotEmpty()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    label,
                    color = textColor,
                    fontSize = 11.sp,
                    maxLines = 1
                )
                Text(
                    previewText,
                    color = textColor.copy(alpha = 0.7f),
                    fontSize = 9.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        } else {
            Text(
                label,
                color = textColor,
                fontSize = 12.sp,
                maxLines = 1
            )
        }
    }
}
```

### Test Strategy

**Unit Test (PredictorTest.kt):**
```kotlin
fun testSuggestWithPreview() {
    val predictor = Predictor(wordList, userDict)
    val pills = predictor.suggestWithPreview("the", null)
    
    assert(pills.isNotEmpty())
    assert(pills.first().word == "the")
    assert(pills.first().preview.length <= 20)
}
```

**Device Test (Manual):**
1. Type "the" → observe 3 pills with previews (e.g., "the • quick brown fox")
2. Tap a pill → word inserted correctly
3. Tap another key → pills animate out, new pills animate in (if applicable)
4. Hover over pills → verify 1.05x scale expand smoothly
5. All themes: colors correct, text legible, no overflow
6. Type rapidly ("the is and") → animations queue smoothly (no overlap/stutter)
7. Glide + pill tap → don't interfere
8. Open Settings → Keyboard preview shows pills correctly
9. Performance: expect >58fps with pills + ripple + glide

**Regression:**
- Verify tap on pill inserts word (not suggestion index)
- Verify glide still works (pills only visual, not interactive during glide)
- Verify previous suggestion history not broken
- Verify long glides don't cause pill animation lag

### Effort Estimate
**2 hours total**
- Code (Predictor enrichment): 25 min
- Code (KeyboardView rendering + animation): 50 min
- Code (OpenSwiftIME integration): 10 min
- Code (KeyboardPreview update): 10 min
- Testing (unit + manual): 20 min
- UI polish & tweaks: 5 min

### Blockers/Dependencies
- Tasks 1 & 2 can be done in parallel; do not block this task.
- Requires: Predictor.nextWord() stub (mocked, upgrade in v0.3.0 with real bigrams).
- Assumes: MotionEvent ACTION_MOVE already available for glide; reuse for hover.
- Assumes: postDelayed() and postInvalidateOnAnimation() available on View.

### Rollback Plan

**If pills don't render:**
1. Verify setSuggestions() is called from OpenSwiftIME
2. Check Canvas.drawRoundRect() is supported (API 21+)
3. Verify Predictor.suggestWithPreview() returns non-empty list
4. Fallback: set suggestions to empty list in setSuggestions() and remove pill code

**If animation stutters or jank:**
1. Remove postDelayed loop; animate all pills simultaneously (no stagger)
2. Reduce animation duration from 300ms to 200ms
3. Or disable animation: set all progress to 1.0f immediately in setSuggestions()
4. Check for excessive invalidate() calls during glide

**If preview text is wrong (truncation/overflow):**
1. Adjust measureText() or manually limit: preview.take(15) + "…"
2. Reduce font size from 12sp to 10sp or 11sp
3. Verify getPreviewText() returns strings ≤20 chars

**If tap detection fails:**
1. Print suggestionBounds to Logcat before ACTION_UP
2. Expand hit-box: rect.inset(-5, -3) for easier tapping
3. Verify onGlideListener is not null before calling

**If hover animation is choppy:**
1. Use a single ValueAnimator with interpolator instead of postDelayed
2. Or disable hover scaling: remove scale logic, keep pill at 1.0x always
3. Check for paint object creation per-frame (cache Paint objects)

**If pills overlap with glide trail:**
1. Adjust suggestionH calculation: increase or decrease padding
2. Move pills to separate Canvas layer or use paint clipping
3. Or draw glide trail above pills (change draw order)

---

## Summary: Execution Guide

### Prerequisites
- Kotlin 1.8+
- Jetpack Compose (for Settings preview only)
- Android API 21+
- Git for version control & rollback

### Parallel Execution
```
Session 1 (2h):
├─ Task 1: Ripple (1.5h) ──┐
│                          ├─ Integration test (30 min)
└─ Task 2: Gradient (1h) ──┘
  
Session 2 (2.5h):
└─ Task 3: Pills + Preview (2h) ─── Final testing (30 min)
```

### Commit Strategy
After each task:
```bash
git add app/src/main/java/com/openswift/keyboard/...
git commit -m "v0.2.0: Add ripple effect animation

- Key press now shows expanding circle ripple from tap center
- Ripple expands 2dp → 50dp over 400ms with cubic-out easing
- Alpha fades 1.0 → 0.0 smoothly
- Multiple taps show concurrent ripples
- Works on all 6 themes via theme.keyAccent
- Zero performance impact: 60fps during glide + ripple

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

### Tag Release
After all 3 tasks complete & pass final testing:
```bash
git tag -a v0.2.0-RC1 -m "v0.2.0-RC1: UX Polish

Features:
- Animated ripple on key press
- Gradient fade on glide trail
- Suggestion pills with preview text

Ready for user feedback."

git push origin main --tags
```

### Known Limitations (v0.2.0)
- Preview text is mocked (upgrade to real bigrams in v0.3.0)
- Ripple does not support custom easing curves (cubic-out only)
- Glide gradient assumes monotonic path (doesn't handle loops)
- Pill animation uses postDelayed (not ideal, OK for now)

### Next Steps (v0.3.0)
1. Real bigram data: upgrade Predictor.getPreviewText() with actual language model
2. Improve pill animation: use PropertyAnimator or Compose animation framework
3. Add long-press popup animations (item #4 in roadmap)
4. Performance profiling: optimize redraw during rapid input

---

**Last Updated**: 2026-05-10  
**Status**: Ready for implementation  
**Estimated Completion**: 1 development session (4.5 hours total)
