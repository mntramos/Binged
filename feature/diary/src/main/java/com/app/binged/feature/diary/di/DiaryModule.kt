package com.app.binged.feature.diary.di

import com.app.binged.feature.diary.viewmodel.DiaryViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val diaryModule = module {
    viewModel { DiaryViewModel(get()) }
}
