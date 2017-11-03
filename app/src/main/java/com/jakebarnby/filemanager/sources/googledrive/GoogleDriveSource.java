package com.jakebarnby.filemanager.sources.googledrive;

import android.Manifest;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.DriveScopes;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.jakebarnby.filemanager.sources.SourceListener;
import com.jakebarnby.filemanager.sources.models.Source;
import com.jakebarnby.filemanager.sources.models.SourceType;
import com.jakebarnby.filemanager.util.Constants;
import com.jakebarnby.filemanager.util.GooglePlayUtils;
import com.jakebarnby.filemanager.util.LogUtils;
import com.jakebarnby.filemanager.util.PreferenceUtils;
import com.jakebarnby.filemanager.util.Utils;

import java.io.IOException;
import java.util.Arrays;

import static com.jakebarnby.filemanager.util.Constants.RequestCodes.ACCOUNT_PICKER;

/**
 * Created by jakebarnby on 2/08/17.
 */

public class GoogleDriveSource extends Source {

    private static final String[] SCOPES = {DriveScopes.DRIVE};
    private GoogleAccountCredential mCredential;

    public GoogleDriveSource(String sourceName, SourceListener listener) {
        super(SourceType.REMOTE, sourceName, listener);
    }

    @Override
    public void authenticateSource(Context context) {
        if (!checkConnectionActive(context)) return;
        fetchCredential(context);

        if (hasToken(context, getSourceName())) {
            loadSource(context);
        } else {
            mSourceListener.onCheckPermissions(Manifest.permission.GET_ACCOUNTS, Constants.RequestCodes.ACCOUNTS_PERMISSIONS);
        }
    }

    @Override
    public void loadSource(Context context) {
        if (!isFilesLoaded()) {
            if (!checkConnectionActive(context)) return;
            new GoogleDriveLoaderTask(this, mSourceListener, mCredential)
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "root");
        }
    }

    @Override
    public void logout(Context context) {
        if (isLoggedIn()) {
            GoogleDriveFactory.getInstance().logout(context);
            setLoggedIn(false);
            setFilesLoaded(false);
            setCredential(null);
            mSourceListener.onLogout();

            LogUtils.logFirebaseEvent(
                    FirebaseAnalytics.getInstance(context),
                    Constants.Analytics.EVENT_LOGOUT_GOOGLEDRIVE);
        }
    }

    public void fetchCredential(Context context) {
        mCredential = GoogleAccountCredential
                .usingOAuth2(context, Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
    }

    public void setCredential(GoogleAccountCredential credential) {
        mCredential = credential;
    }

    public void authGoogle(Fragment fragment) {
        getResultsFromApi(fragment);
    }

    /**
     * Create a google credential and try to call the API with it, do nothing if it fails
     */
    public void authGoogleSilent(Fragment fragment) {
        String accountName = PreferenceUtils.getString(
                fragment.getContext(),
                Constants.Prefs.GOOGLE_NAME_KEY,
                null);

        if (accountName != null) {
            fetchCredential(fragment.getContext());
            mCredential.setSelectedAccountName(accountName);
            getResultsFromApi(fragment);
        }
    }

    /**
     * Attempt to call the API, after verifying that all the preconditions are satisfied
     */
    public void getResultsFromApi(Fragment fragment) {
        if (!isLoggedIn()) {
            if (!GooglePlayUtils.isGooglePlayServicesAvailable(fragment.getContext())) {
                GooglePlayUtils.acquireGooglePlayServices(fragment.getActivity());
            } else if (mCredential != null && mCredential.getSelectedAccountName() == null) {
                fragment.startActivityForResult(mCredential.newChooseAccountIntent(), ACCOUNT_PICKER);
            } else if (!Utils.isConnectionReady(fragment.getContext())) {
                mSourceListener.onNoConnection();
            } else {
                setLoggedIn(true);
                loadSource(fragment.getContext());
            }
        }
    }

    public void saveUserToken(Fragment fragment) {
        try {
            PreferenceUtils.savePref(fragment.getContext(), Constants.Prefs.GOOGLE_TOKEN_KEY, mCredential.getToken());
        } catch (IOException | GoogleAuthException e) {
            e.printStackTrace();
        }
        getResultsFromApi(fragment);
    }

    public void saveUserAccount(Fragment fragment, String accountName) {
        PreferenceUtils.savePref(fragment.getContext(),Constants.Prefs.GOOGLE_NAME_KEY, accountName);
        mCredential.setSelectedAccountName(accountName);
        getResultsFromApi(fragment);
    }
}
