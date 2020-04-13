package com.jakebarnby.filemanager.ui.dialogs

import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.jakebarnby.filemanager.R
import com.jakebarnby.filemanager.util.Constants

class ProgressDialog: DialogFragment() {

    companion object {
        fun newInstance(
            count: Int,
            onPositive: () -> Unit
        ) = ProgressDialog().apply {
                arguments = bundleOf(
                    Constants.DIALOG_TITLE_KEY to getString(R.string.dialog_deleting),
                    Constants.DIALOG_ON_POSITIVE_KEY to onPositive,
                    Constants.DIALOG_DELETE_COUNT to count
                )
            }
    }
}