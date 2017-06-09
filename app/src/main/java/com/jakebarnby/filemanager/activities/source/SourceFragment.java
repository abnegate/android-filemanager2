package com.jakebarnby.filemanager.activities.source;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.airbnb.lottie.LottieAnimationView;
import com.jakebarnby.filemanager.R;
import com.jakebarnby.filemanager.activities.source.adapters.FileSystemAdapter;
import com.jakebarnby.filemanager.activities.source.adapters.FileSystemGridAdapter;
import com.jakebarnby.filemanager.activities.source.adapters.FileSystemListAdapter;
import com.jakebarnby.filemanager.managers.SelectedFilesManager;
import com.jakebarnby.filemanager.models.AccessToken;
import com.jakebarnby.filemanager.models.SourceFile;
import com.jakebarnby.filemanager.util.Constants;
import com.jakebarnby.filemanager.util.TreeNode;

/**
 * A placeholder fragment containing a simple view.
 */
public abstract class SourceFragment extends Fragment {

    private String                  mSourceName;
    private TreeNode<SourceFile>    mRootFileTreeNode;
    private AccessToken             mToken;
    private boolean                 mLoggedIn;
    private boolean                 mFilesLoaded;
    private boolean                 mMultiSelectEnabled;
    protected RecyclerView          mRecycler;
    protected FileSystemListAdapter mFileSystemListAdapter;
    protected FileSystemGridAdapter mFileSystemGridAdapter;

    protected LottieAnimationView mProgressBar;
    protected Button                mConnectButton;

    /**
     * Authenticate the current source
     */
    protected abstract void authenticateSource();

    /**
     * Load the current source
     */
    protected abstract void loadSource();

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

    public AccessToken getToken() {
        return mToken;
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
            newAdapter.setParentDir(curAdapter.getParentDir());
            newAdapter.setCurrentDirChildren(curAdapter.getCurrentDirChildren());
        }
        mRecycler.setAdapter(newAdapter);
        mRecycler.getAdapter().notifyDataSetChanged();
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

        mFileSystemListAdapter = new FileSystemListAdapter(file, file.getChildren());
        mFileSystemListAdapter.setOnClickListener(onClickListener);
        mFileSystemListAdapter.setOnLongClickListener(onLongClickListener);

        mFileSystemGridAdapter = new FileSystemGridAdapter(file, file.getChildren());
        mFileSystemGridAdapter.setOnClickListener(onClickListener);
        mFileSystemGridAdapter.setOnLongClickListener(onLongClickListener);

        setRecyclerLayout();
    }

    /**
     * Construct a click listener for a file or folder
     * @return  The constructed listener
     */
    protected FileSystemAdapter.OnFileClickedListener createOnClickListener() {
        return file -> {
            if (mMultiSelectEnabled) {
                //TODO: Tihs should still open the dir, the checkbox should set selected
                SelectedFilesManager.getInstance().getSelectedFiles().add(file.getData());
            } else {
                if (file.getData().isDirectory() && file.getData().canRead()) {
                    ((FileSystemAdapter) mRecycler.getAdapter()).setCurrentDirectory(file.getParent(), file.getChildren());
                    mRecycler.getAdapter().notifyDataSetChanged();
                } else {
                    //TODO: Open file
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
            //TODO: Open context menu for multi-select
            if (!mMultiSelectEnabled) {
                SelectedFilesManager.getInstance().getSelectedFiles().add(file.getData());
            }
        };
    }
}
