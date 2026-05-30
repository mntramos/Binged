package com.app.binged.domain.usecase

import com.app.binged.core.utils.Result
import com.app.binged.domain.contract.ShowRepository
import com.app.binged.domain.model.Show
import javax.inject.Inject

class SearchShowsUseCase @Inject constructor(
    private val showRepository: ShowRepository
) {
    suspend operator fun invoke(query: String): Result<List<Show>> =
        showRepository.searchShows(query)
}
