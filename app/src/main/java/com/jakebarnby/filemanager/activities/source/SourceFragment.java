package com.jakebarnby.filemanager.activities.source;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;

import com.airbnb.lottie.LottieAnimationView;
import com.jakebarnby.filemanager.R;
import com.jakebarnby.filemanager.activities.source.adapters.FileSystemAdapter;
import com.jakebarnby.filemanager.activities.source.adapters.FileSystemGridAdapter;
import com.jakebarnby.filemanager.activities.source.adapters.FileSystemListAdapter;
import com.jakebarnby.filemanager.managers.SelectedFilesManager;
import com.jakebarnby.filemanager.models.files.SourceFile;
import com.jakebarnby.filemanager.util.Constants;
import com.jakebarnby.filemanager.util.TreeNode;

import java.io.File;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public abstract class SourceFragment extends Fragment {

    private String                  mSourceName;
    private TreeNode<SourceFile>    mRootFileTreeNode;
    private TreeNode<SourceFile>    mCurrentDirectory;
    private boolean                 mLoggedIn;
    private boolean                 mFilesLoaded;
    private boolean                 mMultiSelectEnabled;

    protected RecyclerView          mRecycler;
    protected FileSystemListAdapter mFileSystemListAdapter;
    protected FileSystemGridAdapter mFileSystemGridAdapter;
    protected LottieAnimationView   mProgressBar;
    protected Button                mConnectButton;


    /**
     * Authenticate the current source
     */
    protected abstract void authenticateSource();

    /**
     * Load the current source
     */
    protected abstract void loadSource();

    /**
     * Attempts to open a {@link SourceFile} by finding it's mimetype then opening a compatible application
     * @param file The file to attempt to open
     */
    protected abstract void openFile(SourceFile file);


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_source, container, false);
        mRecycler = (RecyclerView) rootView.findViewById(R.id.recycler_local);
        mProgressBar = (LottieAnimationView) rootView.findViewById(R.id.animation_view);
        mConnectButton = (Button) rootView.findViewById(R.id.btn_connect);
        if (hasToken(getSourceName())) {
            mConnectButton.setVisibility(View.GONE);
        }
        mConnectButton.setOnClickListener(v -> {
            authenticateSource();
        });
        //FIXME: Elevation not being set on API 21, connect button and progress bar cant be seen
        ViewCompat.setElevation(mRecycler, Constants.PROGRESSBAR_ELEVATION-1);
        ViewCompat.setElevation(mProgressBar, Constants.PROGRESSBAR_ELEVATION);
        ViewCompat.setElevation(mConnectButton, Constants.PROGRESSBAR_ELEVATION);
        return rootView;
    }

    public String getSourceName() {
        return mSourceName;
    }

    public TreeNode<SourceFile> getFileTreeRoot() {
        return mRootFileTreeNode;
    }

    public void setFileTreeRoot(TreeNode<SourceFile> mFileTree) {
        this.mRootFileTreeNode = mFileTree;
    }

    public boolean isLoggedIn() {
        return mLoggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.mLoggedIn = loggedIn;
    }

    public boolean isFilesLoaded() {
        return mFilesLoaded;
    }

    public void setFilesLoaded(boolean mFilesLoaded) {
        this.mFilesLoaded = mFilesLoaded;
    }

    public boolean isMultiSelectEnabled() {
        return mMultiSelectEnabled;
    }

    public void setMultiSelectEnabled(boolean mMultiSelectEnabled) {
        this.mMultiSelectEnabled = mMultiSelectEnabled;

        if (mRecycler.getAdapter() != null) {
            ((FileSystemAdapter) mRecycler.getAdapter()).setMultiSelectEnabled(mMultiSelectEnabled);
            mRecycler.getAdapter().notifyDataSetChanged();
        }
    }

    public TreeNode<SourceFile>  getCurrentDirectory() {
        return mCurrentDirectory;
    }

    public void setCurrentDirectory(TreeNode<SourceFile>  mCurrentDirectory) {
        this.mCurrentDirectory = mCurrentDirectory;
    }

    /**
     * Checks if this source has a valid access token
     * @return  Whether there is a valid access token for this source
     */
    protected boolean hasToken(String sourceName) {
        SharedPreferences prefs = getActivity().getPreferences(Context.MODE_PRIVATE);
        String accessToken = prefs.getString(sourceName + "-access-token", null);
        return accessToken != null;
    }

    /**
     * Set the {@link RecyclerView} layout and adapter based on users preferences
     */
    protected void setRecyclerLayout() {
        FileSystemAdapter curAdapter = (FileSystemAdapter) mRecycler.getAdapter();
        FileSystemAdapter newAdapter;

        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        String viewType = sharedPref.getString("ViewAs", "List");
        if (viewType.equals("List")) {
            mRecycler.setLayoutManager(new LinearLayoutManager(getContext(),
                    LinearLayoutManager.VERTICAL,
                    false));
            newAdapter = mFileSystemListAdapter;
        } else {
            mRecycler.setLayoutManager(new GridLayoutManager(getContext(), 4));
            newAdapter = mFileSystemGridAdapter;
        }

        if (curAdapter != null) {
            TreeNode<SourceFile> transformedCurrentDir =
                    transformCurrentDirectory(curAdapter.getCurrentDir(), newAdapter.getRootTreeNode());
            if (transformedCurrentDir != null) {
                newAdapter.setCurrentDirectory(transformedCurrentDir);
            }
        }
        mRecycler.setAdapter(newAdapter);
        if (mRecycler.getAdapter() != null) {
            mRecycler.getAdapter().notifyDataSetChanged();
        }
    }

    /**
     * Attempt the find the current directory from the old adapter to set it to the new one
     * @param oldAdapterDir     The directory to search for
     * @param currentDir        The current directory being searched
     * @return                  The matching directory or null if no match was found
     */
    private TreeNode<SourceFile> transformCurrentDirectory(TreeNode<SourceFile> oldAdapterDir,
                                                           TreeNode<SourceFile> currentDir) {
        for(TreeNode<SourceFile> child : currentDir.getChildren()) {
            if (child.getChildren() != null && child.getChildren().size() > 0) {
                if (child.getData().getName().equals(oldAdapterDir.getData().getName())) {
                    return child;
                } else {
                    transformCurrentDirectory(oldAdapterDir, child);
                }
            }
        }
        return null;
    }

    /**
     * Initialize the UI for this source
     * @param file                  The root file of the source
     * @param onClickListener       The click listener for files and folders
     * @param onLongClickListener   The long click listener for files and folders
     */
    protected void initializeSourceRecyclerView(TreeNode<SourceFile> file,
                                                FileSystemAdapter.OnFileClickedListener onClickListener,
                                                FileSystemAdapter.OnFileLongClickedListener onLongClickListener) {
        mFileSystemListAdapter = new FileSystemListAdapter(file);
        mFileSystemListAdapter.setOnClickListener(onClickListener);
        mFileSystemListAdapter.setOnLongClickListener(onLongClickListener);

        mFileSystemGridAdapter = new FileSystemGridAdapter(file);
        mFileSystemGridAdapter.setOnClickListener(onClickListener);
        mFileSystemGridAdapter.setOnLongClickListener(onLongClickListener);

        setRecyclerLayout();
    }

    /**
     *
     */
    protected void launchFilePicker() {
        // Launch intent to pick file for upload
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, Constants.RequestCodes.FILE_PICKER);
    }

    /**
     * Attempts to open a file by finding it's mimetype then opening a compatible application
     * @param file The file to attempt to open
     */
    protected void viewFileInExternalApp(File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String ext = file.getName().substring(file.getName().indexOf(".") + 1);
        String type = mime.getMimeTypeFromExtension(ext);
        intent.setDataAndType(Uri.fromFile(file), type);

        PackageManager manager = getActivity().getPackageManager();
        List<ResolveInfo> resolveInfo = manager.queryIntentActivities(intent, 0);
        if (resolveInfo.size() > 0) {
            startActivity(intent);
        }
    }

    /**
     * Construct a click listener for a file or folder
     * @return  The constructed listener
     */
    protected FileSystemAdapter.OnFileClickedListener createOnClickListener() {
        return (file, isChecked, position) -> {
            if (mMultiSelectEnabled) {
                if (isChecked) {
                    SelectedFilesManager
                            .getInstance()
                            .getSelectedFiles()
                            .add(file.getData());
                } else {
                    SelectedFilesManager
                            .getInstance()
                            .getSelectedFiles()
                            .remove(file.getData());
                }
                getActivity().setTitle(String.valueOf(SelectedFilesManager.getInstance().getSelectedFiles().size()) + " selected");
            } else {
                setCurrentDirectory(file);
                ((SourceActivity)getActivity()).setActiveDirectory(file);

                if (file.getData().isDirectory() && file.getData().canRead()) {
                    ((FileSystemAdapter) mRecycler.getAdapter()).setCurrentDirectory(file);
                    mRecycler.getAdapter().notifyDataSetChanged();
                } else {
                    openFile(file.getData());
                }
            }
        };
    }

    /**
     * Construct a long click listener for a file or folder
     * @return  The constructed listener
     */
    protected FileSystemAdapter.OnFileLongClickedListener createOnLongClickListener() {
        return file -> {
            if (!mMultiSelectEnabled) {
//                for (SourceFragment fragment: ((SourceActivity)getActivity()).getPagerAdapter().getFragments()) {
//                    fragment.setMultiSelectEnabled(true);
//                }
                setMultiSelectEnabled(true);
            }
        };
    }
}
