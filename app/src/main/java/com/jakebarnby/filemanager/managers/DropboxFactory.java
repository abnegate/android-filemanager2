package com.jakebarnby.filemanager.managers;

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
     * @param downloadPath
     * @param destinationPath
     * @return
     */
    public File downloadFile(String downloadPath, String destinationPath) {
        File file = new File(destinationPath);
        try (OutputStream outputStream = new FileOutputStream(file)) {
            getClient()
                    .files()
                    .download(downloadPath)
                    .download(outputStream);
            return file;
        } catch (DbxException | IOException e) {
            Log.e("DROPBOX", e.getLocalizedMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param fileUri
     * @param destPath
     */
    public FileMetadata uploadFile(String fileUri, String destPath) {
        File localFile = new File(fileUri);
        if (localFile.exists()) {
            //FIXME: This doesn't ensure the name is a valid dropbox file name
            try (InputStream inputStream = new FileInputStream(localFile)) {
                return getClient()
                        .files()
                        .uploadBuilder(destPath + "/" + localFile.getName())
                        .withMode(WriteMode.OVERWRITE)
                        .uploadAndFinish(inputStream);
            } catch (DbxException | IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * @param filePath
     */
    public void deleteFile(String filePath) {
        try {
            getClient()
                    .files()
                    .delete(filePath);
        } catch (DbxException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param name
     * @param path
     */
    public void createFolder(String name, String path) {
        try {
            getClient()
                    .files()
                    .createFolder(name + File.separator + path);
        } catch (DbxException e) {
            e.printStackTrace();
        }
    }
}
