package com.app.binged.domain.usecase

import com.app.binged.domain.contract.EpisodeRepository
import com.app.binged.domain.model.Episode
import kotlinx.coroutines.flow.Flow

class GetAllEpisodesUseCase(private val episodeRepository: EpisodeRepository) {
    operator fun invoke(): Flow<List<Episode>> =
        episodeRepository.getAllEpisodes()
}
