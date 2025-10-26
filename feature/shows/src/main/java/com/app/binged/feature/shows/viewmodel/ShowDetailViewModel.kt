package com.app.binged.feature.shows.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.binged.core.utils.Result
import com.app.binged.core.utils.UiEvent
import com.app.binged.domain.model.Episode
import com.app.binged.domain.model.Show
import com.app.binged.domain.usecase.DeleteEpisodeUseCase
import com.app.binged.domain.usecase.GetEpisodesForShowUseCase
import com.app.binged.domain.usecase.GetShowDetailsUseCase
import com.app.binged.domain.usecase.GetTrackedShowsUseCase
import com.app.binged.domain.usecase.TrackShowUseCase
import com.app.binged.domain.usecase.UntrackShowUseCase
import com.app.binged.domain.usecase.UpdateFavoriteStatusUseCase
import com.app.binged.domain.usecase.UpdateWatchingStatusUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ShowDetailViewModel(
    private val getShowDetailsUseCase: GetShowDetailsUseCase,
    private val getEpisodesForShowUseCase: GetEpisodesForShowUseCase,
    private val trackShowUseCase: TrackShowUseCase,
    private val untrackShowUseCase: UntrackShowUseCase,
    private val deleteEpisodeUseCase: DeleteEpisodeUseCase,
    private val updateFavoriteStatusUseCase: UpdateFavoriteStatusUseCase,
    private val updateWatchingStatusUseCase: UpdateWatchingStatusUseCase,
    getTrackedShowsUseCase: GetTrackedShowsUseCase
) : ViewModel() {

    private val _showDetails = MutableStateFlow<Result<Show>>(Result.Loading)
    val showDetails: StateFlow<Result<Show>> = _showDetails

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val trackedShows = getTrackedShowsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val isTracked = MutableStateFlow(false)
    val isWatching = MutableStateFlow(false)
    val isFavorite = MutableStateFlow(false)

    val episodes: StateFlow<List<Episode>> = MutableStateFlow(emptyList())

    fun loadShowDetails(showId: Int) {
        viewModelScope.launch {
            _showDetails.value = Result.Loading

            try {
                val result = getShowDetailsUseCase(showId)
                _showDetails.value = result

                if (result is Result.Success) {
                    checkIfShowIsTracked(result.data)
                    checkIfShowIsWatching(result.data)
                    checkIfShowIsFavorite(result.data)
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

    private fun checkIfShowIsWatching(show: Show) {
        viewModelScope.launch {
            trackedShows.collect { shows ->
                isWatching.value = shows.firstOrNull { it.id == show.id }?.isWatching ?: false
            }
        }
    }

    private fun checkIfShowIsFavorite(show: Show) {
        viewModelScope.launch {
            trackedShows.collect { shows ->
                isFavorite.value = shows.firstOrNull { it.id == show.id }?.isFavorite ?: false
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
            try {
                trackShowUseCase(show)
                isTracked.value = true
                _uiEvent.emit(UiEvent.ShowSnackbar("Successfully added ${show.name}"))
            } catch (_: Exception) {
                _uiEvent.emit(UiEvent.ShowSnackbar("Failed to add ${show.name}"))
            }
        }
    }

    fun untrackShow() {
        viewModelScope.launch {
            val show = (_showDetails.value as? Result.Success)?.data ?: return@launch
            val untrackShow = untrackShowUseCase(show)
            if (untrackShow > 0) {
                isTracked.value = false
                _uiEvent.emit(UiEvent.ShowSnackbar("Successfully removed ${show.name}"))
            } else {
                _uiEvent.emit(UiEvent.ShowSnackbar("Failed to remove ${show.name}"))
            }
        }
    }

    fun updateWatchingStatus(status: Boolean) {
        viewModelScope.launch {
            val show = (_showDetails.value as? Result.Success)?.data ?: return@launch
            val result = updateWatchingStatusUseCase(show, status)
            if (result <= 0) {
                _uiEvent.emit(UiEvent.ShowSnackbar("Failed to perform action"))
                return@launch
            }

            isWatching.value = status
            val message = if (status) "Added to currently watching" else "Removed from currently watching"
            _uiEvent.emit(UiEvent.ShowSnackbar(message))
        }
    }

    fun updateFavoriteStatus(status: Boolean) {
        viewModelScope.launch {
            val show = (_showDetails.value as? Result.Success)?.data ?: return@launch
            val result = updateFavoriteStatusUseCase(show, status)
            if (result <= 0) {
                _uiEvent.emit(UiEvent.ShowSnackbar("Failed to perform action"))
                return@launch
            }

            isFavorite.value = status
            val message = if (status) "Added to favorites" else "Removed from favorites"
            _uiEvent.emit(UiEvent.ShowSnackbar(message))
        }
    }

    fun deleteEpisode(episode: Episode) {
        viewModelScope.launch {
            deleteEpisodeUseCase(episode)
        }
    }
}
