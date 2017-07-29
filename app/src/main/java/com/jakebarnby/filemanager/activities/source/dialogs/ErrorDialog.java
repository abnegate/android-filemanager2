package com.jakebarnby.filemanager.activities.source.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.jakebarnby.filemanager.R;
import com.jakebarnby.filemanager.util.Constants;

/**
 * Created by Jake on 6/18/2017.
 */

public class ErrorDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String message = getArguments().getString(Constants.ERROR_MESSAGE_KEY);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.err_generic));
        builder.setMessage(message);
        builder.setPositiveButton("OK",(dialog,which)-> dialog.dismiss());
        builder.setNegativeButton("Cancel",(dialog,which)->dialog.dismiss());
        return builder.create();
    }
}
