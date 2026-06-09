# OpenSwift User Guide

A quick reference for power users.

## Keyboard Basics

| Action | Effect |
|--------|--------|
| **Tap key** | Type character |
| **Swipe keys** | Glide typing (decode word) |
| **Long-press vowel** | Show accent variants |
| **Tap suggestion pill** | Insert suggestion |
| **Tap ?123** | Switch to symbols/numbers |
| **Tap ABC** | Return to letters |
| **Tap ⇧ (shift)** | Toggle caps (one letter or lock) |
| **Tap ⌫ (backspace)** | Delete previous character |
| **Tap ⏎ (enter)** | Send newline; auto-correct word |
| **Tap space** | Insert space; auto-correct previous word |

## Glide Typing Tips

- **Smooth is fast** — Slow, deliberate swipes decode better
- **Longer words = longer swipes** — Swipe all the way across for multi-letter words
- **Curves are OK** — Glide decoder finds anchors via direction changes, not exact key hits
- **Threshold**: Swipe must last ≥80ms to activate (short taps are single keys)

## Settings Quick Access

**Settings** → Tabs:

1. **Keyboard**
   - Layout: QWERTY, QWERTZ, AZERTY
   - Glide Typing: on/off
   - Auto-Correct: on/off
   - Auto-Capitalize: on/off
   - Haptic Feedback: on/off
   - Sound Feedback: on/off

2. **Themes**
   - 6 built-in themes
   - Create + edit custom themes
   - Delete custom themes

3. **Snippets**
   - Add (trigger → expansion)
   - List + delete saved
   - Type trigger in any text field; auto-expand on next keystroke

## Accent Popups

Hold down any vowel to see accents:

- **a** → á à â ä ã å æ ā
- **e** → é è ê ë ē ę
- **i** → í ì î ï ī
- **o** → ó ò ô ö õ ø œ ō
- **u** → ú ù û ü ū
- **y** → ý ÿ
- **n** → ñ ń
- **c** → ç ć č
- **s** → ß ś š
- **z** → ž ź ż
- **l** → ł
- **d** → đ

Also works for **s**, **n**, **z**, **l**, **d**, **c**.

## Emoji Grid

Tap the **😊 emoji icon** to open a grid of 60 emoji. Tap any to insert.

**Return to keyboard**: Tap outside grid or press back.

## Clipboard History

*(Feature available; UI in development)*

Recent clipboard items are automatically tracked. Future versions will include a slide-up panel in the keyboard.

## Snippet Manager

### Create a Snippet

1. Open **Settings → Snippets**
2. Type **Trigger** (e.g., "omw") and **Expansion** (e.g., "on my way")
3. Tap **Add**

### Use a Snippet

1. Type the trigger in any text field
2. Press **space**, **period**, or any punctuation
3. The trigger is replaced with the expansion

### Example Snippets

| Trigger | Expansion |
|---------|-----------|
| omw | on my way |
| ty | thank you |
| np | no problem |
| lol | laugh out loud |
| btw | by the way |
| fyi | for your information |
| asap | as soon as possible |
| ttyl | talk to you later |

## Custom Themes

### Create a Custom Theme

1. Settings → **Themes**
2. Pick a **Built-in** theme as base (e.g., "AMOLED Black")
3. Tap **Edit** (coming in v0.2)
4. Adjust colors: background, keys, text, accents, suggestions
5. Save

### Edit a Custom Theme

1. Settings → **Themes** → Custom section
2. Tap a custom theme
3. Adjust colors
4. Save

### Delete a Custom Theme

1. Settings → **Themes** → Custom section
2. Tap the **×** icon next to the theme
3. Confirm

## Switching Languages/Layouts

Currently ships with **English (3500 words)** and 3 layouts:

- **QWERTY** (standard US/UK)
- **QWERTZ** (German, Central European)
- **AZERTY** (French, Belgian)

To switch:

1. Settings → **Keyboard → Layout**
2. Select your layout
3. Tap back; next keyboard uses the new layout

## Auto-Correct

- **Enabled by default**
- Type a misspelled word, press **space**
- OpenSwift suggests the closest dictionary match
- If wrong, just re-type and press space again (learns new word)

### Disable Auto-Correct

1. Settings → **Keyboard**
2. Toggle **Auto-Correct** off

## Auto-Capitalize

- **Enabled by default**
- First letter after sentence-ending punctuation (. ! ?) is auto-capitalized
- First letter of message is auto-capitalized

### Disable

1. Settings → **Keyboard**
2. Toggle **Auto-Capitalize** off

## Learning & Customization

### How OpenSwift Learns

- Every word you type (on space/enter) is recorded
- Bigram patterns (which words follow which) are tracked
- Over time, suggestions become more personal
- **No data leaves your device**

### Reset Learning

*(Coming in v0.2)*

