package com.jakebarnby.filemanager.sources.onedrive

import com.jakebarnby.filemanager.sources.LoaderTask
import com.jakebarnby.filemanager.sources.SourceListener
import com.jakebarnby.filemanager.sources.models.Source
import com.jakebarnby.filemanager.sources.models.SourceFile
import com.jakebarnby.filemanager.util.TreeNode
import com.microsoft.graph.extensions.DriveItem
import com.microsoft.graph.http.GraphServiceException

/**
 * Created by Jake on 8/2/2017.
 */
class OneDriveLoaderTask(
    source: Source,
    listener: SourceListener,
    private val rootDriveItem: DriveItem
) : LoaderTask(source, listener) {

    override fun initRootNode(path: String): Any? {
        val rootSourceFile: SourceFile = OneDriveFile(rootDriveItem)
        rootSourceFile.isDirectory = true
        rootTreeNode = TreeNode(rootSourceFile)
        currentNode = rootTreeNode
        source.currentDirectory = rootTreeNode
        source.setQuotaInfo(OneDriveFactory.storageStats)
        return rootDriveItem
    }

    override fun readFileTree(rootObject: Any?): TreeNode<SourceFile> {
        try {
            if (rootObject == null || rootObject !is DriveItem) {
                return rootTreeNode
            }

            val items = OneDriveFactory.service
                ?.me
                ?.drive
                ?.getItems(rootObject.id)
                ?.children
                ?.buildRequest()
                ?.select("id,name,webUrl,folder,size,createdDateTime,lastModifiedDateTime")
                ?.expand("thumbnails")
                ?.get()

            val pageItems = items?.currentPage
            var dirSize = 0L

            if (pageItems == null) {
                return rootTreeNode
            }
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
                currentNode.parent?.data?.addSize(currentNode.data.size)
                currentNode = currentNode.parent!!
            }
            currentNode.data.addSize(dirSize)
        } catch (e: GraphServiceException) {
            sourceListener.onLoadError(if (e.message != null) e.message else "")
            success = false
        }
        return rootTreeNode
    }

}