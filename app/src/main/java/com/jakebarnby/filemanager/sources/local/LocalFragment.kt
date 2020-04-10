package com.jakebarnby.filemanager.sources.local

import android.Manifest
import android.os.Bundle
import androidx.core.os.bundleOf
import com.jakebarnby.filemanager.sources.SourceFragment
import com.jakebarnby.filemanager.sources.models.SourceType
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

        source = LocalSource(name, rootPath, this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (source.sourceType == SourceType.LOCAL) {
            onCheckPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, RequestCodes.STORAGE_PERMISSIONS)
        }
    }

    companion object {
        fun newInstance(sourceName: String, rootPath: String): SourceFragment =
            LocalFragment().apply {
                arguments = bundleOf(
                    Constants.FRAGMENT_TITLE to sourceName,
                    Constants.LOCAL_ROOT to rootPath
                )
            }
    }
}