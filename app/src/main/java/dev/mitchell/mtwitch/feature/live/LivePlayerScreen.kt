package dev.mitchell.mtwitch.feature.live

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LivePlayerScreen(
    channelLogin: String,
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
            text = "$channelLogin Live",
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = "Live playback will use the shared Media3 player contract after stream resolution is implemented.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = "Chat panel placeholder: default local plugin is enabled, real chat transport arrives in the chat milestone.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
