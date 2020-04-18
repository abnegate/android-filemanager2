package com.jakebarnby.filemanager.di

import com.jakebarnby.filemanager.ui.splash.SplashActivity
import com.jakebarnby.filemanager.ui.tutorial.FileManagerTutorialActivity
import com.jakebarnby.filemanager.ui.sources.SourceActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBindingsModule {

    @PerActivity
    @ContributesAndroidInjector
    abstract fun splashActivityInjector(): SplashActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun tutorialActivityInjector(): FileManagerTutorialActivity

    @PerActivity
    @ContributesAndroidInjector(modules = [SourceModule::class])
    abstract fun sourceActivityInjector(): SourceActivity

}