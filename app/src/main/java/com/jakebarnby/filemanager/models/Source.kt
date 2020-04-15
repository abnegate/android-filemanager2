package com.jakebarnby.filemanager.models

import android.content.Context
import com.jakebarnby.filemanager.managers.PreferenceManager
import com.jakebarnby.filemanager.util.Constants
import com.jakebarnby.filemanager.util.TreeNode
import java.io.Serializable

/**
 * Created by Jake on 7/29/2017.
 */
abstract class Source(
    var sourceConnectionType: SourceConnectionType,
    val sourceId: Int,
    protected val prefsManager: PreferenceManager
) : Serializable {

    lateinit var rootNode: TreeNode<SourceFile>
    lateinit var currentDirectory: TreeNode<SourceFile>

    var totalSpace: Long = 0
    var usedSpace: Long = 0
    var freeSpace: Long = 0
    var isLoggedIn = false
    var isFilesLoaded = false
    var isMultiSelectEnabled = false

    abstract fun authenticate(context: Context)
    abstract fun loadFiles(context: Context)
    abstract fun logout(context: Context)

    fun setQuotaInfo(info: StorageInfo?) {
        totalSpace += info?.totalSpace ?: 0
        usedSpace += info?.usedSpace ?: 0
        freeSpace += info?.freeSpace ?: 0
    }

    val totalSpaceGB: Double
        get() = totalSpace / Constants.BYTES_TO_GIGABYTE

    val usedSpaceGB: Double
        get() = usedSpace / Constants.BYTES_TO_GIGABYTE

    val usedSpacePercent: Int
        get() = (100 * usedSpace / totalSpace).toInt()

    val freeSpaceGB: Double
        get() = freeSpace / Constants.BYTES_TO_GIGABYTE

    fun decreaseFreeSpace(amount: Long) {
        freeSpace -= amount
    }

    fun increaseFreeSpace(amount: Long) {
        freeSpace += amount
    }
}