# Roadmap

## v0.1.0 ✅ (2026-05-03)
**Current Release — Feature-Complete**

- [x] Core IME service with InputMethodService
- [x] QWERTY, QWERTZ, AZERTY layouts
- [x] Glide typing with gesture recognition
- [x] Word prediction (prefix + fuzzy + frequency)
- [x] Auto-correct (Damerau-Levenshtein)
- [x] User learning (bigram dictionary)
- [x] 6 built-in themes (dark-first)
- [x] Emoji grid (60 emoji)
- [x] Clipboard history (25 items)
- [x] Snippet manager (text expansion)
- [x] Settings UI (Compose)
- [x] Theme editor (custom colors)
- [x] Voice recognition (SpeechRecognizer)
- [x] Usage analytics (local-only)
- [x] Accessibility (TalkBack support)
- [x] Per-app settings

---

## v0.2.0 (Next — UX Polish)
**Focus: User experience, performance, visual polish**

### UI/UX
- [ ] Animated key press feedback (ripple effect)
- [ ] Glide trail gradient (fade from start to end)
- [ ] Suggestion pills with preview text
- [ ] Long-press popup animations
- [ ] Clipboard history UI panel (swipe-accessible)
- [ ] Number row toggle (double-tap space)

### Performance
- [ ] WordList lazy loading (load on demand)
- [ ] Glide decoder optimization (spatial hashing for candidate filtering)
- [ ] Suggestion caching (don't recompute for identical prefix)
- [ ] Reduced redraw frequency in KeyboardView

### Polish
- [ ] Sound effects for key press (optional, settings)
- [ ] Custom key sounds per app
- [ ] Glide sensitivity slider (minimum swipe distance)
- [ ] Key haptic strength adjustment
- [ ] Splash screen + onboarding wizard

---

## v0.3.0 (Language & Learning)
**Focus: Multilingual support, smarter prediction**

### Multilingual
- [ ] French dictionary + layout
- [ ] German dictionary + QWERTZ (extended with umlauts)
- [ ] Spanish dictionary
- [ ] Language auto-detection (based on typed words)
- [ ] Language-specific punctuation rules

### Prediction Improvements
- [ ] 3-gram scoring (prev2_word → prev_word → word)
- [ ] Contextual spelling (capitalization patterns)
- [ ] Common phrase recognition
- [ ] Per-user frequency decay (older learn events weighted less)
- [ ] Backup/restore user dictionary

### Privacy & Control
- [ ] Export user dictionary (for backup/porting)
- [ ] Import learned words from file
- [ ] Analytics dashboard (local: top keys, common words)
- [ ] Privacy dashboard (data deletion, settings reset)

---

## v0.4.0 (Advanced Input)
**Focus: Alternative input methods, extensibility**

### Input Methods
- [ ] Swipe-up number row (gesture on space bar)
- [ ] Double-tap shift for caps-lock
- [ ] Flow typing (fluid, unrestricted swipes)
- [ ] T9/multi-tap support (numeric keyboards)
- [ ] Handwriting input integration (if device has stylus)

### Content & Extensions
- [ ] Custom phrase packs (import/export sets of snippets)
- [ ] Emoji categories (recents, favorites, search)
- [ ] GIF picker integration
- [ ] Template system (signatures, canned responses)

### Integration
- [ ] Recent emojis/clips in keyboard
- [ ] Shortcuts to frequently used words
- [ ] Context-aware suggestions (URL patterns, email, phone)
- [ ] App-specific prediction (game mode, focus mode)

---

## v0.5.0 (ML & Cloud-Optional)
**Focus: Optional machine learning, cloud-optional sync**

### Optional Cloud Sync
- [ ] Cloud backup of user dictionary (opt-in, encrypted)
- [ ] Sync snippets & custom themes across devices
- [ ] Optional telemetry for prediction improvement (anonymized, opt-in)

### ML-Based Prediction
- [ ] On-device neural next-word model (optional download)
- [ ] Custom model fine-tuning on user's data
- [ ] Predictive text for full sentences
- [ ] Tone/formality detection (casual vs. formal)

### Advanced Features
- [ ] Custom correction patterns (e.g., "omg" → "oh my god")
- [ ] Text beautification (smart capitalization, punctuation)
- [ ] Multi-user profiles (different predictions per account)
- [ ] Keyboard profiling (adaptive key sizing per user accuracy)

---

## v1.0.0 (Stable & Complete)
**Focus: Stability, polish, community**

- Production-grade performance
- Comprehensive documentation
- Stable API for community extensions
- Native support for 5+ languages
- Theme marketplace (community-submitted themes)
- Plugin system for community-contributed input methods

---

## Parking Lot (Future Consideration)

These ideas are interesting but not prioritized for the current roadmap:

- [ ] Handwriting recognition (requires ML model)
- [ ] Morse code input mode
- [ ] Shorthand expansion (steno-like, requires learning curve)
- [ ] Keyboard profiler app (heat maps of typing patterns)
- [ ] Web dashboard for cross-device management
- [ ] Integration with password managers
- [ ] Biometric unlock for sensitive snippets
- [ ] Keyboard hardware support (Bluetooth keyboard routing)

---

## How to Contribute to Roadmap

1. **Open an issue** with `[Feature Request]` tag
2. **Discuss** in the issue (is it aligned with OpenSwift's goals?)
3. **Submit a PR** when you're ready to implement
4. **Follow the Contributing guide** (architecture, code style, testing)

**Priorities are driven by:**
- User feedback (GitHub issues)
- Feasibility (complexity, dependencies)
- Alignment with core mission (privacy-first, local-only, extensible)

---

**Current Version**: v0.1.0  
**Last Updated**: 2026-05-03  
**Maintainer**: @SysAdminDoc
