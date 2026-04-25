package dev.mitchell.mtwitch.data.playback

import dev.mitchell.mtwitch.core.model.VideoId
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class PlaybackAccessToken(
    val value: String,
    val signature: String,
)

sealed interface PlaybackAccessTokenResult {
    data class Success(val token: PlaybackAccessToken) : PlaybackAccessTokenResult
    data class Failed(val httpStatus: Int? = null) : PlaybackAccessTokenResult
    data object NetworkTimeout : PlaybackAccessTokenResult
}

interface PlaybackAccessTokenProvider {
    suspend fun vodToken(
        channelLogin: String,
        videoId: VideoId,
    ): PlaybackAccessTokenResult
}

sealed interface HttpTextResult {
    data class Success(
        val statusCode: Int,
        val body: String,
    ) : HttpTextResult

    data object NetworkTimeout : HttpTextResult
    data class Failed(val statusCode: Int? = null) : HttpTextResult
}

interface HttpTextClient {
    suspend fun getText(url: String): HttpTextResult
}

class JavaNetHttpTextClient(
    private val userAgent: String,
    private val timeoutMs: Int = 10_000,
) : HttpTextClient {
    override suspend fun getText(url: String): HttpTextResult = withContext(Dispatchers.IO) {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = timeoutMs
            readTimeout = timeoutMs
            setRequestProperty("User-Agent", userAgent)
        }

        try {
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader()?.use { reader -> reader.readText() }.orEmpty()
            HttpTextResult.Success(statusCode = status, body = body)
        } catch (_: SocketTimeoutException) {
            HttpTextResult.NetworkTimeout
        } finally {
            connection.disconnect()
        }
    }
}

class TwitchVodPlaybackSourceResolver(
    private val tokenProvider: PlaybackAccessTokenProvider,
    private val httpClient: HttpTextClient,
) : PlaybackSourceResolver {
    override suspend fun resolve(request: PlaybackRequest): PlaybackSourceResult {
        if (request !is PlaybackRequest.Vod || request.channelLogin.isBlank()) {
            return PlaybackSourceResult.Unavailable(PlaybackUnavailableReason.SourceMissing)
        }

        val tokenResult = tokenProvider.vodToken(request.channelLogin, request.videoId)
        val token = when (tokenResult) {
            is PlaybackAccessTokenResult.Success -> tokenResult.token
            is PlaybackAccessTokenResult.Failed -> return PlaybackSourceResult.Unavailable(PlaybackUnavailableReason.TokenFailed)
            PlaybackAccessTokenResult.NetworkTimeout -> return PlaybackSourceResult.Unavailable(PlaybackUnavailableReason.NetworkTimeout)
        }

        val manifestUrl = usherVodUrl(request.videoId, token)
        val manifestResult = httpClient.getText(manifestUrl)
        val manifest = when (manifestResult) {
            is HttpTextResult.Success -> {
                if (manifestResult.statusCode == 403) {
                    return PlaybackSourceResult.Unavailable(PlaybackUnavailableReason.ManifestForbidden)
                }
                if (manifestResult.statusCode !in 200..299) {
                    return PlaybackSourceResult.Unavailable(PlaybackUnavailableReason.ManifestMalformed)
                }
                manifestResult.body
            }
            is HttpTextResult.Failed -> return PlaybackSourceResult.Unavailable(PlaybackUnavailableReason.ManifestMalformed)
            HttpTextResult.NetworkTimeout -> return PlaybackSourceResult.Unavailable(PlaybackUnavailableReason.NetworkTimeout)
        }

        return when (val parsedManifest = HlsManifestParser.parseMaster(manifest)) {
            is HlsManifestParseResult.Valid -> PlaybackSourceResult.Ready(
                PlayableSource(
                    request = request,
                    hlsUrl = manifestUrl,
                    isLive = false,
                    timelineIssues = emptyList(),
                    diagnostics = PlaybackDiagnostics(
                        operation = "PlaybackAccessToken",
                        manifestStatus = (manifestResult as HttpTextResult.Success).statusCode,
                        variantCount = parsedManifest.variantCount,
                        sanitizedUrl = PlaybackDiagnostics.redactUrl(manifestUrl),
                    ),
                ),
            )
            HlsManifestParseResult.Malformed -> PlaybackSourceResult.Unavailable(PlaybackUnavailableReason.ManifestMalformed)
            HlsManifestParseResult.NoVariants -> PlaybackSourceResult.Unavailable(PlaybackUnavailableReason.NoVariants)
        }
    }

    private fun usherVodUrl(videoId: VideoId, token: PlaybackAccessToken): String {
        val signature = token.signature.urlEncode()
        val value = token.value.urlEncode()
        return "https://usher.ttvnw.net/vod/${videoId.value}.m3u8" +
            "?allow_source=true" +
            "&allow_audio_only=true" +
            "&allow_spectre=true" +
            "&player=twitchweb" +
            "&nauthsig=$signature" +
            "&nauth=$value"
    }

    private fun String.urlEncode(): String {
        return URLEncoder.encode(this, Charsets.UTF_8.name())
    }
}
