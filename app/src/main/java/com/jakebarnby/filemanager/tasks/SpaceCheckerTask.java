package com.jakebarnby.filemanager.tasks;

import android.os.AsyncTask;
import android.os.Environment;

import com.jakebarnby.filemanager.listeners.OnSpaceCheckListener;
import com.jakebarnby.filemanager.managers.DropboxFactory;
import com.jakebarnby.filemanager.managers.GoogleDriveFactory;
import com.jakebarnby.filemanager.managers.OneDriveFactory;
import com.jakebarnby.filemanager.util.Constants;
import com.jakebarnby.filemanager.util.Utils;

/**
 * Check if the given source has enough free space to allow a copy operation of the given size
 */
public class SpaceCheckerTask extends AsyncTask<Void, Void, Boolean> {

    private String                  sourceName;
    private long                    copySize;
    private OnSpaceCheckListener    onSpaceCheckListener;

    /**
     * Create a new instance
     * @param sourceName    The name of the target source
     * @param copySize      The size of all files to be copied
     * @param listener      The listener to callback; true if source has enough space, false otherwise.
     */
    public SpaceCheckerTask(String sourceName, long copySize, OnSpaceCheckListener listener) {
        this.sourceName             = sourceName;
        this.copySize               = copySize;
        this.onSpaceCheckListener   = listener;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        switch (sourceName) {
            case Constants.Sources.LOCAL:
                return (Utils.getFreeSpace(Environment.getExternalStorageDirectory()) > copySize);
            case Constants.Sources.DROPBOX:
                return (DropboxFactory.getInstance().getFreeSpace() > copySize);
            case Constants.Sources.GOOGLE_DRIVE:
                return (GoogleDriveFactory.getInstance().getFreeSpace() > copySize);
            case Constants.Sources.ONEDRIVE:
                return (OneDriveFactory.getInstance().getFreeSpace() > copySize);
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        onSpaceCheckListener.complete(result);
    }
}
