package com.app.binged.feature.search.viewmodel

import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.binged.core.utils.Result
import com.app.binged.core.utils.UiEvent
import com.app.binged.domain.model.Show
import com.app.binged.domain.usecase.GetPopularShowsUseCase
import com.app.binged.domain.usecase.GetTrackedShowsUseCase
import com.app.binged.domain.usecase.SearchShowsUseCase
import com.app.binged.domain.usecase.TrackShowUseCase
import com.app.binged.domain.usecase.UntrackShowUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchShowsUseCase: SearchShowsUseCase,
    private val getPopularShowsUseCase: GetPopularShowsUseCase,
    private val trackShowUseCase: TrackShowUseCase,
    private val untrackShowUseCase: UntrackShowUseCase,
    getTrackedShowsUseCase: GetTrackedShowsUseCase
) : ViewModel() {

    private val _searchResults = MutableStateFlow<List<Show>>(emptyList())
    val searchResults: StateFlow<List<Show>> = _searchResults

    private val _popularShows = MutableStateFlow<Result<List<Show>>>(Result.Loading)
    val popularShows: StateFlow<Result<List<Show>>> = _popularShows

    private val _searchInProgress = MutableStateFlow(false)
    val searchInProgress: StateFlow<Boolean> = _searchInProgress

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore

    private val _searchError = MutableStateFlow<String?>(null)
    val searchError: StateFlow<String?> = _searchError

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private var currentPage = 1
    private var totalPages = 1
    private var currentQuery = ""

    val trackedShowIds: StateFlow<Set<Int>> = getTrackedShowsUseCase()
        .map { shows -> shows.map { it.id }.toSet() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptySet()
        )

    init {
        loadPopularShows()
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _popularShows.value = getPopularShowsUseCase()
            _isRefreshing.value = false
        }
    }

    private fun loadPopularShows() {
        viewModelScope.launch {
            _popularShows.value = getPopularShowsUseCase()
        }
    }

    fun search(query: String) {
        if (query.isBlank()) return
        currentQuery = query
        currentPage = 1
        totalPages = 1
        _searchResults.value = emptyList()
        _searchError.value = null
        viewModelScope.launch {
            _searchInProgress.value = true
            when (val result = searchShowsUseCase(query, page = 1)) {
                is Result.Success -> {
                    _searchResults.value = result.data.items
                    currentPage = result.data.currentPage
                    totalPages = result.data.totalPages
                }
                is Result.Error -> {
                    _searchError.value = result.exception.message ?: "Search failed"
                }
                else -> {}
            }
            _searchInProgress.value = false
        }
    }

    fun loadNextPage() {
        if (currentPage >= totalPages || _isLoadingMore.value || _searchInProgress.value) return
        viewModelScope.launch {
            _isLoadingMore.value = true
            when (val result = searchShowsUseCase(currentQuery, page = currentPage + 1)) {
                is Result.Success -> {
                    _searchResults.value = _searchResults.value + result.data.items
                    currentPage = result.data.currentPage
                    totalPages = result.data.totalPages
                }
                is Result.Error -> {
                    _uiEvent.emit(UiEvent.ShowSnackbar("Failed to load more results"))
                }
                else -> {}
            }
            _isLoadingMore.value = false
        }
    }

    fun clearSearchResults() {
        _searchResults.value = emptyList()
        _searchError.value = null
        currentPage = 1
        totalPages = 1
    }

    fun trackShow(show: Show) {
        viewModelScope.launch {
            try {
                trackShowUseCase(show)
                _uiEvent.emit(UiEvent.ShowSnackbar("Successfully added ${show.name}"))
            } catch (_: SQLiteConstraintException) {
                _uiEvent.emit(UiEvent.ShowSnackbar("${show.name} already added"))
            } catch (_: Exception) {
                _uiEvent.emit(UiEvent.ShowSnackbar("Failed to add ${show.name}"))
            }
        }
    }

    fun untrackShow(show: Show) {
        viewModelScope.launch {
            try {
                untrackShowUseCase(show)
                _uiEvent.emit(UiEvent.ShowSnackbar("Removed ${show.name}"))
            } catch (_: Exception) {
                _uiEvent.emit(UiEvent.ShowSnackbar("Failed to remove ${show.name}"))
            }
        }
    }
}
