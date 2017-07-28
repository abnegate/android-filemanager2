package com.jakebarnby.filemanager.managers;
import android.util.Log;

import com.jakebarnby.filemanager.util.Utils;
import com.microsoft.graph.concurrency.ChunkedUploadProvider;
import com.microsoft.graph.concurrency.IProgressCallback;
import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.extensions.DriveItem;
import com.microsoft.graph.extensions.DriveItemUploadableProperties;
import com.microsoft.graph.extensions.Folder;
import com.microsoft.graph.extensions.IGraphServiceClient;
import com.microsoft.graph.extensions.UploadSession;
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

            DriveItemUploadableProperties properties = new DriveItemUploadableProperties();
            properties.name = fileName;

            UploadSession uploadSession = mGraphClient
                    .getMe()
                    .getDrive()
                    .getItems(parentId)
                    .getChildren(fileName)
                    .getCreateUploadSession(properties)
                    .buildRequest()
                    .post();

            ChunkedUploadProvider provider = new ChunkedUploadProvider(uploadSession, mGraphClient, in, in.available(), DriveItem.class);
            provider.upload(null, new IProgressCallback() {
                @Override
                public void progress(long current, long max) {
                    Log.d("ONE DRIVE UPLOAD", current+"/"+max);
                }

                @Override
                public void success(Object o) {
                    Log.d("ONE DRIVE UPLOAD", "COMPLETE" + o.toString());
                }

                @Override
                public void failure(ClientException ex) {
                    ex.printStackTrace();
                    Log.e("ONE DRIVE UPLOAD", "FAILURE: " + ex.getLocalizedMessage());
                }
            });
        } catch (IOException | GraphServiceException e) {
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
}
