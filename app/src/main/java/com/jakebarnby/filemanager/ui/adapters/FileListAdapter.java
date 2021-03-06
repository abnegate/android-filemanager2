package com.jakebarnby.filemanager.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jakebarnby.filemanager.R;
import com.jakebarnby.filemanager.sources.models.SourceFile;
import com.jakebarnby.filemanager.util.TreeNode;

/**
 * Created by Jake on 5/31/2017.
 */

public class FileListAdapter extends FileAdapter {

    public FileListAdapter(TreeNode<SourceFile> rootNode) {
        super(rootNode);
    }

    @Override
    public FileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_file_list, parent, false);
        return new FileViewHolder(inflatedView);
    }
}
