package com.jakebarnby.filemanager.activities.source.dropbox;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.android.Auth;
import com.dropbox.core.http.OkHttp3Requestor;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.jakebarnby.filemanager.activities.source.SourceFragment;
import com.jakebarnby.filemanager.models.DropboxFile;
import com.jakebarnby.filemanager.models.SourceFile;
import com.jakebarnby.filemanager.util.Constants;
import com.jakebarnby.filemanager.util.TreeNode;

/**
 * Created by Jake on 5/31/2017.
 */

public class DropboxFragment extends SourceFragment {

    private DbxClientV2         mClient;

    /**
     * Return a new instance of this Fragment
     *
     * @param sourceName The name of the source controlled by this fragment
     * @return A new instance of this fragment
     */
    public static SourceFragment newInstance(String sourceName) {
        SourceFragment fragment = new DropboxFragment();
        Bundle args = new Bundle();
        args.putString("TITLE", sourceName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void authenticateSource() {
        if (!hasToken(getSourceName())) {
            Auth.startOAuth2Authentication(getActivity(), Constants.Sources.Keys.DROPBOX_CLIENT_ID);
            Log.d("Jake", "Hit with token: " + Auth.getOAuth2Token());
        }
    }

    /**
     * Set up the dropbox client
     *
     * @param accessToken The access token for this dropbox session
     */
    private void setupClient(String accessToken) {
        if (!isLoggedIn()) {
            DbxRequestConfig requestConfig = DbxRequestConfig.newBuilder("FileManagerAndroid/1.0")
                    .withHttpRequestor(new OkHttp3Requestor(OkHttp3Requestor.defaultOkHttpClient()))
                    .build();
            mClient = new DbxClientV2(requestConfig, accessToken);
            setLoggedIn(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        checkForAccessToken();
    }

    /**
     * Check for a valid access token and store it in shared preferences if found, then load the source
     */
    private void checkForAccessToken() {
        SharedPreferences prefs = getActivity().getPreferences(Context.MODE_PRIVATE);
        String accessToken = prefs.getString("dropbox-access-token", null);
        if (accessToken == null) {
            accessToken = Auth.getOAuth2Token();
            if (accessToken != null) {
                prefs.edit().putString("dropbox-access-token", accessToken).apply();
                setupClient(accessToken);
                loadSource();
            }
        } else {
            if (!isLoggedIn()) {
                setupClient(accessToken);
            }
            loadSource();
        }
    }

    @Override
    protected void loadSource() {
        if (!isFilesLoaded()) {
            new DropboxFileSystemLoader().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    /**
     * Loads a file tree from a users Dropbox account
     */
    private class DropboxFileSystemLoader extends AsyncTask<Void, Void, TreeNode<SourceFile>> {
        private TreeNode<SourceFile> rootFileTreeNode;
        private TreeNode<SourceFile> currentLevelNode;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mConnectButton.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected TreeNode<SourceFile> doInBackground(Void... voids) {
            SourceFile rootSourceFile = new DropboxFile();
            ((DropboxFile) rootSourceFile).setFileProperties(new Metadata("Dropbox"));
            rootSourceFile.setDirectory(true);

            rootFileTreeNode = new TreeNode<>(rootSourceFile);
            currentLevelNode = rootFileTreeNode;

            ListFolderResult result = null;
            try {
                result = mClient.files().listFolder("");
            } catch (DbxException e) {
                e.printStackTrace();
            }
            return parseFileSystem(result);
        }

        /**
         * Recursively build a tree representing the files and folders of this device
         *
         * @param result    The directory to start the search at
         * @return          The root node of the tree
         */
        private TreeNode<SourceFile> parseFileSystem(ListFolderResult result) {
            if (result != null) {
                for (Metadata data : result.getEntries()) {
                    SourceFile sourceFile = new DropboxFile();
                    ((DropboxFile) sourceFile).setFileProperties(data);
                    if (data instanceof FolderMetadata) {
                        currentLevelNode.addChild(sourceFile);
                        currentLevelNode = currentLevelNode.getChildren().get(currentLevelNode.getChildren().size() - 1);
                        try {
                            parseFileSystem(mClient.files().listFolder(data.getPathLower()));
                        } catch (DbxException e) {
                            e.printStackTrace();
                        }
                        currentLevelNode = currentLevelNode.getParent();
                    } else {
                        currentLevelNode.addChild(sourceFile);
                    }
                }
            }
            return rootFileTreeNode;
        }

        @Override
        protected void onPostExecute(TreeNode<SourceFile> fileTree) {
            super.onPostExecute(fileTree);
            setFileTreeRoot(fileTree);
            initializeSourceRecyclerView(fileTree, createOnClickListener(), createOnLongClickListener());
            setFilesLoaded(true);
            mProgressBar.setVisibility(View.GONE);
            mConnectButton.setVisibility(View.GONE);
        }
    }
}
