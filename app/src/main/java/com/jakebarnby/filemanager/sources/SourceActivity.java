package com.jakebarnby.filemanager.sources;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.app.SearchManager;
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
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.ndk.CrashlyticsNdk;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.jakebarnby.filemanager.R;
import com.jakebarnby.filemanager.managers.BillingManager;
import com.jakebarnby.filemanager.models.FileAction;
import com.jakebarnby.filemanager.sources.local.LocalFragment;
import com.jakebarnby.filemanager.sources.models.SourceType;
import com.jakebarnby.filemanager.ui.adapters.FileAdapter;
import com.jakebarnby.filemanager.ui.adapters.SearchResultAdapter;
import com.jakebarnby.filemanager.ui.adapters.SourceLogoutAdapter;
import com.jakebarnby.filemanager.ui.adapters.SourcePagerAdapter;
import com.jakebarnby.filemanager.ui.adapters.SourceUsageAdapter;
import com.jakebarnby.filemanager.ui.dialogs.CreateFolderDialog;
import com.jakebarnby.filemanager.ui.dialogs.CreateZipDialog;
import com.jakebarnby.filemanager.ui.dialogs.PropertiesDialog;
import com.jakebarnby.filemanager.ui.dialogs.RenameDialog;
import com.jakebarnby.filemanager.ui.dialogs.SettingsDialog;
import com.jakebarnby.filemanager.ui.dialogs.SortByDialog;
import com.jakebarnby.filemanager.ui.dialogs.ViewAsDialog;
import com.jakebarnby.filemanager.managers.SelectedFilesManager;
import com.jakebarnby.filemanager.sources.models.Source;
import com.jakebarnby.filemanager.sources.models.SourceManager;
import com.jakebarnby.filemanager.sources.models.SourceFile;
import com.jakebarnby.filemanager.services.SourceTransferService;
import com.jakebarnby.filemanager.util.ComparatorUtils;
import com.jakebarnby.filemanager.util.Constants;
import com.jakebarnby.filemanager.util.PreferenceUtils;
import com.jakebarnby.filemanager.util.TreeNode;
import com.jakebarnby.filemanager.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import io.fabric.sdk.android.Fabric;
import io.github.yavski.fabspeeddial.FabSpeedDial;
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter;
import jp.wasabeef.blurry.Blurry;

import static android.content.Intent.ACTION_MEDIA_BAD_REMOVAL;
import static android.content.Intent.ACTION_MEDIA_MOUNTED;
import static android.content.Intent.ACTION_MEDIA_REMOVED;
import static android.content.Intent.ACTION_MEDIA_UNMOUNTED;
import static android.hardware.usb.UsbManager.ACTION_USB_DEVICE_DETACHED;
import static android.content.Intent.ACTION_SEARCH;
import static com.jakebarnby.filemanager.util.IntentExtensions.ACTION_COMPLETE;
import static com.jakebarnby.filemanager.util.IntentExtensions.ACTION_SHOW_DIALOG;
import static com.jakebarnby.filemanager.util.IntentExtensions.ACTION_SHOW_ERROR;
import static com.jakebarnby.filemanager.util.IntentExtensions.ACTION_UPDATE_DIALOG;
import static com.jakebarnby.filemanager.util.IntentExtensions.EXTRA_DIALOG_CURRENT_VALUE;
import static com.jakebarnby.filemanager.util.IntentExtensions.EXTRA_DIALOG_MESSAGE;
import static com.jakebarnby.filemanager.util.IntentExtensions.EXTRA_DIALOG_TITLE;
import static com.jakebarnby.filemanager.util.IntentExtensions.EXTRA_OPERATION_ID;
import static com.jakebarnby.filemanager.util.IntentExtensions.EXTRA_DIALOG_MAX_VALUE;

public class SourceActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener, SearchView.OnQueryTextListener {

    private SourceManager               mSourceManager;

    private SourcePagerAdapter          mSourcesPagerAdapter;
    private ViewPager                   mViewPager;
    private BroadcastReceiver           mBroadcastReciever;
    private ProgressDialog              mDialog;
    private FabSpeedDial                mFabMenu;
    private ViewGroup                   mBlurWrapper;
    private SearchView                  mSearchView;

