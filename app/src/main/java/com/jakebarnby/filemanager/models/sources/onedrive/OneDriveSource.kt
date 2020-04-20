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
import com.jakebarnby.filemanager.workers.OneDriveFileTreeWalker
import com.microsoft.graph.core.ClientException
import com.microsoft.graph.core.DefaultClientConfig
import com.microsoft.graph.extensions.*
import com.microsoft.graph.http.GraphServiceException
import com.microsoft.identity.client.*
import com.microsoft.identity.client.exception.MsalException
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
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

    companion object {
        private val SCOPES = arrayOf("https://graph.microsoft.com/Files.ReadWrite")
    }

    @Inject
    lateinit var authClient: IMultipleAccountPublicClientApplication

    @Inject
    lateinit var client: IGraphServiceClient

    var authResult: IAuthenticationResult? = null
    var currentAccount: IAccount? = null

    override val storageInfo: StorageInfo?
        get() {
            try {
                val quota = client
                    .me
                    .drive
                    .buildRequest()
                    .select("quota")
                    .get()
                    .quota

                return StorageInfo(
                    quota?.total ?: 0,
                    quota?.used ?: 0
                )
            } catch (e: GraphServiceException) {
                e.printStackTrace()
            }
            return null
        }

    override suspend fun authenticate(context: Context) {}

    fun authenticate(fragment: Fragment) {
        if (isLoggedIn) {
            return
        }
        listener.onLoadStarted()
        try {
            val accessToken = prefsManager.getString(Prefs.ONEDRIVE_TOKEN_KEY, null)
            val userId = prefsManager.getString(Prefs.ONEDRIVE_NAME_KEY, null)
            if (accessToken != null && userId != null) {
                authClient.acquireTokenSilentAsync(
                    SCOPES,
                    authClient.getAccount(userId),
                    authClient.configuration.defaultAuthority.authorityURL.toString(),
                    getAuthSilentCallback(fragment)
                )
            } else {
                authClient.acquireToken(AcquireTokenParameters(
                    AcquireTokenParameters.Builder()
                        .withScopes(SCOPES.toMutableList())
                        .withFragment(fragment)
                        .withCallback(getAuthInteractiveCallback(fragment))
                ))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            listener.onLoadError(e.message ?: "")
        }

        val clientConfig = DefaultClientConfig.createWithAuthenticationProvider {
            it.addHeader("Authorization", String.format("Bearer %s", authResult!!.accessToken))
        }

        client = GraphServiceClient.Builder()
            .fromConfig(clientConfig)
            .buildClient()
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
        listener.onLogout()

        Logger.logFirebaseEvent(
            FirebaseAnalytics.getInstance(context),
            Constants.Analytics.EVENT_LOGOUT_ONEDRIVE
        )
    }

    fun checkForAccessToken(fragment: Fragment) {
        var accessToken = prefsManager.getString(Prefs.ONEDRIVE_TOKEN_KEY, null)
        if (authResult != null) {
            if (accessToken == null || authResult?.accessToken != accessToken) {
                accessToken = authResult?.accessToken
                val userId = authResult?.account?.id
                prefsManager.savePref(Prefs.ONEDRIVE_TOKEN_KEY, accessToken)
                prefsManager.savePref(Prefs.ONEDRIVE_NAME_KEY, userId)
            } else {
                if (!isLoggedIn) {
                    authenticate(fragment)
                } else {
                    loadFiles<OneDriveFileTreeWalker>(fragment.context!!)
                }
            }
        }
        if (accessToken != null) {
            if (!isLoggedIn) {
                authenticate(fragment)
            } else {
                loadFiles<OneDriveFileTreeWalker>(fragment.context!!)
                Logger.logFirebaseEvent(
                    FirebaseAnalytics.getInstance(fragment.context!!),
                    Constants.Analytics.EVENT_LOGIN_ONEDRIVE)
            }
        }
    }

    private fun getAuthSilentCallback(fragment: Fragment): AuthenticationCallback =
        object : AuthenticationCallback {
            override fun onSuccess(authenticationResult: IAuthenticationResult) {
                authResult = authenticationResult
                currentAccount = authenticationResult.account
                isLoggedIn = true
                checkForAccessToken(fragment)
            }

            override fun onError(exception: MsalException) {
                listener.onLoadError(exception.errorCode + " " + exception.message)
            }

            override fun onCancel() {
                listener.onLoadAborted()
            }
        }

    private fun getAuthInteractiveCallback(fragment: Fragment): AuthenticationCallback =
        object : AuthenticationCallback {
            override fun onSuccess(authenticationResult: IAuthenticationResult) {
                authResult = authenticationResult
                currentAccount = authenticationResult.account
                isLoggedIn = true
                checkForAccessToken(fragment)
            }

            override fun onError(exception: MsalException) {
                listener.onLoadError(exception.errorCode + " " + exception.message)
            }

            override fun onCancel() {
                listener.onLoadAborted()
            }
        }

    fun getRootFile(): DriveItem? = client.me.drive.root.buildRequest().get()

    fun getFilesByParentId(id: String): List<DriveItem>? {
        val items = mutableListOf<DriveItem>()
        var pages = client.me.drive
            .getItems(id)
            .children
            .buildRequest()
            .select("id,name,webUrl,folder,size,createdDateTime,lastModifiedDateTime")
            .expand("thumbnails")
            .get()

        while (pages != null) {
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
            return withContext(IO) {
                FileInputStream(file).use {
                    val buffer = ByteArray(file.length().toInt())
                    it.read(buffer)

                    client.me.drive
                        .getItems(params.parentId)
                        .getChildren(transformFileName(params.fileName))
                        .content
                        .buildRequest()
                        .put(buffer)
                }
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
}