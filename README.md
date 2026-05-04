# OpenSwift

[![Version](https://img.shields.io/badge/version-0.1.0-blue)](https://github.com/SysAdminDoc/OpenSwift/releases)
[![License](https://img.shields.io/badge/license-MIT-green)](LICENSE)
[![Platform](https://img.shields.io/badge/platform-Android-brightgreen)]()

A modern, lightweight Android keyboard inspired by SwiftKey. Features glide typing, intelligent word prediction, theme customization, and clipboard management.

## Features

- **Glide Typing** — Swipe continuously across keys for fast text entry; path-aware word decoding using a curated dictionary
- **Word Prediction** — Next-word suggestions with bigram learning; fuzzy matching + frequency weighting
- **Auto-Correct** — Damerau-Levenshtein edit distance with budget-aware correction
- **Multi-Layout** — QWERTY, QWERTZ, AZERTY with long-press accent popups
- **6 Themes** — AMOLED Black, Catppuccin Mocha, GitHub Dark, Swift Dark, Material Light, Pixel; dark-first default
- **Clipboard Manager** — Quick access to 25 recent clipboard items
- **Learning Dictionary** — Tracks user typing patterns; improves predictions over time
- **Haptic & Sound Feedback** — Customizable haptic vibration and audio cues

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
2. Enable unknown sources in Settings > Security
3. Install the APK
4. Go to Settings > Languages & input > On-screen keyboard > Manage on-screen keyboards
5. Enable OpenSwift
6. Select OpenSwift as the default input method

## Settings

- **Theme** — Choose from 6 curated dark and light themes
- **Keyboard Layout** — Switch between QWERTY, QWERTZ, AZERTY
- **Glide Typing** — Enable/disable swipe-to-type
- **Auto-Correct** — Toggle fuzzy correction
- **Auto-Capitalize** — Auto-capitalize after sentence-ending punctuation
- **Haptic Feedback** — Vibration on keypress
- **Key Height** — Adjust keyboard size (56–72 dp)

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
- **UI**: Custom View + Jetpack Compose (settings)
- **Persistence**: SharedPreferences (settings, clipboard, user dictionary)
- **Targeting**: minSdk 26 (Android 8), targetSdk 35

## Privacy

- No cloud sync, no ads, no telemetry
- All data stored locally
- Open source (MIT)

## Roadmap

- [ ] Emoji grid panel
- [ ] Custom phrase/snippet insertion
- [ ] Voice-to-text integration
- [ ] Multilingual prediction
- [ ] Swipe-up numbers row
- [ ] Theme editor

## Contributing

Contributions welcome. Open an issue or PR.

## License

MIT — see [LICENSE](LICENSE)
