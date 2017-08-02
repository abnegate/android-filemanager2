package com.jakebarnby.filemanager.sources.dropbox;

import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.Metadata;
import com.jakebarnby.filemanager.sources.models.SourceFile;
import com.jakebarnby.filemanager.util.Constants;

/**
 * Created by Jake on 6/5/2017.
 */

public class DropboxFile extends SourceFile {

    public DropboxFile(Metadata file) {
        if (file.getPathDisplay() != null) {
            setPath(file.getPathDisplay());
        } else {
            setPath("");
        }
        setName(file.getName());
        setSourceName(Constants.Sources.DROPBOX);
        setDirectory(file instanceof FolderMetadata);
    }
}