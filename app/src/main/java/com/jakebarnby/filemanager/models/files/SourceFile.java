package com.jakebarnby.filemanager.models.files;

import android.net.Uri;

import com.dropbox.core.v2.files.Metadata;
import com.google.api.services.drive.model.File;
import com.microsoft.graph.extensions.DriveItem;

import java.io.Serializable;

/**
 * Created by Jake on 6/5/2017.
 */

public abstract class SourceFile implements Serializable {
    private String          mPath;
    private String          mName;
    private String          mSourceName;
    private String          mThumbnailLink;
    private boolean         mIsDirectory;
    private long            mSize;
    private long            mCreatedTime;
    private long            mModifiedTime;

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
}
