package com.jakebarnby.filemanager.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.jakebarnby.filemanager.R
import com.jakebarnby.filemanager.managers.PreferenceManager
import com.jakebarnby.filemanager.sources.SourceActivity
import com.jakebarnby.filemanager.util.Constants
import com.jakebarnby.filemanager.util.Constants.Prefs
import com.jakebarnby.filemanager.util.Constants.ViewTypes

/**
 * Created by Jake on 5/31/2017.
 *
 * Dialog that displays a choice of layout view options
 */
class ViewAsDialog : DialogFragment() {

    companion object {
        fun newInstance() =
            ViewAsDialog().apply {
                arguments = bundleOf(
                    Constants.DIALOG_TITLE_KEY to getString(R.string.rename)
                )
            }
    }

    private val prefs = PreferenceManager(context!!.getSharedPreferences(
        Prefs.PREFS,
        Context.MODE_PRIVATE
    ))

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val options = arrayOf(
            getString(R.string.list),
            getString(R.string.detailed_list),
            getString(R.string.grid)
        )

        val viewAsType = prefs.getInt(
            Prefs.VIEW_TYPE_KEY,
            ViewTypes.LIST
        )

        return AlertDialog.Builder(activity!!)
            .setTitle(R.string.action_viewas)
            .setSingleChoiceItems(options, viewAsType) { _, which ->
                prefs.savePref(Prefs.VIEW_TYPE_KEY, which)
            }
            .setPositiveButton(R.string.ok) { _, _ ->
                (activity as? SourceActivity)?.initAllRecyclers()
                dismiss()
            }
            .setNegativeButton(R.string.cancel) { dialog: DialogInterface, id ->
                dialog.dismiss()
            }
            .create()
    }
}