Settings → **Privacy** → **Clear User Dictionary**

## Haptic Feedback

By default, each key press vibrates (20ms).

### Adjust or Disable

1. Settings → **Keyboard**
2. Toggle **Haptic Feedback**

## Sound Feedback

Optional audio on key press.

### Enable

1. Settings → **Keyboard**
2. Toggle **Sound Feedback** on

## Accessibility (TalkBack)

OpenSwift supports Android's TalkBack screen reader:

- Key names are announced
- Suggestions are read aloud
- Full keyboard navigation support

Requires:
1. **Settings → Accessibility → TalkBack** → on
2. OpenSwift will detect and announce

## Voice Input

*(Coming in v0.2)*

Tap **🎤** to start voice recognition. Speak; partial and final results appear as suggestions.

## Per-App Settings

*(Coming in v0.2)*

Customize keyboard behavior per app:

- **Games**: Disable glide (tap-only mode)
- **Email**: Adjust key height for precision
- **Chat**: Enable sound feedback

Example:

1. Open a gaming app's text field
2. Settings → **Per-App Settings** → Select game
3. Toggle **Disable Glide**
4. Return to game; glide is off in that app only

## Troubleshooting

### Keyboard is slow to appear

- First load (< 1 second) loads dictionary
- Subsequent loads are instant
- If consistently slow, restart device

### Glide not decoding words

- Ensure **Glide Typing** is enabled (Settings → Keyboard)
- Swipe must last ≥80ms
- Ensure you swipe through actual keys (not gaps)
- Try shorter words first (e.g., "hello" is easier than "extraordinary")

### Wrong word decoded

- Your swipe path skipped keys or hit wrong ones
- Decoded the closest match; try again or use tap-typing
- If it's a dictionary word, it should eventually decode correctly

### Suggestions aren't appearing

- Type ≥1 character (suggestions need prefix)
- Tap another key for prediction to trigger
- Rare words may not be in dictionary (but learned words appear after 1 usage)

### Snippet not expanding

- Trigger must match exactly (case-sensitive)
- Trigger only expands on **space** or **punctuation**, not after regular keys
- Check saved snippets to verify trigger

### Theme doesn't apply

- Save before exiting Settings
- Close and reopen keyboard
- If custom theme, ensure it was saved successfully

### Clipboard history not visible

- Feature is built-in but UI is in development (v0.2)
- Items are tracked automatically in background

---

# Example Snippets & Configurations

## Power User Snippets Pack

Copy these into **Settings → Snippets** for instant productivity:

### Communication
- `omw` → `on my way`
- `ty` → `thank you`
- `np` → `no problem`
- `lmk` → `let me know`
- `fyi` → `for your information`
- `btw` → `by the way`
- `imo` → `in my opinion`
- `afaik` → `as far as I know`
- `asap` → `as soon as possible`
- `ttyl` → `talk to you later`

### Social Media
- `rt` → `retweet`
- `dm` → `direct message`
- `insta` → `Instagram`
- `tiktok` → `TikTok`
- `yt` → `YouTube`

### Technical
- `localhost` → `http://localhost:3000`
- `git` → `git status`
- `cmd` → `command`
- `api` → `API endpoint`
- `db` → `database`

### Email
- `sig` → `Best regards,\nOpenSwift Team`
- `follow` → `Please let me know if you have any questions.`
- `meeting` → `Let's schedule a meeting to discuss this further.`

### Accessibility
- `slower` → `Reduce typing speed`
- `bigger` → `Increase key height`

---

## Custom Theme Color Palettes

### Ice Blue
```
Background: #0F1419
Key Bg: #1A202C
Key Modifier: #0A0E16
Text: #E8F4F8
Accent: #00D9FF
Suggestion Bg: #0A0E16
Suggestion Text: #C0E7F0
Gesture Trail: #00D9FF
```

### Sunset
```
Background: #2D1B00
Key Bg: #4A2C1A
Key Modifier: #1A0F00
Text: #FFE8CC
Accent: #FF6B35
Suggestion Bg: #1A0F00
Suggestion Text: #FFD9B3
Gesture Trail: #FF6B35
```

### Forest
```
Background: #0D2818
Key Bg: #1B4F2C
Key Modifier: #081810
Text: #C8F7DC
Accent: #2ECC71
Suggestion Bg: #081810
Suggestion Text: #A0E8C1
Gesture Trail: #2ECC71
```

### Cyberpunk
```
Background: #0A0E27
Key Bg: #1A1F3A
Key Modifier: #050712
Text: #00FF88
Accent: #FF00FF
Suggestion Bg: #0F1429
Suggestion Text: #00FFDD
Gesture Trail: #FF00FF
```

---

## Per-App Configuration Examples

### Gaming (e.g., Genshin Impact)
```
- Disable glide: yes (tap-only mode for precision)
- Key height: 48 dp (smaller, less accidental touches)
- Sound feedback: off (minimize distraction)
```

