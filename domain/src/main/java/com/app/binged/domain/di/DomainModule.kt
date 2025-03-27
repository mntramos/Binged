package com.app.binged.domain.di

import com.app.binged.domain.usecase.DeleteEpisodeUseCase
import com.app.binged.domain.usecase.GetAllEpisodesUseCase
import com.app.binged.domain.usecase.GetEpisodeDetailsUseCase
import com.app.binged.domain.usecase.GetEpisodesForShowUseCase
import com.app.binged.domain.usecase.GetShowDetailsUseCase
import com.app.binged.domain.usecase.GetTrackedShowsUseCase
import com.app.binged.domain.usecase.LogEpisodeUseCase
import com.app.binged.domain.usecase.SearchShowsUseCase
import com.app.binged.domain.usecase.TrackShowUseCase
import com.app.binged.domain.usecase.UntrackShowUseCase
import org.koin.dsl.module

val domainModule = module {
    // Show use cases
    factory { GetTrackedShowsUseCase(get()) }
    factory { SearchShowsUseCase(get()) }
    factory { GetShowDetailsUseCase(get()) }
    factory { TrackShowUseCase(get()) }
    factory { UntrackShowUseCase(get()) }

    // Episode use cases
    factory { GetAllEpisodesUseCase(get()) }
    factory { GetEpisodeDetailsUseCase(get()) }
    factory { GetEpisodesForShowUseCase(get()) }
    factory { LogEpisodeUseCase(get()) }
    factory { DeleteEpisodeUseCase(get()) }
}
