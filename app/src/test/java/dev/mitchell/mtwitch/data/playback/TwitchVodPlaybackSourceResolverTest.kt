package dev.mitchell.mtwitch.data.playback

import dev.mitchell.mtwitch.core.model.VideoId
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TwitchVodPlaybackSourceResolverTest {
    @Test
    fun resolveReturnsPlayableSourceWhenTokenAndManifestSucceed() = runTest {
        val resolver = resolver(
            tokenResult = PlaybackAccessTokenResult.Success(
                PlaybackAccessToken(value = "value-to-redact", signature = "signature-to-redact"),
            ),
            manifestResult = HttpTextResult.Success(
                statusCode = 200,
                body = """
                    #EXTM3U
                    #EXT-X-STREAM-INF:BANDWIDTH=800000
                    https://cdn.example.test/vod/360p/index-dvr.m3u8
                """.trimIndent(),
            ),
        )

        val result = resolver.resolve(PlaybackRequest.Vod(VideoId("2755960234"), channelLogin = "lirik"))

        val source = (result as PlaybackSourceResult.Ready).source
        assertEquals(false, source.isLive)
        assertEquals(1, source.diagnostics.variantCount)
        assertTrue(source.hlsUrl.startsWith("https://usher.ttvnw.net/vod/2755960234.m3u8?"))
        assertTrue(source.diagnostics.sanitizedUrl?.contains("nauthsig=<redacted>") == true)
        assertTrue(source.diagnostics.sanitizedUrl?.contains("nauth=<redacted>") == true)
        assertTrue(source.diagnostics.sanitizedUrl?.contains("signature-to-redact") == false)
        assertTrue(source.diagnostics.sanitizedUrl?.contains("value-to-redact") == false)
    }

    @Test
    fun resolveSeparatesTokenFailureFromManifestFailure() = runTest {
        val resolver = resolver(
            tokenResult = PlaybackAccessTokenResult.Failed(httpStatus = 500),
            manifestResult = HttpTextResult.Success(statusCode = 200, body = "#EXTM3U"),
        )

        val result = resolver.resolve(PlaybackRequest.Vod(VideoId("2755960234"), channelLogin = "lirik"))

        assertEquals(
            PlaybackSourceResult.Unavailable(PlaybackUnavailableReason.TokenFailed),
            result,
        )
    }

    @Test
    fun resolveMapsManifestForbiddenAfterTokenSuccess() = runTest {
        val resolver = resolver(
            tokenResult = PlaybackAccessTokenResult.Success(
                PlaybackAccessToken(value = "value-to-redact", signature = "signature-to-redact"),
            ),
            manifestResult = HttpTextResult.Success(statusCode = 403, body = "forbidden"),
        )

        val result = resolver.resolve(PlaybackRequest.Vod(VideoId("2755588056"), channelLogin = "criticalrole"))

        assertEquals(
            PlaybackSourceResult.Unavailable(PlaybackUnavailableReason.ManifestForbidden),
            result,
        )
    }

    @Test
    fun resolveMapsMalformedAndNoVariantManifest() = runTest {
        val malformed = resolver(
            tokenResult = PlaybackAccessTokenResult.Success(
                PlaybackAccessToken(value = "value-to-redact", signature = "signature-to-redact"),
            ),
            manifestResult = HttpTextResult.Success(statusCode = 200, body = "not hls"),
        ).resolve(PlaybackRequest.Vod(VideoId("1"), channelLogin = "lirik"))

        val noVariants = resolver(
            tokenResult = PlaybackAccessTokenResult.Success(
                PlaybackAccessToken(value = "value-to-redact", signature = "signature-to-redact"),
            ),
            manifestResult = HttpTextResult.Success(statusCode = 200, body = "#EXTM3U"),
        ).resolve(PlaybackRequest.Vod(VideoId("2"), channelLogin = "lirik"))

        assertEquals(
            PlaybackSourceResult.Unavailable(PlaybackUnavailableReason.ManifestMalformed),
            malformed,
        )
        assertEquals(
            PlaybackSourceResult.Unavailable(PlaybackUnavailableReason.NoVariants),
            noVariants,
        )
    }

    @Test
    fun resolveMapsNetworkTimeout() = runTest {
        val resolver = resolver(
            tokenResult = PlaybackAccessTokenResult.Success(
                PlaybackAccessToken(value = "value-to-redact", signature = "signature-to-redact"),
            ),
            manifestResult = HttpTextResult.NetworkTimeout,
        )

        val result = resolver.resolve(PlaybackRequest.Vod(VideoId("2755960234"), channelLogin = "lirik"))

        assertEquals(
            PlaybackSourceResult.Unavailable(PlaybackUnavailableReason.NetworkTimeout),
            result,
        )
    }

    private fun resolver(
        tokenResult: PlaybackAccessTokenResult,
        manifestResult: HttpTextResult,
    ): TwitchVodPlaybackSourceResolver {
        return TwitchVodPlaybackSourceResolver(
            tokenProvider = object : PlaybackAccessTokenProvider {
                override suspend fun vodToken(
                    channelLogin: String,
                    videoId: VideoId,
                ): PlaybackAccessTokenResult = tokenResult
            },
            httpClient = object : HttpTextClient {
                override suspend fun getText(url: String): HttpTextResult = manifestResult
            },
        )
    }
}
