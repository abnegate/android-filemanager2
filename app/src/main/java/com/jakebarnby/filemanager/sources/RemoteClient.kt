package com.jakebarnby.filemanager.sources

import android.content.Context

interface RemoteClient {

//    fun getFilesAtPath(path: String): TFilesResult?
//    fun getExternalLink(path: String): String?

    fun authenticate(context: Context)

}