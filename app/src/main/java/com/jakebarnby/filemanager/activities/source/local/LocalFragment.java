package com.jakebarnby.filemanager.activities.source.local;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.jakebarnby.filemanager.R;
import com.jakebarnby.filemanager.activities.source.SourceFragment;
import com.jakebarnby.filemanager.models.LocalFile;
import com.jakebarnby.filemanager.models.SourceFile;
import com.jakebarnby.filemanager.util.Constants;
import com.jakebarnby.filemanager.util.TreeNode;

import java.io.File;

/**
 * Created by Jake on 5/31/2017.
 */

public class LocalFragment extends SourceFragment {

    /**
     * Return a new instance of this Fragment
     *
     * @param sourceName The name of the source controlled by this fragment
     * @return A new instance of this fragment
     */
    public static SourceFragment newInstance(String sourceName) {
        SourceFragment fragment = new LocalFragment();
        Bundle args = new Bundle();
        args.putString("TITLE", sourceName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        checkPermissions();
    }

    @Override
    protected void authenticateSource() {
        checkPermissions();
    }

    @Override
    protected void loadSource() {
        if (!isFilesLoaded()) {
            new LocalFileSystemLoader().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Environment.getExternalStorageDirectory());
        }
    }

    /**
     * Loads a file tree from the local file system
     */
    private class LocalFileSystemLoader extends AsyncTask<File, Void, TreeNode<SourceFile>> {
        private TreeNode<SourceFile> rootFileTreeNode;
        private TreeNode<SourceFile> currentLevelNode;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mConnectButton.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected TreeNode<SourceFile> doInBackground(File... files) {
            File rootFile = files[0];
            SourceFile rootSourceFile = new LocalFile();
            ((LocalFile) rootSourceFile).setFileProperties(rootFile);

            rootFileTreeNode = new TreeNode<>(rootSourceFile);
            currentLevelNode = rootFileTreeNode;
            return parseFileSystem(files[0]);
        }

        /**
         * Recursively build a tree representing the files and folders of this device
         *
         * @param currentDir    The directory to start the search at
         * @return              The root node of the tree
         */
        private TreeNode<SourceFile> parseFileSystem(File currentDir) {
            File listFile[] = currentDir.listFiles();
            if (listFile != null && listFile.length > 0) {
                for (File file : listFile) {
                    SourceFile sourceFile = new LocalFile();
                    ((LocalFile) sourceFile).setFileProperties(file);
                    if (file.isDirectory()) {
                        currentLevelNode.addChild(sourceFile);
                        currentLevelNode = currentLevelNode.getChildren().get(currentLevelNode.getChildren().size() - 1);
                        parseFileSystem(file);
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
        }
    }


    /**
     * Show a dialog explaining why local storage permission is necessary
     */
    protected void showPermissionRationaleDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).setMessage(R.string.dialog_message);
        builder.setPositiveButton("OK", (dialog, which) -> requestPermissions(
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                Constants.RequestCodes.STORAGE_PERMISSIONS));

        final AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Check if the user has granted local storage permission and request them if not
     */
    protected void checkPermissions() {
        int permissionCheck = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                showPermissionRationaleDialog();
            } else {
                requestPermissions(
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        Constants.RequestCodes.STORAGE_PERMISSIONS);
            }
        } else {
            loadSource();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constants.RequestCodes.STORAGE_PERMISSIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadSource();
                    setLoggedIn(true);
                } else if (!shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Snackbar.make(mRecycler, R.string.snackbar_permissions, Snackbar.LENGTH_LONG)
                            .setAction(R.string.action_settings, v -> showAppDetails())
                            .show();
                }
                break;
            }
        }
    }

    /**
     * Opens the app details page for this app
     */
    private void showAppDetails() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", "com.jakebarnby.filemanager", null);
        intent.setData(uri);
        startActivity(intent);
    }
}
