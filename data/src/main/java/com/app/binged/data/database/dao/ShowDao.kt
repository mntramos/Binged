package com.app.binged.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.app.binged.data.database.entity.ShowEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShowDao {
    @Query("SELECT * FROM shows")
    fun getAllShows(): Flow<List<ShowEntity>>

    @Query("SELECT * FROM shows WHERE id = :id")
    fun getShowById(id: Int): Flow<ShowEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShow(show: ShowEntity)

    @Query("DELETE FROM shows WHERE id = :id")
    suspend fun deleteShowById(id: Int)

    @Delete
    suspend fun deleteShow(show: ShowEntity): Int

    @Query("UPDATE shows SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Int, isFavorite: Boolean): Int

    @Query("UPDATE shows SET isWatching = :isWatching WHERE id = :id")
    suspend fun updateWatchingStatus(id: Int, isWatching: Boolean): Int

    @Query("DELETE FROM shows")
    suspend fun deleteAll()
}
