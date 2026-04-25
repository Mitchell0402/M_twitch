# M Twitch Design System

## Product Direction

M Twitch is a complete Android Twitch client being built through a VOD-first prototype. The prototype interface should feel calm, fast, and playback-centered. It should prioritize resuming content, scanning VOD lists, and controlling playback without visual noise while leaving room for later live chat, chat replay, and emote surfaces.

The first foundation also includes visible homes for live playback, chat, replay, plugin settings, and diagnostics so future work extends the app instead of redesigning it.

## Visual Style

- Dark-first interface tuned for video viewing.
- High-contrast text on dark surfaces.
- One accent color for active playback and progress.
- No decorative gradients, floating blobs, or oversized marketing sections.
- Rounded corners stay at 8dp or below unless the final Claude Design handoff changes that rule.

## Layout

- Phone-first portrait layout.
- Bottom navigation is avoided in MVP-A; Home and Settings provide explicit
  entry points until the primary live/VOD/chat destinations are proven.
- Home links to channel lookup, recent VODs, settings, and player resume.
- Player controls appear as an overlay on top of the video surface.
- Landscape player prioritizes video and hides nonessential UI.
- Chat surfaces collapse behind a clear affordance on phone-sized screens.
- Plugin settings are grouped under Settings until chat becomes a primary tab.

## Motion

- Navigation transitions should be quick and quiet.
- Player controls fade in and out.
- Scrubber feedback should be immediate.
- Loading states use simple progress indicators.

## Accessibility

- Touch targets should be at least 48dp.
- Icon-only controls need content descriptions.
- Text contrast should remain readable on dark video-adjacent surfaces.
- Scrubber elapsed and remaining time should be visible when controls are shown.
