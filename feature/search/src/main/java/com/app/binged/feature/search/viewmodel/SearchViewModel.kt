package com.app.binged.feature.search.viewmodel

import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.binged.domain.model.Show
import com.app.binged.domain.usecase.SearchShowsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.app.binged.core.utils.Result
import com.app.binged.core.utils.UiEvent
import com.app.binged.domain.usecase.TrackShowUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class SearchViewModel(
    private val searchShowsUseCase: SearchShowsUseCase,
    private val trackShowUseCase: TrackShowUseCase,
) : ViewModel() {

    private val _searchResults = MutableStateFlow<Result<List<Show>>>(Result.Success(emptyList()))
    val searchResults: StateFlow<Result<List<Show>>> = _searchResults

    private val _searchInProgress = MutableStateFlow(false)
    val searchInProgress: StateFlow<Boolean> = _searchInProgress

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun search(query: String) {
        if (query.isBlank()) return

        viewModelScope.launch {
            _searchInProgress.value = true
            _searchResults.value = searchShowsUseCase(query)
            _searchInProgress.value = false
        }
    }

    fun clearSearchResults() {
        _searchResults.value = Result.Success(emptyList())
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
}
