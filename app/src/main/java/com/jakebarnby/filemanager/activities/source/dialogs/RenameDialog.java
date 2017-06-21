package com.jakebarnby.filemanager.activities.source.dialogs;

import android.app.Dialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.jakebarnby.filemanager.R;
import com.jakebarnby.filemanager.activities.source.SourceActivity;
import com.jakebarnby.filemanager.managers.SelectedFilesManager;
import com.jakebarnby.filemanager.services.SourceTransferService;
import com.jakebarnby.filemanager.util.Constants;

/**
 * Created by Jake on 6/18/2017.
 */

public class RenameDialog extends DialogFragment{

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getArguments().getString(Constants.DIALOG_TITLE_KEY));

        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_rename, null);
        EditText input = view.findViewById(R.id.text_rename);
        String name = SelectedFilesManager
                .getInstance()
                .getSelectedFiles()
                .get(0)
                .getData()
                .getName();

        input.setText(name.lastIndexOf('.') > 0 ?
                name.substring(0, name.lastIndexOf('.')) :
                name
        );
        input.setSelection(input.getText().length());

        builder.setView(view);
        builder.setPositiveButton("OK", (dialog, which) -> {
            String newName = name.lastIndexOf('.') > 0 ?
                    input.getText().toString()+name.substring(name.lastIndexOf('.')) :
                    input.getText().toString();

            SourceTransferService.startActionRename(getContext(), newName);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        return builder.create();
    }
}
