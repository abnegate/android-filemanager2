package com.jakebarnby.filemanager.activities.source.onedrive;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.jakebarnby.filemanager.activities.source.SourceFragment;
import com.jakebarnby.filemanager.managers.OneDriveFactory;
import com.jakebarnby.filemanager.models.files.OneDriveFile;
import com.jakebarnby.filemanager.models.files.SourceFile;
import com.jakebarnby.filemanager.util.TreeNode;
import com.microsoft.graph.concurrency.ICallback;
import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.core.DefaultClientConfig;
import com.microsoft.graph.core.IClientConfig;
import com.microsoft.graph.extensions.DriveItem;
import com.microsoft.graph.extensions.GraphServiceClient;
import com.microsoft.graph.extensions.IDriveItemCollectionPage;
import com.microsoft.identity.client.AuthenticationCallback;
import com.microsoft.identity.client.AuthenticationResult;
import com.microsoft.identity.client.MsalClientException;
import com.microsoft.identity.client.MsalException;
import com.microsoft.identity.client.MsalServiceException;
import com.microsoft.identity.client.MsalUiRequiredException;
import com.microsoft.identity.client.PublicClientApplication;
import com.microsoft.identity.client.User;

import java.util.List;

/**
 * Created by Jake on 6/7/2017.
 */
public class OneDriveFragment extends SourceFragment {

    public static final String              TAG         = "ONEDRIVE";
    final static String                     CLIENT_ID   = "019d333e-3e7f-4c04-a214-f12602dd5b10";
    private static final String[]           SCOPES      = {"https://graph.microsoft.com/Files.ReadWrite"};

    private PublicClientApplication         mClient;
    private AuthenticationResult            mAuthResult;

