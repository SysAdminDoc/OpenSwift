# OpenSwift v0.2.0 — Quick Reference Card

## 📋 3 Tasks × 3 Sections = Executable Roadmap

### Task 1️⃣ RIPPLE (1.5h)
**Ripple effect on key tap**

```
ACCEPTANCE:  Tap → circle expands 2-50dp, alpha 1→0 over 400ms
EFFORT:      1.5 hours (code 45min, test 30min, setup 15min)
FILES:       KeyboardView.kt only
RISK:        Low (pure canvas, rollback is simple)
BLOCKERS:    None
ROLLBACK:    git checkout app/src/main/java/.../KeyboardView.kt
```

**Code Sketch:**
```kotlin
// 1. Add ripple state
data class Ripple(val x: Float, val y: Float, val startTime: Long)
private val ripples = mutableListOf<Ripple>()

// 2. On tap (ACTION_DOWN)
ripples.add(Ripple(event.x, event.y, System.currentTimeMillis()))
postInvalidateOnAnimation()

// 3. In onDraw (before glide trail)
for ((idx, ripple) in ripples.withIndex()) {
    val progress = (now - ripple.startTime) / 400f  // 0-1
    val radius = 2 + (48 * progress) * density
    val alpha = (1 - progress) * 255
    canvas.drawCircle(ripple.x, ripple.y, radius, 
        Paint().apply { color = theme.keyAccent; alpha = alpha.toInt() })
}
```

**Test:**
- Tap 5 keys → 5 concurrent ripples visible ✓
- Glide + ripple → no jank ✓
- All 6 themes → ripple visible ✓

---

### Task 2️⃣ GRADIENT (1h)
**Glide trail fades bright→dim**

```
ACCEPTANCE:  Trail opacity 100% start → 50% end, linear per segment
EFFORT:      1 hour (code 25min, test 25min, validate 10min)
FILES:       KeyboardView.kt only
RISK:        Low (simple alpha math)
BLOCKERS:    None (Task 1 parallel-safe)
ROLLBACK:    Revert onDraw glide trail loop to single-color Paint
```

**Code Sketch:**
```kotlin
// 1. Add helper
fun Int.withAlpha(alpha: Float): Int {
    val a = (alpha * 255).toInt().coerceIn(0, 255)
    return (a shl 24) or (this and 0x00FFFFFF)
}

// 2. In onDraw, replace trail loop
for (i in 0 until glideSamples.size - 1) {
    val progress = i / (glideSamples.size - 1f)
    val alpha = 1.0f - (progress * 0.5f)  // 1.0 → 0.5
    val paint = Paint().apply {
        color = theme.gestureTrail.withAlpha(alpha)
        strokeWidth = 6f; strokeCap = Paint.Cap.ROUND
    }
    canvas.drawLine(glideSamples[i].x, glideSamples[i].y,
                    glideSamples[i+1].x, glideSamples[i+1].y, paint)
}
```

**Test:**
- Glide "keyboard" → trail bright start, dim end ✓
- Glide length 3, 6, 10 → smooth fade ✓
- All 6 themes → fade consistent ✓

---

### Task 3️⃣ PILLS (2h)
**Suggestions with preview text, animated, hoverable**

```
ACCEPTANCE:  "word • preview" pills, tap to insert, 1.05x hover scale,
             animate in 80ms stagger, all 6 themes, 3 max visible
EFFORT:      2 hours (code 90min, test 25min, polish 5min)
FILES:       KeyboardView.kt, Predictor.kt, SuggestionPill.kt (new),
             OpenSwiftIME.kt, KeyboardPreview.kt
RISK:        Medium (more code, animation logic)
BLOCKERS:    None (Tasks 1 & 2 parallel-safe)
ROLLBACK:    setSuggestions() fallback to show raw words (no preview)
```

**Code Sketch:**
```kotlin
// 1. Create SuggestionPill.kt
data class SuggestionPill(val word: String, val preview: String)

// 2. Predictor: add suggestWithPreview()
fun suggestWithPreview(prefix: String, prevWord: String?): List<SuggestionPill> {
    val words = suggest(prefix, prevWord)
    return words.map { SuggestionPill(it, getPreviewText(it)) }
}

fun getPreviewText(word: String): String {
    // v0.2.0: mocked. v0.3.0: real bigrams
    return when (word) {
        "the" → "quick brown fox"
        "is" → "a"
        else → ""
    }.take(20)
}

// 3. KeyboardView: render pills in onDraw
for ((idx, pill) in suggestions.withIndex()) {
    val progress = suggestionAnimProgress[idx] ?: 0f
    val scale = if (idx == hoverIndex) 1.05f else 1.0f
    val text = "${pill.word} • ${pill.preview}"
    
    // Draw rounded rectangle
    canvas.drawRoundRect(x, y+4, x+w, y+h-4, 8f, 8f,
        Paint().apply { color = theme.keyBackground; alpha = (progress*255).toInt() })
    
    // Draw text with alpha based on progress
    canvas.drawText(pill.word, x+10, y+22, Paint().apply { alpha = (progress*255).toInt() })
    canvas.drawText(pill.preview, x+50, y+22, Paint().apply { alpha = (progress*200).toInt() })
}

// 4. ACTION_DOWN: trigger staggered animation
for ((i, _) in suggestions.withIndex()) {
    postDelayed({ animateSuggestionPill(i) }, i * 80L)
}

// 5. ACTION_MOVE: detect hover
for ((idx, word) in suggestions.withIndex()) {
    if (suggestionBounds[word]?.contains(x, y) == true) {
        suggestionHoverIndex = idx; invalidate(); break
    }
}

// 6. ACTION_UP: detect pill tap
for ((word, rect) in suggestionBounds) {
    if (rect.contains(x.toInt(), y.toInt())) {
        onGlideListener?.invoke(word); break
    }
}
```

