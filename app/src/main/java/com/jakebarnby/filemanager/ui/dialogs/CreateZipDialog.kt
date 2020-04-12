package com.jakebarnby.filemanager.ui.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.google.android.material.snackbar.Snackbar
import com.jakebarnby.filemanager.R
import com.jakebarnby.filemanager.managers.SelectedFilesManager
import com.jakebarnby.filemanager.services.SourceTransferService
import com.jakebarnby.filemanager.sources.models.SourceFile
import com.jakebarnby.filemanager.util.Constants
import com.jakebarnby.filemanager.util.TreeNode

/**
 * Created by Jake on 10/14/2017.
 */
class CreateZipDialog : DialogFragment() {

    companion object {
        fun newInstance() =
            CreateZipDialog().apply {
                arguments = bundleOf(
                    Constants.DIALOG_TITLE_KEY to getString(R.string.create_zip)
                )
            }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity!!)
            .setTitle(arguments!!.getString(Constants.DIALOG_TITLE_KEY))

        val view = activity!!.layoutInflater.inflate(R.layout.dialog_create_folder, null)
        val input = view.findViewById<EditText>(R.id.txt_new_folder_name)

        builder.setView(view)
        builder.setPositiveButton(getString(R.string.ok)) { dialog: DialogInterface?, which: Int ->
            val activeDirectory = SelectedFilesManager
                .getActionableDirectory(SelectedFilesManager.operationCount)

            val name = input.text.toString() + ".zip"

            for (file in activeDirectory?.children ?: emptyList<TreeNode<SourceFile>>()) {
                if (file.data.isDirectory) {
                    if (file.data.name == name) {
                        Snackbar.make(activity!!.currentFocus!!, getString(R.string.folder_exists), Snackbar.LENGTH_LONG).show()
                        return@setPositiveButton
                    }
                }
            }
            SourceTransferService.Companion.startActionZip(context!!, name)
            SelectedFilesManager.startNewSelection()
        }
        builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog.dismiss() }
        return builder.create()
    }
}