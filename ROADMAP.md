# OpenSwift Roadmap

Active roadmap for forward-looking work. Completed work lives in
[COMPLETED.md](COMPLETED.md), and research/dependency findings live in
[RESEARCH_REPORT.md](RESEARCH_REPORT.md).

Current release line: v0.3.0. Last consolidated: 2026-06-01.

## Current State

- v0.1.0 shipped the production-ready IME foundation.
- v0.2.0 shipped key feedback polish, suggestion pills, encrypted
  SharedPreferences, release-signing config, and performance fixes.
- v0.3.0 shipped reduced motion, high contrast, a privacy dashboard, Settings
  reorganization, and four additional themes.
- The previous v0.2 task/decomposition package is archived under
  [docs/archive/roadmap](docs/archive/roadmap/).

## Active Queue

| Priority | Milestone | Work | Exit criteria |
|---|---|---|---|
| P0 | v0.4 | Multilingual foundation | German, French, and Spanish word lists can be selected manually, with language-specific layouts and no regression to English prediction. |
| P0 | v0.4 | Language detection | Current input context can switch suggestion dictionaries or prompt the user to switch without sending text off-device. |
| P0 | v0.4 | Emoji expansion | Emoji picker supports categories, recents, favorites, and search without first-open lag. |
| P1 | v0.4 | User dictionary portability | Export/import learned words, snippets, and custom themes with clear overwrite/merge behavior. |
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
  [RESEARCH_REPORT.md](RESEARCH_REPORT.md) or a follow-up research note.

## Parking Lot

- Handwriting recognition.
- Morse code input.
- Steno-style shorthand.
- Web dashboard.
- Password-manager integration.
- Biometric unlock for sensitive snippets.
- Hardware keyboard routing.
