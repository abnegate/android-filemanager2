package com.jakebarnby.filemanager.managers

import android.util.SparseArray
import com.jakebarnby.filemanager.models.FileAction
import com.jakebarnby.filemanager.models.Source
import java.io.Serializable
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by Jake on 7/29/2017.
 */
@Singleton
class SourceManager @Inject constructor() : Serializable {

    val sources: MutableList<Source> = mutableListOf()

    lateinit var activeSource: Source

    private val currentFileActions = SparseArray<FileAction>()

    fun getFileAction(operationId: Int): FileAction? {
        return currentFileActions[operationId]
    }

    fun addFileAction(operationId: Int, action: FileAction) {
        currentFileActions.put(operationId, action)
    }

    fun getSource(id: Int) = sources[id]

}