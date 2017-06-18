package com.jakebarnby.filemanager.models.files;

import android.net.Uri;

import com.jakebarnby.filemanager.util.Constants;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by Jake on 6/5/2017.
 */

public class LocalFile extends SourceFile {

    public LocalFile() {
    }

    public void setFileProperties(File file) {
        setUri(Uri.parse(file.getPath()));
        setName(file.getName());
        setSourceName(Constants.Sources.LOCAL);
        setDirectory(file.isDirectory());
        setCanRead(file.canRead());
        setSize(file.length());
        setThumbnailLink(file.getAbsolutePath());
        setModifiedTime(file.lastModified());
    }
}
