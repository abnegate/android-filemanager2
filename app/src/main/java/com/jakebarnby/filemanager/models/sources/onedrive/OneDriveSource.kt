package com.jakebarnby.filemanager.models.sources.onedrive

import android.content.Context
import androidx.fragment.app.Fragment
import com.google.firebase.analytics.FirebaseAnalytics
import com.jakebarnby.filemanager.managers.PreferenceManager
import com.jakebarnby.filemanager.models.Source
import com.jakebarnby.filemanager.models.SourceConnectionType
import com.jakebarnby.filemanager.models.SourceType
import com.jakebarnby.filemanager.models.StorageInfo
import com.jakebarnby.filemanager.util.Constants
import com.jakebarnby.filemanager.util.Constants.Prefs
import com.jakebarnby.filemanager.util.Logger
import com.jakebarnby.filemanager.util.Utils
import com.jakebarnby.filemanager.workers.FileTreeWalkerWorker
import com.jakebarnby.filemanager.workers.OneDriveFileTreeWalker
import com.microsoft.graph.concurrency.ICallback
import com.microsoft.graph.core.ClientException
import com.microsoft.graph.core.DefaultClientConfig
import com.microsoft.graph.extensions.*
import com.microsoft.graph.http.GraphServiceException
import com.microsoft.identity.client.AuthenticationCallback
import com.microsoft.identity.client.AuthenticationResult
import com.microsoft.identity.client.IAccount
import com.microsoft.identity.client.IMultipleAccountPublicClientApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import javax.inject.Inject

/**
 * Created by jakebarnby on 2/08/17.
 */
class OneDriveSource @Inject constructor(
    prefsManager: PreferenceManager
) : Source<
    OneDriveCreateFolderParams,
    OneDriveDownloadParams,
    OneDriveUploadParams,
    OneDriveRenameParams,
    OneDriveDeleteParams,
    OneDriveFileTreeWalker,
    DriveItem,
    DriveItem
    >(
    SourceConnectionType.REMOTE,
    SourceType.ONEDRIVE.id,
    prefsManager
) {

    @Inject
    lateinit var authClient: IMultipleAccountPublicClientApplication

    @Inject
    lateinit var client: IGraphServiceClient

    var currentAccount: IAccount? = null

    val storageInfo: StorageInfo?
        get() {
            try {
                val quota = client
                    .me
                    .drive
                    .buildRequest()
                    .select("quota")
                    .get()
                    .quota

                return StorageInfo().apply {
                    totalSpace += quota?.total ?: 0
                    usedSpace += quota?.used ?: 0
                    freeSpace += quota?.remaining ?: 0
                }
            } catch (e: GraphServiceException) {
                e.printStackTrace()
            }
            return null
        }

    override suspend fun authenticate(context: Context) {}

    fun authenticateSource(fragment: Fragment) {
        onLoadStart?.invoke()
        if (isLoggedIn) {
            return
        }
        try {
            val accessToken = prefsManager.getString(Prefs.ONEDRIVE_TOKEN_KEY, null)
            val userId = prefsManager.getString(Prefs.ONEDRIVE_NAME_KEY, null)
            if (accessToken != null && userId != null) {
                authClient.acquireTokenSilentAsync(SCOPES, authClient.getUser(userId), getAuthSilentCallback(fragment))
            } else {
                authClient.acquireToken(fragment, SCOPES, getAuthInteractiveCallback(fragment))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onLoadError?.invoke(e.message ?: "")
        }
    }

     fun loadFiles(context: Context) {
        val clientConfig = DefaultClientConfig.createWithAuthenticationProvider {
            it.addHeader("Authorization", String.format("Bearer %s", authResult!!.accessToken))
        }

        client = GraphServiceClient.Builder()
            .fromConfig(clientConfig)
            .buildClient()

        super.loadFiles<OneDriveFileTreeWalker>(context)
    }

    override suspend fun logout(context: Context) {
        if (!isLoggedIn) {
            return
        }
        prefsManager.savePref(Prefs.ONEDRIVE_TOKEN_KEY, null as String?)
        prefsManager.savePref(Prefs.ONEDRIVE_NAME_KEY, null as String?)

        isLoggedIn = false
        isFilesLoaded = false
        authResult = null
        presenter.onLogout()

        Logger.logFirebaseEvent(
            FirebaseAnalytics.getInstance(context),
            Constants.Analytics.EVENT_LOGOUT_ONEDRIVE
        )
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
            override suspend fun onSuccess(authenticationResult: AuthenticationResult) {
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

    fun getRootFile(): DriveItem? =
        client.me.drive.root.buildRequest().get()

    fun getFilesByParentId(id: String): List<DriveItem>? {
        val items = mutableListOf<DriveItem>()
        var pages = client.me.drive
            .getItems(id)
            .children
            .buildRequest()
            .select("id,name,webUrl,folder,size,createdDateTime,lastModifiedDateTime")
            .expand("thumbnails")
            .get()

        while(pages != null) {
            items.addAll(pages.currentPage)
            pages = getNextPage(pages)
        }
        return items
    }

    private fun getNextPage(collection: IDriveItemCollectionPage?) =
        collection
            ?.nextPage
            ?.buildRequest()
            ?.select("id,name,webUrl,folder,size,createdDateTime,lastModifiedDateTime")
            ?.expand("thumbnails")
            ?.get()


    override suspend fun createFolder(params: OneDriveCreateFolderParams): DriveItem? {
        val item = DriveItem().apply {
            this.name = params.folderName
            this.folder = Folder()
        }

        return client.me.drive
            .getItems(params.parentId)
            .children
            .buildRequest()
            .post(item)
    }

    override suspend fun download(params: OneDriveDownloadParams): File? {
        val file = File(
            params.destinationPath,
            params.fileName
        )
        try {
            client.me.drive
                .getItems(params.id)
                .content
                .buildRequest()
                .get()
                ?.use {
                    Utils.copyInputStreamToFile(it, file)
                    return file
                }
        } catch (e: IOException) {
            throw e
        } catch (e: ClientException) {
            throw e
        }
        return null
    }

    override suspend fun upload(params: OneDriveUploadParams): DriveItem? {
        val file = File(params.filePath)
        try {
            FileInputStream(file).use {
                val buffer = ByteArray(file.length().toInt())
                it.read(buffer)
                return client.me.drive
                    .getItems(params.parentId)
                    .getChildren(transformFileName(params.fileName))
                    .content
                    .buildRequest()
                    .put(buffer)
            }
        } catch (e: IOException) {
            throw e
        }
    }

    override suspend fun delete(params: OneDriveDeleteParams) {
        client.me.drive
            .getItems(params.driveId)
            .buildRequest()
            .delete()
    }

    override suspend fun rename(params: OneDriveRenameParams): DriveItem? {
        val item = DriveItem().apply {
            name = params.newName
        }
        return client.me.drive
            .getItems(params.driveId)
            .buildRequest()
            .patch(item)
    }

    private fun transformFileName(fileName: String): String {
        var newName = fileName.replace(Constants.Sources.ONEDRIVE_INVALID_CHARS.toRegex(), "%20")
        if (newName.length > Constants.Sources.MAX_FILENAME_LENGTH) {
            newName = newName.substring(0, Constants.Sources.MAX_FILENAME_LENGTH)
        }
        return newName
    }

    companion object {
        private const val CLIENT_ID = "019d333e-3e7f-4c04-a214-f12602dd5b10"
        private val SCOPES = arrayOf("https://graph.microsoft.com/Files.ReadWrite")
    }
}