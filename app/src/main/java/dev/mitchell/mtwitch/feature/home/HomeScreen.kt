package dev.mitchell.mtwitch.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onOpenChannel: (String) -> Unit,
    onOpenLive: (String) -> Unit,
    onOpenChatPlugins: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var channelLogin by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "M Twitch",
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    text = "Resume or open a channel VOD list",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Button(onClick = onOpenSettings) {
                Text(text = "Settings")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = channelLogin,
            onValueChange = { channelLogin = it.trim() },
            label = { Text(text = "Channel login") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Button(
            onClick = { onOpenChannel(channelLogin) },
            enabled = channelLogin.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "Open VODs")
        }

        Button(
            onClick = { onOpenLive(channelLogin) },
            enabled = channelLogin.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "Open Live Placeholder")
        }

        Button(
            onClick = onOpenChatPlugins,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "Chat Plugins")
        }

        Text(
            text = "Recent VODs, live channels, and chat diagnostics will appear here as the client grows.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
