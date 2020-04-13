package com.jakebarnby.filemanager.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.jakebarnby.filemanager.R
import com.jakebarnby.filemanager.util.Constants
import com.jakebarnby.filemanager.util.Constants.DIALOG_ON_POSITIVE_KEY
import com.jakebarnby.filemanager.util.Constants.DIALOG_TITLE_KEY

/**
 * Created by Jake on 6/18/2017.
 */
class CreateFolderDialog : DialogFragment() {

    companion object {
        fun newInstance(
            onPositive: (String) -> Unit
        ) = CreateFolderDialog().apply {
            arguments = bundleOf(
                DIALOG_TITLE_KEY to getString(R.string.create_folder),
                DIALOG_ON_POSITIVE_KEY to onPositive
            )
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val onPositive = arguments
            ?.getSerializable(DIALOG_ON_POSITIVE_KEY) as? (String) -> Unit

        val builder = AlertDialog.Builder(activity!!)
            .setTitle(arguments!!.getString(Constants.DIALOG_TITLE_KEY))

        val view = activity!!.layoutInflater.inflate(R.layout.dialog_create_folder, null)
        val input = view.findViewById<EditText>(R.id.txt_new_folder_name)

        return builder.setView(view)
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                onPositive?.invoke(input.text.toString())
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }.create()
    }
}