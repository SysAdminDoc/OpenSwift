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
- **Custom Packages** — Import validated, encrypted-at-rest JSON packages containing custom themes or keyboard layouts
- **Emoji Picker** — Categorized emoji with recents, favorites, keyword search, and no network dependency
- **Data Portability** — Export/import encrypted-at-rest learned words, snippets, and custom themes with merge or replace behavior
- **Clipboard Manager** — Opt-in history of 25 recent items with sensitive-clip filtering, a dedicated keyboard panel, per-item delete, and clear-all
- **Snippets/Text Expansion** — Create, edit, and delete validated trigger→expansion pairs; type a trigger followed by space, enter, or punctuation to replace it
- **Learning Dictionary** — Persistent per-word frequency tracking and bigram learning (local-only)
- **Voice Input** — Speech recognition with partial result streaming (v0.2+)
- **Number Row** — Dedicated digit row for quick number entry (v0.2+)
- **Usage Analytics** — Local-only keystroke, word, and correction tracking (no data leaves device)
- **Accessibility** — Full TalkBack support, key announcements, navigation support, reduced motion mode (v0.3+)
- **Privacy Dashboard** — View clipboard history, dictionary stats, and delete all data (v0.3+)
- **Sensitive-Field Privacy** — Password, private, and no-suggestions fields automatically disable prediction, glide decoding, learning, snippets, and clipboard capture
- **Per-App Profiles** — Disable predictions/learning or glide and override key height for individual applications without changing global settings
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
- `EncryptedSyncCodec` + `PluginRegistry` — Feature-gated, contract-tested boundaries for authenticated sync envelopes and in-process extensions

## Building

```bash
./gradlew assembleRelease
```

Without release-signing environment variables, the build produces
`app/build/outputs/apk/release/app-release-unsigned.apk`. When all four
`RELEASE_KEYSTORE_PATH`, `RELEASE_KEYSTORE_PASSWORD`, `RELEASE_KEY_ALIAS`, and
`RELEASE_KEY_PASSWORD` values are present, it produces the signed
`app-release.apk` instead.

Run local checks:

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug checkReleaseApkSize
```

`checkReleaseApkSize` builds the minified release, writes
`app/build/reports/apk-size/release.txt`, and fails if the compressed APK exceeds
15 MiB.

### Dependency and size review

The August 2026 release review replaced the all-icons Compose artifact with the
scoped core icon set and moved AndroidX Security Crypto from `1.1.0-alpha06` to
the stable `1.1.0` release. A clean debug APK dropped from 18,916,086 to
11,429,700 bytes (39.6%); the R8-minified release remained effectively flat at
1,820,678 bytes (1.74 MiB, up 62 bytes across both dependency changes).

The Compose BOM remains pinned at `2024.10.01` and AGP at `8.7.2` with Gradle
`8.10.2`. They were reviewed against the current
[Compose](https://developer.android.com/jetpack/androidx/releases/compose) and
[AGP](https://developer.android.com/build/releases/gradle-plugin) release notes;
an AGP 9/Kotlin toolchain migration is intentionally kept separate from this
release-size guardrail.

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
- **Clipboard History** — Opt in to encrypted clipboard capture; Android-marked sensitive clips and private fields are always skipped
- **Snippets** — Manage case-insensitive triggers and multiline replacements; expansion is always disabled in private fields
- **Per-App Profiles** — Tap the keyboard settings key inside an app to prefill its package name, then save prediction, glide, or key-height overrides; reset one profile or all profiles at any time
- **Customization Packages** — Import a versioned JSON theme/layout package; invalid files report the exact field or keyboard action that needs correction
- **Data Portability** — Export a JSON backup, merge imported data, or replace local learned words/snippets/custom themes
- **Incognito Mode** — Disable prediction history, learning, snippets, and clipboard capture for every field

## Customization Package Format

OpenSwift accepts UTF-8 JSON files up to 512 KiB from **Settings → Advanced →
Customization Packages**. Version 1 packages use the following envelope; each
file can contain up to 10 themes and 10 layouts, and must contain at least one:

```json
{
  "format": "openswift.customization",
  "schemaVersion": 1,
  "name": "Midnight Pack",
  "themes": [
    {
      "id": "custom_midnight",
      "name": "Midnight",
      "colors": {
        "background": "#10131A",
        "keyBackground": "#202633",
        "keyModifierBackground": "#171B24",
        "keyText": "#F4F7FF",
        "keyAccent": "#78A9FF",
        "suggestionBackground": "#10131A",
        "suggestionText": "#F4F7FF",
        "gestureTrail": "#C792EA"
      }
    }
  ]
}
```

Theme IDs must start with `custom_`; colors accept `#RRGGBB` or `#AARRGGBB`.
Layout IDs must start with `custom_layout_`. A layout contains `name` and
`rows`, where every key has a `label`, optional `width` (0.25–5), optional
single-character `popup` array, and either the default `character` action or
one of: `shift`, `delete`, `enter`, `space`, `symbols`, `clipboard`, `comma`,
`period`, `emoji`, `settings`, or `spacer`. Layouts allow 3–6 rows, 2–16 keys
per row, and 64 keys total; character labels must be unique, and exactly one
Shift, Delete, Enter, Space, and Symbols action is required. Raw numeric key
codes and unsupported actions are rejected.

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
- Bundled words use an indexed prefix range and length buckets instead of a full-list scan
- Learned-only words are first-class candidates alongside bundled words
- Frequency weighting: log10(frequency + user-count + 1)
- Bigram boost scales with the locally learned previous-word count

