package dev.mitchell.mtwitch.data.playback

import dev.mitchell.mtwitch.core.model.ChannelId
import dev.mitchell.mtwitch.core.model.VideoId

sealed interface PlaybackRequest {
    data class Vod(
        val videoId: VideoId,
        val channelLogin: String = "",
    ) : PlaybackRequest

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
    val diagnostics: PlaybackDiagnostics = PlaybackDiagnostics(),
)

typealias PlayableSource = PlaybackSource

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
    TokenFailed,
    ManifestForbidden,
    ManifestMalformed,
    NoVariants,
    TokenRejected,
    NetworkTimeout,
    StreamOffline,
    AuthRequired,
    RateLimited,
}

data class PlaybackDiagnostics(
    val operation: String? = null,
    val httpStatus: Int? = null,
    val manifestStatus: Int? = null,
    val variantCount: Int? = null,
    val sanitizedUrl: String? = null,
) {
    companion object {
        fun redactUrl(url: String): String {
            return url
                .replace(Regex("([?&]nauthsig=)[^&]+"), "$1<redacted>")
                .replace(Regex("([?&]nauth=)[^&]+"), "$1<redacted>")
                .replace(Regex("([?&]sig=)[^&]+"), "$1<redacted>")
                .replace(Regex("([?&]token=)[^&]+"), "$1<redacted>")
        }
    }
}

interface PlaybackSourceResolver {
    suspend fun resolve(request: PlaybackRequest): PlaybackSourceResult
}
