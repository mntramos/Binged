package com.app.binged.domain.contract

import com.app.binged.core.utils.Result
import com.app.binged.domain.model.PaginatedResult
import com.app.binged.domain.model.Show
import kotlinx.coroutines.flow.Flow

interface ShowRepository {
    fun getTrackedShows(): Flow<List<Show>>
    suspend fun searchShows(query: String, page: Int = 1): Result<PaginatedResult<Show>>
    suspend fun getShowDetails(id: Int): Result<Show>
    suspend fun getPopularShows(): Result<List<Show>>
    suspend fun saveShow(show: Show)
    suspend fun deleteShow(show: Show): Int
    suspend fun updateFavoriteStatus(show: Show, isFavorite: Boolean): Int
    suspend fun updateWatchingStatus(show: Show, isWatching: Boolean): Int
    suspend fun deleteAll()
}
