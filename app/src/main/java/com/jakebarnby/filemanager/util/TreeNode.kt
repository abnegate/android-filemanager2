package com.jakebarnby.filemanager.util

import com.jakebarnby.filemanager.sources.models.SourceFile
import java.io.Serializable
import java.util.*

/**
 * Created by Jake on 5/31/2017.
 */
class TreeNode<T : Serializable>(var data: T) : Serializable {

    var parent: TreeNode<T>? = null
    var children: MutableList<TreeNode<T>> = mutableListOf()

    /**
     *
     * @param child
     */
    fun addChild(child: TreeNode<T>) {
        child.parent = this
        children.add(child)
    }

    /**
     *
     * @param data
     */
    fun addChild(data: T): TreeNode<T> {
        val newChild = TreeNode(data)
        newChild.parent = this
        children.add(newChild)
        return newChild
    }

    /**
     *
     * @param children
     */
    fun addChildren(children: List<TreeNode<T>>) {
        for (t in children) {
            t.parent = this
        }
        this.children.addAll(children)
    }

    fun replaceNode(toReplace: TreeNode<T>) {
        parent = toReplace.parent
        children = toReplace.children
    }

    fun removeChild(toRemove: TreeNode<T>) {
        children.remove(toRemove)
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        }
        return if (other is TreeNode<*>) {
            data == other.data
        } else {
            false
        }
    }

    override fun hashCode() =
        data.hashCode() * 3 / super.hashCode()

    companion object {
        /**
         * Recursively sort down the given node, first sorting by the given comparator then by file/directory
         * @param rootNode      Node to start the sort at
         * @param comparator    SourceFile comparator used for sorting the tree
         */
        fun sortTree(
            rootNode: TreeNode<out SourceFile>,
            comparator: Comparator<TreeNode<out SourceFile>>
        ) {
            Collections.sort(rootNode.children, comparator)
            for (child in rootNode.children) {
                if (child.data.isDirectory) {
                    sortTree(child, comparator)
                }
            }
        }

        /**
         * Recursively search up the given node for a direct parent with the given name
         * @param currentNode    The node to start search for parent at
         * @param parentToFind   Name of the node to find as a parent of currentNode
         * @return               Node with the given name which is a parent of the given node if it exists, otherwise null
         */
        tailrec fun searchForParent(
            currentNode: TreeNode<SourceFile>,
            parentToFind: String
        ): TreeNode<SourceFile>? {
            if (currentNode.parent == null) {
                return null
            }

            return if (currentNode.parent!!.data.name == parentToFind) {
                currentNode.parent
            } else {
                searchForParent(currentNode.parent!!, parentToFind)
            }
        }

        /**
         * Recursively search down the given node down depth first for children who's name contains the given name
         * @param currentNode   The node to start search for children at
         * @param childToFind   Name of the child node
         * @return              List of children of currentNode who's name contains childToFind, may be empty
         */
        fun searchForChildren(
            currentNode: TreeNode<SourceFile>,
            childToFind: String
        ): List<TreeNode<SourceFile>> {
            val results = mutableListOf<TreeNode<SourceFile>>()
            searchForChildren(currentNode, childToFind, results)
            return results
        }

        /**
         * Recursively search down the given down depth first for children who's name contains the given name
         * @param currentNode   The node to start search for children at
         * @param childToFind   Name of the child node
         * @param results       Reference to the list to add results to
         */
        private fun searchForChildren(
            currentNode: TreeNode<SourceFile>,
            childToFind: String,
            results: MutableList<TreeNode<SourceFile>>) {

            if (currentNode.data.name.contains(childToFind, true)) {
                results.add(currentNode)
            }

            for (child in currentNode.children) {
                if (child.data.isDirectory) {
                    searchForChildren(child, childToFind, results)
                } else if (child.data.name.contains(childToFind, true)) {
                    results.add(child)
                }
            }
        }
    }
}