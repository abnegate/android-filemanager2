package com.jakebarnby.filemanager.sources.onedrive

import android.content.Intent
import android.os.Bundle
import androidx.core.os.bundleOf
import com.jakebarnby.filemanager.sources.SourceFragment
import com.jakebarnby.filemanager.util.Constants.Sources

/**
 * Created by Jake on 6/7/2017.
 */
class OneDriveFragment : SourceFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        source = OneDriveSource(Sources.ONEDRIVE, this)
    }

    override fun onResume() {
        super.onResume()
        (source as OneDriveSource).checkForAccessToken(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        (source as OneDriveSource).client?.handleInteractiveRequestRedirect(requestCode, resultCode, data)
    }

    companion object {
        fun newInstance(sourceName: String): SourceFragment =
            OneDriveFragment().apply {
                arguments = bundleOf("TITLE" to sourceName)
            }
    }
}