### Multilingual Input
Manual language selection switches the active offline dictionary and learned-word store:
- English uses QWERTY by default
- German uses QWERTZ by default
- French uses AZERTY by default
- Spanish uses QWERTY by default

When language detection is enabled, OpenSwift scores the current word and recent local context against bundled dictionaries and accent hints. No typed text is sent off-device.

### Auto-Correct
Applied at space, enter, and punctuation boundaries when enabled:
- If word length ≥ 3 and not in dictionary, find closest match
- Conservative bounded edit distance; uncertain matches preserve the typed word
- Learns user's correction pattern

## Performance

- **Dictionary**: 3500+ common English words in raw resource
- **Prediction**: O(log n + k) indexed prefix lookup; fuzzy work is restricted to nearby word lengths
- **Glide decoding**: O(m·n) (m anchors, n dictionary words)
- **Memory**: ~8 MB (word list + user dictionary)

## Tech Stack

- **Language**: Kotlin
- **UI**: Custom View (KeyboardView) + Jetpack Compose (Settings)
- **Persistence**: AES-256 encrypted SharedPreferences with typed-data migration and backup exclusion
- **Dictionary**: 3500-word frequency-weighted English word list
- **Targeting**: minSdk 26 (Android 8), targetSdk 35
- **Build System**: Gradle 8+, ProGuard minification (R8)

## Privacy & Security

- **Zero cloud dependency** — No network requests, no account required
- **Encrypted at rest** — Clipboard history, snippets, learned words, local usage data, per-app profiles, emoji history, settings, custom themes, and custom layouts use AES-256 encrypted preferences
- **No device backup** — Android cloud backup and device-transfer extraction are disabled for OpenSwift data
- **Open source** — MIT licensed; code is auditable
- **No telemetry** — No analytics, no crash reporting, no ads
- **Automatic incognito fields** — Password and app-declared private/no-suggestions editors never expose suggestions or feed local learning and clipboard history
- **Device learns** — User bigrams and word frequencies stay on-device
- **Clipboard history** — Off by default, bounded to 25 unique non-empty items, and cleared on uninstall

### Experimental extension boundaries

Published builds compile both `ENABLE_EXPERIMENTAL_SYNC` and
`ENABLE_EXPERIMENTAL_PLUGINS` as `false`, so neither surface is reachable from
settings or the IME. The sync boundary only accepts dictionary, snippet, and
theme payloads; transports receive versioned AES-256-GCM envelopes whose keys
are derived from non-persisted passphrases. Per-app metadata and analytics are
not part of the contract.

Plugin API v1 supports prediction, theme, and layout capabilities for explicitly
registered in-process extensions. It deliberately provides no APK loading,
reflection discovery, Android `Context`, filesystem, or network capability.
Metadata, namespaces, text lengths, suggestion counts, and keyboard geometry
are bounded; a plugin that throws or violates its contract is unloaded and
quarantined without taking down healthy plugins.

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
