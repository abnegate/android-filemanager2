package com.jakebarnby.filemanager.sources.local;

import com.jakebarnby.filemanager.sources.models.SourceFile;
import com.jakebarnby.filemanager.sources.models.SourceType;
import com.jakebarnby.filemanager.util.Constants;

import java.io.File;

/**
 * Created by Jake on 6/5/2017.
 */

public class LocalFile extends SourceFile {

    public LocalFile(File file, String sourceName) {
        setPath(file.getPath());
        setName(file.getName());
        setSourceType(SourceType.LOCAL);
        setSourceName(sourceName);
        setDirectory(file.isDirectory());
        setSize(file.length());
        setThumbnailLink(file.getAbsolutePath());
        setModifiedTime(file.lastModified());
    }
}
