package dev.mitchell.mtwitch.plugin.chat

import dev.mitchell.mtwitch.core.model.ChannelId
import dev.mitchell.mtwitch.data.chat.ChatEvent
import dev.mitchell.mtwitch.data.chat.ChatMessage
import dev.mitchell.mtwitch.data.chat.ChatMessageFragment
import dev.mitchell.mtwitch.data.chat.ChatMessageId
import dev.mitchell.mtwitch.data.chat.ChatUserId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatPluginRegistryTest {
    private val sampleEvent = ChatEvent.MessageReceived(
        channelId = ChannelId("channel-1"),
        message = ChatMessage(
            id = ChatMessageId("message-1"),
            userId = ChatUserId("user-1"),
            userLogin = "viewer",
            userDisplayName = "Viewer",
            fragments = listOf(ChatMessageFragment.Text("hello")),
            timestampEpochMs = 1_000L,
        ),
    )

    @Test
    fun registryDispatchesToEnabledDefaultPlugin() {
        val registry = ChatPluginRegistry(
            plugins = listOf(DefaultChatPlugin),
            enabledPluginIds = setOf(DefaultChatPlugin.id),
        )

        val actions = registry.dispatch(sampleEvent)

        assertEquals(
            listOf(
                ChatPluginAction.AddLocalNotice(
                    pluginId = "default-chat",
                    message = "Default chat plugin observed: hello",
                ),
            ),
            actions,
        )
    }

    @Test
    fun registrySkipsDisabledPlugins() {
        val registry = ChatPluginRegistry(
            plugins = listOf(DefaultChatPlugin),
            enabledPluginIds = emptySet(),
        )

        assertTrue(registry.dispatch(sampleEvent).isEmpty())
    }

    @Test
    fun registryTurnsPluginFailuresIntoDiagnostics() {
        val brokenPlugin = object : ChatPlugin {
            override val id: String = "broken"
            override val displayName: String = "Broken"
            override val enabledByDefault: Boolean = true

            override fun handle(event: ChatEvent): List<ChatPluginAction> {
                error("boom")
            }
        }
        val registry = ChatPluginRegistry(
            plugins = listOf(brokenPlugin),
            enabledPluginIds = setOf("broken"),
        )

        val actions = registry.dispatch(sampleEvent)

        assertEquals(
            listOf(ChatPluginAction.PluginFailed(pluginId = "broken", reason = "boom")),
            actions,
        )
    }
}
