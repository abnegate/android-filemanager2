package com.jakebarnby.filemanager.activities.source.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jakebarnby.filemanager.R;
import com.jakebarnby.filemanager.managers.SelectedFilesManager;
import com.jakebarnby.filemanager.models.files.SourceFile;
import com.jakebarnby.filemanager.util.TreeNode;

import java.util.Date;
import java.util.List;

/**
 * Created by Jake on 6/19/2017.
 */

public class PropertiesDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_properties, null);
        TextView nameText = view.findViewById(R.id.text_name);
        TextView sourceNameText = view.findViewById(R.id.text_sourceName);
        TextView pathText = view.findViewById(R.id.text_path);
        TextView sizeText = view.findViewById(R.id.text_size);
        TextView createdTimeText = view.findViewById(R.id.text_createdTime);
        TextView modifiedTimeText = view.findViewById(R.id.text_modifiedTime);

        List<TreeNode<SourceFile>> selected = SelectedFilesManager.getInstance().getSelectedFiles();

        if (selected.size() > 1) {
            float totalSize = 0.0f;
            String sourceNames = "";
            for (TreeNode<SourceFile> file : selected){
                if (!sourceNames.contains(file.getData().getSourceName())) {
                    sourceNames += file.getData().getSourceName()+" ";
                }
                totalSize += file.getData().getSize();
            }
            nameText.setText(String.format(getString(R.string.selected_item_count), selected.size()));
            pathText.setText("-");
            sourceNameText.setText(sourceNames);
            sizeText.setText(String.format(getString(R.string.sizeDisplay),totalSize/1024.0/1024.0));
            createdTimeText.setText("-");
            modifiedTimeText.setText("-");

        } else if (selected.size() == 1){
            SourceFile file = selected.get(0).getData();
            nameText.setText(file.getName());
            pathText.setText(file.getPath());
            sourceNameText.setText(file.getSourceName());
            sizeText.setText(String.format(getString(R.string.sizeDisplay),file.getSize()/1024.0/1024.0));
            createdTimeText.setText(new Date(file.getCreatedTime()).toString());
            modifiedTimeText.setText(new Date(file.getModifiedTime()).toString());
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.properties));
        builder.setView(view);
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        return builder.create();
    }
}
