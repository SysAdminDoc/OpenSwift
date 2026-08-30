# Changelog

## v0.3.5 - 2026-08-29

### Added
- New app icon: adaptive, themed (monochrome) and legacy variants regenerated from the 2026-08 icon set.
- Sensitive-field privacy mode automatically hides suggestions and disables glide prediction, learning, snippet expansion, and clipboard capture for password, private, and no-suggestions editors.
- Privacy policy tests cover text, visible, web, and numeric passwords; no-suggestions and no-personalized-learning flags; global incognito; and ordinary fields.
- Clipboard, snippet, learned-word, local usage, per-app, emoji-history, and custom-theme stores now migrate from plaintext into AES-256 encrypted preferences.
- Android backup and device-transfer rules explicitly exclude preferences, with application backup disabled as a second boundary.
- Migration and backup-contract tests protect encrypted-value precedence, plaintext cleanup, custom-theme filtering, and manifest/rule wiring.
- Clipboard capture is now opt-in at the data boundary, skips private fields and Android-marked sensitive clips, rejects empty/duplicate values, and enforces a 25-item cap.
- A dedicated keyboard key opens the clipboard panel, which now includes return, per-item delete, empty-state, and clear-all controls.
- Clipboard policy tests cover gating, duplicate handling, ordering, and retention.
- Prediction regression tests cover prefix ranking, autocorrect confidence, learned-only candidates, bigram boosts, dictionary parsing/persistence, glide decoding, commit replacement, and settings persistence.
- Snippet settings now support validated create, edit, delete, empty, and multiline workflows with live list updates.
- Full, case-insensitive snippet triggers expand at space, enter, or punctuation boundaries; punctuation-prefixed triggers and edits made while the IME remains alive are supported.
- Snippet tests cover encrypted-storage shapes, persistence, rename/delete, duplicate validation, longest-suffix matching, corruption recovery, and private-field gating.
- Per-app profiles now support encrypted prediction/learning, glide, and key-height overrides resolved directly from the active editor package, with add/edit/reset-all UI and persistence tests.
- Versioned JSON customization packages can import custom themes and layouts with bounded sizes, strict color/key/action validation, encrypted storage, user-facing field errors, and runtime/settings selection.
- Experimental sync and plugin entry points are compile-time disabled by default; sync transports now accept authenticated ciphertext only, while plugin API v1 applies capability/namespace/result limits and quarantines failing extensions.
- Sync encryption uses a versioned AES-256-GCM envelope, random salt/nonce, PBKDF2-HMAC-SHA256 passphrase keys, authenticated headers, bounded work factors, and tamper/wrong-passphrase failure tests.
- Settings can export, merge, or replace optional encrypted `.oswsync` snapshots through Android's document picker; operations run off the UI thread, passphrases are never persisted, and no background or network sync is enabled.
- Italian is now the fifth bundled offline language, with a frequency-ranked dictionary, QWERTY default, language-detection hints, and an Android IME subtype.
- Full JSON backups now include custom layouts, validate bounded typed data before any writes, roll back failed preference groups, refresh imported customization immediately, and confirm destructive replacement; encrypted sync remains scoped to dictionaries, snippets, and themes.

### Fixed
- The optional number row now accepts a complete touch gesture and reports the selected key. Its previous `ACTION_DOWN` path returned false, so Android stopped delivering the gesture before `ACTION_UP` could emit a digit.
- Debug APK builds now use Android's managed debug signing configuration instead of requiring a keystore inside the disposable build directory.
- Release APKs can be built unsigned for local verification when signing credentials are absent; signed output still requires all four release-keystore environment variables.
- Privacy delete-all now clears every typed-data store and all bundled language dictionaries.
- Learned-only words now participate in prediction and glide decoding, while punctuation is finalized outside the learned word token.
- Word completion and correction replace the already-committed prefix instead of duplicating typed text; the Auto-Correct setting is now honored at every word boundary.
- Settings content now respects Android 15 safe-drawing insets instead of overlapping status or navigation controls.

### Performance
- The number row now calculates key bounds only when its size changes and reuses its drawing paints instead of allocating rectangles and paints on every frame.
- Prefix prediction uses a binary-searched lexical index and length-bucketed fuzzy candidates, with a 100,000-word regression budget.
- Replacing Compose's extended icon artifact with the core icon set reduced a clean debug APK from 18,916,086 to 11,429,700 bytes (39.6%); the minified release remains 1.74 MiB.
- Release verification now reports compressed APK size and enforces a 15 MiB budget; AndroidX Security Crypto is pinned to stable `1.1.0`.

## v0.3.4 - 2026-06-28

### Added
- **Data portability** - Settings can export learned words, snippets, and custom themes to JSON.
- **Merge/replace imports** - Imported learned-word counts merge additively; matching snippets and themes overwrite by trigger/id, while replace clears local data first.
- **Portability tests** - Unit coverage validates dictionary count merging and snippet/theme overwrite behavior.

## v0.3.3 - 2026-06-28

