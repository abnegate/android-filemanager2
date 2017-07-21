package com.jakebarnby.filemanager.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.dropbox.core.v2.files.FolderMetadata;
import com.jakebarnby.filemanager.R;
import com.jakebarnby.filemanager.activities.source.SourceActivity;
import com.jakebarnby.filemanager.managers.DropboxFactory;
import com.jakebarnby.filemanager.managers.GoogleDriveFactory;
import com.jakebarnby.filemanager.managers.OneDriveFactory;
import com.jakebarnby.filemanager.managers.SelectedFilesManager;
import com.jakebarnby.filemanager.models.files.DropboxFile;
import com.jakebarnby.filemanager.models.files.GoogleDriveFile;
import com.jakebarnby.filemanager.models.files.LocalFile;
import com.jakebarnby.filemanager.models.files.OneDriveFile;
import com.jakebarnby.filemanager.models.files.SourceFile;
import com.jakebarnby.filemanager.util.Constants;
import com.jakebarnby.filemanager.util.TreeNode;
import com.microsoft.graph.extensions.DriveItem;

import java.io.File;
import java.util.List;

/**
 * Created by Jake on 6/6/2017.
 */

public class SourceTransferService extends IntentService {
    public static final String ACTION_COMPLETE = "com.jakebarnby.filemanager.services.action.COMPLETE";
    public static final String ACTION_SHOW_DIALOG = "com.jakebarnby.filemanager.services.action.SHOW_DIALOG";
    public static final String ACTION_UPDATE_DIALOG = "com.jakebarnby.filemanager.services.action.UPDATE_DIALOG";
    public static final String ACTION_ADD_CHILD = "com.jakebarnby.filemanager.services.action.ADD_CHILD";
    public static final String ACTION_REMOVE_CHILD = "com.jakebarnby.filemanager.services.action.REMOVE_CHILD";

    private static final String ACTION_COPY = "com.jakebarnby.filemanager.services.action.COPY";
    private static final String ACTION_MOVE = "com.jakebarnby.filemanager.services.action.MOVE";
    private static final String ACTION_DELETE = "com.jakebarnby.filemanager.services.action.DELETE";
    private static final String ACTION_CREATE_FOLDER = "com.jakebarnby.filemanager.services.action.CREATE_FOLDER";
    private static final String ACTION_RENAME = "com.jakebarnby.filemanager.services.action.RENAME";
    private static final String ACTION_OPEN = "com.jakebarnby.filemanager.services.action.OPEN";

    public static final String EXTRA_CURRENT_COUNT = "com.jakebarnby.filemanager.services.extra.CURRENT_COUNT";
    public static final String EXTRA_TOTAL_COUNT = "com.jakebarnby.filemanager.services.extra.TOTAL_COUNT";
    public static final String EXTRA_CHILD_FILE = "com.jakebarnby.filemanager.services.action.EXTRA_CHILD_FILE";
    private static final String EXTRA_SOURCE_DEST = "com.jakebarnby.filemanager.services.extra.SOURCE DESTINATION";
    public static final String EXTRA_DIALOG_TITLE = "com.jakebarnby.filemanager.services.extra.DIALOG_TITLE";
    private static final String EXTRA_NAME = "com.jakebarnby.filemanager.services.extra.NAME";
    private static final String EXTRA_TO_OPEN = "com.jakebarnby.filemanager.services.extra.TO_OPEN";

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
     * @param move          Whether the files should be deleted after copying
     */
    public static void startActionCopy(Context context, boolean move) {
        Intent intent = new Intent(context, SourceTransferService.class);
        intent.setAction(move ? ACTION_MOVE : ACTION_COPY);
        context.startService(intent);
    }

    /**
     * Start the service for a delete action with the given files.
     * @param context  Context for resources
     */
    public static void startActionDelete(Context context) {
        Intent intent = new Intent(context, SourceTransferService.class);
        intent.setAction(ACTION_DELETE);
        context.startService(intent);
    }

    /**
     * Start the service for a new folder action
     * @param context           Context for resources
     */
    public static void startActionCreateFolder(Context context, String name) {
        Intent intent = new Intent(context, SourceTransferService.class);
        intent.setAction(ACTION_CREATE_FOLDER);
        intent.putExtra(EXTRA_NAME, name);
        context.startService(intent);
    }

    /**
     * Start the service for a rename action
     * @param context Context for resources
     * @param newName The new name for the file or folder
     */
    public static void startActionRename(Context context, String newName) {
        Intent intent = new Intent(context, SourceTransferService.class);
        intent.setAction(ACTION_RENAME);
        intent.putExtra(EXTRA_NAME, newName);
        context.startService(intent);
    }

