package com.jakebarnby.filemanager.managers;
import com.jakebarnby.filemanager.models.SourceStorageStats;
import com.jakebarnby.filemanager.util.Constants;
import com.jakebarnby.filemanager.util.Utils;
import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.extensions.DriveItem;
import com.microsoft.graph.extensions.Folder;
import com.microsoft.graph.extensions.IGraphServiceClient;
import com.microsoft.graph.extensions.Quota;
import com.microsoft.graph.http.GraphServiceException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Jake on 6/9/2017.
 */

public class OneDriveFactory {
    private static final OneDriveFactory ourInstance = new OneDriveFactory();

    public static OneDriveFactory getInstance() {
        return ourInstance;
    }

    private IGraphServiceClient mGraphClient;

    private OneDriveFactory() {
    }

    public IGraphServiceClient getGraphClient() {
        return mGraphClient;
    }

    public void setGraphClient(IGraphServiceClient mGraphClient) {
        this.mGraphClient = mGraphClient;
    }

    /**
     *
     * @param id
     * @param filename
     * @param destinationPath
     * @return
     */
    public File downloadFile(String id, String filename, String destinationPath) {
        File file = new File(destinationPath, filename);

        try(InputStream inputStream = mGraphClient
                                    .getMe()
                                    .getDrive()
                                    .getItems(id)
                                    .getContent()
                                    .buildRequest()
                                    .get()) {
            Utils.copyInputStreamToFile(inputStream, file);
            return file;
        } catch (IOException | ClientException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *  @param filePath
     * @param fileName
     * @param parentId
     */
    public DriveItem uploadFile(String filePath, String fileName, String parentId) {
        File file = new File(filePath);

        try(FileInputStream in = new FileInputStream(file)) {
            byte[] buffer = new byte[(int)file.length()];
            in.read(buffer);

            return mGraphClient
                    .getMe()
                    .getDrive()
                    .getItems(parentId)
                    .getChildren(transformFileName(fileName))
                    .getContent()
                    .buildRequest()
                    .put(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *
     * @param driveId
     */
    public void deleteFile(String driveId) {
        mGraphClient
                .getMe()
                .getDrive()
                .getItems(driveId)
                .buildRequest()
                .delete();
    }

    /**
     *  @param name
     * @param parentId
     */
    public DriveItem createFolder(String name, String parentId) {
        DriveItem item = new DriveItem();
        item.name = name;
        item.folder = new Folder();

        return mGraphClient
                .getMe()
                .getDrive()
                .getItems(parentId)
                .getChildren()
                .buildRequest()
                .post(item);
    }

    public DriveItem rename(String newName, String itemId) {
        DriveItem item = new DriveItem();
        item.name = newName;
        return mGraphClient
                .getMe()
                .getDrive()
                .getItems(itemId)
                .buildRequest()
                .patch(item);
    }

    public SourceStorageStats getStorageStats() {
        try {
            Quota quota = mGraphClient
                    .getMe()
                    .getDrive()
                    .buildRequest()
                    .select("quota")
                    .get()
                    .quota;

            SourceStorageStats info = new SourceStorageStats();
            info.setTotalSpace(quota.total);
            info.setUsedSpace(quota.used);
            info.setFreeSpace(quota.remaining);

            return info;
        } catch (GraphServiceException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String transformFileName(String fileName) {
        String newName = fileName.replaceAll(Constants.Sources.ONEDRIVE_INVALID_CHARS, "%20");
        if (newName.length() > Constants.MAX_FILENAME_LENGTH) {
            newName = newName.substring(0, Constants.MAX_FILENAME_LENGTH);
        }
        return newName;
    }
}
