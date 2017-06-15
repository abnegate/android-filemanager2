package com.jakebarnby.filemanager.managers;

import android.os.Environment;
import android.webkit.MimeTypeMap;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.jakebarnby.filemanager.util.Utils;

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
     * Download a file from Google Drive with the given id and the given path name
     * @param fileId        Id of the file to download
     * @param fileName      The name of the file to download
     * @throws IOException  Throws IO exception if an error occured when downloading the file
     */
    public java.io.File downloadFile(String fileId, String fileName) {
        try {
            java.io.File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            java.io.File file = new java.io.File(path+fileName);
            OutputStream outputStream = new FileOutputStream(file);
            mService.files()
                    .get(fileId)
                    .executeMediaAndDownloadTo(outputStream);
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
    public void uploadFile(String filePath, String parentId) {
        java.io.File file = new java.io.File(filePath);
        File fileMetadata = new File();
        fileMetadata.setParents(Collections.singletonList(parentId));
        fileMetadata.setName(filePath.substring(filePath.lastIndexOf("/")+1));

        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        String mimeType = mimeTypeMap.getMimeTypeFromExtension(Utils.fileExt(filePath));

        FileContent mediaContent = new FileContent(mimeType, file);
        try {
            mService.files().create(fileMetadata, mediaContent)
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
            mService.files().delete(fileId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
