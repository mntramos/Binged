package com.app.binged.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.app.binged.data.database.dao.EpisodeDao
import com.app.binged.data.database.dao.ShowDao
import com.app.binged.data.database.entity.EpisodeEntity
import com.app.binged.data.database.entity.ShowEntity

@Database(
    entities = [ShowEntity::class, EpisodeEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun showDao(): ShowDao
    abstract fun episodeDao(): EpisodeDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE episodes ADD COLUMN runtime INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE episodes ADD COLUMN isRewatch INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}
