# Twitch Android Client MVP Design

Date: 2026-04-24

## Summary

Build a personal-use, full native Android Twitch client. The product direction
includes live playback, VOD playback, live chat, VOD chat replay, third-party
emotes, and locally integrated chat plugins. The first MVP should still ship in
small steps, but it must establish the architectural seams for the full client
instead of treating live/chat/plugin work as unrelated future add-ons.

MVP-A is therefore a client foundation plus a VOD-first usable path. It should
let the user discover and play VODs, remember watch progress, and launch a real
Compose app shell that already knows about live, chat, replay, plugin settings,
and diagnostics. The default chat plugin is a built-in local plugin registered
through the same plugin contract future chat integrations will use.

The app should be native Android, implemented with Kotlin, Jetpack Compose, and
Media3. Google Stitch is the primary frontend design exploration tool. Claude
Design is reserved for translating selected Stitch designs into implementation
handoff material so its limited quota is spent only at high-leverage checkpoints.
The baseline `DESIGN.md` is enough to begin M1 engineering; Stitch and Claude
Design should not block the compileable foundation unless a UI decision affects
navigation or core architecture.

## Long-Term Product Direction

The project should grow into a complete Android Twitch client for personal use.
The final app should support:

- Live stream discovery and playback.
- VOD discovery, playback, scrubbing, and resume.
- Live chat reading and sending.
- VOD chat replay synchronized to playback position.
- Third-party emotes, including BTTV, FFZ, and 7TV where their APIs and usage
  rules allow it.
- A local chat plugin system with a default built-in plugin enabled by default.
- Moderation-aware chat presentation where account permissions allow it.
- Diagnostics for playback, chat connection, token, rate-limit, and plugin
  failures.

VOD playback remains the first end-to-end user path because it is easier to make
useful before solving live chat and replay sync. The foundation must still model
the final client from day one so later live/chat/plugin phases do not force a
navigation, state, or data-layer rewrite.

## MVP-A Goals

- Create a native Android app shell for the complete third-party client.
- Allow the first public VOD playback path to work without requiring user OAuth.
- Let the user search for or open a Twitch channel.
- Show a usable VOD list for the selected channel.
- Play selected VODs with native Android playback.
- Support pause, resume, scrub, and fullscreen orientation behavior.
- Remember recent VODs and per-VOD watch position locally.
- Add typed contracts for account state, playback sources, muted/discontinuous
  playback timeline issues, chat sessions, chat messages, emote providers, and
  chat plugins.
- Register a default local chat plugin through the plugin host, even if MVP-A
  only renders fake/sample chat data.
- Provide polished loading, empty, offline, auth-needed, rate-limited, and error
  states.
- Establish a design workflow where Stitch owns exploration and Claude Design
  owns translation/handoff.

## Non-Goals For MVP-A

- Production live stream playback.
- Mandatory user login for public VOD playback.
- Production chat transport for real Twitch rooms.
- Sending chat messages to Twitch.
- VOD chat replay fetching or synchronization.
- Full BTTV, FFZ, or 7TV network integrations.
- Runtime loading of arbitrary third-party code.
- Downloads or offline viewing.
- Public Play Store release readiness.
- Replacing all official Twitch app behavior.

These are non-goals for the first shippable slice, not product exclusions. MVP-A
must include the contracts and visible settings needed to add them cleanly.

## Product Shape

The prototype MVP should feel like the first slice of a complete client, not a
throwaway VOD app. The first screen should put the user directly into finding,
resuming, or opening content, not a marketing-style landing page.

Core screens:

1. Home / Resume
   - Recent VODs.
   - Followed or recent live channels placeholder.
   - Search or channel entry.
   - Last watched channel shortcut.
2. Channel VOD List
   - Channel identity.
   - Live status placeholder.
   - VOD thumbnails, titles, durations, publish dates, and progress markers.
   - Loading, empty, and recoverable error states.
3. VOD Player
   - Media3 video surface.
   - Overlay controls for play/pause, scrubber, elapsed/remaining time,
     fullscreen, quality, and basic settings.
   - Collapsible chat/replay surface placeholder using fake data.
   - Resume prompt or automatic resume behavior.
4. Live Player Placeholder
   - Channel identity and stream status.
   - Disabled or sample playback state until the live plan implements real
     stream resolution.
   - Slot for the same chat surface used by the VOD player.
5. Chat And Plugin Settings
   - Default local chat plugin enabled by default.
   - Plugin enable/disable state.
   - Emote provider placeholders.
   - Diagnostics link for chat connection and plugin failures.
6. Settings
   - Login/account state.
   - Playback defaults.
   - Storage/history controls.
   - Diagnostics/version information.

