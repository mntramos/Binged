package com.app.binged.domain.contract

import com.app.binged.core.utils.Result
import com.app.binged.domain.model.Episode
import kotlinx.coroutines.flow.Flow

interface EpisodeRepository {
    fun getAllEpisodes(): Flow<List<Episode>>
    fun getEpisodesByShow(showId: Int): Flow<List<Episode>>
    suspend fun getEpisodeDetails(id: Int, season: Int, episode: Int): Result<Episode>
    suspend fun saveEpisode(episode: Episode): Long
    suspend fun deleteEpisode(episode: Episode)
}
