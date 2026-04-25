package dev.mitchell.mtwitch.data.playback

import dev.mitchell.mtwitch.core.model.VideoId
import org.junit.Assert.assertEquals
import org.junit.Test

class PlaybackSourceModelsTest {
    @Test
    fun playbackErrorsSeparateTokenAndManifestFailures() {
        val expected = listOf(
            PlaybackUnavailableReason.TokenFailed,
            PlaybackUnavailableReason.ManifestForbidden,
            PlaybackUnavailableReason.ManifestMalformed,
            PlaybackUnavailableReason.NoVariants,
            PlaybackUnavailableReason.NetworkTimeout,
        )

        assertEquals(expected, expected.map { reason -> reason })
    }

    @Test
    fun playableSourceKeepsSignedUrlOutOfDiagnostics() {
        val source = PlayableSource(
            request = PlaybackRequest.Vod(VideoId("2755960234")),
            hlsUrl = "https://usher.ttvnw.net/vod/2755960234.m3u8",
            isLive = false,
            timelineIssues = emptyList(),
            diagnostics = PlaybackDiagnostics(
                operation = "PlaybackAccessToken",
                httpStatus = 200,
                manifestStatus = 200,
                variantCount = 6,
                sanitizedUrl = "https://usher.ttvnw.net/vod/2755960234.m3u8?<redacted>",
            ),
        )

        assertEquals(6, source.diagnostics.variantCount)
        assertEquals(
            "https://usher.ttvnw.net/vod/2755960234.m3u8?<redacted>",
            source.diagnostics.sanitizedUrl,
        )
    }
}
