package com.jakebarnby.filemanager.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioGroup;

import com.jakebarnby.filemanager.R;
import com.jakebarnby.filemanager.sources.SourceActivity;
import com.jakebarnby.filemanager.util.Constants;
import com.jakebarnby.filemanager.util.PreferenceUtils;

/**
 * Created by Jake on 10/2/2017.
 */

public class SortByDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.sort_by));

        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_sort_by, null);
        initViews(view);

        builder.setView(view);
        builder.setNegativeButton(getText(R.string.close), (dialog, which) -> {
            ((SourceActivity)getActivity()).initAllRecyclers();
            dismiss();
        });
        return builder.create();
    }

    private void initViews(View rootView) {
        RadioGroup sortGroup = rootView.findViewById(R.id.rdg_sort_by);

        int currentSortType = PreferenceUtils.getInt(
                getContext(),
                Constants.Prefs.SORT_TYPE_KEY,
                Constants.SortTypes.NAME);

        setCheckedSortType(currentSortType, sortGroup);
        setSortTypeListener(sortGroup);

        RadioGroup orderGroup = rootView.findViewById(R.id.rdg_order_by);

        int currentOrderType = PreferenceUtils.getInt(
                getContext(),
                Constants.Prefs.ORDER_TYPE_KEY,
                Constants.OrderTypes.ASCENDING);

        setCheckedOrderType(currentOrderType, orderGroup);
        setOrderTypeListener(orderGroup);
    }

    private void setOrderTypeListener(RadioGroup orderGroup) {
        orderGroup.setOnCheckedChangeListener((radioGroup, id) -> {
            switch (id) {
                case R.id.rdb_ascending:
                    PreferenceUtils.savePref(
                            getContext(),
                            Constants.Prefs.ORDER_TYPE_KEY,
                            Constants.OrderTypes.ASCENDING);
                    break;
                case R.id.rdb_descending:
                    PreferenceUtils.savePref(
                            getContext(),
                            Constants.Prefs.ORDER_TYPE_KEY,
                            Constants.OrderTypes.DESCENDING);
                    break;
            }
        });
    }

    private void setCheckedOrderType(int currentOrderType, RadioGroup orderGroup) {
        switch (currentOrderType) {
            case Constants.OrderTypes.ASCENDING:
                orderGroup.check(R.id.rdb_ascending);
                break;
            case Constants.OrderTypes.DESCENDING:
                orderGroup.check(R.id.rdb_descending);
                break;
        }
    }

    private void setSortTypeListener(RadioGroup sortGroup) {
        sortGroup.setOnCheckedChangeListener((radioGroup, id) -> {
            switch (id) {
                case R.id.rdb_name:
                    PreferenceUtils.savePref(
                            getContext(),
                            Constants.Prefs.SORT_TYPE_KEY,
                            Constants.SortTypes.NAME);
                    break;
                case R.id.rdb_size:
                    PreferenceUtils.savePref(
                            getContext(),
                            Constants.Prefs.SORT_TYPE_KEY,
                            Constants.SortTypes.SIZE);
                    break;
                case R.id.rdb_type:
                    PreferenceUtils.savePref(
                            getContext(),
                            Constants.Prefs.SORT_TYPE_KEY,
                            Constants.SortTypes.TYPE);
                    break;
                case R.id.rdb_modified_time:
                    PreferenceUtils.savePref(
                            getContext(),
                            Constants.Prefs.SORT_TYPE_KEY,
                            Constants.SortTypes.MODIFIED_TIME);
                    break;
            }
        });
    }

    private void setCheckedSortType(int currentSortType, RadioGroup sortGroup) {
        switch (currentSortType) {
            case Constants.SortTypes.NAME:
                sortGroup.check(R.id.rdb_name);
                break;
            case Constants.SortTypes.SIZE:
                sortGroup.check(R.id.rdb_size);
                break;
            case Constants.SortTypes.TYPE:
                sortGroup.check(R.id.rdb_type);
                break;
            case Constants.SortTypes.MODIFIED_TIME:
                sortGroup.check(R.id.rdb_modified_time);
                break;
        }
    }
}
