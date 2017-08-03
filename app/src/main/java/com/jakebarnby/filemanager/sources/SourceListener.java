package com.jakebarnby.filemanager.sources;

import com.jakebarnby.filemanager.sources.models.SourceFile;
import com.jakebarnby.filemanager.util.TreeNode;

public interface SourceListener {
    void onCheckPermissions(String permission, int requestCode);
    void onNoConnection();
    void onLoadStarted();
    void onLoadAborted();
    void onLoadError(String errorMessage);
    void onLoadComplete(TreeNode<SourceFile> rootFile);
}
