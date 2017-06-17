package com.jakebarnby.filemanager.activities.source;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.jakebarnby.filemanager.R;

/**
 * Created by Jake on 5/31/2017.
 *
 * Dialog that displays a choice of layout view options
 */
public class ViewAsDialogFragment extends DialogFragment {

    String[]                mOptions = {"List", "Grid"};
    SharedPreferences       mSharedPrefs;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int selectedIndex;

        mSharedPrefs = getActivity().getPreferences(Context.MODE_PRIVATE);
        String viewAsType = mSharedPrefs.getString("ViewAs", "List");
        if (viewAsType.equals("List")) {
            selectedIndex = 0;
        } else if (viewAsType.equals("Grid")) {
            selectedIndex = 1;
        } else {
            selectedIndex = 0;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.action_viewas)
                .setSingleChoiceItems(mOptions, selectedIndex, (dialog, which) -> {
                    mSharedPrefs.edit().putString("ViewAs", mOptions[which]).apply();
                })
                .setPositiveButton(R.string.ok, (dialog, id) -> {
                    ((SourceActivity)getActivity()).getActiveFragment().setRecyclerLayout();
                    dismiss();
                })
                .setNegativeButton(R.string.cancel, (dialog, id) -> {
                    dialog.dismiss();
                });

        return builder.create();
    }
}
