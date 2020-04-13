package com.jakebarnby.filemanager.sources.onedrive

import android.content.Context
import androidx.fragment.app.Fragment
import com.google.firebase.analytics.FirebaseAnalytics
import com.jakebarnby.filemanager.models.Source
import com.jakebarnby.filemanager.models.SourceConnectionType
import com.jakebarnby.filemanager.models.SourceType
import com.jakebarnby.filemanager.sources.dropbox.DropboxClient
import com.jakebarnby.filemanager.ui.sources.SourceFragmentContract
import com.jakebarnby.filemanager.util.Constants
import com.jakebarnby.filemanager.util.Constants.Prefs
import com.jakebarnby.filemanager.util.Logger
import com.microsoft.graph.concurrency.ICallback
import com.microsoft.graph.core.ClientException
import com.microsoft.graph.core.DefaultClientConfig
import com.microsoft.graph.extensions.DriveItem
import com.microsoft.graph.extensions.GraphServiceClient
import com.microsoft.identity.client.AuthenticationCallback
import com.microsoft.identity.client.AuthenticationResult
import com.microsoft.identity.client.MsalException
import com.microsoft.identity.client.PublicClientApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import javax.inject.Inject

/**
 * Created by jakebarnby on 2/08/17.
 */
class OneDriveSource(
    private val presenter: SourceFragmentContract.Presenter
) : Source(
    SourceConnectionType.REMOTE,
    SourceType.ONEDRIVE.id,
    presenter.prefsManager
) {

    @Inject
    lateinit var oneDriveClient: OneDriveClient

    var client: PublicClientApplication? = null

    private var authResult: AuthenticationResult? = null

    override fun authenticate(context: Context) {}

    fun authenticateSource(fragment: Fragment) {
        client = PublicClientApplication(fragment.context!!, CLIENT_ID)
        presenter.onLoadStarted()
        if (isLoggedIn) {
            return
        }
//        if (!checkConnectionActive(fragment.context!!)) {
//            return
//        }

        try {
            val accessToken = presenter.prefsManager.getString(Prefs.ONEDRIVE_TOKEN_KEY, null)
            val userId = presenter.prefsManager.getString(Prefs.ONEDRIVE_NAME_KEY, null)
            if (accessToken != null && userId != null) {
                client!!.acquireTokenSilentAsync(SCOPES, client!!.getUser(userId), getAuthSilentCallback(fragment))
            } else {
                client!!.acquireToken(fragment, SCOPES, getAuthInteractiveCallback(fragment))
            }
        } catch (e: Exception) {
            presenter.onLoadError(e.message)
        }
    }

    override fun loadFiles(context: Context) {
        if (isFilesLoaded || authResult == null) {
            return
        }

//        if (!checkConnectionActive(context)) {
//            return
//        }

        val clientConfig = DefaultClientConfig.createWithAuthenticationProvider {
            it.addHeader("Authorization", String.format("Bearer %s", authResult!!.accessToken))
        }

        OneDriveClient.client = GraphServiceClient.Builder()
            .fromConfig(clientConfig)
            .buildClient()

        OneDriveClient.client
            ?.me
            ?.drive
            ?.root
            ?.buildRequest()
            ?.get(object : ICallback<DriveItem> {

                override fun success(driveItem: DriveItem) {
                    OneDriveLoaderTask(this@OneDriveSource, presenter, driveItem)
                        .executeOnExecutor(Dispatchers.IO.asExecutor(), "")
                }

                override fun failure(ex: ClientException) {
                    presenter.onLoadAborted()
                }
            })
    }

    override fun logout(context: Context) {
        if (isLoggedIn) {
            oneDriveClient.logout()

            isLoggedIn = false
            isFilesLoaded = false
            authResult = null
            presenter.onLogout()

            Logger.logFirebaseEvent(
                FirebaseAnalytics.getInstance(context),
                Constants.Analytics.EVENT_LOGOUT_ONEDRIVE
            )
        }
    }

    /**
     * Check for a valid access token and store it in shared preferences if found, then load the source
     */
    fun checkForAccessToken(fragment: Fragment) {
        var accessToken = presenter.prefsManager.getString(Prefs.ONEDRIVE_TOKEN_KEY, null)
        if (authResult != null) {
            if (accessToken == null || authResult!!.accessToken != accessToken) {
                accessToken = authResult!!.accessToken
                val userId = authResult!!.user.userIdentifier
                presenter.prefsManager.savePref(Prefs.ONEDRIVE_TOKEN_KEY, accessToken)
                presenter.prefsManager.savePref(Prefs.ONEDRIVE_NAME_KEY, userId)
            } else {
                if (!isLoggedIn) {
                    authenticateSource(fragment)
                } else {
                    loadFiles(fragment.context!!)
                }
            }
        }
        if (accessToken != null) {
            if (!isLoggedIn) {
                authenticateSource(fragment)
            } else {
                loadFiles(fragment.context!!)
                Logger.logFirebaseEvent(
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
                presenter.onLoadError(exception.errorCode + " " + exception.message)
            }

            override fun onCancel() {
                presenter.onLoadAborted()
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
                presenter.onLoadError(exception.errorCode + " " + exception.message)
            }

            override fun onCancel() {
                presenter.onLoadAborted()
            }
        }
    }

    companion object {
        private const val CLIENT_ID = "019d333e-3e7f-4c04-a214-f12602dd5b10"
        private val SCOPES = arrayOf("https://graph.microsoft.com/Files.ReadWrite")
    }
}