package com.app.binged.data.di

import androidx.room.Room
import com.app.binged.data.BuildConfig
import com.app.binged.data.api.TmdbService
import com.app.binged.data.database.AppDatabase
import com.app.binged.data.repository.EpisodeRepositoryImpl
import com.app.binged.data.repository.ShowRepositoryImpl
import com.app.binged.domain.contract.EpisodeRepository
import com.app.binged.domain.contract.ShowRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val dataModule = module {
    // Database
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "binged-db"
        ).build()
    }

    // DAOs
    single { get<AppDatabase>().showDao() }
    single { get<AppDatabase>().episodeDao() }

    // API Client
    single {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer ${BuildConfig.TMDB_KEY}")
                    .build()

                chain.proceed(request)
            }
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // API Services
    single { get<Retrofit>().create(TmdbService::class.java) }

    // Repositories
    single<ShowRepository> { ShowRepositoryImpl(get(), get()) }
    single<EpisodeRepository> { EpisodeRepositoryImpl(get()) }
}
