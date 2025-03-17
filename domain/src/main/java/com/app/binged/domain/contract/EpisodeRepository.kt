package com.app.binged.domain.contract

import com.app.binged.domain.model.Episode
import kotlinx.coroutines.flow.Flow

interface EpisodeRepository {
    fun getEpisodesByShow(showId: Int): Flow<List<Episode>>
    suspend fun saveEpisode(episode: Episode): Long
    suspend fun deleteEpisode(episode: Episode)
}
