const CLIENT_ID = process.env.TWITCH_GQL_CLIENT_ID;
const USER_AGENT = "M_twitch playback feasibility spike/2026-04-25";

if (!CLIENT_ID) {
  console.error("Set TWITCH_GQL_CLIENT_ID before running this spike.");
  process.exit(2);
}

const graphQlUrl = "https://gql.twitch.tv/gql";
const headers = {
  "Client-ID": CLIENT_ID,
  "Content-Type": "application/json",
  "User-Agent": USER_AGENT,
};

const channelLogin = "lirik";
const cases = [
  {
    kind: "normal",
    channelLogin,
    vodId: "2755960234",
    expectedTitlePrefix: "Poggoling",
  },
  {
    kind: "muted_or_partially_muted",
    channelLogin,
    vodId: "2755173127",
    expectedTitlePrefix: "Gaming",
  },
];

const failureProbe = {
  kind: "restricted_or_unavailable_probe",
  channelLogin: "criticalrole",
  vodId: "2755588056",
};

const channelVideosQuery = `
query ChannelVideos($login: String!, $limit: Int!) {
  user(login: $login) {
    id
    login
    displayName
    videos(first: $limit, sort: TIME, type: ARCHIVE) {
      edges {
        node {
          id
          title
          lengthSeconds
          createdAt
          broadcastType
        }
      }
    }
  }
}`;

const playbackTokenQuery = `
query PlaybackAccessToken(
  $login: String!,
  $isLive: Boolean!,
  $vodID: ID!,
  $isVod: Boolean!,
  $playerType: String!
) {
  streamPlaybackAccessToken(
    channelName: $login,
    params: { platform: "web", playerBackend: "mediaplayer", playerType: $playerType }
  ) @include(if: $isLive) {
    value
    signature
  }
  videoPlaybackAccessToken(
    id: $vodID,
    params: { platform: "web", playerBackend: "mediaplayer", playerType: $playerType }
  ) @include(if: $isVod) {
    value
    signature
  }
}`;

async function gql(operationName, variables, query) {
  const response = await fetch(graphQlUrl, {
    method: "POST",
    headers,
    body: JSON.stringify({ operationName, variables, query }),
  });
  const text = await response.text();
  let json;
  try {
    json = JSON.parse(text);
  } catch {
    json = { parseError: text.slice(0, 200) };
  }
  return { status: response.status, ok: response.ok, json };
}

function usherVodUrl(vodId, token) {
  return `https://usher.ttvnw.net/vod/${vodId}.m3u8?allow_source=true&allow_audio_only=true&allow_spectre=true&player=twitchweb&nauthsig=${encodeURIComponent(token.signature)}&nauth=${encodeURIComponent(token.value)}`;
}

function parseVariantUrls(masterManifest) {
  return masterManifest
    .split(/\r?\n/)
    .filter((line) => line.startsWith("https://") && line.includes(".m3u8"));
}

function summarizeMutedSegments(variantManifest) {
  const mutedSegmentNumbers = variantManifest
    .split(/\r?\n/)
    .map((line) => line.match(/^(\d+)-muted\.ts$/)?.[1])
    .filter(Boolean)
    .map(Number);

  if (mutedSegmentNumbers.length === 0) {
    return { count: 0, ranges: [] };
  }

  const ranges = [];
  let start = mutedSegmentNumbers[0];
  let previous = start;

  for (const current of mutedSegmentNumbers.slice(1)) {
    if (current === previous + 1) {
      previous = current;
      continue;
    }
    ranges.push({ start, end: previous });
    start = current;
    previous = current;
  }
  ranges.push({ start, end: previous });

  return { count: mutedSegmentNumbers.length, ranges };
}

async function fetchText(url) {
  const response = await fetch(url, { headers: { "User-Agent": USER_AGENT } });
  return { status: response.status, ok: response.ok, text: await response.text() };
}

