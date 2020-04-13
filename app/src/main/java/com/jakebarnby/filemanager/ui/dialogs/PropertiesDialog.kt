package com.jakebarnby.filemanager.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.jakebarnby.filemanager.R
import com.jakebarnby.filemanager.util.Constants
import com.jakebarnby.filemanager.util.Constants.DIALOG_SELECTED_COUNT_KEY
import com.jakebarnby.filemanager.util.Constants.DIALOG_SELECTED_SIZE_KEY
import com.jakebarnby.filemanager.util.Constants.DIALOG_TITLE_KEY

/**
 * Created by Jake on 6/19/2017.
 */
class PropertiesDialog : DialogFragment() {

    companion object {
        fun newInstance(
            selectedCount: Int,
            totalSize: Int
        ) = PropertiesDialog().apply {
            arguments = bundleOf(
                DIALOG_TITLE_KEY to getString(R.string.properties),
                DIALOG_SELECTED_COUNT_KEY to selectedCount,
                DIALOG_SELECTED_SIZE_KEY to totalSize
            )
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val selectedCount = arguments
            ?.getInt(DIALOG_SELECTED_COUNT_KEY) ?: 1

        val totalSize = arguments
            ?.getInt(DIALOG_SELECTED_SIZE_KEY) ?: 1

        val view = layoutInflater.inflate(R.layout.dialog_properties, null)

        val nameText = view.findViewById<TextView>(R.id.txt_name)
        val sourceNameText = view.findViewById<TextView>(R.id.txt_source_name)
        val pathText = view.findViewById<TextView>(R.id.txt_path)
        val sizeText = view.findViewById<TextView>(R.id.txt_size)
        val modifiedTimeText = view.findViewById<TextView>(R.id.txt_modified_time)

        if (selectedCount > 1) {
            nameText.text = String.format(getString(R.string.selected_item_count), selectedCount)
            //sourceNameText.text = sourceNames.toString()
            sizeText.text = String.format(getString(R.string.size_display), totalSize / Constants.BYTES_TO_MEGABYTE)
            pathText.visibility = View.GONE
            modifiedTimeText.visibility = View.GONE
            view.findViewById<View>(R.id.txt_path_label).visibility = View.GONE
            view.findViewById<View>(R.id.txt_modified_time_label).visibility = View.GONE

        } else if (selectedCount == 1) {
//            val file = selected[0].data
//            nameText.text = file.name
//            pathText.text = file.path
//            sourceNameText.text = file.sourceId
//            sizeText.text = String.format(getString(R.string.size_display), file.size / Constants.BYTES_TO_MEGABYTE)
//            modifiedTimeText.text = Utils.getDisplayStringFromDate(file.modifiedTime)
        }

        return AlertDialog.Builder(activity!!)
            .setTitle(getString(R.string.properties))
            .setView(view)
            .setPositiveButton(getString(R.string.ok)) { _, _ -> dismiss() }
            .create()
    }
}