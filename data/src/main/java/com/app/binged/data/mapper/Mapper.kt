package com.app.binged.data.mapper

import com.app.binged.data.database.entity.EpisodeEntity
import com.app.binged.data.database.entity.ShowEntity
import com.app.binged.data.model.TmdbEpisodeResponse
import com.app.binged.data.model.TmdbShowResponse
import com.app.binged.domain.model.Episode
import com.app.binged.domain.model.Show
import java.util.Date

// Show mappers
fun TmdbShowResponse.toDomain(): Show {
    return Show(
        id = id,
        name = name,
        overview = overview,
        backdropPath = backdropPath,
        posterPath = posterPath,
        firstAirDate = firstAirDate,
        rating = voteAverage
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
        rating = rating
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
        rating = rating
    )
}

// Episode mappers
fun TmdbEpisodeResponse.toDomain(): Episode {
    return Episode(
        episodeId = id,
        showId = 0,
        showName = "",
        seasonNumber = seasonNumber,
        episodeNumber = episodeNumber,
        title = name,
        watchedDate = Date(),
        stillPath = stillPath,
        notes = ""
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
        watchedDate = Date(watchedDate),
        stillPath = stillPath,
        notes = notes
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
        watchedDate = watchedDate.time,
        stillPath = stillPath,
        notes = notes
    )
}