### Email (e.g., Gmail)
```
- Disable glide: no (speed typing)
- Key height: 64 dp (larger for accuracy)
- Sound feedback: off (professional silence)
- Theme: Material Light (bright background better for outdoor reading)
```

### Messaging (e.g., WhatsApp)
```
- Disable glide: no (fastest typing)
- Key height: 56 dp (default)
- Sound feedback: on (tactile satisfaction)
- Theme: AMOLED Black (dark, less battery drain)
```

### Accessibility (e.g., Magnifier App)
```
- Disable glide: yes (large single-key presses only)
- Key height: 72 dp (maximum size)
- Sound feedback: on (audible confirmation)
- Theme: High-contrast (custom, white text on black)
```

---

## Keyboard Layout Comparison

### QWERTY (English/US)
**Best for**: General use, English typing
```
q w e r t y u i o p
 a s d f g h j k l
  z x c v b n m
```

### QWERTZ (German/Central Europe)
**Best for**: German, Czech, Hungarian
- Swapped Y and Z
- Quick access to common German umlauts on long-press
```
q w e r t z u i o p
 a s d f g h j k l
  y x c v b n m
```

### AZERTY (French/Belgian)
**Best for**: French, Belgian French
- Completely different layout (optimized for French)
- Numbers on top (shift for numbers)
```
a z e r t y u i o p
 q s d f g h j k l m
  w x c v b n
```

---

## Snippets for Different Professions

### Developer
```
const → const ${} = {}
fn → function() {}
try → try {} catch (e) {}
api → // API endpoint
log → console.log()
import → import {} from ''
export → export default
await → await fetch('')
```

### Writer/Journalist
```
editor → @Editor
source → According to sources,
quote → "Quote here"
para → [New paragraph]
cite → See: [source]
draft → [DRAFT - needs review]
```

### Medical Professional
```
pt → patient
hx → history
sx → symptoms
dx → diagnosis
tx → treatment
rx → prescription
labs → laboratory results
```

### Customer Service
```
sorry → I apologize for the inconvenience.
help → How can I help you?
thanks → Thank you for your patience.
ticket → Ticket number:
escalate → Let me escalate this to our supervisor.
refund → Refund request pending.
```

---

## Accessibility Quick-Setup

For users with motor impairments:

1. **Disable Glide** (Settings → Keyboard → toggle off)
   - Single-tap only (more reliable than swiping)

2. **Increase Key Height** (Settings → Keyboard → Key Height)
   - Set to 72 dp (maximum)
   - Larger targets = easier tapping

3. **Enable Haptic Feedback** (Settings → Keyboard → toggle on)
   - Tactile confirmation on each press

4. **Enable Sound Feedback** (Settings → Keyboard → toggle on)
   - Audible confirmation (especially helpful with vision impairments)

5. **TalkBack** (Settings → Accessibility → TalkBack → on)
   - Screen reader support for full keyboard navigation

6. **Add Common Snippets**
   ```
   Snippet: "help" → "I need assistance"
   Snippet: "pain" → "I'm experiencing pain"
   Snippet: "stop" → "Please stop"
   Snippet: "wait" → "One moment, please"
   ```

---

## Pro Tips & Recipes

### Speed Typing Mode
1. Disable auto-correct (Settings → Keyboard → toggle off)
2. Use glide typing exclusively (swipe through entire words)
3. Select QWERTY layout (most familiar)
4. Use AMOLED theme (least distracting)

### Precision Typing Mode
1. Disable glide (Settings → Keyboard → toggle off)
2. Increase key height (Settings → Keyboard → 64+ dp)
3. Enable haptic feedback (tap confirmation)
4. Select Material Light theme (maximum contrast)

### Multilingual (Coming v0.3)
1. Add snippets in target language (e.g., common French words)
2. Manually train the user dictionary (type a few sentences in French)
3. Switch layout if target language has dedicated layout (QWERTZ for German)

### Emergency Contacts
Create snippets for common emergencies:
```
sos → "EMERGENCY: Need immediate help"
911 → "Calling emergency services"
police → "Police assistance needed"
```

---

## Importing Snippets (Script)

For developers who want to bulk-import snippets:

```bash
#!/bin/bash
# snippets.sh - Import snippets via adb

SNIPPETS=(
  "omw:on my way"
  "ty:thank you"
  "np:no problem"
  "lmk:let me know"
)

for snippet in "${SNIPPETS[@]}"; do
  trigger="${snippet%%:*}"
  expansion="${snippet##*:}"
  echo "Adding: $trigger -> $expansion"
  # Note: Direct SharedPreferences write requires root
  # In v0.2, UI import feature will be added
done
```

---

This guide is community-maintained. Submit additions via GitHub issues!
