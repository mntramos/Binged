package com.app.binged.data.repository

import com.app.binged.core.utils.Result
import com.app.binged.data.api.TmdbService
import com.app.binged.data.database.dao.EpisodeDao
import com.app.binged.data.mapper.toDomain
import com.app.binged.data.mapper.toEntity
import com.app.binged.domain.contract.EpisodeRepository
import com.app.binged.domain.model.Episode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class EpisodeRepositoryImpl(
    private val episodeDao: EpisodeDao,
    private val tmdbService: TmdbService
) : EpisodeRepository {

    override fun getAllEpisodes(): Flow<List<Episode>> {
        return episodeDao.getAllEpisodes().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getEpisodesByShow(showId: Int): Flow<List<Episode>> {
        return episodeDao.getEpisodesByShow(showId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getEpisodeDetails(id: Int, season: Int, episode: Int): Result<Episode> {
        return try {
            val response = tmdbService.getEpisodeDetails(id, season, episode)
            Result.Success(response.toDomain())
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun saveEpisode(episode: Episode): Long {
        return episodeDao.insertEpisode(episode.toEntity())
    }

    override suspend fun deleteEpisode(episode: Episode) {
        episodeDao.deleteEpisode(episode.toEntity())
    }
}
