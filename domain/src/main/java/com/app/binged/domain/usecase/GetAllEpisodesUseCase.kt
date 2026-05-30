package com.app.binged.domain.usecase

import com.app.binged.domain.contract.EpisodeRepository
import com.app.binged.domain.model.Episode
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllEpisodesUseCase @Inject constructor(
    private val episodeRepository: EpisodeRepository
) {
    operator fun invoke(): Flow<List<Episode>> = episodeRepository.getAllEpisodes()
}
