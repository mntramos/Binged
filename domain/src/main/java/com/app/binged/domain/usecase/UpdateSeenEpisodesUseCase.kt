package com.app.binged.domain.usecase

import com.app.binged.domain.contract.ShowRepository
import javax.inject.Inject

class UpdateSeenEpisodesUseCase @Inject constructor(
    private val showRepository: ShowRepository
) {
    enum class Mode { SET, ADD, REMOVE }

    suspend operator fun invoke(showId: Int, seen: Set<String>, mode: Mode) {
        when (mode) {
            Mode.SET -> showRepository.setSeenEpisodes(showId, seen)
            Mode.ADD -> showRepository.addSeenEpisodes(showId, seen)
            Mode.REMOVE -> showRepository.removeSeenEpisodes(showId, seen)
        }
    }
}
