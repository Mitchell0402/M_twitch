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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.mitchell.mtwitch.core.model.ChannelId
import dev.mitchell.mtwitch.core.model.VideoId
import dev.mitchell.mtwitch.core.model.Vod
import kotlin.time.Duration.Companion.hours

@Composable
fun ChannelVodListScreen(
    channelLogin: String,
    onOpenVod: (VideoId) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sampleVods = listOf(
        Vod(
            id = VideoId("sample-1"),
            channelId = ChannelId("sample-channel"),
            channelLogin = channelLogin,
            title = "Sample archived broadcast",
            thumbnailUrl = "",
            duration = 3.hours,
            publishedAtEpochMs = 0L,
            progress = null,
        ),
    )

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
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(sampleVods) { vod ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenVod(vod.id) },
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = vod.title, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "Duration: ${vod.duration.inWholeHours}h",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
