package com.jakebarnby.filemanager.models;

import com.jakebarnby.filemanager.models.files.SourceFile;
import com.jakebarnby.filemanager.util.TreeNode;

public interface SourceListener {
    void onCheckPermissions(String permission, int requestCode);
    void onNoConnection();
    void onLoadStarted();
    void onLoadAborted();
    void onAuthenticationComplete(boolean success);
    void onLoadComplete(TreeNode<SourceFile> rootFile);
}
