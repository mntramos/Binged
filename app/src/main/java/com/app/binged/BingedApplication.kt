package com.app.binged

import android.app.Application
import com.app.binged.data.di.dataModule
import com.app.binged.domain.di.domainModule
import com.app.binged.feature.diary.di.diaryModule
import com.app.binged.feature.search.di.searchModule
import com.app.binged.feature.shows.di.showsModule
import com.app.binged.feature.tracking.di.trackingModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class BingedApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.INFO)
            androidContext(this@BingedApplication)
            modules(listOf(
                dataModule,
                domainModule,
                diaryModule,
                searchModule,
                showsModule,
                trackingModule
            ))
        }
    }
}
