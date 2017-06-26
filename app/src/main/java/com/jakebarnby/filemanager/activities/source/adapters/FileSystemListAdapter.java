package com.jakebarnby.filemanager.activities.source.adapters;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.util.ViewPreloadSizeProvider;
import com.jakebarnby.filemanager.R;
import com.jakebarnby.filemanager.models.files.SourceFile;
import com.jakebarnby.filemanager.util.TreeNode;

/**
 * Created by Jake on 5/31/2017.
 */

public class FileSystemListAdapter extends FileSystemAdapter {

    public FileSystemListAdapter(TreeNode<SourceFile> rootNode,
                                 RequestBuilder<Drawable> requestBuilder,
                                 ViewPreloadSizeProvider sizeProvider) {
        super(rootNode, requestBuilder, sizeProvider);
    }

    @Override
    public FileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_file_list, parent, false);
        return new FileViewHolder(inflatedView);
    }
}
