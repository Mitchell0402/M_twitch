package dev.mitchell.mtwitch.feature.plugins

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.mitchell.mtwitch.plugin.chat.DefaultChatPlugin

@Composable
fun ChatPluginSettingsScreen(
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
            text = "Chat Plugins",
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(text = DefaultChatPlugin.displayName)
        Switch(
            checked = DefaultChatPlugin.enabledByDefault,
            onCheckedChange = {},
        )
        Text(
            text = "The default local plugin is compiled into the app. Future plugins must use the same typed contract.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
