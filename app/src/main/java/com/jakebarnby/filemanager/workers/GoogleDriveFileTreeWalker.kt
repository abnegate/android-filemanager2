package com.jakebarnby.filemanager.workers

import android.content.Context
import androidx.work.WorkerParameters
import com.google.api.services.drive.model.File
import com.jakebarnby.filemanager.models.SourceFile
import com.jakebarnby.filemanager.models.sources.googledrive.GoogleDriveFile
import com.jakebarnby.filemanager.models.sources.googledrive.GoogleDriveSource
import java.util.*
import javax.inject.Inject

class GoogleDriveFileTreeWalker(context: Context, params: WorkerParameters)
    : FileTreeWalkerWorker<File>(context, params) {

    @Inject
    lateinit var client: GoogleDriveSource

    override suspend fun getRootNode(rootPath: String): File? =
        client.getRootFile()

    override suspend fun readFileTree(rootNode: File?) {
        if (rootNode == null) {
            throw NullPointerException("Root file was null")
        }

        val nodesToAdd = mutableSetOf<SourceFile>()
        val rootSourceFile = GoogleDriveFile(rootNode)

        nodesToAdd += rootSourceFile

        val children = Stack<File>().apply {
            addAll(client.getFilesByParentId(rootNode.id)?.files ?: return)
        }

        var currentDirectory = rootSourceFile
        while (children.isNotEmpty()) {
            val child = children.pop()
            val newNode = GoogleDriveFile(child).apply {
                fileId = nodesToAdd.size.toLong()
                parentFileId = currentDirectory.fileId
            }
            nodesToAdd += newNode

            if (!newNode.isDirectory) {
                continue
            }

            currentDirectory = newNode
            children.addAll(client.getFilesByParentId(child.id)?.files ?: emptyList())
        }

        db.fileDao().insertAll(nodesToAdd)
    }
}