package com.jakebarnby.filemanager.managers;

import android.util.Log;

import com.annimon.stream.Stream;
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.DeleteArg;
import com.dropbox.core.v2.files.DeleteBatchJobStatus;
import com.dropbox.core.v2.files.DeleteBatchLaunch;
import com.dropbox.core.v2.files.DeleteBatchResult;
import com.dropbox.core.v2.files.DeleteBatchResultEntry;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.RelocationPath;
import com.dropbox.core.v2.files.WriteMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

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
     * Copies the file from the given path to the given destination path
     * @param filepath  The path of the original file
     * @param destPath  The destination to put the file at
     * @return
     */
    public Metadata copyFile(String filepath, String destPath) {
        try {
            return getClient()
                    .files()
                    .copy(filepath, destPath);
        } catch (DbxException e) {
            e.printStackTrace();
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
     *  @param name
     * @param path
     */
    public FolderMetadata createFolder(String name, String path) {
        try {
            return getClient()
                    .files()
                    .createFolder( path+File.separator+name);
        } catch (DbxException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *
     * @param oldPath
     * @param newPath
     * @return
     */
    public Metadata rename(String oldPath, String newPath) {
        try {
            return  getClient()
                    .files()
                    .move(oldPath, newPath);
        } catch (DbxException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Metadata> uploadBatch(List<String> paths, String destPath) {
        return null;
    }


    public List<Metadata> copyBatch(List<String> paths, String destPath) {
        List<RelocationPath> copyArgs = Stream.of(paths)
                                    .map((path) -> new RelocationPath(path, destPath))
                                    .toList();

        try {
            getClient()
                    .files()
                    .copyBatch(copyArgs);
        } catch (DbxException e) {
            e.printStackTrace();
        }
        return null;
    }


    public List<DeleteBatchResultEntry> deleteBatch(List<String> paths) {
        List<DeleteArg> toDelete = Stream.of(paths)
                                           .map(DeleteArg::new)
                                           .toList();
        List<String> failedItems = new ArrayList<>();

        try {
            DeleteBatchLaunch dbl =  getClient().files().deleteBatch(toDelete);

            if (dbl.isComplete()) {
                DeleteBatchResult result = dbl.getCompleteValue();
                return result.getEntries();
            }

            if (dbl.isAsyncJobId()) {
                String id = dbl.getAsyncJobIdValue();
                DeleteBatchJobStatus status = getClient().files().deleteBatchCheck(id);

                while(status.isInProgress()) {}

                if (status.isComplete()) {
                    return status.getCompleteValue().getEntries();
                }

                if (status.isFailed()) {
                    Log.d("DELETE_BATCH_FAILED", status.getFailedValue().toString());

                }
            }

            if (dbl.isOther()) {
                //TODO: Handle whatever the hell gets here
            }

        } catch (DbxException e) {
            e.printStackTrace();
        }
        return null;
    }

}
