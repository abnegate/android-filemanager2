package com.jakebarnby.filemanager.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.jakebarnby.filemanager.R
import com.jakebarnby.filemanager.util.Constants
import com.jakebarnby.filemanager.util.Constants.DIALOG_CURRENT_FILE_NAME_KEY
import com.jakebarnby.filemanager.util.Constants.DIALOG_ON_POSITIVE_KEY
import com.jakebarnby.filemanager.util.Constants.DIALOG_TITLE_KEY

/**
 * Created by Jake on 6/18/2017.
 */
class RenameDialog : DialogFragment() {

    companion object {
        fun newInstance(
            currentFileName: String,
            onPositive: (String) -> Unit
        ) = RenameDialog().apply {
            arguments = bundleOf(
                DIALOG_TITLE_KEY to getString(R.string.rename),
                DIALOG_CURRENT_FILE_NAME_KEY to currentFileName,
                DIALOG_ON_POSITIVE_KEY to onPositive
            )
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val currentName = arguments
            ?.getString(DIALOG_CURRENT_FILE_NAME_KEY) ?: ""

        val onPositive = arguments
            ?.getSerializable(DIALOG_ON_POSITIVE_KEY) as? (String) -> Unit

        val builder = AlertDialog.Builder(activity!!)
            .setTitle(arguments?.getString(Constants.DIALOG_TITLE_KEY))

        val view = layoutInflater.inflate(R.layout.dialog_rename, null)
        val input = view.findViewById<EditText>(R.id.text_rename)

        input?.setText(
            if (currentName.lastIndexOf('.') > 0) {
                currentName.substring(0, currentName.lastIndexOf('.'))
            } else {
                currentName
            }
        )

        input.setSelection(input.text.length)

        return builder.setView(view)
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                onPositive?.invoke(input.text.toString())
                dismiss()
            }.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.create()

    }
}