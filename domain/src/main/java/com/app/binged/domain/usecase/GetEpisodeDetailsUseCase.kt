package com.app.binged.domain.usecase

import com.app.binged.core.utils.Result
import com.app.binged.domain.contract.EpisodeRepository
import com.app.binged.domain.model.Episode
import javax.inject.Inject

class GetEpisodeDetailsUseCase @Inject constructor(
    private val episodeRepository: EpisodeRepository
) {
    suspend operator fun invoke(id: Int, season: Int, episode: Int): Result<Episode> =
        episodeRepository.getEpisodeDetails(id, season, episode)
}
