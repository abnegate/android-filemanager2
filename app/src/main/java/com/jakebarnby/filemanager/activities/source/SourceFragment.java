package com.jakebarnby.filemanager.activities.source;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.util.ViewPreloadSizeProvider;
import com.jakebarnby.filemanager.R;
import com.jakebarnby.filemanager.activities.source.adapters.FileSystemAdapter;
import com.jakebarnby.filemanager.activities.source.adapters.FileSystemGridAdapter;
import com.jakebarnby.filemanager.activities.source.adapters.FileSystemListAdapter;
import com.jakebarnby.filemanager.managers.SelectedFilesManager;
import com.jakebarnby.filemanager.models.files.SourceFile;
import com.jakebarnby.filemanager.services.SourceTransferService;
import com.jakebarnby.filemanager.util.Constants;
import com.jakebarnby.filemanager.util.TreeNode;

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
    private boolean                 mIsReload;

    protected RecyclerView          mRecycler;
    protected FileSystemListAdapter mFileSystemListAdapter;
    protected FileSystemGridAdapter mFileSystemGridAdapter;
    protected ProgressBar           mProgressBar;
    protected Button                mConnectButton;
    protected View                  mDivider;

    private LinearLayout            mBreadcrumbBar;
    private HorizontalScrollView    mBreadcrumbWrapper;

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
        mBreadcrumbWrapper = rootView.findViewById(R.id.breadcrumb_wrapper);
        mBreadcrumbBar = rootView.findViewById(R.id.breadcrumbs);
        mRecycler = rootView.findViewById(R.id.recycler_local);
        mProgressBar = rootView.findViewById(R.id.animation_view);
        mDivider = rootView.findViewById(R.id.divider);
        mConnectButton = rootView.findViewById(R.id.btn_connect);
        if (hasToken(getSourceName())) {
            mConnectButton.setVisibility(View.GONE);
        }
        mConnectButton.setOnClickListener(v -> {
            authenticateSource();
        });
        return rootView;
    }

    public String getSourceName() {
        return mSourceName;
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

    public void setMultiSelectEnabled(boolean enabled) {
        if (enabled) {
            ((SourceActivity)getActivity()).toggleFloatingMenu(true);
        }
        this.mMultiSelectEnabled = enabled;
        if (mRecycler.getAdapter() != null) {
            ((FileSystemAdapter) mRecycler.getAdapter()).setMultiSelectEnabled(enabled);
            mRecycler.getAdapter().notifyDataSetChanged();
        }
    }

    public TreeNode<SourceFile>  getCurrentDirectory() {
        return mCurrentDirectory;
    }

    public void setCurrentDirectory(TreeNode<SourceFile>  mCurrentDirectory) {
        this.mCurrentDirectory = mCurrentDirectory;
    }

    public boolean isReload() {
        return mIsReload;
    }

    public void setReload(boolean reload) {
        mIsReload = reload;
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
    public void initRecyclerView() {
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

        mRecycler.setAdapter(newAdapter);
        if (mRecycler.getAdapter() != null) {
            mRecycler.getAdapter().notifyDataSetChanged();
        }
        mDivider.setVisibility(View.VISIBLE);
    }

    /**
     *
     * @param oldAdapterDir
     * @param newAdapterDir
     * @return
     */
    protected void transformCurrentDirectory(TreeNode<SourceFile> oldAdapterDir, TreeNode<SourceFile> newAdapterDir) {
        newAdapterDir.setParent(oldAdapterDir.getParent());
        oldAdapterDir.replaceNode(newAdapterDir);
        setCurrentDirectory(newAdapterDir);
        ((FileSystemAdapter)mRecycler.getAdapter()).setCurrentDirectory(newAdapterDir);
        ((SourceActivity)getActivity()).setActiveDirectory(newAdapterDir);
        mRecycler.getAdapter().notifyDataSetChanged();
    }

    /**
     * Initialize the UI for this source
     * @param file                  The root file of the source
     * @param onClickListener       The click listener for files and folders
     * @param onLongClickListener   The long click listener for files and folders
     */
    protected void initAdapters(TreeNode<SourceFile> file,
                                FileSystemAdapter.OnFileClickedListener onClickListener,
                                FileSystemAdapter.OnFileLongClickedListener onLongClickListener) {
        mFileSystemListAdapter = new FileSystemListAdapter(file,
                                                           Glide.with(this).asDrawable(),
                                                           new ViewPreloadSizeProvider());
        mFileSystemListAdapter.setOnClickListener(onClickListener);
        mFileSystemListAdapter.setOnLongClickListener(onLongClickListener);

        mFileSystemGridAdapter = new FileSystemGridAdapter(file,
                                                           Glide.with(this).asDrawable(),
                                                           new ViewPreloadSizeProvider());
        mFileSystemGridAdapter.setOnClickListener(onClickListener);
        mFileSystemGridAdapter.setOnLongClickListener(onLongClickListener);

        initRecyclerView();
    }

    /**
     * Opens the app details page for this app
     */
    protected void showAppDetails() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", "com.jakebarnby.filemanager", null);
        intent.setData(uri);
        startActivity(intent);
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
                            .add(file);
                } else {
                    SelectedFilesManager
                            .getInstance()
                            .getSelectedFiles()
                            .remove(file);
                }

                int size = SelectedFilesManager
                        .getInstance()
                        .getSelectedFiles()
                        .size();
                getActivity().setTitle(size + " selected");
                //TODO: Set the Fragment tab title with selected count, e.g. LOCAL (3) DROPBOX (1)
            } else {
                if (file.getData().isDirectory()) {
                    ((FileSystemAdapter) mRecycler.getAdapter()).setCurrentDirectory(file);
                    setCurrentDirectory(file);
                    ((SourceActivity)getActivity()).setActiveDirectory(file);
                    pushBreadcrumb(file);
                    mRecycler.getAdapter().notifyDataSetChanged();
                } else {
                    SourceTransferService.startActionOpen(getContext(), file.getData());
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
                setMultiSelectEnabled(true);

                SelectedFilesManager
                        .getInstance()
                        .getSelectedFiles()
                        .add(file);

                int size = SelectedFilesManager
                        .getInstance()
                        .getSelectedFiles()
                        .size();
                getActivity().setTitle(size + " selected");
            }
        };
    }

    /**
     * Push a breadcrumb onto the view stack
     * @param directory The directory to push
     */
    protected void pushBreadcrumb(TreeNode<SourceFile> directory) {
        final ViewGroup crumbLayout = (ViewGroup) getActivity().getLayoutInflater()
                .inflate(R.layout.view_breadcrumb, null);

        crumbLayout.findViewById(R.id.crumb_arrow)
                .setVisibility(directory.getParent() == null ? View.GONE : View.VISIBLE);

        TextView text = crumbLayout.findViewById(R.id.crumb_text);
        text.setText(directory.getParent() == null ?
                directory.getData().getSourceName().substring(0,1).toUpperCase() + directory.getData().getSourceName().substring(1).toLowerCase():
                directory.getData().getName());

        crumbLayout.setOnClickListener(v -> {
            TextView crumbText =  v.findViewById(R.id.crumb_text);

            int diff = (mBreadcrumbBar.getChildCount()-1) - mBreadcrumbBar.indexOfChild(v);
            for (int i = 0; i < diff; i++) popBreadcrumb();

            String name = crumbText.getText().toString();
            if (getCurrentDirectory().getData().getName().equals(name)) return;
            TreeNode<SourceFile> selectedParent = TreeNode.findParent(getCurrentDirectory(), name);

            ((SourceActivity)getActivity()).setActiveDirectory(selectedParent);
            ((FileSystemAdapter)mRecycler.getAdapter()).setCurrentDirectory(selectedParent);
            setCurrentDirectory(selectedParent);
            mRecycler.getAdapter().notifyDataSetChanged();
        });
        mBreadcrumbWrapper.postDelayed(() ->
                mBreadcrumbWrapper.fullScroll(HorizontalScrollView.FOCUS_RIGHT),
                50L);
        mBreadcrumbBar.addView(crumbLayout);
        crumbLayout.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.breadcrumb_overshoot_z));
    }

    /**
     * Pop the latest breakcrumb added to the stack
     */
    protected void popBreadcrumb() {
        mBreadcrumbBar.removeViewAt(mBreadcrumbBar.getChildCount()-1);
    }
}
