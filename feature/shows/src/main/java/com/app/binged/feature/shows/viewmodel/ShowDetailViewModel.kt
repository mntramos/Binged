package com.app.binged.feature.shows.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.binged.core.utils.Result
import com.app.binged.core.utils.UiEvent
import com.app.binged.domain.model.Episode
import com.app.binged.domain.model.Show
import com.app.binged.domain.usecase.DeleteEpisodeUseCase
import com.app.binged.domain.usecase.GetEpisodesForShowUseCase
import com.app.binged.domain.usecase.GetSeasonEpisodesUseCase
import com.app.binged.domain.usecase.GetShowDetailsUseCase
import com.app.binged.domain.usecase.GetTrackedShowsUseCase
import com.app.binged.domain.usecase.LogEpisodeUseCase
import com.app.binged.domain.usecase.TrackShowUseCase
import com.app.binged.domain.usecase.UntrackShowUseCase
import com.app.binged.domain.usecase.UpdateCollapsedSeasonsUseCase
import com.app.binged.domain.usecase.UpdateFavoriteStatusUseCase
import com.app.binged.domain.usecase.UpdateSeenEpisodesUseCase
import com.app.binged.domain.usecase.UpdateWatchingStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShowDetailViewModel @Inject constructor(
    private val getShowDetailsUseCase: GetShowDetailsUseCase,
    private val getEpisodesForShowUseCase: GetEpisodesForShowUseCase,
    private val getSeasonEpisodesUseCase: GetSeasonEpisodesUseCase,
    private val trackShowUseCase: TrackShowUseCase,
    private val untrackShowUseCase: UntrackShowUseCase,
    private val deleteEpisodeUseCase: DeleteEpisodeUseCase,
    private val logEpisodeUseCase: LogEpisodeUseCase,
    private val updateFavoriteStatusUseCase: UpdateFavoriteStatusUseCase,
    private val updateWatchingStatusUseCase: UpdateWatchingStatusUseCase,
    private val updateSeenEpisodesUseCase: UpdateSeenEpisodesUseCase,
    private val updateCollapsedSeasonsUseCase: UpdateCollapsedSeasonsUseCase,
    getTrackedShowsUseCase: GetTrackedShowsUseCase
) : ViewModel() {

    private val _showDetails = MutableStateFlow<Result<Show>>(Result.Loading)
    val showDetails: StateFlow<Result<Show>> = _showDetails

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val _episodes = MutableStateFlow<List<Episode>>(emptyList())
    val episodes: StateFlow<List<Episode>> = _episodes

    private val _catalog = MutableStateFlow<List<Episode>>(emptyList())
    val catalog: StateFlow<List<Episode>> = _catalog

    private val _seenEpisodes = MutableStateFlow<Set<String>>(emptySet())
    val seenEpisodes: StateFlow<Set<String>> = _seenEpisodes.asStateFlow()

    private val _collapsedSeasons = MutableStateFlow<Set<Int>>(emptySet())
    val collapsedSeasons: StateFlow<Set<Int>> = _collapsedSeasons.asStateFlow()

    private val _isCatalogLoading = MutableStateFlow(false)
    val isCatalogLoading: StateFlow<Boolean> = _isCatalogLoading

    private var isFetchingCatalog = false

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
                _seenEpisodes.value = tracked?.seenEpisodes?.toSet() ?: emptySet()
                _collapsedSeasons.value = tracked?.collapsedSeasons ?: emptySet()
                if (tracked != null && _catalog.value.isEmpty() && !isFetchingCatalog) {
                    isFetchingCatalog = true
                    _isCatalogLoading.value = true
                    fetchEpisodeCatalog(show)
                }
            }
        }
    }

    private fun fetchEpisodeCatalog(show: Show) {
        viewModelScope.launch {
            val results = (1..show.seasonCount.coerceAtLeast(1)).map { season ->
                async { getSeasonEpisodesUseCase(show.id, season) }
            }.awaitAll()
            _catalog.value = results.filterIsInstance<Result.Success<List<Episode>>>().flatMap { it.data }
            _isCatalogLoading.value = false
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

    fun toggleSeen(seasonNumber: Int, episodeNumber: Int) {
        viewModelScope.launch {
            if (isLogged(seasonNumber, episodeNumber)) return@launch
            val identifier = Episode.identifierOf(seasonNumber, episodeNumber)
            val current = _seenEpisodes.value
            val next = if (identifier in current) current - identifier else current + identifier
            persistSeen(next)
        }
    }

    fun setSeasonSeen(seasonNumber: Int, seen: Boolean) {
        viewModelScope.launch {
            val current = _seenEpisodes.value
            val seasonIds = seasonIdentifiers(seasonNumber)
            if (seasonIds.isEmpty()) return@launch
            val next = if (seen) current + seasonIds else {
                val logged = loggedIdentifiers(seasonNumber)
                current - seasonIds + logged
            }
            persistSeen(next)
        }
    }

    fun toggleSeasonCollapsed(seasonNumber: Int) {
        val next = _collapsedSeasons.value.let {
            if (seasonNumber in it) it - seasonNumber else it + seasonNumber
        }
        _collapsedSeasons.value = next
        viewModelScope.launch {
            updateCollapsedSeasonsUseCase(currentShowId, next)
        }
    }

    fun isSeen(seasonNumber: Int, episodeNumber: Int): Boolean =
        Episode.identifierOf(seasonNumber, episodeNumber) in _seenEpisodes.value

    fun isLogged(seasonNumber: Int, episodeNumber: Int): Boolean =
        _episodes.value.any { it.seasonNumber == seasonNumber && it.episodeNumber == episodeNumber }

    private fun persistSeen(next: Set<String>) {
        _seenEpisodes.value = next
        viewModelScope.launch {
            updateSeenEpisodesUseCase(currentShowId, next, UpdateSeenEpisodesUseCase.Mode.SET)
        }
    }

    private fun seasonIdentifiers(seasonNumber: Int): Set<String> {
        val catalogIds = _catalog.value.map { it.episodeId }.toSet()
        val fromCatalog = _catalog.value.filter { it.seasonNumber == seasonNumber }
        val localOnly = _episodes.value.filter {
            it.seasonNumber == seasonNumber && it.episodeId !in catalogIds
        }
        return (fromCatalog + localOnly).map { it.getIdentifier() }.toSet()
    }

    private fun loggedIdentifiers(seasonNumber: Int): Set<String> =
        _episodes.value.filter { it.seasonNumber == seasonNumber }.map { it.getIdentifier() }.toSet()

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
