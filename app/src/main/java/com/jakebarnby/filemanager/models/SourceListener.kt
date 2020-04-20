package com.jakebarnby.filemanager.models

interface SourceListener {
    fun onCheckForToken()
    fun onCheckPermissions(name: String, requestCode: Int)
    fun onConnect()
    fun onNoConnection()
    fun onLoadStarted()
    fun onLoadAborted()
    fun onLoadError(errorMessage: String?)
    fun onLoadComplete(rootFile: SourceFile)
    fun onLogout()
}