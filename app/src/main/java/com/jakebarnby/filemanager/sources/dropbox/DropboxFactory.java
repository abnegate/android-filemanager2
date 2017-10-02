package com.jakebarnby.filemanager.sources.dropbox;

import android.content.Context;
import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.WriteMode;
import com.dropbox.core.v2.users.SpaceAllocation;
import com.dropbox.core.v2.users.SpaceUsage;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.jakebarnby.filemanager.sources.models.SourceStorageStats;
import com.jakebarnby.filemanager.util.Constants;
import com.jakebarnby.filemanager.util.PreferenceUtils;
import com.jakebarnby.filemanager.util.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Jake on 6/9/2017.
 */

public class DropboxFactory {

    private static final DropboxFactory ourInstance = new DropboxFactory();
    private static final String TAG = "DROPBOX";
    private DbxClientV2 mClient;

    public static DropboxFactory getInstance() {
        return ourInstance;
    }

    public DbxClientV2 getClient() {
        return mClient;
    }

    public void setClient(DbxClientV2 mClient) {
        this.mClient = mClient;
    }

    /**
     * @param downloadPath
     * @param destinationPath
     * @return
     */
    public File downloadFile(String downloadPath, String destinationPath) throws IOException, DbxException {
        File file = new File(destinationPath);
        OutputStream outputStream = new FileOutputStream(file);
        getClient()
                .files()
                .download(downloadPath)
                .download(outputStream);
        return file;
    }

    /**
     * @param fileUri
     * @param destPath
     */
    public FileMetadata uploadFile(String fileUri, String destPath) throws IOException, DbxException {
        File localFile = new File(fileUri);
        if (localFile.exists()) {
            InputStream inputStream = new FileInputStream(localFile);
            return getClient()
                    .files()
                    .uploadBuilder(destPath + "/" + localFile.getName())
                    .withMode(WriteMode.OVERWRITE)
                    .uploadAndFinish(inputStream);
        }
        return null;
    }

    /**
     * @param filePath
     */
    public void deleteFile(String filePath) throws DbxException {
        getClient()
                .files()
                .delete(filePath);
    }

    /**
     * @param name
     * @param path
     */
    public FolderMetadata createFolder(String name, String path) throws DbxException {
        return getClient()
                .files()
                .createFolder(path + File.separator + name);
    }

    /**
     * @param oldPath
     * @param newPath
     * @return
     */
    public Metadata rename(String oldPath, String newPath) throws DbxException {
        return getClient()
                .files()
                .move(oldPath, newPath);
    }

    public void logout(Context context) {
        AsyncTask.execute(() -> {
            try {
                getClient().auth().tokenRevoke();
                setClient(null);

                PreferenceUtils.savePref(context, Constants.Prefs.DROPBOX_TOKEN_KEY, (String) null);
            } catch (DbxException e) {
                Utils.logFirebaseErrorEvent(
                        FirebaseAnalytics.getInstance(context),
                        Constants.Analytics.EVENT_ERROR_DROPBOX_LOGOUT,
                        e.getMessage() == null ? e.toString() : e.getMessage());
            }
        });
    }

    public SourceStorageStats getStorageStats() {
        try {
            SpaceUsage usage = getClient().users().getSpaceUsage();
            long used = usage.getUsed();
            long max = 0;

            SpaceAllocation alloc = usage.getAllocation();
            if (alloc.isIndividual()) {
                max += alloc.getIndividualValue().getAllocated();
            }

            if (alloc.isTeam()) {
                max += alloc.getTeamValue().getAllocated();
            }

            SourceStorageStats info = new SourceStorageStats();
            info.setTotalSpace(max);
            info.setUsedSpace(used);
            info.setFreeSpace(max - used);

            return info;
        } catch (DbxException e) {
            e.printStackTrace();
        }
        return null;
    }
}
