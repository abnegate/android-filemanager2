package com.jakebarnby.filemanager.sources.models;

import android.util.SparseArray;

import com.jakebarnby.filemanager.models.FileAction;
import com.jakebarnby.filemanager.sources.SourceActivity;
import com.jakebarnby.filemanager.util.TreeNode;

/**
 * Created by Jake on 7/29/2017.
 */

public class SourceManager {

    private TreeNode<SourceFile>                    mActiveDirectory;
    private SparseArray<FileAction>  mCurrentFileActions;

    public SourceManager() {
        this.mCurrentFileActions = new SparseArray<>();
    }

    public TreeNode<SourceFile> getActiveDirectory() {
        return mActiveDirectory;
    }

    public void setActiveDirectory(TreeNode<SourceFile> mActiveDirectory) {
        this.mActiveDirectory = mActiveDirectory;
    }

    public FileAction getFileAction(int operationId) {
        return mCurrentFileActions.get(operationId);
    }

    public void addFileAction(int operationId, FileAction action) {
        mCurrentFileActions.put(operationId, action);
    }
}
