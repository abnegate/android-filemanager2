package com.jakebarnby.filemanager.di

import android.content.Context
import com.jakebarnby.filemanager.R
import com.jakebarnby.filemanager.models.sources.local.LocalFragment
import com.jakebarnby.filemanager.ui.sources.*
import com.microsoft.identity.client.IMultipleAccountPublicClientApplication
import com.microsoft.identity.client.PublicClientApplication
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.runBlocking


@Module
abstract class SourceModule {

    @Binds
    abstract fun presenter(sourcePresenter: SourcePresenter)
        : SourceActivityContract.Presenter

    @Binds
    abstract fun fragmentPresenter(fragmentPresenter: SourceFragmentPresenter)
        : SourceFragmentContract.Presenter

    @Binds
    abstract fun fragmentListPresenter(fragmentListPresenter: SourceFragmentListPresenter)
        : SourceFragmentContract.ListPresenter

    @ContributesAndroidInjector
    @PerFragment
    abstract fun localFragment(): LocalFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun dropboxFragment(): DropboxFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun googleDriveFragment(): GoogleDriveFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun oneDriveFragment(): OneDriveFragment

    companion object {
        @Provides
        fun provideMultiAccountPublicClientApp(context: Context): IMultipleAccountPublicClientApplication {
            return runBlocking(IO) {
                PublicClientApplication.createMultipleAccountPublicClientApplication(
                    context,
                    R.raw.msal_config
                )
            }
        }
    }
}