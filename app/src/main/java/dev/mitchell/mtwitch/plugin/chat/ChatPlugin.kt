package dev.mitchell.mtwitch.plugin.chat

import dev.mitchell.mtwitch.data.chat.ChatEvent

interface ChatPlugin {
    val id: String
    val displayName: String
    val enabledByDefault: Boolean

    fun handle(event: ChatEvent): List<ChatPluginAction>
}

sealed interface ChatPluginAction {
    val pluginId: String

    data class AddLocalNotice(
        override val pluginId: String,
        val message: String,
    ) : ChatPluginAction

    data class PluginFailed(
        override val pluginId: String,
        val reason: String,
    ) : ChatPluginAction
}

class ChatPluginRegistry(
    private val plugins: List<ChatPlugin>,
    private val enabledPluginIds: Set<String> = plugins
        .filter { it.enabledByDefault }
        .map { it.id }
        .toSet(),
) {
    fun dispatch(event: ChatEvent): List<ChatPluginAction> {
        return plugins
            .filter { plugin -> plugin.id in enabledPluginIds }
            .flatMap { plugin ->
                try {
                    plugin.handle(event)
                } catch (error: Throwable) {
                    listOf(
                        ChatPluginAction.PluginFailed(
                            pluginId = plugin.id,
                            reason = error.message ?: error.javaClass.simpleName,
                        ),
                    )
                }
            }
    }
}
