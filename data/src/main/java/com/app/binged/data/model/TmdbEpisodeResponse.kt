package com.app.binged.data.model

import com.google.gson.annotations.SerializedName

data class TmdbEpisodeResponse(
    val id: Int,
    val name: String,
    @SerializedName("season_number")
    val seasonNumber: Int,
    @SerializedName("episode_number")
    val episodeNumber: Int,
    @SerializedName("still_path")
    val stillPath: String?
)
