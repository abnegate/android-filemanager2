package com.jakebarnby.filemanager.services

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.os.bundleOf
import com.dropbox.core.DbxException
import com.google.firebase.analytics.FirebaseAnalytics
import com.jakebarnby.filemanager.R
import com.jakebarnby.filemanager.managers.SelectedFilesManager
import com.jakebarnby.filemanager.models.SourceFile
import com.jakebarnby.filemanager.models.SourceType
import com.jakebarnby.filemanager.sources.dropbox.DropboxClient
import com.jakebarnby.filemanager.sources.dropbox.DropboxFile
import com.jakebarnby.filemanager.sources.googledrive.GoogleDriveClient
import com.jakebarnby.filemanager.sources.googledrive.GoogleDriveFile
import com.jakebarnby.filemanager.sources.local.LocalFile
import com.jakebarnby.filemanager.sources.onedrive.OneDriveClient
import com.jakebarnby.filemanager.sources.onedrive.OneDriveFile
import com.jakebarnby.filemanager.ui.sources.SourceActivity
import com.jakebarnby.filemanager.util.Constants
import com.jakebarnby.filemanager.util.FileZipper
import com.jakebarnby.filemanager.util.Intents.ACTION_CLEAR_CACHE
import com.jakebarnby.filemanager.util.Intents.ACTION_COMPLETE
import com.jakebarnby.filemanager.util.Intents.ACTION_COPY
import com.jakebarnby.filemanager.util.Intents.ACTION_DELETE
import com.jakebarnby.filemanager.util.Intents.ACTION_MOVE
import com.jakebarnby.filemanager.util.Intents.ACTION_OPEN
import com.jakebarnby.filemanager.util.Intents.ACTION_RENAME
import com.jakebarnby.filemanager.util.Intents.ACTION_SHOW_DIALOG
import com.jakebarnby.filemanager.util.Intents.ACTION_SHOW_ERROR
import com.jakebarnby.filemanager.util.Intents.ACTION_UPDATE_DIALOG
import com.jakebarnby.filemanager.util.Intents.ACTION_ZIP
import com.jakebarnby.filemanager.util.Intents.EXTRA_DIALOG_CURRENT_VALUE
import com.jakebarnby.filemanager.util.Intents.EXTRA_DIALOG_MAX_VALUE
import com.jakebarnby.filemanager.util.Intents.EXTRA_DIALOG_MESSAGE
import com.jakebarnby.filemanager.util.Intents.EXTRA_DIALOG_TITLE
import com.jakebarnby.filemanager.util.Intents.EXTRA_NEW_NAME
import com.jakebarnby.filemanager.util.Intents.EXTRA_OPERATION_ID
import com.jakebarnby.filemanager.util.Intents.EXTRA_TO_OPEN_PATH
import com.jakebarnby.filemanager.util.Intents.EXTRA_ZIP_FILENAME
import com.jakebarnby.filemanager.util.Logger
import com.jakebarnby.filemanager.util.TreeNode
import com.microsoft.graph.http.GraphServiceException
import dagger.android.DaggerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File.separator
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

/**
 * Created by Jake on 6/6/2017.
 */
class SourceTransferService : DaggerService(), CoroutineScope {

    @Inject
    lateinit var analytics: FirebaseAnalytics

    @Inject
    lateinit var selectedFilesManager: SelectedFilesManager

    @Inject
    lateinit var dropBoxClient: DropboxClient

    @Inject
    lateinit var googleDriveClient: GoogleDriveClient

    @Inject
    lateinit var oneDriveClient: OneDriveClient

