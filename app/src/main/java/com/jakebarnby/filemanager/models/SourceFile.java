package com.jakebarnby.filemanager.models;

import android.net.Uri;

/**
 * Created by Jake on 6/5/2017.
 */

public abstract class SourceFile {

    private Uri mUri;
    private String mName;
    private long mSize;
    private boolean mIsDirectory;
    private boolean mCanRead;

    public SourceFile() {
    }

    public SourceFile(Uri uri) {
        this.mUri = uri;
    }

    public SourceFile(Uri uri, String name) {
        this.mUri = uri;
        this.mName = name;
    }

    public Uri getUri() {
        return mUri;
    }

    public void setUri(Uri mUri) {
        this.mUri = mUri;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
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
