package com.jakebarnby.filemanager.managers;

import com.microsoft.graph.extensions.IGraphServiceClient;

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

    public void downloadFile() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void uploadFile() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void deleteFile(String driveId) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
