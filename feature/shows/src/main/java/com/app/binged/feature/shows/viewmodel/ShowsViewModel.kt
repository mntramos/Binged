package com.app.binged.feature.shows.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.binged.domain.model.Show
import com.app.binged.domain.usecase.GetTrackedShowsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ShowsViewModel @Inject constructor(
    getTrackedShowsUseCase: GetTrackedShowsUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _showSearch = MutableStateFlow(false)
    val showSearch: StateFlow<Boolean> = _showSearch.asStateFlow()

    val shows: StateFlow<List<Show>> = getTrackedShowsUseCase()
        .map { it.sortedBy { it.name } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val filteredShows: StateFlow<List<Show>> = combine(shows, _searchQuery) { allShows, query ->
        if (query.isBlank()) allShows
        else allShows.filter { it.name.contains(query, ignoreCase = true) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val prefs = context.getSharedPreferences("shows_prefs", Context.MODE_PRIVATE)
    private val _isGridView = MutableStateFlow(prefs.getBoolean("is_grid_view", true))
    val isGridView: StateFlow<Boolean> = _isGridView.asStateFlow()

    private val _gridColumns = MutableStateFlow(prefs.getInt("grid_columns", 3))
    val gridColumns: StateFlow<Int> = _gridColumns.asStateFlow()

    private val _showFavorites = MutableStateFlow(prefs.getBoolean("show_favorites", true))
    val showFavorites: StateFlow<Boolean> = _showFavorites.asStateFlow()

    private val _showWatching = MutableStateFlow(prefs.getBoolean("show_watching", true))
    val showWatching: StateFlow<Boolean> = _showWatching.asStateFlow()

    private val _showAllShows = MutableStateFlow(prefs.getBoolean("show_all_shows", true))
    val showAllShows: StateFlow<Boolean> = _showAllShows.asStateFlow()

    fun toggleView() {
        val newValue = !_isGridView.value
        _isGridView.value = newValue
        prefs.edit().putBoolean("is_grid_view", newValue).apply()
    }

    fun setGridColumns(columns: Int) {
        val clamped = columns.coerceIn(2, 4)
        _gridColumns.value = clamped
        prefs.edit().putInt("grid_columns", clamped).apply()
    }

    fun toggleSearch() {
        _showSearch.value = !_showSearch.value
        if (!_showSearch.value) {
            _searchQuery.value = ""
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleShowFavorites() {
        val newValue = !_showFavorites.value
        _showFavorites.value = newValue
        prefs.edit().putBoolean("show_favorites", newValue).apply()
    }

    fun toggleShowWatching() {
        val newValue = !_showWatching.value
        _showWatching.value = newValue
        prefs.edit().putBoolean("show_watching", newValue).apply()
    }

    fun toggleShowAllShows() {
        val newValue = !_showAllShows.value
        _showAllShows.value = newValue
        prefs.edit().putBoolean("show_all_shows", newValue).apply()
    }
}
