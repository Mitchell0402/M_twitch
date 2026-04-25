# Google Stitch Prompt: M3.5 VOD UI Foundation And Polish

Design/refine the VOD discovery and playback UI for M Twitch, a native Android
Twitch client built with Kotlin, Jetpack Compose, and Media3.

M Twitch is a VOD-first prototype of a complete Twitch client. The first usable
path is Home channel entry -> Channel VOD List -> VOD Player, but the product
must still feel like it can grow into live playback, chat, replay, plugins, and
diagnostics without a redesign.

## Current Implementation Context

Current screens:

- `HomeScreen` in
  `app/src/main/java/dev/mitchell/mtwitch/feature/home/HomeScreen.kt`
  - Dark Compose surface with app title, channel login text field, Open VODs,
    Open Live Placeholder, Chat Plugins, Settings, and a placeholder sentence
    for recent VODs/live/diagnostics.
  - No recent VOD cards yet. No channel suggestions yet.
- `ChannelVodListScreen` in
  `app/src/main/java/dev/mitchell/mtwitch/feature/channel/ChannelVodListScreen.kt`
  - Back button, `"$channelLogin VODs"` title, and state-driven content.
  - Current `VodList` uses simple Material cards with title, duration, and an
    optional progress bar.
  - No thumbnail rendering yet, even though `Vod.thumbnailUrl` exists.
- `PlayerScreen` in
  `app/src/main/java/dev/mitchell/mtwitch/feature/player/PlayerScreen.kt`
  - Media3 `PlayerSurface` when playback is ready.
  - Black resolving/unavailable surface when source is not ready.
  - Bottom controls include progress/buffering bar, slider, Back, elapsed /
    duration text, and Play/Pause.
  - No fullscreen button, quality surface, diagnostics sheet, timeline issue
    banner, or chat/replay placeholder yet.
- Route/state wiring in
  `app/src/main/java/dev/mitchell/mtwitch/MtwitchApp.kt`
  - `Home -> Channel -> Player` uses typed `AppRoute`.
  - VOD discovery uses fake data when `TWITCH_GQL_CLIENT_ID` is blank.
  - Playback uses `TwitchVodPlaybackSourceResolver`.

Current state models:

- `ChannelVodListUiState`
  - `Loading`
  - `Content(channel, vods)`
  - `Empty(channel)`
  - `Error(reason)`
- `VodDiscoveryError`
  - `ChannelNotFound`
  - `NetworkTimeout`
  - `RateLimited`
  - `Unauthorized`
  - `Unknown`
- `Vod`
  - `id`, `channelId`, `channelLogin`, `title`, `thumbnailUrl`, `duration`,
    `publishedAtEpochMs`, `progress`
  - `hasProgress` indicates whether a progress marker should be shown.
- `PlaybackSourceResult`
  - `Ready(source)`
  - `Unavailable(reason)`
- `PlaybackUnavailableReason`
  - `SourceMissing`
  - `TokenFailed`
  - `ManifestForbidden`
  - `ManifestMalformed`
  - `NoVariants`
  - `TokenRejected`
  - `NetworkTimeout`
  - `StreamOffline`
  - `AuthRequired`
  - `RateLimited`
- `PlaybackSource`
  - `hlsUrl`, `isLive`, `timelineIssues`, sanitized diagnostics.
- `PlaybackTimelineIssue`
  - `MutedSegment`
  - `Discontinuity`
  - `Unknown`

Current design baseline:

- Dark-first interface tuned for video viewing.
- Calm, fast, playback-centered, not a marketing landing page.
- One accent color for playback/progress.
- High contrast text on dark surfaces.
- Rounded corners 8dp or below unless a later handoff changes that.
- Phone-first portrait layout.
- Landscape player prioritizes video and hides nonessential UI.
- Player controls are overlays on top of video where practical.
- Loading states use simple progress indicators.
- Touch targets should be at least 48dp.

Playback spike constraints from M2:

- Public VOD playback can work without OAuth for tested public VODs.
- Playback token + usher HLS is fragile and must stay isolated behind typed
  playback results.
- The UI must not expose raw GraphQL, token, usher, signed manifest URL, HTTP
  transport internals, cookies, signatures, or token values.
