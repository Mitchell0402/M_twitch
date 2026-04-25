package dev.mitchell.mtwitch.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class AppRouteTest {
    @Test
    fun routesUseTypedValuesInsteadOfStringBuilders() {
        assertEquals(AppRoute.Home, AppRoute.Home)
        assertEquals(AppRoute.Settings, AppRoute.Settings)
        assertEquals(AppRoute.ChatPlugins, AppRoute.ChatPlugins)
    }

    @Test
    fun dynamicRoutesCarryTypedArguments() {
        assertEquals("cohhcarnage", AppRoute.Channel(channelLogin = "cohhcarnage").channelLogin)
        assertEquals("123456789", AppRoute.Player(videoId = "123456789", channelLogin = "lirik").videoId)
        assertEquals("lirik", AppRoute.Player(videoId = "123456789", channelLogin = "lirik").channelLogin)
        assertEquals("cohhcarnage", AppRoute.Live(channelLogin = "cohhcarnage").channelLogin)
    }
}
