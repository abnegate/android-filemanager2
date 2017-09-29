package com.jakebarnby.filemanager.tutorial;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.jakebarnby.filemanager.sources.SourceActivity;
import com.jakebarnby.filemanager.util.Constants;
import com.jakebarnby.tutorial.TutorialActivity;
import com.jakebarnby.tutorial.TutorialPage;

/**
 * Created by Jake on 9/26/2017.
 */

public class FileManagerTutorialActivity extends TutorialActivity {

    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        addPages();
    }

    /**
     * Add all child tutorial pages to the pager adapter.
     */
    private void addPages() {
        addPageWithContent(
                mFirebaseRemoteConfig.getString(Constants.RemoteConfig.TUT_PAGE1_TITLE_KEY),
                mFirebaseRemoteConfig.getString(Constants.RemoteConfig.TUT_PAGE1_CONTENT_KEY),
                mFirebaseRemoteConfig.getString(Constants.RemoteConfig.TUT_PAGE1_SUMMARY_KEY),
                mFirebaseRemoteConfig.getString(Constants.RemoteConfig.TUT_PAGE1_IMAGE_KEY),
                mFirebaseRemoteConfig.getString(Constants.RemoteConfig.TUT_PAGE1_BGCOLOR_KEY));

        addPageWithContent(
                mFirebaseRemoteConfig.getString(Constants.RemoteConfig.TUT_PAGE2_TITLE_KEY),
                mFirebaseRemoteConfig.getString(Constants.RemoteConfig.TUT_PAGE2_CONTENT_KEY),
                mFirebaseRemoteConfig.getString(Constants.RemoteConfig.TUT_PAGE2_SUMMARY_KEY),
                mFirebaseRemoteConfig.getString(Constants.RemoteConfig.TUT_PAGE2_IMAGE_KEY),
                mFirebaseRemoteConfig.getString(Constants.RemoteConfig.TUT_PAGE2_BGCOLOR_KEY));

        addPageWithContent(
                mFirebaseRemoteConfig.getString(Constants.RemoteConfig.TUT_PAGE3_TITLE_KEY),
                mFirebaseRemoteConfig.getString(Constants.RemoteConfig.TUT_PAGE3_CONTENT_KEY),
                mFirebaseRemoteConfig.getString(Constants.RemoteConfig.TUT_PAGE3_SUMMARY_KEY),
                mFirebaseRemoteConfig.getString(Constants.RemoteConfig.TUT_PAGE3_IMAGE_KEY),
                mFirebaseRemoteConfig.getString(Constants.RemoteConfig.TUT_PAGE3_BGCOLOR_KEY));
    }

    /**
     * Add a new <code>TutorialPageFragment</code> to the adapter
     * @param title     Title of the page
     * @param content   Content text of the page
     * @param summary   Summary text of the page
     * @param image     Image url for the page
     * @param bgColor   Background color of the page
     */
    private void addPageWithContent(String title,
                                    String content,
                                    String summary,
                                    String image,
                                    String bgColor) {
        addFragment(new TutorialPage.Builder()
                .setTitle(title)
                .setContent(content)
                .setSummary(summary)
                .setImageUrl(image)
                .setBackgroundColor(Color.parseColor(bgColor))
                .build());
    }

    @Override
    public void finish() {
        super.finish();
        saveFinished();
        startActivity(new Intent(this, SourceActivity.class));
    }

    private void saveFinished() {
        getSharedPreferences(Constants.Prefs.PREFS, MODE_PRIVATE)
                .edit()
                .putBoolean(Constants.Prefs.TUT_SEEN_KEY, true)
                .apply();
    }
}
