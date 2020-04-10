package com.jakebarnby.filemanager.sources.googledrive

import android.content.Context
import android.webkit.MimeTypeMap
import com.google.api.client.http.FileContent
import com.google.api.services.drive.Drive
import com.jakebarnby.filemanager.sources.models.SourceStorageStats
import com.jakebarnby.filemanager.util.Constants.Prefs
import com.jakebarnby.filemanager.util.Constants.Sources
import com.jakebarnby.filemanager.util.PreferenceUtils
import com.jakebarnby.filemanager.util.Utils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Created by Jake on 6/9/2017.
 */
object GoogleDriveFactory {

    var service: Drive? = null

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
            service
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

        return service
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
        service
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
            mimeType = Sources.GOOGLE_DRIVE_FOLDER_MIME
        }

        return service
            ?.files()
            ?.create(fileMetadata)
            ?.setFields("name,id,mimeType,parents,size,hasThumbnail,thumbnailLink,iconLink,modifiedTime")
            ?.execute()
    }

    fun logout(context: Context) {
        PreferenceUtils.savePref(context, Prefs.GOOGLE_TOKEN_KEY, null as String?)
        PreferenceUtils.savePref(context, Prefs.GOOGLE_NAME_KEY, null as String?)
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

        return service
            ?.files()
            ?.update(parentId, file)
            ?.setFields("name")
            ?.execute()
    }

    val storageStats: SourceStorageStats?
        get() {
            try {
                val quota = service
                    ?.about()
                    ?.get()
                    ?.setFields("storageQuota")
                    ?.execute()
                    ?.storageQuota
                val info = SourceStorageStats()
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