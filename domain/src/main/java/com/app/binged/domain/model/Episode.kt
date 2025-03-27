package com.app.binged.domain.model

import java.util.Date

data class Episode(
    val id: Long = 0,
    val episodeId: Int,
    val showId: Int,
    val showName: String,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val title: String,
    val watchedDate: Date,
    val stillPath: String?,
    val notes: String? = null
)
