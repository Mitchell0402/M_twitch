package dev.mitchell.mtwitch.data.playback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HlsManifestParserTest {
    @Test
    fun parseMasterManifestReturnsVariantCount() {
        val manifest = """
            #EXTM3U
            #EXT-X-STREAM-INF:BANDWIDTH=800000,RESOLUTION=640x360
            https://cdn.example.test/vod/360p/index-dvr.m3u8
            #EXT-X-STREAM-INF:BANDWIDTH=3000000,RESOLUTION=1920x1080
            https://cdn.example.test/vod/1080p/index-dvr.m3u8
        """.trimIndent()

        val result = HlsManifestParser.parseMaster(manifest)

        assertEquals(HlsManifestParseResult.Valid(variantCount = 2), result)
    }

    @Test
    fun parseMasterManifestRejectsMissingHeader() {
        val result = HlsManifestParser.parseMaster(
            """
                #EXT-X-STREAM-INF:BANDWIDTH=800000
                https://cdn.example.test/vod/360p/index-dvr.m3u8
            """.trimIndent(),
        )

        assertEquals(HlsManifestParseResult.Malformed, result)
    }

    @Test
    fun parseMasterManifestRejectsMissingVariantUrls() {
        val result = HlsManifestParser.parseMaster(
            """
                #EXTM3U
                #EXT-X-STREAM-INF:BANDWIDTH=800000
            """.trimIndent(),
        )

        assertEquals(HlsManifestParseResult.NoVariants, result)
    }

    @Test
    fun parseVariantManifestKeepsMutedAndDiscontinuityDiagnostics() {
        val manifest = """
            #EXTM3U
            #EXT-X-TARGETDURATION:10
            #EXTINF:10.000,
            1746-muted.ts
            #EXTINF:10.000,
            1747-muted.ts
            #EXT-X-DISCONTINUITY
            #EXTINF:10.000,
            1748.ts
        """.trimIndent()

        val issues = HlsManifestParser.parseTimelineIssues(manifest, segmentDurationMs = 10_000L)

        assertEquals(
            listOf(
                PlaybackTimelineIssue(
                    startMs = 0L,
                    endMs = 20_000L,
                    reason = PlaybackTimelineIssueReason.MutedSegment,
                ),
                PlaybackTimelineIssue(
                    startMs = 20_000L,
                    endMs = 20_000L,
                    reason = PlaybackTimelineIssueReason.Discontinuity,
                ),
            ),
            issues,
        )
    }

    @Test
    fun redactedManifestUrlHidesSensitiveQueryValues() {
        val redacted = PlaybackDiagnostics.redactUrl(
            "https://usher.ttvnw.net/vod/123.m3u8?allow_source=true&nauthsig=sig-value&nauth=token-value",
        )

        assertTrue(redacted.contains("nauthsig=<redacted>"))
        assertTrue(redacted.contains("nauth=<redacted>"))
        assertTrue(!redacted.contains("sig-value"))
        assertTrue(!redacted.contains("token-value"))
    }
}
