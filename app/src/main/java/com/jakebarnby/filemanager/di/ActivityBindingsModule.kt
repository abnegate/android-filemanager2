package com.jakebarnby.filemanager.di

import com.jakebarnby.filemanager.sources.SourceModule
import com.jakebarnby.filemanager.ui.sources.SourceActivity
import dagger.Module
import dagger.android.AndroidInjectionModule
import dagger.android.ContributesAndroidInjector


@Module(
    includes = [
        AndroidInjectionModule::class
    ]
)
abstract class ActivityBindingsModule {

    @PerActivity
    @ContributesAndroidInjector(modules = [SourceModule::class])
    abstract fun sourceActivityInjector(): SourceActivity

}