## Design Platform Workflow

### Google Stitch Responsibilities

Use Stitch for broad, low-cost design exploration:

- 2-3 visual directions for the app shell.
- Navigation structure for Home, Channel, VOD Player, Live Player, Chat/Plugin
  Settings, and Settings.
- Screen states for loading, empty, error, offline, and unauthorized cases.
- Player overlay layout options.
- Chat panel, chat replay panel, and emote-dense message layout options.
- Plugin settings layout for built-in and future local chat plugins.
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
- Clarify how video, chat, plugin settings, and diagnostics share space on
  phone-sized screens.
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
- Media3 / ExoPlayer for HLS VOD and live playback.
- Kotlin coroutines and flows for async state.
- Local persistence for watch history and settings.
- Twitch Helix API for official metadata where possible.
- EventSub/WebSocket and Twitch API surfaces for chat where official APIs cover
  the needed behavior.
- IRC only as a fallback compatibility layer for chat capabilities that cannot
  be handled through the preferred EventSub/API route.
- A small isolated playback-token module for any playback resolution behavior
  that cannot be handled through official API surfaces.
- A local, typed plugin host for chat features. MVP-A plugins are built-in
  Kotlin implementations registered at compile time; arbitrary third-party code
  loading is out of scope until there is a deliberate sandbox/signing design.

High-level modules:

- `app`: Android entry point, dependency wiring, navigation shell.
- `designsystem`: Compose theme, reusable controls, spacing, typography, motion.
- `feature-home`: resume list, search entry, recent channels.
- `feature-channel`: channel profile and VOD list.
- `feature-player`: shared media player surface, controls, orientation handling.
- `feature-live`: live channel playback and live metadata.
- `feature-chat`: live chat surface, input, moderation-aware message rendering.
- `feature-replay`: VOD chat replay timeline and player-position sync.
- `feature-plugins`: chat plugin settings, diagnostics, enable/disable surface.
- `data-twitch`: Twitch metadata, auth, channel lookup, VOD list.
- `data-playback`: VOD/live playback URL resolution and Media3 media item creation.
- `data-chat`: chat session connection, message stream, send-message contract,
  EventSub/API/IRC adapters.
- `data-emotes`: Twitch, BTTV, FFZ, and 7TV emote metadata and cache contracts.
- `data-local`: watch history, settings, plugin configuration, and cached user
  preferences.
- `plugin-chat`: typed plugin API, default local chat plugin, plugin registry,
  plugin diagnostics.

Boundaries should keep Twitch metadata, playback resolution, chat transport,
emote providers, plugin execution, and UI state separate. This matters because
Twitch playback internals, chat API behavior, and third-party emote providers
may change independently. UI code should consume stable typed results rather
than raw HTTP, EventSub, IRC, GraphQL, token, or plugin details.

## Chat Plugin Model

Chat plugins are local integrations that can observe normalized chat events,
decorate messages, provide commands, and expose settings. The default chat
plugin ships with the app and is enabled by default. It should use the same
`ChatPlugin` contract as future optional plugins so the first implementation
tests the real extension boundary.

MVP-A plugin constraints:

- Plugins are compiled into the app and registered by a `ChatPluginRegistry`.
- Plugins receive normalized `ChatEvent` values, not raw Twitch transport
  payloads.
- Plugins return typed `ChatPluginAction` values such as message decorations,
  local notices, command suggestions, or diagnostics.
- A failing plugin cannot crash the chat surface; failures are isolated and
  reported through diagnostics.
- User settings can enable or disable each registered plugin.

Post-MVP plugin work can consider external plugin packages, signatures, script
sandboxing, or a marketplace-like workflow. Those are intentionally deferred
until the built-in plugin boundary proves useful.

## Data Flow

VOD flow:

1. User enters a channel name or selects a recent channel.
2. App resolves channel identity through Twitch metadata.
3. App fetches recent VOD metadata for that channel.
4. User selects a VOD.
5. Player feature requests a playable Media3 item from the playback layer.
6. Playback layer resolves HLS source and exposes result or typed error.
7. Player screen starts playback and persists progress periodically.
8. Optional replay surface subscribes to normalized replay events when that
   later phase is implemented.
9. Home and VOD list surfaces read local progress to show resume affordances.

Live flow:

1. User opens a channel or selects a live channel.
2. App resolves stream metadata and live status through official metadata
   surfaces.
3. Player feature requests a live Media3 item from the playback layer.
4. Chat feature opens a chat session for the same channel when the user is
   authenticated and chat is enabled.
5. Chat transport emits normalized events into the chat pipeline.
6. Plugin registry dispatches events to enabled local plugins.
7. Chat UI renders normalized messages, plugin decorations, emotes, moderation
   notices, connection state, and typed errors.

