package com.jakebarnby.filemanager.ui.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.jakebarnby.filemanager.R
import com.jakebarnby.filemanager.sources.SourceActivity
import com.jakebarnby.filemanager.util.Constants
import com.jakebarnby.filemanager.util.Constants.OrderTypes
import com.jakebarnby.filemanager.util.Constants.Prefs
import com.jakebarnby.filemanager.util.Constants.SortTypes
import com.jakebarnby.filemanager.util.PreferenceUtils

/**
 * Created by Jake on 10/2/2017.
 */
class SortByDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity!!)
        builder.setTitle(getString(R.string.sort_by))
        val view = activity!!.layoutInflater.inflate(R.layout.dialog_sort_by, null)
        initViews(view)
        builder.setView(view)
        builder.setNegativeButton(getText(R.string.close)) { dialog: DialogInterface?, which: Int ->
            (activity as SourceActivity?)!!.initAllRecyclers()
            dismiss()
        }
        return builder.create()
    }

    private fun initViews(rootView: View) {
        val sortGroup = rootView.findViewById<RadioGroup>(R.id.rdg_sort_by)
        val currentSortType = PreferenceUtils.getInt(
                context!!,
                Prefs.SORT_TYPE_KEY,
                SortTypes.NAME
        )

        setCheckedSortType(currentSortType, sortGroup)
        setSortTypeListener(sortGroup)

        val orderGroup = rootView.findViewById<RadioGroup>(R.id.rdg_order_by)
        val currentOrderType = PreferenceUtils.getInt(
                context!!,
                Prefs.ORDER_TYPE_KEY,
                OrderTypes.ASCENDING
        )

        setCheckedOrderType(currentOrderType, orderGroup)
        setOrderTypeListener(orderGroup)
    }

    private fun setOrderTypeListener(orderGroup: RadioGroup) {
        orderGroup.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rdb_ascending -> PreferenceUtils.savePref(
                        context!!,
                        Prefs.ORDER_TYPE_KEY,
                        OrderTypes.ASCENDING
                )
                R.id.rdb_descending -> PreferenceUtils.savePref(
                        context!!,
                        Prefs.ORDER_TYPE_KEY,
                        OrderTypes.DESCENDING
                )
            }
        }
    }

    private fun setCheckedOrderType(currentOrderType: Int, orderGroup: RadioGroup) {
        when (currentOrderType) {
            OrderTypes.ASCENDING -> orderGroup.check(R.id.rdb_ascending)
            OrderTypes.DESCENDING -> orderGroup.check(R.id.rdb_descending)
        }
    }

    private fun setSortTypeListener(sortGroup: RadioGroup) {
        sortGroup.setOnCheckedChangeListener { radioGroup: RadioGroup?, id: Int ->
            when (id) {
                R.id.rdb_name -> PreferenceUtils.savePref(
                        context!!,
                        Prefs.SORT_TYPE_KEY,
                        SortTypes.NAME
                )
                R.id.rdb_size -> PreferenceUtils.savePref(
                        context!!,
                        Prefs.SORT_TYPE_KEY,
                        SortTypes.SIZE
                )
                R.id.rdb_type -> PreferenceUtils.savePref(
                        context!!,
                        Prefs.SORT_TYPE_KEY,
                        SortTypes.TYPE
                )
                R.id.rdb_modified_time -> PreferenceUtils.savePref(
                        context!!,
                        Prefs.SORT_TYPE_KEY,
                        SortTypes.MODIFIED_TIME
                )
            }
        }
    }

    private fun setCheckedSortType(currentSortType: Int, sortGroup: RadioGroup) {
        when (currentSortType) {
            SortTypes.NAME -> sortGroup.check(R.id.rdb_name)
            SortTypes.SIZE -> sortGroup.check(R.id.rdb_size)
            SortTypes.TYPE -> sortGroup.check(R.id.rdb_type)
            SortTypes.MODIFIED_TIME -> sortGroup.check(R.id.rdb_modified_time)
        }
    }
}