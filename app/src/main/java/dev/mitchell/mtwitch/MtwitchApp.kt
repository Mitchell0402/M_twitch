package dev.mitchell.mtwitch

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import dev.mitchell.mtwitch.feature.channel.ChannelVodListScreen
import dev.mitchell.mtwitch.feature.home.HomeScreen
import dev.mitchell.mtwitch.feature.live.LivePlayerScreen
import dev.mitchell.mtwitch.feature.player.PlayerScreen
import dev.mitchell.mtwitch.feature.plugins.ChatPluginSettingsScreen
import dev.mitchell.mtwitch.feature.settings.SettingsScreen
import dev.mitchell.mtwitch.navigation.AppRoute

@Composable
fun MtwitchApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppRoute.Home,
    ) {
        composable<AppRoute.Home> {
            HomeScreen(
                onOpenChannel = { channelLogin ->
                    navController.navigate(AppRoute.Channel(channelLogin))
                },
                onOpenLive = { channelLogin ->
                    navController.navigate(AppRoute.Live(channelLogin))
                },
                onOpenChatPlugins = {
                    navController.navigate(AppRoute.ChatPlugins)
                },
                onOpenSettings = {
                    navController.navigate(AppRoute.Settings)
                },
            )
        }
        composable<AppRoute.Channel> { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoute.Channel>()
            ChannelVodListScreen(
                channelLogin = route.channelLogin,
                onOpenVod = { videoId ->
                    navController.navigate(AppRoute.Player(videoId.value))
                },
                onBack = { navController.popBackStack() },
            )
        }
        composable<AppRoute.Player> { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoute.Player>()
            PlayerScreen(
                videoId = route.videoId,
                onBack = { navController.popBackStack() },
            )
        }
        composable<AppRoute.Live> { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoute.Live>()
            LivePlayerScreen(
                channelLogin = route.channelLogin,
                onBack = { navController.popBackStack() },
            )
        }
        composable<AppRoute.ChatPlugins> {
            ChatPluginSettingsScreen(onBack = { navController.popBackStack() })
        }
        composable<AppRoute.Settings> {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
