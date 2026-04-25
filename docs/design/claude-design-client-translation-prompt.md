# Claude Design Prompt: Translate Stitch Client Direction To Compose Handoff

You are translating the selected Google Stitch design direction for a native Android app named M Twitch.

The app is a complete third-party Twitch client for personal use. The first implementation is a full-client foundation with a VOD-first usable path. It includes:
- Home / Resume
- Channel VOD List
- VOD Player
- Live Player placeholder
- Chat panel and VOD replay panel placeholders
- Chat Plugin Settings
- Settings
- Saved watch progress
- Default local chat plugin settings and diagnostics

The MVP includes contracts and UI placeholders for:
- Live playback
- Live chat
- VOD chat replay
- Third-party emotes
- Local chat plugins

The MVP excludes production implementations of:
- Real live playback
- Real Twitch chat transport
- VOD chat replay fetching
- Third-party emote network integrations
- Downloads
- External plugin loading
- Public store release polish

Use the provided Stitch output as the visual source of truth. Produce an implementation handoff for Kotlin + Jetpack Compose.

The handoff must include:
- Design tokens: colors, typography, spacing, elevation, shape radius
- Component list: app shell, top bars, list rows, VOD cards, player controls, scrubber, buttons, settings rows, error states
- Screen-by-screen layout rules
- State table for loading, empty, auth-needed, offline, playback unavailable, and retrying
- State table for chat disconnected, chat reconnecting, chat rate-limited, plugin failed, and emote provider unavailable
- Motion rules with durations and easing
- Player overlay behavior in portrait and landscape
- Chat panel behavior over or beside video on phone screens
- Plugin settings and diagnostics behavior
- Accessibility notes for touch targets, contrast, and content descriptions
- A concise DESIGN.md that engineering can commit to the repository

Avoid web-specific implementation details. Do not generate a WebView app. Optimize the handoff for native Compose.
