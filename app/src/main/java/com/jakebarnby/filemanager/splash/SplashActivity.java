package com.jakebarnby.filemanager.splash;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.MobileAds;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.jakebarnby.filemanager.BuildConfig;
import com.jakebarnby.filemanager.R;
import com.jakebarnby.filemanager.sources.SourceActivity;
import com.jakebarnby.filemanager.tutorial.FileManagerTutorialActivity;
import com.jakebarnby.filemanager.util.Constants;
import com.jakebarnby.filemanager.util.GooglePlayUtils;
import com.jakebarnby.filemanager.util.PreferenceUtils;

import static com.jakebarnby.filemanager.util.Constants.RequestCodes.GOOGLE_PLAY_SERVICES;

/**
 * Created by Jake on 9/26/2017.
 */

public class SplashActivity extends AppCompatActivity {

    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        if (!PreferenceUtils.getBoolean(this, Constants.Prefs.HIDE_ADS_KEY, false)) {
            MobileAds.initialize(this, Constants.Ads.ADMOB_ID);
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        setupRemoteConfig();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case GOOGLE_PLAY_SERVICES:
                if (resultCode == RESULT_OK) {
                    fetchRemoteConfig();
                } else {
                    goNextActivity();
                }
                break;
        }
    }

    /**
     * Get remote config instance and set defaults then start a fetch call to get server values,
     * when complete tutorial pages will be added.
     */
    private void setupRemoteConfig() {
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();

        mFirebaseRemoteConfig.setConfigSettings(configSettings);
        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);

        if (GooglePlayUtils.isGooglePlayServicesAvailable(this)) {
            fetchRemoteConfig();
        } else {
            GooglePlayUtils.acquireGooglePlayServices(this);
        }
    }

    private void fetchRemoteConfig() {
        mFirebaseRemoteConfig.fetch(Constants.RemoteConfig.RC_CACHE_EXPIRATION_SECONDS)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        mFirebaseRemoteConfig.activateFetched();
                        goNextActivity();
                    } else {
                        //TODO: Log analytics event remote_config_fetch_failed
                        goNextActivity();
                    }
                });
    }

    private void goNextActivity() {
        if (!PreferenceUtils.getBoolean(this, Constants.Prefs.TUT_SEEN_KEY, false)) {
            startActivity(new Intent(this, FileManagerTutorialActivity.class));
        } else {
            startActivity(new Intent(this, SourceActivity.class));
        }
    }
}
