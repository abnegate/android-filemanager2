package com.jakebarnby.filemanager.workers

import android.content.Context
import androidx.work.WorkerParameters
import com.dropbox.core.v2.files.Metadata
import com.jakebarnby.filemanager.models.SourceFile
import com.jakebarnby.filemanager.models.sources.dropbox.DropboxFile
import com.jakebarnby.filemanager.models.sources.dropbox.DropboxSource
import javax.inject.Inject

class DropBoxFileTreeWalker(context: Context, params: WorkerParameters)
    : FileTreeWalkerWorker<String>(context, params) {

    @Inject
    lateinit var client: DropboxSource

    override suspend fun getRootNode(rootPath: String): String? = ""

    override suspend fun readFileTree(rootNode: String?) {
        if (rootNode == null) {
            throw NullPointerException("Root file was null")
        }

        val nodesToAdd = mutableSetOf<SourceFile>()
        val rootSourceFile = DropboxFile(Metadata(rootNode))

        nodesToAdd += rootSourceFile

        val files = client.getFilesAtPath(rootNode, true)

        files?.forEach {
            nodesToAdd += DropboxFile(it).apply {
                thumbnailLink = client.getExternalLink(it.pathDisplay)
                    ?: return@apply
            }
        }

        db.fileDao().insertAll(nodesToAdd)
    }
}
