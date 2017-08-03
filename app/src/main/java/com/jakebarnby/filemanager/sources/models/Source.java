package com.jakebarnby.filemanager.sources.models;

import android.content.Context;
import android.content.SharedPreferences;

import com.jakebarnby.filemanager.sources.SourceListener;
import com.jakebarnby.filemanager.util.Constants;
import com.jakebarnby.filemanager.util.TreeNode;
import com.jakebarnby.filemanager.util.Utils;

/**
 * Created by Jake on 7/29/2017.
 */

public abstract class Source {

    private String                  mSourceName;
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


    public Source(String sourceName, SourceListener listener) {
        this.mSourceName = sourceName;
        this.mSourceListener = listener;
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
        SharedPreferences prefs = context.getSharedPreferences(Constants.SharedPrefs.PREFS, Context.MODE_PRIVATE);
        String accessToken = prefs.getString(sourceName + "-access-token", null);
        return accessToken != null;
    }

    /**
     * If performing an action on a non-local directory, check internet
     */
    public boolean checkConnectionActive(Context context) {
        if (!getSourceName().equals(Constants.Sources.LOCAL)) {
            if (!Utils.isConnectionReady(context)) {
                mSourceListener.onNoConnection();
                return false;
            }
        }
        return true;
    }
}

