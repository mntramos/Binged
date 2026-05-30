package com.app.binged.domain.usecase

import com.app.binged.domain.contract.ShowRepository
import com.app.binged.domain.model.Show
import javax.inject.Inject

class UpdateWatchingStatusUseCase @Inject constructor(
    private val showRepository: ShowRepository
) {
    suspend operator fun invoke(show: Show, isWatching: Boolean): Int =
        showRepository.updateWatchingStatus(show, isWatching)
}
