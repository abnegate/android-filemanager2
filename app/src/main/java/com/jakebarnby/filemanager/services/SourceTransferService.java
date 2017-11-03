package com.jakebarnby.filemanager.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.FolderMetadata;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.jakebarnby.filemanager.R;
import com.jakebarnby.filemanager.managers.SelectedFilesManager;
import com.jakebarnby.filemanager.sources.SourceActivity;
import com.jakebarnby.filemanager.sources.dropbox.DropboxFactory;
import com.jakebarnby.filemanager.sources.dropbox.DropboxFile;
import com.jakebarnby.filemanager.sources.googledrive.GoogleDriveFactory;
import com.jakebarnby.filemanager.sources.googledrive.GoogleDriveFile;
import com.jakebarnby.filemanager.sources.local.LocalFile;
import com.jakebarnby.filemanager.sources.models.SourceFile;
import com.jakebarnby.filemanager.sources.models.SourceType;
import com.jakebarnby.filemanager.sources.onedrive.OneDriveFactory;
import com.jakebarnby.filemanager.sources.onedrive.OneDriveFile;
import com.jakebarnby.filemanager.util.Constants;
import com.jakebarnby.filemanager.util.IntentExtensions;
import com.jakebarnby.filemanager.util.LogUtils;
import com.jakebarnby.filemanager.util.TreeNode;
import com.jakebarnby.filemanager.util.Utils;
import com.jakebarnby.filemanager.util.FileZipper;
import com.microsoft.graph.extensions.DriveItem;
import com.microsoft.graph.http.GraphServiceException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by Jake on 6/6/2017.
 */

public class SourceTransferService extends Service {

    private static final String ACTION_CREATE_FOLDER = "com.jakebarnby.filemanager.services.action.CREATE_FOLDER";
    private FirebaseAnalytics mFirebaseAnalytics;
    private Executor mThreadPool;

    static {
        System.loadLibrary("io-lib");
    }

    /**
     * Calls native io-lib and copies the file at the given path to the given destination
     *
     * @param sourcePath      The path of the file to copy
     * @param destinationPath The destination of the file to copy
     * @return 0 for success, otherwise operation failed
     */
    public native int copyFileNative(String sourcePath, String destinationPath);

    /**
     * Calls native io-lib and deletes the file at the given path to the given destination
     *
     * @param sourcePath The path of the file to delete
     * @return 0 for success, otherwise operation failed
     */
    public native int deleteFileNative(String sourcePath);

    /**
     * Calls native io-lib and creates a new folder
     *
     * @param newPath The name of the folder to create
     * @return 0 for success, otherwise operation failed
     */
    public native int createFolderNative(String newPath);

    /**
     * Calls native io-lib and renames the given file or folder to the given new name
     *
     * @param oldPath The previous path to the file or folder
     * @param newPath The new path to the file or folder
     * @return 0 for success, otherwise operation failed
     */
    public native int renameFolderNative(String oldPath, String newPath);

