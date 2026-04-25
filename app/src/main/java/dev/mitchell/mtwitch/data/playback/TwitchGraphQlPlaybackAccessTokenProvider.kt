package dev.mitchell.mtwitch.data.playback

import dev.mitchell.mtwitch.core.model.VideoId
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class TwitchGraphQlPlaybackAccessTokenProvider(
    private val clientId: String,
    private val userAgent: String,
    private val timeoutMs: Int = 10_000,
) : PlaybackAccessTokenProvider {
    override suspend fun vodToken(
        channelLogin: String,
        videoId: VideoId,
    ): PlaybackAccessTokenResult {
        if (clientId.isBlank()) {
            return PlaybackAccessTokenResult.Failed()
        }

        val response = postGraphQl(playbackAccessTokenBody(channelLogin, videoId))
        if (response is HttpTextResult.NetworkTimeout) {
            return PlaybackAccessTokenResult.NetworkTimeout
        }
        if (response !is HttpTextResult.Success || response.statusCode !in 200..299) {
            return PlaybackAccessTokenResult.Failed((response as? HttpTextResult.Success)?.statusCode)
        }

        return runCatching {
            val tokenObject = JSONObject(response.body)
                .getJSONObject("data")
                .getJSONObject("videoPlaybackAccessToken")
            PlaybackAccessTokenResult.Success(
                PlaybackAccessToken(
                    value = tokenObject.getString("value"),
                    signature = tokenObject.getString("signature"),
                ),
            )
        }.getOrElse {
            PlaybackAccessTokenResult.Failed(response.statusCode)
        }
    }

    private suspend fun postGraphQl(body: String): HttpTextResult = withContext(Dispatchers.IO) {
        val connection = (URL(GRAPHQL_URL).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = timeoutMs
            readTimeout = timeoutMs
            doOutput = true
            setRequestProperty("Client-ID", clientId)
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("User-Agent", userAgent)
        }

        try {
            connection.outputStream.use { stream -> stream.write(body.toByteArray(Charsets.UTF_8)) }
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            HttpTextResult.Success(
                statusCode = status,
                body = stream?.bufferedReader()?.use { reader -> reader.readText() }.orEmpty(),
            )
        } catch (_: SocketTimeoutException) {
            HttpTextResult.NetworkTimeout
        } finally {
            connection.disconnect()
        }
    }

    private fun playbackAccessTokenBody(
        channelLogin: String,
        videoId: VideoId,
    ): String {
        return JSONObject()
            .put("operationName", "PlaybackAccessToken")
            .put(
                "variables",
                JSONObject()
                    .put("login", channelLogin)
                    .put("isLive", false)
                    .put("vodID", videoId.value)
                    .put("isVod", true)
                    .put("playerType", "site"),
            )
            .put("query", PLAYBACK_ACCESS_TOKEN_QUERY)
            .toString()
    }

    private companion object {
        const val GRAPHQL_URL = "https://gql.twitch.tv/gql"
        const val PLAYBACK_ACCESS_TOKEN_QUERY = """
            query PlaybackAccessToken(
              ${'$'}login: String!,
              ${'$'}isLive: Boolean!,
              ${'$'}vodID: ID!,
              ${'$'}isVod: Boolean!,
              ${'$'}playerType: String!
            ) {
              streamPlaybackAccessToken(
                channelName: ${'$'}login,
                params: { platform: "web", playerBackend: "mediaplayer", playerType: ${'$'}playerType }
              ) @include(if: ${'$'}isLive) {
                value
                signature
              }
              videoPlaybackAccessToken(
                id: ${'$'}vodID,
                params: { platform: "web", playerBackend: "mediaplayer", playerType: ${'$'}playerType }
              ) @include(if: ${'$'}isVod) {
                value
                signature
              }
            }
        """
    }
}
