package com.jakebarnby.filemanager.models.files;

import android.net.Uri;

import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.MediaInfo;
import com.dropbox.core.v2.files.MediaMetadata;
import com.dropbox.core.v2.files.Metadata;
import com.jakebarnby.filemanager.util.Constants;

/**
 * Created by Jake on 6/5/2017.
 */

public class DropboxFile extends SourceFile {

    public DropboxFile() {
    }

    public void setFileProperties(Metadata file) {
        if (file instanceof FileMetadata) {
            FileMetadata data = (FileMetadata)file;
            setSize(data.getSize());
            setModifiedTime(data.getClientModified().getTime());
        }

        if (file.getPathDisplay() != null) {
            setUri(Uri.parse(file.getPathDisplay()));
        } else {
            setUri(Uri.parse(""));
        }
        setName(file.getName());
        setSourceName(Constants.Sources.DROPBOX);
        setCanRead(true);
        setDirectory(file instanceof FolderMetadata);

    }
}
