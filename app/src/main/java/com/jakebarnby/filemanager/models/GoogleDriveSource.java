package com.jakebarnby.filemanager.models;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.jakebarnby.filemanager.managers.GoogleDriveFactory;
import com.jakebarnby.filemanager.models.files.GoogleDriveFile;
import com.jakebarnby.filemanager.models.files.SourceFile;
import com.jakebarnby.filemanager.util.Constants;
import com.jakebarnby.filemanager.util.TreeNode;
import com.jakebarnby.filemanager.util.Utils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.jakebarnby.filemanager.util.Constants.RequestCodes.ACCOUNT_PICKER;

/**
 * Created by jakebarnby on 2/08/17.
 */

public class GoogleDriveSource extends Source {

    private static final String[] SCOPES = {DriveScopes.DRIVE};
    private GoogleAccountCredential mCredential;

    public GoogleDriveSource(String sourceName, SourceListener listener) {
        super(sourceName, listener);

    }

    @Override
    public void authenticateSource(Context context) {
        mSourceListener.onCheckPermissions(Manifest.permission.GET_ACCOUNTS, Constants.RequestCodes.ACCOUNTS_PERMISSIONS);
    }

    @Override
    public void loadSource(Context context) {
        if (!isFilesLoaded()) {
            if (!checkConnectionActive(context)) return;
            new GoogleDriveFileSystemLoader(mCredential).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "root");
        }
    }

    public void setCredential(Context context) {
        mCredential = GoogleAccountCredential
                .usingOAuth2(context, Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
    }

    public void authGoogle(Fragment fragment) {
        getResultsFromApi(fragment);
    }

    /**
     * Create a google credential and try to call the API with it, do nothing if it fails
     */
    public void authGoogleSilent(Fragment fragment) {
        String accountName = fragment.getContext().getSharedPreferences(Constants.SharedPrefs.PREFS, Context.MODE_PRIVATE)
                .getString(Constants.SharedPrefs.GOOGLE_ACCOUNT_NAME, null);
        if (accountName != null) {
            mCredential.setSelectedAccountName(accountName);
            getResultsFromApi(fragment);
        }
    }

    /**
     * Attempt to call the API, after verifying that all the preconditions are satisfied
     */
    public void getResultsFromApi(Fragment fragment) {
        if (!isLoggedIn()) {
            if (!isGooglePlayServicesAvailable(fragment.getContext())) {
                acquireGooglePlayServices(fragment.getActivity());
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
        SharedPreferences prefs = fragment.getContext().getSharedPreferences(Constants.SharedPrefs.PREFS, Context.MODE_PRIVATE);
        try {
            prefs.edit().putString(Constants.SharedPrefs.GOOGLE_ACCESS_TOKEN, mCredential.getToken()).apply();
        } catch (IOException | GoogleAuthException e) {
            e.printStackTrace();
        }
        getResultsFromApi(fragment);
    }

    public void saveUserAccount(Fragment fragment, String accountName) {
        SharedPreferences prefs = fragment.getContext().getSharedPreferences(Constants.SharedPrefs.PREFS, Context.MODE_PRIVATE);
        prefs.edit().putString(Constants.SharedPrefs.GOOGLE_ACCOUNT_NAME, accountName).apply();
        mCredential.setSelectedAccountName(accountName);
        getResultsFromApi(fragment);
    }

    /**
     * An asynchronous task that handles the Drive API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class GoogleDriveFileSystemLoader extends AsyncTask<String, Void, TreeNode<SourceFile>> {

        private TreeNode<SourceFile> rootFileTreeNode;
        private TreeNode<SourceFile> currentLevelNode;

        GoogleDriveFileSystemLoader(GoogleAccountCredential credential) {
            HttpTransport transport = new NetHttpTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            GoogleDriveFactory.getInstance().setService(
                    new com.google.api.services.drive.Drive.Builder(transport, jsonFactory, credential)
                            .setApplicationName("File Manager Android")
                            .build());

        }

        @Override
        protected void onPreExecute() {
            mSourceListener.onLoadStarted();
        }

        @Override
        protected TreeNode<SourceFile> doInBackground(String... paths) {
            try {
                File rootFile = GoogleDriveFactory
                        .getInstance()
                        .getService()
                        .files()
                        .get(paths[0])
                        .setFields("name,id,mimeType,parents,size,hasThumbnail,thumbnailLink,iconLink,modifiedTime")
                        .execute();
                SourceFile rootSourceFile = new GoogleDriveFile(rootFile);
                rootFileTreeNode = new TreeNode<>(rootSourceFile);
                currentLevelNode = rootFileTreeNode;
                setCurrentDirectory(rootFileTreeNode);
                setQuotaInfo(GoogleDriveFactory.getInstance().getStorageStats());

                return parseDirectory(rootFile);
            } catch (Exception e) {
                //TODO: Error handling
                return null;
            }
        }

        private TreeNode<SourceFile> parseDirectory(File currentDirectory) throws IOException {
            FileList fileList = GoogleDriveFactory
                    .getInstance()
                    .getService()
                    .files()
                    .list()
                    .setQ(String.format("'%s' in parents", currentDirectory.getId()))
                    .setFields("files(name,id,mimeType,parents,size,hasThumbnail,thumbnailLink,iconLink,modifiedTime)")
                    .execute();
            List<File> files = fileList.getFiles();
            long dirSize = 0L;
            if (files != null) {
                for (File file : files) {
                    SourceFile sourceFile = new GoogleDriveFile(file);
                    if (sourceFile.isDirectory()) {
                        currentLevelNode.addChild(sourceFile);
                        currentLevelNode = currentLevelNode.getChildren().get(currentLevelNode.getChildren().size() - 1);
                        parseDirectory(file);
                        currentLevelNode.getParent().getData().addSize(currentLevelNode.getData().getSize());
                        currentLevelNode = currentLevelNode.getParent();
                    } else {
                        if (file.getSize() != null) {
                            dirSize += file.getSize();
                        }
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

    /**
     * Check that Google Play services APK is installed and up to date.
     * @return True if Google Play Services is available and up to date on this device, false otherwise.
     */
    public boolean isGooglePlayServicesAvailable(Context context) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(context);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    public void acquireGooglePlayServices(Activity activity) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(activity);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(activity, connectionStatusCode);
        }
    }

    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     */
    public static void showGooglePlayServicesAvailabilityErrorDialog(Activity activity, final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                activity,
                connectionStatusCode,
                Constants.RequestCodes.GOOGLE_PLAY_SERVICES);
        dialog.show();
    }
}
