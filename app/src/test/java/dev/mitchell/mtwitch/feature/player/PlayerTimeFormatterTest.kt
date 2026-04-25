package dev.mitchell.mtwitch.feature.player

import org.junit.Assert.assertEquals
import org.junit.Test

class PlayerTimeFormatterTest {
    @Test
    fun formatPlaybackTimeUsesHoursWhenNeeded() {
        assertEquals("00:00", formatPlaybackTime(0L))
        assertEquals("01:05", formatPlaybackTime(65_000L))
        assertEquals("1:01:05", formatPlaybackTime(3_665_000L))
    }

    @Test
    fun formatPlaybackTimeHandlesUnknownDuration() {
        assertEquals("--:--", formatPlaybackTime(-1L))
    }
}