    private var job = Job()

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    companion object {
        private const val ACTION_CREATE_FOLDER = "com.jakebarnby.filemanager.services.action.CREATE_FOLDER"

        init {
            System.loadLibrary("io-lib")
        }

        /**
         * Start the service for a copy or cut action with the given file.
         *
         * @param context Context for resources
         * @param move    Whether the files should be deleted after copying
         */
        fun startActionCopy(context: Context, move: Boolean) {
            val intent = Intent(context, SourceTransferService::class.java).apply {
                action = if (move) {
                    ACTION_MOVE
                } else {
                    ACTION_COPY
                }
            }
            context.startService(intent)
        }

        /**
         * Start the service for a delete action with the given files.
         *
         * @param context Context for resources
         */
        fun startActionDelete(context: Context) {
            val intent = Intent(context, SourceTransferService::class.java).apply {
                action = ACTION_DELETE
            }
            context.startService(intent)
        }

        /**
         * Start the service for a new folder action
         *
         * @param context Context for resources
         */
        fun startActionCreateFolder(context: Context, name: String) {
            val intent = Intent(context, SourceTransferService::class.java).apply {
                action = ACTION_CREATE_FOLDER
                putExtra(EXTRA_NEW_NAME, name)
            }
            context.startService(intent)
        }

        /**
         * Start the service for a rename action
         *
         * @param context Context for resources
         * @param newName The new name for the file or folder
         */
        fun startActionRename(context: Context, newName: String) {
            val intent = Intent(context, SourceTransferService::class.java).apply {
                action = ACTION_RENAME
                putExtra(EXTRA_NEW_NAME, newName)
            }
            context.startService(intent)
        }

        /**
         * Start the service for an open file action
         *
         * @param context Context for resources
         * @param toOpen  The file to open
         */
        fun startActionOpen(context: Context, toOpen: SourceFile) {
            val intent = Intent(context, SourceTransferService::class.java).apply {
                action = ACTION_OPEN
                putExtra(EXTRA_TO_OPEN_PATH, toOpen)
            }
            context.startService(intent)
        }

        fun startActionZip(context: Context, zipFilename: String) {
            val intent = Intent(context, SourceTransferService::class.java).apply {
                action = ACTION_ZIP
                putExtra(EXTRA_ZIP_FILENAME, zipFilename)
            }
            context.startService(intent)
        }

        fun startClearLocalCache(context: Context) {
            val intent = Intent(context, SourceTransferService::class.java).apply {
                action = ACTION_CLEAR_CACHE
            }
            context.startService(intent)
        }
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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        launch(Dispatchers.IO) {
            if (intent != null) {
                val toOpen = intent.getSerializableExtra(EXTRA_TO_OPEN_PATH) as SourceFile
                val newName = intent.getStringExtra(EXTRA_NEW_NAME)
                val zipName = intent.getStringExtra(EXTRA_ZIP_FILENAME)
                val action = intent.action
                val operationId = selectedFilesManager.operationCount - 1

                if (action != null) {
                    when (action) {
                        ACTION_CREATE_FOLDER -> {
                            createFolder(
                                selectedFilesManager.getActionableDirectory(operationId)
                                    ?: throw NullPointerException("Couldn't get actionable dir for create folder"),
                                operationId,
                                newName
                                    ?: throw NullPointerException("Got null name for new folder"),
                                false
                            )
                        }
                        ACTION_RENAME -> {
                            rename(operationId, newName)
                        }
                        ACTION_COPY -> {
                            copy(
                                selectedFilesManager.getActionableDirectory(operationId)
                                    ?: throw NullPointerException("Couldn't get actionable dir for copy"),
                                operationId,
                                move = false,
                                isSilent = false
                            )
                        }
                        ACTION_MOVE -> {
                            copy(
                                selectedFilesManager.getActionableDirectory(operationId)
                                    ?: throw NullPointerException("Couldn't get actionable dir for move"),
                                operationId,
                                move = true,
                                isSilent = false
                            )
                        }
                        ACTION_DELETE -> {
                            delete(operationId, false)
                        }
                        ACTION_OPEN -> {
                            open(operationId, toOpen)
                        }
                        ACTION_ZIP -> {
                            zipSelection(
                                operationId,
                                zipName ?: throw NullPointerException("Got null zip name")
                            )
                        }
                        ACTION_CLEAR_CACHE -> {
                            clearLocalCache()
                        }
                    }
                }
            }
            stopSelf(startId)
        }

        // If we get killed, after returning from here, restart
        return START_REDELIVER_INTENT
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun clearLocalCache() {
        try {
            if (cacheDir.exists()) {
                val result = deleteFileNative(cacheDir.path)
                if (result != 0) throw IOException("Delete cache directory failed")
            }
        } catch (e: IOException) {
            val params = Bundle()
            params.putString(Constants.Analytics.PARAM_ERROR_VALUE, e.message)
            Logger.logFirebaseEvent(
                analytics,
                Constants.Analytics.EVENT_ERROR_CACHE_CLEAR,
                params)
        }
    }

    /**
     * Create a new folder in the given directory with the given name
     *
     * @param operationId Id of the operation
     * @param name        The name for the new folder
     * @param isSilent    Whether to show visual progress for this operation
     */
    private fun createFolder(
        destDir: TreeNode<SourceFile>,
        operationId: Int,
        name: String,
        isSilent: Boolean
    ): TreeNode<SourceFile>? {
        try {
            if (!isSilent) {
                broadcastShowDialog(getString(R.string.dialog_creating_folder), 0)
            }

            var newFile: SourceFile? = null
            when (destDir.data.sourceId) {
                SourceType.DROPBOX.id -> {
                    val dropboxFile = dropBoxClient.createFolder(
                        name,
                        destDir.data.path
                    ) ?: return null
                    newFile = DropboxFile(dropboxFile)
                }
                SourceType.GOOGLE_DRIVE.id -> {
                    val googleFile = googleDriveClient.createFolder(
                        name,
                        (destDir.data as GoogleDriveFile).driveId
                    ) ?: return null

                    newFile = GoogleDriveFile(googleFile)
                }
                SourceType.ONEDRIVE.id -> {
                    val onedriveFile = oneDriveClient.createFolder(
                        name,
                        (destDir.data as OneDriveFile).driveId
                    ) ?: return null
                    newFile = OneDriveFile(onedriveFile)
                }
                else -> {
                    if (destDir.data.sourceId == SourceType.LOCAL.id) {
                        val result = createFolderNative("${destDir.data.path}${separator}${name}")
                        if (result != 0) {
                            throw IOException("creating folder")
                        }

                        newFile = LocalFile(
                            java.io.File("${destDir.data.path}${separator}${name}"),
                            destDir.data.sourceId
                        )
                    }
                }
            }

            if (newFile == null) {
                throw IOException("creating folder")
            }

            val newFolder = destDir.addChild(newFile)

            if (!isSilent) {
                broadcastFinishedTask(operationId)
            }
            Logger.logFirebaseEvent(
                analytics,
                Constants.Analytics.EVENT_SUCCESS_CREATE_FOLDER
            )

            return newFolder
        } catch (e: Exception) {
            hideNotification(operationId)
            broadcastError(String.format(
                "%s %s%s",
                getString(R.string.problem_encountered),
                getString(R.string.creating_folder),
                "."))
            val params = bundleOf(
                Constants.Analytics.PARAM_ERROR_VALUE to e.message,
                Constants.Analytics.PARAM_SOURCE_NAME to destDir.data.sourceId
            )
            Logger.logFirebaseEvent(
                analytics,
                Constants.Analytics.EVENT_ERROR_CREATE_FOLDER,
                params)
        }
        return null
    }

    /**
     * Renames the selected file or folder to the given name
     *
     * @param operationId Id of the operation
     * @param name        The new name of the file or folder
     */
    private fun rename(operationId: Int, name: String?) {
        try {
            if (name == null) {
                throw NullPointerException("No name given to rename operation")
            }

            broadcastShowDialog(getString(R.string.dialog_renaming), 0)

            val toRename: TreeNode<SourceFile> = selectedFilesManager
                .getSelectedFiles(operationId)
                ?.get(0)
                ?: throw NullPointerException("No file to rename")

            val oldPath = toRename.data.path
            val newPath = toRename.data.path.substring(
                0,
                toRename.data.path.lastIndexOf(separator) + 1
            ) + name

            when (toRename.data.sourceId) {
                SourceType.DROPBOX.id -> {
                    dropBoxClient.rename(oldPath, newPath)
                }
                SourceType.GOOGLE_DRIVE.id -> {
                    googleDriveClient.rename(name, (toRename.data as GoogleDriveFile).driveId)
                }
                SourceType.ONEDRIVE.id -> {
                    oneDriveClient.rename(name, (toRename.data as OneDriveFile).driveId)
                }
                else -> {
                    if (toRename.data.sourceId == SourceType.LOCAL.id) {
                        val result = renameFolderNative(oldPath, newPath)
                        if (result != 0) {
                            throw IOException("renaming file")
                        }
                    }
                }
            }

            toRename.data.name = name
            toRename.data.path = newPath

            broadcastFinishedTask(operationId)

            Logger.logFirebaseEvent(
                analytics,
                Constants.Analytics.EVENT_SUCCESS_RENAMING
            )
        } catch (e: Exception) {
            broadcastError(String.format(
                "%s %s%s",
                getString(R.string.problem_encountered),
                getString(R.string.renaming_item),
                "."))

            val sourceName = if (
                selectedFilesManager.getSelectedFiles(operationId)?.get(0) != null &&
                selectedFilesManager.getSelectedFiles(operationId)?.get(0)?.data != null
            ) {
                selectedFilesManager.getSelectedFiles(operationId)?.get(0)?.data?.sourceId
            } else {
                Constants.Analytics.NO_DESTINATION
            }

            val params = bundleOf(
                Constants.Analytics.PARAM_ERROR_VALUE to e.message,
                Constants.Analytics.PARAM_SOURCE_NAME to sourceName
            )
            Logger.logFirebaseEvent(
                analytics,
                Constants.Analytics.EVENT_ERROR_RENAMING,
                params
            )
        }
    }

    /**
     * Open the given [SourceFile]. If it is a remote file, dowload it first
     *
     * @param operationId Id of the operation
     * @param toOpen      The file to open
     */
    private fun open(
        operationId: Int,
        toOpen: SourceFile?
    ) {
        try {
            if (toOpen == null) {
                throw NullPointerException("Null file passed to open")
            }

            broadcastShowDialog(getString(R.string.opening), 0)

            val cachePath = cacheDir.path + separator
            val filePath = if (!java.io.File(cachePath + toOpen.name).exists()) {
                getFile(toOpen)
            } else {
                cachePath + toOpen.name
            }

            broadcastFinishedTask(
                operationId,
                bundleOf(Constants.FILE_PATH_KEY to filePath)
            )
            Logger.logFirebaseEvent(
                analytics,
                Constants.Analytics.EVENT_SUCCESS_OPEN_FILE
            )

        } catch (e: Exception) {
            hideNotification(operationId)
            broadcastError(String.format(
                "%s %s%s",
                getString(R.string.problem_encountered),
                getString(R.string.opening_file),
                "."))

            val sourceName = toOpen?.sourceId ?: Constants.Analytics.NO_DESTINATION

            val params = bundleOf(
                Constants.Analytics.PARAM_ERROR_VALUE to e.message,
                Constants.Analytics.PARAM_SOURCE_NAME to sourceName
            )
            Logger.logFirebaseEvent(
                analytics,
                Constants.Analytics.EVENT_ERROR_OPENING_FILE,
                params
            )
        }
    }

    /**
     * Copy or move the selected files to the given destination, if moving, delete the original files after
     *
     * @param move Whether this is a move or copt operation
     */
    private fun copy(
        destDir: TreeNode<SourceFile>,
        operationId: Int,
        move: Boolean,
        isSilent: Boolean
    ): List<TreeNode<SourceFile>>? {
        try {
            val toCopy = selectedFilesManager.getSelectedFiles(operationId)
                ?: throw NullPointerException("No files passed to copy")
            val newFiles = mutableListOf<TreeNode<SourceFile>>()
            if (!isSilent) {
                postNotification(
                    operationId,
                    getString(R.string.app_name),
                    if (move) {
                        String.format(getString(R.string.moving_count), 1, toCopy.size)
                    } else {
                        String.format(getString(R.string.copying_count), 1, toCopy.size)
                    })
                broadcastShowDialog(
                    if (move) {
                        getString(R.string.moving)
                    } else {
                        getString(R.string.copying)
                    },
                    toCopy.size
                )
            }

            recurseCopy(
                toCopy,
                destDir,
                operationId,
                move,
                isSilent,
                0,
                newFiles
            )

            if (move) {
                delete(operationId, true)
            }

            if (!isSilent) {
                broadcastFinishedTask(operationId)
                Logger.logFirebaseEvent(
                    analytics,
                    Constants.Analytics.EVENT_SUCCESS_COPYING
                )
            }

            return newFiles
        } catch (e: Exception) {
            hideNotification(operationId)
            broadcastError(String.format(
                "%s %s%s",
                getString(R.string.problem_encountered),
                getString(if (move) R.string.moving_items else R.string.copying_items),
                "."))
            val sourceName = if (selectedFilesManager.getActionableDirectory(operationId) != null &&
                selectedFilesManager.getActionableDirectory(operationId)?.data != null) {
                selectedFilesManager.getActionableDirectory(operationId)?.data?.sourceId
            } else {
                Constants.Analytics.NO_DESTINATION
            }
            val params = bundleOf(
                Constants.Analytics.PARAM_ERROR_VALUE to e.message,
                Constants.Analytics.PARAM_SOURCE_NAME to sourceName
            )
            Logger.logFirebaseEvent(
                analytics,
                Constants.Analytics.EVENT_ERROR_COPYING,
                params
            )
        }
        return null
    }

    /**
     * Recursively copy or move the given collection of files and/or folders to the given destionation
     * @param toCopy      The collection of files and/or folders to copy
     * @param destDir     The destination of the operation
     * @param operationId Id of the operation
     * @param move        Whether this a move or copy operation
     * @param depth
     * @param newFiles
     */
    @Throws(
        IOException::class,
        DbxException::class,
        GraphServiceException::class,
        NullPointerException::class
    )
    private fun recurseCopy(
        toCopy: List<TreeNode<SourceFile>>,
        destDir: TreeNode<SourceFile>,
        operationId: Int,
        move: Boolean,
        isSilent: Boolean,
        depth: Int,
        newFiles: MutableList<TreeNode<SourceFile>>
    ) {
        for (file in toCopy) {
            var newItem: TreeNode<SourceFile>?

            if (file.data.isDirectory) {
                newItem = createFolder(
                    destDir,
                    operationId,
                    file.data.name,
                    true
                ) ?: throw NullPointerException("Failed to create folder at destination")

                recurseCopy(
                    file.children,
                    newItem,
                    operationId,
                    move,
                    isSilent,
                    depth + 1,
                    newFiles
                )
            } else {
                val newFilePath = getFile(file.data)
                    ?: throw NullPointerException("Get file returned null")

                val newFile = putFile(
                    newFilePath,
                    file.data.name,
                    destDir.data
                ) ?: throw NullPointerException("Put file returned null")

                newItem = destDir.addChild(newFile)
                var curDir: TreeNode<SourceFile>? = destDir
                while (true) {
                    if (curDir?.data != null) {
                        curDir.data.size += file.data.size
                    }
                    curDir = if (curDir?.parent != null) {
                        curDir.parent
                    } else {
                        break
                    }
                }
            }
            if (depth != 0) {
                continue
            }

            newFiles.add(newItem)

            if (isSilent) {
                return
            }

            broadcastUpdate(toCopy.indexOf(file) + 1)

            val notificationContent = if (move) {
                String.format(
                    getString(R.string.moving_count),
                    toCopy.indexOf(file) + 1,
                    toCopy.size)
            } else {
                String.format(
                    getString(R.string.copying_count),
                    toCopy.indexOf(file) + 1,
                    toCopy.size)
            }

            postNotification(
                operationId,
                getString(R.string.app_name),
                notificationContent
            )
        }
    }

    /**
     * Delete the selected files
     *
     * @param operationId Id of the operation
     * @param isSilent    Whether to show visual progress for this operation
     */
    private fun delete(operationId: Int, isSilent: Boolean) {
        try {
            val toDelete = selectedFilesManager.getSelectedFiles(operationId)
                ?: throw NullPointerException("Null files passed to delete")

            if (!isSilent) {
                broadcastShowDialog(getString(R.string.dialog_deleting), toDelete.size)
            }
            for (file in toDelete) {
                postNotification(
                    operationId,
                    getString(R.string.app_name), String.format(
                    getString(R.string.deleting_count),
                    toDelete.indexOf(file) + 1,
                    toDelete.size)
                )
                when (file.data.sourceId) {
                    SourceType.DROPBOX.id -> {
                        dropBoxClient.deleteFile(file.data.path)
                    }
                    SourceType.GOOGLE_DRIVE.id -> {
                        googleDriveClient.deleteFile((file.data as GoogleDriveFile).driveId)
                    }
                    SourceType.ONEDRIVE.id -> {
                        oneDriveClient.deleteFile((file.data as? OneDriveFile)?.driveId)
                    }
                    else -> if (file.data.sourceId == SourceType.LOCAL.id) {
                        val result = deleteFileNative(file.data.path)
                        if (result != 0) {
                            throw IOException("deleting file")
                        }
                    }
                }
                var curDir = file.parent
                curDir?.removeChild(file)
                while (true) {
                    if (curDir?.data != null) {
                        curDir.data.size -= file.data.size
                    }
                    curDir = if (curDir?.parent != null) {
                        curDir.parent
                    } else {
                        break
                    }
                }
                if (!isSilent) {
                    broadcastUpdate(toDelete.indexOf(file) + 1)
                }
            }
            if (!isSilent) {
                broadcastFinishedTask(operationId)
            }
            Logger.logFirebaseEvent(
                analytics,
                Constants.Analytics.EVENT_SUCCESS_DELETING
            )
        } catch (e: Exception) {
            hideNotification(operationId)
            broadcastError(String.format(
                "%s %s%s",
                getString(R.string.problem_encountered),
                getString(R.string.deleting_items),
                "."))
            val sourceName = if (selectedFilesManager.getActionableDirectory(operationId) != null &&
                selectedFilesManager.getActionableDirectory(operationId)?.data != null) {
                selectedFilesManager.getActionableDirectory(operationId)?.data?.sourceId
            } else {
                Constants.Analytics.NO_DESTINATION
            }

            val params = bundleOf(
                Constants.Analytics.PARAM_ERROR_VALUE to e.message,
                Constants.Analytics.PARAM_SOURCE_NAME to sourceName
            )
            Logger.logFirebaseEvent(
                analytics,
                Constants.Analytics.EVENT_ERROR_DELETE,
                params
            )
        }
    }

    private fun zipSelection(operationId: Int, zipFileName: String) {
        try {
            val destDir = selectedFilesManager.getActionableDirectory(operationId)
                ?: throw NullPointerException("Null files passed to zip")

            broadcastShowDialog(getString(R.string.creating_zip), 0)

            val toZip = copy(
                TreeNode(LocalFile(cacheDir, SourceType.LOCAL.id)),
                operationId,
                move = false,
                isSilent = true
            ) ?: throw NullPointerException("Failed getting files to zip")

            val filesToZipPaths = toZip.map {
                it.data.path
            }

            FileZipper().zipFiles(
                cacheDir.path + separator + zipFileName,
                filesToZipPaths
            )

            val newZip = putFile(
                cacheDir.path + separator + zipFileName,
                zipFileName,
                destDir.data
            ) ?: throw NullPointerException("Failed pitting zip file in dest")

            destDir.addChild(newZip)
            broadcastFinishedTask(operationId)
        } catch (e: Exception) {
            e.printStackTrace()
            hideNotification(operationId)
            broadcastError(String.format(
                "%s %s%s",
                getString(R.string.problem_encountered),
                getString(R.string.zipping_files),
                "."))
            val sourceName = if (selectedFilesManager.getActionableDirectory(operationId) != null &&
                selectedFilesManager.getActionableDirectory(operationId)?.data != null) {
                selectedFilesManager.getActionableDirectory(operationId)?.data?.sourceId
            } else {
                Constants.Analytics.NO_DESTINATION
            }
            val params = bundleOf(
                Constants.Analytics.PARAM_ERROR_VALUE to e.message,
                Constants.Analytics.PARAM_SOURCE_NAME to sourceName
            )
            Logger.logFirebaseEvent(
                analytics,
                Constants.Analytics.EVENT_ERROR_ZIPPING,
                params)
        }
    }

    /**
     * Gets the given source file and stores it in the given destination
     *
     * @param file The file to retrieve
     * @return The path of the retrieved file
     */
    @Throws(IOException::class, DbxException::class)
    private fun getFile(file: SourceFile?): String? {
        if (file == null) {
            throw IOException("Tried to get a null file")
        }

        var newFilePath: String? = null
        val destPath = cacheDir.path + separator + file.name
        when (file.sourceId) {
            SourceType.DROPBOX.id -> {
                val newFile = dropBoxClient.downloadFile(
                    file.path,
                    cacheDir.path + separator + file.name
                )
                if (newFile.exists()) {
                    Logger.logFirebaseEvent(
                        analytics,
                        Constants.Analytics.EVENT_SUCCESS_DROPBOX_DOWNLOAD
                    )
                    newFilePath = newFile.path
                }
            }
            SourceType.GOOGLE_DRIVE.id -> {
                val googleFile = googleDriveClient.downloadFile(
                    (file as GoogleDriveFile).driveId,
                    destPath
                )
                if (googleFile.exists()) {
                    Logger.logFirebaseEvent(
                        analytics,
                        Constants.Analytics.EVENT_SUCCESS_GOOGLEDRIVE_DOWNLOAD
                    )
                    newFilePath = googleFile.path
                }
            }
            SourceType.ONEDRIVE.id -> {
                val oneDriveFile = oneDriveClient.downloadFile(
                    (file as OneDriveFile).driveId,
                    file.name,
                    cacheDir.path
                )

                if (oneDriveFile?.exists() == true) {
                    Logger.logFirebaseEvent(
                        analytics,
                        Constants.Analytics.EVENT_SUCCESS_ONEDRIVE_DOWNLOAD
                    )
                    newFilePath = oneDriveFile.path
                }
            }
            else -> if (file.sourceId == SourceType.LOCAL.id) {
                newFilePath = file.path
            }
        }
        return newFilePath
    }

    /**
     * Puts the given file with the given fileName at the given destination
     *
     * @param newFilePath The path of the file to put
     * @param fileName    The name of the file to put
     * @param destDir     The destination of the file
     */
    @Throws(IOException::class, DbxException::class, NullPointerException::class)
    private fun putFile(
        newFilePath: String,
        fileName: String,
        destDir: SourceFile
    ): SourceFile? {
        when (destDir.sourceId) {
            SourceType.DROPBOX.id -> {
                val dropboxFile = DropboxFile(
                    dropBoxClient.uploadFile(
                        newFilePath,
                        destDir.path
                    ) ?: throw NullPointerException("Upload returned null")
                )
                Logger.logFirebaseEvent(
                    analytics,
                    Constants.Analytics.EVENT_SUCCESS_DROPBOX_UPLOAD
                )
                return dropboxFile
            }
            SourceType.GOOGLE_DRIVE.id -> {
                val googleDriveFile = GoogleDriveFile(
                    googleDriveClient.uploadFile(
                        newFilePath,
                        fileName,
                        (destDir as GoogleDriveFile).driveId
                    ) ?: throw NullPointerException("Upload returned null")
                )
                Logger.logFirebaseEvent(
                    analytics,
                    Constants.Analytics.EVENT_SUCCESS_GOOGLEDRIVE_UPLOAD
                )
                return googleDriveFile
            }
            SourceType.ONEDRIVE.id -> {
                val oneDriveFile = OneDriveFile(
                    oneDriveClient.uploadFile(
                        newFilePath,
                        fileName,
                        (destDir as OneDriveFile).driveId
                    ) ?: throw NullPointerException("Upload returned null")
                )
                Logger.logFirebaseEvent(
                    analytics,
                    Constants.Analytics.EVENT_SUCCESS_ONEDRIVE_UPLOAD
                )
                return oneDriveFile
            }
            else -> if (destDir.sourceId == SourceType.LOCAL.id) {
                val result = copyFileNative(
                    newFilePath,
                    destDir.path + "/" + fileName)
                if (result != 0) {
                    throw IOException("copying file to device failed")
                }
                return LocalFile(
                    java.io.File(destDir.path + "/" + fileName),
                    destDir.sourceId)
            }
        }
        return null
    }

    private fun broadcastFinishedTask(
        operationId: Int,
        bundle: Bundle? = null
    ) {
        val intent = Intent().apply {
            action = ACTION_COMPLETE
        }
        if (bundle != null) {
            bundle.putInt(EXTRA_OPERATION_ID, operationId)
            intent.putExtras(bundle)
        } else {
            intent.putExtra(EXTRA_OPERATION_ID, operationId)
        }

        selectedFilesManager.getSelectedFiles(operationId)?.clear()
        applicationContext.sendBroadcast(intent)
        hideNotification(operationId)
    }

    private fun broadcastError(message: String) {
        val intent = Intent().apply {
            action = ACTION_SHOW_ERROR
            putExtra(EXTRA_DIALOG_MESSAGE, message)
        }
        applicationContext.sendBroadcast(intent)
    }

    /**
     * Notifies the hosting activity to display a dialog with the given title and max progress
     *
     * @param title      The title for the dialog
     * @param totalCount The max progress for the operation
     */
    private fun broadcastShowDialog(title: String, totalCount: Int) {
        val intent = Intent().apply {
            action = ACTION_SHOW_DIALOG
            putExtra(EXTRA_DIALOG_TITLE, title)
            putExtra(EXTRA_DIALOG_MAX_VALUE, totalCount)
        }
        applicationContext.sendBroadcast(intent)
    }

    /**
     * Notifies the hosting activity of an update to a dialog (if showing) displaying the new progress value
     *
     * @param currentCount The current progress of the operation
     */
    private fun broadcastUpdate(currentCount: Int) {
        val intent = Intent().apply {
            action = ACTION_UPDATE_DIALOG
            putExtra(EXTRA_DIALOG_CURRENT_VALUE, currentCount)
        }
        applicationContext.sendBroadcast(intent)
    }

    /**
     * Post a notification with the given title and content to the status bar
     *
     * @param title   Title for the notification
     * @param content Content body of the notification
     */
    private fun postNotification(
        operationId: Int,
        title: String,
        content: String
    ) {
        val builder = NotificationCompat.Builder(this)
            .setSmallIcon(R.drawable.ic_folder_flat)
            .setContentTitle(title)
            .setContentText(content)
        val resultIntent = Intent(this, SourceActivity::class.java)
        //TODO: Get this in activity and open the associated directory
        resultIntent.putExtra(EXTRA_OPERATION_ID, operationId)
        val resultPendingIntent = PendingIntent.getActivity(
            this,
            0,
            resultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT)
        builder.setContentIntent(resultPendingIntent)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.notify(operationId, builder.build())
    }

    /**
     * Removes the notification from the status bar
     */
    private fun hideNotification(operationId: Int) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.cancel(operationId)
    }
}