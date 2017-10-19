package com.jakebarnby.filemanager.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;

import com.jakebarnby.filemanager.R;
import com.jakebarnby.filemanager.managers.SelectedFilesManager;
import com.jakebarnby.filemanager.sources.models.SourceFile;
import com.jakebarnby.filemanager.services.SourceTransferService;
import com.jakebarnby.filemanager.util.Constants;
import com.jakebarnby.filemanager.util.TreeNode;

/**
 * Created by Jake on 6/18/2017.
 */

public class CreateFolderDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getArguments().getString(Constants.DIALOG_TITLE_KEY));

        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_create_folder, null);
        EditText input = view.findViewById(R.id.txt_new_folder_name);
        builder.setView(view);

        builder.setPositiveButton(getString(R.string.ok), (dialog, which) -> {
            TreeNode<SourceFile> activeDirectory =
                    SelectedFilesManager.getInstance().getActionableDirectory(
                            SelectedFilesManager.getInstance().getOperationCount());

            String name = input.getText().toString();

            for(TreeNode<SourceFile> file: activeDirectory.getChildren()) {
                if (file.getData().isDirectory()) {
                    if (file.getData().getName().equals(name)) {
                        Snackbar.make(getActivity().getCurrentFocus(), getString(R.string.folder_exists), Snackbar.LENGTH_LONG).show();
                        return;
                    }
                }
            }
            SourceTransferService.startActionCreateFolder(getContext(), name);
            SelectedFilesManager.getInstance().addNewSelection();
        });
        builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss());
        return builder.create();
    }
}
