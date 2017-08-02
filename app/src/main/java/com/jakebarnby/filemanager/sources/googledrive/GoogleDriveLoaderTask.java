package com.jakebarnby.filemanager.sources.googledrive;

/**
 * Created by Jake on 8/2/2017.
 */

import android.os.AsyncTask;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.jakebarnby.filemanager.sources.LoaderTask;
import com.jakebarnby.filemanager.sources.SourceListener;
import com.jakebarnby.filemanager.sources.models.Source;
import com.jakebarnby.filemanager.sources.models.SourceFile;
import com.jakebarnby.filemanager.util.TreeNode;

import java.io.IOException;
import java.util.List;

/**
 * An asynchronous task that handles the Drive API call.
 * Placing the API calls in their own task ensures the UI stays responsive.
 */
public class GoogleDriveLoaderTask extends LoaderTask {

    public GoogleDriveLoaderTask(Source source, SourceListener listener, GoogleAccountCredential credential) {
        super(source, listener);
        HttpTransport transport = new NetHttpTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        GoogleDriveFactory.getInstance().setService(
                new com.google.api.services.drive.Drive.Builder(transport, jsonFactory, credential)
                        .setApplicationName("File Manager Android")
                        .build());
    }

    @Override
    public Object initRootNode(String path) {
        File rootFile = null;
        try {
            rootFile = GoogleDriveFactory
                    .getInstance()
                    .getService()
                    .files()
                    .get(path)
                    .setFields("name,id,mimeType,parents,size,hasThumbnail,thumbnailLink,iconLink,modifiedTime")
                    .execute();
            SourceFile rootSourceFile = new GoogleDriveFile(rootFile);
            mRootTreeNode = new TreeNode<>(rootSourceFile);
            mCurrentNode = mRootTreeNode;
            mSource.setCurrentDirectory(mRootTreeNode);
            mSource.setQuotaInfo(GoogleDriveFactory.getInstance().getStorageStats());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rootFile;
    }

        @Override
    public TreeNode<SourceFile> readFileTree(Object rootObject) {
        if (rootObject != null && rootObject instanceof File) {
            File node = (File)rootObject;
            FileList fileList = null;
            try {
                fileList = GoogleDriveFactory
                        .getInstance()
                        .getService()
                        .files()
                        .list()
                        .setQ(String.format("'%s' in parents", node.getId()))
                        .setFields("files(name,id,mimeType,parents,size,hasThumbnail,thumbnailLink,iconLink,modifiedTime)")
                        .execute();
            } catch (IOException e) {
                e.printStackTrace();
            }

            List<File> files = fileList.getFiles();
            long dirSize = 0L;
            if (files != null) {
                for (File file : files) {
                    SourceFile sourceFile = new GoogleDriveFile(file);
                    if (sourceFile.isDirectory()) {
                        mCurrentNode.addChild(sourceFile);
                        mCurrentNode = mCurrentNode.getChildren().get(mCurrentNode.getChildren().size() - 1);
                        readFileTree(file);
                        mCurrentNode.getParent().getData().addSize(mCurrentNode.getData().getSize());
                        mCurrentNode = mCurrentNode.getParent();
                    } else {
                        if (file.getSize() != null) {
                            dirSize += file.getSize();
                        }
                        mCurrentNode.addChild(sourceFile);
                    }
                }
                mCurrentNode.getData().addSize(dirSize);
            }
        }
        return mRootTreeNode;
    }
}
