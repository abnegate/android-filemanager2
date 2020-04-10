package com.jakebarnby.filemanager.sources.onedrive

import android.content.Context
import com.jakebarnby.filemanager.sources.models.SourceStorageStats
import com.jakebarnby.filemanager.util.Constants.Prefs
import com.jakebarnby.filemanager.util.Constants.Sources
import com.jakebarnby.filemanager.util.PreferenceUtils
import com.jakebarnby.filemanager.util.Utils
import com.microsoft.graph.core.ClientException
import com.microsoft.graph.extensions.DriveItem
import com.microsoft.graph.extensions.Folder
import com.microsoft.graph.extensions.IGraphServiceClient
import com.microsoft.graph.http.GraphServiceException
import java.io.File
import java.io.FileInputStream
import java.io.IOException

/**
 * Created by Jake on 6/9/2017.
 */
object OneDriveFactory {

    var service: IGraphServiceClient? = null

    /**
     *
     * @param id
     * @param filename
     * @param destinationPath
     * @return
     */
    @Throws(IOException::class)
    fun downloadFile(
        id: String,
        filename: String,
        destinationPath: String
    ): File? {
        val file = File(destinationPath, filename)
        try {
            service
                ?.me
                ?.drive
                ?.getItems(id)
                ?.content
                ?.buildRequest()
                ?.get()
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

    /**
     * @param filePath
     * @param fileName
     * @param parentId
     */
    @Throws(IOException::class, GraphServiceException::class)
    fun uploadFile(
        filePath: String,
        fileName: String,
        parentId: String
    ): DriveItem? {
        val file = File(filePath)
        try {
            FileInputStream(file).use {
                val buffer = ByteArray(file.length().toInt())
                it.read(buffer)
                return service
                    ?.me
                    ?.drive
                    ?.getItems(parentId)
                    ?.getChildren(transformFileName(fileName))
                    ?.content
                    ?.buildRequest()
                    ?.put(buffer)
            }
        } catch (e: IOException) {
            throw e
        }
    }

    /**
     *
     * @param driveId
     */
    @Throws(GraphServiceException::class)
    fun deleteFile(driveId: String?) {
        service
            ?.me
            ?.drive
            ?.getItems(driveId)
            ?.buildRequest()
            ?.delete()
    }

    /**
     * @param name
     * @param parentId
     */
    @Throws(GraphServiceException::class)
    fun createFolder(
        name: String,
        parentId: String
    ): DriveItem? {
        val item = DriveItem().apply {
            this.name = name
            this.folder = Folder()
        }

        return service
            ?.me
            ?.drive
            ?.getItems(parentId)
            ?.children
            ?.buildRequest()
            ?.post(item)
    }

    @Throws(GraphServiceException::class)
    fun rename(
        newName: String,
        itemId: String
    ): DriveItem? {
        val item = DriveItem().apply {
            name = newName
        }
        return service
            ?.getMe()
            ?.drive
            ?.getItems(itemId)
            ?.buildRequest()
            ?.patch(item)
    }

    val storageStats: SourceStorageStats?
        get() {
            try {
                val quota = service
                    ?.me
                    ?.drive
                    ?.buildRequest()
                    ?.select("quota")
                    ?.get()
                    ?.quota

                return SourceStorageStats().apply {
                    totalSpace += quota?.total ?: 0
                    usedSpace += quota?.used ?: 0
                    freeSpace += quota?.remaining ?: 0
                }
            } catch (e: GraphServiceException) {
                e.printStackTrace()
            }
            return null
        }

    private fun transformFileName(fileName: String): String {
        var newName = fileName.replace(Sources.ONEDRIVE_INVALID_CHARS.toRegex(), "%20")
        if (newName.length > Sources.MAX_FILENAME_LENGTH) {
            newName = newName.substring(0, Sources.MAX_FILENAME_LENGTH)
        }
        return newName
    }

    fun logout(context: Context) {
        PreferenceUtils.savePref(context, Prefs.ONEDRIVE_TOKEN_KEY, null as String?)
        PreferenceUtils.savePref(context, Prefs.ONEDRIVE_NAME_KEY, null as String?)
    }
}