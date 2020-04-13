package com.jakebarnby.filemanager.sources.dropbox

import android.content.Context
import androidx.core.os.bundleOf
import com.dropbox.core.DbxException
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.*
import com.google.firebase.analytics.FirebaseAnalytics
import com.jakebarnby.filemanager.managers.PreferenceManager
import com.jakebarnby.filemanager.models.StorageInfo
import com.jakebarnby.filemanager.sources.RemoteClient
import com.jakebarnby.filemanager.util.Constants
import com.jakebarnby.filemanager.util.Constants.Prefs
import com.jakebarnby.filemanager.util.Logger
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject

/**
 * Created by Jake on 6/9/2017.
 */
class DropboxClient @Inject constructor(
    var prefsManager: PreferenceManager
) {

    companion object {
        var client: DbxClientV2? = null
    }

    fun getFilesAtPath(path: String): ListFolderResult? =
        client
            ?.files()
            ?.listFolderBuilder(path)
            ?.start()

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
    fun downloadFile(
        downloadPath: String,
        destinationPath: String
    ): File {
        val file = File(destinationPath)
        FileOutputStream(file).use {
            client?.files()
                ?.download(downloadPath)
                ?.download(it)
        }
        return file
    }

    /**
     * @param fileUri
     * @param destPath
     */
    @Throws(IOException::class, DbxException::class)
    fun uploadFile(
        fileUri: String,
        destPath: String
    ): FileMetadata? {
        val localFile = File(fileUri)
        if (!localFile.exists()) {
            return null
        }

        FileInputStream(localFile).use {
            return client?.files()
                ?.uploadBuilder(destPath + "/" + localFile.name)
                ?.withMode(WriteMode.OVERWRITE)
                ?.uploadAndFinish(it)
        }

    }

    /**
     * @param filePath
     */
    @Throws(DbxException::class)
    fun deleteFile(filePath: String?) {
        client?.files()
            ?.delete(filePath)
    }

    /**
     * @param name
     * @param path
     */
    @Throws(DbxException::class)
    fun createFolder(name: String?, path: String?): FolderMetadata? =
        client?.files()?.createFolder(path + File.separator + name)

    /**
     * @param oldPath
     * @param newPath
     * @return
     */
    @Throws(DbxException::class)
    fun rename(oldPath: String?, newPath: String?): Metadata? =
        client?.files()?.move(oldPath, newPath)

    fun logout(context: Context) {
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
    }

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