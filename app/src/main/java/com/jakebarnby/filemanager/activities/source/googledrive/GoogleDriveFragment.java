package com.jakebarnby.filemanager.activities.source.googledrive;

import android.Manifest;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.jakebarnby.filemanager.R;
import com.jakebarnby.filemanager.activities.source.SourceFragment;
import com.jakebarnby.filemanager.managers.GoogleDriveFactory;
import com.jakebarnby.filemanager.models.files.GoogleDriveFile;
import com.jakebarnby.filemanager.models.files.SourceFile;
import com.jakebarnby.filemanager.util.Constants;
import com.jakebarnby.filemanager.util.TreeNode;
import com.jakebarnby.filemanager.util.Utils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.jakebarnby.filemanager.util.Constants.RequestCodes.ACCOUNT_PICKER;
import static com.jakebarnby.filemanager.util.Constants.RequestCodes.GOOGLE_PLAY_SERVICES;
import static com.jakebarnby.filemanager.util.Constants.RequestCodes.GOOGLE_SIGN_IN;

/**
 * Created by Jake on 5/31/2017.
 */

public class GoogleDriveFragment extends SourceFragment {

    private static final String[] SCOPES = {DriveScopes.DRIVE};
    private GoogleAccountCredential mCredential;

    /**
     * Return a new instance of this Fragment
     * @param sourceName The name of the source controlled by this fragment
     * @return A new instance of this fragment
     */
    public static SourceFragment newInstance(String sourceName) {
        SourceFragment fragment = new GoogleDriveFragment();
        Bundle args = new Bundle();
        args.putString("TITLE", sourceName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void authenticateSource() {
        checkPermissions();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCredential = GoogleAccountCredential.usingOAuth2(
                getActivity().getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

    }

    @Override
    public void onResume() {
        if (!isLoggedIn()) {
            authGoogleSilent();
        }
        super.onResume();
    }

    /**
     * Create a google credential and try to call the API with it, show a login dialog if it fails
     */
    private void authGoogle() {
        getResultsFromApi();
    }

    /**
     * Create a google credential and try to call the API with it, do nothing if it fails
     */
    private void authGoogleSilent() {
        String accountName = getActivity().getPreferences(Context.MODE_PRIVATE)
                .getString(Constants.SharedPrefs.GOOGLE_ACCOUNT_NAME, null);
        if (accountName != null) {
            mCredential.setSelectedAccountName(accountName);
            getResultsFromApi();
            setLoggedIn(true);
        }
    }

    /**
     * Attempt to call the API, after verifying that all the preconditions are satisfied
     */
    private void getResultsFromApi() {
        if (!isLoggedIn()) {
            if (!Utils.isGooglePlayServicesAvailable(getContext())) {
                Utils.acquireGooglePlayServices(getActivity());
            } else if (mCredential != null && mCredential.getSelectedAccountName() == null) {
                startActivityForResult(mCredential.newChooseAccountIntent(), ACCOUNT_PICKER);
            } else if (!Utils.isConnectionReady(getContext())) {
                //TODO: No internet
            } else {
                loadSource();
            }
        }
    }

    @Override
    protected void loadSource() {
        if (!isFilesLoaded()) {
            new GoogleDriveFileSystemLoader(mCredential).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    protected void openFile(SourceFile file) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case GOOGLE_PLAY_SERVICES:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
            case GOOGLE_SIGN_IN:
                if (resultCode == RESULT_OK) {
                    SharedPreferences prefs = getActivity().getPreferences(Context.MODE_PRIVATE);
                    try {
                        prefs.edit().putString(Constants.SharedPrefs.GOOGLE_ACCESS_TOKEN, mCredential.getToken()).apply();
                    } catch (IOException | GoogleAuthException e) {
                        e.printStackTrace();
                    }
                    getResultsFromApi();
                }
                break;
            case ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences prefs = getActivity().getPreferences(Context.MODE_PRIVATE);
                        prefs.edit().putString(Constants.SharedPrefs.GOOGLE_ACCOUNT_NAME, accountName).apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
        }
    }

    /**
     * An asynchronous task that handles the Drive API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class GoogleDriveFileSystemLoader extends AsyncTask<Void, Void, TreeNode<SourceFile>> {
        private Exception mLastError = null;
        private TreeNode<SourceFile> rootFileTreeNode;
        private TreeNode<SourceFile> currentLevelNode;

        GoogleDriveFileSystemLoader(GoogleAccountCredential credential) {
            HttpTransport transport = new NetHttpTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            GoogleDriveFactory.Instance().setService(
                    new com.google.api.services.drive.Drive.Builder(transport, jsonFactory, credential)
                            .setApplicationName("File Manager Android")
                            .build());

        }

        @Override
        protected void onPreExecute() {
            mConnectButton.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected TreeNode<SourceFile> doInBackground(Void... params) {
            try {
                File rootFile = GoogleDriveFactory
                        .Instance()
                        .getService()
                        .files()
                        .get("root")
                        .execute();
                SourceFile rootSourceFile = new GoogleDriveFile();
                ((GoogleDriveFile) rootSourceFile).setFileProperties(rootFile);

                rootFileTreeNode = new TreeNode<>(rootSourceFile);
                currentLevelNode = rootFileTreeNode;
                setCurrentDirectory(rootFileTreeNode);

                return parseDirectory(rootFile);
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         *
         * @param currentDirectory
         * @return
         * @throws IOException
         */
        private TreeNode<SourceFile> parseDirectory(File currentDirectory) throws IOException {
            FileList fileList = GoogleDriveFactory
                    .Instance()
                    .getService()
                    .files()
                    .list()
                    .setQ(String.format("'%s' in parents", currentDirectory.getId()))
                    .setFields("files(name,id,mimeType,parents)")
                    .execute();
            List<File> files = fileList.getFiles();
            if (files != null) {
                for (File file : files) {
                    SourceFile sourceFile = new GoogleDriveFile();
                    ((GoogleDriveFile)sourceFile).setFileProperties(file);
                    if (sourceFile.isDirectory()) {
                        currentLevelNode.addChild(sourceFile);
                        currentLevelNode = currentLevelNode.getChildren().get(currentLevelNode.getChildren().size() - 1);
                        parseDirectory(file);
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
            setFileTreeRoot(fileTree);
            initializeSourceRecyclerView(fileTree, createOnClickListener(), createOnLongClickListener());
            setFilesLoaded(true);
            mProgressBar.setVisibility(View.GONE);
        }

        @Override
        protected void onCancelled() {
            mProgressBar.setVisibility(View.GONE);
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    Utils.showGooglePlayServicesAvailabilityErrorDialog(
                            getActivity(), ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            Constants.RequestCodes.GOOGLE_SIGN_IN);
                } else {
                    Log.e("ERROR", mLastError.getLocalizedMessage());
                }
            } else {
            }
        }
    }

    /**
     * Show a dialog explaining why local storage permission is necessary
     */
    protected void showPermissionRationaleDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).setMessage(R.string.dialog_contact_permission);
        builder.setPositiveButton("OK", (dialog, which) -> requestPermissions(
                new String[]{Manifest.permission.GET_ACCOUNTS},
                Constants.RequestCodes.ACCOUNTS_PERMISSIONS));

        final AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Check if the user has granted local storage permission and request them if not
     */
    protected void checkPermissions() {
        int permissionCheck = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.GET_ACCOUNTS);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.GET_ACCOUNTS)) {
                showPermissionRationaleDialog();
            } else {
                requestPermissions(
                        new String[]{Manifest.permission.GET_ACCOUNTS},
                        Constants.RequestCodes.ACCOUNTS_PERMISSIONS);
            }
        } else {
            authGoogle();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constants.RequestCodes.ACCOUNTS_PERMISSIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    authGoogle();
                } else if (!shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
//                    Snackbar.make(mRecycler, R.string.snackbar_permissions, Snackbar.LENGTH_LONG)
//                            .setAction(R.string.action_settings, v -> String b = "";)
//                            .show();
                }
                break;
            }
        }
    }
}
