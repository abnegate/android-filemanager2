package com.jakebarnby.filemanager.activities.source;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.jakebarnby.filemanager.R;
import com.jakebarnby.filemanager.activities.source.adapters.SourcesPagerAdapter;
import com.jakebarnby.filemanager.managers.SelectedFilesManager;
import com.jakebarnby.filemanager.models.files.SourceFile;
import com.jakebarnby.filemanager.services.SourceTransferService;
import com.jakebarnby.filemanager.util.TreeNode;

import java.util.List;

import io.github.yavski.fabspeeddial.FabSpeedDial;
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter;

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

    public void setActiveDirectory(TreeNode<SourceFile>  currentDirectory) {
        this.mActiveDirectory = currentDirectory;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_source);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSourcesPagerAdapter = new SourcesPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSourcesPagerAdapter);
        mViewPager.addOnPageChangeListener(this);
        mViewPager.setOffscreenPageLimit(mSourcesPagerAdapter.getCount() - 1);

        mBroadcastReciever = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                handleIntent(intent);
            }
        };

        FabSpeedDial fabSpeedDial = (FabSpeedDial) findViewById(R.id.fab_speed_dial);
        fabSpeedDial.setMenuListener(new SimpleMenuListenerAdapter() {
            @Override
            public boolean onMenuItemSelected(MenuItem menuItem) {
                handleFabMenuItemSelected(menuItem);
                return true;
            }
        });

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
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
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Handles menu clicks from the fab action menu
     * @param menuItem  The selected item
     */
    private void handleFabMenuItemSelected(MenuItem menuItem) {
        int id = menuItem.getItemId();
        switch(id) {
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
            case R.id.action_delete:
                SourceTransferService.startActionDelete(SourceActivity.this, SelectedFilesManager.getInstance().getSelectedFiles());
                break;
        }

        for(SourceFragment fragment: mSourcesPagerAdapter.getFragments()) {
            fragment.setMultiSelectEnabled(false);
        }
    }

    /**
     * Call {@link SourceTransferService} to begin copying the currently selected files
     */
    private void startParseAction() {
        if (mCurrentFileAction != null) {
            if (mCurrentFileAction == FileAction.COPY)
                SourceTransferService.startActionCopy(SourceActivity.this, SelectedFilesManager.getInstance().getSelectedFiles(), mActiveDirectory.getData(), false);
            else if (mCurrentFileAction == FileAction.CUT) {
                SourceTransferService.startActionCopy(SourceActivity.this, SelectedFilesManager.getInstance().getSelectedFiles(), mActiveDirectory.getData(), true);
            }
        }
    }

    /**
     * Called when a broadcasted intent is recieved
     * @param intent    The broadcasted intent
     */
    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        switch(action) {
            case ACTION_COMPLETE:
                completeServiceAction();
                break;
            case ACTION_SHOW_DIALOG:
                showProgressDialog(intent);
                break;
            case ACTION_UPDATE_DIALOG:
                updateProgressDialog(intent);
        }
    }

    /**
     *  Called when {@link SourceTransferService} broadcasts that it has completed a background action
     */
    private void completeServiceAction() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
        //TODO: Only need to refresh the current fragment and the current directory of the adapter rather than reload the whole tree
        for(SourceFragment fragment: mSourcesPagerAdapter.getFragments()) {
            fragment.setMultiSelectEnabled(false);
            fragment.setFilesLoaded(false);
            fragment.loadSource();
        }
    }

    /**
     * Shows a dialog allowing a user to choose a grid or list layout for displaying files
     */
    private void showViewAsDialog() {
        ViewAsDialogFragment dialog = new ViewAsDialogFragment();
        dialog.show(getSupportFragmentManager(), "Dialog");
    }

    /**
     * Show a progress dialog
     * @param intent    The broadcasted intent with dialog extras
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
        mDialog.setMax(totalCount);
        mDialog.setProgress(currentCount);
        mDialog.setButton(DialogInterface.BUTTON_NEGATIVE,getString(R.string.cancel), (dialog, which) ->
            stopService(new Intent(this, SourceTransferService.class))
        );
        mDialog.setButton(DialogInterface.BUTTON_POSITIVE,getString(R.string.background), (dialog, which) ->
            dialog.dismiss()
        );
        mDialog.show();
    }

    /**
     * Update the progress of the {@link ProgressDialog} if it is showing
     * @param intent    The broadcasted intent with update extras
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

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        TreeNode<SourceFile> currentDir = mSourcesPagerAdapter.getFragments().get(position).getCurrentDirectory();
        if (currentDir != null) {
            setActiveDirectory(currentDir);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onBackPressed() {
        List<SourceFragment> fragments = mSourcesPagerAdapter.getFragments();
        boolean wasEnabled = false;
        for(SourceFragment fragment: fragments) {
            if (fragment.isMultiSelectEnabled()) {
                fragment.setMultiSelectEnabled(false);
                setTitle(R.string.app_name);
                wasEnabled = true;
            }
        }
        if (wasEnabled) return;
        super.onBackPressed();
    }
}
