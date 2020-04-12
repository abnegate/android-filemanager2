package com.jakebarnby.filemanager.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.jakebarnby.filemanager.R
import com.jakebarnby.filemanager.managers.PreferenceManager
import com.jakebarnby.filemanager.ui.sources.SourceActivity
import com.jakebarnby.filemanager.util.Constants
import com.jakebarnby.filemanager.util.Constants.OrderType
import com.jakebarnby.filemanager.util.Constants.Prefs
import com.jakebarnby.filemanager.util.Constants.SortType

/**
 * Created by Jake on 10/2/2017.
 */
class SortByDialog : DialogFragment() {

    companion object {
        fun newInstance() =
            SortByDialog().apply {
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
        val currentSortType = prefs.getInt(
            Prefs.SORT_TYPE_KEY,
            SortType.NAME
        )

        setCheckedSortType(currentSortType, sortGroup)
        setSortTypeListener(sortGroup)

        val orderGroup = rootView.findViewById<RadioGroup>(R.id.rdg_order_by)
        val currentOrderType = prefs.getInt(
            Prefs.ORDER_TYPE_KEY,
            OrderType.ASCENDING
        )

        setCheckedOrderType(currentOrderType, orderGroup)
        setOrderTypeListener(orderGroup)
    }

    private fun setOrderTypeListener(orderGroup: RadioGroup) {
        orderGroup.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rdb_ascending -> prefs.savePref(
                    Prefs.ORDER_TYPE_KEY,
                    OrderType.ASCENDING
                )
                R.id.rdb_descending -> prefs.savePref(
                    Prefs.ORDER_TYPE_KEY,
                    OrderType.DESCENDING
                )
            }
        }
    }

    private fun setCheckedOrderType(currentOrderType: Int, orderGroup: RadioGroup) {
        when (currentOrderType) {
            OrderType.ASCENDING -> orderGroup.check(R.id.rdb_ascending)
            OrderType.DESCENDING -> orderGroup.check(R.id.rdb_descending)
        }
    }

    private fun setSortTypeListener(sortGroup: RadioGroup) {
        sortGroup.setOnCheckedChangeListener { radioGroup: RadioGroup?, id: Int ->
            when (id) {
                R.id.rdb_name -> prefs.savePref(
                    Prefs.SORT_TYPE_KEY,
                    SortType.NAME
                )
                R.id.rdb_size -> prefs.savePref(
                    Prefs.SORT_TYPE_KEY,
                    SortType.SIZE
                )
                R.id.rdb_type -> prefs.savePref(
                    Prefs.SORT_TYPE_KEY,
                    SortType.TYPE
                )
                R.id.rdb_modified_time -> prefs.savePref(
                    Prefs.SORT_TYPE_KEY,
                    SortType.MODIFIED_TIME
                )
            }
        }
    }

    private fun setCheckedSortType(currentSortType: Int, sortGroup: RadioGroup) {
        when (currentSortType) {
            SortType.NAME -> sortGroup.check(R.id.rdb_name)
            SortType.SIZE -> sortGroup.check(R.id.rdb_size)
            SortType.TYPE -> sortGroup.check(R.id.rdb_type)
            SortType.MODIFIED_TIME -> sortGroup.check(R.id.rdb_modified_time)
        }
    }
}