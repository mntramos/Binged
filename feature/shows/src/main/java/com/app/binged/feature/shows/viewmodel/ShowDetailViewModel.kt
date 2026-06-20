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
import com.app.binged.domain.usecase.LogEpisodeUseCase
import com.app.binged.domain.usecase.TrackShowUseCase
import com.app.binged.domain.usecase.UntrackShowUseCase
import com.app.binged.domain.usecase.UpdateFavoriteStatusUseCase
import com.app.binged.domain.usecase.UpdateWatchingStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShowDetailViewModel @Inject constructor(
    private val getShowDetailsUseCase: GetShowDetailsUseCase,
    private val getEpisodesForShowUseCase: GetEpisodesForShowUseCase,
    private val trackShowUseCase: TrackShowUseCase,
    private val untrackShowUseCase: UntrackShowUseCase,
    private val deleteEpisodeUseCase: DeleteEpisodeUseCase,
    private val logEpisodeUseCase: LogEpisodeUseCase,
    private val updateFavoriteStatusUseCase: UpdateFavoriteStatusUseCase,
    private val updateWatchingStatusUseCase: UpdateWatchingStatusUseCase,
    getTrackedShowsUseCase: GetTrackedShowsUseCase
) : ViewModel() {

    private val _showDetails = MutableStateFlow<Result<Show>>(Result.Loading)
    val showDetails: StateFlow<Result<Show>> = _showDetails

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val _episodes = MutableStateFlow<List<Episode>>(emptyList())
    val episodes: StateFlow<List<Episode>> = _episodes

    private val _isTracked = MutableStateFlow(false)
    val isTracked: StateFlow<Boolean> = _isTracked

    private val _isWatching = MutableStateFlow(false)
    val isWatching: StateFlow<Boolean> = _isWatching

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite

    private val trackedShows = getTrackedShowsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private var currentShow: Show? = null
    private var currentShowId: Int = 0
    private var lastDeletedShow: Show? = null
    private var lastDeletedEpisode: Episode? = null

    fun loadShowDetails(showId: Int) {
        if (currentShowId == showId && _showDetails.value !is Result.Loading) return
        currentShowId = showId

        viewModelScope.launch {
            _showDetails.value = Result.Loading
            val result = getShowDetailsUseCase(showId)
            _showDetails.value = result

            if (result is Result.Success) {
                currentShow = result.data
                observeTrackedState(result.data)
            }
        }

        viewModelScope.launch {
            getEpisodesForShowUseCase(showId).collect { loadedEpisodes ->
                _episodes.value = loadedEpisodes
            }
        }
    }

    private fun observeTrackedState(show: Show) {
        viewModelScope.launch {
            trackedShows.collect { shows ->
                val tracked = shows.find { it.id == show.id }
                _isTracked.value = tracked != null
                _isWatching.value = tracked?.isWatching ?: false
                _isFavorite.value = tracked?.isFavorite ?: false
            }
        }
    }

    fun trackShow() {
        viewModelScope.launch {
            val show = currentShow ?: return@launch
            try {
                trackShowUseCase(show)
                _isTracked.value = true
                _uiEvent.emit(UiEvent.ShowSnackbar("Added ${show.name}"))
            } catch (_: Exception) {
                _uiEvent.emit(UiEvent.ShowSnackbar("Failed to add ${show.name}"))
            }
        }
    }

    fun untrackShow() {
        viewModelScope.launch {
            val show = currentShow ?: return@launch
            val result = untrackShowUseCase(show)
            if (result > 0) {
                _isTracked.value = false
                lastDeletedShow = show
                _uiEvent.emit(UiEvent.ShowSnackbarWithAction("Removed ${show.name}", "Undo"))
            } else {
                _uiEvent.emit(UiEvent.ShowSnackbar("Failed to remove ${show.name}"))
            }
        }
    }

    fun updateWatchingStatus(status: Boolean) {
        viewModelScope.launch {
            val show = currentShow ?: return@launch
            val result = updateWatchingStatusUseCase(show, status)
            if (result <= 0) {
                _uiEvent.emit(UiEvent.ShowSnackbar("Failed to update watching status"))
                return@launch
            }
            _isWatching.value = status
            val msg = if (status) "Marked as watching" else "Removed from watching"
            _uiEvent.emit(UiEvent.ShowSnackbar(msg))
        }
    }

    fun updateFavoriteStatus(status: Boolean) {
        viewModelScope.launch {
            val show = currentShow ?: return@launch
            val result = updateFavoriteStatusUseCase(show, status)
            if (result <= 0) {
                _uiEvent.emit(UiEvent.ShowSnackbar("Failed to update favorite"))
                return@launch
            }
            _isFavorite.value = status
            val msg = if (status) "Added to favorites" else "Removed from favorites"
            _uiEvent.emit(UiEvent.ShowSnackbar(msg))
        }
    }

    fun deleteEpisode(episode: Episode) {
        viewModelScope.launch {
            lastDeletedEpisode = episode
            deleteEpisodeUseCase(episode)
            _uiEvent.emit(UiEvent.ShowSnackbarWithAction("Deleted ${episode.showName} S${episode.seasonNumber}E${episode.episodeNumber}", "Undo"))
        }
    }

    fun undoLastAction() {
        viewModelScope.launch {
            lastDeletedShow?.let { show ->
                trackShowUseCase(show)
                lastDeletedShow = null
                _isTracked.value = true
                _uiEvent.emit(UiEvent.ShowSnackbar("Restored ${show.name}"))
                return@launch
            }
            lastDeletedEpisode?.let { episode ->
                logEpisodeUseCase(episode)
                lastDeletedEpisode = null
                _uiEvent.emit(UiEvent.ShowSnackbar("Restored ${episode.showName} S${episode.seasonNumber}E${episode.episodeNumber}"))
            }
        }
    }
}
