package com.app.binged.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.app.binged.data.database.entity.EpisodeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EpisodeDao {
    @Query("SELECT * FROM episodes ORDER BY watchedDate DESC, id DESC")
    fun getAllEpisodes(): Flow<List<EpisodeEntity>>

    @Query("SELECT * FROM episodes WHERE showId = :showId ORDER BY seasonNumber, episodeNumber")
    fun getEpisodesByShow(showId: Int): Flow<List<EpisodeEntity>>

    @Query("SELECT * FROM episodes WHERE showId = :showId AND seasonNumber = :seasonNumber AND episodeNumber = :episodeNumber LIMIT 1")
    suspend fun getLocalEpisode(showId: Int, seasonNumber: Int, episodeNumber: Int): EpisodeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisode(episode: EpisodeEntity): Long

    @Delete
    suspend fun deleteEpisode(episode: EpisodeEntity)

    @Query("DELETE FROM episodes WHERE episodeId = :episodeId AND showId = :showId")
    suspend fun deleteEpisodeById(episodeId: Int, showId: Int)

    @Query("DELETE FROM episodes WHERE showId = :showId")
    suspend fun deleteEpisodesByShow(showId: Int)

    @Query("DELETE FROM episodes")
    suspend fun deleteAll()
}
