package com.app.binged.search.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.binged.domain.model.Show
import com.app.binged.domain.usecase.SearchShowsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.app.binged.core.utils.Result

class SearchViewModel(
    private val searchShowsUseCase: SearchShowsUseCase
) : ViewModel() {

    private val _searchResults = MutableStateFlow<Result<List<Show>>>(Result.Success(emptyList()))
    val searchResults: StateFlow<Result<List<Show>>> = _searchResults

    private val _searchInProgress = MutableStateFlow(false)
    val searchInProgress: StateFlow<Boolean> = _searchInProgress

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
}
