package com.jakebarnby.filemanager.models;

import android.Manifest;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;

import com.jakebarnby.filemanager.models.files.LocalFile;
import com.jakebarnby.filemanager.models.files.SourceFile;
import com.jakebarnby.filemanager.util.Constants;
import com.jakebarnby.filemanager.util.TreeNode;
import com.jakebarnby.filemanager.util.Utils;

import java.io.File;

/**
 * Created by jakebarnby on 2/08/17.
 */

public class LocalSource extends Source {

    public LocalSource(String sourceName, SourceListener listener) {
        super(sourceName, listener);
    }

    @Override
    public void authenticateSource(Context context) {
        mSourceListener.onCheckPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Constants.RequestCodes.STORAGE_PERMISSIONS);
    }

    @Override
    public void loadSource(Context context) {
        if (!isFilesLoaded()) {
            new LocalFileSystemLoader().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Environment.getExternalStorageDirectory());
        }
    }

    /**
     * Loads a file tree from the local file system
     */
    private class LocalFileSystemLoader extends AsyncTask<File, Void, TreeNode<SourceFile>> {
        private TreeNode<SourceFile> rootFileTreeNode;
        private TreeNode<SourceFile> currentLevelNode;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mSourceListener.onLoadStarted();
        }

        @Override
        protected TreeNode<SourceFile> doInBackground(File... files) {
            File rootFile = files[0];
            SourceFile rootSourceFile = new LocalFile(rootFile);
            rootFileTreeNode = new TreeNode<>(rootSourceFile);
            currentLevelNode = rootFileTreeNode;
            setCurrentDirectory(rootFileTreeNode);
            setQuotaInfo(Utils.getStorageStats(Environment.getExternalStorageDirectory()));
            return parseFileSystem(files[0]);
        }

        /**
         * Recursively build a tree representing the files and folders of this device
         *
         * @param currentDir    The directory to start the search at
         * @return              The root node of the tree
         */
        private TreeNode<SourceFile> parseFileSystem(File currentDir) {
            File listFile[] = currentDir.listFiles();
            long dirSize = 0L;
            if (listFile != null && listFile.length > 0) {
                for (File file : listFile) {
                    SourceFile sourceFile = new LocalFile(file);
                    if (file.isDirectory()) {
                        currentLevelNode.addChild(sourceFile);
                        currentLevelNode = currentLevelNode.getChildren().get(currentLevelNode.getChildren().size() - 1);
                        parseFileSystem(file);
                        currentLevelNode.getParent().getData().addSize(currentLevelNode.getData().getSize());
                        currentLevelNode = currentLevelNode.getParent();
                    } else {
                        dirSize += file.length();
                        currentLevelNode.addChild(sourceFile);
                    }
                }
                currentLevelNode.getData().addSize(dirSize);
            }
            return rootFileTreeNode;
        }

        @Override
        protected void onPostExecute(TreeNode<SourceFile> fileTree) {
            super.onPostExecute(fileTree);
            TreeNode.sortTree(fileTree, (node1, node2) -> {
                int result = Boolean.valueOf(!node1.getData().isDirectory()).compareTo(!node2.getData().isDirectory());
                if (result == 0) {
                    result = node1.getData().getName().toLowerCase().compareTo(node2.getData().getName().toLowerCase());
                }
                return result;
            });

            setFilesLoaded(true);
            mSourceListener.onLoadComplete(fileTree);
        }
    }
}
