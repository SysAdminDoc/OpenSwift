# OpenSwift v0.2.0 — Task Decomposition Summary

## Overview
Three roadmap items from v0.2.0 have been decomposed into atomic, single-session implementation tasks (max 2h each). All task cards are **ready for Copilot execution** with complete implementation plans, test strategies, and rollback procedures.

---

## Task Card Index

### **TASK 1: Animated Key Press Feedback (Ripple Effect)**
- **Effort**: 1.5 hours
- **Files**: `KeyboardView.kt`
- **Complexity**: Low (Canvas animation)
- **Dependencies**: None
- **Status**: ✅ Ready to code

**What it does**: Circular ripple emanates from tap point, expands 2dp→50dp over 400ms, fades 1.0→0.0.

**Key Implementation:**
- Add `ripples: MutableList<Ripple>` state in KeyboardView
- On ACTION_DOWN: create new Ripple(x, y, timestamp)
- In onDraw (before glide trail): render ripples with progress-based radius & alpha
- Clean up expired ripples automatically

**Testing**: Manual on Pixel 6, verify ripple + glide coexist, all 6 themes work, >58fps

---

### **TASK 2: Glide Trail Gradient (Fade from Start to End)**
- **Effort**: 1 hour
- **Files**: `KeyboardView.kt`
- **Complexity**: Very Low (Paint alpha manipulation)
- **Dependencies**: Task 1 parallel-safe
- **Status**: ✅ Ready to code

**What it does**: Glide trail fades 100% opacity at start → 50% at end, linear per segment.

**Key Implementation:**
- Add helper: `Int.withAlpha(alpha: Float)` extension
- Replace single trail Paint with per-segment loop
- Calculate progress: `i / (samples.size - 1)`
- Fade alpha: `1.0f - progress * 0.5f`

**Testing**: Manual glide tests on all 6 themes, verify smooth fade, no performance impact

---

### **TASK 3: Suggestion Pills with Preview Text**
- **Effort**: 2 hours
- **Files**: `KeyboardView.kt`, `Predictor.kt`, `SuggestionPill.kt` (new), `OpenSwiftIME.kt`, `KeyboardPreview.kt`
- **Complexity**: Medium (animation + pill rendering + preview generation)
- **Dependencies**: Tasks 1 & 2 parallel-safe
- **Status**: ✅ Ready to code

**What it does**: Suggestions display as tappable pills with preview text, animate in with 80ms stagger, expand on hover (1.05x).

**Key Implementation:**
- Create `SuggestionPill(word, preview)` data class
- Add `Predictor.suggestWithPreview()` with mocked preview (upgrade in v0.3.0)
- Render pills on canvas with rounded corners (8dp), shadow, scale on hover
- Animate in with staggered `postDelayed()` (300ms duration per pill)
- Handle pill tap in ACTION_UP via `suggestionBounds` map

**Testing**: Type "the" → see "the • quick brown fox", tap pill → inserts word, hover works, all themes OK

---

## Execution Matrix

| Task | Hours | Files | Canvas? | Animation? | Tests | Parallel? |
|------|-------|-------|---------|------------|-------|-----------|
| 1. Ripple | 1.5 | 1 | ✅ | ✅ (frame-based) | Unit + Manual | 📍 Start |
| 2. Gradient | 1 | 1 | ✅ | ❌ (static) | Manual | ✅ With Task 1 |
| 3. Pills | 2 | 5 | ✅ | ✅ (postDelayed) | Unit + Manual | ✅ With 1 & 2 |
| **Total** | **4.5h** | **7** | — | — | — | **2 sessions** |

---

## Quick Reference: Acceptance Criteria

### Task 1: Ripple
- [ ] Ripple: tap center → circle 2dp to 50dp over 400ms
- [ ] Alpha: 1.0 → 0.0 smooth fade
- [ ] Multiple taps: concurrent ripples
- [ ] Color: theme.keyAccent
- [ ] Perf: 60fps + glide + ripple
- [ ] Themes: all 6 work
- [ ] Rollback: safe (no Canvas state leaks)

### Task 2: Gradient
- [ ] Trail: 100% at start → 50% at end
- [ ] Linear fade per segment
- [ ] Color: theme.gestureTrail (hue constant)
- [ ] Perf: 60fps maintained
- [ ] Themes: all 6 work
- [ ] Coexists with ripple (no visual conflict)
- [ ] Short glides (<3 points): smooth fade applies

### Task 3: Pills
- [ ] Format: "word • preview" (20 chars max preview)
- [ ] Tappable (full pill hit-box)
- [ ] Animate in: 80ms stagger left→right
- [ ] Hover: 1.05x scale, smooth transition
- [ ] Max 3 visible; truncate beyond
- [ ] Font: 14sp word, 12sp preview (80% opacity)
- [ ] Themes: all 6 work
- [ ] Settings preview displays correctly

---

## Implementation Order (Recommended)

### Session 1 (2 hours)
1. **Task 1: Ripple** (1.5h) — Start here, foundational
   - Implement ripple state & rendering
   - Test with manual taps
   - Verify 60fps
   
2. **Task 2: Gradient** (1h) — Parallel start at 1h mark
   - Implement withAlpha() helper
   - Replace trail rendering loop
   - Verify smooth fade on all themes

**Integration Test** (30 min): Ripple + Gradient coexist, glide looks smooth