## Playback Behavior

MVP-A playback requirements:

- Start a VOD from the beginning or saved resume position.
- Allow scrubbing across the full VOD timeline.
- Keep controls responsive while buffering.
- Show buffering and retry states without blocking the whole UI.
- Save progress on pause, navigation away, app background, and periodic interval.
- Support portrait and landscape/fullscreen player layouts.
- Keep player lifecycle tied to Compose/Android lifecycle to avoid leaked
  playback sessions.
- Media3 implementation plans must explicitly use Compose cleanup and lifecycle
  observation, such as `DisposableEffect` plus `LifecycleEventObserver`, for
  pause, resume, and release behavior.

Quality selection can begin as automatic quality only. Manual quality can be an
MVP stretch goal if the HLS variants are available cleanly through Media3.

Full-client playback requirements reserved for later milestones:

- Start and stop live playback without leaking player sessions.
- Surface live latency, reconnecting, and stream-offline states.
- Share the player control model between VOD and live where behavior overlaps.
- Keep chat panel updates from forcing expensive player recomposition.
- Synchronize VOD chat replay to player position after seeking.

## Chat, Replay, And Emote Behavior

MVP-A should define the contracts, fake data, default local plugin, settings,
and diagnostics for chat. Production transport comes later.

Full-client chat requirements:

- Read live chat through the preferred Twitch-supported route for the use case.
- Send chat messages only after OAuth scopes and rate limits are handled.
- Normalize raw transport payloads into app-owned message models.
- Support message deletion, clear-chat events, notices, badges, mentions,
  replies, and moderation-visible states where permissions allow it.
- Route normalized events through enabled chat plugins before rendering.
- Resolve Twitch and third-party emotes through provider contracts with caching.
- Keep emote fetch failures recoverable; plain text chat remains readable.
- Keep VOD chat replay synchronized to playback time, including seeks and
  resume position.

## Error Handling

Use user-readable, recoverable errors:

- Not logged in: prompt to authenticate.
- Channel not found: keep search entry visible and explain that the channel was
  not found.
- No VODs: show empty state with channel context.
- Playback source unavailable: offer retry and explain that the VOD may be
  restricted, deleted, or temporarily unavailable.
- Stream offline: keep channel context visible and offer recent VODs if
  available.
- Network timeout: offer retry and keep previously loaded data if available.
- Playback-token failure: keep details in diagnostics/logs, show a simple
  playback unavailable message to the user.
- Chat disconnected: show reconnecting state without interrupting playback.
- Chat rate-limited: disable sending briefly and show a clear cooldown state.
- Plugin failure: disable or isolate the failing plugin for the session and
  record diagnostics.
- Emote provider failure: render plain text and retry/cache independently.

Do not expose raw GraphQL, token, IRC, EventSub, plugin stack trace, or HTTP
implementation details in normal UI.

## Testing And Verification

Design verification:

- Stitch output includes all MVP screens and major states.
- Claude Design handoff resolves component names, states, and motion rules.
- Android UI implementation is compared against the handoff before feature signoff.

Engineering verification:

- Unit tests for data mapping, watch-position persistence, and typed error
  handling.
- Unit tests for chat message normalization, plugin registry dispatch, plugin
  failure isolation, and emote provider contracts.
- Integration tests for repository behavior with mocked Twitch responses.
- UI tests for navigation, loading/error states, and player screen state changes
  where practical.
- Manual device checks for playback start, scrub, pause, resume, orientation
  changes, app background/foreground, and long VOD progress.
- Manual device checks for chat panel scrolling, message burst behavior, plugin
  toggle behavior, and player responsiveness while chat updates.
- Performance checks for startup time, scrolling VOD list, player overlay
  responsiveness, chat burst rendering, emote cache behavior, memory growth
  during playback, and battery-heavy behavior.

## Milestones

### M0: Product And Design Foundation

- Use Stitch to generate initial visual directions for the full client shell.
- Choose one direction.
- Use Claude Design to translate that direction into a handoff.
- Produce the project `DESIGN.md`.
- Include video, chat, replay, plugin settings, and diagnostics surfaces even
  when some are placeholders.
- Treat external design handoff as parallel polish work after baseline
  `DESIGN.md`; it should not block M1 compilation.

### M1: Android Client Foundation

- Create Android project structure.
- Add Compose navigation shell.
- Add app theme based on `DESIGN.md`.
- Add Twitch auth/account state model.
- Add local settings/history/plugin configuration interfaces.
- Add core contracts for playback source resolution, chat sessions, chat
  messages, emote providers, and chat plugins.
- Register a default built-in local chat plugin with fake/sample data.

