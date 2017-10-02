package com.jakebarnby.filemanager.sources.onedrive;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.jakebarnby.filemanager.sources.models.Source;
import com.jakebarnby.filemanager.sources.SourceListener;
import com.jakebarnby.filemanager.sources.models.SourceType;
import com.jakebarnby.filemanager.util.Constants;
import com.jakebarnby.filemanager.util.PreferenceUtils;
import com.jakebarnby.filemanager.util.Utils;
import com.microsoft.graph.concurrency.ICallback;
import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.core.DefaultClientConfig;
import com.microsoft.graph.core.IClientConfig;
import com.microsoft.graph.extensions.DriveItem;
import com.microsoft.graph.extensions.GraphServiceClient;
import com.microsoft.identity.client.AuthenticationCallback;
import com.microsoft.identity.client.AuthenticationResult;
import com.microsoft.identity.client.MsalClientException;
import com.microsoft.identity.client.MsalException;
import com.microsoft.identity.client.PublicClientApplication;
import com.microsoft.identity.client.User;

import java.util.List;

/**
 * Created by jakebarnby on 2/08/17.
 */

public class OneDriveSource extends Source {

    private static final String     CLIENT_ID = "019d333e-3e7f-4c04-a214-f12602dd5b10";
    private static final String[]   SCOPES = {"https://graph.microsoft.com/Files.ReadWrite"};

    private PublicClientApplication     mClient;
    private AuthenticationResult    mAuthResult;

    public OneDriveSource(String sourceName, SourceListener listener) {
        super(SourceType.REMOTE, sourceName, listener);
    }

    public PublicClientApplication getClient() {
        return mClient;
    }

    @Override
    public void authenticateSource(Context context) {}

    public void authenticateSource(Fragment fragment) {
        mClient = new PublicClientApplication(fragment.getContext(), CLIENT_ID);
        mSourceListener.onLoadStarted();
        if (!isLoggedIn()) {
            try {
                String accessToken = PreferenceUtils.getString(fragment.getContext(), Constants.Prefs.ONEDRIVE_TOKEN_KEY, null);
                String userId = PreferenceUtils.getString(fragment.getContext(), Constants.Prefs.ONEDRIVE_NAME_KEY, null);

                if (accessToken != null && userId != null) {
                    mClient.acquireTokenSilentAsync(SCOPES, mClient.getUser(userId), getAuthSilentCallback(fragment));
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

    @Override
    public void logout(Context context) {
        if (isLoggedIn()) {
            OneDriveFactory.getInstance().logout(context);
            setLoggedIn(false);
            setFilesLoaded(false);
            mAuthResult = null;
            mSourceListener.onLogout();

            Utils.logFirebaseEvent(
                    FirebaseAnalytics.getInstance(context),
                    Constants.Analytics.EVENT_LOGOUT_ONEDRIVE);
        }
    }

    /**
     * Check for a valid access token and store it in shared preferences if found, then load the source
     */
    public void checkForAccessToken(Fragment fragment) {
        String accessToken = PreferenceUtils.getString(fragment.getContext(), Constants.Prefs.ONEDRIVE_TOKEN_KEY, null);
        if (mAuthResult != null) {
            if (accessToken == null || !mAuthResult.getAccessToken().equals(accessToken)) {
                accessToken = mAuthResult.getAccessToken();
                String userId = mAuthResult.getUser().getUserIdentifier();
                PreferenceUtils.savePref(fragment.getContext(), Constants.Prefs.ONEDRIVE_TOKEN_KEY, accessToken);
                PreferenceUtils.savePref(fragment.getContext(), Constants.Prefs.ONEDRIVE_NAME_KEY, userId);
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
                Utils.logFirebaseEvent(
                        FirebaseAnalytics.getInstance(fragment.getContext()),
                        Constants.Analytics.EVENT_LOGIN_ONEDRIVE);
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
