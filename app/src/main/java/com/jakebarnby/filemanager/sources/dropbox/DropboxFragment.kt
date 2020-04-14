package com.jakebarnby.filemanager.sources.dropbox

import android.os.Bundle
import androidx.core.os.bundleOf
import com.jakebarnby.filemanager.models.SourceType
import com.jakebarnby.filemanager.ui.sources.SourceFragment
import com.jakebarnby.filemanager.util.Constants
import com.jakebarnby.filemanager.util.Constants.Sources

/**
 * Created by Jake on 5/31/2017.
 */
class DropboxFragment : SourceFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter.setFileSource(DropboxSource(presenter))
    }

    override fun onResume() {
        super.onResume()
        (presenter.source as DropboxSource).checkForAccessToken(context!!)
    }

    companion object {
        private const val TAG = "DROPBOX"

        fun newInstance(sourceId: Int): SourceFragment =
            DropboxFragment().apply {
                arguments = bundleOf(
                    Constants.FRAGMENT_TITLE to SourceType.values()[sourceId]
                )
            }
    }
}