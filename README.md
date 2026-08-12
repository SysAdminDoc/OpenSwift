# OpenSwift

[![Version](https://img.shields.io/badge/version-0.3.4-blue)](https://github.com/SysAdminDoc/OpenSwift/releases)
[![License](https://img.shields.io/badge/license-MIT-green)](LICENSE)
[![Platform](https://img.shields.io/badge/platform-Android-brightgreen)]()

A modern, lightweight Android keyboard inspired by SwiftKey. Features glide typing, intelligent word prediction, theme customization, and clipboard management.

## Features

- **Glide Typing** — Swipe continuously across keys for fast text entry; path-aware word decoding using Damerau-Levenshtein distance
- **Animated Feedback** — Ripple effect on key tap, gradient fade on glide trail (v0.2+)
- **Suggestion Pills** — Rounded pill-shaped suggestions with preview text (v0.2+)
- **Word Prediction** — Context-aware next-word suggestions with bigram learning; fuzzy matching + frequency weighting
- **Multilingual Dictionaries** — English, German, French, and Spanish word lists with language-specific layout defaults
- **Offline Language Detection** — Current/recent input can switch suggestion dictionaries without network calls
- **Auto-Correct** — Edit-distance-based error recovery with adaptive edit budget (handles transpositions like "teh" → "the")
- **Multi-Layout** — QWERTY, QWERTZ, AZERTY with long-press accent popups (á, à, â, ä, etc.)
- **10 Themes** — AMOLED Black, Catppuccin Mocha, GitHub Dark, Swift Dark, Material Light, Pixel, Nord, Dracula, Tokyo Night, High Contrast WCAG AAA (dark-first default)
- **Custom Themes** — Create and edit themes with full color customization
- **Emoji Picker** — Categorized emoji with recents, favorites, keyword search, and no network dependency
- **Data Portability** — Export/import encrypted-at-rest learned words, snippets, and custom themes with merge or replace behavior
- **Clipboard Manager** — Swipeable history of 25 recent items with delete-on-swipe
- **Snippets/Text Expansion** — Create custom trigger→expansion pairs for instant text insertion
- **Learning Dictionary** — Persistent per-word frequency tracking and bigram learning (local-only)
- **Voice Input** — Speech recognition with partial result streaming (v0.2+)
- **Number Row** — Dedicated digit row for quick number entry (v0.2+)
- **Usage Analytics** — Local-only keystroke, word, and correction tracking (no data leaves device)
- **Accessibility** — Full TalkBack support, key announcements, navigation support, reduced motion mode (v0.3+)
- **Privacy Dashboard** — View clipboard history, dictionary stats, and delete all data (v0.3+)
- **Sensitive-Field Privacy** — Password, private, and no-suggestions fields automatically disable prediction, glide decoding, learning, snippets, and clipboard capture
- **Per-App Settings** — Customize behavior per application (disable glide in games, adjust key height for email, etc.)
- **Haptic & Sound Feedback** — Customizable vibration (20ms default) and optional audio cues

## Architecture

- `OpenSwiftIME` — Main InputMethodService; coordinates layout, prediction, and input flow
- `KeyboardView` — Custom View rendering keys, suggestions, and glide trail detection
- `Predictor` — Scoring engine for next-word and auto-correct suggestions
- `GlideDecoder` — Polyline-to-word decoding using anchored key subsequence matching
- `WordList` + `UserDictionary` — Frequency-based per-language word stores + per-user bigram learning
- `Settings` + `ClipboardHistory` — Persistent user preferences and clipboard state
- `DataPortability` — Local JSON export/import for learned words, snippets, and custom themes
- `Themes` + `Layouts` — 10 built-in themes and 3 keyboard layouts with language defaults

## Building

```bash
./gradlew assembleRelease
```

Output: `app/build/outputs/apk/release/app-release.apk`

Run local checks:

```bash
./gradlew testDebugUnitTest
```

## Installation

1. Download the APK from [Releases](https://github.com/SysAdminDoc/OpenSwift/releases)
2. Enable installation from unknown sources (Settings > Security)
3. Install the APK
4. Go to **Settings > Languages & input > On-screen keyboard > Manage on-screen keyboards**
5. Enable **OpenSwift**
6. Set **OpenSwift** as default input method
7. Open any text field and start typing!

## Documentation

Public setup, usage, architecture, and contribution notes are consolidated in this README. Local planning notes are kept in ignored working-tree files so the GitHub README remains the canonical public document.

## Settings

- **Language** — English, German, French, or Spanish with matching default keyboard layout
- **Detect Language** — Local context scoring can switch prediction language automatically
- **Theme** — 10 built-in themes + custom theme editor
- **Keyboard Layout** — QWERTY, QWERTZ, AZERTY
- **Glide Typing** — Enable/disable swipe-to-type
- **Auto-Correct** — Toggle fuzzy correction
- **Auto-Capitalize** — Auto-capitalize after punctuation
- **Haptic Feedback** — Vibration on keypress (20ms default)
- **Sound Feedback** — Optional audio cues
- **Key Height** — Adjust keyboard size (48–72 dp)
- **Data Portability** — Export a JSON backup, merge imported data, or replace local learned words/snippets/custom themes
- **Incognito Mode** — Disable prediction history, learning, snippets, and clipboard capture for every field

## Emoji Picker

- **Categories** — Recent, favorites, smileys, hands, hearts, food, nature, travel, objects, and symbols
- **Recents** — Selected emoji are stored locally for fast reuse
- **Favorites** — Long-press an emoji to toggle it as a favorite
- **Search** — Tap the search field and use the in-picker letters to filter by local keywords

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

### Multilingual Input
Manual language selection switches the active offline dictionary and learned-word store:
- English uses QWERTY by default
- German uses QWERTZ by default
- French uses AZERTY by default
- Spanish uses QWERTY by default

When language detection is enabled, OpenSwift scores the current word and recent local context against bundled dictionaries and accent hints. No typed text is sent off-device.

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
- **Encrypted at rest** — Clipboard history, snippets, learned words, local usage data, per-app profiles, emoji history, settings, and custom themes use AES-256 encrypted preferences
- **No device backup** — Android cloud backup and device-transfer extraction are disabled for OpenSwift data
- **Open source** — MIT licensed; code is auditable
- **No telemetry** — No analytics, no crash reporting, no ads
- **Automatic incognito fields** — Password and app-declared private/no-suggestions editors never expose suggestions or feed local learning and clipboard history
- **Device learns** — User bigrams and word frequencies stay on-device
- **Clipboard history** — Cleared on uninstall

## Roadmap

**v0.4 (Next)**: Dictionary portability, per-app prediction profiles, custom package import

**v0.5**: Optional encrypted sync, plugin framework hardening, optional on-device ML prediction

**v1.0**: Stable release, expanded language support, performance audit, extension API stability

## Contributing

OpenSwift is open to community contributions. Start with the build and architecture sections above, keep changes local-first and privacy-preserving, and verify with Gradle before submitting patches.

**Ideas for contributions:**
- New keyboard layouts (Dvorak, Colemak, Bépo, etc.)
- Language packs (German, French, Spanish word lists)
- Additional themes
- Accessibility improvements
- Performance optimizations

## License

MIT — see [LICENSE](LICENSE)
