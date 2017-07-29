package com.jakebarnby.filemanager.models;

import com.jakebarnby.filemanager.models.files.SourceFile;
import com.jakebarnby.filemanager.util.TreeNode;

/**
 * Created by Jake on 7/29/2017.
 */

public class Source {

    private String                  mSourceName;
    private TreeNode<SourceFile>    mCurrentDirectory;
    private long                    mFreeSpace;
    private boolean                 mLoggedIn;
    private boolean                 mFilesLoaded;
    private boolean                 mMultiSelectEnabled;

    public Source(String sourceName) {
        this.mSourceName = sourceName;
    }

    public String getSourceName() {
        return mSourceName;
    }

    public TreeNode<SourceFile> getCurrentDirectory() {
        return mCurrentDirectory;
    }

    public void setCurrentDirectory(TreeNode<SourceFile> mCurrentDirectory) {
        this.mCurrentDirectory = mCurrentDirectory;
    }

    public long getFreeSpace() {
        return mFreeSpace;
    }

    public void setFreeSpace(long mFreeSpace) {
        this.mFreeSpace = mFreeSpace;
    }

    public void decreaseFreeSpace(long amount) {
        this.mFreeSpace -= amount;
    }

    public void increaseFreeSpace(long amount) {
        this.mFreeSpace += amount;
    }

    public boolean isLoggedIn() {
        return mLoggedIn;
    }

    public void setLoggedIn(boolean mLoggedIn) {
        this.mLoggedIn = mLoggedIn;
    }

    public boolean isFilesLoaded() {
        return mFilesLoaded;
    }

    public void setFilesLoaded(boolean mFilesLoaded) {
        this.mFilesLoaded = mFilesLoaded;
    }

    public boolean isMultiSelectEnabled() {
        return mMultiSelectEnabled;
    }

    public void setMultiSelectEnabled(boolean mMultiSelectEnabled) {
        this.mMultiSelectEnabled = mMultiSelectEnabled;
    }
}
