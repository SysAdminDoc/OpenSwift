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
- Settings UI (Compose) for theme, layout, and feature toggles
- Haptic and sound feedback
- Custom KeyboardView with suggestion strip
- Gradle-based build system with ProGuard minification

### Infrastructure
- GitHub Actions release workflow
- MIT License
- 3500-word English dictionary (curated frequency list)
