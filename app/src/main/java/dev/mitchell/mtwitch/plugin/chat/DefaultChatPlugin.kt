package dev.mitchell.mtwitch.plugin.chat

import dev.mitchell.mtwitch.data.chat.ChatEvent

data object DefaultChatPlugin : ChatPlugin {
    override val id: String = "default-chat"
    override val displayName: String = "Default Chat"
    override val enabledByDefault: Boolean = true

    override fun handle(event: ChatEvent): List<ChatPluginAction> {
        return when (event) {
            is ChatEvent.MessageReceived -> listOf(
                ChatPluginAction.AddLocalNotice(
                    pluginId = id,
                    message = "Default chat plugin observed: ${event.message.plainText}",
                ),
            )
            is ChatEvent.MessageDeleted,
            is ChatEvent.ConnectionStateChanged -> emptyList()
        }
    }
}
