package com.jakebarnby.filemanager.sources.local

import com.jakebarnby.filemanager.workers.LoaderTask
import com.jakebarnby.filemanager.models.Source
import com.jakebarnby.filemanager.models.SourceFile
import com.jakebarnby.filemanager.ui.sources.SourceFragmentContract
import com.jakebarnby.filemanager.util.TreeNode
import com.jakebarnby.filemanager.util.Utils
import java.io.File

/**
 * Created by Jake on 8/2/2017.
 */
class LocalLoaderTask(
    source: Source,
    presenter: SourceFragmentContract.Presenter
) : LoaderTask(source, presenter) {

    override fun initRootNode(path: String): Any? {
        val rootFile = File(path)
        val rootSourceFile: SourceFile = LocalFile(rootFile, source.sourceId)
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
                val sourceFile: SourceFile = LocalFile(file, source.sourceId)
                if (file.isDirectory) {
                    currentNode.addChild(sourceFile)
                    currentNode = currentNode.children[currentNode.children.size - 1]
                    readFileTree(file)

                    currentNode.parent!!.data.size += currentNode.data.size
                    currentNode = currentNode.parent!!
                } else {
                    dirSize += file.length()
                    currentNode.addChild(sourceFile)
                }
            }
            currentNode.data.size += dirSize
        }
        return rootTreeNode
    }
}