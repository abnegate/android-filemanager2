package com.jakebarnby.filemanager.sources.onedrive

import android.content.Context
import android.os.AsyncTask
import androidx.fragment.app.Fragment
import com.google.firebase.analytics.FirebaseAnalytics
import com.jakebarnby.filemanager.sources.SourceListener
import com.jakebarnby.filemanager.sources.models.Source
import com.jakebarnby.filemanager.sources.models.SourceType
import com.jakebarnby.filemanager.util.Constants
import com.jakebarnby.filemanager.util.Constants.Prefs
import com.jakebarnby.filemanager.util.LogUtils
import com.jakebarnby.filemanager.util.PreferenceUtils
import com.microsoft.graph.concurrency.ICallback
import com.microsoft.graph.core.ClientException
import com.microsoft.graph.core.DefaultClientConfig
import com.microsoft.graph.extensions.DriveItem
import com.microsoft.graph.extensions.GraphServiceClient
import com.microsoft.identity.client.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import java.lang.Exception

/**
 * Created by jakebarnby on 2/08/17.
 */
class OneDriveSource(
    sourceName: String,
    listener: SourceListener
) : Source(SourceType.REMOTE, sourceName, listener) {

    var client: PublicClientApplication? = null

    private var authResult: AuthenticationResult? = null

    override fun authenticateSource(context: Context) {}

    fun authenticateSource(fragment: Fragment) {
        client = PublicClientApplication(fragment.context!!, CLIENT_ID)
        sourceListener.onLoadStarted()
        if (isLoggedIn) {
            return
        }
        if (!checkConnectionActive(fragment.context!!)) {
            return
        }

        try {
            val accessToken = PreferenceUtils.getString(fragment.context!!, Prefs.ONEDRIVE_TOKEN_KEY, null)
            val userId = PreferenceUtils.getString(fragment.context!!, Prefs.ONEDRIVE_NAME_KEY, null)
            if (accessToken != null && userId != null) {
                client!!.acquireTokenSilentAsync(SCOPES, client!!.getUser(userId), getAuthSilentCallback(fragment))
            } else {
                client!!.acquireToken(fragment, SCOPES, getAuthInteractiveCallback(fragment))
            }
        } catch (e: Exception) {
            sourceListener.onLoadError(e.message)
        }
    }

    override fun loadSource(context: Context) {
        if (isFilesLoaded || authResult == null) {
            return
        }

        if (!checkConnectionActive(context)) {
            return
        }

        val clientConfig = DefaultClientConfig.createWithAuthenticationProvider {
                it.addHeader("Authorization", String.format("Bearer %s", authResult!!.accessToken))
            }

        OneDriveFactory.service = GraphServiceClient.Builder()
                .fromConfig(clientConfig)
                .buildClient()

        OneDriveFactory.service
            ?.me
            ?.drive
            ?.root
            ?.buildRequest()
            ?.get(object : ICallback<DriveItem> {

                override fun success(driveItem: DriveItem) {
                    OneDriveLoaderTask(this@OneDriveSource, sourceListener, driveItem)
                        .executeOnExecutor(Dispatchers.IO.asExecutor(), "")
                }

                override fun failure(ex: ClientException) {
                    sourceListener.onLoadAborted()
                }
            })
    }

    override fun logout(context: Context) {
        if (isLoggedIn) {
            OneDriveFactory.logout(context)
            isLoggedIn = false
            isFilesLoaded = false
            authResult = null
            sourceListener.onLogout()
            LogUtils.logFirebaseEvent(
                FirebaseAnalytics.getInstance(context),
                Constants.Analytics.EVENT_LOGOUT_ONEDRIVE
            )
        }
    }

    /**
     * Check for a valid access token and store it in shared preferences if found, then load the source
     */
    fun checkForAccessToken(fragment: Fragment) {
        var accessToken = PreferenceUtils.getString(fragment.context!!, Prefs.ONEDRIVE_TOKEN_KEY, null)
        if (authResult != null) {
            if (accessToken == null || authResult!!.accessToken != accessToken) {
                accessToken = authResult!!.accessToken
                val userId = authResult!!.user.userIdentifier
                PreferenceUtils.savePref(fragment.context!!, Prefs.ONEDRIVE_TOKEN_KEY, accessToken)
                PreferenceUtils.savePref(fragment.context!!, Prefs.ONEDRIVE_NAME_KEY, userId)
            } else {
                if (!isLoggedIn) {
                    authenticateSource(fragment)
                } else {
                    loadSource(fragment.context!!)
                }
            }
        }
        if (accessToken != null) {
            if (!isLoggedIn) {
                authenticateSource(fragment)
            } else {
                loadSource(fragment.context!!)
                LogUtils.logFirebaseEvent(
                    FirebaseAnalytics.getInstance(fragment.context!!),
                    Constants.Analytics.EVENT_LOGIN_ONEDRIVE)
            }
        }
    }

    /**
     * OnSpaceCheckCompleteListener method for acquireTokenSilent calls
     * Looks if tokens are in the cache (refreshes if necessary and if we don't forceRefresh)
     * else errors that we need to do an interactive request.
     *
     * @return
     */
    private fun getAuthSilentCallback(fragment: Fragment): AuthenticationCallback {
        return object : AuthenticationCallback {
            override fun onSuccess(authenticationResult: AuthenticationResult) {
                authResult = authenticationResult
                isLoggedIn = true
                checkForAccessToken(fragment)
            }

            override fun onError(exception: MsalException) {
                sourceListener.onLoadError(exception.errorCode + " " + exception.message)
            }

            override fun onCancel() {
                sourceListener.onLoadAborted()
            }
        }
    }

    /**
     * OnSpaceCheckCompleteListener used for interactive request.  If succeeds we use the access
     * token to call the Microsoft Graph. Does not check cache
     *
     * @return
     */
    private fun getAuthInteractiveCallback(fragment: Fragment): AuthenticationCallback {
        return object : AuthenticationCallback {
            override fun onSuccess(authenticationResult: AuthenticationResult) {
                authResult = authenticationResult
                isLoggedIn = true
                checkForAccessToken(fragment)
            }

            override fun onError(exception: MsalException) {
                sourceListener.onLoadError(exception.errorCode + " " + exception.message)
            }

            override fun onCancel() {
                sourceListener.onLoadAborted()
            }
        }
    }

    companion object {
        private const val CLIENT_ID = "019d333e-3e7f-4c04-a214-f12602dd5b10"
        private val SCOPES = arrayOf("https://graph.microsoft.com/Files.ReadWrite")
    }
}