package com.jakebarnby.filemanager.sources;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.internal.NavigationMenu;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.webkit.MimeTypeMap;
import android.widget.RelativeLayout;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.ndk.CrashlyticsNdk;
import com.jakebarnby.filemanager.R;
import com.jakebarnby.filemanager.ui.adapters.FileSystemAdapter;
import com.jakebarnby.filemanager.ui.adapters.SourcePagerAdapter;
import com.jakebarnby.filemanager.ui.adapters.SourceUsageAdapter;
import com.jakebarnby.filemanager.ui.dialogs.CreateFolderDialog;
import com.jakebarnby.filemanager.ui.dialogs.PropertiesDialog;
import com.jakebarnby.filemanager.ui.dialogs.RenameDialog;
import com.jakebarnby.filemanager.ui.dialogs.ViewAsDialog;
import com.jakebarnby.filemanager.managers.SelectedFilesManager;
import com.jakebarnby.filemanager.sources.models.Source;
import com.jakebarnby.filemanager.sources.models.SourceManager;
import com.jakebarnby.filemanager.sources.models.SourceFile;
import com.jakebarnby.filemanager.services.SourceTransferService;
import com.jakebarnby.filemanager.util.Constants;
import com.jakebarnby.filemanager.util.TreeNode;
import com.jakebarnby.filemanager.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.fabric.sdk.android.Fabric;
import io.github.yavski.fabspeeddial.FabSpeedDial;
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter;
import jp.wasabeef.blurry.Blurry;

import static com.jakebarnby.filemanager.services.SourceTransferService.ACTION_COMPLETE;
import static com.jakebarnby.filemanager.services.SourceTransferService.ACTION_SHOW_DIALOG;
import static com.jakebarnby.filemanager.services.SourceTransferService.ACTION_UPDATE_DIALOG;
import static com.jakebarnby.filemanager.services.SourceTransferService.EXTRA_CURRENT_COUNT;
import static com.jakebarnby.filemanager.services.SourceTransferService.EXTRA_DIALOG_TITLE;
import static com.jakebarnby.filemanager.services.SourceTransferService.EXTRA_OPERATION_ID;
import static com.jakebarnby.filemanager.services.SourceTransferService.EXTRA_TOTAL_COUNT;

public class SourceActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {

    private SourceManager               mSourceManager;

    private SourcePagerAdapter mSourcesPagerAdapter;
    private ViewPager                   mViewPager;
    private BroadcastReceiver           mBroadcastReciever;
    private ProgressDialog              mDialog;
    private FabSpeedDial                mFabMenu;
    private RelativeLayout              mBlurWrapper;

    /**
     * Possible file actions
     */
    public enum FileAction {
        COPY,
        CUT,
        RENAME,
        DELETE,
        NEW_FOLDER,
        OPEN
    }

    public SourceManager getSourceManager() {
        return mSourceManager;
    }

    /**
     * Gets the fragment currently visible to the user
     * @return  The fragment currently visible to the user
     */
    public SourceFragment getActiveFragment() {
        return mSourcesPagerAdapter.getFragments().get(mViewPager.getCurrentItem());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_source);

        Fabric.with(this, new Crashlytics(), new CrashlyticsNdk(), new Answers());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSourceManager = new SourceManager();;
        mSourcesPagerAdapter = new SourcePagerAdapter(getSupportFragmentManager());
        mViewPager = findViewById(R.id.view_pager);
        mBlurWrapper = findViewById(R.id.wrapper);
        mViewPager.setAdapter(mSourcesPagerAdapter);
        mViewPager.addOnPageChangeListener(this);
        mViewPager.setOffscreenPageLimit(mSourcesPagerAdapter.getCount() - 1);

