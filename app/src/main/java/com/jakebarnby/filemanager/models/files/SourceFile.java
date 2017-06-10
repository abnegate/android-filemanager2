package com.jakebarnby.filemanager.models.files;

import android.net.Uri;

import java.io.Serializable;

/**
 * Created by Jake on 6/5/2017.
 */

public abstract class SourceFile implements Serializable {

    private transient Uri   mUri;
    private String          mUriString;
    private String          mName;
    private String          mSourceName;
    private long            mSize;
    private boolean         mIsDirectory;
    private boolean         mCanRead;

    public SourceFile() {}

    public Uri getUri() {
        if (mUri == null) {
            mUri = Uri.parse(mUriString);
        }
        return mUri;
    }

    public void setUri(Uri mUri) {
        this.mUri = mUri;
        this.mUriString = mUri.getPath();
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

    public long getSize() {
        return mSize;
    }

    public void setSize(long size) {
        this.mSize = size;
    }

    public boolean isDirectory() {
        return mIsDirectory;
    }

    public void setDirectory(boolean directory) {
        mIsDirectory = directory;
    }

    public boolean canRead() {
        return mCanRead;
    }

    public void setCanRead(boolean mCanRead) {
        this.mCanRead = mCanRead;
    }
}