    /**
     * Start the service for an open file action
     * @param context Context for resources
     * @param toOpen  The file to open
     */
    public static void startActionOpen(Context context, SourceFile toOpen) {
        Intent intent = new Intent(context, SourceTransferService.class);
        intent.setAction(ACTION_OPEN);
        intent.putExtra(EXTRA_TO_OPEN, toOpen);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final SourceFile toOpen = (SourceFile) intent.getSerializableExtra(EXTRA_TO_OPEN);
            final String name = intent.getStringExtra(EXTRA_NAME);
            final String action = intent.getAction();
            switch (action) {
                case ACTION_CREATE_FOLDER:
                    createFolder(name);
                    break;
                case ACTION_RENAME:
                    rename(name);
                    break;
                case ACTION_COPY:
                    copy(false);
                    break;
                case ACTION_MOVE:
                    copy(true);
                    break;
                case ACTION_DELETE:
                    delete(false);
                    break;
                case ACTION_OPEN:
                    open(toOpen);
                    break;
            }
        }
        hideNotification();
    }

    /**
     * Create a new folder in the given directory with the given name
     * @param name              The name for the new folder
     */
    private void createFolder(String name) {
        TreeNode<SourceFile> destDir = SelectedFilesManager.getInstance().getActiveDirectory();
        SourceFile newFile = null;
        switch(destDir.getData().getSourceName()) {
            case Constants.Sources.LOCAL:
                createFolderNative(destDir.getData().getPath()+File.separator+name);
                newFile = new LocalFile(new File(destDir.getData().getPath()+File.separator+name));
                break;
            case Constants.Sources.DROPBOX:
                FolderMetadata dropboxFile = DropboxFactory
                        .getInstance()
                        .createFolder(name, destDir.getData().getPath());
                newFile = new DropboxFile(dropboxFile);
                break;
            case Constants.Sources.GOOGLE_DRIVE:
                com.google.api.services.drive.model.File googleFile = GoogleDriveFactory
                        .getInstance()
                        .createFolder(name, ((GoogleDriveFile)destDir.getData()).getDriveId());
                newFile = new GoogleDriveFile(googleFile);
                break;
            case Constants.Sources.ONEDRIVE:
                DriveItem onedriveFile = OneDriveFactory
                        .getInstance()
                        .createFolder(name, ((OneDriveFile)destDir.getData()).getDriveId());
                newFile = new OneDriveFile(onedriveFile);
                break;
        }
        destDir.addChild(newFile);
        broadcastFinishedTask();
    }

    /**
     * Renames the selected file or folder to the given name
     * @param name       The new name of the file or folder
     */
    private void rename(String name) {
        TreeNode<SourceFile> destDir = SelectedFilesManager.getInstance().getSelectedFiles().get(0);
        String oldPath = null;
        String newPath = null;
        if (destDir.getData().getPath() != null) {
            oldPath = destDir.getData().getPath();
            newPath = destDir.getData().getPath().substring(0, destDir.getData().getPath().lastIndexOf(File.separator) + 1) + name;
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
        destDir.getData().setName(name);
        broadcastFinishedTask();
    }

    /**
     * Open the given {@link SourceFile}. If it is a remote file, dowload it first
     * @param toOpen
     */
    private void open(SourceFile toOpen) {
        broadcastShowDialog(getString(R.string.opening), 0);

        String cachePath = getCacheDir().getPath()+File.separator;
        String filePath;

        if (!new File(cachePath+toOpen.getName()).exists()) {
            filePath = getFile(toOpen);
        } else {
            filePath = cachePath+toOpen.getName();
        }

        Bundle bundle = new Bundle();
        bundle.putString(Constants.FILE_PATH_KEY, filePath);
        broadcastFinishedTask(bundle);
    }

    /**
     * Copy the selected files to the given destination
     */
    private void copy(boolean move) {
        List<TreeNode<SourceFile>> toCopy = SelectedFilesManager.getInstance().getSelectedFiles();
        TreeNode<SourceFile> destDir = SelectedFilesManager.getInstance().getActiveDirectory();

        broadcastShowDialog(move ? getString(R.string.moving) : getString(R.string.copying), toCopy.size());
        for (TreeNode<SourceFile> file : toCopy) {
            String newFilePath = getFile(file.getData());
            putFile(newFilePath, file.getData().getName(), destDir.getData());

            destDir.addChild(file.getData());
            TreeNode<SourceFile> curDir = destDir;
            while (true) {
                curDir.getData().addSize(file.getData().getSize());
                if (curDir.getParent() != null) {
                    curDir = curDir.getParent();
                } else break;
            }
            postNotification("File Manager", "Copying " + (toCopy.indexOf(file) + 1) + " of " + toCopy.size());
            broadcastUpdate(toCopy.indexOf(file) + 1);
        }
        if (move) {
            delete(true);
        }
        broadcastFinishedTask();
    }

    /**
     * Gets the given source file and stores it in the given destination
     * @param file          The file to retrieve
     * @return              The path of the retrieved file
     */
    private String getFile(SourceFile file) {
        String newFilePath = null;
        String destPath = getCacheDir().getPath()+File.separator+file.getName();
        switch (file.getSourceName()) {
            case Constants.Sources.LOCAL:
                newFilePath = file.getPath();
                break;
            case Constants.Sources.DROPBOX:
                File newFile = DropboxFactory
                        .getInstance()
                        .downloadFile(file.getPath(), getCacheDir().getPath()+File.separator+file.getName());
                if (newFile.exists())
                    newFilePath = newFile.getPath();
                break;
            case Constants.Sources.GOOGLE_DRIVE:
                File googleFile = GoogleDriveFactory
                        .getInstance()
                        .downloadFile(((GoogleDriveFile)file).getDriveId(), destPath);
                if (googleFile.exists())
                    newFilePath = googleFile.getPath();
                break;
            case Constants.Sources.ONEDRIVE:
                File oneDriveFile = OneDriveFactory
                        .getInstance()
                        .downloadFile(((OneDriveFile)file).getDriveId(), file.getName(), getCacheDir().getPath());
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
                copyFileNative(newFilePath, destDir.getPath()+"/"+fileName);
                break;
            case Constants.Sources.DROPBOX:
                DropboxFactory
                        .getInstance()
                        .uploadFile(newFilePath, destDir.getPath());
                break;
            case Constants.Sources.GOOGLE_DRIVE:
                GoogleDriveFactory
                        .getInstance()
                        .uploadFile(newFilePath, fileName, ((GoogleDriveFile)destDir).getDriveId());
                break;
            case Constants.Sources.ONEDRIVE:
                OneDriveFactory.getInstance().uploadFile(newFilePath, fileName, ((OneDriveFile)destDir).getDriveId());
                break;
        }
    }

    /**
     * Delete the selected files
     */
    private void delete(boolean isSilent) {
        List<TreeNode<SourceFile>> toDelete = SelectedFilesManager.getInstance().getSelectedFiles();
        TreeNode<SourceFile> currentDir = SelectedFilesManager.getInstance().getActiveDirectory();

        if (!isSilent) broadcastShowDialog(getString(R.string.deleting), toDelete.size());
        for (TreeNode<SourceFile> file : toDelete) {
            switch (file.getData().getSourceName()) {
                case Constants.Sources.LOCAL:
                    deleteFileNative(file.getData().getPath());
                    break;
                case Constants.Sources.DROPBOX:
                    DropboxFactory
                            .getInstance()
                            .deleteFile(file.getData().getPath());
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
            currentDir.removeChild(file);

            TreeNode<SourceFile> curDir = currentDir;
            while (true) {
                curDir.getData().removeSize(file.getData().getSize());
                if (curDir.getParent() != null) {
                    curDir = curDir.getParent();
                } else break;
            }

            if (!isSilent) {
                broadcastUpdate(toDelete.indexOf(file)+1);
            }
        }
        if (!isSilent) {
            broadcastFinishedTask();
        }
    }

    /**
     * Notifies the activity that this operation is finished
     */
    private void broadcastFinishedTask() {
        broadcastFinishedTask(null);
    }

    private void broadcastFinishedTask(Bundle bundle) {
        Intent intent = new Intent();
        intent.setAction(ACTION_COMPLETE);

        if (bundle != null) {
            intent.putExtras(bundle);
        }

        SelectedFilesManager.getInstance().getSelectedFiles().clear();
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    /**
     * Notifies the hosting activity to display a dialog with the given title and max progress
     * @param title         The title for the dialog
     * @param totalCount    The max progress for the operation
     */
    private void broadcastShowDialog(String title, int totalCount) {
        Intent intent = new Intent();
        intent.setAction(ACTION_SHOW_DIALOG);
        intent.putExtra(EXTRA_DIALOG_TITLE, title);
        intent.putExtra(EXTRA_TOTAL_COUNT, totalCount);
        LocalBroadcastManager.getInstance(this)
                .sendBroadcast(intent);
    }

    /**
     * Notifies the hosting activity of an update to a dialog (if showing) displaying the new progress value
     * @param currentCount  The current progress of the operation
     */
    private void broadcastUpdate(int currentCount) {
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