**Test:**
- Type "the" → pills appear with preview "the • quick brown fox" ✓
- Tap pill → word inserted ✓
- Hover pill → scales 1.05x smoothly ✓
- Rapid typing → animations queue without overlap ✓
- All 6 themes → pills visible & colors good ✓

---

## 📊 Summary Table

| Metric | Task 1 | Task 2 | Task 3 | Total |
|--------|--------|--------|--------|-------|
| **Hours** | 1.5 | 1.0 | 2.0 | **4.5** |
| **Files** | 1 | 1 | 5 | **7** |
| **Canvas Animation** | ✅ Frame-based | ❌ Static | ✅ postDelayed | — |
| **Complexity** | 🟢 Low | 🟢 Very Low | 🟡 Medium | — |
| **Dependencies** | None | None | 1 & 2 | — |
| **Test Type** | Unit + Manual | Manual | Unit + Manual | — |
| **Rollback Risk** | 🟢 Low | 🟢 Low | 🟡 Medium | — |

---

## 🚀 Execution Timeline

```
Session 1: 2 hours
├─ 0:00-0:45  Task 1 code
├─ 0:45-1:15  Task 1 test + polish
├─ 1:15-1:40  Task 2 code
└─ 1:40-2:00  Task 2 test + integration (1 + 2)

Session 2: 2.5 hours
├─ 0:00-1:30  Task 3 code (all 5 files)
├─ 1:30-2:00  Task 3 test (unit + manual)
└─ 2:00-2:30  Integration test (all 3 features) + tag release
```

---

## ✅ Pre-Code Checklist

Before starting, ensure:
- [ ] Fresh checkout of main branch
- [ ] `./gradlew clean && ./gradlew build` succeeds
- [ ] v0.1.0 APK installs and keyboard works
- [ ] All 6 themes render correctly
- [ ] Glide typing decodes words correctly
- [ ] adb shell "dumpsys gfxinfo openswift" works for perf monitoring

---

## 🔄 Parallel Execution (Optional)

If you want to speed up:
- Start Task 1 code
- At 45-min mark, start Task 2 code **in a new branch** (keep Task 1 running)
- Merge both after testing

**Caution**: Both modify KeyboardView.kt. Must manually merge if going parallel.

---

## 🔙 Quick Rollback Commands

```bash
# Individual rollback
git checkout app/src/main/java/com/openswift/keyboard/view/KeyboardView.kt

# Full revert to v0.1.0
git reset --hard v0.1.0

# Discard uncommitted changes
git clean -fd && git checkout .
```

---

## 📈 Performance Targets

- **Ripple + Glide**: ≥58 fps (target 60)
- **Heap stability**: <100MB growth after 1000 taps
- **Jank frames**: 0 during glide
- **Tap-to-ripple latency**: <16ms (1 frame @ 60fps)

**Measure with:**
```bash
adb shell "dumpsys gfxinfo openswift | grep 'Frame time'"
adb shell "dumpsys meminfo openswift | head -20"
```

---

## 🎨 Theme Compatibility

All features must work on:
1. AMOLED Black (rgb 0,0,0)
2. Catppuccin Mocha (rgb 30,30,46)
3. GitHub Dark (rgb 13,17,23)
4. Swift Dark (rgb 20,26,38)
5. Material Light (rgb 220,226,234)
6. Custom Theme (user-defined)

**Test procedure:** Open Settings → Theme → select each theme → verify feature visible & pretty.

---

## 📝 Commit Workflow

```bash
# Task 1
git checkout -b feature/ripple-effect
git commit -m "v0.2.0: Add ripple effect animation..."
git push origin feature/ripple-effect

# Task 2 (same branch or separate)
git commit -m "v0.2.0: Add glide trail gradient fade..."
git push origin feature/ripple-effect

# Task 3
git commit -m "v0.2.0: Add suggestion pills with preview text..."
git push origin feature/ripple-effect

# Merge & tag
git checkout main && git merge --no-ff feature/ripple-effect
git tag -a v0.2.0-RC1 -m "v0.2.0 release candidate"
git push origin main --tags
```

---

## 🤔 FAQ

**Q: Can I do Tasks 1 & 2 in parallel?**  
A: Yes. Both modify KeyboardView.kt but in different sections (ripple in ACTION_DOWN + onDraw, gradient in onDraw glide trail). Merge carefully.

**Q: What if ripple slows down glide?**  
A: Reduce rippleAnimDuration to 300ms or remove alpha channel (solid color). Re-profile with Profiler.

**Q: Is preview text mocked in v0.2.0?**  
A: Yes. Real bigram data in v0.3.0. For now, stub returns "quick brown fox" for "the", etc.

**Q: Can I reorder Tasks?**  
A: Task 3 is most complex; do it last. Tasks 1 & 2 are independent and fast.

**Q: How do I test on device without adb?**  
A: Android Studio → Run → Select device → Watch logcat for errors. Manual testing on real Pixel 6 preferred.

---

## 📖 Full Documentation

**For complete code, architecture details, and test scripts:**
- 👉 **TASK_CARDS_v0.2.0.md** (23KB) — Full implementation guide
- 👉 **DECOMPOSITION_SUMMARY.md** (10KB) — This summary expanded

**For roadmap context:**
- 👉 **ROADMAP.md** — v0.2.0 / v0.3.0 / v0.4.0 features

---

**Ready to ship. 4.5 hours. No blockers.**

*Generated 2026-05-10 for OpenSwift v0.2.0 UX Polish release.*
