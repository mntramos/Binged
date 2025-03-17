package com.app.binged.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.app.binged.data.database.dao.EpisodeDao
import com.app.binged.data.database.dao.ShowDao
import com.app.binged.data.database.entity.EpisodeEntity
import com.app.binged.data.database.entity.ShowEntity

@Database(entities = [
    ShowEntity::class,
    EpisodeEntity::class
 ], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun showDao(): ShowDao
    abstract fun episodeDao(): EpisodeDao
}
