package dev.mitchell.mtwitch.navigation

import kotlinx.serialization.Serializable

sealed interface AppRoute {
    @Serializable
    data object Home : AppRoute

    @Serializable
    data object Settings : AppRoute

    @Serializable
    data object ChatPlugins : AppRoute

    @Serializable
    data class Channel(val channelLogin: String) : AppRoute

    @Serializable
    data class Player(
        val videoId: String,
        val channelLogin: String,
    ) : AppRoute

    @Serializable
    data class Live(val channelLogin: String) : AppRoute
}
