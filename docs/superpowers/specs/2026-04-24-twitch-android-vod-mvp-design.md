# Twitch Android VOD MVP Design

Date: 2026-04-24

## Summary

Build a personal-use Android Twitch client. The long-term goal is a complete
native Android client for watching live streams, VODs, chat, chat replay, and
third-party emotes. The first prototype is a VOD-first MVP focused on
discovering Twitch VODs, playing them smoothly, scrubbing within playback,
handling fullscreen/orientation, and remembering watch progress. Live chat, VOD
chat replay, and third-party emotes are intentionally deferred out of the first
prototype, not removed from the product vision.

The app should be native Android, implemented with Kotlin, Jetpack Compose, and
Media3. Google Stitch is the primary frontend design exploration tool. Claude
Design is reserved for translating selected Stitch designs into implementation
handoff material so its limited quota is spent only at high-leverage checkpoints.

## Long-Term Product Direction

The project should grow into a complete Android Twitch client for personal use.
The VOD-first MVP is the first usable prototype because VOD playback and
scrubbing are the clearest missing capabilities in the third-party clients the
user has tried. Later phases should expand the same native app into live
playback, live chat, VOD chat replay, BTTV/FFZ/7TV emotes, and richer player
tools.

## MVP-A Goals

- Let the user search for or open a Twitch channel.
- Show a usable VOD list for the selected channel.
- Play selected VODs with native Android playback.
- Support pause, resume, scrub, and fullscreen orientation behavior.
- Remember recent VODs and per-VOD watch position locally.
- Provide polished loading, empty, and error states.
- Establish a design workflow where Stitch owns exploration and Claude Design
  owns translation/handoff.

## Non-Goals For MVP-A

- Live stream playback.
- Live chat.
- VOD chat replay.
- BTTV, FFZ, or 7TV emotes.
- Downloads or offline viewing.
- Public Play Store release readiness.
- Replacing all official Twitch app behavior.

## Product Shape

The prototype MVP should feel like a focused VOD player rather than the final
complete Twitch client. The first screen should put the user directly into
finding or resuming content, not a marketing-style landing page.

Core screens:

1. Home / Resume
   - Recent VODs.
   - Search or channel entry.
   - Last watched channel shortcut.
2. Channel VOD List
   - Channel identity.
   - VOD thumbnails, titles, durations, publish dates, and progress markers.
   - Loading, empty, and recoverable error states.
3. VOD Player
   - Media3 video surface.
   - Overlay controls for play/pause, scrubber, elapsed/remaining time,
     fullscreen, quality, and basic settings.
   - Resume prompt or automatic resume behavior.
4. Settings
   - Login/account state.
   - Playback defaults.
   - Storage/history controls.
   - Diagnostics/version information.

## Design Platform Workflow

### Google Stitch Responsibilities

Use Stitch for broad, low-cost design exploration:

- 2-3 visual directions for the app shell.
- Navigation structure for Home, Channel VOD List, VOD Player, and Settings.
- Screen states for loading, empty, error, offline, and unauthorized cases.
- Player overlay layout options.
- Motion direction for overlays, transitions, and progress updates.
- Initial `DESIGN.md` style guide with color, typography, spacing, components,
  and motion notes.

Stitch should stay responsible until a screen is roughly 70-80% decided.

### Claude Design Responsibilities

Use Claude Design only after a Stitch direction has been selected:

- Translate selected Stitch screens into a cleaner design system.
- Produce a Compose-ready handoff with components, states, and interaction
  behavior.
- Produce a lightweight HTML prototype or handoff bundle when it helps clarify
  motion or layout.
- Resolve ambiguity before Android implementation begins.

Claude Design should not be used for early broad exploration unless Stitch cannot
express a specific interaction clearly enough.

### Android Implementation Responsibilities

The final app should not be a WebView wrapper of Stitch or Claude output. Use the
design artifacts as source material, then implement the interface in Jetpack
Compose with native player integration.

## Technical Architecture

Recommended stack:

- Kotlin for Android application code.
- Jetpack Compose for UI.
- Media3 / ExoPlayer for HLS VOD playback.
- Kotlin coroutines and flows for async state.
- Local persistence for watch history and settings.
- Twitch Helix API for official metadata where possible.
- A small isolated playback-token module for any unofficial playback resolution
  behavior that cannot be handled through official API surfaces.

High-level modules:

- `app`: Android entry point, dependency wiring, navigation shell.
- `designsystem`: Compose theme, reusable controls, spacing, typography, motion.
- `feature-home`: resume list, search entry, recent channels.
- `feature-channel`: channel profile and VOD list.
- `feature-player`: VOD playback screen, player controls, orientation handling.
- `data-twitch`: Twitch metadata, auth, channel lookup, VOD list.
- `data-playback`: VOD playback URL resolution and Media3 media item creation.
- `data-local`: watch history, settings, and cached user preferences.

