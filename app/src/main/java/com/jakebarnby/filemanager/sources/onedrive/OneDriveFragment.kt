package com.jakebarnby.filemanager.sources.onedrive

import android.content.Intent
import android.os.Bundle
import androidx.core.os.bundleOf
import com.jakebarnby.filemanager.models.SourceType
import com.jakebarnby.filemanager.ui.sources.SourceFragment
import com.jakebarnby.filemanager.util.Constants

/**
 * Created by Jake on 6/7/2017.
 */
class OneDriveFragment : SourceFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter.setFileSource(OneDriveSource(presenter))
    }

    override fun onResume() {
        super.onResume()
        (presenter.source as OneDriveSource).checkForAccessToken(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        (presenter.source as OneDriveSource).client?.handleInteractiveRequestRedirect(requestCode, resultCode, data)
    }

    companion object {
        fun newInstance(sourceId: Int): SourceFragment =
            OneDriveFragment().apply {
                arguments = bundleOf(
                    Constants.FRAGMENT_TITLE to SourceType.values()[sourceId].sourceName
                )
            }
    }
}