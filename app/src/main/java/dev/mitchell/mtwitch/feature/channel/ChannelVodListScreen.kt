package dev.mitchell.mtwitch.feature.channel

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.mitchell.mtwitch.core.model.VideoId
import dev.mitchell.mtwitch.core.model.Vod
import dev.mitchell.mtwitch.data.discovery.Channel
import dev.mitchell.mtwitch.data.discovery.VodDiscoveryError
import kotlin.time.Duration

sealed interface ChannelVodListUiState {
    data object Loading : ChannelVodListUiState
    data class Content(val channel: Channel, val vods: List<Vod>) : ChannelVodListUiState
    data class Empty(val channel: Channel) : ChannelVodListUiState
    data class Error(val reason: VodDiscoveryError) : ChannelVodListUiState
}

@Composable
fun ChannelVodListScreen(
    channelLogin: String,
    uiState: ChannelVodListUiState,
    onOpenVod: (VideoId) -> Unit,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Button(onClick = onBack) {
            Text(text = "Back")
        }
        Text(
            text = "$channelLogin VODs",
            style = MaterialTheme.typography.headlineSmall,
        )

        when (uiState) {
            ChannelVodListUiState.Loading -> LoadingState()
            is ChannelVodListUiState.Content -> VodList(uiState.vods, onOpenVod)
            is ChannelVodListUiState.Empty -> EmptyState(uiState.channel.displayName)
            is ChannelVodListUiState.Error -> ErrorState(uiState.reason, onRetry)
        }
    }
}

@Composable
private fun LoadingState() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CircularProgressIndicator()
        Text(
            text = "Loading recent broadcasts",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun VodList(
    vods: List<Vod>,
    onOpenVod: (VideoId) -> Unit,
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(vods) { vod ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenVod(vod.id) },
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(text = vod.title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "Duration: ${vod.duration.toDisplayText()}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (vod.hasProgress) {
                        LinearProgressIndicator(
                            progress = { vod.progress?.fraction ?: 0f },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(displayName: String) {
    Text(
        text = "$displayName has no recent archived broadcasts.",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun ErrorState(
    reason: VodDiscoveryError,
    onRetry: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = reason.toMessage(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(onClick = onRetry) {
            Text(text = "Retry")
        }
    }
}

private fun Duration.toDisplayText(): String {
    val hours = inWholeHours
    val minutes = (inWholeMinutes % 60).toString().padStart(2, '0')
    return "${hours}h ${minutes}m"
}

private fun VodDiscoveryError.toMessage(): String {
    return when (this) {
        VodDiscoveryError.ChannelNotFound -> "Channel was not found."
        VodDiscoveryError.NetworkTimeout -> "Network timed out while loading VODs."
        VodDiscoveryError.RateLimited -> "Twitch rate limited this request. Try again soon."
        VodDiscoveryError.Unauthorized -> "This channel requires authorization for this content."
        VodDiscoveryError.Unknown -> "Could not load this channel's VODs."
    }
}
