package com.jakebarnby.filemanager.ui.sources

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.app.SearchManager
import android.content.*
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.OvershootInterpolator
import android.view.animation.TranslateAnimation
import android.webkit.MimeTypeMap
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.material.internal.NavigationMenu
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.jakebarnby.filemanager.R
import com.jakebarnby.filemanager.managers.SelectedFilesManager
import com.jakebarnby.filemanager.services.SourceTransferService
import com.jakebarnby.filemanager.sources.local.LocalFragment
import com.jakebarnby.filemanager.models.Source
import com.jakebarnby.filemanager.models.SourceFile
import com.jakebarnby.filemanager.models.SourceConnectionType
import com.jakebarnby.filemanager.models.SourceType
import com.jakebarnby.filemanager.ui.adapters.*
import com.jakebarnby.filemanager.ui.adapters.SourceLogoutAdapter.LogoutListener
import com.jakebarnby.filemanager.ui.dialogs.*
import com.jakebarnby.filemanager.util.*
import com.jakebarnby.filemanager.util.Constants.ADS_MENU_ID
import com.jakebarnby.filemanager.util.Constants.ADS_MENU_POSITION
import com.jakebarnby.filemanager.util.Constants.DialogTags
import com.jakebarnby.filemanager.util.Constants.FILE_PATH_KEY
import com.jakebarnby.filemanager.util.Intents.ACTION_COMPLETE
import com.jakebarnby.filemanager.util.Intents.ACTION_SHOW_DIALOG
import com.jakebarnby.filemanager.util.Intents.ACTION_SHOW_ERROR
import com.jakebarnby.filemanager.util.Intents.ACTION_UPDATE_DIALOG
import com.jakebarnby.filemanager.util.Intents.EXTRA_DIALOG_CURRENT_VALUE
import com.jakebarnby.filemanager.util.Intents.EXTRA_DIALOG_MAX_VALUE
import com.jakebarnby.filemanager.util.Intents.EXTRA_DIALOG_MESSAGE
import com.jakebarnby.filemanager.util.Intents.EXTRA_DIALOG_TITLE
import dagger.android.support.DaggerAppCompatActivity
import io.github.yavski.fabspeeddial.FabSpeedDial
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter
import jp.wasabeef.blurry.Blurry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

class SourceActivity : DaggerAppCompatActivity(), SourceActivityContract.View, CoroutineScope {

    var job = Job()

    override val coroutineContext = job + Dispatchers.Main

    @Inject
    lateinit var presenter: SourceActivityContract.Presenter

    private lateinit var sourcesPagerAdapter: SourcePagerAdapter
    private lateinit var viewPager: ViewPager
    private lateinit var broadcastReceiver: BroadcastReceiver
    private lateinit var progressDialog: ProgressDialog
    private lateinit var contextMenuButton: FabSpeedDial
    private lateinit var contextMenu: NavigationMenu
    private lateinit var blurWrapper: ViewGroup
    private lateinit var interstitialAd: InterstitialAd

    var isCheckingPermissions = false

    override var menuListener = object : SimpleMenuListenerAdapter() {
        override fun onPrepareMenu(navigationMenu: NavigationMenu): Boolean {
            contextMenu = navigationMenu
            presenter.onPrepareContextMenu()
            return super.onPrepareMenu(navigationMenu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            clearBlur()
            handleFabMenuItemSelected(menuItem)
            return true
        }

        override fun onMenuClosed() {
            clearBlur()
            super.onMenuClosed()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_source)

        FirebaseCrashlytics
            .getInstance()
            .setCrashlyticsCollectionEnabled(true)

        setSupportActionBar(findViewById(R.id.toolbar))

        initAds()
        initViews()

        SourceTransferService.startClearLocalCache(this)
    }

    private fun initViews() {
        sourcesPagerAdapter = SourcePagerAdapter(supportFragmentManager)
        viewPager = findViewById(R.id.view_pager)
        blurWrapper = findViewById(R.id.wrapper)
        viewPager.adapter = sourcesPagerAdapter
        viewPager.addOnPageChangeListener(this)
        viewPager.offscreenPageLimit = sourcesPagerAdapter.count - 1
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                handleIntent(intent)
            }
        }
        contextMenuButton = findViewById(R.id.fab_speed_dial)
        contextMenuButton.setMenuListener(menuListener)

        findViewById<TabLayout>(R.id.tabs)
            ?.setupWithViewPager(viewPager)

