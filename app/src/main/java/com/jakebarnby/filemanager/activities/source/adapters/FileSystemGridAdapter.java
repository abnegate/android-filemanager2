package com.jakebarnby.filemanager.activities.source.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jakebarnby.filemanager.R;
import com.jakebarnby.filemanager.models.SourceFile;
import com.jakebarnby.filemanager.util.TreeNode;

import java.util.List;

/**
 * Created by Jake on 5/31/2017.
 */

public class FileSystemGridAdapter extends FileSystemAdapter {

    public FileSystemGridAdapter(TreeNode<SourceFile> parent, List<TreeNode<SourceFile>> children) {
        super(parent, children);
    }

    @Override
    public FileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_single_file_grid, parent, false);
        return new FileViewHolder(inflatedView);
    }
}
