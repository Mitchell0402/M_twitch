# Claude Design Prompt: M3.5 VOD UI Compose Handoff

Translate the selected Stitch direction for M3.5 VOD UI Foundation And Polish
into a Compose-ready handoff for M Twitch.

M Twitch is a VOD-first prototype of a complete native Android Twitch client.
The handoff should polish the current Home -> Channel VOD List -> VOD Player
path while preserving the existing M3 discovery and playback logic.

## Selected Stitch Direction

Use the selected Stitch output for:

- Home channel entry.
- Channel VOD List.
- `VodCard`.
- VOD Player portrait controls.
- VOD Player landscape/fullscreen controls.
- Loading, empty, retry, manifest forbidden, auth-needed, rate-limited,
  buffering, muted segment, discontinuity, and diagnostics-entry states.

If the selected Stitch output misses a required state, fill the gap with the
same visual language instead of inventing a separate style.

## Current Android Implementation

- `app/src/main/java/dev/mitchell/mtwitch/feature/home/HomeScreen.kt`
  - Current Home route UI.
  - Owns the local channel login text field.
  - Calls `onOpenChannel`, `onOpenLive`, `onOpenChatPlugins`, and
    `onOpenSettings`.
- `app/src/main/java/dev/mitchell/mtwitch/feature/channel/ChannelVodListScreen.kt`
  - Current channel VOD list route UI.
  - Defines `ChannelVodListUiState`.
  - Contains private `LoadingState`, `VodList`, `EmptyState`, `ErrorState`,
    duration formatting, and discovery error message mapping.
- `app/src/main/java/dev/mitchell/mtwitch/feature/player/PlayerScreen.kt`
  - Resolves `PlaybackRequest.Vod`.
  - Creates and releases Media3 `ExoPlayer`.
  - Saves watch progress on interval, pause, back, and disposal.
  - Renders `PlayerSurface`, buffering/progress bar, slider, elapsed/duration
    text, Back, and Play/Pause.
  - Maps `PlaybackUnavailableReason` to user-facing player messages.
- `app/src/main/java/dev/mitchell/mtwitch/MtwitchApp.kt`
  - Wires typed `AppRoute.Home`, `AppRoute.Channel`, and `AppRoute.Player`.
  - Builds VOD discovery repository, watch history repository, and playback
    resolver.
- `app/src/main/java/dev/mitchell/mtwitch/core/model/VodModels.kt`
  - `Vod`, `VideoId`, `ChannelId`, `WatchProgress`.
- `app/src/main/java/dev/mitchell/mtwitch/data/discovery/VodDiscoveryModels.kt`
  - `Channel`, `VodDiscoveryResult`, `VodDiscoveryError`,
    `VodDiscoveryRepository`.
- `app/src/main/java/dev/mitchell/mtwitch/data/playback/PlaybackSourceModels.kt`
  - `PlaybackRequest`, `PlaybackSource`, `PlaybackTimelineIssue`,
    `PlaybackTimelineIssueReason`, `PlaybackSourceResult`,
    `PlaybackUnavailableReason`, `PlaybackDiagnostics`.
- `app/src/main/java/dev/mitchell/mtwitch/ui/theme/MtwitchTheme.kt`
  - Current Material color scheme.

Current reusable components:

- No dedicated reusable VOD/player components yet.
- Current UI is mostly inline Material3 `Button`, `OutlinedTextField`, `Card`,
  `Text`, `LinearProgressIndicator`, `CircularProgressIndicator`, and `Slider`.

Proposed new reusable components from this handoff:

- `ScreenHeader`
- `ChannelEntry`
- `VodCard`
- `StateMessage`
- `PlayerControls`
- `PlayerTimeline`
- `TimelineIssueNotice`
- `PlaybackDiagnosticsSheet`

## Existing States To Preserve

Do not change the domain contracts for this polish pass.

Channel VOD list:

- `ChannelVodListUiState.Loading`
- `ChannelVodListUiState.Content(channel, vods)`
- `ChannelVodListUiState.Empty(channel)`
- `ChannelVodListUiState.Error(reason)`

Discovery errors:

- `ChannelNotFound`
- `NetworkTimeout`
- `RateLimited`
- `Unauthorized`
- `Unknown`

