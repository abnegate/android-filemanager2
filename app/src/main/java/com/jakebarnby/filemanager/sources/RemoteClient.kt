package com.jakebarnby.filemanager.sources

interface RemoteClient<TFilesResult> {

    fun getFilesAtPath(path: String): TFilesResult?
    fun getExternalLink(path: String): String?

}