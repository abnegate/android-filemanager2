package com.jakebarnby.filemanager.models.sources.local

import android.Manifest
import android.content.Context
import android.os.StatFs
import androidx.room.Entity
import com.jakebarnby.filemanager.managers.PreferenceManager
import com.jakebarnby.filemanager.models.*
import com.jakebarnby.filemanager.ui.sources.SourceFragmentContract
import com.jakebarnby.filemanager.util.Constants.RequestCodes
import com.jakebarnby.filemanager.workers.LocalFileTreeWalker
import java.io.File

/**
 * Created by jakebarnby on 2/08/17.
 */
@Entity
class LocalSource(
    private val rootPath: String,
    prefsManager: PreferenceManager
) : Source<
    LocalCreateFolderParams,
    DownloadParams,
    UploadParams,
    LocalRenameParams,
    LocalDeleteParams,
    LocalFileTreeWalker,
    File,
    File
    >(
    SourceConnectionType.LOCAL,
    SourceType.LOCAL.id,
    prefsManager
) {

    companion object {
        init {
            System.loadLibrary("io-lib")
        }
    }

    override val storageInfo: StorageInfo?
        get() {
            val fileSystem = StatFs(rootPath)

            return StorageInfo(
                fileSystem.totalBytes,
                fileSystem.availableBytes
            )
        }

    /**
     * Calls native io-lib and copies the file at the given path to the given destination
     *
     * @param sourcePath      The path of the file to copy
     * @param destinationPath The destination of the file to copy
     * @return 0 for success, otherwise operation failed
     */
    private external fun copyFileNative(sourcePath: String?, destinationPath: String?): Int

    /**
     * Calls native io-lib and deletes the file at the given path to the given destination
     *
     * @param sourcePath The path of the file to delete
     * @return 0 for success, otherwise operation failed
     */
    private external fun deleteFileNative(sourcePath: String?): Int

    /**
     * Calls native io-lib and creates a new folder
     *
     * @param newPath The name of the folder to create
     * @return 0 for success, otherwise operation failed
     */
    private external fun createFolderNative(newPath: String?): Int

    /**
     * Calls native io-lib and renames the given file or folder to the given new name
     *
     * @param oldPath The previous path to the file or folder
     * @param newPath The new path to the file or folder
     * @return 0 for success, otherwise operation failed
     */
    private external fun renameFolderNative(oldPath: String?, newPath: String?): Int

    override suspend fun authenticate(context: Context) {
        listener.onCheckPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, RequestCodes.STORAGE_PERMISSIONS)
    }

    override suspend fun logout(context: Context) {}

    override suspend fun createFolder(params: LocalCreateFolderParams): File? {
        val path = "${params.path}${File.separator}${params.folderName}"
        if (createFolderNative(path) == 0) {
            return File(path)
        }
        return null
    }

    override suspend fun download(params: DownloadParams): File? =
        throw NotImplementedError()

    override suspend fun upload(params: UploadParams): File? =
        throw NotImplementedError()

    override suspend fun delete(params: LocalDeleteParams) {
        deleteFileNative(params.filePath)
    }

    override suspend fun rename(params: LocalRenameParams): File? {
        val oldPath = "${params.path}${File.separator}${params.oldName}"
        val newPath = "${params.path}${File.separator}${params.newName}"

        if (renameFolderNative(oldPath, newPath) == 0) {
            return File(newPath)
        }
        return null
    }
}