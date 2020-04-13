package com.jakebarnby.filemanager.sources.onedrive

import com.jakebarnby.filemanager.managers.PreferenceManager
import com.jakebarnby.filemanager.models.StorageInfo
import com.jakebarnby.filemanager.util.Constants.Prefs
import com.jakebarnby.filemanager.util.Constants.Sources
import com.jakebarnby.filemanager.util.Utils
import com.microsoft.graph.core.ClientException
import com.microsoft.graph.extensions.*
import com.microsoft.graph.http.GraphServiceException
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import javax.inject.Inject

/**
 * Created by Jake on 6/9/2017.
 */
class OneDriveClient @Inject constructor(
    var prefsManager: PreferenceManager
) {

    companion object {
        var client: IGraphServiceClient? = null
    }

    fun getFilesByParentId(id: String) =
        client
            ?.me
            ?.drive
            ?.getItems(id)
            ?.children
            ?.buildRequest()
            ?.select("id,name,webUrl,folder,size,createdDateTime,lastModifiedDateTime")
            ?.expand("thumbnails")
            ?.get()

    fun getNextPage(collection: IDriveItemCollectionPage?) =
        collection
            ?.nextPage
            ?.buildRequest()
            ?.select("id,name,webUrl,folder,size,createdDateTime,lastModifiedDateTime")
            ?.expand("thumbnails")
            ?.get()

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
            client
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
                return client
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
        client
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

        return client
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
        return client
            ?.me
            ?.drive
            ?.getItems(itemId)
            ?.buildRequest()
            ?.patch(item)
    }

    val storageInfo: StorageInfo?
        get() {
            try {
                val quota = client
                    ?.me
                    ?.drive
                    ?.buildRequest()
                    ?.select("quota")
                    ?.get()
                    ?.quota

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

    private fun transformFileName(fileName: String): String {
        var newName = fileName.replace(Sources.ONEDRIVE_INVALID_CHARS.toRegex(), "%20")
        if (newName.length > Sources.MAX_FILENAME_LENGTH) {
            newName = newName.substring(0, Sources.MAX_FILENAME_LENGTH)
        }
        return newName
    }

    fun logout() {
        prefsManager.savePref(Prefs.ONEDRIVE_TOKEN_KEY, null as String?)
        prefsManager.savePref(Prefs.ONEDRIVE_NAME_KEY, null as String?)
    }
}