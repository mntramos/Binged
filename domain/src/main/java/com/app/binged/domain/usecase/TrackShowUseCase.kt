package com.app.binged.domain.usecase

import com.app.binged.domain.contract.ShowRepository
import com.app.binged.domain.model.Show
import javax.inject.Inject

class TrackShowUseCase @Inject constructor(
    private val showRepository: ShowRepository
) {
    suspend operator fun invoke(show: Show) = showRepository.saveShow(show)
}
