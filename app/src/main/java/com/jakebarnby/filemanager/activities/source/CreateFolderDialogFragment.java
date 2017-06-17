package com.jakebarnby.filemanager.activities.source;

import android.app.Dialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.jakebarnby.filemanager.R;
import com.jakebarnby.filemanager.services.SourceTransferService;

/**
 * Created by Jake on 6/18/2017.
 */

public class CreateFolderDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.create_folder));

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
            SourceTransferService.startActionCreateFolder(
                    getContext(),
                    input.getText().toString(),
                    ((SourceActivity)getActivity()).getActiveDirectory());
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        return builder.create();
    }

    private int dpToPx(int px) {
        Resources r = getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px, r.getDisplayMetrics());
    }
}
