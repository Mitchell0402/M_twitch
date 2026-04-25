package dev.mitchell.mtwitch.feature.player

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.state.rememberPlayPauseButtonState
import androidx.media3.ui.compose.state.rememberProgressStateWithTickInterval
import dev.mitchell.mtwitch.core.local.WatchHistoryRepository
import dev.mitchell.mtwitch.core.model.VideoId
import dev.mitchell.mtwitch.core.model.WatchProgress
import dev.mitchell.mtwitch.data.playback.PlaybackRequest
import dev.mitchell.mtwitch.data.playback.PlaybackSourceResolver
import dev.mitchell.mtwitch.data.playback.PlaybackSourceResult
import dev.mitchell.mtwitch.data.playback.PlaybackUnavailableReason
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PlayerScreen(
    videoId: String,
    channelLogin: String,
    playbackResolver: PlaybackSourceResolver,
    watchHistoryRepository: WatchHistoryRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val player = remember {
        ExoPlayer.Builder(context).build()
    }
    val videoIdValue = remember(videoId) { VideoId(videoId) }
    var sourceResult by remember(videoId, channelLogin) {
        mutableStateOf<PlaybackSourceResult?>(null)
    }

    fun saveProgress() {
        val durationMs = player.duration.takeIf { duration -> duration != C.TIME_UNSET }
        scope.launch {
            watchHistoryRepository.saveProgress(
                WatchProgress(
                    videoId = videoIdValue,
                    positionMs = player.currentPosition,
                    durationMs = durationMs,
                    updatedAtEpochMs = System.currentTimeMillis(),
                ),
            )
        }
    }

    LaunchedEffect(videoId, channelLogin) {
        sourceResult = null
        val resolved = playbackResolver.resolve(
            PlaybackRequest.Vod(
                videoId = videoIdValue,
                channelLogin = channelLogin,
            ),
        )
        sourceResult = resolved
        if (resolved is PlaybackSourceResult.Ready) {
            val savedProgress = watchHistoryRepository.progressFor(videoIdValue)
            player.setMediaItem(MediaItem.fromUri(resolved.source.hlsUrl))
            player.prepare()
            savedProgress?.let { progress -> player.seekTo(progress.positionMs) }
            player.playWhenReady = true
        }
    }

    LaunchedEffect(player, videoId) {
        while (true) {
            delay(5_000L)
            if (player.currentMediaItem != null) {
                saveProgress()
            }
        }
    }

    DisposableEffect(player, lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    saveProgress()
                    player.pause()
                }
                Lifecycle.Event.ON_RESUME -> Unit
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            saveProgress()
            lifecycleOwner.lifecycle.removeObserver(observer)
            player.release()
        }
    }

    PlayerContent(
        videoId = videoId,
        channelLogin = channelLogin,
        player = player,
        sourceResult = sourceResult,
        onBack = {
            saveProgress()
            onBack()
        },
        onSaveProgress = ::saveProgress,
        modifier = modifier,
    )
}

@Composable
@OptIn(UnstableApi::class)
private fun PlayerContent(
    videoId: String,
    channelLogin: String,
    player: Player,
    sourceResult: PlaybackSourceResult?,
    onBack: () -> Unit,
    onSaveProgress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val playPauseState = rememberPlayPauseButtonState(player)
    val progressState = rememberProgressStateWithTickInterval(player, 500L)
    val durationMs = progressState.durationMs.takeIf { duration -> duration > 0L && duration != C.TIME_UNSET } ?: 0L
    val progressFraction = if (durationMs > 0L) {
        progressState.currentPositionMs.toFloat() / durationMs.toFloat()
    } else {
        0f
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            if (sourceResult is PlaybackSourceResult.Ready) {
                PlayerSurface(
                    player = player,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Text(
                    text = sourceResult.toPlayerMessage(channelLogin, videoId),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(24.dp),
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (player.playbackState == Player.STATE_BUFFERING) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            } else {
                LinearProgressIndicator(
                    progress = { progressFraction.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                )
            }
            Slider(
                value = progressFraction.coerceIn(0f, 1f),
                onValueChange = { value ->
                    if (durationMs > 0L) {
                        player.seekTo((durationMs * value).toLong())
                    }
                },
                onValueChangeFinished = onSaveProgress,
                enabled = durationMs > 0L,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(onClick = onBack) {
                    Text(text = "Back")
                }
                Text(
                    text = "${formatPlaybackTime(progressState.currentPositionMs)} / ${formatPlaybackTime(durationMs)}",
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Button(
                    onClick = {
                        if (!playPauseState.showPlay) {
                            onSaveProgress()
                        }
                        playPauseState.onClick()
                    },
                    enabled = playPauseState.isEnabled,
                ) {
                    Text(text = if (playPauseState.showPlay) "Play" else "Pause")
                }
            }
        }
    }
}

fun formatPlaybackTime(positionMs: Long): String {
    if (positionMs < 0L) return "--:--"
    val totalSeconds = positionMs / 1_000L
    val hours = totalSeconds / 3_600L
    val minutes = (totalSeconds % 3_600L) / 60L
    val seconds = totalSeconds % 60L
    return if (hours > 0L) {
        "$hours:${minutes.twoDigits()}:${seconds.twoDigits()}"
    } else {
        "${minutes.twoDigits()}:${seconds.twoDigits()}"
    }
}

private fun Long.twoDigits(): String = toString().padStart(2, '0')

private fun PlaybackSourceResult?.toPlayerMessage(
    channelLogin: String,
    videoId: String,
): String {
    return when (this) {
        null -> "Resolving playback source"
        is PlaybackSourceResult.Ready -> "$channelLogin VOD: $videoId"
        is PlaybackSourceResult.Unavailable -> reason.toPlayerMessage()
    }
}

private fun PlaybackUnavailableReason.toPlayerMessage(): String {
    return when (this) {
        PlaybackUnavailableReason.SourceMissing -> "Playback source is missing."
        PlaybackUnavailableReason.TokenFailed,
        PlaybackUnavailableReason.TokenRejected
        -> "Could not resolve playback token."
        PlaybackUnavailableReason.ManifestForbidden -> "This VOD manifest is restricted."
        PlaybackUnavailableReason.ManifestMalformed -> "Playback manifest could not be read."
        PlaybackUnavailableReason.NoVariants -> "Playback manifest has no playable variants."
        PlaybackUnavailableReason.NetworkTimeout -> "Network timed out while resolving playback."
        PlaybackUnavailableReason.StreamOffline -> "Stream is offline."
        PlaybackUnavailableReason.AuthRequired -> "This playback source requires login."
        PlaybackUnavailableReason.RateLimited -> "Playback was rate limited. Try again soon."
    }
}
