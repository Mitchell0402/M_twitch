package dev.mitchell.mtwitch.data.playback

class FakePlaybackSourceResolver(
    private val result: PlaybackSourceResult,
) : PlaybackSourceResolver {
    override suspend fun resolve(request: PlaybackRequest): PlaybackSourceResult {
        return when (result) {
            is PlaybackSourceResult.Ready -> result.copy(
                source = result.source.copy(request = request),
            )
            is PlaybackSourceResult.Unavailable -> result
        }
    }
}
