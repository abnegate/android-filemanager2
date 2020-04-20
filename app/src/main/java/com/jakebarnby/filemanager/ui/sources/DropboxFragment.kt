package com.jakebarnby.filemanager.ui.sources

import android.os.Bundle
import androidx.core.os.bundleOf
import com.jakebarnby.filemanager.models.SourceType
import com.jakebarnby.filemanager.models.sources.dropbox.DropboxSource
import com.jakebarnby.filemanager.util.Constants

/**
 * Created by Jake on 5/31/2017.
 */
class DropboxFragment : SourceFragment() {

    companion object {
        private const val TAG = "DROPBOX"

        fun newInstance(sourceId: Int): SourceFragment =
            DropboxFragment().apply {
                arguments = bundleOf(
                    Constants.FRAGMENT_TITLE to SourceType.values()[sourceId].sourceName
                )
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter.setFileSource(DropboxSource(presenter.prefsManager))
    }

    override fun onResume() {
        super.onResume()
        (presenter.source as DropboxSource).checkForAccessToken(context!!)
    }
}