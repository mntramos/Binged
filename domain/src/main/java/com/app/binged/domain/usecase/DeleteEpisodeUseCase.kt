package com.app.binged.domain.usecase

import com.app.binged.domain.contract.EpisodeRepository
import com.app.binged.domain.model.Episode
import javax.inject.Inject

class DeleteEpisodeUseCase @Inject constructor(
    private val episodeRepository: EpisodeRepository
) {
    suspend operator fun invoke(episode: Episode) =
        episodeRepository.deleteEpisode(episode)
}
