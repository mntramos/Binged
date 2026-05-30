package com.app.binged.feature.shows.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.binged.core.utils.Result
import com.app.binged.domain.contract.EpisodeRepository
import com.app.binged.domain.model.Episode
import com.app.binged.domain.usecase.DeleteEpisodeUseCase
import com.app.binged.domain.usecase.GetEpisodeDetailsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface EpisodeDetailEvent {
    data object Deleted : EpisodeDetailEvent
}

@HiltViewModel
class EpisodeDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getEpisodeDetailsUseCase: GetEpisodeDetailsUseCase,
    private val episodeRepository: EpisodeRepository,
    private val deleteEpisodeUseCase: DeleteEpisodeUseCase
) : ViewModel() {

    private val showId: Int = savedStateHandle["showId"] ?: 0
    private val season: Int = savedStateHandle["season"] ?: 0
    private val episode: Int = savedStateHandle["episode"] ?: 0
    private val showName: String = savedStateHandle["showName"] ?: ""

    private val _episodeDetail = MutableStateFlow<Result<Episode>>(Result.Loading)
    val episodeDetail: StateFlow<Result<Episode>> = _episodeDetail

    private val _events = MutableSharedFlow<EpisodeDetailEvent>()
    val events: SharedFlow<EpisodeDetailEvent> = _events.asSharedFlow()

    init {
        loadEpisodeDetails()
    }

    private fun loadEpisodeDetails() {
        viewModelScope.launch {
            _episodeDetail.value = Result.Loading
            val result = getEpisodeDetailsUseCase(showId, season, episode)
            if (result is Result.Success) {
                val localEpisode = episodeRepository.getLocalEpisode(showId, season, episode)
                val notes = localEpisode?.notes ?: ""
                val merged = result.data.copy(showName = showName, notes = notes)
                _episodeDetail.value = Result.Success(merged)
            } else {
                _episodeDetail.value = result
            }
        }
    }

    fun deleteEpisode() {
        viewModelScope.launch {
            val detail = _episodeDetail.value
            if (detail is Result.Success) {
                deleteEpisodeUseCase(detail.data)
                _events.emit(EpisodeDetailEvent.Deleted)
            }
        }
    }

    fun saveNotes(notes: String) {
        viewModelScope.launch {
            val detail = _episodeDetail.value
            if (detail is Result.Success) {
                val localEpisode = episodeRepository.getLocalEpisode(showId, season, episode)
                val episodeToSave = localEpisode?.copy(notes = notes)
                    ?: detail.data.copy(notes = notes)
                episodeRepository.saveEpisode(episodeToSave)
                _episodeDetail.value = Result.Success(detail.data.copy(notes = notes))
            }
        }
    }
}
