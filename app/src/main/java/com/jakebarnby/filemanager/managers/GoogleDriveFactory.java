package com.jakebarnby.filemanager.managers;

/**
 * Created by Jake on 6/9/2017.
 */

class GoogleDriveFactory {
    private static final GoogleDriveFactory ourInstance = new GoogleDriveFactory();

    static GoogleDriveFactory getInstance() {
        return ourInstance;
    }

    private GoogleDriveFactory() {
    }
}
