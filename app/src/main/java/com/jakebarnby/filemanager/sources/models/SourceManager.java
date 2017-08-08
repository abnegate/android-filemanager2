package com.jakebarnby.filemanager.sources.models;

import android.util.SparseArray;

import com.jakebarnby.filemanager.sources.SourceActivity;
import com.jakebarnby.filemanager.sources.dropbox.DropboxSource;
import com.jakebarnby.filemanager.sources.googledrive.GoogleDriveSource;
import com.jakebarnby.filemanager.sources.local.LocalSource;
import com.jakebarnby.filemanager.sources.onedrive.OneDriveSource;
import com.jakebarnby.filemanager.util.Constants;
import com.jakebarnby.filemanager.util.TreeNode;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jake on 7/29/2017.
 */

public class SourceManager {

    private Map<String, Source>                     mSources;
    private TreeNode<SourceFile>                    mActiveDirectory;
    private SparseArray<SourceActivity.FileAction>  mCurrentFileActions;

    public SourceManager() {
        this.mSources = new HashMap<>();
        this.mSources.put(Constants.Sources.LOCAL,          new LocalSource(Constants.Sources.LOCAL));
        this.mSources.put(Constants.Sources.DROPBOX,        new DropboxSource(Constants.Sources.DROPBOX));
        this.mSources.put(Constants.Sources.GOOGLE_DRIVE,   new GoogleDriveSource(Constants.Sources.GOOGLE_DRIVE));
        this.mSources.put(Constants.Sources.ONEDRIVE,       new OneDriveSource(Constants.Sources.ONEDRIVE));

        this.mCurrentFileActions = new SparseArray<>();
    }

    public Map<String, Source> getSources() {
        return mSources;
    }

    public Source getSource(String source) {
        return this.mSources.get(source);
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
