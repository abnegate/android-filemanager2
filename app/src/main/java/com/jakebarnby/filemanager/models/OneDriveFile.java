package com.jakebarnby.filemanager.models;

import android.net.Uri;

import com.microsoft.graph.extensions.DriveItem;

/**
 * Created by Jake on 6/7/2017.
 */

public class OneDriveFile extends SourceFile {
    public void setFileProperties(DriveItem file) {
        setUri(Uri.parse(file.webUrl));
        setName(file.name);
        setCanRead(true);
        setDirectory(file.folder != null);
    }
}
