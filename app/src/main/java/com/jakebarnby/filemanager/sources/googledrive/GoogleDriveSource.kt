package com.jakebarnby.filemanager.sources.googledrive

import android.Manifest
import android.content.Context
import android.os.AsyncTask
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.GoogleAuthException
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.drive.DriveScopes
import com.google.firebase.analytics.FirebaseAnalytics
import com.jakebarnby.filemanager.sources.SourceListener
import com.jakebarnby.filemanager.sources.models.Source
import com.jakebarnby.filemanager.sources.models.SourceType
import com.jakebarnby.filemanager.util.*
import com.jakebarnby.filemanager.util.Constants.Prefs
import com.jakebarnby.filemanager.util.Constants.RequestCodes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import java.io.IOException
import java.lang.Exception
import java.util.*

/**
 * Created by jakebarnby on 2/08/17.
 */
class GoogleDriveSource(
    sourceName: String,
    listener: SourceListener
) : Source(SourceType.REMOTE, sourceName, listener) {

    private var credendtial: GoogleAccountCredential? = null

    companion object {
        private val SCOPES = listOf(DriveScopes.DRIVE)
    }

    override fun authenticateSource(context: Context) {
        if (!checkConnectionActive(context)) return
        fetchCredential(context)
        if (hasToken(context, sourceName)) {
            loadSource(context)
        } else {
            sourceListener.onCheckPermissions(
                Manifest.permission.GET_ACCOUNTS,
                RequestCodes.ACCOUNTS_PERMISSIONS
            )
        }
    }

    override fun loadSource(context: Context) {
        if (!isFilesLoaded) {
            if (!checkConnectionActive(context)) return
            GoogleDriveLoaderTask(this, sourceListener, credendtial)
                .executeOnExecutor(Dispatchers.IO.asExecutor(), "root")
        }
    }

    override fun logout(context: Context) {
        if (isLoggedIn) {
            GoogleDriveFactory.logout(context)

            isLoggedIn = false
            isFilesLoaded = false
            credendtial = null
            sourceListener.onLogout()

            LogUtils.logFirebaseEvent(
                FirebaseAnalytics.getInstance(context),
                Constants.Analytics.EVENT_LOGOUT_GOOGLEDRIVE
            )
        }
    }

    private fun fetchCredential(context: Context) {
        credendtial = GoogleAccountCredential
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
        val accountName = PreferenceUtils.getString(
            fragment.context!!,
            Prefs.GOOGLE_NAME_KEY,
            null)
        if (accountName != null) {
            fetchCredential(fragment.context!!)
            credendtial!!.selectedAccountName = accountName
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

        if (!GooglePlayUtils.isGooglePlayServicesAvailable(fragment.context!!)) {
            GooglePlayUtils.acquireGooglePlayServices(fragment.activity)
        } else if (credendtial != null && credendtial!!.selectedAccountName == null) {
            fragment.startActivityForResult(credendtial!!.newChooseAccountIntent(), RequestCodes.ACCOUNT_PICKER)
        } else if (!Utils.isConnectionReady(fragment.context!!)) {
            sourceListener.onNoConnection()
        } else {
            isLoggedIn = true
            loadSource(fragment.context!!)
        }
    }

    fun saveUserToken(fragment: Fragment) {
        try {
            PreferenceUtils.savePref(fragment.context!!, Prefs.GOOGLE_TOKEN_KEY, credendtial!!.token)
        } catch (e: Exception) {
            e.printStackTrace()
            //TODO: Log error
        }
        getResultsFromApi(fragment)
    }

    fun saveUserAccount(fragment: Fragment, accountName: String?) {
        PreferenceUtils.savePref(fragment.context!!, Prefs.GOOGLE_NAME_KEY, accountName)
        credendtial!!.selectedAccountName = accountName
        getResultsFromApi(fragment)
    }
}