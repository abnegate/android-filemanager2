package com.jakebarnby.filemanager.sources.local

import android.Manifest
import android.content.Context
import com.jakebarnby.filemanager.models.Source
import com.jakebarnby.filemanager.models.SourceConnectionType
import com.jakebarnby.filemanager.models.SourceType
import com.jakebarnby.filemanager.ui.sources.SourceFragmentContract
import com.jakebarnby.filemanager.util.Constants.RequestCodes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor

/**
 * Created by jakebarnby on 2/08/17.
 */
class LocalSource(
    private val rootPath: String?,
    private val presenter: SourceFragmentContract.Presenter
) : Source(
    SourceConnectionType.LOCAL,
    SourceType.LOCAL.id,
    presenter.prefsManager
) {

    override fun authenticate(context: Context) {
        presenter.onCheckPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, RequestCodes.STORAGE_PERMISSIONS)
    }

    override fun loadFiles(context: Context) {
        if (isFilesLoaded) {
            return
        }
        LocalLoaderTask(this, presenter)
            .executeOnExecutor(Dispatchers.IO.asExecutor(), rootPath)
    }

    override fun logout(context: Context) {}

}