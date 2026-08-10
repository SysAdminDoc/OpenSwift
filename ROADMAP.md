# OpenSwift Roadmap

Actionable work only. Historical and completed roadmap material is archived in CHANGELOG.md; blocked work is kept in Roadmap_Blocked.md.

## Actionable Items

| Priority | Milestone | Work | Exit criteria |
|---|---|---|---|
| P1 | v0.4 | Per-app prediction profiles | Existing per-app settings extend to prediction/glide behavior with a visible reset path. |
| P1 | v0.4 | Custom layout/theme package format | JSON or ZIP package format documented with validation and import errors that users can understand. |
| P2 | v0.5 | Optional encrypted sync | Sync is opt-in, encrypted, and scoped to dictionary/snippets/themes; local-only mode remains default. |
| P2 | v0.5 | Plugin framework hardening | `PluginRegistry` supports custom prediction engines, layouts, and themes behind a stable API boundary. |
| P2 | v0.5 | On-device ML prediction | Optional downloadable neural model improves next-word suggestions without network inference. |
| P3 | v1.0 | Stable release readiness | 5+ languages, extension API stability, import/export polish, performance audit, and complete user/developer docs. |

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
