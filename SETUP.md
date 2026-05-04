# Setup Guide

## Installation

### From Release APK
1. Download the latest `.apk` from [Releases](https://github.com/SysAdminDoc/OpenSwift/releases)
2. On your Android device, enable **Settings → Security → Unknown Sources** (or **Install Unknown Apps**)
3. Open the downloaded APK and tap **Install**
4. Grant any prompted permissions

### From Source (Build Yourself)
```bash
git clone https://github.com/SysAdminDoc/OpenSwift.git
cd OpenSwift
./gradlew assembleRelease
adb install app/build/outputs/apk/release/app-release.apk
```

## Enable as Input Method

1. Open **Settings → Languages & input → On-screen keyboard** (or **Virtual keyboard** on some devices)
2. Tap **Manage on-screen keyboards**
3. Toggle **OpenSwift** to enable it
4. Go back and select **Default input method**
5. Choose **OpenSwift**

On some Android versions:
- Settings → System → Languages & input → Virtual keyboard → Manage keyboards

## First Use

- Tap any text field (email, messaging app, etc.) — keyboard should appear
- The **QWERTY** layout loads by default
- **Glide typing** is enabled by default — try swiping across keys
- Tap **Settings** (gear icon) to customize themes, layouts, and features

## Common Tasks

### Switch Keyboard Layout
1. Open Settings
2. Select **Keyboard → Layout**
3. Choose QWERTY, QWERTZ, or AZERTY
4. Tap back; next keyboard session uses the new layout

### Change Theme
1. Open Settings → **Themes** tab
2. Select from 6 built-in themes or create a custom one
3. Custom themes automatically apply to next input

### Add Text Snippet
1. Settings → **Snippets** tab
2. Enter trigger (e.g., "omw") and expansion (e.g., "on my way")
3. Tap **Add**
4. In any text field, type the trigger; it auto-expands on next keystroke

### Access Clipboard History
*(Feature built-in; long-press the clipboard icon in future releases)*
- Recently copied items are tracked automatically
- Currently accessible via IME internal state (future: UI panel)

### Enable/Disable Glide Typing
1. Settings → **Keyboard** tab
2. Toggle **Glide Typing**
3. Changes apply immediately

## Tips & Tricks

**Faster typing with glide** — Swipe through keys smoothly; longer swipes = higher accuracy
**Auto-correct on space** — Press space after a misspelled word; it auto-corrects
**Suggestion pills** — Tap any suggestion to insert it (or continue typing)
**Long-press for accents** — Hold down a vowel to see accent variants (á, à, â, etc.)
**Number row** — Tap **?123** to access numbers and symbols

## Troubleshooting

### Keyboard doesn't appear
- Ensure OpenSwift is enabled in Settings → Languages & input
- Select it as the default input method
- Try restarting the app or device

### Glide typing not working
- Check that **Glide Typing** is enabled in Settings
- Ensure you swipe smoothly (at least 80ms) across multiple keys
- Short taps are treated as single key presses (correct behavior)

### Suggestions aren't appearing
- Type at least 1 character to trigger suggestions
- Ensure the word is in the dictionary or has been typed before
- Check that auto-correct is enabled in Settings

### Custom theme not applying
- Save the theme in Settings → Themes
- Close and reopen the keyboard
- Try switching themes to refresh

### Snippets not expanding
- Trigger must be typed exactly as configured (case-sensitive)
- Expansion occurs when you press space or a punctuation key
- Check the saved snippets list to verify the trigger

## Performance Notes

- **First load**: Loads 3500-word dictionary + user history (< 1 second typically)
- **Memory**: ~8 MB typical footprint
- **Battery**: Minimal drain; local-only prediction (no cloud calls)
- **Storage**: ~5 MB APK + ~2 MB user data (clips, snippets, themes, learned words)

## Privacy

✓ All data is stored **locally** on your device  
✓ No analytics, no crash reporting, no ads  
✓ No internet required  
✓ Source code is public (MIT license)  

---

**Need help?** Open an [issue](https://github.com/SysAdminDoc/OpenSwift/issues) on GitHub.
