package com.app.binged.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "episodes")
data class EpisodeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val episodeId: Int,
    val showId: Int,
    val showName: String,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val title: String,
    val watchedDate: Long,
    val stillPath: String?,
    val notes: String?
)
