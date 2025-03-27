package com.app.binged.feature.tracking.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.binged.core.utils.Result
import com.app.binged.domain.model.Episode
import com.app.binged.domain.usecase.GetEpisodeDetailsUseCase
import com.app.binged.domain.usecase.LogEpisodeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date

class LogEpisodeViewModel(
    private val logEpisodeUseCase: LogEpisodeUseCase,
    private val getEpisodeDetailsUseCase: GetEpisodeDetailsUseCase
) : ViewModel() {

    private val _showId = MutableStateFlow<Int?>(null)

    private val _episodeDetails = MutableStateFlow<Result<Episode>>(Result.Loading)
    val episodeDetails: StateFlow<Result<Episode>> = _episodeDetails

    fun setShowId(id: Int) {
        _showId.value = id
    }

    fun saveEpisode(
        showName: String,
        seasonNumber: Int,
        episodeNumber: Int,
        watchedDate: Date,
        notes: String?
    ) {
        val showId = _showId.value ?: return

        viewModelScope.launch {
            _episodeDetails.value = Result.Loading

            try {
                val result = getEpisodeDetailsUseCase(showId, seasonNumber, episodeNumber)
                _episodeDetails.value = result

                if (result is Result.Success) {
                    val episode = result.data.copy(
                        showId = showId,
                        showName = showName,
                        watchedDate = watchedDate,
                        notes = notes
                    )
                    logEpisodeUseCase(episode)
                }
            } catch (e: Exception) {
                _episodeDetails.value = Result.Error(e)
            }

        }
    }
}
