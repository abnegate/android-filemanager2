package com.jakebarnby.filemanager.models;

import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.Metadata;

/**
 * Created by Jake on 6/5/2017.
 */

public class DropboxFile extends SourceFile {

    public DropboxFile() {
    }

    public void setFileProperties(Metadata file) {
        //setUri(Uri.parse(file.getPathLower()));
        setName(file.getName());
        setCanRead(true);
        setDirectory(file instanceof FolderMetadata);

    }
}
