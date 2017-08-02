package com.jakebarnby.filemanager.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.jakebarnby.filemanager.managers.OneDriveFactory;
import com.jakebarnby.filemanager.models.files.OneDriveFile;
import com.jakebarnby.filemanager.models.files.SourceFile;
import com.jakebarnby.filemanager.util.Constants;
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
 * Created by jakebarnby on 2/08/17.
 */

public class OneDriveSource extends Source {

    public static final String      TAG = "ONEDRIVE";
    private final static String     CLIENT_ID = "019d333e-3e7f-4c04-a214-f12602dd5b10";
    private static final String[]   SCOPES = {"https://graph.microsoft.com/Files.ReadWrite"};

    private PublicClientApplication mClient;
    private AuthenticationResult mAuthResult;

    public OneDriveSource(String sourceName, SourceListener listener) {
        super(sourceName, listener);
    }

    @Override
    public void authenticateSource(Context context) {

    }

    public PublicClientApplication getClient() {
        return mClient;
    }

    public void authenticateSource(Fragment fragment) {
        mClient = new PublicClientApplication(fragment.getContext(), CLIENT_ID);
        mSourceListener.onLoadStarted();
        if (!isLoggedIn()) {
            List<User> users;
            try {
                users = mClient.getUsers();
                if (users != null && users.size() == 1) {
                    mClient.acquireTokenSilentAsync(SCOPES, users.get(0), getAuthSilentCallback(fragment.getContext()));
                } else {
                    mClient.acquireToken(fragment, SCOPES, getAuthInteractiveCallback(fragment.getContext()));
                }
            } catch (MsalClientException e) {
                Log.d(TAG, "MSAL Exception Generated while getting users: " + e.toString());
                mSourceListener.onLoadAborted();

            } catch (IndexOutOfBoundsException e) {
                Log.d(TAG, "User at this position does not exist: " + e.toString());
                mSourceListener.onLoadAborted();
            }
        }
    }

    @Override
    public void loadSource(Context context) {
        if (!isFilesLoaded() && mAuthResult != null) {
            if (!checkConnectionActive(context)) return;
            final IClientConfig mClientConfig = DefaultClientConfig
                    .createWithAuthenticationProvider(iHttpRequest -> {
                        iHttpRequest.addHeader("Authorization", String.format("Bearer %s", mAuthResult.getAccessToken()));
                    });

            OneDriveFactory
                    .getInstance()
                    .setGraphClient(new GraphServiceClient
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


    /**
     * Check for a valid access token and store it in shared preferences if found, then load the source
     */
    public void checkForAccessToken(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.SharedPrefs.PREFS, Context.MODE_PRIVATE);
        String accessToken = prefs.getString("onedrive-access-token", null);
        if (mAuthResult != null) {
            if (accessToken == null || !mAuthResult.getAccessToken().equals(accessToken)) {
                accessToken = mAuthResult.getAccessToken();
                prefs.edit().putString("onedrive-access-token", accessToken).apply();
            } else {
                if (!isLoggedIn()) {
                    authenticateSource(context);
                } else {
                    loadSource(context);
                }
            }
        }
        if (accessToken != null) {
            if (!isLoggedIn()) {
                authenticateSource(context);
            } else {
                loadSource(context);
            }
        }
    }


    /**
     * OnSpaceCheckCompleteListener method for acquireTokenSilent calls
     * Looks if tokens are in the cache (refreshes if necessary and if we don't forceRefresh)
     * else errors that we need to do an interactive request.
     *
     * @return
     */
    private AuthenticationCallback getAuthSilentCallback(Context context) {
        return new AuthenticationCallback() {
            @Override
            public void onSuccess(AuthenticationResult authenticationResult) {
                mAuthResult = authenticationResult;
                setLoggedIn(true);
                checkForAccessToken(context);
            }

            @Override
            public void onError(MsalException exception) {
                if (exception instanceof MsalClientException) {
                    mSourceListener.onLoadAborted();
                } else if (exception instanceof MsalServiceException) {
                    mSourceListener.onLoadAborted();
                } else if (exception instanceof MsalUiRequiredException) {
                    mSourceListener.onLoadAborted();
                }
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "User cancelled login.");
            }
        };
    }

    /**
     * OnSpaceCheckCompleteListener used for interactive request.  If succeeds we use the access
     * token to call the Microsoft Graph. Does not check cache
     *
     * @return
     */
    private AuthenticationCallback getAuthInteractiveCallback(Context context) {
        return new AuthenticationCallback() {
            @Override
            public void onSuccess(AuthenticationResult authenticationResult) {
                mAuthResult = authenticationResult;
                setLoggedIn(true);
                checkForAccessToken(context);
            }

            @Override
            public void onError(MsalException exception) {
                if (exception instanceof MsalClientException) {
                    mSourceListener.onLoadAborted();
                } else if (exception instanceof MsalServiceException) {
                    mSourceListener.onLoadAborted();
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
            SourceFile rootSourceFile = new OneDriveFile(driveItems[0]);
            rootSourceFile.setDirectory(true);
            rootFileTreeNode = new TreeNode<>(rootSourceFile);
            currentLevelNode = rootFileTreeNode;
            setCurrentDirectory(rootFileTreeNode);
            setQuotaInfo(OneDriveFactory.getInstance().getStorageStats());

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
                    .select("id,name,webUrl,folder,size,createdDateTime,lastModifiedDateTime")
                    .expand("thumbnails")
                    .get();

            List<DriveItem> pageItems = items.getCurrentPage();
            long dirSize = 0L;
            if (pageItems != null) {
                for (DriveItem file : pageItems) {
                    SourceFile sourceFile = new OneDriveFile(file);
                    if (file.folder != null) {
                        currentLevelNode.addChild(sourceFile);
                        currentLevelNode = currentLevelNode.getChildren().get(currentLevelNode.getChildren().size() - 1);
                        parseFileTree(file);
                        currentLevelNode.getParent().getData().addSize(currentLevelNode.getData().getSize());
                        currentLevelNode = currentLevelNode.getParent();
                    } else {
                        dirSize += file.size;
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
