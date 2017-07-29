package com.jakebarnby.filemanager.models;

import android.util.SparseArray;

import com.jakebarnby.filemanager.activities.source.SourceActivity;
import com.jakebarnby.filemanager.models.files.SourceFile;
import com.jakebarnby.filemanager.util.TreeNode;

/**
 * Created by Jake on 7/29/2017.
 */

public class SourceManager {

    private TreeNode<SourceFile>                    mActiveDirectory;
    private SparseArray<SourceActivity.FileAction>  mCurrentFileActions;

    public SourceManager() {
        this.mCurrentFileActions = new SparseArray<>();
    }

    public TreeNode<SourceFile> getActiveDirectory() {
        return mActiveDirectory;
    }

    public void setActiveDirectory(TreeNode<SourceFile> mActiveDirectory) {
        this.mActiveDirectory = mActiveDirectory;
    }

    public SourceActivity.FileAction getFileAction(int operationId) {
        return mCurrentFileActions.get(operationId);
    }

    public void addFileAction(int operationId, SourceActivity.FileAction action) {
        mCurrentFileActions.put(operationId, action);
    }
}
