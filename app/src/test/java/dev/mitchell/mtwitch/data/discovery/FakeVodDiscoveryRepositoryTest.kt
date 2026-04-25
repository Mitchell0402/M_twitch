package dev.mitchell.mtwitch.data.discovery

import dev.mitchell.mtwitch.core.model.ChannelId
import dev.mitchell.mtwitch.core.model.VideoId
import dev.mitchell.mtwitch.core.model.Vod
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Duration.Companion.hours

class FakeVodDiscoveryRepositoryTest {
    @Test
    fun openChannelReturnsVodsForKnownLogin() = runTest {
        val repository = FakeVodDiscoveryRepository(
            channels = listOf(
                Channel(
                    id = ChannelId("channel-1"),
                    login = "lirik",
                    displayName = "LIRIK",
                    vods = listOf(sampleVod("1", "lirik")),
                ),
            ),
        )

        val result = repository.openChannel("lirik")

        assertEquals("LIRIK", (result as VodDiscoveryResult.Content).channel.displayName)
        assertEquals(listOf(VideoId("1")), result.vods.map { vod -> vod.id })
    }

    @Test
    fun openChannelReturnsEmptyForKnownChannelWithoutVods() = runTest {
        val repository = FakeVodDiscoveryRepository(
            channels = listOf(
                Channel(
                    id = ChannelId("channel-1"),
                    login = "quiet",
                    displayName = "Quiet",
                    vods = emptyList(),
                ),
            ),
        )

        val result = repository.openChannel("quiet")

        assertTrue(result is VodDiscoveryResult.Empty)
    }

    @Test
    fun openChannelReturnsNotFoundForUnknownLogin() = runTest {
        val repository = FakeVodDiscoveryRepository(channels = emptyList())

        val result = repository.openChannel("missing")

        assertEquals(
            VodDiscoveryResult.Error(VodDiscoveryError.ChannelNotFound),
            result,
        )
    }

    private fun sampleVod(id: String, channelLogin: String): Vod {
        return Vod(
            id = VideoId(id),
            channelId = ChannelId("channel-1"),
            channelLogin = channelLogin,
            title = "Sample broadcast",
            thumbnailUrl = "",
            duration = 2.hours,
            publishedAtEpochMs = 1_000L,
            progress = null,
        )
    }
}
