package com.jakebarnby.filemanager.di

import com.jakebarnby.filemanager.splash.SplashActivity
import com.jakebarnby.filemanager.tutorial.FileManagerTutorialActivity
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
    @ContributesAndroidInjector
    abstract fun splashActivityInjector(): SplashActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun tutorialActivityInjector(): FileManagerTutorialActivity

    @PerActivity
    @ContributesAndroidInjector(modules = [SourceModule::class])
    abstract fun sourceActivityInjector(): SourceActivity

}