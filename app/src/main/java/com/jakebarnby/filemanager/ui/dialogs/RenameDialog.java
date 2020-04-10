package com.jakebarnby.filemanager.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import android.view.View;
import android.widget.EditText;

import com.jakebarnby.filemanager.R;
import com.jakebarnby.filemanager.sources.SourceActivity;
import com.jakebarnby.filemanager.managers.SelectedFilesManager;
import com.jakebarnby.filemanager.sources.models.SourceFile;
import com.jakebarnby.filemanager.services.SourceTransferService;
import com.jakebarnby.filemanager.util.Constants;
import com.jakebarnby.filemanager.util.TreeNode;

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
                .getCurrentSelectedFiles()
                .get(0)
                .getData()
                .getName();

        input.setText(name.lastIndexOf('.') > 0 ?
                name.substring(0, name.lastIndexOf('.')) :
                name
        );
        input.setSelection(input.getText().length());

        builder.setView(view);
        builder.setPositiveButton(getString(R.string.ok), (dialog, which) -> {
            TreeNode<SourceFile> activeDirectory = ((SourceActivity)getActivity()).getSourceManager().getActiveDirectory();
            String newName = name.lastIndexOf('.') > 0 ?
                    input.getText().toString()+name.substring(name.lastIndexOf('.')) :
                    input.getText().toString();
            for(TreeNode<SourceFile> file: activeDirectory.getChildren()) {
                if (file.getData().getName().equalsIgnoreCase(newName)) {
                    Snackbar.make(getActivity().getCurrentFocus(), getString(R.string.file_exists), Snackbar.LENGTH_LONG).show();
                    return;
                }
            }
            SourceTransferService.startActionRename(getContext(), newName);
            SelectedFilesManager.getInstance().addNewSelection();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        return builder.create();
    }
}
