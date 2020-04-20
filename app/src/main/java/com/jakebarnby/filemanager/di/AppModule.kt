package com.jakebarnby.filemanager.di

import android.app.Application
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.net.ConnectivityManager
import androidx.room.Room
import com.google.firebase.analytics.FirebaseAnalytics
import com.jakebarnby.filemanager.data.FileDao
import com.jakebarnby.filemanager.data.FileDatabase
import com.jakebarnby.filemanager.managers.ConnectionManager
import com.jakebarnby.filemanager.util.Constants
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
abstract class AppModule {

    @Binds
    abstract fun bindContext(application: Application): Context

    companion object {

        @Provides
        @Singleton
        fun provideDatabase(context: Context): FileDatabase = Room.databaseBuilder(
            context,
            FileDatabase::class.java, "fileman-db"
        ).build()

        @Provides
        @Singleton
        fun fileDao(db: FileDatabase): FileDao = db.fileDao()

        @Provides
        @Singleton
        fun provideSharedPreferences(context: Context): SharedPreferences =
            context.getSharedPreferences(Constants.Prefs.PREFS, MODE_PRIVATE)

        @Provides
        @Singleton
        fun provideAnalytics(context: Context) = FirebaseAnalytics.getInstance(context)

        @Provides
        @Singleton
        fun provideConnectionManager(context: Context) = ConnectionManager(
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        )
    }
}