package com.jakebarnby.filemanager.sources.local

import com.jakebarnby.filemanager.sources.LoaderTask
import com.jakebarnby.filemanager.sources.models.Source
import com.jakebarnby.filemanager.sources.models.SourceFile
import com.jakebarnby.filemanager.util.TreeNode
import com.jakebarnby.filemanager.util.Utils
import java.io.File

/**
 * Created by Jake on 8/2/2017.
 */
class LocalLoaderTask(
    source: Source, listener: SourceListener
) : LoaderTask(source, listener) {

    override fun initRootNode(path: String): Any? {
        val rootFile = File(path)
        val rootSourceFile: SourceFile = LocalFile(rootFile, source.sourceName)
        rootTreeNode = TreeNode(rootSourceFile)
        currentNode = rootTreeNode
        source.currentDirectory = rootTreeNode
        source.setQuotaInfo(Utils.getStorageStats(File(path)))
        return rootFile
    }

    override fun readFileTree(rootObject: Any?): TreeNode<SourceFile> {
        if (rootObject == null || rootObject !is File) {
            return rootTreeNode
        }
        val listFile = rootObject.listFiles()
        var dirSize = 0L
        if (listFile != null && listFile.isNotEmpty()) {
            for (file in listFile) {
                val sourceFile: SourceFile = LocalFile(file, source.sourceName)
                if (file.isDirectory) {
                    currentNode.addChild(sourceFile)
                    currentNode = currentNode.children[currentNode.children.size - 1]
                    readFileTree(file)

                    currentNode.parent?.data?.addSize(currentNode.data.size)
                    currentNode = currentNode.parent!!
                } else {
                    dirSize += file.length()
                    currentNode.addChild(sourceFile)
                }
            }
            currentNode.data.addSize(dirSize)
        }
        return rootTreeNode
    }
}