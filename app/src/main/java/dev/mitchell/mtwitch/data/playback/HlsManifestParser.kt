package dev.mitchell.mtwitch.data.playback

sealed interface HlsManifestParseResult {
    data class Valid(val variantCount: Int) : HlsManifestParseResult
    data object Malformed : HlsManifestParseResult
    data object NoVariants : HlsManifestParseResult
}

object HlsManifestParser {
    fun parseMaster(manifest: String): HlsManifestParseResult {
        val lines = manifest.lines().map { line -> line.trim() }.filter { line -> line.isNotEmpty() }
        if (lines.firstOrNull() != "#EXTM3U") {
            return HlsManifestParseResult.Malformed
        }

        var streamInfCount = 0
        var variantCount = 0
        var expectVariantUrl = false
        for (line in lines.drop(1)) {
            when {
                line.startsWith("#EXT-X-STREAM-INF") -> {
                    streamInfCount += 1
                    expectVariantUrl = true
                }
                expectVariantUrl && line.startsWith("#") -> return HlsManifestParseResult.Malformed
                expectVariantUrl -> {
                    if (!line.contains(".m3u8")) {
                        return HlsManifestParseResult.Malformed
                    }
                    variantCount += 1
                    expectVariantUrl = false
                }
            }
        }

        if (expectVariantUrl && streamInfCount > 0) {
            return HlsManifestParseResult.NoVariants
        }
        return if (variantCount == 0) HlsManifestParseResult.NoVariants else HlsManifestParseResult.Valid(variantCount)
    }

    fun parseTimelineIssues(
        variantManifest: String,
        segmentDurationMs: Long,
    ): List<PlaybackTimelineIssue> {
        val issues = mutableListOf<PlaybackTimelineIssue>()
        var segmentIndex = 0
        var mutedStartIndex: Int? = null
        val lines = variantManifest.lines().map { line -> line.trim() }.filter { line -> line.isNotEmpty() }

        fun closeMutedRange(exclusiveEndIndex: Int) {
            val startIndex = mutedStartIndex ?: return
            issues += PlaybackTimelineIssue(
                startMs = startIndex * segmentDurationMs,
                endMs = exclusiveEndIndex * segmentDurationMs,
                reason = PlaybackTimelineIssueReason.MutedSegment,
            )
            mutedStartIndex = null
        }

        for (line in lines) {
            if (line == "#EXT-X-DISCONTINUITY") {
                closeMutedRange(segmentIndex)
                issues += PlaybackTimelineIssue(
                    startMs = segmentIndex * segmentDurationMs,
                    endMs = segmentIndex * segmentDurationMs,
                    reason = PlaybackTimelineIssueReason.Discontinuity,
                )
                continue
            }

            if (line.startsWith("#") || !line.endsWith(".ts")) {
                continue
            }

            val isMutedSegment = line.substringAfterLast('/').contains("-muted")
            if (isMutedSegment && mutedStartIndex == null) {
                mutedStartIndex = segmentIndex
            }
            if (!isMutedSegment) {
                closeMutedRange(segmentIndex)
            }
            segmentIndex += 1
        }
        closeMutedRange(segmentIndex)
        return issues
    }
}
