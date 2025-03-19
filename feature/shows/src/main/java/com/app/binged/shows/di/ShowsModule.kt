package com.app.binged.shows.di

import com.app.binged.shows.viewmodel.ShowDetailViewModel
import com.app.binged.shows.viewmodel.ShowsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val showsModule = module {
    viewModel { ShowsViewModel(get()) }
    viewModel { ShowDetailViewModel(get(), get(), get(), get(), get(), get()) }
}
