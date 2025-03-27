package com.app.binged.feature.shows.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.binged.domain.model.Show
import com.app.binged.domain.usecase.GetTrackedShowsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ShowsViewModel(
    getTrackedShowsUseCase: GetTrackedShowsUseCase
) : ViewModel() {
    val shows: StateFlow<List<Show>> = getTrackedShowsUseCase()
        .map { it.sortedBy { it.name } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
