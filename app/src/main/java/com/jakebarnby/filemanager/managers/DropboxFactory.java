package com.jakebarnby.filemanager.managers;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.WriteMode;

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

    public static DropboxFactory Instance() {
        return ourInstance;
    }

    private DropboxFactory() {
    }

    public DbxClientV2 getClient() {
        return mClient;
    }

    public void setClient(DbxClientV2 mClient) {
        this.mClient = mClient;
    }

    /**
     *
     * @param downloadPath
     * @param name
     * @return
     */
    public File downloadFile(String downloadPath, String name) {
        try {
            File path = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS);
            File file = new File(path, name);
            if (!path.exists()) {
                if (!path.mkdirs()) {
                    throw new RuntimeException("Unable to create directory: " + path);
                }
            }

            if (!path.isDirectory()) {
                 throw new IllegalStateException("Download path is not a directory: " + path);
            }

            try (OutputStream outputStream = new FileOutputStream(file)) {
                getClient()
                        .files()
                        .download(downloadPath)
                        .download(outputStream);
            }
            return file;
        } catch (DbxException | IOException e) {
            Log.e("DROPBOX", e.getLocalizedMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     *  @param context
     * @param fileUri
     * @param destPath
     */
    public FileMetadata uploadFile(String fileUri, String destPath) {
        File localFile = new File(fileUri).exists() ? new File(fileUri) : null;

        if (localFile != null) {
            //FIXME: This doesn't ensuring the name is a valid dropbox file name
            String remoteFileName = localFile.getName();

            try (InputStream inputStream = new FileInputStream(localFile)) {
                return getClient()
                        .files()
                        .uploadBuilder(destPath + "/" + remoteFileName)
                        .withMode(WriteMode.OVERWRITE)
                        .uploadAndFinish(inputStream);
            } catch (DbxException | IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
