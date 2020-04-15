package com.jakebarnby.filemanager.util

import com.jakebarnby.filemanager.managers.PreferenceManager
import com.jakebarnby.filemanager.models.OrderType
import com.jakebarnby.filemanager.models.SortType
import com.jakebarnby.filemanager.models.SourceFile
import com.jakebarnby.filemanager.util.Constants.Prefs
import java.util.*

/**
 * Created by Jake on 10/3/2017.
 */
object Comparators {

    fun resolveComparatorForPrefs(prefs: PreferenceManager): Comparator<TreeNode<out SourceFile>> {
        val showFoldersFirst = prefs.getBoolean(Prefs.FOLDER_FIRST_KEY, true)
        val sortType = prefs.getInt(Prefs.SORT_TYPE_KEY, SortType.NAME.value)
        val orderType = prefs.getInt(Prefs.ORDER_TYPE_KEY, SortType.NAME.value)

        return when (sortType) {
            SortType.NAME.value ->
                getComparatorNameForOrder(orderType, showFoldersFirst)
            SortType.MODIFIED_TIME.value ->
                getComparatorTimeForOrder(orderType, showFoldersFirst)
            SortType.SIZE.value ->
                getComparatorSizeForOrder(orderType, showFoldersFirst)
            SortType.TYPE.value ->
                getComparatorTypeForOrder(orderType, showFoldersFirst)
            else ->
                getComparatorNameForOrder(orderType, showFoldersFirst)
        }
    }

    private fun getComparatorNameForOrder(orderType: Int, showFoldersFirst: Boolean): Comparator<TreeNode<out SourceFile>> {
        return if (orderType == OrderType.ASCENDING.value) {
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
        return if (orderType == OrderType.ASCENDING.value) {
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
        return if (orderType == OrderType.ASCENDING.value) {
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
        return if (orderType == OrderType.ASCENDING.value) {
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