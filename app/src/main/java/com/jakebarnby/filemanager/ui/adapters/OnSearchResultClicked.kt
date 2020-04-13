package com.jakebarnby.filemanager.ui.adapters

import com.jakebarnby.filemanager.models.SourceFile
import com.jakebarnby.filemanager.util.TreeNode

@FunctionalInterface
interface OnSearchResultClicked {
    fun navigateToFile(fileNode: TreeNode<SourceFile>)
}