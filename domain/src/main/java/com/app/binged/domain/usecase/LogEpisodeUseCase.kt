package com.app.binged.domain.usecase

import com.app.binged.domain.contract.EpisodeRepository
import com.app.binged.domain.model.Episode
import javax.inject.Inject

class LogEpisodeUseCase @Inject constructor(
    private val episodeRepository: EpisodeRepository
) {
    suspend operator fun invoke(episode: Episode): Long =
        episodeRepository.saveEpisode(episode)
}
