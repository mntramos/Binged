package com.app.binged.domain.contract

import com.app.binged.core.utils.Result
import com.app.binged.domain.model.Show
import kotlinx.coroutines.flow.Flow

interface ShowRepository {
    fun getTrackedShows(): Flow<List<Show>>
    suspend fun searchShows(query: String): Result<List<Show>>
    suspend fun getShowDetails(id: Int): Result<Show>
    suspend fun saveShow(show: Show)
    suspend fun deleteShow(show: Show)
    suspend fun updateFavoriteStatus(show: Show, isFavorite: Boolean)
    suspend fun updateWatchingStatus(show: Show, isWatching: Boolean)
}
