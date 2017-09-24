package com.jakebarnby.filemanager.sources.dropbox;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.android.Auth;
import com.dropbox.core.http.OkHttp3Requestor;
import com.dropbox.core.v2.DbxClientV2;
import com.jakebarnby.filemanager.R;
import com.jakebarnby.filemanager.sources.models.Source;
import com.jakebarnby.filemanager.sources.SourceListener;
import com.jakebarnby.filemanager.sources.models.SourceType;
import com.jakebarnby.filemanager.util.Constants;


/**
 * Created by jakebarnby on 2/08/17.
 */

public class DropboxSource extends Source {

    public DropboxSource(String sourceName, SourceListener listener) {
        super(SourceType.REMOTE, sourceName, listener);
        setLogoId(R.drawable.ic_dropbox);
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
            new DropboxLoaderTask(this, mSourceListener).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
        }
    }

    @Override
    public void logout(Context context) {
        if (isLoggedIn()) {
            DropboxFactory.getInstance().logout(context);
            setLoggedIn(false);
            setFilesLoaded(false);
            mSourceListener.onLogout();
        }
    }

    /**
     * Set up the dropbox client
     * @param accessToken The access token for this dropbox session
     */
    public void setupClient(String accessToken) {
        if (!isLoggedIn()) {
            DbxRequestConfig requestConfig = DbxRequestConfig
                    .newBuilder("FileManagerAndroid/1.0")
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
        SharedPreferences prefs = context.getSharedPreferences(Constants.Prefs.PREFS, Context.MODE_PRIVATE);
        String accessToken = prefs.getString(Constants.Prefs.DROPBOX_TOKEN_KEY, null);
        if (accessToken == null) {
            accessToken = Auth.getOAuth2Token();
            if (accessToken != null) {
                prefs.edit().putString(Constants.Prefs.DROPBOX_TOKEN_KEY, accessToken).apply();
                setupClient(accessToken);
                loadSource(context);
            }
        } else {
            if (!isLoggedIn()) {
                setupClient(accessToken);
                loadSource(context);
            }
        }
    }
}
