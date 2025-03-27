package com.app.binged.domain.usecase

import com.app.binged.domain.contract.ShowRepository
import com.app.binged.domain.model.Show

class TrackShowUseCase(private val showRepository: ShowRepository) {
    suspend operator fun invoke(show: Show) = showRepository.saveShow(show)
}
