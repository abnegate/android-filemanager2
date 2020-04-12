package com.jakebarnby.filemanager.sources.local

import android.Manifest
import android.content.Context
import com.jakebarnby.filemanager.sources.models.Source
import com.jakebarnby.filemanager.sources.models.SourceType
import com.jakebarnby.filemanager.util.Constants.RequestCodes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor

/**
 * Created by jakebarnby on 2/08/17.
 */
class LocalSource(
    sourceName: String,
    private val rootPath: String?,
    listener: SourceListener
) : Source(SourceType.LOCAL, sourceName, listener) {

    override fun authenticate(context: Context) {
        sourceListener.onCheckPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, RequestCodes.STORAGE_PERMISSIONS)
    }

    override fun loadFiles(context: Context) {
        if (isFilesLoaded) {
            return
        }
        LocalLoaderTask(this, sourceListener)
            .executeOnExecutor(Dispatchers.IO.asExecutor(), rootPath)
    }

    override fun logout(context: Context) {}

}