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
import com.jakebarnby.filemanager.managers.GoogleDriveFactory;
import com.jakebarnby.filemanager.managers.OneDriveFactory;
import com.jakebarnby.filemanager.managers.SelectedFilesManager;
import com.jakebarnby.filemanager.models.files.GoogleDriveFile;
import com.jakebarnby.filemanager.models.files.OneDriveFile;
import com.jakebarnby.filemanager.models.files.SourceFile;
import com.jakebarnby.filemanager.util.Constants;
import com.jakebarnby.filemanager.util.TreeNode;

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

    private static final String ACTION_COPY = "com.jakebarnby.filemanager.services.action.COPY";
    private static final String ACTION_MOVE = "com.jakebarnby.filemanager.services.action.MOVE";
    private static final String ACTION_DELETE = "com.jakebarnby.filemanager.services.action.DELETE";
    private static final String ACTION_CREATE_FOLDER = "com.jakebarnby.filemanager.services.action.CREATE_FOLDER";
    private static final String ACTION_RENAME = "com.jakebarnby.filemanager.services.action.RENAME";

    public static final String EXTRA_CURRENT_COUNT = "com.jakebarnby.filemanager.services.extra.CURRENT_COUNT";
    public static final String EXTRA_TOTAL_COUNT = "com.jakebarnby.filemanager.services.extra.TOTAL_COUNT";
    private static final String EXTRA_SOURCE_FILES = "com.jakebarnby.filemanager.services.extra.SOURCE_FILES";
    private static final String EXTRA_SOURCE_DEST = "com.jakebarnby.filemanager.services.extra.SOURCE DESTINATION";
    public static final String EXTRA_DIALOG_TITLE = "com.jakebarnby.filemanager.services.extra.DIALOG_TITLE";
    private static final String EXTRA_NAME = "com.jakebarnby.filemanager.services.extra.NAME";

    static {
        System.loadLibrary("io-lib");
    }

    /**
     * Calls into native io-lib and copies the file at the given path to the given destination
     * @param sourcePath            The path of the file to copy
     * @param destinationPath       The destination of the file to copy
     * @return                      0 for success, otherwise operation failed
     */
    public native int copyFileNative(String sourcePath, String destinationPath);

    /**
     * Calls into native io-lib and deletes the file at the given path to the given destination
     * @param sourcePath    The path of the file to delete
     * @return              0 for success, otherwise operation failed
     */
    public native int deleteFileNative(String sourcePath);

    /**
     *
     * @param newPath
     * @return
     */
    public native int createFolderNative(String newPath);

    /**
     *
     * @param oldPath
     * @param newPath
     * @return
     */
    public native int renameFolderNative(String oldPath, String newPath);

    /**
     * Create a new instance
     */
    public SourceTransferService() {
        super("SourceTransferService");
    }

    /**
     * Start the service for a copy or cut action with the given file.
     * @param context       Context for resources
     * @param toCopy        The files to copy
     * @param sourceDest    The destination directory
     * @param move          Whether the files should be deleted after copying
     */
    public static void startActionCopy(Context context, List<TreeNode<SourceFile>> toCopy, TreeNode<SourceFile> sourceDest, boolean move) {
        Intent intent = new Intent(context, SourceTransferService.class);
        intent.setAction(move ? ACTION_MOVE : ACTION_COPY);
        intent.putExtra(EXTRA_SOURCE_FILES, (Serializable) toCopy);
        intent.putExtra(EXTRA_SOURCE_DEST, sourceDest);
        context.startService(intent);
    }

    /**
     * Start the service for a delete action with the given files.
     * @param context  Context for resources
     * @param toDelete The files to delete
     */
    public static void startActionDelete(Context context, List<TreeNode<SourceFile>> toDelete) {
        Intent intent = new Intent(context, SourceTransferService.class);
        intent.setAction(ACTION_DELETE);
        intent.putExtra(EXTRA_SOURCE_FILES, (Serializable) toDelete);
        context.startService(intent);
    }

    /**
     * Start the service for a new folder action
     * @param context
     * @param currentDirectory
     */
    public static void startActionCreateFolder(Context context, TreeNode<SourceFile> currentDirectory, String name) {
        Intent intent = new Intent(context, SourceTransferService.class);
        intent.setAction(ACTION_CREATE_FOLDER);
        intent.putExtra(EXTRA_SOURCE_DEST, currentDirectory);
        intent.putExtra(EXTRA_NAME, name);
        context.startService(intent);
    }

    public static void startActionRename(Context context, TreeNode<SourceFile> toRename, String newName) {
        Intent intent = new Intent(context, SourceTransferService.class);
        intent.setAction(ACTION_RENAME);
        intent.putExtra(EXTRA_SOURCE_DEST, toRename);
        intent.putExtra(EXTRA_NAME, newName);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final List<TreeNode<SourceFile>> targets = (List<TreeNode<SourceFile>>) intent.getSerializableExtra(EXTRA_SOURCE_FILES);
            final TreeNode<SourceFile> sourceDest = (TreeNode<SourceFile>) intent.getSerializableExtra(EXTRA_SOURCE_DEST);
            final String name = intent.getStringExtra(EXTRA_NAME);
            final String action = intent.getAction();
            switch (action) {
                case ACTION_CREATE_FOLDER:
                    createFolder(sourceDest, name);
                    break;
                case ACTION_RENAME:
                    rename(sourceDest, name);
                    break;
                case ACTION_COPY:
                    copy(targets, sourceDest, false);
                    break;
                case ACTION_MOVE:
                    copy(targets, sourceDest, true);
                    break;
                case ACTION_DELETE:
                    delete(targets, false);
                    break;
            }
        }
        hideNotification();
    }

    /**
     * Create a new folder in the given directory with the given name
     * @param destDir  The directory to create the folder in
     * @param name              The name for the new folder
     */
    private void createFolder(TreeNode<SourceFile> destDir, String name) {
        switch(destDir.getData().getSourceName()) {
            case Constants.Sources.LOCAL:
                createFolderNative(destDir.getData().getUri().getPath()+File.separator+name);
                break;
            case Constants.Sources.DROPBOX:
                DropboxFactory
                        .getInstance()
                        .createFolder(name, destDir.getData().getUri().getPath());
                break;
            case Constants.Sources.GOOGLE_DRIVE:
                GoogleDriveFactory
                        .getInstance()
                        .createFolder(name, ((GoogleDriveFile)destDir.getData()).getDriveId());
                break;
            case Constants.Sources.ONEDRIVE:
                OneDriveFactory
                        .getInstance()
                        .createFolder(name, ((OneDriveFile)destDir.getData()).getDriveId());
                break;
        }
        finishOperation();
    }

    /**
     * Renames the given file or folder to the given name
     * @param destDir    The file or folder to rename
     * @param name       The new name of the file or folder
     */
    private void rename(TreeNode<SourceFile> destDir, String name) {
        String oldPath = null;
        String newPath = null;
        if (destDir.getData().getUri() != null) {
            oldPath = destDir.getData().getUri().getPath();
            newPath = destDir.getData().getUri().getPath().substring(0, destDir.getData().getUri().getPath().lastIndexOf(File.separator) + 1) + name;
        }

        switch(destDir.getData().getSourceName()) {
            case Constants.Sources.LOCAL:

                renameFolderNative(oldPath, newPath);
                break;
            case Constants.Sources.DROPBOX:
                DropboxFactory
                        .getInstance()
                        .rename(oldPath, newPath);
                break;
            case Constants.Sources.GOOGLE_DRIVE:
                GoogleDriveFactory
                        .getInstance()
                        .rename(name, ((GoogleDriveFile)destDir.getData()).getDriveId());
                break;
            case Constants.Sources.ONEDRIVE:
                OneDriveFactory
                        .getInstance()
                        .rename(name, ((OneDriveFile)destDir.getData()).getDriveId());
                break;
        }
        finishOperation();
    }

    /**
     * Copy the given files to the given destination
     * @param toCopy  The files to copy
     * @param destDir Where to copy them to
     */
    private void copy(List<TreeNode<SourceFile>> toCopy, TreeNode<SourceFile> destDir, boolean move) {
        showDialog(move ? getString(R.string.moving) : getString(R.string.copying), toCopy.size());
        for (TreeNode<SourceFile> file : toCopy) {
            String newFilePath = getFile(file.getData());
            putFile(newFilePath, file.getData().getName(), destDir.getData());
            postNotification("File Manager", "Copying " + (toCopy.indexOf(file) + 1) + " of " + toCopy.size());
            updateDialog(toCopy.indexOf(file) + 1);
        }
        if (move) {
            delete(toCopy, true);
        }
        finishOperation();
    }

    /**
     * Gets the given source file and stores it in the given destination
     * @param file          The file to retrieve
     * @return              The path of the retrieved file
     */
    private String getFile(SourceFile file) {
        String newFilePath = null;
        switch (file.getSourceName()) {
            case Constants.Sources.LOCAL:
                newFilePath = file.getUri().getPath();
                break;
            case Constants.Sources.DROPBOX:
                File newFile = DropboxFactory
                        .getInstance()
                        .downloadFile(file.getUri().getPath(), getCacheDir().getPath()+File.separator+file.getName());
                if (newFile.exists())
                    newFilePath = newFile.getPath();
                break;
            case Constants.Sources.GOOGLE_DRIVE:
                File googleFile = GoogleDriveFactory
                        .getInstance()
                        .downloadFile(((GoogleDriveFile)file).getDriveId(), getCacheDir().getPath()+File.separator+file.getName());
                if (googleFile.exists())
                    newFilePath = googleFile.getPath();
                break;
            case Constants.Sources.ONEDRIVE:
                File oneDriveFile = OneDriveFactory
                        .getInstance()
                        .downloadFile(((OneDriveFile)file).getDriveId(), file.getName(), getCacheDir().getPath()+File.separator+file.getName());
                if (oneDriveFile.exists()) {
                    newFilePath = oneDriveFile.getPath();
                }
                break;
        }
        return newFilePath;
    }

    /**
     * Puts the given file with the given fileName at the given destination
     * @param newFilePath   The path of the file to put
     * @param fileName      The name of the file to put
     * @param destDir       The destination of the file
     */
    private void putFile(String newFilePath, String fileName, SourceFile destDir) {
        switch (destDir.getSourceName()) {
            case Constants.Sources.LOCAL:
                copyFileNative(newFilePath, destDir.getUri().getPath()+"/"+fileName);
                break;
            case Constants.Sources.DROPBOX:
                DropboxFactory
                        .getInstance()
                        .uploadFile(newFilePath, destDir.getUri().getPath());
                break;
            case Constants.Sources.GOOGLE_DRIVE:
                GoogleDriveFactory
                        .getInstance()
                        .uploadFile(newFilePath, fileName, ((GoogleDriveFile)destDir).getDriveId());
                break;
            case Constants.Sources.ONEDRIVE:
                OneDriveFactory.getInstance().uploadFile(newFilePath, fileName, destDir.getUri().getPath());
                break;
        }
    }

    /**
     * Delete the given files
     * @param toDelete The files to delete
     */
    private void delete(List<TreeNode<SourceFile>> toDelete, boolean isSilent) {
        if (!isSilent) showDialog(getString(R.string.deleting), toDelete.size());
        for (TreeNode<SourceFile> file : toDelete) {
            switch (file.getData().getSourceName()) {
                case Constants.Sources.LOCAL:
                    deleteFileNative(file.getData().getUri().getPath());
                    break;
                case Constants.Sources.DROPBOX:
                    DropboxFactory
                            .getInstance()
                            .deleteFile(file.getData().getUri().getPath());
                    break;
                case Constants.Sources.GOOGLE_DRIVE:
                    GoogleDriveFactory
                            .getInstance()
                            .deleteFile(((GoogleDriveFile)file.getData()).getDriveId());
                    break;
                case Constants.Sources.ONEDRIVE:
                    OneDriveFactory
                            .getInstance()
                            .deleteFile(((OneDriveFile)file.getData()).getDriveId());
                    break;
            }
            if (!isSilent) updateDialog(toDelete.indexOf(file)+1);
        }
        if (!isSilent) finishOperation();
    }

    /**
     * Notifies the activity that this operation is finished
     */
    private void finishOperation() {
        Intent intent = new Intent();
        intent.setAction(ACTION_COMPLETE);
        SelectedFilesManager.getInstance().getSelectedFiles().clear();
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


    /**
     * Broadcasts that the hosting activity should display a dialog with the given title and max progress
     * @param title         The title for the dialog
     * @param totalCount    The max progress for the operation
     */
    private void showDialog(String title, int totalCount) {
        Intent intent = new Intent();
        intent.setAction(ACTION_SHOW_DIALOG);
        intent.putExtra(EXTRA_DIALOG_TITLE, title);
        intent.putExtra(EXTRA_TOTAL_COUNT, totalCount);
        LocalBroadcastManager.getInstance(this)
                .sendBroadcast(intent);
    }

    /**
     * Broadcasts that the hosting activity should update a dialog (if showing) displaying the new progress value
     * @param currentCount  The current progress of the operation
     */
    private void updateDialog(int currentCount) {
        Intent intent = new Intent();
        intent.setAction(ACTION_UPDATE_DIALOG);
        intent.putExtra(EXTRA_CURRENT_COUNT, currentCount);
        LocalBroadcastManager.getInstance(this)
                .sendBroadcast(intent);
    }

    /**
     * Post a notification with the given title and content to the status bar
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
        notificationManager.notify(Constants.NOTIFICATION_ID, mBuilder.build());
    }

    /**
     * Removes the notification from the status bar
     */
    private void hideNotification() {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(Constants.NOTIFICATION_ID);
    }
}
