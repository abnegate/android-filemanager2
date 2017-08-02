package com.jakebarnby.filemanager.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.android.Auth;
import com.dropbox.core.http.OkHttp3Requestor;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.jakebarnby.filemanager.managers.DropboxFactory;
import com.jakebarnby.filemanager.models.files.DropboxFile;
import com.jakebarnby.filemanager.models.files.SourceFile;
import com.jakebarnby.filemanager.util.Constants;
import com.jakebarnby.filemanager.util.TreeNode;

/**
 * Created by jakebarnby on 2/08/17.
 */

public class DropboxSource extends Source {

    public DropboxSource(String sourceName, SourceListener listener) {
        super(sourceName, listener);
    }

    @Override
    public void authenticateSource(Context context) {
        if (!hasToken(context, getSourceName())) {
            Auth.startOAuth2Authentication(context, Constants.Sources.Keys.DROPBOX_CLIENT_ID);
        }
    }

    @Override
    public void loadSource(Context context) {
        if (!isFilesLoaded()) {
            if (!checkConnectionActive(context)) return;
            new DropboxFileSystemLoader().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
        }
    }

    /**
     * Set up the dropbox client
     * @param accessToken The access token for this dropbox session
     */
    public void setupClient(String accessToken) {
        if (!isLoggedIn()) {
            DbxRequestConfig requestConfig = DbxRequestConfig.newBuilder("FileManagerAndroid/1.0")
                    .withHttpRequestor(new OkHttp3Requestor(OkHttp3Requestor.defaultOkHttpClient()))
                    .build();
            DropboxFactory.getInstance().setClient(new DbxClientV2(requestConfig, accessToken));
            setLoggedIn(true);
        }
    }

    /**
     * Check for a valid access token and store it in shared preferences if found, then load the source
     */
    public void checkForAccessToken(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.SharedPrefs.PREFS, Context.MODE_PRIVATE);
        String accessToken = prefs.getString("dropbox-access-token", null);
        if (accessToken == null) {
            accessToken = Auth.getOAuth2Token();
            if (accessToken != null) {
                prefs.edit().putString("dropbox-access-token", accessToken).apply();
                setupClient(accessToken);
                loadSource(context);
            }
        } else {
            if (!isLoggedIn()) {
                setupClient(accessToken);
            }
            loadSource(context);
        }
    }

    /**
     * Loads a file tree from a users Dropbox account
     */
    private class DropboxFileSystemLoader extends AsyncTask<String, Void, TreeNode<SourceFile>> {
        private TreeNode<SourceFile> rootFileTreeNode;
        private TreeNode<SourceFile> currentLevelNode;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mSourceListener.onLoadStarted();
        }

        @Override
        protected TreeNode<SourceFile> doInBackground(String... paths) {
            SourceFile rootSourceFile = new DropboxFile(new Metadata(paths[0]));
            rootSourceFile.setDirectory(true);
            rootFileTreeNode = new TreeNode<>(rootSourceFile);
            currentLevelNode = rootFileTreeNode;
            setCurrentDirectory(rootFileTreeNode);
            setQuotaInfo(DropboxFactory.getInstance().getStorageStats());

            ListFolderResult result = null;
            try {
                result = DropboxFactory
                        .getInstance()
                        .getClient()
                        .files()
                        .listFolderBuilder(paths[0])
                        //.withRecursive(true)
                        .start();
            } catch (DbxException e) {
                e.printStackTrace();
            }
            return parseFileSystem(result);
        }

        /**
         * Recursively build a tree representing the files and folders of this device
         * @param result    The directory to start the search at
         * @return          The root node of the tree
         */
        private TreeNode<SourceFile> parseFileSystem(ListFolderResult result) {
            if (result != null) {
                long dirSize = 0L;
                for (Metadata data : result.getEntries()) {
                    SourceFile sourceFile = new DropboxFile(data);
                    try {
                        if (!sourceFile.isDirectory()) {
                            sourceFile.setThumbnailLink(DropboxFactory
                                    .getInstance()
                                    .getClient()
                                    .files()
                                    .getTemporaryLink(sourceFile.getPath()).getLink());
                        }
                    } catch (DbxException e) {
                        e.printStackTrace();
                    }
                    if (data instanceof FolderMetadata) {
                        currentLevelNode.addChild(sourceFile);
                        currentLevelNode = currentLevelNode.getChildren().get(currentLevelNode.getChildren().size() - 1);
                        try {
                            parseFileSystem(DropboxFactory
                                    .getInstance()
                                    .getClient()
                                    .files()
                                    .listFolder(data.getPathLower()));
                        } catch (DbxException e) {
                            e.printStackTrace();
                        }
                        currentLevelNode.getParent().getData().addSize(currentLevelNode.getData().getSize());
                        currentLevelNode = currentLevelNode.getParent();
                    } else {
                        dirSize += ((FileMetadata)data).getSize();
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
