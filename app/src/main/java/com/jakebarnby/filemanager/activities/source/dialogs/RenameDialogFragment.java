package com.jakebarnby.filemanager.activities.source.dialogs;

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
import com.jakebarnby.filemanager.activities.source.SourceActivity;
import com.jakebarnby.filemanager.managers.SelectedFilesManager;
import com.jakebarnby.filemanager.services.SourceTransferService;
import com.jakebarnby.filemanager.util.Constants;

/**
 * Created by Jake on 6/18/2017.
 */

public class RenameDialogFragment extends DialogFragment{

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getArguments().getString(Constants.DIALOG_TITLE_KEY));

        final FrameLayout frame = new FrameLayout(getContext());
        final EditText input = new EditText(getContext());
        input.setHint(SelectedFilesManager.getInstance().getSelectedFiles().get(0).getData().getName());
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
            SourceTransferService.startActionRename(
                    getContext(),
                    //FIXME: Shouldn't just get first index
                    SelectedFilesManager.getInstance().getSelectedFiles().get(0),
                    input.getText().toString());
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        return builder.create();
    }

    private int dpToPx(int px) {
        Resources r = getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px, r.getDisplayMetrics());
    }
}
