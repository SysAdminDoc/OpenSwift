# OpenSwift Completed Work

Summary of shipped roadmap items. Full release notes remain in
[CHANGELOG.md](CHANGELOG.md).

Last consolidated: 2026-06-01.

## v0.3.0 - Accessibility And Privacy

- Reduced Motion accessibility toggle.
- High Contrast WCAG AAA theme.
- Privacy Dashboard for clipboard, dictionary, and local data deletion.
- Four additional themes: Nord, Dracula, Tokyo Night, High Contrast.
- Settings reorganized into Appearance, Keyboard, Typing, Feedback,
  Accessibility, and Advanced sections.
- Navigation updated with Home, Settings, Privacy, and About tabs.
- Public accessors added for dictionary stats/reset flows.

## v0.2.0 - Core Polish And Security

- Animated key press ripple feedback.
- Glide trail gradient fade.
- Suggestion pills with preview text.
- Encrypted SharedPreferences for user data.
- Release signing configuration.
- Theme resume crash investigated and guarded.
- Animation cleanup for ripple/trail rendering.

## v0.1.0 - IME Foundation

- `InputMethodService` integration.
- QWERTY, QWERTZ, and AZERTY layouts with long-press accents.
- Glide typing and Damerau-Levenshtein-based decoding.
- Word prediction, auto-correct, user learning, and bigram dictionary.
- Six initial themes, emoji grid, clipboard history, snippets, number row,
  settings UI, custom themes, voice recognition, local analytics, TalkBack,
  per-app settings, haptics, and sound feedback.
- Gradle build, release workflow, MIT license, dictionary resource, and initial
  documentation set.
