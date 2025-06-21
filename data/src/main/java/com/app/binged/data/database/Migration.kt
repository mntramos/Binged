package com.app.binged.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE shows ADD COLUMN tagline TEXT NOT NULL DEFAULT ''")
        database.execSQL("ALTER TABLE shows ADD COLUMN seasonCount INTEGER NOT NULL DEFAULT 1")
    }
}
