package dev.mitchell.mtwitch.core.local

import dev.mitchell.mtwitch.core.model.VideoId
import dev.mitchell.mtwitch.core.model.WatchProgress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class InMemoryWatchHistoryRepository : WatchHistoryRepository {
    private val progressByVideoId = linkedMapOf<VideoId, WatchProgress>()
    private val _recent = MutableStateFlow<List<WatchProgress>>(emptyList())

    override val recent: StateFlow<List<WatchProgress>> = _recent

    override suspend fun saveProgress(progress: WatchProgress) {
        progressByVideoId[progress.videoId] = progress
        _recent.update {
            progressByVideoId.values
                .sortedByDescending { item -> item.updatedAtEpochMs }
        }
    }

    override suspend fun progressFor(videoId: VideoId): WatchProgress? {
        return progressByVideoId[videoId]
    }

    override suspend fun clear() {
        progressByVideoId.clear()
        _recent.value = emptyList()
    }
}
