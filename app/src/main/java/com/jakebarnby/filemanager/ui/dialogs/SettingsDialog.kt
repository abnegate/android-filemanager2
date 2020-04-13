package com.jakebarnby.filemanager.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.jakebarnby.filemanager.R
import com.jakebarnby.filemanager.managers.PreferenceManager
import com.jakebarnby.filemanager.util.Constants.DIALOG_ON_POSITIVE_KEY
import com.jakebarnby.filemanager.util.Constants.DIALOG_TITLE_KEY
import com.jakebarnby.filemanager.util.Constants.Prefs

/**
 * Created by Jake on 10/3/2017.
 */
class SettingsDialog : DialogFragment() {

    companion object {
        fun newInstance(
            onPositive: () -> Unit
        ) =
            SettingsDialog().apply {
                arguments = bundleOf(
                    DIALOG_TITLE_KEY to getString(R.string.action_settings),
                    DIALOG_ON_POSITIVE_KEY to onPositive
                )
            }
    }

    private val prefs = PreferenceManager(context!!.getSharedPreferences(
        Prefs.PREFS,
        Context.MODE_PRIVATE
    ))

    private var hiddenFilesChecked = false
    private var foldersFirstChecked = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val onPositive = arguments
            ?.getSerializable(DIALOG_ON_POSITIVE_KEY) as? () -> Unit

        val builder = AlertDialog.Builder(activity!!).setTitle(getString(R.string.action_settings))

        val view = layoutInflater.inflate(R.layout.dialog_settings, null)
        initViews(view)

        return builder
            .setView(view)
            .setPositiveButton(R.string.ok) { _, _ ->
                prefs.savePref(Prefs.FOLDER_FIRST_KEY, foldersFirstChecked)
                prefs.savePref(Prefs.HIDDEN_FILES_KEY, hiddenFilesChecked)
                onPositive?.invoke()
            }
            .setNegativeButton(R.string.close) { _, _ ->
                dismiss()
            }.create()
    }

    private fun initViews(rootView: View) {
        val foldersFirstCbx = rootView.findViewById<CheckBox>(R.id.ckb_folders_first)
        val hiddenFoldersCbx = rootView.findViewById<CheckBox>(R.id.ckb_hidden_files)
        val foldersFirst = prefs.getBoolean(Prefs.FOLDER_FIRST_KEY, true)
        val showHiddenFiles = prefs.getBoolean(Prefs.HIDDEN_FILES_KEY, false)

        foldersFirstCbx.isChecked = foldersFirst
        hiddenFoldersCbx.isChecked = showHiddenFiles

        foldersFirstCbx.setOnCheckedChangeListener { _, checked ->
            foldersFirstChecked = checked
        }
        hiddenFoldersCbx.setOnCheckedChangeListener { _, checked ->
            hiddenFilesChecked = checked
        }
    }
}