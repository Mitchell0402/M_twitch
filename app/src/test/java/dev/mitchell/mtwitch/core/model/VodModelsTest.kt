package dev.mitchell.mtwitch.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Duration.Companion.minutes

class VodModelsTest {
    @Test
    fun watchProgressFractionClampsIntoValidRange() {
        val progress = WatchProgress(
            videoId = VideoId("123"),
            positionMs = 75.minutes.inWholeMilliseconds,
            durationMs = 150.minutes.inWholeMilliseconds,
            updatedAtEpochMs = 1_000L,
        )

        assertEquals(0.5f, progress.fraction, 0.0001f)
        assertEquals(1f, progress.copy(positionMs = 10_000_000L).fraction, 0.0001f)
        assertEquals(0f, progress.copy(positionMs = -100L).fraction, 0.0001f)
        assertEquals(0f, progress.copy(durationMs = null).fraction, 0.0001f)
    }

    @Test
    fun vodKnowsWhetherItHasProgress() {
        val vod = Vod(
            id = VideoId("456"),
            channelId = ChannelId("channel-1"),
            channelLogin = "example",
            title = "Long broadcast",
            thumbnailUrl = "https://example.com/thumb.jpg",
            duration = 90.minutes,
            publishedAtEpochMs = 2_000L,
            progress = WatchProgress(
                videoId = VideoId("456"),
                positionMs = 30_000L,
                durationMs = 90.minutes.inWholeMilliseconds,
                updatedAtEpochMs = 3_000L,
            ),
        )

        assertTrue(vod.hasProgress)
        assertFalse(vod.copy(progress = null).hasProgress)
    }
}
