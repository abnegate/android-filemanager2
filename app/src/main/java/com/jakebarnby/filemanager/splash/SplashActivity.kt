package com.jakebarnby.filemanager.splash

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.MobileAds
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.jakebarnby.filemanager.R
import com.jakebarnby.filemanager.tutorial.FileManagerTutorialActivity
import com.jakebarnby.filemanager.ui.sources.SourceActivity
import com.jakebarnby.filemanager.util.Constants
import com.jakebarnby.filemanager.util.GooglePlay

/**
 * Created by Jake on 9/26/2017.
 */
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        if (!Preferences.getBoolean(this, Constants.Prefs.HIDE_ADS_KEY, false)) {
            MobileAds.initialize(this, Constants.Ads.ADMOB_ID)
        }

        setupRemoteConfig()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Constants.RequestCodes.GOOGLE_PLAY_SERVICES -> if (resultCode == Activity.RESULT_OK) {
                fetchRemoteConfig()
            } else {
                goToSources()
            }
        }
    }

    /**
     * Get remote config instance and set defaults then start a fetch call to get server values,
     * when complete tutorial pages will be added.
     */
    private fun setupRemoteConfig() {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(30)
            .build()

        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

        if (GooglePlay.isGooglePlayServicesAvailable(this)) {
            fetchRemoteConfig()
        } else {
            GooglePlay.acquireGooglePlayServices(this)
        }
    }

    private fun fetchRemoteConfig() {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        remoteConfig
            .fetchAndActivate()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    goToSources()
                } else {
                    //TODO: Log analytics event remote_config_fetch_failed
                    goToSources()
                }
            }
    }

    private fun goToSources() {
        if (!Preferences.getBoolean(this, Constants.Prefs.TUT_SEEN_KEY, false)) {
            startActivity(Intent(this, FileManagerTutorialActivity::class.java))
        } else {
            startActivity(Intent(this, SourceActivity::class.java))
        }
    }
}