package com.jakebarnby.filemanager.sources.googledrive;

import android.content.Context;
import android.content.SharedPreferences;
import android.webkit.MimeTypeMap;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.About;
import com.jakebarnby.filemanager.sources.models.SourceStorageStats;
import com.jakebarnby.filemanager.util.Constants;
import com.jakebarnby.filemanager.util.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;

/**
 * Created by Jake on 6/9/2017.
 */

public class GoogleDriveFactory {

    private static final GoogleDriveFactory sInstance = new GoogleDriveFactory();

    public static GoogleDriveFactory getInstance() {
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
     * @param fileId
     * @param destinationPath
     * @return
     */
    public File downloadFile(String fileId, String destinationPath) throws IOException {
        File file = new File(destinationPath);
        OutputStream outputStream = new FileOutputStream(file);
        mService.files()
                .get(fileId)
                .executeMediaAndDownloadTo(outputStream);
        return file;
    }

    /**
     * Upload a file at the given path on Google Drive
     *
     * @param filePath Path to upload the file to
     */
    public com.google.api.services.drive.model.File uploadFile(String filePath, String fileName, String parentId) throws IOException {
        File file = new File(filePath);
        com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();

        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        String mimeType = mimeTypeMap.getMimeTypeFromExtension(Utils.fileExt(filePath));

        fileMetadata.setParents(Collections.singletonList(parentId));
        fileMetadata.setMimeType(mimeType);
        fileMetadata.setHasThumbnail(true);
        fileMetadata.setName(fileName);

        FileContent googleFile = new FileContent(mimeType, file);
        return mService.files()
                .create(fileMetadata, googleFile)
                .setFields("name,id,mimeType,parents,size,hasThumbnail,thumbnailLink,iconLink,modifiedTime")
                .execute();
    }

    /**
     * Delete the gile with the given ID from Google Drive
     *
     * @param fileId The ID of the file to delete
     */
    public void deleteFile(String fileId) throws IOException {
        mService.files().delete(fileId).execute();
    }

    /**
     * @param name
     * @param parentId
     */
    public com.google.api.services.drive.model.File createFolder(String name, String parentId) throws IOException {
        com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
        fileMetadata.setName(name);
        fileMetadata.setMimeType(Constants.Sources.GOOGLE_DRIVE_FOLDER_MIME);

        return mService
                .files()
                .create(fileMetadata)
                .setFields("name,id,mimeType,parents,size,hasThumbnail,thumbnailLink,iconLink,modifiedTime")
                .execute();
    }

    public void logout(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.Prefs.PREFS, Context.MODE_PRIVATE);
        prefs.edit().putString(Constants.Prefs.GOOGLE_TOKEN_KEY, null).apply();
        prefs.edit().putString(Constants.Prefs.GOOGLE_NAME_KEY, null).apply();
    }

    /**
     * @param newName
     * @param parentId
     * @return
     */
    public com.google.api.services.drive.model.File rename(String newName, String parentId) throws IOException {
        com.google.api.services.drive.model.File file = new com.google.api.services.drive.model.File();
        return mService.files()
                .update(parentId, file)
                .setFields("name")
                .execute();
    }

    public SourceStorageStats getStorageStats() {
        try {
            About.StorageQuota quota = mService
                    .about()
                    .get()
                    .setFields("storageQuota")
                    .execute()
                    .getStorageQuota();


            SourceStorageStats info = new SourceStorageStats();
            info.setTotalSpace(quota.getLimit());
            info.setUsedSpace(quota.getUsage());
            info.setFreeSpace(quota.getLimit() - (quota.getUsage()));

            return info;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
