package com.jakebarnby.filemanager.models.files;


import android.net.Uri;

import com.google.api.services.drive.model.File;
import com.jakebarnby.filemanager.util.Constants;

/**
 * Created by Jake on 6/5/2017.
 */

public class GoogleDriveFile extends SourceFile {

    private String mDriveId;

    public GoogleDriveFile(File file) {
        if (file.getWebContentLink() != null) {
            setPath(file.getWebContentLink());
        }
        setDriveId(file.getId());
        setName(file.getName());
        setSourceName(Constants.Sources.GOOGLE_DRIVE);
        setDriveId(file.getId());
        setDirectory(file.getMimeType().equals(Constants.Sources.GOOGLE_DRIVE_FOLDER_MIME));
        setThumbnailLink(file.getHasThumbnail() ? file.getThumbnailLink() : file.getIconLink());
        setModifiedTime(file.getModifiedTime().getValue());
        if (!isDirectory()) {
            setSize(file.getSize() == null? 0 : file.getSize());
        }
        if (file.getWebViewLink() != null) {
            setPath(file.getWebViewLink());
        }
    }
    public String getDriveId() {
        return mDriveId;
    }

    public void setDriveId(String mDriveId) {
        this.mDriveId = mDriveId;
    }
}