Playback:

- `PlaybackSourceResult.Ready(source)`
- `PlaybackSourceResult.Unavailable(reason)`
- `sourceResult == null` while resolving.

Playback unavailable reasons:

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

Timeline issues:

- `MutedSegment`
- `Discontinuity`
- `Unknown`

## Required Handoff

Produce a concise Compose handoff with:

- Design tokens:
  - colors,
  - typography,
  - spacing,
  - shapes,
  - elevation,
  - motion.
- Component inventory with Compose-oriented names and responsibilities.
- Screen-by-screen layout rules:
  - Home portrait,
  - Channel VOD List portrait,
  - VOD Player portrait,
  - VOD Player landscape/fullscreen.
- State table for:
  - loading,
  - content,
  - empty,
  - error,
  - retrying,
  - unauthorized/auth-needed,
  - rate-limited,
  - resolving playback source,
  - playing,
  - paused,
  - buffering,
  - manifest forbidden,
  - manifest malformed,
  - no variants,
  - token failed/rejected,
  - source missing,
  - network timeout,
  - muted segment,
  - discontinuity,
  - diagnostics sheet open.
- Player layout rules:
  - overlay hierarchy,
  - control placement,
  - scrubber/timeline behavior,
  - elapsed/remaining time,
  - fullscreen affordance,
  - settings/diagnostics affordance,
  - loading/buffering placement,
  - retry placement.
- VOD card rules:
  - thumbnail placeholder and future image loading behavior,
  - title wrapping,
  - duration,
  - publish date,
  - progress/resume marker,
  - channel context,
  - pressed/focused state.
- Muted/discontinuous messaging:
  - when to show a banner vs timeline marker,
  - copy tone,
  - how to avoid implying the app is broken.
- Accessibility notes:
  - 48dp touch targets,
  - content descriptions for icon-only controls,
  - contrast,
  - long-title wrapping/truncation,
  - scrubber accessibility labels,
  - motion sensitivity.
- Implementation mapping:
  - Existing files to modify later.
  - New component files to create later.
  - State models that should remain unchanged.
  - Missing UI-only state that can be local Compose state.
  - Any truly missing domain/state addition, if unavoidable.

## Implementation Boundaries For Codex

The later Compose implementation must:

- Not change playback resolver, GraphQL token, HLS manifest, or metadata core
  logic for visual polish.
- Not require OAuth for public VOD playback.
- Not leak token values, signatures, cookies, full signed manifest URLs, raw
  GraphQL, usher details, HTTP internals, or stack traces in normal UI.
- Keep feature state separate from reusable visual components.
- Keep player lifecycle behavior intact:
  - `DisposableEffect`,
  - lifecycle pause handling,
  - progress save behavior,
  - `ExoPlayer` release.
- Keep video playback responsive.
- Avoid heavy decoration or animation around the player surface.
- Keep chat/replay as a collapsed placeholder only in M3.5 unless the user asks
  for M8 work.

## Preferred File Mapping For Later Compose Work

This prompt is for design handoff only, but include a mapping for the later
Codex implementation:

- Existing files likely to modify:
  - `feature/home/HomeScreen.kt`
  - `feature/channel/ChannelVodListScreen.kt`
  - `feature/player/PlayerScreen.kt`
  - `ui/theme/MtwitchTheme.kt` only if durable tokens are selected.
- Likely new files:
  - `feature/common/ScreenHeader.kt` or feature-local equivalent.
  - `feature/common/StateMessage.kt` or feature-local equivalent.
  - `feature/channel/VodCard.kt`.
  - `feature/player/PlayerControls.kt`.
  - `feature/player/PlayerTimeline.kt`.
  - `feature/player/TimelineIssueNotice.kt`.
  - `feature/player/PlaybackDiagnosticsSheet.kt`.

## Output Format

Return:

- A concise Compose handoff.
- Design token table.
- Component inventory table.
- State table.
- Screen layout notes.
- Motion notes.
- Accessibility notes.
- Implementation notes for Codex.
- A visual QA checklist for emulator or device screenshots.

Keep the handoff native Android/Compose oriented. Do not generate a WebView app
or HTML-first implementation plan.
