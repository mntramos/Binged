package com.app.binged.data.model

import com.google.gson.annotations.SerializedName

data class TmdbShowResponse(
    val id: Int,
    val name: String,
    val overview: String,
    @SerializedName("backdrop_path")
    val backdropPath: String?,
    @SerializedName("poster_path")
    val posterPath: String?,
    @SerializedName("first_air_date")
    val firstAirDate: String,
    @SerializedName("vote_average")
    val voteAverage: Double
)
