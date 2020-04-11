package com.jakebarnby.filemanager.sources.models

import android.content.Context
import com.jakebarnby.filemanager.sources.SourceListener
import com.jakebarnby.filemanager.util.Constants
import com.jakebarnby.filemanager.util.TreeNode
import com.jakebarnby.filemanager.util.Utils

/**
 * Created by Jake on 7/29/2017.
 */
abstract class Source(
    var sourceType: SourceType,
    val sourceName: String,
    protected var sourceListener: SourceListener
) {

    lateinit var rootNode: TreeNode<SourceFile>
    lateinit var currentDirectory: TreeNode<SourceFile>

    var totalSpace: Long = 0
    var usedSpace: Long = 0
    var freeSpace: Long = 0
    var isLoggedIn = false
    var isFilesLoaded = false
    var isMultiSelectEnabled = false

    /**
     * Authenticate the current mSource
     */
    abstract fun authenticateSource(context: Context)

    /**
     * Load the current mSource
     */
    abstract fun loadSource(context: Context)

    abstract fun logout(context: Context)

    fun setQuotaInfo(info: SourceStorageStats?) {
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

    /**
     * Checks if this mSource has a valid access token
     * @return  Whether there is a valid access token for this mSource
     */
    fun hasToken(
        context: Context,
        sourceName: String
    ): Boolean =
        Preferences.getString(
            context,
            sourceName.toLowerCase() + "-access-token",
            null
        ) != null

    /**
     * If performing an action on a non-local directory, check internet
     */
    fun checkConnectionActive(context: Context): Boolean {
        if (sourceType == SourceType.REMOTE) {
            if (!Utils.isConnectionReady(context)) {
                sourceListener.onNoConnection()
                return false
            }
        }
        return true
    }
}