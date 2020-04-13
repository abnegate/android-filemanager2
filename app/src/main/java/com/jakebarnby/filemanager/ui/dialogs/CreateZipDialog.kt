package com.jakebarnby.filemanager.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.jakebarnby.filemanager.R
import com.jakebarnby.filemanager.util.Constants

/**
 * Created by Jake on 10/14/2017.
 */
class CreateZipDialog : DialogFragment() {

    companion object {
        fun newInstance(
            onPositive: (String) -> Unit
        ) = CreateZipDialog().apply {
            arguments = bundleOf(
                Constants.DIALOG_TITLE_KEY to getString(R.string.create_zip),
                Constants.DIALOG_ON_POSITIVE_KEY to onPositive
            )
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val onPositive = arguments
            ?.getSerializable(Constants.DIALOG_ON_POSITIVE_KEY) as? (String) -> Unit

        val builder = AlertDialog.Builder(activity!!)
            .setTitle(arguments!!.getString(Constants.DIALOG_TITLE_KEY))

        val view = activity!!.layoutInflater.inflate(R.layout.dialog_create_folder, null)
        val input = view.findViewById<EditText>(R.id.txt_new_folder_name)

        return builder.setView(view)
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                onPositive?.invoke(input.text.toString())
                dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ ->
                dismiss()
            }.create()
    }
}