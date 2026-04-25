package dev.mitchell.mtwitch.data.playback

import dev.mitchell.mtwitch.core.model.ChannelId
import dev.mitchell.mtwitch.core.model.VideoId
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FakePlaybackSourceResolverTest {
    @Test
    fun resolverReturnsConfiguredSource() = runTest {
        val resolver = FakePlaybackSourceResolver(
            result = PlaybackSourceResult.Ready(
                source = PlaybackSource(
                    request = PlaybackRequest.Vod(VideoId("123")),
                    hlsUrl = "https://example.com/vod.m3u8",
                    isLive = false,
                    timelineIssues = listOf(
                        PlaybackTimelineIssue(
                            startMs = 30_000L,
                            endMs = 45_000L,
                            reason = PlaybackTimelineIssueReason.MutedSegment,
                        ),
                    ),
                ),
            ),
        )

        val result = resolver.resolve(PlaybackRequest.Vod(VideoId("123")))

        assertEquals(
            PlaybackSourceResult.Ready(
                PlaybackSource(
                    request = PlaybackRequest.Vod(VideoId("123")),
                    hlsUrl = "https://example.com/vod.m3u8",
                    isLive = false,
                    timelineIssues = listOf(
                        PlaybackTimelineIssue(
                            startMs = 30_000L,
                            endMs = 45_000L,
                            reason = PlaybackTimelineIssueReason.MutedSegment,
                        ),
                    ),
                ),
            ),
            result,
        )
    }

    @Test
    fun resolverCanReturnUnavailable() = runTest {
        val resolver = FakePlaybackSourceResolver(
            result = PlaybackSourceResult.Unavailable(PlaybackUnavailableReason.SourceMissing),
        )

        val result = resolver.resolve(PlaybackRequest.Vod(VideoId("missing")))

        assertTrue(result is PlaybackSourceResult.Unavailable)
    }

    @Test
    fun resolverAcceptsLivePlaybackRequests() = runTest {
        val request = PlaybackRequest.Live(
            channelId = ChannelId("channel-1"),
            channelLogin = "cohhcarnage",
        )
        val resolver = FakePlaybackSourceResolver(
            result = PlaybackSourceResult.Ready(
                source = PlaybackSource(
                    request = request,
                    hlsUrl = "https://example.com/live.m3u8",
                    isLive = true,
                    timelineIssues = emptyList(),
                ),
            ),
        )

        val result = resolver.resolve(request)

        assertEquals(
            PlaybackSourceResult.Ready(
                PlaybackSource(
                    request = request,
                    hlsUrl = "https://example.com/live.m3u8",
                    isLive = true,
                    timelineIssues = emptyList(),
                ),
            ),
            result,
        )
    }
}
