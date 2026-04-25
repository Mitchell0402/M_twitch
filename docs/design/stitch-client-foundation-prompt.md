# Google Stitch Prompt: M Twitch Full Client Foundation

Design the first prototype for a native Android Twitch client for personal use. The long-term app is a complete third-party client with live playback, VOD playback, live chat, VOD chat replay, third-party emotes, and local chat plugins. The first version should be VOD-first for the main usable path, but the shell must visibly account for live, chat, replay, plugin settings, and diagnostics.

Primary user goal:
- Find a Twitch channel.
- Browse recent archived broadcasts.
- Open a VOD.
- Watch, pause, scrub, fullscreen, and resume later.
- See where live playback and chat will live in the same app.
- Configure the default local chat plugin even while real chat transport is not implemented.

Design constraints:
- Android phone first.
- Native Jetpack Compose implementation.
- Performance-sensitive video player screen.
- Avoid heavy decorative animation around video playback.
- The first screen should be useful immediately, not a marketing page.
- Chat, VOD chat replay, BTTV, FFZ, 7TV emotes, and chat plugins are later full implementations of the same app, not separate products.
- The default chat plugin is built in locally and enabled by default.

Create 2-3 visual directions and include these screens:
- Home / Resume
- Channel VOD List
- VOD Player portrait
- VOD Player landscape/fullscreen
- Live Player placeholder
- Chat panel / replay panel placeholder
- Chat Plugin Settings
- Settings

For each screen include:
- Loading state
- Empty state
- Recoverable error state
- Offline or auth-needed state where relevant
- Rate-limited or reconnecting state where chat is present

Pay special attention to:
- Player overlay layout
- Scrubber ergonomics
- Recent VOD progress indicators
- Chat panel density and readability
- Emote-heavy message rows without overwhelming playback
- Plugin settings and diagnostics
- One-handed phone use
- Smooth but restrained transitions
- A design-system summary with colors, type scale, spacing, controls, and motion rules

Output a DESIGN.md-style summary after the visual exploration.
