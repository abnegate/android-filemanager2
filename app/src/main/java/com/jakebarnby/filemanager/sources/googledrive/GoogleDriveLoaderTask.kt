package com.jakebarnby.filemanager.sources.googledrive

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import com.jakebarnby.filemanager.models.Source
import com.jakebarnby.filemanager.models.SourceFile
import com.jakebarnby.filemanager.ui.sources.SourceFragmentContract
import com.jakebarnby.filemanager.util.TreeNode
import com.jakebarnby.filemanager.workers.LoaderTask
import java.io.IOException
import javax.inject.Inject

/**
 * Created by Jake on 8/2/2017.
 */
/**
 * An asynchronous task that handles the Drive API call.
 * Placing the API calls in their own task ensures the UI stays responsive.
 */
class GoogleDriveLoaderTask(
    source: Source,
    presenter: SourceFragmentContract.Presenter,
    credential: GoogleAccountCredential?
) : LoaderTask(source, presenter) {

    @Inject
    lateinit var googleDriveClient: GoogleDriveClient

    init {
        GoogleDriveClient.client =
            Drive.Builder(
                NetHttpTransport(),
                JacksonFactory.getDefaultInstance(),
                credential
            ).setApplicationName("File Manager Android").build()
    }

    override fun initRootNode(path: String): Any? {
        var rootFile: File? = null
        try {
            rootFile = googleDriveClient.getFilesAtPath(path)
                ?: throw IOException("Failed to get root file")

            val rootSourceFile: SourceFile = GoogleDriveFile(rootFile)
            rootTreeNode = TreeNode(rootSourceFile)
            currentNode = rootTreeNode
            source.currentDirectory = rootTreeNode
            source.setQuotaInfo(googleDriveClient.storageInfo)
        } catch (e: IOException) {
            presenter.onLoadError(if (e.message != null) e.message else "")
            success = false
        }
        return rootFile
    }

    override fun readFileTree(rootObject: Any?): TreeNode<SourceFile> {
        if (rootObject != null && rootObject is File) {
            var fileList: FileList? = null
            try {
                fileList = googleDriveClient.getFilesByParentId(rootObject.id)
                    ?: throw IOException("Failed to get file")

            } catch (e: IOException) {
                presenter.onLoadError(if (e.message != null) e.message else "")
                success = false
            }
            val files = fileList!!.files
            var dirSize = 0L
            if (files != null) {
                for (file in files) {
                    val sourceFile: SourceFile = GoogleDriveFile(file)
                    if (sourceFile.isDirectory) {
                        currentNode.addChild(sourceFile)
                        currentNode = currentNode.children[currentNode.children.size - 1]
                        readFileTree(file)

                        currentNode.parent!!.data.size += currentNode.data.size
                        currentNode = currentNode.parent!!
                    } else {
                        if (file.getSize() != null) {
                            dirSize += file.getSize()
                        }
                        currentNode.addChild(sourceFile)
                    }
                }
                currentNode.data.size += dirSize
            }
        }
        return rootTreeNode
    }
}