    private BillingManager              mBillingManager;
    private InterstitialAd              mInterstitialAd;

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

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }
        });

        mBillingManager = new BillingManager(this);
        mSourceManager = new SourceManager();
        mSourcesPagerAdapter = new SourcePagerAdapter(getSupportFragmentManager());
        addLocalSources();

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
                prepareContextMenu(navigationMenu);
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

    @SuppressLint("RestrictedApi")
    private void prepareContextMenu(NavigationMenu navigationMenu) {
        Blurry.with(SourceActivity.this)
                .radius(17)
                .sampling(1)
                .async()
                .onto(mBlurWrapper);

        if (mSourceManager.getFileAction(SelectedFilesManager.getInstance().getOperationCount()) == null) {
            navigationMenu.findItem(R.id.action_paste).setVisible(false);
        }

        if  (SelectedFilesManager
                .getInstance()
                .getCurrentSelectedFiles()
                .size() > 1) {
            navigationMenu.findItem(R.id.action_rename).setVisible(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filterLocal = new IntentFilter();
        filterLocal.addAction(ACTION_SHOW_DIALOG);
        filterLocal.addAction(ACTION_UPDATE_DIALOG);
        filterLocal.addAction(ACTION_SHOW_ERROR);
        filterLocal.addAction(ACTION_COMPLETE);
        getApplicationContext().registerReceiver(mBroadcastReciever, filterLocal);

        IntentFilter filterSystem = new IntentFilter();
        filterSystem.addAction(ACTION_MEDIA_MOUNTED);
        filterSystem.addAction(ACTION_MEDIA_REMOVED);
        filterSystem.addAction(ACTION_MEDIA_UNMOUNTED);
        filterSystem.addAction(ACTION_MEDIA_BAD_REMOVAL);
        filterSystem.addDataScheme("file");
        getApplicationContext().registerReceiver(mBroadcastReciever, filterSystem);
    }

    @Override
    protected void onPause() {
        getApplicationContext().unregisterReceiver(mBroadcastReciever);
        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_source, menu);

        if (!PreferenceUtils.getBoolean(this, Constants.Prefs.SHOW_ADS_KEY, false)) {
            menu.add(Menu.NONE, Constants.ADS_MENU_ID, Constants.ADS_MENU_POSITION, R.string.action_remove_ads);
        }

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_view_as:
                showViewAsDialog();
                break;
            case R.id.action_sort_by:
                showSortByDialog();
                break;
            case R.id.action_new_folder:
                showCreateFolderDialog();
                break;
            case R.id.action_multi_select:
                handleMenuMultiSelect();
                break;
            case R.id.action_storage_usage:
                showUsageDialog();
                break;
            case R.id.action_logout:
                showLogoutDialog();
                break;
            case R.id.action_settings:
                showSettingsDialog();
                break;
            case Constants.ADS_MENU_ID:
                mBillingManager.purchaseItem(this, Constants.Billing.SKU_PREMIUM);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSettingsDialog() {
        SettingsDialog dialog = new SettingsDialog();
        dialog.show(getSupportFragmentManager(), Constants.DialogTags.SETTINGS);
    }

    private void addLocalSource(String path) {
        int indexToInsert = 0;
        for(SourceFragment fragment: mSourcesPagerAdapter.getFragments()) {
            if (fragment.getSource().getSourceType() == SourceType.LOCAL) indexToInsert++;
        }

        String newSourceName = indexToInsert == 1 ?
                getString(R.string.sdcard) :
                getString(R.string.usb)+String.valueOf(indexToInsert-1);

        mSourcesPagerAdapter.getFragments()
                .add(indexToInsert, LocalFragment.newInstance(newSourceName, path));
        mSourcesPagerAdapter.notifyDataSetChanged();
    }

    /**
     * Check all external storage sources and add any that are not already added
     */
    private void addLocalSources() {
        String[] paths = Utils.getExternalStorageDirectories(getApplicationContext());
        if (paths.length > 0) {
            for (int i = 0; i < paths.length; i++){
                String[] split = paths[i].split("/");
                String rootDirTitle = split[split.length-1];

                boolean alreadyAdded = false;
                for(SourceFragment fragment: mSourcesPagerAdapter.getFragments()) {
                    if (fragment.getSource() != null &&
                            fragment.getSource().getRootNode().getData().getPath().contains(rootDirTitle)) {
                        alreadyAdded = true;
                        break;
                    }
                }

                if (alreadyAdded) continue;
                mSourcesPagerAdapter.getFragments().add(i+1, LocalFragment.newInstance(
                        i == 0 ? getString(R.string.sdcard) : getString(R.string.usb)+i,
                        paths[i])
                );
            }
            mSourcesPagerAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Remove a local source fragment
     * @param sourcePath    The root path of the source
     */
    private void removeLocalSource(String sourcePath) {
        for (int i = 0; i < mSourcesPagerAdapter.getFragments().size(); i++) {
            Source source = mSourcesPagerAdapter.getFragments().get(i).getSource();
            if (source.getSourceType() == SourceType.LOCAL &&
                source.isFilesLoaded()  &&
                sourcePath.contains(source.getRootNode().getData().getPath())) {

                mSourcesPagerAdapter.getFragments().remove(i);
            }
        }
        mSourcesPagerAdapter.notifyDataSetChanged();
    }

    /**
     * Enabled multiselect if it is not already enabled and add a new selection
     */
    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    private void handleMenuMultiSelect() {
        if (!getActiveFragment().getSource().isMultiSelectEnabled()) {
            getActiveFragment().setMultiSelectEnabled(true);

            if (SelectedFilesManager.getInstance().getOperationCount() == 0) {
                SelectedFilesManager.getInstance().addNewSelection();
            }
        }
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
            case R.id.action_zip:
                showCreateZipDialog();
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
     * Called when a broadcasted intent is recieved
     * @param intent The broadcasted intent
     */
    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        if (action != null) {
            switch (action) {
                case ACTION_COMPLETE:
                    completeServiceAction(intent);
                    break;
                case ACTION_SHOW_DIALOG:
                    showProgressDialog(intent);
                    break;
                case ACTION_SHOW_ERROR:
                    showErrorDialog(intent.getStringExtra(EXTRA_DIALOG_MESSAGE));
                case ACTION_UPDATE_DIALOG:
                    updateProgressDialog(intent);
                    break;
                case ACTION_MEDIA_MOUNTED:
                    addLocalSource(intent.getDataString().replace("file://", ""));
                    break;
                case ACTION_USB_DEVICE_DETACHED:
                case ACTION_MEDIA_BAD_REMOVAL:
                case ACTION_MEDIA_REMOVED:
                    removeLocalSource(intent.getDataString());
                    break;
                case ACTION_SEARCH:
                    doSearch(intent.getStringExtra(SearchManager.QUERY));
            }
        }
    }

    /**
     * Search all files and folders for the given query string
     * @param query     Name of the file or folder to find
     */
    private void doSearch(String query) {
        List<TreeNode<SourceFile>> allResults = new ArrayList<>();
        List<String> sourceNames = new ArrayList<>();

        for(SourceFragment fragment: mSourcesPagerAdapter.getFragments()) {
            List<TreeNode<SourceFile>> results =
                    TreeNode.searchForChildren(fragment.getSource().getRootNode(), query);

            if (results.size() > 0) {
                allResults.addAll(results);
                sourceNames.add(fragment.getSource().getSourceName());
            }
        }

        Collections.sort(allResults,
                (t1, t2) -> t1.getData().getName().toLowerCase().compareTo(t2.getData().getName().toLowerCase())
        );

        AlertDialog searchDialog = new AlertDialog.Builder(this)
                .setNegativeButton(R.string.close, (dialog, which) -> dialog.dismiss())
                .create();

        SearchResultAdapter adapter = new SearchResultAdapter(allResults, toOpen -> {
            List<SourceFragment> fragments = mSourcesPagerAdapter.getFragments();
            for (int i= 0; i < mSourcesPagerAdapter.getFragments().size(); i++) {
                if (fragments.get(i).getSource().getSourceName().equals(toOpen.getData().getSourceName())) {
                    if (searchDialog.isShowing()) {
                        searchDialog.dismiss();
                    }

                    mViewPager.setCurrentItem(i, true);

                    TreeNode<SourceFile> newDir = toOpen.getData().isDirectory() ? toOpen : toOpen.getParent();
                    getActiveFragment().getSource().setCurrentDirectory(newDir);
                    ((FileAdapter)getActiveFragment().mRecycler.getAdapter()).setCurrentDirectory(newDir);
                    mSourceManager.setActiveDirectory(newDir);

                    getActiveFragment().popAllBreadCrumbs();
                    getActiveFragment().pushAllBreadCrumbs(newDir);
                    getActiveFragment().mRecycler.getAdapter().notifyDataSetChanged();
                    getActiveFragment().mRecycler.requestFocus();
                }
            }
        });

        View view = getLayoutInflater().inflate(R.layout.dialog_search_results, null);
        RecyclerView rv = view.findViewById(R.id.rv_search_results);

        if (allResults.isEmpty()) {
            searchDialog.setMessage(getString(R.string.no_results));
        } else {
            rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            rv.setAdapter(adapter);
            searchDialog.setTitle(R.string.search_results);
            searchDialog.setView(view);
        }

        ArrayAdapter<String> arrayadapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        arrayadapter.add(Constants.ALL);
        arrayadapter.addAll(sourceNames);
        arrayadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spn = view.findViewById(R.id.spn_sources);
        spn.setAdapter(arrayadapter);
        spn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selected = (String) adapterView.getItemAtPosition(i);
                if (selected.equals(Constants.ALL)) {
                    adapter.resetDataset();
                    adapter.notifyDataSetChanged();
                } else {
                    adapter.removeAllSourceExcept(selected);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        searchDialog.show();
    }

    /**
     * Check if there are enough files selected and start a cut action
     * otherwise throw a snackbar with error message
     */
    private void startCutAction() {
        if (SelectedFilesManager
                .getInstance()
                .getCurrentSelectedFiles()
                .size() > 0) {
            mSourceManager.addFileAction(SelectedFilesManager.getInstance().getOperationCount(), FileAction.CUT);
            showSnackbar(getString(R.string.cut));
        } else {
            showSnackbar(getString(R.string.err_no_selection));
        }
        disableAllMultiSelect();
    }

    /**
     * Check if there are enough files selected and start a copy action
     * otherwise throw a snackbar with error message
     */
    private void startCopyAction() {
        if (SelectedFilesManager
                .getInstance()
                .getCurrentSelectedFiles()
                .size() > 0) {
            mSourceManager.addFileAction(SelectedFilesManager.getInstance().getOperationCount(), FileAction.COPY);
            showSnackbar(getString(R.string.copied));
        } else {
            showSnackbar(getString(R.string.err_no_selection));
        }
        disableAllMultiSelect();
    }

    /**
     * Check if there are enough files selected then show a confirmation dialog and start a delete action
     *  or do nothing otherwise throw a snackbar with error message
     */
    private void startDeleteAction() {
        if (getActiveFragment().getSource().checkConnectionActive(this)) {

            int size = SelectedFilesManager
                    .getInstance()
                    .getCurrentSelectedFiles()
                    .size();

            if (size > 0) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.warning)
                        .setMessage(String.format(Locale.getDefault(), getString(R.string.dialog_delete_confirm), size))
                        .setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss())
                        .setPositiveButton(R.string.yes, (dialog, which) -> {
                            mSourceManager.addFileAction(SelectedFilesManager.getInstance().getOperationCount(), FileAction.DELETE);

                            SelectedFilesManager.getInstance().addActionableDirectory(
                                    SelectedFilesManager.getInstance().getOperationCount(),
                                    mSourceManager.getActiveDirectory());

                            disableAllMultiSelect();
                            toggleFloatingMenu(false);
                            setTitle(getString(R.string.app_name));
                            SourceTransferService.startActionDelete(SourceActivity.this);
                            getActiveFragment()
                                    .getSource()
                                    .increaseFreeSpace(SelectedFilesManager.getInstance().getCurrentCopySize());
                            SelectedFilesManager.getInstance().addNewSelection();
                        })
                        .create().show();
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
            if (getActiveFragment().getSource().getSourceType() == SourceType.LOCAL &&
                    !getActiveFragment().getSource().getSourceName().equals(Constants.Sources.LOCAL)) {
                showSnackbar(getString(R.string.err_no_ext_write));
                return;
            }

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
                        getActiveFragment().getSource().getSourceName()));
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
            case NEW_ZIP:
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

        int operationCount = PreferenceUtils.getInt(this, Constants.Prefs.OPERATION_COUNT_KEY, 0);
        PreferenceUtils.savePref(this, Constants.Prefs.OPERATION_COUNT_KEY, operationCount+=1);
        if (operationCount == Constants.Ads.SHOW_AD_COUNT) {
            if (mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
                PreferenceUtils.savePref(this, Constants.Prefs.OPERATION_COUNT_KEY, 0);
            }
        }
    }

    /**
     * Complete a service action that modified the file tree
     * @param operationId
     */
    private void completeTreeModification(int operationId) {
        TreeNode.sortTree(
                SelectedFilesManager.getInstance().getActionableDirectory(operationId),
                ComparatorUtils.resolveComparator(this));

        for(SourceFragment fragment: mSourcesPagerAdapter.getFragments()) {
            fragment.refreshRecycler();
        }
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

            if (!getActiveFragment().getSource().isLoggedIn()) {
                showSnackbar(getString(R.string.err_not_logged_in));
                return;
            } else if (!getActiveFragment().getSource().isFilesLoaded()) {
                showSnackbar(getString(R.string.err_not_loaded));
                return;
            }

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
            dialog.show(getSupportFragmentManager(), Constants.DialogTags.CREATE_FOLDER);
        }
    }

    /**
     * Shows a dialog allowing the user to rename a file or folder
     */
    private void showRenameDialog() {
        if (getActiveFragment().getSource().checkConnectionActive(this)) {
            setTitle(getString(R.string.app_name));
            toggleFloatingMenu(false);

            for(SourceFragment fragment: mSourcesPagerAdapter.getFragments()) {
                fragment.setMultiSelectEnabled(false);
            }

            mSourceManager.addFileAction(
                    SelectedFilesManager.getInstance().getOperationCount(),
                    FileAction.RENAME);
            SelectedFilesManager.getInstance().addActionableDirectory(
                    SelectedFilesManager.getInstance().getOperationCount(),
                    mSourceManager.getActiveDirectory());

            int size = SelectedFilesManager.getInstance().getCurrentSelectedFiles().size();

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
            dialog.show(getSupportFragmentManager(), getString(R.string.rename));
        }
    }

    private void showCreateZipDialog() {
        if (getActiveFragment().getSource().checkConnectionActive(this)) {
            if (!getActiveFragment().getSource().isLoggedIn()) {
                showSnackbar(getString(R.string.err_not_logged_in));
                return;
            } else if (!getActiveFragment().getSource().isFilesLoaded()) {
                showSnackbar(getString(R.string.err_not_loaded));
                return;
            }

            setTitle(getString(R.string.app_name));
            toggleFloatingMenu(false);
            disableAllMultiSelect();

            mSourceManager.addFileAction(
                    SelectedFilesManager.getInstance().getOperationCount(),
                    FileAction.NEW_ZIP);
            SelectedFilesManager.getInstance().addActionableDirectory(
                    SelectedFilesManager.getInstance().getOperationCount(),
                    mSourceManager.getActiveDirectory());

            Bundle bundle = new Bundle();
            bundle.putString(Constants.DIALOG_TITLE_KEY, getString(R.string.create_zip));
            CreateZipDialog dialog = new CreateZipDialog();
            dialog.setArguments(bundle);
            dialog.show(getSupportFragmentManager(), getString(R.string.create_zip));
        }
    }

    /**
     * Shows a dialog displaying properties about the selected files and/or folders
     */
    private void showPropertiesDialog() {
        int size = SelectedFilesManager.getInstance().getCurrentSelectedFiles().size();
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
        int totalCount = intent.getIntExtra(EXTRA_DIALOG_MAX_VALUE, 0);
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

    private void showUsageDialog() {
        List<Source> sources = new ArrayList<>();
        for(SourceFragment fragment: mSourcesPagerAdapter.getFragments()) {
            if (fragment.getSource() != null && fragment.getSource().isFilesLoaded()) {
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

    private void showLogoutDialog() {
        List<Source> sources = new ArrayList<>();
        for(SourceFragment fragment: mSourcesPagerAdapter.getFragments()) {
            if (fragment.getSource().isFilesLoaded() &&
                    fragment.getSource().getSourceType() == SourceType.REMOTE) {
                sources.add(fragment.getSource());
            }
        }
        AlertDialog logoutDialog = new AlertDialog.Builder(this)
                .setNegativeButton(R.string.close, (dialog, which) -> dialog.dismiss())
                .create();

        SourceLogoutAdapter adapter = new SourceLogoutAdapter(sources, logoutDialog::dismiss);
        View view = getLayoutInflater().inflate(R.layout.dialog_source_logout, null);
        RecyclerView rv = view.findViewById(R.id.rv_source_logout);

        if (sources.isEmpty()) {
            logoutDialog.setMessage(getString(R.string.no_connected_sources));
        } else {
            rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            rv.setAdapter(adapter);
            logoutDialog.setView(view);
            logoutDialog.setTitle(R.string.dialog_title_logout);
        }

        logoutDialog.show();
    }

    private void showSortByDialog() {
        SortByDialog dialog = new SortByDialog();
        dialog.show(getSupportFragmentManager(), Constants.DialogTags.SORT_BY);
    }

    void showErrorDialog(String message) {
        if (mDialog != null && mDialog.isShowing()) mDialog.dismiss();

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_error)
                .setMessage(message)
                .setNegativeButton(R.string.close, (dialog, which) -> dialog.dismiss())
                .create()
                .show();
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
            int currentCount = intent.getIntExtra(EXTRA_DIALOG_CURRENT_VALUE, 0);
            mDialog.setProgress(currentCount);
        }
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
        try {
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
        } catch (Exception e) {
            showErrorDialog(String.format(
                    "%s %s %s",
                    getString(R.string.problem_encountered),
                    getString(R.string.opening_file),
                    ": "+e.getLocalizedMessage()));
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

    private void disableAllMultiSelect() {
        for (SourceFragment fragment: mSourcesPagerAdapter.getFragments()) {
            fragment.setMultiSelectEnabled(false);
        }
    }

    /**
     * Initializes recyclerviews for all logged in fragments due to a view layout change (Grid <-> List)
     */
    public void initAllRecyclers() {
        for (SourceFragment fragment: mSourcesPagerAdapter.getFragments()) {
            if (fragment != null && fragment.getSource() != null) {
                if (fragment.getSource().isFilesLoaded()) {
                    fragment.initRecyclerView();
                }
            } else {
                Log.e("RECYCLER_INIT", "Fragment or source was null.");
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

        if (getActiveFragment() != null && getActiveFragment().getSource() != null)
        if (getActiveFragment().getSource().isMultiSelectEnabled()) {

            getActiveFragment().setMultiSelectEnabled(false);
            toggleFloatingMenu(false);
            setTitle(R.string.app_name);

            SelectedFilesManager
                    .getInstance()
                    .getCurrentSelectedFiles()
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

            ((FileAdapter) getActiveFragment().mRecycler.getAdapter())
                    .setCurrentDirectory(activeDir.getParent());

            mSourceManager.setActiveDirectory(activeDir.getParent());

            getActiveFragment().refreshRecycler();
            getActiveFragment().popBreadcrumb();
        } else {
            super.onBackPressed();
        }
    }
}
