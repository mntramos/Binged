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
import kotlinx.coroutines.flow.asStateFlow
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
    private val entryId: Long = savedStateHandle["entryId"] ?: 0L

    private val _events = MutableSharedFlow<EpisodeDetailEvent>()
    val events: SharedFlow<EpisodeDetailEvent> = _events.asSharedFlow()

    private val remoteEpisode = MutableStateFlow<Result<Episode>>(Result.Loading)

    private val _entries = MutableStateFlow<List<Episode>>(emptyList())
    val entries: StateFlow<List<Episode>> = _entries.asStateFlow()

    private val _selectedId = MutableStateFlow(entryId)

    val selectedEntry: StateFlow<Episode?> = combine(_entries, _selectedId) { list, selectedId ->
        if (selectedId > 0L) {
            list.firstOrNull { it.id == selectedId } ?: list.firstOrNull()
        } else {
            list.firstOrNull()
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val isLogged: StateFlow<Boolean> = selectedEntry
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val episodeDetail: StateFlow<Result<Episode>> =
        combine(remoteEpisode, selectedEntry) { remote, local ->
            when (remote) {
                is Result.Success -> Result.Success(
                    remote.data.copy(showName = showName, notes = local?.notes.orEmpty())
                )
                else -> remote
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Result.Loading)

    init {
        loadEpisodeDetails()
        observeEntries()
    }

    private fun loadEpisodeDetails() {
        viewModelScope.launch {
            remoteEpisode.value = Result.Loading
            remoteEpisode.value = getEpisodeDetailsUseCase(showId, season, episode)
        }
    }

    private fun observeEntries() {
        viewModelScope.launch {
            episodeRepository.observeEpisodeEntries(showId, season, episode).collect { list ->
                _entries.value = list
            }
        }
    }

    fun selectEntry(id: Long) {
        _selectedId.value = id
    }

    fun deleteEntry(id: Long) {
        viewModelScope.launch {
            val entry = _entries.value.firstOrNull { it.id == id } ?: return@launch
            val isLastEntry = _entries.value.size == 1
            deleteEpisodeUseCase(entry)
            if (isLastEntry) {
                _events.emit(EpisodeDetailEvent.Deleted)
            }
        }
    }

    fun updateEntry(notes: String?, isRewatch: Boolean) {
        viewModelScope.launch {
            selectedEntry.value
                ?.copy(notes = notes, isRewatch = isRewatch)
                ?.let { episodeRepository.saveEpisode(it) }
        }
    }
}
