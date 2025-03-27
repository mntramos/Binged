package com.app.binged.feature.shows.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.binged.domain.model.Show
import com.app.binged.domain.usecase.DeleteEpisodeUseCase
import com.app.binged.domain.usecase.GetEpisodesForShowUseCase
import com.app.binged.domain.usecase.GetShowDetailsUseCase
import com.app.binged.domain.usecase.GetTrackedShowsUseCase
import com.app.binged.domain.usecase.TrackShowUseCase
import com.app.binged.domain.usecase.UntrackShowUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import com.app.binged.core.utils.Result
import com.app.binged.domain.model.Episode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ShowDetailViewModel(
    private val getShowDetailsUseCase: GetShowDetailsUseCase,
    private val getEpisodesForShowUseCase: GetEpisodesForShowUseCase,
    private val trackShowUseCase: TrackShowUseCase,
    private val untrackShowUseCase: UntrackShowUseCase,
    private val deleteEpisodeUseCase: DeleteEpisodeUseCase,
    getTrackedShowsUseCase: GetTrackedShowsUseCase
) : ViewModel() {

    private val _showDetails = MutableStateFlow<Result<Show>>(Result.Loading)
    val showDetails: StateFlow<Result<Show>> = _showDetails

    private val trackedShows = getTrackedShowsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val isTracked = MutableStateFlow(false)

    val episodes: StateFlow<List<Episode>> = MutableStateFlow(emptyList())

    fun loadShowDetails(showId: Int) {
        viewModelScope.launch {
            _showDetails.value = Result.Loading

            try {
                val result = getShowDetailsUseCase(showId)
                _showDetails.value = result

                if (result is Result.Success) {
                    checkIfShowIsTracked(result.data)
                    loadEpisodes(showId)
                }
            } catch (e: Exception) {
                _showDetails.value = Result.Error(e)
            }
        }
    }

    private fun checkIfShowIsTracked(show: Show) {
        viewModelScope.launch {
            trackedShows.collect { shows ->
                isTracked.value = shows.any { it.id == show.id }
            }
        }
    }

    private fun loadEpisodes(showId: Int) {
        viewModelScope.launch {
            getEpisodesForShowUseCase(showId).collect { loadedEpisodes ->
                (episodes as MutableStateFlow).value = loadedEpisodes
            }
        }
    }

    fun trackShow() {
        viewModelScope.launch {
            val show = (_showDetails.value as? Result.Success)?.data ?: return@launch
            trackShowUseCase(show)
            isTracked.value = true
        }
    }

    fun untrackShow() {
        viewModelScope.launch {
            val show = (_showDetails.value as? Result.Success)?.data ?: return@launch
            untrackShowUseCase(show)
            isTracked.value = false
        }
    }

    fun deleteEpisode(episode: Episode) {
        viewModelScope.launch {
            deleteEpisodeUseCase(episode)
        }
    }
}