async function resolveCase(testCase) {
  const channelResult = await gql("ChannelVideos", { login: testCase.channelLogin, limit: 5 }, channelVideosQuery);
  const videos = channelResult.json.data?.user?.videos?.edges?.map((edge) => edge.node) ?? [];
  const selectedVideo = videos.find((video) => video.id === testCase.vodId) ?? null;

  const tokenResult = await gql(
    "PlaybackAccessToken",
    {
      login: testCase.channelLogin,
      isLive: false,
      vodID: testCase.vodId,
      isVod: true,
      playerType: "site",
    },
    playbackTokenQuery,
  );
  const token = tokenResult.json.data?.videoPlaybackAccessToken ?? null;
  if (!token) {
    return {
      ...testCase,
      channelLookupStatus: channelResult.status,
      vodFoundInRecentChannelVideos: Boolean(selectedVideo),
      tokenStatus: tokenResult.status,
      tokenAvailable: false,
      errors: tokenResult.json.errors?.map((error) => error.message) ?? [],
    };
  }

  const masterUrl = usherVodUrl(testCase.vodId, token);
  const masterResult = await fetchText(masterUrl);
  const variantUrls = masterResult.ok ? parseVariantUrls(masterResult.text) : [];
  const firstVariantUrl = variantUrls[0] ?? null;
  const firstVariantResult = firstVariantUrl ? await fetchText(firstVariantUrl) : null;
  const variantManifest = firstVariantResult?.text ?? "";
  const mutedSegments = summarizeMutedSegments(variantManifest);

  return {
    ...testCase,
    channelLookupStatus: channelResult.status,
    channelId: channelResult.json.data?.user?.id ?? null,
    vodFoundInRecentChannelVideos: Boolean(selectedVideo),
    vodTitle: selectedVideo?.title ?? null,
    vodCreatedAt: selectedVideo?.createdAt ?? null,
    vodLengthSeconds: selectedVideo?.lengthSeconds ?? null,
    tokenStatus: tokenResult.status,
    tokenAvailable: true,
    tokenValueLength: token.value.length,
    tokenSignatureLength: token.signature.length,
    masterManifestStatus: masterResult.status,
    masterManifestBytes: masterResult.text.length,
    variantCount: variantUrls.length,
    firstVariantStatus: firstVariantResult?.status ?? null,
    firstVariantBytes: variantManifest.length,
    firstVariantSegmentCount: (variantManifest.match(/#EXTINF:/g) ?? []).length,
    discontinuityCount: (variantManifest.match(/#EXT-X-DISCONTINUITY/g) ?? []).length,
    mutedSegments,
    manifestUrlShape:
      "https://usher.ttvnw.net/vod/{vodID}.m3u8?allow_source=true&allow_audio_only=true&allow_spectre=true&player=twitchweb&nauthsig=<redacted>&nauth=<redacted>",
    firstVariantUrlShape: firstVariantUrl
      ? firstVariantUrl.replace(testCase.vodId, "{vodID}").replace(/index-[^/]+\.m3u8$/, "index-{quality-or-muted-id}.m3u8")
      : null,
  };
}

async function probeFailure(testCase) {
  const tokenResult = await gql(
    "PlaybackAccessToken",
    {
      login: testCase.channelLogin,
      isLive: false,
      vodID: testCase.vodId,
      isVod: true,
      playerType: "site",
    },
    playbackTokenQuery,
  );
  const token = tokenResult.json.data?.videoPlaybackAccessToken ?? null;
  if (!token) {
    return { ...testCase, tokenStatus: tokenResult.status, tokenAvailable: false };
  }

  const manifestResult = await fetchText(usherVodUrl(testCase.vodId, token));
  return {
    ...testCase,
    tokenStatus: tokenResult.status,
    tokenAvailable: true,
    masterManifestStatus: manifestResult.status,
    masterManifestBytes: manifestResult.text.length,
  };
}

const results = [];
for (const testCase of cases) {
  results.push(await resolveCase(testCase));
}

const restrictedProbe = await probeFailure(failureProbe);
const pass =
  results.find((result) => result.kind === "normal")?.masterManifestStatus === 200 &&
  results.find((result) => result.kind === "normal")?.firstVariantStatus === 200 &&
  results.find((result) => result.kind === "muted_or_partially_muted")?.masterManifestStatus === 200 &&
  results.find((result) => result.kind === "muted_or_partially_muted")?.firstVariantStatus === 200 &&
  (results.find((result) => result.kind === "muted_or_partially_muted")?.mutedSegments.count ?? 0) > 0;

console.log(
  JSON.stringify(
    {
      generatedAt: new Date().toISOString(),
      tool: "node scripts/spikes/playback-feasibility.mjs",
      runtime: `Node ${process.version}`,
      clientId: "<redacted>",
      userAgent: USER_AGENT,
      pass,
      cases: results,
      failureProbe: restrictedProbe,
    },
    null,
    2,
  ),
);

process.exit(pass ? 0 : 1);
