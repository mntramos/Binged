package com.app.binged.data.repository

import com.app.binged.data.database.dao.EpisodeDao
import com.app.binged.data.mapper.toDomain
import com.app.binged.data.mapper.toEntity
import com.app.binged.domain.contract.EpisodeRepository
import com.app.binged.domain.model.Episode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class EpisodeRepositoryImpl(
    private val episodeDao: EpisodeDao
) : EpisodeRepository {
    override fun getEpisodesByShow(showId: Int): Flow<List<Episode>> {
        return episodeDao.getEpisodesByShow(showId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveEpisode(episode: Episode): Long {
        return episodeDao.insertEpisode(episode.toEntity())
    }

    override suspend fun deleteEpisode(episode: Episode) {
        episodeDao.deleteEpisode(episode.toEntity())
    }
}
