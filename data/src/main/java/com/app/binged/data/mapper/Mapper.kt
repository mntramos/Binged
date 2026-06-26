package com.app.binged.data.mapper

import com.app.binged.data.database.entity.EpisodeEntity
import com.app.binged.data.database.entity.ShowEntity
import com.app.binged.data.model.TmdbEpisodeResponse
import com.app.binged.data.model.TmdbShowResponse
import com.app.binged.domain.model.Episode
import com.app.binged.domain.model.Show
import java.time.Instant

fun TmdbShowResponse.toDomain(): Show {
    return Show(
        id = id,
        name = name,
        overview = overview,
        backdropPath = backdropPath,
        posterPath = posterPath,
        firstAirDate = firstAirDate,
        rating = voteAverage,
        tagline = tagline ?: "",
        seasonCount = numberOfSeasons ?: 1
    )
}

fun ShowEntity.toDomain(): Show {
    return Show(
        id = id,
        name = name,
        overview = overview,
        backdropPath = backdropPath,
        posterPath = posterPath,
        firstAirDate = firstAirDate,
        rating = rating,
        tagline = tagline,
        seasonCount = seasonCount,
        isFavorite = isFavorite,
        isWatching = isWatching
    )
}

fun Show.toEntity(): ShowEntity {
    return ShowEntity(
        id = id,
        name = name,
        overview = overview,
        backdropPath = backdropPath,
        posterPath = posterPath,
        firstAirDate = firstAirDate,
        rating = rating,
        tagline = tagline,
        seasonCount = seasonCount,
        isFavorite = isFavorite,
        isWatching = isWatching
    )
}

fun TmdbEpisodeResponse.toDomain(showId: Int = 0, showName: String = ""): Episode {
    return Episode(
        episodeId = id,
        showId = showId,
        showName = showName,
        seasonNumber = seasonNumber,
        episodeNumber = episodeNumber,
        title = name,
        watchedDate = Instant.now(),
        stillPath = stillPath,
        notes = "",
        overview = overview,
        airDate = airDate,
        voteAverage = voteAverage,
        runtime = runtime ?: 0
    )
}

fun EpisodeEntity.toDomain(): Episode {
    return Episode(
        id = id,
        episodeId = episodeId,
        showId = showId,
        showName = showName,
        seasonNumber = seasonNumber,
        episodeNumber = episodeNumber,
        title = title,
        watchedDate = Instant.ofEpochMilli(watchedDate),
        stillPath = stillPath,
        notes = notes,
        runtime = runtime
    )
}

fun Episode.toEntity(): EpisodeEntity {
    return EpisodeEntity(
        id = id,
        episodeId = episodeId,
        showId = showId,
        showName = showName,
        seasonNumber = seasonNumber,
        episodeNumber = episodeNumber,
        title = title,
        watchedDate = watchedDate.toEpochMilli(),
        stillPath = stillPath,
        notes = notes,
        runtime = runtime
    )
}
