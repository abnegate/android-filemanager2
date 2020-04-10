package com.jakebarnby.filemanager.sources

import com.jakebarnby.filemanager.sources.models.SourceFile
import com.jakebarnby.filemanager.util.TreeNode

interface SourceListener {
    fun onCheckPermissions(permission: String, requestCode: Int)
    fun onNoConnection()
    fun onLoadStarted()
    fun onLoadAborted()
    fun onLoadError(errorMessage: String?)
    fun onLoadComplete(rootFile: TreeNode<SourceFile>)
    fun onLogout()
}