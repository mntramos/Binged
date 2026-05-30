package com.app.binged.data.di

import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.room.Room
import com.app.binged.data.BuildConfig
import com.app.binged.data.api.TmdbService
import com.app.binged.data.database.AppDatabase
import com.app.binged.data.database.MIGRATION_1_2
import com.app.binged.data.database.MIGRATION_2_3
import com.app.binged.data.database.dao.EpisodeDao
import com.app.binged.data.database.dao.ShowDao
import com.app.binged.data.repository.EpisodeRepositoryImpl
import com.app.binged.data.repository.ShowRepositoryImpl
import com.app.binged.domain.contract.EpisodeRepository
import com.app.binged.domain.contract.ShowRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "binged-db"
        ).addMigrations(
            MIGRATION_1_2,
            MIGRATION_2_3
        ).build()
    }

    @Provides
    fun provideShowDao(database: AppDatabase): ShowDao = database.showDao()

    @Provides
    fun provideEpisodeDao(database: AppDatabase): EpisodeDao = database.episodeDao()

    @Provides
    @Singleton
    fun provideOkHttpClient(@ApplicationContext context: Context): OkHttpClient {
        val isDebug = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (isDebug) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
        }
        return OkHttpClient.Builder()
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
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideTmdbService(retrofit: Retrofit): TmdbService {
        return retrofit.create(TmdbService::class.java)
    }

    @Provides
    @Singleton
    fun provideShowRepository(
        showDao: ShowDao,
        episodeDao: EpisodeDao,
        tmdbService: TmdbService
    ): ShowRepository = ShowRepositoryImpl(showDao, episodeDao, tmdbService)

    @Provides
    @Singleton
    fun provideEpisodeRepository(
        episodeDao: EpisodeDao,
        tmdbService: TmdbService
    ): EpisodeRepository = EpisodeRepositoryImpl(episodeDao, tmdbService)
}
