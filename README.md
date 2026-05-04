# OpenSwift

[![Version](https://img.shields.io/badge/version-0.3.0-blue)](https://github.com/SysAdminDoc/OpenSwift/releases)
[![License](https://img.shields.io/badge/license-MIT-green)](LICENSE)
[![Platform](https://img.shields.io/badge/platform-Android-brightgreen)]()

A modern, lightweight Android keyboard inspired by SwiftKey. Features glide typing, intelligent word prediction, theme customization, and clipboard management.

## Features

- **Glide Typing** — Swipe continuously across keys for fast text entry; path-aware word decoding using Damerau-Levenshtein distance
- **Animated Feedback** — Ripple effect on key tap, gradient fade on glide trail (v0.2+)
- **Suggestion Pills** — Rounded pill-shaped suggestions with preview text (v0.2+)
- **Word Prediction** — Context-aware next-word suggestions with bigram learning; fuzzy matching + frequency weighting
- **Auto-Correct** — Edit-distance-based error recovery with adaptive edit budget (handles transpositions like "teh" → "the")
- **Multi-Layout** — QWERTY, QWERTZ, AZERTY with long-press accent popups (á, à, â, ä, etc.)
- **10 Themes** — AMOLED Black, Catppuccin Mocha, GitHub Dark, Swift Dark, Material Light, Pixel, Nord, Dracula, Tokyo Night, High Contrast WCAG AAA (dark-first default)
- **Custom Themes** — Create and edit themes with full color customization
- **Emoji Grid** — 60 tappable emoji (expandable in v0.4+)
- **Clipboard Manager** — Swipeable history of 25 recent items with delete-on-swipe
- **Snippets/Text Expansion** — Create custom trigger→expansion pairs for instant text insertion
- **Learning Dictionary** — Persistent per-word frequency tracking and bigram learning (local-only)
- **Voice Input** — Speech recognition with partial result streaming (v0.2+)
- **Number Row** — Dedicated digit row for quick number entry (v0.2+)
- **Usage Analytics** — Local-only keystroke, word, and correction tracking (no data leaves device)
- **Accessibility** — Full TalkBack support, key announcements, navigation support, reduced motion mode (v0.3+)
- **Privacy Dashboard** — View clipboard history, dictionary stats, and delete all data (v0.3+)
- **Per-App Settings** — Customize behavior per application (disable glide in games, adjust key height for email, etc.)
- **Haptic & Sound Feedback** — Customizable vibration (20ms default) and optional audio cues

## Architecture

- `OpenSwiftIME` — Main InputMethodService; coordinates layout, prediction, and input flow
- `KeyboardView` — Custom View rendering keys, suggestions, and glide trail detection
- `Predictor` — Scoring engine for next-word and auto-correct suggestions
- `GlideDecoder` — Polyline-to-word decoding using anchored key subsequence matching
- `WordList` + `UserDictionary` — Frequency-based word store + per-user bigram learning
- `Settings` + `ClipboardHistory` — Persistent user preferences and clipboard state
- `Themes` + `Layouts` — 6 built-in themes and 3 keyboard layouts

## Building

```bash
./gradlew assembleRelease
```

Output: `app/build/outputs/apk/release/app-release.apk`

## Installation

1. Download the APK from [Releases](https://github.com/SysAdminDoc/OpenSwift/releases)
2. Enable installation from unknown sources (Settings > Security)
3. Install the APK
4. Go to **Settings > Languages & input > On-screen keyboard > Manage on-screen keyboards**
5. Enable **OpenSwift**
6. Set **OpenSwift** as default input method
7. Open any text field and start typing!

**See [SETUP.md](SETUP.md) for detailed setup, first-use tips, and troubleshooting.**

## Documentation

- **[SETUP.md](SETUP.md)** — Installation, first use, common tasks, troubleshooting
- **[GUIDE.md](GUIDE.md)** — Power-user reference (glide tips, snippets, custom themes, accents)
- **[CONTRIBUTING.md](CONTRIBUTING.md)** — For developers (dev setup, architecture, contributing features)
- **[ROADMAP.md](ROADMAP.md)** — Planned features (v0.2–v1.0) and future ideas
- **[EXAMPLES.md](EXAMPLES.md)** — Snippet packs, theme palettes, per-app configs, accessibility setup

## Settings

- **Theme** — 6 built-in themes + custom theme editor
- **Keyboard Layout** — QWERTY, QWERTZ, AZERTY
- **Glide Typing** — Enable/disable swipe-to-type
- **Auto-Correct** — Toggle fuzzy correction
- **Auto-Capitalize** — Auto-capitalize after punctuation
- **Haptic Feedback** — Vibration on keypress (20ms default)
- **Sound Feedback** — Optional audio cues
- **Key Height** — Adjust keyboard size (48–72 dp)

## How It Works

### Glide Typing
1. User swipes across keys; samples collected at each keypress
2. Anchor keys identified at gesture turning points
3. Dictionary words scored by:
   - Starting key match (required)
   - Ending key match (required)
   - Subsequence coverage (all anchors present in order)
   - Word frequency + user history bonus
4. Top result committed

### Word Prediction
For the current incomplete word:
- Prefix match wins (if word starts with typed prefix)
- Fuzzy match (Damerau-Levenshtein ≤ edit budget)
- Frequency weighting: log10(frequency + user-count + 1)
- Bigram boost: +1.5× if word likely follows previous word (user learns)

### Auto-Correct
Applied on space/enter:
- If word length ≥ 3 and not in dictionary, find closest match
- Bounded edit distance; frequency-weighted
- Learns user's correction pattern

## Performance

- **Dictionary**: 3500+ common English words in raw resource
- **Prediction**: O(n) scan with early exit (edit distance budget)
- **Glide decoding**: O(m·n) (m anchors, n dictionary words)
- **Memory**: ~8 MB (word list + user dictionary)

## Tech Stack

- **Language**: Kotlin
- **UI**: Custom View (KeyboardView) + Jetpack Compose (Settings)
- **Persistence**: SharedPreferences (settings, clipboard, user dictionary)
- **Dictionary**: 3500-word frequency-weighted English word list
- **Targeting**: minSdk 26 (Android 8), targetSdk 35
- **Build System**: Gradle 8+, ProGuard minification (R8)

## Privacy & Security

- **Zero cloud dependency** — No network requests, no account required
- **Local-only** — All data stored in app's private SharedPreferences
- **Open source** — MIT licensed; code is auditable
- **No telemetry** — No analytics, no crash reporting, no ads
- **Device learns** — User bigrams and word frequencies stay on-device
- **Clipboard history** — Cleared on uninstall

## Roadmap

See [ROADMAP.md](ROADMAP.md) for detailed v0.2, v0.3, v0.4, v1.0 milestones.

**v0.2 (Next)**: UI polish (ripple effects, animated suggestions), clipboard panel, number row toggle, onboarding wizard

**v0.3**: Multilingual support (German, French, Spanish), advanced input methods, language detection

**v0.4**: Cloud sync (optional), plugin system, custom input engines

**v1.0**: Stable release, performance optimization, expanded emoji pack

## Contributing

OpenSwift is open to community contributions. See [CONTRIBUTING.md](CONTRIBUTING.md) for:
- Development setup
- Code style guidelines
- How to add new features (example: adding a Dvorak layout)
- Pull request workflow

**Ideas for contributions:**
- New keyboard layouts (Dvorak, Colemak, Bépo, etc.)
- Language packs (German, French, Spanish word lists)
- Additional themes
- Accessibility improvements
- Performance optimizations

## License

MIT — see [LICENSE](LICENSE)
