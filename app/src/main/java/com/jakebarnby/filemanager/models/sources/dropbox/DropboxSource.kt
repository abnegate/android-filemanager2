package com.jakebarnby.filemanager.models.sources.dropbox

import android.content.Context
import android.os.AsyncTask
import androidx.core.os.bundleOf
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.dropbox.core.DbxException
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.android.Auth
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.*
import com.google.firebase.analytics.FirebaseAnalytics
import com.jakebarnby.filemanager.managers.PreferenceManager
import com.jakebarnby.filemanager.models.Source
import com.jakebarnby.filemanager.models.SourceConnectionType
import com.jakebarnby.filemanager.models.SourceType
import com.jakebarnby.filemanager.models.StorageInfo
import com.jakebarnby.filemanager.models.sources.dropbox.params.*
import com.jakebarnby.filemanager.models.sources.googledrive.params.*
import com.jakebarnby.filemanager.util.Constants
import com.jakebarnby.filemanager.util.Constants.Prefs
import com.jakebarnby.filemanager.util.Constants.Sources
import com.jakebarnby.filemanager.util.Logger
import com.jakebarnby.filemanager.workers.DropBoxFileTreeWalker
import com.jakebarnby.filemanager.workers.LocalFileTreeWalker
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Created by jakebarnby on 2/08/17.
 */
class DropboxSource @Inject constructor(
    prefsManager: PreferenceManager
) : Source<
    DropboxCreateFolderParams,
    DropboxDownloadParams,
    DropboxUploadParams,
    DropboxRenameParams,
    DropboxDeleteParams,
    DropBoxFileTreeWalker,
    Metadata,
    FolderMetadata
    >(
    SourceConnectionType.REMOTE,
    SourceType.DROPBOX.id,
    prefsManager
) {


    companion object {
        var client: DbxClientV2? = null
    }


    override suspend fun authenticate(context: Context) {
        if (prefsManager.hasSourceToken(sourceId)) {
            loadFiles<DropBoxFileTreeWalker>(context)
        } else {
            Auth.startOAuth2Authentication(context, Sources.DROPBOX_CLIENT_ID)
        }
    }

    override suspend fun logout(context: Context) {
        if (!isLoggedIn) {
            return
        }
        try {
            client!!.auth().tokenRevoke()
            client = null
            prefsManager.savePref(Prefs.DROPBOX_TOKEN_KEY, null as String?)
        } catch (e: DbxException) {
            val params = bundleOf(Constants.Analytics.PARAM_ERROR_VALUE to e.message)
            Logger.logFirebaseEvent(
                FirebaseAnalytics.getInstance(context),
                Constants.Analytics.EVENT_ERROR_DROPBOX_LOGOUT,
                params
            )
        }

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

        client = DbxClientV2(
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
            loadFiles<DropBoxFileTreeWalker>(context)
        }

        if (accessToken == null) {
            accessToken = Auth.getOAuth2Token()
            if (accessToken != null) {
                prefsManager.savePref(Prefs.DROPBOX_TOKEN_KEY, accessToken)
                setupClient(accessToken)
                loadFiles<DropBoxFileTreeWalker>(context)

                Logger.logFirebaseEvent(
                    FirebaseAnalytics.getInstance(context),
                    Constants.Analytics.EVENT_LOGIN_DROPBOX
                )
            }
        }
    }

    fun getFilesAtPath(path: String, recursive: Boolean): List<Metadata>? {
        val results = mutableListOf<Metadata>()

        val rootFiles = client?.files()
            ?.listFolderBuilder(path)
            ?.withRecursive(recursive)
            ?.start()

        var cursor = rootFiles?.cursor
        var currentPage = rootFiles?.entries

        while (currentPage != null) {
            results.addAll(currentPage)

            val result = client?.files()
                ?.listFolderContinue(cursor)
            cursor = result?.cursor
            currentPage = result?.entries
        }

        return results
    }


    fun getExternalLink(path: String): String? =
        client
            ?.files()
            ?.getTemporaryLink(path)
            ?.link

    /**
     * @param downloadPath
     * @param destinationPath
     * @return
     */
    @Throws(IOException::class, DbxException::class)
    override suspend fun download(params: DropboxDownloadParams): File = withContext(IO) {
        val file = File(params.destinationPath)
        FileOutputStream(file).use {
            client?.files()
                ?.download(params.fileName)
                ?.download(it)
        }
        file
    }


    /**
     * @param fileUri
     * @param destPath
     */
    @Throws(IOException::class, DbxException::class)
    override suspend fun upload(params: DropboxUploadParams): FileMetadata? {
        val localFile = File(params.filePath)
        if (!localFile.exists()) {
            return null
        }

        return withContext(IO) {
            FileInputStream(localFile).use {
                client?.files()
                    ?.uploadBuilder("${params.destinationPath}${File.separator}${localFile.name}")
                    ?.withMode(WriteMode.OVERWRITE)
                    ?.uploadAndFinish(it)
            }
        }
    }

    /**
     * @param filePath
     */
    @Throws(DbxException::class)
    override suspend fun delete(params: DropboxDeleteParams) {
        client?.files()
            ?.deleteV2(params.path)
    }

    /**
     * @param name
     * @param path
     */
    @Throws(DbxException::class)
    override suspend fun createFolder(params: DropboxCreateFolderParams): FolderMetadata? =
        client?.files()
            ?.createFolderV2("${params.path}${File.separator}${params.folderName}")
            ?.metadata

    /**
     * @param oldPath
     * @param newPath
     * @return
     */
    @Throws(DbxException::class)
    override suspend fun rename(params: DropboxRenameParams): Metadata? =
        client?.files()
            ?.moveV2(params.oldPath, params.newPath)
            ?.metadata


    val storageInfo: StorageInfo?
        get() {
            try {
                val usage = client!!.users().spaceUsage
                val used = usage.used
                var max: Long = 0
                val alloc = usage.allocation
                if (alloc.isIndividual) {
                    max += alloc.individualValue.allocated
                }
                if (alloc.isTeam) {
                    max += alloc.teamValue.allocated
                }
                val info = StorageInfo()
                info.totalSpace = max
                info.usedSpace = used
                info.freeSpace = max - used
                return info
            } catch (e: DbxException) {
                e.printStackTrace()
            }
            return null
        }
}