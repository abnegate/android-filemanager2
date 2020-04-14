package com.jakebarnby.filemanager.sources.local

import android.Manifest
import android.os.Bundle
import androidx.core.os.bundleOf
import com.jakebarnby.filemanager.models.SourceConnectionType
import com.jakebarnby.filemanager.models.SourceType
import com.jakebarnby.filemanager.ui.sources.SourceFragment
import com.jakebarnby.filemanager.util.Constants
import com.jakebarnby.filemanager.util.Constants.RequestCodes

/**
 * Created by Jake on 5/31/2017.
 */
class LocalFragment : SourceFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val name = arguments?.getString(Constants.FRAGMENT_TITLE) ?: ""
        val rootPath = arguments?.getString(Constants.LOCAL_ROOT) ?: "/"

        presenter.setFileSource( LocalSource(rootPath, presenter))
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (presenter.source.sourceConnectionType == SourceConnectionType.LOCAL) {
            presenter.onCheckPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, RequestCodes.STORAGE_PERMISSIONS)
        }
    }

    companion object {
        fun newInstance(sourceId: Int, rootPath: String): SourceFragment =
            LocalFragment().apply {
                arguments = bundleOf(
                    Constants.FRAGMENT_TITLE to SourceType.values()[sourceId],
                    Constants.LOCAL_ROOT to rootPath
                )
            }
    }
}