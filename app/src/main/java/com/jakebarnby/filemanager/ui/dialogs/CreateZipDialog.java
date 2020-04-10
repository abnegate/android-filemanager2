package com.jakebarnby.filemanager.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import android.view.View;
import android.widget.EditText;

import com.jakebarnby.filemanager.R;
import com.jakebarnby.filemanager.managers.SelectedFilesManager;
import com.jakebarnby.filemanager.services.SourceTransferService;
import com.jakebarnby.filemanager.sources.models.SourceFile;
import com.jakebarnby.filemanager.util.Constants;
import com.jakebarnby.filemanager.util.TreeNode;

/**
 * Created by Jake on 10/14/2017.
 */

public class CreateZipDialog extends DialogFragment {

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

            String name = input.getText().toString()+".zip";

            for(TreeNode<SourceFile> file: activeDirectory.getChildren()) {
                if (file.getData().isDirectory()) {
                    if (file.getData().getName().equals(name)) {
                        Snackbar.make(getActivity().getCurrentFocus(), getString(R.string.folder_exists), Snackbar.LENGTH_LONG).show();
                        return;
                    }
                }
            }
            SourceTransferService.startActionZip(getContext(), name);
            SelectedFilesManager.getInstance().addNewSelection();
        });
        builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss());
        return builder.create();
    }
}
