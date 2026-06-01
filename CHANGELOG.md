# Changelog

## Unreleased

### Changed
- Consolidated planning docs: active work now lives in `ROADMAP.md`, shipped work in `COMPLETED.md`, research summary in `RESEARCH_REPORT.md`, and historical v0.2/dependency-analysis docs under `docs/archive/`.

## v0.3.0 — 2026-05-04 (Accessibility & Privacy)

### Added
- **Reduced Motion accessibility toggle** — Disables ripple, trail, and animation effects for users with vestibular sensitivity
- **High Contrast WCAG AAA theme** — 10th theme with 7:1+ contrast ratio for color-blind and low-vision users (black text on white, white text on black)
- **Privacy Dashboard** — View clipboard history, dictionary stats, clear all data with single tap
- **4 new themes** — Nord, Dracula, Tokyo Night, High Contrast (total 10 themes)

### Changed
- Settings reorganized into sections: Appearance, Keyboard, Typing, Feedback, **Accessibility**, Advanced
- Navigation bar updated: Home / Settings / **Privacy** / About (4 tabs)

### Fixed
- Private accessors exposed via public methods (UserDictionary.getWordCount(), .reset())

### Accessibility
- Reduced motion compliant with WCAG 2.1 Level AAA
- High Contrast theme meets WCAG AA (7:1 minimum contrast ratio)
- Privacy Dashboard enables user control over local data

---

## v0.2.0 — 2026-05-04

### Added
- **Animated key press feedback** — Ripple effect emanates from tap point, 400ms cubic-out fade with theme-aware color
- **Glide trail gradient** — Trail line fades from opaque to transparent over 300ms during gesture input
- **Suggestion pills UI** — Rounded suggestion boxes with preview text (first 3 chars) for faster visual scanning
- **Encrypted SharedPreferences** — All user data (settings, dictionary, clipboard) now encrypted via AES256-GCM

### Fixed
- SharedPreferences data exposure vulnerability — now uses EncryptedSharedPreferences with MasterKey
- Debug keystore signing — separated debug/release signing configs (production ready)
- Theme compatibility crash on resume — confirmed AppCompat theme requirement

### Changed
- Released keystore configuration — requires environment variables (RELEASE_KEYSTORE_*) or manual setup
- v0.2 pre-release gate: Icon library bloat documented, release signing configured

### Performance
- Ripple list cleanup automatic (no memory leak)
- Trail points cleaned by natural fade-out
- postInvalidateOnAnimation only when active animations present
- 60fps maintained during concurrent glide + ripple + trail rendering

---

## v0.1.0 — 2026-05-03

### Added
- Core IME service with InputMethodService integration
- QWERTY, QWERTZ, AZERTY keyboard layouts with long-press popups
- Glide typing with gesture recognition (Damerau-Levenshtein-based word decoding)
- Intelligent word prediction with bigram learning
- Auto-correct with fuzzy matching and edit budget
- 6 built-in themes: AMOLED Black, Catppuccin Mocha, GitHub Dark, Swift Dark, Material Light, Pixel
- User dictionary with per-word frequency tracking
- Clipboard history manager (25 items)
- Emoji grid (60 emoji, tap-to-insert)
- Snippet manager (text expansion on trigger)
- Number row variant (1234567890)
- Settings UI (Compose) with 3 tabs: Keyboard, Themes, Snippets
- Theme editor (custom color creation + saving)
- Voice recognition (SpeechRecognizer with partial results)
- Usage analytics (local-only keystroke/word/correction tracking)
- Accessibility support (TalkBack announcements)
- Per-app settings (disable glide in specific apps)
- Haptic and sound feedback
- Custom KeyboardView with suggestion strip and glide trail visualization
- Gradle-based build system with ProGuard minification

### Infrastructure
- GitHub Actions release workflow
- MIT License
- 3500-word English dictionary (curated frequency list)
- Comprehensive documentation: SETUP.md, CONTRIBUTING.md, GUIDE.md, ROADMAP.md
- Per-repo CLAUDE.md with architecture + gotchas