- Token success and manifest success are separate. A VOD can resolve a token
  and still return `ManifestForbidden`.
- Muted VODs can use muted playlists or muted segment names.
- Discontinuity tags were not observed in the test cases, but the UI should
  still support muted/discontinuous messaging from `timelineIssues`.

## Current Constraints

- Phone-first Android UI.
- Native Compose implementation, not WebView.
- Preserve existing data, playback, GraphQL, metadata, chat, and plugin
  contracts unless a missing UI state is discovered.
- Do not require OAuth for public VOD playback.
- Keep video playback responsive and avoid heavy decorative animation around
  the player.
- Keep normal user UI readable and non-technical. Technical details belong in a
  diagnostics affordance with redacted data only.
- Do not redesign the whole product shell. Polish the M3 VOD path and leave
  future live/chat/plugin surfaces visibly compatible.

## Explore

Create one focused refinement direction, plus one optional alternate if a
meaningfully different layout improves scan speed or player clarity.

Refine these surfaces:

- Home channel entry
  - Make the channel lookup feel like the primary action.
  - Include space for future recent VODs and last watched channel without
    pretending the data already exists.
  - Keep Settings and Chat Plugins reachable but visually secondary.
- Channel VOD List
  - Strong channel header with display name/login and live-status placeholder.
  - Loading, empty, recoverable error, unauthorized/auth-needed, rate-limited,
    and retrying states.
  - VOD list optimized for fast scanning on phones.
- `VodCard`
  - Thumbnail area or thumbnail placeholder.
  - Title, duration, publish date, channel context, progress marker, and resume
    affordance.
  - Long title wrapping/truncation behavior.
  - Pressed/focused state.
- VOD Player portrait
  - Video surface, top/back affordance, title/channel context, play/pause,
    scrubber, elapsed/remaining time, buffering, retry, fullscreen, and basic
    settings/diagnostics affordance.
  - Collapsed chat/replay placeholder that does not compete with video.
- VOD Player landscape/fullscreen
  - Video-first layout.
  - Minimal overlays.
  - Controls fade in/out and avoid covering important video content.
  - Chat/replay placeholder should be hidden or collapsed unless explicitly
    opened.
- Playback unavailable states
  - Especially `ManifestForbidden`, `AuthRequired`, `NetworkTimeout`,
    `RateLimited`, `TokenFailed`/`TokenRejected`, `ManifestMalformed`, and
    `NoVariants`.
  - Provide retry and diagnostics affordances while keeping copy
    user-readable.
- Muted/discontinuous playback messaging
  - Timeline marker/banner or subtle notice for muted segments.
  - Discontinuity/seek caveat messaging that does not alarm the user.
  - Avoid raw HLS or manifest language in normal UI.

## Create

Deliver:

- Portrait phone layout for Home, Channel VOD List, VOD Card, and VOD Player.
- Landscape/fullscreen phone layout for VOD Player.
- Loading, empty, error, retrying, auth-needed, rate-limited, manifest
  forbidden, buffering, muted segment, discontinuity, and diagnostics entry
  states where relevant.
- Motion notes for:
  - player controls fade in/out,
  - buffering indicator,
  - retry state transition,
  - list item press,
  - timeline issue notice,
  - orientation/fullscreen transition.
- A concise design-system delta:
  - colors,
  - typography,
  - spacing,
  - shapes,
  - controls,
  - reusable components introduced by this polish pass.

Expected component concepts:

- `ScreenHeader`
- `ChannelEntry`
- `VodCard`
- `StateMessage`
- `PlayerControls`
- `PlayerTimeline`
- `TimelineIssueNotice`
- `PlaybackDiagnosticsSheet`

## Avoid

- Web-specific implementation details.
- Marketing/landing-page layouts.
- Decorative gradients, blobs, oversized hero sections, or purely atmospheric
  imagery.
- A visual direction dominated only by purple or purple-blue gradients.
- Raw token, GraphQL, usher, IRC, EventSub, plugin stack trace, HTTP detail,
  signed URL, cookie, signature, or client-secret copy in normal UI.
- Introducing OAuth as required for public VOD playback.
- Heavy animation around the video surface.
- UI that assumes all VOD timelines are continuous or fully audible.
