package com.jakebarnby.filemanager.tutorial

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.jakebarnby.filemanager.ui.sources.SourceActivity
import com.jakebarnby.filemanager.util.Constants.Prefs
import com.jakebarnby.filemanager.util.Constants.RemoteConfig
import com.jakebarnby.tutorial.TutorialActivity
import com.jakebarnby.tutorial.TutorialPage
import dagger.android.AndroidInjection

/**
 * Created by Jake on 9/26/2017.
 */
class FileManagerTutorialActivity : TutorialActivity() {

    lateinit var remoteConfig: FirebaseRemoteConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)
        remoteConfig = FirebaseRemoteConfig.getInstance()
        addPages()
    }

    /**
     * Add all child tutorial pages to the pager adapter.
     */
    private fun addPages() {
        addPageWithContent(
            remoteConfig.getString(RemoteConfig.TUT_PAGE1_TITLE_KEY),
            remoteConfig.getString(RemoteConfig.TUT_PAGE1_CONTENT_KEY),
            remoteConfig.getString(RemoteConfig.TUT_PAGE1_SUMMARY_KEY),
            remoteConfig.getString(RemoteConfig.TUT_PAGE1_IMAGE_KEY),
            remoteConfig.getString(RemoteConfig.TUT_PAGE1_BGCOLOR_KEY))
        addPageWithContent(
            remoteConfig.getString(RemoteConfig.TUT_PAGE2_TITLE_KEY),
            remoteConfig.getString(RemoteConfig.TUT_PAGE2_CONTENT_KEY),
            remoteConfig.getString(RemoteConfig.TUT_PAGE2_SUMMARY_KEY),
            remoteConfig.getString(RemoteConfig.TUT_PAGE2_IMAGE_KEY),
            remoteConfig.getString(RemoteConfig.TUT_PAGE2_BGCOLOR_KEY))
        addPageWithContent(
            remoteConfig.getString(RemoteConfig.TUT_PAGE3_TITLE_KEY),
            remoteConfig.getString(RemoteConfig.TUT_PAGE3_CONTENT_KEY),
            remoteConfig.getString(RemoteConfig.TUT_PAGE3_SUMMARY_KEY),
            remoteConfig.getString(RemoteConfig.TUT_PAGE3_IMAGE_KEY),
            remoteConfig.getString(RemoteConfig.TUT_PAGE3_BGCOLOR_KEY))
    }

    /**
     * Add a new `TutorialPageFragment` to the adapter
     * @param title     Title of the page
     * @param content   Content text of the page
     * @param summary   Summary text of the page
     * @param image     Image url for the page
     * @param bgColor   Background color of the page
     */
    private fun addPageWithContent(title: String,
                                   content: String,
                                   summary: String,
                                   image: String,
                                   bgColor: String) {
        addFragment(TutorialPage.Builder()
            .setTitle(title)
            .setContent(content)
            .setSummary(summary)
            .setImageUrl(image)
            .setBackgroundColor(Color.parseColor(bgColor))
            .build())
    }

    override fun finish() {
        super.finish()
        saveFinished()
        startActivity(Intent(this, SourceActivity::class.java))
    }

    private fun saveFinished() {
        getSharedPreferences(Prefs.PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean(Prefs.TUT_SEEN_KEY, true)
            .apply()
    }
}