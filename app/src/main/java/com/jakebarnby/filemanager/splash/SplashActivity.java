package com.jakebarnby.filemanager.splash;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.jakebarnby.filemanager.BuildConfig;
import com.jakebarnby.filemanager.R;
import com.jakebarnby.filemanager.sources.SourceActivity;
import com.jakebarnby.filemanager.tutorial.FileManagerTutorialActivity;
import com.jakebarnby.filemanager.util.Constants;

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
        setupRemoteConfig();
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

        mFirebaseRemoteConfig.fetch(Constants.ConfigKeys.RC_CACHE_EXPIRATION_SECONDS)
                .addOnCompleteListener(this, task -> {
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
        if (!getSharedPreferences(Constants.Prefs.PREFS, MODE_PRIVATE)
                .getBoolean(Constants.Prefs.TUT_SEEN_KEY, false)) {
            startActivity(new Intent(this, FileManagerTutorialActivity.class));
        } else {
            startActivity(new Intent(this, SourceActivity.class));
        }
    }
}
