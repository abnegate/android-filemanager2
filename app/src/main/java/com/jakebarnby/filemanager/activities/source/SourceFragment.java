package com.jakebarnby.filemanager.activities.source;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
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
import com.jakebarnby.filemanager.models.Source;
import com.jakebarnby.filemanager.models.files.SourceFile;
import com.jakebarnby.filemanager.services.SourceTransferService;
import com.jakebarnby.filemanager.util.Constants;
import com.jakebarnby.filemanager.util.TreeNode;
import com.jakebarnby.filemanager.util.Utils;

/**
 * A placeholder fragment containing a simple view.
 */
public abstract class SourceFragment extends Fragment {

    private Source                  mSource;

    protected RecyclerView          mRecycler;
    protected FileSystemListAdapter mFileSystemListAdapter;
    protected FileSystemGridAdapter mFileSystemGridAdapter;
    protected ProgressBar           mProgressBar;
    protected Button                mConnectButton;
    protected ImageView             mSourceLogo;
    protected View                  mDivider;

    private LinearLayout            mBreadcrumbBar;
    private HorizontalScrollView    mBreadcrumbWrapper;

    /**
     * Authenticate the current mSource
     */
    protected abstract void authenticateSource();

    /**
     * Load the current mSource
     */
    protected abstract void loadSource();

    public Source getSource() {
        return mSource;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_source, container, false);
        mBreadcrumbWrapper = rootView.findViewById(R.id.breadcrumb_wrapper);
        mBreadcrumbBar = rootView.findViewById(R.id.breadcrumbs);
        mRecycler = rootView.findViewById(R.id.recycler_local);
        mProgressBar = rootView.findViewById(R.id.animation_view);
        mDivider = rootView.findViewById(R.id.divider);
        mConnectButton = rootView.findViewById(R.id.btn_connect);
        mSourceLogo = rootView.findViewById(R.id.image_source_logo);
        mSource = new Source(getArguments().getString(Constants.FRAGMENT_TITLE));

        if (hasToken(mSource.getSourceName())) {
            mConnectButton.setVisibility(View.GONE);
            mSourceLogo.setVisibility(View.GONE);
        }
        mConnectButton.setOnClickListener(v -> {
            if (Utils.isConnectionReady(SourceFragment.this.getContext())) {
                authenticateSource();
            } else {
                Snackbar.make(rootView, R.string.err_no_connection, Snackbar.LENGTH_LONG).show();
            }
        });
        return rootView;
    }

    public void setMultiSelectEnabled(boolean enabled) {
        if (enabled) {
            ((SourceActivity)getActivity()).toggleFloatingMenu(true);
        }
        this.mSource.setMultiSelectEnabled(enabled);
        if (mRecycler.getAdapter() != null) {
            ((FileSystemAdapter) mRecycler.getAdapter()).setMultiSelectEnabled(enabled);
            mRecycler.getAdapter().notifyDataSetChanged();
        }
    }

    /**
     * Reload the {@link RecyclerView}
     */
    public void refreshRecycler() {
        mRecycler.getAdapter().notifyDataSetChanged();
    }

    /**
     * Checks if this mSource has a valid access token
     * @return  Whether there is a valid access token for this mSource
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
     * Initialize the UI for this mSource
     * @param file                  The root file of the mSource
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
            if (mSource.isMultiSelectEnabled()) {
                if (isChecked) {
                    SelectedFilesManager
                            .getInstance()
                            .getSelectedFiles(SelectedFilesManager.getInstance().getOperationCount())
                            .add(file);
                } else {
                    SelectedFilesManager
                            .getInstance()
                            .getSelectedFiles(SelectedFilesManager.getInstance().getOperationCount())
                            .remove(file);
                }

                int size = SelectedFilesManager
                        .getInstance()
                        .getSelectedFiles(SelectedFilesManager.getInstance().getOperationCount())
                        .size();
                getActivity().setTitle(size + " selected");
                //TODO: Set the Fragment tab title with selected count, e.g. LOCAL (3) DROPBOX (1)
            } else {
                if (file.getData().isDirectory()) {
                    ((FileSystemAdapter) mRecycler.getAdapter()).setCurrentDirectory(file);
                    mSource.setCurrentDirectory(file);
                    ((SourceActivity)getActivity()).getSourceManager().setActiveDirectory(file);
                    pushBreadcrumb(file);
                    mRecycler.getAdapter().notifyDataSetChanged();
                } else {
//                    if (!((SourceActivity) getActivity()).startFreeSpaceCheck() &&
//                            !getSourceName().equals(Constants.Sources.LOCAL)) {
//                        return;
//                    }

                    if (Utils.getFreeSpace(Environment.getExternalStorageDirectory()) > file.getData().getSize()) {
                        SelectedFilesManager.getInstance().addNewSelection();
                        ((SourceActivity)getActivity()).getSourceManager().addFileAction(
                                SelectedFilesManager.getInstance().getOperationCount()-1,
                                SourceActivity.FileAction.OPEN);
                        SourceTransferService.startActionOpen(getContext(), file.getData());
                    } else {
                        Snackbar.make(mRecycler,
                                String.format(getString(R.string.err_no_free_space), file.getData().getSourceName()),
                                Snackbar.LENGTH_LONG).show();
                    }
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
            if (!mSource.isMultiSelectEnabled()) {
                setMultiSelectEnabled(true);

                if (SelectedFilesManager.getInstance().getOperationCount() == 0) {
                    SelectedFilesManager.getInstance().addNewSelection();
                }

                SelectedFilesManager
                        .getInstance()
                        .getSelectedFiles(SelectedFilesManager.getInstance().getOperationCount())
                        .add(file);

                int size = SelectedFilesManager
                        .getInstance()
                        .getSelectedFiles(SelectedFilesManager.getInstance().getOperationCount())
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
            if (mSource.getCurrentDirectory().getData().getName().equals(name)) return;
            TreeNode<SourceFile> selectedParent = TreeNode.findParent(mSource.getCurrentDirectory(), name);

            ((SourceActivity)getActivity()).getSourceManager().setActiveDirectory(selectedParent);
            ((FileSystemAdapter)mRecycler.getAdapter()).setCurrentDirectory(selectedParent);
            mSource.setCurrentDirectory(selectedParent);
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

    /**
     * If performing an action on a non-local directory, check internet and
     */
    protected boolean checkConnectionStatus() {
        if (!mSource.getSourceName().equals(Constants.Sources.LOCAL)) {
            if (!Utils.isConnectionReady(getContext())) {
                Snackbar.make(mRecycler, R.string.err_no_connection, Snackbar.LENGTH_LONG).show();
                return false;
            }
        }
        return true;
    }
}
