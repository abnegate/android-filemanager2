package com.jakebarnby.filemanager.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.jakebarnby.filemanager.R
import com.jakebarnby.filemanager.managers.PreferenceManager
import com.jakebarnby.filemanager.models.OrderType
import com.jakebarnby.filemanager.models.SortType
import com.jakebarnby.filemanager.util.Constants.DIALOG_ON_POSITIVE_KEY
import com.jakebarnby.filemanager.util.Constants.DIALOG_TITLE_KEY
import com.jakebarnby.filemanager.util.Constants.Prefs

/**
 * Created by Jake on 10/2/2017.
 */
class SortByDialog : DialogFragment() {

    companion object {
        fun newInstance(
            onPositive: () -> Unit
        ) =
            SortByDialog().apply {
                arguments = bundleOf(
                    DIALOG_TITLE_KEY to getString(R.string.rename),
                    DIALOG_ON_POSITIVE_KEY to onPositive
                )
            }
    }

    private val prefs = PreferenceManager(context!!.getSharedPreferences(
        Prefs.PREFS,
        Context.MODE_PRIVATE
    ))

    var selectedIndex = 0

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val onPositive = arguments
            ?.getSerializable(DIALOG_ON_POSITIVE_KEY) as? () -> Unit

        val builder = AlertDialog.Builder(context!!).setTitle(getString(R.string.sort_by))

        val view = layoutInflater.inflate(R.layout.dialog_sort_by, null)
        initViews(view)

        return builder
            .setView(view)
            .setPositiveButton(R.string.ok) { _, _ ->
                prefs.savePref(Prefs.SORT_TYPE_KEY, selectedIndex)
                onPositive?.invoke()
                dismiss()
            }
            .setNegativeButton(getText(R.string.close)) { _, _ ->
                dismiss()
            }.create()
    }

    private fun initViews(rootView: View) {
        val sortGroup = rootView.findViewById<RadioGroup>(R.id.rdg_sort_by)
        val currentSortType = prefs.getInt(
            Prefs.SORT_TYPE_KEY,
            SortType.NAME.ordinal
        )
        selectedIndex = currentSortType

        setCheckedSortType(currentSortType, sortGroup)
        setSortTypeListener(sortGroup)

        val orderGroup = rootView.findViewById<RadioGroup>(R.id.rdg_order_by)
        val currentOrderType = prefs.getInt(
            Prefs.ORDER_TYPE_KEY,
            OrderType.ASCENDING.ordinal
        )

        setCheckedOrderType(currentOrderType, orderGroup)
        setOrderTypeListener(orderGroup)
    }

    private fun setOrderTypeListener(orderGroup: RadioGroup) {
        orderGroup.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rdb_ascending -> prefs.savePref(
                    Prefs.ORDER_TYPE_KEY,
                    OrderType.ASCENDING.ordinal
                )
                R.id.rdb_descending -> prefs.savePref(
                    Prefs.ORDER_TYPE_KEY,
                    OrderType.DESCENDING.ordinal
                )
            }
        }
    }

    private fun setCheckedOrderType(currentOrderType: Int, orderGroup: RadioGroup) {
        when (currentOrderType) {
            OrderType.ASCENDING.ordinal -> orderGroup.check(R.id.rdb_ascending)
            OrderType.DESCENDING.ordinal -> orderGroup.check(R.id.rdb_descending)
        }
    }

    private fun setSortTypeListener(sortGroup: RadioGroup) {
        sortGroup.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rdb_name -> selectedIndex = SortType.NAME.ordinal
                R.id.rdb_size -> selectedIndex = SortType.SIZE.ordinal
                R.id.rdb_type -> selectedIndex = SortType.TYPE.ordinal
                R.id.rdb_modified_time -> selectedIndex = SortType.MODIFIED_TIME.ordinal
            }
        }
    }

    private fun setCheckedSortType(currentSortType: Int, sortGroup: RadioGroup) {
        when (currentSortType) {
            SortType.NAME.ordinal -> sortGroup.check(R.id.rdb_name)
            SortType.SIZE.ordinal -> sortGroup.check(R.id.rdb_size)
            SortType.TYPE.ordinal -> sortGroup.check(R.id.rdb_type)
            SortType.MODIFIED_TIME.ordinal -> sortGroup.check(R.id.rdb_modified_time)
        }
    }
}