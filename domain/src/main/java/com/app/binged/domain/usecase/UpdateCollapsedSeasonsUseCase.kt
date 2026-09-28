package com.app.binged.domain.usecase

import com.app.binged.domain.contract.ShowRepository
import javax.inject.Inject

class UpdateCollapsedSeasonsUseCase @Inject constructor(
    private val showRepository: ShowRepository
) {
    suspend operator fun invoke(showId: Int, collapsed: Set<Int>) {
        showRepository.setCollapsedSeasons(showId, collapsed)
    }
}
