package com.jakebarnby.filemanager.activities.source;

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
import android.support.v7.app.AppCompatActivity;
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

import com.jakebarnby.filemanager.R;
import com.jakebarnby.filemanager.activities.source.adapters.SourcesPagerAdapter;
import com.jakebarnby.filemanager.activities.source.dialogs.CreateFolderDialog;
import com.jakebarnby.filemanager.activities.source.dialogs.PropertiesDialog;
import com.jakebarnby.filemanager.activities.source.dialogs.RenameDialog;
import com.jakebarnby.filemanager.activities.source.dialogs.ViewAsDialog;
import com.jakebarnby.filemanager.managers.SelectedFilesManager;
import com.jakebarnby.filemanager.models.files.SourceFile;
import com.jakebarnby.filemanager.services.SourceTransferService;
import com.jakebarnby.filemanager.util.Constants;
import com.jakebarnby.filemanager.util.TreeNode;
import com.jakebarnby.filemanager.util.Utils;

import java.io.File;
import java.util.List;

import io.github.yavski.fabspeeddial.FabSpeedDial;
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter;
import jp.wasabeef.blurry.Blurry;

import static com.jakebarnby.filemanager.services.SourceTransferService.ACTION_COMPLETE;
import static com.jakebarnby.filemanager.services.SourceTransferService.ACTION_SHOW_DIALOG;
import static com.jakebarnby.filemanager.services.SourceTransferService.ACTION_UPDATE_DIALOG;
import static com.jakebarnby.filemanager.services.SourceTransferService.EXTRA_CURRENT_COUNT;
import static com.jakebarnby.filemanager.services.SourceTransferService.EXTRA_DIALOG_TITLE;
import static com.jakebarnby.filemanager.services.SourceTransferService.EXTRA_TOTAL_COUNT;

public class SourceActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {

    private SourcesPagerAdapter         mSourcesPagerAdapter;
    private ViewPager                   mViewPager;
    private TreeNode<SourceFile>        mActiveDirectory;
    private FileAction                  mCurrentFileAction;
    private BroadcastReceiver           mBroadcastReciever;
    private ProgressDialog              mDialog;
    private FabSpeedDial                mFabMenu;
    private RelativeLayout              mWrapperLayout;

    public enum FileAction {
        COPY,
        CUT
    }

    public SourcesPagerAdapter getPagerAdapter() {
        return mSourcesPagerAdapter;
    }

    public TreeNode<SourceFile> getActiveDirectory() {
        return mActiveDirectory;
    }

    public void setActiveDirectory(TreeNode<SourceFile> currentDirectory) {
        this.mActiveDirectory = currentDirectory;
    }

    public SourceFragment getActiveFragment() {
        return mSourcesPagerAdapter.getFragments().get(mViewPager.getCurrentItem());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_source);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSourcesPagerAdapter = new SourcesPagerAdapter(getSupportFragmentManager());
        mViewPager = findViewById(R.id.view_pager);
        mWrapperLayout = findViewById(R.id.wrapper);
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
            @Override
            public boolean onPrepareMenu(NavigationMenu navigationMenu) {
                Blurry.with(SourceActivity.this)
                        .radius(17)
                        .sampling(1)
                        .onto(mWrapperLayout);
                return super.onPrepareMenu(navigationMenu);
            }

            @Override
            public boolean onMenuItemSelected(MenuItem menuItem) {
                Blurry.delete(mWrapperLayout);
                handleFabMenuItemSelected(menuItem);
                return true;
            }

