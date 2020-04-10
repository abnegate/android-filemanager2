package com.jakebarnby.filemanager.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;

import com.jakebarnby.filemanager.R;
import com.jakebarnby.filemanager.sources.SourceActivity;
import com.jakebarnby.filemanager.util.Constants;
import com.jakebarnby.filemanager.util.PreferenceUtils;

/**
 * Created by Jake on 5/31/2017.
 *
 * Dialog that displays a choice of layout view options
 */
public class ViewAsDialog extends DialogFragment {

    String[]                mOptions;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mOptions = new String[]{
                getString(R.string.list),
                getString(R.string.detailed_list),
                getString(R.string.grid) };

        int viewAsType = PreferenceUtils.getInt(
                getContext(),
                Constants.Prefs.VIEW_TYPE_KEY,
                Constants.ViewTypes.LIST);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.action_viewas)
                .setSingleChoiceItems(mOptions, viewAsType, (dialog, which) -> {
                    PreferenceUtils.savePref(getContext(), Constants.Prefs.VIEW_TYPE_KEY, which);
                })
                .setPositiveButton(R.string.ok, (dialog, id) -> {
                    ((SourceActivity)getActivity()).initAllRecyclers();
                    dismiss();
                })
                .setNegativeButton(R.string.cancel, (dialog, id) -> {
                    dialog.dismiss();
                });

        return builder.create();
    }
}

