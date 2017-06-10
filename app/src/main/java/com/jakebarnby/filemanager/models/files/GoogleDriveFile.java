package com.jakebarnby.filemanager.models.files;


import android.net.Uri;

import com.google.api.services.drive.model.File;
import com.jakebarnby.filemanager.util.Constants;

/**
 * Created by Jake on 6/5/2017.
 */

public class GoogleDriveFile extends SourceFile {

    private String mDriveId;

    public GoogleDriveFile() {
    }

    public void setFileProperties(File file) {
        if (file.getWebContentLink() != null) {
            setUri(Uri.parse(file.getWebContentLink()));
        }
        setDriveId(file.getId());
        setName(file.getName());
        setSourceName(Constants.Sources.GOOGLE_DRIVE);
        setCanRead(true);
        setDirectory(file.getMimeType().equals(Constants.Sources.GOOGLE_DRIVE_FOLDER_MIME));
    }

    public String getDriveId() {
        return mDriveId;
    }

    public void setDriveId(String mDriveId) {
        this.mDriveId = mDriveId;
    }
}
