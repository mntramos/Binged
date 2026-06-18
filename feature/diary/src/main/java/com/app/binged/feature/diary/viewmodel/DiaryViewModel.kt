package com.app.binged.feature.diary.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.binged.core.utils.UiEvent
import com.app.binged.domain.model.Episode
import com.app.binged.domain.usecase.DeleteEpisodeUseCase
import com.app.binged.domain.usecase.GetAllEpisodesUseCase
import com.app.binged.domain.usecase.LogEpisodeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val deleteEpisodeUseCase: DeleteEpisodeUseCase,
    private val logEpisodeUseCase: LogEpisodeUseCase,
    getAllEpisodesUseCase: GetAllEpisodesUseCase
) : ViewModel() {
    val episodes: StateFlow<List<Episode>> = getAllEpisodesUseCase()
        .map { it.sortedWith(compareByDescending<Episode> { it.watchedDate }.thenByDescending { it.id }) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private var lastDeletedEpisode: Episode? = null

    fun deleteEpisode(episode: Episode) {
        viewModelScope.launch {
            lastDeletedEpisode = episode
            deleteEpisodeUseCase(episode)
            _uiEvent.emit(
                UiEvent.ShowSnackbarWithAction(
                    "Deleted ${episode.showName} S${episode.seasonNumber}E${episode.episodeNumber}",
                    "Undo"
                )
            )
        }
    }

    fun undoLastAction() {
        viewModelScope.launch {
            lastDeletedEpisode?.let { episode ->
                logEpisodeUseCase(episode)
                lastDeletedEpisode = null
                _uiEvent.emit(UiEvent.ShowSnackbar("Restored ${episode.showName} S${episode.seasonNumber}E${episode.episodeNumber}"))
            }
        }
    }
}
