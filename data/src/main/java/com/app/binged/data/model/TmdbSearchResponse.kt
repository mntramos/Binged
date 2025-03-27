package com.app.binged.data.model

import com.google.gson.annotations.SerializedName

data class TmdbSearchResponse(
    val page: Int,
    val results: List<TmdbShowResponse>,
    @SerializedName("total_results")
    val totalResults: Int,
    @SerializedName("total_pages")
    val totalPages: Int
)
