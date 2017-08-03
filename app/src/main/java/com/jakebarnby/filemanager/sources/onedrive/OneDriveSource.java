package com.jakebarnby.filemanager.sources.onedrive;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.jakebarnby.filemanager.sources.models.Source;
import com.jakebarnby.filemanager.sources.SourceListener;
import com.jakebarnby.filemanager.sources.models.SourceFile;
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
    public void authenticateSource(Context context) {}

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
                    mClient.acquireTokenSilentAsync(SCOPES, users.get(0), getAuthSilentCallback(fragment));
                } else {
                    mClient.acquireToken(fragment, SCOPES, getAuthInteractiveCallback(fragment));
                }
            } catch (MsalClientException | IndexOutOfBoundsException e) {
                mSourceListener.onLoadError(e.getMessage());
            }
        }
    }

    @Override
    public void loadSource(Context context) {
        if (!isFilesLoaded() && mAuthResult != null) {
            if (!checkConnectionActive(context)) return;
            final IClientConfig mClientConfig = DefaultClientConfig
                    .createWithAuthenticationProvider(request ->
                            request.addHeader("Authorization", String.format("Bearer %s", mAuthResult.getAccessToken()))
                    );

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
                            new OneDriveLoaderTask(OneDriveSource.this, mSourceListener, driveItem)
                                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
                        }

                        @Override
                        public void failure(final ClientException ex) {
                            mSourceListener.onLoadAborted();
                        }
                    });
        }
    }


    /**
     * Check for a valid access token and store it in shared preferences if found, then load the source
     */
    public void checkForAccessToken(Fragment fragment) {
        SharedPreferences prefs = fragment.getContext().getSharedPreferences(Constants.SharedPrefs.PREFS, Context.MODE_PRIVATE);
        String accessToken = prefs.getString("onedrive-access-token", null);
        if (mAuthResult != null) {
            if (accessToken == null || !mAuthResult.getAccessToken().equals(accessToken)) {
                accessToken = mAuthResult.getAccessToken();
                prefs.edit().putString("onedrive-access-token", accessToken).apply();
            } else {
                if (!isLoggedIn()) {
                    authenticateSource(fragment);
                } else {
                    loadSource(fragment.getContext());
                }
            }
        }
        if (accessToken != null) {
            if (!isLoggedIn()) {
                authenticateSource(fragment);
            } else {
                loadSource(fragment.getContext());
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
    private AuthenticationCallback getAuthSilentCallback(Fragment fragment) {
        return new AuthenticationCallback() {
            @Override
            public void onSuccess(AuthenticationResult authenticationResult) {
                mAuthResult = authenticationResult;
                setLoggedIn(true);
                checkForAccessToken(fragment);
            }

            @Override
            public void onError(MsalException exception) {
                mSourceListener.onLoadError(exception.getErrorCode() + " "  + exception.getMessage());
            }

            @Override
            public void onCancel() {
                mSourceListener.onLoadAborted();
            }
        };
    }

    /**
     * OnSpaceCheckCompleteListener used for interactive request.  If succeeds we use the access
     * token to call the Microsoft Graph. Does not check cache
     *
     * @return
     */
    private AuthenticationCallback getAuthInteractiveCallback(Fragment fragment) {
        return new AuthenticationCallback() {
            @Override
            public void onSuccess(AuthenticationResult authenticationResult) {
                mAuthResult = authenticationResult;
                setLoggedIn(true);
                checkForAccessToken(fragment);
            }

            @Override
            public void onError(MsalException exception) {
                mSourceListener.onLoadError(exception.getErrorCode() + " " +    exception.getMessage());
            }

            @Override
            public void onCancel() {
                mSourceListener.onLoadAborted();
            }
        };
    }
}
