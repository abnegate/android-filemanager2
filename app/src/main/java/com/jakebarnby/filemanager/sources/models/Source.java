package com.jakebarnby.filemanager.sources.models;

import android.content.Context;

import com.jakebarnby.filemanager.sources.SourceListener;
import com.jakebarnby.filemanager.util.Constants;
import com.jakebarnby.filemanager.util.PreferenceUtils;
import com.jakebarnby.filemanager.util.TreeNode;
import com.jakebarnby.filemanager.util.Utils;

/**
 * Created by Jake on 7/29/2017.
 */

public abstract class Source {

    private String                  mSourceName;
    private SourceType              mSourceType;
    private TreeNode<SourceFile>    mRootNode;
    private TreeNode<SourceFile>    mCurrentDirectory;
    private long                    mTotalSpace;
    private long                    mUsedSpace;
    private long                    mFreeSpace;
    private boolean                 mLoggedIn;
    private boolean                 mFilesLoaded;
    private boolean                 mMultiSelectEnabled;

    protected SourceListener        mSourceListener;

    /**
     * Authenticate the current mSource
     */
    public abstract void authenticateSource(Context context);

    /**
     * Load the current mSource
     */
    public abstract void loadSource(Context context);

    public abstract void logout(Context context);


    public Source(SourceType type, String sourceName, SourceListener listener) {
        this.mSourceType = type;
        this.mSourceName = sourceName;
        this.mSourceListener = listener;
    }

    public String getSourceName() {
        return mSourceName;
    }

    public SourceType getSourceType() {
        return mSourceType;
    }

    public void setSourceType(SourceType mSourceType) {
        this.mSourceType = mSourceType;
    }

    public TreeNode<SourceFile> getCurrentDirectory() {
        return mCurrentDirectory;
    }

    public void setCurrentDirectory(TreeNode<SourceFile> mCurrentDirectory) {
        this.mCurrentDirectory = mCurrentDirectory;
    }

    public TreeNode<SourceFile> getRootNode() {
        return mRootNode;
    }

    public void setRootNode(TreeNode<SourceFile> mRootNode) {
        this.mRootNode = mRootNode;
    }

    public void setQuotaInfo(SourceStorageStats info) {
        this.mTotalSpace = info.getTotalSpace();
        this.mUsedSpace = info.getUsedSpace();
        this.mFreeSpace = info.getFreeSpace();
    }

    public long getTotalSpace() {
        return mTotalSpace;
    }

    public double getTotalSpaceGB() {
        return mTotalSpace / Constants.BYTES_TO_GIGABYTE;
    }

    public void setTotalSpace(long mTotalSpace) {
        this.mTotalSpace = mTotalSpace;
    }

    public long getUsedSpace() {
        return mUsedSpace;
    }

    public double getUsedSpaceGB() { return mUsedSpace / Constants.BYTES_TO_GIGABYTE; }

    public int getUsedSpacePercent() {
        return (int) (100 * mUsedSpace / mTotalSpace);
    }

    public void setUsedSpace(long mUsedSpace) {
        this.mUsedSpace = mUsedSpace;
    }

    public long getFreeSpace() {
        return mFreeSpace;
    }

    public double getFreeSpaceGB() { return mFreeSpace / Constants.BYTES_TO_GIGABYTE; }

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

    /**
     * Checks if this mSource has a valid access token
     * @return  Whether there is a valid access token for this mSource
     */
    public boolean hasToken(Context context, String sourceName) {
        String accessToken = PreferenceUtils.getString(context,sourceName.toLowerCase() + "-access-token", null);
        return accessToken != null;
    }

    /**
     * If performing an action on a non-local directory, check internet
     */
    public boolean checkConnectionActive(Context context) {
        if (getSourceType() == SourceType.REMOTE) {
            if (!Utils.isConnectionReady(context)) {
                mSourceListener.onNoConnection();
                return false;
            }
        }
        return true;
    }
}

