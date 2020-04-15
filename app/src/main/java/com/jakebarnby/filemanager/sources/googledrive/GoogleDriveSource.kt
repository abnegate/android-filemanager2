package com.jakebarnby.filemanager.sources.googledrive

import android.Manifest
import android.content.Context
import androidx.fragment.app.Fragment
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.drive.DriveScopes
import com.google.firebase.analytics.FirebaseAnalytics
import com.jakebarnby.filemanager.models.Source
import com.jakebarnby.filemanager.models.SourceConnectionType
import com.jakebarnby.filemanager.models.SourceType
import com.jakebarnby.filemanager.ui.sources.SourceFragmentContract
import com.jakebarnby.filemanager.util.Constants
import com.jakebarnby.filemanager.util.Constants.Prefs
import com.jakebarnby.filemanager.util.Constants.RequestCodes
import com.jakebarnby.filemanager.util.GooglePlay
import com.jakebarnby.filemanager.util.Logger
import com.jakebarnby.filemanager.util.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import javax.inject.Inject

/**
 * Created by jakebarnby on 2/08/17.
 */
class GoogleDriveSource @Inject constructor(
    var presenter: SourceFragmentContract.Presenter
) : Source(
    SourceConnectionType.REMOTE,
    SourceType.GOOGLE_DRIVE.id,
    presenter.prefsManager
) {

    @Inject
    lateinit var googleDriveClient: GoogleDriveClient

    private var credential: GoogleAccountCredential? = null

    companion object {
        private val SCOPES = listOf(DriveScopes.DRIVE)
    }

    override fun authenticate(context: Context) {
        //if (!checkConnectionActive(context)) return
        fetchCredential(context)
        if (presenter.prefsManager.hasSourceToken(sourceId)) {
            loadFiles(context)
        } else {
            presenter.onCheckPermissions(
                Manifest.permission.GET_ACCOUNTS,
                RequestCodes.ACCOUNTS_PERMISSIONS
            )
        }
    }

    override fun loadFiles(context: Context) {
        if (!isFilesLoaded) {
//            if (!precheckConnectionActive(context)) return
            GoogleDriveLoaderTask(this, presenter, credential)
                .executeOnExecutor(Dispatchers.IO.asExecutor(), "root")
        }
    }

    override fun logout(context: Context) {
        if (isLoggedIn) {
            googleDriveClient.logout(context)

            isLoggedIn = false
            isFilesLoaded = false
            credential = null
            presenter.onLogout()

            Logger.logFirebaseEvent(
                FirebaseAnalytics.getInstance(context),
                Constants.Analytics.EVENT_LOGOUT_GOOGLEDRIVE
            )
        }
    }

    private fun fetchCredential(context: Context) {
        credential = GoogleAccountCredential
            .usingOAuth2(context, SCOPES)
            .setBackOff(ExponentialBackOff())
    }

    fun authGoogle(fragment: Fragment) {
        getResultsFromApi(fragment)
    }

    /**
     * Create a google credential and try to call the API with it, do nothing if it fails
     */
    fun authGoogleSilent(fragment: Fragment) {
        val accountName = presenter.prefsManager.getString(
            Prefs.GOOGLE_NAME_KEY,
            null
        )
        if (accountName != null) {
            fetchCredential(fragment.context!!)
            credential!!.selectedAccountName = accountName
            getResultsFromApi(fragment)
        }
    }

    /**
     * Attempt to call the API, after verifying that all the preconditions are satisfied
     */
    fun getResultsFromApi(fragment: Fragment) {
        if (isLoggedIn) {
            return
        }

        if (!GooglePlay.isGooglePlayServicesAvailable(fragment.context!!)) {
            GooglePlay.acquireGooglePlayServices(fragment.activity)
        } else if (credential != null && credential!!.selectedAccountName == null) {
            fragment.startActivityForResult(credential!!.newChooseAccountIntent(), RequestCodes.ACCOUNT_PICKER)
        } else if (!Utils.isConnectionReady(fragment.context!!)) {
            presenter.onNoConnection()
        } else {
            isLoggedIn = true
            loadFiles(fragment.context!!)
        }
    }

    fun saveUserToken(fragment: Fragment) {
        try {
            presenter.prefsManager.savePref(Prefs.GOOGLE_TOKEN_KEY, credential!!.token)
        } catch (e: Exception) {
            e.printStackTrace()
            //TODO: Log error
        }
        getResultsFromApi(fragment)
    }

    fun saveUserAccount(fragment: Fragment, accountName: String?) {
        presenter.prefsManager.savePref(Prefs.GOOGLE_NAME_KEY, accountName)
        credential!!.selectedAccountName = accountName
        getResultsFromApi(fragment)
    }
}