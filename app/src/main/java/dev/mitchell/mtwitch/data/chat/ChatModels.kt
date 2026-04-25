package dev.mitchell.mtwitch.data.chat

import dev.mitchell.mtwitch.core.model.ChannelId

@JvmInline
value class ChatMessageId(val value: String)

@JvmInline
value class ChatUserId(val value: String)

@JvmInline
value class EmoteId(val value: String)

enum class ChatConnectionState {
    Disconnected,
    Connecting,
    Connected,
    Reconnecting,
    RateLimited,
}

sealed interface ChatMessageFragment {
    val text: String

    data class Text(override val text: String) : ChatMessageFragment

    data class Emote(
        override val text: String,
        val emoteId: EmoteId,
        val imageUrl: String?,
    ) : ChatMessageFragment
}

data class ChatMessage(
    val id: ChatMessageId,
    val userId: ChatUserId,
    val userLogin: String,
    val userDisplayName: String,
    val fragments: List<ChatMessageFragment>,
    val timestampEpochMs: Long,
) {
    val plainText: String = fragments.joinToString(separator = "") { it.text }
}

sealed interface ChatEvent {
    data class MessageReceived(
        val channelId: ChannelId,
        val message: ChatMessage,
    ) : ChatEvent

    data class MessageDeleted(
        val channelId: ChannelId,
        val messageId: ChatMessageId,
    ) : ChatEvent

    data class ConnectionStateChanged(
        val channelId: ChannelId,
        val state: ChatConnectionState,
    ) : ChatEvent
}

data class EmoteReference(
    val providerId: String,
    val emoteId: EmoteId,
    val code: String,
    val imageUrl: String,
)

interface EmoteProvider {
    val id: String

    suspend fun resolve(
        code: String,
        channelId: ChannelId,
    ): EmoteReference?
}
