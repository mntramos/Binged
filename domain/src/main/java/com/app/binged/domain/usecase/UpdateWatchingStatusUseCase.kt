package com.app.binged.domain.usecase

import com.app.binged.domain.contract.ShowRepository
import com.app.binged.domain.model.Show

class UpdateWatchingStatusUseCase(private val showRepository: ShowRepository) {
    suspend operator fun invoke(show: Show, isWatching: Boolean): Int = showRepository.updateWatchingStatus(show, isWatching)
}
