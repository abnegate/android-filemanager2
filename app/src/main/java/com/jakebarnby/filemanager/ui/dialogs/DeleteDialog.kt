package com.jakebarnby.filemanager.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.jakebarnby.filemanager.R
import com.jakebarnby.filemanager.util.Constants

class DeleteDialog : DialogFragment() {

    companion object {
        fun newInstance() =
            PropertiesDialog().apply {
                arguments = bundleOf(
                    Constants.DIALOG_TITLE_KEY to getString(R.string.properties)
                )
            }
    }

}