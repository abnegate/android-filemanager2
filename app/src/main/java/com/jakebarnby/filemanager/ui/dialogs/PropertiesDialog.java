package com.jakebarnby.filemanager.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.TextView;

import com.jakebarnby.filemanager.R;
import com.jakebarnby.filemanager.managers.SelectedFilesManager;
import com.jakebarnby.filemanager.sources.models.SourceFile;
import com.jakebarnby.filemanager.util.Constants;
import com.jakebarnby.filemanager.util.TreeNode;

import java.util.Date;
import java.util.List;

/**
 * Created by Jake on 6/19/2017.
 */

public class PropertiesDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_properties, null);

        TextView nameText           = view.findViewById(R.id.txt_name);
        TextView sourceNameText     = view.findViewById(R.id.txt_source_name);
        TextView pathText           = view.findViewById(R.id.txt_path);
        TextView sizeText           = view.findViewById(R.id.txt_size);
        TextView modifiedTimeText   = view.findViewById(R.id.txt_modified_time);

        List<TreeNode<SourceFile>> selected =
                SelectedFilesManager.getInstance().getCurrentSelectedFiles();

        if (selected.size() > 1) {
            float totalSize = 0.0f;
            StringBuilder sourceNames = new StringBuilder();

            for (TreeNode<SourceFile> file : selected) {
                if (!sourceNames.toString().contains(file.getData().getSourceName())) {
                    sourceNames.append(file.getData().getSourceName()).append(", ");
                }
                totalSize += file.getData().getSize();
            }

            nameText.setText(String.format(getString(R.string.selected_item_count), selected.size()));
            sourceNameText.setText(sourceNames.toString());
            sizeText.setText(String.format(getString(R.string.size_display), totalSize / Constants.BYTES_TO_MEGABYTE));

            pathText.setVisibility(View.GONE);
            modifiedTimeText.setVisibility(View.GONE);
            view.findViewById(R.id.txt_path_label).setVisibility(View.GONE);
            view.findViewById(R.id.txt_modified_time_label).setVisibility(View.GONE);

        } else if (selected.size() == 1) {
            SourceFile file = selected.get(0).getData();
            nameText.setText(file.getName());
            pathText.setText(file.getPath());
            sourceNameText.setText(file.getSourceName());
            sizeText.setText(String.format(getString(R.string.size_display), file.getSize() / Constants.BYTES_TO_MEGABYTE));
            modifiedTimeText.setText(new Date(file.getModifiedTime()).toString());
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.properties));
        builder.setView(view);
        builder.setPositiveButton(getString(R.string.ok), (dialog, which) -> dialog.dismiss());
        return builder.create();
    }
}
