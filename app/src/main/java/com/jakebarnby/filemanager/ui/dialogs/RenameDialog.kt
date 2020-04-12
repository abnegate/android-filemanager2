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
import com.jakebarnby.filemanager.ui.sources.SourceActivity
import com.jakebarnby.filemanager.util.Constants
import com.jakebarnby.filemanager.util.TreeNode

/**
 * Created by Jake on 6/18/2017.
 */
class RenameDialog : DialogFragment() {

    companion object {
        fun newInstance() =
            CreateFolderDialog().apply {
                arguments = bundleOf(
                    Constants.DIALOG_TITLE_KEY to getString(R.string.rename)
                )
            }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity!!)
            .setTitle(arguments?.getString(Constants.DIALOG_TITLE_KEY))

        val view = activity?.layoutInflater?.inflate(R.layout.dialog_rename, null)
        val input = view?.findViewById<EditText>(R.id.text_rename)

        val name = SelectedFilesManager.currentSelectedFiles[0].data.name
        input?.setText(
            if (name.lastIndexOf('.') > 0) {
                name.substring(0, name.lastIndexOf('.'))
            } else {
                name
            }
        )

        input?.setSelection(input.text.length)

        builder.setView(view)
        builder.setPositiveButton(getString(R.string.ok)) { dialog: DialogInterface?, _: Int ->
            val activeDirectory = (activity as? SourceActivity)?.sourceManager.activeDirectory
            val newName = if (name.lastIndexOf('.') > 0) {
                input?.text?.toString() + name.substring(name.lastIndexOf('.'))
            } else {
                input?.text.toString()
            }
            for (file in activeDirectory.children ?: emptyList<TreeNode<SourceFile>>()) {
                if (file.data.name.equals(newName, ignoreCase = true)) {
                    Snackbar.make(activity!!.currentFocus!!, getString(R.string.file_exists), Snackbar.LENGTH_LONG).show()
                    return@setPositiveButton
                }
            }
            SourceTransferService.startActionRename(context!!, newName)
            SelectedFilesManager.startNewSelection()
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        return builder.create()
    }
}