package com.jakebarnby.filemanager.sources

import android.content.Context
import android.net.ConnectivityManager
import com.jakebarnby.filemanager.di.PerFragment
import com.jakebarnby.filemanager.managers.*
import com.jakebarnby.filemanager.ui.sources.SourceActivityContract
import com.jakebarnby.filemanager.ui.sources.SourceFragmentContract
import com.jakebarnby.filemanager.util.Constants
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import javax.inject.Singleton


@Module
abstract class SourceModule {

    @Binds
    abstract fun presenter(presenter: SourceActivityContract.Presenter): SourceActivityContract.Presenter

    @Binds
    @PerFragment
    abstract fun fragmentPresenter(presenter: SourceFragmentContract.Presenter): SourceFragmentContract.Presenter

    @Module
    companion object {
        @Provides
        fun provideBillingManager(
            context: Context,
            prefs: PreferenceManager
        ) = BillingManager(context, prefs)

        @Provides
        fun provideConnectionManager(context: Context) = ConnectionManager(
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        )
    }
}