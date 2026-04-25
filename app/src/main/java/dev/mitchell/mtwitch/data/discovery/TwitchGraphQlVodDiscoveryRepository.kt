package dev.mitchell.mtwitch.data.discovery

import dev.mitchell.mtwitch.core.model.ChannelId
import dev.mitchell.mtwitch.core.model.VideoId
import dev.mitchell.mtwitch.core.model.Vod
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlin.time.Duration.Companion.seconds

class TwitchGraphQlVodDiscoveryRepository(
    private val clientId: String,
    private val userAgent: String,
    private val timeoutMs: Int = 10_000,
) : VodDiscoveryRepository {
    override suspend fun openChannel(channelLogin: String): VodDiscoveryResult {
        if (clientId.isBlank()) {
            return VodDiscoveryResult.Error(VodDiscoveryError.Unauthorized)
        }

        return when (val response = postGraphQl(channelVideosBody(channelLogin.trim().lowercase()))) {
            GraphQlTextResponse.NetworkTimeout -> VodDiscoveryResult.Error(VodDiscoveryError.NetworkTimeout)
            is GraphQlTextResponse.Failed -> when (response.statusCode) {
                401, 403 -> VodDiscoveryResult.Error(VodDiscoveryError.Unauthorized)
                429 -> VodDiscoveryResult.Error(VodDiscoveryError.RateLimited)
                else -> VodDiscoveryResult.Error(VodDiscoveryError.Unknown)
            }
            is GraphQlTextResponse.Success -> parseChannelVideos(response.body)
        }
    }

    private suspend fun postGraphQl(body: String): GraphQlTextResponse = withContext(Dispatchers.IO) {
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
            val bodyText = stream?.bufferedReader()?.use { reader -> reader.readText() }.orEmpty()
            if (status in 200..299) {
                GraphQlTextResponse.Success(bodyText)
            } else {
                GraphQlTextResponse.Failed(status)
            }
        } catch (_: SocketTimeoutException) {
            GraphQlTextResponse.NetworkTimeout
        } finally {
            connection.disconnect()
        }
    }

    private fun parseChannelVideos(body: String): VodDiscoveryResult {
        return runCatching {
            val user = JSONObject(body)
                .getJSONObject("data")
                .optJSONObject("user")
                ?: return VodDiscoveryResult.Error(VodDiscoveryError.ChannelNotFound)

            val channel = Channel(
                id = ChannelId(user.getString("id")),
                login = user.getString("login"),
                displayName = user.getString("displayName"),
                vods = emptyList(),
            )
            val edges = user
                .getJSONObject("videos")
                .getJSONArray("edges")
            val vods = buildList {
                for (index in 0 until edges.length()) {
                    val node = edges.getJSONObject(index).getJSONObject("node")
                    add(
                        Vod(
                            id = VideoId(node.getString("id")),
                            channelId = channel.id,
                            channelLogin = channel.login,
                            title = node.getString("title"),
                            thumbnailUrl = node.optString("previewThumbnailURL", ""),
                            duration = node.getLong("lengthSeconds").seconds,
                            publishedAtEpochMs = Instant.parse(node.getString("createdAt")).toEpochMilli(),
                            progress = null,
                        ),
                    )
                }
            }
            val channelWithVods = channel.copy(vods = vods)
            if (vods.isEmpty()) {
                VodDiscoveryResult.Empty(channelWithVods)
            } else {
                VodDiscoveryResult.Content(channel = channelWithVods, vods = vods)
            }
        }.getOrElse {
            VodDiscoveryResult.Error(VodDiscoveryError.Unknown)
        }
    }

    private fun channelVideosBody(channelLogin: String): String {
        return JSONObject()
            .put("operationName", "ChannelVideos")
            .put(
                "variables",
                JSONObject()
                    .put("login", channelLogin)
                    .put("limit", 25),
            )
            .put("query", CHANNEL_VIDEOS_QUERY)
            .toString()
    }

    private sealed interface GraphQlTextResponse {
        data class Success(val body: String) : GraphQlTextResponse
        data class Failed(val statusCode: Int) : GraphQlTextResponse
        data object NetworkTimeout : GraphQlTextResponse
    }

    private companion object {
        const val GRAPHQL_URL = "https://gql.twitch.tv/gql"
        const val CHANNEL_VIDEOS_QUERY = """
            query ChannelVideos(${'$'}login: String!, ${'$'}limit: Int!) {
              user(login: ${'$'}login) {
                id
                login
                displayName
                videos(first: ${'$'}limit, sort: TIME, type: ARCHIVE) {
                  edges {
                    node {
                      id
                      title
                      lengthSeconds
                      createdAt
                      previewThumbnailURL(width: 640, height: 360)
                      broadcastType
                    }
                  }
                }
              }
            }
        """
    }
}