### Added
- **Emoji expansion** - Emoji picker now supports categories, recents, favorites, keyword search, and scrolling result grids.
- **Emoji catalog tests** - Unit coverage validates categories, search keyword matching, and unique emoji lookup.

### Changed
- Emoji rendering now reuses paint objects instead of allocating a `Paint` per emoji draw.
- README, About, version metadata, and local roadmap state now reflect emoji expansion as shipped.

## v0.3.2 - 2026-06-28

### Added
- **Offline language detection** - Current and recent typed words can switch suggestion dictionaries without sending text off-device.
- **Detection setting** - Users can disable automatic language detection while keeping manual language selection.
- **Detector tests** - Unit coverage validates dictionary evidence, accent evidence, and ambiguous-current-language behavior.

### Changed
- README, About, version metadata, and local roadmap state now reflect language detection as shipped.

## v0.3.1 - 2026-06-27

### Added
- **Multilingual foundation** - English, German, French, and Spanish offline dictionaries can be selected manually.
- **Language-specific layouts** - German defaults to QWERTZ, French defaults to AZERTY, and English/Spanish default to QWERTY.
- **IME subtypes** - Android can identify English, German, French, and Spanish keyboard subtypes.
- **Language registry tests** - Unit coverage locks the language-to-layout fallback behavior.

### Changed
- Prediction, auto-correct, glide decoding, and learned-word storage now use the active language instead of always using English.
- README and quick-start scripts now point at the tracked public README instead of ignored local markdown docs.

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

## Roadmap archive — 2026-08-10 — ROADMAP.md

<details>
<summary>Original roadmap snapshot</summary>

