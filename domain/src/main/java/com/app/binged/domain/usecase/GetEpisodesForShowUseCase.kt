package com.app.binged.domain.usecase

import com.app.binged.domain.contract.EpisodeRepository
import com.app.binged.domain.model.Episode
import kotlinx.coroutines.flow.Flow

class GetEpisodesForShowUseCase(private val episodeRepository: EpisodeRepository) {
    operator fun invoke(showId: Int): Flow<List<Episode>> =
        episodeRepository.getEpisodesByShow(showId)
}
