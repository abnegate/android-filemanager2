package com.jakebarnby.filemanager.models.sources.googledrive

import android.Manifest
import android.content.Context
import android.webkit.MimeTypeMap
import androidx.fragment.app.Fragment
import androidx.room.Entity
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.FileList
import com.google.firebase.analytics.FirebaseAnalytics
import com.jakebarnby.filemanager.managers.PreferenceManager
import com.jakebarnby.filemanager.models.Source
import com.jakebarnby.filemanager.models.SourceConnectionType
import com.jakebarnby.filemanager.models.SourceType
import com.jakebarnby.filemanager.models.StorageInfo
import com.jakebarnby.filemanager.models.sources.googledrive.params.*
import com.jakebarnby.filemanager.util.Constants
import com.jakebarnby.filemanager.util.Constants.Prefs
import com.jakebarnby.filemanager.util.Constants.RequestCodes
import com.jakebarnby.filemanager.util.GooglePlay
import com.jakebarnby.filemanager.util.Logger
import com.jakebarnby.filemanager.util.Utils
import com.jakebarnby.filemanager.workers.GoogleDriveFileTreeWalker
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject

/**
 * Created by jakebarnby on 2/08/17.
 */
@Entity
class GoogleDriveSource @Inject constructor(
    prefsManager: PreferenceManager
) : Source<
    GoogleDriveCreateFolderParams,
    GoogleDriveDownloadParams,
    GoogleDriveUploadParams,
    GoogleDriveRenameParams,
    GoogleDriveDeleteParams,
    GoogleDriveFileTreeWalker,
    com.google.api.services.drive.model.File,
    com.google.api.services.drive.model.File
    >(
    SourceConnectionType.REMOTE,
    SourceType.GOOGLE_DRIVE.id,
    prefsManager
) {

    companion object {
        private val SCOPES = listOf(DriveScopes.DRIVE)

        var client: Drive? = null
    }

    private var credential: GoogleAccountCredential? = null

    override val storageInfo: StorageInfo?
        get() {
            try {
                val quota = client
                    ?.about()
                    ?.get()
                    ?.setFields("storageQuota")
                    ?.execute()
                    ?.storageQuota
                return StorageInfo(
                    quota?.limit ?: 0,
                    quota?.usage ?: 0
                )
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return null
        }

    override suspend fun authenticate(context: Context) {
        //if (!checkConnectionActive(context)) return
        fetchCredential(context)
        if (prefsManager.hasSourceToken(sourceId)) {
            loadFiles<GoogleDriveFileTreeWalker>(context)
            return
        }
        listener.onCheckPermissions(
            Manifest.permission.GET_ACCOUNTS,
            RequestCodes.ACCOUNTS_PERMISSIONS
        )
    }

    override suspend fun logout(context: Context) {
        if (isLoggedIn) {
            prefsManager.savePref(Prefs.GOOGLE_TOKEN_KEY, null as String?)
            prefsManager.savePref(Prefs.GOOGLE_NAME_KEY, null as String?)

            isLoggedIn = false
            isFilesLoaded = false
            credential = null
            listener.onLogout()

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

    fun authenticate(fragment: Fragment) {
        getResultsFromApi(fragment)
    }

    /**
     * Create a google credential and try to call the API with it, do nothing if it fails
     */
    fun authGoogleSilent(fragment: Fragment) {
        val accountName = prefsManager.getString(
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
            listener.onNoConnection()
        } else {
            isLoggedIn = true
            loadFiles<GoogleDriveFileTreeWalker>(fragment.context!!)
        }
    }

    fun saveUserToken(fragment: Fragment) {
        try {
            prefsManager.savePref(Prefs.GOOGLE_TOKEN_KEY, credential!!.token)
        } catch (e: Exception) {
            e.printStackTrace()
            //TODO: Log error
        }
        getResultsFromApi(fragment)
    }

    fun saveUserAccount(fragment: Fragment, accountName: String?) {
        prefsManager.savePref(Prefs.GOOGLE_NAME_KEY, accountName)
        credential!!.selectedAccountName = accountName
        getResultsFromApi(fragment)
    }

    fun getRootFile() =
        client
            ?.files()
            ?.get("root")
            ?.setFields("name,id,mimeType,parents,size,hasThumbnail,thumbnailLink,iconLink,modifiedTime")
            ?.execute()

    fun getFilesByParentId(id: String): FileList? =
        client
            ?.files()
            ?.list()
            ?.setQ(String.format("'%s' in parents", id))
            ?.setFields("files(name,id,mimeType,parents,size,hasThumbnail,thumbnailLink,iconLink,modifiedTime)")
            ?.execute()


    @Throws(IOException::class)
    override suspend fun download(
        params: GoogleDriveDownloadParams
    ): File = withContext(IO) {
        val file = File(params.destinationPath)
        FileOutputStream(file).use {
            client
                ?.files()
                ?.get(params.id)
                ?.executeMediaAndDownloadTo(it)
        }
        file
    }

    @Throws(IOException::class)
    override suspend fun upload(
        params: GoogleDriveUploadParams
    ): com.google.api.services.drive.model.File? {
        val file = File(params.filePath)

        val mimeTypeMap = MimeTypeMap.getSingleton()
        val fileMimeType = mimeTypeMap.getMimeTypeFromExtension(Utils.fileExt(file.absolutePath))

        val fileMetadata = com.google.api.services.drive.model.File().apply {
            parents = listOf(params.parentId)
            mimeType = fileMimeType
            hasThumbnail = true
            name = params.fileName
        }

        val googleFile = FileContent(fileMimeType, file)

        return withContext(IO) {
            client
                ?.files()
                ?.create(fileMetadata, googleFile)
                ?.setFields("name,id,mimeType,parents,size,hasThumbnail,thumbnailLink,iconLink,modifiedTime")
                ?.execute()
        }
    }

    @Throws(IOException::class)
    override suspend fun delete(params: GoogleDriveDeleteParams) {
        withContext(IO) {
            client
                ?.files()
                ?.delete(params.fileId)
                ?.execute()
        }
    }

    @Throws(IOException::class)
    override suspend fun createFolder(params: GoogleDriveCreateFolderParams): com.google.api.services.drive.model.File? {
        val fileMetadata = com.google.api.services.drive.model.File().apply {
            parents = listOf(params.parentId)
            name = params.folderName
            mimeType = Constants.Sources.GOOGLE_DRIVE_FOLDER_MIME
        }

        return withContext(IO) {
            client
                ?.files()
                ?.create(fileMetadata)
                ?.setFields("name,id,mimeType,parents,size,hasThumbnail,thumbnailLink,iconLink,modifiedTime")
                ?.execute()
        }
    }

    @Throws(IOException::class)
    override suspend fun rename(params: GoogleDriveRenameParams
    ): com.google.api.services.drive.model.File? {
        val file = com.google.api.services.drive.model.File().apply {
            name = params.newName
        }

        return withContext(IO) {
            client
                ?.files()
                ?.update(params.fileId, file)
                ?.setFields("name")
                ?.execute()
        }
    }
}