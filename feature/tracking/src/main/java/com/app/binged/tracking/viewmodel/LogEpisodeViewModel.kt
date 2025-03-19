package com.app.binged.tracking.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.binged.domain.model.Episode
import com.app.binged.domain.usecase.LogEpisodeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date

class LogEpisodeViewModel(
    private val logEpisodeUseCase: LogEpisodeUseCase
) : ViewModel() {

    private val _showId = MutableStateFlow<Int?>(null)

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess

    fun setShowId(id: Int) {
        _showId.value = id
    }

    fun saveEpisode(
        seasonNumber: Int,
        episodeNumber: Int,
        title: String,
        watchedDate: Date,
        notes: String?
    ) {
        val showId = _showId.value ?: return

        viewModelScope.launch {
            val episode = Episode(
                showId = showId,
                seasonNumber = seasonNumber,
                episodeNumber = episodeNumber,
                title = title,
                watchedDate = watchedDate,
                notes = notes
            )

            val id = logEpisodeUseCase(episode)
            _saveSuccess.value = id > 0
        }
    }
}
