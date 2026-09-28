package com.app.binged.feature.tracking.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.binged.core.utils.Result
import com.app.binged.domain.contract.EpisodeRepository
import com.app.binged.domain.model.Episode
import com.app.binged.domain.usecase.GetEpisodeDetailsUseCase
import com.app.binged.domain.usecase.LogEpisodeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class LogEpisodeViewModel @Inject constructor(
    private val logEpisodeUseCase: LogEpisodeUseCase,
    private val getEpisodeDetailsUseCase: GetEpisodeDetailsUseCase,
    private val episodeRepository: EpisodeRepository
) : ViewModel() {

    private val _showId = MutableStateFlow<Int?>(null)
    private val _episodeDetails = MutableStateFlow<Result<Episode>>(Result.Loading)
    val episodeDetails: StateFlow<Result<Episode>> = _episodeDetails

    private val _verificationState = MutableStateFlow<Result<Episode>?>(null)
    val verificationState: StateFlow<Result<Episode>?> = _verificationState

    private val _alreadyLogged = MutableStateFlow(false)
    val alreadyLogged: StateFlow<Boolean> = _alreadyLogged.asStateFlow()

    private val _previousRating = MutableStateFlow<Int?>(null)
    val previousRating: StateFlow<Int?> = _previousRating.asStateFlow()

    private var verifyJob: Job? = null

    fun setShowId(id: Int) {
        _showId.value = id
    }

    fun verifyEpisode(seasonNumber: Int, episodeNumber: Int) {
        verifyJob?.cancel()
        val showId = _showId.value ?: return
        if (seasonNumber <= 0 || episodeNumber <= 0) {
            _verificationState.value = null
            _alreadyLogged.value = false
            _previousRating.value = null
            return
        }
        verifyJob = viewModelScope.launch {
            delay(500)
            _verificationState.value = Result.Loading
            val result = getEpisodeDetailsUseCase(showId, seasonNumber, episodeNumber)
            _verificationState.value = result
            val existing = episodeRepository
                .observeEpisodeEntries(showId, seasonNumber, episodeNumber)
                .first()
            _alreadyLogged.value = existing.isNotEmpty()
            _previousRating.value = existing.firstOrNull()?.rating
        }
    }

    fun saveEpisode(
        showName: String,
        seasonNumber: Int,
        episodeNumber: Int,
        watchedDate: Instant,
        notes: String?,
        isRewatch: Boolean,
        rating: Int?
    ) {
        val showId = _showId.value ?: return

        viewModelScope.launch {
            _episodeDetails.value = Result.Loading

            try {
                val result = getEpisodeDetailsUseCase(showId, seasonNumber, episodeNumber)
                _episodeDetails.value = result

                if (result is Result.Success) {
                    val episode = result.data.copy(
                        id = 0,
                        showId = showId,
                        showName = showName,
                        watchedDate = watchedDate,
                        notes = notes,
                        isRewatch = isRewatch,
                        rating = rating
                    )
                    logEpisodeUseCase(episode)
                }
            } catch (e: Exception) {
                _episodeDetails.value = Result.Error(e)
            }
        }
    }
}
