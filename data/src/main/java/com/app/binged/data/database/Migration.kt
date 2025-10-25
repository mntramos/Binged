package com.app.binged.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE shows ADD COLUMN tagline TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE shows ADD COLUMN seasonCount INTEGER NOT NULL DEFAULT 1")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE shows ADD COLUMN isWatching INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE shows ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
    }
}
