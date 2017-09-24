package com.jakebarnby.filemanager.ui.adapters;

import com.jakebarnby.filemanager.sources.models.SourceFile;
import com.jakebarnby.filemanager.util.TreeNode;

public interface OnSearchResultClicked {
    void navigateToFile(TreeNode<SourceFile> toOpen);
}
