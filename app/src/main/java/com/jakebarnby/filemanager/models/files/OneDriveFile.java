package com.jakebarnby.filemanager.models.files;

import android.net.Uri;

import com.jakebarnby.filemanager.util.Constants;
import com.microsoft.graph.extensions.DriveItem;

/**
 * Created by Jake on 6/7/2017.
 */

public class OneDriveFile extends SourceFile {

    private String mDriveId;

    public OneDriveFile(DriveItem file) {
        setPath(file.webUrl);
        setDriveId(file.id);
        setName(file.name);
        setSourceName(Constants.Sources.ONEDRIVE);
        setDirectory(file.folder != null);
        setSize(file.size);
        setCreatedTime(file.createdDateTime.getTimeInMillis());
        setModifiedTime(file.lastModifiedDateTime.getTimeInMillis());
        if (file.thumbnails != null && file.thumbnails.getCurrentPage().size() > 0) {
            setThumbnailLink(file.thumbnails.getCurrentPage().get(0).small.url);
        }
    }

    public String getDriveId() {
        return mDriveId;
    }

    public void setDriveId(String mDriveId) {
        this.mDriveId = mDriveId;
    }
}
