package com.app.binged.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shows")
data class ShowEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val overview: String,
    val posterPath: String?,
    val firstAirDate: String,
    val rating: Double
)
