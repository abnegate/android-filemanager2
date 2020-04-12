package com.jakebarnby.filemanager.managers

import android.util.SparseArray
import com.jakebarnby.filemanager.sources.models.SourceFile
import com.jakebarnby.filemanager.util.TreeNode
import java.util.*

/**
 * Created by Jake on 6/6/2017.
 */
object SelectedFilesManager {

    private val selectedFilesMap = SparseArray<MutableList<TreeNode<SourceFile>>>()
    private val actionableDirectories = SparseArray<TreeNode<SourceFile>>()

    val currentSelectedFiles: MutableList<TreeNode<SourceFile>>
        get() = selectedFilesMap[operationCount - 1]

    val operationCount: Int
        get() = selectedFilesMap.size()

    val currentCopySize: Long
        get() {
            var copySize: Long = 0
            for (file in currentSelectedFiles) {
                copySize += file.data.size
            }
            return copySize
        }

    fun getSelectedFiles(operationId: Int): MutableList<TreeNode<SourceFile>>? {
        return selectedFilesMap[operationId - 1]
    }

    fun getActionableDirectory(operationId: Int): TreeNode<SourceFile>? {
        return actionableDirectories[operationId]
    }

    fun startNewSelection() {
        selectedFilesMap.put(operationCount, ArrayList())
    }

    fun addActionableDirectory(
        operationId: Int,
        actionableDir: TreeNode<SourceFile>
    ) {
        actionableDirectories.put(operationId, actionableDir)
    }
}