Boundaries should keep Twitch API behavior, playback resolution, and UI state
separate. This matters because Twitch playback internals may change; isolating
them prevents UI code from depending on fragile request details.

## Data Flow

1. User enters a channel name or selects a recent channel.
2. App resolves channel identity through Twitch metadata.
3. App fetches recent VOD metadata for that channel.
4. User selects a VOD.
5. Player feature requests a playable Media3 item from the playback layer.
6. Playback layer resolves HLS source and exposes result or typed error.
7. Player screen starts playback and persists progress periodically.
8. Home and VOD list surfaces read local progress to show resume affordances.

## Playback Behavior

MVP playback requirements:

- Start a VOD from the beginning or saved resume position.
- Allow scrubbing across the full VOD timeline.
- Keep controls responsive while buffering.
- Show buffering and retry states without blocking the whole UI.
- Save progress on pause, navigation away, app background, and periodic interval.
- Support portrait and landscape/fullscreen player layouts.
- Keep player lifecycle tied to Compose/Android lifecycle to avoid leaked
  playback sessions.

Quality selection can begin as automatic quality only. Manual quality can be an
MVP stretch goal if the HLS variants are available cleanly through Media3.

## Error Handling

Use user-readable, recoverable errors:

- Not logged in: prompt to authenticate.
- Channel not found: keep search entry visible and explain that the channel was
  not found.
- No VODs: show empty state with channel context.
- Playback source unavailable: offer retry and explain that the VOD may be
  restricted, deleted, or temporarily unavailable.
- Network timeout: offer retry and keep previously loaded data if available.
- Playback-token failure: keep details in diagnostics/logs, show a simple
  playback unavailable message to the user.

Do not expose raw GraphQL, token, or HTTP implementation details in normal UI.

## Testing And Verification

Design verification:

- Stitch output includes all MVP screens and major states.
- Claude Design handoff resolves component names, states, and motion rules.
- Android UI implementation is compared against the handoff before feature signoff.

Engineering verification:

- Unit tests for data mapping, watch-position persistence, and typed error
  handling.
- Integration tests for repository behavior with mocked Twitch responses.
- UI tests for navigation, loading/error states, and player screen state changes
  where practical.
- Manual device checks for playback start, scrub, pause, resume, orientation
  changes, app background/foreground, and long VOD progress.
- Performance checks for startup time, scrolling VOD list, player overlay
  responsiveness, memory growth during playback, and battery-heavy behavior.

## Milestones

### M0: Design Foundation

- Use Stitch to generate initial visual directions.
- Choose one direction.
- Use Claude Design to translate that direction into a handoff.
- Produce the project `DESIGN.md`.

### M1: Android Foundation

- Create Android project structure.
- Add Compose navigation shell.
- Add app theme based on `DESIGN.md`.
- Add Twitch auth/account state model.
- Add local settings/history storage interfaces.

### M2: VOD Discovery

- Implement channel search/open flow.
- Fetch and display channel VOD list.
- Show thumbnails, titles, durations, publish dates, and progress markers.
- Add loading, empty, and error states.

### M3: VOD Playback

- Resolve playable VOD media item.
- Play with Media3.
- Add play/pause, scrubber, buffering, elapsed/remaining time.
- Add portrait and landscape/fullscreen layouts.

### M4: Personal Usability

- Persist watch progress.
- Add recent VODs and resume behavior.
- Add settings and diagnostic surface.
- Run device QA and performance pass.
- Produce a personal beta APK.

### Later Client Capabilities

These are later phases of the same complete client roadmap after the VOD-first
prototype is stable.

- Live playback.
- Live chat.
- VOD chat replay.
- BTTV/FFZ/7TV emotes.
- Chat-emote rendering performance work.
- Downloader/offline viewing if still desired.

## Risks

- Twitch playback URL/token behavior may change and may require maintenance.
- Official APIs cover metadata better than playback internals and VOD chat
  replay; post-MVP chat replay will likely be more fragile.
- Claude Design quota is limited, so using it too early could block later
  translation checkpoints.
- A highly animated UI can hurt battery and player responsiveness if not kept
  simple around the video surface.

## Implementation Defaults For MVP-A

- Require Twitch OAuth in MVP-A. Use a mobile-safe OAuth flow and keep account
  state visible in Settings.
- Start with automatic quality selection. Manual quality selection is a stretch
  goal only if Media3 exposes variants cleanly without fragile UI work.
- Target Android phones first. Tablet-specific layouts are post-MVP.
- Fetch recent archived broadcasts for the selected channel and show newest
  VODs first. Highlights, uploads, search filters, and sorting controls are
  post-MVP.

## Recommendation

Proceed with MVP-A as scoped here. Keep the first implementation deliberately
small: VOD discovery, VOD playback, scrubbing, fullscreen, and saved progress.
Use Stitch heavily before code, use Claude Design only for selected translation
checkpoints, and defer chat/emotes until playback is stable.
