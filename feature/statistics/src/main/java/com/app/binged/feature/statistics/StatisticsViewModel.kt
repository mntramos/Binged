package com.app.binged.feature.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.binged.domain.model.Episode
import com.app.binged.domain.usecase.GetAllEpisodesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

data class UserStats(
    val totalEpisodes: Int = 0,
    val totalShows: Int = 0,
    val totalHours: Long = 0,
    val episodesThisWeek: Int = 0,
    val episodesThisMonth: Int = 0
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    getAllEpisodesUseCase: GetAllEpisodesUseCase
) : ViewModel() {

    val stats: StateFlow<UserStats> = getAllEpisodesUseCase()
        .map { episodes -> calculateStats(episodes) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserStats()
        )

    private fun calculateStats(episodes: List<Episode>): UserStats {
        val totalEpisodes = episodes.size
        val totalShows = episodes.map { it.showId }.distinct().size
        val totalHours = (totalEpisodes * 30L) / 60
        val episodesThisWeek = episodes.count { episode ->
            val date = episode.watchedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            val now = Instant.now().atZone(ZoneId.systemDefault()).toLocalDate()
            val daysBetween = Duration.between(date.atStartOfDay(), now.atStartOfDay()).toDays()
            daysBetween in 0..6
        }
        val episodesThisMonth = episodes.count { episode ->
            val date = episode.watchedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            val now = Instant.now().atZone(ZoneId.systemDefault()).toLocalDate()
            val daysBetween = Duration.between(date.atStartOfDay(), now.atStartOfDay()).toDays()
            daysBetween in 0..29
        }

        return UserStats(
            totalEpisodes = totalEpisodes,
            totalShows = totalShows,
            totalHours = totalHours,
            episodesThisWeek = episodesThisWeek,
            episodesThisMonth = episodesThisMonth
        )
    }
}
