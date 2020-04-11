package com.jakebarnby.filemanager.sources.models

import android.util.SparseArray
import com.jakebarnby.filemanager.models.FileAction
import com.jakebarnby.filemanager.util.TreeNode

/**
 * Created by Jake on 7/29/2017.
 */
class SourceManager(
    val sources: MutableList<Source> = mutableListOf()
) {

    lateinit var activeSource: Source

    private val currentFileActions = SparseArray<FileAction>()

    fun getFileAction(operationId: Int): FileAction? {
        return currentFileActions[operationId]
    }

    fun addFileAction(operationId: Int, action: FileAction) {
        currentFileActions.put(operationId, action)
    }

}