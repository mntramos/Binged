package com.app.binged.domain.usecase

import com.app.binged.domain.contract.ShowRepository
import com.app.binged.domain.model.Show
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTrackedShowsUseCase @Inject constructor(
    private val showRepository: ShowRepository
) {
    operator fun invoke(): Flow<List<Show>> = showRepository.getTrackedShows()
}
