package com.jakebarnby.filemanager.sources.local;

import android.Manifest;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;

import com.jakebarnby.filemanager.R;
import com.jakebarnby.filemanager.sources.models.Source;
import com.jakebarnby.filemanager.sources.SourceListener;
import com.jakebarnby.filemanager.sources.models.SourceFile;
import com.jakebarnby.filemanager.sources.models.SourceType;
import com.jakebarnby.filemanager.util.Constants;
import com.jakebarnby.filemanager.util.TreeNode;
import com.jakebarnby.filemanager.util.Utils;

import java.io.File;

/**
 * Created by jakebarnby on 2/08/17.
 */

public class LocalSource extends Source {

    private String rootPath;

    public LocalSource(String sourceName, String rootPath, SourceListener listener) {
        super(SourceType.LOCAL, sourceName, listener);
        this.rootPath = rootPath;
    }

    @Override
    public void authenticateSource(Context context) {
        mSourceListener.onCheckPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Constants.RequestCodes.STORAGE_PERMISSIONS);
    }

    @Override
    public void loadSource(Context context) {
        if (!isFilesLoaded()) {
            new LocalLoaderTask(this, mSourceListener)
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, rootPath);
        }
    }

    @Override
    public void logout(Context context) {

    }
}
