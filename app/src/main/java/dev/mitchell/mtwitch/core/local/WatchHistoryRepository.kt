package dev.mitchell.mtwitch.core.local

import dev.mitchell.mtwitch.core.model.VideoId
import dev.mitchell.mtwitch.core.model.WatchProgress
import kotlinx.coroutines.flow.StateFlow

interface WatchHistoryRepository {
    val recent: StateFlow<List<WatchProgress>>

    suspend fun saveProgress(progress: WatchProgress)

    suspend fun progressFor(videoId: VideoId): WatchProgress?

    suspend fun clear()
}