    @Override
    public void onCreate() {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mThreadPool = Executors.newCachedThreadPool();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mThreadPool.execute(() -> {
            if (intent != null) {
                final SourceFile toOpen =
                        (SourceFile) intent.getSerializableExtra(IntentExtensions.EXTRA_TO_OPEN_PATH);

                final String newName = intent.getStringExtra(IntentExtensions.EXTRA_NEW_NAME);
                final String zipName = intent.getStringExtra(IntentExtensions.EXTRA_ZIP_FILENAME);
                final String action = intent.getAction();

                int operationId = SelectedFilesManager.getInstance().getOperationCount() - 1;

                if (action != null) {
                    switch (action) {
                        case ACTION_CREATE_FOLDER:
                            createFolder(
                                    SelectedFilesManager.getInstance().getActionableDirectory(operationId),
                                    operationId,
                                    newName,
                                    false);
                            break;
                        case IntentExtensions.ACTION_RENAME:
                            rename(operationId, newName);
                            break;
                        case IntentExtensions.ACTION_COPY:
                            copy(
                                    SelectedFilesManager.getInstance().getActionableDirectory(operationId),
                                    operationId,
                                    false,
                                    false);
                            break;
                        case IntentExtensions.ACTION_MOVE:
                            copy(
                                    SelectedFilesManager.getInstance().getActionableDirectory(operationId),
                                    operationId,
                                    true,
                                    false);
                            break;
                        case IntentExtensions.ACTION_DELETE:
                            delete(operationId, false);
                            break;
                        case IntentExtensions.ACTION_OPEN:
                            open(operationId, toOpen);
                            break;
                        case IntentExtensions.ACTION_ZIP:
                            zipSelection(operationId, zipName);
                            break;
                        case IntentExtensions.ACTION_CLEAR_CACHE:
                            clearLocalCache();
                            break;
                    }
                }
            }
            stopSelf(startId);
        });

        // If we get killed, after returning from here, restart
        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Start the service for a copy or cut action with the given file.
     *
     * @param context Context for resources
     * @param move    Whether the files should be deleted after copying
     */
    public static void startActionCopy(Context context, boolean move) {
        Intent intent = new Intent(context, SourceTransferService.class);
        intent.setAction(move ? IntentExtensions.ACTION_MOVE : IntentExtensions.ACTION_COPY);
        context.startService(intent);
    }

    /**
     * Start the service for a delete action with the given files.
     *
     * @param context Context for resources
     */
    public static void startActionDelete(Context context) {
        Intent intent = new Intent(context, SourceTransferService.class);
        intent.setAction(IntentExtensions.ACTION_DELETE);
        context.startService(intent);
    }

    /**
     * Start the service for a new folder action
     *
     * @param context Context for resources
     */
    public static void startActionCreateFolder(Context context, String name) {
        Intent intent = new Intent(context, SourceTransferService.class);
        intent.setAction(ACTION_CREATE_FOLDER);
        intent.putExtra(IntentExtensions.EXTRA_NEW_NAME, name);
        context.startService(intent);
    }

    /**
     * Start the service for a rename action
     *
     * @param context Context for resources
     * @param newName The new name for the file or folder
     */
    public static void startActionRename(Context context, String newName) {
        Intent intent = new Intent(context, SourceTransferService.class);
        intent.setAction(IntentExtensions.ACTION_RENAME);
        intent.putExtra(IntentExtensions.EXTRA_NEW_NAME, newName);
        context.startService(intent);
    }

    /**
     * Start the service for an open file action
     *
     * @param context Context for resources
     * @param toOpen  The file to open
     */
    public static void startActionOpen(Context context, SourceFile toOpen) {
        Intent intent = new Intent(context, SourceTransferService.class);
        intent.setAction(IntentExtensions.ACTION_OPEN);
        intent.putExtra(IntentExtensions.EXTRA_TO_OPEN_PATH, toOpen);
        context.startService(intent);
    }

    public static void startActionZip(Context context, String zipFilename) {
        Intent intent = new Intent(context, SourceTransferService.class);
        intent.setAction(IntentExtensions.ACTION_ZIP);
        intent.putExtra(IntentExtensions.EXTRA_ZIP_FILENAME, zipFilename);
        context.startService(intent);
    }

    public static void startClearLocalCache(Context context) {
        Intent intent = new Intent(context, SourceTransferService.class);
        intent.setAction(IntentExtensions.ACTION_CLEAR_CACHE);
        context.startService(intent);
    }

    private void clearLocalCache() {
        try {
            if (getCacheDir().exists()) {
                int result = deleteFileNative(getCacheDir().getPath());
                if (result != 0) throw new IOException("Delete cache directory failed");
            }
        } catch (IOException e) {
            Bundle params = new Bundle();
            params.putString(Constants.Analytics.PARAM_ERROR_VALUE, e.getMessage());
            LogUtils.logFirebaseEvent(
                    mFirebaseAnalytics,
                    Constants.Analytics.EVENT_ERROR_CACHE_CLEAR,
                    params);
        }
    }

    /**
     * Create a new folder in the given directory with the given name
     *
     * @param operationId Id of the operation
     * @param name        The name for the new folder
     * @param isSilent    Whether to show visual progress for this operation
     */
    private TreeNode<SourceFile> createFolder(TreeNode<SourceFile> destDir,
                                              int operationId, String name,
                                              boolean isSilent) {
        try {
            if (!isSilent) broadcastShowDialog(getString(R.string.dialog_creating_folder), 0);
            SourceFile newFile = null;

            switch (destDir.getData().getSourceName()) {
                case Constants.Sources.DROPBOX:
                    FolderMetadata dropboxFile = DropboxFactory
                            .getInstance()
                            .createFolder(name, destDir.getData().getPath());
                    newFile = new DropboxFile(dropboxFile);
                    break;
                case Constants.Sources.GOOGLE_DRIVE:
                    com.google.api.services.drive.model.File googleFile = GoogleDriveFactory
                            .getInstance()
                            .createFolder(name, ((GoogleDriveFile) destDir.getData()).getDriveId());
                    newFile = new GoogleDriveFile(googleFile);
                    break;
                case Constants.Sources.ONEDRIVE:
                    DriveItem onedriveFile = OneDriveFactory
                            .getInstance()
                            .createFolder(name, ((OneDriveFile) destDir.getData()).getDriveId());
                    newFile = new OneDriveFile(onedriveFile);
                    break;
                default:
                    if (destDir.getData().getSourceType() == SourceType.LOCAL) {
                        int result = createFolderNative(
                                destDir.getData().getPath() + File.separator + name);

                        if (result != 0) throw new IOException("creating folder");

                        newFile = new LocalFile(
                                new File(destDir.getData().getPath() + File.separator + name),
                                destDir.getData().getSourceName());
                        break;
                    }
            }
            TreeNode<SourceFile> newFolder = destDir.addChild(newFile);
            if (!isSilent) broadcastFinishedTask(operationId);

            LogUtils.logFirebaseEvent(mFirebaseAnalytics, Constants.Analytics.EVENT_SUCCESS_CREATE_FOLDER);
            return newFolder;
        } catch (IOException | DbxException | GraphServiceException e) {
            broadcastError(String.format(
                    "%s %s%s",
                    getString(R.string.problem_encountered),
                    getString(R.string.creating_folder),
                    "."));

            Bundle params = new Bundle();
            params.putString(Constants.Analytics.PARAM_ERROR_VALUE, e.getMessage());
            params.putString(Constants.Analytics.PARAM_SOURCE_NAME, destDir.getData().getSourceName());
            LogUtils.logFirebaseEvent(
                    mFirebaseAnalytics,
                    Constants.Analytics.EVENT_ERROR_CREATE_FOLDER,
                    params);
        }
        return null;
    }

    /**
     * Renames the selected file or folder to the given name
     *
     * @param operationId Id of the operation
     * @param name        The new name of the file or folder
     */
    private void rename(int operationId, String name) {
        try {
            broadcastShowDialog(getString(R.string.dialog_renaming), 0);
            TreeNode<SourceFile> destDir =
                    SelectedFilesManager.getInstance().getSelectedFiles(operationId).get(0);
            String oldPath = null;
            String newPath = null;
            if (destDir.getData().getPath() != null) {
                oldPath = destDir.getData().getPath();
                newPath = destDir.getData().getPath().substring(
                        0,
                        destDir.getData().getPath().lastIndexOf(File.separator) + 1
                ) + name;
            }

            switch (destDir.getData().getSourceName()) {
                case Constants.Sources.DROPBOX:
                    DropboxFactory
                            .getInstance()
                            .rename(oldPath, newPath);
                    break;
                case Constants.Sources.GOOGLE_DRIVE:
                    GoogleDriveFactory
                            .getInstance()
                            .rename(name, ((GoogleDriveFile) destDir.getData()).getDriveId());
                    break;
                case Constants.Sources.ONEDRIVE:
                    OneDriveFactory
                            .getInstance()
                            .rename(name, ((OneDriveFile) destDir.getData()).getDriveId());
                    break;
                default:
                    if (destDir.getData().getSourceType() == SourceType.LOCAL) {
                        int result = renameFolderNative(oldPath, newPath);
                        if (result != 0) throw new IOException("renaming file");
                        break;
                    }
            }
            destDir.getData().setName(name);
            destDir.getData().setPath(newPath);
            broadcastFinishedTask(operationId);
            LogUtils.logFirebaseEvent(mFirebaseAnalytics, Constants.Analytics.EVENT_SUCCESS_RENAMING);
        } catch (IOException | DbxException | GraphServiceException e) {
            broadcastError(String.format(
                    "%s %s%s",
                    getString(R.string.problem_encountered),
                    getString(R.string.renaming_item),
                    "."));

            String sourceName;
            if (SelectedFilesManager.getInstance().getSelectedFiles(operationId).get(0) != null &&
                    SelectedFilesManager.getInstance().getSelectedFiles(operationId).get(0).getData() != null) {
                sourceName = SelectedFilesManager.getInstance().getSelectedFiles(operationId).get(0).getData().getSourceName();
            } else {
                sourceName = Constants.Analytics.NO_DESTINATION;
            }

            Bundle params = new Bundle();
            params.putString(Constants.Analytics.PARAM_ERROR_VALUE, e.getMessage());
            params.putString(Constants.Analytics.PARAM_SOURCE_NAME, sourceName);
            LogUtils.logFirebaseEvent(
                    mFirebaseAnalytics,
                    Constants.Analytics.EVENT_ERROR_RENAMING,
                    params);
        }
    }

    /**
     * Open the given {@link SourceFile}. If it is a remote file, dowload it first
     *
     * @param operationId Id of the operation
     * @param toOpen      The file to open
     */
    private void open(int operationId, SourceFile toOpen) {
        try {
            broadcastShowDialog(getString(R.string.opening), 0);

            String cachePath = getCacheDir().getPath() + File.separator;
            String filePath;

            if (!new File(cachePath + toOpen.getName()).exists()) {
                filePath = getFile(toOpen);
            } else {
                filePath = cachePath + toOpen.getName();
            }

            Bundle bundle = new Bundle();
            bundle.putString(Constants.FILE_PATH_KEY, filePath);
            broadcastFinishedTask(operationId, bundle);
            LogUtils.logFirebaseEvent(mFirebaseAnalytics, Constants.Analytics.EVENT_SUCCESS_OPEN_FILE);
        } catch (IOException | DbxException | GraphServiceException e) {
            broadcastError(String.format(
                    "%s %s%s",
                    getString(R.string.problem_encountered),
                    getString(R.string.opening_file),
                    "."));

            String sourceName = toOpen == null ? Constants.Analytics.NO_DESTINATION : toOpen.getSourceName();

            Bundle params = new Bundle();
            params.putString(Constants.Analytics.PARAM_ERROR_VALUE, e.getMessage());
            params.putString(Constants.Analytics.PARAM_SOURCE_NAME, sourceName);
            LogUtils.logFirebaseEvent(
                    mFirebaseAnalytics,
                    Constants.Analytics.EVENT_ERROR_OPENING_FILE,
                    params);
        }
    }

    /**
     * Copy or move the selected files to the given destination, if moving, delete the original files after
     *
     * @param move Whether this is a move or copt operation
     */
    private List<TreeNode<SourceFile>> copy(TreeNode<SourceFile> destDir, int operationId, boolean move, boolean isSilent) {
        try {
            List<TreeNode<SourceFile>> toCopy =
                    SelectedFilesManager.getInstance().getSelectedFiles(operationId);

            List<TreeNode<SourceFile>> newFiles = new ArrayList<>();

            if (!isSilent) {
                postNotification(
                        operationId,
                        getString(R.string.app_name),
                        move ? String.format(getString(R.string.moving_count), 1, toCopy.size()) :
                                String.format(getString(R.string.copying_count), 1, toCopy.size()));

                broadcastShowDialog(
                        move ? getString(R.string.moving) : getString(R.string.copying), toCopy.size());
            }

            recurseCopy(toCopy, destDir, operationId, move,isSilent, 0, newFiles);
            if (move) {
                delete(operationId, true);
            }

            if (!isSilent) {
                broadcastFinishedTask(operationId);
                LogUtils.logFirebaseEvent(mFirebaseAnalytics, Constants.Analytics.EVENT_SUCCESS_COPYING);
            }

            return newFiles;
        } catch (IOException | DbxException | GraphServiceException e) {
            broadcastError(String.format(
                    "%s %s%s",
                    getString(R.string.problem_encountered),
                    getString(move ? R.string.moving_items : R.string.copying_items),
                    "."));

            String sourceName;
            if (SelectedFilesManager.getInstance().getActionableDirectory(operationId) != null &&
                    SelectedFilesManager.getInstance().getActionableDirectory(operationId).getData() != null) {
                sourceName = SelectedFilesManager.getInstance().getActionableDirectory(operationId).getData().getSourceName();
            } else {
                sourceName = Constants.Analytics.NO_DESTINATION;
            }

            Bundle params = new Bundle();
            params.putString(Constants.Analytics.PARAM_ERROR_VALUE, e.getMessage());
            params.putString(Constants.Analytics.PARAM_SOURCE_NAME, sourceName);
            LogUtils.logFirebaseEvent(
                    mFirebaseAnalytics,
                    Constants.Analytics.EVENT_ERROR_COPYING,
                    params);
        }
        return null;
    }

    /**
     * Recursively copy or move the given collection of files and/or folders to the given destionation
     *  @param toCopy      The collection of files and/or folders to copy
     * @param destDir     The destination of the operation
     * @param operationId Id of the operation
     * @param move        Whether this a move or copy operation
     * @param depth
     * @param newFiles
     */
    private void recurseCopy(List<TreeNode<SourceFile>> toCopy,
                             TreeNode<SourceFile> destDir,
                             int operationId,
                             boolean move,
                             boolean isSilent,
                             int depth, List<TreeNode<SourceFile>> newFiles)
            throws IOException, DbxException, GraphServiceException {

        for (TreeNode<SourceFile> file : toCopy) {
            TreeNode<SourceFile> newItem;

            if (file.getData().isDirectory()) {
                newItem = createFolder(destDir, operationId, file.getData().getName(), true);
                recurseCopy(file.getChildren(), newItem, operationId, move, isSilent, depth + 1, newFiles);
            } else {
                String newFilePath = getFile(file.getData());
                SourceFile newFile = putFile(newFilePath, file.getData().getName(), destDir.getData());

                newItem = destDir.addChild(newFile);
                TreeNode<SourceFile> curDir = destDir;
                while (true) {
                    curDir.getData().addSize(file.getData().getSize());
                    if (curDir.getParent() != null) {
                        curDir = curDir.getParent();
                    } else break;
                }
            }

            if (depth == 0) {
                newFiles.add(newItem);

                if (!isSilent) {
                    broadcastUpdate(toCopy.indexOf(file) + 1);
                    postNotification(
                            operationId,
                            getString(R.string.app_name),
                            move ? String.format(
                                    getString(R.string.moving_count),
                                    toCopy.indexOf(file) + 1,
                                    toCopy.size()) :
                                    String.format(
                                            getString(R.string.copying_count),
                                            toCopy.indexOf(file) + 1,
                                            toCopy.size()));
                }
            }
        }
    }

    /**
     * Delete the selected files
     *
     * @param operationId Id of the operation
     * @param isSilent    Whether to show visual progress for this operation
     */
    private void delete(int operationId, boolean isSilent) {
        try {
            List<TreeNode<SourceFile>> toDelete =
                    SelectedFilesManager.getInstance().getSelectedFiles(operationId);

            if (!isSilent)
                broadcastShowDialog(getString(R.string.dialog_deleting), toDelete.size());
            for (TreeNode<SourceFile> file : toDelete) {
                postNotification(
                        operationId,
                        getString(R.string.app_name),
                        String.format(
                                getString(R.string.deleting_count),
                                toDelete.indexOf(file) + 1,
                                toDelete.size()));

                switch (file.getData().getSourceName()) {
                    case Constants.Sources.DROPBOX:
                        DropboxFactory
                                .getInstance()
                                .deleteFile(file.getData().getPath());
                        break;
                    case Constants.Sources.GOOGLE_DRIVE:
                        GoogleDriveFactory
                                .getInstance()
                                .deleteFile(((GoogleDriveFile) file.getData()).getDriveId());
                        break;
                    case Constants.Sources.ONEDRIVE:
                        OneDriveFactory
                                .getInstance()
                                .deleteFile(((OneDriveFile) file.getData()).getDriveId());
                        break;
                    default:
                        if (file.getData().getSourceType() == SourceType.LOCAL) {
                            int result = deleteFileNative(file.getData().getPath());
                            if (result != 0) throw new IOException("deleting file");
                            break;
                        }
                }
                file.getParent().removeChild(file);

                TreeNode<SourceFile> curDir = file.getParent();
                while (true) {
                    curDir.getData().removeSize(file.getData().getSize());
                    if (curDir.getParent() != null) {
                        curDir = curDir.getParent();
                    } else break;
                }

                if (!isSilent) {
                    broadcastUpdate(toDelete.indexOf(file) + 1);
                }
            }
            if (!isSilent) {
                broadcastFinishedTask(operationId);
            }

            LogUtils.logFirebaseEvent(mFirebaseAnalytics, Constants.Analytics.EVENT_SUCCESS_DELETING);

        } catch (IOException | DbxException | GraphServiceException e) {
            broadcastError(String.format(
                    "%s %s%s",
                    getString(R.string.problem_encountered),
                    getString(R.string.deleting_items),
                    "."));

            String sourceName;
            if (SelectedFilesManager.getInstance().getActionableDirectory(operationId) != null &&
                    SelectedFilesManager.getInstance().getActionableDirectory(operationId).getData() != null) {
                sourceName = SelectedFilesManager.getInstance().getActionableDirectory(operationId).getData().getSourceName();
            } else {
                sourceName = Constants.Analytics.NO_DESTINATION;
            }

            Bundle params = new Bundle();
            params.putString(Constants.Analytics.PARAM_ERROR_VALUE, e.getMessage());
            params.putString(Constants.Analytics.PARAM_SOURCE_NAME, sourceName);
            LogUtils.logFirebaseEvent(
                    mFirebaseAnalytics,
                    Constants.Analytics.EVENT_ERROR_DELETE,
                    params);
        }
    }

    private void zipSelection(int operationId, String zipFileName) {
        try {
            TreeNode<SourceFile> destDir =
                    SelectedFilesManager.getInstance().getActionableDirectory(operationId);

            broadcastShowDialog(getString(R.string.creating_zip), 0);

            List<TreeNode<SourceFile>> toZip = new ArrayList<>();
            toZip = copy(
                    new TreeNode<>(new LocalFile(getCacheDir(), Constants.Sources.LOCAL)),
                    operationId,
                    false,
                    true);

            List<String> filesToZipPaths = new ArrayList<>();

            for(TreeNode<SourceFile> file: toZip) {
                filesToZipPaths.add(file.getData().getPath());
            }

            FileZipper zipper = new FileZipper();
            zipper.zipFiles(getCacheDir().getPath()+ File.separator + zipFileName, filesToZipPaths);

            SourceFile newZip = putFile(
                    getCacheDir().getPath()+ File.separator + zipFileName,
                    zipFileName,
                    destDir.getData());

            destDir.addChild(newZip);

            broadcastFinishedTask(operationId);
        } catch (IOException | DbxException e) {
            e.printStackTrace();

            broadcastError(String.format(
                    "%s %s%s",
                    getString(R.string.problem_encountered),
                    getString(R.string.zipping_files),
                    "."));

            String sourceName;
            if (SelectedFilesManager.getInstance().getActionableDirectory(operationId) != null &&
                    SelectedFilesManager.getInstance().getActionableDirectory(operationId).getData() != null) {
                sourceName = SelectedFilesManager.getInstance().getActionableDirectory(operationId).getData().getSourceName();
            } else {
                sourceName = Constants.Analytics.NO_DESTINATION;
            }

            Bundle params = new Bundle();
            params.putString(Constants.Analytics.PARAM_ERROR_VALUE, e.getMessage());
            params.putString(Constants.Analytics.PARAM_SOURCE_NAME, sourceName);
            LogUtils.logFirebaseEvent(
                    mFirebaseAnalytics,
                    Constants.Analytics.EVENT_ERROR_ZIPPING,
                    params);
        }
    }

    /**
     * Gets the given source file and stores it in the given destination
     *
     * @param file The file to retrieve
     * @return The path of the retrieved file
     */
    private String getFile(SourceFile file) throws IOException, DbxException {
        String newFilePath = null;
        String destPath = getCacheDir().getPath() + File.separator + file.getName();
        switch (file.getSourceName()) {
            case Constants.Sources.DROPBOX:
                File newFile = DropboxFactory
                        .getInstance()
                        .downloadFile(
                                file.getPath(),
                                getCacheDir().getPath() + File.separator + file.getName());
                if (newFile.exists()) {
                    LogUtils.logFirebaseEvent(mFirebaseAnalytics, Constants.Analytics.EVENT_SUCCESS_DROPBOX_DOWNLOAD);
                    newFilePath = newFile.getPath();
                }
                break;
            case Constants.Sources.GOOGLE_DRIVE:
                File googleFile = GoogleDriveFactory
                        .getInstance()
                        .downloadFile(((GoogleDriveFile) file).getDriveId(), destPath);
                if (googleFile.exists()) {
                    LogUtils.logFirebaseEvent(mFirebaseAnalytics, Constants.Analytics.EVENT_SUCCESS_GOOGLEDRIVE_DOWNLOAD);
                    newFilePath = googleFile.getPath();
                }
                break;
            case Constants.Sources.ONEDRIVE:
                File oneDriveFile = OneDriveFactory
                        .getInstance()
                        .downloadFile(
                                ((OneDriveFile) file).getDriveId(),
                                file.getName(),
                                getCacheDir().getPath());
                if (oneDriveFile.exists()) {
                    LogUtils.logFirebaseEvent(mFirebaseAnalytics, Constants.Analytics.EVENT_SUCCESS_ONEDRIVE_DOWNLOAD);
                    newFilePath = oneDriveFile.getPath();
                }
                break;
            default:
                if (file.getSourceType() == SourceType.LOCAL) {
                    newFilePath = file.getPath();
                    break;
                }

        }
        return newFilePath;
    }

    /**
     * Puts the given file with the given fileName at the given destination
     *
     * @param newFilePath The path of the file to put
     * @param fileName    The name of the file to put
     * @param destDir     The destination of the file
     */
    private SourceFile putFile(String newFilePath,
                               String fileName,
                               SourceFile destDir)
            throws IOException, DbxException {

        switch (destDir.getSourceName()) {
            case Constants.Sources.DROPBOX:
                DropboxFile dropboxFile = new DropboxFile(
                        DropboxFactory
                                .getInstance()
                                .uploadFile(newFilePath, destDir.getPath()));
                LogUtils.logFirebaseEvent(mFirebaseAnalytics, Constants.Analytics.EVENT_SUCCESS_DROPBOX_UPLOAD);
                return dropboxFile;
            case Constants.Sources.GOOGLE_DRIVE:
                GoogleDriveFile googleDriveFile = new GoogleDriveFile(GoogleDriveFactory
                        .getInstance()
                        .uploadFile(
                                newFilePath,
                                fileName,
                                ((GoogleDriveFile) destDir).getDriveId()));
                LogUtils.logFirebaseEvent(mFirebaseAnalytics, Constants.Analytics.EVENT_SUCCESS_GOOGLEDRIVE_UPLOAD);
                return googleDriveFile;
            case Constants.Sources.ONEDRIVE:
                OneDriveFile oneDriveFile = new OneDriveFile(OneDriveFactory
                        .getInstance()
                        .uploadFile(
                                newFilePath,
                                fileName,
                                ((OneDriveFile) destDir).getDriveId()));
                LogUtils.logFirebaseEvent(mFirebaseAnalytics, Constants.Analytics.EVENT_SUCCESS_ONEDRIVE_UPLOAD);
                return oneDriveFile;
            default:

                if (destDir.getSourceType() == SourceType.LOCAL) {
                    int result = copyFileNative(
                            newFilePath,
                            destDir.getPath() + "/" + fileName);

                    if (result != 0) throw new IOException("copying file to device failed");
                    return new LocalFile(
                            new File(destDir.getPath() + "/" + fileName),
                            destDir.getSourceName());
                }
        }
        return null;
    }

    /**
     * Notifies the hosting activity that an operation has completed successfully
     *
     * @param operationId The id of the operation
     */
    private void broadcastFinishedTask(int operationId) {
        broadcastFinishedTask(operationId, null);
    }

    /**
     * Notifies the hosting activity that an operation has completed successfully
     *
     * @param operationId The id of the operation
     * @param bundle      The bundle to deliver to the activityy
     */
    private void broadcastFinishedTask(int operationId, Bundle bundle) {
        Intent intent = new Intent();
        intent.setAction(IntentExtensions.ACTION_COMPLETE);

        if (bundle != null) {
            bundle.putInt(IntentExtensions.EXTRA_OPERATION_ID, operationId);
            intent.putExtras(bundle);
        } else {
            intent.putExtra(IntentExtensions.EXTRA_OPERATION_ID, operationId);
        }

        if (SelectedFilesManager.getInstance().getSelectedFiles(operationId) != null) {
            SelectedFilesManager.getInstance().getSelectedFiles(operationId).clear();
        }

        getApplicationContext().sendBroadcast(intent);
        hideNotification(operationId);
    }

    private void broadcastError(String message) {
        Intent intent = new Intent();
        intent.setAction(IntentExtensions.ACTION_SHOW_ERROR);
        intent.putExtra(IntentExtensions.EXTRA_DIALOG_MESSAGE, message);
        getApplicationContext().sendBroadcast(intent);
    }

    /**
     * Notifies the hosting activity to display a dialog with the given title and max progress
     *
     * @param title      The title for the dialog
     * @param totalCount The max progress for the operation
     */
    private void broadcastShowDialog(String title, int totalCount) {
        Intent intent = new Intent();
        intent.setAction(IntentExtensions.ACTION_SHOW_DIALOG);
        intent.putExtra(IntentExtensions.EXTRA_DIALOG_TITLE, title);
        intent.putExtra(IntentExtensions.EXTRA_DIALOG_MAX_VALUE, totalCount);
        getApplicationContext().sendBroadcast(intent);
    }

    /**
     * Notifies the hosting activity of an update to a dialog (if showing) displaying the new progress value
     *
     * @param currentCount The current progress of the operation
     */
    private void broadcastUpdate(int currentCount) {
        Intent intent = new Intent();
        intent.setAction(IntentExtensions.ACTION_UPDATE_DIALOG);
        intent.putExtra(IntentExtensions.EXTRA_DIALOG_CURRENT_VALUE, currentCount);
        getApplicationContext().sendBroadcast(intent);
    }

    /**
     * Post a notification with the given title and content to the status bar
     *
     * @param title   Title for the notification
     * @param content Content body of the notification
     */
    private void postNotification(int operationId, String title, String content) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_folder_flat)
                        .setContentTitle(title)
                        .setContentText(content);

        Intent resultIntent = new Intent(this, SourceActivity.class);
        //TODO: Get this in activity and open the associated directory
        resultIntent.putExtra(IntentExtensions.EXTRA_OPERATION_ID, operationId);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(operationId, mBuilder.build());
        }
    }

    /**
     * Removes the notification from the status bar
     */
    private void hideNotification(int operationId) {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(operationId);
        }
    }
}
