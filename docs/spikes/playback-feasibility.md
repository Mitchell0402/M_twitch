# Playback Feasibility Spike

Date: 2026-04-25

## Result

Pass for the M2 gate: `channel login -> VOD ID -> PlaybackAccessToken -> playable HLS manifest` works for tested public VODs without Twitch OAuth.

This spike should unblock an isolated M3 playback resolver plan, with one caveat: the playback-token path is Twitch web GraphQL plus usher HLS behavior, not a stable Helix playback API. Keep it behind a small playback provider boundary and expose typed failures to the app.

## Tooling

Script:

```powershell
$env:TWITCH_GQL_CLIENT_ID='<redacted>'
node scripts/spikes/playback-feasibility.mjs
```

The script uses Node's built-in `fetch` only. No production dependencies were added.

The `TWITCH_GQL_CLIENT_ID` value used during the run was the public Twitch web GraphQL client ID and is intentionally redacted here. No OAuth access token, cookie, `PlaybackAccessToken.value`, or `PlaybackAccessToken.signature` was written to the repo.

## Tested Inputs

Channel tested: `lirik`

| Case | Channel | VOD ID | Title | Created at | Length | Result |
| --- | --- | --- | --- | --- | --- | --- |
| Normal VOD | `lirik` | `2755960234` | `Poggoling` | `2026-04-24T16:13:12Z` | `22710s` | Token 200, master manifest 200, variant playlist 200 |
| Muted / partially muted VOD | `lirik` | `2755173127` | `Gaming` | `2026-04-23T16:02:44Z` | `23320s` | Token 200, master manifest 200, variant playlist 200, muted segments observed |

The script also queried recent archived videos for `lirik` and confirmed both VOD IDs were returned from the channel lookup path.

## Token Request Shape

Endpoint:

```text
POST https://gql.twitch.tv/gql
```

Required headers observed:

```http
Client-ID: <redacted>
Content-Type: application/json
User-Agent: M_twitch playback feasibility spike/2026-04-25
```

OAuth was not required for the tested public VODs.

Body shape:

```json
{
  "operationName": "PlaybackAccessToken",
  "variables": {
    "login": "lirik",
    "isLive": false,
    "vodID": "<vod-id>",
    "isVod": true,
    "playerType": "site"
  },
  "query": "query PlaybackAccessToken(...) { streamPlaybackAccessToken(...) @include(if: $isLive) { value signature } videoPlaybackAccessToken(...) @include(if: $isVod) { value signature } }"
}
```

Observed token response:

| Case | Status | Token value length | Signature length |
| --- | ---: | ---: | ---: |
| Normal VOD `2755960234` | 200 | 444 | 40 |
| Muted VOD `2755173127` | 200 | 444 | 40 |

The token `value` and `signature` are treated as secrets and must stay out of logs, diagnostics, docs, and crash reports.

## Manifest URL Shape

Master manifest URL shape:

```text
https://usher.ttvnw.net/vod/{vodID}.m3u8?allow_source=true&allow_audio_only=true&allow_spectre=true&player=twitchweb&nauthsig=<redacted>&nauth=<redacted>
```

Observed master manifest behavior:

| Case | Master status | Master bytes | Variant count | First variant status | First variant segments |
| --- | ---: | ---: | ---: | ---: | ---: |
| Normal VOD `2755960234` | 200 | 2072 | 6 | 200 | 2271 |
| Muted VOD `2755173127` | 200 | 2150 | 6 | 200 | 2332 |

The master playlist contained HLS variants such as `1080p60`, `720p60`, `480p`, `360p`, `160p`, and `Audio Only`. This is suitable for a Media3 `MediaItem` pointed at the signed master URL, letting ExoPlayer choose variants automatically for M3.

Variant URL shape observed:

```text
https://<cloudfront-host>/<vod-path>/<quality>/index-dvr.m3u8
https://<cloudfront-host>/<vod-path>/<quality>/index-muted-<id>.m3u8
```

The tested muted VOD used muted variant playlist names and muted segment filenames:

```text
1746-muted.ts ... 1763-muted.ts
1818-muted.ts ... 1835-muted.ts
```

Muted segment summary for `2755173127`:

| Muted segment count | Ranges |
| ---: | --- |
| 36 | `1746-1763`, `1818-1835` |

No `#EXT-X-DISCONTINUITY` tags were observed in the two first-variant playlists, but M3 should still model muted or discontinuous timeline issues because other VODs can differ.

## Failure Modes

Observed:

- Missing `TWITCH_GQL_CLIENT_ID`: script exits before network calls.
- PowerShell `Invoke-RestMethod` failed in this environment with `Authentication failed, see inner exception`; Node `fetch` succeeded against the same endpoint.
- Restricted or unavailable VOD probe: `criticalrole` VOD `2755588056` returned a playback token with HTTP 200, but the usher master manifest returned HTTP 403. The playback layer must treat token success and manifest success as separate steps.

Expected:

- GraphQL token request can fail with 4xx, schema, persisted-query, integrity, or client-header changes.
- Channel login can resolve but have no recent archived broadcasts.
- VOD can appear in metadata but be deleted, private, subscriber-only, age/region restricted, or temporarily unavailable at manifest time.
- Master manifest can return no variants, only audio, changed quality labels, or CDN URLs that do not match today's shape.
- Muted VODs can have `index-muted-*` playlists and `*-muted.ts` segments; timeline handling must not assume every segment contains normal audio.
- Token query and usher parameters may change because this is not an official Helix playback contract.

## Implementation Notes For M3

- Keep Helix or official metadata lookup separate from playback URL resolution.
- Put GraphQL token and usher HLS logic in a small isolated playback provider.
- Return typed errors for channel lookup, token unavailable, manifest forbidden, manifest malformed, no playable variants, and network timeout.
- Do not require OAuth for public VOD playback unless a specific VOD or account-only feature demands it.
- Log only redacted diagnostics: HTTP status, operation name, VOD ID, variant count, and token/signature length if needed. Never log token values, signatures, cookies, or full signed manifest URLs.
- Start Media3 with automatic quality selection from the signed master manifest. Manual quality selection can be added after the source path is stable.
