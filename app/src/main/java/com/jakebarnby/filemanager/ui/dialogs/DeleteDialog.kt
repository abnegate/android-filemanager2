package com.jakebarnby.filemanager.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.jakebarnby.filemanager.R
import com.jakebarnby.filemanager.util.Constants
import com.jakebarnby.filemanager.util.Constants.DIALOG_DELETE_COUNT
import com.jakebarnby.filemanager.util.Constants.DIALOG_ON_POSITIVE_KEY
import com.jakebarnby.filemanager.util.Constants.DIALOG_TITLE_KEY
import java.io.Serializable
import java.util.*

class DeleteDialog : DialogFragment() {

    companion object {
        fun newInstance(
            count: Int,
            onPositive: () -> Unit
        ) = DeleteDialog().apply {
                arguments = bundleOf(
                    DIALOG_TITLE_KEY to getString(R.string.dialog_deleting),
                    DIALOG_ON_POSITIVE_KEY to onPositive,
                    DIALOG_DELETE_COUNT to count
                )
            }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val deleteCount = arguments
            ?.getInt(DIALOG_DELETE_COUNT)

        val onPositive = arguments
            ?.getSerializable(DIALOG_ON_POSITIVE_KEY) as? () -> Unit

        return AlertDialog.Builder(context!!)
            .setTitle(R.string.warning)
            .setMessage(String.format(Locale.getDefault(), getString(R.string.dialog_delete_confirm), deleteCount))
            .setNegativeButton(R.string.no) { dialog, _ -> dialog.dismiss() }
            .setPositiveButton(R.string.yes) { dialog, _ ->
                onPositive?.invoke()
                dialog.dismiss()
            }
            .create()
    }

}