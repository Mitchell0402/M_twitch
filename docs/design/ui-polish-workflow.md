# UI Polish Workflow

This project uses a prompt-first design workflow for every user-facing polish
checkpoint:

1. Codex reads the current repo state and generates tool prompts.
2. Google Stitch explores or refines the visual direction.
3. Claude Design translates the selected Stitch direction into a Compose-ready
   handoff.
4. Codex implements the handoff in native Jetpack Compose.
5. The milestone is verified with Gradle checks and visual smoke evidence.

Do not send a milestone to Stitch or Claude Design directly from memory. Generate
the prompts from the current implementation first so the tools receive actual
screen names, state models, constraints, and acceptance criteria.

## Per-Checkpoint Files

For each UI polish checkpoint, create these files before using external design
tools:

- `docs/design/mX-<area>-stitch-prompt.md`
- `docs/design/mX-<area>-claude-design-prompt.md`
- `docs/design/mX-<area>-ui-checklist.md`

Example for the VOD polish checkpoint:

- `docs/design/m3-vod-ui-stitch-prompt.md`
- `docs/design/m3-vod-ui-claude-design-prompt.md`
- `docs/design/m3-vod-ui-checklist.md`

## Prompt Generation Inputs

Before writing a Stitch or Claude Design prompt, inspect:

- Current feature screens under `app/src/main/java/dev/mitchell/mtwitch/feature/`.
- Current route/state wiring in `MtwitchApp.kt`.
- Current app-owned state models and contracts under `core/`, `data/`, and
  `plugin/`.
- `DESIGN.md`.
- The current MVP spec in `docs/superpowers/specs/`.
- The latest milestone spike or implementation notes, such as
  `docs/spikes/playback-feasibility.md`.
- Screenshots or emulator observations if available.
- Known constraints such as phone-first layout, Media3 player performance,
  no WebView wrapper, no token leakage, and no OAuth requirement for public VOD
  playback.

## Stitch Prompt Template

Use this structure for each `*-stitch-prompt.md`:

```markdown
# Google Stitch Prompt: [Milestone] [Area] UI Polish

Design/refine the [area] UI for M Twitch, a native Android Twitch client built
with Kotlin, Jetpack Compose, and Media3.

Current implementation context:
- Current screens:
  - `[ScreenName]` in `[file path]`
  - `[ScreenName]` in `[file path]`
- Current state models:
  - `[UiState name]`: `[states]`
  - `[Domain result name]`: `[states]`
- Current constraints:
  - Phone-first Android UI.
  - Native Compose implementation, not WebView.
  - Preserve existing data/playback/chat/plugin contracts unless a missing UI
    state is discovered.
  - Keep video playback responsive and avoid heavy decorative animation around
    the player.

Explore:
- [Primary screen or flow]
- [Secondary screen or flow]
- [Error/retry/empty/auth/offline states]
- [Diagnostics or settings surface]

Create:
- 2-3 visual directions if the direction is still open, or one focused
  refinement if the direction is already chosen.
- Portrait phone layout.
- Landscape or fullscreen layout if the area touches playback.
- Loading, empty, error, retrying, offline/auth-needed, and diagnostics states
  where relevant.
- Motion notes for overlays, transitions, list updates, and state changes.
- A concise design-system delta: colors, typography, spacing, controls, and
  reusable components introduced by this polish pass.

Avoid:
- Web-specific implementation details.
- Marketing/landing-page layouts.
- Decorative gradients, blobs, or oversized hero sections.
- Raw token, GraphQL, usher, IRC, EventSub, plugin stack trace, or HTTP details
  in normal UI.
```

## Claude Design Prompt Template

Use this structure for each `*-claude-design-prompt.md` after a Stitch direction
has been selected:

```markdown
# Claude Design Prompt: [Milestone] [Area] Compose Handoff

Translate the selected Stitch direction for [area] into a Compose-ready handoff
for M Twitch.

Current Android implementation:
- `[file path]`: `[responsibility]`
- `[file path]`: `[responsibility]`
- Current reusable components: `[list]`
- Proposed new reusable components from Stitch: `[list]`

Required handoff:
- Design tokens: colors, typography, spacing, shapes, elevation, and motion.
- Component inventory with Compose-oriented names.
- Screen-by-screen layout rules.
- State table for every implemented state and every required missing state.
- Player, chat, plugin, or diagnostics layout rules if relevant.
- Accessibility notes: touch targets, content descriptions, contrast, readable
  truncation/wrapping, and motion sensitivity.
- Implementation mapping:
  - Existing files to modify.
  - New component files to create.
  - State models that should remain unchanged.
  - Missing UI states that require a small domain/state addition.

Constraints:
- Do not generate a WebView app.
- Do not require OAuth for public VOD playback.
- Do not leak tokens, signatures, cookies, full signed manifest URLs, or raw
  transport internals.
- Keep feature state separate from reusable visual components.
- Keep player and chat performance stable.

Output:
- A concise Compose handoff.
- Component/state tables.
- Implementation notes for Codex.
- A short visual QA checklist for emulator or device screenshots.
```

## Codex Implementation Rules

When implementing a Claude Design handoff:

- Keep route and side-effect code separate from pure visual components where
  the feature is growing.
- Put reusable components close to the feature first unless they are clearly
  shared across multiple features.
- Prefer app-owned UI state over raw network or player state in composables.
- Preserve existing repository, playback, chat, emote, and plugin contracts
  unless a real missing UI state is discovered.
- Add unit tests for formatting, state mapping, and helper behavior.
- Run `.\gradlew.bat --no-daemon :app:testDebugUnitTest :app:assembleDebug :app:lintDebug`.
- Capture emulator or device screenshots for states changed by the polish pass
  when practical.

## Checkpoint Expectations

### M3.5 VOD UI Foundation And Polish

- Polish Home channel entry, Channel VOD List, VOD cards, VOD Player controls,
  buffering, playback unavailable, muted/discontinuous messaging, and
  portrait/landscape behavior.
- Expected new/reworked components: `VodCard`, `ScreenHeader`, `StateMessage`,
  `PlayerControls`, `PlayerTimeline`, `PlaybackDiagnosticsSheet`.

### M4.5 Account UI Polish

- Polish login, logged-in, token expired, logout confirmation, account
  diagnostics, and auth-required messaging.
- Public VOD playback must still work without login.

### M5.5 Live Player UI Polish

- Polish online, offline, reconnecting, unavailable, and low-latency live
  states.
- Reuse VOD player components where semantics match.

### M6.5 Chat UI Polish

- Polish chat row density, badges, notices, deletes, reconnecting,
  rate-limited sending, plugin failure, and collapsed chat over video.
- Verify player responsiveness while chat updates.

### M7.5 Emote And Plugin UI Polish

- Polish emote-heavy chat, provider failure, plugin toggles, command
  suggestions, and diagnostics.
- Plain text chat remains readable when emotes fail.

### M8.5 Replay UI Polish

- Polish replay timeline, seek behavior, unavailable replay, buffering, and
  replay panel collapse/expand behavior.
- Reuse live chat rendering where possible.

### M9.5 Final UI QA And Beta Polish

- Run a final consistency pass across typography, spacing, touch targets,
  contrast, motion, navigation, settings, diagnostics, and long text wrapping.
- Use Stitch only for targeted unresolved flows, not a full redesign.
