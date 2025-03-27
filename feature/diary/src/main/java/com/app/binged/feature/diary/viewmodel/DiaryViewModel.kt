package com.app.binged.feature.diary.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.binged.domain.model.Episode
import com.app.binged.domain.usecase.GetAllEpisodesUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class DiaryViewModel(
    getAllEpisodesUseCase: GetAllEpisodesUseCase
) : ViewModel() {
    val episodes: StateFlow<List<Episode>> = getAllEpisodesUseCase()
        .map { it.sortedBy { it.watchedDate } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
