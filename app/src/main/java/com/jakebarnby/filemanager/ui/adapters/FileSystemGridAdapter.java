package com.jakebarnby.filemanager.ui.adapters;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.util.ViewPreloadSizeProvider;
import com.jakebarnby.filemanager.R;
import com.jakebarnby.filemanager.sources.models.SourceFile;
import com.jakebarnby.filemanager.util.TreeNode;

/**
 * Created by Jake on 5/31/2017.
 */

public class FileSystemGridAdapter extends FileSystemAdapter {

    public FileSystemGridAdapter(TreeNode<SourceFile> rootNode) {
        super(rootNode);
    }

    @Override
    public FileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_file_grid, parent, false);
        return new FileViewHolder(inflatedView);
    }
}
