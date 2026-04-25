package dev.mitchell.mtwitch.data.playback

import dev.mitchell.mtwitch.core.model.ChannelId
import dev.mitchell.mtwitch.core.model.VideoId

sealed interface PlaybackRequest {
    data class Vod(val videoId: VideoId) : PlaybackRequest

    data class Live(
        val channelId: ChannelId,
        val channelLogin: String,
    ) : PlaybackRequest
}

data class PlaybackSource(
    val request: PlaybackRequest,
    val hlsUrl: String,
    val isLive: Boolean,
    val timelineIssues: List<PlaybackTimelineIssue>,
)

data class PlaybackTimelineIssue(
    val startMs: Long,
    val endMs: Long,
    val reason: PlaybackTimelineIssueReason,
)

enum class PlaybackTimelineIssueReason {
    MutedSegment,
    Discontinuity,
    Unknown,
}

sealed interface PlaybackSourceResult {
    data class Ready(val source: PlaybackSource) : PlaybackSourceResult
    data class Unavailable(val reason: PlaybackUnavailableReason) : PlaybackSourceResult
}

enum class PlaybackUnavailableReason {
    SourceMissing,
    TokenRejected,
    NetworkTimeout,
    StreamOffline,
    AuthRequired,
    RateLimited,
}

interface PlaybackSourceResolver {
    suspend fun resolve(request: PlaybackRequest): PlaybackSourceResult
}