            @Override
            public void onMenuClosed() {
                Blurry.delete(mWrapperLayout);
                super.onMenuClosed();
            }
        });

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
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
                if (!getActiveFragment().isMultiSelectEnabled()) {
                    getActiveFragment().setMultiSelectEnabled(true);
                }
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Handles menu clicks from the fab action menu
     *
     * @param menuItem The selected item
     */
    private void handleFabMenuItemSelected(MenuItem menuItem) {
        int id = menuItem.getItemId();
        switch (id) {
            case R.id.action_rename:
                showRenameDialog();
                break;
            case R.id.action_copy:
                mCurrentFileAction = FileAction.COPY;
                Snackbar.make(mViewPager, getString(R.string.copied), Snackbar.LENGTH_SHORT).show();
                break;
            case R.id.action_cut:
                mCurrentFileAction = FileAction.CUT;
                Snackbar.make(mViewPager, getString(R.string.cut), Snackbar.LENGTH_SHORT).show();
                break;
            case R.id.action_paste:
                startParseAction();
                break;
            case R.id.action_properties:
                showPropertiesDialog();
                break;
            case R.id.action_delete:
                SourceTransferService.startActionDelete(SourceActivity.this);
                break;
        }
    }

    /**
     * Call {@link SourceTransferService} to begin copying the currently selected files
     */
    private void startParseAction() {
        if (mCurrentFileAction != null) {
            if (mCurrentFileAction == FileAction.COPY)
                SourceTransferService.startActionCopy(SourceActivity.this, mActiveDirectory, false);
            else if (mCurrentFileAction == FileAction.CUT) {
                SourceTransferService.startActionCopy(SourceActivity.this, mActiveDirectory, true);
            }
        }
    }

    /**
     * Called when a broadcasted intent is recieved
     *
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
        String path = intent.getStringExtra(Constants.FILE_PATH_KEY);

        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
            if (path == null) {
                toggleFloatingMenu(false);
                getActiveFragment().setMultiSelectEnabled(false);
            } else {
                viewFileInExternalApp(new File(path));
                return;
            }
        }
        setTitle(getString(R.string.app_name));
        toggleFloatingMenu(false);
        getActiveFragment().setMultiSelectEnabled(false);
        SelectedFilesManager.getInstance().getSelectedFiles().clear();
        getActiveFragment().replaceCurrentDirectory(getActiveDirectory());

    }

    /**
     * Shows a dialog allowing a user to choose a grid or list layout for displaying files
     */
    private void showViewAsDialog() {
        new ViewAsDialog().show(getSupportFragmentManager(), "ViewAs");
    }

    private void showCreateFolderDialog() {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.DIALOG_TITLE_KEY, getString(R.string.create_folder));
        CreateFolderDialog dialog =  new CreateFolderDialog();
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(), "CreateFolder");
    }

    /**
     * Shows a dialog allowing the user to rename a file or folder
     */
    private void showRenameDialog() {
        int size = SelectedFilesManager.getInstance().getSelectedFiles().size();
        if (size == 0) {
            Snackbar.make(mViewPager, getString(R.string.no_selection), Snackbar.LENGTH_LONG).show();
            return;
        } else if (size > 1) {
            Snackbar.make(mViewPager, getString(R.string.too_many_selected), Snackbar.LENGTH_LONG).show();
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putString(Constants.DIALOG_TITLE_KEY, getString(R.string.rename));
        RenameDialog dialog =  new RenameDialog();
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(), "Rename");
    }

    private void showPropertiesDialog() {
        int size = SelectedFilesManager.getInstance().getSelectedFiles().size();
        if (size == 0) {
            Snackbar.make(mViewPager, getString(R.string.no_selection), Snackbar.LENGTH_LONG).show();
            setTitle(getString(R.string.app_name));
            SelectedFilesManager.getInstance().getSelectedFiles().clear();
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

    /**
     * Attempts to open a file by finding it's mimetype then opening a compatible application
     * @param file The file to attempt to open
     */
    private void viewFileInExternalApp(File file) {
        String extension = Utils.fileExt(file.getPath());
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

        Uri reachableUri = FileProvider.getUriForFile(this, "com.jakebarnby.filemanager", file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(reachableUri, mimeType);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        PackageManager manager = getPackageManager();
        List<ResolveInfo> resolveInfo = manager.queryIntentActivities(intent, 0);
        if (resolveInfo.size() > 0) {
            startActivity(intent);
        } else {
            //TODO: Handle no app to open file
        }
    }

    /**
     *
     * @param enabled
     */
    public void toggleFloatingMenu(boolean enabled) {
        if (!enabled && mFabMenu.getVisibility() == View.INVISIBLE) return;
        if (enabled && mFabMenu.getVisibility() == View.VISIBLE) return;
        if (enabled) mFabMenu.setVisibility(View.VISIBLE);
        final int screenHeight = Utils.getScreenHeight(mFabMenu.getContext());
        TranslateAnimation translate = new TranslateAnimation(0.0f, 0.0f, enabled ? screenHeight : 0.0f, enabled ? 0.0f : screenHeight);
        translate.setInterpolator(enabled? new OvershootInterpolator(0.55f) : new AccelerateInterpolator(2.0f));
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

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        TreeNode<SourceFile> currentDir = getActiveFragment().getCurrentDirectory();
        if (currentDir != null) {
            setActiveDirectory(currentDir);
        }
        if (getActiveFragment().isMultiSelectEnabled()) {
            toggleFloatingMenu(true);
        } else {
            toggleFloatingMenu(false);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onBackPressed() {
        if (getActiveFragment().isMultiSelectEnabled()) {
            getActiveFragment().setMultiSelectEnabled(false);
            toggleFloatingMenu(false);
            setTitle(R.string.app_name);
            SelectedFilesManager.getInstance().getSelectedFiles().clear();
            return;
        }
        super.onBackPressed();
    }
}
