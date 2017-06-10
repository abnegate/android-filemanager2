package com.jakebarnby.filemanager.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.jakebarnby.filemanager.R;
import com.jakebarnby.filemanager.activities.source.SourceActivity;
import com.jakebarnby.filemanager.managers.DropboxFactory;
import com.jakebarnby.filemanager.managers.SelectedFilesManager;
import com.jakebarnby.filemanager.models.files.SourceFile;
import com.jakebarnby.filemanager.util.Constants;

import java.io.File;
import java.io.Serializable;
import java.util.List;

/**
 * Created by Jake on 6/6/2017.
 */

public class SourceTransferService extends IntentService {
    public static final String ACTION_COMPLETE = "com.jakebarnby.filemanager.services.action.COMPLETE";
    public static final String ACTION_SHOW_DIALOG = "com.jakebarnby.filemanager.services.action.SHOW_DIALOG";
    public static final String ACTION_UPDATE_DIALOG = "com.jakebarnby.filemanager.services.action.UPDATE_DIALOG";
    public static final String EXTRA_CURRENT_COUNT = "com.jakebarnby.filemanager.services.extra.CURRENT_COUNT";
    public static final String EXTRA_TOTAL_COUNT = "com.jakebarnby.filemanager.services.extra.TOTAL_COUNT";
    public static final int NOTIFICATION_ID = 100;
    private static final String ACTION_COPY = "com.jakebarnby.filemanager.services.action.COPY";
    private static final String ACTION_MOVE = "com.jakebarnby.filemanager.services.action.MOVE";
    private static final String ACTION_DELETE = "com.jakebarnby.filemanager.services.action.DELETE";
    private static final String EXTRA_SOURCE_FILES = "com.jakebarnby.filemanager.services.extra.SOURCE_FILES";
    private static final String EXTRA_SOURCE_DEST = "com.jakebarnby.filemanager.services.extra.SOURCE DESTINATION";

    static {
        System.loadLibrary("io-lib");
    }

    public native int copyFileNative(String sourcePath, String destinationPath);

    public native int deleteFileNative(String sourcePath);

    public SourceTransferService() {
        super("SourceTransferService");
    }

    /**
     * @param context
     * @param toCopy
     * @param sourceDest
     */
    public static void startActionCopy(Context context, List<SourceFile> toCopy, SourceFile sourceDest, boolean move) {
        Intent intent = new Intent(context, SourceTransferService.class);
        intent.setAction(move ? ACTION_MOVE : ACTION_COPY);
        intent.putExtra(EXTRA_SOURCE_FILES, (Serializable) toCopy);
        intent.putExtra(EXTRA_SOURCE_DEST, sourceDest);
        context.startService(intent);
    }

    /**
     * Start the service for a delete action with the given files
     *
     * @param context  Context for resources
     * @param toDelete The files to delete
     */
    public static void startActionDelete(Context context, List<SourceFile> toDelete) {
        Intent intent = new Intent(context, SourceTransferService.class);
        intent.setAction(ACTION_DELETE);
        intent.putExtra(EXTRA_SOURCE_FILES, (Serializable) toDelete);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final List<SourceFile> targets = (List<SourceFile>) intent.getSerializableExtra(EXTRA_SOURCE_FILES);
            final SourceFile sourceDest = (SourceFile) intent.getSerializableExtra(EXTRA_SOURCE_DEST);

            final String action = intent.getAction();
            switch (action) {
                case ACTION_COPY:
                    copy(targets, sourceDest, false);
                    break;
                case ACTION_MOVE:
                    copy(targets, sourceDest, true);
                    break;
                case ACTION_DELETE:
                    delete(targets);
                    break;
            }
        }
        hideNotification();
    }

    /**
     * Copy the given files to the given destination
     *
     * @param toCopy  The files to copy
     * @param destDir Where to copy them to
     */
    private int copy(List<SourceFile> toCopy, SourceFile destDir, boolean move) {
        showDialog(toCopy.size());
        int returnInt = -1;
        for (SourceFile file : toCopy) {
            String source = null;
            switch (file.getSourceName()) {
                case Constants.Sources.LOCAL:
                    source = file.getUri().getPath();
                    break;
                case Constants.Sources.DROPBOX:
                    File newFile = DropboxFactory.getInstance().downloadFile(file.getUri().getPath(), file.getName());
                    if (newFile.exists())
                        source = newFile.getPath();
                    else
                        returnInt = -1;
                    break;
                case Constants.Sources.GOOGLE_DRIVE:
                    //TODO: Download from google drive and get path as string
                    break;
                case Constants.Sources.ONEDRIVE:
                    //TODO: Download from onedrive and get path as string
                    break;
            }

            switch (destDir.getSourceName()) {
                case Constants.Sources.LOCAL:
                    returnInt = copyFileNative(source, destDir.getUri().getPath() + "/" + file.getName());
                    break;
                case Constants.Sources.DROPBOX:
                    DropboxFactory.getInstance().uploadFile(source, destDir.getUri().getPath());
                    break;
                case Constants.Sources.GOOGLE_DRIVE:
                    //TODO: Upload to gogle drive
                    break;
                case Constants.Sources.ONEDRIVE:
                    //TODO: Upload to onedrive
                    break;
            }
            postNotification("File Manager", "Copying " + (toCopy.indexOf(file) + 1) + " of " + toCopy.size());
            updateDialog(toCopy.indexOf(file) + 1);
        }
        if (move) {
            delete(toCopy);
        }
        postNotification("File Manager", "Copying complete!");

        Intent intent = new Intent();
        intent.setAction(ACTION_COMPLETE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        SelectedFilesManager.getInstance().getSelectedFiles().clear();
        return returnInt;
    }

    /**
     * Delete the given files
     *
     * @param toDelete The files to delete
     */
    private int delete(List<SourceFile> toDelete) {
        int returnInt = -1;
        for (SourceFile file : toDelete) {
            switch (file.getSourceName()) {
                case Constants.Sources.LOCAL:
                    returnInt = deleteFileNative(file.getUri().getPath());
                    break;
                case Constants.Sources.DROPBOX:
                    //TODO: Dropbox delete api call
                    break;
                case Constants.Sources.GOOGLE_DRIVE:
                    //TODO: Google drive delete api call
                    break;
                case Constants.Sources.ONEDRIVE:
                    //TODO: Onedrive delete api call
                    break;
            }
        }
        return returnInt;
    }

    /**
     * Post a notification with the given title and content to the status bar
     *
     * @param title   Title for the notification
     * @param content Content body of the notification
     */
    private void postNotification(String title, String content) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(content);

        Intent resultIntent = new Intent(this, SourceActivity.class);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    /**
     * Removes the notification from the status bar
     */
    private void hideNotification() {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    private void updateDialog(int currentCount) {
        Intent intent = new Intent();
        intent.setAction(ACTION_UPDATE_DIALOG);
        intent.putExtra(EXTRA_CURRENT_COUNT, currentCount);
        LocalBroadcastManager.getInstance(this)
                .sendBroadcast(intent);
    }

    private void showDialog(int totalCount) {
        Intent intent = new Intent();
        intent.setAction(ACTION_SHOW_DIALOG);
        intent.putExtra(EXTRA_TOTAL_COUNT, totalCount);
        LocalBroadcastManager.getInstance(this)
                .sendBroadcast(intent);
    }
}
