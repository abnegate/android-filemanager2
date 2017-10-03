package com.jakebarnby.filemanager.sources.models;

import java.io.Serializable;

/**
 * Created by Jake on 6/5/2017.
 */

public abstract class SourceFile implements Serializable {
    private String          mPath;
    private String          mName;
    private String          mSourceName;
    private SourceType      mSourceType;
    private String          mThumbnailLink;
    private boolean         mIsDirectory;
    private boolean         mIsHidden;
    private long            mSize;
    private long            mModifiedTime;
    private int             mPositionToRestore = -1;

    public String getPath() {
        return mPath;
    }
    public void setPath(String path) {
        this.mPath = path;
    }
    public String getName() {
        return mName;
    }
    public void setName(String mName) {
        this.mName = mName;
    }
    public String getSourceName() {
        return mSourceName;
    }
    public void setSourceName(String mSourceName) {
        this.mSourceName = mSourceName;
    }
    public SourceType getSourceType() {
        return mSourceType;
    }
    public void setSourceType(SourceType mSourceType) {
        this.mSourceType = mSourceType;
    }
    public String getThumbnailLink() {
        return mThumbnailLink;
    }
    public void setThumbnailLink(String mThumbnailLink) {
        this.mThumbnailLink = mThumbnailLink;
    }
    public long getSize() {
        return (long) mSize;
    }
    public void setSize(long size) {
        this.mSize = size;
    }
    public void addSize(long size) {
        this.mSize += size;
    }
    public void removeSize(long size) { this.mSize -= size; }
    public boolean isDirectory() {
        return mIsDirectory;
    }
    public void setDirectory(boolean directory) {
        mIsDirectory = directory;
    }
    public long getModifiedTime() {
        return (long) mModifiedTime;
    }
    public void setModifiedTime(long mModifiedTime) {
        this.mModifiedTime = mModifiedTime;
    }
    public int getPositionToRestore() {
        return mPositionToRestore;
    }
    public void setPositionToRestore(int mPositionToRestore) {
        this.mPositionToRestore = mPositionToRestore;
    }
    public boolean isHidden() {
        return mIsHidden;
    }
    public void setHidden(boolean isHidden) {
        mIsHidden = isHidden;
    }
}
