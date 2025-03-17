package com.app.binged.data.repository

import com.app.binged.core.utils.Result
import com.app.binged.data.api.TmdbService
import com.app.binged.data.database.dao.ShowDao
import com.app.binged.data.mapper.toDomain
import com.app.binged.data.mapper.toEntity
import com.app.binged.domain.contract.ShowRepository
import com.app.binged.domain.model.Show
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ShowRepositoryImpl(
    private val showDao: ShowDao,
    private val tmdbService: TmdbService
) : ShowRepository {
    override fun getTrackedShows(): Flow<List<Show>> {
        return showDao.getAllShows().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun searchShows(query: String): Result<List<Show>> {
        return try {
            val response = tmdbService.searchShows(query)
            Result.Success(response.results.map { it.toDomain() })
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

    override suspend fun saveShow(show: Show) {
        showDao.insertShow(show.toEntity())
    }

    override suspend fun deleteShow(show: Show) {
        showDao.deleteShow(show.toEntity())
    }
}
