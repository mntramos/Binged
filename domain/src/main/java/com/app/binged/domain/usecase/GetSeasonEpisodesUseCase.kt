package com.app.binged.domain.usecase

import com.app.binged.core.utils.Result
import com.app.binged.domain.contract.ShowRepository
import com.app.binged.domain.model.Episode
import javax.inject.Inject

class GetSeasonEpisodesUseCase @Inject constructor(
    private val showRepository: ShowRepository
) {
    suspend operator fun invoke(showId: Int, season: Int): Result<List<Episode>> =
        showRepository.getSeasonEpisodes(showId, season)
}
