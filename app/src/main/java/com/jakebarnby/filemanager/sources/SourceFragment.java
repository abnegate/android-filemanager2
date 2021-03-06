package com.jakebarnby.filemanager.sources;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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

import com.jakebarnby.filemanager.R;
import com.jakebarnby.filemanager.managers.SelectedFilesManager;
import com.jakebarnby.filemanager.models.FileAction;
import com.jakebarnby.filemanager.services.SourceTransferService;
import com.jakebarnby.filemanager.sources.googledrive.GoogleDriveFragment;
import com.jakebarnby.filemanager.sources.googledrive.GoogleDriveSource;
import com.jakebarnby.filemanager.sources.local.LocalFragment;
import com.jakebarnby.filemanager.sources.models.Source;
import com.jakebarnby.filemanager.sources.models.SourceFile;
import com.jakebarnby.filemanager.sources.models.SourceManager;
import com.jakebarnby.filemanager.sources.onedrive.OneDriveFragment;
import com.jakebarnby.filemanager.sources.onedrive.OneDriveSource;
import com.jakebarnby.filemanager.ui.adapters.FileAdapter;
import com.jakebarnby.filemanager.ui.adapters.FileDetailedListAdapter;
import com.jakebarnby.filemanager.ui.adapters.FileGridAdapter;
import com.jakebarnby.filemanager.ui.adapters.FileListAdapter;
import com.jakebarnby.filemanager.util.ComparatorUtils;
import com.jakebarnby.filemanager.util.Constants;
import com.jakebarnby.filemanager.util.PreferenceUtils;
import com.jakebarnby.filemanager.util.TreeNode;
import com.jakebarnby.filemanager.util.Utils;

import java.util.Locale;
import java.util.Stack;

/**
 * A placeholder fragment containing a simple view.
 */
public abstract class SourceFragment extends Fragment implements SourceListener {

    protected Source                    mSource;

    protected RecyclerView              mRecycler;
    protected FileListAdapter           mFileListAdapter;
    protected FileGridAdapter           mFileGridAdapter;
    protected FileDetailedListAdapter   mFileDetailedAdapter;
    protected ProgressBar               mProgressBar;
    protected Button                    mConnectButton;
    protected ImageView                 mSourceLogo;
    protected View                      mDivider;

    private LinearLayout                mBreadcrumbBar;
    private HorizontalScrollView        mBreadcrumbWrapper;

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
        mDivider = rootView.findViewById(R.id.divider_sort);
        mConnectButton = rootView.findViewById(R.id.btn_connect);
        mSourceLogo = rootView.findViewById(R.id.img_source_logo);

        if (getSource().hasToken(getContext(), getSource().getSourceName())) {
            mConnectButton.setVisibility(View.GONE);
        }

        mConnectButton.setOnClickListener(v -> {
            if (getSource().checkConnectionActive(getContext())) {
                if (this instanceof OneDriveFragment) {
                    ((OneDriveSource)getSource()).authenticateSource(this);
                } else {
                    getSource().authenticateSource(getContext());
                }
            }
        });

