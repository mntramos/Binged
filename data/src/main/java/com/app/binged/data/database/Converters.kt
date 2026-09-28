package com.app.binged.data.database

import androidx.room.TypeConverter

/**
 * Room has no native column type for collections, so `ShowEntity.seenEpisodes`
 * is persisted as a single pipe-delimited string. Identifiers are always of the
 * form `S01E02`, so the delimiter can never collide with the payload.
 */
object Converters {
    private const val DELIMITER = "|"

    @TypeConverter
    fun seenEpisodesToString(seenEpisodes: List<String>?): String =
        seenEpisodes?.filter { it.isNotEmpty() }?.joinToString(DELIMITER).orEmpty()

    @TypeConverter
    fun stringToSeenEpisodes(value: String?): List<String> =
        value?.split(DELIMITER)?.filter { it.isNotEmpty() } ?: emptyList()

    @TypeConverter
    fun collapsedSeasonsToString(collapsedSeasons: Set<Int>?): String =
        collapsedSeasons?.filter { it >= 0 }?.joinToString(DELIMITER).orEmpty()

    @TypeConverter
    fun stringToCollapsedSeasons(value: String?): Set<Int> =
        value?.split(DELIMITER)?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()
}
