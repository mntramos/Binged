package com.app.binged.domain.usecase

import com.app.binged.domain.contract.EpisodeRepository
import com.app.binged.domain.model.Episode

class DeleteEpisodeUseCase(private val episodeRepository: EpisodeRepository) {
    suspend operator fun invoke(episode: Episode) =
        episodeRepository.deleteEpisode(episode)
}
