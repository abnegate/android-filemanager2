package com.jakebarnby.filemanager.models

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.jakebarnby.filemanager.managers.PreferenceManager
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

    abstract val storageInfo: StorageInfo?

    var isLoggedIn = false
    var isFilesLoaded = false
    var isMultiSelectEnabled = false

    lateinit var listener: SourceListener

    abstract suspend fun authenticate(context: Context)

    inline fun <reified T : FileTreeWalkerWorker<*>> loadFiles(context: Context) {
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
}