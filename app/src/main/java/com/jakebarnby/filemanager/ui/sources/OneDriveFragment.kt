package com.jakebarnby.filemanager.ui.sources

import android.os.Bundle
import androidx.core.os.bundleOf
import com.jakebarnby.filemanager.models.SourceType
import com.jakebarnby.filemanager.models.sources.onedrive.OneDriveSource
import com.jakebarnby.filemanager.util.Constants

/**
 * Created by Jake on 6/7/2017.
 */
class OneDriveFragment : SourceFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter.setFileSource(OneDriveSource(presenter.prefsManager))
    }

    override fun onResume() {
        super.onResume()
        (presenter.source as OneDriveSource).checkForAccessToken(this)
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