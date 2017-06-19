package com.jakebarnby.filemanager.models.files;

import android.net.Uri;

import com.jakebarnby.filemanager.util.Constants;

import java.io.File;

/**
 * Created by Jake on 6/5/2017.
 */

public class LocalFile extends SourceFile {

    public LocalFile() {
    }

    public void setFileProperties(File file) {
        setPath(file.getPath());
        setName(file.getName());
        setSourceName(Constants.Sources.LOCAL);
        setDirectory(file.isDirectory());
        setSize(file.length());
        setThumbnailLink(file.getAbsolutePath());
        setModifiedTime(file.lastModified());
    }
}