        mSourceLogo.setImageResource(Utils.resolveLogoId(getSource().getSourceName()));
        return rootView;
    }

    @Override
    public void onLoadStarted() {
        mConnectButton.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoadAborted() {
        mConnectButton.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onLoadError(String errorMessage) {
        onLoadAborted();
        ((SourceActivity)getActivity()).showErrorDialog(String.format(
                "%s %s%s",
                getString(R.string.problem_encountered),
                getString(R.string.loading_source),
                ": "+errorMessage));
    }

    @Override
    public void onLoadComplete(TreeNode<SourceFile> fileTree) {
        if (isResumed()) {
            pushBreadcrumb(fileTree);
            initAdapters(fileTree, createOnClickListener(), createOnLongClickListener());

            SourceManager sourceManager = ((SourceActivity) getActivity()).getSourceManager();

            if (sourceManager.getActiveDirectory() == null) {
                sourceManager.setActiveDirectory(fileTree);
            }

            mProgressBar.setVisibility(View.GONE);
            mSourceLogo.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLogout() {
        mRecycler.setVisibility(View.GONE);
        mDivider.setVisibility(View.GONE);
        mBreadcrumbWrapper.setVisibility(View.GONE);
        mSourceLogo.setVisibility(View.VISIBLE);
        mConnectButton.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        popAllBreadCrumbs();
    }

    @Override
    public void onNoConnection() {
        onLoadAborted();
        Snackbar.make(mRecycler, R.string.err_no_connection, Snackbar.LENGTH_LONG).show();
    }

    public void setMultiSelectEnabled(boolean enabled) {
        if (enabled) {
            ((SourceActivity)getActivity()).toggleFloatingMenu(true);
        }
        this.mSource.setMultiSelectEnabled(enabled);
        if (mRecycler.getAdapter() != null) {
            ((FileAdapter) mRecycler.getAdapter()).setMultiSelectEnabled(enabled);
            mRecycler.getAdapter().notifyDataSetChanged();
        }
    }

    /**
     * Set the {@link RecyclerView} layout and adapter based on users preferences
     */
    public void initRecyclerView() {
        FileAdapter newAdapter;

        int viewType = PreferenceUtils.getInt(
                getContext(),
                Constants.Prefs.VIEW_TYPE_KEY,
                Constants.ViewTypes.LIST);

        TreeNode.sortTree(mSource.getRootNode(), ComparatorUtils.resolveComparator(getContext()));

        switch (viewType) {
            case Constants.ViewTypes.LIST:
                mRecycler.setLayoutManager(new LinearLayoutManager(getContext(),
                        LinearLayoutManager.VERTICAL,
                        false));
                newAdapter = mFileListAdapter;
                break;
            case Constants.ViewTypes.DETAILED_LIST:
                mRecycler.setLayoutManager(new LinearLayoutManager(getContext(),
                        LinearLayoutManager.VERTICAL,
                        false));
                newAdapter = mFileDetailedAdapter;
                break;
            case Constants.ViewTypes.GRID:
                mRecycler.setLayoutManager(new GridLayoutManager(getContext(), Constants.GRID_SIZE));
                newAdapter = mFileGridAdapter;
                break;
            default:
                mRecycler.setLayoutManager(new LinearLayoutManager(getContext(),
                        LinearLayoutManager.VERTICAL,
                        false));
                newAdapter = mFileListAdapter;
                break;
        }

        mRecycler.setAdapter(newAdapter);
        if (mRecycler.getAdapter() != null) {
            mRecycler.getAdapter().notifyDataSetChanged();
        }
        mRecycler.setVisibility(View.VISIBLE);
        mDivider.setVisibility(View.VISIBLE);
        mBreadcrumbWrapper.setVisibility(View.VISIBLE);
    }

    /**
     * Initialize the UI for this mSource
     * @param file                  The root file of the mSource
     * @param onClickListener       The click listener for files and folders
     * @param onLongClickListener   The long click listener for files and folders
     */
    protected void initAdapters(TreeNode<SourceFile> file,
                                FileAdapter.OnFileClickedListener onClickListener,
                                FileAdapter.OnFileLongClickedListener onLongClickListener) {
        mFileListAdapter = new FileListAdapter(file);
        mFileListAdapter.setOnClickListener(onClickListener);
        mFileListAdapter.setOnLongClickListener(onLongClickListener);

        mFileGridAdapter = new FileGridAdapter(file);
        mFileGridAdapter.setOnClickListener(onClickListener);
        mFileGridAdapter.setOnLongClickListener(onLongClickListener);

        mFileDetailedAdapter = new FileDetailedListAdapter(file);
        mFileDetailedAdapter.setOnClickListener(onClickListener);
        mFileDetailedAdapter.setOnLongClickListener(onLongClickListener);

        initRecyclerView();
    }

    /**
     * Reload the {@link RecyclerView}
     */
    public void refreshRecycler() {
        mRecycler.getAdapter().notifyDataSetChanged();
    }

    /**
     * Construct a click listener for a file or folder
     * @return  The constructed listener
     */
    protected FileAdapter.OnFileClickedListener createOnClickListener() {
        return (file, isChecked, position) -> {
            if (mSource.isMultiSelectEnabled()) {
                if (isChecked) {
                    SelectedFilesManager
                            .getInstance()
                            .getCurrentSelectedFiles()
                            .add(file);
                } else {
                    SelectedFilesManager
                            .getInstance()
                            .getCurrentSelectedFiles()
                            .remove(file);
                }

                int size = SelectedFilesManager
                        .getInstance()
                        .getCurrentSelectedFiles()
                        .size();

                getActivity().setTitle(String.format(
                        Locale.getDefault(),
                        getString(R.string.selected_title),
                        size));
            } else {
                if (file.getData().isDirectory()) {
                    getSource().getCurrentDirectory().getData().
                            setPositionToRestore(((LinearLayoutManager)mRecycler.getLayoutManager()).findFirstVisibleItemPosition());

                    ((FileAdapter) mRecycler.getAdapter()).setCurrentDirectory(file);

                    mSource.setCurrentDirectory(file);

                    ((SourceActivity)getActivity()).getSourceManager().setActiveDirectory(file);

                    pushBreadcrumb(file);

                    mRecycler.getAdapter().notifyDataSetChanged();

                } else {
                    if (Utils.getStorageStats(Environment.getExternalStorageDirectory())
                            .getFreeSpace() > file.getData().getSize()) {
                        SelectedFilesManager.getInstance().addNewSelection();
                        ((SourceActivity)getActivity()).getSourceManager().addFileAction(
                                SelectedFilesManager.getInstance().getOperationCount()-1,
                                FileAction.OPEN);
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
    protected FileAdapter.OnFileLongClickedListener createOnLongClickListener() {
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

        // If this is the root node set the text to the source name instead of file name
        text.setText(directory.getParent() == null ?
                directory.getData().getSourceName() : directory.getData().getName());

        crumbLayout.setOnClickListener(v -> {
            TextView crumbText =  v.findViewById(R.id.crumb_text);

            int diff = (mBreadcrumbBar.getChildCount()-1) - mBreadcrumbBar.indexOfChild(v);
            for (int i = 0; i < diff; i++) popBreadcrumb();

            String name = crumbText.getText().toString();
            if (mSource.getCurrentDirectory().getData().getName().equals(name)) return;
            TreeNode<SourceFile> selectedParent = TreeNode.searchForParent(mSource.getCurrentDirectory(), name);

            int previousPosition = selectedParent.getData().getPositionToRestore();
            if (previousPosition != -1) {
                ((LinearLayoutManager)mRecycler.getLayoutManager()).scrollToPositionWithOffset(previousPosition, 0);
            }

            ((SourceActivity)getActivity()).getSourceManager().setActiveDirectory(selectedParent);
            ((FileAdapter)mRecycler.getAdapter()).setCurrentDirectory(selectedParent);
            mSource.setCurrentDirectory(selectedParent);
            mRecycler.getAdapter().notifyDataSetChanged();
        });

        mBreadcrumbWrapper.postDelayed(() ->
                mBreadcrumbWrapper.fullScroll(HorizontalScrollView.FOCUS_RIGHT),
                50L);
        mBreadcrumbBar.addView(crumbLayout);
        crumbLayout.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.breadcrumb_overshoot_z));
    }

    protected void pushAllBreadCrumbs(TreeNode<SourceFile> directory) {
        Stack<TreeNode<SourceFile>> breadCrumbs = new Stack<>();
        TreeNode<SourceFile> root = directory;

        breadCrumbs.push(root);
        while (root.getParent() != null) {
            root = root.getParent();
            breadCrumbs.push(root);
        }

        while(breadCrumbs.size() > 0) {
            pushBreadcrumb(breadCrumbs.pop());
        }
    }

    /**
     * Pop the latest breakcrumb added to the stack
     */
    protected void popBreadcrumb() {
        mBreadcrumbBar.removeViewAt(mBreadcrumbBar.getChildCount()-1);
    }

    /**
     * Pop all breadcrumbs in the stack
     */
    protected  void popAllBreadCrumbs() {
        mBreadcrumbBar.removeAllViews();
    }

    @Override
    public void onCheckPermissions(String permissionToCheck, int requestCode) {
        int permissionCheck = ContextCompat.checkSelfPermission(getActivity(), permissionToCheck);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{permissionToCheck}, requestCode);
        } else {
            if (this instanceof LocalFragment) {
                getSource().setLoggedIn(true);
                getSource().loadSource(getContext());
            } else if (this instanceof GoogleDriveFragment) {
                ((GoogleDriveSource)mSource).authGoogle(this);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constants.RequestCodes.STORAGE_PERMISSIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mSource.loadSource(getContext());
                    getSource().setLoggedIn(true);
                } else if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Snackbar.make(mRecycler, R.string.storage_permission, Snackbar.LENGTH_LONG)
                            .setAction(R.string.action_settings, v -> showAppDetails())
                            .show();
                }
                break;
            }

            case Constants.RequestCodes.ACCOUNTS_PERMISSIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (this instanceof GoogleDriveFragment) {
                        ((GoogleDriveSource)mSource).authGoogle(this);
                    }
                } else if (!shouldShowRequestPermissionRationale(Manifest.permission.GET_ACCOUNTS)) {
                    Snackbar.make(mRecycler, R.string.contacts_permission, Snackbar.LENGTH_LONG)
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
    protected void showAppDetails() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", "com.jakebarnby.filemanager", null);
        intent.setData(uri);
        startActivity(intent);
    }
}
