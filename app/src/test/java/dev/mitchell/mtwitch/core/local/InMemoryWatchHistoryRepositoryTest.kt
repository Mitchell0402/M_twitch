package dev.mitchell.mtwitch.core.local

import dev.mitchell.mtwitch.core.model.VideoId
import dev.mitchell.mtwitch.core.model.WatchProgress
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class InMemoryWatchHistoryRepositoryTest {
    @Test
    fun saveProgressStoresLatestProgressForVideo() = runTest {
        val repository = InMemoryWatchHistoryRepository()
        val first = WatchProgress(VideoId("1"), positionMs = 5_000L, durationMs = 60_000L, updatedAtEpochMs = 10L)
        val second = WatchProgress(VideoId("1"), positionMs = 8_000L, durationMs = 60_000L, updatedAtEpochMs = 20L)

        repository.saveProgress(first)
        repository.saveProgress(second)

        assertEquals(second, repository.progressFor(VideoId("1")))
        assertEquals(listOf(second), repository.recent.value)
    }

    @Test
    fun clearRemovesAllProgress() = runTest {
        val repository = InMemoryWatchHistoryRepository()
        repository.saveProgress(WatchProgress(VideoId("1"), positionMs = 5_000L, durationMs = 60_000L, updatedAtEpochMs = 10L))

        repository.clear()

        assertNull(repository.progressFor(VideoId("1")))
        assertEquals(emptyList<WatchProgress>(), repository.recent.value)
    }
}
