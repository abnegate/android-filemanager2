package com.jakebarnby.filemanager.sources.onedrive

import com.jakebarnby.filemanager.workers.LoaderTask
import com.jakebarnby.filemanager.models.Source
import com.jakebarnby.filemanager.models.SourceFile
import com.jakebarnby.filemanager.sources.dropbox.DropboxClient
import com.jakebarnby.filemanager.ui.sources.SourceFragmentContract
import com.jakebarnby.filemanager.util.TreeNode
import com.microsoft.graph.extensions.DriveItem
import com.microsoft.graph.http.GraphServiceException
import javax.inject.Inject

/**
 * Created by Jake on 8/2/2017.
 */
class OneDriveLoaderTask(
    source: Source,
    presenter: SourceFragmentContract.Presenter,
    private val rootDriveItem: DriveItem
) : LoaderTask(source, presenter) {

    @Inject
    lateinit var oneDriveClient: OneDriveClient

    override fun initRootNode(path: String): Any? {
        val rootSourceFile: SourceFile = OneDriveFile(rootDriveItem)
        rootSourceFile.isDirectory = true
        rootTreeNode = TreeNode(rootSourceFile)
        currentNode = rootTreeNode
        source.currentDirectory = rootTreeNode
        source.setQuotaInfo(oneDriveClient.storageInfo)
        return rootDriveItem
    }

    override fun readFileTree(rootObject: Any?): TreeNode<SourceFile> {
        try {
            if (rootObject == null || rootObject !is DriveItem) {
                return rootTreeNode
            }

            var dirSize = 0L
            var items = oneDriveClient.getFilesByParentId(rootObject.id)

            if (items?.currentPage == null) {
                return rootTreeNode
            }

            while (items != null) {
                val pageItems = items.currentPage

                for (file in pageItems) {
                    val sourceFile = OneDriveFile(file)
                    if (file.folder == null) {
                        dirSize += file.size
                        currentNode.addChild(sourceFile)
                        continue
                    }

                    currentNode.addChild(sourceFile)
                    currentNode = currentNode.children[currentNode.children.size - 1]
                    readFileTree(file)

                    currentNode.parent!!.data.size += currentNode.data.size
                    currentNode = currentNode.parent!!
                }
                items = oneDriveClient.getNextPage(items)
            }
            currentNode.data.size += dirSize
        } catch (e: GraphServiceException) {
            presenter.onLoadError(if (e.message != null) e.message else "")
            success = false
        }
        return rootTreeNode
    }

}