package com.jakebarnby.filemanager.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.jakebarnby.filemanager.R
import com.jakebarnby.filemanager.managers.PreferenceManager
import com.jakebarnby.filemanager.ui.sources.SourceActivity
import com.jakebarnby.filemanager.util.Constants
import com.jakebarnby.filemanager.util.Constants.Prefs

/**
 * Created by Jake on 10/3/2017.
 */
class SettingsDialog : DialogFragment() {

    companion object {
        fun newInstance() =
            SettingsDialog().apply {
                arguments = bundleOf(
                    Constants.DIALOG_TITLE_KEY to getString(R.string.action_settings)
                )
            }
    }

    private val prefs = PreferenceManager(context!!.getSharedPreferences(
        Prefs.PREFS,
        Context.MODE_PRIVATE
    ))

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val builder = AlertDialog.Builder(activity!!)
        builder.setTitle(getString(R.string.action_settings))
        val rootView = activity!!.layoutInflater.inflate(R.layout.dialog_settings, null)
        initViews(rootView)
        builder.setView(rootView)
        builder.setNegativeButton(getString(R.string.close)) { dialog: DialogInterface, which: Int ->
            (activity as SourceActivity?)!!.initAllRecyclers()
            dialog.dismiss()
        }
        return builder.create()
    }

    private fun initViews(rootView: View) {
        val foldersFirstCbx = rootView.findViewById<CheckBox>(R.id.ckb_folders_first)
        val hiddenFoldersCbx = rootView.findViewById<CheckBox>(R.id.ckb_hidden_files)
        val foldersFirst = prefs.getBoolean(Prefs.FOLDER_FIRST_KEY, true)
        val showHiddenFiles = prefs.getBoolean(Prefs.HIDDEN_FOLDER_KEY, false)

        foldersFirstCbx.isChecked = foldersFirst
        hiddenFoldersCbx.isChecked = showHiddenFiles

        foldersFirstCbx.setOnCheckedChangeListener { _, checked ->
            prefs.savePref(Prefs.FOLDER_FIRST_KEY, checked)
        }
        hiddenFoldersCbx.setOnCheckedChangeListener { _, checked ->
            prefs.savePref(Prefs.HIDDEN_FOLDER_KEY, checked)
        }

//        Button ossButton = rootView.findViewById(R.id.btn_oss_licenses);
//        ossButton.setOnClickListener((view) -> {
//            Intent intent = new Intent(getActivity(), OssLicensesMenuActivity.class);
//            String title = getString(R.string.oss_license_title);
//            intent.putExtra(Constants.DIALOG_TITLE_KEY, title);
//            startActivity(intent);
//
//        });
    }
}