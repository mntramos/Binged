package com.app.binged.domain.model

data class Show(
    val id: Int,
    val name: String,
    val overview: String,
    val backdropPath: String?,
    val posterPath: String?,
    val firstAirDate: String,
    val rating: Double
)