### Session 2 (2.5 hours)
3. **Task 3: Pills** (2h) — Depends on Sessions 1 output
   - Create SuggestionPill class
   - Implement suggestWithPreview()
   - Render pills on canvas with animation
   - Implement hover scaling
   - Update OpenSwiftIME integration
   - Update KeyboardPreview

**Final Testing** (30 min): Full v0.2.0 feature suite

---

## Rollback Safety

Each task includes a **Rollback Plan** with specific git commands:

```bash
# Quick rollback to v0.1.0
git checkout app/src/main/java/com/openswift/keyboard/view/KeyboardView.kt
git checkout app/src/main/java/com/openswift/keyboard/engine/Predictor.kt
# etc.

# Full version reset
git reset --hard v0.1.0
```

**No data loss**: All UI-only, no database schema changes.

---

## Known v0.2.0 Limitations

These are **intentional**, to be upgraded in v0.3.0+:

1. **Preview text mocked**: Currently stub (e.g., "the" → "quick brown fox"). Real bigram data in v0.3.0.
2. **Ripple easing fixed**: Cubic-out only. Custom easing curves in v0.4.0+.
3. **Glide gradient**: Assumes monotonic path (no loops). Advanced paths in v0.4.0+.
4. **Pill animation**: Uses postDelayed (OK for now). Upgrade to PropertyAnimator/Compose in v0.3.0+.

---

## Files Modified Summary

```
KeyboardView.kt
  ├── Add Ripple data class
  ├── Add ripples list + animation state
  ├── Modify onDraw (add ripple rendering before glide trail)
  ├── Modify ACTION_DOWN (create ripple)
  ├── Add ACTION_MOVE (pill hover detection)
  ├── Modify ACTION_UP (pill tap detection)
  ├── Add suggestion animation helpers
  └── Update setSuggestions() for SuggestionPill type

Predictor.kt
  ├── Add SuggestionPill return type
  ├── Add suggestWithPreview() method
  └── Add getPreviewText() stub

SuggestionPill.kt (NEW)
  └── data class SuggestionPill(word, preview)

OpenSwiftIME.kt
  └── Update setSuggestions() call to use suggestWithPreview()

KeyboardPreview.kt
  └── Add previewText param to PreviewKey composable

Themes.kt
  └── (Optional) Add rippleAlpha field or reuse keyAccent
```

**Total lines added**: ~300  
**Total lines removed**: ~20  
**Net change**: +280 (mostly new animation code)

---

## Testing Checklist

### Manual Device Tests
- [ ] Task 1: Tap 5 keys → 5 concurrent ripples
- [ ] Task 1: Ripple visible all 6 themes
- [ ] Task 1: Glide + ripple coexist without jank
- [ ] Task 2: Glide "keyboard" → trail fades bright→dim
- [ ] Task 2: Glide length 3, 6, 10 → smooth fade on all
- [ ] Task 2: All themes fade consistently
- [ ] Task 3: Type "the" → pills show "the • quick brown fox"
- [ ] Task 3: Tap pill → word inserted correctly
- [ ] Task 3: Hover pill → scales 1.05x smoothly
- [ ] Task 3: Type rapid sequence → pill animations queue smoothly
- [ ] Task 3: Settings keyboard preview displays pills
- [ ] Integration: Ripple + gradient + pills together (no flicker)
- [ ] Performance: adb shell "dumpsys gfxinfo openswift" >58fps

### Unit Tests
- [ ] Ripple: progress calculation 0-1 maps to 2-50dp
- [ ] Gradient: alpha formula 1.0 → 0.5 monotonic
- [ ] Pills: suggestWithPreview returns list with preview ≤20 chars

### Regression Tests
- [ ] v0.1.0 features still work (layouts, emoji, clipboard, snippets)
- [ ] Settings UI responsive
- [ ] Voice recognition not affected
- [ ] Glide decoding still accurate

---

## Commit Message Template

```
git commit -m "v0.2.0: Add ripple effect animation

- Key press shows expanding circle ripple from tap center
- Ripple expands 2dp → 50dp over 400ms with cubic-out easing
- Alpha fades 1.0 → 0.0 smoothly
- Multiple taps show concurrent ripples
- Works on all 6 themes via theme.keyAccent
- Zero performance impact: 60fps during glide + ripple
- Tests: unit ripple math, manual on Pixel 6 API 34

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

---

## Full Documentation

**Complete implementation details, code blocks, and test strategies** are in:
👉 **`TASK_CARDS_v0.2.0.md`** (23KB)

This document contains line-by-line code changes for all 3 tasks, ready to copy-paste into actual files.

---

## Next Steps After v0.2.0

1. **User Feedback** (1 week): Collect feedback on animation timing, visual feel
2. **v0.2.1 Polish** (1 week): Tweak speeds/sizes based on feedback
3. **v0.3.0 Planning** (2 weeks): 
   - Real bigram data (upgrade preview text)
   - Multilingual support (French, German, Spanish)
   - Language auto-detection
4. **v0.4.0 Features**: Alternative input methods (swipe-up number row, T9, etc.)

---

**Status**: ✅ All tasks decomposed and ready for implementation  
**Target Completion**: 1 developer, 1 day (4.5 hours)  
**Quality Gate**: Manual testing on device required (no CI/CD yet for UI animations)

---

*Last Updated: 2026-05-10*  
*For detailed code implementation, see TASK_CARDS_v0.2.0.md*
