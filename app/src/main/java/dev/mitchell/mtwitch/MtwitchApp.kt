package dev.mitchell.mtwitch

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import dev.mitchell.mtwitch.core.local.InMemoryWatchHistoryRepository
import dev.mitchell.mtwitch.core.model.VideoId
import dev.mitchell.mtwitch.data.discovery.FakeVodDiscoveryRepository
import dev.mitchell.mtwitch.data.discovery.TwitchGraphQlVodDiscoveryRepository
import dev.mitchell.mtwitch.data.discovery.VodDiscoveryRepository
import dev.mitchell.mtwitch.data.discovery.VodDiscoveryResult
import dev.mitchell.mtwitch.data.playback.JavaNetHttpTextClient
import dev.mitchell.mtwitch.data.playback.TwitchGraphQlPlaybackAccessTokenProvider
import dev.mitchell.mtwitch.data.playback.TwitchVodPlaybackSourceResolver
import dev.mitchell.mtwitch.feature.channel.ChannelVodListScreen
import dev.mitchell.mtwitch.feature.channel.ChannelVodListUiState
import dev.mitchell.mtwitch.feature.home.HomeScreen
import dev.mitchell.mtwitch.feature.live.LivePlayerScreen
import dev.mitchell.mtwitch.feature.player.PlayerScreen
import dev.mitchell.mtwitch.feature.plugins.ChatPluginSettingsScreen
import dev.mitchell.mtwitch.feature.settings.SettingsScreen
import dev.mitchell.mtwitch.navigation.AppRoute

@Composable
fun MtwitchApp() {
    val navController = rememberNavController()
    val userAgent = "M_twitch Android playback/0.1.0"
    val vodDiscoveryRepository = remember {
        if (BuildConfig.TWITCH_GQL_CLIENT_ID.isBlank()) {
            FakeVodDiscoveryRepository()
        } else {
            TwitchGraphQlVodDiscoveryRepository(
                clientId = BuildConfig.TWITCH_GQL_CLIENT_ID,
                userAgent = userAgent,
            )
        }
    }
    val watchHistoryRepository = remember { InMemoryWatchHistoryRepository() }
    val playbackResolver = remember {
        TwitchVodPlaybackSourceResolver(
            tokenProvider = TwitchGraphQlPlaybackAccessTokenProvider(
                clientId = BuildConfig.TWITCH_GQL_CLIENT_ID,
                userAgent = userAgent,
            ),
            httpClient = JavaNetHttpTextClient(userAgent = userAgent),
        )
    }

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
            ChannelRoute(
                channelLogin = route.channelLogin,
                repository = vodDiscoveryRepository,
                onOpenVod = { videoId ->
                    navController.navigate(
                        AppRoute.Player(
                            videoId = videoId.value,
                            channelLogin = route.channelLogin,
                        ),
                    )
                },
                onBack = { navController.popBackStack() },
            )
        }
        composable<AppRoute.Player> { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoute.Player>()
            PlayerScreen(
                videoId = route.videoId,
                channelLogin = route.channelLogin,
                playbackResolver = playbackResolver,
                watchHistoryRepository = watchHistoryRepository,
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

@Composable
private fun ChannelRoute(
    channelLogin: String,
    repository: VodDiscoveryRepository,
    onOpenVod: (VideoId) -> Unit,
    onBack: () -> Unit,
) {
    var reloadKey by remember(channelLogin) { mutableIntStateOf(0) }
    var uiState by remember(channelLogin) {
        mutableStateOf<ChannelVodListUiState>(ChannelVodListUiState.Loading)
    }

    LaunchedEffect(channelLogin, reloadKey) {
        uiState = ChannelVodListUiState.Loading
        uiState = when (val result = repository.openChannel(channelLogin)) {
            is VodDiscoveryResult.Content -> ChannelVodListUiState.Content(
                channel = result.channel,
                vods = result.vods,
            )
            is VodDiscoveryResult.Empty -> ChannelVodListUiState.Empty(result.channel)
            is VodDiscoveryResult.Error -> ChannelVodListUiState.Error(result.reason)
        }
    }

    ChannelVodListScreen(
        channelLogin = channelLogin,
        uiState = uiState,
        onOpenVod = onOpenVod,
        onRetry = { reloadKey += 1 },
        onBack = onBack,
    )
}
