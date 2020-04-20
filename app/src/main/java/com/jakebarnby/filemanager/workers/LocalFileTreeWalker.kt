package com.jakebarnby.filemanager.workers

import android.content.Context
import androidx.work.WorkerParameters
import com.jakebarnby.filemanager.models.SourceFile
import com.jakebarnby.filemanager.models.sources.local.LocalFile
import java.io.File
import java.util.*

class LocalFileTreeWalker(context: Context, params: WorkerParameters)
    : FileTreeWalkerWorker<File>(context, params) {

    override suspend fun getRootNode(rootPath: String): File? {
        return File(rootPath)
    }

    override suspend fun readFileTree(rootNode: File?) {
        if (rootNode == null) {
            throw NullPointerException("Root file was null")
        }

        val nodesToAdd = mutableSetOf<SourceFile>()
        val rootSourceFile = LocalFile(rootNode)

        nodesToAdd += rootSourceFile

        val children = Stack<File>().apply {
            addAll(rootNode.listFiles() ?: return)
        }

        var currentDirectory = rootSourceFile
        while (children.isNotEmpty()) {
            val child = children.pop()
            val newNode = LocalFile(child)
            nodesToAdd += newNode

            if (!child.isDirectory) {
                continue
            }

            currentDirectory = newNode
            children.addAll(child.listFiles() ?: emptyArray())
        }

        db.fileDao().insertAll(nodesToAdd)
    }
}