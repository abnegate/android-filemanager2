package com.jakebarnby.filemanager.di

import com.jakebarnby.filemanager.services.SourceTransferService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ServiceBindingsModule {
    @ContributesAndroidInjector
    abstract fun transferServiceInjector(): SourceTransferService
}