    /**
     * Return a new instance of this Fragment
     * @param sourceName The name of the source controlled by this fragment
     * @return A new instance of this fragment
     */
    public static SourceFragment newInstance(String sourceName) {
        SourceFragment fragment = new OneDriveFragment();
        Bundle args = new Bundle();
        args.putString("TITLE", sourceName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mClient = new PublicClientApplication(getActivity().getApplicationContext(), CLIENT_ID);
    }

    @Override
    public void onResume() {
        super.onResume();
        checkForAccessToken();
    }

    @Override
    protected void authenticateSource() {
        mConnectButton.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
        if (!isLoggedIn()) {
            List<User> users;
            try {
                users = mClient.getUsers();
                if (users != null && users.size() == 1) {
                    mClient.acquireTokenSilentAsync(SCOPES, users.get(0), getAuthSilentCallback());
                } else {
                    mClient.acquireToken(this, SCOPES, getAuthInteractiveCallback());
                }
            } catch (MsalClientException e) {
                Log.d(TAG, "MSAL Exception Generated while getting users: " + e.toString());
                mConnectButton.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);

            } catch (IndexOutOfBoundsException e) {
                Log.d(TAG, "User at this position does not exist: " + e.toString());
                    mConnectButton.setVisibility(View.VISIBLE);
                    mProgressBar.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mClient.handleInteractiveRequestRedirect(requestCode, resultCode, data);
    }

    @Override
    protected void loadSource() {
        if (!isFilesLoaded() && mAuthResult != null) {
            final IClientConfig mClientConfig = DefaultClientConfig
                    .createWithAuthenticationProvider(iHttpRequest -> {
                            iHttpRequest.addHeader("Authorization", String.format("Bearer %s", mAuthResult.getAccessToken()));
                    });

            OneDriveFactory
                    .getInstance()
                    .setGraphClient(
                        new GraphServiceClient
                            .Builder()
                            .fromConfig(mClientConfig)
                            .buildClient());

            OneDriveFactory
                    .getInstance()
                    .getGraphClient()
                    .getMe()
                    .getDrive()
                    .getRoot()
                    .buildRequest()
                    .get(new ICallback<DriveItem>() {
                        @Override
                        public void success(DriveItem driveItem) {
                            new OneDriveFileSystemLoader().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, driveItem);
                        }

                        @Override
                        public void failure(final ClientException ex) {
                            // Handle failure
                        }
                    });
        }
    }

    @Override
    protected void openFile(SourceFile file) {

    }

    /**
     * Check for a valid access token and store it in shared preferences if found, then load the source
     */
    private void checkForAccessToken() {
        SharedPreferences prefs = getActivity().getPreferences(Context.MODE_PRIVATE);
        String accessToken = prefs.getString("onedrive-access-token", null);
        if (mAuthResult != null) {
            if (accessToken == null || !mAuthResult.getAccessToken().equals(accessToken)) {
                prefs.edit().putString("onedrive-access-token", mAuthResult.getAccessToken()).apply();
            } else {
                if (!isLoggedIn()) {
                    authenticateSource();
                } else {
                    loadSource();
                }
            }
        }
        if (accessToken != null) {
            if (!isLoggedIn()) {
                authenticateSource();
            } else {
                loadSource();
            }
        }
    }

    /**
     * Callback method for acquireTokenSilent calls
     * Looks if tokens are in the cache (refreshes if necessary and if we don't forceRefresh)
     * else errors that we need to do an interactive request.
     * @return
     */
    private AuthenticationCallback getAuthSilentCallback() {
        return new AuthenticationCallback() {
            @Override
            public void onSuccess(AuthenticationResult authenticationResult) {
                Log.d(TAG, "Successfully authenticated");
                mAuthResult = authenticationResult;
                setLoggedIn(true);
                checkForAccessToken();
            }

            @Override
            public void onError(MsalException exception) {
                Log.d(TAG, "Authentication failed: " + exception.toString());
                if (exception instanceof MsalClientException) {
                } else if (exception instanceof MsalServiceException) {
                    mConnectButton.setVisibility(View.VISIBLE);
                    mProgressBar.setVisibility(View.GONE);
                } else if (exception instanceof MsalUiRequiredException) {
                    mConnectButton.setVisibility(View.VISIBLE);
                    mProgressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "User cancelled login.");
            }
        };
    }


    /**
     * Callback used for interactive request.  If succeeds we use the access
     * token to call the Microsoft Graph. Does not check cache
     * @return
     */
    private AuthenticationCallback getAuthInteractiveCallback() {
        return new AuthenticationCallback() {
            @Override
            public void onSuccess(AuthenticationResult authenticationResult) {
                Log.d(TAG, "Successfully authenticated");
                Log.d(TAG, "ID Token: " + authenticationResult.getIdToken());
                mAuthResult = authenticationResult;
                setLoggedIn(true);
                checkForAccessToken();
            }

            @Override
            public void onError(MsalException exception) {
                Log.d(TAG, "Authentication failed: " + exception.toString());
                if (exception instanceof MsalClientException) {
                    mConnectButton.setVisibility(View.VISIBLE);
                    mProgressBar.setVisibility(View.GONE);
                } else if (exception instanceof MsalServiceException) {
                    mConnectButton.setVisibility(View.VISIBLE);
                    mProgressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "User cancelled login.");
            }
        };
    }

    private class OneDriveFileSystemLoader extends AsyncTask<DriveItem, Void, TreeNode<SourceFile>> {

        private TreeNode<SourceFile> rootFileTreeNode;
        private TreeNode<SourceFile> currentLevelNode;

        @Override
        protected TreeNode<SourceFile> doInBackground(DriveItem... driveItems) {
            SourceFile rootSourceFile = new OneDriveFile();
            ((OneDriveFile) rootSourceFile).setFileProperties(driveItems[0]);
            rootSourceFile.setDirectory(true);
            rootFileTreeNode = new TreeNode<>(rootSourceFile);
            currentLevelNode = rootFileTreeNode;
            setCurrentDirectory(rootFileTreeNode);
            parseFileTree(driveItems[0]);

            return parseFileTree(driveItems[0]);
        }

        /**
         * @param currentDirectory
         */
        private TreeNode<SourceFile> parseFileTree(DriveItem currentDirectory) {
            IDriveItemCollectionPage items = OneDriveFactory
                                                    .getInstance()
                                                    .getGraphClient()
                                                    .getMe()
                                                    .getDrive()
                                                    .getItems(currentDirectory.id)
                                                    .getChildren()
                                                    .buildRequest()
                                                    .select("id, name, webUrl, folder")
                                                    .get();

            List<DriveItem> pageItems = items.getCurrentPage();
            for (DriveItem file : pageItems) {
                SourceFile sourceFile = new OneDriveFile();
                ((OneDriveFile) sourceFile).setFileProperties(file);
                if (file.folder != null) {
                    currentLevelNode.addChild(sourceFile);
                    currentLevelNode = currentLevelNode.getChildren().get(currentLevelNode.getChildren().size() - 1);
                    parseFileTree(file);
                    currentLevelNode = currentLevelNode.getParent();
                } else {
                    currentLevelNode.addChild(sourceFile);
                }
            }
            return rootFileTreeNode;
        }

        @Override
        protected void onPostExecute(TreeNode<SourceFile> sourceFileTreeNode) {
            super.onPostExecute(sourceFileTreeNode);
            setFileTreeRoot(sourceFileTreeNode);
            initializeSourceRecyclerView(sourceFileTreeNode, createOnClickListener(), createOnLongClickListener());
            setFilesLoaded(true);
            mProgressBar.setVisibility(View.GONE);
        }
    }
}
