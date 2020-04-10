package com.jakebarnby.filemanager.splash

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.tasks.Task
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.jakebarnby.filemanager.BuildConfig
import com.jakebarnby.filemanager.R
import com.jakebarnby.filemanager.sources.SourceActivity
import com.jakebarnby.filemanager.tutorial.FileManagerTutorialActivity
import com.jakebarnby.filemanager.util.Constants
import com.jakebarnby.filemanager.util.GooglePlayUtils
import com.jakebarnby.filemanager.util.PreferenceUtils

/**
 * Created by Jake on 9/26/2017.
 */
class SplashActivity : AppCompatActivity() {
    private var mFirebaseRemoteConfig: FirebaseRemoteConfig? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        if (!PreferenceUtils.getBoolean(this, Constants.Prefs.HIDE_ADS_KEY, false)) {
            MobileAds.initialize(this, Constants.Ads.ADMOB_ID)
        }
    }

    override fun onPostResume() {
        super.onPostResume()
        setupRemoteConfig()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Constants.RequestCodes.GOOGLE_PLAY_SERVICES -> if (resultCode == Activity.RESULT_OK) {
                fetchRemoteConfig()
            } else {
                goNextActivity()
            }
        }
    }

    /**
     * Get remote config instance and set defaults then start a fetch call to get server values,
     * when complete tutorial pages will be added.
     */
    private fun setupRemoteConfig() {
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build()
        mFirebaseRemoteConfig!!.setConfigSettings(configSettings)
        mFirebaseRemoteConfig!!.setDefaults(R.xml.remote_config_defaults)
        if (GooglePlayUtils.isGooglePlayServicesAvailable(this)) {
            fetchRemoteConfig()
        } else {
            GooglePlayUtils.acquireGooglePlayServices(this)
        }
    }

    private fun fetchRemoteConfig() {
        mFirebaseRemoteConfig!!.fetch(Constants.RemoteConfig.RC_CACHE_EXPIRATION_SECONDS.toLong())
                .addOnCompleteListener { task: Task<Void?> ->
                    if (task.isSuccessful) {
                        mFirebaseRemoteConfig!!.activateFetched()
                        goNextActivity()
                    } else {
                        //TODO: Log analytics event remote_config_fetch_failed
                        goNextActivity()
                    }
                }
    }

    private fun goNextActivity() {
        if (!PreferenceUtils.getBoolean(this, Constants.Prefs.TUT_SEEN_KEY, false)) {
            startActivity(Intent(this, FileManagerTutorialActivity::class.java))
        } else {
            startActivity(Intent(this, SourceActivity::class.java))
        }
    }
}