### M2: Playback Feasibility Spike

- Run a standalone spike before building more UI.
- Verify channel login to VOD ID to playback token to playable HLS manifest.
- Test one normal VOD and one muted/partially muted VOD.
- Record headers, token request shape, manifest assumptions, and failure modes
  in `docs/spikes/playback-feasibility.md`.
- Stop M3 work until this path is proven or a replacement playback strategy is
  chosen.

### M3: VOD Discovery And Playback

- Implement channel search/open flow.
- Fetch and display channel VOD list.
- Show thumbnails, titles, durations, publish dates, and progress markers.
- Add loading, empty, and error states.
- Resolve playable VOD media item.
- Play with Media3.
- Add play/pause, scrubber, buffering, elapsed/remaining time.
- Surface muted or discontinuous VOD segments as timeline metadata and avoid
  assuming HLS segments are continuous.
- Add portrait and landscape/fullscreen layouts.

### M4: OAuth And Account

- Add mobile-safe OAuth only after the public VOD path is proven.
- Handle token refresh, expiry, logout, and account diagnostics.
- Gate followed channels, sending chat, moderator affordances, and protected
  API calls behind account state.

### M5: Live Playback

- Resolve live stream state and playable media item.
- Add live player states for online, offline, reconnecting, and unavailable.
- Share player lifecycle and controls with the VOD player where practical.
- Keep chat surface available but transport can still be fake until M6.

### M6: Live Chat And Default Plugin

- Connect live chat through the selected Twitch-supported transport.
- Normalize chat events and render badges, notices, deletes, and message states.
- Send messages only after scopes and rate-limit handling are implemented.
- Run normalized events through the default local chat plugin.
- Add plugin diagnostics and user-visible enable/disable controls.

### M7: Emotes And Plugin Expansion

- Add Twitch emote rendering.
- Add BTTV, FFZ, and 7TV provider contracts and caches.
- Let chat plugins decorate messages and provide local commands.
- Stress-test chat bursts and emote-heavy rooms.

### M8: VOD Chat Replay

- Fetch or resolve replay source through an isolated replay provider.
- Synchronize replay messages to VOD playback position.
- Handle seeking, resume, buffering, and unavailable replay states.
- Reuse the live chat renderer and plugin pipeline where possible.

### M9: Personal Usability And Beta

- Persist watch progress.
- Add recent VODs and resume behavior.
- Add settings and diagnostic surface.
- Run device QA and performance pass.
- Produce a personal beta APK.

## Risks

- Twitch playback URL/token behavior may change and may require maintenance.
- Official APIs cover metadata better than playback internals and VOD chat
  replay; playback and replay providers must stay isolated.
- Public playback-token and HLS manifest behavior is the highest-risk assumption
  and must be validated in a standalone spike immediately after M1.
- Twitch VODs can contain muted or discontinuous segments that may affect Media3
  timeline behavior and seeking.
- Twitch chat capabilities differ by transport, scopes, rate limits, and account
  permissions.
- EventSub and chat messages can be duplicated or disconnected; message IDs and
  connection state need explicit handling.
- Third-party emote provider APIs may change or rate limit independently.
- External plugin loading can create security and stability risk, so MVP-A uses
  compile-time local plugins only.
- Claude Design quota is limited, so using it too early could block later
  translation checkpoints.
- A highly animated UI or chat surface can hurt battery and player
  responsiveness if not kept simple around the video surface.

## Implementation Defaults For MVP-A

- Do not require Twitch OAuth for the first public VOD playback path. Keep an
  account-state model and Settings surface in MVP-A, then implement OAuth in its
  own milestone before followed channels, chat sending, or moderator features.
- Start with automatic quality selection. Manual quality selection is a stretch
  goal only if Media3 exposes variants cleanly without fragile UI work.
- Target Android phones first. Tablet-specific layouts are post-MVP.
- Fetch recent archived broadcasts for the selected channel and show newest
  VODs first. Highlights, uploads, search filters, and sorting controls are
  post-MVP.
- Implement chat/plugin foundation with fake data, typed contracts, default
  built-in plugin registration, and settings. Real chat transport is M6.
- Use DataStore for simple settings/plugin toggles and keep history behind an
  interface so Room can replace or augment it when querying needs grow.
- Keep third-party emote providers behind provider contracts from the start.

## Recommendation

Proceed with MVP-A as a full-client foundation with a VOD-first usable path.
Keep the first implementation deliberately small, but create the typed seams for
live playback, chat, replay, emotes, and local chat plugins before feature code
starts depending on VOD-only assumptions. Use Stitch heavily before code, use
Claude Design only for selected translation checkpoints, and implement real
chat/emote transports only after the app shell and VOD path are stable.
