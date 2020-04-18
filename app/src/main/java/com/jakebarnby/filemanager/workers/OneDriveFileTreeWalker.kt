package com.jakebarnby.filemanager.workers

import android.content.Context
import androidx.work.WorkerParameters
import com.jakebarnby.filemanager.models.SourceFile
import com.jakebarnby.filemanager.models.sources.onedrive.OneDriveFile
import com.jakebarnby.filemanager.models.sources.onedrive.OneDriveSource
import com.microsoft.graph.extensions.DriveItem
import java.util.*
import javax.inject.Inject

class OneDriveFileTreeWalker(context: Context, params: WorkerParameters)
    : FileTreeWalkerWorker<DriveItem>(context, params) {

    @Inject
    lateinit var client: OneDriveSource

    override suspend fun getRootNode(rootPath: String): DriveItem? =
        client.getRootFile()

    override suspend fun readFileTree(rootNode: DriveItem?) {
        if (rootNode == null) {
            throw NullPointerException("Root file was null")
        }

        val nodesToAdd = mutableSetOf<SourceFile>()
        val rootSourceFile = OneDriveFile(rootNode)

        nodesToAdd += rootSourceFile

        val children = Stack<DriveItem>().apply {
            addAll(client.getFilesByParentId(rootNode.id) ?: return)
        }

        var currentDirectory = rootSourceFile
        while (children.isNotEmpty()) {
            val child = children.pop()
            val newNode = OneDriveFile(child).apply {
                fileId = nodesToAdd.size.toLong()
                parentFileId = currentDirectory.fileId
            }
            nodesToAdd += newNode

            if (!newNode.isDirectory) {
                continue
            }

            currentDirectory = newNode
            children.addAll(client.getFilesByParentId(child.id) ?: emptyList())
        }

        db.fileDao().insertAll(nodesToAdd)
    }
}