package com.jakebarnby.filemanager.models

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.jakebarnby.filemanager.managers.PreferenceManager
import com.jakebarnby.filemanager.util.Constants
import com.jakebarnby.filemanager.util.TreeNode
import com.jakebarnby.filemanager.workers.DropBoxFileTreeWalker
import com.jakebarnby.filemanager.workers.FileTreeWalkerWorker
import java.io.File
import java.io.Serializable
import java.util.concurrent.TimeUnit

/**
 * Created by Jake on 7/29/2017.
 */
abstract class Source<
    TCreateParams : CreateFolderParams,
    TDownloadParams : DownloadParams,
    TUploadParams : UploadParams,
    TRenameParams : RenameParams,
    TDeleteParams : DeleteParams,
    TTreeWalker: FileTreeWalkerWorker<*>,
    TFileType,
    TFolderType
    >(
    var sourceConnectionType: SourceConnectionType,
    val sourceId: Int,
    protected val prefsManager: PreferenceManager
) : Serializable {

    lateinit var currentDirectory: SourceFile
    lateinit var currentDirChildren: List<SourceFile>

    var totalSpace: Long = 0
    var usedSpace: Long = 0
    var freeSpace: Long = 0
    var isLoggedIn = false
    var isFilesLoaded = false
    var isMultiSelectEnabled = false

    var onLoadStart: (() -> Unit)? = null
    var onLoadError: ((String) -> Unit)? = null
    var onLoadAborted: ((String) -> Unit)? = null
    var onLoadComplete: (() -> Unit)? = null
    var onLogout: (() -> Unit)? = null

    val totalSpaceGB: Double
        get() = totalSpace / Constants.BYTES_TO_GIGABYTE

    val usedSpaceGB: Double
        get() = usedSpace / Constants.BYTES_TO_GIGABYTE

    val usedSpacePercent: Int
        get() = (100 * usedSpace / totalSpace).toInt()

    val freeSpaceGB: Double
        get() = freeSpace / Constants.BYTES_TO_GIGABYTE

    abstract suspend fun authenticate(context: Context)

    inline fun <reified T : FileTreeWalkerWorker<*>> loadFiles(
        context: Context,
        treeWalkerClass: Class<T>
    ) {
        if (isFilesLoaded) {
            return
        }
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                this::class.java.name,
                ExistingPeriodicWorkPolicy.REPLACE,
                PeriodicWorkRequestBuilder<T>(15, TimeUnit.MINUTES)
                    .build()
            )
    }

    abstract suspend fun createFolder(params: TCreateParams): TFolderType?
    abstract suspend fun download(params: TDownloadParams): File?
    abstract suspend fun upload(params: TUploadParams): TFileType?
    abstract suspend fun delete(params: TDeleteParams)
    abstract suspend fun rename(params: TRenameParams): TFileType?
    abstract suspend fun logout(context: Context)

    fun setQuotaInfo(info: StorageInfo?) {
        totalSpace += info?.totalSpace ?: 0
        usedSpace += info?.usedSpace ?: 0
        freeSpace += info?.freeSpace ?: 0
    }
}