```markdown
# OpenSwift Roadmap

Active roadmap for forward-looking work. Completed work lives in git history
and `CHANGELOG.md`; research/dependency findings live in `RESEARCH.md`.

Current release line: v0.3.4. Last consolidated: 2026-06-28.

## Current State

- v0.1.0 shipped the production-ready IME foundation.
- v0.2.0 shipped key feedback polish, suggestion pills, encrypted
  SharedPreferences, release-signing config, and performance fixes.
- v0.3.0 shipped reduced motion, high contrast, a privacy dashboard, Settings
  reorganization, and four additional themes.
- v0.3.1 shipped manually selectable English, German, French, and Spanish
  dictionaries with language-specific layout defaults.
- v0.3.2 shipped offline language detection for current/recent input context.
- v0.3.3 shipped emoji categories, recents, favorites, and keyword search.
- v0.3.4 shipped local export/import for learned words, snippets, and custom themes.
- The previous v0.2 task/decomposition package is archived under
  [docs/archive/roadmap](docs/archive/roadmap/).

## Active Queue

| Priority | Milestone | Work | Exit criteria |
|---|---|---|---|
| P1 | v0.4 | Per-app prediction profiles | Existing per-app settings extend to prediction/glide behavior with a visible reset path. |
| P1 | v0.4 | Custom layout/theme package format | JSON or ZIP package format documented with validation and import errors that users can understand. |
| P2 | v0.5 | Optional encrypted sync | Sync is opt-in, encrypted, and scoped to dictionary/snippets/themes; local-only mode remains default. |
| P2 | v0.5 | Plugin framework hardening | `PluginRegistry` supports custom prediction engines, layouts, and themes behind a stable API boundary. |
| P2 | v0.5 | On-device ML prediction | Optional downloadable neural model improves next-word suggestions without network inference. |
| P3 | v1.0 | Stable release readiness | 5+ languages, extension API stability, import/export polish, performance audit, and complete user/developer docs. |

## Release Gates

- No network dependency for core typing.
- Clipboard, dictionary, snippets, and settings remain encrypted or private to
  the app sandbox.
- Accessibility modes must work with TalkBack, reduced motion, and high
  contrast enabled.
- Keyboard rendering must stay smooth during glide, ripple, suggestion, and
  theme updates.
- Dependency additions must justify APK size and license impact in
  `RESEARCH.md` or a follow-up research note.

## Parking Lot

- Handwriting recognition.
- Morse code input.
- Steno-style shorthand.
- Web dashboard.
- Password-manager integration.
- Biometric unlock for sensitive snippets.
- Hardware keyboard routing.

## Research-Driven Additions

Existing-item notes: v0.4 language detection should stay offline and respect sensitive-field mode; v0.4 emoji expansion should use Unicode/CLDR annotations; v0.5 sync and plugin work should not expose placeholder crypto or runtime extension points until contract tests exist.

- [ ] P0 - Sensitive-field privacy mode
  Why: Password, private, and no-suggestions fields must not feed learning, prediction, clipboard capture, analytics, or visible suggestion UI.
  Evidence: Android `EditorInfo` and `InputType` docs; `app/src/main/java/com/openswift/keyboard/OpenSwiftIME.kt`; `app/src/main/java/com/openswift/keyboard/data/Settings.kt`.
  Touches: `OpenSwiftIME`, `Settings`, `ClipboardHistory`, `UserDictionary`, `UsageAnalytics`, IME tests.
  Acceptance: Password/private/no-suggestions fields force incognito behavior, clear current word, hide suggestions, skip clipboard capture and learning, and have unit tests for representative `inputType` combinations.
  Complexity: M

- [ ] P0 - Typed-data encryption and backup exclusions
  Why: Clipboard history, snippets, user dictionary, analytics, per-app settings, and custom themes are typed or preference data that should not be restored or copied accidentally from plaintext stores.
  Evidence: `android:allowBackup="true"` in `app/src/main/AndroidManifest.xml`; plaintext stores in `ClipboardHistory`, `SnippetManager`, `UserDictionary`, `UsageAnalytics`, `PerAppSettings`, and `ThemeEditor`.
  Touches: Manifest backup rules, secure storage migration, privacy reset/export paths, settings UI.
  Acceptance: Sensitive stores are encrypted or explicitly excluded from Auto Backup/data extraction, migration preserves existing local data, reset/delete clears all stores, and tests cover migration plus backup-rule presence.
  Complexity: L

- [ ] P0 - Clipboard capture gating
  Why: Clipboard capture currently runs when the input view starts, but the settings surface promises clipboard control and Android supports marking copied content sensitive.
  Evidence: Android sensitive clipboard guidance; `app/src/main/java/com/openswift/keyboard/OpenSwiftIME.kt`; `app/src/main/java/com/openswift/keyboard/clipboard/ClipboardHistory.kt`.
  Touches: `OpenSwiftIME`, `ClipboardHistory`, settings toggles, clipboard UI.
  Acceptance: Clipboard history is off unless enabled, skips sensitive/private fields, ignores duplicate/empty values, enforces retention, and exposes a reachable clipboard panel with delete-all behavior.
  Complexity: M

- [ ] P1 - Snippet CRUD and expansion parity
  Why: The README advertises snippets, but the settings create action is incomplete and IME expansion only checks single digit keys.
  Evidence: `app/src/main/java/com/openswift/keyboard/ui/SettingsActivity.kt`; `app/src/main/java/com/openswift/keyboard/snippets/SnippetManager.kt`; `app/src/main/java/com/openswift/keyboard/OpenSwiftIME.kt`.
  Touches: Snippet settings UI, snippet storage, IME text buffer, tests.
  Acceptance: Users can create/edit/delete snippets, expansion works for documented triggers, expansion respects sensitive-field privacy, and tests cover trigger matching plus persistence.
  Complexity: M

- [ ] P1 - IME engine regression suite
  Why: Current tracked tests only cover language metadata, leaving prediction, glide decoding, privacy, and settings behavior unprotected.
  Evidence: `app/src/test/java/com/openswift/keyboard/data/KeyboardLanguagesTest.kt`; `Predictor`; `GlideDecoder`; `MultilingualPredictor`; `UserDictionary`.
  Touches: JVM tests, fixture dictionaries, fake `EditorInfo` cases, deterministic predictor inputs.
  Acceptance: Tests cover prefix prediction, autocorrect threshold behavior, learned-word candidates, bigram boosts, glide decoding, language dictionary loading, sensitive mode, and settings persistence.
  Complexity: M

- [ ] P1 - Prediction pipeline correctness
  Why: Learned-only words are not first-class candidates, punctuation can be folded into the current word, and prefix scanning over full dictionaries will not scale with larger language packs.
  Evidence: `app/src/main/java/com/openswift/keyboard/engine/Predictor.kt`; `app/src/main/java/com/openswift/keyboard/OpenSwiftIME.kt`; `UserDictionary`.
  Touches: Candidate generation, scoring, word-boundary handling, dictionary index structures, tests.
  Acceptance: Learned words can appear without static dictionary membership, punctuation commits do not poison learning, larger dictionaries stay under the latency budget, and ranking tests pin expected outcomes.
  Complexity: L

- [ ] P1 - APK and dependency budget gate
  Why: Keyboard install friction and IME startup performance are sensitive to APK size, icon bloat, and unstable dependencies.
  Evidence: `app/build.gradle.kts`; `androidx.compose.material:material-icons-extended`; `androidx.security:security-crypto:1.1.0-alpha06`; AndroidX/AGP release notes.
  Touches: Gradle dependencies, release build reporting, icon imports, dependency update notes.
  Acceptance: Release builds report APK size, dependency changes record size impact, unused extended icons are replaced with scoped vectors or core icons, and security/Compose/AGP versions are reviewed before release.
  Complexity: S

- [ ] P2 - Sync and plugin contract hardening
  Why: Future sync and plugins are high-trust features, and placeholder encryption or runtime extension points must not become reachable by accident.
  Evidence: `app/src/main/java/com/openswift/keyboard/sync/CloudSync.kt`; `app/src/main/java/com/openswift/keyboard/plugins/PluginRegistry.kt`.
  Touches: Sync API, plugin registry, feature flags, tests, settings visibility.
  Acceptance: Sync and plugin entry points remain hidden until authenticated encryption, key handling, capability limits, failure isolation, and contract tests are implemented.
  Complexity: L
```

</details>
