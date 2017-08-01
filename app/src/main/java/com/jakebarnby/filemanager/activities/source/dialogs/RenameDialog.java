package com.jakebarnby.filemanager.activities.source.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;

import com.jakebarnby.filemanager.R;
import com.jakebarnby.filemanager.managers.SelectedFilesManager;
import com.jakebarnby.filemanager.models.files.SourceFile;
import com.jakebarnby.filemanager.services.SourceTransferService;
import com.jakebarnby.filemanager.util.Constants;
import com.jakebarnby.filemanager.util.TreeNode;
import com.jakebarnby.filemanager.util.Utils;

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
        String name = SelectedFilesManager.getInstance()
                .getSelectedFiles(SelectedFilesManager.getInstance().getOperationCount())
                .get(0)
                .getData()
                .getName();

        input.setText(name.lastIndexOf('.') > 0 ?
                name.substring(0, name.lastIndexOf('.')) :
                name);
        input.setSelection(input.getText().length());

        builder.setView(view);

        builder.setPositiveButton("OK", (dialog, which) -> {
            TreeNode<SourceFile> destDir =
                    SelectedFilesManager.getInstance().getActionableDirectory(
                            SelectedFilesManager.getInstance().getOperationCount());

            if (input.getText().toString().length() <= 0) {
                Snackbar.make(getView(), R.string.err_no_name, Snackbar.LENGTH_LONG).show();
                return;
            }

            String uniqueName = Utils.generateUniqueFilename(input.getText().toString(), destDir);

            if (name.lastIndexOf('.') > 0) {
                uniqueName += name.substring(name.lastIndexOf('.'));
            }

            SourceTransferService.startActionRename(getContext(), uniqueName);
            SelectedFilesManager.getInstance().addNewSelection();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            SelectedFilesManager.getInstance().getSelectedFiles(
                    SelectedFilesManager.getInstance().getOperationCount()).clear();
            dialog.dismiss();
        });
        return builder.create();
    }
}
