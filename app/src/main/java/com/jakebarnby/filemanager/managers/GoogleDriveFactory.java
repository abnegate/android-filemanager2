package com.jakebarnby.filemanager.managers;

import android.os.Environment;
import android.webkit.MimeTypeMap;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.jakebarnby.filemanager.util.Constants;
import com.jakebarnby.filemanager.util.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;

/**
 * Created by Jake on 6/9/2017.
 */

public class GoogleDriveFactory {

    private static final GoogleDriveFactory sInstance = new GoogleDriveFactory();

    public static GoogleDriveFactory Instance() {
        return sInstance;
    }

    private Drive mService;

    private GoogleDriveFactory() {
    }

    public void setService(Drive mService) {
        this.mService = mService;
    }

    public Drive getService() {
        return mService;
    }

    /**
     *
     * @param fileId
     * @param destinationPath
     * @return
     */
    public File downloadFile(String fileId, String destinationPath) {
        File file = new File(destinationPath);
        try(OutputStream outputStream = new FileOutputStream(file)) {
            mService.files()
                    .get(fileId)
                    .executeAndDownloadTo(outputStream);
            return file;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Upload a file at the given path on Google Drive
     * @param filePath  Path to upload the file to
     */
    public void uploadFile(String filePath, String fileName, String parentId) {
        File file = new File(filePath);
        com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
        fileMetadata.setParents(Collections.singletonList(parentId));
        fileMetadata.setName(fileName);

        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        String mimeType = mimeTypeMap.getMimeTypeFromExtension(Utils.fileExt(filePath));

        FileContent googleFile = new FileContent(mimeType, file);
        try {
            mService.files()
                    .create(fileMetadata, googleFile)
                    .setFields("id")
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("File uploaded");
    }

    /**
     * Delete the gile with the given ID from Google Drive
     * @param fileId    The ID of the file to delete
     */
    public void deleteFile(String fileId) {
        try {
            mService.files().delete(fileId).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 
     * @param name
     * @param parentId
     */
    public void createFolder(String name, String parentId) {
        com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
        fileMetadata.setName(name);
        fileMetadata.setMimeType(Constants.GOOGLE_DRIVE_FOLDER_MIME);
        fileMetadata.setParents(Collections.singletonList(parentId));
        try {
            com.google.api.services.drive.model.File file = mService
                                                                .files()
                                                                .create(fileMetadata)
                                                                .setFields("id")
                                                                .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
