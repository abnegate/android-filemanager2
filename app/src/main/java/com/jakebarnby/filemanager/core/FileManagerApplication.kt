package com.jakebarnby.filemanager.core

import com.google.android.gms.ads.MobileAds
import com.jakebarnby.filemanager.R
import com.jakebarnby.filemanager.di.DaggerAppComponent
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication

class FileManagerApplication : DaggerApplication() {

    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this, getString(R.string.admob_app_id))
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerAppComponent.builder().application(this).build()
    }
}