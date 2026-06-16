package com.app.binged.data.repository

import com.app.binged.core.utils.Result
import com.app.binged.data.api.TmdbService
import com.app.binged.data.database.dao.EpisodeDao
import com.app.binged.data.mapper.toDomain
import com.app.binged.data.mapper.toEntity
import com.app.binged.data.sync.SyncManager
import com.app.binged.domain.contract.EpisodeRepository
import com.app.binged.domain.model.Episode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EpisodeRepositoryImpl @Inject constructor(
    private val episodeDao: EpisodeDao,
    private val tmdbService: TmdbService,
    private val syncManager: SyncManager
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
            Result.Success(response.toDomain(showId = id))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getLocalEpisode(showId: Int, seasonNumber: Int, episodeNumber: Int): Episode? {
        return episodeDao.getLocalEpisode(showId, seasonNumber, episodeNumber)?.toDomain()
    }

    override suspend fun saveEpisode(episode: Episode): Long {
        val entity = episode.toEntity()
        val id = episodeDao.insertEpisode(entity)
        val saved = entity.copy(id = id)
        syncManager.pushEpisode(saved)
        return id
    }

    override suspend fun deleteEpisode(episode: Episode) {
        episodeDao.deleteEpisodeById(episode.episodeId, episode.showId)
        syncManager.deleteEpisode(episode.episodeId, episode.showId)
    }

    override suspend fun deleteAll() {
        episodeDao.deleteAll()
    }
}
