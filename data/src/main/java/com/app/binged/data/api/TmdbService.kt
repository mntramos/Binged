package com.app.binged.data.api

import com.app.binged.data.model.TmdbEpisodeResponse
import com.app.binged.data.model.TmdbSearchResponse
import com.app.binged.data.model.TmdbShowResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbService {

    @GET("tv/{id}")
    suspend fun getShowDetails(
        @Path("id") id: Int
    ): TmdbShowResponse

    @GET("tv/{id}/season/{season}/episode/{episode}")
    suspend fun getEpisodeDetails(
        @Path("id") id: Int,
        @Path("season") season: Int,
        @Path("episode") episode: Int,
    ): TmdbEpisodeResponse

    @GET("search/tv")
    suspend fun searchShows(
        @Query("query") query: String
    ): TmdbSearchResponse

    // TODO
    @GET("tv/popular")
    suspend fun getPopularShows(): TmdbSearchResponse
}
