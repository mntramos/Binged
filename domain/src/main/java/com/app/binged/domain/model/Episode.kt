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
    val notes: String? = null,
    val overview: String = "",
    val airDate: String = "",
    val voteAverage: Double = 0.0,
    val runtime: Int = 0
) {
    fun getIdentifier(): String {
        val season = seasonNumber.toString().padStart(2, '0')
        val episode = episodeNumber.toString().padStart(2, '0')
        return "S${season}E${episode}"
    }
}