        presenter.onAddLocalSources(
            Utils.getExternalStorageDirectories(applicationContext),
            getString(R.string.sdcard),
            getString(R.string.usb)
        )
    }

    private fun initAds() {
        interstitialAd = InterstitialAd(this)
        interstitialAd.adUnitId = "ca-app-pub-3940256099942544/1033173712"
        interstitialAd.loadAd(AdRequest.Builder().build())
        interstitialAd.adListener = object : AdListener() {
            override fun onAdClosed() {
                interstitialAd.loadAd(AdRequest.Builder().build())
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val filterLocal = IntentFilter().apply {
            addAction(Intents.ACTION_SHOW_DIALOG)
            addAction(Intents.ACTION_UPDATE_DIALOG)
            addAction(Intents.ACTION_SHOW_ERROR)
            addAction(Intents.ACTION_COMPLETE)
        }
        val filterSystem = IntentFilter().apply {
            addAction(Intent.ACTION_MEDIA_MOUNTED)
            addAction(Intent.ACTION_MEDIA_REMOVED)
            addAction(Intent.ACTION_MEDIA_UNMOUNTED)
            addAction(Intent.ACTION_MEDIA_BAD_REMOVAL)
            addDataScheme("file")
        }

        applicationContext.registerReceiver(broadcastReceiver, filterLocal)
        applicationContext.registerReceiver(broadcastReceiver, filterSystem)
    }

    override fun onPause() {
        applicationContext.unregisterReceiver(broadcastReceiver)
        super.onPause()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
        presenter.onSourceSelected(position)
    }

    override fun onPageScrollStateChanged(state: Int) {
    }

    override fun onBackPressed() {
        presenter.onBack()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_source, menu)

        if (!presenter.prefsManager.getBoolean(Constants.Prefs.HIDE_ADS_KEY, false)) {
            menu.add(Menu.NONE, ADS_MENU_ID, ADS_MENU_POSITION, R.string.action_remove_ads)
        }

        val searchManager =
            getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView =
            menu.findItem(R.id.action_search).actionView as SearchView

        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView.setOnQueryTextListener(this)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_view_as -> presenter.onChangeViewType()
            R.id.action_sort_by -> presenter.onSortBy()
            R.id.action_new_folder -> presenter.onCreateFolder()
            R.id.action_multi_select -> presenter.onStartMultiSelect()
            R.id.action_storage_usage -> presenter.onShowUsage()
            R.id.action_logout -> presenter.onLogout()
            R.id.action_settings -> presenter.onShowSettings()
            ADS_MENU_ID -> {
                launch {
                    presenter.billingManager.purchaseItem(
                        this@SourceActivity,
                        Constants.Billing.SKU_PREMIUM
                    )
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun startCreateFolderService(name: String) {
        SourceTransferService.startActionCreateFolder(this, name)
    }

    override fun startRenameService(newName: String) {
        SourceTransferService.startActionRename(this, newName)
    }

    override fun startDeleteService() {
        SourceTransferService.startActionDelete(this)
    }

    override fun startCopyService() {
        SourceTransferService.startActionCopy(this, false)
    }

    override fun startMoveService() {
        SourceTransferService.startActionCopy(this, true)
    }

    override fun startZipService(name: String) {
        SourceTransferService.Companion.startActionZip(this, name)
    }

    override fun addLocalSourceView(position: Int, name: String, rootPath: String) {
        sourcesPagerAdapter.fragments.add(
            position,
            // TODO: Fix additional local source ids
            LocalFragment.newInstance(5, rootPath)
        )
        sourcesPagerAdapter.notifyDataSetChanged()
    }

    override fun changeToSource(position: Int) {
        viewPager.setCurrentItem(position, true)
    }

    override fun removeLocalSourceView(position: Int) {
        sourcesPagerAdapter.fragments.removeAt(position)
        sourcesPagerAdapter.notifyDataSetChanged()
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String): Boolean {
        return false
    }

    /**
     * Handles menu clicks from the fab action menu
     * @param menuItem The selected item
     */
    private fun handleFabMenuItemSelected(menuItem: MenuItem) {
        when (menuItem.itemId) {
            R.id.action_rename -> presenter.onRename()
            R.id.action_copy -> presenter.onCopy()
            R.id.action_cut -> presenter.onCut()
            R.id.action_paste -> presenter.onPaste()
            R.id.action_zip -> presenter.onCreateZip()
            R.id.action_properties -> presenter.onShowProperties()
            R.id.action_delete -> presenter.onDelete()
        }
    }

    /**
     * Called when a broadcasted intent is recieved
     * @param intent The broadcasted intent
     */
    private fun handleIntent(intent: Intent) {
        val action = intent.action
        if (action != null) {
            when (action) {
                ACTION_COMPLETE -> {
                    val operationId = intent.getIntExtra(Intents.EXTRA_OPERATION_ID, 0)
                    val path = intent.getStringExtra(FILE_PATH_KEY)

                    presenter.onServiceActionComplete(operationId, path)
                }
                ACTION_SHOW_DIALOG -> {
                    val title = intent.getStringExtra(EXTRA_DIALOG_TITLE)
                        ?: getString(R.string.operation_in_progress)
                    val maxCount = intent.getIntExtra(EXTRA_DIALOG_MAX_VALUE, 0)
                    showProgressDialog(title, maxCount)
                }
                ACTION_SHOW_ERROR -> {
                    showErrorDialog(intent.getStringExtra(EXTRA_DIALOG_MESSAGE))
                }
                ACTION_UPDATE_DIALOG -> {
                    val currentCount = intent.getIntExtra(EXTRA_DIALOG_CURRENT_VALUE, 0)
                    updateProgressDialog(currentCount)
                }
                Intent.ACTION_MEDIA_MOUNTED -> {
                    presenter.onAddLocalSource(
                        intent.dataString!!.replace("file://", ""),
                        presenter.sourceManager.sources.count {
                            it.sourceConnectionType == SourceConnectionType.LOCAL
                        },
                        getString(R.string.sdcard),
                        getString(R.string.usb)
                    )
                }
                UsbManager.ACTION_USB_DEVICE_DETACHED,
                Intent.ACTION_MEDIA_BAD_REMOVAL,
                Intent.ACTION_MEDIA_REMOVED -> {
                    presenter.onRemoveLocalSource(intent.dataString!!)
                }
                Intent.ACTION_SEARCH -> {
                    presenter.onSearch(intent.getStringExtra(SearchManager.QUERY) ?: "")
                }
            }
        }
    }

    override fun showAd() {
        if (interstitialAd.isLoaded) {
            interstitialAd.show()
            presenter.prefsManager.savePref(Constants.Prefs.OPERATION_COUNT_KEY, 0)
        }
    }

    override fun showViewAsDialog() {
        ViewAsDialog().show(supportFragmentManager, "ViewAs")
    }

    override fun showCreateFolderDialog() {
        CreateFolderDialog.newInstance {
            presenter.createFolder(it)
        }.show(supportFragmentManager, DialogTags.CREATE_FOLDER)
    }

    /**
     * Shows a dialog allowing the user to rename a file or folder
     */
    override fun showRenameDialog(currentFileName: String) {
        RenameDialog.newInstance(currentFileName) {
            presenter.rename(currentFileName, it)
        }.show(supportFragmentManager, getString(R.string.rename))
    }

    override fun showDeleteDialog(deleteCount: Int) {
        DeleteDialog.newInstance(deleteCount) {
            presenter.deleteFiles()
        }.show(supportFragmentManager, DialogTags.DELETE)
    }

    override fun showCreateZipDialog() {
        CreateZipDialog.newInstance {
            presenter.zip(it)
        }.show(supportFragmentManager, getString(R.string.create_zip))
    }

    override fun showPropertiesDialog(selectedCount: Int, totalSize: Int) {
        PropertiesDialog.newInstance(selectedCount, totalSize)
            .show(supportFragmentManager, DialogTags.PROPERTIES)
    }

    override fun showProgressDialog(title: String, maxProgress: Int) {
        val currentCount = 0

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle(title)
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        progressDialog.isIndeterminate = true
        if (maxProgress != 0) {
            progressDialog.max = maxProgress
            progressDialog.progress = currentCount
        } else {
            progressDialog.setProgressNumberFormat(null)
            progressDialog.setProgressPercentFormat(null)
        }
        progressDialog.setButton(
            DialogInterface.BUTTON_NEGATIVE,
            getString(R.string.cancel)
        ) { _, _ ->
            // TODO: Check if this stops ALL operations
            stopService(Intent(this, SourceTransferService::class.java))
        }
        progressDialog.setButton(
            DialogInterface.BUTTON_POSITIVE,
            getString(R.string.background)
        ) { dialog, _ -> dialog.dismiss() }

        progressDialog.show()
    }

    override fun isProgressShowing() = progressDialog.isShowing

    override fun hideProgressDialog() {
        if (progressDialog.isShowing) {
            progressDialog.dismiss()
        }
    }

    override fun showUsageDialog(loadedSources: List<Source>) {
        val adapter = SourceUsageAdapter(loadedSources)
        val view = layoutInflater.inflate(R.layout.dialog_source_usage, null)
        val rv: RecyclerView = view.findViewById(R.id.rv_source_usage)

        rv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rv.adapter = adapter

        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_title_usage)
            .setNegativeButton(R.string.close) { dialog: DialogInterface, _ -> dialog.dismiss() }
            .setView(view)
            .create()
            .show()
    }

    override fun showLogoutDialog(loggedInSources: List<Source>) {
        val logoutDialog = AlertDialog
            .Builder(this)
            .setNegativeButton(R.string.close) { dialog, _ -> dialog.dismiss() }
            .create()

        val adapter = SourceLogoutAdapter(loggedInSources.toMutableList(), object : LogoutListener {
            override fun onLastLogout() {
                logoutDialog.dismiss()
            }
        })

        val view = layoutInflater.inflate(R.layout.dialog_source_logout, null)
        val rv = view.findViewById<RecyclerView>(R.id.rv_source_logout)

        if (loggedInSources.isEmpty()) {
            logoutDialog.setMessage(getString(R.string.no_connected_sources))
        } else {
            rv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            rv.adapter = adapter
            logoutDialog.setView(view)
            logoutDialog.setTitle(R.string.dialog_title_logout)
        }
        logoutDialog.show()
    }

    override fun showSortByDialog() {
        SortByDialog.newInstance {
            //initAllRecyclers()
        }.show(supportFragmentManager, DialogTags.SORT_BY)
    }

    /**
     * Show the settings dialog
     */
    override fun showSettingsDialog() {
        SettingsDialog.newInstance {
            //initAllRecyclers()
        }.show(supportFragmentManager, DialogTags.SETTINGS)
    }

    private fun showErrorDialog(message: String?) {
        hideProgressDialog()
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_error)
            .setMessage(message)
            .setNegativeButton(R.string.close) { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    override fun showSearchDialog(
        results: List<TreeNode<SourceFile>>,
        sourceNames: List<String>
    ) {
        val searchDialog = AlertDialog.Builder(this)
            .setNegativeButton(R.string.close) { dialog, _ -> dialog.dismiss() }
            .create()

        val adapter = SearchResultAdapter(results.toMutableList(), object : OnSearchResultClicked {
            override fun navigateToFile(fileNode: TreeNode<SourceFile>) {
                searchDialog.dismiss()
                presenter.onNavigateToFile(fileNode)
            }
        })

        val view = layoutInflater.inflate(R.layout.dialog_search_results, null)
        val rv: RecyclerView = view.findViewById(R.id.rv_search_results)

        if (results.isEmpty()) {
            searchDialog.setMessage(getString(R.string.no_results))
        } else {
            rv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            rv.adapter = adapter
            searchDialog.setTitle(R.string.search_results)
            searchDialog.setView(view)
        }

        val arrayAdapter =
            ArrayAdapter<String?>(this, android.R.layout.simple_spinner_item).apply {
                add(Constants.ALL)
                addAll(sourceNames)
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }

        val spn = view.findViewById<Spinner>(R.id.spn_sources)
        spn.adapter = arrayAdapter
        spn.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                val selected = adapterView.getItemAtPosition(i) as String
                if (selected == Constants.ALL) {
                    adapter.resetDataset()
                    adapter.notifyDataSetChanged()
                } else {
                    adapter.removeAllSourceExcept(SourceType.valueOf(selected).id)
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }
        searchDialog.show()
    }

    /**
     * Update the progress of the [ProgressDialog] if it is showing
     *
     * @param intent The broadcasted intent with update extras
     */
    override fun updateProgressDialog(newProgress: Int) {
        if (progressDialog.isShowing) {
            if (progressDialog.isIndeterminate) {
                progressDialog.isIndeterminate = false
            }
            progressDialog.progress = newProgress
        }
    }

    override fun blurFileList() {
        Blurry.with(this)
            .radius(17)
            .sampling(1)
            .async()
            .animate(300)
            .onto(blurWrapper)
    }

    override fun clearBlur() {
        Blurry.delete(blurWrapper)
    }

    @SuppressLint("RestrictedApi")
    override fun hidePasteMenuItem() {
        contextMenu.findItem(R.id.action_paste).isVisible = false
    }

    @SuppressLint("RestrictedApi")
    override fun hideRenameMenuItem() {
        contextMenu.findItem(R.id.action_rename).isVisible = false
    }

    override fun showOpeningDialog(path: String) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.file_ready))
            .setMessage(String.format(getString(R.string.open_now), path))
            .setNegativeButton(getString(R.string.no)) { dialog: DialogInterface, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                viewFileInExternalApp(path)
            }
            .create()
            .show()
    }

    /**
     * Attempts to open a file by finding it's mimetype then opening a compatible application
     * @param path  The absolute path of the file to open
     */
    override fun viewFileInExternalApp(path: String) {
        try {
            val file = File(path)
            val extension = Utils.fileExt(path)
            val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            val reachableUri = FileProvider.getUriForFile(this, applicationInfo.packageName, file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(reachableUri, mimeType)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            val resolveInfo = packageManager.queryIntentActivities(intent, 0)
            if (resolveInfo.size > 0) {
                startActivity(intent)
            } else {
                showNoAppAvailableSnackBar()
            }
        } catch (e: Exception) {
            showErrorDialog(String.format(
                "%s %s %s",
                getString(R.string.problem_encountered),
                getString(R.string.opening_file),
                ": " + e.localizedMessage)
            )
        }
    }

    override fun toggleContextMenu(enabled: Boolean) {
        if ((!enabled && contextMenuButton.visibility == View.INVISIBLE) ||
            (enabled && contextMenuButton.visibility == View.VISIBLE)) {
            return
        }

        if (enabled) {
            contextMenuButton.visibility = View.VISIBLE
        }

        val screenHeight = Utils.getScreenHeight(this).toFloat()

        val translate = TranslateAnimation(
            0.0f,
            0.0f,
            if (enabled) screenHeight else 0.0f,
            if (enabled) 0.0f else screenHeight
        )

        if (enabled) {
            translate.interpolator = OvershootInterpolator(0.55f)
        } else {
            translate.interpolator = AccelerateInterpolator(2.0f)
        }

        translate.duration = 400
        translate.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                if (!enabled) contextMenuButton.visibility = View.INVISIBLE
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
        contextMenuButton.startAnimation(translate)
    }

    override fun setAppNameTitle() {
        title = getString(R.string.app_name)
    }

    override fun refreshFileLists(operationId: Int) {
//        val sortRoot = SelectedFilesManager.getActionableDirectory(operationId) ?: return
//        TreeNode.sortTree(
//            sortRoot,
//            Comparators.resolveComparatorForPrefs(presenter.prefsManager)
//        )
//        for (fragment in sourcesPagerAdapter.fragments) {
//            fragment.refreshRecycler()
//        }
    }

    override fun popAllBreadCrumbs() {
        sourcesPagerAdapter.fragments[viewPager.currentItem].apply {
            popAllBreadCrumbs()

            recycler?.adapter?.notifyDataSetChanged()
            recycler?.requestFocus()
        }
    }

    override fun pushAllBreadCrumbs(newDir: TreeNode<SourceFile>) {
        sourcesPagerAdapter.fragments[viewPager.currentItem].apply {
            pushAllBreadCrumbs(newDir)

            recycler?.adapter?.notifyDataSetChanged()
            recycler?.requestFocus()
        }
    }

    override fun showCutSnackBar() =
        Snackbar.make(viewPager, R.string.cut, Snackbar.LENGTH_LONG).show()

    override fun showNoSelectionSnackBar() =
        Snackbar.make(viewPager, R.string.err_no_selection, Snackbar.LENGTH_LONG).show()

    override fun showCopiedSnackBar() =
        Snackbar.make(viewPager, R.string.copied, Snackbar.LENGTH_LONG).show()

    override fun showNoConnectionSnackBar() =
        Snackbar.make(viewPager, R.string.err_no_connection, Snackbar.LENGTH_LONG).show()

    override fun showUnwritableDestinationSnackBar() =
        Snackbar.make(viewPager, R.string.err_no_ext_write, Snackbar.LENGTH_LONG).show()

    override fun showNotLoggedInSnackBar() =
        Snackbar.make(viewPager, R.string.err_not_logged_in, Snackbar.LENGTH_LONG).show()

    override fun showNotLoadedSnackBar() =
        Snackbar.make(viewPager, R.string.err_not_loaded, Snackbar.LENGTH_LONG).show()

    override fun showNotEnoughSpaceSnackBar() =
        Snackbar.make(viewPager, R.string.err_no_free_space, Snackbar.LENGTH_LONG).show()

    override fun showTooManySelectedSnackBar() =
        Snackbar.make(viewPager, R.string.err_too_many_selected, Snackbar.LENGTH_LONG).show()

    override fun showNoAppAvailableSnackBar() =
        Snackbar.make(viewPager, R.string.err_no_app_available, Snackbar.LENGTH_LONG).show()

    override fun showFileExistsSnackBar() {
        Snackbar.make(viewPager, R.string.folder_exists, Snackbar.LENGTH_LONG).show()
    }
}