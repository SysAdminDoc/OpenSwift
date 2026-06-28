# Research - OpenSwift

## Executive Summary

OpenSwift is a privacy-first Android keyboard with a strong local-only positioning, custom canvas keyboard, Compose settings UI, glide typing, prediction, themes, snippets, clipboard history, and a multilingual foundation. The best next work is trust and correctness before adding larger v0.4/v0.5 surfaces: enforce sensitive-field privacy, encrypt or exclude all typed-data stores, make clipboard capture opt-in and context-aware, restore the advertised snippet workflow, add engine/UI regression tests, fix prediction pipeline gaps, control APK/dependency growth, and anchor existing language/emoji plans in offline Android, Unicode, and CLDR behavior.

## Product Map

- Core workflows: enable the IME, type with tap or glide, accept predictions, autocorrect on space, switch layouts/languages, manage themes/settings, use clipboard history, expand snippets, and eventually export/import personal data.
- User personas: privacy-sensitive Android users, open-source keyboard users, multilingual typists, accessibility users who need predictable contrast/haptics, and power users who want local customization without cloud dependency.
- Platforms and distribution: Android IME targeting minSdk 26 and targetSdk 35, Gradle/AGP release APK builds, no current Play-services dependency, and GitHub-hosted source/releases.
- Key integrations and data flows: `InputMethodService` receives `EditorInfo` and typed text; `OpenSwiftIME` sends key commits; `Predictor`, `GlideDecoder`, `WordList`, `UserDictionary`, and `MultilingualPredictor` provide suggestions; settings use encrypted preferences; clipboard/snippets/user dictionary/analytics/per-app/theme data currently use separate local stores.

## Competitive Landscape

- FlorisBoard: strong open keyboard architecture, layout/editor ambitions, theme depth, and active issue queue. Learn from its separation of keyboard logic, UI, language assets, and user-facing customization. Avoid long-running rewrites that delay core typing stability.
- HeliBoard: practical privacy-focused Android keyboard with current community demand around dictionaries, layouts, clipboard, and UX polish. Learn from its incremental maintenance model and user-visible import/export needs. Avoid allowing feature breadth to outrun test coverage.
- AnySoftKeyboard: mature add-on and language-pack ecosystem. Learn from dictionary/language modularity and extension boundaries. Avoid plugin complexity until OpenSwift has stable contracts and security tests.
- AOSP LatinIME and OpenBoard: proven baselines for IME lifecycle, subtypes, suggestions, correction, and password-field behavior. Learn from conservative handling of Android edge cases. Avoid stale visual patterns and under-documented settings.
- FUTO Keyboard: privacy-forward commercial/open hybrid with emphasis on local typing assistance and voice. Learn from the trust posture and visible privacy story. Avoid default network or account assumptions in core typing.
- Simple Keyboard, Unexpected Keyboard, Trime, and Thumb-Key: focused keyboards that win by doing a smaller typing model well. Learn from clear persona fit and specialty layouts. Avoid adding niche modes before current advertised features work end to end.
- Gboard, Microsoft SwiftKey, Grammarly Keyboard, and Fleksy: define user expectations for emoji search/categories, clipboard, multilingual suggestions, voice, themes, and writing assistance. Learn the table stakes. Avoid privacy tradeoffs, opaque data flows, and cloud-required core features.

## Security, Privacy, and Reliability

- Verified: `app/src/main/AndroidManifest.xml` has `android:allowBackup="true"` and no visible backup exclusion rules. Clipboard history, snippets, user dictionary, usage analytics, per-app settings, and custom themes are stored outside `EncryptedSharedPreferences`.
- Verified: `app/src/main/java/com/openswift/keyboard/OpenSwiftIME.kt` calls clipboard capture during input view start and learns typed words without first enforcing sensitive `EditorInfo`/`InputType` modes, `settings.incognitoMode`, or `settings.clipboardEnabled`.
- Verified: `app/src/main/java/com/openswift/keyboard/data/Settings.kt` exposes incognito and clipboard toggles, but the IME path does not consistently enforce them.
- Verified: `app/src/main/java/com/openswift/keyboard/sync/CloudSync.kt` contains placeholder encryption that returns input bytes unchanged. Keep sync hidden until real authenticated encryption, key management, and tests exist.
- Verified: `app/src/main/java/com/openswift/keyboard/plugins/PluginRegistry.kt` is a placeholder. Any future runtime extension path needs explicit permission, signing, API, and failure isolation before exposure.
- Verified: `app/src/main/java/com/openswift/keyboard/speech/VoiceRecognizer.kt` exists, but the manifest has no `RECORD_AUDIO` permission and the IME UI does not expose a complete voice flow.
- Verified: `app/src/main/java/com/openswift/keyboard/snippets/SnippetManager.kt` supports storage, but settings UI creation is incomplete and IME expansion currently checks only single digit keys.
- Likely: user dictionary persistence can grow without cap and prediction does not surface learned-only words as first-class candidates. This weakens the privacy/local-learning value proposition.

