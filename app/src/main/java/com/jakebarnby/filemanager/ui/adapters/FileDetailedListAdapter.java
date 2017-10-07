package com.jakebarnby.filemanager.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jakebarnby.filemanager.R;
import com.jakebarnby.filemanager.sources.models.SourceFile;
import com.jakebarnby.filemanager.util.Constants;
import com.jakebarnby.filemanager.util.TreeNode;
import com.jakebarnby.filemanager.util.Utils;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Jake on 9/30/2017.
 */

public class FileDetailedListAdapter extends FileAdapter {

    /**
     * Create a new FileAdapter instance with the given root tree node
     *
     * @param rootNode The root node of the file tree
     */
    public FileDetailedListAdapter(TreeNode<SourceFile> rootNode) {
        super(rootNode);
    }

    @Override
    public FileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_file_detailed_list, parent, false);
        return new FileDetailedViewHolder(view);
    }

    private class FileDetailedViewHolder extends FileViewHolder {

        private final TextView mSizeOrCountText;
        private final TextView mModifiedDateText;

        FileDetailedViewHolder(View itemView) {
            super(itemView);

            mSizeOrCountText = itemView.findViewById(R.id.txt_item_size);
            mModifiedDateText = itemView.findViewById(R.id.txt_item_modified_datetime);
        }

        @Override
        void bindHolder(TreeNode<SourceFile> currentDir) {
            super.bindHolder(currentDir);

            if (currentDir.getData().isDirectory()) {
                mSizeOrCountText.setText(currentDir.getChildren() == null ?
                        "0 items" :
                        currentDir.getChildren().size() + " items");
            } else {
                mSizeOrCountText.setText(String.format(
                        Locale.getDefault(),
                        "%.2f %s",
                        currentDir.getData().getSize() / Constants.BYTES_TO_MEGABYTE,
                        " MB"));
            }

            if (!currentDir.getData().getSourceName().equals(Constants.Sources.DROPBOX)) {
                String displayTime = Utils.getDisplayStringFromDate(currentDir.getData().getModifiedTime());
                mModifiedDateText.setText(displayTime);
            }
        }
    }
}
