package com.jakebarnby.filemanager.sources.dropbox

import android.content.Context
import android.os.AsyncTask
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.android.Auth
import com.dropbox.core.v2.DbxClientV2
import com.google.firebase.analytics.FirebaseAnalytics
import com.jakebarnby.filemanager.sources.SourceListener
import com.jakebarnby.filemanager.sources.models.Source
import com.jakebarnby.filemanager.sources.models.SourceType
import com.jakebarnby.filemanager.util.Constants
import com.jakebarnby.filemanager.util.Constants.Prefs
import com.jakebarnby.filemanager.util.Constants.Sources
import com.jakebarnby.filemanager.util.LogUtils
import com.jakebarnby.filemanager.util.PreferenceUtils

/**
 * Created by jakebarnby on 2/08/17.
 */
class DropboxSource(
    sourceName: String,
    listener: SourceListener
) : Source(SourceType.REMOTE, sourceName, listener) {

    override fun authenticateSource(context: Context) {
        if (hasToken(context, sourceName)) {
            loadSource(context)
        } else {
            Auth.startOAuth2Authentication(context, Sources.DROPBOX_CLIENT_ID)
        }
    }

    override fun loadSource(context: Context) {
        if (!isFilesLoaded) {
            if (!checkConnectionActive(context)) return
            DropboxLoaderTask(this, sourceListener).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "")
        }
    }

    override fun logout(context: Context) {
        if (!isLoggedIn) {
            return
        }
        DropboxFactory.logout(context)
        isLoggedIn = false
        isFilesLoaded = false
        sourceListener.onLogout()
        LogUtils.logFirebaseEvent(
            FirebaseAnalytics.getInstance(context),
            Constants.Analytics.EVENT_LOGOUT_DROPBOX
        )

    }

    /**
     * Set up the dropbox client
     * @param accessToken The access token for this dropbox session
     */
    fun setupClient(accessToken: String) {
        if (isLoggedIn) {
            return
        }

        DropboxFactory.client = DbxClientV2(
            DbxRequestConfig
                .newBuilder("FileManagerAndroid/1.0")
                .build(),
            accessToken
        )
        isLoggedIn = true
    }

    /**
     * Check for a valid access token and store it in shared preferences if found, then load the source
     */
    fun checkForAccessToken(context: Context) {
        var accessToken = PreferenceUtils.getString(
            context,
            Prefs.DROPBOX_TOKEN_KEY,
            null
        )

        if (accessToken != null && !isLoggedIn) {
            setupClient(accessToken)
            loadSource(context)
        }

        if (accessToken == null) {
            accessToken = Auth.getOAuth2Token()
            if (accessToken != null) {
                PreferenceUtils.savePref(context, Prefs.DROPBOX_TOKEN_KEY, accessToken)
                setupClient(accessToken)
                loadSource(context)
                LogUtils.logFirebaseEvent(
                    FirebaseAnalytics.getInstance(context),
                    Constants.Analytics.EVENT_LOGIN_DROPBOX
                )
            }
        }
    }
}