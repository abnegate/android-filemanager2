package com.jakebarnby.filemanager.di

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import androidx.room.Room
import com.google.firebase.analytics.FirebaseAnalytics
import com.jakebarnby.filemanager.data.FileDatabase
import com.jakebarnby.filemanager.data.FileRepository
import com.jakebarnby.filemanager.managers.*
import com.jakebarnby.filemanager.util.Constants
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


@Module
abstract class AppModule {

    @Binds
    abstract fun bindContext(application: Application): Context

    @Binds
    @Singleton
    abstract fun selectedFilesManager(filesManager: SelectedFilesManager): SelectedFilesManager

    @Binds
    @Singleton
    abstract fun sourceManager(sourceManager: SourceManager) : SourceManager

    @Module
    companion object {

        @Provides
        @Singleton
        fun provideDatabase(context: Context): FileDatabase = Room.databaseBuilder(
            context,
            FileDatabase::class.java, "fileman-db"
        ).build()

        @Provides
        fun provideFileDao(fileDatabase: FileDatabase) =
            fileDatabase.fileDao()

        @Provides
        fun provideFileRepository(repository: FileRepository) = repository

        @Provides
        fun providePreferencesManager(context: Context) = PreferenceManager(
            context.getSharedPreferences(Constants.Prefs.PREFS, Context.MODE_PRIVATE)
        )

        @Provides
        @Singleton
        fun provideAnalytics(context: Context) =
            FirebaseAnalytics.getInstance(context)
    }
}