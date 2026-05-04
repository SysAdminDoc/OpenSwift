# Contributing to OpenSwift

We welcome contributions! This guide explains how to set up, build, test, and submit changes.

## Development Setup

### Prerequisites
- Android Studio 2024.1+
- JDK 17+
- Android SDK (API level 35)
- Git

### Clone & Build

```bash
git clone https://github.com/SysAdminDoc/OpenSwift.git
cd OpenSwift
./gradlew assembleDebug     # Build debug APK
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

### Install Debug Build

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Project Structure

```
OpenSwift/
├── app/
│   ├── src/main/
│   │   ├── java/com/openswift/keyboard/
│   │   │   ├── OpenSwiftIME.kt          # Main IME service
│   │   │   ├── engine/
│   │   │   │   ├── Predictor.kt         # Scoring engine
│   │   │   │   ├── GlideDecoder.kt      # Swipe→word
│   │   │   │   ├── WordList.kt          # Dictionary loader
│   │   │   │   ├── UserDictionary.kt    # Bigram learner
│   │   │   │   └── MultilingualPredictor.kt
│   │   │   ├── view/
│   │   │   │   ├── KeyboardView.kt      # Main keyboard rendering
│   │   │   │   └── NumberRowView.kt
│   │   │   ├── ui/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── SettingsActivity.kt  # Settings UI
│   │   │   │   ├── EmojiView.kt
│   │   │   │   └── ClipboardView.kt
│   │   │   ├── layout/                  # Keyboard layouts
│   │   │   ├── theme/                   # 6 themes + editor
│   │   │   ├── data/                    # Settings, persistence
│   │   │   ├── voice/                   # SpeechRecognizer
│   │   │   ├── analytics/               # Usage tracking
│   │   │   └── accessibility/           # TalkBack support
│   │   └── res/                         # Resources, strings, icons
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── gradle/
├── .github/workflows/
│   └── release.yml                      # GitHub Actions CI/CD
├── CLAUDE.md                            # Project notes
├── CHANGELOG.md
├── README.md
└── SETUP.md
```

## Key Classes

### `OpenSwiftIME`
- **Role**: Main InputMethodService; entry point
- **Responsibilities**:
  - Manage keyboard layout switching (QWERTY, QWERTZ, AZERTY, Symbols)
  - Route key presses to handlers
  - Coordinate prediction, auto-correct, learning
  - Manage emoji mode, clipboard mode, settings

### `KeyboardView`
- **Role**: Custom View rendering the keyboard grid
- **Responsibilities**:
  - Draw keys, suggestions, glide trail
  - Detect touches: single tap (key) vs. swipe (glide)
  - Calculate key bounds dynamically
  - Report key and glide events to IME

### `Predictor`
- **Role**: Word suggestion and auto-correct scoring
- **Responsibilities**:
  - Score dictionary candidates by prefix, fuzzy distance, frequency, bigrams
  - Provide next-word suggestions given previous word
  - Auto-correct misspelled words

### `GlideDecoder`
- **Role**: Convert gesture polyline → word
- **Responsibilities**:
  - Extract anchor keys from swipe path
  - Subsequence-match anchors against dictionary
  - Score by frequency, length proximity

### `UserDictionary`
- **Role**: Persist user's typed words and bigram patterns
- **Responsibilities**:
  - Unigram counts (how often each word was typed)
  - Bigram counts (prev_word → next_word frequency)
  - JSON serialization to SharedPreferences
  - Learning on space/enter

## Making Changes

### Code Style
- Follow Kotlin conventions
- Use meaningful variable names
- Comment complex logic (Damerau-Levenshtein, glide anchor detection, etc.)
- Prefer sealed classes for state, data classes for immutable structures

### Adding a New Feature

**Example: Add a new keyboard layout (Dvorak)**

1. Create the layout in `layout/Layouts.kt`:
   ```kotlin
   val Dvorak = KeyLayout(
       "dvorak",
       listOf(
           "',.pyfgcrl".map { letter(it) },
           // ... rest of rows
       )
   )
   ```

2. Add it to the settings UI (`ui/SettingsActivity.kt`):
   ```kotlin
   listOf("qwerty" to "QWERTY", "dvorak" to "Dvorak", ...).forEach { ... }
   ```

3. Update `Layouts.byId()`:
   ```kotlin
   fun byId(id: String): KeyLayout = when (id) {
       "dvorak" -> Dvorak
       ...
   }
   ```

4. Test:
   ```bash
   ./gradlew assembleDebug && adb install -r ...
   ```

### Adding a New Theme

1. Define in `theme/Themes.kt`:
   ```kotlin
   val MyTheme = KbTheme(
       id = "mytheme",
       name = "My Theme",
       background = KbTheme.rgb(20, 20, 40),
       // ... other colors
   )
   ```

2. Add to `Themes.all`:
   ```kotlin
   val all: List<KbTheme> = listOf(..., MyTheme)
   ```

3. Test in Settings UI.

### Improving Prediction

The `Predictor` class scores candidates. To tweak:
- Adjust `budget` thresholds in `suggest()` (edit distance tolerance)
- Modify frequency weight: `Math.log10(freq + 1.0)`
- Adjust bigram boost: currently `1.5x` for known bigrams
- Add more context (3-gram, 4-gram) if needed

## Building & Testing

### Build Variants
```bash
./gradlew assembleDebug      # Debug (fast, unminified)
./gradlew assembleRelease    # Release (minified, signed)
./gradlew clean              # Clean build artifacts
```

### Manual Testing Checklist
- [ ] Open a text field (email, chat, etc.)
- [ ] Tap key individually → should type letter
- [ ] Swipe across keys (glide) → should decode word
- [ ] Type misspelled word, press space → should auto-correct
- [ ] Switch layouts in Settings → apply correctly
- [ ] Switch themes → render colors correctly
- [ ] Add snippet → expansion works
- [ ] Access emoji grid → insert emoji
- [ ] Check suggestions strip → pills display, tappable

### Debug Logging
Add to `OpenSwiftIME` or `KeyboardView`:
```kotlin
android.util.Log.d("OpenSwift", "onKeyPressed: $code $label")
```

View logs:
```bash
adb logcat | grep OpenSwift
```

## Commit & PR Workflow

1. **Fork** the repo
2. **Create a feature branch**:
   ```bash
   git checkout -b feature/my-feature
   ```
3. **Make changes**, test locally
4. **Commit** with clear message:
   ```bash
   git commit -m "Add Dvorak layout

   - Implement KeyLayout for Dvorak ANSI
   - Update settings UI to include Dvorak option
   - Update Layouts.byId() router
   - Tested with manual keyboard interaction"
   ```
5. **Push** and open a pull request

### Commit Message Format
```
<type>: <subject>

