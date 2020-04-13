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
import com.jakebarnby.filemanager.models.ViewType
import com.jakebarnby.filemanager.ui.sources.SourceActivity
import com.jakebarnby.filemanager.util.Constants.DIALOG_ON_POSITIVE_KEY
import com.jakebarnby.filemanager.util.Constants.DIALOG_TITLE_KEY
import com.jakebarnby.filemanager.util.Constants.Prefs

/**
 * Created by Jake on 5/31/2017.
 *
 * Dialog that displays a choice of layout view options
 */
class ViewAsDialog : DialogFragment() {

    companion object {
        fun newInstance(
            onPositive: () -> Unit
        ) = ViewAsDialog().apply {
            arguments = bundleOf(
                DIALOG_TITLE_KEY to getString(R.string.action_viewas),
                DIALOG_ON_POSITIVE_KEY to onPositive
            )
        }
    }

    private val prefs = PreferenceManager(context!!.getSharedPreferences(
        Prefs.PREFS,
        Context.MODE_PRIVATE
    ))

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val onPositive = arguments
            ?.getSerializable(DIALOG_ON_POSITIVE_KEY) as? () -> Unit

        val options = arrayOf(
            getString(R.string.list),
            getString(R.string.detailed_list),
            getString(R.string.grid)
        )
        val currentViewType = prefs.getInt(
            Prefs.VIEW_TYPE_KEY,
            ViewType.LIST.value
        )
        var selectedIndex = currentViewType

        return AlertDialog.Builder(activity!!)
            .setTitle(R.string.action_viewas)
            .setSingleChoiceItems(options, currentViewType) { _, which ->
                selectedIndex = which
            }
            .setPositiveButton(R.string.ok) { _, _ ->
                prefs.savePref(Prefs.VIEW_TYPE_KEY, selectedIndex)
                onPositive?.invoke()
                dismiss()
            }
            .setNegativeButton(R.string.cancel) { dialog: DialogInterface, id ->
                dialog.dismiss()
            }
            .create()
    }
}