package com.app.binged.feature.tracking.di

import com.app.binged.feature.tracking.viewmodel.LogEpisodeViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val trackingModule = module {
    viewModel { LogEpisodeViewModel(get()) }
}