<body (optional)>

<footer (optional)>
```

Types: `feat`, `fix`, `docs`, `refactor`, `perf`, `test`

Example:
```
feat: add Dvorak keyboard layout

Implement Dvorak ANSI layout as a new KeyLayout variant.
Users can select it from Settings > Keyboard > Layout.
Updated settings UI and layout router.

Tested manually on Android 13.
```

## Release Process

### Versioning
- Format: `vX.Y.Z` (semantic versioning)
- Update version in:
  - `app/build.gradle.kts` (`versionName = "X.Y.Z"`)
  - `CHANGELOG.md` (new section)
  - `README.md` (shields.io badge)
  - `CLAUDE.md` (version history)

### Create Release
```bash
./gradlew assembleRelease
git tag v0.2.0
git push origin v0.2.0
gh release create v0.2.0 --generate-notes \
  -a app/build/outputs/apk/release/app-release.apk
```

The GitHub Actions workflow (`.github/workflows/release.yml`) automates this on workflow dispatch.

## Architecture Decision Records (ADRs)

### Why Damerau-Levenshtein?
- Handles adjacent transpositions ("teh" → "the")
- O(n·m) with bounded edit distance (early exit)
- Better than Levenshtein for real typos

### Why Subsequence for Glide?
- Swipes don't hit keys in exact order (path curves)
- Subsequence allows skipping keys while preserving order
- Anchor-based: only turns (directional changes) count as key boundaries

### Why JSON for User Dictionary?
- Simple to understand and debug
- Incremental save (every 8 learns)
- Fits in SharedPreferences (~50KB for typical user)
- No external dependency

## FAQ

**Q: Can I add a new language?**  
A: Yes. Create a `WordList` and `UserDictionary` for the language, register in `MultilingualPredictor.registerDictionary()`, update settings UI.

**Q: How do I test glide decoding?**  
A: Call `glideDecoder.decode(samples)` with test `GlideDecoder.Sample` objects. Or trace with `adb logcat`.

**Q: How do I profile performance?**  
A: Use Android Studio's Profiler (CPU, Memory, Battery tabs). Focus on `KeyboardView.onDraw()` and `Predictor.suggest()`.

**Q: Can I use a different prediction engine?**  
A: Yes. Implement a new `Predictor` subclass or replace it entirely in `OpenSwiftIME.onCreate()`.

---

**Thanks for contributing!** 🎉
