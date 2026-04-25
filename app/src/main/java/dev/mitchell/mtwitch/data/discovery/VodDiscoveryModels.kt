package dev.mitchell.mtwitch.data.discovery

import dev.mitchell.mtwitch.core.model.ChannelId
import dev.mitchell.mtwitch.core.model.Vod

data class Channel(
    val id: ChannelId,
    val login: String,
    val displayName: String,
    val vods: List<Vod>,
)

sealed interface VodDiscoveryResult {
    data class Content(
        val channel: Channel,
        val vods: List<Vod>,
    ) : VodDiscoveryResult

    data class Empty(val channel: Channel) : VodDiscoveryResult
    data class Error(val reason: VodDiscoveryError) : VodDiscoveryResult
}

enum class VodDiscoveryError {
    ChannelNotFound,
    NetworkTimeout,
    RateLimited,
    Unauthorized,
    Unknown,
}

interface VodDiscoveryRepository {
    suspend fun openChannel(channelLogin: String): VodDiscoveryResult
}
