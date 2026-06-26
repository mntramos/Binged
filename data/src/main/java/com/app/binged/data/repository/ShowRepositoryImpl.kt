package com.app.binged.data.repository

import com.app.binged.core.utils.Result
import com.app.binged.data.api.TmdbService
import com.app.binged.data.database.dao.EpisodeDao
import com.app.binged.data.database.dao.ShowDao
import com.app.binged.data.mapper.toDomain
import com.app.binged.data.mapper.toEntity
import com.app.binged.data.sync.SyncManager
import com.app.binged.domain.contract.ShowRepository
import com.app.binged.domain.model.PaginatedResult
import com.app.binged.domain.model.Show
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
class ShowRepositoryImpl @Inject constructor(
    private val showDao: ShowDao,
    private val episodeDao: EpisodeDao,
    private val tmdbService: TmdbService,
    private val syncManager: SyncManager
) : ShowRepository {

    override fun getTrackedShows(): Flow<List<Show>> {
        return showDao.getAllShows().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun searchShows(query: String, page: Int): Result<PaginatedResult<Show>> {
        return try {
            val response = tmdbService.searchShows(query, page)
            Result.Success(
                PaginatedResult(
                    items = response.results.map { it.toDomain() },
                    currentPage = response.page,
                    totalPages = response.totalPages
                )
            )
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getShowDetails(id: Int): Result<Show> {
        return try {
            val response = tmdbService.getShowDetails(id)
            Result.Success(response.toDomain())
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getPopularShows(): Result<List<Show>> {
        return try {
            val response = tmdbService.getPopularShows()
            Result.Success(response.results.map { it.toDomain() })
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun saveShow(show: Show) {
        val entity = show.toEntity()
        showDao.insertShow(entity)
        syncManager.pushShow(entity)
    }

    override suspend fun deleteShow(show: Show): Int {
        episodeDao.deleteEpisodesByShow(show.id)
        val result = showDao.deleteShow(show.toEntity())
        syncManager.deleteShow(show.id)
        return result
    }

    override suspend fun updateFavoriteStatus(show: Show, isFavorite: Boolean): Int {
        val result = showDao.updateFavoriteStatus(show.id, isFavorite)
        if (result > 0) {
            showDao.getShowById(show.id).first()?.let(syncManager::pushShow)
        }
        return result
    }

    override suspend fun updateWatchingStatus(show: Show, isWatching: Boolean): Int {
        val result = showDao.updateWatchingStatus(show.id, isWatching)
        if (result > 0) {
            showDao.getShowById(show.id).first()?.let(syncManager::pushShow)
        }
        return result
    }

    override suspend fun deleteAll() {
        showDao.deleteAll()
    }
}
