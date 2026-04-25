# Design Artifact Checklist

M1 engineering can run from the baseline `DESIGN.md` while Stitch and Claude
Design work continues in parallel. Every later UI polish checkpoint must start
by generating prompts from the current repo state, then sending those prompts to
Stitch and Claude Design.

## Foundation Artifacts

- `DESIGN.md` with selected visual direction and design tokens.
- Stitch export or screenshots for Home / Resume.
- Stitch export or screenshots for Channel VOD List.
- Stitch export or screenshots for VOD Player portrait.
- Stitch export or screenshots for VOD Player landscape/fullscreen.
- Stitch export or screenshots for Live Player placeholder.
- Stitch export or screenshots for chat panel and VOD replay panel placeholder.
- Stitch export or screenshots for Chat Plugin Settings.
- Stitch export or screenshots for Settings.
- Claude Design handoff notes for Compose components.
- Claude Design state table for loading, empty, error, offline, and auth-needed states.
- Claude Design state table for chat disconnected, chat reconnecting, rate-limited, plugin failed, and emote provider unavailable states.
- Claude Design motion rules for navigation and player overlays.

The first implementation can use the baseline `DESIGN.md` in this repository while external artifacts are being prepared. Replace it with the Claude Design handoff before final UI polish.

## Per-Polish Checkpoint Artifacts

For each UI polish checkpoint, create:

- Milestone Stitch prompt generated from the current repo state.
- Milestone Claude Design prompt generated after the Stitch direction is chosen.
- Milestone UI checklist with required states and screenshot targets.
- Stitch export or screenshots for every changed screen.
- Claude Design Compose handoff with component inventory and state table.
- Codex implementation notes mapping handoff components to Compose files.
- Gradle verification result.
- Emulator or device screenshots when the changed surface is visual or
  interactive.

Expected checkpoint files:

- `docs/design/m3-vod-ui-stitch-prompt.md`
- `docs/design/m3-vod-ui-claude-design-prompt.md`
- `docs/design/m3-vod-ui-checklist.md`
- `docs/design/m4-account-ui-stitch-prompt.md`
- `docs/design/m4-account-ui-claude-design-prompt.md`
- `docs/design/m4-account-ui-checklist.md`
- `docs/design/m5-live-ui-stitch-prompt.md`
- `docs/design/m5-live-ui-claude-design-prompt.md`
- `docs/design/m5-live-ui-checklist.md`
- `docs/design/m6-chat-ui-stitch-prompt.md`
- `docs/design/m6-chat-ui-claude-design-prompt.md`
- `docs/design/m6-chat-ui-checklist.md`
- `docs/design/m7-emote-plugin-ui-stitch-prompt.md`
- `docs/design/m7-emote-plugin-ui-claude-design-prompt.md`
- `docs/design/m7-emote-plugin-ui-checklist.md`
- `docs/design/m8-replay-ui-stitch-prompt.md`
- `docs/design/m8-replay-ui-claude-design-prompt.md`
- `docs/design/m8-replay-ui-checklist.md`
- `docs/design/m9-final-ui-stitch-prompt.md`
- `docs/design/m9-final-ui-claude-design-prompt.md`
- `docs/design/m9-final-ui-checklist.md`

Use `docs/design/ui-polish-workflow.md` as the template for every checkpoint.
