package com.jakebarnby.filemanager.managers;

/**
 * Created by Jake on 6/9/2017.
 */

class OneDriveFactory {
    private static final OneDriveFactory ourInstance = new OneDriveFactory();

    static OneDriveFactory getInstance() {
        return ourInstance;
    }

    private OneDriveFactory() {
    }
}
