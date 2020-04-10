package com.jakebarnby.filemanager.ui.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.jakebarnby.filemanager.R
import com.jakebarnby.filemanager.managers.SelectedFilesManager
import com.jakebarnby.filemanager.sources.models.SourceFile
import com.jakebarnby.filemanager.util.Constants
import com.jakebarnby.filemanager.util.TreeNode
import com.jakebarnby.filemanager.util.Utils

/**
 * Created by Jake on 6/19/2017.
 */
class PropertiesDialog : DialogFragment() {

    companion object {
        fun newInstance() =
            PropertiesDialog().apply {
                arguments = bundleOf(
                    Constants.DIALOG_TITLE_KEY to getString(R.string.properties)
                )
            }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val view = activity!!.layoutInflater.inflate(R.layout.dialog_properties, null)
        val nameText = view.findViewById<TextView>(R.id.txt_name)
        val sourceNameText = view.findViewById<TextView>(R.id.txt_source_name)
        val pathText = view.findViewById<TextView>(R.id.txt_path)
        val sizeText = view.findViewById<TextView>(R.id.txt_size)
        val modifiedTimeText = view.findViewById<TextView>(R.id.txt_modified_time)
        val selected = SelectedFilesManager.currentSelectedFiles

        if (selected.size > 1) {
            var totalSize = 0.0f
            val sourceNames = StringBuilder()
            for (file in selected) {
                if (!sourceNames.toString().contains(file.data.sourceName)) {
                    sourceNames.append(file.data.sourceName).append(", ")
                }
                totalSize += file.data.size.toFloat()
            }

            nameText.text = String.format(getString(R.string.selected_item_count), selected.size)
            sourceNameText.text = sourceNames.toString()
            sizeText.text = String.format(getString(R.string.size_display), totalSize / Constants.BYTES_TO_MEGABYTE)
            pathText.visibility = View.GONE
            modifiedTimeText.visibility = View.GONE
            view.findViewById<View>(R.id.txt_path_label).visibility = View.GONE
            view.findViewById<View>(R.id.txt_modified_time_label).visibility = View.GONE

        } else if (selected.size == 1) {
            val file = selected[0].data
            nameText.text = file.name
            pathText.text = file.path
            sourceNameText.text = file.sourceName
            sizeText.text = String.format(getString(R.string.size_display), file.size / Constants.BYTES_TO_MEGABYTE)
            modifiedTimeText.text = Utils.getDisplayStringFromDate(file.modifiedTime)
        }

        val builder = AlertDialog.Builder(activity!!)
            .setTitle(getString(R.string.properties))
            .setView(view)
            .setPositiveButton(getString(R.string.ok)) { dialog, _ -> dialog.dismiss() }

        return builder.create()
    }
}