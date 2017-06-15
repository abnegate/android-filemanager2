package com.jakebarnby.filemanager.models.files;

import android.net.Uri;

import com.jakebarnby.filemanager.util.Constants;
import com.microsoft.graph.extensions.DriveItem;

/**
 * Created by Jake on 6/7/2017.
 */

public class OneDriveFile extends SourceFile {

    private String mDriveId;

    public void setFileProperties(DriveItem file) {
        setUri(Uri.parse(file.webUrl));
        setDriveId(file.id);
        setName(file.name);
        setSourceName(Constants.Sources.ONEDRIVE);
        setCanRead(true);
        setDirectory(file.folder != null);
    }

    public String getDriveId() {
        return mDriveId;
    }

    public void setDriveId(String mDriveId) {
        this.mDriveId = mDriveId;
    }
}
