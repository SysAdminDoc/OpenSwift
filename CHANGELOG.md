# Changelog

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
