package com.jakebarnby.filemanager.sources.googledrive

import android.content.Context
import android.webkit.MimeTypeMap
import com.google.api.client.http.FileContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.FileList
import com.jakebarnby.filemanager.managers.PreferenceManager
import com.jakebarnby.filemanager.models.StorageInfo
import com.jakebarnby.filemanager.util.Constants
import com.jakebarnby.filemanager.util.Constants.Prefs
import com.jakebarnby.filemanager.util.Utils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject

/**
 * Created by Jake on 6/9/2017.
 */
class GoogleDriveClient @Inject constructor(
    var prefsManager: PreferenceManager
) {

    companion object {
        var client: Drive? = null
    }

    fun getFilesAtPath(path: String): com.google.api.services.drive.model.File? =
        client
            ?.files()
            ?.get(path)
            ?.setFields("name,id,mimeType,parents,size,hasThumbnail,thumbnailLink,iconLink,modifiedTime")
            ?.execute()

    fun getFilesByParentId(id: String): FileList? =
        client
            ?.files()
            ?.list()
            ?.setQ(String.format("'%s' in parents", id))
            ?.setFields("files(name,id,mimeType,parents,size,hasThumbnail,thumbnailLink,iconLink,modifiedTime)")
            ?.execute()

    /**
     * @param fileId
     * @param destinationPath
     * @return
     */
    @Throws(IOException::class)
    fun downloadFile(
        fileId: String,
        destinationPath: String
    ): File {
        val file = File(destinationPath)
        FileOutputStream(file).use {
            client
                ?.files()
                ?.get(fileId)
                ?.executeMediaAndDownloadTo(it)
        }
        return file
    }

    /**
     * Upload a file at the given path on Google Drive
     *
     * @param filePath Path to upload the file to
     */
    @Throws(IOException::class)
    fun uploadFile(
        filePath: String,
        fileName: String,
        parentId: String
    ): com.google.api.services.drive.model.File? {
        val file = File(filePath)

        val mimeTypeMap = MimeTypeMap.getSingleton()
        val fileMimeType = mimeTypeMap.getMimeTypeFromExtension(Utils.fileExt(filePath))

        val fileMetadata = com.google.api.services.drive.model.File().apply {
            parents = listOf(parentId)
            mimeType = fileMimeType
            hasThumbnail = true
            name = fileName
        }

        val googleFile = FileContent(fileMimeType, file)

        return client
            ?.files()
            ?.create(fileMetadata, googleFile)
            ?.setFields("name,id,mimeType,parents,size,hasThumbnail,thumbnailLink,iconLink,modifiedTime")
            ?.execute()
    }

    /**
     * Delete the gile with the given ID from Google Drive
     *
     * @param fileId The ID of the file to delete
     */
    @Throws(IOException::class)
    fun deleteFile(fileId: String) {
        client
            ?.files()
            ?.delete(fileId)
            ?.execute()
    }

    /**
     * @param folderName
     * @param parentId
     */
    @Throws(IOException::class)
    fun createFolder(
        folderName: String,
        parentId: String
    ): com.google.api.services.drive.model.File? {
        val fileMetadata = com.google.api.services.drive.model.File().apply {
            parents = listOf(parentId)
            name = folderName
            mimeType = Constants.Sources.GOOGLE_DRIVE_FOLDER_MIME
        }

        return client
            ?.files()
            ?.create(fileMetadata)
            ?.setFields("name,id,mimeType,parents,size,hasThumbnail,thumbnailLink,iconLink,modifiedTime")
            ?.execute()
    }

    fun logout(context: Context) {
        prefsManager.savePref(Prefs.GOOGLE_TOKEN_KEY, null as String?)
        prefsManager.savePref(Prefs.GOOGLE_NAME_KEY, null as String?)
    }

    /**
     * @param newName
     * @param parentId
     * @return
     */
    @Throws(IOException::class)
    fun rename(
        newName: String,
        parentId: String
    ): com.google.api.services.drive.model.File? {
        val file = com.google.api.services.drive.model.File().apply {
            name = newName
        }

        return client
            ?.files()
            ?.update(parentId, file)
            ?.setFields("name")
            ?.execute()
    }

    val storageInfo: StorageInfo?
        get() {
            try {
                val quota = client
                    ?.about()
                    ?.get()
                    ?.setFields("storageQuota")
                    ?.execute()
                    ?.storageQuota
                val info = StorageInfo()
                info.totalSpace = quota?.limit ?: 0
                info.usedSpace = quota?.usage ?: 0
                info.freeSpace = (quota?.limit ?: 0) - (quota?.usage ?: 0)
                return info
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return null
        }
}