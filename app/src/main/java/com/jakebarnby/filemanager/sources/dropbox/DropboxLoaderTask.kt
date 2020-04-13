package com.jakebarnby.filemanager.sources.dropbox

import com.dropbox.core.DbxException
import com.dropbox.core.v2.files.FileMetadata
import com.dropbox.core.v2.files.FolderMetadata
import com.dropbox.core.v2.files.ListFolderResult
import com.dropbox.core.v2.files.Metadata
import com.jakebarnby.filemanager.models.Source
import com.jakebarnby.filemanager.models.SourceFile
import com.jakebarnby.filemanager.ui.sources.SourceFragmentContract
import com.jakebarnby.filemanager.util.TreeNode
import com.jakebarnby.filemanager.workers.LoaderTask
import javax.inject.Inject

/**
 * Created by Jake on 8/2/2017.
 */
class DropboxLoaderTask(
    source: Source,
    presenter: SourceFragmentContract.Presenter
) : LoaderTask(source, presenter) {

    @Inject
    lateinit var dropboxClient: DropboxClient

    override fun initRootNode(path: String): Any? {
        val rootSourceFile: SourceFile = DropboxFile(Metadata(path))
        rootSourceFile.isDirectory = true
        rootTreeNode = TreeNode(rootSourceFile)
        currentNode = rootTreeNode
        source.currentDirectory = rootTreeNode
        source.setQuotaInfo(dropboxClient.storageInfo)
        var result: ListFolderResult? = null
        try {
            result = dropboxClient.getFilesAtPath(path)

        } catch (e: DbxException) {
            presenter.onLoadError(if (e.message != null) e.message else "")
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
                    dropboxClient.getExternalLink(sourceFile.path)?.let {
                        sourceFile.thumbnailLink = it
                    }
                }
            } catch (e: DbxException) {
                presenter.onLoadError(if (e.message != null) e.message else "")
                success = false
            }
            if (data is FolderMetadata) {
                currentNode.addChild(sourceFile)
                currentNode = currentNode.children[currentNode.children.size - 1]
                try {
                    readFileTree(
                        dropboxClient.getFilesAtPath(data.getPathLower())
                    )
                } catch (e: DbxException) {
                    presenter.onLoadError(if (e.message != null) e.message else "")
                }

                if (currentNode.parent != null) {
                    currentNode.parent!!.data.size += currentNode.data.size
                    currentNode = currentNode.parent!!
                }
            } else {
                dirSize += (data as FileMetadata).size
                currentNode.addChild(sourceFile)
            }
        }
        currentNode.data.size += dirSize

        return rootTreeNode
    }
}