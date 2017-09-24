package com.jakebarnby.filemanager.sources.onedrive;

import com.jakebarnby.filemanager.sources.models.SourceFile;
import com.jakebarnby.filemanager.sources.models.SourceType;
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
        setSourceType(SourceType.REMOTE);
        setSourceName(Constants.Sources.ONEDRIVE);
        setDirectory(file.folder != null);
        setSize(file.size);
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
