# M3.5 VOD UI Foundation And Polish Checklist

This checklist gates the M3.5 design-tool pass and the later Compose polish
implementation. The current stage creates design prompts and a landing plan
only; it does not implement Compose UI.

## Stage 1: Prompt Artifacts

- [ ] `docs/design/m3-vod-ui-stitch-prompt.md` exists.
- [ ] Stitch prompt was generated from the current repo state, not memory alone.
- [ ] Stitch prompt names current Home, Channel VOD List, Player, and route
  wiring files.
- [ ] Stitch prompt covers Home channel entry.
- [ ] Stitch prompt covers Channel VOD List.
- [ ] Stitch prompt covers `VodCard`.
- [ ] Stitch prompt covers player controls.
- [ ] Stitch prompt covers buffering.
- [ ] Stitch prompt covers manifest forbidden.
- [ ] Stitch prompt covers empty/error/retry states.
- [ ] Stitch prompt covers muted/discontinuous messaging.
- [ ] Stitch prompt covers portrait and landscape/fullscreen behavior.
- [ ] `docs/design/m3-vod-ui-claude-design-prompt.md` exists.
- [ ] Claude Design prompt maps selected Stitch output to Compose components.
- [ ] Claude Design prompt names existing implementation files and state
  models.
- [ ] Claude Design prompt lists state models that should remain unchanged.
- [ ] Claude Design prompt includes Compose-ready components:
  `ScreenHeader`, `ChannelEntry`, `VodCard`, `StateMessage`,
  `PlayerControls`, `PlayerTimeline`, `TimelineIssueNotice`, and
  `PlaybackDiagnosticsSheet`.
- [ ] Claude Design prompt forbids WebView implementation.
- [ ] Claude Design prompt forbids OAuth as a requirement for public VOD
  playback.
- [ ] Claude Design prompt forbids leaking GraphQL, token, usher, cookie,
  signature, full signed manifest URL, or raw HTTP details in normal UI.

## Stage 2: Stitch Review Targets

- [ ] Home portrait layout shows channel lookup as the primary action.
- [ ] Home keeps Settings and Chat Plugins available but secondary.
- [ ] Home leaves space for recent VODs and last watched channel.
- [ ] Channel VOD List has a clear channel header.
- [ ] Channel VOD List includes live-status placeholder treatment.
- [ ] VOD cards support thumbnail or thumbnail placeholder.
- [ ] VOD cards show title, duration, publish date, progress/resume marker,
  and pressed/focused state.
- [ ] Long VOD titles have a clear wrapping/truncation rule.
- [ ] Loading state is calm and non-blocking.
- [ ] Empty state keeps channel context visible.
- [ ] Error state includes retry.
- [ ] Unauthorized/auth-needed state does not imply login is required for all
  public VOD playback.
- [ ] Rate-limited state sets expectation to try again later.
- [ ] Player portrait layout keeps video primary.
- [ ] Player landscape/fullscreen layout hides nonessential UI.
- [ ] Player controls include play/pause, scrubber, elapsed/remaining time,
  fullscreen, retry where relevant, and diagnostics/settings affordance.
- [ ] Buffering state keeps controls responsive.
- [ ] Manifest forbidden state is user-readable and avoids raw HLS language.
- [ ] Muted segment messaging is visible but low-noise.
- [ ] Discontinuity messaging explains possible seek/timeline oddness without
  blaming the user.

## Stage 3: Claude Design Handoff Targets

- [ ] Claude Design receives the full
  `docs/design/m3-vod-ui-claude-design-prompt.md`.
- [ ] Claude Design receives the selected Stitch direction name or label.
- [ ] Claude Design receives Stitch artifacts for Home, Channel VOD List,
  `VodCard`, VOD Player portrait, and VOD Player landscape/fullscreen.
- [ ] Claude Design receives Stitch artifacts or notes for loading, empty,
  error/retry, auth-needed, rate-limited, manifest forbidden, buffering,
  muted segment, discontinuity, and diagnostics-entry states.
- [ ] Claude Design receives any Stitch tokens, component notes, spacing notes,
  and motion notes.
- [ ] Claude Design receives a short list of any required states missing from
  the Stitch output.
- [ ] Claude Design is explicitly told to translate the selected Stitch
  direction into native Compose, not redesign from scratch.
- [ ] Handoff includes color, typography, spacing, shape, elevation, and motion
  tokens.
- [ ] Handoff includes component inventory with Compose-oriented names.
- [ ] Handoff includes screen-by-screen layout rules.
- [ ] Handoff includes a full state table for implemented and required states.
- [ ] Handoff maps every state to existing or proposed Compose components.
- [ ] Handoff identifies which changes are visual-only and which, if any, need
  new UI state.
- [ ] Handoff keeps playback resolver, GraphQL token, HLS manifest, and
  metadata logic unchanged.
- [ ] Handoff keeps token/signature/full signed URL details out of normal UI.
- [ ] Handoff includes accessibility notes for touch targets, contrast, content
  descriptions, text wrapping, and scrubber labels.
- [ ] Handoff includes motion notes for overlay fade, retry transition,
  buffering, orientation/fullscreen, and timeline issue notice.

## Stage 4: Later Compose Landing Plan

- [ ] Implement reusable visual components before route-specific polish when
  the component will be shared.
- [ ] Prefer feature-local components unless a component is clearly shared.
- [ ] Keep route side effects in current route/screen files.
- [ ] Preserve `ChannelVodListUiState`.
- [ ] Preserve `VodDiscoveryError`.
- [ ] Preserve `PlaybackSourceResult`.
- [ ] Preserve `PlaybackUnavailableReason`.
- [ ] Preserve `PlaybackTimelineIssue`.
- [ ] Preserve player lifecycle cleanup and progress-saving behavior.
- [ ] Add focused tests only for new formatting/state helper behavior.
- [ ] Run `.\gradlew.bat --no-daemon :app:testDebugUnitTest :app:assembleDebug :app:lintDebug`.
- [ ] Capture emulator or device screenshots for Home, VOD list, playing,
  buffering, manifest forbidden, empty, error/retry, muted/discontinuous notice,
  portrait player, and landscape/fullscreen player.
- [ ] Update `DESIGN.md` only after the selected design direction is
  implemented and durable tokens/components are confirmed.

## Non-Goals For M3.5

- [ ] Do not change playback resolver behavior.
- [ ] Do not change GraphQL token behavior.
- [ ] Do not change Twitch metadata/discovery core logic.
- [ ] Do not add OAuth as a requirement for public VOD playback.
- [ ] Do not implement VOD chat replay.
- [ ] Do not implement live playback polish.
- [ ] Do not introduce a WebView wrapper.
- [ ] Do not commit or push until explicitly requested.
