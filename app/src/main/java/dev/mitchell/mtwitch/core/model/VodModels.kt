package dev.mitchell.mtwitch.core.model

import kotlin.math.max
import kotlin.math.min
import kotlin.time.Duration

@JvmInline
value class VideoId(val value: String)

@JvmInline
value class ChannelId(val value: String)

data class WatchProgress(
    val videoId: VideoId,
    val positionMs: Long,
    val durationMs: Long?,
    val updatedAtEpochMs: Long,
) {
    val fraction: Float
        get() {
            val knownDurationMs = durationMs ?: return 0f
            if (knownDurationMs <= 0L) return 0f
            val clampedPosition = min(max(positionMs, 0L), knownDurationMs)
            return clampedPosition.toFloat() / knownDurationMs.toFloat()
        }
}

data class Vod(
    val id: VideoId,
    val channelId: ChannelId,
    val channelLogin: String,
    val title: String,
    val thumbnailUrl: String,
    val duration: Duration,
    val publishedAtEpochMs: Long,
    val progress: WatchProgress?,
) {
    val hasProgress: Boolean = progress != null && progress.positionMs > 0L
}
