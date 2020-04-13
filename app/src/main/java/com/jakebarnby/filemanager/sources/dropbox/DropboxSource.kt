package com.jakebarnby.filemanager.sources.dropbox

import android.content.Context
import android.os.AsyncTask
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.android.Auth
import com.dropbox.core.v2.DbxClientV2
import com.google.firebase.analytics.FirebaseAnalytics
import com.jakebarnby.filemanager.managers.PreferenceManager
import com.jakebarnby.filemanager.models.Source
import com.jakebarnby.filemanager.models.SourceConnectionType
import com.jakebarnby.filemanager.models.SourceType
import com.jakebarnby.filemanager.ui.sources.SourceFragmentContract
import com.jakebarnby.filemanager.util.Constants
import com.jakebarnby.filemanager.util.Constants.Prefs
import com.jakebarnby.filemanager.util.Constants.Sources
import com.jakebarnby.filemanager.util.Logger
import javax.inject.Inject

/**
 * Created by jakebarnby on 2/08/17.
 */
class DropboxSource(
    private val presenter: SourceFragmentContract.Presenter
) : Source(
    SourceConnectionType.REMOTE,
    SourceType.DROPBOX.id,
    presenter.prefsManager
) {

    @Inject
    lateinit var dropboxClient: DropboxClient

    override fun authenticate(context: Context) {
        if (prefsManager.hasSourceToken(sourceId)) {
            loadFiles(context)
        } else {
            Auth.startOAuth2Authentication(context, Sources.DROPBOX_CLIENT_ID)
        }
    }

    override fun loadFiles(context: Context) {
        if (!isFilesLoaded) {
            DropboxLoaderTask(this, presenter)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "")
        }
    }

    override fun logout(context: Context) {
        if (!isLoggedIn) {
            return
        }
        dropboxClient.logout(context)

        isLoggedIn = false
        isFilesLoaded = false

        presenter.onLogout()

        Logger.logFirebaseEvent(
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

        DropboxClient.client = DbxClientV2(
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
        var accessToken = prefsManager.getString(
            Prefs.DROPBOX_TOKEN_KEY,
            null
        )

        if (accessToken != null && !isLoggedIn) {
            setupClient(accessToken)
            loadFiles(context)
        }

        if (accessToken == null) {
            accessToken = Auth.getOAuth2Token()
            if (accessToken != null) {
                prefsManager.savePref(Prefs.DROPBOX_TOKEN_KEY, accessToken)
                setupClient(accessToken)
                loadFiles(context)

                Logger.logFirebaseEvent(
                    FirebaseAnalytics.getInstance(context),
                    Constants.Analytics.EVENT_LOGIN_DROPBOX
                )
            }
        }
    }
}