        mBroadcastReciever = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                handleIntent(intent);
            }
        };

        mFabMenu = findViewById(R.id.fab_speed_dial);
        mFabMenu.setMenuListener(new SimpleMenuListenerAdapter() {
            @SuppressLint("RestrictedApi")
            @Override
            public boolean onPrepareMenu(NavigationMenu navigationMenu) {
                if (SelectedFilesManager
                        .getInstance()
                        .getSelectedFiles(SelectedFilesManager.getInstance().getOperationCount()).size() == 0) {
                    showSnackbar(getString(R.string.err_no_selection));
                    return false;
                }
                Blurry.with(SourceActivity.this)
                        .radius(17)
                        .sampling(1)
                        .onto(mBlurWrapper);

                if (mSourceManager.getFileAction(SelectedFilesManager.getInstance().getOperationCount()) == null) {
                    navigationMenu.findItem(R.id.action_paste).setVisible(false);
                }
                if  (SelectedFilesManager
                        .getInstance()
                        .getSelectedFiles(SelectedFilesManager.getInstance().getOperationCount())
                        .size() > 1) {
                    navigationMenu.findItem(R.id.action_rename).setVisible(false);
                }
                return super.onPrepareMenu(navigationMenu);
            }

            @Override
            public boolean onMenuItemSelected(MenuItem menuItem) {
                Blurry.delete(mBlurWrapper);
                handleFabMenuItemSelected(menuItem);
                return true;
            }

            @Override
            public void onMenuClosed() {
                Blurry.delete(mBlurWrapper);
                super.onMenuClosed();
            }
        });

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        SourceTransferService.startClearLocalCache(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_SHOW_DIALOG);
        filter.addAction(ACTION_UPDATE_DIALOG);
        filter.addAction(ACTION_COMPLETE);
        LocalBroadcastManager
                .getInstance(this)
                .registerReceiver(mBroadcastReciever, filter);
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager
                .getInstance(this)
                .unregisterReceiver(mBroadcastReciever);
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_source, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                break;
            case R.id.action_viewas:
                showViewAsDialog();
                break;
            case R.id.action_new_folder:
                showCreateFolderDialog();
                break;
            case R.id.action_multi_select:
                if (!getActiveFragment().getSource().isMultiSelectEnabled()) {
                    getActiveFragment().setMultiSelectEnabled(true);

                    if (SelectedFilesManager.getInstance().getOperationCount() == 0) {
                        SelectedFilesManager.getInstance().addNewSelection();
                    }
                }
                break;
            case R.id.action_storage_usage:
                showUsageDialog();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Handles menu clicks from the fab action menu
     * @param menuItem The selected item
     */
    private void handleFabMenuItemSelected(MenuItem menuItem) {
        int id = menuItem.getItemId();
        switch (id) {
            case R.id.action_rename:
                showRenameDialog();
                break;
            case R.id.action_copy:
                startCopyAction();
                break;
            case R.id.action_cut:
                startCutAction();
                break;
            case R.id.action_paste:
                doPasteChecks();
                break;
            case R.id.action_properties:
                showPropertiesDialog();
                break;
            case R.id.action_delete:
                startDeleteAction();
                break;
        }
    }

    /**
     * Check if there are enough files selected and start a cut action
     * otherwise throw a snackbar with error message
     */
    private void startCutAction() {
        if (SelectedFilesManager
                .getInstance()
                .getSelectedFiles(SelectedFilesManager.getInstance().getOperationCount())
                .size() > 0) {
            mSourceManager.addFileAction(SelectedFilesManager.getInstance().getOperationCount(), FileAction.CUT);
            showSnackbar(getString(R.string.cut));
        } else {
            showSnackbar(getString(R.string.err_no_selection));
        }
        getActiveFragment().setMultiSelectEnabled(false);
    }

    /**
     * Check if there are enough files selected and start a copy action
     * otherwise throw a snackbar with error message
     */
    private void startCopyAction() {
        if (SelectedFilesManager
                .getInstance()
                .getSelectedFiles(SelectedFilesManager.getInstance().getOperationCount())
                .size() > 0) {
            mSourceManager.addFileAction(SelectedFilesManager.getInstance().getOperationCount(), FileAction.COPY);
            showSnackbar(getString(R.string.copied));
        } else {
            showSnackbar(getString(R.string.err_no_selection));
        }
        for(SourceFragment fragment: mSourcesPagerAdapter.getFragments()) {
            fragment.setMultiSelectEnabled(false);
        }
    }

    /**
     * Check if there are enough files selected and start a delete action
     * otherwise throw a snackbar with error message
     */
    private void startDeleteAction() {
        if (getActiveFragment().getSource().checkConnectionActive(this)) {
            if (SelectedFilesManager
                    .getInstance()
                    .getSelectedFiles(SelectedFilesManager.getInstance().getOperationCount())
                    .size() > 0) {

                mSourceManager.addFileAction(SelectedFilesManager.getInstance().getOperationCount(), FileAction.DELETE);

                SelectedFilesManager.getInstance().addActionableDirectory(
                        SelectedFilesManager.getInstance().getOperationCount(),
                        mSourceManager.getActiveDirectory());

                getActiveFragment().setMultiSelectEnabled(false);
                setTitle(getString(R.string.app_name));
                toggleFloatingMenu(false);
                SourceTransferService.startActionDelete(SourceActivity.this);
                getActiveFragment()
                        .getSource()
                        .increaseFreeSpace(SelectedFilesManager.getInstance().getCurrentCopySize());
                SelectedFilesManager.getInstance().addNewSelection();
            } else {
                showSnackbar(getString(R.string.err_no_selection));
            }
        }
    }

    /**
     * Check if the active source is connected, logged in, loaded and has enough free space.
     * If so, start a paste action via {@link SourceTransferService};
     * Otherwise show a snackbar with the error message
     */
    private void doPasteChecks() {
        if (getActiveFragment().getSource().checkConnectionActive(this)) {

            if (!getActiveFragment().getSource().isLoggedIn()) {
                showSnackbar(getString(R.string.err_not_logged_in));
                return;
            } else if (!getActiveFragment().getSource().isFilesLoaded()) {
                showSnackbar(getString(R.string.err_not_loaded));
                return;
            }

            long copySize = SelectedFilesManager
                    .getInstance()
                    .getCurrentCopySize();

            if (copySize < getActiveFragment().getSource().getFreeSpace()) {
                startPasteAction(copySize);
            } else {
                showSnackbar(String.format(
                        getString(R.string.err_no_free_space),
                        getActiveFragment().getSource().getSourceName().toLowerCase()));
            }
        }
    }

    /**
     * Start a paste action via {@link SourceTransferService}
     */
    private void startPasteAction(long copySize) {
        if (mSourceManager.getFileAction(
                SelectedFilesManager.getInstance().getOperationCount()) != null) {
            getActiveFragment().setMultiSelectEnabled(false);
            setTitle(getString(R.string.app_name));
            toggleFloatingMenu(false);

            SelectedFilesManager.getInstance().addActionableDirectory(
                    SelectedFilesManager.getInstance().getOperationCount(),
                    mSourceManager.getActiveDirectory());

            FileAction curAction =
                    mSourceManager.getFileAction(SelectedFilesManager.getInstance().getOperationCount());

            if (curAction == FileAction.COPY)
                SourceTransferService.startActionCopy(SourceActivity.this, false);
            else if (curAction == FileAction.CUT) {
                SourceTransferService.startActionCopy(SourceActivity.this, true);
            }
            SelectedFilesManager.getInstance().addNewSelection();
            getActiveFragment().getSource().decreaseFreeSpace(copySize);
        }
    }

    /**
     * Called when a broadcasted intent is recieved
     * @param intent The broadcasted intent
     */
    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        switch (action) {
            case ACTION_COMPLETE:
                completeServiceAction(intent);
                break;
            case ACTION_SHOW_DIALOG:
                showProgressDialog(intent);
                break;
            case ACTION_UPDATE_DIALOG:
                updateProgressDialog(intent);
        }
    }

    /**
     * Called when {@link SourceTransferService} broadcasts that it has completed a background action
     */
    private void completeServiceAction(Intent intent) {
        int operationId = intent.getIntExtra(EXTRA_OPERATION_ID, 0);
        String path = intent.getStringExtra(Constants.FILE_PATH_KEY);

        switch(mSourceManager.getFileAction(operationId)) {
            case CUT:
            case COPY:
            case DELETE:
            case RENAME:
            case NEW_FOLDER:
                completeTreeModification(operationId);
                break;
            case OPEN:
                startOpenFile(path);
                break;
            default:
                break;
        }

        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    /**
     * Complete a service action that modified the file tree
     * @param operationId
     */
    private void completeTreeModification(int operationId) {
        //SelectedFilesManager.getInstance().getSelectedFiles(operationId).clear();
        TreeNode.sortTree(SelectedFilesManager.getInstance().getActionableDirectory(operationId), (node1, node2) -> {
            int result = Boolean.valueOf(!node1.getData().isDirectory()).compareTo(!node2.getData().isDirectory());
            if (result == 0) {
                result = node1.getData().getName().toLowerCase().compareTo(node2.getData().getName().toLowerCase());
            }
            return result;
        });

        getActiveFragment().refreshRecycler();
    }

    /**
     * Show a snackbar with the given message.
     * @param message   The message to display in the snackbar.
     */
    private void showSnackbar(String message) {
        Snackbar.make(mViewPager, message, Snackbar.LENGTH_LONG).show();
    }

    /**
     * Shows a dialog allowing a user to choose a grid or list layout for displaying files.
     */
    private void showViewAsDialog() {
        new ViewAsDialog().show(getSupportFragmentManager(), "ViewAs");
    }

    /**
     * Shows a dialog asking for a new folder name which creates the folder on completion
     */
    private void showCreateFolderDialog() {
        if (getActiveFragment().getSource().checkConnectionActive(this)) {
            mSourceManager.addFileAction(
                    SelectedFilesManager.getInstance().getOperationCount(),
                    FileAction.NEW_FOLDER);
            SelectedFilesManager.getInstance().addActionableDirectory(
                    SelectedFilesManager.getInstance().getOperationCount(),
                    mSourceManager.getActiveDirectory());

            Bundle bundle = new Bundle();
            bundle.putString(Constants.DIALOG_TITLE_KEY, getString(R.string.create_folder));
            CreateFolderDialog dialog = new CreateFolderDialog();
            dialog.setArguments(bundle);
            dialog.show(getSupportFragmentManager(), "CreateFolder");
        }
    }

    /**
     * Shows a dialog allowing the user to rename a file or folder
     */
    private void showRenameDialog() {
        if (getActiveFragment().getSource().checkConnectionActive(this)) {

            getActiveFragment().setMultiSelectEnabled(false);
            setTitle(getString(R.string.app_name));
            toggleFloatingMenu(false);

            mSourceManager.addFileAction(
                    SelectedFilesManager.getInstance().getOperationCount(),
                    FileAction.RENAME);
            SelectedFilesManager.getInstance().addActionableDirectory(
                    SelectedFilesManager.getInstance().getOperationCount(),
                    mSourceManager.getActiveDirectory());

            int size = SelectedFilesManager.getInstance().getSelectedFiles(
                    SelectedFilesManager.getInstance().getOperationCount()).size();

            if (size == 0) {
                showSnackbar(getString(R.string.err_no_selection));
                return;
            } else if (size > 1) {
                showSnackbar(getString(R.string.err_too_many_selected));
                return;
            }

            Bundle bundle = new Bundle();
            bundle.putString(Constants.DIALOG_TITLE_KEY, getString(R.string.rename));
            RenameDialog dialog = new RenameDialog();
            dialog.setArguments(bundle);
            dialog.show(getSupportFragmentManager(), "Rename");
        }
    }

    /**
     * Shows a dialog displaying properties about the selected files and/or folders
     */
    private void showPropertiesDialog() {
        int size = SelectedFilesManager.getInstance().getSelectedFiles(SelectedFilesManager.getInstance().getOperationCount()).size();
        if (size == 0) {
            showSnackbar(getString(R.string.err_no_selection));
        } else {
            new PropertiesDialog().show(getSupportFragmentManager(), "Properties");
        }
    }

    /**
     * Show a progress dialog
     *
     * @param intent The broadcasted intent with dialog extras
     */
    private void showProgressDialog(Intent intent) {
        String title = intent.getStringExtra(EXTRA_DIALOG_TITLE);
        if (title == null) {
            title = "Operation in progress..";
        }
        int totalCount = intent.getIntExtra(EXTRA_TOTAL_COUNT, 0);
        int currentCount = 0;

        mDialog = new ProgressDialog(this);
        mDialog.setTitle(title);
        mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mDialog.setIndeterminate(true);
        if (totalCount != 0) {
            mDialog.setMax(totalCount);
            mDialog.setProgress(currentCount);
        } else {
            mDialog.setProgressNumberFormat(null);
            mDialog.setProgressPercentFormat(null);
        }
        mDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), (dialog, which) ->
                stopService(new Intent(this, SourceTransferService.class))
        );
        mDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.background), (dialog, which) ->
                dialog.dismiss()
        );
        mDialog.show();
    }

    /**
     * Update the progress of the {@link ProgressDialog} if it is showing
     *
     * @param intent The broadcasted intent with update extras
     */
    private void updateProgressDialog(Intent intent) {
        if (mDialog != null && mDialog.isShowing()) {
            if (mDialog.isIndeterminate()) {
                mDialog.setIndeterminate(false);
            }
            int currentCount = intent.getIntExtra(EXTRA_CURRENT_COUNT, 0);
            mDialog.setProgress(currentCount);
        }
    }

    private void showUsageDialog() {
        List<Source> sources = new ArrayList<>();
        for(SourceFragment fragment: mSourcesPagerAdapter.getFragments()) {
            if (fragment.getSource().isFilesLoaded()) {
                sources.add(fragment.getSource());
            }
        }
        SourceUsageAdapter adapter = new SourceUsageAdapter(sources);

        View view = getLayoutInflater().inflate(R.layout.dialog_source_usage, null);
        RecyclerView rv = view.findViewById(R.id.rv_source_usage);
        rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rv.setAdapter(adapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder
                .setTitle(R.string.dialog_title_usage)
                .setNegativeButton(R.string.close, (dialog, which) -> dialog.dismiss())
                .setView(view)
                .create()
                .show();
    }

    /**
     * Open a file at the given path. If the open operation was finished in the background,
     * inform user file is ready to open with a dialog. Otherwise open the file immediately.
     *
     * If a user selects 'No' and chooses not the open the file, it will still be cached when
     * attempting to open again (until next app launch.)
     *
     * @param filePath  The absolute path of the file to open
     */
    private void startOpenFile(String filePath) {
        if (mDialog != null && !mDialog.isShowing()) {
            String filename = filePath.substring(
                    filePath.lastIndexOf(File.separator)+1,
                    filePath.length());

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.file_ready));
            builder.setMessage(String.format(getString(R.string.open_now), filename));

            builder.setNegativeButton(getString(R.string.no), (dialog, which) ->
                    dialog.dismiss()
            );
            builder.setPositiveButton(getString(R.string.yes), (dialog, which) ->
                    viewFileInExternalApp(filePath)
            );

            builder.create().show();
        } else {
            viewFileInExternalApp(filePath);
        }
    }

    /**
     * Attempts to open a file by finding it's mimetype then opening a compatible application
     * @param filePath  The absolute path of the file to open
     */
    private void viewFileInExternalApp(String filePath) {
        if (filePath == null) return;

        File file = new File(filePath);
        String extension = Utils.fileExt(filePath);
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

        Uri reachableUri = FileProvider.getUriForFile(this, getApplicationInfo().packageName, file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(reachableUri, mimeType);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        PackageManager manager = getPackageManager();
        List<ResolveInfo> resolveInfo = manager.queryIntentActivities(intent, 0);
        if (resolveInfo.size() > 0) {
            startActivity(intent);
        } else {
            showSnackbar(getString(R.string.err_no_app_available));
        }
    }

    /**
     * Toggles the floating action context menu
     * @param enabled   Whether the menu should enabled or not
     */
    public void toggleFloatingMenu(boolean enabled) {
        if (!enabled && mFabMenu.getVisibility() == View.INVISIBLE) return;
        if (enabled && mFabMenu.getVisibility() == View.VISIBLE) return;
        if (enabled) mFabMenu.setVisibility(View.VISIBLE);
        final int screenHeight = Utils.getScreenHeight(mFabMenu.getContext());
        TranslateAnimation translate = new TranslateAnimation(0.0f, 0.0f, enabled ? screenHeight : 0.0f, enabled ? 0.0f : screenHeight);
        translate.setInterpolator(enabled ? new OvershootInterpolator(0.55f) : new AccelerateInterpolator(2.0f));
        translate.setDuration(400);
        translate.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (!enabled) mFabMenu.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mFabMenu.startAnimation(translate);
    }

    /**
     * Initializes recyclerviews for all logged in fragments due to a view layout change (Grid <-> List)
     */
    public void initAllRecyclers() {
        for (SourceFragment fragment: mSourcesPagerAdapter.getFragments()) {
            if (fragment.getSource().isFilesLoaded()) {
                fragment.initRecyclerView();
            }
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        mSourceManager.setActiveDirectory(getActiveFragment().getSource().getCurrentDirectory());
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onBackPressed() {
        TreeNode<SourceFile> activeDir = mSourceManager.getActiveDirectory();

        if (getActiveFragment().getSource().isMultiSelectEnabled()) {

            getActiveFragment().setMultiSelectEnabled(false);
            toggleFloatingMenu(false);
            setTitle(R.string.app_name);

            SelectedFilesManager
                    .getInstance()
                    .getSelectedFiles(SelectedFilesManager.getInstance().getOperationCount())
                    .clear();

        } else if (getActiveFragment().getSource().isLoggedIn() &&
                activeDir.getParent() != null) {

            int previousPosition = activeDir.getParent().getData().getPositionToRestore();
            if (previousPosition != -1) {
                ((LinearLayoutManager)getActiveFragment().mRecycler.getLayoutManager()).scrollToPositionWithOffset(previousPosition, 0);
            }

            getActiveFragment()
                    .getSource()
                    .setCurrentDirectory(activeDir.getParent());

            ((FileSystemAdapter) getActiveFragment().mRecycler.getAdapter())
                    .setCurrentDirectory(activeDir.getParent());

            mSourceManager.setActiveDirectory(activeDir.getParent());

            getActiveFragment().refreshRecycler();
            getActiveFragment().popBreadcrumb();
        } else {
            super.onBackPressed();
        }
    }
}
