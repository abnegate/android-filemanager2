package com.jakebarnby.filemanager.di

import com.jakebarnby.filemanager.models.sources.SourceFragmentPresenter
import com.jakebarnby.filemanager.models.sources.SourcePresenter
import com.jakebarnby.filemanager.ui.sources.DropboxFragment
import com.jakebarnby.filemanager.ui.sources.GoogleDriveFragment
import com.jakebarnby.filemanager.models.sources.local.LocalFragment
import com.jakebarnby.filemanager.models.sources.onedrive.OneDriveFragment
import com.jakebarnby.filemanager.ui.sources.SourceActivityContract
import com.jakebarnby.filemanager.ui.sources.SourceFragmentContract
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector


@Module
abstract class SourceModule {

    @Binds
    abstract fun presenter(sourcePresenter: SourcePresenter): SourceActivityContract.Presenter

    @Binds
    abstract fun fragmentPresenter(fragmentPresenter: SourceFragmentPresenter): SourceFragmentContract.Presenter

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
}