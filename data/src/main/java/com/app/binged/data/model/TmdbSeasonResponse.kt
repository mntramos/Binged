package com.app.binged.data.model

import com.google.gson.annotations.SerializedName

data class TmdbSeasonResponse(
    val id: Int = 0,
    val name: String = "",
    val episodes: List<TmdbEpisodeResponse> = emptyList(),
    @SerializedName("episode_count")
    val episodeCount: Int = 0
)
