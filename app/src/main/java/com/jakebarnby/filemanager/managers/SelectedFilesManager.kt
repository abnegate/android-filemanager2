package com.jakebarnby.filemanager.managers

import android.util.SparseArray
import com.jakebarnby.filemanager.models.SourceFile
import com.jakebarnby.filemanager.util.TreeNode
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by Jake on 6/6/2017.
 */
@Singleton
class SelectedFilesManager @Inject constructor() {

    private val selectedFilesMap = SparseArray<MutableList<SourceFile>>()
    private val actionableDirectories = SparseArray<SourceFile>()

    val operationCount: Int
        get() = selectedFilesMap.size()

    val currentSelectedFiles: List<SourceFile>
        get() = selectedFilesMap[operationCount - 1]

    val currentSelectionSize: Long
        get() {
            var copySize: Long = 0
            for (file in currentSelectedFiles) {
                copySize += file.size
            }
            return copySize
        }

    fun getSelectedFiles(operationId: Int): List<SourceFile>? {
        return selectedFilesMap[operationId - 1]
    }

    fun addToSelection(operationId: Int, file: SourceFile) {
        selectedFilesMap[operationId - 1].add(file)
    }

    fun addToCurrentSelection(file: SourceFile) {
        selectedFilesMap[operationCount - 1].add(file)
    }

    fun removeFromSelection(operationId: Int, file: SourceFile) {
        selectedFilesMap[operationId - 1].remove(file)
    }

    fun removeFromCurrentSelection(file: SourceFile) {
        selectedFilesMap[operationCount - 1].remove(file)
    }

    fun clearCurrentSelection() {
        selectedFilesMap[operationCount - 1].clear()
    }

    fun getActionableDirectory(operationId: Int): SourceFile? {
        return actionableDirectories[operationId]
    }

    fun startNewSelection() {
        selectedFilesMap.put(operationCount, ArrayList())
    }

    fun addActionableDirectory(
        operationId: Int,
        actionableDir: SourceFile
    ) {
        actionableDirectories.put(operationId, actionableDir)
    }
}