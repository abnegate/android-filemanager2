package com.jakebarnby.filemanager.ui.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.jakebarnby.filemanager.R
import com.jakebarnby.filemanager.sources.SourceActivity
import com.jakebarnby.filemanager.util.Constants.Prefs
import com.jakebarnby.filemanager.util.Constants.ViewTypes
import com.jakebarnby.filemanager.util.PreferenceUtils

/**
 * Created by Jake on 5/31/2017.
 *
 * Dialog that displays a choice of layout view options
 */
class ViewAsDialog : DialogFragment() {

    private lateinit var options: Array<String>

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        options = arrayOf(
            getString(R.string.list),
            getString(R.string.detailed_list),
            getString(R.string.grid)
        )

        val viewAsType = PreferenceUtils.getInt(
            context!!,
            Prefs.VIEW_TYPE_KEY,
            ViewTypes.LIST
        )

        return AlertDialog.Builder(activity!!)
            .setTitle(R.string.action_viewas)
            .setSingleChoiceItems(options, viewAsType) { _, which ->
                PreferenceUtils.savePref(context!!, Prefs.VIEW_TYPE_KEY, which)
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