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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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

    private val _events = MutableSharedFlow<EpisodeDetailEvent>()
    val events: SharedFlow<EpisodeDetailEvent> = _events.asSharedFlow()

    private val remoteEpisode = MutableStateFlow<Result<Episode>>(Result.Loading)
    private val localEntry = MutableStateFlow<Episode?>(null)

    val isLogged: StateFlow<Boolean> = localEntry
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val episodeDetail: StateFlow<Result<Episode>> =
        combine(remoteEpisode, localEntry) { remote, local ->
            when (remote) {
                is Result.Success -> Result.Success(
                    remote.data.copy(showName = showName, notes = local?.notes.orEmpty())
                )
                else -> remote
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Result.Loading)

    init {
        loadEpisodeDetails()
        observeLocalEntry()
    }

    private fun loadEpisodeDetails() {
        viewModelScope.launch {
            remoteEpisode.value = Result.Loading
            remoteEpisode.value = getEpisodeDetailsUseCase(showId, season, episode)
        }
    }

    private fun observeLocalEntry() {
        viewModelScope.launch {
            episodeRepository.observeLocalEpisode(showId, season, episode).collect { local ->
                localEntry.value = local
            }
        }
    }

    fun deleteEpisode() {
        viewModelScope.launch {
            val detail = episodeDetail.value
            if (detail is Result.Success) {
                deleteEpisodeUseCase(detail.data)
                _events.emit(EpisodeDetailEvent.Deleted)
            }
        }
    }

    fun saveNotes(notes: String) {
        viewModelScope.launch {
            val episodeToSave = localEntry.value?.copy(notes = notes)
                ?: (remoteEpisode.value as? Result.Success)?.data?.copy(notes = notes)
            episodeToSave?.let { episodeRepository.saveEpisode(it) }
        }
    }
}
