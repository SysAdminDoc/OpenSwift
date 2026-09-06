# OpenSwift product and engineering review

## Current position

OpenSwift has a credible place among privacy-focused Android keyboards. Its clearest advantage is easy to verify: the published app requests no network permission, while prediction, glide decoding, language detection, snippets, and learning run on the device.

The product is best suited to Android users who want a transparent keyboard they can sideload, inspect, and customize. It is not trying to match the cloud language models or distribution reach of the largest commercial keyboards.

## Verified release claims

- The Android manifest does not request `INTERNET`.
- English, German, French, Spanish, and Italian dictionaries are bundled with the app.
- Password and other sensitive fields suppress suggestions, learning, glide decoding, snippets, analytics, and clipboard capture.
- Clipboard history is opt-in, ignores Android-marked sensitive clips, and keeps no more than 25 unique items.
- Typed-data stores migrate into encrypted preferences. Android backup and device transfer are disabled.
- JSON backups are validated before import. Passphrase-protected snapshots use an authenticated AES-256-GCM envelope.
- The published extension and transport entry points are compile-time disabled. OpenSwift does not load third-party APKs.

These claims are enforced by unit tests and manifest contract tests. The 0.3.6 release was also exercised as an installed keyboard on an isolated Android 15 emulator.

## Product review

OpenSwift's strongest story is control. A user can see what the keyboard stores, clear individual data groups, export a readable backup, or create an encrypted snapshot. The privacy dashboard makes those boundaries visible instead of burying them in documentation.

The customization model is another useful distinction. Ten built-in themes cover dark, light, and high-contrast use. Validated JSON packages can add layouts and themes without introducing executable plugins.

The main limitation is language depth. The bundled dictionaries favor common words and are much smaller than commercial keyboard language models. Distribution is also limited to GitHub releases, so installation requires Android sideloading. The speech-recognition adapter is present in the codebase but is not connected to a key in the published build.

## Engineering assessment

The current privacy boundary is consistent across settings, clipboard data, snippets, learned words, analytics, per-app profiles, emoji history, and custom themes. Import validation happens before writes, destructive replacement requires confirmation, and failed preference groups roll back.

The typing engine uses indexed prefix lookup, fuzzy correction, local bigram learning, and a dedicated glide decoder. This design is understandable and testable. Larger dictionaries or an optional neural model would need new latency and memory measurements before release.

The custom keyboard remains a drawing-based Android view, while the management app uses Jetpack Compose. That split is reasonable for direct touch handling, but every visual change should continue to be checked on a real IME surface rather than only in Compose previews.

## Recommended next work

1. Expand dictionary quality with measured startup, memory, and suggestion-latency budgets.
2. Add end-to-end IME tests for editor switching, private fields, gesture input, and suggestion replacement.
3. Test TalkBack and high-contrast behavior on physical devices from more than one Android vendor.
4. Decide whether voice input belongs in the product. If it does, ship a complete permission and failure flow. Otherwise remove the dormant adapter.
5. Prepare store-ready listing material only after a repeatable signed release and upgrade test passes on physical hardware.

## Release conclusion

Version 0.3.6 is ready for GitHub distribution. The new presentation accurately reflects the installed product, the privacy language is supported by code and tests, and the README states the current limits without implying unfinished features are available.