## Architecture Assessment

- Create a single typed-data privacy boundary around clipboard, snippets, user dictionary, analytics, per-app settings, and themes. It should decide encryption, backup exclusion, export/import, retention limits, and deletion behavior in one place.
- Add an IME privacy state machine derived from `EditorInfo`, `InputType`, app package, user incognito mode, and settings. `OpenSwiftIME` should consult it before prediction, learning, clipboard capture, snippet expansion, voice, and analytics.
- Split prediction into indexed candidate generation and scoring. Current prefix scanning over `WordList.words` is simple and readable, but it will not scale to larger language packs and does not cleanly merge learned-only words.
- Add regression tests for `Predictor`, `GlideDecoder`, `UserDictionary`, `MultilingualPredictor`, snippet expansion, sensitive input handling, and settings persistence. Current tracked tests only cover language metadata.
- Add UI-level tests or screenshot checks for suggestion taps, clipboard panel reachability, snippets CRUD, reduced motion, high contrast, number row, and per-app settings.
- Keep v0.4 language detection offline. Android subtypes, current input context, dictionary confidence, and optional on-device language ID are better fits than any network-backed detection.
- Use Unicode TR51 and CLDR annotations for emoji categories/search rather than a hand-maintained emoji list.
- Review dependency weight and stability before adding new libraries. `material-icons-extended` can inflate APK size, and `androidx.security:security-crypto:1.1.0-alpha06` should be revisited before relying on it for all typed data.

## Rejected Ideas

- Default cloud sync or telemetry: conflicts with the local-first trust promise and should stay absent unless the user explicitly opts in and the data path is encrypted and testable.
- GIF/sticker marketplace: high permission, network, moderation, and APK-size cost with little benefit to the current privacy-first keyboard goal.
- Online language detection or writing services: poor fit for sensitive keyboard input and no-network release gates.
- Broad plugin marketplace in v0.4: too much security surface before plugin signing, capability limits, and crash isolation are defined.
- Handwriting, Morse, steno, and hardware keyboard focus before core IME hardening: useful later, but current research favors fixing privacy, snippets, prediction, testing, and language/emoji foundations first.

## Sources

- OpenSwift: https://github.com/SysAdminDoc/OpenSwift
- FlorisBoard: https://github.com/florisboard/florisboard
- HeliBoard: https://github.com/Helium314/HeliBoard
- AnySoftKeyboard: https://github.com/AnySoftKeyboard/AnySoftKeyboard
- OpenBoard: https://github.com/openboard-team/openboard
- AOSP LatinIME: https://android.googlesource.com/platform/packages/inputmethods/LatinIME/
- FUTO Keyboard: https://github.com/FUTO-org/android-keyboard
- Simple Keyboard: https://github.com/SimpleMobileTools/Simple-Keyboard
- Unexpected Keyboard: https://github.com/Julow/Unexpected-Keyboard
- Trime: https://github.com/osfans/trime
- Thumb-Key: https://github.com/dessalines/thumb-key
- Gboard Help: https://support.google.com/gboard/
- Microsoft SwiftKey: https://www.microsoft.com/en-us/swiftkey
- Grammarly Keyboard: https://www.grammarly.com/keyboard
- Fleksy: https://www.fleksy.com/
- Android input methods: https://developer.android.com/develop/ui/views/touch-and-input/creating-input-method
- `InputMethodService`: https://developer.android.com/reference/android/inputmethodservice/InputMethodService
- `EditorInfo`: https://developer.android.com/reference/android/view/inputmethod/EditorInfo
- `InputType`: https://developer.android.com/reference/android/text/InputType
- Android backup: https://developer.android.com/identity/data/autobackup
- Android sensitive clipboard content: https://developer.android.com/develop/ui/views/touch-and-input/copy-paste#SensitiveContent
- Unicode TR51 emoji: https://unicode.org/reports/tr51/
- Unicode CLDR: https://cldr.unicode.org/
- ML Kit language identification: https://developers.google.com/ml-kit/language/identification/android
- AndroidX Security releases: https://developer.android.com/jetpack/androidx/releases/security
- Compose BOM releases: https://developer.android.com/jetpack/androidx/releases/compose-bom
- Android Gradle Plugin releases: https://developer.android.com/build/releases/gradle-plugin
- Google Tink releases: https://github.com/google/tink/releases
- Security Crypto artifact history: https://mvnrepository.com/artifact/androidx.security/security-crypto
- Android FOSS app index: https://github.com/offa/android-foss

## Open Questions

- None blocking. The next pass should validate APK size, runtime latency, and exact behavior on a device or emulator after the privacy and snippet fixes land.
