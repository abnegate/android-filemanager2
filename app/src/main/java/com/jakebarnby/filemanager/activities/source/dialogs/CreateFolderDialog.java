package com.jakebarnby.filemanager.activities.source.dialogs;

import android.app.Dialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
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
import com.jakebarnby.filemanager.models.files.SourceFile;
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

        final FrameLayout frame = new FrameLayout(getContext());
        final EditText input = new EditText(getContext());
        input.setHint(getString(R.string.new_folder_name));
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setMaxLines(1);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        int px = dpToPx(20);
        params.setMargins(px,px,px,px);
        input.setLayoutParams(params);

        frame.addView(input);
        builder.setView(frame);

        builder.setPositiveButton("OK", (dialog, which) -> {
            TreeNode<SourceFile> activeDirectory = ((SourceActivity)getActivity()).getActiveDirectory();
            String name = input.getText().toString();

            for(TreeNode<SourceFile> file: activeDirectory.getChildren()) {
                if (file.getData().isDirectory()) {
                    if (file.getData().getName().equalsIgnoreCase(name)) {
                        Snackbar.make(getActivity().getCurrentFocus(), "A folder with that name already exists here", Snackbar.LENGTH_LONG).show();
                        return;
                    }
                }
            }
            SourceTransferService.startActionCreateFolder(getContext(), activeDirectory, name);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        return builder.create();
    }

    private int dpToPx(int px) {
        Resources r = getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px, r.getDisplayMetrics());
    }
}
