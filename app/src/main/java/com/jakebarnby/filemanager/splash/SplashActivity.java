package com.jakebarnby.filemanager.splash;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.jakebarnby.filemanager.R;
import com.jakebarnby.filemanager.sources.SourceActivity;
import com.jakebarnby.filemanager.tutorial.FileManagerTutorialActivity;
import com.jakebarnby.filemanager.util.Constants;

/**
 * Created by Jake on 9/26/2017.
 */

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (!getSharedPreferences(Constants.Prefs.PREFS, MODE_PRIVATE)
                .getBoolean(Constants.Prefs.TUT_SEEN_KEY, false)) {
            startActivity(new Intent(this, FileManagerTutorialActivity.class));
        } else {
            startActivity(new Intent(this, SourceActivity.class));
        }
    }
}
