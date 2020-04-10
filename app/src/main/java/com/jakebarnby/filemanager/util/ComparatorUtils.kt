package com.jakebarnby.filemanager.util

import android.content.Context
import com.jakebarnby.filemanager.sources.models.SourceFile
import com.jakebarnby.filemanager.util.Constants.OrderTypes
import com.jakebarnby.filemanager.util.Constants.Prefs
import com.jakebarnby.filemanager.util.Constants.SortTypes
import java.util.*

/**
 * Created by Jake on 10/3/2017.
 */
object ComparatorUtils {

    fun resolveComparatorForPrefs(context: Context): Comparator<TreeNode<out SourceFile>> {
        val showFoldersFirst = PreferenceUtils.getBoolean(
                context,
                Prefs.FOLDER_FIRST_KEY,
                true
        )
        val sortType = PreferenceUtils.getInt(
                context,
                Prefs.SORT_TYPE_KEY,
                SortTypes.NAME
        )
        val orderType = PreferenceUtils.getInt(
                context,
                Prefs.ORDER_TYPE_KEY,
                SortTypes.NAME
        )

        return when (sortType) {
            SortTypes.NAME ->
                getComparatorNameForOrder(orderType, showFoldersFirst)
            SortTypes.MODIFIED_TIME ->
                getComparatorTimeForOrder(orderType, showFoldersFirst)
            SortTypes.SIZE ->
                getComparatorSizeForOrder(orderType, showFoldersFirst)
            SortTypes.TYPE ->
                getComparatorTypeForOrder(orderType, showFoldersFirst)
            else ->
                getComparatorNameForOrder(orderType, showFoldersFirst)
        }
    }

    private fun getComparatorNameForOrder(orderType: Int, showFoldersFirst: Boolean): Comparator<TreeNode<out SourceFile>> {
        return if (orderType == OrderTypes.ASCENDING) {
            Comparator { node1, node2 ->
                if (showFoldersFirst) {
                    var result = (!node1.data.isDirectory).compareTo(!node2.data.isDirectory)
                    if (result == 0) {
                        result = node1.data.name.compareTo(node2.data.name, true)
                    }
                    return@Comparator result
                } else {
                    return@Comparator node1.data.name.compareTo(node2.data.name, true)
                }
            }
        } else {
            Comparator { node1: TreeNode<out SourceFile>, node2: TreeNode<out SourceFile> ->
                if (showFoldersFirst) {
                    var result = (!node1.data.isDirectory).compareTo(!node2.data.isDirectory)
                    if (result == 0) {
                        result = node2.data.name.compareTo(node1.data.name, true)
                    }
                    return@Comparator result
                } else {
                    return@Comparator node2.data.name.compareTo(node1.data.name, true)
                }
            }
        }
    }

    private fun getComparatorTimeForOrder(orderType: Int, showFoldersFirst: Boolean): Comparator<TreeNode<out SourceFile>> {
        return if (orderType == OrderTypes.ASCENDING) {
            Comparator { node1: TreeNode<out SourceFile>, node2: TreeNode<out SourceFile> ->
                if (showFoldersFirst) {
                    var result = (!node1.data.isDirectory).compareTo(!node2.data.isDirectory)
                    if (result == 0) {
                        result = node1.data.modifiedTime.compareTo(node2.data.modifiedTime)
                    }
                    return@Comparator result
                } else {
                    return@Comparator node1.data.modifiedTime.compareTo(node2.data.modifiedTime)
                }
            }
        } else {
            Comparator { node1: TreeNode<out SourceFile>, node2: TreeNode<out SourceFile> ->
                if (showFoldersFirst) {
                    var result = (!node1.data.isDirectory).compareTo(!node2.data.isDirectory)
                    if (result == 0) {
                        result = node2.data.modifiedTime.compareTo(node1.data.modifiedTime)
                    }
                    return@Comparator result
                } else {
                    return@Comparator node2.data.modifiedTime.compareTo(node1.data.modifiedTime)
                }
            }
        }
    }

    private fun getComparatorSizeForOrder(orderType: Int, showFoldersFirst: Boolean): Comparator<TreeNode<out SourceFile>> {
        return if (orderType == OrderTypes.ASCENDING) {
            Comparator { node1: TreeNode<out SourceFile>, node2: TreeNode<out SourceFile> ->
                if (showFoldersFirst) {
                    var result = (!node1.data.isDirectory).compareTo(!node2.data.isDirectory)
                    if (result == 0) {
                        result = node1.data.size.compareTo(node2.data.size)
                    }
                    return@Comparator result
                } else {
                    return@Comparator node1.data.size.compareTo(node2.data.size)
                }
            }
        } else {
            Comparator { node1: TreeNode<out SourceFile>, node2: TreeNode<out SourceFile> ->
                if (showFoldersFirst) {
                    var result = (!node1.data.isDirectory).compareTo(!node2.data.isDirectory)
                    if (result == 0) {
                        result = node2.data.size.compareTo(node1.data.size)
                    }
                    return@Comparator result
                } else {
                    return@Comparator node2.data.size.compareTo(node1.data.size)
                }
            }
        }
    }

    private fun getComparatorTypeForOrder(orderType: Int, showFoldersFirst: Boolean): Comparator<TreeNode<out SourceFile>> {
        return if (orderType == OrderTypes.ASCENDING) {
            Comparator { node1: TreeNode<out SourceFile>, node2: TreeNode<out SourceFile> ->
                if (showFoldersFirst) {
                    var result = (!node1.data.isDirectory).compareTo(!node2.data.isDirectory)
                    if (result == 0) {
                        result = node1.data.size.compareTo(node2.data.size)
                    }
                    return@Comparator result
                } else {
                    return@Comparator node1.data.size.compareTo(node2.data.size)
                }
            }
        } else {
            Comparator { node1: TreeNode<out SourceFile>, node2: TreeNode<out SourceFile> ->
                if (showFoldersFirst) {
                    var result = (!node1.data.isDirectory).compareTo(!node2.data.isDirectory)
                    if (result == 0) {
                        result = node2.data.size.compareTo(node1.data.size)
                    }
                    return@Comparator result
                } else {
                    return@Comparator node2.data.size.compareTo(node1.data.size)
                }
            }
        }
    }
}