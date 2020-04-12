package com.jakebarnby.filemanager.sources.dropbox

import com.dropbox.core.DbxException
import com.dropbox.core.v2.files.FileMetadata
import com.dropbox.core.v2.files.FolderMetadata
import com.dropbox.core.v2.files.ListFolderResult
import com.dropbox.core.v2.files.Metadata
import com.jakebarnby.filemanager.sources.LoaderTask
import com.jakebarnby.filemanager.sources.models.Source
import com.jakebarnby.filemanager.sources.models.SourceFile
import com.jakebarnby.filemanager.util.TreeNode

/**
 * Created by Jake on 8/2/2017.
 */
class DropboxLoaderTask(
    source: Source,
    listener: SourceListener
) : LoaderTask(source, listener) {

    override fun initRootNode(path: String): Any? {
        val rootSourceFile: SourceFile = DropboxFile(Metadata(path))
        rootSourceFile.isDirectory = true
        rootTreeNode = TreeNode(rootSourceFile)
        currentNode = rootTreeNode
        source.currentDirectory = rootTreeNode
        source.setQuotaInfo(DropboxFactory.storageStats)
        var result: ListFolderResult? = null
        try {
            result = DropboxFactory.client
                ?.files()
                ?.listFolderBuilder(path)
                ?.start()
        } catch (e: DbxException) {
            sourceListener.onLoadError(if (e.message != null) e.message else "")
            success = false
        }
        return result
    }

    override fun readFileTree(rootObject: Any?): TreeNode<SourceFile> {
        if (rootObject == null || rootObject !is ListFolderResult) {
            return rootTreeNode
        }

        var dirSize = 0L
        for (data in rootObject.entries) {
            val sourceFile: SourceFile = DropboxFile(data)
            try {
                if (!sourceFile.isDirectory) {
                    DropboxFactory.client
                        ?.files()
                        ?.getTemporaryLink(sourceFile.path)
                        ?.link
                        ?.let {
                            sourceFile.thumbnailLink = it
                        }
                }
            } catch (e: DbxException) {
                sourceListener.onLoadError(if (e.message != null) e.message else "")
                success = false
            }
            if (data is FolderMetadata) {
                currentNode.addChild(sourceFile)
                currentNode = currentNode.children[currentNode.children.size - 1]
                try {
                    readFileTree(
                        DropboxFactory.client
                            ?.files()
                            ?.listFolder(data.getPathLower())
                    )
                } catch (e: DbxException) {
                    sourceListener.onLoadError(if (e.message != null) e.message else "")
                }

                if (currentNode.parent != null) {
                    currentNode.parent!!.data.addSize(currentNode.data.size)
                    currentNode = currentNode.parent!!
                }
            } else {
                dirSize += (data as FileMetadata).size
                currentNode.addChild(sourceFile)
            }
        }
        currentNode.data.addSize(dirSize)

        return rootTreeNode
    }
}