package com.app.binged.domain.usecase

import com.app.binged.core.utils.Result
import com.app.binged.domain.contract.ShowRepository
import com.app.binged.domain.model.Show

class GetShowDetailsUseCase(private val showRepository: ShowRepository) {
    suspend operator fun invoke(id: Int): Result<Show> =
        showRepository.getShowDetails(id)
}
