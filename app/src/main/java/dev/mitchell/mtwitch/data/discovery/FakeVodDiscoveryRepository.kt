package dev.mitchell.mtwitch.data.discovery

import dev.mitchell.mtwitch.core.model.ChannelId
import dev.mitchell.mtwitch.core.model.VideoId
import dev.mitchell.mtwitch.core.model.Vod
import kotlin.time.Duration.Companion.hours

class FakeVodDiscoveryRepository(
    private val channels: List<Channel> = defaultChannels,
) : VodDiscoveryRepository {
    override suspend fun openChannel(channelLogin: String): VodDiscoveryResult {
        val normalizedLogin = channelLogin.trim().lowercase()
        val channel = channels.firstOrNull { channel -> channel.login.lowercase() == normalizedLogin }
            ?: return VodDiscoveryResult.Error(VodDiscoveryError.ChannelNotFound)

        return if (channel.vods.isEmpty()) {
            VodDiscoveryResult.Empty(channel)
        } else {
            VodDiscoveryResult.Content(channel = channel, vods = channel.vods)
        }
    }

    companion object {
        private val defaultChannels = listOf(
            Channel(
                id = ChannelId("lirik"),
                login = "lirik",
                displayName = "LIRIK",
                vods = listOf(
                    Vod(
                        id = VideoId("2755960234"),
                        channelId = ChannelId("lirik"),
                        channelLogin = "lirik",
                        title = "Poggoling",
                        thumbnailUrl = "",
                        duration = 6.hours,
                        publishedAtEpochMs = 1_776_000_792_000L,
                        progress = null,
                    ),
                    Vod(
                        id = VideoId("2755173127"),
                        channelId = ChannelId("lirik"),
                        channelLogin = "lirik",
                        title = "Gaming",
                        thumbnailUrl = "",
                        duration = 6.hours,
                        publishedAtEpochMs = 1_775_913_764_000L,
                        progress = null,
                    ),
                ),
            ),
        )
    }
}
