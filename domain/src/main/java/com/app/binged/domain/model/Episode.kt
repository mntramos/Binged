package com.app.binged.domain.model

import java.util.Date

data class Episode(
    val id: Long = 0,
    val showId: Int,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val title: String,
    val watchedDate: Date,
    val notes: String? = null
)
