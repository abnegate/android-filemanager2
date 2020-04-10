package com.jakebarnby.filemanager.sources.dropbox

import android.os.Bundle
import androidx.core.os.bundleOf
import com.jakebarnby.filemanager.sources.SourceFragment
import com.jakebarnby.filemanager.util.Constants
import com.jakebarnby.filemanager.util.Constants.Sources

/**
 * Created by Jake on 5/31/2017.
 */
class DropboxFragment : SourceFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        source = DropboxSource(Sources.DROPBOX, this)
    }

    override fun onResume() {
        super.onResume()
        (source as DropboxSource).checkForAccessToken(context!!)
    }

    companion object {
        private const val TAG = "DROPBOX"

        fun newInstance(sourceName: String): SourceFragment =
            DropboxFragment().apply {
                arguments = bundleOf(Constants.FRAGMENT_TITLE to sourceName)
            }
    }
}