package com.app.binged.domain.usecase

import com.app.binged.domain.contract.ShowRepository
import com.app.binged.domain.model.Show
import kotlinx.coroutines.flow.Flow

class GetTrackedShowsUseCase(private val showRepository: ShowRepository) {
    operator fun invoke(): Flow<List<Show>> = showRepository.getTrackedShows()
}
