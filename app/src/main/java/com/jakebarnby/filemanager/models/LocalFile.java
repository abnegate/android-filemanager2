package com.jakebarnby.filemanager.models;

import android.net.Uri;

import java.io.File;

/**
 * Created by Jake on 6/5/2017.
 */

public class LocalFile extends SourceFile {

    public LocalFile() {
    }

    public LocalFile(Uri uri, String name) {
        super(uri, name);
    }

    public void setFileProperties(File file) {
        setUri(Uri.parse(file.getAbsolutePath()));
        setName(file.getName());
        setDirectory(file.isDirectory());
        setCanRead(file.canRead());
        setSize(file.length());
    }
}
